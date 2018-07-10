/**
 * Created by shahin on 9/06/2015.
 */
$(function() {
    var sliderPanel = $(".swipebox-container");
    sliderPanel.each(function() {
        var $this = $(this);
        attachSwipeBox($this);
        attachLazyLoad($this);
    })
    function attachSwipeBox(container) {
        container.find('.swipebox').swipebox()
    }

    function attachLazyLoad(container) {
        if(container.is(".lazy-load")) {
            var imgAvailable = true
            var loading = false;
            var widgetId = container.parents(".widget.widget-gallery").attr("widget-id")
            var data = {id: widgetId, offset: 0}
            $(window).scroll(function() {
                if (loading) {
                    return;
                }
                var i = container.offset().top + container.height()
                if ($(window).scrollTop()/* == $(document).height() - $(window).height()*/ + $(window).height() >= i) {
                    loading = true;
                    if (!imgAvailable) {
                        return;
                    }
                    container.append("<div class='scroll-more-loader'><img src=" + app.baseUrl + 'plugins/swipe-box-slider/images/site/ajax-loader.gif' + "></div>");
                    bm.ajax({
                        url: app.baseUrl + 'swipeBoxSlider/lazyLoad',
                        data: data,
                        response: function() {
                            container.find(".scroll-more-loader").remove();
                        },
                        success: function (resp) {
                            var html = $(resp.html);
                            var content = html.find(".box");
                            container.find(".box-container").append(content);
                            data.offset = resp.offset;
                            if(resp.end) {
                                imgAvailable = false
                            }
                            loading = false;
                        },
                        error: function() {
                            imgAvailable = false
                        }
                    });
                }
            });
        }
    }

})