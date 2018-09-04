<g:if test="${product.isCombined}">
    <g:applyLayout name="_productwidget">
        <g:include view="site/product/includedProducts.gsp" model="${pageScope.variables}"/>
    </g:applyLayout>
</g:if>