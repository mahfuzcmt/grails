<div class="component-config macro">
    <div class="title-bar">
        <span class="title"><g:message code="macro"/></span>
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
        <div class="form-row inline-element">
            <label><g:message code="value"/></label>
            <input type="text" disabled name="value" value="${params.value}">
        </div>
        <div class="form-row inline-element">
            <label><g:message code="font.size"/></label>
            <g:select name="fontSize" from="${(5..30).collect {it + " px"}}" keys="${(5..30)}" value="${params.fontSize ?: 14}"/>
        </div>
        <div class="form-row inline-element">
            <label><g:message code="font.style"/></label>
            <p>
                <span><g:message code="bold"/></span>
                <input type="checkbox" name="bold" value="bold" ${params.bold ? 'checked' : ''}>
                <span><g:message code="italic"/></span>
                <input type="checkbox" name="italic" value="italic" ${params.italic ? 'checked' : ''}>
                <span><g:message code="underline"/></span>
                <input type="checkbox" name="underline" value="underline" ${params.bold ? 'underline' : ''}>
            </p>
        </div>
        <div class="form-row inline-element">
            <label><g:message code="align"/></label>
            <g:set var="align" value="${['left', 'center', 'right']}"/>
            <g:select name="align" from="${align.collect {g.message(code: it)}}" keys="${align}" value="${params.align}"/>
        </div>
        <div class="form-row inline-element">
            <label><g:message code="font.color"/></label>
            <span class="value">${params.color ?: '#000000'}</span>
            <input type="text" class="color-picker" name="color" value="${params.color ?: '#000000'}">
        </div>
        <div class="form-row inline-element">
            <label><g:message code="link"/></label>
            <input type="checkbox" name="link" class="single" value="true" toggle-target="link-text-element" ${params.link ? 'checked' : ''}>
        </div>
        <div class="form-row link-text-element">
            <input type="text" name="linkUrl" value="${params.linkUrl ?: '#'}">
        </div>
    </div>
</div>