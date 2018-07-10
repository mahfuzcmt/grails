<div class="left-panel">
    <div class="body album-list blocktype-list">
        <g:each in="${albumList}" var="album">
            <div class="album-thumb blocklist-item ${album.id == selected ? "selected" : "" }" album-id="${album.id}" album-name="${album.name}">
                <span class="float-menu-navigator"></span>
                <span class="album-title listitem-title">${album.name.encodeAsBMHTML()}</span>
                <span class="album-count blocklist-subitem-summary-view">${album.imageCount()}&nbsp;<g:message code="images"/></span>
            </div>
        </g:each>
    </div>
</div>