<form action="${app.relativeBaseUrl()}eventAdmin/voidComplementaryTicket" method="post" class="edit-popup-form">
    <div class="form-row">
        <span><g:message code="number.of.tickets.void"/></span>:
        ${tickets.size()}
        <input type="hidden" name="ticketCount" value="${tickets.size()}">
    </div>
    <input type="hidden" name="ticketNumber" value="${params.id}">
    <div class="form-row">
        <input type="radio" name="void" value="void" checked><span><g:message code="void"/></span>
        <input toggle-target="ticket-number-replace" type="radio" name="void" value="replace"><span><g:message code="replace"/></span>
    </div>
    <div class="form-row mandatory ticket-number-replace">
        <label><g:message code="seats"/></label>
        <div class="large multitxtchosen" data-placeholder="<g:message code='enter.seat.numbers'/>" chosen-validation='function[validateEventSeat]' name="seat" validation="chosen-required">
        </div>
    </div>
    <div class="form-row ticket-number-replace">
        <label><g:message code="send.email"/></label>
        <input type="checkbox" class="single" name="sendMail" value="true" uncheck-value="false" toggle-target="email-row">
    </div>
    <div class="form-row email-row">
        <label><g:message code="email"/></label>
        <input type="text" class="large" validation="required@if{self::visible} email" name="email">
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="void"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>