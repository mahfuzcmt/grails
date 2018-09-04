app.widget.GoogleMap = (function () {
    return {
        init: function (widget) {
            bm.onReady(window, "google", function () {
                var container = widget.find(".google-map-container");
                var configs = container.config("map");
                if (container.is(".initialized")) {
                    return;
                }
                container.loader(false)
                var myCenter = new google.maps.LatLng(+configs.lat, +configs.lng);
                var map = new google.maps.Map(container[0], {
                    center: myCenter,
                    zoom: configs.zoom,
                    mapTypeId: 'roadmap'
                });
                if(configs.radius) {
                    var circle = new google.maps.Circle({
                        center: myCenter,
                        radius: configs.radius,
                        strokeColor: "#005b9a",
                        strokeOpacity: 0.5,
                        strokeWeight: 1,
                        fillColor: "#5094f0",
                        fillOpacity: 0.2
                    });
                    circle.setMap(map);
                }
                var marker = new google.maps.Marker({
                    position: myCenter,
                    icon: configs.pin ? configs.pin : undefined
                });
                marker.setMap(map);
                var popupText = widget.find(".popup-text").val()
                if(popupText) {
                    var infoWindow = new google.maps.InfoWindow({
                        content: popupText
                    });
                    marker.addListener('click', function() {
                        infoWindow.open(map, marker);
                    });
                }
                container.addClass("initialized")
            });
        }
    }
})();

$(function () {
    $(".widget-googleMap").each(function () {
        app.widget.GoogleMap.init($(this))
    })
});
