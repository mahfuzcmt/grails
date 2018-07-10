<%@ page import="com.webcommander.plugin.event_management.VenueLocationSection" %>
<form action="${app.relativeBaseUrl()}eventAdmin/saveComplementaryTicket" method="post" class="edit-popup-form create-edit-form">
    <input type="hidden" name="eventId" value="${eventId}">
    <input type="hidden" name="sessionId" value="${sessionId}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="complementary.ticket.info"/></h3>
            <div class="info-content"><g:message code="section.text.complementary.ticket.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="section"/></label>
                <g:select from="${sections}" optionKey="id" optionValue="name" name="section" class="large"/>
                <g:each in="${sections}" var="section">
                    <input type="hidden" id="pattern-storage-${section.id}" value="${section.rowPrefixType == 'alphabetic' ? '([A-Z]{1,2})(\\d+)' : '(\\d+)([A-Z]{1,2})'}">
                </g:each>
            </div>
            <div class="form-row mandatory">
                <label><g:message code="seats"/></label>
                <div class="large multitxtchosen" data-placeholder="<g:message code='enter.seat.numbers'/>" chosen-validation='function[validateEventSeat]' name="seat" validation="chosen-required">
                </div>
            </div>
            <div class="form-row">
                <label><g:message code="send.email"/></label>
                <input type="checkbox" class="single" name="sendMail" value="true" uncheck-value="false" toggle-target="email-row">
            </div>
            <div class="form-row email-row">
                <label><g:message code="email"/></label>
                <input type="text" class="large" validation="required@if{self::visible} email" name="email">
            </div>
            <div class="button-line">
                <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="save"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>

</form>