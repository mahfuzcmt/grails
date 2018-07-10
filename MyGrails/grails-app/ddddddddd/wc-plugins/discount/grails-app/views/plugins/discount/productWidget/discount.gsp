<g:applyLayout name="_productwidget">
    <div class="discount" page="product">
        <plugin:hookTag hookPoint="cartNotificationMessage" attrs="${[data: selectedDiscountData, isShowDiscountedMessage: true]}"/>
    </div>
</g:applyLayout>