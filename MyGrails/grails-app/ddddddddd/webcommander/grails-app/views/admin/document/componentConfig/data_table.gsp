<%@ page import="com.webcommander.constants.NamedConstants" %>
<div class="component-config data_table">
    <div class="title-bar">
        <span class="title"><g:message code="data.table"/></span>
        <span class="close-config"><g:message code="close"/></span>
    </div>
    <div class="config-body">
        <div class="body-title"><g:message code="manage.style"/></div>
        <div class="form-row inline-element">
            <label><g:message code="arrange"/></label>
            <div class="multi-button">
                <span class="button send-back" data-action="send-back" title="<g:message code="send.back"/>"><<</span>
                <span class="button back" data-action="back" title="<g:message code="back"/>"><</span>
                <span class="button front" data-action="front" title="<g:message code="front"/>">></span>
                <span class="button bring-front" data-action="bring-front" title="<g:message code="bring.front"/>">>></span>
            </div>
        </div>
        <div class="form-row inline-element">
            <label><g:message code="rows"/></label>
            <input type="text" name="rows" restrict="decimal" validation="required gte[2] maxlength[5]" value="${params.rows ?: '3'}">
        </div>
        <div class="form-row inline-element">
            <label><g:message code="column"/></label>
            <input type="text" name="columns" restrict="decimal" validation="required gte[2] maxlength[5]" value="${params.columns ?: '3'}">
        </div>
        <div class="form-row inline-element">
            <label><g:message code="row.border"/></label>
            <span class="unit"><g:message code="px"/></span>
            <input type="text" name="rowBorder" value="${params.rowBorder ?: 0}">
        </div>
        <div class="form-row inline-element">
            <label><g:message code="column.border"/></label>
            <span class="unit"><g:message code="px"/></span>
            <input type="text" name="columnBorder" value="${params.columnBorder ?: 0}">
        </div>
        <div class="form-row inline-element">
            <label><g:message code="border.style"/></label>
            <g:select name="borderStyle" from="${NamedConstants.CSS_BORDER_STYLE.collect {g.message(code: it)}}" keys="${NamedConstants.CSS_BORDER_STYLE}" value="${params.borderStyle ?: 'solid'}"/>
        </div>
        <div class="form-row inline-element">
            <label><g:message code="border.color"/></label>
            <span class="value">${params.borderColor ?: '#000000'}</span>
            <input type="text" class="color-picker" name="borderColor" value="${params.borderColor ?: '#000000'}">
        </div>
        <div class="form-row inline-element">
            <label><g:message code="header.background"/></label>
            <span class="value">${params.headerBackground ?: '#FFFFFF'}</span>
            <input type="text" class="color-picker" name="headerBackground" value="${params.headerBackground ?: '#FFFFFF'}">
        </div>
        <div class="form-row inline-element">
            <label><g:message code="row.background"/></label>
            <span class="value">${params.rowBackground ?: '#FFFFFF'}</span>
            <input type="text" class="color-picker" name="rowBackground" value="${params.rowBackground ?: '#FFFFFF'}">
        </div>
        <g:if test="${params.cellEditing == "true"}">
            <div class="cell-config ">
                <div class="form-row inline-element cell">
                    <label><g:message code="cell.font.size"/></label>
                    <g:select name="cellFontSize" from="${(5..30).collect {it + " px"}}" keys="${(5..30)}" value="${params.cellFontSize ?: 14}"/>
                </div>
                <div class="form-row inline-element cell">
                    <label><g:message code="cell.text.align"/></label>
                    <g:set var="align" value="${['left', 'center', 'right']}"/>
                    <g:select name="cellTextAlign" from="${align.collect {g.message(code: it)}}" keys="${align}" value="${params.cellTextAlign}"/>
                </div>
                <div class="form-row inline-element cell" style="display: none;">
                    <label><g:message code="cell.font.family"/></label>
                    <g:select name="cellFontFamily" from="${NamedConstants.DOCUMENT_FONT_FAMILIES.values()}" keys="${NamedConstants.DOCUMENT_FONT_FAMILIES.keySet()}"/>
                </div>
                <div class="form-row inline-element cell">
                    <label><g:message code="cell.font.color"/></label>
                    <span class="value">${params.cellFontColor ?: '#000000'}</span>
                    <input type="text" class="color-picker" name="cellFontColor" value="${params.cellFontColor ?: '#000000'}">
                </div>
                <div class="form-row inline-element cell">
                    <label><g:message code="cell.background.color"/></label>
                    <span class="value">${params.cellBackground ?: 'transparent'}</span>
                    <input type="text" class="color-picker" name="cellBackground" value="${params.cellBackground ?: ''}">
                </div>
            </div>
        </g:if>

        <input type="hidden" name="tableHeader">
        <input type="hidden" name="tableRow">
    </div>
</div>
