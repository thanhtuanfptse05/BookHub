package com.bookhub.service;

import com.bookhub.config.AppConstants;
import com.bookhub.config.JpaUtil;
import com.bookhub.dto.AdminUserRowDTO;
import com.bookhub.dto.PageResult;
import com.bookhub.entity.RoleCode;
import com.bookhub.entity.User;
import com.bookhub.entity.UserStatus;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.NotFoundException;
import com.bookhub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Admin user management service with self-lock and last-admin protection (FR-02..04, EC-03).
 */
public class AdminUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminUserService.class);

    private final UserRepository userRepo;

    public AdminUserService() {
        this(new UserRepository());
    }

    public AdminUserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public PageResult<AdminUserRowDTO> searchUsers(String keyword, UserStatus status, int page) {
        return JpaUtil.executeReadOnly(em ->
                userRepo.searchAdminUsers(em, keyword, status, page, AppConstants.DEFAULT_ADMIN_PAGE_SIZE));
    }

    public UserStatus toggleLock(Long currentAdminId, Long targetUserId, String action) {
        boolean locking = "lock".equalsIgnoreCase(action);
        boolean unlocking = "unlock".equalsIgnoreCase(action);
        if (!locking && !unlocking) {
            throw new BusinessException("Thao tác tài khoản không hợp lệ.");
        }
        if (locking && currentAdminId != null && currentAdminId.equals(targetUserId)) {
            throw new BusinessException("Bạn không thể khóa chính tài khoản quản trị đang đăng nhập.");
        }

        return JpaUtil.executeInTransaction(em -> {
            User target = userRepo.findById(em, targetUserId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản."));
            if (locking && target.getRole().getRoleCode() == RoleCode.ADMIN) {
                long activeAdmins = userRepo.countActiveAdmins(em);
                if (activeAdmins <= 1L) {
                    throw new BusinessException("Không thể khóa tài khoản Quản trị viên đang hoạt động cuối cùng.");
                }
            }
            UserStatus next = locking ? UserStatus.LOCKED : UserStatus.ACTIVE;
            target.setStatus(next);
            LOGGER.info("Admin userId={} changed target userId={} status to {}", currentAdminId, targetUserId, next);
            return next;
        });
    }
}
