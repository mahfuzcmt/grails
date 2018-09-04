app.tabs.variation = function () {
    this.text = $.i18n.prop("variations");
    this.tip = $.i18n.prop("manage.variations");
    this.ui_class = "variations";
    this.notEditable = true;
    this.disableScroll = true;
    app.tabs.variation._super.constructor.apply(this, arguments);
};

var _v = app.tabs.variation.inherit(app.MultiTab);

app.ribbons.web_commerce.push(app.tabs.variation.ribbon_data = {
    text: $.i18n.prop("variation"),
    ui_class: "variation",
    processor: app.tabs.variation,
    license: "allow_variation_feature",
    ecommerce: true
});

_v.ajax_url = app.baseUrl + "variationAdmin/loadAppView";

_v.init = function() {
    var _self = this;
    this.views = {};
    this.body.find(".bmui-tab").on("tab:activate", function(ev, data) {
        if(!data.newPanel.is(".bmui-tab-loading")) {
            _self.c_tab = _self.views[data.newTab.attr("data-tabify-tab-id")];
            _self.reload()
        }
    });
    app.tabs.variation._super.init.apply(this, arguments)
};

_v.onContentLoad = function(data) {
    var _self = this;
    var view = new app.SingleTableView();
    view.tool = data.panel.tool;
    view.body = data.panel;
    view.appTab = this;
    view.ajax_url = data.tab.attr("data-tabify-url");
    view.selectableCellSelect = data.panel.find(".last-row select").clone();
    view.afterTableReload = $.proxy(this.afterTableReload, this);
    var type = data.tab.attr("data-tabify-tab-id");
    _self.tab_objs[type] = view.appTab;
    view.tab_type = type;
    this.views[type] = this.c_tab = view;
    var typeSelector = view.body.tool.find("[name='typeSelector']");
    typeSelector.on("change", function() {
        _self.reload()
    });
    view.beforeReloadRequest = function(param) {
        param.search = view.appTab.simpleSearchText;
        param.isDisposable = typeSelector.val() == "disposable"
    };
    switch(type) {
        case "type":
            view.afterCellEdit = $.proxy(this.afterTypeCellEdit, this);
            view.afterCellSelect = $.proxy(this.afterCellSelect, this);
            view.beforeCellSelect = $.proxy(this.beforeCellSelect, this);

            view.init();
            this.attachTypeEvents(view);
            break;
        case "option":
            view.afterCellEdit = $.proxy(this.afterOptionCellEdit, this);
            view.init();
            this.attachOptionEvents(view);
            break;
    }

};

/****************************** Type Tab **********************************/
_v.attachTypeEvents = function(tab) {
    var _self = this;
    tab.tool.find(".reload").click(function() {
        _self.reload()
    })
};

_v.attachTypeTableEvents = function(panel) {
    var _self = this;
    var lastRow = panel.find(".last-row");
    panel.find(".add-row").on("click", function() {
        var isValid = lastRow.valid({
            show_error: false,
            validate_on_call_only: true
        });
        if(!isValid) {
            return;
        }
        var name =  panel.find("[name=name]").val();
        var standard = panel.find("[name=standard]").val();
        _self.updateType(null, name, standard);
    });

    panel.find(".remove").on("click", function() {
        _self.removeType($(this));
    });

    lastRow.on("invalid", function(event, obj) {
        bm.notify($.i18n.prop(obj.msg_template, obj.msg_params), "alert");
        app.variationUtils.errorHighlight(obj.validator.elm, $.i18n.prop("name.is.required"));
    });

    lastRow.on("keypress.key_return", ":input", function() {
        panel.find(".add-row").trigger("click");
    });
};

_v.updateType = function(id, name, standard, rejectHandler) {
    var _self = this;
    var data = {id: id, name: name, standard: standard};
    bm.ajax({
        controller: "variationAdmin",
        action: "saveType",
        data: data,
        show_success_status: false
    }).done(function() {
        _self.reload();
        app.global_event.trigger("variation-type-update");
    }).error(function() {
        if(rejectHandler) rejectHandler()
    })
};

_v.removeType = function(button) {
    var _self = this;
    var data = button.config("entity");
    bm.remove("variationType", "VariationType", $.i18n.prop("confirm.remove.variation.type",[data.name]), app.baseUrl + "variationAdmin/deleteType", data.id, {
        is_final: true,
        success: function () {
            _self.reload();
            app.global_event.trigger("variation-type-remove");
        }
    });
};

_v.afterTypeCellEdit = function(td, value, oldValue) {
    var tr = td.parent();
    var id = tr.attr("entity-id"), name = tr.find("td.editable .value").text(), standard = tr.find(".standard .text").text();
    if(!value || value.length > 100 ) {
        if(value.length > 100 ) {
            bm.notify($.i18n.prop("enter.no.more.characters", [100]), "error");
            return false;
        }
        app.variationUtils.errorHighlight(td.find("input"));
        return false;
    }
    this.updateType(id, name, standard, function() {
        tr.find("td.editable .value").text(oldValue)
    });
};

/****************************** Value Tab **********************************/
_v.attachOptionEvents = function(tab) { };

_v.attachOptionTableEvents = function(panel) {
    var _self = this;
    var view = this.c_tab;
    var lastRow = panel.find(".last-row");
    panel.find(".add-row").on("click", function() {
        var isValid = lastRow.valid({
            show_error: false,
            validate_on_call_only: true
        });
        if(!isValid) {
            return;
        }
        if(lastRow.find("input.image-chooser").length) {
            _self.updateImage(lastRow, panel);
        } else {
            var name =  lastRow.find("[name=type]").val();
            var valueField = lastRow.find("[name=value]");
            var value = valueField.val();
            var label = lastRow.find("[name=label]").val();
            var order = lastRow.find("[name=order]").val();
            if(!value) {
                app.variationUtils.errorHighlight(valueField);
                return;
            }
            _self.updateOption({type: name, value: value, label: label, order: order});
        }
    });

    panel.find(".remove").on("click", function() {
        _self.removeOption($(this));
    });
    lastRow.on("invalid", function(event, obj) {
        app.variationUtils.errorHighlight(obj.validator.elm, $.i18n.prop(obj.msg_template, obj.msg_params));
    });
    var select = lastRow.find("#variation-type-selector");
    var typeMap = view.typeMap = JSON.parse(select.attr("types"));
    select.on("change", function(evt, oldValue, newValue) {
        var valueTd = lastRow.find(".represent-value");
        if(newValue) {
            var variationType = typeMap[newValue];
            app.variationUtils.updateValueTd(valueTd, newValue, variationType)
        } else {
            valueTd.empty();
        }
    });

    lastRow.on("keypress.key_return", ":input", function() {
        panel.find(".add-row").trigger("click");
    });

    panel.find(".color-picker").spectrum({
        showInput: true,
        preferredFormat: "hex",
        change: function(color) {
            var tr = $(this).parents("tr");
            var type = tr.find(".type-name").attr("type");
            _self.updateOption({id: tr.attr("entity-id"), type: type, value: color.toHexString(), label: tr.find(".label .value").text(), order: tr.find(".label-order").attr("value-attr")});
        }
    });

    panel.find(".option-image").updateUi();
    panel.find('[name=representingImage]').on('change', function(evt) {
        _self.updateImage($(this).parents("tr"), panel);
    });
};

_v.updateImage = function(tr, panel) {
    var _self = this;
    app.variationUtils.updateImage(tr, panel, function() {
        _self.reload();
        app.global_event.trigger("variation-option-update");
    })
};

_v.updateOption = function(data, panel) {
    var _self = this;
    app.variationUtils.updateOption(data, panel, function() {
        _self.reload();
        app.global_event.trigger("variation-option-update");
    })
};

_v.removeOption = function(button) {
    var _self = this;
    var data = button.config("entity");
    bm.remove("variationOption", "VariationOption", $.i18n.prop("confirm.remove.variation.value", [data.name]), app.baseUrl + "variationAdmin/deleteValue", data.id, {
        is_final: true,
        success: function () {
            _self.reload();
            app.global_event.trigger("variation-option-remove");
        }
    });
};

_v.afterOptionCellEdit = function(td, value, oldValue) {
    var tr = td.parent();
    var id = tr.attr("entity-id");
    var filedName = td.attr("name");
    if(!value) {
        app.variationUtils.errorHighlight(td.find("input"));
        return false
    }
    if(td.is(".label") && value.length > 100 ) {
        app.variationUtils.errorHighlight(td.find("input"), $.i18n.prop("enter.no.more.characters", [100]));
        return false
    }
    if(td.is(".value") && value.length > 50 ) {
        app.variationUtils.errorHighlight(td.find("input"), $.i18n.prop("enter.no.more.characters", [50]));
        return false
    }
    var type = td.siblings(".type-name").attr("type");
    var data = {id: id, type: type, value: value, label: tr.find(".label .value").text(), order: tr.find(".label-order").attr("value-attr")};
    if(td.is(".label-order")){
        data.value = td.prev().attr("value-attr");
        data.order = value;
    }
    if(td.is(".label")) {
        data.label = value;
        data.value = td.next("td").attr("value-attr");
    }
    this.updateOption(data);
};

/****************************** All Tabs **********************************/
_v.reload = function() {
    this.c_tab.tabulator.reload()
};

_v.afterTableReload = function() {
    var _self = this;
    var view = _self.c_tab;
    if(view.tab_type == "type") {
        _self.attachTypeTableEvents(view.body)
    } else {
        _self.attachOptionTableEvents(view.body)
    }
};

_v.afterCellSelect = function(td, value, oldValue) {
    var tr = td.parent();
    var id = tr.attr("entity-id"), name = tr.find("td.editable .value").text(), standard = tr.find("td.selectable .value").text();
    this.updateType(id, name, standard, function() {
        td.find(".value").text(oldValue);
    });
};

_v.beforeCellSelect = function(td) {
    if(+td.attr("option-count") > 0) {
        bm.notify($.i18n.prop("remove.options.change.standard"), "alert");
        return false;
    }
};


/****************************** product tab contributions **********************************/

var _epv = app.editProduct.prototype;
_epv._init = _epv.init;
_epv.init = function() {
    var _self = this;
    _self._init();
    app.global_event.on("variation-type-update variation-type-remove variation-option-update variation-option-remove", function() {
        if(_self.tabs["variation"].length) {
            _self.tabs["variation"].reload();
        }
    });
};

/******Variation combination Chosen*******/
_epv.initSpecialVariationChosen = function(selects, update) {
    selects.each(function() {
        var select = $(this);
        if(update) {
            select.chosen("destroy");
        }
        select.on('chosen:ready', function(evt, chosen) {
            chosen = chosen.chosen;
            var data = chosen.results_data;
            if(select.is(".color")) {
                app.variationUtils.updateSpecialOptionChosen(data, chosen, "colorOption");
            } else if(select.is(".image")) {
                app.variationUtils.updateSpecialOptionChosen(data, chosen, "imageOption");
            }
            chosen.container.updateUi();
        }).chosen();
        select.on("chosen:showing_dropdown", function(evt, chosen) {
            chosen = chosen.chosen;
            if(chosen.dropdown) {
                chosen.dropdown.updateUi();
            }
        });
    })
};

app.editProduct.tabInitFunctions.variation = function(panel) {
    var _self = this;
    var form = panel.find("#bmui-tab-assign form");
    var pId = panel.find(".product-id").val();
    var model = form.find("input[name=model]:checked").val();
    var tabBody = this.body.find(".product-editor-body");
    var _config = {};
    var variation = {};
    _self.on_global("enterprise-product-update", function(ev, productId) {
        if(pId == productId) {
            reload()
        }
    });
    panel.on("change", function() {
        panel.clearDirty();
    });
    panel.save = function(handle) {
        if(handle) {
            handle();
        }
    };

    var select = panel.find("select.variation-option-select.color, select.variation-option-select.image");
    _self.initSpecialVariationChosen(select, true);

    var typeObj = panel.find("#variationType").change(function(ev, oldValue, newValue) {
        if(newValue) {
            getOptionForType(newValue);
            if(model) {
                var typeInp = form.find("input.add-to");
                var newInp = "<input type='hidden' name='addType' class='add-to' value='"+newValue+"'>";
                if(typeInp.length) {
                    typeInp.replaceWith(newInp);
                } else {
                    form.append(newInp);
                }
            }
        } else {
            panel.find("#options-for-type-" + oldValue).chosen("destroy").closest(".form-row").remove();
        }
    }).obj();

    typeObj.before_choice_close = function(li) {
        var type = panel.find("#variationType").chosen("choice_value", li);
        if(panel.find("#options-for-type-" + type).chosen("choices_count") > 0) {
            bm.notify($.i18n.prop("type.cant.removed.have.to.remove.all.options"), "alert");
            return false;
        } else {
            form.find("input.add-to").remove();
        }
    };

    typeObj.before_choice_chose = function(evt) {
        var index = this.result_highlight.attr("data-option-array-index");
        var value = this.results_data[index].value;
        var typeAddDom = panel.find(".variation-type-add-section");
        if(value == "add-variation-type"){
            this.results_hide();
            typeAddDom.removeClass("hidden");
            return false;
        } else {
            if(!typeAddDom.hasClass("hidden")){
                typeAddDom.addClass("hidden")
            }
        }
    };

    if(model) {
        typeObj.before_choice_chose = function(evt) {
            var index = this.result_highlight.attr("data-option-array-index");
            var value = this.results_data[index].value;
            var typeAddDom = panel.find(".variation-type-add-section");
            if(value == "add-variation-type"){
                this.results_hide();
                this.search_field.val("");
                typeAddDom.removeClass("hidden");
                return false;
            }  else {
                if(!typeAddDom.hasClass("hidden")){
                    typeAddDom.addClass("hidden")
                }
            }
            if(form.find("input.add-to").length) {
                bm.notify($.i18n.prop("only.allowed.single.variation.type"), "alert");
                return false;
            }
        };
        bindChosenChange(panel);
        attachOptionAddDom(panel);
    }

    form.form("prop", "preSubmit", function() {
        var vType = $(this).find('input[name=model]:checked').val();
        var imageAddMode = panel.find('.image-option-add-mode').val();
        if(imageAddMode == "true") return false;
        if(!vType) {
            bm.notify($.i18n.prop("please.select.a.variation.model"), "alert");
            return false;
        }
        var error = false;
        form.find("select.variation-option-select").each(function() {
            if(!$(this).val()) {
                error = true
            }
        });
        if(error) {
            bm.notify($.i18n.prop("please.select.variation.options"), "alert");
            return false;
        }
    });

    form.form("prop", "ajax.success", form.form("prop", "ajax.success").blend(function() {
        panel.reload();
    }));

    function bindChosenChange(chosenContent) {
        chosenContent.find("select.variation-option-select").each(function() {
            var chosenObj = $(this).change(function(ev, oldValue, newValue) {}).obj();
            addOption(chosenObj);
            removeOption(chosenObj);
        })
    }

    function addOption(chosenObj) {
        chosenObj.before_choice_chose = function(evt) {
            var index = this.result_highlight.attr("data-option-array-index");
            var value = this.results_data[index].value;
            if(value == "add-option"){
                this.search_field.val("");
                this.results_hide(); // Hide result poup
                var chosenWrapper = evt.currentTarget.closest('.chosen-wrapper');
                var optionAddSection = $(chosenWrapper).find('.variation-option-add-section');
                $(optionAddSection).removeClass('hidden'); // Show option entry section
                var valueTd = $(optionAddSection).find('.represent-value');
                var typeValue =  $(optionAddSection).find(".type-id").val();
                var variationType = $(optionAddSection).find(".type-id").attr('data-type-standard');
                app.variationUtils.updateValueTd(valueTd, typeValue, variationType);
                return false;
            } else {
                var vId = panel.find('.variation-id').val();
                if(vId){
                    app.variationUtils.addOptionForProduct(panel, value);
                    return false;
                }
            }
        }
    }

    function removeOption(chosenObj) {
        chosenObj.before_choice_close = function(li) {
            var vId = panel.find('.variation-id').val();
            if(vId){
                var isOption = this.form_field_jq.is(".variation-option-select");
                var id = this.form_field_jq.chosen("choice_value", li);
                var name = this.form_field_jq.find('[value='+id+']').html();
                bm.confirm($.i18n.prop("confirm.variation.option.remove", [name]), function () {
                    panel.loader();
                    bm.ajax({
                        url: app.baseUrl + 'variationAdmin/removeOption',
                        data: {product: pId, option: id},
                        response: function() {
                            panel.loader(false);
                        },
                        success: function(resp) {
                            panel.reload();
                        }
                    })
                }, function () {
                });
                return false;
            }
        }
    }

    if(panel.find(".variation-select").length) {
        selectVariationView();
        attachCombination();
        var func = model.capitalize() + "Variation";
        variation = window[func].call(this, panel);
    }

    function activateVariation(vId, deactivate) {
        bm.ajax({
            controller: "variationAdmin",
            action: "activateVariation",
            data: {id: vId, deactivate: deactivate},
            success: function(resp) {
                reload(_config);
                app.global_event.trigger("variation-status-update");
            }
        })
    }

    function attachCombination() {
        var data = {};
        var chosen = panel.find(".product-variation-select");
        chosen.chosen();
        var select = panel.find("select[name^=combobox]");
        select.on("change", function(evt, oldValue, newValue) {
            data = _config;
            data[$(this).attr("name")] = newValue;
            reload(data);
        });
        panel.find(".tool-icon.variation-active").on("click", function() {
            var vId = $(this).parents(".matrix-cell").attr("v-id");
            activateVariation(vId);
        });
        renderConfig();
    }

    function swapTypes(popup) {
        var radio = popup.find("input[radio^=radio]");
        radio.on("change", function() {
            var $this = $(this);
            var tempRadios = popup.find("[radio="+$this.attr('radio')+"]").not($this);
            tempRadios.each(function() {
                var radio = $(this);
                radio.prop("checked", false);
                radio.radio("state", 2);
            })
        })
    }

    function selectVariationView() {
        panel.find(".variation-select").on("click", function() {
            var data = _config;
            data.id = pId;
            bm.floatingPanel($(this), app.baseUrl + "variationAdmin/loadConfigView", data, {
                clazz: "variation-config-panel",
                events: {
                    content_loaded: function(popup) {
                        var element = popup.el;
                        swapTypes(element);
                        var configForm = element.find(".create-edit-form");
                        configForm.find(".select-variation").on("click", function() {
                            var cache = configForm.serializeObject();
                            var selectGroup = configForm.find(".variation-config");
                            if(!(Object.keys(cache).length > selectGroup.length)) {
                                bm.notify($.i18n.prop("please.select.correct.combination"), "alert");
                                return;
                            }
                            popup.close();
                            reload(cache, function() {
                                _config = cache;
                            });
                        });
                        configForm.find(".cancel-button").on("click", function() {
                            popup.close();
                        });
                        element.updateUi();
                    }
                },
                position_collison: "none"
            })
        });
    }

    function renderConfig() {
        var menuConfig = [
            {
                text: $.i18n.prop("edit"),
                ui_class: "edit",
                action: "edit"
            },
            {
                text: $.i18n.prop("set.as.default.variation"),
                ui_class: "base-combination",
                action: "set-default"
            },
            {
                text: $.i18n.prop("deactivate"),
                ui_class: "deactivate",
                action: "deactivate"
            }
        ];
        app.global_event.trigger("variation-menu-config", [menuConfig]);
        var menu = bm.menu(menuConfig, panel.find(".variation-config"), undefined, {
            click: function(action, entity) {
                var data = entity.config("entity");
                app.global_event.trigger("variation-menu-click", [action, data]);
                switch (action) {
                    case "set-default":
                        setDefault(data.id);
                        break;
                    case "edit":
                        var combination = [];
                        panel.find(".combination-dropdown select option:selected").each(function() {
                            combination.push($(this).text());
                        });
                        combination.push(data.combination);
                        variation.edit(data.id, data.name + " [" + combination.join(", ") + "]", function() {
                                panel.find(".matrix-table .matrix-cell").removeClass("active");
                                entity.parents(".matrix-cell").addClass("active");
                                tabBody.scrollTop(tabBody[0].scrollHeight);
                            },
                            function() {
                                reload(_config);
                            });
                        break;
                    case "deactivate":
                        activateVariation(data.id, true);
                        break;
                }
            },
            open: function(entity) {
                var data = entity.config("entity");
                if(data.base) {
                    menu.disable("set-default");
                    menu.disable("deactivate");
                } else {
                    menu.enable("set-default");
                    menu.enable("deactivate");
                }
            }
        }, "click", ["center bottom", "right+22 top+7"]);
    }

    function setDefault(id) {
        bm.ajax({
            controller: "variationAdmin",
            action: "setDefault",
            data: {id: id, pId: pId},
            success: function(resp) {
                reload(_config);
            }
        })
    }

    function reload(data, callback) {
        var postData = {};
        var dropdown = panel.find(".combination-dropdown").serializeObject();
        if(dropdown && Object.keys(dropdown).length) {
            postData = dropdown
        }
        postData.pId = pId;
        if(data) {
            $.extend(postData, data);
        }
        panel.loader();
        bm.ajax({
            controller: "variationAdmin",
            action: "loadCombination",
            data: postData,
            dataType: "html",
            response: function() {
                panel.loader(false);
            },
            success: function(resp) {
                var tab = panel.find("#bmui-tab-combination");
                var combination = tab.find(".variation-combination");
                tab.find(".standard-variation-panel").remove();
                if(combination.length) {
                    combination.replaceWith(resp);
                } else {
                    tab.append(resp);
                }
                tab.updateUi();
                attachCombination();
                if(callback) {
                    callback($(resp));
                }
                panel.clearDirty();
            }
        })
    }

    attachTypeAddDom(panel);

    function attachTypeAddDom(panel) {
        panel.find(".validation-type-add").on("click", function() {
            var panelSelect = panel.find("#variationType");
            var name =  panel.find("[name=name]").val();
            if(!name) {
                bm.notify($.i18n.prop("please.enter.variation.type.name"), "alert");
                return false;
            }
            var standard = panel.find("[name=standard]").val();
            var data = {id: null, name: name, standard: standard};
            bm.ajax({
                controller: "variationAdmin",
                action: "saveType",
                data: data,
                show_success_status: false
            }).done(function(response) {
                bm.notify($.i18n.prop("variation.type.added.successfully"), "success");
                app.variationUtils.clearVariationAddDom(panel, "vType");
                app.variationUtils.updateChosenDrop(panelSelect, response.typeId, name);
                panelSelect.trigger("validate");
                if(response.typeId) getOptionForType(response.typeId);
            }).error(function() {
                console.log('Error');
            })
        });
        panel.find(".remove-variation-type-row").on("click", function () {
            var typeAddSection = panel.find(".variation-type-add-section");
            typeAddSection.addClass("hidden");
        });
    }

    function attachOptionAddDom(content) {
        content.find(".variation-option-add").on("click", function() {
            var optionAddSection = $(this).closest('.form-row').find('.variation-option-add-section');
            var typeValue =  optionAddSection.find(".type-id").val();
            var optionSelectId = panel.find('#options-for-type-'+typeValue);
            var lastRow = $(this).closest('.form-row').find('.last-row');
            var optionValue = optionAddSection.find("[name=value]").val();
            var optionLabel = optionAddSection.find("[name=label]").val();
            var optionOrder = optionAddSection.find("[name=order]").val();
            var item = '';
            if(!optionValue && !lastRow.find("input.image-chooser")) {
                item = optionAddSection.find("[name=value]");
                app.variationUtils.errorHighlight(item);
                return
            }
            if(!optionLabel) {
                item = optionAddSection.find("[name=label]");
                app.variationUtils.errorHighlight(item);
                return
            }
            if(!optionOrder || !VALIDATION_RULES.digits.check(optionOrder)) {
                var msg = $.i18n.prop("order.must.be.number");
                item = optionAddSection.find("[name=order]");
                app.variationUtils.errorHighlight(item, msg);
                return
            }
            if(lastRow.find("input.image-chooser").length) {
                panel.find(".image-option-add-mode").val("true");
                app.variationUtils.updateImage(lastRow, panel, function(resp) {
                    var optionId = resp.option.id;
                    var optionVal = resp.option.value;
                    app.variationUtils.afterOptionSave(panel, resp);
                });
            } else {
                var data = {type: typeValue, value: optionValue, label: optionLabel, order: optionOrder, optionSelectId: optionSelectId};
                app.variationUtils.updateOption(data, panel, function(resp){
                    app.variationUtils.afterOptionSave(panel, resp);
                });
            }
        });
        content.find(".remove-variation-option-row").on("click", function () {
             var optionAddSection = $(this).closest('.form-row').find('.variation-option-add-section');
             optionAddSection.addClass("hidden");
        });
    }

    function getOptionForType(newValue){
        bm.ajax({
            controller: "variationAdmin",
            action: "optionsForType",
            dataType: "html",
            data: {id: newValue},
            success: function(resp) {
                var row = $(resp);
                panel.find(".option-chooser-wrapper").append(row);
                var select = row.find("select.variation-option-select");
                _self.initSpecialVariationChosen(select);
                row.updateUi();
                attachOptionAddDom(row);
                bindChosenChange(row);
            }
        })
    }
};

var _u = app.variationUtils = {};

_u.afterOptionSave = function(panel, resp){
    var variationId = panel.find(".variation-id").val();
    var optionId = resp.option.id;
    var optionVal = resp.option.value;
    var typeId = resp.option.type.id;
    var optionSelectId = panel.find('#options-for-type-'+typeId);
    this.updateChosenDrop(optionSelectId, optionId, optionVal);
    this.refreshSpecialVariationChosen(optionSelectId);
    this.clearVariationAddDom(panel, "vOption");
    if(optionId && variationId){
        this.addOptionForProduct(panel, optionId);
    }
};

_u.updateChosenDrop = function(selectDomId, optionId, optionValue){
    selectDomId.append("<option value='"+optionId+"'>"+optionValue+"</option>");
    var selectedValues = selectDomId.val();
    selectedValues ? selectedValues.push(optionId) : selectedValues = optionId;
    selectDomId.val(selectedValues);
    selectDomId.trigger("chosen:updated");
};

_u.refreshSpecialVariationChosen = function (selects) {
    var chosenObj = $(selects).obj();
    var data = chosenObj.results_data;
    if(selects.is(".color")) {
        this.updateSpecialOptionChosen(data, chosenObj, "colorOption");
    } else if(selects.is(".image")) {
        this.updateSpecialOptionChosen(data, chosenObj, "imageOption");
    }
    chosenObj.container.updateUi();
    $(selects).on("chosen:showing_dropdown", function(evt, chosen) {
        if(chosenObj.dropdown) chosenObj.dropdown.updateUi();
    });
};

_u.updateSpecialOptionChosen = function (data, chosenObj, optionType){
    $.each(data, function(i, item) {
        if(item.value == 'add-option'){
            item.html = "<span class='value'>+ Add Option</span>";
        } else {
            if(optionType == "colorOption"){
                item.html = "<span title='"+item.text+"' class='chosen-color' style='background: "+item.text+"'></span><span class='value'></span>";
            } else if(optionType == "imageOption"){
                item.html = "<img class='chosen-image' title='"+item.text+"' src='"+app.baseUrl + "resources/"+app.tenant+"/variation/option/option-" + item.value+ "/16-" + item.text+"'><span class='value'></span>";
            }
        }
        if(chosenObj.container.is(".chosen-container-multi")) {
            var selected = chosenObj.container.find("[data-option-array-index="+item.array_index+"]").prev();
            selected.html(item.html);
        }
    });
    if(chosenObj.container.is(".chosen-container-single")) {
        var html = data.filter("this.value=='"+selects.val()+"'")[0].html;
        chosenObj.container.find(".chosen-single span").addClass("special-chosen-select").html(html);
    }
};

_u.clearVariationAddDom = function (panel, clearType){
    if(clearType == "vOption"){
        panel.find(".option-label-input").val('');
        panel.find(".option-order-input").val('');
        panel.find(".image-chooser").val('');
        panel.find(".image-option-add-mode").val("false");
        panel.find(".variation-option-add-section").addClass("hidden");
    } else if (clearType == "vType") {
        panel.find("[name=name]").val('');
        panel.find(".variation-type-add-section").addClass("hidden");
    }
};

_u.addOptionForProduct = function (panel, value, typeValue){
    var form = panel.find("#bmui-tab-assign form");
    var pId = panel.find("input[name=pId]").val();
    var model = panel.find("#bmui-tab-assign form").find("input[name=model]:checked").val();
    var type = form.find("input.add-to").val();
    var typeVal = form.find("select[name='" + type + ".variationOption']");
    if(typeVal[0] != this.form_field) type = undefined;
    if(typeValue) type = typeValue;
    panel.loader();
    var postData = {product: pId, option: value, type: type, model: model};
    bm.ajax({
        url: app.baseUrl + 'variationAdmin/addOption',
        data: postData,
        response: function() {
            panel.loader(false);
        },
        success: function(resp) {
            panel.reload();
        }
    })
};

_u.reload = function(){

};

_u.updateValueTd = function (valueTd, newValue, variationType){
    if(newValue) {
        if(variationType === "text") {
            valueTd.html('<input type="text" name="value" class="td-full-width small" validation="required maxlength[50]" maxlength="50" placeholder="'+ $.i18n.prop("enter.value")+'">');
        } else if(variationType === "color") {
            var colorInput = $("<input type='text' name='value' class='color-picker' value='#ECC'/>");
            valueTd.html(colorInput);
            colorInput.spectrum({
                showInput: true,
                preferredFormat: "hex",
                change: function(color) {
                    $(this).val(color.toHexString());
                }
            })
        } else {
            var previewId = bm.getUUID();
            var tr = valueTd.parents("tr");
            var imageUploaderWrap = $('<span class="file-wrapper">' +
                '<input type="file" name="representingImage" class="image-chooser" file-type="image" size-limit="' +
                (5*1024) + '" clazz="small" previewer="preview-' + previewId + '">' +
                '<span class="image-preview"><img id="preview-' + previewId + '"></span></span>');
            valueTd.html(imageUploaderWrap);
            var imageUploader = imageUploaderWrap.find('input.image-chooser');
            imageUploader.on("file-reset", function() {
                imageUploaderWrap.find(".image-preview img").hide();
            });
            imageUploader.on("file-add", function(ev, file) {
                imageUploader.data("cached-value", file)
            })
        }
        valueTd.updateUi();
    } else {
        valueTd.empty();
    }
};

_u.updateImage = function(tr, panel, success) {
    var fileWrapper = tr.find(".file-wrapper");
    var imageChooser = fileWrapper.find("input.image-chooser");
    if(imageChooser.val() || imageChooser.data("cached-value")) {
        var type = tr.find("#variation-type-selector").val() || tr.find(".type-name").attr("type");
        var id = tr.attr("entity-id");
        if(id) {
            fileWrapper.append('<input type="hidden" name="id" value="' + id + '">')
        }
        fileWrapper.append('<input type="hidden" name="type" value="' + type + '">');
        fileWrapper.wrap('<form method="post" action="'+app.baseUrl+'variationAdmin/saveOption"></form>');
        var form = fileWrapper.parent();
        panel.loader();
        form.submit(function(e) {
            var formObj = $(this);
            var formURL = formObj.attr("action");
            var formData = new FormData(this);
            if(!imageChooser.val()) {
                formData.set("representingImage", imageChooser.data("cached-value"))
            }
            var label = tr.find(".represent-label input[name=label]").val() || tr.find(".label .value").text();
            var order = tr.find(".represent-order input[name=order]").val() || tr.find(".label-order").attr("value-attr");
            if(label) {
                formData.append("label", label);
            }
            if(order){
                formData.append("order",order);
            }
            bm.ajax({
                url: formURL,
                type: 'POST',
                data:  formData,
                mimeType:"multipart/form-data",
                contentType: false,
                cache: false,
                processData: false,
                response: function() {
                    panel.loader(false);
                },
                success: function(resp) {
                    success(resp);
                },
                error: function() {
                    console.log("error");
                }
            });
            e.preventDefault();
            form.unbind("submit");
        });
        form.submit();
    } else if(tr.find(".actions-column .add-row").length) {
        bm.notify($.i18n.prop("image.is.required"), "alert");
    }
};

_u.errorHighlight = function (item, msg) {
    var type = "value";
    var td = item.parent("td");
    if(td.is(".represent-type")) {
        type = "type"
    } else if(td.is(".represent-label")) {
        type = "label"
    } else if(td.is(".represent-order")){
        type = "order"
    }
    bm.notify(msg ? msg : $.i18n.prop( type + ".is.required"), "alert");
    item.addClass("error-highlight");
    setTimeout(function() {
        item.removeClass("error-highlight")
    }, 3000);
};

_u.updateOption = function(data, panel, success) {
    var _self = this;
    bm.ajax({
        controller: "variationAdmin",
        action: "saveOption",
        data: data,
        show_success_status: false
    }).done(function(resp) {
        success(resp);
    })
};

$(function() {
    app.global_event.on("on-backend-order-create", function(evt, config, popupElement, callBack) {
        if(config.tr.attr("has-variation") != "true") {
            return;
        }
        config.return = true;
        var cached = false;
        bm.editPopup(app.baseUrl + "variation/loadOrderVariationPopup", $.i18n.prop("select.combination.for"), config.name, {id: config.id}, {
            events: {
                content_loaded: function() {
                    var _self = this;
                    initVariationSelection(config.id, _self, function(resp) {
                        config.sku = resp.sku;
                        config.name = resp.name +"("+ resp.vString +")";
                        config.price = resp.basePrice;
                        config.stock = resp.stock;
                        config.spinConfig = resp.spin || {};
                        _self.find(".price-amount").text(resp.basePrice);
                        _self.find(".total-amount").text(resp.basePrice);
                        cached = true;
                    });
                    app.global_event.trigger("order-create-variation-selection-init", [_self, config]);
                }
            },
            beforeSubmit: function(form, settings, popup) {
                var vInfo = JSON.parse(form.find(".variation-short-config").val() || "{}");
                if(!cached) {
                    var cart = this.find(".add-to-cart-button");
                    config.sku = cart.attr("sku");
                    config.name = vInfo.name +"("+ cart.attr("v-string") + ")";
                    config.price = form.find(".total-amount").text();
                    config.stock = cart.attr("stock");
                    config.spinConfig = cart.config("spin") || {};
                }
                config.pos = -1;
                popupElement.find(".selected-products .product-name").each(function() {
                    if($(this).text() == config.name) {
                        config.pos = 1;
                    }
                });
                var options = form.serializeObject()['config.options'];
                config.options = typeof options == "string" ? [options] : options;
                config.beforeSubmit && config.beforeSubmit();
                delete config.beforeSubmit
                callBack(config);
                cached = true;
                popup.close();
                return false;
            }
        })
    });
});