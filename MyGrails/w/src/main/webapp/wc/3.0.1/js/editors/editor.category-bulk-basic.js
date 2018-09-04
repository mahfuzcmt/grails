 bm.onReady(app.tabs, "categoryBulkEditor", function () {
    var _panel
    var appTab

    app.tabs.categoryBulkEditor.Basic = function (_this, parentTab) {
        _panel = _this
        appTab = parentTab
    }

    var _eb = app.tabs.categoryBulkEditor.Basic.prototype;

    _eb.reload = function () {
        this.tabulator.reload();
    }

    _eb.afterTableReload = function () {
        _panel.clearDirty();
        appTab.body.find(".bmui-tab-body-container").find(".bulkedit-parent-td-proxy").remove();
        var basic = appTab.tab_objs.basic
    }

    _eb.init = function () {
        var _self = this;
        appTab.body.find(".toolbar-item.reload").on("click", function () {
            _self.reload();
        });
        app.global_event.on("category-bulk-updated-basic", function () {
            _self.reload();
        });
        this.tabulator = bm.table(_panel.find(".category-bulk-edit-tab.basic-table"), {
            url: app.baseUrl + "categoryAdmin/loadCategoryBulkProperties",
            beforeReloadRequest: function () {
                _self.beforeReloadRequest(this.param)
            },
            afterLoad: function () {
                _panel.tool.filter(".action-header:visible").hide();
                if (_self.afterTableReload) {
                    _self.afterTableReload()
                }
            },
            onload: function() {
            },
            selectableCellSelect: app.tabs.categoryBulkEditor.selectableCellSelect,
            afterCellSelect: app.tabs.categoryBulkEditor.afterCellSelect,
            afterCellEdit: app.tabs.categoryBulkEditor.afterCellEdit
        });
        _self.beforeReloadRequest = function (param) {
            $.extend(param, {property: "basic", ids: this.ids});
        };

        this.table = _panel.find(".body table");
    }

     function bindCustomerSelection(container, selector, customerField, groupField) {
         container.find(selector).on("click", function(){
             bm.customerAndGroupSelectionPopup(container, {
                 customerField: customerField,
                 groupField: groupField
             })
         })
     }

    app.tabs.categoryBulkEditor.selectableCellSelect = function (td) {
        var select;
        if (td.is(".is-available")) {
            select = $("<select><option value='true'>" + $.i18n.prop('available') + "</option><option value='false'>" + $.i18n.prop('not.available') + "</option></select>")
        } else if (td.is(".is-active")) {
            select = $("<select><option value='true'>" + $.i18n.prop('active') + "</option><option value='false'>" + $.i18n.prop('inactive') + "</option></select>")
        } else if (td.is(".parent-category")) {
            select = app.tabs.categoryBulkEditor.getDropDown("category", "select.category");
        }  else if (td.is(".discount-profile")) {
            select = app.tabs.categoryBulkEditor.getDropDown("discount", "select.discount.profile");
        } else if (td.is(".tax-profile")) {
            select = app.tabs.categoryBulkEditor.getDropDown("tax", "select.tax.profile");
        } else if (td.is(".shipping-profile")) {
            select = app.tabs.categoryBulkEditor.getDropDown("shipping", "select.shipping.profile");
        } else if (td.is(".disable-tracking")) {
            select = $("<select><option value='true'>" + $.i18n.prop('enable') + "</option><option value='false'>" + $.i18n.prop('disable') + "</option></select>")
        } else if (td.is(".restricted-price")) {
            select = $("<select><option value='none'>" + $.i18n.prop('nobody') + "</option><option value='except_customer'>" + $.i18n.prop('only.customers') + "</option><option value='except_selected'>" + $.i18n.prop('selected.customers.only') + "</option></select>").updateUi()
        } else if (td.is(".restricted-purchase")) {
            select = $("<select><option value='none'>" + $.i18n.prop('nobody') + "</option><option value='except_customer'>" + $.i18n.prop('only.customers') + "</option><option value='except_selected'>" + $.i18n.prop('selected.customers.only') + "</option></select>").updateUi()
        }
        return select;
    }

    app.tabs.categoryBulkEditor.afterCellSelect = function (td, value, label, oldVal) {
        td.find("input[type=hidden]").val(value);
        var dispValue = td.find(".disp-value");
        dispValue.html(value ? label : value);
        if (td.is(".is-available")) {
            if (td.is(".custom-select")) {
                app.tabs.categoryBulkEditor.changeAvailability(td, value, label);
            }
            td.find(".status").removeClass(appTab.status[oldVal]).addClass(appTab.status[value]).refreshTooltip(label);
        } else if (td.is(".is-active")) {
            if (td.is(".custom-select")) {
                app.tabs.categoryBulkEditor.changeAadministrativeStatus(td, value, label);
            }
            td.find(".status").removeClass(appTab.status[oldVal]).addClass(appTab.status[value]).refreshTooltip(label);
        } else if (td.is(".product-condition")) {
            if (td.is(".custom-select")) {
                app.tabs.categoryBulkEditor.changeCustomSelect(td, "product-condition", value, label)
            }
            td.find(".status").removeClass(appTab.status[oldVal]).addClass(appTab.status[value]).refreshTooltip(label);
        } else if (td.is(".disable-tracking")) {
            if (td.is(".custom-select")) {
                app.tabs.categoryBulkEditor.changeTracking(td, value, label);
            }
            td.find(".status").removeClass(appTab.status[oldVal]).addClass(appTab.status[value]).refreshTooltip(label);
        } else if (td.is(".on-sale")) {
            var onSale = td.find(".is-on-sale");
            var salePrice = td.find(".sale-price");
            if (value == "true") {
                function updateVal() {
                    var newVal = salePrice.val()
                    if (newVal) {
                        salePrice.hide();
                        dispValue.html(parseFloat(newVal).toFixed(2)).show();
                    } else {
                        app.tabs.categoryBulkEditor.errorHighlight(salePrice);
                        bm.notify($.i18n.prop("this.field.is.required"), "error");
                    }
                }
                dispValue.hide();
                salePrice.show();
                salePrice[salePrice.attr("restrict")]();
                salePrice[0].focus();
                salePrice.on("focusout", function () {
                    updateVal();
                }).on("keydown", function (e) {
                    if (e.keyCode == 13 || e.keyCode == 27) {
                        updateVal();
                    }
                });
            } else {
                salePrice.hide();
            }
        } else if (td.is(".custom-select.discount-profile")) {
            app.tabs.categoryBulkEditor.changeCustomSelect(td, "discount-profile", value, label)
        } else if (td.is(".custom-select.tax-profile")) {
            app.tabs.categoryBulkEditor.changeCustomSelect(td, "tax-profile", value, label)
        } else if (td.is(".custom-select.shipping-profile")) {
            app.tabs.categoryBulkEditor.changeCustomSelect(td, "shipping-profile", value, label)
        } else if (td.is(".custom-select.restricted-price") || td.is(".restricted-price")) {
            if (td.is(".custom-select.restricted-price")) {
                app.tabs.categoryBulkEditor.changeCustomSelect(td, "restricted-price", value, label)
            }
            if (value == 'except_selected') {
                td.find(".restrict-price-except-select-customer").show()
            } else {
                td.find(".restrict-price-except-select-customer").hide()
            }
            bindCustomerSelection(td, ".restrict-price-except-select-customer", "restrictPriceExceptCustomer", "restrictPriceExceptCustomerGroup")
        } else if (td.is(".custom-select.restricted-purchase")) {
            app.tabs.categoryBulkEditor.changeCustomSelect(td, "restricted-purchase", value, label)
            if (value == 'except_selected') {
                td.find(".restrict-purchase-except-select-customer").show()
            } else {
                td.find(".restrict-purchase-except-select-customer").hide()
            }
            bindCustomerSelection(td, ".restrict-purchase-except-select-customer", "restrictPurchaseExceptCustomer", "restrictPurchaseExceptCustomerGroup")
        }
        if (value != null) {
            appTab.activePanel().setDirty();
        }
    }

    app.tabs.categoryBulkEditor.changeTracking = function (td, value, label) {
        if (value) {
            var oldVal = (value == "true") ? "false" : value;
            td.parents("tbody").find("td.disable-tracking").each(function () {
                var tempTd = $(this);
                tempTd.find("input[type=hidden]").val(value);
                tempTd.find(".value").html(value);
                tempTd.find(".status").removeClass(appTab.status[oldVal]).addClass(appTab.status[value]).refreshTooltip(label);
            })
        }
    }

    app.tabs.categoryBulkEditor.changeCustomSelect = function (td, clazz, value, label) {
        value = value == "false" ? "" : value;
        td.parents("tbody").find("td." + clazz).each(function () {
            var tempTd = $(this);
            tempTd.find("input[type=hidden]").val(value);
            tempTd.find(".value").html(value);
            tempTd.find(".disp-value").html(value ? label : value);
        })
    }

    app.tabs.categoryBulkEditor.changeAvailability = function (td, value, label) {
        if (value) {
            var oldVal = (value == "true") ? "false" : value;
            td.parents("tbody").find("td.is-available").each(function () {
                var tempTd = $(this);
                tempTd.find("input[type=hidden]").val(value);
                tempTd.find(".value").html(value);
                tempTd.find(".status").removeClass(appTab.status[oldVal]).addClass(appTab.status[value]).refreshTooltip(label);
            })
        }
    }

    app.tabs.categoryBulkEditor.changeAadministrativeStatus = function (td, value, label) {
        if (value) {
            var oldVal = (value == "true") ? "false" : value;
            td.parents("tbody").find("td.is-active").each(function () {
                var tempTd = $(this);
                tempTd.find("input[type=hidden]").val(value);
                tempTd.find(".value").html(value);
                tempTd.find(".status").removeClass(appTab.status[oldVal]).addClass(appTab.status[value]).refreshTooltip(label);
            })
        }
    }

    app.tabs.categoryBulkEditor.afterCellEdit = function (td, editFieldVal, oldVal) {
        var editField = td.find(".td-full-width");
        var validation = editField.attr("validation");
        if (td.is(".editable") && validation) {
            var errorObj = ValidationField.validateAs(editField, validation);
            if (errorObj) {
                bm.notify($.i18n.prop(errorObj.msg_template, errorObj.msg_params), "error");
                app.tabs.categoryBulkEditor.errorHighlight(editField);
                return false;
            }
            var uniqueAttr = td.attr("unique");
            if (uniqueAttr) {
                var unique = true;
                td.parents("tbody").find("td." + uniqueAttr).each(function () {
                    var tempTd = $(this);
                    var value = tempTd.find("input[type=hidden]").val().trim();
                    if (value == editFieldVal.trim()) {
                        unique = false;
                        return;
                    }
                })
                if (!unique) {
                    bm.notify($.i18n.prop("for.already.exists", [editFieldVal, $.i18n.prop(uniqueAttr)]), "error");
                    app.tabs.categoryBulkEditor.errorHighlight(editField);
                    return false;
                }
            }
        }
        if (td.is(".custom-edit")) {
            td.find(".value").hide();
            if (editFieldVal) {
                var clazz = "0"
                if (td.is(".base-price")) {
                    clazz = "base-price";
                } else if (td.is(".min-order-quantity")) {
                    clazz = "min-order-quantity";
                } else if (td.is(".custom-edit") && td.attr("extra-attr")) {
                    clazz = td.attr("extra-attr");
                }
                app.tabs.categoryBulkEditor.changeAllValues(td, clazz, editFieldVal)
            }
        }
        if (td.is(".editable.name")) {
            app.tabs.categoryBulkEditor.changeAllNameHeading(td, "name", editFieldVal);
        } else if (td.is(".editable.heading")) {
            app.tabs.categoryBulkEditor.changeAllNameHeading(td, "heading", editFieldVal);
        }
        td.find("input[type=hidden]").val(editFieldVal);
    }

    app.tabs.categoryBulkEditor.changeAllNameHeading = function (td, name, editFieldVal) {
        var targetName = td.find("input[name$=" + name + "]").attr("name");
        appTab.allPanel().find("input[name='" + targetName + "']").parent("td").each(function () {
            var tempTd = $(this);
            tempTd.find(".value").html(editFieldVal);
            tempTd.find("input[type=hidden]").val(editFieldVal);
        })
    }

    app.tabs.categoryBulkEditor.changeAllValues = function (td, clazz, editFieldVal) {
        td.parents("tbody").find("td." + clazz).each(function () {
            var tempTd = $(this);
            tempTd.find("input[type=hidden]").val(editFieldVal);
            tempTd.find(".value").html(editFieldVal);
        })
    }

    app.tabs.categoryBulkEditor.errorHighlight = function (item) {
        var tabContainer = appTab.activePanel();
        var updateBtn = tabContainer.find(".submit-button");
        updateBtn.attr("disabled", true);
        item.addClass("error-highlight");
        setTimeout(function () {
            item.removeClass("error-highlight");
            updateBtn.removeAttr("disabled");
        }, 1000);
    }

    app.tabs.categoryBulkEditor.getDropDown = function (domain, tip) {
        var tabContainer = appTab.activePanel();
        var entry = tabContainer.find("input[name=" + domain + "]").val();
        var jsonObj = $.parseJSON(entry);
        var select = $("<select><option value=''>" + $.i18n.prop(tip ? tip : "select") + "</option></select>");
        $.each(jsonObj, function (i, $this) {
            select.append("<option value='" + $this.id + "'>" + $this.name + "</option>");
        })
        return select;
    }

    app.tabs.categoryBulkEditor.attachParentDropDown = function () {
        var basic = appTab.tab_objs.basic
        var selector;
        if(selector) {
            attachParentSelector(selector);
        } else {
            bm.ajax({
                url: app.baseUrl + "categoryAdmin/loadCategoryBulkProperties",
                dataType: "html",
                data: {ids: basic.ids, property: "parent-dom"},
                success: function(resp) {
                    selector = $(resp);
                    attachParentSelector(selector);
                }
            });
        }
        function attachParentSelector(selector) {
            basic.table.find("td.parent").each(function() {
                var td = $(this);
                var _select = selector.filter("." + td.attr("parent"));
                var form = appTab.body.find("#bmui-tab-basic table.content");
                var div = $("<div class='bulkedit-parent-td-proxy chosen-wrapper' style='position: absolute'></div>").appendTo(form);
                div.append(_select);
                function updatePosition() {
                    div.position({
                        my: "left top",
                        at: "left top",
                        of: td,
                        collision: "none",
                        within: basic.table
                    })
                    div.width(td.outerWidth(), true)
                    div.height(td.outerHeight(), true)
                }
                updatePosition();
                appTab.body.find(".bmui-tab-body-container").on("scroll", function() {
                    div.height(td.outerHeight(), true);
                })
                _select.chosen();
                var choices = div.find(".chosen-choices");
                choices.scrollbar();
                choices.css({'max-height': td.outerHeight()});
                td.parents("tr").on("heightChange", function() {
                    updatePosition();
                })
            });
        }

    }
});
