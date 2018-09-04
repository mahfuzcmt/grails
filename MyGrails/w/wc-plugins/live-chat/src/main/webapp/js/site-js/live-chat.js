/**
 * Created by sajedur on 22/10/2014.
 */
app.config.validation_chat_init_error_position = app.config.validation_chat_init_error_position || "positioned-left"
app.config.validation_chat_offline_message_error_position = app.config.validation_chat_offline_message_error_position || "positioned-left"

$(function() {
    var lastUpdate, interval, chatId, liveChatPopup, helperPopup, isMinimize = false, isTerminated,
        chatMessageTemplate = '<div class="message-row"><div class="sender-time"><span class="sender #SENDER_TYPE#">#SENDER_NAME#</span><span class="time">#TIME#</span></div>' +
            '<div class="message">#MESSAGE#</div></div>',
        notificationTemplate = '<div class="message-row"><div class="system-message">#MESSAGE#</div></div>',
        fileMessageTemplate = '<div class="message-row"><div class="sender-time"><span class="sender #SENDER_TYPE#">#SENDER_NAME#</span><span class="time">#TIME#</span></div>' +
            '<div class="message"><a target="_blank" href="' + app.baseUrl + 'liveChat/downloadFile?id=#FILEID#">#FILENAME#</a></div></div>';

    function renderMessage(panel, data, type) {
        var msgSpan = $('<div class="message-block ' + type + '-message">' + data + '</div>');
        panel.find(".notification-container").prepend(msgSpan);
        setTimeout(function(){
            msgSpan.remove();
        }, 5000);
    }
    function closeHelperPopup() {
        helperPopup.remove();
        liveChatPopup.show();
        helperPopup = null;
    }

    function attachSendFilePopupEvents(popup) {
        var hasError = false;
        popup.find("input[type=file]").on("change", function(evt) {
            var target = evt.target;
            if(window.File && window.FileList) {
                if(target.files.length) {
                    var file = target.files[0];
                    popup.find(".file-name").text(file.name);
                    var fileSize = file.size;
                    if(fileSize > (5 * 1024 * 1024) ) {
                        var errorMessage = $.i18n.prop("can.not.upload.over");
                        popup.find(".error-message").text(errorMessage);
                        popup.find(".submit-button").addClass("disabled").attr("disabled", "disabled");
                        popup.find(".uploader-input").addClass("error");
                    } else {
                        popup.find(".error-message").text("");
                        popup.find(".submit-button").removeClass("disabled").removeAttr("disabled");
                        popup.find(".uploader-input").removeClass("error");
                    }
                    var suffix = "MB";
                    if(fileSize < 1024) {
                        suffix = "Byte"
                    } else if(fileSize < (1024 * 1024)) {
                        fileSize = fileSize / 1024;
                        suffix = "KB"
                    } else {
                        fileSize = fileSize / (1024 * 1024);
                    }
                    popup.find(".file-size").text(fileSize.toFixed(2) + " " + suffix);
                } else {
                    popup.find(".file-name").text("");
                    popup.find(".file-size").text("");
                    popup.find(".error-message").text("")
                }
            }
        });
        popup.find("form").form({
            ajax: true,
            preSubmit: function(ajaxSetting) {
                var message = $(notificationTemplate.replace("#MESSAGE#", "Uploading...."));
                var chatArea = liveChatPopup.find(".chat-area");
                closeHelperPopup();
                chatArea.append(message);
                chatArea.parent().scrollTop(chatArea.prop('scrollHeight'));
                $.extend(ajaxSetting, {
                    success: function(resp) {
                        message.remove();
                    },
                    error: function(xhr, status, resp) {
                        message.remove();
                        renderMessage(liveChatPopup, resp.message, "error");
                    }
                })
            }
        });
    }

    function attachSendEmailPopupEvents(popup) {
        popup.find("form").form({
            ajax: true,
            preSubmit: function(ajaxSetting) {
                $.extend(ajaxSetting, {
                    success: function(resp) {
                        closeHelperPopup();
                        liveChatPopup.find(".send-chat-to-mail-button").remove();
                        renderMessage(liveChatPopup, resp.message, "success");
                    }
                })
            }
        });
    }

    function renderHelperPopup (popUrl, clazz, data) {
        function attachEvent(content) {
            content.find(".header .close, .cancel-button").click(function () {
                closeHelperPopup();
            });
            if(content.is(".send-file-popup")) {
                attachSendFilePopupEvents(content);
            } else {
                attachSendEmailPopupEvents(content);
            }
        }
        function render(content) {
            liveChatPopup.hide();
            helperPopup = content;
            $("body").append(helperPopup);

        }
        bm.ajax({
            url: popUrl,
            data: data,
            dataType: "html",
            success: function(resp) {
                var content = $(resp);
                render(content)
                attachEvent(content)
            }
        });
    }

    function closePopup() {
        $(".live-chat-popup").remove();
        liveChatPopup = null;
        interval = null;
        lastUpdate = null;
        chatId = null;
    }

    function resetForm(form) {
        form.find("input, textarea").each(function() {
            $(this).val("");
        })
    }

    function rateChat() {
        if(!isTerminated) {
            var rateButton = $(this);
            var value = rateButton.is(".active") ? "": rateButton.attr("value");
            bm.ajax({
                url: app.baseUrl + "liveChat/rateChat",
                data: {id: chatId, rating: value},
                success: function() {
                    rateButton.siblings(".rating-button").removeClass("active");
                    if(value) {
                        rateButton.addClass("active");
                    } else {
                        rateButton.removeClass("active");
                    }
                }
            });
        }
    }

    function leaveChat() {
        bm.ajax({
            url: app.baseUrl + "liveChat/chatLeave",
            success: function() {
                closePopup();
            }
        })
    }

    function updateChatMessages(chatArea, messages) {
        messages.every(function() {
            var message;
            var time = new Date(this.time).toString("HH:mm");
            var type = this.notificationType;
            if(this.isNotification && type == "file_transfer") {
                var senderName = $.i18n.prop("you");
                if(this.senderType == "agent") {
                    senderName = this.notificationArgs ? this.notificationArgs[0] : ""
                }
                message = fileMessageTemplate.replace("#SENDER_TYPE#", this.senderType).replace("#SENDER_NAME#", senderName).replace("#FILENAME#", this.fileName).replace("#TIME#", time).replace("#FILEID#", this.fileIdentifier);
            } else if(this.isNotification) {
                var supportedNotifications = ["terminate_chat", "rate_chat_good", "rate_chat_bad", "rating_cancel", "leave_chat"];
                var joinedNotifications = ["transfer_chat_accept", "invite_chat_accept"];

                if($.inArray(type, supportedNotifications) >= 0) {
                    var text = $.i18n.prop("notification." + type + ".for.customer", this.notificationArgs);
                    message = notificationTemplate.replace("#MESSAGE#", text);
                } else if( $.inArray(type, joinedNotifications) >= 0) {
                    var text = $.i18n.prop("notification." + type + ".for.admin", this.notificationArgs ? this.notificationArgs : []);
                    message = notificationTemplate.replace("#MESSAGE#", text);
                } else {
                    return;
                }
            } else {
                var senderName = $.i18n.prop("you");
                if(this.senderType == "agent") {
                    senderName = this.name;
                }
                message = chatMessageTemplate.replace("#SENDER_TYPE#", this.senderType).replace("#SENDER_NAME#", senderName).replace("#MESSAGE#", this.message.htmlEncode()).replace("#TIME#", time);
            }
            message = $(message);
            chatArea.append(message);
            chatArea.parent().scrollTop(chatArea.prop('scrollHeight'));
        })
    }

    function startUpdaterRobot(chatWindow) {
        var chatArea = chatWindow.find(".chat-area");
        bm.ajax({
            url: app.baseUrl + "liveChat/chatUpdate",
            data: {lastUpdate: lastUpdate},
            success: function(resp) {
                lastUpdate = resp.lastUpdate;
                updateChatMessages(chatArea, resp.messages)
            },
            error: function(xhr, status, resp) {
                if(resp.isTerminated) {
                    interval = null;
                    isTerminated = true;
                    var message = [{isNotification: true, notificationType: "terminate_chat"}];
                    updateChatMessages(chatArea, message);
                    liveChatPopup.find(".send-file-button").remove();
                    liveChatPopup.find("button, textarea").attr("disabled", "disabled");
                    liveChatPopup.find(".chat-window").addClass("terminated").attr("disabled", "disabled");
                }
            }
        })
    }

    var webSocket;
    function sendChatMessage(message) {
        if(webSocket.readyState !== webSocket.CLOSED) {
            var messageObject = {};
            messageObject.message = message;
            messageObject.user = 'customer';
            var json = JSON.stringify(messageObject);
            webSocket.send(json);
        }
    }

    function bindChatPopupEvents(chatPopup) {
        var chatWindow = chatPopup.find(".chat-window");
        chatId = chatWindow.attr("chat-id");
        encryptedId = chatWindow.find("[name=encryptedId]").val();
        var chatMessageForm = chatPopup.find(".chat-message-form");
            if(chatId) {
                var webSocketUrl =  bm.getBaseURLProtocol() == "https:" ? "wss://" : "ws://" +location.host+ "/socket/chat/"+ chatId;
                webSocket = new WebSocket(webSocketUrl);
            }
            chatMessageForm.on('submit', function() {
                var message = chatMessageForm.find("[name=message]").val();
                if(message.length == 0) {
                    return false;

            }
            sendChatMessage(message);
            resetForm(chatMessageForm);
            return false;
        });

        var chatBox = chatMessageForm.find("textarea");
        chatBox.on("keypress", function(e) {
            var isReturnKey = (e.which == 13 || e.which == 10)
            if ( isReturnKey && !e.shiftKey && !e.ctrlKey){
                chatMessageForm.submit();
                return false
            } else if(isReturnKey && e.ctrlKey ) {
                chatBox.val(chatBox.val() + "\n");
            }
        });
        isTerminated = false;
        interval = 1000;
        startUpdaterRobot(chatPopup);
        var buttonHolder = chatPopup.find(".button-holder");
        chatPopup.find(".send-chat-to-mail-button").on("mousedown", function() {
            renderHelperPopup(app.baseUrl+ "liveChat/sendChatToMailPopup", "send-chat-to-mail-popup", {encryptedId: encryptedId});
        });
        chatPopup.find(".send-file-button").on("mousedown", function() {
            renderHelperPopup(app.baseUrl+ "liveChat/sendFilePopup", "send-file-popup");
        });
        chatPopup.find(".rating-button").on("click", rateChat)
        chatPopup.find(".more-option").on("click", function() {
            if(isTerminated && buttonHolder.is(".expanded")) {
                buttonHolder.removeClass("expanded");
            } else {
                buttonHolder.addClass("expanded")
            }
        });
        chatPopup.find(".more-option").blur(function() {
            buttonHolder.removeClass("expanded");
        });
        var leaveChatWindow = chatPopup.find(".leave-chat-window");
        var closeButton = chatPopup.find(".header .close-button");
        var minimizeButton = chatPopup.find(".header .minimize");
        chatPopup.find(".header .close-button, .leave-chat-button").on("mousedown", function() {
            if(isTerminated) {
                closePopup();
            } else {
                leaveChatWindow.show();
                closeButton.hide();
                minimizeButton.hide();
                chatWindow.hide();
            }

        });
        leaveChatWindow.find(".button.cancel").on("click", function(){
            leaveChatWindow.hide();
            closeButton.show();
            minimizeButton.show();
            if(!isMinimize) {
                chatWindow.show();
            }
        });
        leaveChatWindow.find(".button.leave").on("click", function() {
            leaveChat();
        });
        function maximize(button) {
            button.addClass("minimize").removeClass("maximize");
            liveChatPopup.addClass("minimized").removeClass("maximized");
            if(!leaveChatWindow.is(":visible")) {
                chatWindow.show();
                closeButton.show();
                minimizeButton.show();
            }
            isMinimize = false;
        }
        function minimize(button) {
            button.addClass("maximize").removeClass("minimize");
            liveChatPopup.addClass("minimized").removeClass("maximized");
            chatWindow.hide();
            isMinimize = true;
        }
        minimizeButton.on("click", function() {
            var $this = $(this);
            if($this.is(".minimize")) {
                minimize($this);
            } else {
                maximize($this)
            }
        });
        chatWindow.find(".content").scrollbar({
            vertical: {
                offset: -5
            }
        });
        webSocketUpdater(liveChatPopup);
    }

    function bindInitChatAreaEvents(initPopup) {
        initPopup.find(".header .close-button").on("click", function() {
            closePopup();
        });
        var offlineMessageForm = initPopup.find(".live-chat-offline-message-form");
        offlineMessageForm.form({
            ajax: true,
            preSubmit: function(ajaxSettings) {
                offlineMessageForm.loader();
                $.extend(ajaxSettings, {
                    success: function(resp) {
                        initPopup.find(".content-wrap").html(resp.html)
                    },
                    error: function(xhr, status, resp) {
                        offlineMessageForm.loader(false);
                        renderMessage(initPopup, resp.message, "error")
                    }
                })
            }
        });
        var chatInfoForm = initPopup.find(".chat-info-form");
        chatInfoForm.form({
            ajax: true,
            preSubmit: function(ajaxSettings) {
                $.extend(ajaxSettings, {
                    success: function(resp) {
                        liveChatPopup.remove();
                        liveChatPopup = $(resp);
                        $("body").append(liveChatPopup)
                        bindChatPopupEvents(liveChatPopup);
                    },
                    dataType: "html"
                });
            }
        });
    }

    function webSocketUpdater(chatWindow) {
        var chatArea = chatWindow.find(".chat-area");
        if(webSocket){
            webSocket.onmessage = function (resp) {
                startUpdaterRobot(chatWindow);
            };
        }
    }

    function renderChatPopUp() {
       bm.ajax({
           url: app.baseUrl + "liveChat/initChat",
           dataType: "html",
           success: function(resp) {
               if(!liveChatPopup) {
                   liveChatPopup= $(resp);
                   $("body").append(liveChatPopup);
                   bindPopupEvents(liveChatPopup);
               webSocketUpdater(liveChatPopup);}
           }
       })
    }

    function bindPopupEvents(popup) {
        if(popup.is(".initial-popup")) {
            bindInitChatAreaEvents(popup);
        } else {
            bindChatPopupEvents(popup);
        }
    }

    $(".widget-liveChat").each(function() {
        var widget = $(this);
        widget.find(".live-chat-init-button").on("click", function() {
            if($(".live-chat-popup").length > 0 ) {
                return
            }
            var initButton = $(this);
            renderChatPopUp();
        });
    });

    bm.ajax({
        url: app.baseUrl + "liveChat/isChatRunning",
        success: function() {
            bm.onReady(window, "$_i18n_properties_loaded", renderChatPopUp)
        }
    })

});