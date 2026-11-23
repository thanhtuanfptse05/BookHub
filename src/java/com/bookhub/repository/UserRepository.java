package com.bookhub.repository;

import com.bookhub.dto.AdminUserRowDTO;
import com.bookhub.dto.PageResult;
import com.bookhub.entity.RoleCode;
import com.bookhub.entity.User;
import com.bookhub.entity.UserStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JPA repository for dbo.Users. Uses JOIN FETCH on role and DTO projections omitting passwordHash.
 */
public class UserRepository extends BaseRepository<User, Long> {

    public UserRepository() {
        super(User.class);
    }

    public Optional<User> findByEmail(EntityManager em, String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        List<User> rows = em.createQuery(
                        "SELECT u FROM User u JOIN FETCH u.role WHERE LOWER(u.email) = LOWER(:email)",
                        User.class)
                .setParameter("email", email.trim())
                .setMaxResults(1)
                .getResultList();
        return rows.stream().findFirst();
    }

    public boolean existsByEmail(EntityManager em, String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        Long count = em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE LOWER(u.email) = LOWER(:email)",
                        Long.class)
                .setParameter("email", email.trim())
                .getSingleResult();
        return count != null && count > 0L;
    }

    public long countByRole(EntityManager em, RoleCode roleCode) {
        Long cnt = em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.role.roleCode = :rc", Long.class)
                .setParameter("rc", roleCode)
                .getSingleResult();
        return cnt != null ? cnt : 0L;
    }

    public long countActiveAdmins(EntityManager em) {
        Long cnt = em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.role.roleCode = :rc AND u.status = :st",
                        Long.class)
                .setParameter("rc", RoleCode.ADMIN)
                .setParameter("st", UserStatus.ACTIVE)
                .getSingleResult();
        return cnt != null ? cnt : 0L;
    }

    public PageResult<AdminUserRowDTO> searchAdminUsers(
            EntityManager em, String keyword, UserStatus status, int page, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        Map<String, Object> params = new LinkedHashMap<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            where.append("AND (LOWER(u.fullName) LIKE :kw OR LOWER(u.email) LIKE :kw OR LOWER(COALESCE(u.phone, '')) LIKE :kw) ");
            params.put("kw", "%" + keyword.trim().toLowerCase() + "%");
        }
        if (status != null) {
            where.append("AND u.status = :st ");
            params.put("st", status);
        }
        TypedQuery<Long> countQ = em.createQuery("SELECT COUNT(u) FROM User u" + where, Long.class);
        params.forEach(countQ::setParameter);
        long total = countQ.getSingleResult();
        if (total == 0L) {
            return new PageResult<>(Collections.emptyList(), 1, pageSize, 0L);
        }
        int totalPages = (int) Math.ceil((double) total / pageSize);
        int safePage = Math.min(Math.max(1, page), totalPages);
        TypedQuery<AdminUserRowDTO> dataQ = em.createQuery(
                "SELECT new com.bookhub.dto.AdminUserRowDTO("
                        + "u.userId, u.fullName, u.email, u.phone, r.roleCode, u.status, u.createdAt) "
                        + "FROM User u JOIN u.role r" + where + "ORDER BY u.createdAt DESC, u.userId DESC",
                AdminUserRowDTO.class);
        params.forEach(dataQ::setParameter);
        List<AdminUserRowDTO> rows = dataQ.setFirstResult((safePage - 1) * pageSize).setMaxResults(pageSize).getResultList();
        return new PageResult<>(rows, safePage, pageSize, total);
    }
}
