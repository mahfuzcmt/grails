<g:if test="${equipments.size() > 0}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="add.equipment.info"/> </h3>
            <div class="info-content"><g:message code="form.section.text.select.equipment.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row venue  chosen-wrapper">
                <label><g:message code="select.equipment"/></label>
                <g:select from="${equipments.name}" keys="${equipments.id}" name="equipment" class="large equipment-selector"/>
            </div>
        </div>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button add-equipment-btn edit-popup-form-submit"><g:message code="save"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</g:if>
<g:else>
    <div class="info-row">
        <span class="info-message"><g:message code="no.equipment.created"/> <span class="link switch-to-equipment-tab"><g:message code='create.equipment.first'/></span></span>
    </div>
</g:else>