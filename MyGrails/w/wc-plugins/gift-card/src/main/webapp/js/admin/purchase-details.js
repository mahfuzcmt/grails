app.tabs.purchaseDetails = function (configs) {
    this.productId = configs.productId
    this.constructor_args = arguments;
    this.text = $.i18n.prop("gift.card");
    this.tip = $.i18n.prop("purchase.details");
    this.ui_class = "gift_card_purchase_details";
    this.ajax_url = app.baseUrl + "giftCardAdmin/loadPurchaseDetails?productId=" + this.productId;
    app.tabs.purchaseDetails._super.constructor.apply(this, arguments);
};
var _pd = app.tabs.purchaseDetails.inherit(app.SingleTableTab)
_pd.sortable = {
    list: {
        "7": "created",
        "8": "availableTo"
    },
    sorted: "7",
    dir: "down"
};

_pd.menu_entries = [
    {
        text: $.i18n.prop("adjust.gift.card.amount"),
        ui_class: "adjust-amount",
        action: "adjust_amount"
    },
    {
        text: $.i18n.prop("activate.deactivate"),
        ui_class: "change-status",
        action: "change_status"
    }
];

_pd.onActionClick = function (action, data) {
    switch (action) {
        case "adjust_amount":
            this.adjustAmount(data.id, data.name);
            break;
        case "change_status":
            this.changeStatus(data.id, data.name);
            break;
    }
};

_pd.adjustAmount = function (id) {
    var _self = this
    bm.editPopup(app.baseUrl + "giftCardAdmin/adjustAmountPopup", $.i18n.prop("adjust.gift.card.amount"), null, {id: id}, {
        success: function () {
            _self.reload();
        }
    })
};

_pd.delete= function (id, name) {
    var _self = this;
    bm.remove("giftCard", "GiftCard", $.i18n.prop("confirm.delete", [$.i18n.prop("gift.card")]), app.baseUrl + "giftCardAdmin/delete", id, {
        success: function () {
            _self.reload();
        }
    });
};

_pd.changeStatus = function (id) {
    var _self = this;
    bm.ajax({
        url: app.baseUrl + "giftCardAdmin/changeStatus",
        data: {id: id},
        success: function () {
            _self.reload();
        }
    })
};
