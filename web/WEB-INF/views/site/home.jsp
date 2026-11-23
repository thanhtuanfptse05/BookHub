<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <jsp:include page="/WEB-INF/views/common/head.jsp" />
</head>
<body>
  <jsp:include page="/WEB-INF/views/common/header.jsp" />
  <main class="bh-main">
    <div class="bh-container">
      <jsp:include page="/WEB-INF/views/common/messages.jsp" />

      <!-- Hero Banner (SCR-01) -->
      <section class="bh-hero mb-5">
        <div class="row align-items-center g-4">
          <div class="col-12 col-lg-8">
            <span class="bh-badge bh-badge-teal mb-3">Hiệu sách chọn lọc &middot; Giao COD toàn quốc</span>
            <h1 class="mb-3">Mỗi trang sách mở ra một hành trình tri thức mới</h1>
            <p class="bh-hero-sub mb-4">
              Tuyển chọn đầu sách Công nghệ, Kinh tế, Văn học, Kỹ năng, Ngoại ngữ và Thiếu nhi chính hãng.
              Thanh toán khi nhận hàng (COD) an tâm, phí giao hàng cố định <strong>30.000 &#8363;/đơn</strong>.
            </p>
            <div class="d-flex flex-wrap align-items-center gap-3">
              <a class="btn bh-btn-accent" href="${pageContext.request.contextPath}/books">Khám phá danh mục sách</a>
              <a class="btn bh-btn-outline-neutral" href="${pageContext.request.contextPath}/books?sort=bestseller">Xem sách bán chạy</a>
            </div>
          </div>
          <div class="col-12 col-lg-4 d-none d-lg-block">
            <div class="bh-card p-4 text-dark">
              <div class="small bh-muted text-uppercase fw-semibold mb-2">Cam kết BookHub</div>
              <ul class="list-unstyled d-flex flex-column gap-2 small mb-0">
                <li><strong>100% COD:</strong> Kiểm tra sách trước khi thanh toán.</li>
                <li><strong>30.000 &#8363; đồng giá:</strong> Không phụ phí ẩn toàn quốc.</li>
                <li><strong>Đánh giá thực:</strong> Chỉ người đã nhận hàng mới viết review.</li>
              </ul>
            </div>
          </div>
        </div>
      </section>

      <!-- Category Shortcuts -->
      <section class="mb-5" aria-labelledby="catHeading">
        <div class="d-flex justify-content-between align-items-end mb-3">
          <h2 id="catHeading" class="h3 mb-0">Chọn theo thể loại</h2>
          <a class="small fw-semibold text-decoration-none" href="${pageContext.request.contextPath}/books">Xem tất cả &rsaquo;</a>
        </div>
        <div class="row g-3 row-cols-2 row-cols-md-3 row-cols-lg-6">
          <c:forEach var="cat" items="${home.categories}">
            <div class="col">
              <a class="bh-cat-tile h-100" href="${pageContext.request.contextPath}/books?categoryId=${cat.categoryId}">
                <span class="bh-serif fw-semibold fs-6"><c:out value="${cat.name}" /></span>
                <span class="small bh-muted mt-2"><c:out value="${cat.activeBookCount}" /> đầu sách</span>
              </a>
            </div>
          </c:forEach>
        </div>
      </section>

      <!-- Shelf 1: Featured Books -->
      <section class="mb-5" aria-labelledby="featuredHeading">
        <div class="d-flex justify-content-between align-items-end mb-3">
          <h2 id="featuredHeading" class="h3 mb-0">Sách nổi bật</h2>
          <a class="small fw-semibold text-decoration-none" href="${pageContext.request.contextPath}/books">Xem tất cả &rsaquo;</a>
        </div>
        <div class="row g-3 row-cols-2 row-cols-md-3 row-cols-lg-4">
          <c:forEach var="item" items="${home.featuredBooks}">
            <div class="col">
              <c:set var="bookCard" value="${item}" scope="request" />
              <jsp:include page="/WEB-INF/views/common/book-card.jsp" />
            </div>
          </c:forEach>
        </div>
      </section>

      <!-- Shelf 2: Newest Books -->
      <section class="mb-5" aria-labelledby="newestHeading">
        <div class="d-flex justify-content-between align-items-end mb-3">
          <h2 id="newestHeading" class="h3 mb-0">Sách mới nhất</h2>
          <a class="small fw-semibold text-decoration-none" href="${pageContext.request.contextPath}/books?sort=newest">Xem tất cả &rsaquo;</a>
        </div>
        <div class="row g-3 row-cols-2 row-cols-md-3 row-cols-lg-4">
          <c:forEach var="item" items="${home.newestBooks}">
            <div class="col">
              <c:set var="bookCard" value="${item}" scope="request" />
              <jsp:include page="/WEB-INF/views/common/book-card.jsp" />
            </div>
          </c:forEach>
        </div>
      </section>

      <!-- Shelf 3: Bestseller Books -->
      <section class="mb-2" aria-labelledby="bestsellerHeading">
        <div class="d-flex justify-content-between align-items-end mb-3">
          <h2 id="bestsellerHeading" class="h3 mb-0">Sách bán chạy</h2>
          <a class="small fw-semibold text-decoration-none" href="${pageContext.request.contextPath}/books?sort=bestseller">Xem tất cả &rsaquo;</a>
        </div>
        <div class="row g-3 row-cols-2 row-cols-md-3 row-cols-lg-4">
          <c:forEach var="item" items="${home.bestsellerBooks}">
            <div class="col">
              <c:set var="bookCard" value="${item}" scope="request" />
              <jsp:include page="/WEB-INF/views/common/book-card.jsp" />
            </div>
          </c:forEach>
        </div>
      </section>
    </div>
  </main>
  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
