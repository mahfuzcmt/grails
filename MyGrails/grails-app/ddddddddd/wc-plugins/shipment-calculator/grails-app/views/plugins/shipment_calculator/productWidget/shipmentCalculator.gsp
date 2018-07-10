<g:applyLayout name="_productwidget">
    <app:enqueueSiteJs src="plugins/shipment-calculator/js/shipment-calculator.js" scriptId="shipment-calculator"/>
    <span class='shipment-calculator button et_pdp_shipping_calculator' et-category="button" page='product' productId='${product.id}'>${g.message(code: "shipment.calculator")}</span>
</g:applyLayout>