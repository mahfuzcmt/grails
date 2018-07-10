app.widget.LocationMap = (function () {
    return {
        init: function (widget) {
            bm.onReady(window, "google", function () {
                var container = widget.find(".location-container");
                configs = container.config("location");
                container.loader(false);
                _infowindow = new google.maps.InfoWindow();
                geocoder = new google.maps.Geocoder();

                bm.ajax({
                    url: "/locationPage/getDefaultAddress",
                    success : function (resp) {
                        if(resp.addressFromStoreDetail){
                            geocoder.geocode( {'address' : resp.country}, function(results, status) {
                                if (status == google.maps.GeocoderStatus.OK) {
                                    var mapCenter = results[0].geometry.location;
                                    setMapWithMarkerForDefaultView(resp.location, container, resp.showMarker, mapCenter);
                                }
                            });
                        }else {
                            setMapWithMarkerForDefaultView(resp.location, container, resp.showMarker);
                        }
                    }
                });

                var countryName = "Australia";
                var country = widget.find(".countryChoice");
                var searchField = widget.find("#tags");

                country.on("click", function () {
                    var _self = $(this);
                    countryName = _self[0].text;
                    if(countryName === "AUSTRALIA") {
                        countryName = "Australia";
                    }
                    var activeButton = widget.find(".activeCountry");
                    activeButton.removeClass("activeCountry");
                    _self.addClass("activeCountry");

                    var searchVal = searchField[0].value;
                    bm.ajax({
                        url: "/locationPage/getAddressByName",
                        data: {name: searchVal},
                        success : function (resp) {
                            if(resp.address.length < 1) {
                                searchField[0].value = "";
                            } else {
                                if(countryName === "Australia") {
                                    if(searchVal.indexOf(countryName) === -1) {
                                        searchField[0].value = "";
                                    }
                                } else {
                                    if(searchVal.indexOf("Australia") !== -1) {
                                        searchField[0].value = "";
                                    }
                                }
                            }
                        }
                    });
                    bindAutocomplete(countryName);
                });

                bindAutocomplete(countryName)

                function bindAutocomplete(countryNm) {
                    searchField.autocomplete({
                        serviceUrl: app.baseUrl + "locationPage/autoComplete",
                        type: 'GET',
                        showNoSuggestionNotice: true,
                        noSuggestionNotice: 'No result Found',
                        autoSelectFirst: true,
                        preventBadQueries: true,
                        params: {name: countryNm ? countryNm : 'Australia'}
                    })
                }

                var findBtn = widget.find("#findLocation");
                findBtn.on("click", function () {
                    bm.ajax({
                        url: "/locationPage/getAddressByName",
                        data: {name: searchField[0].value},
                        success : function (resp) {
                            if(resp.address.length > 0) {
                                var loc = resp.address[0];
                                setMapWithMakerForSearchedPlace(loc, 0.00, container);
                            } else {
                                bm.notify($.i18n.prop("no.location.found"), "error");
                            }
                        }
                    })
                })
            });
        }
    }
})();

var _infowindow;
var configs;
var initialRange = 4000;
var normalRange = 10;
var geocoder;

$(function () {
    $(".widget-location").each(function () {
        app.widget.LocationMap.init($(this))
    })
});

var setMapWithMarkerForDefaultView = function (address, container, showMarker, mapCenter) {
    var myCenter;
    if(mapCenter){
        myCenter = mapCenter;
    }else {
        myCenter = new google.maps.LatLng(address.latitude, address.longitude);
    }

    var map = new google.maps.Map(container[0], {
        center: myCenter,
        zoom: 4,
        mapTypeId: 'roadmap'
    });

    if(showMarker === false){
        return;
    }
    var marker = new google.maps.Marker({
        position: myCenter,
        map: map,
        icon: configs.pin ? configs.pin : undefined
    });
    var addrs = address.locationAddress.split(',');

    var html = "<div class='location-info-window' style='max-width: 200px;'>";
    html += "<h4>" + address.name + "</h4>";
    html += "<address>" + address.locationAddress + "<br>" + "<i>" + address.contactEmail + "</i>" + "</address>";
    html += "<div class='location-hours-info'>" + "<h5>" + "Description" +"</h5>" + address.description + "</div>";
    if(address.showWebpageInDetails){
        html += "<div class='webpage-details'>" + "<a href=\"" + address.webpageUrl + "\">" + address.linkText +"</a>";
    }
    html += "</div>";

    google.maps.event.addListener(marker, 'click', function() {
        _infowindow.close();
        _infowindow.setContent(html);
        _infowindow.open(map, this);
    });
    showNearByLocations(address, map, marker, false, initialRange);
}

var setMapWithMakerForSearchedPlace = function (address, distance, localContainer) {
    var myCenter = new google.maps.LatLng(address.latitude, address.longitude);
    var localMap = new google.maps.Map(localContainer[0], {
        center: myCenter,
        zoom: 16,
        mapTypeId: 'roadmap'
    });

    var localMarker = new google.maps.Marker({
        position: myCenter,
        map: localMap,
        icon: configs.pin ? configs.pin : undefined
    });
    localMap.setCenter(myCenter);

    var html = "<div class='location-info-window' style='max-width: 200px;'>";
    html += "<h4>" + address.name + "</h4>";
    html += "<address>" + address.locationAddress + "<br>" + "<i>" + address.contactEmail + "</i>" + "</address>";
    html += "<div class='location-hours-info'>" + "<h5>" + "Description" +"</h5>" + address.description + "</div>";
    if(address.showWebpageInDetails){
        html += "<div class='webpage-details'>" + "<a href=\"" + address.webpageUrl + "\">" + address.linkText +"</a>";
    }
    html += "</div>";
    distance = distance.toFixed(2);
    html += "<br/>" + "<b style='color: orangered;'>" + distance + "</b>";
    html += "<b style='color: orangered;'>" + ' km' + "</b>";

    google.maps.event.addListener(localMarker, 'click', function() {
        _infowindow.close();
        _infowindow.setContent(html);
        _infowindow.open(localMap, this);
    });
    google.maps.event.trigger(localMarker, 'click');

    showNearByLocations(address, localMap, localMarker, true, normalRange);
}

var setMapWithMakerForTenKmsSurroundingPlaces = function(address, distance, localMap) {
    var myCenter = new google.maps.LatLng(address.latitude, address.longitude);
    var mrker = new google.maps.Marker({
        position: myCenter,
        map: localMap,
        icon: configs.pin ? configs.pin : undefined
    });

    var html = "<div class='location-info-window' style='max-width: 200px;'>";
    html += "<h4>" + address.name + "</h4>";
    html += "<address>" + address.locationAddress + "<br>" + "<i>" + address.contactEmail + "</i>" + "</address>";
    html += "<div class='location-hours-info'>" + "<h5>" + "Description" +"</h5>" + address.description + "</div>";
    if(address.showWebpageInDetails){
        html += "<div class='webpage-details'>" + "<a href=\"" + address.webpageUrl + "\">" + address.linkText +"</a>";
    }
    html += "</div>";
    distance = distance.toFixed(2);
    html += "<br/>" + "<b style='color: orangered;'>" + distance + "</b>";
    html += "<b style='color: orangered;'>" + ' km' + "</b>";

    google.maps.event.addListener(mrker, 'click', function() {
        _infowindow.close();
        _infowindow.setContent(html);
        _infowindow.open(localMap, this);
    });

    return mrker;
}

var deg2rad = function (deg) {
    return deg * (Math.PI/180)
}

var getDistanceFromLatLonInKm = function (lat1,lon1,lat2,lon2) {
    var radiusOfEarth = 6371;
    var dLat = deg2rad(lat2-lat1);
    var dLon = deg2rad(lon2-lon1);
    var a =
        Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
        Math.sin(dLon/2) * Math.sin(dLon/2)
    ;
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    var distanceInKilometers = radiusOfEarth * c;
    return distanceInKilometers;
}

var setFitBoundPropertyForLocations = function (localMap, localMarkersArray, localMarker, isToFitBounds) {
    var bounds = new google.maps.LatLngBounds();

    for(var i = 0; i < localMarkersArray.length ; i++) {
        bounds.extend(localMarkersArray[i].getPosition());
    }
    bounds.extend(localMarker.getPosition());
    if(isToFitBounds === true) {
        localMap.fitBounds(bounds);
        localMap.setZoom(localMap.getZoom() - 1);
    }
}

var showNearByLocations = function (adrLoc, localMap, localMarker, isToFitBounds, range) {

    var tempMarker,
        localMarkersArray = [];

    bm.ajax({
        url: "/locationPage/getAddresses",
        success: function (res) {
            var locationsAround = res.locations,
                dist;
            for(var i = 0; i < locationsAround.length; i++) {
                dist = getDistanceFromLatLonInKm(adrLoc.latitude, adrLoc.longitude, locationsAround[i].latitude, locationsAround[i].longitude);
                if(dist <= range && dist !== 0) {
                    tempMarker = setMapWithMakerForTenKmsSurroundingPlaces(locationsAround[i], dist, localMap);
                    localMarkersArray.push(tempMarker);
                }
            }

            if(localMarkersArray.length > 0) {
                setFitBoundPropertyForLocations(localMap, localMarkersArray, localMarker, isToFitBounds);
            }
        }
    })
}

