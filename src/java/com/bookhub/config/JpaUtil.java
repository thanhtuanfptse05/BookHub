package com.bookhub.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-safe singleton EntityManagerFactory manager and transaction boundary helper.
 * Each operation opens and closes its own EntityManager; transactions stay at Service scope (A-03).
 */
public final class JpaUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaUtil.class);

    private static volatile EntityManagerFactory entityManagerFactory;
    private static volatile AppConfig activeConfig;

    private JpaUtil() {
    }

    public static synchronized void init() {
        if (isInitialized()) {
            return;
        }
        init(AppConfig.load());
    }

    public static synchronized void init(AppConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("AppConfig must not be null");
        }
        if (isInitialized()) {
            LOGGER.info("JpaUtil is already initialized; reusing existing EntityManagerFactory.");
            return;
        }
        try {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory(
                    AppConstants.PERSISTENCE_UNIT_NAME,
                    config.toJpaProperties());
            entityManagerFactory = emf;
            activeConfig = config;
            LOGGER.info(
                    "Initialized persistence unit '{}' successfully (target={}, source={}).",
                    AppConstants.PERSISTENCE_UNIT_NAME,
                    config.getSanitizedDbUrl(),
                    config.getSourceDescription());
        } catch (RuntimeException ex) {
            LOGGER.error(
                    "Failed to initialize persistence unit '{}' (target={}, source={}). Check DB connectivity and schema validation.",
                    AppConstants.PERSISTENCE_UNIT_NAME,
                    config.getSanitizedDbUrl(),
                    config.getSourceDescription());
            throw ex;
        }
    }

    public static boolean isInitialized() {
        EntityManagerFactory emf = entityManagerFactory;
        return emf != null && emf.isOpen();
    }

    public static AppConfig getConfig() {
        AppConfig cfg = activeConfig;
        if (cfg == null) {
            throw new IllegalStateException("JpaUtil has not been initialized.");
        }
        return cfg;
    }

    public static EntityManager getEntityManager() {
        EntityManagerFactory emf = entityManagerFactory;
        if (emf == null || !emf.isOpen()) {
            throw new IllegalStateException(
                    "EntityManagerFactory is not initialized or already closed.");
        }
        return emf.createEntityManager();
    }

    public static <T> T executeReadOnly(Function<EntityManager, T> work) {
        EntityManager em = getEntityManager();
        try {
            return work.apply(em);
        } finally {
            closeQuietly(em);
        }
    }

    public static <T> T executeInTransaction(Function<EntityManager, T> work) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = work.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException | Error ex) {
            rollbackQuietly(tx);
            throw ex;
        } finally {
            closeQuietly(em);
        }
    }

    public static void executeInTransactionVoid(Consumer<EntityManager> work) {
        executeInTransaction(em -> {
            work.accept(em);
            return null;
        });
    }

    public static synchronized void shutdown() {
        EntityManagerFactory emf = entityManagerFactory;
        entityManagerFactory = null;
        activeConfig = null;
        if (emf != null && emf.isOpen()) {
            emf.close();
            LOGGER.info("Closed persistence unit '{}' cleanly.", AppConstants.PERSISTENCE_UNIT_NAME);
        }
    }

    private static void rollbackQuietly(EntityTransaction tx) {
        if (tx != null && tx.isActive()) {
            try {
                tx.rollback();
            } catch (RuntimeException rollbackEx) {
                LOGGER.warn("Rollback failed after transaction error: {}", rollbackEx.getMessage());
            }
        }
    }

    private static void closeQuietly(EntityManager em) {
        if (em != null && em.isOpen()) {
            try {
                em.close();
            } catch (RuntimeException closeEx) {
                LOGGER.warn("Failed to close EntityManager cleanly: {}", closeEx.getMessage());
            }
        }
    }
}
