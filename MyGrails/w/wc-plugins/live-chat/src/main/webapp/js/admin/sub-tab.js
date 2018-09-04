 bm.onReady(app.tabs, "liveChat", function() {
    app.tabs.liveChat.subTab = function(panel, appTab, ajaxUrl) {
        this.tool = panel.tool
        this.body = panel
        this.appTab = appTab
        this.ajax_url = ajaxUrl
        app.tabs.liveChat.subTab._super.constructor.call(this, arguments)
    }
});
(function() {
    var _st = app.tabs.liveChat.subTab.inherit(app.SingleTableView)
    var _super = app.tabs.liveChat.subTab._super

    _st.init = function() {
        _super.init.apply(this, arguments)
        var _self = this;
        this.tool.find(".reload").click(function() {
            _self.reload()
        })
    }
})()

