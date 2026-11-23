<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<footer class="bh-footer">
  <div class="bh-container">
    <div class="row g-4">
      <div class="col-12 col-md-5">
        <div class="bh-serif fs-4 fw-bold text-white mb-2">BookHub</div>
        <p class="small mb-2">Hiệu sách trực tuyến hiện đại, chọn lọc đầu sách chất lượng về Công nghệ, Kinh tế, Văn học, Kỹ năng, Ngoại ngữ và Thiếu nhi.</p>
        <p class="small mb-0">Thanh toán COD minh bạch · Phí giao hàng cố định 30.000 &#8363;/đơn.</p>
      </div>
      <div class="col-6 col-md-3">
        <div class="bh-footer-title">Khám phá</div>
        <ul class="list-unstyled d-flex flex-column gap-2 small mb-0">
          <li><a href="${pageContext.request.contextPath}/">Trang chủ</a></li>
          <li><a href="${pageContext.request.contextPath}/books">Danh mục sách</a></li>
          <li><a href="${pageContext.request.contextPath}/books?sort=bestseller">Sách bán chạy</a></li>
          <li><a href="${pageContext.request.contextPath}/account/orders">Tra cứu đơn hàng</a></li>
        </ul>
      </div>
      <div class="col-6 col-md-4">
        <div class="bh-footer-title">Liên hệ &amp; Hỗ trợ</div>
        <ul class="list-unstyled d-flex flex-column gap-2 small mb-0">
          <li>Hotline: 1900 6868 (8:00 – 21:00)</li>
          <li>Email: support@bookhub.vn</li>
          <li>Địa chỉ: Khu CNC Hòa Lạc, Thạch Thất, Hà Nội</li>
        </ul>
      </div>
    </div>
    <div class="bh-footer-bottom d-flex flex-column flex-sm-row justify-content-between gap-2">
      <span>&copy; 2026 BookHub. Đồ án PRJ301 — Jakarta EE 10 &amp; Hibernate ORM.</span>
      <span>Thanh toán khi nhận hàng (COD) toàn quốc</span>
    </div>
  </div>
</footer>
<jsp:include page="/WEB-INF/views/common/confirm-modal.jsp" />
<script src="${pageContext.request.contextPath}/assets/vendor/bootstrap/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/bookhub.js"></script>
