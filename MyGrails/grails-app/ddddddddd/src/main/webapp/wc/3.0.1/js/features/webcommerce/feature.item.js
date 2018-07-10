app.tabs.item = function () {
    this.constructor_args = arguments;
    this.text = $.i18n.prop("products");
    this.tip = $.i18n.prop("manage.products");
    this.ui_class = "products";
    app.tabs.item._super.constructor.apply(this, arguments);
};

var _i = app.tabs.item.inherit(app.ExplorerPanelTab);
_i.sortBtnEnable = false;

(function() {
    function attachEvent() {
        var _self = this;
        this.on_global(["product-restore", "category-restore"], function() {
            _self.reload();
        });
        this.filterList()
        this.body.on("click", ".actions-column .move-controls span", function() {
            var el = $(this), id = el.closest("td").find(".action-navigator").attr("data-id");
            _self.orderTarget = id;
            _self.orderAction = el.attr("order-dir");
            _self.reload();
            _self.orderAction = _self.orderTarget = null;
        });
        this.on_global('default-image-update', function() {
            var img = _self.body.find('.category .image img').attr('src');
            $('<img src="' + img + '">').clearCache(function() {
                img = _self.body.find('.product .image img').attr('src');
                if(img) {
                    $('<img src="' + img + '">').clearCache(function() {
                        app.Tab.getTab('tab-product').reload();
                    })
                } else {
                    app.Tab.getTab('tab-product').reload();
                }
            })
        });
    }
    _i.init = function () {
        app.tabs.item._super.init.call(this);
        attachEvent.call(this);
    };

    app.global_event.on('after-ribbon-render', function(e, ribbonBar) {
        if(app.isPermitted("product.view.list") && app.isPermitted("category.view.list")) {
            ribbonBar.enable("item");
        } else {
            ribbonBar.disable("item");
        }
    });
})();

_i.filterList = function () {
    var _self = this;
    this.body.find(".category-selector").change(function() {
        if (_self instanceof app.tabs.item.product) {
            var menu = _self.tabulator.menu
            if($(this).val() == "") {
                _self.tabulator.sortRemove("8")
                _self.sortBtnEnable = false;
            } else {
                _self.tabulator.sortAdd("8", "idx")
                _self.sortBtnEnable = true;
            }
        }
        _self.body.find(".search-form").trigger("submit");
    });
    this.body.find(".type-selector").change(function() {
        _self.body.find(".search-form").trigger("submit");
    });
}

_i.onSwitchMenuClick = function(type) {
    app.Tab.changeView(this, "item", type, app.tabs.item.ribbon_data.supers[type]);
};

_i.afterCellEdit = function(cell, newValue) {
    var _self = this;
    var type = this instanceof app.tabs.item.product ? "product" : "category";
    bm.ajax({
        url: app.baseUrl + type + "Admin/changeOrder",
        data: {id: cell.attr("entity-id"), value: newValue},
        complete: function () {
            _self.reload();
        }
    })
};

_i.switch_menu_entries = [
    {
        text: $.i18n.prop("categories"),
        ui_class: "view-switch category",
        action: "category"
    },
    {
        text: $.i18n.prop("products"),
        ui_class: "view-switch product",
        action: "product"
    }
];

_i.action_menu_entries = [
    {
        text: $.i18n.prop("import.product.and.category"),
        ui_class: "import",
        action: "import"
    },
    {
        text: $.i18n.prop("export.product.and.category"),
        ui_class: "export",
        action: "export"
    },
    {
        ui_class: "item-separator"
    },
    {
        text: $.i18n.prop("manage.product.owner.permissions"),
        ui_class: "manage-owner-permissions product-permissions",
        action: "manage-product-owner-permissions",
        license: CONSTANTS.LICENSES.ACL
    },
    {
        text: $.i18n.prop("manage.category.owner.permissions"),
        ui_class: "manage-owner-permissions category-permissions",
        action: "manage-category-owner-permissions",
        license: CONSTANTS.LICENSES.ACL
    }
];

_i.create_menu_entries = [
    {
        text: $.i18n.prop("create.product"),
        ui_class: "create-product",
        action: "create-product"
    },
    {
        text: $.i18n.prop("create.category"),
        ui_class: "create-category",
        action: "create-category"
    },
    {
        text: $.i18n.prop("create.combined.product"),
        ui_class: "create-combined-product",
        action: "create-combined-product"
    }
];

_i.onCreateMenuClick = function(action) {
    switch (action) {
        case "create-product":
            var categoryId;
            if(this instanceof app.tabs.item.explorer) {
                categoryId = this.treePanel.find(".body").tree("inst").activeNode.data.id;
            }
            this.createProduct(false, categoryId);
            break;
        case "create-category":
            var categoryId;
            if(this instanceof app.tabs.item.explorer) {
                categoryId = this.treePanel.find(".body").tree("inst").activeNode.data.id;
            }
            this.createCategory(categoryId);
            break;
        case "create-combined-product":
            var categoryId;
            if(this instanceof app.tabs.item.explorer) {
                categoryId = this.treePanel.find(".body").tree("inst").activeNode.data.id
            }
            this.createProduct(true, categoryId);
            break;
    }
};

_i.onActionMenuClick = function(action, item) {
    var _self = this;
    switch (action) {
        case "save-order":
            _self.saveOrder("product");
            break;
        case "import":
            _self.initImportWindow();
            break;
        case "export":
            _self.exportProductAndCategory();
            break;
        case "manage-product-owner-permissions":
            _self.manageProductOwnerPermissions();
            break;
        case "manage-category-owner-permissions":
            _self.manageCategoryOwnerPermissions();
            break;
    }
};

_i.saveOrder = function(type) {
    var _self = this;
    var url = type + "Admin/saveCurrentOrder",
        data = $.extend({}, _self.currentSort);
    bm.ajax({
        url: url,
        data: data,
        success: function() {
            _self.reload();
        }
    })
};

_i.actionMenuClick = function (action, data) {
    switch (action) {
        case "addProduct":
            this.createProduct(false, data.id);
            break;
        case "addSubcategory":
            this.createCategory(data.id);
            break;
        case "editProduct":
            this.editProduct(data.id, data.name);
            break;
        case "copyProduct":
            this.copyProduct(data.id);
            break;
        case "viewInSiteProduct":
            window.open(app.siteBaseUrl + "product/" + data.url + "?adminView=true");
            break;
        case "deleteProduct":
            this.deleteProduct(data.id, data.name);
            break;
        case "editCategory":
            this.editCategory(data.id, data.name);
            break;
        case "viewInSiteCategory":
            window.open(app.siteBaseUrl + "category/" + data.url + "?adminView=true");
            break;
        case "deleteCategory":
            this.deleteCategory(data.id, data.name);
            break;
        case "createCombined":
            this.createCombined(data.id, data.name);
            break;
        case "productPermission":
            this.manageProductEntityPermission(data.id, data.name);
            break;
        case "categoryPermission":
            this.manageCategoryEntityPermission(data.id, data.name);
            break;
    }
};

_i.onActionClick = function() {
    if(arguments.length == 4) {
        this.actionMenuClick(arguments[1], arguments[2]);
    } else {
        this.actionMenuClick(arguments[0], arguments[1])
    }
};

_i.onMenuOpen = function(navigator, config) {
    var menu = [];
    var item;
    var productItem = [
        {
            key: "product.edit.properties",
            class: "edit",
            isEntity: true
        },
        {
            key: "product.create",
            class: "copy-product"
        },
        {
            key: "product.remove",
            class: "delete",
            isEntity: true
        },
        {
            key: "product.edit.permission",
            class: "manage-permission.product-permission",
            isEntity: true
        }
    ];
    var categoryItem = [
        {
            key: "category.edit",
            class: "edit",
            isEntity: true
        },
        {
            key: "category.create",
            class: "add-sub-category"
        },
        {
            key: "product.create",
            class: "add-product"
        },
        {
            key: "category.remove",
            class: "delete",
            isEntity: true
        },
        {
            key: "category.edit.permission",
            class: "manage-permission.category-permission",
            isEntity: true
        }
    ];
    if(navigator == "product" || navigator == "combined") {
        menu = this.explorer.menu[navigator];
        item = productItem
    } else if(navigator == "category") {
        menu = this.explorer.menu[navigator];
        item = categoryItem
    } else {
        menu = this.tabulator.menu;
        config = navigator.config("entity");
        if(config.type == "product") {
            item = productItem
        } else if(config.type == "category"){
            item = categoryItem
        }
    }
    app.checkPermission(menu, item, config);
};
_i.checkPermissionForImportExport = function(menu) {
    if(app.isPermitted("product.import.excel", {}) && app.isPermitted("category.import.excel", {})) {
        menu.enable("import");
    } else {
        menu.disable("import");
    }
    if(app.isPermitted("product.export.excel", {}) && app.isPermitted("category.export.excel", {})) {
        menu.enable("export");
    } else {
        menu.disable("export");
    }
};

_i.onActionMenuOpen = function() {
    var actionMenu = this.action_menu;
    if(this.sortBtnEnable) {
        actionMenu.enable("save-order");
    } else {
        actionMenu.disable("save-order");
    }
    var itemList = [
        {
            key: "product.create",
            class: "create-product"
        },
        {
            key: "category.create",
            class: "create-category"
        },
        {
            key: "product.create",
            class: "create-combined-product"
        },
        {
            key: "category.edit.permission",
            class: "manage-owner-permissions.category-permissions"
        },
        {
            key: "product.edit.permission",
            class: "manage-owner-permissions.product-permissions"
        },
        {
            key: "product.view.list",
            class: "product, .combined"
        },
        {
            key: "category.view.list",
            class: "category"
        }
    ];
    app.checkPermission(actionMenu, itemList);
    this.checkPermissionForImportExport(actionMenu);
};

_i.editProduct = function (id, name, tab) {
    if(!tab) {
        tab = this;
    }
    var editor = new app.editProduct(tab, {id: id, name: name});
    editor.init();
};

_i.editCategory = function (id, name, tab) {
    if(!tab) {
        tab = this;
    }
    var editor = new app.editCategory(tab, {id: id, name: name});
    editor.init();
};

app.tabs.item.editProduct = _i.editProduct;

app.tabs.item.editCategory = _i.editCategory;

_i.createProduct = function (isCombined, categoryId) {
    var _self = this;
    var data = {property: "basic", target: "create", isCombined: isCombined};
    if(categoryId) {
        data.categoryId = categoryId;
    }
    var title = isCombined ? $.i18n.prop("create.combined.product") : $.i18n.prop("create.product");
    this.renderCreatePanel(app.baseUrl + "productAdmin/loadProductProperties", title, null, data, {
        width: 930,
        success: function (res) {
            app.global_event.trigger("product-create");
            _self.editProduct(res.id, res.name)
        },
        content_loaded: function() {
            var panel = this;
            var _form = panel.find("form");
            panel.find(".select-customer").on("click", function(){
                bm.customerAndGroupSelectionPopup(_form, {})
            });
            var newId = bm.getUUID();
            panel.find("#prodAvailFrom").attr("id", newId);
            panel.find("#prodAvailTo").attr("validation", "skip@if{self::hidden} either_required[" + newId + "," + $.i18n.prop("from") + " ," + $.i18n.prop("to") + "]");
        }
    })
};

_i.createCategory = function (categoryId) {
    var data = {property: "basic", target: "create"};
    if(categoryId) {
        data.categoryId = categoryId;
    }
    var _self = this;
    this.renderCreatePanel( app.baseUrl + "categoryAdmin/loadCategoryProperties", $.i18n.prop("create.category"), null, data, {
        width: 920,
        success: function () {
            if(categoryId) {
                app.global_event.trigger("category-update")
            } else  {
                app.global_event.trigger("category-create")
            }
        },
        content_loaded: function() {
                var panel = this;
                var _form = panel.find("form");
                panel.find(".select-customer").on("click", function(){
                    bm.customerAndGroupSelectionPopup(_form,{})
                });
                var newId = bm.getUUID();
                panel.find("#catAvailFrom").attr("id", newId);
                panel.find("#catAvailTo").attr("validation", "skip@if{self::hidden} either_required[" + newId + "," + $.i18n.prop("from") + " ," + $.i18n.prop("to") + "]");
            }
    });
};

app.navigation_item_ref_create_func.category = _i.createCategory;
app.navigation_item_ref_create_func.product = _i.createProduct;

_i.initImportWindow = function() {
    var _self = this;
    var form;

    this.renderCreatePanel(app.baseUrl + "itemImport/uploadFileView", $.i18n.prop("Import Product And Category"), '', undefined, {
        width: 220,
        success: function(resp) {
            _self.configImportedDataMap(resp.html)
        },
        content_loaded: function(popup, _form) {
            form = _form;
            var fileObject = form.find("[name=itemImportFile]");
            fileObject.on("change", function () {
                form.submit();
            });
        },
        beforeSubmit: function(form) {
            form.loader()
        },
        error: function() {
            form.find(".single-file-without-preview").removeClass("file-added");
            form.loader(false);
        }
    });
};

_i.configImportedDataMap = function(resp) {
    this.renderCreatePanel(undefined, $.i18n.prop("Import Product And Category"), '', undefined, {
        width: 495,
        draggable: false,
        content_loaded: function(popup){
            var productImagePathBox = popup.find("[name=productImagePath]");
            var productVideoPathBox = popup.find("[name=productVideoPath]");
            var categoryImagePathBox = popup.find("[name=categoryImagePath]");
            var currentPathBox;

            productImagePathBox.on("focus", function () {
                currentPathBox = productImagePathBox;
                bm.selectFromAssetLibrary($.i18n.prop("select.your.image.path"), pathHandler, undefined, {}, false);
            });
            productVideoPathBox.on("focus", function () {
                currentPathBox = productVideoPathBox;
                bm.selectFromAssetLibrary($.i18n.prop("select.your.video.path"), pathHandler, undefined, {}, false);
            });
            categoryImagePathBox.on("focus", function () {
                currentPathBox = categoryImagePathBox;
                bm.selectFromAssetLibrary($.i18n.prop("select.your.image.path"), pathHandler, undefined, {}, false);
            });

            function pathHandler(pathUrl) {
                currentPathBox.val(pathUrl);
            }

            popup.find(".product-work-sheet").change(function() {
                var workSheetName = popup.find(".product-work-sheet").val();
                bm.ajax({
                    url: app.baseUrl + "itemImport/productMappingFields",
                    dataType: "html",
                    data: {workSheetName: workSheetName},
                    success: function(resp) {
                        popup.find("#product-mapping-wrap").replaceWith(resp);
                        popup.find("#product-mapping-wrap").updateUi();
                    }
                });
            });

            popup.find(".category-work-sheet").change(function() {
                var workSheetName = popup.find(".category-work-sheet").val();
                bm.ajax({
                    url: app.baseUrl + "itemImport/categoryMappingFields",
                    dataType: "html",
                    data: {workSheetName: workSheetName},
                    success: function(resp) {
                        popup.find("#category-mapping-wrap").html(resp).updateUi();
                    }
                });
            });
        },
        success: function(resp) {
            var data = {token: resp.token, name: resp.name, detail_url: app.baseUrl + "itemImport/progressView", detail_status_url: app.baseUrl + "itemImport/progressStatus",
                detail_viewer: app.tabs.item.import_status_viewer};
            TaskManager.createTask(data);
            bm.taskPopup(app.baseUrl + "itemImport/progressView", data, {width: 800, clazz: "task-popup item-import"})
        },
        content: resp
    });
};

app.tabs.item.import_status_viewer = {
    init: function (_popup) {
        var popup = _popup.getDom();
        this.categoryProgressBar = new ProgressBar(popup.find(".category-progress"));
        this.productProgressBar = new ProgressBar(popup.find(".product-progress"));
        this.totalProgressBar = new ProgressBar(popup.find(".total-progress"));
        this.categoryProgressBar.render();
        this.productProgressBar.render();
        this.totalProgressBar.render();
    },
    update: function(_popup, resp) {
        var popup = _popup.getDom();
        this.categoryProgressBar.setPosition(resp.categoryProgress);
        this.productProgressBar.setPosition(resp.productProgress);
        this.totalProgressBar.setPosition(resp.totalProgress);
        popup.find(".category-progress-count").text(resp.categoryProgress + "%");
        popup.find(".category-record-complete").text(resp.categoryComplete);
        popup.find(".category-record-total").text(resp.totalCategoryRecord);
        popup.find(".category-success-count").text(resp.categorySuccessCount);
        popup.find(".category-warning-count").text(resp.categoryWarningCount);
        popup.find(".category-error-count").text(resp.categoryErrorCount);
        popup.find(".product-progress-count").text(resp.productProgress + "%");
        popup.find(".product-record-complete").text(resp.productComplete);
        popup.find(".product-record-total").text(resp.totalProductRecord);
        popup.find(".product-success-count").text(resp.productSuccessCount);
        popup.find(".product-warning-count").text(resp.productWarningCount);
        popup.find(".product-error-count").text(resp.productErrorCount);
        popup.find(".total-progress-count").text(resp.totalProgress + "%");
        popup.find(".total-record-complete").text(resp.recordComplete);
        popup.find(".total-record-total").text(resp.totalRecord);
        popup.find(".total-success-count").text(resp.totalSuccessCount);
        popup.find(".total-warning-count").text(resp.totalWarningCount);
        popup.find(".total-error-count").text(resp.totalErrorCount);
        if(resp.status == "aborted") {
            popup.find(".item-import-aborted").show();
            popup.find(".category-progress .progress-bar .completed").addClass(resp.status);
            popup.find(".product-progress .progress-bar .completed").addClass(resp.status);
            popup.find(".total-progress .progress-bar .completed").addClass(resp.status);
        }
        if (resp.totalProgress == 100) {
            var tab = app.Tab.getTab('tab-item');
            if(tab) {
                tab.reload();
            }
            this.activeLogSummaryLink(popup, resp.token);
            popup.find('.content').append('<div class="button-line">' +
                '<button type="button" class="button close-button">' + $.i18n.prop('close') + '</button>' + ' &nbsp; ' +
                '<a type="button" target="_blank" class="button download-button" href="' + app.baseUrl + 'itemImport/download?token=' + resp.token + '">' + $.i18n.prop('download') + '</a>' +
                '</div>');
            popup.find(".close-button").click(function() {
                _popup.close();
            });
        }
    },
    activeLogSummaryLink: function(_popup, token) {
        var _self = this;
        _popup.find(".total-success-count").addClass('link').click(function () {
            _self.fetchLogSummary(_popup, token, "success")
        });
        _popup.find(".total-warning-count").addClass('link').click(function () {
            _self.fetchLogSummary(_popup, token, "warning")
        });
        _popup.find(".total-error-count").addClass('link').click(function () {
            _self.fetchLogSummary(_popup, token, "error")
        })
    },
    fetchLogSummary: function (_popup, token, type) {
        bm.ajax({
            url: app.baseUrl + "itemImport/" + type + "LogSummary",
            dataType: "html",
            data: {token: token},
            success: function (resp) {
                _popup.find(".log-summary").html(resp).updateUi();
            }
        });
    }
};

_i.exportProductAndCategory = function() {
    var popup_content;
    var form = '<form class="item-export-download" style="display: none" action="'+ app.baseUrl +'itemExport/download"> <input type="hidden" name="fileName"></form>';
    form = $(form);
    bm.editPopup(app.baseUrl + "itemExport/initExport", $.i18n.prop("export.product.and.category"), undefined, {}, {
        width: 430,
        events: {
           content_loaded: function(popup) {
               popup_content = this;
               var tabs = popup_content.find(".bmui-tab");
               var exportProduct = popup_content.find("[name='exportProduct']");
               var exportCategory = popup_content.find("[name='exportCategory']");
               function check($this) {
                   if(exportProduct.checkbox("state") != "checked" && exportCategory.checkbox("state") != "checked" ) {
                       bm.notify($.i18n.prop("can.not.deselect.both"), "alert");
                       $this.checkbox("state", "checked");
                       return false;
                   }
                   return true;
               }
               exportProduct.on("change", function() {
                   var $this = $(this);
                   if(!check($this)) {
                       return;
                   }
                   if(!($this.checkbox("state") == "checked")) {
                       tabs.tabify("activate", "category");
                       tabs.tabify("disable", "product")
                   } else {
                       tabs.tabify("enable", "product")
                   }
               });
               exportCategory.change(function(){
                   var $this = $(this);
                   if(!check($this)) {
                       return;
                   }
                   if(!($this.checkbox("state") == "checked")) {
                       tabs.tabify("activate", "product");
                       tabs.tabify("disable", "category")
                   } else {
                       tabs.tabify("enable", "category")
                   }
               });
           }
       },
       beforeSubmit: function(form, data, popup) {
           setTimeout(function() {
               popup.close()
           }, 5000)
       }
    });
};

_i.manageProductOwnerPermissions = function() {
    bm.permissionPopup($.i18n.prop("manage.owner.permissions"), $.i18n.prop("product"), {for: "owner", type: "product"})
};

_i.manageCategoryOwnerPermissions = function() {
    bm.permissionPopup($.i18n.prop("manage.owner.permissions"), $.i18n.prop("category"), {for: "owner", type: "category"})
};

_i.createCombined = function () {
    app.tabs.item.createProduct(true)
};

_i.createCollection = _i.createCombined;

_i.copyProduct = function (id) {
    var _self = this;
    bm.ajax({
        url: app.baseUrl + "productAdmin/copyProduct",
        data: {id: id},
        success: function () {
            app.global_event.trigger("product-create");
            _self.reload(true)
        }
    })
};

_i.deleteProduct = function (id, name, type) {
    var _self = this;
    var tab = app.Tab.getTab("tab-edit-product-" + id);
    if (tab) {
        bm.notify($.i18n.prop("product.opened.editor.try.again"), "error");
        return;
    }
    bm.remove("product", $.i18n.prop("product"), $.i18n.prop("confirm.delete.product", [name]), app.baseUrl + "productAdmin/deleteProduct", id, {
        success: function () {
            app.global_event.trigger("product-delete", [id]);
            _self.reload(true);
        }
    })
};

_i.deleteSelectedProducts = function (ids) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.product"), function () {
        bm.ajax({
            url: app.baseUrl + "productAdmin/deleteSelected",
            data: {ids: ids},
            success: function () {
                _self.reload();
                app.global_event.trigger("send-trash", ["product", ids]);
                _self.body.find(".action-header").hide();
            }
        })
    }, function () {
    });
};

_i.viewCategory = function (id) {
    bm.viewPopup(app.baseUrl + "categoryAdmin/view", {id: id}, {width: 800});
};

app.tabs.item.viewProduct = function(id){
    bm.viewPopup(app.baseUrl + "productAdmin/view", {id: id}, {width: 800});
};

_i.deleteCategory = function (id, name) {
    var _self = this;
    bm.remove("category", $.i18n.prop("category"), $.i18n.prop("confirm.delete.category", [name]), app.baseUrl + "categoryAdmin/deleteCategory", id, {
        success: function () {
            _self.reload();
        }
    })
};

_i.deleteSelectedCategories = function (ids) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.category"), function () {
        bm.ajax({
            url: app.baseUrl + "categoryAdmin/deleteSelected",
            data: {ids: ids},
            success: function () {
                _self.reload();
                app.global_event.trigger("send-trash", ["category", ids]);
                _self.body.find(".action-header").hide();
            }
        })
    }, function () {
    });
};

_i.bulkEdit = function(ids) {
    var tab = app.Tab.getTab("tab-category-bulk-editor");
    if(!tab) {
        tab = new app.tabs.categoryBulkEditor({
            categoryIds: ids,
            id: "tab-category-bulk-editor"
        });
        tab.render();
        tab.setActive();
    } else {
        bm.notify($.i18n.prop("category.bulk.editor.open.already"), "alert");
    }
};

_i.manageProductEntityPermission = function(id, name) {
    bm.permissionPopup($.i18n.prop("manage.permissions"), name, {id: id, for: "entity", type: "product"})
};

_i.manageCategoryEntityPermission = function(id, name) {
    bm.permissionPopup($.i18n.prop("manage.permissions"), name, {id: id, for: "entity", type: "category"})
};

_i.beforeReloadRequest = function (param) {
    app.tabs.item._super.beforeReloadRequest.call(this, param);
    var categoryFilter = this.body.find("select.category-selector");
    var typeFilter = this.body.find("select.type-selector");
    if (this.advanceSearchFilter) {
        categoryFilter.chosen("val", param.parent)
        typeFilter.chosen("val", param.productType)
    }
    var order = this.orderTarget ? {orderTarget: this.orderTarget, orderAction: this.orderAction} : {}
    $.extend(param, {parent: categoryFilter.val(), productType: typeFilter.val()}, order)
    if(param.sort){
        this.currentSort = {
            sort: param.sort,
            dir: param.dir,
            parent: param.parent
        }
    }
};

_i.clearFilters = function() {
    this.body.find("select.category-selector").chosen("val", "")
};

_i.afterTableReload = function() {
    var _self = this;
    var currentSort = this.currentSort,
        saveOrder = this.body.find(".save-order");
    if(currentSort){
        if(currentSort.sort != "idx" && currentSort.parent != "") {
            saveOrder.show();
        } else {
            saveOrder.hide();
        }
    } else {
        saveOrder.hide();
    }
    _self.body.find(".tool-group.action-header").hide();
};

app.tabs.item.product = function () {
    app.tabs.item.product._super.constructor.apply(this, arguments);
};
app.tabs.item.product.inherit(app.tabs.item);
var _p = app.tabs.item.product.prototype;
(function () {
    function attachEvents() {
        var _self = this;
        this.on_global(["product-create", "product-delete", "product-update", "product-restore"], function () {
            _self.reload(true);
        });
        _self.body.find(".toolbar .create").on("click", function() {
           _self.createProduct()
        });

    }

    _p.init = function () {
        app.tabs.item.product._super.init.call(this);
        attachEvents.call(this)
    }
})();

_p.switch_menu_entries = [
    {
        text: $.i18n.prop("categories"),
        ui_class: "view-switch category",
        action: "category"
    },
    {
        text: $.i18n.prop("explorer.view"),
        ui_class: "view-switch explorer",
        action: "explorer"
    }
];

_p.action_menu_entries = [
    {
        text: $.i18n.prop("save.ordering"),
        ui_class: "save save-order",
        action: "save-order"
    },
    {
        ui_class: "item-separator"
    },
    {
        text: $.i18n.prop("import.product.and.category"),
        ui_class: "import",
        action: "import"
    },
    {
        text: $.i18n.prop("export.product.and.category"),
        ui_class: "export",
        action: "export"
    },
    {
        ui_class: "item-separator"
    },
    {
        text: $.i18n.prop("manage.product.owner.permissions"),
        ui_class: "manage-owner-permissions product-permissions",
        action: "manage-product-owner-permissions",
        license: CONSTANTS.LICENSES.ACL
    }
];

_p.ajax_url = app.baseUrl + "itemAdmin/loadProductView";
_p.advanceSearchUrl = app.baseUrl + "itemAdmin/productAdvanceFilter";
_p.advanceSearchTitle = $.i18n.prop("product");

_p.sortable = {
    list: {
        "1": "name",
        "2": "sku",
        "3": "basePrice",
        "4": "availableStock",
        "5": "isAvailable",
        "6": "created",
        "7": "updated",
    },
    sorted: "2",
    dir: "up"
}

_p.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit",
        action: "editProduct"
    },
    {
        text: $.i18n.prop("copy"),
        ui_class: "copy",
        action: "copyProduct"
    },
    {
        text: $.i18n.prop("view.in.site"),
        ui_class: "view-in-site",
        action: "viewInSiteProduct"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "deleteProduct"
    },
    {
        text: $.i18n.prop("permission"),
        ui_class: "manage-permission product-permission",
        action: "productPermission"
    }

];

_p.onSelectedActionClick = function(action, selecteds) {
    switch (action){
        case "remove":
            this.deleteSelectedProducts(selecteds.collect("id"));
            break;
        case "bulkEdit":
            this.bulkEdit(selecteds.collect("id"));
            break;
    }
};

_p.bulkEdit = function(ids) {
    var tab = app.Tab.getTab("tab-product-bulk-editor");
    if(!tab) {
        tab = new app.tabs.productBulkEditor({
            productIds: ids,
            id: "tab-product-bulk-editor"
        });
        tab.render();
        tab.setActive();
    } else {
        bm.notify($.i18n.prop("product.bulk.editor.open.already"), "alert");
    }
};

app.tabs.item.category = function () {
    app.tabs.item.category._super.constructor.apply(this, arguments);
};
app.tabs.item.category.inherit(app.tabs.item);
var _c = app.tabs.item.category.prototype;
_c.ajax_url = app.baseUrl + "itemAdmin/loadCategoryView";
_c.advanceSearchUrl = app.baseUrl + "itemAdmin/categoryAdvanceFilter";
_c.advanceSearchTitle = $.i18n.prop("category");

(function () {
    function attachEvents() {
        var _self = this;
        this.on_global(["category-create","category-update", "category-delete"], function(){
            bm.updateCategorySelector(_self.body.find(".category-selector"), "webcommerce.Category");
            _self.reload();
        });
        this.body.find(".toolbar .create").on('click', function() {
           _self.createCategory();
        });
    }

    _c.init = function () {
        app.tabs.item.category._super.init.call(this);
        attachEvents.call(this)
    }
})();

_c.switch_menu_entries = [
    {
        text: $.i18n.prop("products"),
        ui_class: "view-switch product",
        action: "product"
    },
    {
        text: $.i18n.prop("explorer.view"),
        ui_class: "view-switch explorer",
        action: "explorer"
    }
];

_c.action_menu_entries = [
    {
        text: $.i18n.prop("import.product.and.category"),
        ui_class: "import",
        action: "import"
    },
    {
        text: $.i18n.prop("export.product.and.category"),
        ui_class: "export",
        action: "export"
    },
    {
        ui_class: "item-separator"
    },
    {
        text: $.i18n.prop("manage.category.owner.permissions"),
        ui_class: "manage-owner-permissions category-permissions",
        action: "manage-category-owner-permissions",
        license: CONSTANTS.LICENSES.ACL
    }
];

_c.sortable = {
    list: {
        "1": "availableFor",
        "2": "name",
        "3": "sku",
        "5": "created",
        "6": "updated",
        "8": "idx"
    },
    sorted: "2",
    dir: "up"
};

_c.menu_entries = [
    {
        text: $.i18n.prop("add.product"),
        ui_class: "add-product",
        action: "addProduct"
    },
    {
        text: $.i18n.prop("add.subcategory"),
        ui_class: "add-sub-category",
        action: "addSubcategory"
    },
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit",
        action: "editCategory"
    },
    {
        text: $.i18n.prop("view.in.site"),
        ui_class: "view-in-site",
        action: "viewInSiteCategory"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "deleteCategory"
    },
    {
        text: $.i18n.prop("permission"),
        ui_class: "manage-permission category-permission",
        action: "categoryPermission"
    }
];

_c.onSelectedActionClick = function(action, selecteds) {
    switch (action){
        case "remove":
            this.deleteSelectedCategories(selecteds.collect("id"));
            break;
        case "bulkEdit":
            this.bulkEdit(selecteds.collect("id"));
            break;
    }
};

app.tabs.item.explorer = function () {
    app.tabs.item.explorer._super.constructor.apply(this, arguments);
};

var _e = app.tabs.item.explorer.inherit(app.tabs.item);

app.ribbons.web_commerce.push(app.tabs.item.ribbon_data = {
    text: $.i18n.prop("products"),
    ui_class: "item",
    ecommerce: true,
    processor: app.tabs.item.explorer,
    views: [
        {ui_class: "category", text: $.i18n.prop("category"), permission: "category.view.list"},
        {ui_class: "product", text: $.i18n.prop("product"), permission: "product.view.list"}
    ],
    supers: {
        explorer: "ExplorerPanelTab",
        category: "SingleTableTab",
        product: "SingleTableTab"
    }
});

_e.ajax_url = app.baseUrl + "itemAdmin/loadExplorerView";
_e.explorer_url = app.baseUrl + "itemAdmin/explorePanel";
_e.menu_entries = {
    category: _c.menu_entries,
    product: _p.menu_entries,
    combined: _p.menu_entries
};
_e.tree_node_load_url = app.baseUrl + "itemAdmin/categoryTree";
_e.root_node_name = $.i18n.prop("root.category");
_e.advanceSearchUrl = app.baseUrl + "itemAdmin/explorerAdvanceFilter";

(function () {
    function attachEvents() {
        var _self = this;
        this.on_global(["category-create", "product-create", "category-update", "product-update", "category-image-change", "product-image-change"], function () {
            _self.reload();
        });
    }

    _e.init = function () {
        app.tabs.item.explorer._super.init.call(this);
        attachEvents.call(this)
    }
})();