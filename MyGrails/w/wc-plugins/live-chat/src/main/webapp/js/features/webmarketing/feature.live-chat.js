/**
 * Created by sajed on 5/29/2014.
 */
app.tabs.liveChat = function () {
    this.text = $.i18n.prop("live.chat");
    this.tip = $.i18n.prop("manage.live.chat");
    this.ui_class = "live-chat";
    var activeChatId = LiveChatManager.getActiveChatId();
    this.ajax_url = app.baseUrl + "liveChatAdmin/loadAppView?chatId=" +  (activeChatId ? activeChatId : "");
    app.tabs.liveChat._super.constructor.apply(this, arguments);
};

app.ribbons.web_marketing.push({
    text: $.i18n.prop("live.chat"),
    processor: app.tabs.liveChat,
    ui_class: "live-chat",
    license: "allow_live_chat_feature"
});

app.tabs.liveChat.tabInitFunctions = {
    visitor: function(panel) {
        var editor = new app.tabs.liveChat.subTab(panel, this, app.baseUrl + "liveChatAdmin/loadVisitor");
        editor.init();
    },
    agent: function(panel) {
        var editor = new app.tabs.liveChat.subTab(panel, this, app.baseUrl + "liveChatAdmin/loadAgent");
        editor.init()
    }
}

var _lc = app.tabs.liveChat.inherit(app.MultiTab);
_lc.notEditable = true;
(function(){
    _lc.init = function() {
        app.tabs.liveChat._super.init.apply(this, arguments)
        var multiTab = this.multiTab  = this.body.find(".bmui-tab");
        this.activeTab = "chat";
        app.global_event.on("active-chat-changed.live-chat-tab", function(event, newId) {
            app.global_event.trigger("before-chat-tab-reload-chat");
            multiTab.tabify("reload", "chat", app.baseUrl + "liveChatAdmin/loadChat?chatId=" + (newId ? newId : ""));
        });
    }
})();

_lc.onContentLoad = function(data) {
    if(typeof app.tabs.liveChat.tabInitFunctions[data.index] == "function") {
        app.tabs.liveChat.tabInitFunctions[data.index].call(this, data.panel, data.tab);
    }
}

_lc.onTabActive = function(data) {
    this.activeTab = data.newIndex;
}

_lc.close = function() {
    app.tabs.liveChat._super.close.call(this);
    LiveChatManager.setActiveChatId(null);
    app.global_event.off(".live-chat-tab");
    app.global_event.trigger("on-live-chat-tab-close");
}