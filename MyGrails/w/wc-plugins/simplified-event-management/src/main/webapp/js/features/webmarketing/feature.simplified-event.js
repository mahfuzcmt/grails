app.tabs.simplifiedEvent = function () {
    this.text = $.i18n.prop("simplified.events");
    this.tip = $.i18n.prop("manage.simplified.events");
    this.ui_class = "simplified-event";
    this.ajax_url = app.baseUrl + "simplifiedEventAdmin/loadAppView";
    app.tabs.simplifiedEvent._super.constructor.apply(this, arguments);
};

app.ribbons.web_commerce.push({
    text: $.i18n.prop("simplified.events"),
    processor: app.tabs.simplifiedEvent,
    ui_class: "simplified-event",
    license: "allow_simplified_event_feature"
});

var _se = app.tabs.simplifiedEvent.inherit(app.MultiTab);
_se.notEditable = true
_se.changeHeader = false

_se.changeTabUrl = function(index, url) {
    this.body.find(".bmui-tab").tabify("reload", index, url)
}

_se.onContentLoad = function(data) {
    var index = data.index;
    var tab = app.tabs.simplifiedEvent[index.capitalize()]
    if (tab) {
        var subTab = new tab(data.panel, this);
        this.tab_objs[index] = subTab;
        subTab.init();
    }
}

app.global_event.on('after-ribbon-render', function(e, ribbonBar) {
    if(app.isPermitted("simplified_event.view.list")) {
        ribbonBar.enable("simplified_event");
    } else {
        ribbonBar.disable("simplified_event");
    }
});