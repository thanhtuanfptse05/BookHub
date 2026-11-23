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
      <h1 class="h2 mb-1">Quản lý danh mục</h1>
      <p class="small bh-muted mb-0">Thêm, sửa và chuyển trạng thái hoạt động (không xóa cứng để bảo toàn liên kết sách).</p>
    </div>
  </div>

  <div class="row g-4">
    <!-- Category Form (Add / Edit) -->
    <div class="col-12 col-lg-4">
      <section class="bh-card p-4">
        <c:set var="editing" value="${not empty editCategory or not empty formId}" />
        <h2 class="h5 mb-3">${editing ? 'Chỉnh sửa danh mục' : 'Thêm danh mục mới'}</h2>
        <form action="${pageContext.request.contextPath}/admin/categories" method="post" novalidate>
          <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
          <input type="hidden" name="action" value="save">
          <input type="hidden" name="id" value="<c:out value='${not empty formId ? formId : editCategory.categoryId}' />">

          <div class="mb-3">
            <label class="form-label small fw-semibold" for="catName">Tên danh mục <span class="text-danger">*</span></label>
            <input id="catName" name="name" type="text" maxlength="80" required
                   class="form-control ${not empty errors.name ? 'is-invalid' : ''}"
                   value="<c:out value='${not empty formName ? formName : editCategory.name}' />" placeholder="VD: Lịch sử">
            <c:if test="${not empty errors.name}">
              <div class="invalid-feedback"><c:out value="${errors.name}" /></div>
            </c:if>
          </div>

          <div class="mb-3">
            <label class="form-label small fw-semibold" for="catSlug">Slug URL <span class="text-danger">*</span></label>
            <input id="catSlug" name="slug" type="text" maxlength="100" required
                   class="form-control bh-mono ${not empty errors.slug ? 'is-invalid' : ''}"
                   value="<c:out value='${not empty formSlug ? formSlug : editCategory.slug}' />" placeholder="VD: lich-su">
            <c:if test="${not empty errors.slug}">
              <div class="invalid-feedback"><c:out value="${errors.slug}" /></div>
            </c:if>
          </div>

          <div class="mb-3">
            <label class="form-label small fw-semibold" for="catDesc">Mô tả ngắn</label>
            <textarea id="catDesc" name="description" rows="3" maxlength="300"
                      class="form-control ${not empty errors.description ? 'is-invalid' : ''}"><c:out value="${not empty formDescription ? formDescription : editCategory.description}" /></textarea>
            <c:if test="${not empty errors.description}">
              <div class="invalid-feedback"><c:out value="${errors.description}" /></div>
            </c:if>
          </div>

          <div class="form-check mb-4">
            <c:set var="isChecked" value="${empty editCategory and empty formId ? true : (not empty formId ? formActive : editCategory.active)}" />
            <input id="catActive" name="isActive" type="checkbox" value="true" class="form-check-input" ${isChecked ? 'checked' : ''}>
            <label class="form-check-label small" for="catActive">Đang hiển thị trên cửa hàng</label>
          </div>

          <div class="d-flex gap-2">
            <button type="submit" class="btn bh-btn-primary flex-grow-1">${editing ? 'Lưu cập nhật' : 'Thêm danh mục'}</button>
            <c:if test="${editing}">
              <a class="btn bh-btn-outline-neutral" href="${pageContext.request.contextPath}/admin/categories">Hủy</a>
            </c:if>
          </div>
        </form>
      </section>
    </div>

    <!-- Categories Table -->
    <div class="col-12 col-lg-8">
      <section class="bh-card p-4">
        <form action="${pageContext.request.contextPath}/admin/categories" method="get" class="row g-2 mb-3">
          <div class="col">
            <label class="visually-hidden" for="catKw">Tìm danh mục</label>
            <input id="catKw" name="keyword" type="search" class="form-control"
                   value="<c:out value='${keyword}' />" placeholder="Tìm theo tên hoặc slug...">
          </div>
          <div class="col-auto">
            <button type="submit" class="btn bh-btn-outline-teal">Tìm</button>
          </div>
        </form>

        <div class="table-responsive">
          <table class="table bh-table mb-0">
            <thead>
              <tr>
                <th>Tên danh mục</th>
                <th>Slug</th>
                <th class="text-center">Số sách</th>
                <th>Trạng thái</th>
                <th class="text-end">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="cat" items="${categories}">
                <tr>
                  <td class="fw-semibold"><c:out value="${cat.name}" /></td>
                  <td class="bh-mono small bh-muted"><c:out value="${cat.slug}" /></td>
                  <td class="text-center"><c:out value="${cat.totalBooks}" /></td>
                  <td>
                    <c:choose>
                      <c:when test="${cat.active}"><span class="bh-badge bh-badge-success">Hoạt động</span></c:when>
                      <c:otherwise><span class="bh-badge bh-badge-neutral">Ngừng hoạt động</span></c:otherwise>
                    </c:choose>
                  </td>
                  <td class="text-end bh-nowrap">
                    <a class="btn btn-sm bh-btn-outline-neutral me-1"
                       href="${pageContext.request.contextPath}/admin/categories?editId=${cat.categoryId}">Sửa</a>
                    <form action="${pageContext.request.contextPath}/admin/categories" method="post" class="d-inline">
                      <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
                      <input type="hidden" name="action" value="toggleActive">
                      <input type="hidden" name="id" value="${cat.categoryId}">
                      <button type="submit"
                              class="btn btn-sm ${cat.active ? 'bh-btn-danger-outline' : 'bh-btn-outline-teal'}"
                              data-confirm-title="${cat.active ? 'Ngừng hoạt động danh mục' : 'Kích hoạt lại danh mục'}"
                              data-confirm-message="${cat.active ? 'Ngừng hoạt động danh mục sẽ ẩn sách thuộc danh mục này khỏi cửa hàng công khai nhưng vẫn giữ toàn bộ dữ liệu.' : 'Kích hoạt lại danh mục này để hiển thị trên cửa hàng?'}"
                              data-confirm-ok="${cat.active ? 'Ngừng hoạt động' : 'Kích hoạt'}">
                        ${cat.active ? 'Ngừng' : 'Mở lại'}
                      </button>
                    </form>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </div>

  <jsp:include page="/WEB-INF/views/admin/layout-end.jsp" />
</body>
</html>
