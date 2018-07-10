<form action="${app.relativeBaseUrl()}zone/save" method="post" class="create-edit-form edit-popup-form">
    <g:if test="${zone}">
        <input type="hidden" name="id" value='${zone.id}'/>
    </g:if>
    <g:include view="/admin/zone/create.gsp" model="${[states: states ?: [], zone: zone, size: params.fieldClass ?: "large"]}"/>
    <div class="form-row btn-row">
        <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="${zone ? 'update' : 'create'}"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>