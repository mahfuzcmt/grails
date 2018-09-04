$(function () {
    function initAutoLoad(container) {
        var loading = false;
        var urlPrefix = container.find(".product-sorting").attr("urlprefix")
        var url = bm.path(document.location.href)
        var widget = container.parents("div.widget-product")
        var category = container.parents("div.category-details")
        var requestData = {}
        if (widget.length) {
            requestData.type = "productWidget"
            requestData.id = widget.attr("widget-id")
            requestData.sort = url.query[urlPrefix + "-sort"]
        } else if (category.length) {
            requestData.type = "category"
            requestData.url = url.name
            requestData.sort = url.query[urlPrefix + "-sort"]
        }
        var prdAvailable = true
        $(window).scroll(function() {
            if (loading) {
                return;
            }
            var i = container.offset().top + container.height()
            if ($(window).scrollTop() + $(window).height() >= i) {
                if (!prdAvailable) {
                    return;
                }
                loading = true;
                container.append("<div class='scroll-more-loader'><img src=" + app.baseUrl + 'plugins/auto-product-load-on-scroll/images/site/ajax-loader.gif' + "></div>");
                bm.ajax({
                    url: app.baseUrl + 'productScroll/loadProductScrollView/',
                    data: requestData,
                    dataType: 'json',
                    success: function (response) {
                        var html = $(response.html)
                        var content = container.find("div.content")
                        if (response.displayType == "list") {
                            content.find("tbody").append(html.find("tbody").html())
                        } else {
                            content.append(html)
                        }
                        bindAddToCartClickEvent(html)
                        app.global_event.trigger("new-product-block-added", [html])
                        requestData.offset = response.offset
                        requestData.max = response.max
                        if(response.end == "0") {
                            prdAvailable = false
                        }
                    },
                    complete: function() {
                        loading = false;
                        container.find(".scroll-more-loader").remove();
                    },
                    error: function() {
                        prdAvailable = false
                    }
                });
            }
        });
    }

    $(".auto_scroll_product").each(function() {
        initAutoLoad($(this))
    })
});