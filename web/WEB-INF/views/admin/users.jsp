<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <jsp:include page="/WEB-INF/views/common/head.jsp" />
</head>
<body>
  <jsp:include page="/WEB-INF/views/admin/layout-start.jsp" />

  <h1 class="h2 mb-4">Quản lý người dùng</h1>

  <section class="bh-card p-3 p-md-4 mb-4">
    <form action="${pageContext.request.contextPath}/admin/users" method="get" class="row g-2 align-items-end">
      <div class="col-12 col-md-5">
        <label class="form-label small fw-semibold" for="auKw">Tìm kiếm tài khoản</label>
        <input id="auKw" name="keyword" type="search" class="form-control"
               value="<c:out value='${param.keyword}' />" placeholder="Họ tên, email, số điện thoại...">
      </div>
      <div class="col-6 col-md-4">
        <label class="form-label small fw-semibold" for="auStatus">Trạng thái</label>
        <select id="auStatus" name="status" class="form-select">
          <option value="">Tất cả trạng thái</option>
          <c:forEach var="st" items="${allStatuses}">
            <option value="${st.name()}" ${param.status == st.name() ? 'selected' : ''}><c:out value="${st.name()}" /></option>
          </c:forEach>
        </select>
      </div>
      <div class="col-6 col-md-3 d-flex gap-2">
        <button type="submit" class="btn bh-btn-primary flex-grow-1">Lọc</button>
        <a class="btn bh-btn-outline-neutral" href="${pageContext.request.contextPath}/admin/users">Xóa</a>
      </div>
    </form>
  </section>

  <section class="bh-card p-3 p-md-4">
    <div class="table-responsive">
      <table class="table bh-table mb-0">
        <thead>
          <tr>
            <th>Người dùng</th>
            <th>Điện thoại</th>
            <th>Vai trò</th>
            <th>Trạng thái</th>
            <th>Ngày tạo</th>
            <th class="text-end">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="u" items="${pageResult.items}">
            <tr>
              <td>
                <div class="fw-semibold"><c:out value="${u.fullName}" /></div>
                <div class="small bh-muted"><c:out value="${u.email}" /></div>
              </td>
              <td><c:out value="${not empty u.phone ? u.phone : '—'}" /></td>
              <td>
                <span class="bh-badge ${u.roleCode.name() == 'ADMIN' ? 'bh-badge-info' : 'bh-badge-neutral'}">
                  <c:out value="${u.roleCode}" />
                </span>
              </td>
              <td>
                <c:choose>
                  <c:when test="${u.status.name() == 'ACTIVE'}"><span class="bh-badge bh-badge-success">Hoạt động</span></c:when>
                  <c:when test="${u.status.name() == 'LOCKED'}"><span class="bh-badge bh-badge-danger">Đã khóa</span></c:when>
                  <c:otherwise><span class="bh-badge bh-badge-neutral">Vô hiệu</span></c:otherwise>
                </c:choose>
              </td>
              <td class="small bh-muted"><c:out value="${u.createdAtFormatted}" /></td>
              <td class="text-end bh-nowrap">
                <c:choose>
                  <c:when test="${u.userId == sessionScope.currentUser.userId}">
                    <span class="small bh-muted">Tài khoản hiện tại</span>
                  </c:when>
                  <c:when test="${u.status.name() == 'ACTIVE'}">
                    <form action="${pageContext.request.contextPath}/admin/users" method="post" class="d-inline">
                      <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
                      <input type="hidden" name="userId" value="${u.userId}">
                      <input type="hidden" name="action" value="lock">
                      <button type="submit" class="btn btn-sm bh-btn-danger-outline"
                              data-confirm-title="Khóa tài khoản"
                              data-confirm-message="Khóa tài khoản ${u.email}? Người dùng sẽ không thể đăng nhập cho đến khi được mở khóa."
                              data-confirm-ok="Khóa tài khoản">Khóa</button>
                    </form>
                  </c:when>
                  <c:otherwise>
                    <form action="${pageContext.request.contextPath}/admin/users" method="post" class="d-inline">
                      <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
                      <input type="hidden" name="userId" value="${u.userId}">
                      <input type="hidden" name="action" value="unlock">
                      <button type="submit" class="btn btn-sm bh-btn-outline-teal">Mở khóa</button>
                    </form>
                  </c:otherwise>
                </c:choose>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty pageResult.items}">
            <tr><td colspan="6" class="text-center py-4 bh-muted">Không có tài khoản nào khớp bộ lọc.</td></tr>
          </c:if>
        </tbody>
      </table>
    </div>
    <jsp:include page="/WEB-INF/views/common/pagination.jsp" />
  </section>

  <jsp:include page="/WEB-INF/views/admin/layout-end.jsp" />
</body>
</html>
