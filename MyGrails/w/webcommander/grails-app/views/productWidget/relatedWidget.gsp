<g:if test="${relatedProducts.size()}">
    <g:applyLayout name="_productwidget">
        <div class="title"><site:message code="${config.title}"/></div>
        <g:include view="widget/productListings.gsp" model="${[productList: relatedProducts, config: config]}"/>
    </g:applyLayout>
</g:if>