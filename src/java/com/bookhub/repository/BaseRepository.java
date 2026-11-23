package com.bookhub.repository;

import jakarta.persistence.EntityManager;
import java.util.Optional;

/**
 * Shared JPA repository foundation. Receives EntityManager from Service transaction boundary;
 * never begins or commits transactions on its own (Constitution A-03).
 */
public abstract class BaseRepository<T, ID> {

    private final Class<T> entityClass;
    private final String entityName;

    protected BaseRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.entityName = entityClass.getSimpleName();
    }

    public Optional<T> findById(EntityManager em, ID id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(em.find(entityClass, id));
    }

    public T persist(EntityManager em, T entity) {
        em.persist(entity);
        return entity;
    }

    public T merge(EntityManager em, T entity) {
        return em.merge(entity);
    }

    public void remove(EntityManager em, T entity) {
        em.remove(entity);
    }

    public long countAll(EntityManager em) {
        Long count = em.createQuery("SELECT COUNT(e) FROM " + entityName + " e", Long.class)
                .getSingleResult();
        return count != null ? count : 0L;
    }
}
