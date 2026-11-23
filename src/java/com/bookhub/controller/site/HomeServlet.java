package com.bookhub.controller.site;

import com.bookhub.dto.HomePageDTO;
import com.bookhub.service.CatalogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Storefront landing page (SCR-01) mapped strictly to empty pattern "" so Tomcat DefaultServlet serves /assets/*.
 */
@WebServlet(name = "HomeServlet", urlPatterns = {""})
public class HomeServlet extends HttpServlet {

    private final CatalogService catalogService = new CatalogService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HomePageDTO home = catalogService.getHomeData();
        req.setAttribute("home", home);
        req.setAttribute("activeNav", "home");
        req.setAttribute("pageTitle", "BookHub — Hiệu sách trực tuyến tuyển chọn");
        req.setAttribute("metaDescription",
                "Khám phá hàng trăm đầu sách Công nghệ, Kinh tế, Văn học, Kỹ năng tại BookHub. Thanh toán COD toàn quốc, phí giao hàng cố định 30.000 VND.");
        req.getRequestDispatcher("/WEB-INF/views/site/home.jsp").forward(req, resp);
    }
}
