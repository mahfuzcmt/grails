<div class="layout-list">
    <g:each in="${layouts}" var="layout">
        <div class="layout-thumb" layout-id="${layout.id}" title="${layout.name.encodeAsBMHTML()}">
            ${layout.name.encodeAsBMHTML()}
        </div>
    </g:each>
</div>