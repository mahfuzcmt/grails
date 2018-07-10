<%@ page import="com.webcommander.plugin.blog.content.BlogCategory" %>
<form action="${app.relativeBaseUrl()}blogAdmin/saveCategory" method="post" class="edit-popup-form">
    <input type="hidden" name="id" value="${category.id}">
    <div class="form-row mandatory">
        <label><g:message code="name"/></label>
        <input type="text" class="medium" name="name" value="${category.name.encodeAsBMHTML()}" validation="required">
    </div>
    <div class="form-row">
        <label><g:message code="parent.section"/></label>
        <ui:hierarchicalSelect name="parent" class="medium section-selector" domain="${BlogCategory}" filter="${category.id ? {ne("id", category.id)} : {}}" prepend="${['': g.message(code: "no.parent")]}" value="${category.parent?.id}"/>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="${category.id ? "update" : "save"}"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>