<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <jsp:include page="/WEB-INF/views/common/head.jsp" />
</head>
<body>
  <jsp:include page="/WEB-INF/views/admin/layout-start.jsp" />

  <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
    <div>
      <h1 class="h2 mb-1">Quản lý sách</h1>
      <p class="small bh-muted mb-0">Tổng cộng <c:out value="${pageResult.totalItems}" /> đầu sách khớp bộ lọc.</p>
    </div>
    <a class="btn bh-btn-primary" href="${pageContext.request.contextPath}/admin/books/form">+ Thêm sách mới</a>
  </div>

  <section class="bh-card p-3 p-md-4 mb-4">
    <form action="${pageContext.request.contextPath}/admin/books" method="get" class="row g-2 align-items-end">
      <div class="col-12 col-md-4">
        <label class="form-label small fw-semibold" for="abKw">Tìm kiếm</label>
        <input id="abKw" name="keyword" type="search" class="form-control"
               value="<c:out value='${param.keyword}' />" placeholder="Tên sách, tác giả, ISBN...">
      </div>
      <div class="col-6 col-md-3">
        <label class="form-label small fw-semibold" for="abCat">Danh mục</label>
        <select id="abCat" name="categoryId" class="form-select">
          <option value="">Tất cả danh mục</option>
          <c:forEach var="cat" items="${categories}">
            <option value="${cat.categoryId}" ${param.categoryId == cat.categoryId.toString() ? 'selected' : ''}>
              <c:out value="${cat.name}" />
            </option>
          </c:forEach>
        </select>
      </div>
      <div class="col-6 col-md-3">
        <label class="form-label small fw-semibold" for="abStatus">Trạng thái</label>
        <select id="abStatus" name="status" class="form-select">
          <option value="">Tất cả</option>
          <option value="active" ${param.status == 'active' ? 'selected' : ''}>Đang bán</option>
          <option value="inactive" ${param.status == 'inactive' ? 'selected' : ''}>Ngừng kinh doanh</option>
        </select>
      </div>
      <div class="col-12 col-md-2 d-flex gap-2">
        <button type="submit" class="btn bh-btn-primary flex-grow-1">Lọc</button>
        <a class="btn bh-btn-outline-neutral" href="${pageContext.request.contextPath}/admin/books">Xóa</a>
      </div>
    </form>
  </section>

  <section class="bh-card p-3 p-md-4">
    <div class="table-responsive">
      <table class="table bh-table mb-0">
        <thead>
          <tr>
            <th>Mã / Sách</th>
            <th>Danh mục</th>
            <th class="text-end">Giá bán</th>
            <th class="text-center">Tồn kho</th>
            <th>Trạng thái</th>
            <th class="text-end">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="b" items="${pageResult.items}">
            <tr>
              <td>
                <div class="fw-semibold"><c:out value="${b.title}" /></div>
                <div class="small bh-muted">
                  <span class="bh-mono"><c:out value="${b.bookCode}" /></span>
                  &middot; <c:out value="${b.author}" />
                  <c:if test="${not empty b.isbn}"> &middot; ISBN: <span class="bh-mono"><c:out value="${b.isbn}" /></span></c:if>
                </div>
              </td>
              <td><c:out value="${b.categoryName}" /></td>
              <td class="text-end bh-price"><c:out value="${b.formattedPrice}" /></td>
              <td class="text-center">
                <span class="bh-badge ${b.stockQty > 0 ? 'bh-badge-teal' : 'bh-badge-neutral'}"><c:out value="${b.stockQty}" /></span>
              </td>
              <td>
                <c:choose>
                  <c:when test="${b.active}"><span class="bh-badge bh-badge-success">Đang bán</span></c:when>
                  <c:otherwise><span class="bh-badge bh-badge-neutral">Ngừng bán</span></c:otherwise>
                </c:choose>
              </td>
              <td class="text-end bh-nowrap">
                <a class="btn btn-sm bh-btn-outline-neutral me-1"
                   href="${pageContext.request.contextPath}/admin/books/form?id=${b.bookId}">Sửa</a>
                <form action="${pageContext.request.contextPath}/admin/books" method="post" class="d-inline">
                  <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
                  <input type="hidden" name="action" value="toggleActive">
                  <input type="hidden" name="bookId" value="${b.bookId}">
                  <button type="submit"
                          class="btn btn-sm ${b.active ? 'bh-btn-danger-outline' : 'bh-btn-outline-teal'}"
                          data-confirm-title="${b.active ? 'Ngừng kinh doanh sách' : 'Mở bán lại sách'}"
                          data-confirm-message="${b.active ? 'Sách sẽ bị ẩn khỏi storefront công khai nhưng lịch sử đơn hàng cũ vẫn được giữ nguyên.' : 'Mở bán lại cuốn sách này trên cửa hàng?'}"
                          data-confirm-ok="${b.active ? 'Ngừng bán' : 'Mở bán'}">
                    ${b.active ? 'Ngừng bán' : 'Mở bán'}
                  </button>
                </form>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${pageResult.empty}">
            <tr><td colspan="6" class="text-center py-4 bh-muted">Không có sách nào khớp bộ lọc hiện tại.</td></tr>
          </c:if>
        </tbody>
      </table>
    </div>
    <jsp:include page="/WEB-INF/views/common/pagination.jsp" />
  </section>

  <jsp:include page="/WEB-INF/views/admin/layout-end.jsp" />
</body>
</html>
