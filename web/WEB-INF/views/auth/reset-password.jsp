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
      <h1 class="h3 mb-2">Đặt lại mật khẩu</h1>
      <jsp:include page="/WEB-INF/views/common/messages.jsp" />

      <c:choose>
        <c:when test="${tokenValid}">
          <p class="small bh-muted mb-4">Nhập mật khẩu mới tối thiểu 8 ký tự, gồm cả chữ và số.</p>
          <form action="${pageContext.request.contextPath}/reset-password" method="post" novalidate>
            <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
            <input type="hidden" name="token" value="<c:out value='${token}' />">

            <div class="mb-3">
              <label class="form-label fw-medium" for="rpNew">Mật khẩu mới <span class="text-danger">*</span></label>
              <div class="input-group">
                <input id="rpNew" name="newPassword" type="password" required
                       class="form-control ${not empty errors.newPassword ? 'is-invalid' : ''}">
                <button type="button" class="btn bh-btn-outline-neutral" data-toggle-password="rpNew">Hiện</button>
              </div>
              <c:if test="${not empty errors.newPassword}">
                <div class="text-danger small mt-1"><c:out value="${errors.newPassword}" /></div>
              </c:if>
            </div>

            <div class="mb-4">
              <label class="form-label fw-medium" for="rpConfirm">Xác nhận mật khẩu mới <span class="text-danger">*</span></label>
              <div class="input-group">
                <input id="rpConfirm" name="confirmPassword" type="password" required
                       class="form-control ${not empty errors.confirmPassword ? 'is-invalid' : ''}">
                <button type="button" class="btn bh-btn-outline-neutral" data-toggle-password="rpConfirm">Hiện</button>
              </div>
              <c:if test="${not empty errors.confirmPassword}">
                <div class="text-danger small mt-1"><c:out value="${errors.confirmPassword}" /></div>
              </c:if>
            </div>

            <button type="submit" class="btn bh-btn-primary w-100">Cập nhật mật khẩu</button>
          </form>
        </c:when>
        <c:otherwise>
          <div class="bh-alert bh-alert-warning mb-4" role="alert">
            <div>Liên kết đặt lại mật khẩu không hợp lệ, đã được sử dụng hoặc đã hết hạn (quá 15 phút).</div>
          </div>
          <a class="btn bh-btn-primary w-100" href="${pageContext.request.contextPath}/forgot-password">Yêu cầu liên kết mới</a>
        </c:otherwise>
      </c:choose>
    </section>

    <a class="small bh-muted mt-3 text-decoration-none" href="${pageContext.request.contextPath}/login">&larr; Quay lại đăng nhập</a>
  </main>
  <script src="${pageContext.request.contextPath}/assets/vendor/bootstrap/bootstrap.bundle.min.js"></script>
  <script src="${pageContext.request.contextPath}/assets/js/bookhub.js"></script>
</body>
</html>
