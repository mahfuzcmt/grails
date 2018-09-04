<g:applyLayout name="_paymentGateway">
<div id="sq-payment-form" applicationid="${squareConfig ? squareConfig["applicationId"] : " "}" locationid="${squareConfig ? squareConfig["locationId"] : " "}">
    <script type="text/javascript" src='https://js.squareup.com/v2/paymentform'></script>
    <script type="text/javascript" src="${app.systemResourceBaseUrl()}plugins/square-payment-gateway/js/payment-form/sqpaymentform.js"></script>

    <div id="sq-ccbox">
        <form id="nonce-form" novalidate action="${app.relativeBaseUrl()}squareTransaction/processApiPayment" method="POST">
            Pay with a Credit Card
            <table>
                <tbody>
                <tr>
                    <td>Card Number:</td>
                    <td><div id="sq-card-number"></div></td>
                </tr>
                <tr>
                    <td>CVV:</td>
                    <td><div id="sq-cvv"></div></td>
                </tr>
                <tr>
                    <td>Expiration Date:</td>
                    <td><div id="sq-expiration-date"></div></td>
                </tr>
                <tr>
                    <td>Postal Code:</td>
                    <td><div id="sq-postal-code"></div></td>
                </tr>
                <tr>
                    <td colspan="2">
                        <button id="sq-creditcard" class="button-credit-card" onclick="requestCardNonce(event)">
                            Pay with card
                        </button>
                    </td>
                </tr>
                </tbody>
            </table>
            <input type="hidden" id="card-nonce" name="nonce">
        </form>
    </div>
</div>
</g:applyLayout>
