app.tabs.eventSession = function(config) {
    this.id = config.id;
    this.eventId = config.eventId;
    this.text = $.i18n.prop('event.session');
    this.tip = $.i18n.prop('manage.event.session');
    this.ui_class = "event-session edit-tab";
    this.ajax_url = app.baseUrl + "eventAdmin/loadSessionAppView?eventId=" + this.eventId;
    app.tabs.eventSession._super.constructor.apply(this, arguments);
}

var _es = app.tabs.eventSession.inherit(app.SingleTableTab)

_es.onMenuOpen = function(navigator) {
    if(navigator.is("tr.disable-venue-invitation span")) {
        this.tabulator.menu.find(".book-venue").addClass("disabled");
    } else {
        this.tabulator.menu.find(".book-venue").removeClass("disabled");
    }
    if(navigator.is("tr.disable-equipment-invitation span")) {
        this.tabulator.menu.find(".book-equipment").addClass("disabled");
    } else {
        this.tabulator.menu.find(".book-equipment").removeClass("disabled");
    }
    if(navigator.is("tr.disable-manage-ticket span")) {
        this.tabulator.menu.find(".menu-item.manage-ticket").addClass("disabled");
    } else {
        this.tabulator.menu.find(".menu-item.manage-ticket").removeClass("disabled");
    }
    if(navigator.is("tr.disable-delete span")) {
        this.tabulator.menu.find(".menu-item.delete").addClass("disabled");
    } else {
        this.tabulator.menu.find(".menu-item.delete").removeClass("disabled");
    }
}

_es.sortable = {
    list: {
        "1": "name",
        "2": "startTime",
        "3": "endTime"
    },
    sorted: "1",
    dir: "up"
}

_es.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit",
        action: "edit"
    },
    {
        text: $.i18n.prop("book.venue"),
        ui_class: "book-venue",
        action: "bookVenueForEventSession"
    },
    {
        text: $.i18n.prop("book.equipment"),
        ui_class: "book-equipment",
        action: "bookEquipmentForEventSession"
    },
    {
        text: $.i18n.prop("manage.tickets"),
        ui_class: "manage-ticket",
        action: "manageTicket"
    },
    {
        text: $.i18n.prop("manage.topic"),
        ui_class: "manage-topic",
        action: "manageTopic"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "remove"
    }
];

_es.onActionClick = function(action, data) {
    switch(action) {
        case "edit":
            this.edit(data.id, data.name);
            break;
        case "bookVenueForEventSession":
            this.bookVenueForEventSession(data.id, data.name);
            break;
        case "bookEquipmentForEventSession":
            this.bookEquipmentForEventSession(data.id, data.name);
            break;
        case "manageTicket":
            this.manageTicket(data.id, data.name)
            break;
        case "manageTopic":
            this.manageTopic(data.id, data.name)
            break;
        case "remove":
            this.deleteSession(data.id, data.name);
            break;
    }
};

_es.onSelectedActionClick = function(action, selecteds) {
    switch(action) {
        case "remove":
            this.deleteSelectedSession(selecteds.collect("id"));
            break;
    }
};

(function() {
    function attachEvents() {
        var _self = this;
        this.on_global("event-session-updated", function (evt) {
            _self.reload();
        });
        this.on_global("event-session-restore", function() {
            _self.reload();
        });
        this.on("close", function () {
            app.tabs.eventSession.tab = null;
        });
        this.on_global("equipment-invitation-request-updated venue-location-invitation-approved", function() {
            _self.reload()
        })
        this.body.find(".toolbar .create").on("click", function() {
           _self.createEventSession();
        });
    }

    _es.init = function () {
        var _self = this;
        app.tabs.eventSession._super.init.call(this);
        app.tabs.eventSession.tab = this;
        attachEvents.call(this);
    }
})();

_es.bookVenueForEventSession = function(id, name) {
    bm.editPopup( app.baseUrl + "eventAdmin/bookVenue", $.i18n.prop("select.venue.location"), name, {sessionId: id}, {
        width: 650,
        success: function() {
            app.global_event.trigger("venue-booked")
        },
        events: {
            content_loaded: function () {
                var content = this;
                content.find('.activate-venue-tab').click(function() {
                    content.find('.close').trigger('click');
                    ComponentManager.openTab(ComponentManager.getRibbonData(app.ribbons.web_marketing, "event"));
                    app.Tab.getTab("tab-event").body.find('.bmui-tab').tabify('option', 'active', 'venue');
                });
                content.find("select.venue-selector").change(function() {
                    var venueId = this.value;
                    var venueName = $("option:selected", this).text();
                    bm.ajax({
                        url: app.baseUrl + "eventAdmin/venueLocationForVenue",
                        data: {venueId: venueId},
                        dataType: "html",
                        success: function(resp){
                            $(content.find(".venue-location")).replaceWith($(resp));
                            content.find(".venue-location").updateUi();
                            content.find('.activate-venue-location-tab').click(function() {
                                content.find('.close').trigger('click');
                                ComponentManager.openTab(ComponentManager.getRibbonData(app.ribbons.web_commerce, "event"));
                                app.tabs.event.Venue.manageLocation(venueId, venueName)
                            });
                        }
                    })
                });
                content.find('select.venue-selector').trigger('change');
            }
        }
    })
}

_es.bookEquipmentForEventSession = function(id, name) {
    bm.editPopup( app.baseUrl + "eventAdmin/bookEquipment", $.i18n.prop("select.equipment"), name, {sessionId: id}, {
        width: 650,
        success: function() {
            app.global_event.trigger("booking-request-sent")
        },
        events: {
            content_loaded: function() {
                var popup = this;
                popup.find('.activate-equipment-tab').click(function() {
                    popup.find('.close').trigger('click');
                    ComponentManager.openTab(ComponentManager.getRibbonData(app.ribbons.web_marketing, "event"));
                    app.Tab.getTab("tab-event").body.find('.bmui-tab').tabify('option', 'active', 'equipment');
                });
            }
        }
    })
}

_es.edit = function(id, name) {
    var data = {id: id, eventId: this.eventId},
        title = $.i18n.prop("edit.event.session");
    if(typeof id == "undefined") {
        data = {eventId: this.eventId};
        title = $.i18n.prop("create.event.session");
    }
    this.renderCreatePanel(app.baseUrl + "eventAdmin/editSession", title, name, data, {
        content_loaded: function() {
            var form = this.find("form.create-edit-form");
            personalizedProgram(form);
            function personalizedProgram(panel) {
                var fileBlock = panel.find(".personalized-file-block");
                panel.find("input[type=file]").on("file-add", function(event, file) {
                    fileBlock = panel.find(".personalized-file-block")
                    var fileName = file.name
                    var itemTemplate = '<div class="personalized-file-block file-selection-queue"><span class="file #EXTENTION#"><span class="tree-icon"></span></span>' +
                        '<span class="name">#FILENAME#</span><span class="tool-icon remove" file-name="#FILENAME#"></span>' +
                        '</div>';
                    if(fileName.length) {
                        var fileName = fileName.split(".")
                        itemTemplate = itemTemplate.replaceAll("#EXTENTION#", fileName[fileName.length-1])
                        fileBlock.replaceWith(itemTemplate.replaceAll("#FILENAME#", fileName.join(".")))
                        panel.find("input[name=file-remove]").remove()
                        attachRemoveEvent(panel.find(".personalized-file-block"))
                    }
                });
                attachRemoveEvent(fileBlock)
                function attachRemoveEvent(fileBlock) {
                    fileBlock.find(".tool-icon.remove").click(function() {
                        $("<input type='hidden' name='file-remove' value='true'>").appendTo(panel)
                        fileBlock.children().remove();
                    })
                }
            }
        },
        success: function() {
            if(app.tabs.eventSession.tab) {
                app.tabs.eventSession.tab.reload();
            }
            app.global_event.trigger("event-session-updated", [id]);
        },
        ajax: {
            data: {eventId: data.eventId}
        }
    });
};

_es.createEventSession = function() {
    this.edit(undefined, "");
};

_es.deleteSession = function(id, name) {
    var _self = this
    bm.remove("eventSession", $.i18n.prop("event.session"), $.i18n.prop("confirm.delete.event.session", [name]), app.baseUrl + "eventAdmin/deleteEventSession", id, {
        is_final: true,
        success: function () {
            _self.reload()
        }
    })
};

_es.deleteSelectedSession = function(ids) {
    var _self = this
    bm.confirm($.i18n.prop("confirm.delete.selected.event.sessions"), function() {
        bm.ajax({
            url: app.baseUrl + "eventAdmin/deleteSelectedEventSessions",
            data: {ids: ids},
            success: function () {
                _self.reload()
                _self.body.find(".action-header").hide();
            }
        })
    },function(){
    });
};

_es.manageTicket = function(id, name) {
    var tabId = "tab-manage-ticket-" + id;
    var tab = app.Tab.getTab(tabId);
    if(!tab) {
        tab = new app.tabs.manageTicket.lastView({
            id: tabId, name: name, eventSessionId: id
        });
        tab.render();
    }
    tab.setActive();
};

_es.manageTopic = function(id, name) {
    var tabId = "tab-manage-topic-" + id;
    var tab = app.Tab.getTab(tabId);
    if(!tab) {
        tab = new app.tabs.manageTopic({
            id: tabId,
            name: name,
            sessionId: id
        });
        tab.render();
    }
    tab.setActive();
};

VALIDATION_RULES.compareSessionWithEvent = {
    check : function(value, ranges) {
        if(!value) {
            return true;
        }
        var sessionTime = new Date(value);
        var eventTime = new Date(ranges[3].trim());
        if(ranges[1].trim() == 'start') {
            if(sessionTime < eventTime) {
                return {msg_template: "Session start time can't be earlier than Event start time(" + ranges[3].trim() + ')'};
            }
            return true;
        }
        else {
            if(sessionTime > eventTime) {
                return {msg_template: "Session end time can't be later than Event end time(" + ranges[3].trim() + ')'};
            }
            return true;
        }
    }
};
