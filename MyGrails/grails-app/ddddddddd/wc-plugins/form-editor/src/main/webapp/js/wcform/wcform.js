$(function() {
    var activeToolConfig = null;
    var activeToolbar = null;
    var selectedFormElementObject = null;
    var formEditor;
    var newElementDropTemplate, newFieldTemplate;
    var configDropDown;
    var fieldConfigHtmlCache = {};

    bm.onReady($.i18n, "prop", function() {
        newFieldTemplate = '<div class="form-field">\
                <span class="editable-label"></span>\
                <span class="editable-field"></span>\
                <span class="value-cache" style="display: none">\
                    <input type="hidden" name="fieldid">\
                </span>\
            </div>';
        newElementDropTemplate = '<div class="row block-container-1">' +
            '<div class="block block-1">' + newFieldTemplate +
             '<span class="tool-icon remove remove-field"></span>\
            </div>' +
             '<span class="close-option-header">\
                <span class="settings"></span>\
             </span>' +
        '</div>';
        configDropDown = bm.createMenu([
            {
                text: $.i18n.prop("move.up"),
                ui_class: "move-up"
            },
            {
                text: $.i18n.prop("move.down"),
                ui_class: "move-down"
            },
            {
                text: $.i18n.prop("copy"),
                ui_class: "copy"
            },
            {
                text: $.i18n.prop("remove"),
                ui_class: "remove"
            },
            {
                text: $.i18n.prop("add.block"),
                ui_class: "add-block"
            },
            {
                text: $.i18n.prop("add.condition"),
                ui_class: "add-condition"
            }
        ])
    });

    bm.WcFormBuilder = function(mainForm, popup, title) {
        var _self = this, sortables = mainForm.find(".field-drop-zone");
        this.formEditor = formEditor = mainForm;
        this.event = $({});
        _self.title = title
       _self.initDragDrop();
        mainForm.find(".save-web-form").click(function() {
            var $this = $(this);
            if($this.is(".disabled")) {
                return;
            }
            var eventData = {veto: false};
            _self.event.trigger("before-save", eventData);
            if(eventData.veto == true) { return }
            $this.addClass("disabled");
            var data = _self.getSaveData();
            bm.ajax({
                url: app.baseUrl + "formAdmin/save",
                type: "post",
                data: data,
                success: function() {
                    popup.trigger("success")
                },
                complete: function() {
                    $this.removeClass("disabled")
                }
            })
        });
        mainForm.find(".toolbar-btn.add, .add-elm").on("click", function() {
            _self.renderAddElmPop()
        });
        this.leftBar = mainForm.find(".left-bar");
        this.initLeftBar();
        this.populateFormFields();
        this.initializeConfigMenu();
        mainForm.find("[uncheck-value]").pairuncheck();
        _self.formEditor.find(".row").each(function() {
            _self.attachRowEvents($(this))
        });
        this.event.one("field-add", function() {
            formEditor.find(".blank-form-msg").hide()
        })
    };

    var _fb  = bm.WcFormBuilder.prototype;

    _fb.dropZoneTemplate = '<div class="field-drop-zone"></div>';

    _fb.setTitle = function() {
        var _self = this
        var title = '<span class="title">' + _self.title + '</span>';
        if(selectedFormElementObject) {
            var type = _self.getSelectedFieldType(), emphasized = formEditor.find(".field-thumb." + type + " .label").text() || type;
            title += ' - <span class="emphasized"> ' + $.i18n.prop("settings.for", [emphasized])  + '</span>';
        }
        formEditor.find(".header-title").html(title)
    };

    _fb.getSelectedFieldId = function(field) {
        return (field || selectedFormElementObject).find("[name='fieldid']").val();
    };

    _fb.getSelectedFieldType = function(field) {
        return this.getSelectedFieldProp(field, "type")
    };

    _fb.getSelectedFieldProp = function(field, name) {
        return (field || selectedFormElementObject).find("[name='" + this.getSelectedFieldId() + "." + name + "']").val();
    };

    _fb.initLeftBar = function() {
        var _self = this, leftBar = this.leftBar, setupConfig = leftBar.find(".setup-config");
        leftBar.scrollbar();
        bm.autoToggle(setupConfig).attachValidator().find("input:checkbox[name][uncheck-value]").pairuncheck();
        var validator = setupConfig.data("validatorInst");
        setupConfig.accordion();
        setupConfig.on("beforeexpand", function() {
            return validator.isValid()
        });
        this.event.on("before-save", function(ev, data) {
            if(validator.isValid() == false) {
                _self.deactivateFormField();
                data.veto = true
            }
        });
        this.event.on("condition-update", function() {
            var conditions = {}, template = setupConfig.find(".template"), conditionWrapper = leftBar.find(".conditions");
            _self.formEditor.find(".value-cache input.condition").each(function() {
                var $this = $(this), name = $this.attr("name").split(".");
                if(!conditions[name[2]]) {
                    conditions[name[2]] = {}
                }
                conditions[name[2]][name[3]] = $this.val();
            });
            conditionWrapper.empty();
            $.each(conditions, function(key, value) {
                var condition = template.clone().removeAttr("style").removeClass("template");
                condition.find(".targetOption").text(value.targetOption);
                condition.find(".action").text(value.action);
                condition.find(".dependentFieldName").text(_self.formEditor.find(".form-field .value-cache [name='" + value.dependentFieldUUID + ".name']").val());
                conditionWrapper.append(condition)
            });
            if($.isEmptyObject(conditions)) {
                conditionWrapper.append('<div class="no-entry-row">' + $.i18n.prop('no.condition.available') + '</div>')
            }
        });
        var senderEmailField = setupConfig.find("[name=senderEmailFieldUUID]"), senderEmailFieldUUID = senderEmailField.attr("value");
        senderEmailField.on("change", function() {
            senderEmailFieldUUID = senderEmailField.val()
        });
         function populateSenderEmail() {
            var emailSelector = setupConfig.find("[name=senderEmailFieldUUID]");
            emailSelector.chosen("removeAll");
            emailSelector.chosen("add", {text: $.i18n.prop("default.email"), value: ""});
            _self.getFieldList("textBox", function(fieldId, field) {
                var validation = field.find("[name='" + _self.getSelectedFieldId(field) + ".validation']").val() || "";
                if(validation.contains("email")) return true;
                return false
            }).every(function(index, value) {
                emailSelector.chosen("add", {text: value.label, value: value.uuid})
            });
            emailSelector.chosen("val", senderEmailFieldUUID);
        }
        this.event.on("field-add", function(ev, type) {
            populateSenderEmail();
        });
        populateSenderEmail();
        this.event.trigger("condition-update");
        leftBar.find(".field-config > .back").on("click", function() {
            _self.deactivateFormField()
        })
    };

    _fb.renderAddElmPop = function() {
        var _self = this;
        bm.editPopup(null, $.i18n.prop("add.form.elm"), null, null, {
            content: _self.formEditor.find(".edit-popup-form").clone().removeClass("display-none"),
            events: {
                content_loaded: function() {
                    var $this = $(this);
                    $this.find(".field-thumb").on("click", function() {
                        $this.find(".field-thumb.selected").removeClass("selected");
                        $(this).addClass("selected")
                    })
                }
            },
            beforeSubmit: function(form, data, popup) {
                _self.addElement(form.find(".field-thumb.selected").attr("field-type"));
                popup.close();
                return false
            }
        })
    };

    _fb.renderFieldConfig = function() {
        var _self = this, field = this.selectedFormElementObject, leftBar = _self.leftBar,
            fieldConfig = leftBar.find(".field-config"), type = _self.getSelectedFieldType();
        fieldConfig.loader();
        var configDom = fieldConfigHtmlCache[type];
        function load(resp) {
            fieldConfig.find(".body").replaceWith(resp);
            leftBar.find(".setup-config").removeClass("active");
            fieldConfig.addClass("active");
            _self.initFieldConfig();
            fieldConfig.loader(false)
        }
        if(configDom) {
            load(configDom);
            return
        }
        bm.ajax({
            url: app.baseUrl + "formAdmin/fieldConfig",
            data: {type: type},
            dataType: "html",
            success: function(resp) {
                fieldConfigHtmlCache[type] = resp;
                load(resp)
            }
        })
    };

    _fb.initFieldConfig = function() {
        var _self = this, fieldConfig = _self.leftBar.find(".field-config"),
            field = _self.selectedFormElementObject, fieldId = field.find("[name='fieldid']").val();
        function updateCache(k, v) {
            var name = fieldId + k.substring(5);
            if($.isArray(v)) {
                selectedFormElementObject.find("[name='" + name + "']").remove();
                var cache = selectedFormElementObject.find(".value-cache");
                v.every(function() {
                    var input = $("<input type='hidden' name='" + name + "'>");
                    input.val(this);
                    cache.append(input)
                })
            } else {
                var field = selectedFormElementObject.find("[name='" + name + "']");
                if(!field.length) {
                    field = $("<input type='hidden' name='" + name + "'>");
                    selectedFormElementObject.find(".value-cache").append(field)
                }
                field.val(v)
            }
        }
        _self.event.off(".field-config");
        var accordion = fieldConfig.find(".leftbar-accordion");
        accordion.accordion();
        accordion.on("beforeexpand", function() {
            return validator.isValid()
        });
        fieldConfig.updateUi();
        fieldConfig.find(":text[restrict]").each(function() {
            var input = $(this);
            input[input.attr("restrict")]();
        });
        fieldConfig.find(".config-section").each(function() {
            _self.populateConfigSection($(this));
            _self.initConfigSection($(this))
        });
        bm.autoToggle(fieldConfig.find(".body")).attachValidator().find("input:checkbox[name][uncheck-value]").pairuncheck();
        fieldConfig.find('.dropdown-time-picker').each(function () {
            $(this).combodate({
                firstItem: 'name',
                minuteStep: 1
            });
        });
        fieldConfig.find('.dropdown-date-picker').each(function () {
            $(this).combodate({
                smartDays: true,
                maxYear: new Date().getFullYear(),
            });
        });
        var validator = fieldConfig.find(".body").data("validatorInst");
        fieldConfig.find("[name^='field.']").on("change", function() {
            var $this = $(this), name = $this.attr("name"), value;
            var fieldValidator = $this.data("validatorFiledInst");
            if(fieldValidator && fieldValidator.validate() == false) {return}
            if($this.is(":checkbox") && $this.prop("checked") == false) {
                value = ""
            } else {
                value = $this.val()
            }
            if(name.startsWith("field.extra.")) {
                var names = name.split("."), type = names[2];
                if(type == "config") {
                    _self.removeFieldConfig(names[3], field)
                }
                _self.addOrUpdateFieldExtra({ type: type, label: names[3], value: value})
            } else {
                updateCache(name, $this.val());
            }
            if($this.attr("field-update") == "true") {
                _self.populateFormField(field)
            }
        })
    };

    _fb.populateConfigSection = function(configSection) {
        var _self = this, fieldid = selectedFormElementObject.find("[name='fieldid']").val(), configs = _self.getFieldConfigs(selectedFormElementObject);
        configSection.find(":input[name^='field']").each(function() {
            var name = $(this).attr("name"), val;
            var field = $(this);
            if(name.startsWith("field.extra.config")) {
                var config = configs[name.substring(19)];
                val = config ? config.value : ""
            } else {
                name = fieldid + name.substring(5);
                val = selectedFormElementObject.find("[name='" + name + "']").val();
            }

            if(field.is("select")) {
                field.chosen("val", val)
            } else if(field.is(":checkbox")) {
                field.prop("checked", val)
            } else if(field.is(":radio")) {
                field.radio("val", field.attr("value") == val ? val : "")
            } else {
                field.val(val)
            }
        });
        var type = configSection.attr("type").camelCase();
        if(_self["_populate" + type + "ConfigSection"]) {
            _self["_populate" + type + "ConfigSection"](configSection, fieldid, configs)
        }
    };

    _fb._populateValidationConfigSection = function(configSection, fieldid) {
        configSection.find(".validation-required").prop("checked", false);
        configSection.find(".validation-maxlength").prop("checked", false).siblings("input").val("");
        configSection.find(".validation-custom").prop("checked", false).siblings("input").val("");
        configSection.find(":radio").eq(0).radio("val", "");
        var cacheField = selectedFormElementObject.find("[name='" + fieldid + "." + "validation']");
        if(cacheField.length) {
            var lengthValidation =  "", customValidation = "", numberValidation = " ";
            cacheField.val().split(" ").every(function(key, value) {
                value = value.trim();
                if(value == "") { return }
                if(value.startsWith("maxlength")) {
                    var length = value.substring(10, value.length - 1);
                    configSection.find(".maxlength-limit").val(length);
                    lengthValidation += value + " "
                } else if(value.startsWith("minlength")) {
                    var length = value.substring(10, value.length - 1);
                    configSection.find(".minlength-limit").val(length);
                    lengthValidation += value + " "
                } else if(value.startsWith("max")) {
                    var length = value.substring(4, value.length - 1);
                    configSection.find(".max-limit").val(length);
                    numberValidation += " "  + value
                } else if(value.startsWith("min")) {
                    var length = value.substring(4, value.length - 1);
                    configSection.find(".min-limit").val(length);
                    numberValidation += value + " "
                } else if(value.equals("required")) {
                    configSection.find(".validation-required").prop("checked", true)
                } else if(configSection.find(".validation-" + value).length) {
                    configSection.find(".validation-" + value).radio("val", "" + value)
                } else {
                    customValidation += value + " "
                }
            });
            if(customValidation) {
                configSection.find(".validation-custom").val(customValidation).prop("checked", true).siblings("input").val(customValidation)
            }
            configSection.find(".validation-number").val("number" + numberValidation);
            configSection.find(".validation-length").val(lengthValidation);
        }
    };

    _fb._populateFilePropConfigSection = function(configSection, fieldid, configs) {
        var formats = configs["upload_extensions"];
        formats = (formats ? formats.value : "").split(",");
        formats.forEach(function(format) {
            configSection.find(".format-" + format).prop("checked", true)
        })
    };

    _fb._populateFieldOptionsConfigSection = function(configSection, fieldid) {
        var _self = this, type = selectedFormElementObject.find("[name='" + fieldid + ".type']").val(),
            addOptionForm = configSection.find(".add-option-form");
        function populate(ev, data) {
            var option = _self.getFieldExtraProp(fieldid, selectedFormElementObject, "option", data.value);
            addOptionForm.find('.remove-btn').removeClass("hidden");
            addOptionForm.find(".submit-btn").text($.i18n.prop("update"));
            addOptionForm.find("[name=id]").val(option.id);
            addOptionForm.find("[name=label]").val(option.label);
            addOptionForm.find("[name=value]").val(option.value);
            addOptionForm.find("[name=extraValue]").prop("checked", option.extraValue ? true : false);
            addOptionForm.find()
        }
        this.event.on("option-edit.field-config", populate);
        var selectedExtraOpt = selectedFormElementObject.find(".selected-option").val();
        if(selectedExtraOpt) {
            populate(null, {value: selectedExtraOpt})
        }
        var selectionType, style;
        if(type == "dropDown") {
            selectionType = "single";
            style = "dropDown"
        } else if(type == "radioButton") {
            selectionType = "single";
            style = "radio"
        } else if( type == "checkBox") {
            selectionType = "multi";
            style = "radio"
        }
        configSection.find(".type-" + selectionType).radio("val", selectionType);
        configSection.find(".style-" + style).radio("val", style)
    };

    _fb._initValidationConfigSection = function(item) {
        var _self = this;
        function handleToggle(toggler, target) {
            if(toggler[0].checked) {
                target.show("slide")
            } else {
                target.hide("slide")
            }
        }
        item.find("[toggle]").each(function() {
            var $this = $(this), target = item.find("." + $this.attr("toggle"));
            $this.on("change" , function() {
                handleToggle($this, target)
            });
            if($this.is(":radio")) {
                item.find("[name=" + $this.attr("name") + "]:radio").on("change", function() {
                    handleToggle($this, target)
                })
            }
        });
        item.find(".validation-number, .validation-custom").each(function() {
            $(this).triggerHandler("change");
        });
        var lengthFields = item.find(".maxlength-limit, .minlength-limit");
        lengthFields.on("change", function() {
            var rule = "";
            lengthFields.each(function() {
                var $this = $(this), val= $this.val().trim();
                if($this.valid() && val) {
                    rule += $this.attr("rule") + "[" + val + "] "
                }
            });
            item.find(".validation-length").val(rule);
        });
        var numericFields =  item.find(".max-limit, .min-limit");
        numericFields.on("change", function() {
            var rule = "number ";
            numericFields.each(function() {
                var $this = $(this), val = $this.val().trim();
                if($this.valid() && val) {
                    rule += $this.attr("rule") + "[" + val + "] "
                }
            });
            item.find(".validation-number").val(rule);
        });
        item.find(".custom-validation").on("change", function() {
            $(this).siblings("input").val($(this).val())
        });
        item.on("change", function(ev) {
            if($(ev.target).is(".validation-field") == false) {
                var validation = item.serializeObject()["r-validation"];
                if($.isArray(validation)) {
                    validation = validation.join(" ")
                }
                item.find("[name='field.validation']").val(validation).triggerHandler("change")
            }
        });
    };

    _fb._initFieldOptionsConfigSection = function(item) {
        var _self = this;
        var addOptionForm = item.find(".add-option-form"), removeOptionBtn = addOptionForm.find(".remove-btn");
        addOptionForm.form({
            preSubmit: function() {
                var data = this.serializeObject();
                data.type = "option";
                _self.addOrUpdateFieldExtra(data);
                this.clearForm(true);
                removeOptionBtn.addClass("hidden");
                addOptionForm.find(".submit-btn").text($.i18n.prop("add"));
                _self.populateFormField(selectedFormElementObject);
                return false
            }
        });
        function onTypeChange(form) {
            var data = form.serializeObject(), type;
            if(data.selectionType == "single" && data.style == "radio") {
                type = "radioButton"
            } else if(data.selectionType == "multi" && data.style == "radio"){
                type = "checkBox"
            } else {
                type = "dropDown"
            }
            if(type == "dropDown") {
                item.find(".option-type").show("blind")
            } else {
                item.find(".option-type").hide("blind")
            }
            if(type == "dropDown" && data["field.extra.config.option_type"] != "none") {
                addOptionForm.hide("blind")
            } else {
                addOptionForm.show("blind")
            }
            var typeConfig = selectedFormElementObject.find("[name='" + _self.getSelectedFieldId() + ".type']"), oldType = typeConfig.val();
            selectedFormElementObject.removeClass(oldType).addClass(type)
            typeConfig.val(type);
        }
        var typeChangeForm = item.find(".type-change-from");
        typeChangeForm.on("change", function() {
            onTypeChange($(this));
            _self.populateFormField(selectedFormElementObject)
        });
        onTypeChange(typeChangeForm);
        removeOptionBtn.on("click", function () {
            var id = addOptionForm.find('[name=id]').val();
            _self.removeFieldExtra(id, "option");
            _self.populateFormField(selectedFormElementObject);
            addOptionForm.clearForm(true);
            removeOptionBtn.addClass('hidden');
        })
    };

    _fb.initConfigSection = function(item) {
        var _self = this, type = item.attr("type");
        if(_self["_init" + type.camelCase() + "ConfigSection"]) {
            _self["_init" + type.camelCase() + "ConfigSection"](item)
        }
        switch(type) {
            case "basic":
                item.find("[name='field.label']").on("change", function() {
                    var label = $(this).val().trim();
                    if(label) {
                        selectedFormElementObject.find(".editable-label").text(label)
                    }
                });
                break;
            case "file-prop":
                item.find(".check-box-group").on("change", function() {
                    var $this = $(this);
                    var formats = $this.serializeObject()["format"];
                    if($.isArray(formats)) {
                        formats = formats.join(",")
                    }
                    item.find(".extensions").val(formats).triggerHandler("change")
                })
        }
    };

    _fb.getFieldExtraProp = function(fieldId,  field, type, id){
        var fieldPrefix = fieldId + ".extra." + type + "." + id, data = {id: id};
        field.find("[name^='" + fieldPrefix + "']").each(function() {
            var $this = $(this), name = $this.attr("name").split(".");
            name = name[name.length - 1];
            data[name] = $this.val()
        });
        return data
    };

    _fb.getFieldExtraProps = function(fieldId,  field, type) {
        var fieldPrefix = fieldId + ".extra." + type, cache = {}, props =  [];
        field.find("[name^='" + fieldPrefix + "']").each(function() {
            var $this = $(this), propId = $this.attr("prop-id"), name = $this.attr("name").split("."), prop = cache[propId];
            name = name[name.length - 1];
            if(prop == undefined) {
                prop = cache[propId] = {id: propId};
                props.push(prop)
            }
            prop[name] = $this.val()
        });
        return props
    };

    _fb.getFieldConfigs = function(field) {
        var _self = this, field = (field || selectedFormElementObject),
            props = _self.getFieldExtraProps(_self.getSelectedFieldId(field), field, "config"), configs = {};
        props.forEach(function($this) {
           configs[$this.label] = {value: $this.value, id: $this.id}
        });
        return configs
    };

    _fb.addOrUpdateFieldExtra = function(data) {
        var propId = data.id ? data.id : bm.getUUID();
        var fieldId = selectedFormElementObject.find("[name='fieldid']").val(),
            fieldPrefix = fieldId + ".extra." + data.type + "." + propId + ".", valueCache = selectedFormElementObject.find(".value-cache");
        delete data.id;
        delete data.type;
        $.each(data, function(k, v) {
            var propKey = fieldPrefix + k;
            var input = valueCache.find("[name='" + propKey + "']");
            if(input.length) {
                input.val(v.htmlEncode())
            } else {
                valueCache.append($("<input type='hidden' prop-id='" + propId + "' name='" + propKey + "' value='" + v.htmlEncode() + "'>"))
            }
        })
    };

    _fb.removeFieldExtra = function(id, type, field) {
        field = (field || selectedFormElementObject);
        var _self = this, fieldId = _self.getSelectedFieldId(field),
            fieldPrefix = fieldId + ".extra." + type + "." + id + ".", valueCache = field.find(".value-cache");
        field.find("[name^='" + fieldPrefix + "']").remove()
    };

    _fb.removeFieldConfig = function(name, field) {
        var _self = this, config = _self.getFieldConfigs(field)[name];
        if(config) {
            _self.removeFieldExtra(config.id, "config", field)
        }
    };

    _fb.deactivateFormField = function() {
        var _self = this
        if(selectedFormElementObject) {
            selectedFormElementObject.removeClass("selected-element");
            _self.selectedFormElementObject = selectedFormElementObject = null;
        }
        _self.leftBar.find(".setup-config").addClass("active");
        _self.leftBar.find(".field-config").removeClass("active").find(".body").html("")
        _self.setTitle()
    };

    _fb.selectField = function(field) {
        var _self = this;
        if(field.is(".selected-element")) {
            return
        }
        _self.formEditor.find(".field-drop-zone .selected-element").removeClass("selected-element");
        if(selectedFormElementObject) {
            selectedFormElementObject.find(".close-option-header .settings").trigger("blur")
        }
        field.addClass("selected-element");
        _self.selectedFormElementObject = selectedFormElementObject = field;
        _self.renderFieldConfig();
        _self.setTitle()
    };

    _fb.attachFieldEvents = function(field) {
        var _self = this;
        field.on("click", function() {
            var $this = $(this);
            _self.selectField($this)
        });

    };

    _fb.attachRowEvents = function(row) {
        var _self = this;
        row.on("click", function(ev) {
            var $this = $(this);
            if($(ev.target).is(".close-option-header .settings, .close-option-header .close")) {
                return;
            }
            if($this.is(".selected-row")) {
                return
            }
            _self.selectedRow = $this;
            _self.formEditor.find(".field-drop-zone .selected-row").removeClass("selected-row");
            $this.addClass("selected-row")
        });
        row.find(".block").each(function() {
            _self.attachBlockEvents($(this))
        });
        row.on("sort:sort", function(ev, obj) {
            _self.formEditor.find(".field-drop-zone:not(:has(.row), .empty-zone)").remove();
            _self.afterDrop(obj.elm, obj.elm.parents(".field-drop-zone"))
        })
    };

    _fb.prepareDropDownTemplate = function(field, fieldid) {
        var _self = this, options = _self.getFieldExtraProps(fieldid, field, "option"), config = _self.getFieldConfigs(field), dropDown = field.find(".editable-field select");
        field.find(".chosen-container").remove();
        dropDown.chosen();
        if(!config.option_type || config.option_type.value.trim() == "none") {
            options.forEach(function(op) {
                dropDown.chosen("add", {text: op.label, value: op.id});
            });
            var chosen = dropDown.data("wcuiChosen");
            chosen.results_data.forEach(function(item) {
                item.html = '<span class="text">' + item.html + '</span><span class="btn edit">Edit</span>'
            });
            var result_select = chosen.result_select;
            chosen.result_select = function(evt) {
                var retVal = result_select.call(this, arguments);
                if($(evt.target).is(".btn.edit")) {
                    var data = chosen.results_data[chosen.current_selectedIndex];
                    field.find(".selected-option").val(data.value);
                    _self.event.triggerHandler("option-edit", data)
                }
                return retVal;
            };
        }
    };

    _fb.prepareRadioButtonTemplate = function(field, fieldid) {
        var _self = this, options = _self.getFieldExtraProps(fieldid, field, "option");
        var container = field.find(".editable-field");
        container.empty();
        container.append(' <input type="hidden" class="selected-option">');
        options.forEach(function(op) {
            var box = $("<span class='box'><input type=\"radio\" disabled><label>" + op.label.htmlEncode() +  "</label></span>");
            container.append(box);
            box.on("click", function() {
                container.find(".selected-option").val(op.id);
                container.find(".box.selected").removeClass("selected");
                box.addClass("selected");
                if(field.is(".selected-element")) {
                    _self.event.triggerHandler("option-edit", {value: op.id})
                }
            })
        });
        container.find(":radio").radio()
    };

    _fb.prepareTextBoxTemplate = function(field, fieldid) {
        var _self = this, showConfirmEmail = _self.getFieldConfigs(field)["show_confirm_email"];
        var validation = field.find("[name='" + fieldid + ".validation']").val() || "";
        if(showConfirmEmail && validation.contains("email") && showConfirmEmail.value == "true") {
            var insertedElement = $(formEditor.find("#form-fields-template-container .confirmMail-template").html());
            insertedElement.find(".value-cache").remove();
            var label = field.find(".editable-label").remove();
            field.find(".editable-field").html(insertedElement);
            field.find(".email label").text(label.text())
        }
    };

    _fb.prepareCheckBoxTemplate = function(field, fieldid) {
        var _self = this, options = _self.getFieldExtraProps(fieldid, field, "option");
        var container = field.find(".editable-field");
        container.empty();
        container.append(' <input type="hidden" class="selected-option">');
        options.forEach(function(op)  {
            var box = $("<span class='box'><input type=\"checkbox\" disabled><label>" + op.label.htmlEncode() +  "</label></span>");
            container.append(box);
            box.on("click", function() {
                container.find(".selected-option").val(op.id);
                container.find(".box.selected").removeClass("selected");
                box.addClass("selected");
                if(field.is(".selected-element")) {
                    _self.event.triggerHandler("option-edit", {value: op.id})
                }
            })
        });
        container.find(":checkbox").checkbox()
    };

    _fb.prepareFullNameTemplate = function(field, fieldid) {
        var container = field.find(".editable-field");
        var boxes = {};
        var count = 0;
        field.find("[name^='" + fieldid + ".field.']").each(function() {
            var fid = this.name.substring(43, 79);
            var box = boxes[fid];
            if(!box) {
                box = container.find(">*:eq(" + count++ + ")");
                boxes[fid] = box;
                box.data("fieldid", fid)
            }
            var prop = this.name.substring(80);
            switch(prop) {
                case "label":
                    box.find("label").text(this.value)
            }
        });
        container.find(":checkbox").checkbox()
    };

    _fb.prepareFileTemplate = function(field, fieldid) {
        var _self = this, config = _self.getFieldConfigs(field);
        if(config.upload_style && config.upload_style.value == "upload") {
            field.find(".dropzone-wrapper").remove();
            field.find(".masked-file-input").removeClass("masked-file-input")
        }
    };

    _fb.prepareDateTemplate = function(field, fieldid) {
        var _self = this, config = _self.getFieldConfigs(field),
            type = config.type ? config.type.value : "date", dateType = config.date_type ? config.date_type.value : "calender";
        var template = field.find(".sub-template." + type + "-" + dateType)
        if(template.length == 0) {
            template = field.find(".sub-template." + type)
        }
        if(template.length == 0) {
            template = field.find(".sub-template.default")
        }
        field.find(".sub-template").hide()
        template.show()
    };

    _fb.populateFormField = function(field) {
        var fieldid = field.find("[name='fieldid']").val();
        var template = $(newFieldTemplate);
        template.find(".value-cache").remove();
        field.children(":not(.value-cache)").remove();
        field.prepend(template.children());
        var type = field.find("[name='" + fieldid + ".type']").val();
        var label = field.find("[name='" + fieldid + ".label']").val();
        var placable = formEditor.find("#form-fields-template-container ." + type + "-template").clone();
        placable.find(".value-cache").remove();
        var insertedElement = placable.html();
        if(placable.is(".label-less-group-template")) {
            field.find(".editable-label").remove()
        } else {
            field.find(".editable-label").text(label)
        }
        field.find(".editable-field").append(insertedElement);
        var prepareFnc = "prepare" + type.capitalize() + "Template";
        if(this[prepareFnc]){
            this[prepareFnc](field, fieldid)
        }
    };

    _fb.populateFormFields = function() {
        var _self = this;
        formEditor.find(".form-field").each(function() {
            _self.populateFormField($(this))
        })
    };

    _fb.addCondition = function() {
        var _self = this, fields = _self.getFieldList(), selectedField = _self.selectedFormElementObject, fieldId = selectedField.find('[name=fieldid]').val(), activeFieldData = {};
        activeFieldData.label = selectedField.find("[name='" + fieldId + ".label']").val();
        activeFieldData.uuid = fieldId;
        activeFieldData.options = [];
        activeFieldData.conditions = {};
        selectedField.find("[name^='" + fieldId + ".extra.option']").each(function() {
            activeFieldData.options.push($(this).val())
        });
        selectedField.find(".value-cache input.condition").each(function() {
            var $this = $(this), name = $this.attr("name").split(".");
            if(!activeFieldData.conditions[name[2]]) {
                activeFieldData.conditions[name[2]] = {}
            }
            activeFieldData.conditions[name[2]][name[3]] = $this.val();
        });
        var data = {
            activeField: activeFieldData,
            fields: fields
        };
        bm.editPopup(app.baseUrl + "formAdmin/addCondition", $.i18n.prop("add.condition"), null, {data: JSON.stringify(data)}, {
            width: 800,
            events: {
                content_loaded: function(popupObj) {
                    var popup = this, editorTable = popup.find(".form-condition-editor"), lastRow = editorTable.find(".last-row"),
                        rowTemplate = editorTable.find(".template"), activeRow, isUpdating;
                    editorTable.find("tr:gt(0):not(.last-row, .template)").each(function() {
                       attachRowEvents($(this));
                    });
                    lastRow.find(".add-update").on("click", function() {
                        var haveToAppend = false, success = true;
                        if(!activeRow) {
                            activeRow = rowTemplate.clone().removeAttr("style").removeClass("template");
                            haveToAppend = true;
                            attachRowEvents(activeRow)
                        }
                        lastRow.find("select").each(function() {
                            var $this = $(this), field = activeRow.find("." + $this.attr("name"));
                            if(!$this.val()) {
                                success = false;
                                $this.siblings(".chosen-container").addClass("error-highlight");
                                setTimeout(function() {
                                    $this.siblings(".chosen-container").removeClass("error-highlight")
                                }, 5000);
                                return false
                            }
                            field.val($this.val());
                            field.siblings(".text").text($this.find(":selected").text())
                        });
                        if(success && haveToAppend) {
                            lastRow.before(activeRow)
                        }
                        if(success) {
                            activeRow.removeClass("editing");
                            activeRow = null;
                        }

                    });
                    function attachRowEvents(row) {
                        row.find(".edit").on("click", function() {
                            lastRow.find("select").each(function() {
                                var $this = $(this), field = row.find("." + $this.attr("name"));
                                $this.chosen("val", field.val())
                            });
                            activeRow = row;
                            activeRow.addClass("editing")
                        });

                        row.find(".remove").on("click", function() {
                            if(row.addClass(".editing")) {
                                activeRow = null
                            }
                            row.remove();
                        });
                    }
                }
            },
            beforeSubmit: function(form, data, popup) {
                selectedField.find(".value-cache [type=hidden].condition").remove();
                form.find(".form-condition-editor tr:gt(0):not(.last-row, .template)").each(function() {
                    var $this = $(this), uuid = bm.getUUID(), valueCache = selectedField.find(".value-cache");
                    $this.find("input[type=hidden]").each(function() {
                        var $this = $(this);
                        var config = $("<input/>", {
                            name: fieldId + "." + "conditions." + uuid + "." +  $this.attr("name"),
                            type: "hidden",
                            value: $this.val(),
                            class: "condition",
                            "group-id": uuid
                        });
                        valueCache.append(config)
                    });
                });
                _self.event.trigger("condition-update");
                popup.close();
                return false;
            }
        })
    };

    _fb.beforeRemoveFormField = function(field) {
        var _self = this, fieldId = field.find(".value-cache [name=fieldid]").val();
        _self.formEditor.find(".form-field .value-cache [name$='dependentFieldUUID'][value='" + fieldId + "'].condition").each(function() {
           var $this = $(this);
            _self.formEditor.find(".form-field .value-cache [group-id='" + $this.attr('group-id') + "']").remove()
        });
    };

    _fb.removeFormField = function(field) {
        var _self = this;
        if(activeToolConfig){
            activeToolConfig.clearValidator();
        }
        if(field.is(".selected-element")) {
            _self.deactivateFormField();
            this.selectedFormElementObject = selectedFormElementObject = undefined;
            formEditor.find(".tab-header.tab-properties").hide();
            field.parents(".row").removeClass("selected-row")
        }
        _self.beforeRemoveFormField(field);
        field.remove();
    };

    _fb.removeSelectedElement = function() {
        var _self = this, removeElement = this.selectedRow.parents(".field-drop-zone");
        var nextElement, nextField;
        nextElement = (nextElement = removeElement.next()).length ? nextElement : ((nextElement = removeElement.prev()).length ? nextElement : undefined);
        if(activeToolConfig){
            activeToolConfig.clearValidator();
        }
        if(nextElement && (nextField = nextElement.find(".form-field:eq(0)")).length) {
            nextField.trigger("click");
        } else {
            _self.deactivateFormField();
            _self.selectedFormElementObject = selectedFormElementObject = undefined;
            formEditor.find(".tab-header.tab-properties").hide()
        }
        removeElement.find(".form-field").each(function() {
            _self.beforeRemoveFormField($(this))
        });
        removeElement.remove();
    };

    _fb.moveUpRow = function(selectedRow) {
        if(selectedRow.is(":first-child")) {
            return;
        }
        var prev = selectedRow.prev();
        prev.before(selectedRow)
    };

    _fb.moveDownRow = function(selectedRow) {
        if(selectedRow.is(":last-child")) {
            return;
        }
        var next = selectedRow.next();
        if(next.is(".empty-zone")) {
            return;
        }
        next.after(selectedRow)
    };

    _fb.copyRow = function(selectedRow) {
        var newElement = selectedRow.outer();
        selectedRow.find("[name='fieldid'], [name$='.uuid']").each(function() {
            var toCopy = $(this).val();
            var afterCopy = bm.getUUID();
            newElement = newElement.replaceAll(toCopy, afterCopy)
        });
        newElement = selectedRow.after(newElement).next();
        newElement.find(".floating-menu").remove();
        newElement.removeClass("selected-row");
        newElement.find(".selected-element").removeClass("selected-element");
        this.attachRowEvents(newElement.find(".row"))
    };

    _fb.initializeConfigMenu = function() {
        var _self = this;
        function hideMenu(napa) {
            napa.removeClass("expanded up-side").addClass("collapsed");
            configDropDown.off("blur").removeClass("up-side").remove();
        }
        function handleClick(action) {
            switch(action) {
                case "move-up":
                   _self.moveUpRow(_self.selectedRow.parent());
                    break;
                case "move-down":
                    _self.moveDownRow(_self.selectedRow.parent());
                    break;
                case "copy":
                    _self.copyRow(_self.selectedRow.parent());
                    break;
                case "remove":
                    _self.removeSelectedElement();
                    break;
                case "add-block":
                    _self.addBlock();
                    break;
                case "add-condition":
                    _self.addCondition();
                    break;
            }
        }

        formEditor.find(".field-drop-zone-wrapper").on("click", ".settings", function () {
            var navigator = $(this);
            var napa = navigator.parent();
            if (!napa.is("[tabindex]")) {
                napa.attr("tabindex", "0")
            }
            if (!napa.is(".expanded")) {
                napa.removeClass("collapsed").addClass("expanded");
                napa.after(configDropDown);
                var noOfBlockSelectedRow = _self.selectedRow.find(".block").length;
                if(noOfBlockSelectedRow >= 3) {
                    configDropDown.find(".add-block").hide();
                } else {
                    configDropDown.find(".add-block").show();
                }
                if(selectedFormElementObject && selectedFormElementObject.is(".radioButton, .dropDown")) {
                    configDropDown.find(".add-condition").show();
                } else {
                    configDropDown.find(".add-condition").hide();
                }
                var height = napa.outerHeight();
                var menuHeight = configDropDown.outerHeight();
                var requiredBottom = napa.offset().top - $(window).scrollTop() + height + menuHeight;
                var actualBottom = $(window).height();
                if (actualBottom < requiredBottom) {
                    configDropDown.position({
                        my: "right bottom",
                        at: "right top+1",
                        of: napa
                    }).addClass("up-side");
                    napa.addClass("up-side")
                } else {
                    configDropDown.position({
                        my: "right top",
                        at: "right bottom-1",
                        of: napa
                    })
                }
                napa.focus().on("blur", function () {
                    hideMenu(napa)
                })
            } else {
                hideMenu(napa)
            }
        }).on("mousedown", ".floating-menu .menu-item", function () {
            var item = $(this);
            if (item.is(".disabled")) {
                return;
            }
            handleClick(item.attr("action"));
            hideMenu(item.closest(".floating-menu").prev())
        });
        formEditor.find(".field-drop-zone").on("click", ".close", function() {
            _self.removeSelectedElement()
        })
    };

    _fb.getFieldList = function(type, filter) {
        var data = [], selector = ".form-field" + (type ? "." + type : "");
        this.formEditor.find(selector).each(function() {
            var $this = $(this), fieldId = $this.find('[name=fieldid]').val();
            if(filter && filter(fieldId, $this) == false) { return }
            data.push({
                uuid: fieldId,
                label: $this.find("[name='" + fieldId + ".name']").val()
            })
        });
        return data;
    };

    _fb.fieldLabelResolver = function(type, suffix) {
        var label;
        switch(type) {
            case "country":
                label = $.i18n.prop("country");
                break;
            case "weekday":
                label = $.i18n.prop("weekday");
                break;
            case "month":
                label = $.i18n.prop("month");
                break;
            case "dateOfMonth":
                label = $.i18n.prop("date.of.month");
                break;
            case "email":
                label = $.i18n.prop("email");
                break;
            case "label":
                label = $.i18n.prop("label");
                break;
            case "file":
                label = $.i18n.prop("file.upload");
                break;
            default :
                label =   $.i18n.prop("field.label") + " " + suffix;
                break
        }
        return label
    };

    _fb.fieldNameResolver = function(type, suffix) {
        var name;
        switch(type) {
            case "country":
                name = $.i18n.prop("country");
                break;
            case "weekday":
                name = $.i18n.prop("weekday");
                break;
            case "month":
                name = $.i18n.prop("month");
                break;
            case "dateOfMonth":
                name = $.i18n.prop("date.of.month");
                break;
            case "email":
                name = $.i18n.prop("email");
                break;
            default :
                name =   $.i18n.prop("field") + " " + suffix;
                break
        }
        return name
    };

    _fb.attachBlockEvents = function(block) {
        var _self = this;
        block.find(".remove-field").on("click", function() {
            _self.removeBlock(block);
        });
        block.on("click", function() {
            if(block.has(".form-field").length) {
                return
            }
           if(block.is(".selected-block")) {
               _self.selectedBlock = null;
               block.removeClass("selected-block")
           } else if(block.has(".form-field")) {
               if(_self.selectedBlock) _self.selectedBlock.removeClass("selected-block");
               _self.selectedBlock = block;
               block.addClass("selected-block")
           }
        });
        _self.attachFieldEvents(block.find(".form-field"));
    };

    _fb.addBlock = function() {
        var _self = this, blockTemplate = '<div class="block">\
            <span class="tool-icon remove remove-field"><i></i></span></div>';
        _self.destroyDragDrop();
        var noOfBlock = _self.selectedRow.find(".block").length;
        if(noOfBlock == 3) {
            return;
        }
        _self.selectedRow.removeClass("block-container-" + noOfBlock);
        _self.selectedRow.addClass("block-container-" + (noOfBlock + 1));
        var newBlock = $(blockTemplate).addClass("block-" + (noOfBlock + 1)).addClass("sortable");
        _self.selectedRow.append(newBlock);
        _self.attachBlockEvents(newBlock);
        _self.initDragDrop()
        newBlock.triggerHandler("click")
    };

    _fb.removeBlock = function(block) {
        var _self = this, formField = block.find(".form-field"), blockContainer = block.parents(".row");
        if(formField.length) {
            _self.removeFormField(formField);
        }
        if(block.is(".sortable")) {
            _self.sortableObj.removeElement(block);
        }
        if(block.is(".selected-block")) {
            _self.selectedBlock = null
        }
        block.remove();
        _self.mergeBlocks(blockContainer)
    };

    _fb.mergeBlocks = function(container) {
        var blocks = container.find(".block");
        if(blocks.length == 1 && blocks.find(".form-field").length == 0) {
            this.sortableObj.removeElement(container.parent());
            container.parent().remove();
            return;
        }
        blocks.each(function(index) {
            var $this = $(this), clazz = "block block-" + (index + 1) + ($this.is(".sortable") ? " sortable" : "");
            $this.removeClass();
            $this.addClass(clazz);

        });
        container.attr("class").split(" ").every(function(index, cls) {
            if(cls.startsWith("block-container")) {
                container.removeClass(cls)
            }
        });
        container.addClass("block-container-" + blocks.length)
    };

    _fb.initDragDrop = function() {
        var _self = this;
        var sortables = _self.formEditor.find('.field-drop-zone, .field-drop-zone .block.sortable');
        this.sortableObj = new bm.Sortable(sortables, {
            shim: true,
            handle: ".row",
            beforeSort: function(placeholder, sortTarget) {
                return !sortTarget.is(".block")
            }
        });
    };

    _fb.destroyDragDrop = function() {
        var _self = this;
        _self.formEditor.find('.field-drop-zone').off();
        this.sortableObj.destroy()
    };

    _fb.addElement = function(type) {
        var _self = this, block = this.selectedBlock || formEditor.find(".field-drop-zone.empty-zone");
        var isOnBlock = block.is(".block");
        var template = $(isOnBlock ? newFieldTemplate : newElementDropTemplate);
        var placable = formEditor.find("#form-fields-template-container ." + type + "-template");
        var isLabelLess = placable.is(".label-less-group-template");
        if(isLabelLess) {
            template.find(".editable-label").remove()
        }
        var tempCache;
        if(placable.find(".value-cache").length) {
            tempCache = placable.find(".value-cache").remove()
        }
        var insertedElement = placable.html();
        template.find(".editable-field").append(insertedElement);
        if(tempCache) {
            placable.append(tempCache)
        }
        var fieldid = bm.getUUID();
        template.find("[name='fieldid']").val(fieldid);
        var typeField = $("<input type='hidden' name='" + fieldid + ".type'>");
        typeField.val(type);
        var name = $("<input type='hidden' name='" + fieldid + ".name'>");
        var suffix = formEditor.find(".form-field").length;
        var nameValue = _self.fieldNameResolver(type, suffix);
        name.val(nameValue);
        var label = $("<input type='hidden' name='" + fieldid + ".label'>");
        var labelValue = isLabelLess ? "" : _self.fieldLabelResolver(type, suffix);
        label.val(labelValue);
        template.find(".editable-label").text(label.val());
        var cacheHolder = template.find(".value-cache");
        cacheHolder.append(typeField).append(name).append(label);
        if(tempCache) {
            tempCache.children().each(function() {
                var $this = $(this), propId = $this.attr("prop-id"), input = $("<input type='hidden' " + (propId ? "prop-id='" + propId + "'" : "") + "name='" + fieldid + "." + $this.attr("name") + "'>");
                input.val($(this).val());
                cacheHolder.append(input)
            })
        }
        block.append(template);
        var field = isOnBlock ? template : template.find(".form-field");
        field.addClass(type);
        if(_self["prepare" + type.camelCase() + "Template"]) {
            _self["prepare" + type.camelCase() + "Template"](field, fieldid)
        }
        if(isOnBlock) {
            block.removeClass("selected-block");
            _self.selectedBlock = null;
            _self.destroyDragDrop();
            template.parents(".block").removeClass("sortable");
            _self.attachFieldEvents(template);
            _self.initDragDrop();
        } else {
            this.sortableObj.addHandle(template);
            _self.attachRowEvents(template);
            _self.afterDrop(template, template.parents(".field-drop-zone"))
        }
        _self.event.triggerHandler("field-add");
        _self.selectField(field)
    };

    _fb.afterDrop = function(element, sortbleTarget) {
        var _self = this, newDropZone = $(_self.dropZoneTemplate);
        _self.destroyDragDrop();
        if(sortbleTarget.children().length > 1) {
            var isAppendBefore = element.index() == 0;
            newDropZone.append(element);
            if(isAppendBefore) {
                sortbleTarget.before(newDropZone)
            } else {
                sortbleTarget.after(newDropZone)
            }
        }
        if(sortbleTarget.is(".empty-zone")) {
            sortbleTarget.removeClass("empty-zone");
            newDropZone.addClass("empty-zone");
            sortbleTarget.after(newDropZone)
        }
        _self.initDragDrop()
    };

    _fb.getFieldData = function(field, data, keyPrefix) {
        field.find(".value-cache input[type=hidden]:not([name=fieldid])").each(function() {
            var $this = $(this), key = keyPrefix + $this.attr("name");
            if(data[key] == undefined) {
                data[key] = $this.val();
            } else {
                if(!(data[key] instanceof Array)) {
                    data[key] = [data[key]]
                }
                data[key].push($this.val())
            }
        });
    };

    _fb.getSaveData = function() {
        var _self = this, data = this.leftBar.find(".setup-config").serializeObject();
        data.id = this.formEditor.find("[name=id]").val();
        data.rows = [];
        this.formEditor.find(".row").each(function() {
            var row = $(this), blocks = row.find(".block");
            var rowId = bm.getUUID(), fieldIdKey = rowId + "." + "fieldid";
            data.rows.push(rowId);
            data[fieldIdKey] = [];
            blocks.each(function() {
                var block = $(this), formField = block.find(".form-field");
                if(formField.length) {
                    data[fieldIdKey].push(formField.find("[name=fieldid]").val());
                    _self.getFieldData(formField, data, rowId + ".")
                } else {
                    var fieldId = bm.getUUID();
                    data[fieldIdKey].push(fieldId);
                    data[rowId + "." + fieldId + ".type"] = "empty";
                    data[rowId + "." + fieldId + ".name"] = "Empty"
                }
            });
        });
        return data;
    };
});