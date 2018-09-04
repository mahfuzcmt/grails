<g:applyLayout name="_productwidget">
    <g:if test="${session.customer}">
        <loyaltyPoint:productWidget productId="${product.id}" productData="${productData}"/>
    </g:if>
</g:applyLayout>