app.tabs.manageLocation = function() {
    this.constructor_args = arguments;
    this.text = $.i18n.prop("manage.location");
    this.tip = $.i18n.prop("manage.location");
    this.ui_class = "manage-location edit-tab";
    $.extend(this, {
        venueId: this.data.venue.id,
        name: this.data.venue.name,
        ajax_url: this.ajax_url + "?venueId=" + this.data.venue.id
    });
}

var _ml = app.tabs.manageLocation.prototype;

(function () {
    function attachEvents() {
        var _self = this;
        app.tabs.manageLocation.attachToggleCell(_self);
    }
    _ml.init = function () {
        attachEvents.call(this)
    }
})();

app.tabs.manageLocation.attachToggleCell = function(_self) {
    _self.body.find(".toggle-cell").click(function() {
        var $this = $(this)
        var detailsRow = $this.parents("tr").next()
        if($this.is(".collapsed")) {
            $this.removeClass("collapsed").addClass("expanded")
            detailsRow.show()
        } else {
            $this.removeClass("expanded").addClass("collapsed");
            detailsRow.hide()
        }
    })
}

_ml.onSwitchMenuClick = function(type) {
    app.Tab.changeView(this, "manageLocation", type);
    app.tabs.manageLocation.lastView = app.tabs.manageLocation[type]
}

////////////////////////location////////////////////////////

app.tabs.manageLocation.lastView = app.tabs.manageLocation.location = function () {
    app.tabs.manageLocation.location._super.constructor.apply(this, arguments);
}

var _mll = app.tabs.manageLocation.location.inherit(app.SingleTableTab, app.tabs.manageLocation);

(function () {
    function attachEvents() {
        var _self = this;
        this.on_global("booking-request-sent venue-booked venue-location-invitation-approved venue-location-invitation-rejected", function() {
            _self.reload()
        })
        bindInvitationTables(_self)
        this.body.find(".toolbar .create").on("click", function() {
            _self.editLocation();
        });
    }

    _mll.init = function () {
        app.tabs.manageLocation.location._super.init.call(this)
        attachEvents.call(this)
    }
})();

function bindInvitationTables(_self){
    var menu_entries = [
        {
            text: $.i18n.prop("approve"),
            ui_class: "approve",
            action: "approved"
        },
        {
            text: $.i18n.prop("reject"),
            ui_class: "reject",
            action: "rejected"
        }
    ]
    $.each(_self.body.find(".venue-location-invitation-table"), function(ind, elm){
        var _self = this
        this.tabulator = bm.table($(elm), $.extend({
            menu_entries: menu_entries
        }))
        this.tabulator.onMenuOpen = function(navigator) {
            if(navigator.attr("entity-status") == "approved") {
                _self.tabulator.menu.find(".menu-item.approve").addClass("disabled")
                _self.tabulator.menu.find(".menu-item.reject").removeClass("disabled")
            } else if(navigator.attr("entity-status") == "rejected") {
                _self.tabulator.menu.find(".menu-item.approve").removeClass("disabled")
                _self.tabulator.menu.find(".menu-item.reject").addClass("disabled")
            } else {
                _self.tabulator.menu.find(".menu-item.approve").removeClass("disabled")
                _self.tabulator.menu.find(".menu-item.reject").removeClass("disabled")
            }
        }
        this.tabulator.onActionClick = function (action, data) {
            switch (action) {
                case "approved":
                    _mll.approveVenueLocationInvitation(data.id);
                    break;
                case "rejected":
                    _mll.rejectVenueLocationInvitation(data.id);
                    break;
            }
        }
    })
}

_mll.onContentLoad = function() {
    bindInvitationTables(this)
}

_mll.ajax_url = app.baseUrl + "eventAdmin/loadVenueLocations";
_mll.sortable = {
    list: {
        "2": "name"
    },
    dir: "up"
}
_mll.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit",
        action: "edit"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "remove"
    },
    {
        text: $.i18n.prop("view.in.site"),
        ui_class: "preview view",
        action:"view-in-website"
    }
];

_mll.onActionClick = function (action, data) {
    var _self = this;
    switch (action) {
        case "edit":
            _self.editLocation(data.id, data.name);
            break;
        case "remove":
            _self.deleteLocation(data.id, data.name)
            break;
        case "view-in-website":
            _self.viewInSite(data.url)
            break;
    }
}

_mll.onSelectedActionClick = function(action, selectedIds) {
    var _self = this;
    switch (action) {
        case "remove":
            _self.deleteSelectedLocations(selectedIds)
            break;
    }
}

_mll.switch_menu_entries = [
    {
        text: $.i18n.prop("venue.location.section.list"),
        ui_class: "view-switch location-section list-view",
        action: "section"
    },
    {
        text: $.i18n.prop("seat.map.list"),
        ui_class: "view-switch seat-map list-view",
        action: "seatMap"
    }
];


_mll.viewInSite = function (url) {
    var url = app.baseUrl + "venueLocation/" + url + "?adminView=true"
    window.open(url,'_blank');
}

_mll.editLocation = function(id, name) {
    var _self = this,
        title = id ? $.i18n.prop("edit.location") : $.i18n.prop("create.location")
    this.renderCreatePanel(app.baseUrl + "eventAdmin/editLocation", title, name, {id: id, venueId: _self.venueId}, {
        content_loaded: function() {
            var _self = this
            var imageList = _self.find(".location-image-container");
            imageList.find(".remove").on("click", function(){
                var entity = $(this).closest(".image-thumb")
                removeImage(entity);
            });
            function removeImage(entity) {
                var imageId = entity.attr("image-id");
                $("<input type='hidden' name='remove-images' value='" + imageId + "'>").appendTo(entity.closest("form"))
                entity.remove();
                imageList.scrollbar("update", true)
            }
        },
        success : function() {
            _self.reload()
        }
    })
}

_mll.approveVenueLocationInvitation = function(id) {
    bm.ajax({
        url: app.baseUrl + "eventAdmin/approveVenueLocationInvitation",
        data: {id: id},
        success: function () {
            app.global_event.trigger("venue-location-invitation-approved");
        }
    })
}

_mll.rejectVenueLocationInvitation= function(id) {
    bm.ajax({
        url: app.baseUrl + "eventAdmin/rejectVenueLocationInvitation",
        data: {id: id},
        success: function () {
            app.global_event.trigger("venue-location-invitation-rejected");
        }
    })
}

_mll.deleteLocation = function(id, name) {
    var _self = this
    bm.remove("venueLocation", "Venue Location", $.i18n.prop("confirm.delete.location", [name]), app.baseUrl + "eventAdmin/deleteLocation", id, {
        is_final: true,
        success: function () {
            _self.reload();
        }
    })
}

_mll.deleteSelectedLocations = function(ids) {
    var _self = this
    bm.confirm($.i18n.prop("confirm.delete.selected.venue.location"), function() {
        var modifiedIds = []
        ids.forEach(function(element) {
            modifiedIds.push(element.id);
        });
        bm.ajax({
            url: app.baseUrl + "eventAdmin/deleteSelectedVenueLocations",
            data: {ids: modifiedIds},
            success: function () {
                _self.reload()
                _self.body.find(".action-header").hide();
            }
        })
    },function(){
    });
}

_mll.afterTableReload = function() {
    app.tabs.manageLocation.attachToggleCell(this);
    bindInvitationTables(this);
}

////////////////////////////Section//////////////////////////////

app.tabs.manageLocation.section = function () {
    app.tabs.manageLocation.section._super.constructor.apply(this, arguments);
}

var _mls = app.tabs.manageLocation.section.inherit(app.SingleTableTab, app.tabs.manageLocation);
_mls.beforeReloadRequest = function(params) {
    app.tabs.manageLocation.section._super.beforeReloadRequest.apply(this, arguments)
    $.extend(params, {locationId: this.body.find(".location-selector").val()} )
};

(function () {
    function attachEvents() {
        var _self = this;
        this.body.find(".location-selector").change(function(){
            _self.body.find(".search-form").trigger("submit");
        });
        this.body.find(".toolbar .create").on("click", function() {
            _self.editVenueLocationSection();
        })
    }
    _mls.init = function () {
        app.tabs.manageLocation.section._super.init.call(this)
        attachEvents.call(this)
    }
})();

_mls.ajax_url = app.baseUrl + "eventAdmin/loadVenueLocationSections";
_mls.sortable = {
    list: {
        "1": "name",
        "3": "ticketPrice"
    },
    sorted: "1",
    dir: "up"
}
_mls.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit",
        action: "edit"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "delete"
    }
];

_mls.onActionClick = function (action, data) {
    var _self = this;
    switch (action) {
        case "edit":
            _self.editVenueLocationSection(data.id, data.name);
            break;
        case "delete":
            _self.deleteVenueLocationSection(data.id, data.name)
            break
    }
}

_mls.onSelectedActionClick = function(action, selectedIds) {
    var _self = this;
    switch (action) {
        case "remove":
            _self.deleteSelectedVenueLocationSections(selectedIds)
            break;
    }
}

_mls.switch_menu_entries = [
    {
        text: $.i18n.prop("venue.location.list"),
        ui_class: "view-switch location list-view",
        action: "location"
    },
    {
        text: $.i18n.prop("seat.map.list"),
        ui_class: "view-switch seat-map list-view",
        action: "seatMap"
    }
];

_mls.editVenueLocationSection = function(id, name) {
    var _self = this,
        title = id ? $.i18n.prop("edit.location.section") : $.i18n.prop("create.location.section"),
        locationId = this.body.find(".location-selector").val()

    this.renderCreatePanel(app.baseUrl + "eventAdmin/editVenueLocationSection", title, name, {id: id, locationId: locationId}, {
        success : function() {
            _self.reload()
        },
        content_loaded: function(popupObj) {
            var $popup = this;
            $popup.find('.create-location-first').click(function() {
                $popup.find(".toolbar-btn.cancel").trigger("click");
                app.Tab.changeView(_self, "manageLocation", "location");
            });
            $popup.find('select.row-prefix-type').change(function() {
                if($(this).val() == 'alphabetic') {
                    $popup.find('[name=columnPrefixType]').chosen('val', 'numeric');
                } else {
                    $popup.find('[name=columnPrefixType]').chosen('val', 'alphabetic');
                }
            });
            $popup.find('select.column-prefix-type').change(function() {
                if($(this).val() == 'alphabetic') {
                    $popup.find('[name=rowPrefixType]').chosen('val', 'numeric');
                } else {
                    $popup.find('[name=rowPrefixType]').chosen('val', 'alphabetic');
                }
            });
        }
    });
}

_mls.deleteVenueLocationSection = function(id, name) {
    var _self = this;
    bm.remove("venueLocationSection", "Venue Location Section", $.i18n.prop("confirm.delete.section", [name]), app.baseUrl + "eventAdmin/deleteVenueLocationSection", id, {
        success: function () {
            _self.reload();
        }
    })
}

_mls.deleteSelectedVenueLocationSections = function(ids) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.venue.location.sections"), function() {
        var modifiedIds = [];
        ids.forEach(function(element) {
            modifiedIds.push(element.id)
        });
        bm.ajax({
            url: app.baseUrl + "eventAdmin/deleteSelectedVenueLocationSections",
            data: {ids: modifiedIds},
            success: function () {
                _self.reload()
                _self.body.find(".action-header").hide();
            }
        })
    },function(){
    });
}

//////////////////////////// Seat Map ///////////////////////////////

app.tabs.manageLocation.seatMap = function() {
    this.ui_body_class = "simple-tab seat-map-view";
    app.tabs.manageLocation.seatMap._super.constructor.apply(this, arguments);
}

var _sm = app.tabs.manageLocation.seatMap.inherit(app.Tab, app.tabs.manageLocation);

_sm.ajax_url = app.baseUrl + "eventAdmin/loadVenueLocationSeatMap";

_sm.switch_menu_entries = [
    {
        text: $.i18n.prop("venue.location.list"),
        ui_class: "view-switch location list-view",
        action: "location"
    },
    {
        text: $.i18n.prop("venue.location.section.list"),
        ui_class: "view-switch location-section list-view",
        action: "section"
    }
];

(function () {
    function attachEvents() {
        var _self = this;
        this.body.find(".selector").change(function() {
            _self.reload()
        })
        this.body.find(".app-tab-content-container").scrollbar({
            show_horizontal: true,
            vertical: {
                offset: -2
            },
            horizontal: {
                offset: 12
            }

        })
        this.body.find(".reload").click(function() {
            _self.reload()
        })

    }

    _sm.init = function () {
        app.tabs.manageLocation.seatMap._super.init.call(this)
        attachEvents.call(this)
    }

    _sm.reload = function() {
        var _self = this;
        this.body.loader();
        bm.ajax({
            url: app.baseUrl + "eventAdmin/loadVenueLocationSeatMap",
            data: {
                venueId: this.data.venue.id,
                locationId: this.body.find(".location-selector").val(),
                section: _self.body.find("select.section-selector").val()
            },
            dataType: "html",
            response: function() {
                _self.body.loader(false);
            },
            success: function(resp) {
                resp = $(resp)
                _self.body.find(".location-selector-wrapper").html(resp.find(".location-selector-wrapper").html()).find("select").chosen({disable_search: true})
                _self.body.find(".section-selector-wrapper").html(resp.find(".section-selector-wrapper").html()).find("select").chosen({disable_search: true})
                _self.body.find(".app-tab-content-container .section-seat-view").html(resp.filter(".app-tab-content-container").find(".section-seat-view").html())
                resp.find(".toggle-cell")
            }
        })
    }
})();