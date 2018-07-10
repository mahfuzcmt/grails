<%@ page import="com.webcommander.constants.NamedConstants" %>
<div class="component-config area">
    <div class="title-bar">
        <span class="title"><g:message code="area"/></span>
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
            <label><g:message code="width"/></label>
            <span class="unit"><g:message code="px"/></span>
            <input type="text" name="width" value="${params.width ?: 640}">
        </div>
        <div class="form-row inline-element">
            <label><g:message code="height"/></label>
            <span class="unit"><g:message code="px"/></span>
            <input type="text" name="height" value="${params.height ?: 150}">
        </div>
        <div class="form-row inline-element">
            <label><g:message code="border.size"/></label>
            <span class="unit"><g:message code="px"/></span>
            <input type="text" name="borderSize" value="${params.borderSize ?: 0}">
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
            <label><g:message code="background.color"/></label>
            <span class="value">${params.backgroundColor ?: '#eeeeee'}</span>
            <input type="text" class="color-picker" name="backgroundColor" value="${params.backgroundColor ?: '#eeeeee'}">
        </div>
    </div>
</div>