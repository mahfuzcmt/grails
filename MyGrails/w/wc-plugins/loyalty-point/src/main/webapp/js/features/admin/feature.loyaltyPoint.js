app.tabs.loyaltyPoint = function (configs) {
    this.text = $.i18n.prop("loyalty.point");
    this.tip = $.i18n.prop("manage.loyalty.point");
    this.ui_class = "loyalty-point";
    this.ajax_url = app.baseUrl + "loyaltyPointAdmin/loadReportView";
    app.tabs.loyaltyPoint._super.constructor.apply(this, arguments);
};

app.ribbons.report.push({
    text: $.i18n.prop("loyalty.point"),
    processor: app.tabs.loyaltyPoint,
    ui_class: "loyalty-point",
    ecommerce: true
});

app.tabs.loyaltyPoint.inherit(app.SingleTableTab);

var _lp = app.tabs.loyaltyPoint.prototype;
_lp.advanceSearchUrl = app.baseUrl + "loyaltyPointAdmin/advanceFilter";

(function () {
    function attachEvent() {
        var _self = this;
        _self.on_global("customer-delete", function() {
            _self.reload();
        });
    }

    _lp.init = function () {
        app.tabs.loyaltyPoint._super.init.call(this);
        attachEvent.call(this);
    };
})();

_lp.sortable = {
    list: {
        "0": "fullName",
        "1": "pointCredited",
        "2": "type",
        "3": "created"
    },
    sorted: "3",
    dir: "up"
};