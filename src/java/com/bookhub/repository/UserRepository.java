package com.bookhub.repository;

import com.bookhub.entity.User;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for dbo.Users. Uses JOIN FETCH on role to prevent N+1 reads (A-09).
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
}
