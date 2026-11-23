<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <jsp:include page="/WEB-INF/views/common/head.jsp" />
</head>
<body>
  <jsp:include page="/WEB-INF/views/admin/layout-start.jsp" />

  <div class="mb-3">
    <a class="small text-decoration-none bh-muted" href="${pageContext.request.contextPath}/admin/books">&larr; Quay lại danh sách sách</a>
  </div>
  <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-4">
    <h1 class="h2 mb-0">${empty form.bookId ? 'Thêm sách mới' : 'Chỉnh sửa sách'}</h1>
    <c:if test="${not empty form.bookId}">
      <span class="bh-badge bh-badge-teal bh-mono"><c:out value="${form.bookCode}" /></span>
    </c:if>
  </div>

  <form action="${pageContext.request.contextPath}/admin/books" method="post" novalidate>
    <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
    <input type="hidden" name="action" value="save">
    <c:if test="${not empty form.bookId}">
      <input type="hidden" name="bookId" value="${form.bookId}">
    </c:if>

    <div class="row g-4">
      <div class="col-12 col-lg-8">
        <section class="bh-card p-4">
          <div class="row g-3">
            <div class="col-12">
              <label class="form-label fw-semibold" for="bTitle">Tên sách <span class="text-danger">*</span></label>
              <input id="bTitle" name="title" type="text" maxlength="200" required
                     class="form-control ${not empty errors.title ? 'is-invalid' : ''}"
                     value="<c:out value='${form.title}' />">
              <c:if test="${not empty errors.title}"><div class="invalid-feedback"><c:out value="${errors.title}" /></div></c:if>
            </div>

            <div class="col-12 col-md-6">
              <label class="form-label fw-semibold" for="bCat">Danh mục <span class="text-danger">*</span></label>
              <select id="bCat" name="categoryId" required class="form-select ${not empty errors.categoryId ? 'is-invalid' : ''}">
                <option value="">-- Chọn danh mục --</option>
                <c:forEach var="cat" items="${categories}">
                  <option value="${cat.categoryId}" ${form.categoryId == cat.categoryId ? 'selected' : ''}>
                    <c:out value="${cat.name}" /><c:if test="${!cat.active}"> (Ngừng hoạt động)</c:if>
                  </option>
                </c:forEach>
              </select>
              <c:if test="${not empty errors.categoryId}"><div class="invalid-feedback"><c:out value="${errors.categoryId}" /></div></c:if>
            </div>

            <div class="col-12 col-md-6">
              <label class="form-label fw-semibold" for="bAuthor">Tác giả <span class="text-danger">*</span></label>
              <input id="bAuthor" name="author" type="text" maxlength="150" required
                     class="form-control ${not empty errors.author ? 'is-invalid' : ''}"
                     value="<c:out value='${form.author}' />">
              <c:if test="${not empty errors.author}"><div class="invalid-feedback"><c:out value="${errors.author}" /></div></c:if>
            </div>

            <div class="col-12 col-md-4">
              <label class="form-label fw-semibold" for="bPublisher">Nhà xuất bản</label>
              <input id="bPublisher" name="publisher" type="text" maxlength="150" class="form-control"
                     value="<c:out value='${form.publisher}' />">
            </div>

            <div class="col-6 col-md-4">
              <label class="form-label fw-semibold" for="bYear">Năm xuất bản</label>
              <input id="bYear" name="publishedYear" type="number" min="1000" max="2200"
                     class="form-control ${not empty errors.publishedYear ? 'is-invalid' : ''}"
                     value="<c:out value='${form.publishedYear}' />">
              <c:if test="${not empty errors.publishedYear}"><div class="invalid-feedback"><c:out value="${errors.publishedYear}" /></div></c:if>
            </div>

            <div class="col-6 col-md-4">
              <label class="form-label fw-semibold" for="bIsbn">ISBN (tùy chọn)</label>
              <input id="bIsbn" name="isbn" type="text" maxlength="20"
                     class="form-control bh-mono ${not empty errors.isbn ? 'is-invalid' : ''}"
                     value="<c:out value='${form.isbn}' />" placeholder="978-...">
              <c:if test="${not empty errors.isbn}"><div class="invalid-feedback"><c:out value="${errors.isbn}" /></div></c:if>
            </div>

            <div class="col-6">
              <label class="form-label fw-semibold" for="bPrice">Giá bán (VND) <span class="text-danger">*</span></label>
              <input id="bPrice" name="price" type="number" min="1000" step="1000" required
                     class="form-control ${not empty errors.price ? 'is-invalid' : ''}"
                     value="<c:out value='${form.price}' />">
              <c:if test="${not empty errors.price}"><div class="invalid-feedback"><c:out value="${errors.price}" /></div></c:if>
            </div>

            <div class="col-6">
              <label class="form-label fw-semibold" for="bStock">Tồn kho <span class="text-danger">*</span></label>
              <input id="bStock" name="stockQty" type="number" min="0" required
                     class="form-control ${not empty errors.stockQty ? 'is-invalid' : ''}"
                     value="<c:out value='${form.stockQty}' />">
              <c:if test="${not empty errors.stockQty}"><div class="invalid-feedback"><c:out value="${errors.stockQty}" /></div></c:if>
            </div>

            <div class="col-12">
              <label class="form-label fw-semibold" for="bShort">Mô tả ngắn</label>
              <textarea id="bShort" name="shortDescription" rows="2" maxlength="500" class="form-control"><c:out value="${form.shortDescription}" /></textarea>
            </div>

            <div class="col-12">
              <label class="form-label fw-semibold" for="bDesc">Mô tả chi tiết</label>
              <textarea id="bDesc" name="description" rows="5" class="form-control"><c:out value="${form.description}" /></textarea>
            </div>

            <div class="col-12 d-flex flex-wrap gap-4 pt-2">
              <div class="form-check">
                <input id="bFeatured" name="featured" type="checkbox" value="true" class="form-check-input" ${form.featured ? 'checked' : ''}>
                <label class="form-check-label" for="bFeatured">Hiển thị ở mục Sách nổi bật (Trang chủ)</label>
              </div>
              <div class="form-check">
                <input id="bActive" name="active" type="checkbox" value="true" class="form-check-input" ${form.active ? 'checked' : ''}>
                <label class="form-check-label" for="bActive">Đang kinh doanh (hiển thị công khai)</label>
              </div>
            </div>
          </div>

          <div class="d-flex gap-2 mt-4">
            <button type="submit" class="btn bh-btn-primary">Lưu sách</button>
            <a class="btn bh-btn-outline-neutral" href="${pageContext.request.contextPath}/admin/books">Hủy</a>
          </div>
        </section>
      </div>

      <div class="col-12 col-lg-4">
        <section class="bh-card p-4">
          <h2 class="h5 mb-3">Ảnh bìa sách</h2>
          <div class="mb-3">
            <label class="form-label small fw-semibold" for="coverUrl">URL ảnh bìa</label>
            <input id="coverUrl" name="coverUrl" type="text" maxlength="500"
                   class="form-control ${not empty errors.coverUrl ? 'is-invalid' : ''}"
                   value="<c:out value='${form.coverUrl}' />" placeholder="/assets/images/book-placeholder.svg">
            <c:if test="${not empty errors.coverUrl}"><div class="invalid-feedback"><c:out value="${errors.coverUrl}" /></div></c:if>
            <div class="form-text">Chấp nhận đường dẫn nội bộ <code>/assets/...</code> hoặc URL <code>https://</code>.</div>
          </div>
          <div class="bh-book-cover-wrap rounded-4 border">
            <c:set var="previewSrc" value="${not empty form.coverUrl ? form.coverUrl : '/assets/images/book-placeholder.svg'}" />
            <c:choose>
              <c:when test="${previewSrc.startsWith('http://') or previewSrc.startsWith('https://')}">
                <img id="coverPreviewImg" src="<c:out value='${previewSrc}' />"
                     alt="Xem trước ảnh bìa" data-fallback="${pageContext.request.contextPath}/assets/images/book-placeholder.svg">
              </c:when>
              <c:otherwise>
                <img id="coverPreviewImg" src="${pageContext.request.contextPath}<c:out value='${previewSrc}' />"
                     alt="Xem trước ảnh bìa" data-fallback="${pageContext.request.contextPath}/assets/images/book-placeholder.svg">
              </c:otherwise>
            </c:choose>
          </div>
        </section>
      </div>
    </div>
  </form>

  <jsp:include page="/WEB-INF/views/admin/layout-end.jsp" />
</body>
</html>
