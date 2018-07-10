(function() {
    app.tabs.setting = function (configs) {
        this.text = $.i18n.prop("settings");
        this.tip = $.i18n.prop("manage.settings");
        this.ui_class = "settings";
        this.ajax_url = app.baseUrl + "setting/loadAppView";
        app.tabs.setting._super.constructor.apply(this, arguments);
    };

    app.ribbons.administration.push({
        text: $.i18n.prop("settings"),
        processor: app.tabs.setting,
        ui_class: "settings"
    });

    app.tabs.setting.inherit(app.MultiTab);

    var _s = app.tabs.setting.prototype;

    _s.init = function() {
        var _self = this;
        app.tabs.setting._super.init.apply(this, arguments);
        var searchForm = this.body.find('.bmui-tab.left-side-header .search-form'), searchInput = searchForm.find('input');
        function searchHeader() {
            _self.body.find(".bmui-tab.left-side-header .bmui-tab-header").each(function() {
                var $this = $(this), name = $this.find('.title').text().trim();
                if(name.match(new RegExp(searchInput.val(), "i"))) {
                    $this.show()
                } else {
                    $this.hide()
                }
            })
        }
        searchInput.ichange(700, function() {
            searchHeader()
        }).focus();
        searchForm.form({
            preSubmit: function() {
                searchHeader();
                return false
            }
        });
    };
    var expandedPanel;

    _s.initProductImageSettings = function(data) {
        var _form = data.panel.find("form#frmProductImageSetting");
        _form.find(".use-original").on("change.disableInputs", function(){
            if($(this).prop("checked")){
                $(this).closest(".double-input-row").find("input:text").attr("disabled","disabled").addClass("disabled")
            } else {
                $(this).closest(".double-input-row").find("input:text").removeAttr("disabled").removeClass("disabled");
            }
        });
        _form.find(".use-original").trigger("change.disableInputs");
        data.success = function () {
            app.global_event.trigger("product-image-change");
        }
    };

    _s.initGeneralSettings = function(data) {
        var _form = data.panel.find("form#generalSettingsForm");
        this.countryChange = bm.countryChange(_form, {inputClass: "medium"});
        _form.find(".display-captcha-setting").on("change", function() {
            var captchaRadio = _form.find(".display-captcha-type");
            if($(this).is( ":checked" )){
                captchaRadio.show()
            } else {
                captchaRadio.hide()
            }
        });
        this.on_global(["page-create", "page-update", "send-trash", "page-restore"], function() {
            bm.ajax({
                url: app.baseUrl + "setting/loadUpdatedPageSelects",
                dataType: "json",
                show_response_status: false,
                success: function (resp) {
                    function tryPersistenceUpdate(oldSelect, newVal){
                        var newSelect = $(resp.select);
                        oldSelect.html(newSelect.html());
                        oldSelect.chosen();
                        oldSelect.chosen('val', newVal);
                    }
                    tryPersistenceUpdate(_form.find("select[name='general.page404']"), resp.page404);
                    tryPersistenceUpdate(_form.find("select[name='general.page403']"), resp.page403);
                }
            })
        });
        data.success = function() {
            data.panel.reload({cachesToClear: ['favicon']});
        }
    };

    _s.initDefaultImagesSettings = function(data) {
        data.panel.find('.reset-to-default .reset-icon').click(function() {
            var $this = $(this);
            var container = $this.parents('.form-section-container');
            container.find('.image-preview').attr('src', app.baseUrl + 'setting/loadDefaultImage?type=' + $this.attr('reset-type') + "&_=" + (new Date()).getTime());
            container.find('.reset-image').val('1');
            data.panel.setDirty();
        });

        data.panel.find("input[name^=defaultImages]").on("change", function() {
            var resetInput = $(this).siblings(".reset-to-default").find(".reset-image");
            resetInput.val("0");
        });

        data.success = function(resp) {
            resp.cachesToClear.iterate(function(iterator, idx) {
                var image = this;
                $('<img src="' + image + '">').clearCache(function() {
                    if(idx == resp.cachesToClear.length - 1) {
                        data.panel.reload();
                        app.global_event.trigger('default-image-update');
                    }
                    iterator.next();
                })
            });
        };
    };

    _s.initTaxAndCurrencySettings = function(data) {
        var form = data.panel.find("form"), configTypeSelector= form.find("[name='tax.configuration_type']"), configType = configTypeSelector.val();
        /*configTypeSelector.on("change", function (ev, oldVal, newVal) {
           if(configType == "manual" && newVal == "default") {
               bm.confirm($.i18n.prop('confirm.tax.config.restore'), function () {})
           }
        });*/
        form.find(".customize-tax.active").on("click", function () {
            ComponentManager.openTab(ComponentManager.getRibbonData(app.ribbons.web_commerce, "tax"));
        });
        this.on_global(["tax-profile-create", "tax-profile-update", "tax-profile-delete"], function(){
            form.find(".tax-profile-selector").each(function(){
                bm.updateDomainSelector($(this), "webcommerce.TaxProfile");
            })
        });
        data.success = function() {
            app.taxConfigType = configTypeSelector.val()
            data.panel.reload({cachesToClear: ['favicon']});
        }
        this.initCurrencyExtension.apply(this, arguments);

    };

    _s.initShippingSettings = function(data) {
        var _self = this
        var form = data.panel.find("form");
        _self.bindClassMenuEntries(form)
        this.on_global(["shipping-profile-create", "shipping-profile-update", "shipping-profile-delete"], function(){
            form.find(".shipping-profile-selector").each(function(){
                bm.updateDomainSelector($(this), "webcommerce.ShippingProfile");
            })
        });
        form.find(".add-new-button").on("click", function () {
            $(this).hide()
            form.find(".form-row .submit-button").hide()
            _self.addEditShippingClass(form)
        })

    };

    _s.bindClassMenuEntries = function (form) {
        var _self = this;
        var menu_entries = [
            {
                text: $.i18n.prop("edit"),
                ui_class: "edit"
            },
            {
                text: $.i18n.prop("remove"),
                ui_class: "remove"
            },
            {
                text: $.i18n.prop("make.default"),
                ui_class: "make_default"
            }
        ];
        bm.menu(menu_entries, form, ".shipping-class-actions", {
            click: function(action, navigator) {
                var item = navigator.closest(".shipping-class-item")
                switch(action) {
                    case "edit":
                        _self.addEditShippingClass(form, item)
                        break;
                    case "remove":
                        _self.removeClass(form, item)
                        break;
                    case "make_default":
                        _self.makeDefaultClass(form, item)
                        break;
                }
            }
        }, "click", ["center bottom", "right+22 top+7"]);
    }

    _s.removeClass = function (form, item) {
        var _self = this
        if(form.find(".shipping-class-row").length == 1 || item.is(".default")) {
            bm.notify($.i18n.prop("default.class.can.not.be.deleted"), "alert");
            return
        }
        var name = item.find(".name").text();
        var id = item.attr("class-id");
        bm.remove("shippingClass", "Shipping Class", $.i18n.prop("confirm.remove.shipping.class", [name]),
            app.baseUrl + "shippingAdmin/deleteShippingClass", id, {
                success: function () {
                    item.remove()
                },
                is_final: true
            });
    }

    _s.makeDefaultClass = function (form, item) {
        var _self = this
        bm.ajax({
            url: app.baseUrl + "setting/saveConfigurations",
            data: {type: "shipping", "shipping.default_shipping_class": item.attr("class-id")},
            success: function () {
                form.find(".shipping-class-item.default").removeClass("default")
                item.addClass("default")
            }
        });
    }

    _s.addEditShippingClass = function (form, item) {
        var _self = this
        var addNewClassDiv = form.find(".shipping-class-form-template").clone().addClass("add-new-class-row create-edit-form").removeClass("shipping-class-form-template").show()
        form.find(".add-new-class-row").remove()
        form.find(".shipping-data").show()
        if(item) {
            item.find(".shipping-data").hide()
            addNewClassDiv.find("input.id").val(item.attr("class-id"))
            item.append(addNewClassDiv)
            var $addNewClass = item.find(".add-new-class-row")
            $addNewClass.find(".class-name").val(item.find(".name").text());
            $addNewClass.find(".class-description").val(item.find(".description").text());
            $addNewClass.find(".submit-button").text($.i18n.prop("update"));
            $addNewClass.attr("class-id", item.attr("class-id"));
        } else {
            form.find(".shipping-classes").append(addNewClassDiv)
            item = form.find(".add-new-class-row")
        }
        form.find(".add-new-class-row").form()
        item.find(".submit-button").on("click", function (e) {
            _self.submitShippingClassForm(form, item)
            e.preventDefault()
        })

        item.find(".cancel-button").on("click", function () {
            this.closest(".add-new-class-row").remove()
            form.find(".shipping-data").show()
            form.find(".add-new-button").show()
            form.find(".form-row .submit-button").show()
        })
    }

    _s.submitShippingClassForm = function (form, item) {
        var _self = this
        var name = item.find(".class-name").val();
        var desc = item.find(".class-description").val();
        var id = item.attr("class-id");
        if(item.valid()) {
            var formData = {
                id: id,
                name: name,
                description: desc
            }
            bm.ajax({
                url: app.baseUrl + "shippingAdmin/saveShippingClass",
                data: formData,
                success: function (resp) {
                    form.find(".shipping-data").show();
                    if(id) {
                        item.find(".shipping-data").find(".name").text(name);
                        item.find(".shipping-data").find(".description").text(desc)
                    } else {
                        var newItem = '<div class="shipping-class-item" class-id="'+ resp.id +'"><div class="shipping-data"><span class="shipping-class-actions"></span><div class="name">' + name + '</div><div class="description">' + desc + '</div></div></div>'
                        form.find(".shipping-classes").append(newItem);
                        _self.bindClassMenuEntries(form)
                    }
                    form.find(".add-new-class-row").remove();
                    form.find(".add-new-button").show()
                    form.find(".form-row .submit-button").show()
                }
            });
        }
    };

    _s.initShippingApiSettings = function(data) {
        var form = data.panel.find("form");
        form.find('.packing-algorithm').change(function() {
            resetNote($(this));
        });
        resetNote(form.find('.packing-algorithm'));
        function resetNote($select) {
            $select.closest('.form-row').find('.algorithm-note').remove();
            $select.closest('.form-row').append('<span class="algorithm-note">' + $.i18n.prop($select.val().toLowerCase().replaceAll('_', '.') + '.algorithm.note') + '</span>')
        }
    };

    _s.initCustomerRegistrationSettings = function(data) {
        var _self = this;
        var form = data.panel.find('form.fields-setting');
        _self.formFieldsSetting(form);
        _self.restore(data.panel, "restoreCustomerRegistrationSettings");
    };

    _s.initBillingAddressSettings = function(data) {
        var _self = this;
        var form = data.panel.find('form.fields-setting');
        _self.formFieldsSetting(form);
        _self.restore(data.panel, "restoreBillingAddressSettings");
    };

    _s.initShippingAddressSettings = function(data) {
        var _self = this;
        var form = data.panel.find('form.fields-setting');
        _self.formFieldsSetting(form);
        _self.restore(data.panel, "restoreShippingAddressSettings");
    };

    _s.initCustomerProfilePageSettings = function(data) {
        var _self = this;
        _self.customerProfileFieldsInlineChangeOnHover()
        data.panel.find(".label-bar").unbind();
        data.panel.find(".accordion-panel").accordion({all_close: true});
        data.panel.find(".child.label-bar").unbind();
        data.panel.find(".accordion-panel.child").childAccordion({all_close: true});
        data.panel.find(".abandoned_cart_settings").on("click", function() {
            ComponentManager.openTab(ComponentManager.getRibbonData(app.ribbons.administration, "settings"), {active: "abandoned_cart"})
        })
        data.panel.find(".gift_card_settings").on("click", function() {
            ComponentManager.openTab(ComponentManager.getRibbonData(app.ribbons.administration, "settings"), {active: "giftCard"})
        })
        data.panel.find(".loyalty_point_settings").on("click", function() {
            ComponentManager.openTab(ComponentManager.getRibbonData(app.ribbons.administration, "settings"), {active: "loyaltyPoint"})
        })
        data.panel.find(".wallet-item").jqObject.off("click");

        data.panel.find(".label-bar").click(function (ui) {
            data.panel.find(".child.label-bar").unbind();
            var itemPanel = ui.target.nextElementSibling.jqObject;
            if(!this.jqObject.is('.collapsed')){
                if(this.jqObject.next(".accordion-item").find(".no-item").length == 0){
                    data.panel.find(".accordion-panel.child").childAccordion({all_close: true});
                }
                _self.customerSettngsTabSortableOn(itemPanel);
                data.panel.find(".child.label-bar").click(function (ui) {
                    data.panel.find(".child-child.configurable-row").unbind();
                    var itemPanel = ui.target.nextElementSibling.jqObject;
                    if(!this.jqObject.is('.collapsed')){
                        _self.customerSettngsTabSortableOn(itemPanel);
                    }else {
                        data.panel.find(".child-child.configurable-row").unbind();
                    }
                });
            }else {
                data.panel.find(".child.label-bar").unbind();
            }
        });
        data.panel.find(".wcui-checkbox ,.inline-editable").click(function (event) {
            event.stopPropagation();
        });

        var form = data.panel.find('form.customer-profile-settings');
        _self.customerSettngsTabSortableOn(form);
        _self.customerProfileFieldsInlineChangeSetting(form);
        _self.restore(data.panel, "restoreCustomerProfileSetting");
    };

    _s.customerProfileFieldsInlineChangeSetting = function(form) {
        form.find('.inline-editable').editableOnHover().on("inlinechange", function () {
            var span = this.jqObject;
            var label =  span.text();
            span.parent().find('.label-customer-profile-settings').val(label)
        });
    };

    _s.customerProfileFieldsInlineChangeOnHover = function() {
        $.extend($.prototype, {
            editableOnHover: function () {

                var editBtn = "<span class='tool-icon edit inline-edit' title='Edit'></span> ".jqObject
                var revertBtn = '<span class="tool-icon reset" title="Revert"></span>'.jqObject

                this.each(function () {
                    var span = this.jqObject
                    span.hover(function () {

                        if (this.edit_off) {
                            return
                        }
                        span.append(editBtn)
                        span.append(revertBtn)

                        span.find(".reset").on("click",function () {
                            var span = this.jqObject.closest(".inline-editable")
                            var defaultLevel = span.parent().find(".default_label").text()
                            span.text(defaultLevel)
                            span.parent().find('.label-customer-profile-settings').val(defaultLevel)
                        });

                        span.find(".edit").on("click", function () {
                            var span = this.jqObject.closest(".inline-editable")
                            var type = span.attr("data-editable-type") || "text"

                            editBtn.detach()
                            span.nat.edit_off = true

                            span.addClass("editting")
                            bm["editInline" + type.capitalize()](span).always(function () {
                                span.nat.edit_off = false
                                span.removeClass("editting")
                            })

                            span.find(".reset").remove()

                        });

                    }, function () {
                        if (this.edit_off) {
                            return
                        }
                        editBtn.detach()
                        revertBtn.detach()
                    })
                });

                return this;
            }
        });
    };


    _s.customerSettngsTabSortableOn = function(form) {

        var content_item
        var dragBox

        form.find('.scroll-item-wrapper').sortable({
            containment: 'parent',
            placeholder: true,
            placeholder_size: true,
            axis: "y",

            beforeStart: function (ui) {
                var isCollapsed =  ui.elm.is('.collapsed')
                var isEditableModeOff = ui.elm.find(".inline-edit-input").length > 0 ? false :true
                var isSortable =  (isCollapsed && isEditableModeOff) ? true : false
                if(!isSortable){
                    return false;
                }
            },
            start: function (ui) {
                dragBox = ui.elm
                content_item = ui.elm.next('div')
            },
            stop: function() {
                var otherItem = dragBox.next('.accordion-item')

                if(!this.jqObject[0].candidate.is('.no-item')){
                    if(otherItem.length > 0){
                        otherItem.insertBefore(dragBox)
                    }
                    content_item.insertAfter(dragBox)
                }

                if(this.jqObject[0].candidate.is('.child-child')){
                    form.find('input:hidden.order-child-child').each(function(idx, elm) {
                        $(elm).val(idx + 1)
                    });
                }
                if(this.jqObject[0].candidate.is('.child')){
                    form.find('input:hidden.order-child').each(function(idx, elm) {
                        $(elm).val(idx + 1)
                    });
                }
                else {
                    form.find('input:hidden.order').each(function(idx, elm) {
                        $(elm).val(idx + 1)
                    });
                }

            }
        });
        form.find('.sortable-container').sortable({
            containment: 'parent',
            placeholder: "address-form-sort-placeholder",
            axis: "y",
            stop: function() {
                if(this.jqObject[0].candidate.is('.child-child')){
                    form.find('input:hidden.order-child-child').each(function(idx, elm) {
                        $(elm).val(idx + 1)
                    });
                }if(this.jqObject[0].candidate.is('.child')){
                    form.find('input:hidden.order-child').each(function(idx, elm) {
                        $(elm).val(idx + 1)
                    });
                }else {
                    form.find('input:hidden.order').each(function(idx, elm) {
                        $(elm).val(idx + 1)
                    });
                }

            }
        });
    };


    _s.formFieldsSetting = function(form) {
        form.find('.sortable-container').sortable({
            containment: 'parent',
            placeholder: "address-form-sort-placeholder",
            stop: function() {
                form.find('input:hidden.order').each(function(idx, elm) {
                    $(elm).val(idx + 1)
                });
            }
        });
        form.find(".tool-icon").click(function () {
            var icon = $(this);
            var formRow = icon.closest(".configurable-row");
            if (icon.is(".add")) {
                icon.removeClass("add").addClass("remove").closest(".inactive").removeClass("inactive").addClass("active");
                formRow.find("input.active").val(1);
                formRow.find(".required[name]").checkbox("enable", true);
            } else if (icon.is(".remove")) {
                icon.removeClass("remove").addClass("add").closest(".active").removeClass("active").addClass("inactive");
                formRow.find("input.active").val(0);
                formRow.find(".required[name]").checkbox("state", "unchecked").checkbox("enable", false);
            }
        });
        form.find(".configurable-row input:checkbox").on("change", function () {
            var checkbox = $(this);
            var formRow = checkbox.closest(".configurable-row");
            if(checkbox.prop("checked")){
                checkbox.closest(".configurable-row").addClass("required");
                formRow.find(".required").val(1)
            } else {
                checkbox.closest(".configurable-row").removeClass("required");
                formRow.find(".required").val(0)
            }
        });
    };

    _s.restore = function(panel, action) {
        var _self = this;
        panel.tool.find(".reset-default").on("click", function() {
            bm.confirm($.i18n.prop("confirm.restore.setting"), function () {
                bm.ajax( {
                    url: app.baseUrl + "setting/" + action,
                    success: function() {
                        _self.body.find(".reset-default").closest(".remove-after-reload").remove();
                        panel.reload()
                    }
                })
            }, function () {
            })
        })
    };

    _s.initStoreSettings = function(data) {
        var form = data.panel.find("form");
        this.countryChange = bm.countryChange(form, {inputClass: "medium"});
        data.success = function(resp) {
            if(resp.imageName) {
                var image = $('<img src="' + app.baseUrl + 'resources/store/' + resp.imageName + '"/>');
                image.clearCache(function() {
                    data.panel.reload();
                });
            } else {
                data.panel.reload();
            }
        }
    };

    _s.initCategoryImageSettings = function(data) {
        var _form = data.panel.find("form#frmCategoryImageSetting");
        _form.find(".use-original").on("change.disableInputs", function(){
            if($(this).prop("checked")){
                $(this).closest(".double-input-row").find("input:text").attr("disabled","disabled").addClass("disabled")
            }else{
                $(this).closest(".double-input-row").find("input:text").removeAttr("disabled").removeClass("disabled");
            }
        });
        _form.find(".use-original").trigger("change.disableInputs");
        data.success = function () {
            app.global_event.trigger("category-image-change");
        }
    };

    _s.initEmailSettings = function(data) {
        var _self = this;
        var container = data.panel.find(".accordion-panel");
        var menu_entries = [
            {
                text: $.i18n.prop("view"),
                ui_class: "view",
                action: "view"
            },
            {
                text: $.i18n.prop("edit"),
                ui_class: "edit",
                action: "edit"
            }
        ];
        $.each(container.find(".email-table"), function(ind, elm){
            this.tabulator = bm.table($(elm), $.extend({
                menu_entries: menu_entries
            }));
            this.tabulator.onActionClick = function (action, data) {
                switch (action) {
                    case "view":
                        _self.viewTemplate(data.id, data.name);
                        break;
                    case "edit":
                        _self.editTemplate(data.id, data.name);
                        break;
                }
            }
        });
        this.viewTemplate = function (id, name) {
            var title = $.i18n.prop("edit.template");
            var events = {
                content_loaded: function (popup) {
                    this.updateUi();
                    var _inView = this;
                    this.find("> .content").scrollbar({
                        reduce_margin_from_offset: false,
                        vertical: {
                            offset: 20
                        }
                    });
                    _inView.find(".edit-mail").click(function() {
                        popup.close();
                        _self.editTemplate(id, name)
                    });
                    _inView.find(".send-mail").click(function() {
                        bm.editPopup(app.baseUrl + "setting/testMailView", $.i18n.prop("send.test.mail"), $.i18n.prop(name), {id:id})
                    })
                }
            };
            new POPUP($.extend({
                title: title + (" - <span class='emphasized'>" + $.i18n.prop(name) + "</span>"),
                ajax_url: app.baseUrl + "setting/viewTemplate",
                ajax_settings: {
                    data: {id: id}
                },
                draggable: true
            }, {width: 765}, {clazz: "edit-popup" , events: events}))
        };
        _self.editTemplate = function (id, name) {
           var htmlTextArea;
           var htmlEditor;
           bm.editPopup(app.baseUrl + "setting/editTemplate", $.i18n.prop("edit.template"), $.i18n.prop(name), {id: id}, {
               width: 765,
               draggable: false,
               success: function(){
                   expandedPanel = $(container.find(".expanded")).find("[name='labelBarIdentifier']").val();
                   data.panel.reload()
               },
               events: {
                   content_loaded: function() {
                       var _self = this;
                       _self.find(".reset").click(function() {
                           bm.ajax({
                               url: app.baseUrl + "templateAdmin/setDefaultContent",
                               data: {id: id},
                               success: function(resp) {
                                   _self.find(".data-txt").val(resp.contentTxt);
                                   _self.find(".data-html").html(resp.contentHtml);
                                   if(htmlEditor) {
                                       htmlEditor.doc.setValue(resp.contentHtml);
                                   }
                               }
                           })
                       });
                       var tab = _self.on("tabs");
                       htmlTextArea = _self.find(".code-mirror-editor");
                       tab.on("tab:activate", function(evt, obj) {
                          if(obj.newIndex == "html") {
                              if(!htmlEditor) {
                                  htmlEditor = htmlTextArea.htmlCodeEditor()
                              }
                          }
                       });

                   }
               },
               beforeSubmit: function(form, data) {
                   if(htmlEditor) {
                       htmlTextArea.val(htmlEditor.getValue())
                   }
               }
           })
        }
    };

    _s.initWebtoolSettings = function(data) {
        var _self = this, panel = data.panel;
        this.one("redirect-301-import-success", function() {
            panel.reload();
        });
        var _self = this;
        var fileList = panel.find(".seo-file-list");
        var itemTemplate = '<div class="item">' +
            '<div class="name">#FILENAME#</div><div class="action"><span class="tool-icon remove" file-name="#FILENAME#"></span></div>' +
        '</div>';
        fileList.scrollbar({
            vertical: {
                height: 150,
                offset: -2
            }
        });
        panel.find(".seo-upload-container").on("file-submit", function(ev, resp) {
            if(resp.fileName) {
                fileList.append(itemTemplate.replaceAll("#FILENAME#", resp.fileName))
            }
        });
        fileList.delegate(".tool-icon.remove", "click", function() {
            var $this = $(this);
            bm.ajax({
                url: app.baseUrl + "setting/removeSeoFile",
                data: {fileName: $this.attr("file-name")},
                success: function() {
                    $this.parents(".item").remove();
                }
            })
        });
        var menu = {};
        menu.action_menu_entries = [
            {
                text: $.i18n.prop("import.301.redirects"),
                ui_class: "import-urls import",
                action: "import"
            },
            {
                text: $.i18n.prop("export.301.redirects"),
                ui_class: "export",
                action: "export"
            }
        ];

        menu.onActionMenuClick = function(action) {
            switch (action) {
                case "import":
                    _self.initRedirectImport();
                    break;
                case "export":
                    window.open(app.baseUrl + "redirect/export");
                    break;
            }
        };

        bm.menu(menu.action_menu_entries, _self.toolbar.find(".action-tool"), null, {
            click: $.proxy(menu, "onActionMenuClick")
        }, "click", ["right bottom+7", "right top"]);

        panel.tool.find(".toolbar-item.import-urls").on("click", function(){
            _self.initRedirectImport();
        });
        panel.find("[name=seoUpload], .redirects-301-container").on("change", function() {
            return false;
        });
        this.init301Redirect(panel.find(".redirects-301-container"));
        this.on_global("301-redirect-import", function() {
            var container = panel.find(".redirects-301-container").loader();
            bm.ajax({
                url: app.baseUrl + "setting/load301Redirect",
                dataType: "html",
                success: function(resp) {
                    container.replaceWith(resp);
                    _self.init301Redirect(panel.find(".redirects-301-container"));
                }
            })
        })
    };

    _s.init301Redirect = function(panel) {
        var rowHtml = '<tr mapping-id="#ID#"><td class="old-url editable">#OLD#</td><td class="new-url editable">#NEW#</td><td><span class="tool-icon remove"></span></td></tr>';
        var lastRow = panel.find("tr.last-row");
        var mappingTable = panel.find("table.url-mappings");
        function addOrUpdateMapping(id, oldUrl, newUrl, modifiedTd, oldVal) {
            if(oldUrl == newUrl) {
                bm.notify($.i18n.prop("old.new.url.must.not.be.same"), "error");
                return;
            }
            bm.ajax({
                url: app.baseUrl + "redirect/mapping301Redirect",
                type: "post",
                data: {id: id, oldUrl: oldUrl, newUrl: newUrl},
                success: function(resp) {
                    afterMapping(resp.id, resp.oldUrl, resp.newUrl)
                },
                error: function() {
                    if(modifiedTd) {
                        modifiedTd.find(".value").text(oldVal);
                    }
                }
            })
        }

        function attachEvent(content) {
            bm.makeTableCellEditable(content.find("td.editable"), function(td, newVal, oldVal) {
                var parent = td.parent("tr");
                var flag = false;
                if(!newVal) {
                    errorHighlight(td.find("input"));
                    bm.notify($.i18n.prop("value.must.not.be.empty"), "error");
                    return false
                }
                var oldUrl = parent.find("td.old-url span.value").text(),
                    newUrl = parent.find("td.new-url span.value").text();
                if(oldUrl == newUrl) {
                    errorHighlight(td.find("input"));
                    bm.notify($.i18n.prop("old.new.url.must.not.be.same"), "error");
                    return false
                }
                afterModification(parent, td, oldVal);
            });
            content.find("td span.remove").on("click", function() {
                removeRow($(this).parents("tr"));
            })
        }

        function addRow(id, oldUrl, newUrl) {
            var row = rowHtml.replace("#ID#", id).replace("#OLD#", oldUrl).replace("#NEW#", newUrl);
            row = $(row);
            lastRow.before(row);
            attachEvent(row);
            lastRow.find(".old-url").val("");
            lastRow.find(".new-url").val("")
        }

        function removeRow(row) {
            var mappingId = row.attr("mapping-id");
            bm.ajax({
                url: app.baseUrl + "redirect/remove301Redirect",
                data: {mappingId: mappingId},
                success: function(resp) {
                    row.remove();
                }
            })
        }

        function updateRow(row, oldUrl, newUrl) {
            row.find("td.old-url span.value").text(oldUrl);
            row.find("td.new-url span.value").text(newUrl)
        }

        function afterMapping(id, oldUrl, newUrl) {
            var row = mappingTable.find("tr[mapping-id=" + id + "]");
            if(row.length) {
                updateRow(row, oldUrl, newUrl)
            } else {
                addRow(id, oldUrl, newUrl)
            }

        }

        function afterModification(row, modifiedTd, oldVal) {
            var id = row.attr("mapping-id"),
                oldUrl = row.find("td.old-url span.value").text(),
                newUrl = row.find("td.new-url span.value").text();
                addOrUpdateMapping(id, oldUrl, newUrl, modifiedTd, oldVal);
        }
        function errorHighlight(item) {
            item.addClass("error-highlight");
            setTimeout(function() {
                item.removeClass("error-highlight")
            }, 1000);
        }
        var multiCondition = panel.find(".last-row.multi-conditions");
        multiCondition.attachValidator();
        var addMapping = function() {
            if(!multiCondition.valid()) {
                return;
            }
            var newUrl =  lastRow.find(".new-url").val(), oldUrl = lastRow.find(".old-url").val();
            if(!oldUrl) {
                errorHighlight(lastRow.find(".old-url"));
                return;
            }
            if(!newUrl) {
                errorHighlight(lastRow.find(".new-url"));
                return;
            }
            addOrUpdateMapping(null, oldUrl, newUrl);
        };
        lastRow.find("input").bind("keydown.key_return", addMapping);
        lastRow.find(".add-row").on("click", addMapping);
        attachEvent(panel);
    };

    _s.initResponsiveSettings = function(data) {
        var panel = data.panel;
        panel.find(".resolutions-container").on("change", function() {
            return false
        });
        this.initResponsiveResolutionPanel(panel.find(".resolutions-container"))
    };

    _s.initResponsiveResolutionPanel = function(panel) {
        var rowHtml = '<tr resolution-id="#ID#"><td class="min-width editable" restrict="numeric">#MIN#</td><td class="max-width editable" restrict="numeric">#MAX#</td><td><span class="tool-icon remove"></span></td></tr>';
        var lastRow = panel.find("tr.last-row");
        var resolutionTable = panel.find("table.resolutions");
        function addOrUpdateResolution(id, minWidth, maxWidth, modifiedTd, oldVal) {
            if(minWidth && maxWidth && +minWidth > +maxWidth) {
                bm.notify($.i18n.prop("min.width.must.less.than.max.width"), "error");
                return false
            }
            bm.ajax({
                url: app.baseUrl + "setting/saveResolution",
                type: "post",
                data: {id: id, maxWidth: maxWidth, minWidth: minWidth},
                success: function(resp) {
                    afterSave(resp.id, minWidth, maxWidth)
                },
                error: function() {
                    if(modifiedTd) {
                        modifiedTd.find(".value").text(oldVal);
                    }
                }
            });
        }

        function attachEvent(content) {
            bm.makeTableCellEditable(content.find("td.editable"), function(td, newVal, oldVal) {
                var parent = td.parent("tr");
                return afterModification(parent, td, oldVal);
            });
            content.find("td span.remove").on("click", function() {
                removeRow($(this).parents("tr"));
            })
        }

        function addRow(id, minWidth, maxWidth) {
            var row = rowHtml.replace("#ID#", id).replace("#MIN#", minWidth).replace("#MAX#", maxWidth);
            row = $(row);
            lastRow.before(row);
            attachEvent(row);
            lastRow.find(".min-width").val("");
            lastRow.find(".max-width").val("")
        }

        function removeRow(row) {
            var id = row.attr("resolution-id");
            bm.ajax({
                url: app.baseUrl + "setting/removeResolution",
                data: {id: id},
                success: function(resp) {
                    row.remove();
                }
            })
        }

        function updateRow(row, oldUrl, newUrl) {
            row.find("td.old-url span.value").text(oldUrl);
            row.find("td.new-url span.value").text(newUrl)
        }

        function afterSave(id, oldUrl, newUrl) {
            var row = resolutionTable.find("tr[resolution-id=" + id + "]");
            if(row.length) {
                updateRow(row, oldUrl, newUrl)
            } else {
                addRow(id, oldUrl, newUrl)
            }

        }

        function afterModification(row, modifiedTd, oldVal) {
            var id = row.attr("resolution-id"),
                minWidth = row.find("td.min-width span.value").text(),
                maxWidth = row.find("td.max-width span.value").text();
            if(!minWidth && !maxWidth) {
                errorHighlight(modifiedTd.find("input"));
                bm.notify($.i18n.prop("value.must.not.be.empty"), "error");
                return false
            }
            return addOrUpdateResolution(id, minWidth, maxWidth, modifiedTd, oldVal);
        }
        function errorHighlight(item) {
            item.addClass("error-highlight");
            setTimeout(function() {
                item.removeClass("error-highlight")
            }, 1000);
        }
        var addResulation = function() {
            var minWidth =  lastRow.find(".min-width").val(), maxWidth = lastRow.find(".max-width").val();
            if(!minWidth && !maxWidth) {
                errorHighlight(lastRow.find(".max-width"));
                return;
            }
            addOrUpdateResolution(null, minWidth, maxWidth);
        };
        lastRow.find("input").bind("keydown.key_return", addResulation);
        lastRow.find(".add-row").on("click", addResulation);
        attachEvent(panel);
        lastRow.find("input").numeric();
    };

    _s.onContentLoad = function (data) {
        var index = data.index.capitalize();
        data.panel.find("form").form({
            ajax: {
                success: function() {
                    data.panel.clearDirty();
                    if(data.success) {
                        data.success.apply(this, arguments); //args: resp, status, xhr, form
                    }
                    app.global_event.trigger("after-" + data.index + "-settings-update");
                }
            }
        });
        if (this["init" + index + "Settings"]) {
            this["init" + index + "Settings"](data);
        }
    };

    _s.initCheckoutPageSettings = function(data){
        var panel = data.panel;
        var currentType = panel.find("[name='checkout_page.terms_and_condition_type']");
        currentType.on("change", function(){
            var type = $(this).val();
            bm.ajax({
                url: app.baseUrl + "setting/loadReferenceSelectorBasedOnType",
                data: {type: type},
                dataType: "html",
                success: function(resp) {
                    var selectorRow = panel.find(".ref-selector-row");
                    resp = $(resp);
                    selectorRow.html(resp.html());
                    selectorRow.updateUi();
                    var body = panel.find("form").obj(ValidationPanel);
                    body.attach(selectorRow.find("[validation]"), body)
                }
            })
        });


        data.success = function () {
            app.global_event.trigger("checkout-settings-change");
        }
    };

    _s.initBackupRestoreSettings = function(data) {
        var _self = this;
        var panel = data.panel;
        var backupLoader = "<div class='div-mask'><span class='vertical-aligner'></span><span class='loader'></span>" + $.i18n.prop("backup.contents.and.configuration") + "<div>";
        var restoreLoader = "<div class='div-mask'><span class='vertical-aligner'></span><span class='loader'></span>" + $.i18n.prop("restore.contents.and.configuration") + "<div>";
        var selectDom = panel.find("select[name=restorePoint]");
        var restoreButton = panel.find(".submit-button.restore");
        if(!selectDom.val()) {
            restoreButton.prop("disabled", true);
        }
        selectDom.change(function() {
            if(!$(this).val()) {
                restoreButton.prop("disabled", true);
            } else {
                restoreButton.removeAttr("disabled");
            }
        });
        panel.find(".submit-button.backup").on("click", function() {li;
            backupNow();
        });

        restoreButton.on("click", function() {
            restoreNow(selectDom.val());
        });

        function backupNow() {
            var body = $("body");
            bm.mask(body, backupLoader);
            bm.ajax({
                type: "post",
                url: app.baseUrl + "setting/backupRestore",
                data: {backup: true},
                dataType: 'json',
                success: function(resp) {
                    var backup = resp.backup;
                    selectDom.chosen("add", {text: backup.text, value: backup.value});
                    bm.unmask(body);
                    bm.notify($.i18n.prop("backup.success"), "success");
                },
                error: function() {
                    bm.unmask(body);
                    bm.notify($.i18n.prop("backup.fail"), "error");
                }
            });
        }

        function restoreNow(restorePoint) {
            bm.ajax({
                type: "post",
                url: app.baseUrl + "setting/backupRestore",
                data: {restorePoint: restorePoint},
                dataType: 'json',
                success: function(resp) {
                    var time = new Date().toGMTString();
                    $("body").loader();
                    function isRestarted(callback) {
                        bm.ajax({
                            url: app.baseUrl + "app/isRestarted",
                            data: {time: time},
                            success: function(resp) {
                                if(resp.isRestarted) {
                                    callback(true);
                                } else {
                                    isRestarted(callback, 10000)
                                }
                            },
                            error: function() {
                                setTimeout(function() {
                                    isRestarted(callback)
                                }, 10000);
                            }
                        })
                    }
                    isRestarted(function(success) {
                        $("body").loader(false);
                        if(success) {
                            bm.notify($.i18n.prop("restore.success"), "success");
                            bm.alert($.i18n.prop("you.have.to.login.again"), "alert", function() {
                                location.href = app.baseUrl + "admin";
                            });
                        } else {
                            bm.notify($.i18n.prop("restore.fail"), "error");
                        }
                    });

                },
                error: function() {
                    bm.unmask(panel);
                    bm.notify($.i18n.prop("restore.fail"), "error");
                }
            });
        }
    };

    _s.afterEmailReload = function(data) {
        if(expandedPanel) {
            var container = data.panel.find(".accordion-panel");
            container.find("." + expandedPanel).trigger("click");
            expandedPanel = null
        }
    };

    _s.afterTableReload = function(data) {
        var index = data.index.capitalize();
        if (this["after" + index + "Reload"]) {
            this["after" + index + "Reload"](data);
        }
    };

    _s.initTask = function(token, name, successEventName) {
        var data = {
            token: token,
            name: name,
            detail_url: app.baseUrl + "taskCommon/progressView",
            detail_status_url: app.baseUrl + "taskCommon/progressStatus",
            detail_viewer: app.tabs.setting.import_status_viewer
        };
        TaskManager.createTask(data);
        app.tabs.setting.success_event_name = successEventName
        bm.taskPopup(app.baseUrl + "taskCommon/progressView", data, {width: 800})
    };

    _s.initRedirectImport = function() {
        var _self = this;
        bm.editPopup(app.baseUrl + "redirect/uploadImportFile", $.i18n.prop("import.301.redirect"), '', {}, {
            success: function(resp) {
                var successEventName = "redirect-301-import-success"
                _self.initTask(resp.token, resp.name, successEventName);
                app.global_event.fire("301-redirect-import")
            }
        })
    };

    _s.initClearDisposable = function() {
        var _self = this;
        bm.confirm($.i18n.prop('confirm.clear.disposable'), function() {
            bm.ajax({
                url: app.baseUrl + "setting/initClearDisposable",
                success: function(resp) {
                    _self.initTask(resp.token, resp.name);
                }
            })
        }, function(){})
    };

    _s.initAdministrationSettings = function(data) {
        var _self = this, form = data.panel.find("form");
        function reloadIfConfirm() {
            bm.confirm($.i18n.prop("have.to.reload.changes.effective"), function() {
                window.location.href = app.baseUrl + "admin"
            })
        }
        form.find(".ecommerce-switch").on("change", function(ev) {
            var _this = $(this);
            if (!_this.prop("checked")){
                bm.editPopup(app.baseUrl + "ecommerce/getUsedWidgetAndPlugins", $.i18n.prop("ecommerce.switching"), null, null, {
                    clazz: "ecommerce-popup",
                    events: {
                        content_loaded: function(popupObj) {
                            var $this = $(this);
                            $this.find(".ecommerce-popup-confirm").on("click", function() {
                                popupObj.close(0)
                            })
                            popupObj.on("close", function (ev, data) {
                                if(data) {
                                    var panel = $(".administration-edit-form")
                                    panel.find(".ecommerce-switch, .wcui-checkbox").click()
                                }
                            })
                        }
                    }
                })
            }
        });

        form.find(".clear-disposable-items").on("click", function() {
            _self.initClearDisposable()
        });
        form.find(".refresh-license-button").click(function() {
            bm.ajax({
                controller: "license",
                action: "refresh",
                success: function(resp) {
                    if(resp.notifications) {
                        var wrapper = $("#administrative-notification-wrapper");
                        if(!wrapper.length) {
                            wrapper = $("<div id='administrative-notification-wrapper'></div>").appendTo(document.body)
                        }
                        resp.notifications.every(function() {
                            var notification = $("<div class='administrative-notification'></div>").appendTo(wrapper);
                            notification.addClass("notification-" + this.type);
                            var title = $("<div class='title'></div>").appendTo(notification);
                            title.text($.i18n.prop(this.type));
                            var messageBox = $("<div class='message'></div>").appendTo(notification);
                            messageBox.text(this.message)
                        });
                        DashboardManager.initializeAdministrativeNotification();
                        reloadIfConfirm();
                    }
                }
            })
        });

        var success = form.form("prop", "ajax.success");
        form.form("prop", "ajax.success", success.blend(function() {
            reloadIfConfirm()
        }))
    };

    app.tabs.setting.import_status_viewer = {
        init: function (_popup) {
            var popup = _popup.getDom();
            this.totalProgressBar = new ProgressBar(popup.find(".progress"));
            this.totalProgressBar.render();
        },
        update: function(_popup, resp) {
            var popup = _popup.getDom();
            this.totalProgressBar.setPosition(resp.totalProgress);
            popup.find(".progress-count").text(resp.totalProgress + "%");
            popup.find(".record-complete").text(resp.complete);
            popup.find(".record-total").text(resp.totalRecord);
            popup.find(".success-count").text(resp.successCount);
            popup.find(".warning-count").text(resp.warningCount);
            popup.find(".error-count").text(resp.errorCount);
            if(resp.status == "aborted") {
                popup.find(".import-aborted").show();
                popup.find(".progress .progress-bar .completed").addClass(resp.status);
            }
            if (resp.status  == "complete") {
                this.activeLogSummaryLink(popup, resp.token, resp.cacheFile);
                popup.find('.content').append('<div class="button-line">' +
                    '<button type="button" class="button close-button">' + $.i18n.prop('close') + '</button>' + ' &nbsp; ' +
                    '<a type="button" target="_blank" class="button download-button" href="' + app.baseUrl + 'taskCommon/download?token=' + resp.token + '&cacheFile='+ resp.cacheFile +'">' + $.i18n.prop('download') + '</a>' +
                    '</div>');
                popup.find(".close-button").click(function() {
                    _popup.close();
                });
                app.global_event.trigger(app.tabs.setting.success_event_name)
                app.tabs.setting.success_event_name = ""
            }
        },
        activeLogSummaryLink: function(_popup, token, cacheFile) {
            var _self = this;
            _popup.find(".success-count").addClass('link').click(function () {
                _self.fetchLogSummary(_popup, token, cacheFile, "success")
            });
            _popup.find(".warning-count").addClass('link').click(function () {
                _self.fetchLogSummary(_popup, token, cacheFile, "warning")
            });
            _popup.find(".error-count").addClass('link').click(function () {
                _self.fetchLogSummary(_popup, token, cacheFile, "error")
            })
        },
        fetchLogSummary: function (_popup, token, cacheFile, type) {
            bm.ajax({
                url: app.baseUrl + "taskCommon/" + type + "LogSummary",
                dataType: "html",
                data: {token: token, cacheFile: cacheFile},
                success: function (resp) {
                    _popup.find(".log-summary").html(resp).updateUi();
                }
            });
        }
    }
})();