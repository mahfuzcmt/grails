<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="button.text"/></div>
        <div class="sidebar-group-body">
            <input type="text" class="sidebar-input" name="buttonText" value="${config.buttonText ?: "s:subscribe"}">
        </div>
    </div>
    <div class="slider-group">
        <div class="sidebar-group-body">
            <input type="checkbox" name="inplace" value="true" uncheck-value="false" ${(config.inplace == "true" || !config.inplace) ? "checked" : ""} toggle-target="inplace" class="sidebar-input single">
            <label><g:message code="inplace.subscription"/></label>
        </div>
    </div>
    <div class="inplace">
        <div class="sidebar-group">
            <div class="sidebar-group-label"><g:message code="email.place.holder"/></div>
            <div class="sidebar-group-body">
                <input type="text" class="sidebar-input" name="placeHolder" value="${config.placeHolder ?: ''}">
            </div>
        </div>
        <div class="sidebar-group">
            <div class="sidebar-group-label"><g:message code="email.label"/></div>
            <div class="sidebar-group-body">
                <input type="text" class="sidebar-input" name="labelText" value="${config.labelText ?: "s:newsletter.signup"}">
            </div>
        </div>
        <div class="sidebar-group">
            <div class="sidebar-group-body">
                <input type="checkbox" class="sidebar-input single" name="hasName" value="true" uncheck-value="false" ${config.hasName == "true" ? "checked" : ""} toggle-target="name-fields">
                <label><g:message code="enable.name.field"/></label>
            </div>
        </div>
        <div class="name-fields">
            <div class="sidebar-group">
                <div class="sidebar-group-label"><g:message code="name.place.holder"/></div>
                <div class="sidebar-group-body">
                    <input type="text" class="sidebar-input" name="namePlaceHolder" value="${config.namePlaceHolder ?: ''}" >
                </div>
            </div>
            <div class="sidebar-group">
                <div class="sidebar-group-label"><g:message code="name.label"/></div>
                <div class="sidebar-group-body">
                    <input type="text" class="sidebar-input" name="nameLabelText" value="${config.nameLabelText ?: "s:name"}">
                </div>
            </div>
        </div>
    </div>
</g:applyLayout>