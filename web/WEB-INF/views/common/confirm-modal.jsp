<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="modal fade" id="bhConfirmModal" tabindex="-1" aria-labelledby="bhConfirmTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content bh-card p-2">
      <div class="modal-header border-0 pb-1">
        <h2 class="modal-title fs-5 bh-serif" id="bhConfirmTitle">Xác nhận thao tác</h2>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
      </div>
      <div class="modal-body pt-2">
        <p class="mb-0 bh-muted" id="bhConfirmMessage">Bạn chắc chắn muốn thực hiện thao tác này?</p>
      </div>
      <div class="modal-footer border-0 pt-2">
        <button type="button" class="btn bh-btn-outline-neutral" data-bs-dismiss="modal">Giữ nguyên</button>
        <button type="button" class="btn bh-btn-danger" id="bhConfirmSubmitBtn">Xác nhận</button>
      </div>
    </div>
  </div>
</div>
