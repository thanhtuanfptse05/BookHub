<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <jsp:include page="/WEB-INF/views/common/head.jsp" />
</head>
<body>
  <jsp:include page="/WEB-INF/views/admin/layout-start.jsp" />

  <h1 class="h2 mb-3">Báo cáo thống kê (SQL Aggregates)</h1>

  <!-- Date Range Filter Card (SCR-22) -->
  <section class="bh-card p-3 p-md-4 mb-4">
    <c:if test="${not empty dateError}">
      <div class="bh-alert bh-alert-warning mb-3" role="alert">
        <div><c:out value="${dateError}" /></div>
      </div>
    </c:if>
    <form action="${pageContext.request.contextPath}/admin/reports" method="get" class="row g-2 align-items-end">
      <div class="col-6 col-md-4">
        <label class="form-label small fw-semibold" for="repFrom">Từ ngày (Orders.created_at)</label>
        <input id="repFrom" name="fromDate" type="date" class="form-control" value="<c:out value='${param.fromDate}' />">
      </div>
      <div class="col-6 col-md-4">
        <label class="form-label small fw-semibold" for="repTo">Đến ngày (gồm cả cuối ngày)</label>
        <input id="repTo" name="toDate" type="date" class="form-control" value="<c:out value='${param.toDate}' />">
      </div>
      <div class="col-12 col-md-4 d-flex gap-2">
        <button type="submit" class="btn bh-btn-primary flex-grow-1">Xem báo cáo</button>
        <a class="btn bh-btn-outline-neutral" href="${pageContext.request.contextPath}/admin/reports">Toàn thời gian</a>
      </div>
    </form>
  </section>

  <!-- Scope Disclosure Notice (FR-08) -->
  <div class="bh-alert bh-alert-info mb-4 small">
    <div>
      <strong>Phạm vi số liệu:</strong> Doanh thu (<code>SUM</code>), giá trị đơn trung bình (<code>AVG</code>) và Top bán chạy (<code>SUM(quantity)</code>)
      <strong>chỉ tính đơn ở trạng thái DELIVERED</strong> lọc theo <code>Orders.created_at</code>.
      Thống kê giá sách (<code>MIN</code>/<code>MAX</code>/<code>AVG</code>) và tổng tồn kho (<code>SUM(stock_qty)</code>)
      <strong>chỉ tính sách đang kinh doanh (Books.is_active = 1)</strong>. Khi không có dữ liệu hiển thị <code>0 &#8363;</code> (không chia cho 0).
    </div>
  </div>

  <!-- 4 Aggregate KPI Cards -->
  <section class="row g-3 mb-4">
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="bh-card p-4 h-100">
        <div class="small bh-muted mb-2">Doanh thu đã giao <span class="bh-mono">(SUM)</span></div>
        <div class="bh-kpi-value"><c:out value="${report.formattedDeliveredRevenue}" /></div>
        <div class="small bh-muted mt-1"><c:out value="${report.deliveredOrderCount}" /> đơn DELIVERED</div>
      </div>
    </div>
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="bh-card p-4 h-100">
        <div class="small bh-muted mb-2">Giá trị đơn TB <span class="bh-mono">(AVG)</span></div>
        <div class="bh-kpi-value"><c:out value="${report.formattedAvgOrderValue}" /></div>
        <div class="small bh-muted mt-1">Trên đơn DELIVERED</div>
      </div>
    </div>
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="bh-card p-4 h-100">
        <div class="small bh-muted mb-2">Tổng tồn kho <span class="bh-mono">(SUM stock_qty)</span></div>
        <div class="bh-kpi-value"><c:out value="${report.totalActiveStockUnits}" /></div>
        <div class="small bh-muted mt-1">Trên <c:out value="${report.activeBookCount}" /> sách đang bán</div>
      </div>
    </div>
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="bh-card p-4 h-100">
        <div class="small bh-muted mb-2">Tổng đơn trong kỳ <span class="bh-mono">(COUNT)</span></div>
        <div class="bh-kpi-value"><c:out value="${report.totalOrdersInRange}" /></div>
        <div class="small bh-muted mt-1">Cả 5 trạng thái đơn</div>
      </div>
    </div>
  </section>

  <!-- Active Book Price Stats (MIN / MAX / AVG) -->
  <section class="bh-card p-4 mb-4">
    <h2 class="h5 mb-3">Thống kê giá sách đang kinh doanh (<code>Books.is_active = 1</code>)</h2>
    <div class="row g-3">
      <div class="col-12 col-md-4">
        <div class="border rounded-3 p-3">
          <div class="small bh-muted">Giá thấp nhất (<code>MIN</code>)</div>
          <div class="bh-kpi-value mt-1"><c:out value="${report.formattedMinBookPrice}" /></div>
        </div>
      </div>
      <div class="col-12 col-md-4">
        <div class="border rounded-3 p-3">
          <div class="small bh-muted">Giá trung bình (<code>AVG</code>)</div>
          <div class="bh-kpi-value mt-1"><c:out value="${report.formattedAvgBookPrice}" /></div>
        </div>
      </div>
      <div class="col-12 col-md-4">
        <div class="border rounded-3 p-3">
          <div class="small bh-muted">Giá cao nhất (<code>MAX</code>)</div>
          <div class="bh-kpi-value mt-1"><c:out value="${report.formattedMaxBookPrice}" /></div>
        </div>
      </div>
    </div>
  </section>

  <!-- Order Status COUNT Bars + Top 10 Bestsellers -->
  <div class="row g-4">
    <div class="col-12 col-xl-5">
      <section class="bh-card p-4 h-100">
        <h2 class="h5 mb-3">Số đơn theo trạng thái (<code>COUNT GROUP BY status</code>)</h2>
        <div class="d-flex flex-column gap-3">
          <c:forEach var="st" items="${report.statusCounts}">
            <div>
              <div class="d-flex justify-content-between align-items-center mb-1 small">
                <span class="bh-badge ${st.badgeClass}"><c:out value="${st.label}" /></span>
                <span><strong><c:out value="${st.count}" /></strong> đơn (<c:out value="${st.percentage}" />%)</span>
              </div>
              <div class="bh-bar-track" role="progressbar" aria-valuenow="${st.percentage}" aria-valuemin="0" aria-valuemax="100" aria-label="${st.label}">
                <div class="bh-bar-fill" style="width:${st.percentage}%"><!-- ui-check:ignore --></div>
              </div>
            </div>
          </c:forEach>
        </div>
      </section>
    </div>

    <div class="col-12 col-xl-7">
      <section class="bh-card p-4 h-100">
        <h2 class="h5 mb-3">Top 10 sách bán chạy (<code>SUM(quantity)</code> đơn DELIVERED)</h2>
        <div class="table-responsive">
          <table class="table table-sm bh-table mb-0">
            <thead>
              <tr><th>Mã</th><th>Tên sách</th><th class="text-center">SL bán</th><th class="text-end">Doanh số</th></tr>
            </thead>
            <tbody>
              <c:forEach var="row" items="${report.topBestsellers}">
                <tr>
                  <td class="bh-mono small bh-muted"><c:out value="${row.bookCode}" /></td>
                  <td>
                    <div class="fw-semibold"><c:out value="${row.title}" /></div>
                    <div class="small bh-muted"><c:out value="${row.author}" /></div>
                  </td>
                  <td class="text-center"><span class="bh-badge bh-badge-teal"><c:out value="${row.unitsSold}" /></span></td>
                  <td class="text-end bh-price"><c:out value="${row.formattedRevenue}" /></td>
                </tr>
              </c:forEach>
              <c:if test="${empty report.topBestsellers}">
                <tr><td colspan="4" class="text-center py-4 bh-muted">Chưa có đơn hàng DELIVERED nào trong khoảng thời gian đã chọn.</td></tr>
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
