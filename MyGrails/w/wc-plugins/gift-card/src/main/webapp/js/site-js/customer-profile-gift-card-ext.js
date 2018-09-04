$(function () {
    app.customerProfile.gift_card = function (giftCardPanel) {
        this.panel = giftCardPanel
    };

    var _g = app.customerProfile.gift_card.prototype;
    _g.giftCardToolTip = function ($this) {
        var url = app.baseUrl + "giftCard/profileToolTip";
        bm.floatingPanel($this, url, {}, 350, null, {
            clazz: "gift-card-tip",
            position_collison: "none"
        })
    };

    _g.checkBalance = function (code) {
        var _self = this;
        var panel = _self.panel;
        panel.loader();
        bm.ajax({
            url: app.baseUrl + "giftCard/checkBalance",
            data: {code: code},
            dataType: "html",
            success: function(resp) {
                $(resp).insertAfter(panel.find(".check-panel"))
            },
            response: function () {
                panel.find(".card-details").remove();
                panel.loader(false);
            },
            error: function(status, message) {
                var errorText = $(status.responseText).text().trim()
                renderMessage(panel, errorText, 'error');
            }
        })
    };

    _g.init = function () {
        var _self = this;
        var panel = _self.panel;
        panel.find(".tool-tip.gift-card").on("click", function () {
            _self.giftCardToolTip($(this))
        });
        panel.find(".check-balance").on("click", function () {
            if(panel.find(".check-panel").valid()) {
                var code = panel.find(".gift-card-code").val();
                _self.checkBalance(code)
            }


        })
    };
    var customerProfileDom = $("#customer-profile-tabs");
    customerProfileDom.on("tab:load", function (ev, data) {
        switch (data.index) {
            case "my-entitlements":
                var giftCard = new app.customerProfile.gift_card(data.panel.find(".gift_card"));
                giftCard.init();
                break;
        }
    })
});


