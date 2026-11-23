package com.bookhub.controller.admin;

import com.bookhub.exception.BusinessException;
import com.bookhub.service.ReportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@WebServlet(name = "AdminReportServlet", urlPatterns = {"/admin/reports"})
public class AdminReportServlet extends HttpServlet {

    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String rawFrom = req.getParameter("fromDate");
        String rawTo = req.getParameter("toDate");
        LocalDate fromDate = null;
        LocalDate toDate = null;
        String dateError = null;

        try {
            if (rawFrom != null && !rawFrom.trim().isEmpty()) {
                fromDate = LocalDate.parse(rawFrom.trim());
            }
            if (rawTo != null && !rawTo.trim().isEmpty()) {
                toDate = LocalDate.parse(rawTo.trim());
            }
            req.setAttribute("report", reportService.getReport(fromDate, toDate));
        } catch (DateTimeParseException ex) {
            dateError = "Định dạng ngày không hợp lệ (dùng YYYY-MM-DD). Đang hiển thị báo cáo toàn thời gian.";
            req.setAttribute("report", reportService.getReport(null, null));
        } catch (BusinessException ex) {
            dateError = ex.getMessage() + " Đang hiển thị báo cáo toàn thời gian.";
            req.setAttribute("report", reportService.getReport(null, null));
        }

        req.setAttribute("dateError", dateError);
        req.setAttribute("adminNav", "reports");
        req.setAttribute("pageTitle", "Báo cáo thống kê — BookHub Admin");
        req.setAttribute("noindex", Boolean.TRUE);
        req.getRequestDispatcher("/WEB-INF/views/admin/reports.jsp").forward(req, resp);
    }
}
