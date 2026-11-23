package com.bookhub.service;

import com.bookhub.config.AppConstants;
import com.bookhub.config.JpaUtil;
import com.bookhub.dto.SessionUser;
import com.bookhub.entity.PasswordResetToken;
import com.bookhub.entity.Role;
import com.bookhub.entity.RoleCode;
import com.bookhub.entity.User;
import com.bookhub.entity.UserStatus;
import com.bookhub.exception.BusinessException;
import com.bookhub.repository.PasswordResetTokenRepository;
import com.bookhub.repository.RoleRepository;
import com.bookhub.repository.UserRepository;
import com.bookhub.util.PasswordUtil;
import com.bookhub.util.UrlSafeUtil;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles registration, login authentication, and one-time password reset flows (03-feat-auth-account).
 */
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\-\\s()]{8,20}$");

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final ResetLinkNotifier resetLinkNotifier;

    public AuthService() {
        this(new UserRepository(), new RoleRepository(),
                new PasswordResetTokenRepository(), new LocalLogResetLinkNotifier());
    }

    public AuthService(UserRepository userRepo, RoleRepository roleRepo,
            PasswordResetTokenRepository tokenRepo, ResetLinkNotifier resetLinkNotifier) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.tokenRepo = tokenRepo;
        this.resetLinkNotifier = resetLinkNotifier;
    }

    public SessionUser register(String fullName, String email, String phone, String password, String confirmPassword) {
        String cleanName = trimToEmpty(fullName);
        String normalizedEmail = UrlSafeUtil.normalizeEmail(email);
        String cleanPhone = trimToEmpty(phone);

        Map<String, String> errors = new LinkedHashMap<>();
        if (cleanName.isEmpty() || cleanName.length() > 100) {
            errors.put("fullName", "Họ và tên là bắt buộc (tối đa 100 ký tự).");
        }
        if (normalizedEmail.isEmpty() || normalizedEmail.length() > 255 || !EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            errors.put("email", "Vui lòng nhập địa chỉ email hợp lệ.");
        }
        if (!cleanPhone.isEmpty() && !PHONE_PATTERN.matcher(cleanPhone).matches()) {
            errors.put("phone", "Số điện thoại không hợp lệ (8–20 ký tự số).");
        }
        if (!PasswordUtil.meetsPolicy(password)) {
            errors.put("password", "Mật khẩu phải có tối thiểu 8 ký tự, gồm cả chữ và số.");
        }
        if (password == null || !password.equals(confirmPassword)) {
            errors.put("confirmPassword", "Xác nhận mật khẩu không khớp.");
        }
        if (!errors.isEmpty()) {
            throw new BusinessException("Vui lòng kiểm tra lại thông tin đăng ký.", errors);
        }

        return JpaUtil.executeInTransaction(em -> {
            if (userRepo.existsByEmail(em, normalizedEmail)) {
                Map<String, String> dup = new LinkedHashMap<>();
                dup.put("email", "Email này đã được đăng ký trong hệ thống.");
                throw new BusinessException("Email đã tồn tại.", dup);
            }
            Role customerRole = roleRepo.findByCode(em, RoleCode.CUSTOMER)
                    .orElseThrow(() -> new IllegalStateException("Role CUSTOMER is missing from dbo.Roles"));

            User user = new User();
            user.setRole(customerRole);
            user.setFullName(cleanName);
            user.setEmail(normalizedEmail);
            user.setPhone(cleanPhone.isEmpty() ? null : cleanPhone);
            user.setPasswordHash(PasswordUtil.hash(password));
            user.setStatus(UserStatus.ACTIVE);
            userRepo.persist(em, user);
            em.flush();

            LOGGER.info("Registered new customer userId={}", user.getUserId());
            return toSessionUser(user);
        });
    }

    public SessionUser authenticate(String email, String password) {
        String normalizedEmail = UrlSafeUtil.normalizeEmail(email);
        if (normalizedEmail.isEmpty() || password == null || password.isEmpty()) {
            throw new BusinessException("Email hoặc mật khẩu không đúng.");
        }

        return JpaUtil.executeReadOnly(em -> {
            User user = userRepo.findByEmail(em, normalizedEmail)
                    .orElseThrow(() -> new BusinessException("Email hoặc mật khẩu không đúng."));

            // Gate 1 Q2: verify BCrypt password first so LOCKED status is never disclosed on wrong password
            if (!PasswordUtil.verify(password, user.getPasswordHash())) {
                throw new BusinessException("Email hoặc mật khẩu không đúng.");
            }
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new BusinessException("Tài khoản của bạn đã bị khóa.");
            }
            LOGGER.info("Authenticated userId={} role={}", user.getUserId(), user.getRole().getRoleCode());
            return toSessionUser(user);
        });
    }

    public Optional<String> requestPasswordReset(String email, String appBaseUrl) {
        String normalizedEmail = UrlSafeUtil.normalizeEmail(email);
        if (normalizedEmail.isEmpty() || !EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            Map<String, String> errors = new LinkedHashMap<>();
            errors.put("email", "Vui lòng nhập địa chỉ email hợp lệ.");
            throw new BusinessException("Địa chỉ email không hợp lệ.", errors);
        }

        int ttlMinutes = JpaUtil.isInitialized()
                ? JpaUtil.getConfig().getResetTokenTtlMinutes()
                : AppConstants.DEFAULT_RESET_TOKEN_TTL_MINUTES;

        Optional<String> rawTokenOpt = JpaUtil.executeInTransaction(em -> {
            Optional<User> userOpt = userRepo.findByEmail(em, normalizedEmail);
            if (userOpt.isEmpty() || userOpt.get().getStatus() != UserStatus.ACTIVE) {
                return Optional.empty();
            }
            User user = userOpt.get();
            LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            String rawToken = PasswordUtil.generateOpaqueToken();

            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setTokenHash(PasswordUtil.sha256Hex(rawToken));
            token.setCreatedAt(now);
            token.setExpiresAt(now.plusMinutes(ttlMinutes));
            tokenRepo.persist(em, token);
            return Optional.of(rawToken);
        });

        rawTokenOpt.ifPresent(rawToken -> {
            String base = (appBaseUrl == null || appBaseUrl.isEmpty())
                    ? AppConstants.DEFAULT_BASE_URL
                    : appBaseUrl.replaceAll("/+$", "");
            String resetUrl = base + "/reset-password?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
            resetLinkNotifier.notifyResetLink(normalizedEmail, resetUrl);
        });
        return rawTokenOpt;
    }

    public boolean isResetTokenValid(String rawToken) {
        if (rawToken == null || rawToken.trim().isEmpty()) {
            return false;
        }
        String hash = PasswordUtil.sha256Hex(rawToken.trim());
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        return JpaUtil.executeReadOnly(em -> tokenRepo.findByTokenHash(em, hash)
                .map(t -> t.getUsedAt() == null && t.getExpiresAt().isAfter(now))
                .orElse(false));
    }

    public void resetPassword(String rawToken, String newPassword, String confirmPassword) {
        if (rawToken == null || rawToken.trim().isEmpty()) {
            throw new BusinessException("Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
        }
        Map<String, String> errors = new LinkedHashMap<>();
        if (!PasswordUtil.meetsPolicy(newPassword)) {
            errors.put("newPassword", "Mật khẩu mới phải có tối thiểu 8 ký tự, gồm cả chữ và số.");
        }
        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            errors.put("confirmPassword", "Xác nhận mật khẩu mới không khớp.");
        }
        if (!errors.isEmpty()) {
            throw new BusinessException("Vui lòng kiểm tra lại mật khẩu mới.", errors);
        }

        String hash = PasswordUtil.sha256Hex(rawToken.trim());
        JpaUtil.executeInTransactionVoid(em -> {
            LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            PasswordResetToken token = tokenRepo.findByTokenHash(em, hash)
                    .orElseThrow(() -> new BusinessException("Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn."));

            int updated = tokenRepo.markUsedAtomically(em, token.getResetId(), now);
            if (updated != 1) {
                throw new BusinessException("Liên kết đặt lại mật khẩu đã được sử dụng hoặc đã hết hạn.");
            }
            User user = token.getUser();
            user.setPasswordHash(PasswordUtil.hash(newPassword));
            tokenRepo.invalidateAllOpenForUser(em, user.getUserId(), now);
            LOGGER.info("Reset password completed for userId={}", user.getUserId());
        });
    }

    private static SessionUser toSessionUser(User user) {
        return new SessionUser(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().getRoleCode());
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
