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
        this.interval = 1000;
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
                this.chatMessageUpdater();
            }
            this.tagSelector = body.find("select.tag-selector");
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
                var webSocketUrl =  (bm.getBaseURLProtocol() == "https:" ? "wss://" : "ws://") + bm.getAbsoluteURL("socket/chat/" + chatId);
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
            this.body.find(".terminate-chat").on("click", function() {
                webSocket.close();
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
            var _self =this;
            bm.confirm($.i18n.prop("confirm.terminate.chat"), function() {
                bm.ajax({
                    url: app.baseUrl + "liveChatAdmin/terminateChat",
                    data: {chatId: _self.chatId},
                    success: function() {
                        LiveChatManager.terminateChat(_self.chatId);
                        app.global_event.trigger("chat-terminate", _self.chatId)
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
                    var data = JSON.parse(resp.data);
                    var messageData = data.messages ? [data.messages] : [data];
                    _self.updateMessage(messageData, data.customerName);
                    _self.lastUpdate = data.lastUpdate;
                };
            }
        };

        _ct.chatMessageUpdater = function() {
            var _self = this;
            if(this.chatId && this.interval) {
                bm.ajax({
                    url: app.baseUrl + "liveChatAdmin/updateActiveChat",
                    data: {lastUpdate: _self.lastUpdate, chatId: _self.chatId},
                    success: function(resp) {
                        if(_self.chatId == resp.chatId) {
                            _self.updateMessage(resp.messages, resp.customerName);
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

        _ct.updateMessage = function(messages, customerName) {
            var _self = this;
            var chatArea = _self.chatWindow.find(".chat-area");

            messages.every(function() {
                var message;
                var time = new Date(this.time).toString("HH:mm");
                var type = this.notificationType;
                if(this.isNotification && type == "file_transfer") {
                    var senderName = customerName;
                    if(this.senderType == "agent") {
                        senderName = $.i18n.prop("admin");
                    }
                    message = _self.fileTransferMessaeTempate.replace("#SENDERTYPE#", this.senderType).replace("#NAME#", senderName).replace("#FILENAME#", this.fileName).replace("#TIME#", time).replace("#FILEID#", this.fileIdentifier);
                } else if(this.isNotification) {
                    var text = $.i18n.prop("notification." + type + ".for.admin", this.notificationArgs ? this.notificationArgs : []);
                    message = _self.noficationTempate.replace("#MESSAGE#", text);

                } else {
                    var name = $.i18n.prop("admin");
                    if(this.senderType == "customer") {
                        name = customerName;
                    }
                    message = _self.messageTemplate.replace("#NAME#", name).replace("#MESSAGE#", this.message.htmlEncode()).replace("#TIME#", time).replace("#SENDERTYPE#", this.senderType);
                }
                message = $(message);
                chatArea.append(message);
                chatArea.scrollTop(chatArea.prop('scrollHeight'));
            });
            if(messages.length && (!LiveChatManager.isWindowActive() || _self.parentTab.activeTab != "chat")) {
                LiveChatManager.playBuzz();
            }
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

        _ct.uploadFile = function() {
            var chatId = this.chatId;
            bm.editPopup(app.baseUrl + "liveChatAdmin/sendFilePopup", $.i18n.prop("select.file"), null, {chatId: chatId});
        };

        _ct.reload = function() {
            this.interval = null;
            app.global_event.off(".chat-tab");
            this.body.reload();
        }

    })();
});
