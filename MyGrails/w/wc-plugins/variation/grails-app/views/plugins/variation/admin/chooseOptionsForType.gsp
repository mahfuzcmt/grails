<div class="form-row chosen-wrapper">
    <label><g:message code="variation.option.for" args="${[type.name]}"/><span class="suggestion"> <g:message code="select.variation.option"/></span></label>
    <g:select name="${type.id}.variationOption" class="variation-option-select ${type.standard}" id="options-for-type-${type.id}" from="${allOptions}" optionKey="id"
              noSelection="['add-option':'+ Add Option']" optionValue="value" multiple="multiple" data-placeholder="${g.message(code: 'choose.an.option')}" value="${selectedOptions}"/>

    <table class="content variation-option-add-section hidden">
        <tr class="represent-type last-row">
            <input type="hidden" class="type-id" value="${type.id}" data-type-standard="${type.standard}">
            <td class="represent-label">
                <input class="td-full-width option-label-input small" type="text" name="label" maxlength="100" placeholder="<g:message code="enter.label"/>">
            </td>
            <td class="represent-value">

            </td>
            <td class="represent-order">
                <input class="td-full-width option-order-input small" type="text" name="order" maxlength="9" placeholder="<g:message code="enter.order"/>">
            </td>
            <td class="actions-column type-name" type="${type.id}">
                <span class="tool-icon add add-row variation-option-add"></span>
                <span class="cancel remove-variation-option-row"> Cancel </span>
            </td>
        </tr>
    </table>
</div>

