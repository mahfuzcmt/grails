<%@ page import="com.webcommander.admin.Operator" %>
<g:if test="${locationId < 1}">
    <div>
        <span><g:message code="no.venue.location.created"/> <span class="link create-location-first"><g:message code="create.venue.location.first"/></span></span>
    </div>
</g:if>
<g:else>
    <form action="${app.relativeBaseUrl()}eventAdmin/saveVenueLocationSection" method="post" class="create-edit-form location-section-create-form">
        <div class="form-section">
            <div class="form-section-info">
                <h3><g:message code="venue.location.section"/> </h3>
                <div class="info-content"><g:message code="form.section.text.event.venue.location"/></div>
            </div>
            <div class="form-section-container">
                <input type="hidden" name="id" value="${section?.id}">
                <input type="hidden" name="locationId" value="${locationId}">
                <div class="double-input-row">
                    <div class="form-row mandatory">
                        <label><g:message code="name"/></label>
                        <input type="text" name="name" class="large unique" value="${section.name}" validation="required rangelength[2, 100]"
                               maxlength="100" unique-action="isUniqueVenueLocationSection" composite-unique="venueLocation.id">
                        <input type="hidden" name="venueLocation.id" value="${locationId}">
                    </div><div class="form-row mandatory">
                        <label><g:message code="ticket.price"/></label>
                        <input type="text" class="large" name="ticketPrice" value="${section.ticketPrice?.toPrice() ?: ""}" validation="required number max[99999999]" restrict="decimal">
                    </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="ticket.name"/></label>
                        <input type="text" class="large" name="ticketName" value="${section.ticketName}">
                    </div><div class="form-row mandatory row-column">
                        <label><g:message code="row.column"/></label>
                        <g:set var="rowCount" value="${UUID.randomUUID().toString()}"/>
                        <g:set var="colCount" value="${UUID.randomUUID().toString()}"/>
                        <div class="twice-input-row">
                            <input type="text" id="${rowCount}" class="smaller ${rowCount}"
                                   name="rowCount" validation="required min[1] max[702]@if{global:.row-prefix-type option:eq(0):selected}"
                                   restrict="numeric" value="${section.rowCount}" ${ticketExists ? 'disabled' : ''}><span>x</span><input type="text" id="${colCount}" class="smaller ${colCount}" name="columnCount" validation="required min[1] max[702]@if{global:.column-prefix-type option:eq(0):selected}" restrict="numeric" value="${section.columnCount}" ${ticketExists ? 'disabled' : ''}>
                        </div>
                </div>
                </div>
                <div class="form-row">
                    <label>&nbsp;</label>
                </div>
                <div class="double-input-row">
                    <div class="form-row mandatory">
                        <label><g:message code="row.prefix.type"/></label>
                        <g:set var="rowType" value="${UUID.randomUUID().toString()}"/>
                        <g:select from="${[g.message(code: 'alphabetic'), g.message(code: 'numeric')]}" keys="['alphabetic','numeric']" name="rowPrefixType"
                                  class="large row-prefix-type ${rowType}" value="${section?.rowPrefixType}" disabled="${ticketExists}"/>
                    </div><div class="form-row mandatory">
                        <label><g:message code="order"/></label>
                        <g:set var="rowOrder" value="${UUID.randomUUID().toString()}"/>
                        <g:select from="${[g.message(code: 'ascending'), g.message(code: 'descending')]}" keys="['ascending','descending']" name="rowPrefixOrder" class="row-order large ${rowOrder}"
                                  value="${section.rowPrefixOrder}" disabled="${ticketExists}"/>
                    </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="starts.at"/></label>
                        <input type="text" name="rowPrefixStartsAt" class="large" value="${section.rowPrefixStartsAt}"
                               validation="match[(^[A-Z]]{1,2}$)]@if{global:.row-prefix-type option:eq(0):selected} match[^[1-9]]\d*$]@if{global:.row-prefix-type option:eq(1):selected} compare[${rowCount}, alphabetic, gte]@if{global:.row-prefix-type option:eq(0):selected}@if{global:.row-order option:eq(1):selected} compare[${rowCount}, numeric, gte]@if{global:.row-prefix-type option:eq(1):selected}@if{global:.row-order option:eq(1):selected}"
                               message_template_0="should.be.a2z" message_template_1="should.contain.digits" depends=".${rowCount}, .${rowType}, ${rowOrder}" ${ticketExists ? 'disabled' : ''}>
                    </div><div class="form-row">
                        <label><g:message code="access.between"/></label>
                        <input type="text" name="rowAccessBetween" class="large" value="${section.rowAccessBetween}" restrict="numeric"
                               validation="compare[${rowCount}, number, lte]" ${ticketExists ? 'disabled' : ''}>
                    </div>
                </div>

                <div class="form-row">
                    <label>&nbsp;</label>
                </div>
                <div class="double-input-row">
                    <div class="form-row mandatory">
                        <label><g:message code="column.prefix.type"/></label>
                        <g:set var="colType" value="${UUID.randomUUID().toString()}"/>
                        <g:select from="${[g.message(code: 'alphabetic'), g.message(code: 'numeric')]}" keys="['alphabetic','numeric']" name="columnPrefixType"
                                  class="large column-prefix-type ${colType}" value="${section.columnPrefixType}" disabled="${ticketExists}"/>
                    </div><div class="form-row mandatory">
                        <label><g:message code="order"/></label>
                        <g:set var="colOrder" value="${UUID.randomUUID().toString()}"/>
                        <g:select from="${[g.message(code: 'ascending'), g.message(code: 'descending')]}" keys="['ascending','descending']" name="columnPrefixOrder"
                                  class="column-order large ${colOrder}" value="${section.columnPrefixOrder}" disabled="${ticketExists}"/>
                    </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="starts.at"/></label>
                        <input type="text" name="columnPrefixStartsAt" class="large" value="${section.columnPrefixStartsAt}"
                               validation="match[(^[A-Z]]{1,2}$)]@if{global:.column-prefix-type option:eq(0):selected} match[^[1-9]]\d*$]@if{global:.column-prefix-type option:eq(1):selected} compare[${colCount}, alphabetic, gte]@if{global:.column-prefix-type option:eq(0):selected}@if{global:.column-order option:eq(1):selected} compare[${colCount}, numeric, gte]@if{global:.column-prefix-type option:eq(1):selected}@if{global:.column-order option:eq(1):selected}"
                               message_template_0="should.be.a2z" message_template_1="should.contain.digits" depends=".${colCount}, .${colType}, ${colOrder}" ${ticketExists ? 'disabled' : ''}>
                    </div><div class="form-row">
                        <label><g:message code="access.between"/></label>
                        <input type="text" name="columnAccessBetween" class="large" value="${section.columnAccessBetween}" restrict="numeric"
                               validation="compare[${colCount}, number, lte]" ${ticketExists ? 'disabled' : ''}>
                    </div>
                </div>
                <div class="form-row">
                    <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="${section.id ? "update" : "save"}"/></button>
                    <button type="button" class="cancel-button"><g:message code="cancel"/></button>
                </div>
            </div>
        </div>
    </form>
</g:else>