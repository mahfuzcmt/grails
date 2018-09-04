<g:applyLayout name="_widget">
    <g:if test="${config.inplace == "true"}">
        <div class="newsletter inplace valid-verify-form">
            <g:if test="${config.hasName == "true"}">
                <div class="form-row mandatory">
                    <label><site:message code="${config.nameLabelText ?: 's:name'}"/>:</label>
                    <input type="text" class="medium subscription-name" name="name" validation="required maxlength[250]" placeholder="<site:message code="${config.namePlaceHolder ?: ''}"/>"
                           maxlength="250">
                </div>
            </g:if>
            <div class="form-row mandatory">
                <label><site:message code="${config.labelText ?: 's:email'}"/>:</label>
                <input type="text" class="medium subscription-email" name="email" validation="required single_email maxlength[250]"
                       placeholder="<site:message code="${config.placeHolder ?: ''}"/>" maxlength="250">
            </div>
            <div class="form-row button-container">
                <label>&nbsp</label>
                <button class="newsletter-subscription submit-button"><site:message code="${config.buttonText}"/></button>
            </div>
        </div>
    </g:if>
    <g:else>
        <button class="submit-button page-button"><site:message code="${config.buttonText?: 's:subscribe'}"/></button>
    </g:else>
</g:applyLayout>