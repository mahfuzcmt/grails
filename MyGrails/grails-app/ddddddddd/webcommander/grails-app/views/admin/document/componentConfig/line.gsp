<%@ page import="com.webcommander.constants.NamedConstants" %>
<div class="component-config line">
    <div class="title-bar">
        <span class="title"><g:message code="line"/></span>
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
            <label><g:message code="height"/></label>
            <span class="unit"><g:message code="px"/></span>
            <input type="text" name="borderTopWidth" value="${params.borderTopWidth ?: 1}">
        </div>
        <div class="form-row inline-element">
            <label><g:message code="line.style"/></label>
            <g:select name="lineStyle" from="${NamedConstants.CSS_BORDER_STYLE.collect {g.message(code: it)}}" keys="${NamedConstants.CSS_BORDER_STYLE}" value="${params.lineStyle ?: 'solid'}"/>
        </div>
        <div class="form-row inline-element">
            <label><g:message code="orientation"/></label>
            <g:select name="orientation" from="${NamedConstants.ORIENTATION.collect {g.message(code: it)}}" keys="${NamedConstants.ORIENTATION}" value="${params.orientation ?: 'horizontal'}"/>
        </div>
        <div class="form-row inline-element">
            <label><g:message code="line.color"/></label>
            <span class="value">${params.lineColor ?: '#000000'}</span>
            <input type="text" class="color-picker" name="lineColor" value="${params.lineColor ?: '#000000'}">
        </div>
    </div>
</div>