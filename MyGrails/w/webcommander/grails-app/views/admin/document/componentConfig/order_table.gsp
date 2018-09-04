<%@ page import="com.webcommander.constants.NamedConstants" %>
<div class="component-config order_table">
    <div class="title-bar">
        <span class="title"><g:message code="order.table"/></span>
        <span class="close-config"><g:message code="close"/></span>
    </div>
    <div class="config-body">
        <div class="form-row inline-element">
            <label><g:message code="arrange"/></label>
            <div class="multi-button">
                <span class="button send-back" data-action="send-back" title="<g:message code="send.back"/>"><<</span>
                <span class="button back" data-action="back" title="<g:message code="back"/>"><</span>
                <span class="button front" data-action="front" title="<g:message code="front"/>">></span>
                <span class="button bring-front" data-action="bring-front" title="<g:message code="bring.front"/>">>></span>
            </div>
        </div>
        <table>
            <thead>
            <tr>
                <th class="name"><g:message code="column.name"/></th>
                <th class="value"><g:message code="column.value"/></th>
                <th class="action"></th>
            </tr>
            </thead>
            <tbody>
            <g:set var="componentConfig" value="${config ?: NamedConstants.ORDER_TABLE_COLUMN}"/>
            <g:set var="constantColumn" value="${["Name", "Unit Price"]}"/>
            <g:each in="${componentConfig}" var="header" status="i">
                <tr class="order-column">
                    <td class="name">
                        <span class="${constantColumn.contains(header.key) ? '' : 'inline-editable'}" data-editable-validation="required maxlength[100]">${header.key}</span>
                        <input class="column-name" type="hidden" name="column-${header.key}" value="${params['column-'+header.key] ?: header.key}">
                    </td>
                    <td class="value">
                        <g:select name="value-${header.key}" disabled="${constantColumn.contains(header.key)}" from="${NamedConstants.ORDER_TABLE_COLUMN.collect {it.key}}" keys="${NamedConstants.ORDER_TABLE_COLUMN.collect {it.value}}" value="${params['value-'+header.key] ?: header.value}"/>
                        <g:if test="${constantColumn.contains(header.key)}"><input type="hidden" name="value-${header.key}" value="${(params['value-'+header.key] ?: header.value).encodeAsBMHTML()}"></g:if>
                    </td>
                    <td class="action"><g:if test="${!constantColumn.contains(header.key)}"><span class="remove remove-column"></span></g:if></td>
                </tr>
            </g:each>
            </tbody>
        </table>
        <div class="add-column link-btn">+ <g:message code="add.column"/></div>
        <div class="manage-style">
            <div class="body-title"><g:message code="manage.style"/></div>
            <div class="form-row inline-element">
                <label><g:message code="row.border"/></label>
                <span class="unit"><g:message code="px"/></span>
                <input type="text" name="rowBorder" value="${params.rowBorder ?: '1'}">
            </div>
            <div class="form-row inline-element">
                <label><g:message code="column.border"/></label>
                <span class="unit"><g:message code="px"/></span>
                <input type="text" name="columnBorder" value="${params.columnBorder ?: '1'}">
            </div>
            <div class="form-row inline-element">
                <label><g:message code="border.style"/></label>
                <g:select name="borderStyle" from="${NamedConstants.CSS_BORDER_STYLE.collect {g.message(code: it)}}" keys="${NamedConstants.CSS_BORDER_STYLE}" value="${params.borderStyle ?: 'solid'}"/>
            </div>
            <div class="form-row inline-element">
                <label><g:message code="border.color"/></label>
                <span class="value">${params.borderColor ?: '#e6e6e6'}</span>
                <input type="text" class="color-picker" name="borderColor" value="${params.borderColor ?: '#e6e6e6'}">
            </div>
            <div class="form-row inline-element">
                <label><g:message code="header.background"/></label>
                <span class="value">${params.headerBackground ?: '#3997D6'}</span>
                <input type="text" class="color-picker" name="headerBackground" value="${params.headerBackground ?: '#3997D6'}">
            </div>
            <div class="form-row inline-element">
                <label><g:message code="row.background"/></label>
                <span class="value">${params.rowBackground ?: '#fafbfc'}</span>
                <input type="text" class="color-picker" name="rowBackground" value="${params.rowBackground ?: '#fafbfc'}">
            </div>
        </div>
    </div>
</div>