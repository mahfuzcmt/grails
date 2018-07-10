 bm.onReady(app.tabs, "event", function() {
    var _panel
    var appTab
    
    var Equipment = app.tabs.event.Equipment = function(_this, parentTab) {
        _panel = _this
        appTab = parentTab
    }

    var _eq = Equipment.prototype;
     _eq.advanceSearchUrl = app.baseUrl + "eventAdmin/filterEquipment";
     _eq.advanceSearchTitle = $.i18n.prop("equipment");

    function bindInvitationTables() {
        var _self = this
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
        $.each(_panel.find(".equipment-invitation-table"), function(ind, elm) {
            var _inner = this
            this.tabulator = bm.table($(elm), $.extend({
                menu_entries: menu_entries
            }))
            this.tabulator.onActionClick = function (action, data) {
                switch (action) {
                    case "approved":
                        _self.approveInvitation(data.id);
                        break;
                    case "rejected":
                        _self.rejectInvitation(data.id);
                        break;
                }
            }
            this.tabulator.onMenuOpen = function(navigator) {
                if(navigator.attr("entity-status") == "approved") {
                    _inner.tabulator.menu.find(".menu-item.approve").addClass("disabled")
                    _inner.tabulator.menu.find(".menu-item.reject").removeClass("disabled")
                } else if(navigator.attr("entity-status") == "rejected") {
                    _inner.tabulator.menu.find(".menu-item.approve").removeClass("disabled")
                    _inner.tabulator.menu.find(".menu-item.reject").addClass("disabled")
                }else {
                    _inner.tabulator.menu.find(".menu-item.approve").removeClass("disabled")
                    _inner.tabulator.menu.find(".menu-item.reject").removeClass("disabled")
                }
            }
        })
    }

    _eq.switch_menu_entries = [
        {
            text: $.i18n.prop("equipment.type.list"),
            ui_class: "view-switch equipment-type list-view",
            action: "equipmentType"
        }
    ];

    function attachEquipmentEvent() {
        var _self = this
        var namespace = "event-management-equipment"
        app.global_event.on("equipment-update." + namespace + " equipment-booked." + namespace + " booking-request-sent." + namespace, function() {
            _self.tabulator.reload();
        })
        appTab.on("close", function() {
            app.global_event.off("." + namespace)
        })
        this.sortable = {
            list: {
                "2": "name",
                "5": "created",
                "6": "updated"
            },
            sorted: "2",
            dir: "up"
        };
        _self.onActionMenuClick = function(action) {
            switch (action) {
                case "equipmentType":
                    app.tabs.event.Equipment = EquipmentType;
                    app.global_event.off("." + namespace);
                    appTab.changeTabUrl("equipment", app.baseUrl + "eventAdmin/loadEquipmentTypeView");
                    break;
            }
        }
        appTab.toolbar.find(".create-equipment").on("click", function() {
            app.tabs.event.createEquipment();
        });
        bm.menu(_self.switch_menu_entries, appTab.toolbar.find(".switch-menu.equipment-tool"), null, {
            click: $.proxy(_self, "onActionMenuClick")
        }, "click");
        this.tabulator = bm.table($(_panel.find(".equipment-table")), $.extend({
            url: app.baseUrl + "eventAdmin/loadEquipmentView",
            menu_entries: [
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
            ],
            beforeReloadRequest: function () {
                var params = this.param
                params.searchText = _self.simpleSearchText
                _self.beforeReloadRequest(params)
            },

            afterLoad: function() {
                _self.attachToggleCell()
                _panel.tool.find(".action-header:visible").hide();
                _self.table.tscrollbar("content");
                bindInvitationTables.call(_self);
            }
        }, {
            sortable: this.sortable.list,
            sorted: this.sortable.sorted,
            sortedDir: this.sortable.dir
        }))
        this.tabulator.onActionClick = function (action, data) {
            switch (action) {
                case "edit":
                    app.tabs.event.createEquipment(data.id, data.name);
                    break;
                case "delete":
                    _self.removeEquipment(data.id, data.name);
                    break;
            }
        }
        this.table = _panel.find(".body table");
        var container = _panel.find(".event-tab")
        _self.attachToggleCell();
        _self.beforeReloadRequest = function (param) {
            var searchFilter = {searchText: _self.simpleSearchText };
            if (_self.advanceSearchFilter !== false) {
                searchFilter = _self.advanceSearchFilter;
            }
            $.extend(param, searchFilter);
        }
    }

     _eq.attachToggleCell = function() {
        _panel.find(".toggle-cell").click(function() {
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
    _eq.init = function() {
        attachEquipmentEvent.call(this)
        bindInvitationTables.call(this)
    }

    _eq.reload = function() {
        this.tabulator.reload()
    }

    _eq.onContentLoad = function() {
        bindInvitationTables.call(this)
    }

    _eq.onSelectedActionClick = function(action, ids) {
        switch(action) {
            case "remove":
                this.removeSelected(ids);
                break;
        }
    };


    app.tabs.event.createEquipment = function (id, name) {
        var _self = this;
        var title = id ?  title =  $.i18n.prop("edit.equipment") : $.i18n.prop("create.equipment")
        appTab.renderCreatePanel(app.baseUrl + "eventAdmin/editEquipment", title, name, {id: id}, {
            success : function() {
                app.global_event.trigger("equipment-update", [id])
            },
            content_loaded: function() {
                var popup = this;
                popup.find('.activate-equipment-type-tab').click(function() {
                    popup.find(".toolbar-btn.cancel").trigger("click");
                    app.tabs.event.Equipment = EquipmentType;
                    app.global_event.off(".event-management-equipment");
                    appTab.changeTabUrl("equipment", app.baseUrl + "eventAdmin/loadEquipmentTypeView");
                });
            }
        })
    }

    _eq.approveInvitation = function(id) {
        bm.ajax({
            url: app.baseUrl + "eventAdmin/approveEquipmentInvitation",
            data: {id: id},
            success: function () {
                _panel.reload()
                app.global_event.trigger("equipment-invitation-request-updated")
            }
        })
    }

    _eq.rejectInvitation = function(id) {
        bm.ajax({
            url: app.baseUrl + "eventAdmin/rejectEquipmentInvitation",
            data: {id: id},
            success: function () {
                _panel.reload()
                app.global_event.trigger("equipment-invitation-request-updated")
            }
        })
    }

    _eq.removeEquipment = function (id, name) {
        bm.remove("equipment", $.i18n.prop("equipment"), $.i18n.prop("confirm.delete.equipment", [name]), app.baseUrl + "eventAdmin/deleteEquipment", id, {
            is_final: true,
            success: function () {
                _panel.reload()
            }
        })
    }

    _eq.removeSelected = function(ids) {
        bm.confirm($.i18n.prop("confirm.delete.selected.equipment"), function() {
            bm.ajax({
                url: app.baseUrl + "eventAdmin/deleteSelectedEquipments",
                data: {ids: ids},
                success: function () {
                    _panel.reload()
                    _panel.tool.find(".action-header").hide();
                }
            })
        },function(){
        });
    }

    var EquipmentType = function(_this, parentTab) {
        _panel = _this
        appTab = parentTab
    }

    var _eqt = EquipmentType.prototype;

    _eqt.switch_menu_entries = [
        {
            text: $.i18n.prop("equipment.list"),
            ui_class: "view-switch equipment list-view",
            action: "equipment"
        }
    ];

    function attachEquipmentTypeEvent() {
        var _self = this
        var namespace = "event-management-equipment"
        app.global_event.on("equipment-type-updated." + namespace + " equipment-type-created." + namespace, function() {
            _self.tabulator.reload()
        })
        appTab.on("close", function() {
            app.global_event.off("." + namespace)
        })
        _self.onActionMenuClick = function(action) {
            switch (action) {
                case "equipment":
                    app.tabs.event.Equipment = Equipment
                    app.global_event.off("." + namespace)
                    appTab.changeTabUrl("equipment", app.baseUrl + "eventAdmin/loadEquipmentView")
                    break;
            }
        }

        appTab.toolbar.find(".create-equipment-type").on("click", function() {
            _self.createEquipmentType();
        });
        bm.menu(_self.switch_menu_entries, appTab.toolbar.find(".equipment-type-tool .switch-menu"), null, {
            click: $.proxy(_self, "onActionMenuClick")
        }, "click");
        this.sortable = {
            list: {
                "1": "name"
            },
            sorted: "1",
            dir: "up"
        };
        this.tabulator = bm.table($(_panel.find(".equipment-type-table")), $.extend({
            url: app.baseUrl + "eventAdmin/loadEquipmentTypeView",
            menu_entries: [
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
            ],
            beforeReloadRequest: function () {
                var params = this.param;
                params.searchText = _self.simpleSearchText;
                _self.beforeReloadRequest(params)
            },
            afterLoad: function () {
                _panel.tool.find(".action-header:visible").hide();
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
                    _self.createEquipmentType(data.id, data.name);
                    break;
                case "delete":
                    _self.removeEquipmentType(data.id, data.name);
                    break;
            }
        }
        this.table = _panel.find(".body table");
    }

    _eqt.init = function() {
        attachEquipmentTypeEvent.call(this)
    }

    _eqt.reload = function() {
        this.tabulator.reload()
    }

    _eqt.beforeReloadRequest = function (param) {
        var searchFilter = {searchText: this.simpleSearchText };
        $.extend(param, searchFilter);
    }

    _eqt.onSelectedActionClick = function(action, ids) {
        switch(action) {
            case "remove":
                this.removeSelectedEquipmentType(ids);
                break;
        }
    };

    _eqt.createEquipmentType = function (id, name) {
        var title = id ?  title =  $.i18n.prop("edit.equipment.type") : $.i18n.prop("create.equipment.type")
        appTab.renderCreatePanel(app.baseUrl + "eventAdmin/editEquipmentType", title, name, {id: id}, {
            success : function() {
                if(id) {
                    app.global_event.trigger("equipment-type-updated", [id])
                } else {
                    app.global_event.trigger("equipment-type-created")
                }
            }
        })
    }

    _eqt.removeEquipmentType = function (id, name) {
        var _self = this
        bm.remove("equipmentType", $.i18n.prop("equipment.type"), $.i18n.prop("confirm.delete.equipment.type", [name]), app.baseUrl + "eventAdmin/deleteEquipmentType", id, {
            is_final: true,
            success: function () {
                _self.tabulator.reload()
            }
        })
    }

    _eqt.removeSelectedEquipmentType = function(ids) {
        var _self = this;
        bm.confirm($.i18n.prop("confirm.delete.selected.equipment.type"), function() {
            bm.ajax({
                url: app.baseUrl + "eventAdmin/deleteSelectedEquipmentTypes",
                data: {ids: ids},
                success: function () {
                    _self.tabulator.reload()
                }
            })
        },function(){
        });
    }
})
