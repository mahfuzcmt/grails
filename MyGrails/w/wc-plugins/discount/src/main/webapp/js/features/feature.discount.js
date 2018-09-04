app.tabs.discount = function () {
    app.tabs.discount._super.constructor.apply(this, arguments);
    this.text = $.i18n.prop("discount");
    this.tip = $.i18n.prop("manage.discount");
    this.ui_class = "new-discount";
    this.ajax_url = app.baseUrl + "discount/loadAppView";
    this.right_panel_url = "discount/explorerView";
    this.left_panel_url = "discount/leftPanel"
};

app.ribbons.web_commerce.push({
    text: $.i18n.prop("New Discount"),
    processor: app.tabs.discount,
    ui_class: "new-discount",
    license: "allow_discount_feature",
    ecommerce: true
});

app.tabs.discount.inherit(app.TwoPanelExplorerTab);
var _d = app.tabs.discount.prototype;

_d.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit",
        action: "edit"
    },
    {
        text: $.i18n.prop("copy"),
        ui_class: "copy"
    },
    {
        text: $.i18n.prop("coupon.code.history"),
        ui_class: "couponCodeHistory",
        action: "couponCodeHistory"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "delete"
    }

];

_d.onActionClick = function (action, data) {
    switch (action) {
        case "edit" :
            this.loadEditor(data.id, data.name);
            break;
        case "copy":
            this.copy(data.id, data.name);
            break;
        case "couponCodeHistory":
            this.couponCodeHistory(data.id, data.name);
            break;
        case "delete":
            this.delete(data.id, data.name);
            break;
    }
};

_d.init = function () {
    var _self = this;
    app.tabs.discount._super.init.call(this);
    this.body.find(".create-discount").on('click', function () {
        _self.create();
    });
};

_d.initLeftPanel = function (leftPanel) {
    var _self = this;
    app.tabs.discount._super.initLeftPanel.apply(this, arguments);
    this.body.find(".toolbar .create").on('click', function () {
        _self.create()
    });
    bm.instantSearch(leftPanel, leftPanel.find(".type-filter"), ".explorer-item", ".type");
    var couponExport = leftPanel.find(".type-filter a");
    function preventDefault(event) {
        event.preventDefault();
    }
    couponExport.bind("click", preventDefault);
    leftPanel.find(".type-filter .search-text").on("change", function () {
        if($(this).val() == $.i18n.prop("offer.coupon")) {
            leftPanel.find(".type-filter .export").removeAttr("disabled");
            couponExport.unbind("click", preventDefault);
        } else {
            leftPanel.find(".type-filter .export").attr("disabled", "");
            couponExport.bind("click", preventDefault);
        }
    })
};

_d.create = function () {
    var _self = this;
    var type = null;
    _self.loadEditor(null, null, type);

};

_d.loadEditor = function (id, name, type) {
    var _self = this, data = {id: id, type: type};
    this.renderCreatePanel(app.baseUrl + 'discount/editor', $.i18n.prop("discount.and.coupon"), null, data, {
        clazz: "discount-editor",
        content_loaded: function () {
            new app.DiscountEditor(this)
        },
        success: function () {
            _self.reload()
        }
    });
};

_d.copy = function (id, name) {
    var _self = this;
    bm.ajax({
        url: app.baseUrl + "discount/copy",
        data: {id: id},
        success: function () {
            _self.reload();
        }
    })
};

_d.delete = function (id, name) {
    var _self = this;
    bm.remove("discount", "Discount", $.i18n.prop("confirm.delete.discount", [name]), app.baseUrl + "discount/delete", id, {
        success: function () {
            _self.reload();
        }
    })
};

_d.couponCodeHistory = function (id, name) {
    var _self = this;

    var title = "coupon.code.history";

    var tabId = "coupon-code-history-" + id;
    var tab = app.Tab.getTab(tabId);
    if(!tab) {
        tab = new app.tabs.couponCodeHistoryTab({
            id: tabId, name: title, tip: "manage.coupon.code", discountId: id
        });
        tab.render();
    }
    tab.setActive();

};

app.tabs.couponCodeHistoryTab = function(config) {
    this.id = config.id;
    this.discountId = config.discountId;
    this.text = $.i18n.prop(config.name);
    this.tip = $.i18n.prop(config.tip);
    this.ui_class = "coupon-code-history";
    this.ajax_url = app.baseUrl + "coupon/couponCodeHistory?discountId=" + this.discountId;
    app.tabs.couponCodeHistoryTab._super.constructor.apply(this, arguments);
};
app.tabs.couponCodeHistoryTab.inherit(app.SingleTableTab);

var _d_c_c_h = app.tabs.couponCodeHistoryTab.prototype;

_d_c_c_h.menu_entries = [
    {
        text: $.i18n.prop("view"),
        ui_class: "view",
        action: "view"
    },
    {
        text: $.i18n.prop("enable"),
        ui_class: "enable",
        action: "enable"
    },
    {
        text: $.i18n.prop("disable"),
        ui_class: "disable",
        action: "disable"
    }

];

_d_c_c_h.onActionClick = function (action, data) {
    switch (action) {
        case "view" :
            this.couponCodeUsage(data.id);
            break;
        case "enable":
            this.ouponCodeEnable(data.id);
            break;
        case "disable":
            this.ouponCodeDisable(data.id);
            break;
    }
};

_d_c_c_h.init = function () {
    var _self = this;
    app.tabs.couponCodeHistoryTab._super.init.call(this);
};

_d_c_c_h.ouponCodeEnable = function (id) {
    var _self = this;
    bm.ajax({
        url: app.baseUrl + "coupon/enableCode",
        data: {id: id},
        success: function () {
            _self.reload();
        }
    })
};

_d_c_c_h.ouponCodeDisable = function (id) {
    var _self = this;
    bm.ajax({
        url: app.baseUrl + "coupon/disableCode",
        data: {id: id},
        success: function () {
            _self.reload();
        }
    })
};

_d_c_c_h.couponCodeUsage = function (id) {
    var _self = this;
    var title = "total.usages.counter"

    var url = app.baseUrl + "coupon/loadCouponCodeUsages";
    bm.editPopup(url, $.i18n.prop(title), null, {discountCouponCodeId: id}, {
        width: 850,
        success: function () {
        },
        events: {
            content_loaded: function () {
                var content = $(this);
                var bindPaginator = function () {
                    var paginator = content.find(".pagination").obj();
                    if (paginator) {
                        paginator.onPageClick = function (page) {
                            bm.ajax({
                                url: url,
                                data: {
                                    discountCouponCodeId: id,
                                    offset: (page-1) * 10
                                },
                                dataType: "html",
                                success: function (resp) {
                                    $(content.find(".table-view")).html($(resp)).updateUi();
                                    bindPaginator()
                                }
                            })
                        }
                    }

                    var searchBox = content.find(".search-text");
                    content.find(".icon-search").on("click", function() {
                        bm.ajax({
                            url: url,
                            data: {
                                discountCouponCodeId: id,
                                searchText: searchBox.val()
                            },
                            dataType: "html",
                            success: function (resp) {
                                $(content.find(".table-view")).html($(resp)).updateUi();
                                bindPaginator()
                            }
                        })
                    })
                };
                bindPaginator()

            }
        }
    });

};

window.discountCouponPrefixValidation = function (value) {
    return /^[a-zA-Z0-9]+-?$/.test(value)
};


