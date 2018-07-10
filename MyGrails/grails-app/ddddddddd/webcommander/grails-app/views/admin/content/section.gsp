<%@ page import="com.webcommander.content.Section" %>
<form action="${app.relativeBaseUrl()}content/saveSection" method="post" class="edit-edit-form create-edit-form">
    <input type="hidden" name="id" value="${section.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="section.info"/></h3>
            <div class="info-content"><g:message code="section.text.section.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row mandatory">
                <label><g:message code="name"/><span class="suggestion">e. g. E-Commerce</span></label>
                <input type="text" class="medium unique" name="name" value="${section.name.encodeAsBMHTML()}" validation="required rangelength[2,100]" unique-action="isSectionUnique" maxlength="100">
            </div>
            <div class="form-row">
                <label><g:message code="parent.section"/><span class="suggestion">e. g. No Parent</span></label>
                <ui:hierarchicalSelect name="parent" class="medium section-selector" domain="${Section}" filter="${section.id ? {ne("id", section.id)} : {}}" prepend="${['': g.message(code: "no.parent")]}" value="${section.parent?.id}"/>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="${section.id ? "update" : "save"}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>