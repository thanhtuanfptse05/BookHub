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
        <a href="${pageContext.request.contextPath}/">Trang chủ</a> /
        <a href="${pageContext.request.contextPath}/books">Danh mục sách</a> /
        <a href="${pageContext.request.contextPath}/books?categoryId=${book.categoryId}"><c:out value="${book.categoryName}" /></a> /
        <span><c:out value="${book.title}" /></span>
      </nav>

      <jsp:include page="/WEB-INF/views/common/messages.jsp" />

      <!-- Main Book Block (SCR-03 / SCR-03b) -->
      <section class="bh-card p-4 p-lg-5 mb-4">
        <div class="row g-4 g-lg-5 align-items-start">
          <div class="col-12 col-md-4">
            <div class="bh-book-cover-wrap rounded-4 border">
              <c:set var="imgSrc" value="${book.coverUrl}" />
              <c:choose>
                <c:when test="${not empty imgSrc and (imgSrc.startsWith('http://') or imgSrc.startsWith('https://'))}">
                  <img src="<c:out value='${imgSrc}' />" alt="Bìa sách <c:out value='${book.title}' />"
                       data-fallback="${pageContext.request.contextPath}/assets/images/book-placeholder.svg">
                </c:when>
                <c:otherwise>
                  <img src="${pageContext.request.contextPath}<c:out value='${imgSrc}' />" alt="Bìa sách <c:out value='${book.title}' />"
                       data-fallback="${pageContext.request.contextPath}/assets/images/book-placeholder.svg">
                </c:otherwise>
              </c:choose>
            </div>
          </div>

          <div class="col-12 col-md-8">
            <a class="bh-chip text-decoration-none mb-2" href="${pageContext.request.contextPath}/books?categoryId=${book.categoryId}">
              <c:out value="${book.categoryName}" />
            </a>
            <h1 class="h2 mb-2"><c:out value="${book.title}" /></h1>

            <div class="d-flex flex-wrap align-items-center gap-3 mb-3 small">
              <span>
                <strong class="bh-star">&#9733; <c:out value="${book.formattedAverageRating}" /></strong>
                <span class="bh-muted">(<c:out value="${book.reviewCount}" /> đánh giá đã duyệt)</span>
              </span>
              <span class="bh-muted">&middot;</span>
              <span class="bh-mono bh-muted"><c:out value="${book.bookCode}" /></span>
            </div>

            <div class="d-flex flex-wrap align-items-center gap-3 mb-4">
              <span class="bh-price fs-2"><c:out value="${book.formattedPrice}" /></span>
              <c:choose>
                <c:when test="${book.inStock}">
                  <span class="bh-badge bh-badge-success">Còn hàng (<c:out value="${book.stockQty}" /> cuốn)</span>
                </c:when>
                <c:otherwise>
                  <span class="bh-badge bh-badge-neutral">Hết hàng</span>
                </c:otherwise>
              </c:choose>
            </div>

            <c:if test="${not empty book.shortDescription}">
              <p class="bh-muted mb-4"><c:out value="${book.shortDescription}" /></p>
            </c:if>

            <!-- Add to Cart Form -->
            <c:choose>
              <c:when test="${book.inStock}">
                <form action="${pageContext.request.contextPath}/cart" method="post" class="d-flex flex-wrap align-items-center gap-3 mb-4">
                  <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
                  <input type="hidden" name="action" value="add">
                  <input type="hidden" name="bookId" value="${book.bookId}">
                  <input type="hidden" name="returnUrl" value="/books/detail?id=${book.bookId}">

                  <div class="bh-qty-stepper" data-qty-stepper>
                    <button type="button" data-qty-dec aria-label="Giảm số lượng">&minus;</button>
                    <label class="visually-hidden" for="detailQty">Số lượng</label>
                    <input id="detailQty" type="number" name="quantity" value="1" min="1" max="${book.stockQty}">
                    <button type="button" data-qty-inc aria-label="Tăng số lượng">+</button>
                  </div>

                  <button type="submit" class="btn bh-btn-primary">Thêm vào giỏ hàng</button>
                </form>
              </c:when>
              <c:otherwise>
                <div class="d-flex flex-wrap align-items-center gap-3 mb-2">
                  <div class="bh-qty-stepper">
                    <button type="button" disabled aria-label="Giảm số lượng">&minus;</button>
                    <input type="number" value="0" disabled aria-label="Số lượng">
                    <button type="button" disabled aria-label="Tăng số lượng">+</button>
                  </div>
                  <button type="button" class="btn bh-btn-outline-neutral" disabled>Thêm vào giỏ hàng</button>
                </div>
                <p class="small bh-muted mb-4">Sách tạm hết hàng. Vui lòng quay lại sau.</p>
              </c:otherwise>
            </c:choose>

            <!-- Book Metadata Table -->
            <div class="table-responsive">
              <table class="table table-sm bh-table mb-0">
                <tbody>
                  <tr><th scope="row">Tác giả</th><td><c:out value="${book.author}" /></td></tr>
                  <tr><th scope="row">Nhà xuất bản</th><td><c:out value="${not empty book.publisher ? book.publisher : '—'}" /></td></tr>
                  <tr><th scope="row">Năm xuất bản</th><td><c:out value="${not empty book.publishedYear ? book.publishedYear : '—'}" /></td></tr>
                  <tr><th scope="row">ISBN</th><td class="bh-mono"><c:out value="${not empty book.isbn ? book.isbn : '—'}" /></td></tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </section>

      <!-- Detailed Description -->
      <section class="bh-card p-4 mb-4">
        <h2 class="h4 mb-3">Mô tả chi tiết</h2>
        <p class="mb-0"><c:out value="${not empty book.description ? book.description : book.shortDescription}" /></p>
      </section>

      <!-- Reviews Section (#reviews) -->
      <section id="reviews" class="bh-card p-4 mb-4">
        <h2 class="h4 mb-4">Đánh giá của khách hàng (<c:out value="${book.reviewCount}" />)</h2>
        <div class="row g-4">
          <div class="col-12 col-lg-7">
            <c:choose>
              <c:when test="${empty book.approvedReviews}">
                <p class="bh-muted mb-0">Chưa có đánh giá nào được duyệt cho cuốn sách này.</p>
              </c:when>
              <c:otherwise>
                <div class="d-flex flex-column gap-3">
                  <c:forEach var="rev" items="${book.approvedReviews}">
                    <article class="border rounded-3 p-3">
                      <div class="d-flex justify-content-between align-items-center gap-2 mb-1">
                        <strong><c:out value="${rev.reviewerName}" /></strong>
                        <span class="small bh-muted"><c:out value="${rev.createdAtFormatted}" /></span>
                      </div>
                      <div class="bh-star small mb-1" aria-label="${rev.rating} trên 5 sao">
                        <c:forEach begin="1" end="${rev.rating}">&#9733;</c:forEach>
                        <c:forEach begin="${rev.rating + 1}" end="5"><span class="bh-star-empty">&#9733;</span></c:forEach>
                      </div>
                      <c:if test="${not empty rev.title}">
                        <div class="fw-semibold mb-1"><c:out value="${rev.title}" /></div>
                      </c:if>
                      <p class="mb-0 small"><c:out value="${rev.content}" /></p>
                    </article>
                  </c:forEach>
                </div>
              </c:otherwise>
            </c:choose>
          </div>

          <div class="col-12 col-lg-5">
            <div class="border rounded-3 p-3">
              <h3 class="h5 mb-2">Viết đánh giá</h3>
              <div class="bh-alert bh-alert-info small mb-3">
                <div>Chỉ khách hàng có đơn hàng chứa cuốn sách này đã giao thành công (DELIVERED) mới được gửi đánh giá (tối đa 1 lần/sách, hiển thị sau khi Admin duyệt).</div>
              </div>
              <c:choose>
                <c:when test="${empty sessionScope.currentUser}">
                  <a class="btn bh-btn-outline-teal w-100" href="${pageContext.request.contextPath}/login?returnUrl=%2Fbooks%2Fdetail%3Fid%3D${book.bookId}">
                    Đăng nhập để viết đánh giá
                  </a>
                </c:when>
                <c:when test="${book.alreadyReviewed}">
                  <p class="small bh-muted mb-0">Bạn đã gửi đánh giá cho cuốn sách này.</p>
                </c:when>
                <c:otherwise>
                  <form action="${pageContext.request.contextPath}/books/review" method="post">
                    <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
                    <input type="hidden" name="bookId" value="${book.bookId}">
                    <div class="mb-3">
                      <label class="form-label small fw-semibold" for="revRating">Số sao (1–5) <span class="text-danger">*</span></label>
                      <select id="revRating" name="rating" class="form-select" required>
                        <option value="5">5 sao — Rất hài lòng</option>
                        <option value="4">4 sao — Hài lòng</option>
                        <option value="3">3 sao — Bình thường</option>
                        <option value="2">2 sao — Chưa tốt</option>
                        <option value="1">1 sao — Không hài lòng</option>
                      </select>
                    </div>
                    <div class="mb-3">
                      <label class="form-label small fw-semibold" for="revTitle">Tiêu đề (tùy chọn)</label>
                      <input id="revTitle" name="title" type="text" maxlength="150" class="form-control" placeholder="Tóm tắt cảm nhận của bạn">
                    </div>
                    <div class="mb-3">
                      <label class="form-label small fw-semibold" for="revContent">Nội dung đánh giá <span class="text-danger">*</span></label>
                      <textarea id="revContent" name="content" rows="3" maxlength="2000" required class="form-control"
                                placeholder="Chia sẻ chi tiết trải nghiệm đọc sách..."></textarea>
                    </div>
                    <button type="submit" class="btn bh-btn-primary w-100">Gửi đánh giá</button>
                  </form>
                </c:otherwise>
              </c:choose>
            </div>
          </div>
        </div>
      </section>

      <!-- Q&A Section (#qa) -->
      <section id="qa" class="bh-card p-4 mb-5">
        <h2 class="h4 mb-4">Hỏi đáp về sách (<c:out value="${book.answeredQuestions.size()}" />)</h2>
        <div class="row g-4">
          <div class="col-12 col-lg-7">
            <c:choose>
              <c:when test="${empty book.answeredQuestions}">
                <p class="bh-muted mb-0">Chưa có câu hỏi nào được giải đáp cho cuốn sách này.</p>
              </c:when>
              <c:otherwise>
                <div class="d-flex flex-column gap-3">
                  <c:forEach var="qa" items="${book.answeredQuestions}">
                    <article class="border rounded-3 p-3">
                      <div class="d-flex justify-content-between align-items-center gap-2 mb-1">
                        <strong><c:out value="${qa.subject}" /></strong>
                        <span class="small bh-muted"><c:out value="${qa.createdAtFormatted}" /></span>
                      </div>
                      <div class="small bh-muted mb-2">Hỏi bởi <c:out value="${qa.askerName}" /></div>
                      <p class="small mb-3"><c:out value="${qa.content}" /></p>
                      <div class="bh-alert bh-alert-success small mb-0">
                        <div>
                          <div class="fw-semibold mb-1">&#10003; BookHub phản hồi (<c:out value="${qa.responderName}" /> &middot; <c:out value="${qa.answeredAtFormatted}" />)</div>
                          <div><c:out value="${qa.answerContent}" /></div>
                        </div>
                      </div>
                    </article>
                  </c:forEach>
                </div>
              </c:otherwise>
            </c:choose>
          </div>

          <div class="col-12 col-lg-5">
            <div class="border rounded-3 p-3">
              <h3 class="h5 mb-2">Đặt câu hỏi cho cửa hàng</h3>
              <c:choose>
                <c:when test="${empty sessionScope.currentUser}">
                  <p class="small bh-muted mb-3">Đăng nhập tài khoản để gửi câu hỏi và nhận phản hồi chính thức từ BookHub.</p>
                  <a class="btn bh-btn-outline-teal w-100" href="${pageContext.request.contextPath}/login?returnUrl=%2Fbooks%2Fdetail%3Fid%3D${book.bookId}">
                    Đăng nhập để đặt câu hỏi
                  </a>
                </c:when>
                <c:otherwise>
                  <form action="${pageContext.request.contextPath}/books/question" method="post">
                    <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
                    <input type="hidden" name="bookId" value="${book.bookId}">
                    <div class="mb-3">
                      <div class="d-flex justify-content-between">
                        <label class="form-label small fw-semibold" for="qaSubject">Chủ đề <span class="text-danger">*</span></label>
                        <span id="qaSubCount" class="small bh-muted">0/150</span>
                      </div>
                      <input id="qaSubject" name="subject" type="text" maxlength="150" required
                             data-char-count="qaSubCount" class="form-control" placeholder="VD: Sách có bản bìa cứng không?">
                    </div>
                    <div class="mb-3">
                      <div class="d-flex justify-content-between">
                        <label class="form-label small fw-semibold" for="qaContent">Nội dung câu hỏi <span class="text-danger">*</span></label>
                        <span id="qaContentCount" class="small bh-muted">0/1500</span>
                      </div>
                      <textarea id="qaContent" name="content" rows="3" maxlength="1500" required
                                data-char-count="qaContentCount" class="form-control"
                                placeholder="Mô tả câu hỏi của bạn..."></textarea>
                    </div>
                    <button type="submit" class="btn bh-btn-primary w-100">Gửi câu hỏi</button>
                  </form>
                </c:otherwise>
              </c:choose>
            </div>
          </div>
        </div>
      </section>

      <!-- Similar Books -->
      <c:if test="${not empty book.similarBooks}">
        <section aria-labelledby="similarHeading">
          <h2 id="similarHeading" class="h3 mb-3">Sách tương tự</h2>
          <div class="row g-3 row-cols-2 row-cols-md-4">
            <c:forEach var="item" items="${book.similarBooks}">
              <div class="col">
                <c:set var="bookCard" value="${item}" scope="request" />
                <jsp:include page="/WEB-INF/views/common/book-card.jsp" />
              </div>
            </c:forEach>
          </div>
        </section>
      </c:if>
    </div>
  </main>
  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
