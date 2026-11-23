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
      <nav aria-label="Breadcrumb" class="mb-3 small bh-muted">
        <a href="${pageContext.request.contextPath}/cart">Giỏ hàng</a> / <span>Thanh toán COD</span>
      </nav>
      <h1 class="h2 mb-4">Xác nhận đặt hàng (COD)</h1>
      <jsp:include page="/WEB-INF/views/common/messages.jsp" />

      <c:if test="${cart.hasWarnings()}">
        <div class="bh-alert bh-alert-warning mb-4" role="alert">
          <div>
            <div class="fw-semibold mb-1">Lưu ý thay đổi tồn kho / kinh doanh:</div>
            <ul class="mb-0 ps-3 small">
              <c:forEach var="w" items="${cart.warnings}"><li><c:out value="${w}" /></li></c:forEach>
            </ul>
          </div>
        </div>
      </c:if>

      <form action="${pageContext.request.contextPath}/checkout" method="post" novalidate>
        <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
        <div class="row g-4">
          <div class="col-12 col-lg-7">
            <section class="bh-card p-4 mb-4">
              <h2 class="h4 mb-3">Thông tin giao hàng</h2>
              <div class="mb-3">
                <label class="form-label fw-semibold" for="coName">Họ tên người nhận <span class="text-danger">*</span></label>
                <input id="coName" name="recipientName" type="text" maxlength="100" required
                       class="form-control ${not empty errors.recipientName ? 'is-invalid' : ''}"
                       value="<c:out value='${formRecipientName}' />">
                <c:if test="${not empty errors.recipientName}"><div class="invalid-feedback"><c:out value="${errors.recipientName}" /></div></c:if>
              </div>
              <div class="mb-3">
                <label class="form-label fw-semibold" for="coPhone">Số điện thoại nhận hàng <span class="text-danger">*</span></label>
                <input id="coPhone" name="recipientPhone" type="tel" maxlength="20" required
                       class="form-control ${not empty errors.recipientPhone ? 'is-invalid' : ''}"
                       value="<c:out value='${formRecipientPhone}' />" placeholder="0912345678">
                <c:if test="${not empty errors.recipientPhone}"><div class="invalid-feedback"><c:out value="${errors.recipientPhone}" /></div></c:if>
              </div>
              <div class="mb-3">
                <label class="form-label fw-semibold" for="coAddress">Địa chỉ nhận hàng <span class="text-danger">*</span></label>
                <textarea id="coAddress" name="shippingAddress" rows="3" maxlength="500" required
                          class="form-control ${not empty errors.shippingAddress ? 'is-invalid' : ''}"
                          placeholder="Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành phố"><c:out value="${formShippingAddress}" /></textarea>
                <c:if test="${not empty errors.shippingAddress}"><div class="invalid-feedback"><c:out value="${errors.shippingAddress}" /></div></c:if>
              </div>
              <div class="mb-0">
                <label class="form-label fw-semibold" for="coNote">Ghi chú đơn hàng (tùy chọn)</label>
                <textarea id="coNote" name="note" rows="2" maxlength="500"
                          class="form-control ${not empty errors.note ? 'is-invalid' : ''}"
                          placeholder="VD: Giao giờ hành chính, gọi trước khi giao..."><c:out value="${formNote}" /></textarea>
              </div>
            </section>

            <section class="bh-card p-4">
              <h2 class="h4 mb-3">Phương thức thanh toán</h2>
              <div class="border rounded-3 p-3 d-flex align-items-start gap-3">
                <input id="payCod" type="radio" checked disabled class="form-check-input mt-1">
                <label for="payCod" class="mb-0">
                  <strong class="d-block">Thanh toán khi nhận hàng (COD)</strong>
                  <span class="small bh-muted">Bạn thanh toán tiền mặt trực tiếp cho nhân viên giao hàng khi nhận sách.</span>
                </label>
              </div>
            </section>
          </div>

          <div class="col-12 col-lg-5">
            <aside class="bh-card p-4">
              <h2 class="h4 mb-3">Tóm tắt đơn hàng (<c:out value="${cart.totalQuantity}" /> cuốn)</h2>
              <div class="d-flex flex-column gap-2 mb-3">
                <c:forEach var="item" items="${cart.items}">
                  <div class="d-flex justify-content-between gap-2 small">
                    <span><c:out value="${item.title}" /> &times; <strong><c:out value="${item.quantity}" /></strong></span>
                    <span class="bh-nowrap"><c:out value="${item.formattedLineTotal}" /></span>
                  </div>
                </c:forEach>
              </div>
              <hr>
              <div class="d-flex justify-content-between mb-2 small">
                <span class="bh-muted">Tạm tính</span>
                <span class="bh-nowrap"><c:out value="${cart.formattedSubtotal}" /></span>
              </div>
              <div class="d-flex justify-content-between mb-3 small">
                <span class="bh-muted">Phí vận chuyển cố định</span>
                <span class="bh-nowrap"><c:out value="${cart.formattedShippingFee}" /></span>
              </div>
              <div class="d-flex justify-content-between align-items-center mb-4">
                <span class="fw-semibold">Tổng cộng (COD)</span>
                <span class="bh-price fs-3"><c:out value="${cart.formattedTotalAmount}" /></span>
              </div>
              <button type="submit" class="btn bh-btn-primary w-100 mb-2" ${cart.hasWarnings() ? 'disabled' : ''}>Đặt hàng</button>
              <a class="btn bh-btn-outline-neutral w-100" href="${pageContext.request.contextPath}/cart">&larr; Quay lại giỏ hàng</a>
            </aside>
          </div>
        </div>
      </form>
    </div>
  </main>
  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
