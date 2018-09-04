$(function() {
    app.initEventDetailsRequest = function(reqInfoBtns) {
        $.each(reqInfoBtns, function(idx, elm) {
            bindEventDetailsRequestAction(elm)
        });
        function bindEventDetailsRequestAction(elm) {
            $(elm).click(function() {
                var btn = $(this);
                var eventId = btn.attr('event-id');
                var eventName = btn.attr('event-name');
                var content = eventDetailsRequestPopup(eventName);
                appendRequestForm(content);
                var event_popup;
                content.find(".close-popup, .cancel-button").click(function () {
                    event_popup.close();
                });
                event_popup = content.popup({
                    is_fixed: true,
                    is_always_up: true,
                    clazz: "popup",
                    auto_close: false
                }).obj(POPUP);
                $(content).find('.submit-button').click(function() {
                    content.find('form').form({
                        ajax: {
                            data: {eventId: eventId},
                            dataType: 'json',
                            success: function(resp) {
                                appendStatusMessage(content, resp.status, resp.message)
                            },
                            error: function(resp) {
                                var jsonResp = JSON.parse(resp.responseText);
                                appendStatusMessage(content, jsonResp.status, jsonResp.message)
                            }
                        }
                    })
                });
            })
        }
        function eventDetailsRequestPopup(eventName) {
            var form = $('<form action="' + app.baseUrl + 'simplifiedEvent/sendPersonalizedProgram"></form>');
            form.append('<div class="header"><span class="title">' + $.i18n.prop('request.details.for.event', [eventName]) + '</span></div>');
            form.append('<div class="body"></div>');
            form.append('<div class="footer"></div> ');
            return $('<div class="event-details-request-popup"></div>').append(form);
        }

        function appendRequestForm(popup) {
            popup.find('.body').append('<div class="form-row mandatory">' +
                '<label>' + $.i18n.prop('email') + '</label>' +
                '<input type="text" name="email" validation="required email" placeholder="example@example.com">' +
                '</div>');
            popup.find('.footer').append('<span class="button-line">' +
                '<button type="submit" class="button submit-button">' + $.i18n.prop('submit.request') + '</button>' +
                '<button type="button" class="button cancel-button">' + $.i18n.prop('cancel') + '</button>' +
                '</span>')
        }

        function appendStatusMessage(popup, status, message) {
            var statusDom = popup.find('.body .status');
            if(!statusDom.length) {
                statusDom = popup.find('.body').prepend('<span class="status ' + status + '"></span>').find('.status');
            }
            if(status == 'success') {
                popup.find('.form-row').remove();
                popup.find('.submit-button').remove();
                statusDom.html(message).show();
                popup.find('.cancel-button').text($.i18n.prop('ok'));
            }else {
                statusDom.html(message).show();
            }
            setTimeout(function() {
                popup.fadeOut(400);
                $(".popup-mask").css('display', 'none');
            }, 10000)
        }
    };
    var reqInfoBtns = $('.event-widget-container').find('.request-info .button');
    app.initEventDetailsRequest(reqInfoBtns);
});