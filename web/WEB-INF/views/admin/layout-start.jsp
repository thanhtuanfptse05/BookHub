<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<div class="bh-admin-layout">
  <aside class="bh-admin-sidebar bh-admin-sidebar-desktop" aria-label="Menu quản trị">
    <div class="d-flex align-items-center justify-content-between px-2 py-2 mb-3">
      <a class="text-white text-decoration-none bh-serif fs-4 fw-bold" href="${pageContext.request.contextPath}/admin">BookHub</a>
      <span class="bh-admin-pill">ADMIN</span>
    </div>
    <a class="bh-admin-nav-link ${adminNav == 'dashboard' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin">Tổng quan</a>
    <a class="bh-admin-nav-link ${adminNav == 'books' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/books">Quản lý sách</a>
    <a class="bh-admin-nav-link ${adminNav == 'categories' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/categories">Quản lý danh mục</a>
    <a class="bh-admin-nav-link ${adminNav == 'orders' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/orders">Quản lý đơn hàng</a>
    <a class="bh-admin-nav-link ${adminNav == 'users' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/users">Người dùng</a>
    <a class="bh-admin-nav-link ${adminNav == 'reviews' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/reviews">Duyệt đánh giá</a>
    <a class="bh-admin-nav-link ${adminNav == 'questions' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/questions">Hỏi đáp Q&amp;A</a>
    <a class="bh-admin-nav-link ${adminNav == 'reports' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/reports">Báo cáo thống kê</a>
    <div class="mt-auto pt-4">
      <a class="bh-admin-nav-link" href="${pageContext.request.contextPath}/">&larr; Về trang bán hàng</a>
    </div>
  </aside>

  <div class="offcanvas offcanvas-start bh-admin-sidebar" tabindex="-1" id="adminMobileSidebar" aria-label="Menu quản trị di động">
    <div class="d-flex align-items-center justify-content-between px-2 py-2 mb-3">
      <span class="text-white bh-serif fs-4 fw-bold">BookHub <span class="bh-admin-pill ms-1">ADMIN</span></span>
      <button type="button" class="btn-close btn-close-white" data-bs-dismiss="offcanvas" aria-label="Đóng menu"></button>
    </div>
    <a class="bh-admin-nav-link ${adminNav == 'dashboard' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin">Tổng quan</a>
    <a class="bh-admin-nav-link ${adminNav == 'books' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/books">Quản lý sách</a>
    <a class="bh-admin-nav-link ${adminNav == 'categories' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/categories">Quản lý danh mục</a>
    <a class="bh-admin-nav-link ${adminNav == 'orders' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/orders">Quản lý đơn hàng</a>
    <a class="bh-admin-nav-link ${adminNav == 'users' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/users">Người dùng</a>
    <a class="bh-admin-nav-link ${adminNav == 'reviews' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/reviews">Duyệt đánh giá</a>
    <a class="bh-admin-nav-link ${adminNav == 'questions' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/questions">Hỏi đáp Q&amp;A</a>
    <a class="bh-admin-nav-link ${adminNav == 'reports' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/reports">Báo cáo thống kê</a>
    <a class="bh-admin-nav-link mt-3" href="${pageContext.request.contextPath}/">&larr; Về trang bán hàng</a>
  </div>

  <div class="bh-admin-content">
    <header class="bh-admin-topbar">
      <div class="d-flex align-items-center gap-3">
        <button class="btn bh-btn-outline-neutral d-lg-none px-2" type="button"
                data-bs-toggle="offcanvas" data-bs-target="#adminMobileSidebar"
                aria-controls="adminMobileSidebar" aria-label="Mở menu quản trị">&#9776;</button>
        <span class="small bh-muted d-none d-sm-inline">Cổng Quản Trị BookHub</span>
      </div>
      <div class="d-flex align-items-center gap-2">
        <c:if test="${not empty sessionScope.currentUser}">
          <span class="bh-avatar-pill">
            <span class="bh-avatar-circle"><c:out value="${sessionScope.currentUser.initials}" /></span>
            <span class="d-none d-sm-inline"><c:out value="${sessionScope.currentUser.fullName}" /></span>
          </span>
        </c:if>
        <form action="${pageContext.request.contextPath}/logout" method="post" class="m-0">
          <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
          <button type="submit" class="btn bh-btn-outline-neutral">Đăng xuất</button>
        </form>
      </div>
    </header>
    <main class="p-3 p-md-4 flex-grow-1">
      <jsp:include page="/WEB-INF/views/common/messages.jsp" />
