<%@ page import="com.webcommander.content.Section" %>
<form action="${app.relativeBaseUrl()}snippetAdmin/saveSnippet" method="post" class="edit-popup-form create-edit-form">
    <input type="hidden" name="id" value="${snippet.id}">
    <div class="form-snippet">
        <div class="form-section-info">
            <h3><g:message code="snippet.info"/></h3>
            <div class="info-content"><g:message code="section.text.snippet.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row mandatory">
                <label><g:message code="name"/><span class="suggestion">e. g. Millionaire</span></label>
                <input type="text" class="medium unique" name="name" value="${snippet.name?.encodeAsBMHTML()}" validation="required rangelength[2,100]" unique-action="isSnippetUnique" maxlength="100">
            </div>
            <div class="form-row mandatory">
                <label><g:message code="section"/><span class="suggestion">e. g. E-Commerce</span></label>
                <ui:hierarchicalSelect name="parent" class="medium section-selector" domain="${Section}" prepend="${['': g.message(code:  "none")]}" value="${snippet.parent?.id}"/>
            </div>
            <div class="form-row">
                <label><g:message code="description"/><span class="suggestion">e. g. List of Millionaires </span></label>
                <textarea class="xx-larger" validation="maxlength[500]" maxlength="500" name="description">${snippet.description?:""}</textarea>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="${snippet.id ? "update" : "save"}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>