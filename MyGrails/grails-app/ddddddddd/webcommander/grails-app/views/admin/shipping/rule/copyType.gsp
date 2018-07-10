<div class="copy-rule-panel">
    <div class="edit-popup-form">
        <span class="title"><g:message code="how.do.you.want.to.apply.the.rules"/></span>
        <div class="copy-rule-option">
            <g:if test="${params.view == "default"}">
                <div class="copy-type selected" type="use">
                    <span class="label"><g:message code="use.rule"/></span>
                    <div class="description"><g:message code="use.rule.description"/></div>
                </div>
            </g:if>

            <div class="copy-type ${params.view == "default" ? '' : 'selected'}" type="copy">
                <span class="label"><g:message code="copy.rule"/></span>
                <div class="description"><g:message code="copy.rule.description"/></div>
            </div>
        </div>

        <div class="button-line">
            <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="create"/></button>
            <button type="button" class="cancel-button"><g:message code="cancel"/></button>
        </div>
    </div>
</div>