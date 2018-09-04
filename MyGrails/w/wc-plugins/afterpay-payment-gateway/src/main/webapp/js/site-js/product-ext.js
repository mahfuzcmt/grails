(function () {
    app.productWidgets.initAfterPayInfo = function (afterPayInfo) {
        var isActive = false;

        afterPayInfo.find(".learnmore").on("click", function () {
            if (isActive) {
                return;
            }
            popup.addClass("loading")
            popup.popup({
                content: popup,
                is_always_up: true,
                is_fixed: true
            });
            var dom = $(popupDom);
            popup.append(dom)
            popup.find(".popup-image").on("load", function () {
                popup.removeClass("loading").obj(POPUP).position()
            });
            popup.find(".close-button").click(function () {
                popup.obj(POPUP).close();
                isActive = false;
            });
            isActive = true;
            var reload = function () {
                var src = app.baseUrl + afterPayInfo.find(".learnmore").attr("image-path");
                popup.addClass("loading").find(".popup-image").attr("src", src)
            };
            reload();
        });

        var popup = $('<div class="image-popup-container"></div>');
        var popupDom = '<div class="image-popup-mask"></div>' +
            '<span class="image-wrapper">' +
            '<span class="close-button navigator"></span>' +
            '<span class="image-loader"></span>' +
            '<img class="popup-image">' +
            '</span>';
    };
    app.productWidgets.initAfterPayInfo($(".afterpay-installment"));
})();
