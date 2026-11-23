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
        <h1 class="h2 mb-0">Đơn hàng của tôi</h1>
        <div class="d-flex gap-2">
          <a class="btn bh-btn-outline-neutral" href="${pageContext.request.contextPath}/account/profile">Hồ sơ cá nhân</a>
          <a class="btn bh-btn-primary" href="${pageContext.request.contextPath}/account/orders">Đơn hàng của tôi</a>
        </div>
      </div>

      <jsp:include page="/WEB-INF/views/common/messages.jsp" />

      <div class="d-flex flex-wrap gap-2 mb-4">
        <a class="btn btn-sm ${empty selectedStatus ? 'bh-btn-primary' : 'bh-btn-outline-neutral'}"
           href="${pageContext.request.contextPath}/account/orders">Tất cả</a>
        <c:forEach var="st" items="${allStatuses}">
          <a class="btn btn-sm ${selectedStatus == st.name() ? 'bh-btn-primary' : 'bh-btn-outline-neutral'}"
             href="${pageContext.request.contextPath}/account/orders?status=${st.name()}"><c:out value="${st.name()}" /></a>
        </c:forEach>
      </div>

      <c:choose>
        <c:when test="${pageResult.empty}">
          <section class="bh-empty-state">
            <h2 class="h4 mb-2">Bạn chưa có đơn hàng nào</h2>
            <p class="bh-muted mb-4">Khám phá các đầu sách yêu thích và đặt đơn COD nhanh chóng.</p>
            <a class="btn bh-btn-primary" href="${pageContext.request.contextPath}/books">Khám phá danh mục sách</a>
          </section>
        </c:when>
        <c:otherwise>
          <section class="bh-card p-3 p-md-4">
            <div class="table-responsive">
              <table class="table bh-table mb-0">
                <thead>
                  <tr>
                    <th>Mã đơn</th>
                    <th>Ngày đặt</th>
                    <th class="text-center">Số lượng</th>
                    <th class="text-end">Tổng tiền</th>
                    <th>Trạng thái</th>
                    <th class="text-end">Chi tiết</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach var="ord" items="${pageResult.items}">
                    <tr>
                      <td class="bh-mono fw-semibold"><c:out value="${ord.orderCode}" /></td>
                      <td><c:out value="${ord.createdAtFormatted}" /></td>
                      <td class="text-center"><c:out value="${ord.itemCount}" /></td>
                      <td class="text-end bh-price"><c:out value="${ord.formattedTotal}" /></td>
                      <td><span class="bh-badge ${ord.statusBadgeClass}"><c:out value="${ord.statusLabel}" /></span></td>
                      <td class="text-end">
                        <a class="btn btn-sm bh-btn-outline-teal"
                           href="${pageContext.request.contextPath}/account/orders/detail?id=${ord.orderId}">Xem chi tiết</a>
                      </td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>
            <jsp:include page="/WEB-INF/views/common/pagination.jsp" />
          </section>
        </c:otherwise>
      </c:choose>
    </div>
  </main>
  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
