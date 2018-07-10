<g:form controller="shippingRuleImport" action="initImport" method="post" class="edit-popup-form">
    <div class="double-input-row">
        <div class="form-row">
            <label class="large"><g:message code="rate.work.sheet"/></label>
            <select name="rateWorkSheet" class="rate-work-sheet large" entity-component="rate">
                <option value=""><g:message code="not.import"/></option>
                <g:each in="${sheetNames}" var="sheet">
                    <option value="${sheet}" ${sheet == rateSheet ? "selected='selected'" : ""}>${sheet}</option>
                </g:each>
            </select>
        </div><div class="form-row">
                <label class="large"><g:message code="overwrite.existing.rate"/></label>
                <select name="rateOverwrite" class="large required">
                    <option value="1">Yes</option>
                    <option value="0">No</option>
                </select>
            </div>
    </div>

    <div class="double-input-row">
        <div class="form-row">
            <label class="large"><g:message code="rule.work.sheet"/></label>
            <select name="ruleWorkSheet" class="rule-work-sheet large" entity-component="rule">
                <option value=""><g:message code="not.import"/></option>
                <g:each in="${sheetNames}" var="sheet">
                    <option value="${sheet}" ${sheet == ruleSheet ? "selected='selected'" : ""}>${sheet}</option>
                </g:each>
            </select>
        </div><div class="form-row">
                <label class="large"><g:message code="overwrite.existing.rule"/></label>
                <select name="ruleOverwrite" class="large required">
                    <option value="1">Yes</option>
                    <option value="0">No</option>
                </select>
            </div>
    </div>

    <div class="double-input-row">
        <div class="form-row">
            <label class="large"><g:message code="zone.work.sheet"/></label>
            <select name="zoneWorkSheet" class="zone-work-sheet large" entity-component="zone">
                <option value=""><g:message code="not.import"/></option>
                <g:each in="${sheetNames}" var="sheet">
                    <option value="${sheet}" ${sheet == zoneSheet ? "selected='selected'" : ""}>${sheet}</option>
                </g:each>
            </select>
        </div><div class="form-row">
                <label class="large"><g:message code="overwrite.existing.zone"/></label>
                <select name="zoneOverwrite" class="large required">
                    <option value="1">Yes</option>
                    <option value="0">No</option>
                </select>
            </div>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="init.import"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</g:form>