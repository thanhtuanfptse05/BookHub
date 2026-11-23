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
      <h1 class="h3 mb-1">Quên mật khẩu</h1>
      <p class="small bh-muted mb-4">Nhập email đã đăng ký để nhận liên kết đặt lại mật khẩu (hiệu lực 15 phút).</p>
      <jsp:include page="/WEB-INF/views/common/messages.jsp" />

      <form action="${pageContext.request.contextPath}/forgot-password" method="post" novalidate>
        <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
        <div class="mb-4">
          <label class="form-label fw-medium" for="fpEmail">Email <span class="text-danger">*</span></label>
          <input id="fpEmail" name="email" type="email" required
                 class="form-control ${not empty errors.email ? 'is-invalid' : ''}"
                 value="<c:out value='${formEmail}' />" placeholder="ban@example.com">
          <c:if test="${not empty errors.email}">
            <div class="invalid-feedback"><c:out value="${errors.email}" /></div>
          </c:if>
        </div>
        <button type="submit" class="btn bh-btn-primary w-100">Gửi liên kết đặt lại</button>
      </form>

      <p class="small text-center bh-muted mt-4 mb-0">
        <a href="${pageContext.request.contextPath}/login">&larr; Quay lại đăng nhập</a>
      </p>
    </section>

    <a class="small bh-muted mt-3 text-decoration-none" href="${pageContext.request.contextPath}/">&larr; Quay lại trang chủ</a>
  </main>
  <script src="${pageContext.request.contextPath}/assets/vendor/bootstrap/bootstrap.bundle.min.js"></script>
  <script src="${pageContext.request.contextPath}/assets/js/bookhub.js"></script>
</body>
</html>
