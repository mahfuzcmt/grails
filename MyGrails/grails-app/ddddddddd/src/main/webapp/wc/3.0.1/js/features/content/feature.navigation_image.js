app.tabs.navigationImage = function() {
    app.tabs.navigationImage._super.constructor.apply(this, arguments);
    $.extend(this, {
        text: $.i18n.prop("navigationitem.images"),
        name: this.navigation.name,
        tip: $.i18n.prop("manage.navigationitem.images"),
        ui_class: "navigation-item-image-editor",
        ui_body_class: "simple-tab",
        ajax_url: app.baseUrl + "navigation/loadNavigationImageEditor?id=" + this.navigation.id,
        strict_layout : false
    });
}

app.tabs.navigationImage.inherit(app.Tab);

var _en = app.tabs.navigationImage.prototype;

(function(){
    _en.init = function(){
        app.tabs.navigationImage._super.init.call(this);
        this.header.find(".title").append(" - <span class='entity-name'></span>");
        this.header.find(".entity-name").text(this.navigation.name);
    }
})()
