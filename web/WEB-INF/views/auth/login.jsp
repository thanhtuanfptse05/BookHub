<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <jsp:include page="/WEB-INF/views/common/head.jsp" />
</head>
<body>
  <main class="bh-auth-shell">
    <a class="bh-brand mb-4" href="${pageContext.request.contextPath}/">
      <span class="bh-brand-icon" aria-hidden="true">B</span>
      <span>BookHub</span>
    </a>

    <section class="bh-auth-card">
      <h1 class="h3 mb-1">Đăng nhập</h1>
      <p class="small bh-muted mb-4">Chào mừng bạn quay lại hiệu sách BookHub.</p>
      <jsp:include page="/WEB-INF/views/common/messages.jsp" />

      <form action="${pageContext.request.contextPath}/login" method="post" novalidate>
        <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
        <c:if test="${not empty returnUrl}">
          <input type="hidden" name="returnUrl" value="<c:out value='${returnUrl}' />">
        </c:if>

        <div class="mb-3">
          <label class="form-label fw-medium" for="loginEmail">Email <span class="text-danger">*</span></label>
          <input id="loginEmail" name="email" type="email" required
                 class="form-control" value="<c:out value='${formEmail}' />" placeholder="ban@example.com">
        </div>

        <div class="mb-4">
          <div class="d-flex justify-content-between align-items-center mb-1">
            <label class="form-label fw-medium mb-0" for="loginPassword">Mật khẩu <span class="text-danger">*</span></label>
            <a class="small" href="${pageContext.request.contextPath}/forgot-password">Quên mật khẩu?</a>
          </div>
          <div class="input-group">
            <input id="loginPassword" name="password" type="password" required class="form-control" placeholder="Nhập mật khẩu">
            <button type="button" class="btn bh-btn-outline-neutral" data-toggle-password="loginPassword">Hiện</button>
          </div>
        </div>

        <button type="submit" class="btn bh-btn-primary w-100">Đăng nhập</button>
      </form>

      <p class="small text-center bh-muted mt-4 mb-0">
        Chưa có tài khoản? <a class="fw-semibold" href="${pageContext.request.contextPath}/register">Đăng ký ngay</a>
      </p>
    </section>

    <a class="small bh-muted mt-3 text-decoration-none" href="${pageContext.request.contextPath}/">&larr; Quay lại trang chủ</a>
  </main>
  <script src="${pageContext.request.contextPath}/assets/vendor/bootstrap/bootstrap.bundle.min.js"></script>
  <script src="${pageContext.request.contextPath}/assets/js/bookhub.js"></script>
</body>
</html>
