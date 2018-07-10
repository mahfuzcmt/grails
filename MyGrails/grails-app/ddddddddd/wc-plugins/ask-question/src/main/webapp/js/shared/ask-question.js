$(function() {
    app.config.ask_question_message_display_time = 5000;
    function questionTabEvents(panel) {
        var question = panel.find(".question-form");
        function reload() {
            question.find("[name=name], [name=email], [name=question]").val("")
        }
        function renderMessage(type, data) {
            var msgSpan = $('<div class="message-block ' + type + '">' + data + '</div>');
            panel.find(".message-container").append(msgSpan);
            msgSpan.scrollHere()
            setTimeout(function(){
                msgSpan.remove();
            }, app.config.ask_question_message_display_time);
        }
        question.form({
            ajax: true,
            preSubmit: function(ajaxSettings){
                $.extend(ajaxSettings , {
                    success: function(resp){
                        renderMessage(resp.status, resp.message)
                        reload();
                        app.captchaUtil.reloadCaptcha(resp.captchaType);
                    },
                    error : function(a, b , resp) {
                        renderMessage(resp.status, resp.message)
                        if(resp.captchaValidation) {
                            app.captchaUtil.reloadCaptcha(resp.captchaType);
                        }
                    }
                })
            }
        });
    }

    $(".product-widget.widget-information .bmui-tab").on("tab:load", function (event, ui) {
        var questionPanel = $(ui.panel).find(".ask-question-panel");
        if(questionPanel) {
            questionTabEvents(questionPanel);
        }
    });

})
