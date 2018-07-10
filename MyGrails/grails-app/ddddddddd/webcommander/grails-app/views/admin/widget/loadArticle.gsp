<g:form class="create-edit-form" controller="widget" action="saveArticleWidget">
    <span class="configure-btn" title="<g:message code="configuration"/> "><i></i></span>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="article.selection.info"/></h3>
            <div class="info-content"><g:message code="section.text.article.selection"/></div>
        </div>
        <div class="form-section-container">
            <div class="widget-config-panel">
                <div class="form-row">
                    <label><g:message code="title"/></label>
                    <input type="text" class="medium" name="title" value="${widget.title}">
                </div>
                <div class="form-row">
                    <label>&nbsp;</label>
                    <input type="checkbox" name="article_title" class="single" value="show" uncheck-value="hide" ${config.article_title == 'show' ? 'checked' : ''}>
                    <label><g:message code="show.article.title"/></label>
                </div>
                <div class="form-row">
                    <label><g:message code="display.option"/></label>
                    <div>
                        <div><input type="radio" class="radio" name="display_option" value="full" ${config.display_option ? (config.display_option == "full" ? "checked" : "") : "checked"}><g:message code="full.article"/></div>
                        <div><input type="radio" class="radio" name="display_option" value="summary" ${config.display_option == "summary" ? "checked" : ""}><g:message code="article.summary"/></div>
                        <div><input type="radio" class="radio" name="display_option" value="summary_link" ${config.display_option == "summary_link" ? "checked" : ""}><g:message code="summary.link"/></div>
                    </div>
                </div>
            </div>
            <div>
                <g:include view="/admin/content/selection.gsp" model="${[articles: articles, fieldName: "article"]}"/>
            </div>
            <div class="form-row">
                <button type="submit" class="edit-popup-form-submit submit-button"><g:message code="update"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</g:form>