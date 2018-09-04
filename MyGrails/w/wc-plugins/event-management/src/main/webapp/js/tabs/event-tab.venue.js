 bm.onReady(app.tabs, "event", function() {
    var _panel
    var appTab

    app.tabs.event.Venue = function(_this, parentTab) {
        _panel = _this
        appTab = parentTab
    }

    var _v = app.tabs.event.Venue.prototype;

    function attachEvent() {
        var _self = this
        var namespace = "event-management-venue-tab"
        app.global_event.on("venue-booked." + namespace, function() {
            _self.tabulator.reload()
        })
        app.global_event.on("venue-update." + namespace, function() {
            _self.tabulator.reload()
        })
        appTab.on("close." + namespace, function() {
            app.global_event.off("." + namespace)
        })
        this.sortable = {
            list: {
                "1": "name"
            },
            sorted: "1",
            dir: "up"
        };
        appTab.toolbar.find(".create-venue").on("click", function() {
            app.tabs.event.createVenue();
        })
        this.tabulator = bm.table($(_panel.find(".venue-table")), $.extend({
            url: app.baseUrl + "eventAdmin/loadVenueView",
            menu_entries: [
                {
                    text: $.i18n.prop("edit"),
                    ui_class: "edit",
                    action: "edit"
                },
                {
                    text: $.i18n.prop("manage.location"),
                    ui_class: "manage-location",
                    action: "manageLocation"
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
            }
        }, {
            sortable: this.sortable.list,
            sorted: this.sortable.sorted,
            sortedDir: this.sortable.dir
        }))
        this.tabulator.onActionClick = function (action, data) {
            switch (action) {
                case "edit":
                    app.tabs.event.createVenue(data.id, data.name);
                    break;
                case "manageLocation":
                    app.tabs.event.Venue.manageLocation(data.id, data.name)
                    break;
                case "delete":
                    _self.deleteVenue(data.id, data.name);
                    break;
            }
        }

        this.table = _panel.find(".body table");
        var container = _panel.find(".event-tab")
        var tabContainer = appTab.body.find(".bmui-tab-body-container");
        if (_panel.find("table.content .select-column :checkbox").length > 0) {
            var table = _panel.find("table")
            bm.tableCheckAll(table, function() {
                _panel.tool.find(".action-header").show();
            }, function() {
                _panel.tool.find(".action-header").show();
            }, function() {
                _panel.tool.find(".action-header").hide();
            })
            $(_panel.tool.find(".action-header")).find(".action-on-selection").change(function () {
                var selected = table.find("td.select-column :checkbox:checked")
                var selectedIds = [];
                selected.each(function () {
                    selectedIds.push($(this).attr("data-id"))
                });
                var action = $(this).val();
                _self.onSelectedActionClick(action, selectedIds)
                $(this).chosen("val", "");
            });
        }
    }

    _v.reload = function() {
        this.tabulator.reload()
    }
    _v.init = function() {
        attachEvent.call(this)
    }

    _v.beforeReloadRequest = function (param) {
        var searchFilter = {searchText: this.simpleSearchText };
        $.extend(param, searchFilter);
    }

    _v.onSelectedActionClick = function(action, ids) {
        switch(action) {
            case "remove":
                this.deleteSelected(ids);
                break;
        }
    };

    app.tabs.event.createVenue = function (id, name) {
        var title = id ? $.i18n.prop("edit.venue") : $.i18n.prop("create.venue")
        appTab.renderCreatePanel(app.baseUrl + "eventAdmin/editVenue", title, name, {id: id}, {
            width: 795,
            success : function() {
                app.global_event.trigger("venue-update",[id])
            }
        })
    }

    app.tabs.event.Venue.manageLocation = function (id, name) {
        var tab = app.Tab.getTab("tab-manage-location-of-venue-" + id);
        if (!tab) {
            tab = new app.tabs.manageLocation.lastView({
                id: "tab-manage-location-of-venue-" + id,
                data: {
                    venue: {
                        id: id,
                        name: name
                    }
                }
            });
            tab.render();
        }
        tab.setActive();
    }

    _v.deleteVenue = function (id, name) {
        if(app.Tab.getTab("tab-manage-location-of-venue-" + id)) {
            bm.notify($.i18n.prop("manage.location.tab.open.close.that.first"),"error");
            return;
        }
        var _self = this;
        bm.remove("venue", $.i18n.prop("venue"), $.i18n.prop("confirm.delete.venue", [name]), app.baseUrl + "eventAdmin/deleteVenue", id, {
            is_final: true,
            success: function () {
               _panel.reload()
            }
        })
    }

    _v.deleteSelected = function(ids) {
        bm.confirm($.i18n.prop("confirm.delete.selected.event"), function() {
            bm.ajax({
                url: app.baseUrl + "eventAdmin/deleteSelectedVenues",
                data: {ids: ids},
                success: function () {
                    _panel.reload()
                    _panel.tool.filter(".action-header").hide();
                }
            })
        },function(){
        });
    }

    _v.removeSelected = function(ids) {
        bm.ajax({
            url: app.baseUrl + "eventAdmin/deleteSelectedVenue",
            data: {ids: ids},
            success: function () {
                _panel.reload()
            }
        })
    }
})
