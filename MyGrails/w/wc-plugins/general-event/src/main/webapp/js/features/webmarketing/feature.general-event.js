app.tabs.generalEvent = function() {
    this.constructor_args = arguments;
    this.text = $.i18n.prop('general.event');
    this.tip = $.i18n.prop('general.event.management');
    this.ui_class = 'general-event';
    app.tabs.generalEvent._super.constructor.apply(this, arguments);
};

var _ge = app.tabs.generalEvent.inherit(app.SingleTableTab);

/////////////////////////////////////////////// EVENT SECTION //////////////////////////////////////////////////////////

app.tabs.generalEvent.event = function() {
    app.tabs.generalEvent.event._super.constructor.apply(this, arguments);
};

app.ribbons.web_commerce.push(app.tabs.generalEvent.ribbon_data = {
    text: $.i18n.prop('general.event'),
    ui_class: 'general-event',
    processor: app.tabs.generalEvent.event,
    license: 'allow_general_event_feature'
});

(function(prototype) {
    prototype = app.tabs.generalEvent.event.inherit(app.tabs.generalEvent);

    prototype.ajax_url = app.baseUrl + 'generalEventAdmin/loadEventAppView';
    prototype.advanceSearchUrl = app.baseUrl + "generalEventAdmin/advanceFilter";
    prototype.advanceSearchTitle = $.i18n.prop("event");

    (function () {
        var attachEvents = function() {
            var _self = this;

            this.on_global("event-create event-update delete-event", function() {
                _self.reload();
            });

            this.sortable = {
                list: {
                    "1": "name",
                    "2": "startTime",
                    "3": "endTime"
                },
                sorted: "1",
                dir: "up"
            };

            _self.body.find(".toolbar .create").on("click", function() {
                _self.createEvent();
            });

            this.on("close", function () {
                app.tabs.content.tab = null;
            });

            _self.attachToggleCell();
            _self.bindRecurringEventsTable.call(this);
        };

        prototype.init = function () {
            app.tabs.generalEvent.event._super.init.call(this);
            app.tabs.generalEvent.event.tab = this;
            attachEvents.call(this);
        };
    })();

    prototype.switch_menu_entries = [
        {
            text: $.i18n.prop('calendar'),
            ui_class: 'view-switch calendar',
            action: 'calendar'
        },
        {
            text: $.i18n.prop('venue'),
            ui_class: 'view-switch venue',
            action: 'venue'
        },
        {
            text: $.i18n.prop('equipment'),
            ui_class: 'view-switch equipment',
            action: 'equipment'
        }
    ];

    prototype.menu_entries = [
        {
            text: $.i18n.prop('edit'),
            ui_class: 'edit',
            action: 'editEvent'
        },
        {
            text: $.i18n.prop('view.in.site'),
            ui_class: 'preview view',
            action: 'view-in-site'
        },
        {
            text: $.i18n.prop("custom.field.edit"),
            ui_class: "custom-field edit",
            action: "custom-field"
        },
        {
            text: $.i18n.prop("attendee.details"),
            ui_class: "attendee-details details",
            action: "attendeeDetails"
        },
        {
            text: $.i18n.prop('remove'),
            ui_class: 'delete',
            action: 'deleteEvent'
        }
    ];

    prototype.onActionClick = function(action, data) {
        switch(action) {
            case 'editEvent':
                this.editEvent(data.id, data.name);
                break;
            case "view-in-site":
                this.viewInSite(data.id)
                break;
            case "custom-field":
                this.customField(data.id, data.name)
                break;
            case "attendeeDetails":
                this.attendeeDetails(data)
                break;
            case 'deleteEvent':
                this.deleteEvent(data.id, data.name);
                break;
        }
    };

    prototype.bindRecurringEventsTable = function() {
        var _self = this
        var menu_entries = [
            {
                text: $.i18n.prop("view.in.site"),
                ui_class: "preview view",
                action: "view-recurring-event-in-site"
            },
            {
                text: $.i18n.prop("attendee.details"),
                ui_class: "attendee-details view",
                action: "attendeeDetails"
            },
        ];
        $.each(_self.body.find(".recurring-event-table"), function(ind, elm) {
            var _inner = this
            this.tabulator = bm.table($(elm), $.extend({
                menu_entries: menu_entries
            }))
            this.tabulator.onActionClick = function (action, data) {
                switch (action) {
                    case "view-recurring-event-in-site":
                        _self.viewRecurringEventInSite(data);
                        break;
                    case "attendeeDetails":
                        _self.attendeeDetails(data)
                        break;
                }
            }
        })
    };

    prototype.createEvent = prototype.editEvent = function(id, name, tab) {
        var _superSelf = this
        var data = {id: id},
            title = $.i18n.prop("edit.event"),
            _self = this;

        if (typeof id == "undefined") {
            data = {};
            name = "";
            title = $.i18n.prop("create.event");
        }
        if(!tab) {
            tab = _self;
        }
        tab.renderCreatePanel(app.baseUrl + "generalEventAdmin/editEvent", title, name, data, {
            success: function () {
                if(id) {
                    app.global_event.trigger("event-update", [id]);
                } else {
                    app.global_event.trigger("event-create");
                }
            },
            content_loaded: function() {
                var _self = this
                bm.metaTagEditor(_self.find("#bmui-tab-metatag"));
                var form = this.find("form.create-edit-form");

                //////////////////////////////// ADDING PERSONALIZED PROGRAM TO EVENT SECTION //////////////////////////////
                var personalizedProgram = function(panel) {
                    var fileBlock = panel.find(".personalized-file-block");
                    panel.find("input[name=file]").on("file-add", function(event, file) {
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
                };
                personalizedProgram(form);

                ///////////////////////////// ADDING CUSTOMER & CUSTOMER GROUP TO EVENT SECTION ////////////////////////////
                form.find(".select-customer").on("click", function(){
                    bm.customerAndGroupSelectionPopup(form, {})
                });

                ////////////////////////////////// ADDING IMAGES TO EVENT SECTION //////////////////////////////////////////
                var removeImage = function(entity) {
                    var imageId = entity.attr("image-id");
                    $("<input type='hidden' name='remove-images' value='" + imageId + "'>").appendTo(entity.closest("form"))
                    entity.trigger("change").remove();
                    imageList.scrollbar("update", true)
                };

                var imageList = _self.find(".event-image-container");
                imageList.find(".remove").on("click", function(){
                    var entity = $(this).closest(".image-thumb")
                    removeImage(entity);
                });

                imageList = imageList.find(".one-line-scroll-content").scrollbar({
                    show_vertical: false,
                    show_horizontal: true,
                    use_bar: false,
                    visible_on: "auto",
                    horizontal: {
                        handle: {
                            left: imageList.find(".left-scroller"),
                            right: imageList.find(".right-scroller")
                        }
                    }
                });

                $(window).on("resize." + this.id, function() {
                    imageList.scrollbar("update", true)
                });

                imageList.sortable({
                    axis: "x",
                    placeholder: true,
                    handle: ".image-thumb",
                    containment: "parent",
                    stop: function() {
                        imageList.trigger("change")
                    }
                });

                ////////////////////////////////// ADDING EQUIPMENT TO EVENT SECTION ///////////////////////////////////////
                var equipmentForm = _self.find(".form-row-add-equipment");
                equipmentForm.find(".add-equipment-btn").on("click", function() {
                    _superSelf.addEquipment($(this), form, equipmentTable);
                });
                var equipmentTable = _self.find(".equipment-table-container");
                attachEquipmentTableEvents();
                function attachEquipmentTableEvents() {
                    bm.menu([
                        {
                            text: $.i18n.prop("remove"),
                            ui_class: "remove",
                            action: "removeEquipment"
                        }
                    ], equipmentTable, ".action-navigator", {
                        click: function (action, entity) {
                            switch (action) {
                                case "removeEquipment":
                                    equipmentTable.find('.equipment-item-container').find('.row.old-row').remove();
                                    equipmentTable.find('.equipment-item-container').prepend('<input type="hidden" name="oldEquipmentRemoved" value="true">');
                                    break;
                            }
                        }
                    }, "click");
                    equipmentTable.on("click", ".remove-new-equipment-panel", function() {
                        $(this).parents(".row.new-row").remove();
                    })
                }

                ////////////////////////////////// ADDING VENUE TO EVENT SECTION ///////////////////////////////////////////
                var venueForm = _self.find(".form-row-venue-1");
                venueForm.find(".add-venue-btn").on("click", function() {
                    _superSelf.addVenue($(this), form, venueTable);
                });
                var venueTable = _self.find(".venue-table-container");
                attachVenueTableEvents();
                function attachVenueTableEvents() {
                    bm.menu([
                        {
                            text: $.i18n.prop("remove"),
                            ui_class: "remove",
                            action: "removeVenue"
                        }
                    ], venueTable, ".action-navigator", {
                        click: function (action, entity) {
                            switch (action) {
                                case "removeVenue":
                                    venueTable.find('.venue-item-container').find('.row.old-row').remove();
                                    venueTable.find('.venue-item-container').prepend('<input type="hidden" name="oldVenueRemoved" value="true">');
                                    break;
                            }
                        }
                    }, "click");
                    venueTable.on("click", ".remove-new-venue-panel", function() {
                        $(this).parents(".row.new-row").remove();
                    });
                }
            }
        });
    };

    prototype.addEquipment = function($this, form, equipmentTable) {
        var _self = this
        var id = form.find('[name="id"]').val();
        var data = {eventId: id}
        var url = "generalEventAdmin/addEquipmentView";
        bm.floatingPanel($this, app.baseUrl + url, data, 900, null, {
            clazz: "add-equipment-popup event",
            events: {
                content_loaded: function(popup) {
                    var element = popup.el;
                    element.updateUi();
                    var equipmentId, equipmentName;
                    element.find("select.equipment-selector").change(function() {
                        equipmentId = this.value;
                        equipmentName = $("option:selected", this).text();
                    });
                    element.find("select.equipment-selector").trigger('change');
                    element.find(".cancel-button").on("click", function() {
                        popup.close();
                    });
                    element.find(".add-equipment-btn").on("click", function() {
                        popup.close();
                        addEquipmentValue(equipmentId, equipmentName);
                    });
                    element.find('.switch-to-equipment-tab').on('click', function() {
                        $(document).find('.create-panel').remove();
                        popup.close();
                        var type = 'equipment'
                        _self.onSwitchMenuClick(type);
                    });
                }
            },
            position_collison: "none"
        });
        function addEquipmentValue(equipmentId, text) {
            var newTd = equipmentTable.find(".add-new-equipment");
            if(newTd.length) {
                newTd.remove()
            }
            var template = '<div class="row new-row"><div class="item-row equipment-row add-new-equipment"  style="position: relative;"><input type="hidden" name="equipment" class="large" value='+ equipmentId + '>'+ '<span class="value">' + text + '</span>' +
                "<div class='column remove-column'><span class='tool-icon remove-new-equipment-panel remove'></span></div></div></div>";
            equipmentTable.find('.row.new-row').remove();
            equipmentTable.find(".equipment-item-container").prepend(template);
            form.animate({scrollTop : 0}, "slow");
            bm.highlight(equipmentTable.find(".add-new-equipment"), 1000*5);
        };
    };

    prototype.addVenue = function($this, form, venueTable) {
        var _self = this
        var id = form.find('[name="id"]').val();
        var data = {eventId: id}
        var url = "generalEventAdmin/addVenueView";
        bm.floatingPanel($this, app.baseUrl + url, data, 900, null, {
            clazz: "add-venue-popup event",
            events: {
                content_loaded: function(popup) {
                    var element = popup.el;
                    element.updateUi();
                    element.find(".cancel-button").on("click", function() {
                        popup.close();
                    });
                    element.find('.switch-to-venue-tab, .switch-to-venue-location-tab').on('click', function() {
                        $(document).find('.create-panel').remove();
                        popup.close();
                        var type = 'venue'
                        _self.onSwitchMenuClick(type);
                    });
                    var venueId, venueName, locationId, locationName;
                    element.find("select.venue-selector").change(function() {
                        venueId = this.value;
                        venueName = $("option:selected", this).text();
                        bm.ajax({
                            url: app.baseUrl + "generalEventAdmin/venueLocationForVenue",
                            data: {venueId: venueId},
                            dataType: "html",
                            success: function(resp){
                                $(element.find(".venue-location")).replaceWith($(resp));
                                element.find(".venue-location").updateUi();
                                element.find('.switch-to-venue-location-tab').click(function() {
                                    $(document).find('.create-panel').remove();
                                    popup.close();
                                    var type = 'venue'
                                    _self.onSwitchMenuClick(type);
                                });
                                element.find("select.location-selector").change(function() {
                                    locationId = this.value;
                                    locationName = $("option:selected", this).text();
                                }).trigger('change');
                                element.find('span.link').hasClass('switch-to-venue-location-tab') ? element.find(".add-venue-btn").attr("disabled", true) : element.find(".add-venue-btn").attr("disabled", false);
                            }
                        });
                    }).trigger('change');

                    element.find(".add-venue-btn").on("click", function() {
                        popup.close();
                        addVenueValue(locationId, locationName, venueName);
                    });
                }
            },
            position_collison: "none"
        });
        function addVenueValue(locationId, locationName, venueName) {
            var newTd = venueTable.find(".add-new-venue");
            if(newTd.length) {
                newTd.remove()
            }
            var template = '<div class="row new-row"><div class="item-row venue-row add-new-venue"  style="position: relative;"><input type="hidden" name="location" class="large" value='+
                locationId + '>' + '<span class="new-venue">' + '<span class="label">' + 'Venue: ' + '</span>' + '<span class="value">' + venueName + '</span>' + '</span>' + '<span class="new-location">' + '<span class="label">' + 'Location: ' + '</span>' + '<span class="value">' + locationName + '</span>' + '</span>' + "<div class='column remove-column'><span class='tool-icon remove-new-venue-panel remove'></span></div></div></div>";
            venueTable.find('.row.new-row').remove();
            venueTable.find(".venue-item-container").prepend(template);
            form.animate({scrollTop : 0}, "slow");
            bm.highlight(venueTable.find(".add-new-venue"), 1000*5);
        };
    };

    prototype.viewInSite = function(id){
        var url = app.siteBaseUrl + "generalEvent/" + id +"?adminView=true"
        window.open(url);
    };

    prototype.viewRecurringEventInSite = function (data) {
        var url = app.siteBaseUrl + "generalEvent/" + data.id + '?isRecurring=' + data.isrecurring +"&adminView=true"
        window.open(url);
    };

    prototype.customField = function(id, name) {
        var tabId = "event-custom-field" + id;
        var tab = app.Tab.getTab(tabId);
        if(!tab) {
            tab = new app.tabs.generalEventCustomField({
                id:  tabId, name: name, eventId: id
            });
            tab.render();
        }
        tab.setActive();
    };

    prototype.attendeeDetails = function(data) {
        if(data.haschild) {
            bm.notify('Please look into the child events', 'error');
            return
        }
        var tabId = "event-attendee-details" + data.id;
        var tab = app.Tab.getTab(tabId);
        if(!tab) {
            tab = new app.tabs.attendeeDetailsTab({
                id: tabId, name: data.name, eventId: data.id, isRecurring: data.isrecurring
            });
            tab.render();
        }
        tab.setActive();
    };

    prototype.attachToggleCell = function() {
        this.body.find(".toggle-cell").click(function() {
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
    };

    prototype.afterTableReload = function() {
        this.attachToggleCell();
        this.bindRecurringEventsTable.call(this);
    };

    prototype.deleteEvent = function(id, name) {
        bm.remove("general-event", $.i18n.prop("event"), $.i18n.prop("confirm.delete.event", [name]), app.baseUrl + "generalEventAdmin/deleteEvent", id, {
            is_final: true,
            success: function () {
                app.global_event.trigger("delete-event", [id]);
            }
        });
    };

    prototype.deleteSelectedEvents = function (ids) {
        bm.confirm($.i18n.prop("confirm.delete.selected.event"), function() {
            bm.ajax({
                url: app.baseUrl + "generalEventAdmin/deleteSelectedEvents",
                data: {ids: ids},
                success: function () {
                    app.global_event.trigger("delete-event", [ids]);
                }
            });
        },function(){
        });
    };

    prototype.onSelectedActionClick = function(action, selecteds) {
        switch(action) {
            case "remove":
                this.deleteSelectedEvents(selecteds.collect("id"));
                break;
        }
    };

    prototype.onSwitchMenuClick = function(type) {
        app.Tab.changeView(this, "generalEvent", type, type == "explorer" ? "ExplorerPanelTab" : "SingleTableTab");
    };
}(undefined));


VALIDATION_RULES.dateTimeCheck = {
    check : function(date, ranges) {
        if(!date) {
            return true;
        }
        var monthWith30 = [3, 5, 8, 10]
        var month = '#' + ranges[0]
        month = $($.find(month)).val()
        if(date > 31) {
            return {msg_template: "Invalid Date!"};
        }
        else if(month == 1 && date > 28) {
            return {msg_template: "Invalid Date!"};
        }else if(monthWith30.indexOf(parseInt(month)) > -1 && date > 30) {
            return {msg_template: "Invalid Date!"};
        }
        return true
    }
};

VALIDATION_RULES.dayCheck = {
    check : function(day) {
        if(!day || (day <= 31 && day >= 1)) {
            return true;
        }else{
            return {msg_template: "Invalid Date!"};
        }
    }
};


//////////////////////////////////////////////// CALENDAR SECTION //////////////////////////////////////////////////////

app.tabs.generalEvent.calendar = function() {
    app.tabs.generalEvent.calendar._super.constructor.apply(this, arguments)
};

(function(prototype) {

    prototype = app.tabs.generalEvent.calendar.inherit(app.tabs.generalEvent);

    prototype.ajax_url = app.baseUrl + "generalEventAdmin/loadCalenderView";

    prototype.switch_menu_entries = [
        {
            text: $.i18n.prop('event'),
            ui_class: 'view-switch general-event',
            action: 'event'
        },
        {
            text: $.i18n.prop('venue'),
            ui_class: 'view-switch venue',
            action: 'venue'
        },
        {
            text: $.i18n.prop('equipment'),
            ui_class: 'view-switch equipment',
            action: 'equipment'
        }
    ];

    (function () {
        var attachEvents = function() {
            var _self = this;

            this.on_global("event-create event-update", function() {
                _self.reload();
            });

            this.on("close", function () {
                app.tabs.content.tab = null;
            });
        };

        prototype.init = function () {
            app.tabs.generalEvent.calendar._super.init.call(this);
            app.tabs.generalEvent.calendar.tab = this;
            attachEvents.call(this);
            this.initCalendar();
        };
    })();

    prototype.initCalendar = function() {
        var _self = this;
        var container = _self.body;
        _self.calendarWrapper = container.find('.calendar-container');
        var dateFormat = this.body.find('.dateFormat').val().replaceAll('E', 'd');
        var timeFormat = this.body.find('.timeFormat').val().replace('aaa', 'TT');
        bm.ajax({
            url: app.baseUrl + 'generalEventAdmin/loadAllEventData',
            data: null,
            dataType: 'json',
            success: function(resp) {
                _self.calendarWrapper.html('');
                var dayKeys = ['sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday'];
                var dayNames = [];
                for(var i = 0; i < dayKeys.length; i++) {
                    dayNames.push($.i18n.prop(dayKeys[i]))
                }
                _self.calendarWrapper.fullCalendar({
                    viewDisplay: function(view) {
                        if(view.name === 'agendaWeek' || view.name === 'agendaDay') {
                            view.setHeight(9999);
                        }
                        _self.calendarWrapper.find(".fc-day").children().css('min-height', '182px');
                    },
                    header: {
                        left: 'month,agendaWeek,agendaDay',
                        center: 'title',
                        right: 'today prev,next'
                    },
                    buttonText: {
                        today: $.i18n.prop('today'),
                        month: $.i18n.prop('month'),
                        week: $.i18n.prop('week'),
                        day: $.i18n.prop('day')
                    },
                    dayNames: dayNames,
                    editable: false,
                    events: resp,
                    eventRender: function(event, element) {
                        var html = '<span class="title">' + bm.htmlEncode(event.title) + '</span> ' +
                            '<span class="start-time">' + $.fullCalendar.formatDate(event.start, timeFormat) + '</span>';
                        element.html(html);
                    }
                });
            }
        });
        _self.body.scrollbar()
    };

    prototype.onSwitchMenuClick = function(type) {
        app.Tab.changeView(this, "generalEvent", type, type == "explorer" ? "ExplorerPanelTab" : "SingleTableTab");
    };
}(undefined));

////////////////////////////////////////////// EQUIPMENT SECTION ////////////////////////////////////////////////////////

app.tabs.generalEvent.equipment = function() {
    app.tabs.generalEvent.equipment._super.constructor.apply(this, arguments);
};

(function(prototype) {
    prototype = app.tabs.generalEvent.equipment.inherit(app.tabs.generalEvent);

    prototype.ajax_url = app.baseUrl + "generalEventAdmin/loadEquipmentAppView";

    prototype.createEquipment = prototype.editEquipment = function(id, name, tab) {
        var _self = this;
        var data = {id: id},
            title = $.i18n.prop("edit.equipment")
        if (typeof id == "undefined") {
            data = {};
            name = "";
            title = $.i18n.prop("create.equipment");
        }
        if(!tab) {
            tab = _self;
        }
        tab.renderCreatePanel(app.baseUrl + "generalEventAdmin/editEquipment", title, name, data, {
            width: 920,
            success: function () {
                _self.reload();
            }
        });
    };

    prototype.deleteEquipment = function(id, name) {
        var _self = this;
        bm.remove('general-equipment', $.i18n.prop('equipment'), $.i18n.prop('confirm.delete.equipment', [name]), app.baseUrl + 'generalEventAdmin/deleteEquipment', id, {
            is_final: true,
            success: function() {
                _self.reload();
            }
        });
    };

    prototype.removeSelectedEquipments = function(ids) {
        var _self = this;
        var _ids = [];
        ids.forEach(function(obj) {
            _ids.push(obj.id);
        });
        bm.confirm($.i18n.prop('confirm.delete.selected.equipment'), function() {
            bm.ajax({
                url: app.baseUrl + 'generalEventAdmin/deleteSelectedEquipments',
                data: {ids: _ids},
                success: function() {
                    _self.reload();
                }
            });
        });
    };

    prototype.menu_entries = [
        {
            text: $.i18n.prop('edit'),
            ui_class: 'edit',
            action: 'edit'
        },
        {
            text: $.i18n.prop('remove'),
            ui_class: 'delete',
            action: 'delete'
        }
    ];

    (function() {
        var _super = app.tabs.generalEvent.equipment._super;
        prototype.init = function() {
            var _self = this;
            this.body.find(".toolbar .create").on("click", function() {
                _self.editEquipment();
            });
            _super.init.apply(this, arguments)
        };

    })();

    prototype.switch_menu_entries = [
        {
            text: $.i18n.prop('calendar'),
            ui_class: 'view-switch calendar',
            action: 'calendar'
        },
        {
            text: $.i18n.prop('event'),
            ui_class: 'view-switch general-event',
            action: 'event'
        },
        {
            text: $.i18n.prop('venue'),
            ui_class: 'view-switch venue',
            action: 'venue'
        }
    ];

    prototype.onActionClick = function(action, data) {7
        switch(action) {
            case 'edit':
                this.editEquipment(data.id, data.name);
                break;
            case 'delete':
                this.deleteEquipment(data.id, data.name);
                break;
        }
    };

    prototype.onSelectedActionClick = function(action, ids) {
        switch(action) {
            case "remove":
                this.removeSelectedEquipments(ids);
                break;
        }
    };

    prototype.onSwitchMenuClick = function(type) {
        app.Tab.changeView(this, "generalEvent", type, type == "explorer" ? "ExplorerPanelTab" : "SingleTableTab");
    };
}(undefined));

///////////////////////////////////////////////// VENUE SECTION /////////////////////////////////////////////////////////

app.tabs.generalEvent.venue = function() {
  app.tabs.generalEvent.venue._super.constructor.apply(this, arguments);
};

(function(prototype) {
    prototype = app.tabs.generalEvent.venue.inherit(app.tabs.generalEvent);

    prototype.resize_disabled = true;

    prototype.ajax_url = app.baseUrl + 'generalEventAdmin/loadVenueAppView';

    prototype.switch_menu_entries = [
        {
            text: $.i18n.prop('event'),
            ui_class: 'view-switch general-event',
            action: 'event'
        },
        {
            text: $.i18n.prop('equipment'),
            ui_class: 'view-switch equipment',
            action: 'equipment'
        },
        {
            text: $.i18n.prop('calendar'),
            ui_class: 'view-switch calendar',
            action: 'calendar'
        }
    ];

    (function() {
        function attachEvents() {
            var _self = this;
            _self.body.find(".toolbar-btn.save").on("click", function () {
                _self.saveVenue();
            });
            bm.attachModernUiPanel.call(this);
            _self.body.hasClass('table-view') ? _self.body.removeClass('table-view') : ''
        }

        prototype.init = function() {
            app.tabs.generalEvent.venue._super.init.call(this);
            attachEvents.call(this);
            this.attachRightPanel();
        };
    })();

    prototype.leftPanelConfig = {
        thumbPanel: $('<div item-name="" item-id="" class="item-thumb blocklist-item new-item fade-in-up">' +
            '<input type="text" maxlength="100" validation="required rangelength[2,100]" class="item-title full-width" placeholder="' +
            $.i18n.prop("enter.venue.name")+'">' +
            '<span class="float-tooliconbar"><span class="tool-icon apply" title="' + $.i18n.prop("apply") + '"></span>' +
            '<span class="tool-icon discard" title="' + $.i18n.prop("discard") + '"></span></span></div>'),
        url: app.baseUrl + "generalEventAdmin/createVenue",
        thumb_menu_entries: [{
            text: $.i18n.prop("remove"),
            ui_class: "delete",
            action: "delete"
        }],
        thumbMenuClick: function(action, config) {
            switch(action) {
                case "delete":
                    this.deleteVenue(config.id, config.name)
                    break;
            }
        },
        leftNavigationClick: function(type) {
            switch (type) {
                case "location":
                    this.renderLocations();
                    break;
                case "section":
                    this.renderSections();
                    break;
                case "seatmap":
                    this.renderSeatMap();
                    break
            }
        }
    };

    prototype.attachRightPanel = function() {
        var _self = this;
        var rightPanel = _self.body.find(".right-panel");
        var venueForm = rightPanel.find("form.venue-edit-form");
        venueForm.form({ajax_submit: true, submitButton: _self.body.find(".save.event-venue")});

        rightPanel.find(".add-location-btn").on("click", function() {
            _self.createLocation();
        });

        rightPanel.scrollbar();
        var locationTable = _self.body.find(".location-table-container");

        if(venueForm.length) {
            var sortable = locationTable.find(".location-item-container").sortable({
                handles: "tr.scrollable-rule",
                axis: 'y',
                placeholder: true,
                beforeStart: function(ui) {
                    if(ui.elm.find("form").length) {
                        return false
                    }
                },
                sort: function(ui) {
                    var item = ui.elm;
                    item.find("td.section-thumb").css({width: undefined});
                }
            });
        }

        function attachTableEvent() {
            bm.menu([
                {
                    text: $.i18n.prop("edit.location"),
                    ui_class: "edit",
                    action: "edit-location"
                },
                {
                    text: $.i18n.prop("remove"),
                    ui_class: "remove",
                    action: "remove"
                }
            ], locationTable, ".action-navigator", {
                click: function (action, entity) {
                    var data = entity.config("entity");
                    var base = entity.parents(".location-row");
                    switch (action) {
                        case "edit-location":
                            _self.editLocation(data.id, data.venueid, data.name);
                            break;
                        case "remove":
                            _self.deleteLocation(data.id, data.name)
                            break;
                    }
                }
            }, "click");
            locationTable.on("click", ".left-column .edit-section", function() {
                _self.createSection($(this));
            })
        }

        var saveBtn = _self.body.find(".toolbar-btn.save");
        if(venueForm.length) {
            saveBtn.show();
            rightPanel.find(".content").removeClass("create-edit-form");
        } else {
            saveBtn.hide();
            _self.body.find(".item-thumb.selected").removeClass("selected");
        }
        attachTableEvent();
    };

    prototype.saveVenue = function() {
        var _self = this
        var form = _self.body.find("form.venue-edit-form");
        var newTd = form.find(".location-table-container .add-new-location");
        if(!form.valid()) {
            return false;
        }
        if(newTd.length) {
            bm.notify($.i18n.prop("you.have.an.unconfirmed.location"), "alert");
            form.animate({scrollTop : form[0].scrollHeight}, "slow");
            bm.highlight(newTd);
            return;
        }
        var venueCache = form.serializeObject();
        var locationCache = _self.body.find(".location-item-container").serializeObject();
        bm.ajax({
            url: app.baseUrl + "generalEventAdmin/saveVenue",
            data: $.extend(venueCache, {venueLocations: locationCache["venue-location"]}),
            success: function(resp) {
                app.global_event.trigger("venue-update", [resp.id])
                _self.body.find(".left-panel [item-id="+resp.id+"] .item-title").text(venueCache.name);
                _self.reload(resp.id);
            }
        });
    };

    prototype.deleteVenue = function (id, name) {
        var _self = this;
        bm.remove("venue", $.i18n.prop("venue"), $.i18n.prop("confirm.delete.venue", [name]), app.baseUrl + "generalEventAdmin/deleteVenue", id, {
            is_final: true,
            success: function () {
                _self.reload(undefined, true);
                _self.reload();
            }
        })
    }

    prototype.renderLocations = function() {
        var _self = this;
        var panel = _self.body.find(".right-panel");
        panel.loader("updating");
        bm.ajax({
            url: app.baseUrl + "generalEventAdmin/loadLocationView",
            dataType: "html",
            response: function() {
                panel.loader(false);
            },
            success: function(resp) {
                resp = $(resp);
                var body = panel.find(".body");
                body.empty();
                body.append(resp);
                _self.attachRightPanel();
            }
        });
    };

    prototype.renderSections = function() {
        var _self = this;
        var panel = _self.body.find(".right-panel");
        var sectionUrl = app.baseUrl + "generalEventAdmin/loadSectionView"
        var sectionTable
        panel.loader("updating");
        bm.ajax({
            url: sectionUrl,
            dataType: "html",
            response: function() {
                panel.loader(false);
            },
            success: function(resp) {
                sectionTable = $(resp);
                var body = panel.find(".body").empty();
                bm.table(sectionTable, {
                    url: sectionUrl,
                    onload: function() {
                    },
                    menu_entries: [
                        {
                            text: $.i18n.prop("edit"),
                            ui_class: "edit",
                            action: "edit"
                        },
                        {
                            text: $.i18n.prop("remove"),
                            ui_class: "remove",
                            action: "remove"
                        }
                    ],
                    onActionClick: function(action, data, navigator) {
                        switch (action) {
                            case "edit":
                                _self.editSection(null, data.sectionid, data.locationid, data.name, 'edit');
                                break;
                            case "remove":
                                _self.deleteSection(data.sectionid, data.name);
                                break;
                        }
                    }
                });
                body.append(sectionTable);
                _self.body.find(".toolbar-btn.save").hide();
                panel.scrollbar();
            }
        });
    };

    prototype.renderSeatMap = function() {
        var _self = this;
        var panel = _self.body.find(".right-panel");
        var seatMapUrl = app.baseUrl + "generalEventAdmin/loadSeatMapView"
        panel.loader("updating");
        bm.ajax({
            url: seatMapUrl,
            dataType: "html",
            data: {
                venueId: panel.find("select.venue-selector").val(),
                locationId: panel.find("select.location-selector").val(),
                section: panel.find("select.section-selector").val()
            },
            response: function() {
                panel.loader(false);
            },
            success: function(resp) {
                var body = panel.find(".body").empty();
                body.append($(resp));
                _self.body.find(".toolbar-btn.save").hide();
                panel.scrollbar();
                panel.updateUi();
                panel.find(".selector").change(function() {
                    _self.renderSeatMap();
                });
                panel.find(".reload").click(function() {
                    _self.renderSeatMap();
                });
            }
        });
    };

    prototype.createLocation = prototype.editLocation = function(id, venueId, name, tab) {
        var _self = this;
        var panel = _self.body.find(".right-panel");
        venueId = venueId || panel.find(".venue-id").val();
        var data = {id: id, venueId: venueId},
            title = $.i18n.prop("edit.location")
        if (typeof id == "undefined") {
            data = {venueId: venueId};
            name = "";
            title = $.i18n.prop("create.location");
        }
        if(!tab) {
            tab = _self;
        }
        tab.renderCreatePanel(app.baseUrl + "generalEventAdmin/editVenueLocation", title, name, data, {
            width: 920,
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
            success: function (resp) {
                _self.reload(venueId);
            }
        });
    };

    prototype.deleteLocation = function(id, name) {
        var _self = this
        var panel = _self.body.find(".right-panel");
        var venueId = panel.find(".venue-id").val();
        bm.remove("venueLocation", "Venue Location", $.i18n.prop("confirm.delete.location", [name]), app.baseUrl + "generalEventAdmin/deleteLocation", id, {
            is_final: true,
            success: function () {
                venueId ? _self.reload(venueId) : _self.renderLocations();
            }
        })
    };

    prototype.createSection = prototype.editSection = function($this, id, locationId, name, type, tab) {
        var _self = this
        var panel = _self.body.find(".right-panel");
        var venueId
        if($this) {
            venueId = panel.find(".venue-id").val();
            locationId =  $this.parent().find('[name="location-id"]').val();
        }
        var data = {id: id, locationId: locationId},
            title = $.i18n.prop("edit.section")
        if (typeof id == "undefined") {
            data = {locationId: locationId};
            name = "";
            title = $.i18n.prop("create.section");
        }
        if(!tab) {
            tab = _self;
        }

        tab.renderCreatePanel(app.baseUrl + "generalEventAdmin/editSection", title, name, data, {
            width: 920,
            success: function (resp) {
                type ? _self.renderSections() : (venueId ? _self.reload(venueId) : _self.renderLocations());
            },
            content_loaded: function(popupObj) {
                var $popup = this;
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
    };

    prototype.deleteSection = function(id, name) {
        var _self = this;
        bm.remove("venueLocationSection", "Venue Location Section", $.i18n.prop("confirm.delete.section", [name]), app.baseUrl + "generalEventAdmin/deleteVenueLocationSection", id, {
            success: function () {
                _self.renderSections();
            }
        })
    };

    prototype.reload = function(id, isLeft, callback) {
        var _self = this;
        var panel, url;
        if(isLeft) {
            panel = this.body.find(".left-panel .body");
            url = app.baseUrl + "generalEventAdmin/loadLeftVenuePanel"
        } else {
            panel = this.body.find(".right-panel .body");
            url = app.baseUrl + "generalEventAdmin/loadRightVenuePanel"
        }
        panel.addClass("updating").loader();
        bm.ajax({
            url: url,
            dataType: "html",
            data: {selected: id},
            response: function() {
                panel.removeClass("updating");
                panel.loader(false);
            },
            success: function(resp) {
                resp = $(resp);
                if(isLeft) {
                    var countDom = _self.body.find(".item-group .count");
                    var count = resp.find(".blocklist-item").length;
                    countDom.text(count);
                    panel.empty().append(resp);
                    panel.updateUi();
                    bm.attachModernUiPanel.call(_self);
                } else {
                    panel.empty().append(resp);
                    panel.updateUi();
                    _self.attachRightPanel();
                }
                if(callback) {
                    callback(id);
                }
            }
        });
    };

    prototype.onSwitchMenuClick = function(type) {
        app.Tab.changeView(this, "generalEvent", type, type == "explorer" ? "ExplorerPanelTab" : "SingleTableTab");
    };
}(undefined));

//////////////////////////////////////////////// CUSTOM FIELDS SECTION //////////////////////////////////////////////////

app.tabs.generalEventCustomField = function(config) {
    this.id = config.id;
    this.eventId = config.eventId;
    this.text = $.i18n.prop('event.custom.field');
    this.tip = $.i18n.prop('manage.event.custom.field');
    this.ui_class = "event-custom-field edit-tab";
    this.ajax_url = app.baseUrl + "generalEventAdmin/loadCustomFieldAppView?eventId=" + this.eventId;
    app.tabs.generalEventCustomField._super.constructor.apply(this, arguments);
};

(function(prototype) {
    prototype = app.tabs.generalEventCustomField.inherit(app.SingleTableTab);

    (function() {
        function attachEvents() {
            var _self = this;
            _self.on_global("event-custom-field-updated", function () {
                _self.reload();
            });
            _self.on("close", function () {
                app.tabs.generalEventCustomField.tab = null;
            });
            _self.body.find(".toolbar .create").on("click", function() {
                _self.createEventCustomField(undefined, "");
            });
        };

        prototype.init = function () {
            var _self = this;
            app.tabs.generalEventCustomField._super.init.call(this);
            app.tabs.generalEventCustomField.tab = this;
            attachEvents.call(this);
        };
    })();

    prototype.menu_entries = [
        {
            text: $.i18n.prop("edit"),
            ui_class: "edit",
            action: "edit"
        },
        {
            text: $.i18n.prop("remove"),
            ui_class: "delete",
            action: "remove"
        }
    ];

    prototype.onActionClick = function(action, data) {
        switch(action) {
            case "edit":
                this.edit(data.id, data.label);
                break;
            case "remove":
                this.deleteCustomField(data.id, data.label);
                break;
        }
    };

    prototype.action_menu_entries = [
        {
            text: $.i18n.prop("edit.group.label"),
            ui_class: "edit-label",
            action: "edit-label"
        }
    ];

    prototype.onActionMenuClick = function(action) {
        switch(action) {
            case "edit-label":
                this.editFieldLabel();
                break;
        }
    };

    prototype.createEventCustomField = prototype.edit = function(id, name) {
        var data = {id: id, eventId: this.eventId},
            title = $.i18n.prop("edit.event.custom.field");
        if(typeof id == "undefined") {
            data = {eventId: this.eventId};
            title = $.i18n.prop("create.event.custom.field");
        }
        this.renderCreatePanel(app.baseUrl + "generalEventAdmin/editCustomField", title, name, data, {
            success: function() {
                if(app.tabs.generalEventCustomField.tab) {
                    app.tabs.generalEventCustomField.tab.reload();
                }
                app.global_event.trigger("event-custom-field-updated", [id]);
            }
        });
    };

    prototype.deleteCustomField = function(id, name) {
        var _self = this
        bm.remove("eventCustomField", $.i18n.prop("event.custom.field"), $.i18n.prop("confirm.delete.event.custom.field", [name]), app.baseUrl + "generalEventAdmin/deleteCustomField", id, {
            is_final: true,
            success: function () {
                _self.reload()
            }
        })
    };

    prototype.editFieldLabel = function() {
        var title = $.i18n.prop("set.group.title");
        var data = {eventId: this.eventId};
        bm.editPopup(app.baseUrl + "generalEventAdmin/editCustomFieldLabel", title, "", data, {})
    }
}(undefined));

app.tabs.attendeeDetailsTab = function(config) {
    this.id = config.id;
    this.eventId = config.eventId;
    this.text = $.i18n.prop('attendee.details');
    this.tip = $.i18n.prop('manage.attendee.details');
    this.ui_class = "attendee-details";
    this.ajax_url = app.baseUrl + "generalEventAdmin/loadAttendeeDetailsView?eventId=" + this.eventId + '&isRecurring=' + config.isRecurring;
    app.tabs.attendeeDetailsTab._super.constructor.apply(this, arguments);
};
app.tabs.attendeeDetailsTab.inherit(app.SingleTableTab);