package com.bookhub.service;

import com.bookhub.config.JpaUtil;
import com.bookhub.controller.site.LogoutServlet;
import com.bookhub.dto.SessionUser;
import com.bookhub.dto.UserProfileDTO;
import com.bookhub.entity.PasswordResetToken;
import com.bookhub.entity.RoleCode;
import com.bookhub.entity.User;
import com.bookhub.exception.BusinessException;
import com.bookhub.repository.PasswordResetTokenRepository;
import com.bookhub.repository.RoleRepository;
import com.bookhub.repository.UserRepository;
import com.bookhub.util.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for 03-feat-auth-account (AC-01..AC-10) against live SQL Server BookHubDB.
 */
class AuthAccountIntegrationTest {

    private final UserRepository userRepo = new UserRepository();
    private final RoleRepository roleRepo = new RoleRepository();
    private final PasswordResetTokenRepository tokenRepo = new PasswordResetTokenRepository();
    private final UserService userService = new UserService();

    @BeforeAll
    static void init() {
        JpaUtil.init();
    }

    @AfterAll
    static void close() {
        JpaUtil.shutdown();
    }

    // @spec auth-account/AC-01
    // @spec auth-account/AC-02
    // @spec auth-account/AC-03
    // @spec auth-account/AC-04
    @Test
    void registerNormalizeEmailDuplicateWeakPasswordAndLoginActiveOrLocked() {
        AuthService authService = new AuthService(userRepo, roleRepo, tokenRepo, (e, u) -> {});
        String marker = UUID.randomUUID().toString().substring(0, 8);
        String rawEmail = "  Test." + marker + "@BookHub.VN  ";
        String expectedEmail = "test." + marker + "@bookhub.vn";
        Long createdUserId = null;

        try {
            // AC-02: weak password rejected
            BusinessException weakEx = assertThrows(
                    BusinessException.class,
                    () -> authService.register("Test User", rawEmail, null, "1234567", "1234567"));
            assertTrue(weakEx.getFieldErrors().containsKey("password"));

            // AC-01: valid registration normalizes email and stores BCrypt hash + CUSTOMER/ACTIVE
            SessionUser reg = authService.register("Nguyễn Test " + marker, rawEmail, "0912345678", "Pass1234", "Pass1234");
            createdUserId = reg.getUserId();
            assertEquals(expectedEmail, reg.getEmail());
            assertEquals(RoleCode.CUSTOMER, reg.getRoleCode());

            // AC-02: duplicate email (case-insensitive) rejected
            BusinessException dupEx = assertThrows(
                    BusinessException.class,
                    () -> authService.register("Other", expectedEmail.toUpperCase(), null, "Pass1234", "Pass1234"));
            assertTrue(dupEx.getFieldErrors().containsKey("email"));

            // AC-03: login succeeds for ACTIVE customer and seeded ADMIN
            SessionUser loggedIn = authService.authenticate(rawEmail, "Pass1234");
            assertEquals(createdUserId, loggedIn.getUserId());
            SessionUser admin = authService.authenticate("admin@bookhub.vn", "Admin@123");
            assertEquals(RoleCode.ADMIN, admin.getRoleCode());

            // AC-04: wrong password & unknown email return generic message; LOCKED user returns locked message ONLY on valid password
            BusinessException wrongPwd = assertThrows(
                    BusinessException.class, () -> authService.authenticate(expectedEmail, "WrongPass9"));
            assertEquals("Email hoặc mật khẩu không đúng.", wrongPwd.getMessage());

            BusinessException lockedWrongPwd = assertThrows(
                    BusinessException.class, () -> authService.authenticate("locked.user@bookhub.vn", "WrongPass9"));
            assertEquals("Email hoặc mật khẩu không đúng.", lockedWrongPwd.getMessage());

            BusinessException lockedRightPwd = assertThrows(
                    BusinessException.class, () -> authService.authenticate("locked.user@bookhub.vn", "Customer@123"));
            assertEquals("Tài khoản của bạn đã bị khóa.", lockedRightPwd.getMessage());
        } finally {
            deleteTestUser(createdUserId);
        }
    }

    // @spec auth-account/AC-05
    @Test
    void logoutInvalidatesSessionAndRedirectsHome() throws Exception {
        LogoutServlet servlet = new LogoutServlet();
        AtomicBoolean invalidated = new AtomicBoolean(false);
        AtomicReference<String> redirect = new AtomicReference<>();

        HttpSession session = (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(), new Class<?>[]{HttpSession.class},
                (p, m, a) -> {
                    if ("invalidate".equals(m.getName())) {
                        invalidated.set(true);
                    }
                    return null;
                });
        HttpServletRequest req = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(), new Class<?>[]{HttpServletRequest.class},
                (p, m, a) -> {
                    if ("getSession".equals(m.getName())) return session;
                    if ("getContextPath".equals(m.getName())) return "/BookHub";
                    if ("getMethod".equals(m.getName())) return "POST";
                    return null;
                });
        HttpServletResponse resp = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(), new Class<?>[]{HttpServletResponse.class},
                (p, m, a) -> {
                    if ("sendRedirect".equals(m.getName())) redirect.set((String) a[0]);
                    return null;
                });

        servlet.service(req, resp);
        assertTrue(invalidated.get());
        assertEquals("/BookHub/", redirect.get());
    }

    // @spec auth-account/AC-06
    // @spec auth-account/AC-07
    // @spec auth-account/AC-08
    @Test
    void forgotAndResetPasswordEnforceSha256OneTimeUseAndExpiry() {
        AtomicReference<String> capturedUrl = new AtomicReference<>();
        AuthService authService = new AuthService(userRepo, roleRepo, tokenRepo, (e, u) -> capturedUrl.set(u));
        String email = "reset." + UUID.randomUUID().toString().substring(0, 8) + "@bookhub.vn";
        Long userId = null;

        try {
            SessionUser user = authService.register("Reset Tester", email, null, "OldPass123", "OldPass123");
            userId = user.getUserId();

            // AC-06: non-existent email produces empty token without error
            assertTrue(authService.requestPasswordReset("nobody@bookhub.vn", "http://localhost:8080/BookHub").isEmpty());

            // AC-06: active email produces opaque token stored as SHA-256 hash
            Optional<String> rawToken1 = authService.requestPasswordReset(email, "http://localhost:8080/BookHub");
            Optional<String> rawToken2 = authService.requestPasswordReset(email, "http://localhost:8080/BookHub");
            assertTrue(rawToken1.isPresent());
            assertTrue(rawToken2.isPresent());
            assertNotNull(capturedUrl.get());
            assertTrue(authService.isResetTokenValid(rawToken1.get()));

            // AC-07: reset password updates BCrypt hash, marks token used, and invalidates other open tokens
            authService.resetPassword(rawToken1.get(), "NewPass456", "NewPass456");
            assertFalse(authService.isResetTokenValid(rawToken1.get()));
            assertFalse(authService.isResetTokenValid(rawToken2.get()), "Other open tokens must be invalidated");
            assertEquals(userId, authService.authenticate(email, "NewPass456").getUserId());

            // AC-08: reusing already-used token or unknown token is rejected
            assertThrows(BusinessException.class, () -> authService.resetPassword(rawToken1.get(), "Another999", "Another999"));
            assertThrows(BusinessException.class, () -> authService.resetPassword("invalid-token", "Another999", "Another999"));

            // AC-08: expired token rejected
            String expiredRaw = PasswordUtil.generateOpaqueToken();
            Long uid = userId;
            JpaUtil.executeInTransactionVoid(em -> {
                PasswordResetToken expired = new PasswordResetToken();
                expired.setUser(em.getReference(User.class, uid));
                expired.setTokenHash(PasswordUtil.sha256Hex(expiredRaw));
                expired.setExpiresAt(LocalDateTime.now().minusMinutes(5));
                tokenRepo.persist(em, expired);
            });
            assertFalse(authService.isResetTokenValid(expiredRaw));
            assertThrows(BusinessException.class, () -> authService.resetPassword(expiredRaw, "Another999", "Another999"));
        } finally {
            deleteTestUser(userId);
        }
    }

    // @spec auth-account/AC-09
    // @spec auth-account/AC-10
    @Test
    void profileUpdateAllowListAndChangePasswordInvalidateOpenTokens() {
        AuthService authService = new AuthService(userRepo, roleRepo, tokenRepo, (e, u) -> {});
        String email = "profile." + UUID.randomUUID().toString().substring(0, 8) + "@bookhub.vn";
        Long userId = null;

        try {
            SessionUser user = authService.register("Profile Old", email, "0900111222", "InitPass1", "InitPass1");
            userId = user.getUserId();

            // AC-09: only fullName and phone update; email and role stay immutable
            SessionUser updated = userService.updateProfile(userId, "Profile New", "0988777666");
            assertEquals("Profile New", updated.getFullName());
            assertEquals(email, updated.getEmail());
            assertEquals(RoleCode.CUSTOMER, updated.getRoleCode());
            UserProfileDTO dto = userService.getProfile(userId);
            assertEquals("0988777666", dto.getPhone());

            // AC-10: wrong current password rejected; valid change updates hash and invalidates open reset tokens
            Optional<String> pendingToken = authService.requestPasswordReset(email, "http://localhost:8080/BookHub");
            assertTrue(pendingToken.isPresent());
            assertTrue(authService.isResetTokenValid(pendingToken.get()));

            Long uid = userId;
            assertThrows(BusinessException.class, () -> userService.changePassword(uid, "WrongCur1", "NextPass2", "NextPass2"));
            userService.changePassword(userId, "InitPass1", "NextPass2", "NextPass2");

            assertFalse(authService.isResetTokenValid(pendingToken.get()), "Pending reset token must be invalidated");
            assertEquals(userId, authService.authenticate(email, "NextPass2").getUserId());
        } finally {
            deleteTestUser(userId);
        }
    }

    private void deleteTestUser(Long userId) {
        if (userId == null) {
            return;
        }
        JpaUtil.executeInTransactionVoid(em -> {
            em.createQuery("DELETE FROM PasswordResetToken t WHERE t.user.userId = :uid")
                    .setParameter("uid", userId).executeUpdate();
            em.createQuery("DELETE FROM User u WHERE u.userId = :uid")
                    .setParameter("uid", userId).executeUpdate();
        });
    }
}
