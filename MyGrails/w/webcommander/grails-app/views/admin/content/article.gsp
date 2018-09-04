<%@ page import="com.webcommander.content.Section" %>
<form action="${app.relativeBaseUrl()}content/saveArticle" method="post" class="edit-popup-form create-edit-form">
    <input type="hidden" name="id" value="${article.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="article.info"/></h3>
            <div class="info-content"><g:message code="section.text.article.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row mandatory">
                <label><g:message code="name"/><span class="suggestion">e. g. Millionaire</span></label>
                <input type="text" class="medium unique" name="name" value="${article.name.encodeAsBMHTML()}" validation="required rangelength[2,100]" unique-action="isArticleUnique"
                       unique-restore="restoreArticleFromTrash" maxlength="100">
            </div>
            <div class="form-row trash-row" style="display: none;">
                <label><g:message code="what.to.do"/></label>
                <span><a onclick="return false" class="trash-duplicate-restore fake-link"><g:message code="restore"/></a> <g:message code="restore.and.close.window"/> <g:message code="or"/></span>
                <span><input type="checkbox" name="deleteTrashItem.name" class="trash-duplicate-delete multiple"> <g:message code="delete.and.save"/></span>
            </div>
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="visibility"/><span class="suggestion">e. g. Published</span></label>
                    <g:select name="isPublished" class="medium" from="${[g.message(code: "published"), g.message(code: "unpublished")]}" keys="${["true","false"]}" value="${article.isPublished}" />
                </div><div class="form-row chosen-wrapper">
                    <label><g:message code="section"/><span class="suggestion">e. g. E-Commerce</span></label>
                    <ui:hierarchicalSelect name="section" class="medium section-selector" domain="${Section}" prepend="${['': g.message(code:  "none")]}" value="${article.section?.id}"/>
                </div>
            </div>
            <div class="form-row">
                <label><g:message code="summary"/><span class="suggestion">e. g. List of Millionaires</span></label>
                <textarea class="xx-larger" validation="maxlength[500]" maxlength="500" name="summary">${article.summary}</textarea>
            </div>
            <div class="form-row thicker-row">
                <label><g:message code="content"/><span class="suggestion">e. g. Millionaires Details List</span></label>
                <textarea class="wceditor no-auto-size xx-larger" toolbar-type="advanced" name="content" validation="maxlength[65500]" maxlength="65500">${article.content}</textarea>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="${article.id ? "update" : "save"}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>