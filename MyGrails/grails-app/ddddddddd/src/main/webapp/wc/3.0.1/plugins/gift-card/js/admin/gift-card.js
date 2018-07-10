$(function() {
    var _s = app.tabs.setting.prototype;
    /*
    * @param data -> {index, $tab, $panel}
    * */
    _s.initGiftCardSettings = function(data) {
        var form = data.panel.find("form");
        this.on_global(["tax-profile-create", "tax-profile-update", "tax-profile-delete"], function(){
            form.find(".tax-profile-selector").each(function(){
                bm.updateDomainSelector($(this), "webcommerce.TaxProfile");
            });
        });
        this.on_global(["discount-profile-create", "discount-profile-update", "discount-profile-delete"], function(){
            form.find(".discount-profile-selector").each(function(){
                bm.updateDomainSelector($(this), "webcommerce.DiscountProfile");
            });
        });
    };

    var _gc =  app.tabs.item.product.prototype;
    _gc.menu_entries.push({
        text: $.i18n.prop("view.purchase.details"),
        ui_class: "purchase-details",
        action: "purchase_details"
    });


    _gc.onMenuOpen = _gc.onMenuOpen.blend(function(navigation){
        var menu = this.tabulator.menu;
        if(!(navigation.attr("product-type") === "gift_card")) {
            menu.find(".menu-item.purchase-details").hide()
        } else {
            menu.find(".menu-item.purchase-details").show()
        }
    });

    var _gci = app.tabs.item.prototype;
    _gci.onMenuOpen = _gci.onMenuOpen.blend(function(navigator, config, navigation){
        var menu;
        if(navigator == "product" || navigator == "combined") {
            menu = this.explorer.menu[navigator];
            if(!(navigation.find(".float-menu-navigator").attr("product-type") === "gift_card")) {
                menu.find(".menu-item.purchase-details").hide()
            } else {
                menu.find(".menu-item.purchase-details").show()
            }
        } else if(navigator == "category") {
        } else {
            menu = this.tabulator.menu;
            config = navigator.config("entity");
            if(config.type == "product") {
                if(!(navigation.find(".float-menu-navigator").attr("product-type") === "gift_card")) {
                    menu.find(".menu-item.purchase-details").hide()
                } else {
                    menu.find(".menu-item.purchase-details").show()
                }
            } else if(config.type == "category"){
                menu.find(".menu-item.purchase-details").hide()
            }
        }
    });

    _gci.actionMenuClick = _gci.actionMenuClick.blend(function (action, data) {
        var _self = this;
        switch (action) {
            case "purchase_details":
                this.viewPurchaseDetails(data.id);
                break;
        }
    });

    _gci.viewPurchaseDetails = function (productId) {
        var tabId = "gc-purchase-details-" + productId,
            tab = app.Tab.getTab(tabId);
        if(!tab) {
            tab = new app.tabs.purchaseDetails({
                id: tabId,
                productId: productId
            });
            tab.render()
        }
        tab.setActive()
    }

    var _or = app.tabs.order.prototype;

    _or.afterTableReload = _or.afterTableReload.blend(function () {
        var _self = this
        var body = _self.body
        body.find(".view-address").on("click", function () {
            _self.addressDetails($(this))
        })
    })

    _or.addressDetails = function ($this) {
        var url = app.baseUrl + "giftCardAdmin/addressToolTip";
        bm.floatingPanel($this, url, {orderItemId: $this.attr("order-item-id")}, 350, null, {
            clazz: "gift-card-address-details"
        })
    };

    app.global_event.on("order-create-variation-selection-init", function (evt, content, config) {
        var form = content.find(".gift-card-fields");
        if(form.length) {
            bm.initCountryChangeHandler(form.find(".country-row select"),  "gift_card.stateId");
            bm.initCityValidator(form.find('[name="gift_card.postCode"]'), "gift_card.countryId", "gift_card.stateId", content, "gift_card.city");
            config.beforeSubmit = function () {
                config.attrs = form.serializeObject();
            }
        }
    });

    app.global_event.on("on-backend-order-create", function(evt, config, popupElement, callBack) {
        if(config.tr.attr("has-variation") == "true" || config.tr.attr("product-type") != "gift_card") {
            return;
        }
        config.return = true;
        bm.editPopup(app.baseUrl + "giftCardAdmin/loadCartSelectionPopup", $.i18n.prop("chose.options"), config.name, {id: config.id}, {
            events: {
                content_loaded: function() {
                    app.global_event.trigger("order-create-variation-selection-init", [this, config]);
                }
            },
            beforeSubmit: function(form, settings, popup) {
                config.beforeSubmit && config.beforeSubmit();
                delete config.beforeSubmit
                callBack(config);
                popup.close();
                return false;
            }
        })

    });
});