<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty flashSuccess}">
  <div class="bh-alert bh-alert-success mb-4" role="status">
    <div><c:out value="${flashSuccess}" /></div>
  </div>
</c:if>
<c:if test="${not empty flashError}">
  <div class="bh-alert bh-alert-danger mb-4" role="alert">
    <div><c:out value="${flashError}" /></div>
  </div>
</c:if>
<c:if test="${not empty flashWarning}">
  <div class="bh-alert bh-alert-warning mb-4" role="status">
    <div><c:out value="${flashWarning}" /></div>
  </div>
</c:if>
<c:if test="${not empty flashInfo}">
  <div class="bh-alert bh-alert-info mb-4" role="status">
    <div><c:out value="${flashInfo}" /></div>
  </div>
</c:if>
<c:if test="${not empty errorMessage}">
  <div class="bh-alert bh-alert-danger mb-4" role="alert">
    <div><c:out value="${errorMessage}" /></div>
  </div>
</c:if>
<c:if test="${not empty successMessage}">
  <div class="bh-alert bh-alert-success mb-4" role="status">
    <div><c:out value="${successMessage}" /></div>
  </div>
</c:if>
