package com.bookhub.repository;

import com.bookhub.entity.Role;
import com.bookhub.entity.RoleCode;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class RoleRepository extends BaseRepository<Role, Integer> {

    public RoleRepository() {
        super(Role.class);
    }

    public Optional<Role> findByCode(EntityManager em, RoleCode code) {
        if (code == null) {
            return Optional.empty();
        }
        List<Role> rows = em.createQuery(
                        "SELECT r FROM Role r WHERE r.roleCode = :code", Role.class)
                .setParameter("code", code)
                .setMaxResults(1)
                .getResultList();
        return rows.stream().findFirst();
    }
}
