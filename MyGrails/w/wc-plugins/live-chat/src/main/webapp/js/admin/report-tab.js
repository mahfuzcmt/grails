 bm.onReady(app.tabs, "liveChat", function() {
    app.tabs.liveChat.tabInitFunctions.report = function(panel) {
        var tab = new app.tabs.liveChat.report(panel, this, app.baseUrl + "liveChatAdmin/loadReport");
        tab.init();
    }

    bm.onReady(app.tabs.liveChat, "archive", function() {
        app.tabs.liveChat.report = function(panel, appTab, ajaxUrl) {
            app.tabs.liveChat.archive.apply(this, arguments);
        }
    });
});

$(function() {
    var _rt =  app.tabs.liveChat.report.inherit(app.tabs.liveChat.archive);
    var _super = app.tabs.liveChat.report._super;
    _rt.init = function() {
        var _self = this;
        _super.init.call(this);
        this.quickFilter = _self.tool.find("[name=quickFilter]");
        this.quickFilter.on("change", function() {
            _self.reload();
        });
    }

    _rt.beforeReloadRequest = function (param) {
        var filter = this.quickFilter.val();
        $.extend(param, {quickFilter: filter})
        _super.beforeReloadRequest.call(this, param);
    }
})