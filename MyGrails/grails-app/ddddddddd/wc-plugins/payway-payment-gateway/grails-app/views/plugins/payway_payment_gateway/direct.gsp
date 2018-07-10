<g:applyLayout name="_paymentGateway">
    <form action="${app.relativeBaseUrl()}payWay/processApiPayment" method="post" class="payment-form pay-way-payment-form valid-verify-form">
        <meta name="layout" content="paymentGateway">
        <div class="header-wrapper">
            <h1><g:message code="card.information"/></h1>
        </div>
        <div class="form-row mandatory">
            <label><g:message code="card.number"/></label>
            <input class="large" name="card.PAN" type="text" validation="required cardnumber[${creditCardConfig["cardType"] == "custom" ? "" : creditCardConfig['creditCards'] }]" autocomplete="off">
        </div>
        <div class="double-input-row mandatory">
            <label><g:message code="card.expiry.date"/><span class="note form-type-info"> (MM/YYYY)</span></label>
            <g:set var="month" value="${UUID.randomUUID().toString()}"/>
            <g:set var="year" value="${UUID.randomUUID().toString()}"/>
            <div class="mandatory mandatory-chosen-wrapper">
                <g:select from="${['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']}" id="${month}" class="small credit-card-date-expiry-month" name="card.expiryMonth" noSelection="['':'-Choose month-']" validation="required creditCardExpiryDate[${year}]" depends="#${year}"></g:select>
            </div> <span class="form-type-info">/</span> <div class="mandatory mandatory-chosen-wrapper">
                <g:set var="currentYear" value="${Calendar.getInstance().get(Calendar.YEAR)}"></g:set>
                <g:select from="${currentYear..2099}" keys="${(currentYear % 2000)..99}" class="small credit-card-date-expiry-day" id="${year}" name="card.expiryYear" noSelection="['':'-Choose year-']" validation="required" ></g:select>
            </div>
        </div>
        <div class="form-row mandatory">
            <label><g:message code="card.verification.number"/></label>
            <input name="card.CVN" type="text" class="tiny credit-card-verification-number" validation="required cvv" autocomplete="off">
        </div>
        <div class="form-row tlr">
            <input type="submit" value="<g:message code="pay.now"/>">
        </div>
    </form>
</g:applyLayout>