<g:form class="edit-popup-form create-edit-form" controller="widget" action="saveEventWidget">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="event.selection.info"/></h3>
            <div class="info-content"><g:message code="section.text.event.selection"/></div>
        </div>
        <div class="form-section-container">
            <div class="filter-block">
                <div class="widget-toolbar">
                    <div class="left-bar">
                        <select name="selectionType" class="selection-type">
                            <option value="all" ${config.selectionType == 'all' ? 'selected="selected"' : ''}><g:message code="all.public.upcoming.events"/></option>
                            <option value="event" ${config.selectionType == 'event' ? 'selected="selected"' : ''}><g:message code="selected.events"/></option>
                            <option value="venueLocation" ${config.selectionType == 'venueLocation' ? 'selected="selected"' : ''}><g:message code="events.selected.venues"/></option>
                        </select>
                    </div>
                    <div class="tool-group">
                        <span class="configure-btn" title="<g:message code="configuration"/>"><i></i></span>
                    </div>
                </div>
            </div>
            <div class="widget-config-panel">
                <div class="form-row">
                    <label><g:message code="title"/></label>
                    <input type="text" class="medium" name="title" value="${widget.title}">
                </div>
                <div class="form-row">
                    <label><g:message code="show"/></label>
                    <div>
                        <input type="checkbox" class="single" name="showPrice" value="1" uncheck-value="0" ${config.showPrice == '1' ? 'checked="checked"' : ''}/>
                        <g:message code="price"/>
                        <input type="checkbox" class="single" name="showBookNow" value="1" uncheck-value="0" ${config.showBookNow == '1' ? 'checked="checked"' : ''}/>
                        <g:message code="book.now"/>
                        <input type="checkbox" class="single" name="showAvailability" value="1" uncheck-value="0" ${config.showAvailability == '1' ? 'checked="checked"' : ''}/>
                        <g:message code="availability"/>
                        <input type="checkbox" class="single" name="showRequestInfo" value="1" uncheck-value="0" ${config.showRequestInfo == '1' ? 'checked="checked"' : ''}/>
                        <g:message code="request.info"/>
                    </div>
                </div>
                <div class="form-row">
                    <label><g:message code="display.type"/></label>
                    <input type="radio" toggle-target="basic-calendar-view-field" name="displayType" value="basic-calendar"
                        ${config.displayType != 'advance-calendar' || config.displayType != 'list' ? 'checked="checked"' : ''}/><g:message code="basic.calendar"/>
                    <input type="radio" toggle-target="advance-calendar-view-field" name="displayType" value="advance-calendar"
                        ${config.displayType == 'advance-calendar' ? 'checked="checked"' : ''}/><g:message code="advance.calendar"/>
                    <input type="radio" toggle-target="list-view-field" name="displayType" value="list"
                        ${config.displayType == 'list' ? 'checked="checked"' : ''}/><g:message code="list"/>
                </div>
                <div class="form-row basic-calendar-view-field advance-calendar-view-field">
                    <label><g:message code="calendar.view.settings"/></label>
                    <input type="checkbox" class="single" name="embedShortInfo" value="1" uncheck-value="0"  ${config.embedShortInfo == '1' ? 'checked="checked"' : ''}/>
                        <g:message code="embed.short.info"/>
                    <input type="radio" name="detailsOn" value="mouseover" ${config.detailsOn != 'click' ? 'checked="checked"' : ''}/><g:message code="details.on.hover"/>
                    <input type="radio" name="detailsOn" value="click" ${config.detailsOn == 'click' ? 'checked="checked"' : ''}/><g:message code="details.on.click"/>
                </div>
                <div class="form-row basic-calendar-view-field advance-calendar-view-field">
                    <label><g:message code="show.days"/></label>
                    <input type="radio" name="dayChar" value="1" ${config.dayChar == '1' ? 'checked="checked"' : ''}/><g:message code="n.character" args="${[1]}"/>
                    <input type="radio" name="dayChar" value="3" ${config.dayChar == '3' ? 'checked="checked"' : ''}/><g:message code="n.character" args="${[3]}"/>
                    <input type="radio" name="dayChar" value="-1" ${config.dayChar != '1' && config.dayChar != '3' ? 'checked="checked"' : ''}/><g:message code="full"/>
                </div>
                <div class="form-row list-view-field">
                    <label><g:message code="list.view.settings"/></label>
                    <input type="radio" toggle-target="pagination-field" name="listViewType" value="paginated"
                        ${config.listViewType == 'paginated' ? 'checked="checked"' : ''}/><g:message code="paginated.view"/>
                    <input type="radio" name="listViewType" value="scrollable" ${config.listViewType == 'scrollable' ? 'checked="checked"' : ''}/>
                        <g:message code="scrollable.view"/>
                    <input type="radio" toggle-target="show-all-field" name="listViewType" value="show_all"
                    ${config.listViewType != 'scrollable' && config.listViewType != 'paginated' ? 'checked="checked"' : ''}/>
                    <g:message code="show.all"/>
                </div>
                <div class="form-row pagination-field list-view-field">
                    <label><g:message code="show.pagination"/></label>
                    <select name="paginationPlacement" class="medium">
                        <option value="topAndBottom" ${config.paginationPlacement == 'topAndBottom' ? 'selected="selected"' : ''}><g:message code="top.bottom"/></option>
                        <option value="top" ${config.paginationPlacement == 'top' ? 'selected="selected"' : ''}><g:message code="top"/></option>
                        <option value="bottom" ${config.paginationPlacement == 'bottom' ? 'selected="selected"' : ''}><g:message code="bottom"/></option>
                    </select>
                </div>
                <div class="form-row pagination-field list-view-field">
                    <label><g:message code="items.per.page"/></label>
                    <input type="text" name="itemsPerPage" value="${config.itemsPerPage}" class="medium">
                </div>
                <div class="form-row show-all-field list-view-field">
                    <label><g:message code="height"/></label>
                    <input type="text" name="listViewHeight" value="${config.listViewHeight}" class="medium"> &nbsp;<span class="note">px</span>
                </div>
            </div>
            <div class="selection-panel">
                <g:if test="${config.selectionType == 'event'}">
                    <g:include view="plugins/event_management/admin/widget/eventSelectionPanel.gsp" model="[selectedEvents: selectedEvents]"/>
                </g:if>
                <g:else>
                    <g:include view="plugins/event_management/admin/widget/venueLocationSelectionPanel.gsp" model="[selectedLocations: selectedLocations]" />
                </g:else>
            </div>
            <div class="button-line btn-row">
                <button type="submit" class="edit-popup-form-submit submit-button"><g:message code="update"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</g:form>