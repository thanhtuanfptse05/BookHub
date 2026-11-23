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
      <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-4">
        <h1 class="h2 mb-0">Giỏ hàng của bạn</h1>
        <c:if test="${not cart.empty}">
          <form action="${pageContext.request.contextPath}/cart" method="post" class="m-0">
            <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
            <input type="hidden" name="action" value="clear">
            <button type="submit" class="btn btn-sm bh-btn-danger-outline"
                    data-confirm-title="Xóa toàn bộ giỏ hàng"
                    data-confirm-message="Bạn chắc chắn muốn xóa toàn bộ sách khỏi giỏ hàng?"
                    data-confirm-ok="Xóa toàn bộ">Xóa toàn bộ giỏ</button>
          </form>
        </c:if>
      </div>

      <jsp:include page="/WEB-INF/views/common/messages.jsp" />

      <c:if test="${cart.hasWarnings()}">
        <div class="bh-alert bh-alert-warning mb-4" role="alert">
          <div>
            <div class="fw-semibold mb-1">Cần điều chỉnh giỏ hàng trước khi thanh toán:</div>
            <ul class="mb-0 ps-3 small">
              <c:forEach var="w" items="${cart.warnings}">
                <li><c:out value="${w}" /></li>
              </c:forEach>
            </ul>
          </div>
        </div>
      </c:if>

      <c:choose>
        <c:when test="${cart.empty}">
          <section class="bh-empty-state">
            <h2 class="h4 mb-2">Giỏ hàng của bạn đang trống</h2>
            <p class="bh-muted mb-4">Hãy khám phá hàng trăm đầu sách chọn lọc và thêm vào giỏ để đặt mua COD.</p>
            <a class="btn bh-btn-primary" href="${pageContext.request.contextPath}/books">Khám phá sách</a>
          </section>
        </c:when>
        <c:otherwise>
          <div class="row g-4">
            <div class="col-12 col-lg-8">
              <div class="d-flex flex-column gap-3">
                <c:forEach var="item" items="${cart.items}">
                  <article class="bh-card p-3 p-md-4">
                    <div class="row g-3 align-items-center">
                      <div class="col-3 col-sm-2">
                        <div class="bh-book-cover-wrap rounded-3 border">
                          <c:choose>
                            <c:when test="${item.coverUrl.startsWith('http://') or item.coverUrl.startsWith('https://')}">
                              <img src="<c:out value='${item.coverUrl}' />" alt="Bìa sách <c:out value='${item.title}' />"
                                   data-fallback="${pageContext.request.contextPath}/assets/images/book-placeholder.svg">
                            </c:when>
                            <c:otherwise>
                              <img src="${pageContext.request.contextPath}<c:out value='${item.coverUrl}' />" alt="Bìa sách <c:out value='${item.title}' />"
                                   data-fallback="${pageContext.request.contextPath}/assets/images/book-placeholder.svg">
                            </c:otherwise>
                          </c:choose>
                        </div>
                      </div>
                      <div class="col-9 col-sm-6">
                        <a class="bh-serif fw-semibold text-decoration-none text-dark d-block mb-1"
                           href="${pageContext.request.contextPath}/books/detail?id=${item.bookId}">
                          <c:out value="${item.title}" />
                        </a>
                        <div class="small bh-muted mb-1"><c:out value="${item.author}" /></div>
                        <div class="small">
                          Đơn giá: <strong class="bh-price"><c:out value="${item.formattedUnitPrice}" /></strong>
                          &middot; <span class="bh-muted">Tồn kho: <c:out value="${item.stockQty}" /></span>
                        </div>
                      </div>
                      <div class="col-12 col-sm-4 d-flex flex-sm-column align-items-center align-items-sm-end justify-content-between gap-2">
                        <div class="bh-price fs-5"><c:out value="${item.formattedLineTotal}" /></div>
                        <form action="${pageContext.request.contextPath}/cart" method="post" class="d-flex align-items-center gap-2">
                          <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
                          <input type="hidden" name="action" value="update">
                          <input type="hidden" name="cartItemId" value="${item.cartItemId}">
                          <div class="bh-qty-stepper" data-qty-stepper>
                            <button type="button" data-qty-dec aria-label="Giảm">&minus;</button>
                            <label class="visually-hidden" for="qty_${item.cartItemId}">Số lượng</label>
                            <input id="qty_${item.cartItemId}" type="number" name="quantity"
                                   value="${item.quantity}" min="1" max="${item.stockQty > 0 ? item.stockQty : 1}">
                            <button type="button" data-qty-inc aria-label="Tăng">+</button>
                          </div>
                          <button type="submit" class="btn btn-sm bh-btn-outline-teal">Cập nhật</button>
                        </form>
                        <form action="${pageContext.request.contextPath}/cart" method="post" class="m-0">
                          <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
                          <input type="hidden" name="action" value="remove">
                          <input type="hidden" name="cartItemId" value="${item.cartItemId}">
                          <button type="submit" class="btn btn-sm btn-link text-danger text-decoration-none p-0">Xóa</button>
                        </form>
                      </div>
                    </div>
                  </article>
                </c:forEach>
              </div>
            </div>

            <div class="col-12 col-lg-4">
              <aside class="bh-card p-4">
                <h2 class="h4 mb-3">Tóm tắt đơn hàng</h2>
                <div class="d-flex justify-content-between mb-2 small">
                  <span class="bh-muted">Tổng số lượng</span>
                  <strong><c:out value="${cart.totalQuantity}" /> cuốn</strong>
                </div>
                <div class="d-flex justify-content-between mb-2">
                  <span class="bh-muted">Tạm tính</span>
                  <span class="bh-price"><c:out value="${cart.formattedSubtotal}" /></span>
                </div>
                <div class="d-flex justify-content-between mb-3 small">
                  <span class="bh-muted">Phí vận chuyển COD cố định</span>
                  <span class="bh-nowrap"><c:out value="${cart.formattedShippingFee}" /></span>
                </div>
                <hr>
                <div class="d-flex justify-content-between align-items-center mb-4">
                  <span class="fw-semibold">Ước tính tổng cộng</span>
                  <span class="bh-price fs-4"><c:out value="${cart.formattedTotalAmount}" /></span>
                </div>
                <a class="btn bh-btn-primary w-100 mb-2 ${cart.hasWarnings() ? 'disabled' : ''}"
                   href="${pageContext.request.contextPath}/checkout">Tiến hành đặt hàng</a>
                <a class="btn bh-btn-outline-neutral w-100" href="${pageContext.request.contextPath}/books">Tiếp tục mua sách</a>
              </aside>
            </div>
          </div>
        </c:otherwise>
      </c:choose>
    </div>
  </main>
  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
