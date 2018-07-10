app.editCategory = function (appTab, category) {
    this.itemTab = appTab;
    this.category = category;
    app.editCategory._super.constructor.call(this, arguments);
};

(function () {
    var _ec = app.editCategory.inherit(app.MultiTab);
    var _super = app.editCategory._super;
    _ec.changeHeader = false;

    _ec.init = function() {
        var _self = this;
        _self.itemTab.renderCreatePanel(app.baseUrl + "categoryAdmin/loadCategoryEditor", $.i18n.prop("edit.category"), _self.category.name, {id: _self.category.id}, {
            createPanelTemplate: $('<div class="embedded-edit-form-panel create-panel fade-in-up"><div class="header"><span class="header-title"></span><span class="toolbar toolbar-right"><span class="tool-group toolbar-btn save save-all">' + $.i18n.prop("save.all")+ '</span><span class="tool-group toolbar-btn cancel">' + $.i18n.prop("cancel") + '</span></span></div><div class="body"></div></div>'),
            ajax: {
                show_success_status: false
            },
            submit_n_cancle: false,
            content_loaded: function (template) {
                _self.body = this;
                _self.header = this.find(".header");
                _super.init.apply(_self, arguments);
            }
        });
    };

    function bindCustomerSelection(panel, selector, customerField, groupField) {
        panel.find(selector).on("click", function(){
            bm.customerAndGroupSelectionPopup(panel, {
                customerField: customerField,
                groupField: groupField
            })
        })
    }

    app.editCategory.tabInitFuncs = {
        productSettings: function(panel) {
            var _form = panel.find("form");
            bindCustomerSelection(_form, ".restrict-price-except-select-customer", "restrictPriceExceptCustomer", "restrictPriceExceptCustomerGroup");
            bindCustomerSelection(_form, ".restrict-purchase-except-select-customer", "restrictPurchaseExceptCustomer", "restrictPurchaseExceptCustomerGroup")
        },
        basic: function (panel) {
            var category = this.category;
            var _form = panel.find("form");
            panel.find(".select-customer").on("click", function () {
                var navigator = $(this);
                var customers;
                var customerGroups;
                if (_form.find("[name='isCustomerSelectorDirty']").length) {
                    bm.customerAndGroupSelectionPopup(_form, {}).on("close", function (e, error) {
                        if (!error) {
                            navigator.trigger("change");
                        }
                    })
                } else {
                    bm.customerAndGroupSelectionPopup(_form, { url: app.baseUrl + "categoryAdmin/customerSelectionPopup?id=" + category.id}).on("close", function (e, error) {
                        if (!error) {
                            navigator.trigger("change");
                        }
                    })
                }
            });
            var newId = bm.getUUID();
            panel.find("#catAvailFrom").attr("id", newId);
            panel.find("#catAvailTo").attr("validation", "skip@if{self::hidden} either_required[" + newId + "," + $.i18n.prop("from") + " ," + $.i18n.prop("to") + "]");
        },
        metatags: function (panel) {
            bm.metaTagEditor(panel);
        },
        link: function (panel) {
            bm.initProductSelection(panel, "linked", {
                "column-sort": false
            });
        }
    };

    _ec.save = function (callback) {
        var _self = this;
        bm.iterate(this.panels, function (handle, index) {
            var panel = this;
            if (panel.isDirty) {
                panel.find("form").form("submit", {
                    success: function () {
                        panel.clearDirty();
                        if (!_self.isDirty()) {
                            if (callback) {
                                callback();
                            }
                        } else {
                            handle.next();
                        }
                    },
                    error: function () {
                        _self.setActiveTab(index);
                        handle.next();
                    },
                    invalid: function () {
                        _self.setActiveTab(index);
                        _self.find("form").valid("position");
                        handle.next();
                    }
                })
            } else {
                handle.next();
            }
        })
    };

    _ec.onContentLoad = function (data) {
        var _self = this;
        data.panel.find("form").form({
            ajax: {
                success: function () {
                    app.global_event.trigger("category-update", [_self.category.id]);
                    data.panel.clearDirty();
                }
            }
        });
        if (typeof app.editCategory.tabInitFuncs[data.index] == "function") {
            app.editCategory.tabInitFuncs[data.index].call(this, data.panel);
        }
    }
})();