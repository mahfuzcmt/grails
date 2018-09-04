<div class="toolbar-share">
    <span class="item-group entity-count title toolbar-left">
        <g:message code="category"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="category-bulk-edit-tab advanced-table table-view">
    <div class="bulk-editor-form" action="${app.relativeBaseUrl()}categoryAdmin/saveAdvancedBulkProperties">
        <div class="body">
            <table class="content advance-bulk-edit-table">
                <colgroup>
                    <g:set var="colWidth" value="${[20, 30]}"/>
                    <col class="name-column" style="width: ${colWidth[0]}%">
                    <col class="tracking-column" style="width: ${colWidth[1]}%">
                    <plugin:hookTag hookPoint="advanceCategoryBulkColgroup" attrs="[:]"/>
                    <col class="restricted-price-column">
                    <col class="rstricted-purchase-column">
                </colgroup>
                <tr>
                    <th><g:message code="product.name"/></th>
                    <th class="disable-tracking-column"><g:message code="tracking"/></th>
                    <plugin:hookTag hookPoint="advanceCategoryBulkHeaderColumn" attrs="[:]"/>
                    <th class="restricted-price-column"><g:message code="restrict.price"/></th>
                    <th class="restricted-purchase-column"><g:message code="restrict.purchase"/></th>
                </tr>
                <tr class="data-row">
                    <td></td>
                    <td class="selectable custom-select disable-tracking hidden-overflow-selectable"></td>
                    <plugin:hookTag hookPoint="advanceCategoryBulkChangeAllColumn" attrs="[:]"/>
                    <td class="selectable custom-select restricted-price hidden-overflow-selectable"></td>
                    <td class="selectable custom-select restricted-purchase hidden-overflow-selectable"></td>
                </tr>
                <g:each in="${categories}" var="category" status="i">
                    <tr class="data-row">
                        <g:set var="id" value="${category.id}"/>
                        <input type="hidden" name="id" value="${id}">
                        <td class="editable name" validation="required rangelength[2, 100]"><input type="hidden" name="${id}.name" value="${category.name}"><span class="value">${category.name.encodeAsBMHTML()}</span></td>
                        <td class="disable-tracking selectable hidden-overflow-selectable">
                            <input type="hidden" name="${id}.disableTracking" value="${category.disableGooglePageTracking}">
                            <span class="value">${category.disableGooglePageTracking}</span>
                            <span class="status ${category.disableGooglePageTracking ? "positive" : "negative"}" title="${category.disableGooglePageTracking ? g.message(code: 'disable') : g.message(code: 'enable')}"></span>
                        </td>

                        <plugin:hookTag hookPoint="advanceCategoryBulkDataColumn" attrs="[category: category, target: 'category']"/>

                        <td class="restricted-price selectable hidden-overflow-selectable">
                            <input type="hidden" name="${id}.restrictPriceFor" value="${category.restrictPriceFor}">
                            <span class="disp-value">${g.message(code: category.restrictPriceFor == 'none' ? 'nobody' : category.restrictPriceFor)}</span>
                            <span class="value">${category.restrictPriceFor}</span>
                            <g:each in="${category.restrictPriceExceptCustomers}" var="customer">
                                <input type="hidden" name="${id}.restrictPriceExceptCustomer" value="${customer.id}">
                            </g:each>
                            <g:each in="${category.restrictPriceExceptCustomerGroups}" var="customerGroup">
                                <input type="hidden" name="${id}.restrictPriceExceptCustomerGroup" value="${customerGroup.id}">
                            </g:each>
                        </td>
                        <td class="restricted-purchase selectable hidden-overflow-selectable">
                            <input type="hidden" name="${id}.restrictPurchaseFor" value="${category.restrictPurchaseFor}">
                            <span class="disp-value">${g.message(code: category.restrictPurchaseFor == 'none' ? 'nobody' : category.restrictPurchaseFor)}</span>
                            <span class="value">${category.restrictPurchaseFor}</span>
                            <g:each in="${category.restrictPurchaseExceptCustomers}" var="customer">
                                <input type="hidden" name="${id}.restrictPurchaseExceptCustomer" value="${customer.id}">
                            </g:each>
                            <g:each in="${category.restrictPurchaseExceptCustomerGroups}" var="customerGroup">
                                <input type="hidden" name="${id}.restrictPurchaseExceptCustomerGroup" value="${customerGroup.id}">
                            </g:each>
                        </td>
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
