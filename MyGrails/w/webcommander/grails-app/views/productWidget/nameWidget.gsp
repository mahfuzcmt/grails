<g:applyLayout name="_productwidget">
    <h1 class="product-name">
        ${productData.heading ? productData.heading.encodeAsBMHTML() : productData.name.encodeAsBMHTML()}
    </h1>
</g:applyLayout>