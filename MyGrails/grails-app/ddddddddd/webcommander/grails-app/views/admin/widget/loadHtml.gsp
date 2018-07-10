<form action="widget/saveHtmlWidget" class="create-edit-form" method="post">
    <span class="configure-btn" title="<g:message code="configuration"/> "><i></i></span>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="html.selection.info"/></h3>
            <div class="info-content"><g:message code="section.text.html.selection"/></div>
        </div>
        <div class="form-section-container">
            <div class="widget-config-panel">
                <div class="form-row">
                    <label><g:message code="title"/></label>
                    <input type="text" class="medium" name="title" value="${widget.title}">
                </div>
            </div>
            <div class="form-row thicker-row">
                <label><g:message code="content"/></label>
                <textarea class="wceditor no-auto-size" validation="rangelength[0, 1500]" maxlength="1500" toolbar-type="simple" name="content">${widget.content}</textarea>
            </div>
            <div class="form-row file-container thicker-row">
                <label><g:message code="from.file"/></label>
                <input type="file" file-type="text" text-helper="no" size-limit="2048">
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>