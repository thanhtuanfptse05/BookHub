<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <jsp:include page="/WEB-INF/views/common/head.jsp" />
</head>
<body>
  <jsp:include page="/WEB-INF/views/admin/layout-start.jsp" />

  <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-4">
    <div>
      <h1 class="h2 mb-1">Tổng quan cửa hàng</h1>
      <p class="small bh-muted mb-0">Chỉ số thời gian thực từ SQL Server BookHubDB.</p>
    </div>
    <a class="btn bh-btn-outline-teal" href="${pageContext.request.contextPath}/admin/reports">Xem báo cáo chi tiết &rarr;</a>
  </div>

  <!-- 4 Primary KPI Cards (SCR-14) -->
  <section class="row g-3 mb-4" aria-label="Chỉ số KPI chính">
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="bh-card p-4 h-100">
        <div class="d-flex justify-content-between align-items-start gap-2 mb-2">
          <span class="small bh-muted">Doanh thu đã giao (DELIVERED)</span>
          <span class="bh-kpi-icon" aria-hidden="true">&#8363;</span>
        </div>
        <div class="bh-kpi-value"><c:out value="${dashboard.formattedDeliveredRevenue}" /></div>
        <div class="small bh-muted mt-1"><c:out value="${dashboard.deliveredOrders}" /> đơn đã giao thành công</div>
      </div>
    </div>
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="bh-card p-4 h-100">
        <div class="d-flex justify-content-between align-items-start gap-2 mb-2">
          <span class="small bh-muted">Tổng đơn hàng</span>
          <span class="bh-kpi-icon" aria-hidden="true">#</span>
        </div>
        <div class="bh-kpi-value"><c:out value="${dashboard.totalOrders}" /></div>
        <div class="mt-1">
          <span class="bh-badge bh-badge-warning"><c:out value="${dashboard.pendingOrders}" /> đơn chờ xác nhận</span>
        </div>
      </div>
    </div>
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="bh-card p-4 h-100">
        <div class="d-flex justify-content-between align-items-start gap-2 mb-2">
          <span class="small bh-muted">Sách đang kinh doanh</span>
          <span class="bh-kpi-icon" aria-hidden="true">B</span>
        </div>
        <div class="bh-kpi-value"><c:out value="${dashboard.activeBooks}" /></div>
        <div class="small bh-muted mt-1">Đầu sách is_active = 1</div>
      </div>
    </div>
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="bh-card p-4 h-100">
        <div class="d-flex justify-content-between align-items-start gap-2 mb-2">
          <span class="small bh-muted">Tổng khách hàng</span>
          <span class="bh-kpi-icon" aria-hidden="true">U</span>
        </div>
        <div class="bh-kpi-value"><c:out value="${dashboard.totalCustomers}" /></div>
        <div class="small bh-muted mt-1">Tài khoản vai trò CUSTOMER</div>
      </div>
    </div>
  </section>

  <!-- 2 Moderation Queue Cards -->
  <section class="row g-3 mb-4" aria-label="Hàng chờ kiểm duyệt">
    <div class="col-12 col-md-6">
      <div class="bh-alert bh-alert-warning align-items-center">
        <div>
          <div class="fw-semibold">Đánh giá chờ duyệt: <c:out value="${dashboard.pendingReviews}" /></div>
          <div class="small">Duyệt đánh giá mới để hiển thị công khai và cập nhật điểm sao sách.</div>
        </div>
        <a class="btn btn-sm bh-btn-outline-neutral" href="${pageContext.request.contextPath}/admin/reviews?status=PENDING">Xử lý ngay</a>
      </div>
    </div>
    <div class="col-12 col-md-6">
      <div class="bh-alert bh-alert-warning align-items-center">
        <div>
          <div class="fw-semibold">Câu hỏi Q&amp;A chưa trả lời: <c:out value="${dashboard.openQuestions}" /></div>
          <div class="small">Phản hồi chính thức cho khách hàng trên trang chi tiết sách.</div>
        </div>
        <a class="btn btn-sm bh-btn-outline-neutral" href="${pageContext.request.contextPath}/admin/questions?status=OPEN">Trả lời ngay</a>
      </div>
    </div>
  </section>

  <!-- Recent Orders + Top 5 Bestsellers -->
  <div class="row g-4">
    <div class="col-12 col-xxl-7">
      <section class="bh-card p-4 h-100">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h2 class="h5 mb-0">5 đơn hàng gần đây</h2>
          <a class="small text-decoration-none" href="${pageContext.request.contextPath}/admin/orders">Xem tất cả &rsaquo;</a>
        </div>
        <div class="table-responsive">
          <table class="table table-sm bh-table mb-0">
            <thead>
              <tr><th>Mã đơn</th><th>Khách hàng</th><th class="text-end">Tổng tiền</th><th>Trạng thái</th></tr>
            </thead>
            <tbody>
              <c:forEach var="ord" items="${dashboard.recentOrders}">
                <tr>
                  <td class="bh-mono fw-semibold">
                    <a class="text-decoration-none" href="${pageContext.request.contextPath}/admin/orders?detailId=${ord.orderId}">
                      <c:out value="${ord.orderCode}" />
                    </a>
                  </td>
                  <td><c:out value="${ord.customerName}" /></td>
                  <td class="text-end bh-price"><c:out value="${ord.formattedTotal}" /></td>
                  <td><span class="bh-badge ${ord.statusBadgeClass}"><c:out value="${ord.statusLabel}" /></span></td>
                </tr>
              </c:forEach>
              <c:if test="${empty dashboard.recentOrders}">
                <tr><td colspan="4" class="text-center py-3 bh-muted">Chưa có đơn hàng nào.</td></tr>
              </c:if>
            </tbody>
          </table>
        </div>
      </section>
    </div>

    <div class="col-12 col-xxl-5">
      <section class="bh-card p-4 h-100">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h2 class="h5 mb-0">Top 5 sách bán chạy (DELIVERED)</h2>
          <a class="small text-decoration-none" href="${pageContext.request.contextPath}/admin/reports">Báo cáo &rsaquo;</a>
        </div>
        <div class="table-responsive">
          <table class="table table-sm bh-table mb-0">
            <thead>
              <tr><th>Sách</th><th class="text-center">Đã bán</th><th class="text-end">Doanh số</th></tr>
            </thead>
            <tbody>
              <c:forEach var="row" items="${dashboard.topBestsellers}">
                <tr>
                  <td>
                    <div class="fw-semibold"><c:out value="${row.title}" /></div>
                    <div class="small bh-muted"><c:out value="${row.author}" /></div>
                  </td>
                  <td class="text-center"><span class="bh-badge bh-badge-teal"><c:out value="${row.unitsSold}" /></span></td>
                  <td class="text-end bh-price"><c:out value="${row.formattedRevenue}" /></td>
                </tr>
              </c:forEach>
              <c:if test="${empty dashboard.topBestsellers}">
                <tr><td colspan="3" class="text-center py-3 bh-muted">Chưa có dữ liệu sách bán chạy.</td></tr>
              </c:if>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </div>

  <jsp:include page="/WEB-INF/views/admin/layout-end.jsp" />
</body>
</html>
