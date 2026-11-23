package com.bookhub.repository;

import com.bookhub.entity.PasswordResetToken;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for dbo.PasswordResetTokens with atomic one-time consumption (EC-03).
 */
public class PasswordResetTokenRepository extends BaseRepository<PasswordResetToken, Long> {

    public PasswordResetTokenRepository() {
        super(PasswordResetToken.class);
    }

    public Optional<PasswordResetToken> findByTokenHash(EntityManager em, String tokenHash) {
        if (tokenHash == null || tokenHash.isEmpty()) {
            return Optional.empty();
        }
        List<PasswordResetToken> rows = em.createQuery(
                        "SELECT t FROM PasswordResetToken t JOIN FETCH t.user WHERE t.tokenHash = :hash",
                        PasswordResetToken.class)
                .setParameter("hash", tokenHash)
                .setMaxResults(1)
                .getResultList();
        return rows.stream().findFirst();
    }

    public int markUsedAtomically(EntityManager em, Long resetId, LocalDateTime now) {
        return em.createQuery(
                        "UPDATE PasswordResetToken t SET t.usedAt = :now "
                                + "WHERE t.resetId = :id AND t.usedAt IS NULL AND t.expiresAt > :now")
                .setParameter("now", now)
                .setParameter("id", resetId)
                .executeUpdate();
    }

    public int invalidateAllOpenForUser(EntityManager em, Long userId, LocalDateTime now) {
        return em.createQuery(
                        "UPDATE PasswordResetToken t SET t.usedAt = :now "
                                + "WHERE t.user.userId = :userId AND t.usedAt IS NULL")
                .setParameter("now", now)
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
