package com.bookhub.dto;

import com.bookhub.entity.RoleCode;
import java.io.Serializable;

/**
 * Immutable authenticated principal stored in HttpSession (never stores JPA Entity or passwordHash).
 */
public final class SessionUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final String fullName;
    private final String email;
    private final RoleCode roleCode;

    public SessionUser(Long userId, String fullName, String email, RoleCode roleCode) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.roleCode = roleCode;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public RoleCode getRoleCode() {
        return roleCode;
    }

    public boolean isAdmin() {
        return roleCode == RoleCode.ADMIN;
    }

    public boolean isCustomer() {
        return roleCode == RoleCode.CUSTOMER;
    }

    /**
     * Two-letter initials matching Figma avatar pills (e.g. "Nguyễn Minh Anh" -> "MA", "Quản trị viên" -> "QT").
     */
    public String getInitials() {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "BH";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            String w = parts[0];
            return w.substring(0, Math.min(2, w.length())).toUpperCase();
        }
        if (isAdmin()) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        int last = parts.length - 1;
        return (parts[last - 1].substring(0, 1) + parts[last].substring(0, 1)).toUpperCase();
    }

    @Override
    public String toString() {
        return "SessionUser{userId=" + userId + ", roleCode=" + roleCode + "}";
    }
}
