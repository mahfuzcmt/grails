<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="video.id"/> </div>
        <div class="sidebar-group-body">
            <input type="text" name="mediaUrl" class="sidebar-input" value="${widget.content}">
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="width" /></div>
        <div class="sidebar-group-body">
            <input type="text" name="width" class="sidebar-input" validation="number maxlength[3]" value="${config.width}"/>
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="height" /></div>
        <div class="sidebar-group-body">
            <input type="text" name="height" class="sidebar-input" validation="number maxlength[3]" value="${config.height}" />
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="auto.play"/></div>
        <div class="sidebar-group-body">
            <input type="checkbox" class="single" name="autoPlay" value="1" uncheck-value="0" ${config.autoPlay == "1" ? "checked" : ""}>
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="show.suggested.videos.finishes"/></div>
        <div class="sidebar-group-body">
            <input type="checkbox" class="single" name="showSuggesion" value="1" uncheck-value="0" ${config.showSuggesion == "1" ? "checked" : ""}>
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="show.player.controls"/></div>
        <div class="sidebar-group-body">
            <input type="checkbox" class="single" name="showControls" value="1" uncheck-value="0" ${config.showControls == "1" ? "checked" : ""}>
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="show.video.title.player.actions"/></div>
        <div class="sidebar-group-body">
            <input type="checkbox" class="single" name="showInfo" value="1" uncheck-value="0" ${config.showInfo == "1" ? "checked" : ""}>
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="allow.full.screen"/> </div>
        <div class="sidebar-group-body">
            <input type="radio" name="allowFullScreen" class="radio" value="yes" ${config.allowFullScreen == "yes" ? "checked" : ""}>
            <label><g:message code="yes"/></label>
        </div>
        <div class="sidebar-group-body">
            <input type="radio" name="allowFullScreen" class="radio" value="no" ${config.allowFullScreen == "no" ? "checked" : ""}>
            <label><g:message code="no"/></label>
        </div>
    </div>
</g:applyLayout>