<form action="${app.relativeBaseUrl()}zone/save" method="post" class="create-edit-form edit-popup-form">
    <g:if test="${zone}">
        <input type="hidden" name="id" value='${zone.id}'/>
    </g:if>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="zone.create.info"/></h3>
            <div class="info-content"><g:message code="section.text.create.zone"/></div>
        </div>
        <div class="form-section-container">
            <g:include view="/admin/zone/create.gsp" model="${[states: states ?: [], zone: zone, size: params.fieldClass ?: "large"]}"/>
            <div class="form-row">
                <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="${zone ? 'update' : 'create'}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>
