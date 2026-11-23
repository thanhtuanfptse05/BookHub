package com.bookhub.dto;

import com.bookhub.entity.RoleCode;
import com.bookhub.util.FormatUtil;
import java.time.LocalDateTime;

/**
 * Read/display profile DTO for SCR-13 (never includes passwordHash).
 */
public final class UserProfileDTO {

    private final Long userId;
    private final String fullName;
    private final String email;
    private final String phone;
    private final RoleCode roleCode;
    private final String createdAtFormatted;

    public UserProfileDTO(Long userId, String fullName, String email, String phone,
            RoleCode roleCode, LocalDateTime createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.roleCode = roleCode;
        this.createdAtFormatted = FormatUtil.formatDateTime(createdAt);
    }

    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public RoleCode getRoleCode() { return roleCode; }
    public String getCreatedAtFormatted() { return createdAtFormatted; }
}
