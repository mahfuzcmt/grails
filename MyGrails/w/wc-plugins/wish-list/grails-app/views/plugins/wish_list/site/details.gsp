<div class="wish-list-details">
    <g:if test="${totalCount == 0}">
        <span><g:message code="no.product.added.in.wish.list"/> </span>
    </g:if>
    <g:else>
        <g:include view="widget/productListings.gsp" model="[productList: productList, config: config, totalCount: totalCount, offset: offset, max: max, url_prefix: 'prwd']"/>
    </g:else>
</div>