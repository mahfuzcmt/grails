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
<div class="product-bulk-edit-tab webtool-table table-view">
    <div class="bulk-editor-form" action="${app.relativeBaseUrl()}productAdmin/saveSeoBulkProperties">
        <div class="body">
            <table class="content">
                <colgroup>
                    <col class="name-column">
                    <col class="heading-column">
                    <col class="track-column">
                </colgroup>
                <tr>
                    <th><g:message code="product.name"/></th>
                    <th><g:message code="heading"/></th>
                    <th class="status-column"><g:message code="tracking"/></th>
                </tr>
                <tr class="data-row">
                    <td></td>
                    <td></td>
                    <td class="selectable custom-select disable-tracking hidden-overflow-selectable"></td>
                </tr>
                <g:each in="${products}" var="product" status="i">
                    <tr class="data-row">
                        <g:set var="id" value="${product.id}"/>
                        <input type="hidden" name="id" value="${id}">
                        <td class="editable name" validation="required rangelength[2, 100]"><input type="hidden" name="${id}.name" value="${product.name}"><span class="value">${product.name.encodeAsBMHTML()}</span></td>
                        <td class="editable heading"><input type="hidden" name="${id}.heading" value="${product.heading}"><span class="value">${product.heading.encodeAsBMHTML()}</span></td>
                        <td class="status-column product-status disable-tracking selectable hidden-overflow-selectable">
                            <input type="hidden" name="${id}.disableGooglePageTracking" value="${product.disableGooglePageTracking}">
                            <span class="value">${product.disableGooglePageTracking}</span>
                            <span class="status ${product.disableGooglePageTracking ? "positive" : "negative"}" title="${product.disableGooglePageTracking ? g.message(code: 'enable') : g.message(code: 'disable')}"></span>
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
