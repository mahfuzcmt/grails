$(function () {
    var errMgsTemplates = {
            number: "incorrect.card.number",
            cvv: "cvv.invalid",
            expirationDate: "invalid.expiration.date"
        }, authorization = $('#braintree-client-token').val(),
        submit = document.querySelector('input[type="submit"]'),
        form = document.querySelector('#checkout-form'),
        fields = {
            number: {
                selector: '#card-number',
                placeholder: '4111 1111 1111 1111'
            },
            cvv: {
                selector: '#cvv',
                placeholder: '123'
            },
            expirationDate: {
                selector: '#expiration-date',
                placeholder: '10 / 2019'
            }
        };
    braintree.client.create({
        authorization: authorization
    }, function (clientErr, clientInstance) {
        if (clientErr) {
            return;
        }
        braintree.hostedFields.create({
            client: clientInstance,
            styles: {
                'input': {
                    'font-size': '14pt'
                },
                'input.invalid': {
                    'color': 'red'
                },
                'input.valid': {
                    'color': 'green'
                }
            },
            fields: fields
        }, function (hostedFieldsErr, hostedFieldsInstance) {
            if (hostedFieldsErr) {
                console.log(hostedFieldsErr);
                return;
            }
            hostedFieldsInstance.on("focus", function (event) {
                var fieldName = event.emittedBy, field = event.fields[fieldName], hostedField = $(fields[fieldName].selector);
                hostedField.siblings(".errorlist").remove()
            });
            hostedFieldsInstance.on("blur", function (event) {
                var fieldName = event.emittedBy, field = event.fields[fieldName], selector = fields[fieldName].selector, errMgs = null
                if(field.isEmpty) {
                    errMgs = "field.required"
                } else if (!field.isValid){
                    errMgs = errMgsTemplates[fieldName]
                }
                if(errMgs == null) {
                    return
                }
                errMgs = '<div class="errorlist after"><div class="message-block message-text">' + $.i18n.prop(errMgs) + '</div></div>'
                $(selector).after(errMgs)
            });
            submit.removeAttribute('disabled');
            form.addEventListener('submit', function (event) {
                event.preventDefault();
                submit.setAttribute('disabled', "disabled");
                hostedFieldsInstance.tokenize(function (tokenizeErr, payload) {
                    if (tokenizeErr) {
                        submit.removeAttribute('disabled');
                        var invalidFieldKeys = tokenizeErr.details ? tokenizeErr.details.invalidFieldKeys : ['number', "cvv", "expirationDate"];
                        invalidFieldKeys.forEach(function(fieldName){
                            var hostedField = $(fields[fieldName].selector),
                                errMgs = '<div class="errorlist after"><div class="message-block message-text">'
                                    + $.i18n.prop(tokenizeErr.code == "HOSTED_FIELDS_FIELDS_EMPTY" ? "field.required" : errMgsTemplates[fieldName]) + '</div></div>';
                            hostedField.siblings(".errorlist").remove()
                            hostedField.after(errMgs)
                        });
                        return;
                    }
                    document.querySelector('input[name="payment-method-nonce"]').value = payload.nonce;
                    form.submit();
                });
            }, false);
        });
    });
});

