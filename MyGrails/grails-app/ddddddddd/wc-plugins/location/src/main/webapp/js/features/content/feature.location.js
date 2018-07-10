app.tabs.location = function() {
    this.constructor_args = arguments;
    this.text = $.i18n.prop("locations");
    this.tip = $.i18n.prop("manage.locations");
    this.ui_class = "location";
    this.ajax_url = app.baseUrl + "locationAdmin/loadLocationAppView";
    app.tabs.location._super.constructor.apply(this, arguments);
}

app.tabs.location.inherit(app.SingleTableTab);

app.ribbons.web_content.push(app.tabs.location.ribbon_data = {
    text: $.i18n.prop("location"),
    ui_class: "location",
    processor: app.tabs.location
});

var _lo = app.tabs.location.prototype;

function getLocationInfo(location) {
    _self = this;
    var places = location.getPlaces();
    var place;
    if(places.length === 0) {
        bm.notify($.i18n.prop("no.location.found"), "error");
        return null;
    }
    place = places[0];
    return place;
}

(function () {
    function attachEvent() {
        var _self = this
        this.on_global("location-restore", function() {
            _self.reload();
        });
        this.body.find(".toolbar .create").on("click", function() {
            _self.createLocation();
        });
        this.body.find("#location-name").on("keyup", function () {
            alert("Search field is empty");
        });
    }
    _lo.init = function(){
        app.tabs.location._super.init.call(this);
        attachEvent.call(this);
    }
})()

_lo.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "remove"
    }
]

_lo.onActionClick = function (action, data) {
    switch (action) {
        case "edit":
            this.editLocation(data.id, data.name);
            break;
        case "remove":
            this.deleteLocation(data.id, data.name);
            break;
    }
};

_lo.createLocation = function (id, name) {
    var _self = this;
    var title = $.i18n.prop("create.location");
    if(id){
        title = $.i18n.prop("edit.location");
    }
    this.renderCreatePanel(app.baseUrl + "locationAdmin/createLocation", title, name, {id: id}, {
        width:710,
        success: function () {
            _self.reload();
        },
        content_loaded: function(form){
            this.countryChange = bm.countryChange(form, {stateName: "state.id"});
            _self.initMap(form);
        }
    });
}

_lo.editLocation = _lo.createLocation

_lo.deleteLocation = function (id, name) {
    var _self = this;
    bm.remove("Location", "location", $.i18n.prop("confirm.delete.location", [name]), app.baseUrl + "locationAdmin/delete", id, {
        success: function () {
            _self.reload();
        }
    });
}

_lo.initMap = function (locationForm) {
    _self = this;
    var loc_address = locationForm.find('#location-address')[0];
    var map = new google.maps.Map(loc_address, {
        zoom: 4,
        center: {lat: -25.2744, lng: 133.7751}
    });

    var loc_name = locationForm.find('#location-name')[0];
    var address = new google.maps.places.SearchBox(loc_name);
    address.addListener('places_changed', function () {
        var loc_info = getLocationInfo(address);
        if(loc_info) {
            loadLocationInfo(loc_info, locationForm);
            setTimeout(loadStateForCountry, 100, locationForm);
        }
    });
}

var searchedCountry;
var loadedStateCode, state;

function loadLocationInfo(locationInfo, locationForm) {
    _self = this;
    var name = locationForm.find("#location-name")[0];
    var address = locationForm.find("#location-address")[0];
    var postCode = locationForm.find("#location-post-code")[0];
    var city = locationForm.find("#location-subrub-city")[0];
    var country = locationForm.find("#countryId");
    var lati = locationForm.find("#lat")[0];
    var longi = locationForm.find("#lng")[0];
    var formattedAddress = locationForm.find("#formatted-address")[0];

    var addressComponents = locationInfo.address_components;
    address.value = locationInfo.formatted_address;

    lati.value = locationInfo.geometry.location.lat();
    longi.value = locationInfo.geometry.location.lng();
    formattedAddress.value = locationInfo.formatted_address;

    addressComponents.forEach(function (st, index) {
        if(st.types[0] === "administrative_area_level_1") {
            loadedStateCode = st.short_name;
        }
    });

    var formatted_address = locationInfo.formatted_address;
    name.value = formatted_address.split(',')[0];

    addressComponents.forEach(function (val, idx) {
        if(val.types[0] === "postal_code") {
            postCode.value = val.long_name;
        } else if(val.types[0] === "administrative_area_level_2" || val.types[0] === "locality") {
            city.value = val.long_name;
        } else if(val.types[0] === "country") {
            bm.ajax({
                async: false,
                url: "/locationAdmin/getCountryByCode",
                data: {countryCode: val.short_name},
                success : function (resp) {
                    searchedCountry = resp.country;
                    if(searchedCountry.length) {
                        searchedCountry = searchedCountry[0];
                        country.val(searchedCountry.id).trigger("chosen:updated").trigger('change');
                    }
                }
            })
        }
    });
}

function loadStateForCountry(locationForm) {
    bm.ajax({
        url: "/locationAdmin/getStateByCode",
        data: {countryId: searchedCountry.id, stateCode: loadedStateCode},
        success : function (loadedState) {
            state = locationForm.find("#state");
            var searchedState = loadedState.state;
            if(searchedState.length) {
                searchedState = searchedState[0];
                state.val(searchedState.id).trigger('chosen:updated');
            }
        }
    })
}

