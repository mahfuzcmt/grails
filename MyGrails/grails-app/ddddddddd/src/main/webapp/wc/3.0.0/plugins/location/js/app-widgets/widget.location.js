app.widget.location = function(configs){
    app.widget.location._super.constructor.apply(this, arguments);
};


var _l = app.widget.location.inherit(app.widget.base);
app.widget.location.initShortConfig = function(configContainer, widet, edtor) {
    bm.onReady(window, "google", {
        ready: function() {
            var input = configContainer.find('#tags');
        },
        not: function() {
            bm.addScript("https://maps.googleapis.com/maps/api/js?key=" + configContainer.find("[name=api_key]").val() + "&v=3&libraries=places");
        }
    });
    configContainer.find(".reset-pin").on("click", function() {
        configContainer.find("[name=pin_url]").val("").trigger("change")
    })
};


_l.afterContentChange = function(widget, cache, params) {
    var iframeWindow = widget.editor.iframeWindow;
    bm.onReady(iframeWindow.app.widget, "LocationMap", {
        ready: function() {
            iframeWindow.app.widget.LocationMap.init(widget.elm);
            loadDropdown();
        },
        not: function() {
            iframeWindow.bm.addScript("https://maps.googleapis.com/maps/api/js?key=AIzaSyAnuT05x7qP92GOezatuNLCEf1F1dlUI60&v=3&libraries=places");
            iframeWindow.bm.addScript(app.systemResourceUrl + "plugins/location/js/site-js/location-map.js");
        }
    })
};

var loadDropdown = function () {
    $(".widget-location").each(function () {
        app.widget.LocationMap.init($(this));
    })
}