<g:applyLayout name="_productwidget">
    <plugin:hookTag hookPoint="productCartBlockForEditor">
        <div class="stepper ">
            <input type="text" class="text-type stepper-input" value="1" spin-min="1" spin-max="2" spin-step="1">
            <span class="stepper-arrow up et_cartp_increase_quantity" et-category="button">Up</span>
            <span class="stepper-arrow down et_cartp_decrease_quantity" et-category="button">Down</span>
        </div>
        <span class="add-to-cart-button button" product-id="" cart-quantity=""><g:message code="add.to.cart"/></span>
    </plugin:hookTag>
</g:applyLayout>