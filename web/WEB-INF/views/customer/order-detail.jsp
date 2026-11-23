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
      <div class="mb-3">
        <a class="small text-decoration-none bh-muted" href="${pageContext.request.contextPath}/account/orders">&larr; Đơn hàng của tôi</a>
      </div>

      <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
        <div>
          <h1 class="h2 mb-1">Đơn hàng <span class="bh-mono"><c:out value="${order.orderCode}" /></span></h1>
          <div class="small bh-muted">
            Đặt lúc <c:out value="${order.createdAtFormatted}" />
            <c:if test="${order.status.name() == 'CANCELLED'}">
              &middot; Đã hủy lúc <c:out value="${order.cancelledAtFormatted}" />
            </c:if>
          </div>
        </div>
        <div class="d-flex align-items-center gap-2">
          <span class="bh-badge ${order.statusBadgeClass}"><c:out value="${order.statusLabel}" /></span>
          <c:if test="${order.canCustomerCancel}">
            <form action="${pageContext.request.contextPath}/account/orders/cancel" method="post" class="m-0">
              <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
              <input type="hidden" name="orderId" value="${order.orderId}">
              <button type="submit" class="btn bh-btn-danger-outline"
                      data-confirm-title="Xác nhận hủy đơn hàng"
                      data-confirm-message="Bạn chắc chắn muốn hủy đơn ${order.orderCode}? Tồn kho sẽ được hoàn lại tự động."
                      data-confirm-ok="Xác nhận hủy đơn">Hủy đơn hàng</button>
            </form>
          </c:if>
        </div>
      </div>

      <jsp:include page="/WEB-INF/views/common/messages.jsp" />

      <!-- 4-step Order Stepper (SCR-12 / SCR-12b) -->
      <c:if test="${order.status.name() != 'CANCELLED'}">
        <section class="bh-stepper mb-4" aria-label="Tiến trình đơn hàng">
          <c:set var="st" value="${order.status.name()}" />
          <div class="bh-step-item ${st == 'PENDING' ? 'current' : 'done'}">1. Chờ xác nhận</div>
          <div class="bh-step-item ${st == 'CONFIRMED' ? 'current' : (st == 'SHIPPING' or st == 'DELIVERED' ? 'done' : '')}">2. Đã xác nhận</div>
          <div class="bh-step-item ${st == 'SHIPPING' ? 'current' : (st == 'DELIVERED' ? 'done' : '')}">3. Đang giao hàng</div>
          <div class="bh-step-item ${st == 'DELIVERED' ? 'done' : ''}">4. Đã giao hàng</div>
        </section>
      </c:if>

      <div class="row g-4 mb-4">
        <div class="col-12 col-md-6">
          <section class="bh-card p-4 h-100">
            <h2 class="h5 mb-3">Thông tin nhận hàng</h2>
            <div class="small d-flex flex-column gap-1">
              <div><strong>Người nhận:</strong> <c:out value="${order.recipientName}" /></div>
              <div><strong>Số điện thoại:</strong> <c:out value="${order.recipientPhone}" /></div>
              <div><strong>Địa chỉ:</strong> <c:out value="${order.shippingAddress}" /></div>
              <c:if test="${not empty order.note}">
                <div><strong>Ghi chú:</strong> <c:out value="${order.note}" /></div>
              </c:if>
            </div>
          </section>
        </div>
        <div class="col-12 col-md-6">
          <section class="bh-card p-4 h-100">
            <h2 class="h5 mb-3">Thông tin thanh toán</h2>
            <div class="small d-flex flex-column gap-1">
              <div><strong>Phương thức:</strong> Thanh toán khi nhận hàng (<c:out value="${order.paymentMethod}" />)</div>
              <div><strong>Tạm tính:</strong> <span class="bh-nowrap"><c:out value="${order.formattedSubtotal}" /></span></div>
              <div><strong>Phí giao hàng:</strong> <span class="bh-nowrap"><c:out value="${order.formattedShippingFee}" /></span></div>
              <div class="fs-6 pt-1"><strong>Tổng cộng:</strong> <span class="bh-price"><c:out value="${order.formattedTotalAmount}" /></span></div>
            </div>
          </section>
        </div>
      </div>

      <section class="bh-card p-4">
        <h2 class="h5 mb-3">Danh sách sách trong đơn (Snapshot lúc đặt)</h2>
        <div class="table-responsive">
          <table class="table bh-table mb-0">
            <thead>
              <tr>
                <th>Tên sách</th>
                <th class="text-end">Đơn giá</th>
                <th class="text-center">SL</th>
                <th class="text-end">Thành tiền</th>
                <c:if test="${order.status.name() == 'DELIVERED'}">
                  <th class="text-end">Đánh giá</th>
                </c:if>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="item" items="${order.items}">
                <tr>
                  <td class="fw-semibold">
                    <a class="text-decoration-none text-dark" href="${pageContext.request.contextPath}/books/detail?id=${item.bookId}">
                      <c:out value="${item.bookTitleSnapshot}" />
                    </a>
                  </td>
                  <td class="text-end bh-nowrap"><c:out value="${item.formattedUnitPrice}" /></td>
                  <td class="text-center"><c:out value="${item.quantity}" /></td>
                  <td class="text-end bh-price"><c:out value="${item.formattedLineTotal}" /></td>
                  <c:if test="${order.status.name() == 'DELIVERED'}">
                    <td class="text-end bh-nowrap">
                      <c:choose>
                        <c:when test="${item.canWriteReview}">
                          <a class="btn btn-sm bh-btn-outline-teal"
                             href="${pageContext.request.contextPath}/books/detail?id=${item.bookId}#reviews">Viết đánh giá</a>
                        </c:when>
                        <c:otherwise>
                          <span class="small bh-muted">Đã đánh giá</span>
                        </c:otherwise>
                      </c:choose>
                    </td>
                  </c:if>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </main>
  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
