<g:applyLayout name="_widget">
    <g:if test="${config.hype_url}">
        <a href="${config.hype_url}" target="${config.link_target}">
            <g:if test="${config.type == "image"}">
                <img alt="${config.alt_text.encodeAsBMHTML()}" src="${logoUrl}" />
            </g:if>
            <g:else>
                <div class="text-logo">${config.text.encodeAsBMHTML()}</div>
            </g:else>
        </a>
    </g:if>
    <g:else>
        <g:if test="${config.type == "image"}">
            <img alt="${config.alt_text.encodeAsBMHTML()}" src="${logoUrl}" />
        </g:if>
        <g:else>
            <div class="text-logo">${config.text.encodeAsBMHTML()}</div>
        </g:else>
    </g:else>
</g:applyLayout>