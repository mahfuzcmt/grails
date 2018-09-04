app.tabs.event = function () {
    this.text = $.i18n.prop("events");
    this.tip = $.i18n.prop("manage.events");
    this.ui_class = "event";
    this.ajax_url = app.baseUrl + "eventAdmin/loadAppView";
    app.tabs.event._super.constructor.apply(this, arguments);
};

app.ribbons.web_commerce.push({
    text: $.i18n.prop("event"),
    processor: app.tabs.event,
    ui_class: "event",
    license: "allow_event_feature"
});

var _e = app.tabs.event.inherit(app.MultiTab);
_e.notEditable = true
_e.changeHeader = false

_e.changeTabUrl = function(index, url) {
    this.body.find(".bmui-tab").tabify("reload", index, url)
}

_e.onContentLoad = function(data) {
    var index = data.index;
    var tab = app.tabs.event[index.capitalize()]
    if (tab) {
        var subTab = new tab(data.panel, this);
        this.tab_objs[index] = subTab;
        subTab.init();
    }
}

app.global_event.on('after-ribbon-render', function(e, ribbonBar) {
    if(app.isPermitted("event.view.list")) {
        ribbonBar.enable("event");
    } else {
        ribbonBar.disable("event");
    }
});