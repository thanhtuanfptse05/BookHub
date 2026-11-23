<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <jsp:include page="/WEB-INF/views/common/head.jsp" />
</head>
<body>
  <jsp:include page="/WEB-INF/views/admin/layout-start.jsp" />

  <h1 class="h2 mb-4">Quản lý đơn hàng</h1>

  <section class="bh-card p-3 p-md-4 mb-4">
    <form action="${pageContext.request.contextPath}/admin/orders" method="get" class="row g-2 align-items-end">
      <div class="col-12 col-md-3">
        <label class="form-label small fw-semibold" for="aoCode">Mã đơn</label>
        <input id="aoCode" name="orderCode" type="search" class="form-control bh-mono"
               value="<c:out value='${param.orderCode}' />" placeholder="BH-2026...">
      </div>
      <div class="col-6 col-md-2">
        <label class="form-label small fw-semibold" for="aoStatus">Trạng thái</label>
        <select id="aoStatus" name="status" class="form-select">
          <option value="">Tất cả</option>
          <c:forEach var="st" items="${allStatuses}">
            <option value="${st.name()}" ${param.status == st.name() ? 'selected' : ''}><c:out value="${st.name()}" /></option>
          </c:forEach>
        </select>
      </div>
      <div class="col-6 col-md-2">
        <label class="form-label small fw-semibold" for="aoFrom">Từ ngày</label>
        <input id="aoFrom" name="fromDate" type="date" class="form-control" value="<c:out value='${param.fromDate}' />">
      </div>
      <div class="col-6 col-md-2">
        <label class="form-label small fw-semibold" for="aoTo">Đến ngày</label>
        <input id="aoTo" name="toDate" type="date" class="form-control" value="<c:out value='${param.toDate}' />">
      </div>
      <div class="col-6 col-md-3 d-flex gap-2">
        <button type="submit" class="btn bh-btn-primary flex-grow-1">Lọc</button>
        <a class="btn bh-btn-outline-neutral" href="${pageContext.request.contextPath}/admin/orders">Xóa</a>
      </div>
    </form>
  </section>

  <c:if test="${not empty selectedOrder}">
    <section class="bh-card p-4 mb-4">
      <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
        <div>
          <h2 class="h4 mb-0">Chi tiết đơn <span class="bh-mono"><c:out value="${selectedOrder.orderCode}" /></span></h2>
          <div class="small bh-muted">Khách: <c:out value="${selectedOrder.customerName}" /> (<c:out value="${selectedOrder.customerEmail}" />) &middot; <c:out value="${selectedOrder.createdAtFormatted}" /></div>
        </div>
        <span class="bh-badge ${selectedOrder.statusBadgeClass}"><c:out value="${selectedOrder.statusLabel}" /></span>
      </div>

      <div class="row g-4">
        <div class="col-12 col-lg-8">
          <div class="small mb-3">
            <strong>Người nhận:</strong> <c:out value="${selectedOrder.recipientName}" /> &middot; <c:out value="${selectedOrder.recipientPhone}" /><br>
            <strong>Địa chỉ:</strong> <c:out value="${selectedOrder.shippingAddress}" />
            <c:if test="${not empty selectedOrder.note}"><br><strong>Ghi chú:</strong> <c:out value="${selectedOrder.note}" /></c:if>
          </div>
          <div class="table-responsive">
            <table class="table table-sm bh-table mb-0">
              <thead>
                <tr><th>Tên sách (Snapshot)</th><th class="text-end">Đơn giá</th><th class="text-center">SL</th><th class="text-end">Thành tiền</th></tr>
              </thead>
              <tbody>
                <c:forEach var="item" items="${selectedOrder.items}">
                  <tr>
                    <td><c:out value="${item.bookTitleSnapshot}" /></td>
                    <td class="text-end bh-nowrap"><c:out value="${item.formattedUnitPrice}" /></td>
                    <td class="text-center"><c:out value="${item.quantity}" /></td>
                    <td class="text-end bh-price"><c:out value="${item.formattedLineTotal}" /></td>
                  </tr>
                </c:forEach>
              </tbody>
              <tfoot>
                <tr><td colspan="3" class="text-end small bh-muted">Tạm tính + Phí ship (<c:out value="${selectedOrder.formattedShippingFee}" />)</td><td class="text-end bh-price"><c:out value="${selectedOrder.formattedTotalAmount}" /></td></tr>
              </tfoot>
            </table>
          </div>
        </div>

        <div class="col-12 col-lg-4">
          <div class="border rounded-3 p-3">
            <h3 class="h6 fw-bold mb-2">Chuyển trạng thái đơn hàng</h3>
            <c:choose>
              <c:when test="${empty selectedOrder.allowedAdminTransitions}">
                <p class="small bh-muted mb-0">Đơn hàng đã ở trạng thái kết thúc (<strong><c:out value="${selectedOrder.statusLabel}" /></strong>), không thể chuyển tiếp.</p>
              </c:when>
              <c:otherwise>
                <div class="d-flex flex-column gap-2">
                  <c:forEach var="nextSt" items="${selectedOrder.allowedAdminTransitions}">
                    <form action="${pageContext.request.contextPath}/admin/orders" method="post" class="m-0">
                      <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
                      <input type="hidden" name="orderId" value="${selectedOrder.orderId}">
                      <input type="hidden" name="targetStatus" value="${nextSt.name()}">
                      <c:choose>
                        <c:when test="${nextSt.name() == 'CANCELLED'}">
                          <button type="submit" class="btn bh-btn-danger-outline w-100"
                                  data-confirm-title="Hủy đơn hàng ${selectedOrder.orderCode}"
                                  data-confirm-message="Chuyển đơn sang Đã hủy sẽ hoàn lại tồn kho cho toàn bộ sách trong đơn đúng 1 lần."
                                  data-confirm-ok="Xác nhận hủy">Chuyển sang CANCELLED (Hủy đơn)</button>
                        </c:when>
                        <c:otherwise>
                          <button type="submit" class="btn bh-btn-primary w-100">Chuyển sang <c:out value="${nextSt.name()}" /></button>
                        </c:otherwise>
                      </c:choose>
                    </form>
                  </c:forEach>
                </div>
              </c:otherwise>
            </c:choose>
            <div class="small bh-muted mt-3">
              Quy tắc: PENDING &rarr; CONFIRMED &rarr; SHIPPING &rarr; DELIVERED; chỉ hủy được khi PENDING hoặc CONFIRMED.
            </div>
          </div>
        </div>
      </div>
    </section>
  </c:if>

  <section class="bh-card p-3 p-md-4">
    <div class="table-responsive">
      <table class="table bh-table mb-0">
        <thead>
          <tr>
            <th>Mã đơn</th>
            <th>Khách hàng</th>
            <th>Ngày đặt</th>
            <th class="text-center">SL</th>
            <th class="text-end">Tổng tiền</th>
            <th>Trạng thái</th>
            <th class="text-end">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="ord" items="${pageResult.items}">
            <tr>
              <td class="bh-mono fw-semibold"><c:out value="${ord.orderCode}" /></td>
              <td>
                <div><c:out value="${ord.customerName}" /></div>
                <div class="small bh-muted"><c:out value="${ord.customerEmail}" /></div>
              </td>
              <td><c:out value="${ord.createdAtFormatted}" /></td>
              <td class="text-center"><c:out value="${ord.itemCount}" /></td>
              <td class="text-end bh-price"><c:out value="${ord.formattedTotal}" /></td>
              <td><span class="bh-badge ${ord.statusBadgeClass}"><c:out value="${ord.statusLabel}" /></span></td>
              <td class="text-end">
                <a class="btn btn-sm bh-btn-outline-teal"
                   href="${pageContext.request.contextPath}/admin/orders?detailId=${ord.orderId}">Chi tiết</a>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty pageResult.items}">
            <tr><td colspan="7" class="text-center py-4 bh-muted">Không có đơn hàng nào khớp bộ lọc.</td></tr>
          </c:if>
        </tbody>
      </table>
    </div>
    <jsp:include page="/WEB-INF/views/common/pagination.jsp" />
  </section>

  <jsp:include page="/WEB-INF/views/admin/layout-end.jsp" />
</body>
</html>
