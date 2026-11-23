<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<header class="bh-header">
  <div class="bh-container">
    <div class="bh-header-bar">
      <div class="d-flex align-items-center gap-3">
        <a class="bh-brand" href="${pageContext.request.contextPath}/">
          <span class="bh-brand-icon" aria-hidden="true">B</span>
          <span>BookHub</span>
        </a>
        <nav class="d-none d-lg-flex align-items-center gap-1" aria-label="Điều hướng chính">
          <a class="bh-nav-link ${activeNav == 'home' ? 'active' : ''}" href="${pageContext.request.contextPath}/">Trang chủ</a>
          <a class="bh-nav-link ${activeNav == 'books' ? 'active' : ''}" href="${pageContext.request.contextPath}/books">Danh mục sách</a>
          <c:if test="${not empty sessionScope.currentUser and sessionScope.currentUser.admin}">
            <a class="bh-nav-link" href="${pageContext.request.contextPath}/admin">Quản trị</a>
          </c:if>
        </nav>
      </div>

      <form class="bh-search-pill d-none d-md-block" action="${pageContext.request.contextPath}/books" method="get" role="search">
        <label class="visually-hidden" for="headerSearchDesktop">Tìm sách theo tên, tác giả, ISBN</label>
        <input id="headerSearchDesktop" type="search" name="keyword"
               value="<c:out value='${param.keyword}' />"
               placeholder="Tìm tên sách, tác giả, ISBN...">
        <button type="submit" aria-label="Tìm kiếm sách">&#128269;</button>
      </form>

      <div class="d-flex align-items-center gap-2">
        <c:choose>
          <c:when test="${not empty sessionScope.currentUser}">
            <div class="dropdown">
              <button class="bh-avatar-pill dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                <span class="bh-avatar-circle"><c:out value="${sessionScope.currentUser.initials}" /></span>
                <span class="d-none d-sm-inline"><c:out value="${sessionScope.currentUser.fullName}" /></span>
              </button>
              <ul class="dropdown-menu dropdown-menu-end">
                <c:if test="${sessionScope.currentUser.admin}">
                  <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin">Bảng điều khiển Admin</a></li>
                  <li><hr class="dropdown-divider"></li>
                </c:if>
                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/account/profile">Hồ sơ cá nhân</a></li>
                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/account/orders">Đơn hàng của tôi</a></li>
                <li><hr class="dropdown-divider"></li>
                <li>
                  <form action="${pageContext.request.contextPath}/logout" method="post" class="m-0">
                    <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
                    <button type="submit" class="dropdown-item text-danger">Đăng xuất</button>
                  </form>
                </li>
              </ul>
            </div>
          </c:when>
          <c:otherwise>
            <a class="btn bh-btn-outline-teal bh-nowrap" href="${pageContext.request.contextPath}/login">Đăng nhập</a>
          </c:otherwise>
        </c:choose>

        <a class="bh-cart-btn" href="${pageContext.request.contextPath}/cart" aria-label="Giỏ hàng">
          <span aria-hidden="true">&#128722;</span>
          <c:if test="${not empty sessionScope.cartCount and sessionScope.cartCount > 0}">
            <span class="bh-cart-badge"><c:out value="${sessionScope.cartCount}" /></span>
          </c:if>
        </a>

        <button class="btn bh-btn-outline-neutral d-lg-none px-2" type="button"
                data-bs-toggle="collapse" data-bs-target="#mobileNavDrawer"
                aria-controls="mobileNavDrawer" aria-expanded="false" aria-label="Mở menu">
          &#9776;
        </button>
      </div>
    </div>

    <div class="pb-3 d-md-none">
      <form class="bh-search-pill mw-100" action="${pageContext.request.contextPath}/books" method="get" role="search">
        <label class="visually-hidden" for="headerSearchMobile">Tìm sách</label>
        <input id="headerSearchMobile" type="search" name="keyword"
               value="<c:out value='${param.keyword}' />"
               placeholder="Tìm tên sách, tác giả, ISBN...">
        <button type="submit" aria-label="Tìm kiếm">&#128269;</button>
      </form>
    </div>

    <div class="collapse d-lg-none pb-3" id="mobileNavDrawer">
      <nav class="d-flex flex-column gap-1" aria-label="Điều hướng di động">
        <a class="bh-nav-link ${activeNav == 'home' ? 'active' : ''}" href="${pageContext.request.contextPath}/">Trang chủ</a>
        <a class="bh-nav-link ${activeNav == 'books' ? 'active' : ''}" href="${pageContext.request.contextPath}/books">Danh mục sách</a>
        <c:if test="${not empty sessionScope.currentUser and sessionScope.currentUser.admin}">
          <a class="bh-nav-link" href="${pageContext.request.contextPath}/admin">Quản trị cửa hàng</a>
        </c:if>
      </nav>
    </div>
  </div>
</header>
