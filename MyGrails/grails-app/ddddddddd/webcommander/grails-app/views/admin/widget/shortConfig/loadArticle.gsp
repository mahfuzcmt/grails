<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <div class="sidebar-group-body">
            <input type="checkbox" class="single" name="article_title" value="show" uncheck-value="hide" ${config.article_title == 'show' ? 'checked' : ''}>
            <label><g:message code="show.article.title"/></label>
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="display.option"/></div>
        <div class="sidebar-group-body">
            <div>
                <input type="radio" name="display_option" value="full" ${config.display_option == "full" ? "checked" : ""}
                <label><g:message code="full.article"/></label>
            </div>
            <div>
                <input type="radio" name="display_option" value="summary" ${config.display_option == "summary" ? "checked" : ""}>
                <label><g:message code="article.summary"/></label>
            </div>
            <div>
                <input type="radio" name="display_option" value="summary_link" ${config.display_option == "summary_link" ? "checked" : ""}>
                <label><g:message code="summary.link"/></label>
            </div>
        </div>
    </div>
</g:applyLayout>