package com.bookhub.dto;

import com.bookhub.entity.RoleCode;
import com.bookhub.entity.UserStatus;
import com.bookhub.util.FormatUtil;
import java.time.LocalDateTime;

/**
 * Safe admin user row DTO (explicitly omits passwordHash per NFR-02).
 */
public final class AdminUserRowDTO {

    private final Long userId;
    private final String fullName;
    private final String email;
    private final String phone;
    private final RoleCode roleCode;
    private final UserStatus status;
    private final String createdAtFormatted;

    public AdminUserRowDTO(Long userId, String fullName, String email, String phone,
            RoleCode roleCode, UserStatus status, LocalDateTime createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.roleCode = roleCode;
        this.status = status;
        this.createdAtFormatted = FormatUtil.formatDateTime(createdAt);
    }

    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public RoleCode getRoleCode() { return roleCode; }
    public UserStatus getStatus() { return status; }
    public String getCreatedAtFormatted() { return createdAtFormatted; }
}
