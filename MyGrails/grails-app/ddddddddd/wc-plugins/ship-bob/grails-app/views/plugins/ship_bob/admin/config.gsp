<form class="create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post">
    <input type="hidden" name="type" value="ship_bob">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="ship.bob"/></h3>
            <div class="info-content"><g:message code="section.text.ship.bob.setting.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="enable"/></label>
                <input type="checkbox" class="single" name="ship_bob.is_enabled" value="true" ${configs.is_enabled == "true" ? "checked" : ""} toggle-target="ship-bob-config" />
            </div>
            <div class="form-row ship-bob-config">
                <label><g:message code="api.key"/></label>
                <input type="text" name="ship_bob.api_key" value="${configs.api_key}" validation="required@if{self::visible}"/>
            </div>
            <div class="form-row ship-bob-config">
                <label><g:message code="shipping.option"/></label>
                <g:select name="ship_bob.shipping_option" keys="${["1", "2", "3"]}" from="${["Overnight (FedEx & UPS overnight)", "Expedited (FedEx 2 day, UPS 2nd Day Air, USPS Express)", "Standard (USPS First Class & Priority, FedEx Ground, UPS Ground) "]}" value="${configs.shipping_option}"/>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>