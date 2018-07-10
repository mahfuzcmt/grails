<div class="toolbar-share">
    <span class="item-group entity-count title toolbar-left">
        <g:message code="product"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="product-bulk-edit-tab advanced-table table-view">
    <div class="bulk-editor-form" action="${app.relativeBaseUrl()}productAdmin/saveAdvancedBulkProperties">
        <div class="body">
            <table class="content advance-bulk-edit-table">
                <colgroup>
                    <g:set var="colWidth" value="${[20, 30]}"/>
                    <col class="name-column" style="width: ${colWidth[0]}%">
                    <col class="heading-column" style="width: ${colWidth[1]}%">
                    <col class="condition-column">
                    <plugin:hookTag hookPoint="advanceProductBulkColgroup" attrs="[:]"/>
                </colgroup>
                <tr>
                    <th><g:message code="product.name"/></th>
                    <th><g:message code="heading"/></th>
                    <th class="condition-column"><g:message code="condition"/></th>
                    <plugin:hookTag hookPoint="advanceProductBulkHeaderColumn" attrs="[:]"/>
                </tr>
                <tr class="data-row">
                    <td></td>
                    <td></td>
                    <td class="selectable custom-select product-condition hidden-overflow-selectable"></td>
                    <plugin:hookTag hookPoint="advanceProductBulkChangeAllColumn" attrs="[:]"/>
                </tr>
                <g:each in="${products}" var="product" status="i">
                    <tr class="data-row">
                        <g:set var="id" value="${product.id}"/>
                        <input type="hidden" name="id" value="${id}">
                        <td class="editable name" validation="required rangelength[2, 100]"><input type="hidden" name="${id}.name" value="${product.name}"><span class="value">${product.name.encodeAsBMHTML()}</span></td>
                        <td class="editable heading"><input type="hidden" name="${id}.heading" value="${product.heading}"><span class="value">${product.heading.encodeAsBMHTML()}</span></td>
                        <td class="product-condition selectable hidden-overflow-selectable">
                            <input type="hidden" name="${id}.condition" value="${product.productCondition}">
                            <span class="disp-value">${g.message(code: product.productCondition)}</span>
                            <span class="value">${product.productCondition}</span>
                        </td>
                        <plugin:hookTag hookPoint="advanceProductBulkDataColumn" attrs="[product: product, target: 'product']"/>
                    </tr>
                </g:each>
            </table>
        </div>
        <div class="form-row">
            <label>&nbsp;</label>
            <button type="button" class="submit-button"><g:message code="update"/></button>
        </div>
    </div>
</div>
