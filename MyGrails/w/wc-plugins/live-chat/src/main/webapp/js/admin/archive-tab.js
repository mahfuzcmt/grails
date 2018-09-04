 bm.onReady(app.tabs, "liveChat", function() {
    app.tabs.liveChat.tabInitFunctions.archive = function(panel) {
        var tab = new app.tabs.liveChat.archive(panel, this, app.baseUrl + "liveChatAdmin/loadArchive");
        tab.init();
    }

    bm.onReady(app.tabs.liveChat, "subTab", function() {
        app.tabs.liveChat.archive = function(panel, appTab, ajaxUrl) {
            app.tabs.liveChat.subTab.apply(this, arguments);
            this.ajax_url = ajaxUrl;
        }
    });
});

$(function() {
    var _at = app.tabs.liveChat.archive.inherit(app.tabs.liveChat.subTab);
    var _super = app.tabs.liveChat.archive._super;


    _at.init = function() {
         var _self = this;
        _super.init.call(this);
        app.global_event.on("chat-terminate.live-chat-tab", function() {
            _self.reload();
        });
        this.tagSelector = this.tool.find("select.tag-selector");
        this.tagSelector.on("change", function() {
           _self.reload();
        });
        app.global_event.on("chat-tag-saved.live-chat-tab", function(event, id, name) {
           _self.tagSelector.chosen("add", {text: name, value: id});
        });
        app.global_event.on("chat-tag-deleted.live-chat-tab", function(event, id) {
           _self.tagSelector.chosen("remove", id);
        });
        this.tool.find(".add-filter-tool").on("click", function () {
            bm.editPopup(_self.advanceSearchUrl, $.i18n.prop("advanced.search"), _self.advanceSearchTitle, {tag: _self.tagSelector.val()}, {
                width: _self.advance_search_popup_width || 480,
                beforeSubmit: function (form, data, popup) {
                    _self.advanceSearchFilter = form.serializeObject();
                    popup.close();
                    _self.tool.find(".remove-filter-tool").removeClass("disabled");
                    _self.tagSelector.chosen("val", "");
                    _self.reload();
                    return false;
                }
            });
        });
        this.tool.find(".remove-filter-tool").on("click", function () {
            if ($(this).is(".disabled")) {
                return;
            } else {
                _self.advanceSearchFilter = false;
                _self.tool.find(".remove-filter-tool").addClass("disabled");
                _self.reload()
            }
        });

        app.global_event.on("chat-terminate", function() {
            _self.reload();
        })
    }

    _at.sortable = {};

    _at.menu_entries = [
        {
            text: $.i18n.prop("view"),
            ui_class: "view"
        },
        {
            text: $.i18n.prop("export.txt"),
            ui_class: "export.txt"
        },
        {
            text: $.i18n.prop("email.history"),
            ui_class: "email.history"
        }
    ];

    _at.onActionClick = function(action, data) {
        switch (action) {
            case "view":
                this.view(data.id)
                break;
            case "export.txt":
                this.exportAsTxt(data.id)
                break;
            case "email.history":
                this.emailHistory(data.id)
                break;
        }
    };

    _at.view = function(id) {
        bm.viewPopup(app.baseUrl + "liveChatAdmin/viewChat", {id: id})
    };

    _at.emailHistory = function(id) {
        bm.editPopup(app.baseUrl + "liveChatAdmin/sendImmediatelyChatToMailPopup", $.i18n.prop("email.chat.history"), null, {chatId: id});
    };
    _at.exportAsTxt = function(id) {
        window.open(app.baseUrl + "liveChatAdmin/exportChat?chatId="+id)
    };
    _at.advanceSearchUrl = app.baseUrl + "liveChatAdmin/chatFilter";
    _at.advanceSearchTitle = $.i18n.prop("live.chat");
    _at.beforeReloadRequest = function (param) {
        var tag = this.tagSelector.val();
        $.extend(param, {tag: tag})
        _super.beforeReloadRequest.call(this, param);
    }
})