<form action="${app.relativeBaseUrl()}categoryAdmin/saveAdvanced" method="post" class="create-edit-form">
    <input type="hidden" name="id" value="${category.id}" >
    <plugin:hookTag hookPoint="categoryAdvancedEditTab" attrs="${[categoryId:category.id]}">
        <div class="form-section">
            <div class="form-section-info">
                <h3><g:message code="webtool"/></h3>
                <div class="info-content"><g:message code="section.text.category.webtool"/> </div>
            </div>
            <div class="form-section-container">
                <div class="form-row">
                    <label><g:message code="disable.tracking"/></label>
                    <input type="checkbox" class="single" name="disableTracking" value="true" uncheck-value="false" ${category.disableGooglePageTracking ? "checked=checked" : ""}/>
                </div>
            </div>
        </div>
    </plugin:hookTag>
    <div class="form-section">
        <div class="form-section-container">
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>