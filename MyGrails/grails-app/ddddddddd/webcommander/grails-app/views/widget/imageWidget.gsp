<g:applyLayout name="_widget">
    <a href="${config.hype_url ?: '#'}" target="${config.link_target}">
        <g:if test="${config.upload_type == 'asset_library'}">
            <img alt="${config.alt_text}" src="${app.baseUrl() + widget.content}">
        </g:if>
        <g:else>
            <img alt="${config.alt_text}" src="${widget.content}">
        </g:else>
    </a>
</g:applyLayout>