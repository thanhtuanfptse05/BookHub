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
      <section class="bh-empty-state mx-auto">
        <div class="bh-badge bh-badge-danger mb-3">HTTP 403</div>
        <h1 class="h2 mb-2">Không có quyền truy cập</h1>
        <p class="bh-muted mb-4">Bạn không có quyền truy cập khu vực này. Vui lòng đăng nhập bằng tài khoản phù hợp hoặc quay lại cửa hàng.</p>
        <div class="d-flex justify-content-center gap-2 flex-wrap">
          <a class="btn bh-btn-primary" href="${pageContext.request.contextPath}/">Về trang chủ</a>
          <a class="btn bh-btn-outline-neutral" href="${pageContext.request.contextPath}/login">Đăng nhập</a>
        </div>
      </section>
    </div>
  </main>
  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
