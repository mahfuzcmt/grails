app.widget.googleMap = function(configs){
    app.widget.googleMap._super.constructor.apply(this, arguments);
};

var _g = app.widget.googleMap.inherit(app.widget.base);
app.widget.googleMap.initShortConfig = function(configContainer, widet, edtor) {
    bm.onReady(window, "google", {
        ready: function() {
            var input = configContainer.find('.pac-input');
            input.on("change", function(ev) {
                ev.originalEvent.ignore = true
            });
            var autocomplete = new google.maps.places.SearchBox(input[0]);
            autocomplete.addListener('places_changed', function() {
                var places = autocomplete.getPlaces(), place;
                if(places.length > 0 && (place = places[0]) && place.geometry) {
                    var location = place.geometry.location;
                    configContainer.find('[name=latitude]').val(location.lat())
                    configContainer.find('[name=longitude]').val(location.lng())
                    configContainer.find('[name=address]').val(input.val()).trigger("change")
                }
            })
        },
        not: function() {
            bm.addScript("https://maps.googleapis.com/maps/api/js?key=" + configContainer.find("[name=api_key]").val() + "&v=3&libraries=places")
        }
    });
    configContainer.find(".reset-pin").on("click", function() {
        configContainer.find("[name=pin_url]").val("").trigger("change")
    })
};

_g.afterContentChange = function(widget, cache, params) {
    var iframeWindow = widget.editor.iframeWindow;
    bm.onReady(iframeWindow.app.widget, "GoogleMap", {
        ready: function() {
            iframeWindow.app.widget.GoogleMap.init(widget.elm)
        },
        not: function() {
            iframeWindow.bm.addScript("https://maps.googleapis.com/maps/api/js?key=AIzaSyAnuT05x7qP92GOezatuNLCEf1F1dlUI60&v=3&libraries=places");
            iframeWindow.bm.addScript(app.systemResourceUrl + "plugins/google-map/js/site-js/google-map.js")
        }
    })
};