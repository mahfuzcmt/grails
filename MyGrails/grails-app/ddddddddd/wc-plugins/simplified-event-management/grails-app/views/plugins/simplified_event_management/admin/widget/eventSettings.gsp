<g:form class="edit-popup-form create-edit-form" controller="widget" action="saveSimplifiedEventWidget">
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
                            <option value="event" ${config.selectionType == 'event' ? 'selected="selected"' : ''}><g:message code="selected.events"/></option>
                        </select>
                    </div>
                </div>
                <div class="tool-group">
                    <span class="configure-btn" title="<g:message code="configuration"/>"><i></i></span>
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
                        <div class="sidebar-group-label">
                            <g:message code="label.for.book.now"/>
                        </div>
                        <div class="sidebar-group-body">
                            <input type="text" name="labelForBookNow" class="sidebar-input" value="${config.labelForBookNow ?: "s:book.now"}" validation="required">
                        </div>
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
                </div>
                <div class="form-row basic-calendar-view-field advance-calendar-view-field">
                    <label><g:message code="calendar.view.settings"/></label>
                    <input type="radio" name="detailsOn" value="mouseover" ${config.detailsOn != 'click' ? 'checked="checked"' : ''}/><g:message code="details.on.hover"/>
                    <input type="radio" name="detailsOn" value="click" ${config.detailsOn == 'click' ? 'checked="checked"' : ''}/><g:message code="details.on.click"/>
                </div>
                <div class="form-row basic-calendar-view-field advance-calendar-view-field">
                    <label><g:message code="show.days"/></label>
                    <input type="radio" name="dayChar" value="1" ${config.dayChar == '1' ? 'checked="checked"' : ''}/><g:message code="n.character" args="${[1]}"/>
                    <input type="radio" name="dayChar" value="3" ${config.dayChar == '3' ? 'checked="checked"' : ''}/><g:message code="n.character" args="${[3]}"/>
                    <input type="radio" name="dayChar" value="-1" ${config.dayChar != '1' && config.dayChar != '3' ? 'checked="checked"' : ''}/><g:message code="full"/>
                </div>
            </div>
            <div class="selection-panel">
                <g:if test="${config.selectionType == 'event'}">
                    <g:include view="plugins/simplified_event_management/admin/widget/eventSelectionPanel.gsp" model="[selectedEvents: selectedEvents]"/>
                </g:if>
            </div>
            <div class="button-line btn-row">
                <button type="submit" class="edit-popup-form-submit submit-button"><g:message code="update"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</g:form>