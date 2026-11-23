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
        <a href="${pageContext.request.contextPath}/">Trang chủ</a> / <span>Danh mục sách</span>
      </nav>
      <h1 class="h2 mb-4">Danh mục sách</h1>
      <jsp:include page="/WEB-INF/views/common/messages.jsp" />

      <div class="row g-4">
        <!-- Filter Sidebar -->
        <div class="col-12 col-lg-3">
          <aside class="bh-card p-4">
            <h2 class="h5 mb-3">Bộ lọc tìm kiếm</h2>
            <c:if test="${not empty filterError}">
              <div class="bh-alert bh-alert-warning mb-3 small" role="alert">
                <div><c:out value="${filterError}" /></div>
              </div>
            </c:if>
            <form action="${pageContext.request.contextPath}/books" method="get">
              <div class="mb-3">
                <label class="form-label small fw-semibold" for="fKeyword">Từ khóa</label>
                <input id="fKeyword" name="keyword" type="text" class="form-control"
                       value="<c:out value='${criteria.keyword}' />" placeholder="Tên sách, tác giả, ISBN">
              </div>
              <div class="mb-3">
                <label class="form-label small fw-semibold" for="fCategory">Thể loại</label>
                <select id="fCategory" name="categoryId" class="form-select">
                  <option value="">Tất cả thể loại</option>
                  <c:forEach var="cat" items="${categories}">
                    <option value="${cat.categoryId}" ${criteria.categoryId == cat.categoryId ? 'selected' : ''}>
                      <c:out value="${cat.name}" /> (<c:out value="${cat.activeBookCount}" />)
                    </option>
                  </c:forEach>
                </select>
              </div>
              <div class="row g-2 mb-3">
                <div class="col-6">
                  <label class="form-label small fw-semibold" for="fMin">Giá từ (&#8363;)</label>
                  <input id="fMin" name="minPrice" type="number" min="0" step="1000" class="form-control"
                         value="<c:out value='${param.minPrice}' />" placeholder="0">
                </div>
                <div class="col-6">
                  <label class="form-label small fw-semibold" for="fMax">Đến (&#8363;)</label>
                  <input id="fMax" name="maxPrice" type="number" min="0" step="1000" class="form-control"
                         value="<c:out value='${param.maxPrice}' />" placeholder="500000">
                </div>
              </div>
              <div class="mb-4">
                <label class="form-label small fw-semibold" for="fSort">Sắp xếp theo</label>
                <select id="fSort" name="sort" class="form-select">
                  <c:forEach var="opt" items="${sortOptions}">
                    <option value="${opt.code}" ${criteria.sort == opt ? 'selected' : ''}>
                      <c:out value="${opt.label}" />
                    </option>
                  </c:forEach>
                </select>
              </div>
              <button type="submit" class="btn bh-btn-primary w-100 mb-2">Áp dụng bộ lọc</button>
              <a class="btn bh-btn-outline-neutral w-100" href="${pageContext.request.contextPath}/books">Xóa bộ lọc</a>
            </form>
          </aside>
        </div>

        <!-- Book Grid -->
        <div class="col-12 col-lg-9">
          <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
            <div class="small bh-muted">
              Tìm thấy <strong><c:out value="${pageResult.totalItems}" /></strong> cuốn sách
              <c:if test="${pageResult.totalPages > 0}">
                (Trang <c:out value="${pageResult.page}" />/<c:out value="${pageResult.totalPages}" />)
              </c:if>
            </div>
            <c:if test="${criteria.hasActiveFilters()}">
              <div class="d-flex flex-wrap align-items-center gap-2">
                <c:if test="${not empty criteria.keyword}">
                  <span class="bh-chip">Từ khóa: <c:out value="${criteria.keyword}" /></span>
                </c:if>
                <c:if test="${not empty criteria.minPrice}">
                  <span class="bh-chip">Từ <c:out value="${criteria.minPrice}" /> &#8363;</span>
                </c:if>
                <c:if test="${not empty criteria.maxPrice}">
                  <span class="bh-chip">Đến <c:out value="${criteria.maxPrice}" /> &#8363;</span>
                </c:if>
                <a class="small fw-semibold text-danger text-decoration-none" href="${pageContext.request.contextPath}/books">Xóa bộ lọc</a>
              </div>
            </c:if>
          </div>

          <c:choose>
            <c:when test="${pageResult.empty}">
              <section class="bh-empty-state">
                <h2 class="h4 mb-2">Không tìm thấy sách phù hợp</h2>
                <p class="bh-muted mb-4">Thử điều chỉnh từ khóa, thể loại hoặc khoảng giá để xem thêm đầu sách tại BookHub.</p>
                <a class="btn bh-btn-primary" href="${pageContext.request.contextPath}/books">Xóa bộ lọc</a>
              </section>
            </c:when>
            <c:otherwise>
              <div class="row g-3 row-cols-2 row-cols-md-3">
                <c:forEach var="item" items="${pageResult.items}">
                  <div class="col">
                    <c:set var="bookCard" value="${item}" scope="request" />
                    <jsp:include page="/WEB-INF/views/common/book-card.jsp" />
                  </div>
                </c:forEach>
              </div>
              <jsp:include page="/WEB-INF/views/common/pagination.jsp" />
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </main>
  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
