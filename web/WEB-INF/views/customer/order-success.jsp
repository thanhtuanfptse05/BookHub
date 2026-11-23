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
      <section class="bh-card p-4 p-md-5 text-center mx-auto">
        <div class="bh-badge bh-badge-success mb-3">&#10003; Đặt hàng thành công</div>
        <h1 class="h2 mb-2">Cảm ơn bạn đã đặt sách tại BookHub!</h1>
        <p class="bh-muted mb-4">
          Mã đơn hàng của bạn là <strong class="bh-mono text-dark"><c:out value="${order.orderCode}" /></strong>
          &middot; Trạng thái: <span class="bh-badge ${order.statusBadgeClass}"><c:out value="${order.statusLabel}" /></span>
        </p>

        <div class="border rounded-3 p-3 text-start mb-4 small">
          <div><strong>Người nhận:</strong> <c:out value="${order.recipientName}" /> (<c:out value="${order.recipientPhone}" />)</div>
          <div><strong>Địa chỉ:</strong> <c:out value="${order.shippingAddress}" /></div>
          <div><strong>Thanh toán:</strong> COD &middot; Tổng thanh toán: <strong class="bh-price"><c:out value="${order.formattedTotalAmount}" /></strong></div>
        </div>

        <div class="d-flex justify-content-center gap-2 flex-wrap">
          <a class="btn bh-btn-primary" href="${pageContext.request.contextPath}/account/orders/detail?id=${order.orderId}">Xem chi tiết đơn hàng</a>
          <a class="btn bh-btn-outline-teal" href="${pageContext.request.contextPath}/books">Tiếp tục mua sách</a>
        </div>
      </section>
    </div>
  </main>
  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
