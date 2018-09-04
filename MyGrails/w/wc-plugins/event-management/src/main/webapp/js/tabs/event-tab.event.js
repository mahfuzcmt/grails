 bm.onReady(app.tabs, "event", function() {
    var _panel
    var appTab

    app.tabs.event.Event = function(_this, parentTab) {
        _panel = _this
        appTab = parentTab
    }

    var _e = app.tabs.event.Event.prototype;

     _e.advanceSearchUrl = app.baseUrl + "eventAdmin/filterEvent";
     _e.advanceSearchTitle = $.i18n.prop("event");

    _e.onMenuOpen = function(navigator) {
        var menu = this.tabulator.menu;
        var itemList = [
            {
                key: "event.edit",
                class: "edit"
            },
            {
                key: "event.remove",
                class: "delete"
            }
        ];
        app.checkPermission(menu, itemList);

        if(navigator.is("tr.disable-session span")) {
            menu.find(".menu-item.manage-session").addClass("disabled");
        } else {
            menu.find(".menu-item.manage-session").removeClass("disabled");
        }
        if(navigator.is("tr.disable-venue-invitation span")) {
            menu.find(".book-venue").addClass("disabled");
        } else {
            menu.find(".book-venue").removeClass("disabled");
        }
        if(navigator.is("tr.disable-equipment-invitation span")) {
            menu.find(".book-equipment").addClass("disabled");
        } else {
            menu.find(".book-equipment").removeClass("disabled");
        }
        if(navigator.is("tr.disable-manage-ticket span")) {
            menu.find(".menu-item.manage-ticket").addClass("disabled");
        } else {
            menu.find(".menu-item.manage-ticket").removeClass("disabled");
        }
        if(navigator.is("tr.disable-delete span")) {
            menu.find(".menu-item.delete").addClass("disabled");
        } else {
            menu.find(".menu-item.delete").removeClass("disabled");
        }

    }

    _e.reload = function() {
        this.tabulator.reload()
    }


    _e.init = function() {
        var _self = this;
        var namespace = "event-management-event"
        app.global_event.on("ticket-purchased." + namespace + " event-deleted." + namespace + " event-create." + namespace + " event-update." + namespace + " equipment-invitation-approved." + namespace + " venue-location-invitation-approved." + namespace +
            " event-session-updated." + namespace + " booking-request-sent." + namespace + " equipment-invitation-request-updated." + namespace, function() {
            _self.tabulator.reload()
        })
        appTab.on("close", function() {
            app.global_event.off("." + namespace)
        })
        appTab.toolbar.find(".create.create-event").on("click", function() {
            app.tabs.event.createEvent();
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
        this.tabulator = bm.table($(_panel.find(".event-table")), {
            url: app.baseUrl + "eventAdmin/loadEventView",
            menu_entries: [
                {
                    text: $.i18n.prop("edit"),
                    ui_class: "edit",
                    action: "edit"
                },
                {
                    text: $.i18n.prop("view.in.site"),
                    ui_class: "preview view",
                    action:"view-in-website"
                },
                {
                    text: $.i18n.prop("manage.session"),
                    ui_class: "manage-session",
                    action: "manageSession"
                },
                {
                    text: $.i18n.prop("book.venue"),
                    ui_class: "book-venue",
                    action: "bookVenueForEvent"
                },
                {
                    text: $.i18n.prop("book.equipment"),
                    ui_class: "book-equipment",
                    action: "bookEquipmentForEvent"
                },
                {
                    text: $.i18n.prop("manage.tickets"),
                    ui_class: "manage-ticket",
                    action: "manageTicket"
                },
                {
                    text: $.i18n.prop("remove"),
                    ui_class: "delete",
                    action: "delete"
                }
            ],
            beforeReloadRequest: function () {
                var params = this.param
                params.searchText = _self.simpleSearchText
                _self.beforeReloadRequest(params)
            },
            afterLoad: function() {
                _panel.tool.filter(".action-header:visible").hide();
                _self.table.tscrollbar("content");
                if (_self.onContentLoad) {
                    _self.onContentLoad()
                }
            },
            sortable: this.sortable.list,
            sorted: this.sortable.sorted,
            sortedDir: this.sortable.dir
        })
        this.tabulator.onMenuOpen = $.proxy(this.onMenuOpen, this);
        this.tabulator.onActionClick = function (action, data) {
            switch (action) {
                case "edit":
                    app.tabs.event.createEvent(data.id, data.name);
                    break;
                case "view-in-website":
                    _self.viewInWebsite(data.id)
                    break;
                case "delete":
                    _self.deleteEvent(data.id, data.name);
                    break;
                case "manageSession":
                    _self.manageSession(data.id, data.name)
                    break;
                case "bookVenueForEvent":
                    _self.bookVenueForEvent(data.id, data.name)
                    break;
                case "bookEquipmentForEvent":
                    _self.bookEquipmentForEvent(data.id, data.name)
                    break;
                case "manageTicket":
                    _self.manageTicket(data.id, data.name)
                    break;
            }
        }

        if (_panel.find("table.content .select-column :checkbox").length > 0) {
            var table = _panel.find("table")
            bm.tableCheckAll(table, function() {
                _panel.tool.find(".action-header").show();
            }, function() {
                _panel.tool.find(".action-header").show();
            }, function() {
                _panel.tool.find(".action-header").hide();
            })
            $(_panel.tool.find(".action-header")).find(".event-action-on-selection").change(function () {
                var selected = table.find("td.select-column :checkbox:checked")
                var selecteds = [];
                selected.each(function () {
                    selecteds.push($(this).config("entity"))
                });
                var action = $(this).val();
                _self.onSelectedActionClick(action, selecteds)
                $(this).chosen("val", "");
            });
        }
        _self.onSelectedActionClick = function(action, selecteds) {
            switch(action) {
                case "remove":
                    _self.deleteSelected(selecteds.collect("id"));
                    break;
            }
        };
        _self.beforeReloadRequest = function (param) {
            var searchFilter = {searchText: _self.simpleSearchText };
            if (this.advanceSearchFilter !== false) {
                searchFilter = this.advanceSearchFilter;
            }
            $.extend(param, searchFilter);
        };

        this.table = _panel.find(".body table");
        _panel.find(".event-tab")
    }

    app.tabs.event.createEvent = function (id, name) {
        var title = id ? $.i18n.prop("edit.event") : $.i18n.prop("create.event")
        appTab.renderCreatePanel(app.baseUrl + "eventAdmin/createEvent", title, name, {id: id}, {
            width: 765,
            content_loaded: function() {
                var _self = this
                var imageList = _self.find(".event-image-container");
                imageList.find(".remove").on("click", function(){
                    var entity = $(this).closest(".image-thumb")
                    removeImage(entity);
                });
                function removeImage(entity) {
                    var imageId = entity.attr("image-id");
                    $("<input type='hidden' name='remove-images' value='" + imageId + "'>").appendTo(entity.closest("form"))
                    entity.trigger("change").remove();
                    imageList.scrollbar("update", true)
                }
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
                })
                $(window).on("resize." + this.id, function() {
                    imageList.scrollbar("update", true)
                })
                imageList.sortable({
                    containment: "parent",
                    stop: function() {
                        imageList.trigger("change")
                    }
                })
                bm.metaTagEditor(_self.find("#bmui-tab-metatag"));
            },
            success : function() {
                if(id) {
                    app.global_event.trigger("event-update", [id]);
                } else {
                    app.global_event.trigger("event-create");
                }
            }
        })
    };

    app.tabs.event.viewEvent = function(id, name) {
        bm.viewPopup(app.baseUrl + "eventAdmin/viewEvent", {id: id}, { width: 600});
    };

    _e.deleteEvent = function (id, name) {
        if(app.Tab.getTab("tab-event-session-" + id)) {
            bm.notify($.i18n.prop("event.session.tab.open.close.that.first"),"error");
            return;
        }
        bm.remove("event", $.i18n.prop("event"), $.i18n.prop("confirm.delete.event", [name]), app.baseUrl + "eventAdmin/deleteEvent", id, {
            is_final: true,
            success: function () {
                app.global_event.trigger("event-deleted", [id]);
            }
        })
    };

     _e.viewInWebsite = function(id){
         var url = app.siteBaseUrl + "event/" + id +"?adminView=true"
         window.open(url,'_blank');
     }

    _e.deleteSelected = function (ids) {
        bm.confirm($.i18n.prop("confirm.delete.selected.event"), function() {
            bm.ajax({
                url: app.baseUrl + "eventAdmin/deleteSelectedEvents",
                data: {ids: ids},
                success: function () {
                    _panel.reload()
                    _panel.tool.filter(".action-header").hide();
                }
            })
        },function(){
        });
    }

    _e.manageSession = function (id, name) {
        var tabId = "tab-event-session-" + id;
        var tab = app.Tab.getTab(tabId);
        if(!tab) {
            tab = new app.tabs.eventSession({
                id: tabId, name: name, eventId: id
            });
            tab.render();
        }
        tab.setActive();
    };

    _e.manageTicket = function(id, name) {
        var tabId = "tab-manage-ticket-" + id;
        var tab = app.Tab.getTab(tabId);
        if(!tab) {
            tab = new app.tabs.manageTicket.lastView({
                id: tabId, name: name, eventId: id
            });
            tab.render();
        }
        tab.setActive();
    }

    _e.bookVenueForEvent = function (id, name) {
        bm.editPopup( app.baseUrl + "eventAdmin/bookVenue", $.i18n.prop("select.venue.location"), name, {id: id}, {
            width: 650,
            success: function() {
                app.global_event.trigger("booking-request-sent")
            },
            events: {
                content_loaded: function () {
                    var content = this;
                    content.find('.activate-venue-tab').click(function() {
                        content.find('.close').trigger('click');
                        appTab.body.find('.bmui-tab').tabify('option', 'active', 'venue');
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
                                    app.tabs.event.Venue.manageLocation(venueId, venueName)
                                });
                            }
                        })
                    });
                    content.find('select.venue-selector').trigger('change');
                }
            }
        });
    };

    _e.bookEquipmentForEvent = function (id, name){
        bm.editPopup( app.baseUrl + "eventAdmin/bookEquipment", $.i18n.prop("select.equipment"), name, {eventId: id}, {
            width: 650,
            success: function() {
                app.global_event.trigger("booking-request-sent")
            },
            events: {
                content_loaded: function() {
                    var popup = this;
                    popup.find('.activate-equipment-tab').click(function() {
                        popup.find('.close').trigger('click');
                        appTab.body.find('.bmui-tab').tabify('option', 'active', 'equipment');
                    });
                }
            }
        })
    };
});
