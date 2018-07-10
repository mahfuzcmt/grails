app.tabs.customer = function () {
    this.constructor_args = arguments;
    this.text = $.i18n.prop(app.ecommerce.bool()?"customers":"members");
    this.tip = $.i18n.prop("manage.customers", [app.ecommerce.bool()?"Customers":"Members"]);
    this.ui_class = "customers";
    app.tabs.customer._super.constructor.apply(this, arguments);
};

var _cust = app.tabs.customer.inherit(app.SingleTableTab);

(function () {
    function attachEvent() {
        var _self = this;
        this.on_global("customer-restore", function () {
            _self.reload();
        });
        this.on("close", function () {
            app.tabs.customer.tab = null;
        });
    }

    _cust.init = function () {
        var _self = this;
        app.tabs.customer._super.init.call(this);
        app.tabs.customer.tab = this;
        attachEvent.call(this);

        app.global_event.on("customer-update", function () {
            _self.reload();
        })
    };
    app.global_event.on('after-ribbon-render', function (e, ribbonBar) {
        if (app.isPermitted("customer.view.list")) {
            ribbonBar.enable("customer");
        } else {
            ribbonBar.disable("customer");
        }
    });

})();

_cust.onSwitchMenuClick = function (type) {
    app.Tab.changeView(this, "customer", type, type == "explorer" ? "ExplorerPanelTab" : "SingleTableTab");
};

_cust.onActionMenuClick = function (action) {
    switch (action) {
        case "import-customer":
            this.initCustomerImport();
            break;
        case "export-customer":
            this.exportCustomer();
            break;
        case "create-customer-group":
            this.createCustomerGroup();
            break;
    }
};

app.tabs.customer.customers = function () {
    app.tabs.customer.customers._super.constructor.apply(this, arguments);
};

app.ribbons.administration.push(app.tabs.customer.ribbon_data = {
    text: $.i18n.prop(app.ecommerce.bool()?"customer":"member"),
    ui_class: "customer",
    processor: app.tabs.customer.customers,
    views: [
        {ui_class: "customerGroups", text: $.i18n.prop("customer.group", [app.ecommerce.bool()?"Customer":"Member"])}
    ]
});

var _c = app.tabs.customer.customers.inherit(app.tabs.customer);

_c.ajax_url = app.baseUrl + "customerAdmin/loadAppView";
_c.advanceSearchUrl = app.baseUrl + "customerAdmin/advanceFilter";
_c.advanceSearchTitle = $.i18n.prop(app.ecommerce.bool()?"customers":"members");
_c.advance_search_popup_width = 600;

(function () {
    _c.init = function () {
        var _self = this;
        this.body.find(".toolbar .create").on("click", function () {
            _self.createCustomer();
        });
        app.tabs.customer.customers._super.init.apply(this, arguments)
    }
})();

_c.switch_menu_entries = [
    {
        text: $.i18n.prop("customer.group.list", [app.ecommerce.bool()?"Customer":"Member"]),
        ui_class: "view-switch customer-group list-view",
        action: "customerGroups"
    }
];

_c.action_menu_entries = [
    {
        text: $.i18n.prop("import.customer", [app.ecommerce.bool()?"Customer":"Member"]),
        ui_class: "import-customer",
        action: "import-customer"
    },
    {
        text: $.i18n.prop("export.customer", [app.ecommerce.bool()?"Customer":"Member"]),
        ui_class: "export-customer",
        action: "export-customer"
    }

];

_c.sortable = {
    list: {
        "1": "firstName",
        "2": "lastName",
        "3": "userName",
        "4": "storeCredit",
        "6": "created"
    },
    sorted: "1",
    dir: "up"
};

_c.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit"
    },
    {
        text: $.i18n.prop("adjust.store.credit"),
        ui_class: "adjust-store-credit"
    },
    {
        text: $.i18n.prop("store.credit.history"),
        ui_class: "store-credit-history"
    },
    {
        text: $.i18n.prop("reset.password"),
        ui_class: "reset-password"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "remove"
    }
];

_c.onMenuOpen = function (navigator) {
    var menu = this.tabulator.menu;
    var item = [
        {
            key: "customer.edit.properties",
            class: "edit"
        },
        {
            key: "customer.adjust.store.credit",
            class: "adjust-store-credit"
        },
        {
            key: "customer.view.store.credit.history",
            class: "store-credit-history"
        },
        {
            key: "customer.remove",
            class: "remove"
        }
    ];
    app.checkPermission(menu, item);
};

_c.onSelectedActionClick = function (action, selecteds) {
    switch (action) {
        case "remove":
            this.deleteSelectedCustomers(selecteds.collect("id"));
            break;
        case "bulkEdit":
            this.bulkEdit(selecteds.collect("id"));
            break;
    }
};

_c.onActionClick = function (action, data) {
    switch (action) {
        case "edit":
            this.editCustomer(data.id, data.name);
            break;
        case "adjust-store-credit":
            this.adjustStoreCredit(data.id, data.name);
            break;
        case "store-credit-history":
            this.storeCreditHistory(data.id, data.name);
            break;
        case "reset-password":
            this.resetPassword(data.id, data.name);
            break;
        case "remove":
            this.deleteCustomer(data.id, data.name);
            break;
    }
};

_c.createCustomer = function (config) {
    var title = $.i18n.prop("create.customer", [app.ecommerce.bool()?"Customer":"Member"]);
    config = $.extend({
        success: function () {
            app.global_event.trigger("customer-create");
            if (app.tabs.customer.tab) {
                app.tabs.customer.tab.reload()
            }
        },
        content_loaded: function (form) {
            this.countryChange = bm.countryChange(form, {stateName: "state.id"});
            bm.initCityValidator(form.find("[name=postCode]"), null, "state.id");
        }
    }, config);
    this.renderCreatePanel(app.baseUrl + "customerAdmin/create", title, "", {}, config);
};

_c.viewCustomer = function (id) {
    bm.viewPopup(app.baseUrl + "customerAdmin/view", {id: id});
};

_c.editCustomer = function (id, name) {
    var _self = this;
    this.renderCreatePanel(app.baseUrl + "customerAdmin/create", $.i18n.prop("edit.customer", [app.ecommerce.bool()?"Customer":"Member"]), name, {id: id}, {
        success: function () {
            _self.reload();
        },
        content_loaded: function (popup) {
            this.countryChange = bm.countryChange($(this), {stateName: "state.id"});
            bm.initCityValidator(popup.find("[name=postCode]"), null, "state.id");
        }
    });
};

_c.adjustStoreCredit = function (id, name) {
    var _self = this;
    bm.editPopup(app.baseUrl + "customerAdmin/loadStoreCredit", $.i18n.prop("adjust.store.credit"), name, {id: id}, {
        success: function () {
            _self.reload()
        }
    });
};

_c.storeCreditHistory = function (id, name) {
    var url = app.baseUrl + "customerAdmin/loadStoreCreditHistory";
    bm.editPopup(url, $.i18n.prop("store.credit.history"), name, {id: id}, {
        width: 850,
        success: function () {
        },
        events: {
            content_loaded: function () {
                var content = $(this);
                var bindPaginator = function () {
                    var paginator = content.find(".pagination").obj();
                    if (paginator) {
                        paginator.onPageClick = function (page) {
                            bm.ajax({
                                url: url,
                                data: {
                                    id: id,
                                    offset: (page - 1) * 5
                                },
                                dataType: "html",
                                success: function (resp) {
                                    $(content.find(".table-view")).html($(resp)).updateUi();
                                    bindPaginator()
                                }
                            })
                        }
                    }
                };
                bindPaginator()
            }
        }
    });
};

_c.resetPassword = function (id, name) {
    var _self = this;
    bm.editPopup(app.baseUrl + "customerAdmin/resetPassPopup", $.i18n.prop("reset.password"), name, {id: id})
};
_c.deleteCustomer = function (id, name) {
    var _self = this;
    bm.remove("customer", "Customer", $.i18n.prop("confirm.delete.customer", [app.ecommerce.bool()?"Customer":"Member", name]), app.baseUrl + "customerAdmin/delete", id, {
        success: function () {
            app.global_event.trigger("delete-customer", [id]);
            _self.reload();
        }
    })
};

_c.deleteSelectedCustomers = function (ids) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.customer", [app.ecommerce.bool()?"Customers":"Members"]), function () {
        bm.ajax({
            url: app.baseUrl + "customerAdmin/deleteSelected",
            data: {ids: ids},
            success: function () {
                _self.reload();
                app.global_event.trigger("delete-customer", [ids]);
                app.global_event.trigger("send-trash", ["customer", ids]);
                _self.body.find(".action-header").hide();
            }
        })
    }, function () {
    });
};

_c.bulkEdit = function(ids) {
    var tab = app.Tab.getTab("tab-customer-bulk-editor");
    if(!tab) {
        tab = new app.tabs.customerBulkEditor({
            customerIds: ids,
            id: "tab-customer-bulk-editor"
        });
        tab.render();
        tab.setActive();
    } else {
        bm.notify($.i18n.prop("customer.bulk.editor.open.already", [app.ecommerce.bool()?"Customer":"Member"]), "alert");
    }
};

_c.exportCustomer = function () {
    var popup_content;
    var _self = this;
    var data = {};
    if (_self.advanceSearchFilter) {
        data.filter = JSON.stringify(_self.advanceSearchFilter)
    } else if (_self.simpleSearchText) {
        data.filter = '{"searchText": "' + _self.simpleSearchText + '"}';
    }
    bm.editPopup(app.baseUrl + "customerExportImport/initExport", $.i18n.prop("export.customer", [app.ecommerce.bool()?"Customer":"Member"]), undefined, data, {
        width: 420,
        events: {
            content_loaded: function (popup) {

            }
        },
        beforeSubmit: function (form, data, popup) {
            setTimeout(function () {
                popup.close()
            }, 2000)
        }
    });
};

_c.initCustomerImport = function () {
    bm.editPopup(app.baseUrl + "customerExportImport/uploadImportFile", $.i18n.prop("import.customer", [app.ecommerce.bool()?"Customer":"Member"]), '', {}, {
        success: function (resp) {
            var data = {
                token: resp.token,
                name: resp.name,
                detail_url: app.baseUrl + "customerExportImport/progressView",
                detail_status_url: app.baseUrl + "customerExportImport/progressStatus",
                detail_viewer: app.tabs.customer.import_status_viewer
            };
            TaskManager.createTask(data);
            bm.taskPopup(app.baseUrl + "customerExportImport/progressView", data, {width: 800, top: 100})
        }
    })
};

app.tabs.customer.import_status_viewer = {
    init: function (_popup) {
        var popup = _popup.getDom();
        this.totalProgressBar = new ProgressBar(popup.find(".progress"));
        this.totalProgressBar.render();
    },
    update: function (_popup, resp) {
        var popup = _popup.getDom();
        this.totalProgressBar.setPosition(resp.totalProgress);
        popup.find(".progress-count").text(resp.totalProgress + "%");
        popup.find(".record-complete").text(resp.complete);
        popup.find(".record-total").text(resp.totalRecord);
        popup.find(".success-count").text(resp.successCount);
        popup.find(".warning-count").text(resp.warningCount);
        popup.find(".error-count").text(resp.errorCount);
        if (resp.status == "aborted") {
            popup.find(".import-aborted").show();
            popup.find(".progress .progress-bar .completed").addClass(resp.status);
        }
        if (resp.totalProgress == 100) {
            var customerTab = app.Tab.getTab("tab-customer");
            if (customerTab) {
                customerTab.reload()
            }
            this.activeLogSummaryLink(popup, resp.token);
            popup.find('.content').append('<div class="button-line">' +
                '<button type="button" class="button close-button">' + $.i18n.prop('close') + '</button>' + ' &nbsp; ' +
                '<a type="button" target="_blank" class="button download-button" href="' + app.baseUrl + 'customerExportImport/download?token=' + resp.token + '">' + $.i18n.prop('download') + '</a>' +
                '</div>');
            popup.find(".close-button").click(function () {
                _popup.close();
            });
        }
    },
    activeLogSummaryLink: function (_popup, token) {
        var _self = this;
        _popup.find(".success-count").addClass('link').click(function () {
            _self.fetchLogSummary(_popup, token, "success")
        });
        _popup.find(".warning-count").addClass('link').click(function () {
            _self.fetchLogSummary(_popup, token, "warning")
        });
        _popup.find(".error-count").addClass('link').click(function () {
            _self.fetchLogSummary(_popup, token, "error")
        })
    },
    fetchLogSummary: function (_popup, token, type) {
        bm.ajax({
            url: app.baseUrl + "customerExportImport/" + type + "LogSummary",
            dataType: "html",
            data: {token: token},
            success: function (resp) {
                _popup.find(".log-summary").html(resp).updateUi();
                _popup.find(".log-summary").find("table").parent().scrollbar({
                    vertical: {
                        offset: 0,
                        step: "auto",
                    }
                });
            }
        });
    }
};

app.tabs.customer.customerGroups = function () {
    app.tabs.customer.customerGroups._super.constructor.apply(this, arguments);
};

var _g = app.tabs.customer.customerGroups.inherit(app.tabs.customer);
(function () {
    _g.init = function () {
        var _self = this;
        this.body.find(".toolbar .create").on("click", function () {
            _self.createCustomerGroup();
        });
        app.tabs.customer.customerGroups._super.init.apply(this, arguments)
    }
})();

_g.ajax_url = app.baseUrl + "customerGroup/loadAppView";
_g.advanceSearchUrl = app.baseUrl + "customerGroup/advanceFilter";
_g.advanceSearchTitle = $.i18n.prop("customer.group", [app.ecommerce.bool()?"Customer":"Member"]);

_g.switch_menu_entries = [
    {
        text: $.i18n.prop("customer.list", [app.ecommerce.bool()?"Customer":"Member"]),
        ui_class: "view-switch customers list-view",
        action: "customers"
    }
];

_g.sortable = {
    list: {
        "1": "name",
        "4": "created"
    },
    sorted: "2",
    dir: "up"
};

_g.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "remove"
    }
];

_g.onSelectedActionClick = function (action, selecteds) {
    switch (action) {
        case "assign_customer":
            this.assignCustomer(selecteds);
            break;
        case "status":
            this.changeStatus(selecteds);
            break;
        case "remove":
            this.deleteSelectedCustomerGroups(selecteds.collect("id"));
            break;
    }
};

_g.onActionClick = function (action, data) {
    switch (action) {
        case "edit":
            this.editCustomerGroup(data.id, data.name);
            break;
        case "remove":
            this.deleteCustomerGroup(data.id, data.name);
            break;
    }
};

_g.editCustomerGroup = function (id, name) {
    var _self = this, title = id ? $.i18n.prop("edit.customer.group", [app.ecommerce.bool()?"Customer":"Member"]) : $.i18n.prop("create.customer.group", [app.ecommerce.bool()?"Customer":"Member"]);
    this.renderCreatePanel(app.baseUrl + "customerGroup/create", title, name, {id: id}, {
        width: 850,
        content_loaded: function () {
            var _self = this;
            var customerLeftPanelUrl = "customerAdmin/loadCustomerForMultiSelect";
            var groupLeftPanelUrl = "customerGroup/loadCustomerGroupForMultiSelect";
            var cncSelector = bm.twoSideSelection(_self, 10, "customer", customerLeftPanelUrl, {
                view: false,
                edit: false,
                "column-sort": false
            }, ["customer"], {resetRightPanel: false});
            var removeSearchBtn = $('<span class="tool-icon remove-search" style="display: none"></span>');
            var searchText = _self.find("input.search-text");
            var searchForm = _self.find(".search-form");
            searchForm.prepend(removeSearchBtn);
            cncSelector.beforeLoadTableContent = function (params) {
                var _param = {
                    searchText: searchText.val()
                };
                $.extend(params, _param);
                if (searchText.val()) {
                    removeSearchBtn.show();
                } else {
                    removeSearchBtn.hide();
                }
            };
            _self.find("select[name='selection-type']").change(function () {
                if (this.value == 'customer') {
                    cncSelector.setUrl(customerLeftPanelUrl, "customer", {
                        view: false,
                        edit: false,
                        "column-sort": true
                    }, ["customer"])
                } else {
                    cncSelector.setUrl(groupLeftPanelUrl, "customer-group", {
                        view: false,
                        edit: false,
                        "column-sort": true
                    }, ["customerGroup"])
                }
            });
            searchForm.form({
                disable_on_submit: false,
                preSubmit: function () {
                    cncSelector.reload();
                    return false;
                }
            });

            removeSearchBtn.on("click", function () {
                searchText.val("");
                cncSelector.reload();
            })
        },
        success: function () {
            _self.reload();
        }
    });
};

_g.createCustomerGroup = _g.editCustomerGroup;

_g.deleteCustomerGroup = function (id, name) {
    var _self = this;
    bm.remove("customer-group", "Customer Group", $.i18n.prop("confirm.delete.customer.group", [app.ecommerce.bool()?"Customer":"Member", name]), app.baseUrl + "customerGroup/delete", id, {
        is_final: true,
        success: function () {
            _self.reload();
        }
    })
};

_g.deleteSelectedCustomerGroups = function (ids) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.customer.group", [app.ecommerce.bool()?"Customer":"Member"]), function () {
        bm.ajax({
            url: app.baseUrl + "customerGroup/deleteSelected",
            data: {ids: ids},
            success: function () {
                _self.reload();
                _self.body.find(".action-header").hide();
            }
        })
    }, function () {
    });
};

_g.changeStatus = function(selecteds) {
    var _self = this;
    bm.editPopup(app.baseUrl + 'customerGroup/loadStatusOption', $.i18n.prop('status'), null , {},  {
        width: 600,
        events: {
            content_loaded: function() {
                var $this = $(this);
                var ids = selecteds.collect("id");
                $.each(ids, function (index, value) {
                    $this.find(".edit-popup-form").append('<input type="hidden" name="id" value="' + value + '">');
                });
            }
        },
        success: function () {
            _self.reload();
        }
    });
};

_g.assignCustomer = function(selecteds) {
    var _self = this;
    bm.editPopup(app.baseUrl + 'customerGroup/loadCustomerOption', $.i18n.prop('assign.customer', [app.ecommerce.bool()?"Customer":"Member"]), null , {},  {
        width: 600,
        events: {
            content_loaded: function() {
                var _self = this;
                var $this = $(this);
                var ids = selecteds.collect("id");
                $.each(ids, function (index, value) {
                    $this.find(".edit-popup-form").append('<input type="hidden" name="id" value="' + value + '">');
                });

                var customerLeftPanelUrl = "customerAdmin/loadCustomerForMultiSelect";
                var groupLeftPanelUrl = "customerGroup/loadCustomerGroupForMultiSelect";
                var cncSelector = bm.twoSideSelection(_self, 10, "customer", customerLeftPanelUrl, {
                    view: false,
                    edit: false,
                    "column-sort": false
                }, ["customer"]);
                var removeSearchBtn = $('<span class="tool-icon remove-search" style="display: none"></span>');
                var searchText = _self.find("input.search-text");
                var searchForm = _self.find(".search-form");
                searchForm.prepend(removeSearchBtn);
                cncSelector.beforeLoadTableContent = function (params) {
                    var _param = {
                        searchText: searchText.val()
                    };
                    $.extend(params, _param);
                    if (searchText.val()) {
                        removeSearchBtn.show();
                    } else {
                        removeSearchBtn.hide();
                    }
                };
                _self.find("select[name='selection-type']").change(function () {
                    if (this.value == 'customer') {
                        cncSelector.setUrl(customerLeftPanelUrl, "customer", {
                            view: false,
                            edit: false,
                            "column-sort": true
                        }, ["customer"])
                    } else {
                        cncSelector.setUrl(groupLeftPanelUrl, "customer-group", {
                            view: false,
                            edit: false,
                            "column-sort": true
                        }, ["customerGroup"])
                    }
                });
                searchForm.form({
                    disable_on_submit: false,
                    preSubmit: function () {
                        cncSelector.reload();
                        return false;
                    }
                });

                removeSearchBtn.on("click", function () {
                    searchText.val("");
                    cncSelector.reload();
                })
            }
        },
        success: function () {
            _self.reload();
        }
    });
};


