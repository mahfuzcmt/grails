app.widget.LocationMap = (function () {

    //
    var _infowindow;
    var configs;
    var initialRange = 4000;
    var normalRange = 10;
    var geocoder;
    var defaultViewMapZoom = 4;
    var searchViewMapZoom = 16;
    var mapType = "roadmap";

    $(function () {
        $(".widget-location").each(function () {
            app.widget.LocationMap.init($(this))
        })
    });

    var setMapWithMarkerForDefaultView = function (address, container, showMarker, mapCenter) {
        var myCenter;
        if(mapCenter){
            myCenter = mapCenter;
        } else {
            myCenter = new google.maps.LatLng(address.latitude, address.longitude);
        }

        var map = new google.maps.Map(container[0], {
            center: myCenter,
            zoom: defaultViewMapZoom,
            mapTypeId: mapType
        });

        if(showMarker === false){
            return;
        }
        var marker = new google.maps.Marker({
            position: myCenter,
            map: map,
            icon: configs.pin ? configs.pin : undefined
        });

        var htmlDom = getMarkerDom(address, false);
        addMapEventListener(marker, htmlDom, map);
        showNearByLocations(address, map, marker, false, initialRange);
    }

    var setMapWithMakerForSearchedPlace = function (address, distance, localContainer) {
        var myCenter = new google.maps.LatLng(address.latitude, address.longitude);
        var localMap = new google.maps.Map(localContainer[0], {
            center: myCenter,
            zoom: searchViewMapZoom,
            mapTypeId: mapType
        });

        var localMarker = new google.maps.Marker({
            position: myCenter,
            map: localMap,
            icon: configs.pin ? configs.pin : undefined
        });
        localMap.setCenter(myCenter);

        var htmlDom = getMarkerDom(address, false, distance);
        addMapEventListener(localMarker, htmlDom, localMap);
        google.maps.event.trigger(localMarker, 'click');

        showNearByLocations(address, localMap, localMarker, true, normalRange);
    }

    var setMapWithMakerForTenKmsSurroundingPlaces = function(address, distance, localMap) {
        var myCenter = new google.maps.LatLng(address.latitude, address.longitude);
        var marker = new google.maps.Marker({
            position: myCenter,
            map: localMap,
            icon: configs.pin ? configs.pin : undefined
        });

        var htmlDom = getMarkerDom(address, false, distance);
        addMapEventListener(marker, htmlDom, localMap);
        return marker;
    }

    var getMarkerDom = function(address, isToShowDistance, distance){
        var dom = "<div class='location-info-window'>";
        dom += "<div class='location-name'>" + "<h4>" + (address.locationHeadingName || address.name) + "</h4>" + "</div>";
        dom += "<div class='location-address'>" + "<address>" + address.locationAddress + "</address>" + "</div>";
        dom += "<br>";
        if(address.showEmailInDetails) {
            dom += "<div class='location-email'>" + "<a href=\"mailto:" + address.contactEmail + "\" target=\"_top\">" + "E: " + address.contactEmail + "</a>" + "</div>";
        }
        if(address.showPhoneNumberInDetails){
            dom += "<div class='location-phone'>" + "<a href=\"tel:" + address.phoneNumber + "\">" + "T: " + address.phoneNumber +"</a>" + "</div>";
        }
        if(address.description) {
            dom += "<br>";
            dom += "<div class='location-hours-info'>" + address.description + "</div>";
        }
        if(address.showWebpageInDetails){
            dom += "<br>";
            dom += "<div class='location-webpage'>" + "<a href=\"" + address.webpageUrl + "\" target=\"_blank\">" + address.linkText +"</a>" + "</div>";
        }
        if(isToShowDistance === true && distance !== 0){
            dom += "<br>";
            dom += "<div class='location-distance'>" + "<span>" + distance + " km" + "</span>" + "</div>";
        }
        dom += "</div>";
        return dom;
    }

    var addMapEventListener = function(marker, html, map){
        google.maps.event.addListener(marker, 'click', function() {
            _infowindow.close();
            _infowindow.setContent(html);
            _infowindow.open(map, this);
        });
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
        return formatNumber(distanceInKilometers)
    }

    var formatNumber = function (number){
        var _number = parseFloat(number.toFixed(1));
        if(_number % 1 === 0){
            return Math.round(_number);
        }
        return _number;
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

        var tempMarker;
        var localMarkersArray = [];

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
    //
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

