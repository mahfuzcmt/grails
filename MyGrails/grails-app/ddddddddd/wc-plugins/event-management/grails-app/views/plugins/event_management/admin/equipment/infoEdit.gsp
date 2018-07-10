<%@ page import="com.webcommander.plugin.event_management.EquipmentType; com.webcommander.admin.Operator" %>
<form action="${app.relativeBaseUrl()}eventAdmin/saveEquipment" method="post" class="create-edit-form">
    <input type="hidden" name="id" value="${equipment.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="equipment.info"/> </h3>
            <div class="info-content"><g:message code="form.section.text.equipment.info"/> </div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="equipment.name"/><span class="suggestion"><g:message code="suggestion.equipment.name"/></span></label>
                    <input type="text" name="name" class="large unique" value="${equipment?.name}" validation="required rangelength[2, 100]" unique-action="isEquipmentUnique" maxlength="100">
                </div><div class="form-row">
                    <label><g:message code="auto.accept"/> <g:message code="check.box"/></label>
                    <input type="checkbox" class="single" name="autoAccept" value="true" uncheck-value="false" ${equipment.autoAccept ? "checked" : ""}>
                </div>
            </div>
            <div class="double-input-row">
                <g:if test="${equipmentTypes.size()}"><div class="form-row">
                        <label><g:message code="equipment.types"/><span class="suggestion"><g:message code="suggestion.equipment.type"/></span></label>
                        <ui:domainSelect class="large equipment-type-selector" name="type" domain="${EquipmentType}" value="${equipment.type?.id}" text="name" validation="required"/>
                    </div></g:if><g:else><div class="info-row">
                        <span class="info-message"><g:message code="no.equipment.type.created"/> <span class="link activate-equipment-type-tab"><g:message code='create.equipment.type.first'/></span></span>
                    </div></g:else><div class="form-row">
                        <label><g:message code="equipment.organiser"/><span class="suggestion"><g:message code="suggestion.equipment.organizer"/></span></label>
                        <ui:domainSelect class="large organiser-selector" name="organiser" domain="${Operator}" text="fullName" value="${equipment.organiser?.id}"/>
                </div>
            </div>

            <div class="form-row">
                <label><g:message code="description"/><span class="suggestion"><g:message code="suggestion.equipment.description"/></span></label>
                <textarea class="large" name="description" validation="maxlength[500]" maxlength="500">${equipment.description}</textarea>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button edit-popup-form-submit"
                    ${equipmentTypes.size() ? '' : 'style="display:none"'}><g:message code="${equipment.id ? "update" : "save"}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>