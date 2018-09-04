<g:applyLayout name="_paymentGateway">
    <form id="payment-form" action="${app.relativeBaseUrl()}stripe/processApiPayment" method="post" class ="payment-form anz-payment-gateway-form valid-verify-form">
        <span class="payment-errors"></span>
        <meta name="layout" content="paymentGateway">
        <div class="header-wrapper">
            <h1><g:message code="card.information"/></h1>
        </div>
        <div class="form-row mandatory">
            <label><g:message code="card.number"/></label>
            <input class="large" name="card.PAN" data-stripe="number" type="text" validation="required cardnumber[${creditCardConfig["cardType"] == "custom" ? "" : creditCardConfig['creditCards'] }]" autocomplete="off">
        </div>
        <div class="double-input-row mandatory">
            <label><g:message code="card.expiry.date"/><span class="note form-type-info"> (MM/YYYY)</span></label>
            <g:set var="month" value="${UUID.randomUUID().toString()}"/>
            <g:set var="year" value="${UUID.randomUUID().toString()}"/>
            <div class="mandatory mandatory-chosen-wrapper">
                <g:select from="${['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']}" id="${month}" class="small credit-card-date-expiry-month" data-stripe="exp_month" name="card.expiryMonth" noSelection="['':'-Choose month-']" validation="required creditCardExpiryDate[${year}]" depends="#${year}"/>
            </div>
            <span class="form-type-info">/</span>
            <div class="mandatory mandatory-chosen-wrapper">
                <g:set var="currentYear" value="${Calendar.getInstance().get(Calendar.YEAR)}"/>
                <g:select from="${currentYear..2099}" keys="${(currentYear % 2000)..99}" class="small credit-card-date-expiry-day" id="${year}" data-stripe="exp_year" name="card.expiryYear" noSelection="['':'-Choose year-']" validation="required"/>
            </div>
        </div>
        <div class="form-row mandatory">
            <label><g:message code="card.verification.number"/></label>
            <input name="card.CVN" data-stripe="cvc" type="text" class="tiny credit-card-verification-number" validation="required cvv" autocomplete="off">
        </div>
        <div class="form-row tlr">
            <input type="submit" value="<g:message code="pay.now"/>">
        </div>
    </form>
    <script type="text/javascript" src="https://js.stripe.com/v2/"></script>
    <script type="text/javascript">
        Stripe.setPublishableKey('${stripeConfig["public_key"]}');
        var $form = $('#payment-form');
        $form.submit(function(event) {
            $form.find('.submit').prop('disabled', true);
            Stripe.card.createToken($form, stripeResponseHandler);
            return false;
        });
        function stripeResponseHandler(status, response) {
            var $form = $('#payment-form');
            if (response.error) {
                $form.find('.payment-errors').text(response.error.message);
                $form.find('.submit').prop('disabled', false);
                console.log(respone.error)
            } else {
                var token = response.id;
                $form.append($('<input type="hidden" name="stripeToken">').val(token));
                $form.get(0).submit();
            }
        };
    </script>
</g:applyLayout>