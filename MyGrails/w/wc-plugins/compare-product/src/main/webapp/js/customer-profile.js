$(function() {
    var customerProfileDom = $("#customer-profile-tabs");
    var PANEL;
    customerProfileDom.on("tab:load", function(ev, data) {
        var flag = true;
        switch(data.index) {
            case "overview":
                bm.onReady(window, "compare_product_global_js_loaded", {
                    ready: function () {
                    },
                    not: function () {
                        $("head").append("<script src='" + app.systemResourceUrl + "plugins/compare-product/js/compare-product.js'></script>");
                    }
                })
                break;
        }
    });
})