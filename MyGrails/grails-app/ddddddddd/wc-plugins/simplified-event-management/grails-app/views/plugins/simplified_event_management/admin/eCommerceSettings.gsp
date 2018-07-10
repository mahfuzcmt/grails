<%@ page import="com.webcommander.plugin.simplified_event_management.constants.SimplifiedEventConstants" %>
<div class="section-separator"></div>
<div class="form-section">
    <div class="form-section-info">
        <h3><g:message code="event.inventory"/></h3>
        <div class="info-content"><g:message code="section.text.setting.event.inventoory"/></div>
    </div>
    <div class="form-section-container">
        <div class="form-row thicker-row chosen-wrapper">
            <label><g:message code="update.ticket.stock"/><span class="suggestion"><g:message code="suggestion.event.setting.ecommerce.update.stock.message"/></span></label>
            <ui:namedSelect class="medium" key="${SimplifiedEventConstants.UPDATE_TICKET_STOCK}" name="e_commerce.update_ticket_stock" value="${config.update_ticket_stock}"/>
        </div>
    </div>
</div>