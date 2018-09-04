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
        <div class="sidebar-group-body">
            <input type="checkbox" class="single" name="showAvailability" value="1" uncheck-value="0" ${config.showAvailability == '1' ? 'checked="checked"' : ''}/>
            <label><g:message code="availability"/></label>
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
        <div class="sidebar-group-body">
            <input type="radio" toggle-target="list-view-field" name="displayType" value="list" ${config.displayType == 'list' ? 'checked="checked"' : ''}>
            <label><g:message code="list"/></label>
        </div>
    </div>
    <div class="sidebar-group basic-calendar-view-field advance-calendar-view-field">
        <div class="sidebar-group-label" ><g:message code="calendar.view.settings"/></div>
        <div class="sidebar-group-body">
            <input type="checkbox" class="single" name="embedShortInfo" value="1" uncheck-value="0"  ${config.embedShortInfo == '1' ? 'checked="checked"' : ''}>
            <g:message code="embed.short.info"/>
        </div>
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
    <div class="sidebar-group list-view-field">
        <div class="sidebar-group-label" ><g:message code="list.view.settings"/></div>
        <div class="sidebar-group-body">
            <input type="radio" toggle-target="pagination-field" name="listViewType" value="paginated" ${config.listViewType == 'paginated' ? 'checked="checked"' : ''}>
            <g:message code="paginated.view"/>
        </div>
        <div class="sidebar-group-body">
            <input type="radio" name="listViewType" value="scrollable" ${config.listViewType == 'scrollable' ? 'checked="checked"' : ''}>
            <g:message code="scrollable.view"/>
        </div>
        <div class="sidebar-group-body">
            <input type="radio" toggle-target="show-all-field" name="listViewType" value="show_all" ${config.listViewType != 'scrollable' && config.listViewType != 'paginated' ? 'checked="checked"' : ''}>
            <g:message code="show.all"/>
        </div>
    </div>
    <div class="sidebar-group pagination-field list-view-field">
        <div class="sidebar-group-label" ><g:message code="show.pagination"/></div>
        <div class="sidebar-group-body">
            <select name="paginationPlacement" class="sidebar-input">
                <option value="topAndBottom" ${config.paginationPlacement == 'topAndBottom' ? 'selected="selected"' : ''}><g:message code="top.bottom"/></option>
                <option value="top" ${config.paginationPlacement == 'top' ? 'selected="selected"' : ''}><g:message code="top"/></option>
                <option value="bottom" ${config.paginationPlacement == 'bottom' ? 'selected="selected"' : ''}><g:message code="bottom"/></option>
            </select>
        </div>
    </div>
    <div class="sidebar-group pagination-field list-view-field">
        <div class="sidebar-group-label" ><g:message code="items.per.page"/></div>
        <input type="text" name="itemsPerPage" value="${config.itemsPerPage}" class="sidebar-input">
    </div>
    <div class="sidebar-group-body show-all-field list-view-field">
        <div class="sidebar-group-label" ><g:message code="height"/></div>
        <input type="text"  name="listViewHeight" value="${config.listViewHeight}" class="smaller sidebar-input  single-action">
        <span class="note">px</span>
    </div>
</g:applyLayout>