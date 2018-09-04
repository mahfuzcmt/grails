<%@ page import="grails.converters.JSON; com.webcommander.webcommerce.Category" %>
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
<div class="product-bulk-edit-tab basic-table table-view">
    <div class="bulk-editor-form" action="${app.relativeBaseUrl()}productAdmin/saveBasicBulkProperties">
        <div class="body">
            <table class="content">
                <input type="hidden" name="brand" value='${brand}'>
                <input type="hidden" name="manufacturer" value='${manufacturer}'>
                <span id="parents" style="display: none">${parents}</span>
                <colgroup>
                    <col class="select-column">
                    <col class="heading-column">
                    <col class="url-column">
                    <col class="parent-column">
                    <col class="status-column">
                    <col class="administrative-status-column">
                    <col class="price-column">
                </colgroup>
                <tr>
                    <th><g:message code="product.name"/></th>
                    <th><g:message code="heading"/></th>
                    <th><g:message code="url.identifier"/></th>
                    <th><g:message code="parent.category"/></th>
                    <th class="status-column"><g:message code="availability"/></th>
                    <th class="administrative-status-column"><g:message code="administrative.status"/></th>
                    <th><g:message code="base.price"/></th>
                </tr>
                <tr class="data-row">
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td class="selectable custom-select is-available hidden-overflow-selectable"></td>
                    <td class="selectable custom-select is-active hidden-overflow-selectable"></td>
                    <td class="editable custom-edit base-price" restrict="decimal" validation="number"></td>
                </tr>
                <g:each in="${products}" var="product" status="i">
                    <tr class="data-row">
                        <g:set var="id" value="${product.id}"/>
                        <input type="hidden" name="id" value="${id}">
                        <td class="editable name" validation="required rangelength[2, 100]"><input type="hidden" name="${id}.name" value="${product.name}"><span class="value">${product.name.encodeAsBMHTML()}</span></td>
                        <td class="editable heading"><input type="hidden" name="${id}.heading" value="${product.heading}"><span class="value">${product.heading.encodeAsBMHTML()}</span></td>
                        <td class="editable url" validation="required maxlength[100] url_folder" unique="url"><input type="hidden" name="${id}.url" value="${product.url}"><span class="value">${product.url.encodeAsBMHTML()}</span></td>
                        <td class="bulk-edit-parent-selector form-row chosen-wrapper">
                            <ui:hierarchicalSelect name="${product.id}.categories" class="parents-selector special-select-chosen" domain="${Category}" custom-attrs="${[multiple: 'true', 'chosen-highlighted': product.parent?.id ?: 0, 'data-placeholder': g.message(code: "select.categories"), 'chosen-hiddenfieldname': "${product.id}.parent"]}" values="${product.parents.id ?: 0}"/>
                        </td>
                        <td class="status-column product-status is-available selectable hidden-overflow-selectable">
                            <input type="hidden" name="${id}.isAvailable" value="${product.isAvailable}">
                            <span class="value">${product.isAvailable}</span>
                            <span class="status ${product.isAvailable ? "positive" : "negative"}" title="${product.isAvailable ? g.message(code: 'available') : g.message(code: 'not.available')}"></span>
                        </td>
                        <td class="status-column product-status is-active selectable hidden-overflow-selectable">
                            <input type="hidden" name="${id}.isActive" value="${product.isActive}">
                            <span class="value">${product.isActive}</span>
                            <span class="status ${product.isActive ? "positive" : "negative"}" title="${product.isActive ? g.message(code: 'active') : g.message(code: 'inactive')}"></span>
                        </td>
                        <td class="editable base-price" restrict="decimal" validation="required number price">
                            <input type="hidden" name="${id}.basePrice" value="${product.basePrice.toAdminPrice()}">
                            <span class="value">${product.basePrice?.toAdminPrice()}</span>
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
