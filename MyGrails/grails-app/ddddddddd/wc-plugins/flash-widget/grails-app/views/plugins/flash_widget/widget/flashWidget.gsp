<g:applyLayout name="_widget">
    <object data="${widget.content}" type="application/x-shockwave-flash" width="${config.width}" height="${config.height}" align="middle">
        <g:each in="${config.paramName}" status="i" var="param">
            <param name="${param}" value="${config.paramValue[i]}">
        </g:each>
    </object>
</g:applyLayout>