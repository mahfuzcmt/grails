<%@ page import="com.webcommander.webcommerce.Category; com.webcommander.admin.Operator" %>
<g:form action="${app.relativeBaseUrl()}zone/loadAppView" class="edit-popup-form">
    <div class="form-row">
        <label><g:message code="name"/></label>
        <input name="searchText" type="text" class="medium">
    </div>
    <div class="form-row">
        <label><g:message code="country"/></label>
        <input name="country" type="text" class="medium">
    </div>
    <div class="form-row">
        <label><g:message code="state"/></label>
        <input name="state" type="text" class="medium">
    </div>
    <div class="form-row">
        <label><g:message code="post.code"/></label>
        <input name="post-code" type="text" class="medium">
    </div>

    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</g:form>