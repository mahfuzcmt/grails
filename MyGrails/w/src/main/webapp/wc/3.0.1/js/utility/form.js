(function() {
    var DATA_KEY = "form-inst";
    var DEFAULT_OPTIONS = {
        preSubmit: undefined,
        disable_on_submit: true,
        disable_on_invalid: true,
        text_change_on_submit: true
    };

    var passwordStrengthMeterText = ["very.weak", "weak", "better", "medium", "strong", "strongest"];
    var passwordStrengthMeterClass = ["very-weak", "weak", "better", "medium", "strong", "strongest"];

    var careFunctions = $.form_care_functions = {
        checkbox: function() {
            var allchecks =  this.find("input:checkbox[name][uncheck-value]");
            allchecks.pairuncheck()
        },
        auto_toggle: function() {
            if(bm.autoToggle) {
                bm.autoToggle(this)
            }
        },
        textarea: function() {
            if($.form_no_textarea_autosize) {
                return;
            }
            this.find("textarea:not('.no-auto-size')").autosize();
        },
        events: function(obj)  {
            var dom = this;
            var form = obj.elm;
            if(dom.is(form)) { // not an appended dom
                this.bind("submit", function() {
                    if (browser.ie && browser.version == 8) {
                        var isPrevent = dom.data("ie8-prevent-submit");
                        if (isPrevent) {
                            dom.removeData("ie8-prevent-submit");
                            return false;
                        }
                    }
                    return instanceFuncs.submit.call(obj)
                });
            }
            if(!form.is("form")) {
                dom.find("input, textarea").bind("keydown.key_return", function() {
                    form.triggerHandler("submit");
                    if(browser.ie && browser.version == 8) {
                        var closestForm = form.closest("form");
                        if(closestForm.length) {
                            closestForm.data("ie8-prevent-submit", true);
                        }
                    } else {
                        return false;
                    }
                });
                if(dom.is(form)) { // not an appended dom
                    obj.submitButton.click(function () {
                        dom.triggerHandler("submit")
                    });
                    dom.find(".reset-button").click(function () {
                        dom.triggerHandler("reset");
                    });
                }
            }
        },
        validation: function(obj) {
            var validConfig = obj.validation_config || {},
                errorPosition = this.attr("error-position");
            if(errorPosition) {
                validConfig.error_position = errorPosition
            } else if (window.app && app.is_front_end) {
                var validationConfigKey = this.attr("validation-config-key");
                if (validationConfigKey) {
                    var errorPosition = app.config["validation_" + validationConfigKey + "_error_position"];
                    if (errorPosition) {
                        validConfig.error_position = errorPosition
                    }
                }
            }
            if(this.is(obj.elm)) {
                this.attachValidator(validConfig);
            } else {
                obj.elm.obj(ValidationPanel).attach(this.find("[validation]"), validConfig)
            }
        },
        restrict: function() {
            this.find(":text[restrict]").each(function() {
                var input = $(this);
                input[input.attr("restrict")]();
            });
        },
        unique: function() {
            var _self = this;
            var action = this.attr("action") || this.attr("unique-action");
            if(!action) {
                return
            }
            var last = action.lastIndexOf("/");
            action = action.substring(0, last) + "/";
            var edit = this.find("input[name=id]").length;
            var uniqueFieldCount = 0;
            this.find(".unique").each(function() {
                var field = $(this);
                bm.unique({
                    url: action + (field.attr("unique-action") || "isUnique"),
                    restoreUrl: action + (field.attr("unique-restore") || "restoreFromTrash"),
                    editMode: edit,
                    elm: field,
                    form: _self,
                    uniqueFieldCount: uniqueFieldCount+=1,
                    fieldError: false,
                    inTrash: false
                })
            })
        },
        focus_on_row: function() {
            this.find(".form-row > input").focus(function() {
                $(this).parent().addClass("active-row");
            }).blur(function() {
                $(this).parent().removeClass("active-row");
            })
        },
        disable_on_invalid: function(instance) {
            var _form = this;
            if(!instance.disable_on_invalid) {
                return;
            }
            var invalid = false;
            _form.find("[validation]").each(function() {
                var lockId = bm.getUUID();
                $(this).bind("invalid.form_js", function(e) {
                    if(e.target == e.currentTarget) {
                        instanceFuncs.lock.call(instance, lockId);
                        if(!invalid) {
                            var element = $(this);
                            if(window.app && !app.is_front_end) {
                                var scrollPanel = element.parents(".scrollable.vertical");
                                if(scrollPanel.length) {
                                    scrollPanel.scrollTo(element, {offsetTop: "180"});
                                }
                            }
                        }
                        invalid = true;
                    }
                }).bind("remove.form_js clear-error.form_js", function(e) {
                    if(e.target == e.currentTarget) {
                        instanceFuncs.unlock.call(instance, lockId);
                        invalid = false;
                    }
                })
            })
        },
        password_strength_meter: function() {
            var _form = this;
            var passwords = _form.find(".password-strength-meter");
            $.each(passwords, function(index, passwordField) {
                $(passwordField).on("keyup", function() {
                    var password = $(passwordField).val();
                    var score   = 0;
                    //if password bigger than 6 give 1 point
                    if(password.length > 6) score++;
                    //if password has both lower and uppercase characters give 1 point
                    if((password.match(/[a-z]/) ) && ( password.match(/[A-Z]/) ) ) score++;
                    //if password has at least one number give 1 point
                    if(password.match(/\d+/)) score++;
                    //if password has at least one special caracther give 1 point
                    if(password.match(/.[!,@,#,$,%,^,&,*,?,_,~,-,(,)]/) )	score++;
                    //if password bigger than 12 give another 1 point
                    if(password.length > 12) score++;

                    var passwordMeterDom;
                    var siblings = $(passwordField).siblings();
                    $.each(siblings, function(idx, sibling) {
                        if($(sibling).hasClass("password-meter-wrap")) {
                            passwordMeterDom = $(sibling);
                            return false;
                        }
                    });
                    if (!passwordMeterDom) {
                        passwordMeterDom = $("<span class='password-meter-wrap'></span>");
                        $(passwordField).after(passwordMeterDom);
                    }
                    var descDom = passwordMeterDom.find(".password-description");
                    if(descDom.length == 0) {
                        passwordMeterDom.append("<div class='password-description'></div>");
                    }
                    descDom.text($.i18n.prop(passwordStrengthMeterText[score]));
                    if(password.length == 0) {
                        descDom.text("");
                    }
                    var barDom = passwordMeterDom.find(".password-strength-bar");
                    if(barDom.length == 0) {
                        passwordMeterDom.append("<div class='password-strength-bar'></div>");
                    }
                    barDom.removeClass();
                    barDom.addClass("password-strength-bar " + passwordStrengthMeterClass[score]);
                    if(password.length == 0) {
                        barDom.removeClass();
                    }
                });
            });
        },
        abn_input_formatting: function () {
            var _form = this;
            var abnField = _form.find(".abn-validation");
            abnField.on({
                keypress: function (element) {
                    var key = element.which;
                    if (!(key >= 48 && key <= 57)) {
                        element.preventDefault();
                        return false;
                    }
                },
                keyup: function (element) {
                    var key = element.which;
                    var abnValue = abnField.val();
                    var abnFormattedValue = '';
                    if (!(key >= 37 && key <= 40) && (key != 8)) {
                        abnValue = abnValue.replace(/\s+/g, "");
                        if (abnValue.length > 2) {
                            for (var index = 0; index < abnValue.length; index++) {
                                if (index == 2) {
                                    abnFormattedValue += " ";
                                } else if (index == 5) {
                                    abnFormattedValue += " ";
                                } else if (index == 8) {
                                    abnFormattedValue += " ";
                                }
                                abnFormattedValue += abnValue[index];
                            }
                            abnField.val(abnFormattedValue);
                        }
                    }
                }
            });
        }
    };

    var instanceFuncs = {
        append: function(dom) {
            var instance = this;
            $.each(careFunctions, function() {
                this.call(dom, instance);
            })
        },
        lock: function(lockId) {
            if(!this.lock) {
                this.lock = {};
            }
            this.lock[lockId] = true;
            if(this.submitButton) {
                this.submitButton.attr("disabled", "disabled");
            }
            this.elm.trigger("lock")
        },
        unlock: function(lockId) {
            if(this.lock && this.lock[lockId]) {
                delete this.lock[lockId];
            }
            if($.isEmptyObject(this.lock)) {
                if(this.submitButton) {
                    this.submitButton.removeAttr("disabled");
                }
                this.elm.trigger("unlock")
            }
        },
        prop: function(name, value) {
            return bm.prop(this, name, value)
        },
        force_submit: function(settings) {
            var _form = this, lockId = bm.getUUID();
            this.elm.addClass("submitting");
            instanceFuncs.lock.call(this, lockId);
            if (this.disable_on_submit && this.submitButton) {
                if(this.text_change_on_submit) {
                    _form.submitButton.each(function() {
                        var btn = $(this);
                        var text = btn.text();
                        this.orgText = text;
                        if (_form.disable_button_text) {
                            btn.text(_form.disable_button_text);
                        } else {
                            if (text == $.i18n.prop("create")) {
                                text = $.i18n.prop("creating")
                            } else if (text == $.i18n.prop("update")) {
                                text = $.i18n.prop("updating")
                            } else {
                                text = $.i18n.prop("submitting")
                            }
                            btn.text(text + " ...");
                        }
                    })
                }
                this.submitButton.attr("disabled", "disabled")
            }
            var _def_settings = $.extend({}, this.ajax === true ? {} : this.ajax);
            var _response;
            settings = settings || {};
            if(_def_settings.success && settings.success) {
                settings.success = settings.success.blend(_def_settings.success)
            }
            if(_def_settings.error && settings.error) {
                settings.error = settings.error.blend(_def_settings.error)
            }
            if(_def_settings.response || settings.response) {
                _response = function() {
                    if(settings.response) {
                        settings.response.apply(this, arguments);
                    }
                    if(_def_settings.response) {
                        _def_settings.response.apply(this, arguments);
                    }
                }
            }
            if(_def_settings.complete && settings.complete) {
                settings.complete = settings.complete.blend(_def_settings.complete)
            }
            var modified_settings = $.extend({}, _def_settings, settings);
            if(!this.elm.is("form")) {
                modified_settings.data = $.extend(this.elm.serializeObject(), modified_settings.data);
                if(!modified_settings.url && modified_settings.controller) {
                    modified_settings.url = app.baseUrl + modified_settings.controller + "/" + modified_settings.action
                }
            }
            this.elm.ajaxSubmit($.extend(modified_settings, {
                response: function (type, response, status, xhr) {
                    _form.elm.removeClass("submitting");
                    instanceFuncs.unlock.call(_form, lockId);
                    if(type == "success") {
                        _form.elm.trigger("ajax-submit-success", [response, status, xhr])
                    } else {
                        _form.elm.trigger("ajax-submit-error", [xhr, status, response])
                    }
                    if (_form.disable_on_submit) {
                        if (_form.text_change_on_submit) {
                            _form.submitButton.each(function() {
                                $(this).text(this.orgText);
                            })
                        }
                        _form.submitButton.removeAttr("disabled", "disabled")
                    }
                    if (_response) {
                        _response.apply(_form.elm, arguments);
                    }
                }
            }));
        },
        submit: function(settings) {
            if (!$.isEmptyObject(this.lock)) {
                return false;
            }
            try {
                if (!this.elm.valid()) {
                    if(settings && settings.invalid) {
                        settings.invalid.call(this.elm)
                    }
                    return false;
                }
                if(this.elm.attr("disabled")) {
                    return false;
                }
                var beforeSubmitRet = true;
                if (typeof this.preSubmit === 'function') {
                    settings = settings || (this.ajax ? {} : null);
                    var _success;
                    if(settings) {
                        _success = settings.success;
                        settings.success = undefined;
                    }
                    beforeSubmitRet = this.preSubmit.call(this.elm, settings);
                    if(settings && settings.success && _success) {
                        settings.success = settings.success.blend(_success);
                    } else if(settings && _success) {
                        settings.success = _success
                    }
                }
                if (beforeSubmitRet !== false && this.ajax) {
                    instanceFuncs.force_submit.call(this, settings);
                    return false;
                }
                if(beforeSubmitRet !== false  && this.disable_on_submit && !this.ajax) {
                    this.onSubmitLockId = bm.getUUID();
                    instanceFuncs.lock.call(this, this.onSubmitLockId);
                }
                return beforeSubmitRet;
            } catch (ex) {
                log(ex.stack);
                return false;
            }
        },
        set_submit: function(button) {
            this.submitButton = button
        }
    };

    function init(instance) {
        var _self = this;
        var obj = this.data(DATA_KEY);
        if(!obj.submitButton) {
            obj.submitButton = this.find("[type='submit'], .submit-button");
            if(obj.submitButton.length > 1) {
                this.attr("class").split(" ").every(function() {
                    var button = obj.submitButton.filter("." + this + "-submit");
                    if(button.length) {
                        obj.submitButton = button;
                        return false;
                    }
                })
            }
        }
        obj.disable_on_invalid = this.attr("disable-on-invalid") ? this.attr("disable-on-invalid") == "true" :
            (instance.hasOwnProperty("disable_on_invalid") ? obj.disable_on_invalid : obj.disable_on_invalid);
        $.each(careFunctions, function() {
            this.call(_self, instance);
        })
    }

    $.fn.form = function(funcs) {
        var forms = this;
        if(typeof funcs == "string") {
            var rtrn;
            var _arguments = Array.prototype.slice.call(arguments, 1);
            forms.each(function() {
                var obj = $(this).data(DATA_KEY);
                if(!obj) {
                    return true
                }
                if(typeof instanceFuncs[funcs] == "function") {
                    rtrn = instanceFuncs[funcs].apply(obj, _arguments);
                } else {
                    rtrn = obj[funcs]
                }
                if(typeof rtrn != "undefined") {
                    return false
                }
            });
            if(typeof rtrn != "undefined") {
                return rtrn
            }
            return this;
        }
        forms.each(function() {
            var form = $(this);
            if(!form.data(DATA_KEY)) {
                var inst = $.extend({}, DEFAULT_OPTIONS, funcs, {elm: form});
                form.data(DATA_KEY, inst);
                init.call(form, inst);
            }
        });
        return this;
    }
})();
