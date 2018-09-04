<g:applyLayout name="_paymentGateway">
    <app:stylesheet href="plugins/braintree-payment-gateway/css/site/braintree-ui.css"/>
    <script type="text/javascript" src="https://js.braintreegateway.com/web/3.3.0/js/client.min.js"></script>
    <script type="text/javascript" src="https://js.braintreegateway.com/web/3.3.0/js/hosted-fields.min.js"></script>
    <app:javascript src="plugins/braintree-payment-gateway/js/site-js/braintree.js"/>

    <form id="checkout-form" action="${app.relativeBaseUrl()}brainTree/processApiPayment" method="post" class="payment-form brain-tree-payment-gateway-form">
        <input type="hidden" id="braintree-client-token" value="${clientToken}">
        <div id="error-message"></div>
        <div class="form-row">
            <label class="hosted-fields--label" for="card-number"><g:message code="card.number"/></label>
            <div class="hosted-field" name="card_number" id="card-number"></div>
        </div>
        <div class="form-row">
            <label class="hosted-fields--label" for="cvv"><g:message code="card.verification.number"/></label>
            <div class="hosted-field" name="cvv" id="cvv"></div>
        </div>
        <div class="form-row">
            <label class="hosted-fields--label" for="expiration-date"><g:message code="card.expiry.date"/></label>
            <div class="hosted-field" name="expiration_date" id="expiration-date"></div>
        </div>
        <input type="hidden" name="payment-method-nonce">
        <div class="braintree-button-container">
            <input type="submit" class="button button--small button--green" value="<g:message code="submit"/>" disabled>
        </div>
    </form>
</g:applyLayout>