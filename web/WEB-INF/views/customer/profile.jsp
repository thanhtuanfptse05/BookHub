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
      <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
        <h1 class="h2 mb-0">Tài khoản của tôi</h1>
        <div class="d-flex gap-2">
          <a class="btn ${accountTab == 'profile' ? 'bh-btn-primary' : 'bh-btn-outline-neutral'}"
             href="${pageContext.request.contextPath}/account/profile">Hồ sơ cá nhân</a>
          <a class="btn ${accountTab == 'orders' ? 'bh-btn-primary' : 'bh-btn-outline-neutral'}"
             href="${pageContext.request.contextPath}/account/orders">Đơn hàng của tôi</a>
        </div>
      </div>

      <jsp:include page="/WEB-INF/views/common/messages.jsp" />

      <div class="row g-4">
        <div class="col-12 col-lg-6">
          <section class="bh-card p-4 h-100">
            <h2 class="h4 mb-3">Thông tin cá nhân</h2>
            <form action="${pageContext.request.contextPath}/account/profile" method="post" novalidate>
              <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
              <input type="hidden" name="action" value="updateProfile">

              <div class="mb-3">
                <label class="form-label fw-medium" for="profFullName">Họ và tên <span class="text-danger">*</span></label>
                <input id="profFullName" name="fullName" type="text" maxlength="100" required
                       class="form-control ${not empty errors.fullName ? 'is-invalid' : ''}"
                       value="<c:out value='${not empty formFullName ? formFullName : profile.fullName}' />">
                <c:if test="${not empty errors.fullName}">
                  <div class="invalid-feedback"><c:out value="${errors.fullName}" /></div>
                </c:if>
              </div>

              <div class="mb-3">
                <label class="form-label fw-medium" for="profEmail">Email đăng nhập</label>
                <input id="profEmail" type="email" class="form-control" disabled
                       value="<c:out value='${profile.email}' />">
                <div class="form-text">Email là định danh cố định, không thể thay đổi.</div>
              </div>

              <div class="mb-3">
                <label class="form-label fw-medium" for="profPhone">Số điện thoại</label>
                <input id="profPhone" name="phone" type="tel" maxlength="20"
                       class="form-control ${not empty errors.phone ? 'is-invalid' : ''}"
                       value="<c:out value='${not empty formPhone ? formPhone : profile.phone}' />"
                       placeholder="0912345678">
                <c:if test="${not empty errors.phone}">
                  <div class="invalid-feedback"><c:out value="${errors.phone}" /></div>
                </c:if>
              </div>

              <div class="mb-4 small bh-muted">
                Vai trò: <strong><c:out value="${profile.roleCode}" /></strong> &middot;
                Ngày tham gia: <c:out value="${profile.createdAtFormatted}" />
              </div>

              <button type="submit" class="btn bh-btn-primary">Lưu thay đổi</button>
            </form>
          </section>
        </div>

        <div class="col-12 col-lg-6">
          <section class="bh-card p-4 h-100">
            <h2 class="h4 mb-3">Đổi mật khẩu</h2>
            <form action="${pageContext.request.contextPath}/account/profile" method="post" novalidate>
              <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
              <input type="hidden" name="action" value="changePassword">

              <div class="mb-3">
                <label class="form-label fw-medium" for="cpCurrent">Mật khẩu hiện tại <span class="text-danger">*</span></label>
                <div class="input-group">
                  <input id="cpCurrent" name="currentPassword" type="password" required
                         class="form-control ${not empty errors.currentPassword ? 'is-invalid' : ''}">
                  <button type="button" class="btn bh-btn-outline-neutral" data-toggle-password="cpCurrent">Hiện</button>
                </div>
                <c:if test="${not empty errors.currentPassword}">
                  <div class="text-danger small mt-1"><c:out value="${errors.currentPassword}" /></div>
                </c:if>
              </div>

              <div class="mb-3">
                <label class="form-label fw-medium" for="cpNew">Mật khẩu mới <span class="text-danger">*</span></label>
                <div class="input-group">
                  <input id="cpNew" name="newPassword" type="password" required
                         class="form-control ${not empty errors.newPassword ? 'is-invalid' : ''}">
                  <button type="button" class="btn bh-btn-outline-neutral" data-toggle-password="cpNew">Hiện</button>
                </div>
                <div class="form-text">Tối thiểu 8 ký tự, gồm cả chữ và số.</div>
                <c:if test="${not empty errors.newPassword}">
                  <div class="text-danger small mt-1"><c:out value="${errors.newPassword}" /></div>
                </c:if>
              </div>

              <div class="mb-4">
                <label class="form-label fw-medium" for="cpConfirm">Xác nhận mật khẩu mới <span class="text-danger">*</span></label>
                <div class="input-group">
                  <input id="cpConfirm" name="confirmPassword" type="password" required
                         class="form-control ${not empty errors.confirmPassword ? 'is-invalid' : ''}">
                  <button type="button" class="btn bh-btn-outline-neutral" data-toggle-password="cpConfirm">Hiện</button>
                </div>
                <c:if test="${not empty errors.confirmPassword}">
                  <div class="text-danger small mt-1"><c:out value="${errors.confirmPassword}" /></div>
                </c:if>
              </div>

              <button type="submit" class="btn bh-btn-primary">Cập nhật mật khẩu</button>
            </form>
          </section>
        </div>
      </div>
    </div>
  </main>
  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
