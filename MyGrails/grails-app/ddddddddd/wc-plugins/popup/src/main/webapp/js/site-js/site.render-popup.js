$(function() {
    var renderPopup = function (config) {
        config.el.css({display: ''})
        var popup = renderSitePopup($.extend(config, {
            is_fixed: true,
            is_always_up: true,
            width: null,
            height: null,
            left: null,
            is_center: false,
            default_left: null,
            animation_clazz: app.config.wc_site_popup_animation_clazz,
            clazz: "wc-site-popup"
        }));
        popup.el.css({left: '', top: ''})
        return popup
    }

    var initPopup = $('.wc-site-popup')
    if(initPopup.length) {
        renderPopup({
            el: initPopup
        })
    }

    $(document).find(".render-site-popup").on("click", function() {
        var $this = $(this), id = $this.attr("identifier"), popup = renderPopup({
            el: $('<div class="wc-site-popup" id="site-popup-body-' + id + '"><div class="loader"><div></div>')
        })
        bm.ajax({
            url: app.baseUrl + "popup/popupContent",
            data: {identifier: id},
            dataType: "html",
            success: function(resp) {
                resp = $(resp)
                popup.el.html(resp.html())
                popup.el.find(".close-popup").on("click", function() {
                    popup.close()
                });
            }
        })
    })
});