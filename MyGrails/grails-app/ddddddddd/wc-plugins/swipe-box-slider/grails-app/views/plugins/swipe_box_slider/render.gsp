<%
    if(!request.is_swipe_slider_loaded) {
        out << "<style>.swipe-image-wrap .box {width: ${100/config.item_per_column.toInteger(1)}%}</style>"
        request.css_cache?.push("plugins/swipe-box-slider/css/swipebox.css")
        out << "<script type='text/javascript' src='${app.systemResourceBaseUrl()}plugins/swipe-box-slider/js/slider/jquery.swipebox.js'></script>"
        out << "<script type='text/javascript' src='${app.systemResourceBaseUrl()}plugins/swipe-box-slider/js/slider/ios-orientationchange-fix.js'></script>"
        request.is_swipe_slider_loaded = true
    }
%>

<g:include view="plugins/swipe_box_slider/swipeBox.gsp"/>