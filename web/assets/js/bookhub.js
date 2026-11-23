/* bookhub.js — Minimal vanilla ES6 helpers: quantity stepper, confirm modal, password toggle, cover preview, char counter, anti-double-submit. */
document.addEventListener("DOMContentLoaded", function () {
  // 1. Quantity stepper (- / +)
  document.querySelectorAll("[data-qty-stepper]").forEach(function (wrap) {
    var input = wrap.querySelector("input[type='number']");
    var dec = wrap.querySelector("[data-qty-dec]");
    var inc = wrap.querySelector("[data-qty-inc]");
    if (!input) return;
    var min = parseInt(input.getAttribute("min") || "1", 10);
    var max = parseInt(input.getAttribute("max") || "999", 10);
    if (dec) {
      dec.addEventListener("click", function () {
        var v = parseInt(input.value || String(min), 10);
        input.value = String(Math.max(min, isNaN(v) ? min : v - 1));
      });
    }
    if (inc) {
      inc.addEventListener("click", function () {
        var v = parseInt(input.value || String(min), 10);
        input.value = String(Math.min(max, isNaN(v) ? min : v + 1));
      });
    }
  });

  // 2. Password visibility toggle
  document.querySelectorAll("[data-toggle-password]").forEach(function (btn) {
    btn.addEventListener("click", function () {
      var targetId = btn.getAttribute("data-toggle-password");
      var input = document.getElementById(targetId);
      if (!input) return;
      var showing = input.type === "text";
      input.type = showing ? "password" : "text";
      btn.setAttribute("aria-pressed", String(!showing));
      btn.textContent = showing ? "Hiện" : "Ẩn";
    });
  });

  // 3. Cover image URL live preview (SCR-16)
  var coverInput = document.getElementById("coverUrl");
  var coverPreview = document.getElementById("coverPreviewImg");
  if (coverInput && coverPreview) {
    var fallback = coverPreview.getAttribute("data-fallback") || "";
    coverInput.addEventListener("input", function () {
      var val = coverInput.value.trim();
      coverPreview.src = val ? val : fallback;
    });
    coverPreview.addEventListener("error", function () {
      if (fallback && coverPreview.src !== fallback) {
        coverPreview.src = fallback;
      }
    });
  }

  // 4. Live character counter (SCR-03 Q&A / Review)
  document.querySelectorAll("[data-char-count]").forEach(function (el) {
    var out = document.getElementById(el.getAttribute("data-char-count"));
    if (!out) return;
    var max = el.getAttribute("maxlength") || "";
    var update = function () {
      out.textContent = el.value.length + (max ? "/" + max : "");
    };
    el.addEventListener("input", update);
    update();
  });

  // 5. Shared Bootstrap Confirm Modal
  var modalEl = document.getElementById("bhConfirmModal");
  if (modalEl && typeof bootstrap !== "undefined") {
    var bsModal = new bootstrap.Modal(modalEl);
    var msgEl = document.getElementById("bhConfirmMessage");
    var titleEl = document.getElementById("bhConfirmTitle");
    var okBtn = document.getElementById("bhConfirmSubmitBtn");
    var pendingForm = null;

    document.querySelectorAll("[data-confirm-message]").forEach(function (trigger) {
      trigger.addEventListener("click", function (ev) {
        ev.preventDefault();
        pendingForm = trigger.closest("form");
        if (!pendingForm) return;
        if (titleEl) titleEl.textContent = trigger.getAttribute("data-confirm-title") || "Xác nhận thao tác";
        if (msgEl) msgEl.textContent = trigger.getAttribute("data-confirm-message") || "Bạn chắc chắn muốn tiếp tục?";
        if (okBtn) okBtn.textContent = trigger.getAttribute("data-confirm-ok") || "Xác nhận";
        bsModal.show();
      });
    });

    if (okBtn) {
      okBtn.addEventListener("click", function () {
        if (pendingForm) {
          okBtn.disabled = true;
          pendingForm.submit();
        }
      });
    }
  }

  // 6. Fallback cover image when cover_url fails to load (EC-04)
  document.querySelectorAll("img[data-fallback]").forEach(function (img) {
    img.addEventListener("error", function () {
      var fb = img.getAttribute("data-fallback");
      if (fb && img.src.indexOf(fb) === -1) {
        img.src = fb;
      }
    });
  });

  // 7. Prevent accidental double POST submission
  document.querySelectorAll("form[method='post'], form[method='POST']").forEach(function (form) {
    form.addEventListener("submit", function () {
      var submitBtn = form.querySelector("button[type='submit']:not([data-confirm-message])");
      if (submitBtn && !submitBtn.disabled) {
        submitBtn.disabled = true;
        submitBtn.setAttribute("data-original-text", submitBtn.textContent);
        submitBtn.textContent = "Đang xử lý...";
      }
    });
  });
});
