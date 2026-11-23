package com.bookhub.service;

import com.bookhub.config.JpaUtil;
import com.bookhub.dto.SessionUser;
import com.bookhub.dto.UserProfileDTO;
import com.bookhub.entity.User;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.NotFoundException;
import com.bookhub.repository.PasswordResetTokenRepository;
import com.bookhub.repository.UserRepository;
import com.bookhub.util.PasswordUtil;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Manages self-service customer/admin profile updates (allow-listed fields only) and password changes (FR-09..11).
 */
public class UserService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\-\\s()]{8,20}$");

    private final UserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;

    public UserService() {
        this(new UserRepository(), new PasswordResetTokenRepository());
    }

    public UserService(UserRepository userRepo, PasswordResetTokenRepository tokenRepo) {
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
    }

    public UserProfileDTO getProfile(Long userId) {
        return JpaUtil.executeReadOnly(em -> {
            User user = userRepo.findById(em, userId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản."));
            return new UserProfileDTO(
                    user.getUserId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getRole().getRoleCode(),
                    user.getCreatedAt());
        });
    }

    /**
     * Updates strictly full_name and phone for the authenticated userId (ignores email/role/status per FR-09).
     */
    public SessionUser updateProfile(Long userId, String fullName, String phone) {
        String cleanName = fullName == null ? "" : fullName.trim();
        String cleanPhone = phone == null ? "" : phone.trim();

        Map<String, String> errors = new LinkedHashMap<>();
        if (cleanName.isEmpty() || cleanName.length() > 100) {
            errors.put("fullName", "Họ và tên là bắt buộc (tối đa 100 ký tự).");
        }
        if (!cleanPhone.isEmpty() && !PHONE_PATTERN.matcher(cleanPhone).matches()) {
            errors.put("phone", "Số điện thoại không hợp lệ (8–20 ký tự số).");
        }
        if (!errors.isEmpty()) {
            throw new BusinessException("Thông tin hồ sơ chưa hợp lệ.", errors);
        }

        return JpaUtil.executeInTransaction(em -> {
            User user = userRepo.findById(em, userId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản."));
            user.setFullName(cleanName);
            user.setPhone(cleanPhone.isEmpty() ? null : cleanPhone);
            return new SessionUser(
                    user.getUserId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole().getRoleCode());
        });
    }

    public void changePassword(Long userId, String currentPassword, String newPassword, String confirmPassword) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (currentPassword == null || currentPassword.isEmpty()) {
            errors.put("currentPassword", "Vui lòng nhập mật khẩu hiện tại.");
        }
        if (!PasswordUtil.meetsPolicy(newPassword)) {
            errors.put("newPassword", "Mật khẩu mới phải có tối thiểu 8 ký tự, gồm cả chữ và số.");
        }
        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            errors.put("confirmPassword", "Xác nhận mật khẩu mới không khớp.");
        }
        if (!errors.isEmpty()) {
            throw new BusinessException("Vui lòng kiểm tra lại thông tin đổi mật khẩu.", errors);
        }

        JpaUtil.executeInTransactionVoid(em -> {
            User user = userRepo.findById(em, userId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản."));
            if (!PasswordUtil.verify(currentPassword, user.getPasswordHash())) {
                Map<String, String> err = new LinkedHashMap<>();
                err.put("currentPassword", "Mật khẩu hiện tại không đúng.");
                throw new BusinessException("Mật khẩu hiện tại không đúng.", err);
            }
            user.setPasswordHash(PasswordUtil.hash(newPassword));
            tokenRepo.invalidateAllOpenForUser(em, userId, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        });
    }
}
