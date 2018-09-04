// // Set the application ID
// var applicationId = "sandbox-sq0idp-V5DQHPATRi9OzugIeUDCNg";
//
// // Set the location ID
// var locationId = "CBASELEymQOxY76-IH7w87AOChsgAQ";

/*
 * function: requestCardNonce
 *
 * requestCardNonce is triggered when the "Pay with credit card" button is
 * clicked
 *
 * Modifying this function is not required, but can be customized if you
 * wish to take additional action when the form button is clicked.
 */
function requestCardNonce(event) {

    // Don't submit the form until SqPaymentForm returns with a nonce
    event.preventDefault();

    // Request a nonce from the SqPaymentForm object
    paymentForm.requestCardNonce();
}

// Create and initialize a payment form object
var paymentForm = new SqPaymentForm({

    // Initialize the payment form elements
    applicationId: $("#sq-payment-form").attr('applicationid'),
    locationId: $("#sq-payment-form").attr('locationid'),
    inputClass: 'sq-input',

    // Customize the CSS for SqPaymentForm iframe elements
    inputStyles: [{
        fontSize: '.9em'
    }],

    // Initialize Apple Pay placeholder ID
    applePay: false,

    // Initialize Masterpass placeholder ID
    masterpass: false,

    // Initialize the credit card placeholders
    cardNumber: {
        elementId: 'sq-card-number',
        placeholder: '•••• •••• •••• ••••'
    },
    cvv: {
        elementId: 'sq-cvv',
        placeholder: 'CVV'
    },
    expirationDate: {
        elementId: 'sq-expiration-date',
        placeholder: 'MM/YY'
    },
    postalCode: {
        elementId: 'sq-postal-code'
    },

    // SqPaymentForm callback functions
    callbacks: {

        /*
         * callback function: methodsSupported
         * Triggered when: the page is loaded.
         */
        methodsSupported: function (methods) {

        },

        /*
         * callback function: createPaymentRequest
         * Triggered when: a digital wallet payment button is clicked.
         */
        // createPaymentRequest: function () {
        //
        //     var paymentRequestJson ;
        //     /* ADD CODE TO SET/CREATE paymentRequestJson */
        //     return paymentRequestJson ;
        // },

        /*
         * callback function: validateShippingContact
         * Triggered when: a shipping address is selected/changed in a digital
         *                 wallet UI that supports address selection.
         */
        // validateShippingContact: function (contact) {
        //
        //     var validationErrorObj ;
        //     /* ADD CODE TO SET validationErrorObj IF ERRORS ARE FOUND */
        //     return validationErrorObj ;
        // },

        /*
         * callback function: cardNonceResponseReceived
         * Triggered when: SqPaymentForm completes a card nonce request
         */
        cardNonceResponseReceived: function (errors, nonce, cardData, billingContact, shippingContact) {
            if (errors) {
                // Log errors from nonce generation to the Javascript console
                var errorMsg = " ";
                var skipComma = true;
                errors.forEach(function (error) {
                    if (skipComma === true) {
                        skipComma = false;
                        errorMsg += error.message;
                    } else {
                        errorMsg += (", " + error.message);
                    }
                });
                bm.notify(errorMsg, "error");
                return;
            }

            // alert('Nonce received: ' + nonce);
            /* FOR TESTING ONLY */

            // Assign the nonce value to the hidden form field
            document.getElementById('card-nonce').value = nonce;

            // POST the nonce form to the payment processing page
            document.getElementById('nonce-form').submit();

        },

        /*
         * callback function: unsupportedBrowserDetected
         * Triggered when: the page loads and an unsupported browser is detected
         */
        unsupportedBrowserDetected: function () {
            bm.notify($.i18n.prop("unsupported.browser"), "error");
        },

        /*
         * callback function: inputEventReceived
         * Triggered when: visitors interact with SqPaymentForm iframe elements.
         */
        inputEventReceived: function (inputEvent) {
            switch (inputEvent.eventType) {
                case 'focusClassAdded':
                    /* HANDLE AS DESIRED */
                    break;
                case 'focusClassRemoved':
                    /* HANDLE AS DESIRED */
                    break;
                case 'errorClassAdded':
                    /* HANDLE AS DESIRED */
                    break;
                case 'errorClassRemoved':
                    /* HANDLE AS DESIRED */
                    break;
                case 'cardBrandChanged':
                    /* HANDLE AS DESIRED */
                    break;
                case 'postalCodeChanged':
                    /* HANDLE AS DESIRED */
                    break;
            }
        },

        /*
         * callback function: paymentFormLoaded
         * Triggered when: SqPaymentForm is fully loaded
         */
        paymentFormLoaded: function () {
            /* HANDLE AS DESIRED */
        }
    }
});