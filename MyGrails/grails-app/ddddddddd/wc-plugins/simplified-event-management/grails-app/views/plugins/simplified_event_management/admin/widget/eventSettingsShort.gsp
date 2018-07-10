<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="show"/></div>
        <div class="sidebar-group-body">
            <input type="checkbox" class="single" name="showPrice" value="1" uncheck-value="0" ${config.showPrice == '1' ? 'checked="checked"' : ''}/>
            <label><g:message code="price"/></label>
        </div>
        <div class="sidebar-group-body">
            <input type="checkbox" class="single" name="showBookNow" value="1" uncheck-value="0" ${config.showBookNow == '1' ? 'checked="checked"' : ''}/>
            <label><g:message code="book.now"/></label>
        </div>
        <div class="sidebar-group-label">
            <g:message code="label.for.book.now"/>
        </div>
        <div class="sidebar-group-body">
            <input type="text" name="labelForBookNow" class="sidebar-input" value="${config.labelForBookNow ?: "s:book.now"}" validation="required">
        </div>
        <div class="sidebar-group-body">
            <input type="checkbox" class="single" name="showRequestInfo" value="1" uncheck-value="0" ${config.showRequestInfo == '1' ? 'checked="checked"' : ''}/>
            <label><g:message code="request.info"/></label>
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label" ><g:message code="display.type"/></div>
        <div class="sidebar-group-body">
            <input type="radio" toggle-target="basic-calendar-view-field" name="displayType" value="basic-calendar" ${config.displayType == 'basic-calendar' ? 'checked="checked"' : ''}>
            <label><g:message code="basic.calendar"/></label>
        </div>
        <div class="sidebar-group-body">
            <input type="radio" toggle-target="advance-calendar-view-field" name="displayType" value="advance-calendar" ${config.displayType == 'advance-calendar' ? 'checked="checked"' : ''}>
            <label><g:message code="advance.calendar"/></label>
        </div>
    </div>
    <div class="sidebar-group basic-calendar-view-field advance-calendar-view-field">
        <div class="sidebar-group-label" ><g:message code="calendar.view.settings"/></div>
        <div class="sidebar-group-body">
            <input type="radio" name="detailsOn" value="mouseover" ${config.detailsOn == 'mouseover' ? 'checked="checked"' : ''}>
            <g:message code="details.on.hover"/>
        </div>
        <div class="sidebar-group-body">
            <input type="radio" name="detailsOn" value="click" ${config.detailsOn == 'click' ? 'checked="checked"' : ''}>
            <g:message code="details.on.click"/>
        </div>
    </div>
    <div class="sidebar-group basic-calendar-view-field advance-calendar-view-field">
        <div class="sidebar-group-label" ><g:message code="show.days"/></div>
        <div class="sidebar-group-body">
            <input type="radio" name="dayChar" value="1" ${config.dayChar == '1' ? 'checked="checked"' : ''}>
            <g:message code="n.character" args="${[1]}"/>
        </div>
        <div class="sidebar-group-body">
            <input type="radio" name="dayChar" value="3" ${config.dayChar == '3' ? 'checked="checked"' : ''}>
            <g:message code="n.character" args="${[3]}"/>
        </div>
        <div class="sidebar-group-body">
            <input type="radio" name="dayChar" value="-1" ${config.dayChar == '-1' ? 'checked="checked"' : ''}>
            <g:message code="full"/>
        </div>
    </div>
</g:applyLayout>