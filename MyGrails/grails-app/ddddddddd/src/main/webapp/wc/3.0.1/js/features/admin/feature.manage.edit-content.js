app.tabs.manageContent = function() {
    app.tabs.manageContent._super.constructor.apply(this, arguments);
    this.constructor_args = arguments;
    this.text = "Manage";
    this.name = "Manage";
    this.tip = "Manage";
    this.ui_class = "my-account-content";
    this.ui_body_class = "manage-my-account-content";
    $.extend(this, {
        ajax_url: "myAccount/loadManageAction"
    });
}

var _mc = app.tabs.manageContent.inherit(app.Tab);

(function () {
    function attachEvents() {
        var _self = this;
    }
    _mc.init = function () {
        app.tabs.manageContent._super.init.call(this);
        attachEvents.call(this)
    }
})();