<div style="text-align: center; margin-top: 100px;"><h3 style="display: inline-block; padding: 30px 60px; font-size: 24px;"><g:message code="you.redirected.shortly.payment.provider"/></h3></div>
<form action="${requestMap.ServiceUrl}" id="payment-method-form" method="post" target="_self" >
    <input type="hidden" name="cmd" value="_xclick">
    <input type="hidden" name="upload" value="1">
    <input type="hidden" name="no_note" value="1">
    <input type="hidden" name="bn" value="BuyNow">
    <input type="hidden" name="amount" value="${requestMap.Amount}">
    <input type="hidden" name="item_name" value="Order# ${requestMap.MerchantInvoice}">
    <input type="hidden" name="item_number" value="${requestMap.itemNumber}">
    <input type="hidden" name="currency_code" value="${requestMap.Currency}">
    <input type="hidden" name="no_shipping" value="1">
    <input type="hidden" name="return" value="${requestMap.ReturnUrl}">
    <input type="hidden" name="notify_url" value="${requestMap.NotifyUrl}">
    <input type="hidden" name="cancel_return" value="${requestMap.cancelUrl}">
    <input type="hidden" name="address1" value="${requestMap.CustomerAddress1}">
    <input type="hidden" name="address2" value="${requestMap.CustomerAddress2}">
    <input type="hidden" name="city" value="${requestMap.CustomerCity}">
    <input type="hidden" name="country" value="${requestMap.CustomerCountry}">
    <input type="hidden" name="email" value="${requestMap.CustomerEmail}">
    <input type="hidden" name="first_name" value="${requestMap.CustomerFirstName}">
    <input type="hidden" name="last_name" value="${requestMap.CustomerLastName}">
    <input type="hidden" name="zip" value="${requestMap.CustomerPostCode}">
    <input type="hidden" name="business" value="${requestMap.Email}">
    <input type="hidden" name="invoice" value="${requestMap.MerchantInvoice}">
    <input type="hidden" name="rm" value="2">
    <input type="hidden" name="custom" value="${requestMap.MerchantReference}">
</form>
<script type="text/javascript">
    var $form = document.forms[0];
    if(top != self) {
        $form.target = "_blank"
    }
    $form.submit();
</script>