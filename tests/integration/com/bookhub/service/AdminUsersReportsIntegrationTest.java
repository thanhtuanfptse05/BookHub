package com.bookhub.service;

import com.bookhub.config.JpaUtil;
import com.bookhub.dto.AdminUserRowDTO;
import com.bookhub.dto.DashboardDTO;
import com.bookhub.dto.PageResult;
import com.bookhub.dto.ReportSummaryDTO;
import com.bookhub.entity.OrderStatus;
import com.bookhub.entity.UserStatus;
import com.bookhub.exception.BusinessException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for 08-feat-admin-users-reports (AC-01..AC-06) against live SQL Server BookHubDB.
 */
class AdminUsersReportsIntegrationTest {

    private final AuthService authService = new AuthService();
    private final AdminUserService adminUserService = new AdminUserService();
    private final ReportService reportService = new ReportService();

    @BeforeAll
    static void init() {
        JpaUtil.init();
    }

    @AfterAll
    static void close() {
        JpaUtil.shutdown();
    }

    // @spec admin-users-reports/AC-01
    @Test
    void dashboardComputesAccurateKpisModerationQueuesRecentOrdersAndBestsellers() {
        DashboardDTO d = reportService.getDashboard();
        assertTrue(d.getActiveBooks() >= 24);
        assertTrue(d.getTotalCustomers() >= 4);
        assertTrue(d.getTotalOrders() >= 8);
        assertEquals(new BigDecimal("2446000.00"), d.getDeliveredRevenue());
        assertEquals(2L, d.getPendingReviews());
        assertEquals(2L, d.getOpenQuestions());
        assertEquals(5, d.getRecentOrders().size());
        assertEquals(5, d.getTopBestsellers().size());
    }

    // @spec admin-users-reports/AC-02
    // @spec admin-users-reports/AC-03
    @Test
    void userManagementLockUnlockEnforcesLoginBlockAndProtectsSelfAndLastAdmin() {
        String marker = UUID.randomUUID().toString().substring(0, 8);
        String email = "locktest." + marker + "@bookhub.vn";
        Long custId = null;

        try {
            custId = authService.register("Lock Target " + marker, email, "0909111222", "Pass1234", "Pass1234").getUserId();

            // AC-02: search returns DTO without passwordHash; lock blocks login; unlock restores login
            PageResult<AdminUserRowDTO> found = adminUserService.searchUsers(email, UserStatus.ACTIVE, 1);
            assertEquals(1L, found.getTotalItems());
            assertEquals(email, found.getItems().get(0).getEmail());

            assertEquals(UserStatus.LOCKED, adminUserService.toggleLock(1L, custId, "lock"));
            BusinessException lockLoginEx = assertThrows(
                    BusinessException.class, () -> authService.authenticate(email, "Pass1234"));
            assertEquals("Tài khoản của bạn đã bị khóa.", lockLoginEx.getMessage());

            assertEquals(UserStatus.ACTIVE, adminUserService.toggleLock(1L, custId, "unlock"));
            assertEquals(custId, authService.authenticate(email, "Pass1234").getUserId());

            // AC-03: admin cannot lock self
            assertThrows(BusinessException.class, () -> adminUserService.toggleLock(1L, 1L, "lock"));

            // AC-03: locking 2nd admin works, then locking the LAST remaining active admin is blocked
            Long admin2Id = JpaUtil.executeReadOnly(em -> em.createQuery(
                            "SELECT u.userId FROM User u WHERE u.email = 'admin2@bookhub.vn'", Long.class)
                    .getSingleResult());
            try {
                adminUserService.toggleLock(1L, admin2Id, "lock");
                assertThrows(BusinessException.class, () -> adminUserService.toggleLock(admin2Id, 1L, "lock"));
            } finally {
                adminUserService.toggleLock(1L, admin2Id, "unlock");
            }
        } finally {
            if (custId != null) {
                Long uid = custId;
                JpaUtil.executeInTransactionVoid(em ->
                        em.createQuery("DELETE FROM User u WHERE u.userId = :u").setParameter("u", uid).executeUpdate());
            }
        }
    }

    // @spec admin-users-reports/AC-04
    // @spec admin-users-reports/AC-05
    // @spec admin-users-reports/AC-06
    @Test
    void sqlAggregateReportMatchesSeededValuesValidatesDateRangeAndHandlesZeroDataSafely() {
        // AC-04: verify COUNT/SUM/AVG/MIN/MAX match audited 03_demo_queries.sql values
        ReportSummaryDTO all = reportService.getReport(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
        assertEquals(new BigDecimal("2446000.00"), all.getDeliveredRevenue());
        assertEquals(new BigDecimal("611500.00"), all.getAvgOrderValue());
        assertEquals(4L, all.getDeliveredOrderCount());
        assertEquals(5, all.getStatusCounts().size());
        assertEquals(new BigDecimal("45000.00"), all.getMinBookPrice(), "Inactive 15,000 VND book must be excluded from MIN");
        assertEquals(new BigDecimal("320000.00"), all.getMaxBookPrice());
        assertTrue(all.getTotalActiveStockUnits() >= 450L);
        assertFalse(all.getTopBestsellers().isEmpty());

        // AC-05: fromDate > toDate throws BusinessException cleanly (no HTTP 500)
        assertThrows(BusinessException.class,
                () -> reportService.getReport(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 1)));

        // AC-06: empty date range yields 0 revenue, 0 AOV, all 5 statuses present at 0, no divide-by-zero
        ReportSummaryDTO emptyRange = reportService.getReport(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 31));
        assertEquals(new BigDecimal("0.00"), emptyRange.getDeliveredRevenue());
        assertEquals(new BigDecimal("0.00"), emptyRange.getAvgOrderValue());
        assertEquals(0L, emptyRange.getTotalOrdersInRange());
        assertEquals(OrderStatus.values().length, emptyRange.getStatusCounts().size());
        assertTrue(emptyRange.getTopBestsellers().isEmpty());
    }
}
