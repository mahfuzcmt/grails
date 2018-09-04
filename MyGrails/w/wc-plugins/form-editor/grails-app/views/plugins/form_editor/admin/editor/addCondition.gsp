<form class="edit-popup-form">
    <table class="form-condition-editor">
        <tr>
            <th></th>
            <th><g:message code="option"/></th>
            <th><g:message code="action"/></th>
            <th><g:message code="on"/></th>
            <th></th>
        </tr>
        <g:each in="${activeField.conditions}" var="condition">
            <tr>
                <td><g:message code="if.field.value"/><span>=</span></td>
                <td>
                    <span class="text">${condition.value.targetOption}</span>
                    <input type="hidden" class="targetOption" name="targetOption" value="${condition.value.targetOption}">
                </td>
                <td>
                    <span class="text"><g:message code="${condition.value.action}"/> </span>
                    <input type="hidden" class="action" name="action" value="${condition.value.action}">
                </td>
                <td>
                    <span class="text">${fields.find { it.uuid == condition.value.dependentFieldUUID}?.label}</span>
                    <input type="hidden" class="dependentFieldUUID" name="dependentFieldUUID" value="${condition.value.dependentFieldUUID}">
                </td>
                <td>
                    <span class="tool-icon edit"></span>
                    <span class="tool-icon remove"></span>
                </td>
            </tr>
        </g:each>
        <tr class="last-row">
            <td><g:message code="if.field.value"/><span>=</span></td>
            <td><g:select name="targetOption" class="option-selector" from="${activeField.options}" /></td>
            <td><g:select name="action" class="action-selector" from="${[g.message(code: "show"), g.message(code:  "hide")]}" keys="${["show", "hide"]}" /></td>
            <td><g:select name="dependentFieldUUID" class="dependent-selector" from="${fields.label}" keys="${fields.uuid}"/> </td>
            <td><button type="button" class="add-update"><g:message code="add"/> </button></td>
        </tr>
        <tr class="template" style="display: none">
            <td><g:message code="if.field.value"/> <span>=</span></td>
            <td>
                <span class="text"></span>
                <input type="hidden" class="targetOption" name="targetOption">
            </td>
            <td>
                <span class="text"></span>
                <input type="hidden" class="action" name="action">
            </td>
            <td>
                <span class="text"></span>
                <input type="hidden" class="dependentFieldUUID" name="dependentFieldUUID">
            </td>
            <td>
                <span class="tool-icon edit"></span>
                <span class="tool-icon remove"></span>
            </td>
        </tr>
    </table>
    <div class="button-line">
        <button type="submit" class="edit-popup-form-submit submit-button"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>