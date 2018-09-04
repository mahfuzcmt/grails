 bm.onReady(app.tabs, "liveChat", function() {
    app.tabs.liveChat.tabInitFunctions.chat = function(panel) {
        var tab = new app.tabs.liveChat.chat(this, panel);
        tab.init();
    };

    app.tabs.liveChat.chat = function(parentTab, panel) {
        this.parentTab = parentTab;
        this.body = panel;
        this.tool = panel.tool;
        this.chatId = LiveChatManager.getActiveChatId();
        this.chatWindow = panel.find(".chat-window");
        this.lastUpdate = null;
        this.updaterIsStarted = false;
        this.createWebSocket(this.chatId);
    };


    (function() {
        var _ct = app.tabs.liveChat.chat.prototype;
        _ct.messageTemplate = '<div class="message-block #SENDERTYPE#"><span class="name">#NAME#</span><div class="message-wrap"><span class="time">#TIME#</span><span class="message">#MESSAGE#</span></div></div>';
        _ct.fileTransferMessaeTempate = '<div class="message-block #SENDERTYPE#"><span class="name">#NAME#</span>' +
            '<div class="message-wrap file-warp"><span class="time">#TIME#</span>' +
            '<span class="message"><a target="_blank" href="' + app.baseUrl + 'liveChatAdmin/downloadFile?id=#FILEID#">#FILENAME#</a></span></div>';
        _ct.noficationTempate = '<div class="message-block"><span class="notification">#MESSAGE#</span></div>';
        _ct.init = function() {
            var _self = this;
            var body = this.body;
            body.find(".chat-area").scrollbar();
            if (this.chatId) {
                this.showChatWindow(true);
                this.webSocketUpdater();
            }
            this.tagSelector = body.find("select.tag-selector");
            this.actionSelector = body.find("select.action-selector");
            this.actionSelector.on("change", function() {
               var action = this.value;
                switch (action) {
                    case "invite_operator":
                        _self.inviteOperator(_self.chatId);
                        break;
                    case "transfer":
                        _self.transferChat(_self.chatId);
                        break;
                    case "export_txt":
                        _self.exportChat(_self.chatId);
                        break;
                    case "email_history":
                         _self.sendChatHistoryToMail(_self.chatId);
                        break;
                    case "block":
                        console.log("block");
                        break;
                    case "terminate":
                        _self.terminateChat();
                        break;
                }
            });

            app.global_event.one("on-live-chat-tab-close.chat-tab", function () {
                _self.interval = null;
                _self.chatId = null;
                app.global_event.off(".chat-tab");
            });
            app.global_event.one("before-chat-tab-reload-chat", function() {
                _self.interval = null;
                _self.chatId = null;
                app.global_event.off(".chat-tab");
            });
            app.global_event.on("chat-tag-saved.chat-tab", function(event, id, name) {
                if(_self.tagSelector.length) {
                    _self.tagSelector.chosen("add", {text: name, value: id});
                }
            });
            app.global_event.on("chat-tag-deleted.chat-tab", function(event, id) {
                if(_self.tagSelector.length) {
                    _self.tagSelector.chosen("remove", id);
                }
            });
            app.global_event.on("before-logout.chat-tab", function(event, id) {
               _self.interval = null;
            });
            _self.attachEvents();
        };

        var webSocket;
        _ct.createWebSocket = function(chatId) {
            if(chatId) {
                var webSocketUrl =  bm.getBaseURLProtocol() == "https:" ? "wss://" : "ws://" +location.host+ "/socket/chat/"+ chatId;
                webSocket = new WebSocket(webSocketUrl);
            }
        }


        function sendChatMessage(message) {
            if(webSocket.readyState !== webSocket.CLOSED) {
                var messageObject = {};
                messageObject.message = message;
                messageObject.user = 'agent';
                var json = JSON.stringify(messageObject);
                webSocket.send(json);
            }
        }

        _ct.attachEvents = function() {
            var _self = this;
            var chatMessageForm = this.chatWindow.find(".chat-message-form");
            var chatBox = chatMessageForm.find("textarea");

            chatMessageForm.on('submit', function () {
                var message = chatMessageForm.find("[name=message]").val();
                if(!_self.chatId || message.length == 0) {
                    return false;
                }
                chatBox.attr("disabled", "disabled");
                chatBox.val("");
                sendChatMessage(message);
                chatBox.removeAttr("disabled");
                chatBox.focus();
                return false;
            });

            chatBox.on("keypress", function(e) {
                var isReturnKey = (e.which == 13 || e.which == 10)
                if ( isReturnKey && !e.shiftKey && !e.ctrlKey){
                    chatMessageForm.submit();
                    return false
                } else if(isReturnKey && e.ctrlKey ) {
                    chatBox.val(chatBox.val() + "\n");
                }
            });
            this.tool.find(".activate").on("click", function() {
                _self.activateChat();
            });
            this.body.find(".activate-chat").on("click", function() {
                _self.activateChat();
            });
            this.tool.find(".chat-switch").on("click", function() {
                var _this = $(this).find(".availability-switch")
                var data = _this.data()
                if (!data.isactive){
                    _self.activateChat();
                } else{
                    _self.deactivateChat();
                }
            });
            this.body.find(".terminate-chat").on("click", function() {
                _self.terminateChat();
            });
            this.tool.find(".deactivate").on("click", function() {
                _self.deactivateChat();
            });
            this.tagSelector.on("selection_added", function(event, addedValue) {
                _self.addTag(addedValue);
            });
            this.tagSelector.on("selection_removed", function(event, removedValue) {
                _self.removeTag(removedValue)
            });
            this.body.find(".transfer-chat").on("click", function() {
               _self.transferChat(_self.chatId);
            });
            this.body.find(".send-file").on("click", function() {
                _self.uploadFile();
            })
            this.body.find(".update-chat").on("click", function() {
                _self.updateChat();
            });

        };
        _ct.activateChat = function() {
            var _self = this;
            bm.ajax({
                url: app.baseUrl + "liveChatAdmin/activateChat",
                success: function(resp) {
                     LiveChatManager.init();
                    _self.reload();
                }
            })
        };
        _ct.deactivateChat = function() {
            var _self = this;
            bm.ajax({
                url: app.baseUrl + "liveChatAdmin/deactivateChat",
                success: function(resp) {
                    LiveChatManager.deactivate();
                    _self.reload();
                }
            })
        };

        _ct.terminateChat = function() {
            var _self = this;
            bm.confirm($.i18n.prop("confirm.terminate.chat"), function() {
                bm.ajax({
                    url: app.baseUrl + "liveChatAdmin/terminateChat",
                    data: {chatId: _self.chatId},
                    success: function() {
                        LiveChatManager.terminateChat(_self.chatId);
                        app.global_event.trigger("chat-terminate", _self.chatId);
                        app.global_event.trigger("active-chat-changed", null);
                    }
                })
            }, function() {});
        };

        _ct.startUpdater = function() {
            this.createWebSocket(_self.chatId);
        };

        _ct.showChatWindow = function (show) {
            if(show) {
                this.body.find(".no-active-chat").hide();
                this.chatWindow.show();
            } else {
                this.body.find(".no-active-chat").show();
                this.chatWindow.hide();
            }
        };

        _ct.webSocketUpdater = function() {
            var _self = this;
            if(webSocket) {
                webSocket.onmessage = function (resp) {
                    var config;
                    if(resp && (resp.data != undefined || resp.data != "")){
                        config= JSON.parse(resp.data)
                    }
                    _self.chatMessageUpdater(config);
                };
             }
        };

        _ct.chatMessageUpdater = function(config) {
            var _self = this;
            var chatId = LiveChatManager.getActiveChatId();
            //if(this.chatId && this.interval) {
            if(chatId) {

                bm.ajax({
                    url: app.baseUrl + "liveChatAdmin/updateActiveChat",
                    data: {lastUpdate: _self.lastUpdate, chatId: chatId},
                    success: function(resp) {
                        if(chatId == resp.chatId) {
                            _self.updateMessage(resp.messages, config);
                            _self.lastUpdate = resp.lastUpdate;
                        }
                    },
                    error: function(xhr, status, resp) {
                        if(resp.isTerminated) {
                            _self.interval = null;
                            _self.updateMessage([{
                                isNotification: true,
                                notificationType: "terminate_chat"
                            }])
                        }
                    }
                });
            }
        };

        _ct.updateMessage = function(messages, config) {
            var _self = this;
            var chatArea = _self.chatWindow.find(".chat-area");
            var type;
            messages.every(function() {
                var message;
                var time = new Date(this.time).toString("HH:mm");
                type = this.notificationType;
                if(this.isNotification && type == "file_transfer") {
                    var senderName = this.notificationArgs ? this.notificationArgs[0] : ""
                    message = _self.fileTransferMessaeTempate.replace("#SENDERTYPE#", this.senderType).replace("#NAME#", senderName).replace("#FILENAME#", this.fileName).replace("#TIME#", time).replace("#FILEID#", this.fileIdentifier);
                } else if(this.isNotification) {
                    var text = $.i18n.prop("notification." + type + ".for.admin", this.notificationArgs ? this.notificationArgs : []);
                    message = _self.noficationTempate.replace("#MESSAGE#", text);
                } else {
                    message = _self.messageTemplate.replace("#NAME#", this.name).replace("#MESSAGE#", this.message.htmlEncode()).replace("#TIME#", time).replace("#SENDERTYPE#", this.senderType);
                }
                message = $(message);
                chatArea.append(message);
                chatArea.scrollTop(chatArea.prop('scrollHeight'));
            });
            if(messages.length) {
                if(config !== undefined || config != null){
                    if(config.new_incoming_chat_sound !== undefined && config.new_incoming_chat_sound == 'true' && type == undefined){
                        LiveChatManager.playBuzz();
                    } else if(config.new_chat_message_sound !== undefined && config.new_chat_message_sound == 'true'  && type == undefined){
                        LiveChatManager.playBuzz();
                    } else if(config.disconnect_sound !== undefined && config.disconnect_sound == 'true' && type =="leave_chat"){
                        LiveChatManager.playBuzz();
                    } else if(config.operator_joins_a_chat_sound !== undefined && config.operator_joins_a_chat_sound == 'true' && type =="invite_chat_accept"){
                        LiveChatManager.playBuzz();
                    } else if(config.chat_is_transferred_to_another_operator_sound !== undefined && config.chat_is_transferred_to_another_operator_sound == 'true'  && type =="transfer_chat_accept"){
                        LiveChatManager.playBuzz();
                    }
                }
            }
        };

        _ct.updateChat = function() {
            var _self = this;
            var profileForm =  this.body.find(".chat-update-form");
            profileForm.form({
                ajax: {
                    success: function() {
                        _self.body.reload()
                    }
                }
            })
        };

        _ct.addTag = function(tagId) {
            var _self = this;
            bm.ajax({
                url: app.baseUrl + "liveChatAdmin/addTagToChat",
                data: {tagId : tagId, chatId: _self.chatId}
            })
        };
        _ct.removeTag = function(tagId) {
            var _self = this;
            bm.ajax({
                url: app.baseUrl + "liveChatAdmin/removeTagFromChat",
                data: {tagId : tagId, chatId: _self.chatId}
            })
        };
        _ct.transferChat = function(chatId) {
            bm.editPopup(app.baseUrl + "liveChatAdmin/initTransferRequest", $.i18n.prop("send.transfer.request"), null, {chatId: chatId});
        };
        _ct.inviteOperator = function(chatId) {
            bm.editPopup(app.baseUrl + "liveChatAdmin/initInviteRequest", $.i18n.prop("send.invite.request"), null, {chatId: chatId});
        };
        _ct.uploadFile = function() {
            var chatId = this.chatId;
            bm.editPopup(app.baseUrl + "liveChatAdmin/sendFilePopup", $.i18n.prop("select.file"), null, {chatId: chatId}, {
                success: function () {}
            })
        };

        _ct.sendChatHistoryToMail = function(chatId) {
           bm.editPopup(app.baseUrl + "liveChatAdmin/sendChatToMailPopup", $.i18n.prop("email.chat.history"), null, {chatId: chatId});
        };

        _ct.exportChat = function(chatId) {
            window.open(app.baseUrl + "liveChatAdmin/exportChat?chatId="+chatId)
        };

        _ct.reload = function() {
            this.interval = null;
            app.global_event.off(".chat-tab");
            this.body.reload();
        }
    })();
});
