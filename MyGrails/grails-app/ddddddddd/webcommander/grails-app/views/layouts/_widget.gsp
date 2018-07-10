<g:if test="${!params?.noLayout}">
    <div class="widget ${"widget-" + widget.widgetType} ${widget.clazz}" id="wi-${widget.uuid}" widget-id="${widget.id}" widget-type="${widget.widgetType}">
        <g:if test="${widget.title}">
            <div class="widget-title">${site.message(code: widget.title).encodeAsBMHTML()}</div>
        </g:if>
        <g:layoutBody/>
    </div>
</g:if>
<g:else>
    <g:if test="${widget.title}">
        <div class="widget-title">${site.message(code: widget.title).encodeAsBMHTML()}</div>
    </g:if>
    <g:layoutBody/>
</g:else>