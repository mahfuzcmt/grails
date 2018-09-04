<g:if test="${equipments.size() > 0}">
    <form action="${app.relativeBaseUrl()}eventAdmin/saveEquipmentBooking" method="post" class="edit-popup-form create-edit-form">
        <div class="form-section">
            <div class="form-section-info">
                <h3><g:message code="book.equipment.info"/> </h3>
                <div class="info-content"><g:message code="form.section.text.book.equipment"/></div>
            </div>
            <div class="form-section-container">
                <input type="hidden" name="eventId" value="${eventId}">
                <input type="hidden" name="sessionId" value="${sessionId}">
                <div class="form-row venue  chosen-wrapper">
                    <label><g:message code="select.equipment"/></label>
                    <g:select from="${equipments.name}" keys="${equipments.id}" name="equipment" class="large equipment-selector"/>
                </div>
                <div class="form-row">
                    <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="update"/></button>
                    <button type="button" class="cancel-button"><g:message code="cancel"/></button>
                </div>
            </div>
        </div>
    </form>
</g:if>
<g:else>
    <div class="info-row">
        <span class="info-message"><g:message code="no.equipment.created"/> <span class="link activate-equipment-tab"><g:message code='create.equipment.first'/></span></span>
    </div>
</g:else>