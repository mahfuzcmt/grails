<h4 class="group-label"><g:message code="products"/></h4>
<div class="view-content-block">
    <g:each in="${products}" var="product" status="i">
        <div class="view-list-row">
            ${product.name.encodeAsBMHTML()} ${product.isInTrash ? "(" + g.message(code: "in.trash") + ")" : ""}
        </div>
    </g:each>
</div>