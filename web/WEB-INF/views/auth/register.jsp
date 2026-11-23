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
      <h1 class="h3 mb-1">Đăng ký tài khoản</h1>
      <p class="small bh-muted mb-4">Tạo tài khoản khách hàng để mua sách, theo dõi đơn hàng và gửi đánh giá.</p>
      <jsp:include page="/WEB-INF/views/common/messages.jsp" />

      <form action="${pageContext.request.contextPath}/register" method="post" novalidate>
        <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">

        <div class="mb-3">
          <label class="form-label fw-medium" for="regFullName">Họ và tên <span class="text-danger">*</span></label>
          <input id="regFullName" name="fullName" type="text" maxlength="100" required
                 class="form-control ${not empty errors.fullName ? 'is-invalid' : ''}"
                 value="<c:out value='${formFullName}' />" placeholder="Nguyễn Minh Anh">
          <c:if test="${not empty errors.fullName}">
            <div class="invalid-feedback"><c:out value="${errors.fullName}" /></div>
          </c:if>
        </div>

        <div class="mb-3">
          <label class="form-label fw-medium" for="regEmail">Email <span class="text-danger">*</span></label>
          <input id="regEmail" name="email" type="email" maxlength="255" required
                 class="form-control ${not empty errors.email ? 'is-invalid' : ''}"
                 value="<c:out value='${formEmail}' />" placeholder="ban@example.com">
          <c:if test="${not empty errors.email}">
            <div class="invalid-feedback"><c:out value="${errors.email}" /></div>
          </c:if>
        </div>

        <div class="mb-3">
          <label class="form-label fw-medium" for="regPhone">Số điện thoại</label>
          <input id="regPhone" name="phone" type="tel" maxlength="20"
                 class="form-control ${not empty errors.phone ? 'is-invalid' : ''}"
                 value="<c:out value='${formPhone}' />" placeholder="0912345678 (tùy chọn)">
          <c:if test="${not empty errors.phone}">
            <div class="invalid-feedback"><c:out value="${errors.phone}" /></div>
          </c:if>
        </div>

        <div class="mb-3">
          <label class="form-label fw-medium" for="regPassword">Mật khẩu <span class="text-danger">*</span></label>
          <div class="input-group">
            <input id="regPassword" name="password" type="password" required
                   class="form-control ${not empty errors.password ? 'is-invalid' : ''}"
                   placeholder="Tối thiểu 8 ký tự, gồm chữ và số">
            <button type="button" class="btn bh-btn-outline-neutral" data-toggle-password="regPassword">Hiện</button>
          </div>
          <div class="form-text">Tối thiểu 8 ký tự, bao gồm cả chữ cái và chữ số.</div>
          <c:if test="${not empty errors.password}">
            <div class="text-danger small mt-1"><c:out value="${errors.password}" /></div>
          </c:if>
        </div>

        <div class="mb-4">
          <label class="form-label fw-medium" for="regConfirm">Xác nhận mật khẩu <span class="text-danger">*</span></label>
          <div class="input-group">
            <input id="regConfirm" name="confirmPassword" type="password" required
                   class="form-control ${not empty errors.confirmPassword ? 'is-invalid' : ''}"
                   placeholder="Nhập lại mật khẩu">
            <button type="button" class="btn bh-btn-outline-neutral" data-toggle-password="regConfirm">Hiện</button>
          </div>
          <c:if test="${not empty errors.confirmPassword}">
            <div class="text-danger small mt-1"><c:out value="${errors.confirmPassword}" /></div>
          </c:if>
        </div>

        <button type="submit" class="btn bh-btn-primary w-100">Đăng ký</button>
      </form>

      <p class="small text-center bh-muted mt-4 mb-0">
        Đã có tài khoản? <a class="fw-semibold" href="${pageContext.request.contextPath}/login">Đăng nhập</a>
      </p>
    </section>

    <a class="small bh-muted mt-3 text-decoration-none" href="${pageContext.request.contextPath}/">&larr; Quay lại trang chủ</a>
  </main>
  <script src="${pageContext.request.contextPath}/assets/vendor/bootstrap/bootstrap.bundle.min.js"></script>
  <script src="${pageContext.request.contextPath}/assets/js/bookhub.js"></script>
</body>
</html>
