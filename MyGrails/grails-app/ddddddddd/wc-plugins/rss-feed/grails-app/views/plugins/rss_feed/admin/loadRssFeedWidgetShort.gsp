<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="rss.feed.url"/></div>
        <input type="text" class="sidebar-input" name="feed_url" value="${widget.content}" validation="required url">
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="no.of.item.to.display"/></div>
        <input type="text" class="sidebar-input" name="item_to_display" restrict="numeric" validation="required number gt[0]" value="${config.item_to_display}">
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="show.hide"/>: </div>
        <div class="sidebar-group-body">
            <input type="checkbox" class="single" name="show_title" value="true" uncheck-value="false" ${config.show_title == 'false'? "" : "Checked"}>
            <label><g:message code="show.feed.title"/></label>
        </div>
        <div class="sidebar-group-body">
            <input type="checkbox" class="single" name="show_content" value="true" uncheck-value="false" ${config.show_content == 'false'? "" : "Checked"}>
            <label><g:message code="show.item.content"/></label>
        </div>
        <div class="sidebar-group-body">
            <input type="checkbox" class="single" name="show_author" value="true" uncheck-value="false" ${config.show_author == "true" ? "checked" : ""}>
            <label><g:message code="show.item.author"/></label>
        </div>
        <div class="sidebar-group-body">
            <input type="checkbox" class="single" name="show_date" value="true" uncheck-value="false" ${config.show_date == "true" ? "checked" : ""}>
            <label><g:message code="show.item.date"/></label>
        </div>
    </div>
</g:applyLayout>