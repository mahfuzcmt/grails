<%@ page import="grails.converters.JSON; com.webcommander.webcommerce.Category" %>
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
<div class="category-bulk-edit-tab basic-table table-view">
    <div class="bulk-editor-form" action="${app.relativeBaseUrl()}categoryAdmin/saveBasicBulkProperties">
        <div class="body">
            <table class="content">
                <colgroup>
                    <col class="select-column">
                    %{--<col class="parent-column">--}%
                    <col class="status-column">
                </colgroup>
                <tr>
                    <th><g:message code="product.name"/></th>
                    %{--<th><g:message code="parent.category"/></th>--}%
                    <th class="status-column"><g:message code="availability"/></th>
                </tr>
                <tr class="data-row">
                    <td></td>
                    %{--<td class="selectable custom-select parent-category hidden-overflow-selectable"><span class="value"></span></td>--}%
                    <td class="selectable custom-select is-available hidden-overflow-selectable"></td>
                </tr>
                <g:each in="${categories}" var="category" status="i">
                    <tr class="data-row">
                        <g:set var="id" value="${category.id}"/>
                        <input type="hidden" name="id" value="${id}">
                        <td class="editable name" validation="required rangelength[2, 100]"><input type="hidden" name="${id}.name" value="${category.name}"><span class="value">${category.name.encodeAsBMHTML()}</span></td>
                        %{--<td class="parent-category selectable hidden-overflow-selectable">
                            <input type="hidden" name="${id}.parent" value="${category.parent}">
                            <span class="disp-value">${category.parent}</span>
                            <span class="value">${category.parent}</span>
                        </td>--}%
                        <td class="status-column category-status is-available selectable hidden-overflow-selectable">
                            <input type="hidden" name="${id}.isAvailable" value="${category.isAvailable}">
                            <span class="value">${category.isAvailable}</span>
                            <span class="status ${category.isAvailable ? "positive" : "negative"}" title="${category.isAvailable ? g.message(code: 'available') : g.message(code: 'not.available')}"></span>
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
