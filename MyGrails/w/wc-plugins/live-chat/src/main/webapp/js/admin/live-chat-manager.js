var LiveChatManager = (function() {
    var updaterActive,
        updateInterval,
        lastUpdate,
        activeChatId,
        attendedChats,
        orphanChatBlocks,
        transferRequests,
        inviteRequests,
        audio,
        haveToPlayBuzz = false,
        isWindowActive = true;

    function changeActiveChat(newChatId) {
        activeChatId = newChatId
        if(activeChatId){
            app.global_event.trigger("active-chat-changed", activeChatId)
        }
    }

    function activateOrphanChat(chatId, statusBlock) {
        bm.ajax({
            url: app.baseUrl + "liveChatAdmin/activateOrphanChat",
            data: {chatId: chatId},
            success: function(resp) {
                statusBlock.removeClass("orphan-chat");
                statusBlock.addClass("agent-chat")
                changeActiveChat(chatId)
            }
        });
    }

    function openChatTab() {
        ComponentManager.openTab(ComponentManager.getRibbonData(app.ribbons.web_marketing, "live-chat"), {active: "chat"});
    }

    function statusBlockClickHandler() {
        var statusBlock = $(this);
        var chatId = statusBlock.attr("chat-id");
        if(statusBlock.is(".orphan-chat")) {
            activateOrphanChat(chatId, statusBlock)
        } else if(activeChatId != chatId) {
            changeActiveChat(chatId);
        }
        statusBlock.removeClass("has-new-message");
        openChatTab();
    }

    function removeTransferBlock(chatId) {
        var index = transferRequests.find("this.chatId == " + chatId );
        if(index) {
            var block = StatusBarManager.get("chat-" + chatId);
            transferRequests.splice(index, 1);
            block.remove();
            $("#status-bar .transfer-request[chat-id=" + chatId + "]").remove()
            transferRequests.filter("this.chatId == " + chatId ).every(function() {
                transferRequests.remove(this)
            });
        }
    }

    function removeInviteBlock(chatId) {
        var index = inviteRequests.find("this.chatId == " + chatId );
        if(index) {
            var block = StatusBarManager.get("chat-" + chatId);
            inviteRequests.splice(index, 1);
            block.remove();
            $("#status-bar .invite-request[chat-id=" + chatId + "]").remove()
            inviteRequests.filter("this.chatId == " + chatId ).every(function() {
                inviteRequests.remove(this)
            });
        }
    }

    function transferRequestHandler(requset) {
        var title = $.i18n.prop("confirm.accept.chat.transfer.request", [requset.requsterName, bm.htmlEncode(requset.message)]);
        bm.confirm(title, [
            {
                text: $.i18n.prop("accept"),
                handler: function() {
                    bm.ajax({
                        url: app.baseUrl + "liveChatAdmin/acceptTransferRequest",
                        data: {chatId: requset.chatId, requesterId: requset.requesterId},
                        success: function() {
                            removeTransferBlock(requset.chatId);
                            var chat = {
                                chatId: requset.chatId,
                                name: requset.visitorName,
                                newMessage: 0
                            };
                            addAttendedBlock(chat);
                            changeActiveChat(requset.chatId);
                            openChatTab();
                        },
                        error: function(xhr, status, resp) {
                            if(resp.errorType == "not_available") {
                                removeTransferBlock(requset.chatId);
                            }
                        }
                    });
                }
            },
            {
                text: $.i18n.prop("reject"),
                handler: function() {
                    bm.ajax({
                        url: app.baseUrl + "liveChatAdmin/rejectTransferRequest",
                        data: {chatId: requset.chatId, requesterId: requset.requesterId},
                        success: function() {
                            removeTransferBlock(requset.chatId);
                        },
                        error: function(xhr, status, resp) {
                            if(resp.errorType == "not_available") {
                                removeTransferBlock(requset.chatId);
                            }
                        }
                    });
                }
            }
        ], undefined, undefined, {clazz: "chat-accept-request-confirm"});
    }

    function inviteRequestHandler(requset) {
        var title = $.i18n.prop("confirm.accept.chat.invite.request", [requset.requsterName, bm.htmlEncode(requset.message)]);
        bm.confirm(title, [
            {
                text: $.i18n.prop("accept"),
                handler: function() {
                    bm.ajax({
                        url: app.baseUrl + "liveChatAdmin/acceptInviteRequest",
                        data: {chatId: requset.chatId, requesterId: requset.requesterId},
                        success: function() {
                            removeInviteBlock(requset.chatId);
                            var chat = {
                                chatId: requset.chatId,
                                name: requset.visitorName,
                                newMessage: 0
                            };
                            addAttendedBlock(chat);
                            changeActiveChat(requset.chatId);
                            openChatTab();
                        },
                        error: function(xhr, status, resp) {
                            if(resp.errorType == "not_available") {
                                removeInviteBlock(requset.chatId);
                            }
                        }
                    });
                }
            },
            {
                text: $.i18n.prop("reject"),
                handler: function() {
                    bm.ajax({
                        url: app.baseUrl + "liveChatAdmin/rejectInviteRequest",
                        data: {chatId: requset.chatId, requesterId: requset.requesterId},
                        success: function() {
                            removeInviteBlock(requset.chatId);
                        },
                        error: function(xhr, status, resp) {
                            if(resp.errorType == "not_available") {
                                removeInviteBlock(requset.chatId);
                            }
                        }
                    });
                }
            }
        ], undefined, undefined, {clazz: "chat-accept-request-confirm"});
    }

    function renderOrphanBlock(orphanChats) {
        var toRemove = []
        orphanChatBlocks.every(function(index, request) {
            var index = orphanChats.find("this.chatId == " + request.chatId );
            var block = StatusBarManager.get("chat-" + request.chatId);
            if(index > -1) {
                orphanChats.splice(index, 1);
            } else {
                if(block) {
                    block.remove();
                }
                toRemove.push(request)
            }
        });
        toRemove.every(function(index, request) {
            orphanChatBlocks.remove(request);
        });
        orphanChats.every(function() {
            var request = this
            var blockId = "chat-" + this.chatId;
            var block = StatusBarManager.allocate(blockId, "live-chat orphan-chat", statusBlockClickHandler);
            block.set('<span class="title"><span class="label">' + $.i18n.prop("live.chat") + "</span> - <span class='name'>" + request.name + '</span></span>')
            block.el.attr("chat-id", this.chatId);
            orphanChatBlocks.push(this);
            block.el.flash();
            haveToPlayBuzz = true;
        });
    }

    function modifyActiveChatBlock(block) {
        if(block){
            block.el.removeClass("has-new-message");
            var flashInst = block.el.data("flash-inst");
            if(flashInst) {
                flashInst.stop();
            }
        }
    }

    function addAttendedBlock(chat) {
        var chatId = chat.chatId;
        var index = attendedChats.indexOf(chatId);
        var blockId = "chat-" + chatId;
        if(index < 0) {
           var block = StatusBarManager.allocate(blockId, "live-chat agent-chat", statusBlockClickHandler);
           block.el.attr("chat-id", chat.chatId);
           block.set('<span class="new-messages">' + chat.newMessage + '</span><span class="title"><span class="label">' + $.i18n.prop("live.chat") + "</span> - <span class='name'>" + chat.name + '</span></span>');
           attendedChats.push(chatId);
           return block
       } else {
           return StatusBarManager.get(blockId)
       }

    }

    function renderAttendedChatBlock(chats) {
        var toRemove = []
        attendedChats.every(function(index, value) {
            var index = chats.find("this.chatId == " + value );
            var block = StatusBarManager.get("chat-" + value);
            if(index > -1) {
                var chat = chats[index];
                if(chat.newMessage && chat.chatId != activeChatId) {
                    block.el.find(".new-messages").text(chat.newMessage);
                    if(!block.el.is(".has-new-message")) {
                        block.el.addClass("has-new-message");
                        haveToPlayBuzz = true;
                    }
                    block.el.flash();
                } else {
                    modifyActiveChatBlock(block)
                }
                chats.splice(index, 1);
            } else {
                if(block) {
                    block.remove();
                }
                toRemove.push(value)
            }
        });
        toRemove.every(function(index, value) {
            attendedChats.remove(value);
        })
        chats.every(function() {
            var block = addAttendedBlock(this);
            if(this.newMessage) {
                block.el.addClass("has-new-message");
                haveToPlayBuzz = true;
                block.el.flash();
            }
        });
    }

    function renderTransferRequestBlock(chats) {
        var toRemove = []
        transferRequests.every(function(index, request) {
            var index = chats.find("this.chatId == " + request.chatId );
            var block = StatusBarManager.get("chat-" + request.chatId);
            if(index > -1) {
                chats.splice(index, 1);
            } else {
                if(block) {
                    block.remove();
                }
                toRemove.push(request)
            }
        });
        toRemove.every(function(index, request) {
            transferRequests.remove(request);
        });
        chats.every(function() {
            var request = this
            var blockId = "chat-" + this.chatId;
            var block = StatusBarManager.allocate(blockId, "live-chat transfer-request", function() {
                transferRequestHandler(request);
            });
            block.set('<span class="title"><span class="label">' + $.i18n.prop("live.chat") + "</span> - <span class='name'>" + request.visitorName + '</span></span>')
            block.el.attr("chat-id", this.chatId);
            block.el.flash();
            haveToPlayBuzz = true;
            transferRequests.push(this);
        });
    }

    function renderInviteRequestBlock(chats) {
        var toRemove = []
        inviteRequests.every(function(index, request) {
            var index = chats.find("this.chatId == " + request.chatId );
            var block = StatusBarManager.get("chat-" + request.chatId);
            if(index > -1) {
                chats.splice(index, 1);
            } else {
                if(block) {
                    block.remove();
                }
                toRemove.push(request)
            }
        });
        toRemove.every(function(index, request) {
            inviteRequests.remove(request);
        });
        chats.every(function() {
            var request = this
            var blockId = "chat-" + this.chatId;
            var block = StatusBarManager.allocate(blockId, "live-chat invite-request", function() {
                inviteRequestHandler(request);
            });
            block.set('<span class="title"><span class="label">' + $.i18n.prop("live.chat") + "</span> - <span class='name'>" + request.visitorName + '</span></span>')
            block.el.attr("chat-id", this.chatId);
            block.el.flash();
            haveToPlayBuzz = true;
            inviteRequests.push(this);
        });
    }

    function infoUpdater() {
        bm.ajax({
            url: app.baseUrl + "liveChatAdmin/updateInfo",
            data: {activeChatId: activeChatId, lastUpdate: lastUpdate},
            success: function(resp) {
                renderOrphanBlock(resp.orphanChats);
                renderAttendedChatBlock(resp.attendedChats);
                renderTransferRequestBlock(resp.transferRequests);
                renderInviteRequestBlock(resp.inviteRequests);
                lastUpdate = resp.lastUpdate;
                if(haveToPlayBuzz) {
                    playBuzz();
                    haveToPlayBuzz = false;
                }
            },
            complete: function(xht, status, resp) {
                if(updaterActive) {
                    setTimeout(function() {
                        infoUpdater();
                    }, updateInterval)
                }
            }
        });
    }

    function initBuzz() {
        var audioTag = '<audio id="chatBuzz">' +
            '<source src="' + app.systemResourceUrl + 'plugins/live-chat/sounds/buzz.mp3" type="audio/mpeg">' +
        '</audio>';
        $(document).find("body").append(audioTag);
        audio = document.getElementById("chatBuzz");
    }

    function playBuzz() {
        try {
            audio.play();
        } catch (ex) {}
    }

    function removeAgentChatStatusBlocks() {
        attendedChats.every(function(index, value) {
            StatusBarManager.remove("chat-" + value);
        });
    }

    function removeOrphanChatStatusBlocks() {
        orphanChatBlocks.every(function(index, value) {
            StatusBarManager.remove("chat-" + value.chatId);
        });
    }

    return {
        init: function() {
            if(!updaterActive) {
                updaterActive = true;
                updateInterval = 10000;
                attendedChats = [];
                orphanChatBlocks = [];
                transferRequests = [];
                inviteRequests = [];
                initBuzz();
                infoUpdater();
            }
        },
        getActiveChatId: function() {
            if(!activeChatId && attendedChats && attendedChats.length > 0) {
                activeChatId = attendedChats[0];
                var block = StatusBarManager.get("chat-" + activeChatId);
                modifyActiveChatBlock(block);
            }
            return activeChatId;
        },
        setActiveChatId: function(chatId) {
            activeChatId = chatId
        },
        terminateChat: function(chatId){
            attendedChats.remove(parseInt(chatId));
            StatusBarManager.remove("chat-" + chatId);
            if(activeChatId == chatId) {
                this.setActiveChatId(null);
                changeActiveChat(this.getActiveChatId());
            }
        },
        playBuzz: function() {
            playBuzz();
        },
        infoUpdater: function() {
            infoUpdater();
        },

        deactivate: function() {
            if(updaterActive) {
                removeOrphanChatStatusBlocks();
                removeAgentChatStatusBlocks();
                attendedChats = null
                orphanChatBlocks = null
                lastUpdate = null
                activeChatId = null
                updateInterval = null
                transferRequests = null
                inviteRequests = null
                updaterActive = false;
            }
        },
        isUpdaterActive: function() {
            return updaterActive;
        },
        setIsWindowActive: function(value) {
            isWindowActive = value;
        },
        isWindowActive: function() {
            return isWindowActive;
        }
    }
})()

$(function() {
    $(window).on("blur", function() {
        LiveChatManager.setIsWindowActive(false);
    });
    $(window).on("focus", function() {
        LiveChatManager.setIsWindowActive(true);
    })
    $(".logout-link").on("click", function() {
        app.global_event.trigger("before-logout")
        if(LiveChatManager.isUpdaterActive()) {
            LiveChatManager.deactivate();
        }
    })
    bm.ajax({
        url: app.baseUrl + "liveChatAdmin/isActiveChat",
        success: function(resp) {
            if(resp.isActive) {
                LiveChatManager.init();
            }
        }
    })
})