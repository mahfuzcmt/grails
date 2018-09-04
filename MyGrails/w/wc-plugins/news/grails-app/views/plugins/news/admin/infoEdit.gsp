<%@ page import="com.webcommander.content.Article" %>
<form action="${app.relativeBaseUrl()}news/save" method="post" class="edit-popup-form create-edit-form" enctype="multipart/form-data" xmlns="http://www.w3.org/1999/html">
    <input type="hidden" name="id" value="${news.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="news.info"/></h3>
            <div class="info-content"><g:message code="section.text.news.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="news.title"/><span class="suggestion"><g:message code="suggestion.news.title"/></span></label>
                    <input type="text" class="medium unique" name="title" value="${news.title.encodeAsBMHTML()}" validation="required rangelength[2,100]" maxlength="100">
                </div><div class="form-row mandatory">
                    <label><g:message code="news.date"/><span class="suggestion"><g:message code="suggestion.news.date"/></span></label>
                    <input type="text" class="create-edit-date timefield" no-next="false" name="newsDate" value="${news.newsDate?.toDatePickerFormat(true, session.timezone)}" validation="required">
                </div>
            </div>
            <div class="form-row mandatory mandatory-chosen-wrapper">
                <label><g:message code="article"/><span class="suggestion"> e.g. Choose Article Topic</span> </label>
                <ui:domainSelect name="article" class="medium" domain="${Article}" validation="required" filter="${{order("created", "desc")}}"
                                 value="${news.article ? news.article.id : ""}"/>
            </div>
            <div class="form-row thicker-row">
                <label><g:message code="summary"/><span class="suggestion"><g:message code="suggestion.news.summary"/></span></label>
                <textarea name="summary" class="medium" style="min-height: 120px;" maxlength="500" validation="maxlength[500]">${news.summary.encodeAsBMHTML()}</textarea>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="${news.id ? "update" : "save"}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>