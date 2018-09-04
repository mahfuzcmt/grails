<g:each in="${layoutList}" var="layout">
    <div class="layout-thumb blocklist-item ${layout.id == selected ? "selected" : "" }" layout-id="${layout.id}" layout-name="${layout.name}">
        <span class="float-menu-navigator" is-default="${layout.id == defaultLayout}"></span>
        <span class="layout-title listitem-title">${layout.name.encodeAsBMHTML()}</span>
        <g:set var="attached" value="${layout.attachedPageForLayout(4)}"/>
        <g:set var="pages" value="${attached.pages}"/>
        <g:set var="autoPages" value="${attached.autoPages}"/>
        <span class="attach-page blocklist-subitem-summary-view">${(pages ? pages.join(", ") : "") + (autoPages ? (pages ? ", " : "") + autoPages.join(", ") : "")}</span>
    </div>
</g:each>
