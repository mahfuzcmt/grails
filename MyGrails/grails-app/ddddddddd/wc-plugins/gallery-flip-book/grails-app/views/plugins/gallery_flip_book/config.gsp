<div class="gallery-config-view">
    <%@ page import="com.webcommander.plugin.gallery_flip_book.FlipBookConstants; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
    <div class="double-input-row">
        <div class="form-row mandatory">
            <label><g:message code="width"/></label>
            <input type="text" name="width" value="${widgetConfig['width'] ?: 1000}" validation="digits required" class="small" restrict="numeric">
            <span class="note">(px)</span>
        </div><div class="form-row mandatory">
            <label><g:message code="height"/></label>
            <input type="text" name="height" value="${widgetConfig['height'] ?: 800}" validation="digits required" class="small" restrict="numeric">
            <span class="note">(px)</span>
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="auto.center"/></label>
            <input type="checkbox" class="single" name="autoCenter" value="true" ${widgetConfig['autoCenter'] == "true" ? "checked": ""}/>
        </div><div class="form-row">
            <label><g:message code="flipping.direction"/></label>
            <g:select name="flippingDirection" from="${FlipBookConstants.FLIPPING_DIRECTION}" value="${widgetConfig['flippingDirection']}" optionKey="key" optionValue="value" class="small"/>
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="show.page"/></label>
            <g:select name="displayPage" from="${FlipBookConstants.SHOW_PAGE}" value="${widgetConfig['displayPage']}" optionKey="key" optionValue="value" class="small"/>
        </div><div class="form-row mandatory">
            <label><g:message code="transition.duration"/></label>
            <input type="text" name="transDuration" value="${widgetConfig['transDuration'] ?: 1000 }" validation="digits required" restrict="numeric" class="small">
            <span class="note">(ms)</span>
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row mandatory">
            <label><g:message code="starting.page.number"/></label>
            <input type="text" name="startPage" value="${widgetConfig['startPage'] ?: "1"}" validation="digits required gt[0]" restrict="numeric" class="small">
        </div><div class="form-row">
            <label><g:message code="show.gradients"/></label>
            <input type="checkbox" class="single" name="showGradients" value="true" ${widgetConfig['showGradients'] == "true" ? "checked": ""}/>
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="enable.zooming"/></label>
            <input type="checkbox" class="single" name="zooming" value="true" ${widgetConfig['zooming'] == "true" ? "checked": ""}/>
        </div><div class="form-row">
            <label><g:message code="show.thumbnails"/></label>
            <input type="checkbox" class="single" name="thumbnails" value="true" ${widgetConfig['thumbnails'] == "true" ? "checked": ""}/>
        </div>
    </div>
    <div class="button-line">
        <button type="button" class="previous"><g:message code="previous"/></button>
        <button type="submit" class="edit-popup-form-submit submit-button apply"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</div>
