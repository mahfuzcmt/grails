<g:applyLayout name="_widget">
    <span class="breadcrumb-item root"><a href="${homeItem.url}"><span class="icon"></span><span class="separator">&raquo;</span></a></span>
    <g:each in="${breadcrumbItems}" var="item">
        <span class="breadcrumb-item"><a href="${item.url}"><span class="label">${item.title}</span><span class="separator">&raquo;</span></a></span>
    </g:each>
    <span class="breadcrumb-item current">${currentItem}</span>
</g:applyLayout>