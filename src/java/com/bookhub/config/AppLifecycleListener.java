package com.bookhub.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Bootstraps and shuts down the singleton JPA EntityManagerFactory with Tomcat lifecycle (FR-04, FR-05).
 */
@WebListener
public class AppLifecycleListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        JpaUtil.init();
        sce.getServletContext().setAttribute(AppConstants.CONTEXT_ATTR_CONFIG, JpaUtil.getConfig());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JpaUtil.shutdown();
    }
}
