/**
 * Created by sajedur on 29/10/2014.
 */
 bm.onReady(app.tabs, "liveChat", function() {
    app.tabs.liveChat.tabInitFunctions.agent = function(panel) {
        var editor = new app.tabs.liveChat.agent(panel, this)
        editor.init()
    }
})

app.tabs.liveChat.agent = function(panel, appTab) {
    this.tool = panel.tool
    this.body = panel
    this.appTab = appTab
    this.ajax_url = app.baseUrl + "liveChatAdmin/loadAgent"
    app.tabs.liveChat.agent._super.constructor.call(this, arguments)
};

(function() {
    var _at = app.tabs.liveChat.agent.inherit(app.SingleTableView)
    var _super = app.tabs.liveChat.agent._super

    _at.init = function() {
        _super.init.apply(this, arguments)
        var _self = this;
        this.tool.find(".reload").click(function() {
            _self.reload()
        })
        _self.attachEvents();
    }


    _at.sortable = {
        list: {
            "0": "name"
        },
        sorted: "0",
        dir: "up"
    }

})()