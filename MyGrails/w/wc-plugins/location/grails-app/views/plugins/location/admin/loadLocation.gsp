<g:applyLayout name="_editWidget">
    <input type="hidden" name="pin_url" value="${config.pin_url}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="location.selection.info"/></h3>
            <div class="info-content"><g:message code="section.text.location.edit.widget"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <input type="file" name="localImage" file-type="image" previewer="map-marker-preview" validation="${config.pin_url ? "" : "drop-file-required"}">
            </div>
        </div>
        <fieldset>
            <legend><g:message code="preview"/> </legend>
            <div class="img-preview">
                <img id="map-marker-preview" src="${config.pin_url ? app.baseUrl() + config.pin_url : ""}" ${config.pin_url ? "" : "style='display: none'"}>
            </div>
        </fieldset>
        <div class="form-row">
            <button type="submit" class="submit-button"><g:message code="update"/></button>
            <button type="button" class="cancel-button"><g:message code="cancel"/></button>
        </div>
    </div>
</g:applyLayout>
