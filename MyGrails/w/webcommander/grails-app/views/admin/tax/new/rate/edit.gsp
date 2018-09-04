<tr class="new-code-row">
    <td colspan="4">
        <form action="${app.relativeBaseUrl()}taxAdmin/saveTaxCode" method="post" class="create-edit-form">
            <input type="hidden" name="id" value="${code.id}">
            <input type="hidden" name="ruleId" value="${params.'tax-rule-id'}">
            <div class="triple-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="name"/><span class="suggestion">e.g. Tax Code 1</span></label>
                    <input type="text" class="medium unique" name="name" value="${code.name.encodeAsBMHTML()}" validation="required"
                           unique-action="isCodeUnique" maxlength="100" validation="maxlength[100]">
                </div><div class="form-row">
                    <label><g:message code="label"/><span class="suggestion">e,g, Tax Code 1</span></label>
                    <input type="text" maxlength="100" validation="maxlength[100]" class="medium" name="label"
                           value="${code.label.encodeAsBMHTML()}">
                </div><div class="form-row">
                    <label><g:message code="rate"/><span class="note">(%)</span> <span class="suggestion">e.g. 5</span></label>
                    <input type="text" class="medium" name="rate" value="${BigDecimal.valueOf(code.rate)}" restrict="decimal"
                           validation="required number maxlength[9]" maxlength="9">
                </div>
            </div>

            <div class="form-row btn-row">
                <button type="submit" class="submit-button"><g:message code="${code.id ? "update" : "save"}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </form>
    </td>
</tr>
