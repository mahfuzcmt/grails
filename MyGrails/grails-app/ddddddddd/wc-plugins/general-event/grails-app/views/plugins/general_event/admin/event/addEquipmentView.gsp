<%@ page import="com.webcommander.plugin.general_event.Equipment" %>
<div class="form-section">
    <div class="form-section-info-block">
        <h3><g:message code="equipment"/></h3>
        <div class="info-content"><g:message code="form.section.text.add.equipment.info"/></div>
    </div>
    <div class="form-section-container-block equipment-table-container with-top-btn">
        <div class="btn-panel add-item">
            <button class="submit-button add-equipment-btn" type="button">+&nbsp;<g:message code="add.equipment"/></button>
        </div>
        <div class="equipment-item-container">
            <g:if test="${event?.equipment}">
                <div class="row old-row">
                    <span class="name">${event.equipment.name.encodeAsBMHTML()}</span>
                    <div class="column actions-column">
                        <span class="action-navigator collapsed" ></span>
                    </div>
                </div>
            </g:if>
        </div>
    </div>
</div>