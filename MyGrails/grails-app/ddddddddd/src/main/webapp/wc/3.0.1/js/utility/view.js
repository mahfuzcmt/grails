bm.view = {};

$.extend(bm, {
    alert: function (message, type, okHandler) {
        var dom = $("<div><div class='header-line'><span class='icon " + type + "'></span><span class='ok'></span></div><div class='body'><span class='message content'></span></div></div>");
        var okButton = dom.find(".ok").text($.i18n.prop("ok"));
        return dom.popup({
            is_fixed: true,
            is_always_up: true,
            clazz: "alert-popup " + type,
            content: message,
            events: {
                render: function (popup) {
                    okButton.click(function () {
                        if (okHandler) {
                            okHandler(0);
                        }
                        popup.close(0)
                    });
                    if (okHandler) {
                        popup.on("close", function (ev, code) {
                            if (code) {
                                okHandler(1);
                            }
                        })
                    }
                }
            }
        }).obj(POPUP);
    },
    attachModernUiPanel: function () { // TODO: Should be removed
        var _self = this;
        var leftPanel = _self.body.find(".modern-list-left-panel");
        if (leftPanel.length) {
            var leftPanelConfig = $.extend({
                thumbPanel: $(),
                url: "",
                thumb_menu_entries: [],
                thumbMenuClick: function (config) {
                },
                leftNavigationClick: function (type) {
                }
            }, _self.leftPanelConfig || {});

            leftPanel.find(".left-panel-btn button").click(function () {
                var newItem = leftPanel.find(".item-thumb.new-item");
                var selected = leftPanel.find(".body .selected").attr("item-id");
                if (newItem.length) {
                    newItem.find(".item-title").focus();
                    return;
                }
                var newDom = leftPanelConfig.thumbPanel.clone();
                _self.body.find(".left-panel .item-wrapper").prepend(newDom);
                var field = ValidationField.createDetachedField(newDom.find("input"), {
                    error_position: "after"
                });
                var applyClicked = false;
                newDom.find(".item-title").focus().on("keyup.key_return", function () {
                    updateName();
                }).on("blur", function () {
                    if (!applyClicked) {
                        _self.reload(selected, true);
                    }
                    applyClicked = false;
                });

                newDom.find(".tool-icon.apply").mousedown(function () {
                    applyClicked = true
                }).click(function () {
                    updateName();
                });

                function updateName() {
                    if (field.validate()) {
                        var name = field.elm.val();
                        bm.ajax({
                            show_response_status: false,
                            url: leftPanelConfig.url,
                            data: {name: name},
                            success: function (resp) {
                                _self.reload(resp.id, true, function (id) {
                                    leftPanel.find(".item-thumb[item-id=" + id + "]").trigger("click");
                                });
                            },
                            error: function (a, b, resp) {
                                field.elm.focus();
                                field.showError({
                                    msg_template: resp.message,
                                    msg_params: [],
                                    rule: undefined
                                })
                            }
                        })
                    } else {
                        field.elm.focus()
                    }
                }
            });
            bm.menu(leftPanelConfig.thumb_menu_entries, _self.body.find(".left-panel .float-menu-navigator"), null, {
                hide: function (entity) {
                    entity.parent().removeClass("float-menu-opened");
                },
                click: function (action, navigator) {
                    var config = navigator.closest(".item-thumb").config("item");
                    leftPanelConfig.thumbMenuClick.call(_self, action, config);
                }
            }, "click", ["center bottom", "right+22 top+7"]);

            _self.body.find(".item-thumb").on("click", function (e) {
                var $this = $(this);
                if ($(e.target).is(".float-menu-navigator")) { //prevent firing for childs
                    return;
                }
                _self.body.find(".item-thumb.selected").removeClass("selected");
                $this.addClass("selected");
                var selected = $this.attr("item-id");
                _self.reload(selected);
            });
            leftPanel.find(".one-line-scroll-content").scrollbar({
                show_vertical: false,
                show_horizontal: true,
                use_bar: false,
                visible_on: "auto",
                horizontal: {
                    handle: {
                        left: leftPanel.find(".left-scroller"),
                        right: leftPanel.find(".right-scroller")
                    },
                    step: "auto"
                }
            });
            leftPanel.find(".navigation-button").on("click", function () {
                var $this = $(this);
                leftPanelConfig.leftNavigationClick.call(_self, $this.attr("item-type"))
            });
            //Modern List Left Panel End
        }
    },
    autoToggle: function (box) {
        var namespace = bm.getUUID();

        function filterRadios(next_target) {
            var radios = next_target.filter(":radio");
            next_target = next_target.not(radios);
            var processed = [];
            radios.each(function () {
                var $this = $(this), name = $this.attr("name");
                if (processed.contains(name)) {
                    return;
                }
                processed.push(name);
                var win = radios.filter("[name='" + name + "']:checked");
                if (!win.length) {
                    win = box.find("[name='" + name + "']:checked")
                }
                if (!win.length) {
                    win = $this
                }
                next_target = next_target.add(win)
            });
            return next_target
        }

        function chainToggle(target, show, mainTargetSelector) {
            var next_target = target.find("[toggle-target]");
            if (target.is("[toggle-target]")) {
                next_target = next_target.add(target)
            }
            next_target = filterRadios(next_target);
            next_target.each(function () {
                var _target = $(this);
                var evName = _target.is("select, :checkbox, :radio") ? "change" : "click";
                if (show) {
                    _target.triggerHandler(evName + "." + namespace, [false, mainTargetSelector])
                } else {
                    _target.triggerHandler(evName + "." + namespace, [true, mainTargetSelector])
                }
            })
        }

        function handleToggle(target, checked, animation, mainTargetSelector) {
            target.each(function () {
                var _target = $(this);
                if (mainTargetSelector && _target.is(mainTargetSelector)) {
                    return;
                }
                var reverse = _target.is("[do-reverse-toggle]");
                var _checked = checked;
                if (reverse) {
                    _checked = !checked
                }
                var is_hide = _target.css("display") == "none";
                if (_checked && is_hide) {
                    var done = function () {
                        chainToggle(_target, true, mainTargetSelector);
                    };
                    if (animation) {
                        _target.show(animation, done);
                    } else {
                        _target.show();
                        done()
                    }
                } else if (!_checked && !is_hide) {
                    var done = function () {
                        var validatable = _target.find('[validation]:not([validate-on="call-only"])');
                        if (_target.is("[validation]")) {
                            validatable = validatable.add(_target)
                        }
                        validatable.trigger('[validation]:not([validate-on="call-only"])');
                        chainToggle(_target, false, mainTargetSelector);
                        _target.find("input").valid();
                    };
                    if (animation) {
                        _target.hide(animation, done);
                    } else {
                        _target.hide();
                        done()
                    }
                } else {
                    chainToggle(_target, _checked, mainTargetSelector);
                }
            })
        }

        function isATargetOrInChain(toggler) {
            var yes = false;
            var targets = box.find("[toggle-target]");
            targets.each(function () {
                var target = $(this).attr("toggle-target");
                if ($(this).is("select")) {
                    var options = $(this).find("option");
                    options.each(function () {
                        var targetSelector = "." + target + "-" + $(this).val();
                        if (toggler.closest(targetSelector, box[0]).length) {
                            yes = true;
                            return false;
                        }
                    })
                } else {
                    yes = toggler.closest("." + target, box[0]).length
                }
                if (yes) {
                    return false
                }
            });
            return yes;
        }

        var getAllChainedTargets = function (toggler) {
            var allTogglers = toggler;
            toggler.each(function () {
                var _toggler = $(this);
                var nextTarget;
                if (_toggler.is("select")) {
                    var type = _toggler.val().replaceAll("\\.", "_");
                    nextTarget = box.find("." + _toggler.attr("toggle-target") + "-" + type);
                } else {
                    nextTarget = box.find("." + _toggler.attr("toggle-target"))
                }
                if (nextTarget.length) {
                    var nextTogglers = nextTarget.find("[toggle-target]");
                    nextTogglers = nextTogglers.add(nextTarget.filter("[toggle-target]"));
                    if (nextTogglers.length) {
                        allTogglers = allTogglers.add(getAllChainedTargets(nextTogglers))
                    }
                }
            });
            return allTogglers
        };
        var allTargets = box.find("[toggle-target]").each(function () {
            var toggler = $(this);
            var animation = toggler.attr("toggle-anim") ? toggler.attr("toggle-anim") : "blind";
            if (animation == "none") {
                animation = null;
            }
            var evName;
            if (toggler.is("select")) {
                evName = "change";
                toggler.on(evName + "." + namespace, function (e, forceOff, excludeTargets) {
                    if (typeof forceOff != "boolean") {
                        forceOff = false;
                        excludeTargets = null
                    }
                    var type = toggler.val().replaceAll("\\.", "_");
                    var target = "." + toggler.attr("toggle-target") + "-" + type;
                    var toHideTargets = $();
                    toggler.find("option").each(function (i, opt) {
                        var option_type = $(opt).val().replaceAll("\\.", "_");
                        if (option_type != type) {
                            var selector = toggler.attr("toggle-target") + "-" + option_type;
                            var hideTarget = box.find("." + selector);
                            toHideTargets = toHideTargets.add(hideTarget.not(target))
                        }
                    });
                    target = box.find(target);
                    var subExcludeTargets = excludeTargets ? excludeTargets.add(getAllChainedTargets(target)) : getAllChainedTargets(target);
                    handleToggle(toHideTargets, false, animation, subExcludeTargets);
                    handleToggle(target, forceOff ? false : true, animation, excludeTargets)
                });
            } else if (toggler.is(":checkbox, :radio")) {
                evName = "change";
                var targetText = "." + toggler.attr("toggle-target");
                var target = box.find(targetText);

                function changeHandler(e, forceOff, excludeTargets, isMain) {
                    if (typeof forceOff != "boolean") {
                        forceOff = false;
                        excludeTargets = null
                    }
                    if (!forceOff) {
                        var otherRadios = box.find("[name='" + toggler.attr("name") + "']").not(toggler).filter("[toggle-target]:not([independent])");
                        var toHideTargets = $();
                        otherRadios.each(function (i, opt) {
                            var hideTarget = box.find("." + $(opt).attr("toggle-target"));
                            toHideTargets = toHideTargets.add(hideTarget.not(target))
                        });
                        handleToggle(toHideTargets, false, animation, isMain ? (excludeTargets ? excludeTargets.add(getAllChainedTargets(target)) : getAllChainedTargets(target)) : excludeTargets)
                    }
                    var checked = toggler.prop("checked");
                    toggler.state = checked;
                    if (toggler.is(".toggle-reverse")) {
                        checked = !checked
                    }
                    handleToggle(target, forceOff ? false : checked, animation, excludeTargets, isMain);
                }

                toggler.on(evName + "." + namespace, function (a, b, c) {
                    changeHandler(a, b, c, true)
                });
                if (toggler.is(":radio")) {
                    var otherRadios = box.find("[name='" + toggler.attr("name") + "']:radio").not("[toggle-target]");
                    otherRadios.on(evName + "." + namespace, function (e, xx, excludeTarget) {
                        if (typeof xx != "boolean") {
                            excludeTarget = null
                        }
                        if (toggler.state || toggler.state === undefined) {
                            changeHandler(e, true, excludeTarget)
                        }
                    })
                }
            } else {
                evName = "click";
                var target = box.find("." + toggler.attr("toggle-target"));
                toggler.on(evName + "." + namespace, function (e, forceOff, excludeTargets) {
                    var checked = toggler.attr("row-expanded") == "true";
                    if (typeof forceOff == "undefined") {
                        checked = !checked;
                        toggler.attr("row-expanded", "" + checked)
                    }
                    handleToggle(target, forceOff ? false : checked, animation, excludeTargets)
                })
            }
        });
        filterRadios(allTargets).each(function () {
            var toggler = $(this);
            var evName = toggler.is("select, :checkbox, :radio") ? "change" : "click";
            if (!isATargetOrInChain(toggler)) {
                toggler.triggerHandler(evName + "." + namespace, [false]);
            }
        });
        return box
    },
    cBaseDoc: function (nodes) {
        var slideDom = $("<div id='documentation-slide' style='display: none'><div class='icon-block'><span class='tool-icon close-icon' title='" + $.i18n.prop("close") + "'></span></div><iframe src='about:blank'></iframe></div>");
        var body = $(document.body);
        nodes.each(function () {
            var node = $(this);
            if (!node.attr("attached")) {
                node.click(function () {
                    var docUrl = node.attr("target-url");
                    if (slideDom.is(":hidden")) {
                        body.prepend(slideDom);
                        slideDom.css({height: window.innerHeight});
                        slideDom.find(".tool-icon.close-icon").click(function () {
                            slideDom.toggle("slide", {direction: 'right'}, function () {
                                body.find("#documentation-slide").remove();
                            });
                        });
                        slideDom.toggle("slide", {direction: 'right'});
                        slideDom.find("iframe").attr("src", docUrl);
                    } else {
                        slideDom.find("iframe").attr("src", docUrl);
                    }
                });
                node.attr("attached", true);
            }
        });
    },
    clearTextSelection: function (_document) {
        if (!_document) {
            _document = document
        }
        var selection = null;
        var _window = _document.defaultView;
        if (_window.getSelection) {
            selection = _window.getSelection();
        } else if (_document.selection) {
            selection = _document.selection;
        }
        if (selection) {
            if (selection.empty) {
                selection.empty();
            }
            if (selection.removeAllRanges) {
                selection.removeAllRanges();
            }
        }
    },
    confirm: function (message, yesHandler, noHandler, cancelHandler, config) {
        if (!config) {
            config = {}
        }
        $.extend(config, {
            focus_button_class: "yes-button"
        });
        var dom = $("<div><div class='header-line'><span class='title'></span><span class='icon close'></span></div><div class='body'><span class='message content'></span></div><div class='button-line'></div></div>");
        dom.find(".title").text($.i18n.prop("confirmation"));
        return dom.popup({
            close_on_escape: false,
            is_fixed: true,
            is_always_up: true,
            clazz: "confirm-popup " + (config.clazz ? config.clazz : ""),
            content: message,
            events: {
                render: function (popup) {
                    var buttonLine = this.find(".button-line");

                    var crossIcon = dom.find(".icon");
                    crossIcon.click(function () {
                        popup.close()
                    });
                    if (yesHandler instanceof Array) {
                        yesHandler.every(function () {
                            var btnObj = this;
                            var button = $("<button class='" + btnObj.clazz + "'></button>");
                            button.text($.i18n.prop(btnObj.text));
                            buttonLine.append(button);
                            button.on("click", function () {
                                btnObj.handler.call(popup);
                                popup.close();
                            });
                            button.focus();
                        });
                    } else if (yesHandler) {
                        if (noHandler) {
                            button = $("<button class='no-button'></button>");
                            button.text($.i18n.prop("no"));
                            buttonLine.append(button);
                            button.click(function () {
                                if ($.isFunction(noHandler)) {
                                    noHandler.call(popup);
                                }
                                popup.close()
                            });
                            if (cancelHandler) {
                                var button = $("<button class='cancel-button'></button>");
                                button.text($.i18n.prop("cancel"));
                                buttonLine.append(button);
                                button.click(function () {
                                    if ($.isFunction(cancelHandler)) {
                                        cancelHandler();
                                    }
                                    popup.close()
                                })
                            }
                        }

                        var button = $("<button class='yes-button'></button>");
                        button.text($.i18n.prop("yes"));
                        buttonLine.append(button);
                        button.click(function () {
                            popup.close();
                            yesHandler.call(popup);
                        });
                    }

                    if (config.focus_button_class != false) {
                        var btn = buttonLine.find("." + config.focus_button_class).addClass("confirm-focus");

                        popup.el.on("keyup", function (evt) {
                            if (btn.hasClass("confirm-focus")) {
                                switch (evt.keyCode) {
                                    case 13:
                                        btn.trigger("click");
                                        break;
                                    case 37:
                                        var tmp = btn.prev("button");
                                        btn = tmp.length ? tmp : btn;
                                        break;
                                    case 39:
                                        var tmp = btn.next("button");
                                        btn = btn = tmp.length ? tmp : btn;
                                        break;
                                }
                                focusBtn(btn);
                            }
                        });

                        function focusBtn(btn) {
                            buttonLine.find("button").removeClass("confirm-focus");
                            btn.addClass("confirm-focus");
                        }
                    }

                }
            }
        }).obj(POPUP);
    },
    countryChange: function (container, config) {
        container.find("#countryId").on('change', function () {
            var name = this.name;
            var type;
            if (name.indexOf(".")) {
                type = name.split(".")[0]
            }
            var prefixed = $(this).attr("name").endsWith("default_country");
            var id = $(this).val();
            var data = $.extend({}, {
                id: id,
                inputClass: "large",
                stateLabel: prefixed ? "default.state" : "state",
                stateName: (type ? type + "." : "") + (prefixed ? "default_state" : "state"),
                noSelection: null
            }, config);
            bm.ajax({
                url: app.baseUrl + "app/loadStateForCountry",
                dataType: 'html',
                data: data,
                success: function (data) {
                    container.find(".form-row.state-selector-row").remove();
                    container.find(".form-row.country-selector-row").after(data);
                    container.find(".form-row.state-selector-row").updateUi();
                    container.trigger("state-load");
                }
            })
        });
    },
    countryChangeSelection: function (countrySelect, isMultiple, params) {
        var countryRow = countrySelect.closest(".form-row");
        var stateSelectionRow = countryRow.next('.state-selector-row');
        var postCodeRow = countryRow.siblings('.post-code');
        if (stateSelectionRow) {
            bindStateChange(stateSelectionRow);
        }
        var country = countrySelect.val();
        if (!country || country.length > 1) {
            postCodeRow.hide();
        }
        countrySelect.change(function () {
            var value = $(this).val();
            stateSelectionRow.remove();
            if ((value && value.length == 1) || !isMultiple) {
                $.extend(params, {id: isMultiple ? value[0] : value});
                bm.ajax({
                    url: app.baseUrl + "app/loadStateForCountry",
                    dataType: 'html',
                    data: params,
                    success: function (data) {
                        stateSelectionRow = $(data).insertAfter(countryRow);
                        if (stateSelectionRow) {
                            bindStateChange(stateSelectionRow);
                            stateSelectionRow.updateUi();
                        }
                        countryRow.parents("form").trigger("state-load")
                    }
                });
                postCodeRow.show();
            } else if (isMultiple) {
                postCodeRow.hide();
            }
        });
        function bindStateChange(stateSelectionRow) {
            stateSelectionRow.find("select[name=zone\\.state\\.id]").change(function () {
                var value = $(this).val();
                if (value && value.length > 1) {
                    postCodeRow.hide();
                } else if (postCodeRow) {
                    postCodeRow.show();
                }
            }).trigger("change");
        }
    },
    createMenu: function (entries, parentUiClass) {
        if (entries instanceof $) {
            return entries.addClass("floating-menu")
        }
        var menu = $("<div class='floating-menu' tabindex='0'></div>");
        $.each(entries, function () {
            var ui_class = this.ui_class;
            var item;
            if (ui_class == "item-separator") {
                item = $("<div class='" + ui_class + "'></div>");
            } else {
                item = $("<div class='menu-item'><span class='icon'></span><span class='label'></span></div>");
                if (this.href) {
                    item.addClass(this.ui_class);
                    item = item.wrap("<a></a>").parent().attr("href", this.href).attr("target", this.target || "_self")
                }
                item.find(".label").text(this.text);
                item.addClass(this.ui_class);
                if (this.license && app.licenses && !app.licenses[this.license]) {
                    item.addClass("restricted insufficient-provision");
                }
                if (this.permission) {
                    item.attr("permission", this.permission)
                }
                item.attr("action", (parentUiClass ? parentUiClass + ">" : "") + (this.action || ui_class));
                if (this.data) {
                    item[0].data = this.data
                }
            }
            var itemEl = item[0];
            menu.append(item);
            if (this.sub) {
                itemEl.sub = bm.createMenu(this.sub, ui_class).addClass("sub-menu")
            }
        });
        return menu;
    },
    customerAndGroupSelectionPopup: function (inputForm, configs) {
        var customerField, groupField;
        var url = configs.url || app.baseUrl + "customerAdmin/selectCustomerAndGroups";
        var title = configs.title || $.i18n.prop("select.customers.or.groups", [app.ecommerce.bool()?"Customer":"Member"]);
        var emp_title = configs.emphasized;

        var successHandler = configs.success;
        customerField = configs.customerField || "customer";
        groupField = configs.groupField || "customerGroup";
        var data = configs.data || {};
        $.extend(data, {
            customerField: customerField,
            groupField: groupField
        });
        var previewPanel = configs.preview_panel || inputForm;
        var previewer = configs.previewer || function (data, form) {
            if (!previewPanel.find("input[name=isCustomerSelectorDirty]").length) {
                previewPanel.append('<input type="hidden" name="isCustomerSelectorDirty" value="true">');
            }
            previewPanel.find("input[name='" + customerField + "'], input[name='" + groupField + "']").remove();
            form.find("input:hidden[name]").each(function () {
                var name = $(this).attr("name");
                var inp = $("<input type='hidden' name='" + name + "' value='" + $(this).val() + "'>");
                previewPanel.append(inp)
            });
        };
        var data = {}, customers = data[customerField] = [], groups = data[groupField] = [];
        previewPanel.find("input:hidden[name]").each(function () {
            var $this = $(this), name = $this.attr("name"), parentRow = $this.parents("tr");
            data[name] && data[name].push($this.val());
        });
        data = $.extend({
            customerField: customerField,
            groupField: groupField,
            customer: customers,
            customerGroup: groups
        }, configs.data);
        var beforeSubmit = configs.beforeSubmit || function (form, settings, popup) {
            var data = {};
            data[customerField] = [];
            data[groupField] = [];
            form.find("input:hidden[name]").each(function () {
                var $this = $(this), name = $this.attr("name"), parentRow = $this.parents("tr");
                data[name].push({
                    name: parentRow.find("td:eq(0)").text().trim(),
                    value: $this.val()
                })
            });
            previewer(data, form, previewPanel);
            inputForm.trigger("change");
            popup.close();
            return false;
        }
        return bm.editPopup(url, title, emp_title, data, {
            width: configs.width ? configs.width : 850,
            height: configs.height ? configs.height : 730,
            events: {
                content_loaded: function () {
                    var _self = this;
                    var customerLeftPanelUrl = "customerAdmin/loadCustomerForMultiSelect";
                    var groupLeftPanelUrl = "customerGroup/loadCustomerGroupForMultiSelect";
                    var cncSelector = bm.twoSideSelection(_self, 10, "customer", customerLeftPanelUrl, {
                        view: false,
                        edit: false,
                        "column-sort": false
                    }, [customerField]);
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
                            }, [customerField])
                        } else {
                            cncSelector.setUrl(groupLeftPanelUrl, "customer-group", {
                                view: false,
                                edit: false,
                                "column-sort": true
                            }, [groupField])
                        }
                    });
                    searchForm.form({
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
            beforeSubmit: beforeSubmit,
            success: successHandler
        })
    },
    customTooltip: function (url, count, pos, atBottom, atRight, callback) {
        config = {
            auto_close_on_success: true,
            disable_on_submit: true,
            disable_on_invalid: true,
            disable_button_text: undefined,
            modify_ui: true,
            on_load_loader: false
        };
        if (!pos[count - 1].length) {
            count = count + 1;
            atBottom = "7";
            atRight = "-7"
        }
        var events = (config.events || {});
        $(pos[count - 1]).addClass("active");
        events.content_loaded = function (popup) {
            var _self = this;
            _self.updateUi();
            if (count > 2) {
                atBottom = "7";
                atRight = "-7";
                var btn = $("<button type='button' class='submit-button-pre'></button>");
                btn.text($.i18n.prop("prev"));
                _self.find(".pop-bottom").append(btn)
            }
            if (count == 8) {
                _self.find(".submit-button-next").remove();
                var btn = $("<button type='button' class='submit-button-done'></button>");
                btn.text($.i18n.prop("done"));
                _self.find(".pop-bottom").prepend(btn);
                var div = $("<div class='show-next-wrapper'>" + "<input type='checkbox' name='remember' class='single'>" + "<span class='show-next'> Show on next login</span>" + "</div>");
                _self.find(".pop-bottom").append(div)
            }
            var check = true;
            _self.find(".single").on("change", function () {
                check = this.checked ? false : true
            });
            _self.find(".submit-button-done, .submit-button-skip").click(function () {
                bm.ajax({
                    url: app.baseUrl + "user/saveIsMatured",
                    data: {check: check},
                    success: function (resp) {
                        if (callback && callback.getStart) {
                            callback.getStart();
                        }
                    }
                });
                popup.close(1);
                $(pos[count - 1]).removeClass("active")
            });
            _self.find(".submit-button-next").click(function () {
                count++;
                popup.close(1);
                $(pos[count - 2]).removeClass("active");
                atBottom = "7";
                atRight = "-7";
                atRight = (count == 3) ? "+15" : "-7";
                if (count < pos.length + 1) {
                    bm.customTooltip(app.baseUrl + "dashboard/loadOnScreenHelp", count, pos, atBottom, atRight, callback);
                }
            });
            _self.find(".submit-button-pre").click(function () {
                popup.close(1);
                if (count > 1) {
                    $(pos[count - 1]).removeClass("active");
                    count--;
                    switch (count) {
                        case 1:
                            atBottom = "100";
                            atRight = "-100";
                            break;
                        case 3:
                            atBottom = "7";
                            atRight = "+15";
                            break;
                        default :
                            atBottom = "7";
                            atRight = "-7"
                    }
                }
                bm.customTooltip(app.baseUrl + "dashboard/loadOnScreenHelp", count, pos, atBottom, atRight, callback)
            });
            _self.find(".tool-tip-close").click(function () {
                $(pos[count - 1]).removeClass("active");
                popup.close(1);
                if (callback && callback.getStart) {
                    callback.getStart(1);
                }
            });
            _self.addClass("loaded")
        };
        delete config.events;
        return new POPUP($.extend({
            template: '<div></div>',
            show_title: false,
            width: 380,
            height: 150,
            ajax_url: url,
            ajax_settings: {
                data: {count: count, wizard: pos[0].length ? true : null}
            },
            ui_position: {
                my: "right top+10",
                at: "right" + atRight + " bottom+" + atBottom,
                of: pos[count - 1]
            },
            draggable: false
        }, config, {clazz: "tool-tip-popup", events: events}))
    },
    disableTextSelection: function (_document) {
        if (!_document) {
            _document = document
        }
        if (browser.ff) {
            bm.clearTextSelection(_document);
            _document.body.style["MozUserSelect"] = "none"
        } else {
            _document.onselectstart = function () {
                return false;
            }
        }
    },
    editPopup: function (url, title, emphasized, data, config) {
        config = $.extend({}, {
            auto_close_on_success: true,
            disable_on_submit: true,
            disable_on_invalid: true,
            disable_button_text: undefined,
            modify_ui: true,
            width: 500,
            form_selector: ".edit-popup-form",
            buttonLine: "button-line",
            scroll: true
        }, config);
        var events = (config.events || {});
        var _caller_content_loaded = events ? events.content_loaded : undefined;
        events.content_loaded = function (popup) {
            var form = this.find(config.form_selector);
            var newSubmit;
            if (typeof(config.buttonLine) == "string") {
                var buttonLine = form.find("." + config.buttonLine);
                newSubmit = buttonLine.clone();
                buttonLine.remove();
            } else {
                newSubmit = config.buttonLine
            }
            var button = newSubmit.find(".submit-button, button[type=submit]").addClass("edit-popup-form-submit").on("click", function () {
                form.submit();
            });
            this.append(newSubmit);
            if (config.scroll) {
                this.find("> .content").scrollbar({
                    reduce_margin_from_offset: false,
                    vertical: {
                        offset: 0
                    }
                });
            }
            this.updateUi();
            var _success = (config.ajax && config.ajax.success) || config.success;
            form.form({
                ajax: this.find(".edit-popup-form").attr("no-ajax") == null,
                disable_on_submit: config.disable_on_submit,
                disable_on_invalid: config.disable_on_invalid,
                disable_button_text: config.disable_button_text,
                submitButton: button,
                preSubmit: function (ajaxSettings) {
                    $.extend(ajaxSettings, {
                        response: config.response,
                        error: config.error
                    }, config.ajax, {
                        success: function () {
                            if (config.auto_close_on_success) {
                                popup.close();
                            }
                            if (_success) {
                                _success.apply(this, arguments);
                            }
                        }
                    });
                    if (config.beforeSubmit) {
                        return config.beforeSubmit.call(popup.getDom(), form, ajaxSettings ? (ajaxSettings.data = ajaxSettings.data || {}) : null, popup);
                    }
                }
            });
            form.find("[default-focus]").focus();
            var tabs = form.find(".bmui-tab:first");
            if (tabs.length) {
                form.on("invalid", function (ev, field) {
                    if (tabs.tabs && tabs.tabs instanceof Function) {
                        tabs.tabs("activate", field.validator.elm);
                    }
                })
            }
            this.find(".cancel-button").click(function () {
                popup.close(1);
            });
            popup.on("content-change", function (ev, added, removed) {
                if (added) {
                    form.obj(ValidationPanel).attach(added.filter("[validation]").add(added.find("[validation]")));
                }
                if (removed) {
                    form.obj(ValidationPanel).detach(removed.filter("[validation]").add(removed.find("[validation]")));
                }
            });
            form.on("trash-restore", function () {
                popup.close();
            });
            if (_caller_content_loaded) {
                _caller_content_loaded.apply(this, [popup, form]);
            }
        };
        delete config.events;
        return new POPUP($.extend({
            title: title + (emphasized ? " - <span class='emphasized'>" + bm.htmlEncode(emphasized) + "</span>" : ""),
            width: 430,
            ajax_url: url,
            ajax_settings: {
                data: data
            },
            draggable: false
        }, config, {clazz: "edit-popup" + (config.clazz ? " " + config.clazz : ""), events: events}))
    },
    enableTextSelection: function (_document) {
        if (!_document) {
            _document = document
        }
        if (browser.ff) {
            _document.body.style["MozUserSelect"] = ""
        } else {
            _document.onselectstart = null;
        }
    },
    errorHighlight: function (item, time) {
        item.addClass("error-highlight");
        setTimeout(function () {
            item.removeClass("error-highlight");
        }, time ? time : 1000);
    },
    floatingPanel: function (ref, url, data, conf) {
        conf = conf ? conf : {};
        conf.clazz = "floating-panel-popup" + (conf.clazz ? " " + conf.clazz : "")
        var popup = new POPUP($.extend({
            ajax_url: url,
            ajax_settings: {
                data: data
            },
            show_title: false,
            masking: false,
            show_close: false,
            close_on_blur: true,
            modal: false,
            is_center: false,
            width: 350,
            ui_position: {
                my: "right top",
                at: "right+10 bottom+7",
                of: ref,
                collision: conf.position_collison
            }
        }, conf));
        return popup
    },
    floatingPopup: function (_content, _config) {
        _content.find(".popup-body").hide()
        _content.find(".action-navigator").click(function () {
            var config = $.extend({}, _config)
            var navigator = this.jqObject;
            var popup = navigator.data("popup")
            if(popup) {
                popup.show();
                return;
            }
            var body = navigator.next(".popup-body");
            config = $.extend({
                content: body,
                position_collison: "none",
                events: {
                    content_loaded: function (popup) {
                        body.show();
                    }
                }
            }, config);

            popup = bm.floatingPanel(navigator, null, {}, config);
            popup.close = popup.hide;
            navigator.data("popup", popup)
        })
        return _content
    },
    getTopScroller: function (_document) {
        return $(/webkit/i.test(navigator.userAgent) || _document.compatMode == 'BackCompat' ? _document.body : _document.documentElement)
    },
    highlight: function (item, time, blink) {
        item.addClass("highlight-row" + (blink ? " blink" : ""));
        setTimeout(function () {
            item.removeClass("highlight-row" + (blink ? " blink" : ""));
        }, time ? time : 3000);
    },
    initArticleSelection: function (container, fieldName, actions) {
        if (typeof fieldName == "undefined") {
            fieldName = "article"
        }
        var _actions = $.extend({
            tab: null,
            view: false,
            edit: false,
            "column-sort": true
        }, actions);
        var articleSelector = this.twoSideSelection(container, 10, "article", app.baseUrl + "content/loadArticlesForSelection", _actions, [fieldName]);
        var sectionSelector = container.find("select.section-selector").change(function () {
            articleSelector.reload();
        });
        var articleSorting = container.find("select.article-sorting").change(function () {
            articleSelector.reload();
        });
        container.find(".icon-search").click(function () {
            articleSelector.reload();
        });
        articleSelector.beforeLoadTableContent = function (params) {
            var _param = {
                searchText: container.find("input.search-text").val(),
                section: sectionSelector.val(),
                sortBy: articleSorting.val()
            };
            $.extend(params, _param)
        };
        container.find(".filter-block").form({
            disable_on_submit: false,
            preSubmit: function () {
                articleSelector.reload();
            }
        });
        articleSelector.onViewClick = function (type, value, name, row) {
            app.tabs.content.viewArticle(value["article"]);
        };
        articleSelector.onEditClick = function (type, value, name, row) {
            app.tabs.content.editArticle(value[fieldName], name, _actions.tab);
        };
        return articleSelector
    },
    initBrandSelection: function (container, fieldName, actions) {
        if (typeof fieldName == "undefined") {
            fieldName = "brand"
        }
        var _actions = $.extend({
            tab: null,
            view: false,
            edit: false,
            "column-sort": true
        }, actions);
        var brandSelector = this.twoSideSelection(container, 10, "brand", app.baseUrl + "brandAdmin/loadBrandForSelection", _actions, [fieldName]);
        var searchInput = container.find("input.search-text");
        var removeSearchBtn = $('<span class="remove-search" title="' + $.i18n.prop("remove.search") + '">').tooltipster();
        removeSearchBtn.on("click", function () {
            searchInput.val("");
            removeSearchBtn.hide();
            brandSelector.reload();
        });
        searchInput.before(removeSearchBtn);
        removeSearchBtn.hide();
        brandSelector.beforeLoadTableContent = function (params) {
            var _param = {
                searchText: searchInput.val()
            };
            $.extend(params, _param);
        };
        container.find(".filter-block").form({
            disable_on_submit: false,
            preSubmit: function () {
                brandSelector.reload();
                if (searchInput.val()) {
                    removeSearchBtn.show();
                } else {
                    removeSearchBtn.hide();
                }
            }
        });
        brandSelector.onEditClick = function (type, value, name, row) {
            app.tabs.brand.editBrand(value[fieldName], name, _actions.tab);
        };
        return brandSelector
    },
    initCategorySelection: function (container, fieldName, actions) {
        if (typeof fieldName == "undefined") {
            fieldName = "category"
        }
        var _actions = $.extend({
            tab: null,
            view: false,
            edit: false,
            "column-sort": true
        }, actions);
        var categorySelector = this.twoSideSelection(container, 10, "category", app.baseUrl + "categoryAdmin/loadCategoriesForSelection", _actions, [fieldName]);
        var categorySorting = container.find("select.category-sorting").change(function () {
            categorySelector.reload();
        });
        categorySelector.beforeLoadTableContent = function (params) {
            var _param = {
                searchText: container.find("input.search-text").val(),
                sortBy: categorySorting.val()
            };
            $.extend(params, _param);
        };
        container.find(".filter-block").form({
            disable_on_submit: false,
            preSubmit: function () {
                categorySelector.reload();
            }
        });
        categorySelector.onEditClick = function (type, value, name, row) {
            app.tabs.item.editCategory(value[fieldName], name, _actions.tab);
        };
        return categorySelector
    },
    initCityValidator: function (postcodes, countryFiledName, stateFiledName, form, cityFieldName) {
        countryFiledName = countryFiledName ? countryFiledName : "country.id";
        stateFiledName = stateFiledName ? stateFiledName : "state.id";
        cityFieldName = cityFieldName ? cityFieldName : "city";
        if (postcodes.length) {
            postcodes.each(function () {
                var postCode = $(this);
                form = form ? form : postCode.closest("form");
                var state = form.find('[name="' + stateFiledName + '"]');
                var country = form.find('[name="' + countryFiledName + '"]');
                var container = form.find('.city-selector-row');

                function checker(excludeCountry) {
                    var data = {state: state.val(), postCode: postCode.val(), cityFieldName: cityFieldName};
                    if (excludeCountry != true) {
                        data.country = country.val()
                    }
                    var validation = container.find(":input").attr("validation");
                    if (validation) {
                        data.validation = validation;
                    }
                    bm.ajax({
                        url: app.baseUrl + 'app/loadCitiesByCountryOrState',
                        data: data,
                        dataType: 'html',
                        success: function (resp) {
                            resp = $(resp);
                            if (app.is_front_end) {
                                container.find(":input").remove();
                            } else {
                                container.children().not("label").remove();
                            }
                            container.append(resp);
                            if (!app.is_front_end && resp.attr("et-category") == "dropdown") {
                                resp.chosen();
                            }
                            var validator = resp.closest("form").obj(ValidationPanel);
                            if (validator) {
                                validator.attach(resp.filter("[validation]"), validator);
                            }
                            var stateId = resp.find("option:selected").attr("state");
                            if (stateId) {
                                state.val(stateId);
                                if (!app.is_front_end) state.trigger("chosen:updated")
                            }
                        }
                    });
                }

                if (container.length) {
                    state.change(function () {
                        checker(true)
                    });
                    form.on("state-load", function () {
                        state = form.find('[name="' + stateFiledName + '"]');
                        checker();
                        if (state.length) {
                            state.change(checker)
                        }
                    });
                    postCode.ichange(checker);
                }
            })
        }
    },
    initCountryChangeHandler: function (select, stateName) {
        select.change(function () {
            var _self = $(this);
            var form = _self.closest("form");
            var id = $(this).val();
            $.ajax({
                url: app.baseUrl + "app/loadStateForCountry",
                dataType: 'html',
                data: {id: id, stateName: stateName},
                success: function (data) {
                    form.find(".form-row.state-selector-row").remove();
                    select.parents(".form-row").after(data);
                    form.trigger("state-load")
                }
            })
        });
    },
    initFilterSelection: function (container) {
        var filterSelector = this.twoSideSelection(container, 10, "filter", app.baseUrl + "filterAdmin/loadFiltersForSelection", {
            edit: false,
            "column-sort": true
        }, ["filter"]);
        var removeSearchBtn = $('<span class="tool-icon remove-search" style="display: none"></span>');
        var searchText = container.find("input.search-text");
        var searchForm = container.find(".search-form");
        searchForm.prepend(removeSearchBtn);
        filterSelector.beforeLoadTableContent = function (params) {
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
        searchForm.form({
            disable_on_submit: false,
            submitButton: searchForm.find(".search-button"),
            preSubmit: function () {
                filterSelector.reload();
            }
        });
        removeSearchBtn.on("click", function () {
            searchText.val("");
            filterSelector.reload();
        });
        return filterSelector

    },
    initProductSelection: function (container, fieldName, actions) {
        if (typeof fieldName == "undefined") {
            fieldName = "product"
        }
        var _actions = $.extend({
            tab: null,
            view: false,
            edit: false,
            "column-sort": true
        }, actions);
        var productSelector = this.twoSideSelection(container, 10, fieldName, app.baseUrl + "productAdmin/loadProductsForSelection", _actions, [fieldName]);
        var categorySelector = container.find("select.category-selector").change(function () {
            productSelector.reload();
        });
        var productSorting = container.find("select.product-sorting").change(function () {
            productSelector.reload();
        });
        var specialProducts = container.find("select.special-product-filtering").change(function () {
            productSelector.reload();
        });
        productSelector.beforeLoadTableContent = function (params) {
            var _param = {
                searchText: container.find("input.search-text").val(),
                parent: categorySelector.val(),
                sortBy: productSorting.val(),
                specialProductsFilter: specialProducts.val(),
                priceFrom: container.find("input[name='form-amount']").val(),
                priceTo: container.find("input[name='to-amount']").val()
            };
            $.extend(params, _param);
        };
        container.find(".filter-block").form({
            disable_on_submit: false,
            preSubmit: function () {
                productSelector.reload();
            }
        });
        productSelector.onEditClick = function (type, value, name, row) {
            app.tabs.item.editProduct(value["product"], name, _actions.tab);
        };
        productSelector.onViewClick = function (type, value, name, row) {
            app.tabs.item.viewProduct(value[fieldName]);
        };
        return productSelector
    },
    initZoneSelection: function (container, fieldName, actions, fieldNames) {
        fieldName = fieldName ? fieldName : "zone";
        fieldNames = fieldNames ? fieldNames : [fieldName]
        var _actions = $.extend({
            tab: null,
            view: false,
            edit: false,
            "column-sort": true
        }, actions);
        app.global_event.on("zone-create zone-update zone-delete", function () {
            zoneSelector.reload();
        });
        var zoneSelector = this.twoSideSelection(container, 10, fieldName, app.baseUrl + "zone/loadZoneForSelection", _actions, fieldNames);
        zoneSelector.beforeLoadTableContent = function (params) {
            var _param = {
                searchText: container.find("input.search-text").val(),
            };
            $.extend(params, _param);
        };
        container.find(".filter-block").form({
            disable_on_submit: false,
            preSubmit: function () {
                zoneSelector.reload();
            }
        });
        return zoneSelector
    },
    innerHeight: function (tag, height, height_definition) {
        var sizing = tag.css("box-sizing");
        var to_reduce = 0;
        switch (height_definition) {
            case "inner":
                to_reduce = tag.innerHeight() - tag.height();
                break;
            case "outer":
            default:
                to_reduce = sizing == "border-box" ? 0 : tag.outerHeight() - tag.height();
                break;
            case "extra_outer":
                to_reduce = tag.outerHeight(true) - (sizing == "border-box" ? tag.outerHeight() : tag.height());
                break;
        }
        return height - to_reduce;
    },
    /**
     * @param tag
     * @param width
     * @param width_definition [inner | outer | extra_outer] default is outer (padding, border, margin)
     * @returns {Number} the width to set to get the same width defined by width_definition
     */
    innerWidth: function (tag, width, width_definition) {
        var sizing = tag.css("box-sizing");
        var to_reduce = 0;
        switch (width_definition) {
            case "inner":
                to_reduce = tag.innerWidth() - tag.width();
                break;
            case "outer":
            default:
                to_reduce = sizing == "border-box" ? 0 : tag.outerWidth() - tag.width();
                break;
            case "extra_outer":
                to_reduce = tag.outerWidth(true) - (sizing == "border-box" ? tag.outerWidth() : tag.width());
                break;
        }
        return width - to_reduce;
    },
    instantSearch: function (searchContext, searchForm, nodeSelector, textSelector, callback) {
        var searchBox = searchForm.find(".search-text");

        function filter() {
            var searchText = searchBox.val();
            if (searchText) {
                searchContext.addClass("search-active")
            } else {
                searchContext.removeClass("search-active")
            }
            searchContext.find(nodeSelector).each(function () {
                var $this = $(this), name = $this.find(textSelector).text().trim();
                if (name.match(new RegExp(searchText, "i"))) {
                    $this.show()
                } else {
                    $this.hide()
                }
            });
        }

        searchBox.ichange(800, function () {
            filter()
        });
        searchForm.form({
            preSubmit: function () {
                filter();
                return false
            }
        })
    },
    makeTableCellEditable: function (tds, callback) {
        function editingTd() {
            var icon = $(this);
            var td = icon.parent();
            var tr = td.parent();
            td.addClass("editing");
            var tdVal = td.find(".value");
            var oldVal = tdVal.chide().text().trim();
            var validation = td.attr("validation");
            var maxlength = td.attr("maxlength");
            td.append("<input type='text' class='td-full-width' maxlength='" + (maxlength ? maxlength : "") + "' validation='" + (validation ? validation : "") + "'>");
            var restrict = td.attr("restrict");
            var editField = td.find("input[type='text']");
            editField.val(oldVal);
            if (restrict) {
                editField[restrict]();
            }
            var input = editField.get(0);
            input.selectionStart = input.selectionEnd = input.value.length;
            input.focus();
            td.trigger("cell-edit");
            tr.siblings("tr.data-row").addBack().each(function () {
                $(this).trigger("heightChange");
            });
            function updateTd($this) {
                var editFieldVal = $this.val();
                tdVal.text(editFieldVal);
                if (editFieldVal != oldVal) {
                    if (callback && callback(td, editFieldVal, oldVal) === false) {
                        input.focus();
                        return;
                    }
                }
                editField.remove();
                td.removeClass("editing");
                tdVal.cshow();
                tr.siblings("tr.data-row").addBack().each(function () {
                    $(this).trigger("heightChange");
                })
            }

            editField.on("focusout", function () {
                updateTd($(this));
            }).on("keypress", function (e) {
                if (e.keyCode == 13) {
                    e.preventDefault();
                    updateTd($(this));
                }
            });
        }

        tds.each(function () {
            var td = $(this);
            if (!td.find(".value").length) {
                var html = td.html();
                td.html('<span class="value"></span>');
                td.find(".value").html(html)
            }
            var clazz = "edit";
            if (td.is(".custom-edit")) {
                clazz = "change-all";
                td.append('<span class="fake-link ' + clazz + '">' + $.i18n.prop("change.all") + '</span>');
            } else {
                td.append('<span class="tool-icon ' + clazz + '"></span>');
            }
            var editBtn = td.find("." + clazz);
            editBtn.on("click", editingTd);
        });
    },
    makeTableCellSelectable: function (tds, select, callback, before) {
        function editingTd() {
            var icon = $(this);
            var td = icon.parent();
            td.addClass("editing");
            var tdVal = td.find(".value");
            var tdText = td.find(".text");
            var oldVal = tdVal.chide().text().trim();
            var _select = $.isFunction(select) ? select(td) : select.clone().show();
            _select.addClass('td-full-width');
            if (td.is(".hidden-overflow-selectable")) {
                var div = $("<div class='selectable-td-proxy'></div>").appendTo(document.body);
                div.append(_select);
                div.position({
                    my: "left top",
                    at: "left top",
                    of: td,
                    collision: "none"
                });
                div.width(td.outerWidth(), true);
                div.height(td.outerHeight(), true)
            } else {
                td.append(_select);
            }
            _select.val(oldVal);
            _select.chosen();
            _select.on("chosen:hiding_dropdown", function () {
                var editFieldVal = $(this).val();
                _select.chosen("remove");
                if (oldVal != editFieldVal) {
                    tdVal.text(editFieldVal);
                    var selectedText = $(this).find("option:selected").text();
                    tdText.text(selectedText);
                    if (callback) {
                        callback(td, editFieldVal, selectedText, oldVal);
                    }
                }
                td.removeClass("editing");
                if (td.is(".hidden-overflow-selectable")) {
                    $(".selectable-td-proxy").remove()
                }
                tdVal.cshow();
            });
            _select.chosen("dropshow");
            return false;
        }

        tds.each(function () {
            var td = $(this);
            if (!td.find(".value").length) {
                var html = td.html();
                td.html('<span class="value"></span><span class="text"></span>');
                td.find(".value").html(html);
                td.find(".text").html(html)
            }
            var clazz = "edit";
            if (td.is(".custom-select")) {
                clazz = "change-all";
                td.prepend('<span class="fake-link ' + clazz + '">' + $.i18n.prop("change.all") + '</span>');
            } else {
                td.append('<span class="tool-icon ' + clazz + '"></span>');
            }
            var editBtn = td.find("." + clazz);
            editBtn.on("click", function () {
                if (before && before(td) === false) {
                    return;
                }
                editingTd.call(this)
            });
        });
    },
    mask: function (tag, maskHtml) {
        maskHtml = $(maskHtml);
        var position = tag.position();
        var positionType = tag.css("position");
        var css = {
            position: "absolute"
        };
        if (positionType == 'static') {
            $.extend(css, {
                left: position.left + tag.leftRib(true, false, false),
                top: position.top + tag.topRib(true, false, false),
                width: maskHtml.width(tag.outerWidth()),
                height: maskHtml.height(tag.outerHeight())
            });
        } else {
            $.extend(css, {
                left: 0,
                top: 0,
                right: 0,
                bottom: 0
            });
        }
        maskHtml.css(css).addClass("div-mask");
        tag.append(maskHtml);
    },
    maskIframe: function (iframe) {
        var mask = $("<div class='iframe-mask'></div>").css($.extend({
            bottom: 0,
            left: 0,
            position: "absolute",
            right: 0,
            top: 0,
            "z-index": 500
        }, browser.ie && browser.version == 8 ? {"background-image": "url(../../images/common/transparent_00.png)"} : {"background-color": "transparent"}));
        iframe.contents().find("body").append(mask);
    },
    menu: function (menu, panel, delegate, handler, evName, position) {
        if (!menu) {
            return;
        }
        var retrieveType;
        var activeMenu;
        if (handler.multitype) {
            var _menu = {};
            $.each(menu, function (ind) {
                _menu[ind] = bm.createMenu(this)
            });
            menu = _menu;
            retrieveType = function (entity) {
                return entity.attr("content-type")
            }
        } else if ($.isArray(menu)) {
            retrieveType = function () {
                return "def"
            };
            menu = {def: bm.createMenu(menu)};
        } else {
            retrieveType = function () {
                return "def"
            };
            menu = {def: menu};
        }
        var eventId = bm.getUUID();
        if ($.isFunction(handler)) {
            handler = {click: handler};
        }
        function showMenu(_menu, entity, evt) {
            if (!_menu) {
                return;
            }
            $(document.body).append(_menu);
            activeMenu = _menu;
            var position_ref = position ? (panel.length > 1 || delegate ? entity.closest(delegate || panel) : panel) : evt;
            _menu.position({
                at: position ? position[0] : "left top+1",
                my: position ? position[1] : "left top",
                of: position_ref,
                using: function (pos, info) {
                    var items = $(this);
                    items.css({
                        left: pos.left + 'px',
                        top: pos.top + 'px'
                    });
                    if (info.vertical == "top") {
                        items.removeClass("up-side").addClass("down-side");
                    } else {
                        items.addClass("up-side").removeClass("down-side");
                    }
                    if (handler.open) {
                        handler.open.call(_menu, entity, info)
                    }
                    items.find(".menu-item").each(function () {
                        var item = $(this), permission = item.attr("permission");
                        if (permission) {
                            app.isPermitted(permission) ? item.removeClass("disabled") : item.addClass("disabled")
                        }
                    })
                }
            }).find(".menu-item").on("click", function () {
                var item = this.jqObject;
                if (item.is(".disabled")) {
                    return;
                }
                if (handler.multitype) {
                    handler.click(retrieveType(entity), item.attr("action"), entity, this.data);
                } else {
                    handler.click(item.attr("action"), entity, this.data);
                }
                if (!item.hasClass("ignore-hide-on-select")) {
                    hideMenuWithEvent(entity);
                }
            });
            $(document).on("mousedown." + eventId, function (ev) {
                if (!_menu.isParentOf(ev.target)) {
                    hideMenuWithEvent(entity);
                }
            });
            _menu.children().each(function () {
                if (this.sub) {
                    var item = $(this);
                    var subMenu = this.sub;
                    item.hover(function () {
                        if (activeMenu.active_sub_menu) {
                            activeMenu.active_sub_menu.hide()
                        }
                        activeMenu.active_sub_menu = subMenu.show().position({
                            my: "left top",
                            at: "right+10 top",
                            of: item
                        })
                    }, function () {
                        setTimeout(function () {
                            if (!activeMenu.hover_on_submenu) {
                                subMenu.hide()
                            }
                        }, 100)
                    });
                    subMenu.hover(function () {
                        activeMenu.hover_on_submenu = true
                    }, function () {
                        activeMenu.hover_on_submenu = false
                    })
                }
            })
        }

        function hideMenu() {
            if (activeMenu) {
                $(document).off("mousedown." + eventId);
                activeMenu.remove().find(".menu-item").off("click");
                if (activeMenu.active_sub_menu) {
                    activeMenu.active_sub_menu.hide();
                    delete activeMenu.active_sub_menu;
                    delete activeMenu.hover_on_submenu
                }
            }
        }

        function hideMenuWithEvent(entity) {
            if (handler.hide && handler.hide(entity) === false) {
                return;
            }
            hideMenu()
        }

        if (!handler.multitype && position && evName == "always") {
            showMenu(menu['def'], panel, null)
        } else {
            panel.on(evName || "contextmenu", delegate, function (evt) {
                var entity = $(this);
                showMenu(menu[retrieveType(entity)], entity, evt);
                return false;
            });
            $.each(menu, function () {
                $(this).children().each(function () {
                    if (this.sub) {
                        var item = $(this);
                        var subMenu = this.sub;
                        item.append(subMenu.hide())
                    }
                })
            })
        }
        var extendable = {
            show: function (entity, reference) {
                if (arguments.length > 1) {
                    hideMenu();
                    showMenu(this, entity, reference);
                    return;
                }
                if (activeMenu) {
                    var item = activeMenu.find("[action='" + entity + "']");
                    if (!item.length) {
                        item = activeMenu.find("." + entity)
                    }
                    item.show();
                }
            },
            hide: function (action) {
                if (!action) {
                    hideMenu();
                    return;
                }
                if (activeMenu) {
                    var item = activeMenu.find("[action='" + action + "']");
                    if (!item.length) {
                        item = activeMenu.find("." + action)
                    }
                    item.hide();
                }
            },
            enable: function (action) {
                if (!action) {
                    return;
                }
                if (activeMenu) {
                    var item = activeMenu.find("[action='" + action + "']");
                    if (!item.length) {
                        item = activeMenu.find("." + action)
                    }
                    item.removeClass("disabled");
                }
            },
            disable: function (action) {
                if (!action) {
                    return;
                }
                if (activeMenu) {
                    var item = activeMenu.find("[action='" + action + "']");
                    if (!item.length) {
                        item = activeMenu.find("." + action)
                    }
                    item.addClass("disabled");
                }
            }
        };
        var returnable = $.each(menu, function () {
            $.extend(this, extendable)
        });
        if (returnable.def) {
            return returnable.def
        }
        return returnable
    },
    metaTagEditor: function (panel) {
        var rowTemplate = '<tr><td class="name editable" maxlength="">#NAME#</td><td class="value editable" maxlength="">#VALUE#</td><td class="actions-column"><input type="hidden" name="tag_name" value="#NAME#">' +
            '<input type="hidden" name="tag_content" value="#VALUE#"><span class="tool-icon remove"></span></td></tr>';
        var metaTagSection = panel.find(".meta-tag-editor"), tagTable = metaTagSection.find("table"), lastRow = tagTable.find("tr.last-row");

        function attachRowEvent(content) {
            bm.makeTableCellEditable(content.find("td.editable"), function (td, newVal, oldVal) {
                var parent = td.parent("tr");
                var flag = false;
                if (!newVal) {
                    errorHighlight(td.find("input"));
                    bm.notify($.i18n.prop("value.must.not.be.empty"), "error");
                    return false
                }
                if (td.is(".name")) {
                    parent.find("[name=tag_name]").val(newVal)
                } else {
                    parent.find("[name=tag_content]").val(newVal)
                }
            });
            content.find("td span.remove").on("click", function () {
                $(this).parents("tr").remove();
            })
        }

        function addTag(name, value, lastRow) {
            var row = $(rowTemplate.replaceAll("#NAME#", name.htmlEncode()).replaceAll("#VALUE#", value.htmlEncode()));
            if (lastRow) {
                if (lastRow.find(".name").attr("maxlength")) {
                    row.find(".name").attr("maxlength", lastRow.find(".name").attr("maxlength"))
                }
                if (lastRow.find(".value").attr("maxlength")) {
                    row.find(".value").attr("maxlength", lastRow.find(".value").attr("maxlength"))
                }
            }
            attachRowEvent(row);
            lastRow.before(row)
        }

        tagTable.find("tr:gt(0):not(.last-row)").each(function () {
            attachRowEvent($(this));
        });
        function errorHighlight(item) {
            item.addClass("error-highlight");
            setTimeout(function () {
                item.removeClass("error-highlight")
            }, 1000);
        }

        var addMapping = function () {
            var name = lastRow.find(".name").val(), value = lastRow.find(".value").val();
            if (!name) {
                errorHighlight(lastRow.find(".name"));
                return;
            }
            if (!value) {
                errorHighlight(lastRow.find(".value"));
                return;
            }
            addTag(name, value, lastRow);
            lastRow.find("input").val("");
            return false
        };
        lastRow.find("input").bind("keyup.key_return", addMapping);
        lastRow.find(".add-row").on("click", addMapping);
        lastRow.find("input").on("keydown, keyup, keypress", function (e) {
            if (e.keyCode == 13) {
                return false
            }
        })
    },
    notify: function (message, type) {
        var dom = $(".popup.notification");
        if (dom.length) {
            dom.find(".message").html(message.trim());
            dom.find(".type").text($.i18n.prop(type) + "!");
            dom.removeClass("success error info warning alert").addClass(type);
            dom.obj().resetCloseTimer();
            return;
        }
        dom = $("<div class='notification'><span class='icon'></span><span class='type'></span></span><span class='message'></span><span class='close'></span></div>");
        dom.find(".message").html(message.trim());
        dom.find(".type").text($.i18n.prop(type) + "!");
        return dom.popup({
            auto_close: 3000,
            auto_active: false,
            auto_active_on_focus: false,
            modal: false,
            is_fixed: true,
            is_center: true,
            top: 20,
            is_always_up: true,
            clazz: type,
            position: function () {
                var dom = this.getDom();
                dom.css({top: -dom.outerHeight(), left: bm.center(dom.outerWidth(), 0, true).left});
                dom.animate({top: 20}, {easing: 'easeOutBounce'});
            },
            close: function () {
                var dom = this.getDom();
                var _self = this;
                var _arguments = arguments;
                dom.fadeOut(function () {
                    POPUP.prototype.close.apply(_self, _arguments);
                })
            },
            events: {
                render: function () {
                    var content = this;
                    content.hover(function () {
                            content.addClass("hover");
                        },
                        function () {
                            content.removeClass("hover");
                        })
                }
            }
        });
    },
    pageableTable: function (table, configs) {
        var total = table.find("tr:gt(0)").length, paginationTag = $("paginator", {
            max: configs.max || 3,
            offset: 0,
            total: total
        });
        table.after(paginationTag);
        paginationTag.paginator({
            onPageClick: function (page) {
                var paginator = paginationTag.obj();
                table.find("tr:not(:first)").hide();
                var perpage = paginator.all ? paginator.getTotal() : paginator.getItemsPerPage();
                var offsetIndex = (page - 1) * perpage;
                if (offsetIndex < 1) {
                    offsetIndex = 0;
                }
                table.find("tr:gt(" + offsetIndex + "):lt(" + perpage + ")").show();
            }
        })
    },
    pageableView: function (wrapper, configs) {
        var explorer = {};
        explorer.menu = bm.menu(configs.menu_entries, wrapper, ".float-menu-navigator", {
            open: function (entity) {
                if (entity.parent().is(".grid-item")) {
                    entity = entity.parent();
                    entity.addClass("float-menu-opened");
                    var data = entity.config("content");
                    if (explorer.onMenuOpen) {
                        explorer.onMenuOpen(type, data.type, entity);
                    }
                }
            },
            hide: function (entity) {
                entity = menu_opened_from_tree ? entity.parent(".tree-node") : entity.parent(".grid-item");
                entity.removeClass("float-menu-opened");
            },
            click: function (type, action, entity) {
                var data;
                if (menu_opened_from_tree) {
                    data = treeOptions.treeObj.data;
                    entity = undefined
                } else {
                    data = entity.parent(".grid-item").config("content");
                }
                explorer.onActionClick(type, action, data, entity)
            },
            multitype: true
        }, "click", ["center bottom", "right+21 top+2"]);
    },
    permissionPopup: function (title, emphasized, data) {
        if (app.licenses && !app.licenses[CONSTANTS.LICENSES.ACL]) {
            bm.notify($.i18n.prop("feature.disabled", [$.i18n.prop(CONSTANTS.LICENSES.ACL)]), "alert");
            return;
        }

        function bindAllowDenyAll(panel) {
            var allowAll = panel.find("[name=allowAll]");
            var denyAll = panel.find("[name=denyAll]");
            var allowGroup = panel.find(".allow-group");
            var denyGroup = panel.find(".deny-group");

            if (allowGroup) {
                allowGroup.on("change", function () {
                    if (allowGroup.filter(":checked").length == allowGroup.length) {
                        allowAll.checkbox("state", "checked");
                    } else {
                        allowAll.checkbox("state", "unchecked");
                    }

                    var _self = $(this);
                    var deny = _self.closest("tr").find(".deny-group");
                    if (_self.is(":checked") && deny.is(":checked")) {
                        deny.checkbox("state", "unchecked");
                        deny.trigger("change");
                    }
                });
            }

            if (denyGroup) {
                denyGroup.on("change", function () {
                    if (denyGroup.filter(":checked").length == denyGroup.length) {
                        denyAll.checkbox("state", "checked");
                    } else {
                        denyAll.checkbox("state", "unchecked");
                    }

                    var _self = $(this);
                    var allow = _self.closest("tr").find(".allow-group");
                    if (_self.is(":checked") && allow.is(":checked")) {
                        allow.checkbox("state", "unchecked");
                        allow.trigger("change");
                    }
                });
            }

            if (allowAll) {
                allowAll.on("change", function () {
                    allowGroup.prop("checked", this.checked);
                    allowGroup.trigger("change")
                })
            }
            if (denyAll) {
                denyAll.on("change", function () {
                    if ($(this).is(":checked")) {
                        denyGroup.checkbox("state", "checked");
                        denyGroup.trigger("change");
                    } else {
                        denyGroup.checkbox("state", "unchecked");
                        denyGroup.trigger("change");
                    }

                })
            }
        }

        bm.editPopup(app.baseUrl + "role/managePermissions", title, emphasized, data, {
            width: 450,
            events: {
                content_loaded: function () {
                    var panel = this;
                    var selectName = "type";
                    bindAllowDenyAll(panel);
                    panel.find("select[name='" + selectName + "']").on("change", function (e, old) {
                        var type = this.value;
                        var form = panel.find(".edit-popup-form");
                        var typeBlock = $("<div class='block-for-" + old + "'><input type='hidden' name='" + selectName + "' value='" + old + "'></div>").hide();
                        typeBlock.append(panel.find("form > .permission-entry-list"));
                        form.append(typeBlock);
                        typeBlock = panel.find(".block-for-" + type);
                        if (typeBlock.length) {
                            form.append(typeBlock.find(".permission-entry-list"));
                            typeBlock.remove()
                        } else {
                            panel.find(".content").loader();
                            var _data = $.extend({}, data);
                            _data[selectName] = type;
                            var entityType = panel.find("input[name=entityType]").val();
                            if (entityType) {
                                _data.type = entityType;
                                _data.user = type
                            }
                            bm.ajax({
                                url: app.baseUrl + "role/managePermissions",
                                data: _data,
                                dataType: "html",
                                response: function (x, block) {
                                    panel.find(".content").loader(false);
                                    block = block[0] == "<" ? $(block) : $("<div class='permission-entry-list'>" + block + "</div>");
                                    var list = block.find(".permission-entry-list");
                                    if (!list.length) {
                                        list = $("<div class='permission-entry-list'></div>");
                                        list.append(block)
                                    }
                                    form.append(list);
                                    list.find("input:checkbox").checkbox().filter("[uncheck-value]").pairuncheck();
                                    bindAllowDenyAll(list);
                                }
                            })
                        }
                    })
                }
            },
            success: function () {
                app.updatePermissions();
            }
        });
    },
    posRefChangeLtoR: function(tag) {
        var parent = tag.offsetParent();
        var right = parent.innerWidth() - tag.position().left - tag.outerWidth()
        tag.css({
            left: "auto",
            right: right
        })
    },
    /**
     * @overload - 1
     * @param inputForm
     * @param config -> {url, data, title, onSuccess}
     */
    productAndCategorySelectionPopup:  function (inputForm, configs) {
        var productField, categoryField;
        var url = configs.url || app.baseUrl + "itemAdmin/loadProductAndCategorySelector";
        var title = configs.title || $.i18n.prop("select.products.or.categories");
        var emp_title = configs.emphasized;

        var success_handler = configs.success;
        productField = configs.productField || "product";
        categoryField = configs.categoryField || "category";
        var previewPanel = configs.preview_panel || inputForm;
        var previewer = configs.previewer || function (data, form) {
            previewPanel.find("input[name='" + productField + "'], input[name='" + categoryField + "']").remove();
            form.find("input:hidden[name]").each(function () {
                var name = $(this).attr("name");
                var inp = $("<input type='hidden' name='" + name + "' value='" + $(this).val() + "'>");
                previewPanel.append(inp);
            });
        };
        var data = {}, products = data[productField] = [], categories = data[categoryField] = [];
        previewPanel.find("input:hidden[name]").each(function () {
            var $this = $(this), name = $this.attr("name");
            data[name] && data[name].push($this.val());
        });
        data = $.extend({
            productField: productField,
            categoryField: categoryField,
            selectedProducts: products,
            selectedCategories: categories
        }, configs.data);

        return bm.editPopup(url, title, emp_title, data, {
            width: 850,
            events: {
                content_loaded: function () {
                    var _self = this;
                    var productLeftPanelUrl = "productAdmin/loadProductsForSelection";
                    var categoryLeftPanelUrl = "categoryAdmin/loadCategoriesForSelection";
                    var cncSelector = bm.twoSideSelection(_self, 10, "product", productLeftPanelUrl, {
                        view: false,
                        edit: false,
                        "column-sort": false
                    }, [productField]);
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
                        if (this.value == 'product') {
                            cncSelector.setUrl(productLeftPanelUrl, "product", {
                                view: false,
                                edit: false,
                                "column-sort": true
                            }, [productField])
                        } else {
                            cncSelector.setUrl(categoryLeftPanelUrl, "category", {
                                view: false,
                                edit: false,
                                "column-sort": true
                            }, [categoryField])
                        }
                    });
                    searchForm.form({
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
            beforeSubmit: function (form, settings, popup) {
                var data = {};
                data[productField] = [];
                data[categoryField] = [];
                form.find("input:hidden[name]").each(function () {
                    var $this = $(this), name = $this.attr("name"), parentRow = $this.parents("tr");
                    data[name].push({
                        name: parentRow.find("td:eq(0)").text().trim(),
                        value: $this.val()
                    })
                });
                previewer(data, form, previewPanel);
                inputForm.trigger("change");
                popup.close();
                return false;
            },
            success: success_handler
        });
    },
    productSelectionPopup: function (inputForm, configs) {
        var url = configs.url || app.baseUrl + "productAdmin/productSelectionPopup";
        var title = configs.title || $.i18n.prop("select.products");
        var emp_title = configs.emphasized;
        var data = configs.data || {};
        var success_handler;
        var fieldName = configs.fieldName || "product";
        var products = [];
        var previewHolder = configs.preview_holder || inputForm;
        previewHolder.find("input[name='" + fieldName + "']").each(function () {
            products.push($(this).val())
        });
        $.extend(data, {
            fieldName: fieldName,
            product: products
        });
        var previewer = configs.previewer || function (data, selectionPanel) {
            previewHolder.find("input[name='" + fieldName + "']").remove();
            selectionPanel.find("input:hidden[name]").each(function () {
                var name = $(this).attr("name");
                var inp = $("<input type='hidden' name='" + name + "' value='" + $(this).val() + "'>");
                previewHolder.append(inp)
            });
        };
        return bm.editPopup(url, title, emp_title, data, {
            width: 850,
            form_selector: ".product-selection-panel",
            events: {
                content_loaded: function (popup, form) {
                    var container = this;
                    var selector = bm.initProductSelection(container, fieldName);
                    $(this).data("selector", selector);
                }
            },
            beforeSubmit: function (form, settings, popup) {
                var data = {};
                data[fieldName] = [];
                form.find("input:hidden[name]").each(function () {
                    var $this = $(this), name = $this.attr("name"), parentRow = $this.parents("tr");
                    data[name].push({
                        name: parentRow.find("td:eq(0)").text().trim(),
                        value: $this.val()
                    })
                });
                previewer(data, form, previewHolder);
                inputForm.trigger("change");
                popup.close();
                return false;
            }
        })
    },
    recipientSelectorPopup: function (inputForm, config, selectionDone) {
        var selectedInputClazz = 'selected-recipient';
        var title = config.title ? config.title : $.i18n.prop("select.recipient");
        var url = app.baseUrl + "customerAdmin/loadRecipientSelector";
        var emp_title = "";
        var data = config.data;
        if (config.url) {
            url = config.url;
        }
        inputForm.find('.' + selectedInputClazz).each(function () {
            var field = data[$(this).attr('name')];
            if (!field) {
                field = data[$(this).attr('name')] = [];
            }
            field.push($(this).val())
        });
        bm.editPopup(url, title, emp_title, data, {
            width: 850,
            events: {
                content_loaded: function () {
                    var _self = this;
                    var customerLeftPanelUrl = "customerAdmin/loadCustomerForMultiSelect";
                    var groupLeftPanelUrl = "customerGroup/loadCustomerGroupForMultiSelect";
                    var cncSelector = bm.twoSideSelection(_self, 10, "customer", customerLeftPanelUrl, {
                        view: false,
                        edit: false,
                        "column-sort": true
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
                    cncSelector.getPanelBody = function () {
                        return '<div class="body">' +
                            '<div class="form-row"><label>' + $.i18n.prop('name') + '</label><input type="text" class="medium name"></div>' +
                            '<div class="form-row"><label>' + $.i18n.prop('email') + '</label>' +
                            '<input type="text" class="medium email" validation="required email"></div>' +
                            '</div><div class="inline-button-line"><button type="button" class="submit-button add-email">' + $.i18n.prop('add') + '</button></div>';
                    };
                    cncSelector.initContent = function (panel, selectFunc) {
                        var nameField = panel.find(".name");
                        var emailField = panel.find(".email");
                        panel.attachValidator();
                        function splitEmails(func) {
                            var name = nameField.is(":disabled") ? "" : nameField.val().trim();
                            var email = emailField.val();
                            if (name) {
                                var _email = email.trim();
                                email = name + " <" + email + ">";
                                func([email], [_email, name]);
                            } else {
                                $.each(email.split(/[,;]/), function () {
                                    var _email = this.trim();
                                    if (_email == "") {
                                        return;
                                    }
                                    var name = "";
                                    var email = _email;
                                    var gInd = _email.indexOf("<");
                                    if (gInd > -1) {
                                        name = _email.substring(0, gInd).trim();
                                        _email = _email.substring(gInd + 1);
                                        gInd = _email.indexOf(">");
                                        _email = _email.substring(0, gInd).trim();
                                    }
                                    func([email], [_email, name]);
                                });
                            }
                        }

                        panel.find(".add-email").click(function () {
                            if (panel.valid() === false) {
                                return;
                            }
                            splitEmails(selectFunc);
                            nameField.val("").removeAttr("disabled");
                            emailField.val("");
                        });
                        emailField.blur(function () {
                            var email = this.value;
                            if (email.indexOf(",") != -1 || email.indexOf(";") != -1 || email.indexOf("<") != -1) {
                                nameField.attr("disabled", "disabled").val($.i18n.prop("single.email.without.name"));
                            } else {
                                if (nameField.is(":disabled")) {
                                    nameField.val("").removeAttr("disabled");
                                }
                            }
                        });
                        panel.obj(ValidationPanel).addValidatorRule(emailField, function () {
                            var match = false;
                            splitEmails(function (value, nameEmail) {
                                if (cncSelector.isSelected("email", nameEmail[0])) {
                                    match = true;
                                }
                            });
                            return !match;
                        }, $.i18n.prop("email.address.exist"))
                    };
                    _self.find("select[name='selection-type']").change(function () {
                        if (this.value == 'customer') {
                            cncSelector.setUrl(customerLeftPanelUrl, "customer", {
                                view: false,
                                edit: false,
                                "column-sort": true
                            }, ["customer"])
                        } else if (this.value == 'group') {
                            cncSelector.setUrl(groupLeftPanelUrl, "customer-group", {
                                view: false,
                                edit: false,
                                "column-sort": true
                            }, ["customerGroup"])
                        } else {
                            cncSelector.setUrl(null, "email", {
                                view: false,
                                edit: false,
                                "column-sort": true
                            }, ["recipientEmail", "recipientName"])
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
            beforeSubmit: function (form, settings, popup) {
                if (inputForm instanceof $) {
                    inputForm.find("." + selectedInputClazz).remove();
                    form.find('input:hidden[name]').each(function () {
                        inputForm.append('<input type="hidden" class="' + selectedInputClazz + '" name="' + $(this).attr('name') + '" value="' + $(this).val() + '">')
                    });
                    if (typeof selectionDone == "function") {
                        selectionDone(form)
                    }
                    popup.close();
                    return false;
                }
            }
        });
    },
    remove: function (type_code, type, confirm, url, id, config) {
        var success = function () {
            if (!config.is_final) {
                app.global_event.trigger("send-trash", [type_code, id])
            }
            app.global_event.trigger(type_code + "-deleted", [id]);
            if (config.success) {
                config.success.apply(this, arguments)
            }
        };
        var data = {id: id};
        if (config.at2_reply) {
            data.at2_reply = config.at2_reply
        }
        if (config.at1_reply) {
            data.at1_reply = config.at1_reply
        }
        if (config.is_final) {
            data.is_final = true
        }
        var proceed = function () {
            if (config.start) {
                config.start();
            }
            bm.ajax({
                url: url,
                data: data,
                response: config.stop,
                success: success,
                error: function (xhr, status, resp) {
                    if (resp.error_code == "attachment.exists") {
                        var attachShowData = {id: id, type: type_code};
                        if (config.is_final) {
                            attachShowData.is_final = true;
                        }
                        function generateEntityString(entityList) {
                            var types = '';
                            $.each(entityList, function (entity) {
                                var count = this;
                                var is_list = true;
                                if ($.isPlainObject(this)) {
                                    count = this.count;
                                    is_list = this.list || true
                                }
                                types += is_list ? (", <a class='fake-link' href='javascript:window.tempFunction(\"" + entity + "\")'>" + count + " " + $.i18n.prop(entity) + "</a>") : (", " + count + " " + $.i18n.prop(entity))
                            });
                            return types.substring(2)
                        }

                        function generateAlertMessage(atn, msg_key) {
                            var has_types;
                            var as_types;
                            var types;
                            var args = [type];
                            if (atn.has) {
                                has_types = generateEntityString(atn.has);
                                delete atn.has;
                                msg_key += ".has";
                                args.push(has_types)
                            }
                            if (atn.as) {
                                as_types = atn.as.collect(function () {
                                    return $.i18n.prop("" + this)
                                }).join(", ");
                                delete atn.as;
                                msg_key += ".as";
                                args.push(as_types)
                            }
                            types = generateEntityString(atn);
                            if (types) {
                                msg_key += ".attach";
                                args.push(types)
                            }
                            msg_key += ".message";
                            return $.i18n.prop(msg_key, args)
                        }

                        var att = resp.attachments;
                        if (att.at3) {
                            var message = generateAlertMessage(att.at3, "at3.prevent");
                            var popup = bm.alert(message, "error", function () {
                            });
                            window.tempFunction = function (entity) {
                                popup.close();
                                bm.viewPopup(app.baseUrl + "app/viewAttachments", $.extend({}, attachShowData, {
                                    entity: entity,
                                    att_type: 3
                                }), {width: 500});
                            }
                        } else if (att.at2) {
                            var message = generateAlertMessage(att.at2, "at2.information");
                            var popup = bm.confirm(message, function () {
                                bm.remove(type_code, type, confirm, url, id, $.extend({}, config, {
                                    at2_reply: "yes",
                                    no_confirm: true
                                }))
                            }, function () {
                            });
                            window.tempFunction = function (entity) {
                                popup.hide();
                                var attachView = bm.viewPopup(app.baseUrl + "app/viewAttachments", $.extend({}, attachShowData, {
                                    entity: entity,
                                    att_type: 2
                                }), {width: 500});
                                attachView.on("close", function () {
                                    popup.show()
                                })
                            }
                        } else if (att.at1) {
                            var message = generateAlertMessage(att.at1, "at1.information");
                            var popup = bm.confirm(message, function () {
                                bm.remove(type_code, type, confirm, url, id, $.extend({}, config, {
                                    at1_reply: "exclude",
                                    no_confirm: true
                                }))
                            }, function () {
                                bm.remove(type_code, type, confirm, url, id, $.extend({}, config, {
                                    at1_reply: "include",
                                    no_confirm: true
                                }))
                            }, function () {
                            });
                            window.tempFunction = function (entity) {
                                popup.hide();
                                var attachView = bm.viewPopup(app.baseUrl + "app/viewAttachments", $.extend({}, attachShowData, {
                                    entity: entity,
                                    att_type: 1
                                }), {width: 500});
                                attachView.on("close", function () {
                                    popup.show()
                                })
                            }
                        }
                    }
                }
            })
        };
        if (config.no_confirm) {
            proceed()
        } else {
            bm.confirm($("<p>").text(confirm), proceed, function () {
            });
        }
    },
    renderSitePopup: function (url, title, emphasized, data, config) {
        config = $.extend({}, {
            auto_close_on_success: true,
            disable_on_submit: true,
            disable_on_invalid: true,
            disable_button_text: undefined,
            modify_ui: true,
            width: 500,
            form_selector: ".site-popup-form",
            buttonLine: "button-line",
            scroll: true
        }, config);
        var events = (config.events || {});
        var _caller_content_loaded = events ? events.content_loaded : undefined;
        events.content_loaded = function (popup) {
            var form = this.find(config.form_selector);
            var newSubmit;
            if (typeof(config.buttonLine) == "string") {
                var buttonLine = form.find("." + config.buttonLine);
                newSubmit = buttonLine.clone();
                buttonLine.remove();
            } else {
                newSubmit = config.buttonLine
            }
            var button = newSubmit.find(".submit-button, button[type=submit]").addClass("site-popup-form-submit").on("click", function () {
                form.submit();
            });
            this.append(newSubmit);
            var _success = (config.ajax && config.ajax.success) || config.success;
            form.form({
                ajax: this.find(".site-popup-form").attr("no-ajax") == null,
                disable_on_submit: config.disable_on_submit,
                disable_on_invalid: config.disable_on_invalid,
                disable_button_text: config.disable_button_text,
                submitButton: button,
                preSubmit: function (ajaxSettings) {
                    $.extend(ajaxSettings, {
                        response: config.response,
                        error: config.error
                    }, config.ajax, {
                        success: function () {
                            if (config.auto_close_on_success) {
                                popup.close();
                            }
                            if (_success) {
                                _success.apply(this, arguments);
                            }
                        }
                    });
                    if (config.beforeSubmit) {
                        return config.beforeSubmit.call(popup.getDom(), form, ajaxSettings ? (ajaxSettings.data = ajaxSettings.data || {}) : null, popup);
                    }
                }
            });
            form.find("[default-focus]").focus();
            this.find(".cancel-button").click(function () {
                popup.close(1);
            });
            popup.on("content-change", function (ev, added, removed) {
                if (added) {
                    form.obj(ValidationPanel).attach(added.filter("[validation]").add(added.find("[validation]")));
                }
                if (removed) {
                    form.obj(ValidationPanel).detach(removed.filter("[validation]").add(removed.find("[validation]")));
                }
            });
            if (_caller_content_loaded) {
                _caller_content_loaded.apply(this, [popup, form]);
            }
        };
        delete config.events;
        return new POPUP($.extend({
            title: title + (emphasized ? " - <span class='emphasized'>" + bm.htmlEncode(emphasized) + "</span>" : ""),
            width: 430,
            ajax_url: url,
            ajax_settings: {
                data: data
            }
        }, config, {clazz: "site-popup" + (config.clazz ? " " + config.clazz : ""), events: events}))
    },
    renderZoneView: function (_self) {
        var zoneUrl = app.baseUrl + "zone/loadZoneView";
        var zoneTable;

        function reload() {
            var panel = _self.body.find(".right-panel");
            panel.loader();
            bm.ajax({
                url: zoneUrl,
                dataType: "html",
                response: function () {
                    panel.loader(false);
                }, success: function (resp) {
                    zoneTable = $(resp);
                    panel.find(".body").html(zoneTable);
                    bm.table(zoneTable, {
                        url: zoneUrl,
                        onload: function () {
                        },
                        menu_entries: [
                            {
                                text: $.i18n.prop("edit"),
                                ui_class: "edit",
                                action: "edit"
                            },
                            {
                                text: $.i18n.prop("remove"),
                                ui_class: "remove",
                                action: "remove"
                            }
                        ],
                        onActionClick: function (action, data, navigator) {
                            switch (action) {
                                case "edit":
                                    editZone(navigator.parents("tr"), data);
                                    break;
                                case "remove":
                                    app.tabs.zone.deleteZone(data.id, data.name, undefined, reload);
                                    break;
                            }
                        }
                    });
                    zoneTable.find(".add-zone-btn").on("click", function () {
                        var base = $(zoneTable.find("tr")[0]);
                        editZone(base);
                    });
                    _self.body.find(".toolbar-btn.save").hide();
                    panel.scrollbar();
                }
            });
        }

        function editZone(base, data) {
            var panel = _self.body.find(".right-panel");
            panel.loader();
            bm.ajax({
                url: app.baseUrl + "zone/createZone",
                data: data,
                dataType: "html",
                response: function () {
                    panel.loader(false);
                },
                success: function (resp) {
                    resp = $("<td colspan='5'>" + resp + "</td>");
                    var form = resp.find("form");
                    bm.countryChangeSelection(form.find("select[name=zone\\.country\\.id]"), true, {
                        isMultiple: "true",
                        stateName: 'zone.state.id'
                    });
                    form.find(".cancel-button").on("click", function () {
                        reload();
                    });
                    base.empty().append(resp);
                    form.form({
                        ajax: true,
                        preSubmit: function (ajaxSetting) {
                            $.extend(ajaxSetting, {
                                success: function (resp) {
                                    reload();
                                }
                            })
                        }
                    });
                    form.updateUi();
                }
            })
        }

        reload();
    },
    selectFromAssetLibrary: function (title, selectButtonHandler, fileExtensions, config, isFile) {
        var fileUrl;
        var isSelected;
        config = $.extend({}, config, {
            _self: this,
            width: 500,
            events: {
                content_loaded: function (popup) {
                    var dom = this;
                    var targeteBody = this.find(".remote_folders").scrollbar({
                        vertical: {
                            offset: 2
                        }
                    });
                    var backPath;

                    function loadAssetLibrary(repository) {
                        if (typeof repository == "undefined") {
                            repository = "pub"
                        }
                        WebDAVClient.doPropFind(repository, this, {
                            onPopulate: function (childList) {
                                targeteBody.find(".folder, .file").remove();
                                if (repository != "pub") {
                                    var $tr = $('<div class="folder"> <span class="tool-icon back" title="' + $.i18n.prop("back") + '"></span></div>');
                                    targeteBody.append($tr);
                                    $tr.click(function () {
                                        isSelected = false;
                                        backPath = backPath.substring(0, backPath.lastIndexOf("/"));
                                        loadAssetLibrary(backPath)
                                    });
                                }
                                $.each(childList.folders, function (key, value) {
                                    var $tr = $('<div class="folder"> <span class="tree-icon"></span> <span class="tree-title">' + value.name + '</span></div>');
                                    targeteBody.append($tr);
                                    var clicks = 0;
                                    $tr.click(function () {
                                        var timer = null;
                                        clicks++;
                                        if (clicks === 1) {
                                            timer = setTimeout(function () {
                                                clicks = 0;
                                                $tr.siblings(".selected").removeClass("selected");
                                                isSelected = false;
                                                if (isFile == false) {
                                                    isSelected = true;
                                                    $tr.addClass("selected").siblings(".selected").removeClass("selected");
                                                    fileUrl = bm.encodePath(app.baseUrl + "pub" + value.path.substring(value.path.indexOf("/")))
                                                }
                                            }, 200);
                                        } else {
                                            clicks = 0;
                                            clearTimeout(timer);
                                            backPath = value.path;
                                            loadAssetLibrary(value.path);
                                            targeteBody.next().find(".selectedValue").removeAttr("src").data("loaded", false);
                                        }
                                    });
                                });
                                $.each(childList.files, function (key, value) {
                                    var imageExt = ["bmp", "gif", "jpg", "png", "psd", "pspimage", "thm", "tif", "yuv", "jpeg"];
                                    var extension = value.clazz;
                                    fileExtensions = fileExtensions ? fileExtensions : imageExt;
                                    if (fileExtensions.contains(extension) && isFile != false) {
                                        var $tr = $('<div class="file ' + extension + '"> <span class="tree-icon"></span> <span class="tree-title">' + value.name + '</span></div>');
                                        targeteBody.append($tr);
                                        $tr.click(function () {
                                            isSelected = false;
                                            fileUrl = bm.encodePath(app.systemPubUrl + "/" + value.path.substring(value.path.indexOf("/")));
                                            if (isFile === undefined) {
                                                var img = $("<img>", {
                                                    src: fileUrl
                                                });
                                                dom.find(".asset-library-image-preview").html(img);
                                                img.on("load", function () {
                                                    isSelected = true;
                                                    $tr.addClass("selected").siblings(".selected").removeClass("selected")
                                                })
                                            } else if (isFile) {
                                                targeteBody.find(".file.selected").removeClass("selected");
                                                $tr.addClass("selected");
                                                isSelected = true;
                                            }
                                        })
                                    }
                                })
                            }
                        })
                    }

                    loadAssetLibrary("pub");
                    this.find(".file-selector").form({
                        preSubmit: function () {
                            if (isSelected) {
                                selectButtonHandler(fileUrl);
                                popup.close();
                            } else {
                                if (isFile) {
                                    bm.notify($.i18n.prop("file.not.selected"), "error")
                                } else if (isFile === undefined) {
                                    bm.notify($.i18n.prop("image.not.selected"), "error")
                                } else {
                                    bm.notify($.i18n.prop("folder.not.selected"), "error")
                                }
                            }
                            return false
                        }
                    })
                }
            }
        });
        bm.editPopup(app.baseUrl + "remoteRepository/selectFileFromAssetLibraryForWidget", title, "", {isFile: isFile === undefined ? isFile : true}, config)
    },
    selectionFloatingPanel: function (ref, url, data, config) {
        var instance = {
            isLoaded: false,
        };
        instance.reload = function () {
            var _self = this, element = _self.el, data = {searchText: _self.searchText};
            element.loader();
            config.beforeReload && config.beforeReload(data);
            bm.ajax({
                url: url,
                data: data,
                dataType: "html",
                response: function() {
                    element.loader(false);
                },
                success: function(resp) {
                    element.find(".body").replaceWith($(resp).find(".body"));
                    _self.attachEvents();
                }
            });
        };
        instance.attachEvents = function () {
            var _self = this, element = _self.el;
            element.find(".body").scrollbar({
                vertical: {
                    offset: 15
                }
            });
            element.updateUi();
            element.find(".select-item").on("click", function() {
                var $this = $(this);
                if(!$this.is(".selected")) {
                    config.onSelect($this.config("entity"));
                }
                _self.popup.close()
            })
        };
        var events = (config.events || {});
        var _caller_content_loaded = events ? events.content_loaded : undefined;
        events.content_loaded = function (popup) {
            var el = instance.el = popup.el, searchForm = el.find(".search-form");
            instance.popup = popup;
            el.find(".cancel-button").on("click", function() {
                popup.close();
            });
            searchForm.form({
                preSubmit: function() {
                    var searchText = searchForm.find(".search-text").val();
                    if(instance.searchText === searchText) {
                        return false
                    }
                    instance.searchText = searchText;
                    instance.reload();
                    return false
                }
            });
            instance.attachEvents();
            _caller_content_loaded && _caller_content_loaded.apply(this, arguments)
        };
        delete config.events;
        bm.floatingPanel(ref, url, data, $.extend({
            width: 400,
            height: null,
            events: events,
            position_collison: "none"
        }, config));
        return instance
    },
    table: function (tableWrapper, settings) {
        var _config = {
            url: '',
            appendUrl: null,
            type: "get"
        };
        var tabulator;
        var paginator = tableWrapper.find(".pagination") ? tableWrapper.find(".pagination").obj() : null;
        var paginatedTable = tableWrapper.find(settings.isNonTable ? ".table:first" : "table:first");
        var menu;

        function _sendReloadRequest() {
            _config.param = paginator ? {
                max: paginator.all ? -1 : paginator.itemsPerPage,
                offset: (paginator.currentPage - 1) * paginator.itemsPerPage
            } : {max: -1, offset: 0};
            if (_config.sortable && _config.sorted) {
                $.extend(_config.param, {
                    sort: _config.sortable[_config.sorted],
                    dir: _config.sortedDir == "down" ? "desc" : "asc"
                });
            }
            if (_config.beforeReloadRequest) {
                _config.beforeReloadRequest();
            }
            tableWrapper.loader();
            bm.ajax({
                url: _config.url + (_config.appendUrl || ""),
                type: _config.type,
                data: _config.param,
                dataType: 'html',
                success: function () {
                    onReloadSuccess.apply(this, arguments);
                    if (_config.afterLoad) {
                        _config.afterLoad();
                    }
                },
                response: function () {
                    tableWrapper.loader(false);
                }
            });
        }

        function initializeSortableColumns() {
            if (_config.sortable) {
                $.each(_config.sortable, function (ind) {
                    var columnHead = paginatedTable.find("th:nth-child(" + (+ind + 1) + ")").eq(0);
                    columnHead.addClass("sortable");
                });
                if (_config.sorted) {
                    paginatedTable.find("th:nth-child(" + (+_config.sorted + 1) + ")").eq(0).addClass("sort-" + _config.sortedDir);
                }
            }
        }

        function onReloadSuccess(resp) {
            resp = $(resp);
            paginatedTable.html(resp.find(settings.isNonTable ? ".table" : "table").html()).updateUi();
            var title = resp.filter(".header").find(".title");
            if(title.length == 0) {
                title = resp.find(".header").find(".title");
            }
            tableWrapper.find(".header .title").replaceWith(title);
            tableWrapper.trigger("tabulator-load-success", [resp]);
            var paginatorTag = resp.find("paginator");
            var total = +paginatorTag.attr("total");
            var max = +paginatorTag.attr("max");
            var offset = +paginatorTag.attr("offset");
            if (paginator) {
                paginator.update(total, max, Math.floor(offset / max) + 1);
            }
            initializeSortableColumns();
            initializeEditableColumns();
            initializeSelectableColumns()
        }

        if (settings.menu_entries) {
            menu = bm.menu(settings.menu_entries, paginatedTable, ".action-navigator", {
                click: function (action, navigator) {
                    tabulator.onActionClick(action, navigator.config("entity"), navigator);
                },
                open: function (navigator, pos_hash) {
                    if (tabulator.onMenuOpen) {
                        tabulator.onMenuOpen(navigator, menu)
                    }
                }
            }, "click", ["right+10 bottom+5", "right top"]);
        }
        paginatedTable.on("click", ".sortable", function (ev) {
            var targetCell = $(ev.target);
            var index = targetCell.index();
            _config.sorted = "" + index;
            _config.sortedDir = targetCell.is(".sort-up") ? "down" : "up";
            tabulator.reload();
        });
        tabulator = $.extend({
            paginator: paginator,
            reload: function () {
                if (paginator) {
                    paginator.onPageClick(paginator.getCurrentPage());
                } else {
                    _sendReloadRequest();
                }
            },
            menu: menu,
            sortAdd: function (position, index) {
                _config.sortable[position] = index;
            },
            sortRemove: function (position) {
                delete _config.sortable[position];
            }
        }, settings);
        $.extend(_config, settings);
        tableWrapper.find('select.per-page-count').change(function (ev, oldValue, newValue) {
            paginator.update(undefined, +newValue, 1);
            _sendReloadRequest();
        });
        if (paginator) {
            paginator.onPageClick = _sendReloadRequest;
        }
        function initializeEditableColumns() {
            var editableCell = paginatedTable.find("td.editable");
            if (editableCell.length) {
                bm.makeTableCellEditable(editableCell, tabulator.afterCellEdit);
            }
        }

        function initializeSelectableColumns() {
            var selectable = paginatedTable.find("td.selectable");
            if (selectable.length && tabulator.selectableCellSelect) {
                bm.makeTableCellSelectable(selectable, tabulator.selectableCellSelect, tabulator.afterCellSelect, tabulator.beforeCellSelect);
            }
        }

        initializeSortableColumns();
        initializeEditableColumns();
        initializeSelectableColumns();
        return tabulator;
    },
    tableCheckAll: function (table, onFull, onPartial, onNo) {
        var orgTable = table.filter(".content");
        orgTable.delegate("td.select-column :checkbox", "change", function () {
            var checkBoxes = table.find("td.select-column :checkbox");
            if (checkBoxes.length == checkBoxes.filter(":checked").length) {
                orgTable.find(".check-all").prop("checked", true);
                if (onFull && checkBoxes.filter(":checked").length > 1) {
                    onFull()
                }
            } else if (checkBoxes.filter(":checked").length > 1) {
                orgTable.find(".check-all").checkbox("state", "partial");
                if (onPartial) {
                    onPartial()
                }
            } else {
                orgTable.find(".check-all").prop("checked", false);
                if (onNo) {
                    onNo()
                }
            }
            if (orgTable.obj(Scrollbar)) {
                orgTable.tscrollbar("content", orgTable.find(".check-all").closest("th"));
            }
        });
        table.delegate(".check-all", "change", function () {
            var allCheckBoxes = orgTable.find(".select-column :checkbox");
            allCheckBoxes.prop("checked", this.checked);
            if (this.checked && allCheckBoxes.filter(":checked").length >2) {
                if (onFull) {
                    onFull()
                }
            } else {
                if (onNo) {
                    onNo()
                }
            }
            orgTable.tscrollbar("content", orgTable.find(".check-all").closest("th"));
        });
    },
    tableToggleRow: function (container, callback) {
        container.find(".toggle-cell").click(function () {
            var _this = $(this);
            var detailsRow = _this.parents("tr").next();
            if (_this.is(".collapsed")) {
                _this.removeClass("collapsed").addClass("expanded");
                detailsRow.show()
            } else {
                _this.removeClass("expanded").addClass("collapsed");
                detailsRow.hide()
            }
            if (callback) {
                callback(_this);
            }
        })

    },
    taskPopup: function (url, data, popConfig) {
        if (!app.taskPopupCaches) {
            app.taskPopupCaches = {};
        }
        var hash = data.token + "#" + data.name;
        if (app.taskPopupCaches[hash]) {
            app.taskPopupCaches[hash].setActive();
            return;
        }
        var repeater = function () {
            bm.ajax({
                url: data.detail_status_url,
                data: {token: data.token},
                success: function (resp) {
                    data.detail_viewer.update(viewer, resp);
                    if (resp.status == "running") {
                        setTimeout(repeater, 1000);
                    }
                }
            })
        };
        var viewer = new POPUP($.extend({
            title: $.i18n.prop("task.detail") + " - <span class='emphasized'>" + bm.htmlEncode(data.name) + "</span>",
            ajax_url: url,
            ajax_settings: {
                data: data
            },
            clazz: "task-popup",
            draggable: false,
            modal: false,
            events: {
                content_loaded: function () {
                    data.detail_viewer.init(viewer);
                    repeater();
                }
            }
        }, popConfig));
        viewer.on("close", function () {
            delete app.taskPopupCaches[hash];
        });
        return app.taskPopupCaches[hash] = viewer;
    },
    twoSideSelection: function (container, itemPerPageSelect, itemName, leftTableUrl, actions, fieldNames, config = {}) {
        var initOtherContentInitialized = false;
        var def_actions = {remove: true, view: true, edit: true, config: false, "column-sort": false};
        var perPageCount;
        actions = $.extend({}, def_actions, actions);
        var defaultMenuEntries = {
            view: {text: $.i18n.prop("view"), ui_class: "view", action: "view"},
            edit: {text: $.i18n.prop("edit"), ui_class: "edit", action: "edit"},
            config: {text: $.i18n.prop("configure"), ui_class: "config", action: "config"},
            remove: {text: $.i18n.prop("remove"), ui_class: "remove", action: "remove"}
        };
        var _config = config
        var funcBinds = {
            reload: function () {
                if (!leftLoading) {
                    leftPaginator.onPageClick(leftPaginator.getCurrentPage());
                }
            },
            setUrl: function (url, name, _actions, fields) {
                itemName = name;
                leftTableUrl = url;
                fieldNames = fields;
                actions = $.extend({}, def_actions, _actions, {"column-sort": actions["column-sort"]});
                if (!leftLoading) {
                    loadLeftPanelTable(1);
                }
                actionMenu = bm.createMenu(generateMenuEntries());

            },
            clearAllSelection: function () {
                clearAllSelection(false);
            },
            getAllSelectedByField: function (field) {
                var retArray = [];
                if (field) {
                    rightTable.find("input[name='" + field + "']").each(function (id, elm) {
                        retArray.push($(elm).val());
                    });
                }
                return retArray;
            },
            hasSelected: function (field, value) {
                var retVal = false;
                if (typeof value == "undefined") {
                    rightTable.find("tr:not(.empty) td.actions-column").each(function (idx, elm) {
                        if ($(elm).find("input:first").val() == field) {
                            retVal = true;
                        }
                    })

                } else {
                    rightTable.find("input[name='" + field + "']").each(function (idx, elm) {
                        if ($(elm).val() == value) {
                            retVal = true;
                        }
                    });
                }
                return retVal;
            },
            onViewClick: function () {
            },
            onEditClick: function (type, value, name, row) {
            },
            onDeleteClick: function (type, value, name, row) {
            },
            onConfigClick: function () {
            },
            onNewSelection: function () {
            },
            getPanelBody: function () {
            },
            triggerExternalInit: function () {
                funcBinds.initContent(leftPanel, function (name, value, row) {
                    if (typeof row == "undefined") {
                        addSelectedRow([
                            {name: name, value: value}
                        ]);
                    } else {
                        addSelectedRow([
                            {name: name, value: value, row: row}
                        ]);
                    }
                });
            },
            unselect: function (type, id) {
                var affectedInputs = itemName == type ? leftPanel.find("td.actions-column[" + type + "='" + id + "'] input:checkbox") : [];
                if (!affectedInputs.length) {
                    affectedInputs = rightTable.find("tr:has(td.actions-column[item='" + id + "'][type='" + type + "'])");
                }
                affectedInputs.filter("input:checkbox").prop("checked", false);
                uncheckInputs(affectedInputs);
            },
            getSavedData: function (id, type, dataName) {
                if (typeof id == "string") {
                    id = rightTable.find("td.actions-column[item='" + id + "'][type='" + type + "']");
                }
                return id.data(dataName);
            },
            saveData: function (id, type, dataName, value) {
                if (typeof id == "string") {
                    id = rightTable.find("td.actions-column[item='" + id + "'][type='" + type + "']");
                }
                id.data(dataName, value);
            },
            getAllSelectedData: function () {
                var fromdom = rightTable.serializeObject();
                var selected = rightTable.find("td.actions-column[item]");
                selected.each(function () {
                    var _data = $(this).data();
                    if (_data) {
                        fromdom = bm.serializeMix(fromdom, _data)
                    }
                });
                return fromdom;
            },
            isSelected: function (name, value) {
                var selected = rightTable.find("input[name='" + name + "'][value='" + value + "']");
                return selected.length
            },
            resetRightPanel: function (resp, rightPanelSelectedTd, callback) {
                var respTds = resp.find("td.actions-column");
                for (var i= 0; i < rightPanelSelectedTd.length; i++) {
                    var td = $(rightPanelSelectedTd[i]);
                    var respTd = respTds.find("input[value=" + td.attr("item") + "]").parent();
                    if (respTd.length) {
                        td.prev("td").text(respTd.prev("td").text());
                    } else {
                        td.parent().remove();
                    }
                }
                if (callback) {
                    callback();
                }
            }
        };

        function onMenuActionClick(action, navigator) {
            var cell = navigator.closest("td");
            var values = getCellData(cell);
            var type = cell.attr("type");
            switch (action) {
                case "remove":
                    var row = cell.closest("tr");
                    funcBinds.onDeleteClick(type, values, cell.prev().html().trim(), row);
                    var inputsToUncheck = $();
                    if (leftTable && itemName == type) {
                        var value = cell.attr("item");
                        inputsToUncheck = inputsToUncheck.add(leftTable.find("td.actions-column[" + type + "='" + value + "'] input:checkbox"));
                        inputsToUncheck.prop("checked", false);
                        leftTable.trigger("change")
                    }
                    removeSelections(inputsToUncheck, row);
                    break;
                case "view":
                    funcBinds.onViewClick(type, values, cell.prev().html().trim(), cell);
                    break;
                case "edit":
                    funcBinds.onEditClick(type, values, cell.prev().html().trim(), cell.closest("tr"));
                    break;
                case "config":
                    funcBinds.onConfigClick(type, values, cell.prev().html().trim(), cell);
                    break;
            }
        }

        function generateMenuEntries() {
            var x = [];
            $.each(defaultMenuEntries, function (key, val) {
                if (actions[key]) {
                    x.push(val);
                }
            });
            return x;
        }

        function hideMenu(navigator) {
            navigator.removeClass("expanded up-side").addClass("collapsed");
            actionMenu.off("blur").removeClass("up-side").remove();
            var hiddenClasses = navigator.attr("entry-hidden");
            actionMenu.find(hiddenClasses).removeClass("hidden");
        }

        function fillRightGaps(fillUpCount) {
            var rows = $();
            for (var g = 0; g < fillUpCount; g++) {
                var row = diceRow.clone();
                rightTable.append(row);
                rows = rows.add(row);
            }
            return rows;
        }

        function fillRequiredGaps() {
            var rightRowCount = rightTable.find("tr td[item]").length;
            var column = leftTable.find("tr td").length / 2;
            var leftRowCount = column > 1 ? column : 1;
            var fillUpCount = rightRowCount ? ((leftRowCount - rightRowCount) < 0 ? 0 : (leftRowCount - rightRowCount)) : leftRowCount;
            if (rightRowCount != 0 && fillUpCount == perPageCount) {
                return;
            }
            var alreadyFilledCount = perPageCount < 0 ? 0 : rightTable.find("tr.empty").length;
            if (fillUpCount < alreadyFilledCount) {
                rightTable.find("tr.empty").filter(":lt(" + (alreadyFilledCount - fillUpCount) + ")").remove();
            } else {
                fillRightGaps(fillUpCount - alreadyFilledCount);
            }
            rightPaginator.onPageClick(rightPaginator.getCurrentPage())
        }

        function getCellData(cell) {
            var obj = {};
            if (cell.find("input[name]").length > 0) {
                cell.find("input[name]").each(function () {
                    obj[$(this).attr("name")] = $(this).val();
                });
            } else {
                fieldNames.every(function (i, fn) {
                    if (cell.attr(fn)) {
                        obj[fn] = cell.attr(fn)
                    }
                });
            }
            return obj;
        }

        function clearCurrentPageSelections() {
            clearAllSelection(true);
            if (typeof funcBinds.onRemoveAll == "function") {
                funcBinds.onRemoveAll.call(this)
            }
        }

        function clearAllSelection(viewedOnly) {
            var cellsToClear = rightTable.find("td[item].actions-column" + (viewedOnly ? ":visible" : ""));
            var inputsToUncheck = $();
            if (leftTable.length) {
                cellsToClear.each(function () {
                    var value = $(this).attr("item");
                    inputsToUncheck = inputsToUncheck.add(leftPanel.find("td.actions-column[" + itemName + "='" + value + "'] input:checkbox")).prop("checked", false);
                });
                leftTable.trigger("change")
            }
            removeSelections(inputsToUncheck, cellsToClear.closest("tr"))
        }

        function removeSelections(checks, selectedRows) {
            checks.closest("tr").removeClass("selected");
            if (checks.length) {
                updateHeaderCheckStatus();
            }
            if (selectedRows.length) {
                removeSelectedRows(selectedRows)
            }
        }

        function uncheckInputs(checksToUncheck) {
            var selectedRows = $();
            checksToUncheck.each(function () {
                var input = $(this);
                var value = input.parent().attr(itemName);
                selectedRows = selectedRows.add(rightTable.find("td.actions-column[item='" + value + "'][type='" + itemName + "']").closest("tr"));
            });
            removeSelections(checksToUncheck, selectedRows)
        }

        function removeSelectedRows(selectedRows) {
            selectedRows.remove();
            rightPaginator.setTotal(rightPaginator.getTotal() - selectedRows.length);
            rightPaginator.onPageClick(rightPaginator.getCurrentPage());
            fillRequiredGaps()
        }

        function rightPanelPageReload(page) {
            var perpage = rightPaginator.all ? (rightPaginator.getTotal() > 10 ? rightPaginator.getTotal() : 10) : rightPaginator.getItemsPerPage();
            var offsetIndex = (page - 1) * perpage;
            if (offsetIndex < 1) {
                offsetIndex = 0;
            }
            var rightTableTr = rightTable.find("tr:not(:first)")
            for (var i = 0; i < rightPaginator.getTotal(); i++) {
                if(((i >= offsetIndex) && (i <= (perpage * page)))) {
                    $(rightTableTr[i]).show()
                } else {
                    $(rightTableTr[i]).hide()
                }
            }
        }

        function addSelectedRow(entries) {
            rightPaginator.setTotal(rightPaginator.getTotal() + entries.length);
            var emptyRows = rightTable.find(".empty");
            if (emptyRows.length < entries.length) {
                fillRightGaps(entries.length);
                if (rightPaginator.getPageCount() == rightPaginator.getCurrentPage() + 1) {
                    rightPaginator.setCurrentPage(rightPaginator.getCurrentPage() + 1, true);
                }
            }
            $.each(entries, function () {
                var emptyRow;
                if (this.row) {
                    emptyRow = this.row;
                } else {
                    emptyRow = rightTable.find(".empty:first").removeClass("empty");
                }
                var emptyColums = emptyRow.find("td").not(":last");
                for (var i = 0; i < this.name.length; i++) {
                    $(emptyColums[i]).text(this.name[i]);
                }
                emptyRow.attr("type", itemName).find("td.actions-column").attr("item", this.value).attr("type", itemName);
                emptyRow.addClass(itemName);
                var _self = this;
                emptyRow.find("input[type='hidden']").each(function (i, elm) {
                    $(elm).attr("name", fieldNames[i]).val(_self.value[i]);
                });
                funcBinds.onNewSelection(emptyRow)
            });
            rightTable.trigger("row-update");
            rightPaginator.onPageClick(rightPaginator.getCurrentPage());
        }

        function checkInputs(inputs) {
            inputs.closest("tr").addClass("selected");
            var entries = [];
            var rightTableColumns = $(rightTable.find("tr")[0]).find("th").not(":last");
            inputs.each(function () {
                var input = $(this);
                var value = [];
                input.siblings("input").each(function () {
                    value.push($(this).val())
                });
                var row = input.closest("tr");
                var values = [];
                var leftTableColums = row.find("td").not(":last");
                if (rightTableColumns.length == leftTableColums.length) {
                    for (var i = 0; i < rightTableColumns.length; i++) {
                        values.push($(leftTableColums[i]).text().trim());
                    }
                } else if (rightTableColumns.length < leftTableColums.length) {
                    var i = 0;
                    var tempText = [];
                    while (i < rightTableColumns.length - 1) {
                        values.push($(leftTableColums[i]).text().trim());
                        i++;
                    }
                    while (i < leftTableColums.length) {
                        if ($(leftTableColums[i]).text()) {
                            tempText.push($(leftTableColums[i]).text().trim());
                        }
                        i++;
                    }
                    values.push(tempText.join('-'));
                } else {
                    var l = 0;
                    var r = 0;
                    while (r < rightTableColumns.length) {
                        values.push($(leftTableColums[l]).text().trim());
                        l++;
                        r++;
                        if (l == leftTableColums.length) {
                            l = 0;
                        }
                    }
                }
                entries.push({name: values, value: value});
            });
            addSelectedRow(entries);
            updateHeaderCheckStatus();
        }

        function updateHeaderCheckStatus() {
            var checkBoxes = leftTable.find("td.actions-column :checkbox");
            if (checkBoxes.filter(":checked").length == 0) {
                leftTable.find(".check-all").prop("checked", false)
            } else if (checkBoxes.length == checkBoxes.filter(":checked").length) {
                leftTable.find(".check-all").prop("checked", true)
            } else {
                leftTable.find(".check-all").checkbox("state", "partial")
            }
            if (leftTable.find(".check-all").closest("th").length) {
                leftTable.tscrollbar("content", leftTable.find(".check-all").closest("th"))
            }
        }

        function checkSelecteds() {
            var allCells = leftPanel.find("td.actions-column");
            allCells.each(function () {
                var value = $(this).attr(itemName);
                var rightSideRow = rightTable.find("td[type='" + itemName + "'][item='" + value + "']");
                if (rightSideRow.length) {
                    var tr = $(this).closest("tr").addClass("selected");
                    tr.find("input:checkbox").prop("checked", true);
                }
            });
            var cBoxCount = allCells.find("input:checkbox").length,
                selectedCBoxCount = allCells.find("input:checkbox:checked").length,
                checkAll = leftTable.find(".check-all:first").filter(":hidden");
            if (cBoxCount == selectedCBoxCount && selectedCBoxCount != 0) {
                checkAll.prop("checked", true);
            } else if (selectedCBoxCount > 0) {
                checkAll.checkbox("state", "partial");
            } else {
                checkAll.prop("checked", false);
            }
            updateHeaderCheckStatus();
        }

        function reloadRightTable(callback) {
            var rightPanelSelectedTd = rightPanel.find("td.actions-column[type='" + itemName + "']");
            var ids = [];
            rightPanelSelectedTd.each(function () {
                ids.push($(this).attr("item"));
            });
            if (ids.length) {
                rightPanel.loader();
                bm.ajax({
                    url: leftTableUrl,
                    data: {ids: ids, max: ids.length},
                    dataType: "html",
                    response: function () {
                        rightPanel.loader(false);
                    },
                    success: function (resp) {
                        if(_config.resetRightPanel == false) {
                            callback();
                        } else {
                            funcBinds.resetRightPanel($(resp), rightPanelSelectedTd, callback)
                        }
                    },
                    error: function () {
                        if (callback) {
                            callback();
                        }
                    }
                })
            }
        }

        function loadLeftPanelTable(page) {
            leftPanel.loader();
            leftLoading = true;
            var param = {
                max: perPageCount,
                offset: perPageCount * (page - 1),
                fieldName: itemName
            };
            if (funcBinds.beforeLoadTableContent) {
                funcBinds.beforeLoadTableContent(param);
            }
            if (leftTableUrl) {
                bm.ajax({
                    url: leftTableUrl,
                    data: param,
                    dataType: 'html',
                    response: function () {
                        leftPanel.loader(false)
                    },
                    success: function (resp) {
                        if (initOtherContentInitialized) {
                            initOtherContentInitialized = false;
                            leftPanel.html(resp);
                            leftPanel.updateUi();
                            leftPaginator = initializeLeftPagination()
                        } else {
                            var table = $(resp).find("table");
                            leftTable.tscrollbar("destroy");
                            leftPanel.find("table").replaceWith(table);
                            table.updateUi();
                        }
                        var paginatorTag = $(resp).find("paginator");
                        var total = +paginatorTag.attr("total");
                        var max = +paginatorTag.attr("max");
                        var offset = +paginatorTag.attr("offset");
                        leftPaginator.update(total, max, Math.floor(offset / max) + 1);
                        leftTable = leftPanel.find("table").tscrollbar({
                            vertical: {
                                height: 442
                            }
                        });
                        reloadRightTable(function () {
                            checkSelecteds();
                            fillRequiredGaps();
                        });
                    },
                    complete: function () {
                        leftPanel.removeClass("updating");
                        leftLoading = false;
                    }
                });
            } else {
                function initOtherContent(html) {
                    leftPanel.html(html);
                    funcBinds.initContent(leftPanel, function (name, value) {
                        addSelectedRow([
                            {name: name, value: value}
                        ]);
                    });
                    leftPanel.loader(false)
                    leftLoading = false;
                    leftTable = $("");
                }

                initOtherContentInitialized = true;
                var body = funcBinds.getPanelBody(itemName, param);
                if (typeof body == "string") {
                    initOtherContent(body);
                } else {
                    body.then = initOtherContent
                }
            }
        }

        function initializeLeftPanel() {
            checkSelecteds();
            leftPanel.on("click", ".tool-icon.view", function (ev) {
                if ($(ev.target).hasClass("disabled")) {
                    return;
                }
                var cell = $(ev.target).parent();
                var id = cell.attr(itemName);
                funcBinds.onViewClick(itemName, id, cell.prev().html().trim(), cell);
            }).on("click", ".tool-icon.edit", function (ev) {
                if ($(ev.target).hasClass("disabled")) {
                    return;
                }
                var cell = $(ev.target).parent();
                var id = cell.attr(itemName);
                funcBinds.onEditClick(itemName, id, cell.prev().html().trim(), cell.closest("tr"));
            });

            var leftPaginator = initializeLeftPagination();

            return leftPaginator
        }

        function initializeRightPanel() {
            var allCells = rightPanel.find("td.actions-column");
            allCells.each(function () {
                var td = $(this);
                var tr = td.closest("tr");
                tr.addClass(td.attr("type"));
            });
        }

        function initializeLeftPagination() {
            var leftPaginator = leftPanel.find(".pagination").obj();
            if (leftPaginator) {
                leftPaginator.onPageClick = loadLeftPanelTable
            }

            container.find(".per-page-count").change(function (ev, old, newValue) {
                newValue = +newValue;
                leftPaginator.update(undefined, newValue, 1);
                rightPaginator.update(undefined, newValue, 1);
                perPageCount = newValue;
                rightPaginator.onPageClick(rightPaginator.getCurrentPage());
                funcBinds.reload();
            });
            return leftPaginator
        }

        var actionMenu = bm.createMenu(generateMenuEntries());
        if (typeof itemPerPageSelect == "number") {
            perPageCount = itemPerPageSelect;
        } else {
            if (!itemPerPageSelect) {
                itemPerPageSelect = container.find(".pagination-perpage-selector");
            } else if (!itemPerPageSelect.is("select")) {
                itemPerPageSelect = itemPerPageSelect.find("select");
            }
            itemPerPageSelect.change(function () {
                perPageCount = +this.value;
                loadLeftPanelTable(1);
                rightPaginator.setItemsPerPage(perPageCount);
                fillRequiredGaps();
                rightPaginator.onPageClick(rightPaginator.getCurrentPage());
            });
            perPageCount = +itemPerPageSelect.val();
        }
        if (!itemPerPageSelect || !itemPerPageSelect.length) {
            perPageCount = 10;
        }
        var rightPanel = container.find(".last-column .column-content");
        var leftPanel = container.find(".first-column .column-content");
        var diceRow = rightPanel.find(".action-column-dice-content table tr").addClass("empty");
        var rightTable = rightPanel.find("table:first");
        var leftTable = leftPanel.find("table:first");
        if (actions["column-sort"]) {
            rightTable.find("th:eq(0)").sortablecolumn({});
            rightTable.bind("sort", function () {
                rightPaginator.setCurrentPage(rightPaginator.getCurrentPage(), true);
                rightTable.tscrollbar("content", rightTable.find("th:eq(0)"))
            });
        }
        var rightPaginator = rightPanel.find(".pagination").obj();
        rightPaginator.onPageClick = rightPanelPageReload;
        fillRequiredGaps();
        rightTable.on("click", ".remove-item", function () {
            var cell = $(this).closest("td");
            var values = getCellData(cell);
            var type = cell.attr("type");
            var row = cell.closest("tr");
            funcBinds.onDeleteClick(type, values, cell.prev().html().trim(), row);
            var inputsToUncheck = $();
            if (leftTable && itemName == type) {
                var value = cell.attr("item");
                inputsToUncheck = inputsToUncheck.add(leftTable.find("td.actions-column[" + type + "='" + value + "'] input:checkbox"));
                inputsToUncheck.prop("checked", false)
            }
            removeSelections(inputsToUncheck, row)
        });
        container.on("click", ".action-navigator", function () {
            var navigator = $(this);
            var hiddenClasses = navigator.attr("entry-hidden");
            actionMenu.find(hiddenClasses).addClass("hidden");
            if (navigator.is(".collapsed")) {
                if (!navigator.is("[tabindex]")) {
                    navigator.attr("tabindex", "0")
                }
                navigator.removeClass("collapsed").addClass("expanded");
                navigator.after(actionMenu);
                var height = navigator.outerHeight();
                var menuHeight = actionMenu.outerHeight();
                var requiredBottom = navigator.offset().top - $(window).scrollTop() + height + menuHeight;
                var actualBottom = $(window).height();
                if (actualBottom < requiredBottom) {
                    actionMenu.position({
                        my: "right bottom",
                        at: "right top+1",
                        of: navigator
                    }).addClass("up-side");
                    navigator.addClass("up-side")
                } else {
                    actionMenu.position({
                        my: "right top",
                        at: "right bottom-1",
                        of: navigator
                    })
                }
                navigator.focus().on("blur", function () {
                    hideMenu(navigator)
                });
            } else {
                hideMenu(navigator)
            }
        });
        container.on("mousedown", ".menu-item", function (ev) {
            var item = $(this);
            if (item.is(".disabled")) {
                return;
            }
            var navigator = item.closest(".floating-menu").siblings(".action-navigator");
            onMenuActionClick(item.attr("action"), navigator);
        });
        rightTable.find(".remove-all").on("click", clearCurrentPageSelections);
        rightTable.tscrollbar({
            vertical: {
                height: 443
            }
        });
        rightTable.on("click", "td.actions-column .move-up", function (ev) {
            var thisRow = $(ev.target).closest("tr");
            var prevRow = thisRow.prev();
            if (prevRow.length && prevRow.has("th").length == 0) {
                thisRow.swap(prevRow);
                if (prevRow.is(":hidden")) {
                    thisRow.hide();
                    prevRow.show();
                }
                rightTable.find("th:eq(0)").sortablecolumn("resetSortState");
                rightTable.tscrollbar("content", rightTable.find("th:eq(0)"))
            }
            rightTable.trigger("change")
        }).on("click", "td.actions-column .move-down", function (ev) {
            var thisRow = $(ev.target).closest("tr");
            var nextRow = thisRow.next();
            if (nextRow.length && !nextRow.is(".empty")) {
                thisRow.swap(nextRow);
                if (nextRow.is(":hidden")) {
                    thisRow.hide();
                    nextRow.show();
                }
                rightTable.find("th:eq(0)").sortablecolumn("resetSortState");
                rightTable.tscrollbar("content", rightTable.find("th:eq(0)"))
            }
            rightTable.trigger("change")
        });
        rightPaginator.setCurrentPage(1, true);
        var leftPanel = container.find(".first-column .column-content");
        var leftLoading = false;
        leftPanel.on("change", "td.actions-column input:checkbox", function (ev) {
            if ($(ev.target).prop("checked")) {
                checkInputs($(ev.target));
            } else {
                uncheckInputs($(ev.target));
            }
        });
        var leftTable = leftPanel.find("table").tscrollbar({
            vertical: {
                height: 443
            }
        });
        leftPanel.on("change", ".check-all", function (ev) {
            if ($(ev.target).prop("checked")) {
                var inputsToCheck = leftPanel.find("td input:checkbox:not(:checked)");
                if (inputsToCheck.length) {
                    inputsToCheck.each(function () {
                        $(this).prop("checked", true)
                    });
                    checkInputs(inputsToCheck);
                } else {
                    updateHeaderCheckStatus()
                }
            } else {
                var checkedBoxes = leftPanel.find("td input:checkbox:checked");
                if (checkedBoxes.length) {
                    checkedBoxes.each(function () {
                        $(this).prop("checked", false)
                    });
                    uncheckInputs(checkedBoxes);
                }
            }
        });
        var leftPaginator = leftTableUrl ? initializeLeftPanel() : null;
        initializeRightPanel();
        return funcBinds;
    },
    unique: function (config) {
        var _config = {
            url: '',
            elm: undefined,
            editMode: false
        };
        var lockId = bm.getUUID();
        config = $.extend({lockId: lockId}, _config, config);
        var form = config.elm.closest("form");

        function manageTrashRow(row, list) {
            if (list.length == 1) {
                $(list[0]).closest(".form-row").after(row);
                var deleteBox = row.find(".trash-duplicate-delete");
                if (deleteBox.is(":checked")) {
                    deleteBox.trigger("change")
                }
                deleteBox.attr("name", "deleteTrashItem." + list[0].name);
                row.show();
            } else {
                row.hide();
            }
        }

        function clearErrorLock(form, el) {
            form.form("unlock", lockId);
            if (el.is(".trash-conflict-element")) {
                el.removeClass("trash-conflict-element");
                var trashRow = form.find(".trash-row");
                var trashList = form.data("trash-conflicts-list");
                trashList.remove(el[0]);
                manageTrashRow(trashRow, trashList);
            }
        }

        var composite_name = config.elm.attr("composite-unique");
        var composite_field;
        var elm = config.elm;
        if (composite_name) {
            composite_field = form.find("[name='" + composite_name + "']")
        }
        var last_time = 0;

        function afterUniqueFieldChange() {
            var now_time = new Date().getTime();
            if (now_time - last_time <= 20) {
                return
            }
            last_time = now_time;
            var el = config.elm;
            var params = {};
            var form = config.form || el.closest("form");
            if (config.editMode) {
                params.id = form.find("input[name=id]").val();
            }
            params.field = el.attr("unique-field") || el.attr("name");
            params.value = el.val().trim();
            params.customName = el.attr("unique-field-name");
            if (composite_name) {
                params.compositeField = composite_name;
                params.compositeValue = composite_field.val().trim();
            }
            if (!params.value) {
                return;
            }
            if (this != el[0]) {
                el[0].validation_obj.clear()
            }
            bm.ajax({
                url: config.url,
                data: params,
                show_response_status: false,
                success: function () {
                    config.elm.trigger("unique");
                    clearErrorLock(form, el)
                },
                error: function (xhr, status, data) {
                    el[0].validation_obj.showError({msg_template: data.message});
                    form.form("lock", lockId);
                    if (data.errorFlag == "inTrash") {
                        el.data("trash-params", params);
                        if (!el.is(".trash-conflict-element")) {
                            var trashRow = form.find(".trash-row");
                            var trashList = form.data("trash-conflicts-list");
                            if (!trashList) {
                                form.data("trash-conflicts-list", trashList = [el[0]]);
                            } else {
                                trashList.push(el[0])
                            }
                            manageTrashRow(trashRow, trashList);
                            el.addClass("trash-conflict-element")
                        }
                    } else if (el.is(".trash-conflict-element")) {
                        el.removeClass("trash-conflict-element");
                        var trashRow = form.find(".trash-row");
                        var trashList = form.data("trash-conflicts-list");
                        trashList.remove(el[0]);
                        manageTrashRow(trashRow, trashList);
                    }
                }
            });
        }

        elm.on("change blur", afterUniqueFieldChange);
        if (composite_field) {
            composite_field.on("change", afterUniqueFieldChange)
        }
        form.find(".trash-duplicate-restore").off("click").click(function () {
            var trashList = form.data("trash-conflicts-list");
            var params = $(trashList[0]).data("trash-params");
            var _config = trashList[0].unique_config;
            bm.ajax({
                url: _config.restoreUrl,
                data: params,
                success: function (resp) {
                    form.trigger("trash-restore", [resp.type, resp.id]);
                    app.global_event.trigger("trash-restore", [resp.type, resp.id]);
                    app.global_event.trigger(resp.type + "-restore", [resp.id]);
                    var popup = form.closest('.popup').obj(POPUP);
                    if (popup) {
                        popup.close()
                    }
                }
            })
        });
        form.find(".trash-duplicate-delete").off("change").on("change", function () {
            var trashList = form.data("trash-conflicts-list");
            var _config = trashList[0].unique_config;
            if ($(this).prop("checked")) {
                form.form("unlock", _config.lockId).on("ajax-submit-success", function () {
                    app.global_event.trigger("trash-delete")
                });
            } else {
                form.form("lock", _config.lockId).off("ajax-submit-success");
            }
        });
        var obj = ValidationField.createDetachedField(elm);
        config.elm.on("focus", function () {
            clearErrorLock(form, config.elm);
            obj.clear();
        });
        config.elm[0].validation_obj = obj;
        config.elm[0].unique_config = config
    },
    unmask: function (tag) {
        tag.unmask();
    },
    updateCategorySelector: function (selects, domain) {
        selects.filter("select").each(function () {
            var select = $(this),
                currentValues = select.val(),
                assignedValues = select.attr("select-values") != "[]" ? JSON.parse(select.attr("select-values")) : [''],
                data = {domain: domain};
            if (currentValues) {
                if (typeof  currentValues == "string") {
                    currentValues = [currentValues];
                }
            } else {
                currentValues = [];
            }
            if (select.attr("select-key")) {
                data.key = select.attr("select-key")
            }
            if (select.attr("select-text")) {
                data.text = select.attr("select-text")
            }
            bm.ajax({
                url: app.baseUrl + "app/hierarchySelector",
                data: data,
                dataType: "html",
                success: function (resp) {
                    var newSelect = $(resp);
                    var prepends = select.find("option.domain-prepend");
                    var appends = select.find("option.domain-append");
                    select.find("option").remove();
                    var currentValuesExists = false;
                    $.each(currentValues, function (i, val) {
                        if (newSelect.find("option[value='" + val + "']").length > 0) {
                            currentValuesExists = true
                        }
                    });
                    var selectValues = currentValuesExists ? currentValues : assignedValues;
                    select.append(prepends)
                        .append(newSelect.find("option"))
                        .append(appends)
                        .val(selectValues);
                    if (select.attr("multiple")) {
                        select.trigger("liszt:updated")
                    } else {
                        select.chosen();
                    }
                }
            })
        })
    },
    updateDomainSelector: function (selects, domain) {
        selects.filter("select").each(function () {
            var select = $(this),
                currentValues = select.val(),
                assignedValues = select.attr("select-values") != "[]" ? JSON.parse(select.attr("select-values")) : [''],
                data = {domain: domain};

            if (currentValues) {
                if (typeof  currentValues == "string") {
                    currentValues = [currentValues];
                }
            } else {
                currentValues = [];
            }
            if (select.attr("select-key")) {
                data.key = select.attr("select-key")
            }
            if (select.attr("select-text")) {
                data.text = select.attr("select-text")
            }
            bm.ajax({
                url: app.baseUrl + "app/domainSelector",
                data: data,
                dataType: "html",
                success: function (resp) {
                    var newSelect = $(resp);
                    var prepends = select.find("option.domain-prepend");
                    var appends = select.find("option.domain-append");
                    select.find("option").remove();
                    var currentValuesExists = false;
                    $.each(currentValues, function (i, val) {
                        if (newSelect.find("option[value='" + val + "']").length > 0) {
                            currentValuesExists = true
                        }
                    });
                    var selectValues = currentValuesExists ? currentValues : assignedValues;
                    select.append(prepends)
                        .append(newSelect.find("option"))
                        .append(appends)
                        .val(selectValues);
                    if (select.attr("multiple")) {
                        select.trigger("liszt:updated")
                    } else {
                        select.chosen();
                    }
                }
            })
        })
    },
    uploadImageToAssetLibrary: function (config) {
        config = $.extend({}, config);
        bm.editPopup(app.baseUrl + "app/imageUploadForm", $.i18n.prop("upload.image"), "", {}, config);
    },
    viewPopup: function (url, data, popConfig) {
        if (!app.viewCaches) {
            app.viewCaches = {};
        }
        var cache_key = url + "#" + bm.hash(data);
        var viewer = app.viewCaches[cache_key];
        if (viewer) {
            viewer.setActive();
            return viewer;
        }
        viewer = new POPUP($.extend({
            ajax_url: url,
            ajax_settings: {
                data: data
            },
            show_title: false,
            clazz: "view-popup",
            draggable: true,
            drag_handle: "*",
            drag_cancel: ":input, .view-content-block",
            modal: false,
            width: 500
        }, popConfig));
        viewer.on("close", function () {
            delete app.viewCaches[cache_key];
        });
        return app.viewCaches[cache_key] = viewer;
    },
    waitPopup: function () {
        return new POPUP({
            show_close: false,
            width: 620,
            height: 400,
            el: $('<div><div class="bg-white div-mask" style="position: absolute; top: 0; bottom: 0; left: 0; right: 0"><div class="loader2"></div><div class="wait-message">' + $.i18n.prop('this.may.take.some.time') + '</div></div></div>')
        });
    },
    zoneSelectionPopup: function (inputForm, configs) {
        var url = configs.url || app.baseUrl + "zone/zoneSelectionPopup";
        var title = configs.title || $.i18n.prop("select.zones");
        var emp_title = configs.emphasized;
        var data = configs.data || {};
        var success_handler;
        var fieldName = configs.fieldName || "zone";
        var zones = [];
        var previewHolder = configs.preview_holder || inputForm;
        previewHolder.find("input[name='" + fieldName + "']").each(function () {
            zones.push($(this).val())
        });
        $.extend(data, {
            fieldName: fieldName,
            zone: zones
        });
        var previewer = configs.previewer || function (data, selectionPanel) {
            previewHolder.find("input[name='" + fieldName + "']").remove();
            selectionPanel.find("input:hidden[name]").each(function () {
                var name = $(this).attr("name");
                var inp = $("<input type='hidden' name='" + name + "' value='" + $(this).val() + "'>");
                previewHolder.append(inp)
            });
        };
        return bm.editPopup(url, title, emp_title, data, {
            width: 850,
            events: {
                content_loaded: function (popup, form) {
                    var container = this;
                    bm.initZoneSelection(container, "zone", null, [fieldName])
                }
            },
            beforeSubmit: function (form, settings, popup) {
                var data = {};
                data[fieldName] = [];
                form.find("input:hidden[name]").each(function () {
                    var $this = $(this), name = $this.attr("name"), parentRow = $this.parents("tr");
                    data[name].push({
                        name: parentRow.find("td:eq(0)").text().trim(),
                        value: $this.val()
                    })
                });
                previewer(data, form, previewHolder, configs.previewConfig);
                inputForm.trigger("change");
                popup.close();
                return false;
            }
        })
    },
    zoneSelector: function (popup, zoneDropdown) {
        var zonePanel = popup.find(".zone-create-panel"), form = popup.find("form");
        if (!zonePanel.length) {
            zonePanel = $('<div class="zone-create-panel" action="/zone/"></div>');
            zoneDropdown.parent().after(zonePanel);
        }
        zoneDropdown.change(function () {
            if (zoneDropdown.val() == "create-zone") {
                bm.ajax({
                    url: app.baseUrl + "zone/fields",
                    dataType: "html",
                    success: function (resp) {
                        var zoneFields = $(resp);
                        zonePanel.append(zoneFields);
                        zoneFields.updateUi();
                        form.form("append", zonePanel);
                        bm.countryChangeSelection(zoneFields.find("select[name=zone\\.country\\.id]"), true, {
                            isMultiple: "true",
                            stateName: 'zone.state.id'
                        });
                    }
                });
            } else {
                var zoneFields = popup.find(".zone-create-panel *");
                popup.trigger("content-change", [null, zoneFields]);
                zoneFields.remove();
            }
        });
        zoneDropdown.trigger('change');
    },
    countryChange: function (container, config) {
        container.find("#countryId").on('change', function () {
            var name = this.name;
            var type;
            if (name.indexOf(".")) {
                type = name.split(".")[0]
            }
            var prefixed = $(this).attr("name").endsWith("default_country");
            var id = $(this).val();
            var data = $.extend({}, {
                id: id,
                inputClass: "large",
                stateLabel: prefixed ? "default.state" : "state",
                stateName: (type ? type + "." : "") + (prefixed ? "default_state" : "state"),
                noSelection: null
            }, config);
            bm.ajax({
                url: app.baseUrl + "app/loadStateForCountry",
                dataType: 'html',
                data: data,
                success: function (data) {
                    container.find(".form-row.state-selector-row").remove();
                    container.find(".form-row.country-selector-row").after(data);
                    container.find(".form-row.state-selector-row").updateUi();
                    container.trigger("state-load");
                }
            })
        });
    },
    countryChangeSelection: function (countrySelect, isMultiple, params) {
        var countryRow = countrySelect.closest(".form-row");
        var stateSelectionRow = countryRow.next('.state-selector-row');
        var postCodeRow = countryRow.siblings('.post-code');
        if (stateSelectionRow) {
            bindStateChange(stateSelectionRow);
        }
        var country = countrySelect.val();
        if (!country || country.length > 1) {
            postCodeRow.hide();
        }
        countrySelect.change(function () {
            var value = $(this).val();
            stateSelectionRow.remove();
            if ((value && value.length == 1) || !isMultiple) {
                $.extend(params, {id: isMultiple ? value[0] : value});
                bm.ajax({
                    url: app.baseUrl + "app/loadStateForCountry",
                    dataType: 'html',
                    data: params,
                    success: function (data) {
                        stateSelectionRow = $(data).insertAfter(countryRow);
                        if (stateSelectionRow) {
                            bindStateChange(stateSelectionRow);
                            stateSelectionRow.updateUi();
                        }
                        countryRow.parents("form").trigger("state-load")
                    }
                });
                postCodeRow.show();
            } else if (isMultiple) {
                postCodeRow.hide();
            }
        });
        function bindStateChange(stateSelectionRow) {
            stateSelectionRow.find("select[name=zone\\.state\\.id]").change(function () {
                var value = $(this).val();
                if (value && value.length > 1) {
                    postCodeRow.hide();
                } else if (postCodeRow) {
                    postCodeRow.show();
                }
            }).trigger("change");
        }
    },
    updateDomainSelector: function (selects, domain) {
        selects.filter("select").each(function () {
            var select = $(this),
                currentValues = select.val(),
                assignedValues = select.attr("select-values") != "[]" ? JSON.parse(select.attr("select-values")) : [''],
                data = {domain: domain};

            if (currentValues) {
                if (typeof  currentValues == "string") {
                    currentValues = [currentValues];
                }
            } else {
                currentValues = [];
            }
            if (select.attr("select-key")) {
                data.key = select.attr("select-key")
            }
            if (select.attr("select-text")) {
                data.text = select.attr("select-text")
            }
            bm.ajax({
                url: app.baseUrl + "app/domainSelector",
                data: data,
                dataType: "html",
                success: function (resp) {
                    var newSelect = $(resp);
                    var prepends = select.find("option.domain-prepend");
                    var appends = select.find("option.domain-append");
                    select.find("option").remove();
                    var currentValuesExists = false;
                    $.each(currentValues, function (i, val) {
                        if (newSelect.find("option[value='" + val + "']").length > 0) {
                            currentValuesExists = true
                        }
                    });
                    var selectValues = currentValuesExists ? currentValues : assignedValues;
                    select.append(prepends)
                        .append(newSelect.find("option"))
                        .append(appends)
                        .val(selectValues);
                    if (select.attr("multiple")) {
                        select.trigger("liszt:updated")
                    } else {
                        select.chosen();
                    }
                }
            })
        })
    },
    updateCategorySelector: function (selects, domain) {
        selects.filter("select").each(function () {
            var select = $(this),
                currentValues = select.val(),
                assignedValues = select.attr("select-values") != "[]" ? JSON.parse(select.attr("select-values")) : [''],
                data = {domain: domain};
            if (currentValues) {
                if (typeof  currentValues == "string") {
                    currentValues = [currentValues];
                }
            } else {
                currentValues = [];
            }
            if (select.attr("select-key")) {
                data.key = select.attr("select-key")
            }
            if (select.attr("select-text")) {
                data.text = select.attr("select-text")
            }
            bm.ajax({
                url: app.baseUrl + "app/hierarchySelector",
                data: data,
                dataType: "html",
                success: function (resp) {
                    var newSelect = $(resp);
                    var prepends = select.find("option.domain-prepend");
                    var appends = select.find("option.domain-append");
                    select.find("option").remove();
                    var currentValuesExists = false;
                    $.each(currentValues, function (i, val) {
                        if (newSelect.find("option[value='" + val + "']").length > 0) {
                            currentValuesExists = true
                        }
                    });
                    var selectValues = currentValuesExists ? currentValues : assignedValues;
                    select.append(prepends)
                        .append(newSelect.find("option"))
                        .append(appends)
                        .val(selectValues);
                    if (select.attr("multiple")) {
                        select.trigger("liszt:updated")
                    } else {
                        select.chosen();
                    }
                }
            })
        })
    },
    tableToggleRow: function (container, callback) {
        container.find(".toggle-cell").click(function () {
            var _this = $(this);
            var detailsRow = _this.parents("tr").next();
            if (_this.is(".collapsed")) {
                _this.removeClass("collapsed").addClass("expanded");
                detailsRow.show()
            } else {
                _this.removeClass("expanded").addClass("collapsed");
                detailsRow.hide()
            }
            if (callback) {
                callback(_this);
            }
        })

    },
    selectFromAssetLibrary: function (title, selectButtonHandler, fileExtensions, config, isFile) {
        var fileUrl;
        var isSelected;
        config = $.extend({}, config, {
            _self: this,
            width: 500,
            events: {
                content_loaded: function (popup) {
                    var dom = this;
                    var targeteBody = this.find(".remote_folders").scrollbar({
                        vertical: {
                            offset: 2
                        }
                    });
                    var backPath;

                    function loadAssetLibrary(repository) {
                        if (typeof repository == "undefined") {
                            repository = "pub"
                        }
                        WebDAVClient.doPropFind(repository, this, {
                            onPopulate: function (childList) {
                                targeteBody.find(".folder, .file").remove();
                                if (repository != "pub") {
                                    var $tr = $('<div class="folder"> <span class="tool-icon back" title="' + $.i18n.prop("back") + '"></span></div>');
                                    targeteBody.append($tr);
                                    $tr.click(function () {
                                        isSelected = false;
                                        backPath = backPath.substring(0, backPath.lastIndexOf("/"));
                                        loadAssetLibrary(backPath)
                                    });
                                }
                                $.each(childList.folders, function (key, value) {
                                    var $tr = $('<div class="folder"> <span class="tree-icon"></span> <span class="tree-title">' + value.name + '</span></div>');
                                    targeteBody.append($tr);
                                    var clicks = 0;
                                    $tr.click(function () {
                                        var timer = null;
                                        clicks++;
                                        if (clicks === 1) {
                                            timer = setTimeout(function () {
                                                clicks = 0;
                                                $tr.siblings(".selected").removeClass("selected");
                                                isSelected = false;
                                                if (isFile == false) {
                                                    isSelected = true;
                                                    $tr.addClass("selected").siblings(".selected").removeClass("selected");
                                                    fileUrl = bm.encodePath(app.baseUrl + "pub" + value.path.substring(value.path.indexOf("/")))
                                                }
                                            }, 200);
                                        } else {
                                            clicks = 0;
                                            clearTimeout(timer);
                                            backPath = value.path;
                                            loadAssetLibrary(value.path);
                                            targeteBody.next().find(".selectedValue").removeAttr("src").data("loaded", false);
                                        }
                                    });
                                });
                                $.each(childList.files, function (key, value) {
                                    var imageExt = ["bmp", "gif", "jpg", "png", "psd", "pspimage", "thm", "tif", "yuv", "jpeg"];
                                    var extension = value.clazz;
                                    fileExtensions = fileExtensions ? fileExtensions : imageExt;
                                    if (fileExtensions.contains(extension) && isFile != false) {
                                        var $tr = $('<div class="file ' + extension + '"> <span class="tree-icon"></span> <span class="tree-title">' + value.name + '</span></div>');
                                        targeteBody.append($tr);
                                        $tr.click(function () {
                                            isSelected = false;
                                            fileUrl = bm.encodePath(app.systemPubUrl + "/" + value.path.substring(value.path.indexOf("/")+1));
                                            if (isFile === undefined) {
                                                var img = $("<img>", {
                                                    src: fileUrl
                                                });
                                                dom.find(".asset-library-image-preview").html(img);
                                                img.on("load", function () {
                                                    isSelected = true;
                                                    $tr.addClass("selected").siblings(".selected").removeClass("selected")
                                                })
                                            } else if (isFile) {
                                                targeteBody.find(".file.selected").removeClass("selected");
                                                $tr.addClass("selected");
                                                isSelected = true;
                                            }
                                        })
                                    }
                                })
                            }
                        })
                    }

                    loadAssetLibrary("pub");
                    this.find(".file-selector").form({
                        preSubmit: function () {
                            if (isSelected) {
                                selectButtonHandler(fileUrl);
                                popup.close();
                            } else {
                                if (isFile) {
                                    bm.notify($.i18n.prop("file.not.selected"), "error")
                                } else if (isFile === undefined) {
                                    bm.notify($.i18n.prop("image.not.selected"), "error")
                                } else {
                                    bm.notify($.i18n.prop("folder.not.selected"), "error")
                                }
                            }
                            return false
                        }
                    })
                }
            }
        });
        bm.editPopup(app.baseUrl + "remoteRepository/selectFileFromAssetLibraryForWidget", title, "", {isFile: isFile === undefined ? isFile : true}, config)
    },
    uploadImageToAssetLibrary: function (config) {
        config = $.extend({}, config);
        bm.editPopup(app.baseUrl + "app/imageUploadForm", $.i18n.prop("upload.image"), "", {}, config);
    },
    makeTableCellEditable: function (tds, callback) {
        function editingTd() {
            var icon = $(this);
            var td = icon.parent();
            var tr = td.parent();
            td.addClass("editing");
            var tdVal = td.find(".value");
            var oldVal = tdVal.chide().text().trim();
            var validation = td.attr("validation");
            var maxlength = td.attr("maxlength");
            td.append("<input type='text' class='td-full-width' maxlength='" + (maxlength ? maxlength : "") + "' validation='" + (validation ? validation : "") + "'>");
            var restrict = td.attr("restrict");
            var editField = td.find("input[type='text']");
            editField.val(oldVal);
            if (restrict) {
                editField[restrict]();
            }
            var input = editField.get(0);

            var hiddenField = td.find("input[type='hidden']");

            input.selectionStart = input.selectionEnd = input.value.length;
            input.focus();
            td.trigger("cell-edit");
            tr.siblings("tr.data-row").addBack().each(function () {
                $(this).trigger("heightChange");
            });
            function updateTd($this) {
                var editFieldVal = $this.val();
                tdVal.text(editFieldVal);
                if (editFieldVal != oldVal) {
                    hiddenField.val(editFieldVal);
                    if (callback && callback(td, editFieldVal, oldVal) === false) {
                        input.focus();
                        return;
                    }
                }
                editField.remove();
                td.removeClass("editing");
                tdVal.cshow();
                tr.siblings("tr.data-row").addBack().each(function () {
                    $(this).trigger("heightChange");
                })
            }

            editField.on("focusout", function () {
                updateTd($(this));
            }).on("keypress", function (e) {
                if (e.keyCode == 13) {
                    e.preventDefault();
                    updateTd($(this));
                }
            });
        }

        tds.each(function () {
            var td = $(this);
            if (!td.find(".value").length) {
                var html = td.html();
                td.html('<span class="value"></span>');
                td.find(".value").html(html)
            }
            var clazz = "edit";
            if (td.is(".custom-edit")) {
                clazz = "change-all";
                td.append('<span class="fake-link ' + clazz + '">' + $.i18n.prop("change.all") + '</span>');
            } else {
                td.append('<span class="tool-icon ' + clazz + '"></span>');
            }
            var editBtn = td.find("." + clazz);
            editBtn.on("click", editingTd);
        });
    },
    makeTableCellSelectable: function (tds, select, callback, before) {
        function editingTd() {
            var icon = $(this);
            var td = icon.parent();
            td.addClass("editing");
            var tdVal = td.find(".value");
            var tdText = td.find(".text");
            var oldVal = tdVal.chide().text().trim();
            var _select = $.isFunction(select) ? select(td) : select.clone().show();
            _select.addClass('td-full-width');
            if (td.is(".hidden-overflow-selectable")) {
                var div = $("<div class='selectable-td-proxy'></div>").appendTo(document.body);
                div.append(_select);
                div.position({
                    my: "left top",
                    at: "left top",
                    of: td,
                    collision: "none"
                });
                div.width(td.outerWidth(), true);
                div.height(td.outerHeight(), true)
            } else {
                td.append(_select);
            }
            _select.val(oldVal);
            _select.chosen();
            _select.on("chosen:hiding_dropdown", function () {
                var editFieldVal = $(this).val();
                _select.chosen("remove");
                if (oldVal != editFieldVal) {
                    tdVal.text(editFieldVal);
                    var selectedText = $(this).find("option:selected").text();
                    tdText.text(selectedText);
                    if (callback) {
                        callback(td, editFieldVal, selectedText, oldVal);
                    }
                }
                td.removeClass("editing");
                if (td.is(".hidden-overflow-selectable")) {
                    $(".selectable-td-proxy").remove()
                }
                tdVal.cshow();
            });
            _select.chosen("dropshow");
            return false;
        }

        tds.each(function () {
            var td = $(this);
            if (!td.find(".value").length) {
                var html = td.html();
                td.html('<span class="value"></span><span class="text"></span>');
                td.find(".value").html(html);
                td.find(".text").html(html)
            }
            var clazz = "edit";
            if (td.is(".custom-select")) {
                clazz = "change-all";
                td.prepend('<span class="fake-link ' + clazz + '">' + $.i18n.prop("change.all") + '</span>');
            } else {
                td.append('<span class="tool-icon ' + clazz + '"></span>');
            }
            var editBtn = td.find("." + clazz);
            editBtn.on("click", function () {
                if (before && before(td) === false) {
                    return;
                }
                editingTd.call(this)
            });
        });
    },
    autoToggle: function (box) {
        var namespace = bm.getUUID();

        function filterRadios(next_target) {
            var radios = next_target.filter(":radio");
            next_target = next_target.not(radios);
            var processed = [];
            radios.each(function () {
                var $this = $(this), name = $this.attr("name");
                if (processed.contains(name)) {
                    return;
                }
                processed.push(name);
                var win = radios.filter("[name='" + name + "']:checked");
                if (!win.length) {
                    win = box.find("[name='" + name + "']:checked")
                }
                if (!win.length) {
                    win = $this
                }
                next_target = next_target.add(win)
            });
            return next_target
        }

        function chainToggle(target, show, mainTargetSelector) {
            var next_target = target.find("[toggle-target]");
            if (target.is("[toggle-target]")) {
                next_target = next_target.add(target)
            }
            next_target = filterRadios(next_target);
            next_target.each(function () {
                var _target = $(this);
                var evName = _target.is("select, :checkbox, :radio") ? "change" : "click";
                if (show) {
                    _target.triggerHandler(evName + "." + namespace, [false, mainTargetSelector])
                } else {
                    _target.triggerHandler(evName + "." + namespace, [true, mainTargetSelector])
                }
            })
        }

        function handleToggle(target, checked, animation, mainTargetSelector) {
            target.each(function () {
                var _target = $(this);
                if (mainTargetSelector && _target.is(mainTargetSelector)) {
                    return;
                }
                var reverse = _target.is("[do-reverse-toggle]");
                var _checked = checked;
                if (reverse) {
                    _checked = !checked
                }
                var is_hide = _target.css("display") == "none";
                if (_checked && is_hide) {
                    var done = function () {
                        chainToggle(_target, true, mainTargetSelector);
                    };
                    if (animation) {
                        _target.show(animation, done);
                    } else {
                        _target.show();
                        done()
                    }
                } else if (!_checked && !is_hide) {
                    var done = function () {
                        var validatable = _target.find('[validation]:not([validate-on="call-only"])');
                        if (_target.is("[validation]")) {
                            validatable = validatable.add(_target)
                        }
                        validatable.trigger('[validation]:not([validate-on="call-only"])');
                        chainToggle(_target, false, mainTargetSelector);
                        _target.find("input").valid();
                    };
                    if (animation) {
                        _target.hide(animation, done);
                    } else {
                        _target.hide();
                        done()
                    }
                } else {
                    chainToggle(_target, _checked, mainTargetSelector);
                }
            })
        }

        function isATargetOrInChain(toggler) {
            var yes = false;
            var targets = box.find("[toggle-target]");
            targets.each(function () {
                var target = $(this).attr("toggle-target");
                if ($(this).is("select")) {
                    var options = $(this).find("option");
                    options.each(function () {
                        var targetSelector = "." + target + "-" + $(this).val();
                        if (toggler.closest(targetSelector, box[0]).length) {
                            yes = true;
                            return false;
                        }
                    })
                } else {
                    yes = toggler.closest("." + target, box[0]).length
                }
                if (yes) {
                    return false
                }
            });
            return yes;
        }

        var getAllChainedTargets = function (toggler) {
            var allTogglers = toggler;
            toggler.each(function () {
                var _toggler = $(this);
                var nextTarget;
                if (_toggler.is("select")) {
                    var type = _toggler.val().replaceAll("\\.", "_");
                    nextTarget = box.find("." + _toggler.attr("toggle-target") + "-" + type);
                } else {
                    nextTarget = box.find("." + _toggler.attr("toggle-target"))
                }
                if (nextTarget.length) {
                    var nextTogglers = nextTarget.find("[toggle-target]");
                    nextTogglers = nextTogglers.add(nextTarget.filter("[toggle-target]"));
                    if (nextTogglers.length) {
                        allTogglers = allTogglers.add(getAllChainedTargets(nextTogglers))
                    }
                }
            });
            return allTogglers
        };
        var allTargets = box.find("[toggle-target]").each(function () {
            var toggler = $(this);
            var animation = toggler.attr("toggle-anim") ? toggler.attr("toggle-anim") : "blind";
            if (animation == "none") {
                animation = null;
            }
            var evName;
            if (toggler.is("select")) {
                evName = "change";
                toggler.on(evName + "." + namespace, function (e, forceOff, excludeTargets) {
                    if (typeof forceOff != "boolean") {
                        forceOff = false;
                        excludeTargets = null
                    }
                    var type = toggler.val().replaceAll("\\.", "_");
                    var target = "." + toggler.attr("toggle-target") + "-" + type;
                    var toHideTargets = $();
                    toggler.find("option").each(function (i, opt) {
                        var option_type = $(opt).val().replaceAll("\\.", "_");
                        if (option_type != type) {
                            var selector = toggler.attr("toggle-target") + "-" + option_type;
                            var hideTarget = box.find("." + selector);
                            toHideTargets = toHideTargets.add(hideTarget.not(target))
                        }
                    });
                    target = box.find(target);
                    var subExcludeTargets = excludeTargets ? excludeTargets.add(getAllChainedTargets(target)) : getAllChainedTargets(target);
                    handleToggle(toHideTargets, false, animation, subExcludeTargets);
                    handleToggle(target, forceOff ? false : true, animation, excludeTargets)
                });
            } else if (toggler.is(":checkbox, :radio")) {
                evName = "change";
                var targetText = "." + toggler.attr("toggle-target");
                var target = box.find(targetText);

                function changeHandler(e, forceOff, excludeTargets, isMain) {
                    if (typeof forceOff != "boolean") {
                        forceOff = false;
                        excludeTargets = null
                    }
                    if (!forceOff) {
                        var otherRadios = box.find("[name='" + toggler.attr("name") + "']").not(toggler).filter("[toggle-target]:not([independent])");
                        var toHideTargets = $();
                        otherRadios.each(function (i, opt) {
                            var hideTarget = box.find("." + $(opt).attr("toggle-target"));
                            toHideTargets = toHideTargets.add(hideTarget.not(target))
                        });
                        handleToggle(toHideTargets, false, animation, isMain ? (excludeTargets ? excludeTargets.add(getAllChainedTargets(target)) : getAllChainedTargets(target)) : excludeTargets)
                    }
                    var checked = toggler.prop("checked");
                    toggler.state = checked;
                    if (toggler.is(".toggle-reverse")) {
                        checked = !checked
                    }
                    handleToggle(target, forceOff ? false : checked, animation, excludeTargets, isMain);
                }

                toggler.on(evName + "." + namespace, function (a, b, c) {
                    changeHandler(a, b, c, true)
                });
                if (toggler.is(":radio")) {
                    var otherRadios = box.find("[name='" + toggler.attr("name") + "']:radio").not("[toggle-target]");
                    otherRadios.on(evName + "." + namespace, function (e, xx, excludeTarget) {
                        if (typeof xx != "boolean") {
                            excludeTarget = null
                        }
                        if (toggler.state || toggler.state === undefined) {
                            changeHandler(e, true, excludeTarget)
                        }
                    })
                }
            } else {
                evName = "click";
                var target = box.find("." + toggler.attr("toggle-target"));
                toggler.on(evName + "." + namespace, function (e, forceOff, excludeTargets) {
                    var checked = toggler.attr("row-expanded") == "true";
                    if (typeof forceOff == "undefined") {
                        checked = !checked;
                        toggler.attr("row-expanded", "" + checked)
                    }
                    handleToggle(target, forceOff ? false : checked, animation, excludeTargets)
                })
            }
        });
        filterRadios(allTargets).each(function () {
            var toggler = $(this);
            var evName = toggler.is("select, :checkbox, :radio") ? "change" : "click";
            if (!isATargetOrInChain(toggler)) {
                toggler.triggerHandler(evName + "." + namespace, [false]);
            }
        });
        return box
    },
    cBaseDoc: function (nodes) {
        var slideDom = $("<div id='documentation-slide' style='display: none'><div class='icon-block'><span class='tool-icon close-icon' title='" + $.i18n.prop("close") + "'></span></div><iframe src='about:blank'></iframe></div>");
        var body = $(document.body);
        nodes.each(function () {
            var node = $(this);
            if (!node.attr("attached")) {
                node.click(function () {
                    var docUrl = node.attr("target-url");
                    if (slideDom.is(":hidden")) {
                        body.prepend(slideDom);
                        slideDom.css({height: window.innerHeight});
                        slideDom.find(".tool-icon.close-icon").click(function () {
                            slideDom.toggle("slide", {direction: 'right'}, function () {
                                body.find("#documentation-slide").remove();
                            });
                        });
                        slideDom.toggle("slide", {direction: 'right'});
                        slideDom.find("iframe").attr("src", docUrl);
                    } else {
                        slideDom.find("iframe").attr("src", docUrl);
                    }
                });
                node.attr("attached", true);
            }
        });
    },
    floatingPanel: function (ref, url, data, conf) {
        conf = conf ? conf : {};
        conf.clazz = "floating-panel-popup" + (conf.clazz ? " " + conf.clazz : "")
        var popup = new POPUP($.extend({
            ajax_url: url,
            ajax_settings: {
                data: data
            },
            show_title: false,
            masking: false,
            show_close: false,
            close_on_blur: true,
            modal: false,
            is_center: false,
            width: 350,
            ui_position: {
                my: "right top",
                at: "right+10 bottom+7",
                of: ref,
                collision: conf.position_collison
            }
        }, conf));
        return popup
    },
    selectionFloatingPanel: function (ref, url, data, config) {
        var instance = {
            isLoaded: false,
        };
        instance.reload = function () {
            var _self = this, element = _self.el, data = {searchText: _self.searchText};
            element.loader();
            config.beforeReload && config.beforeReload(data);
            bm.ajax({
                url: url,
                data: data,
                dataType: "html",
                response: function() {
                    element.loader(false);
                },
                success: function(resp) {
                    element.find(".body").replaceWith($(resp).find(".body"));
                    _self.attachEvents();
                }
            });
        };
        instance.attachEvents = function () {
            var _self = this, element = _self.el;
            element.find(".body").scrollbar({
                vertical: {
                    offset: 15
                }
            });
            element.updateUi();
            element.find(".select-item").on("click", function() {
                var $this = $(this);
                if(!$this.is(".selected")) {
                    config.onSelect($this.config("entity"));
                }
                _self.popup.close()
            })
        };
        var events = (config.events || {});
        var _caller_content_loaded = events ? events.content_loaded : undefined;
        events.content_loaded = function (popup) {
            var el = instance.el = popup.el, searchForm = el.find(".search-form");
            instance.popup = popup;
            el.find(".cancel-button").on("click", function() {
                popup.close();
            });
            searchForm.form({
                preSubmit: function() {
                    var searchText = searchForm.find(".search-text").val();
                    if(instance.searchText === searchText) {
                        return false
                    }
                    instance.searchText = searchText;
                    instance.reload();
                    return false
                }
            });
            instance.attachEvents();
            _caller_content_loaded && _caller_content_loaded.apply(this, arguments)
        };
        delete config.events;
        bm.floatingPanel(ref, url, data, $.extend({
            width: 400,
            height: null,
            events: events,
            position_collison: "none"
        }, config));
        return instance
    },
    metaTagEditor: function (panel) {
        var rowTemplate = '<tr><td class="name editable" maxlength="">#NAME#</td><td class="value editable" maxlength="">#VALUE#</td><td class="actions-column"><input type="hidden" name="tag_name" value="#NAME#">' +
            '<input type="hidden" name="tag_content" value="#VALUE#"><span class="tool-icon remove"></span></td></tr>';
        var metaTagSection = panel.find(".meta-tag-editor"), tagTable = metaTagSection.find("table"), lastRow = tagTable.find("tr.last-row");

        function attachRowEvent(content) {
            bm.makeTableCellEditable(content.find("td.editable"), function (td, newVal, oldVal) {
                var parent = td.parent("tr");
                var flag = false;
                if (!newVal) {
                    errorHighlight(td.find("input"));
                    bm.notify($.i18n.prop("value.must.not.be.empty"), "error");
                    return false
                }
                if (td.is(".name")) {
                    parent.find("[name=tag_name]").val(newVal)
                } else {
                    parent.find("[name=tag_content]").val(newVal)
                }
            });
            content.find("td span.remove").on("click", function () {
                $(this).parents("tr").remove();
            })
        }

        function addTag(name, value, lastRow) {
            var row = $(rowTemplate.replaceAll("#NAME#", name.htmlEncode()).replaceAll("#VALUE#", value.htmlEncode()));
            if (lastRow) {
                if (lastRow.find(".name").attr("maxlength")) {
                    row.find(".name").attr("maxlength", lastRow.find(".name").attr("maxlength"))
                }
                if (lastRow.find(".value").attr("maxlength")) {
                    row.find(".value").attr("maxlength", lastRow.find(".value").attr("maxlength"))
                }
            }
            attachRowEvent(row);
            lastRow.before(row)
        }

        tagTable.find("tr:gt(0):not(.last-row)").each(function () {
            attachRowEvent($(this));
        });
        function errorHighlight(item) {
            item.addClass("error-highlight");
            setTimeout(function () {
                item.removeClass("error-highlight")
            }, 1000);
        }

        var addMapping = function () {
            var name = lastRow.find(".name").val(), value = lastRow.find(".value").val();
            if (!name) {
                errorHighlight(lastRow.find(".name"));
                return;
            }
            if (!value) {
                errorHighlight(lastRow.find(".value"));
                return;
            }
            addTag(name, value, lastRow);
            lastRow.find("input").val("");
            return false
        };
        lastRow.find("input").bind("keyup.key_return", addMapping);
        lastRow.find(".add-row").on("click", addMapping);
        lastRow.find("input").on("keydown, keyup, keypress", function (e) {
            if (e.keyCode == 13) {
                return false
            }
        })
    },
    initCityValidator: function (postcodes, countryFiledName, stateFiledName, form, cityFieldName) {
        countryFiledName = countryFiledName ? countryFiledName : "country.id";
        stateFiledName = stateFiledName ? stateFiledName : "state.id";
        if (postcodes.length) {
            postcodes.each(function () {
                var postCode = $(this);
                form = form ? form : postCode.closest("form");
                var state = form.find('[name="' + stateFiledName + '"]');
                var country = form.find('[name="' + countryFiledName + '"]');
                var container = form.find('.city-selector-row');

                function checker(excludeCountry) {
                    var data = {state: state.val(), postCode: postCode.val(), cityFieldName: cityFieldName};
                    if (excludeCountry != true) {
                        data.country = country.val()
                    }
                    var validation = container.find(":input").attr("validation");
                    if (validation) {
                        data.validation = validation;
                    }
                    bm.ajax({
                        url: app.baseUrl + 'app/loadCitiesByCountryOrState',
                        data: data,
                        dataType: 'html',
                        success: function (resp) {
                            resp = $(resp);
                            if (app.is_front_end) {
                                container.find(":input").remove();
                            } else {
                                container.children().not("label").remove();
                            }
                            container.append(resp);
                            if (!app.is_front_end && resp.attr("et-category") == "dropdown") {
                                resp.chosen();
                            }
                            var validator = resp.closest("form").obj(ValidationPanel);
                            if (validator) {
                                validator.attach(resp.filter("[validation]"), validator);
                            }
                            var stateId = resp.find("option:selected").attr("state");
                            if (stateId) {
                                state.val(stateId);
                                if (!app.is_front_end) state.trigger("chosen:updated")
                            }
                        }
                    });
                }

                if (container.length) {
                    state.change(function () {
                        checker(true)
                    });
                    form.on("state-load", function () {
                        state = form.find('[name="' + stateFiledName + '"]');
                        checker();
                        if (state.length) {
                            state.change(checker)
                        }
                    });
                    postCode.ichange(checker);
                }
            })
        }
    },
    initCountryChangeHandler: function (select, stateName) {
        select.change(function () {
            var _self = $(this);
            var form = _self.closest("form");
            var id = $(this).val();
            $.ajax({
                url: app.baseUrl + "app/loadStateForCountry",
                dataType: 'html',
                data: {id: id, stateName: stateName},
                success: function (data) {
                    form.find(".form-row.state-selector-row").remove();
                    select.parents(".form-row").after(data);
                    form.trigger("state-load")
                }
            })
        });
    },
    customTooltip: function (url, count, pos, atBottom, atRight, callback) {
        config = {
            auto_close_on_success: true,
            disable_on_submit: true,
            disable_on_invalid: true,
            disable_button_text: undefined,
            modify_ui: true,
            on_load_loader: false
        };
        if (!pos[count - 1].length) {
            count = count + 1;
            atBottom = "7";
            atRight = "-7"
        }
        var events = (config.events || {});
        $(pos[count - 1]).addClass("active");
        events.content_loaded = function (popup) {
            var _self = this;
            _self.updateUi();
            if (count > 2) {
                atBottom = "7";
                atRight = "-7";
                var btn = $("<button type='button' class='submit-button-pre'></button>");
                btn.text($.i18n.prop("prev"));
                _self.find(".pop-bottom").append(btn)
            }
            if (count == 8) {
                _self.find(".submit-button-next").remove();
                var btn = $("<button type='button' class='submit-button-done'></button>");
                btn.text($.i18n.prop("done"));
                _self.find(".pop-bottom").prepend(btn);
                var div = $("<div class='show-next-wrapper'>" + "<input type='checkbox' name='remember' class='single'>" + "<span class='show-next'> Show on next login</span>" + "</div>");
                _self.find(".pop-bottom").append(div)
            }
            var check = true;
            _self.find(".single").on("change", function () {
                check = this.checked ? false : true
            });
            _self.find(".submit-button-done, .submit-button-skip").click(function () {
                bm.ajax({
                    url: app.baseUrl + "user/saveIsMatured",
                    data: {check: check},
                    success: function (resp) {
                        if (callback && callback.getStart) {
                            callback.getStart();
                        }
                    }
                });
                popup.close(1);
                $(pos[count - 1]).removeClass("active")
            });
            _self.find(".submit-button-next").click(function () {
                count++;
                popup.close(1);
                $(pos[count - 2]).removeClass("active");
                atBottom = "7";
                atRight = "-7";
                atRight = (count == 3) ? "+15" : "-7";
                if (count < pos.length + 1) {
                    bm.customTooltip(app.baseUrl + "dashboard/loadOnScreenHelp", count, pos, atBottom, atRight, callback);
                }
            });
            _self.find(".submit-button-pre").click(function () {
                popup.close(1);
                if (count > 1) {
                    $(pos[count - 1]).removeClass("active");
                    count--;
                    switch (count) {
                        case 1:
                            atBottom = "100";
                            atRight = "-100";
                            break;
                        case 3:
                            atBottom = "7";
                            atRight = "+15";
                            break;
                        default :
                            atBottom = "7";
                            atRight = "-7"
                    }
                }
                bm.customTooltip(app.baseUrl + "dashboard/loadOnScreenHelp", count, pos, atBottom, atRight, callback)
            });
            _self.find(".tool-tip-close").click(function () {
                $(pos[count - 1]).removeClass("active");
                popup.close(1);
                if (callback && callback.getStart) {
                    callback.getStart(1);
                }
            });
            _self.addClass("loaded")
        };
        delete config.events;
        return new POPUP($.extend({
            template: '<div></div>',
            show_title: false,
            width: 380,
            height: 150,
            ajax_url: url,
            ajax_settings: {
                data: {count: count, wizard: pos[0].length ? true : null}
            },
            ui_position: {
                my: "right top+10",
                at: "right" + atRight + " bottom+" + atBottom,
                of: pos[count - 1]
            },
            draggable: false
        }, config, {clazz: "tool-tip-popup", events: events}))
    },
    highlight: function (item, time, blink) {
        item.addClass("highlight-row" + (blink ? " blink" : ""));
        setTimeout(function () {
            item.removeClass("highlight-row" + (blink ? " blink" : ""));
        }, time ? time : 3000);
    },
    renderZoneView: function (_self) {
        var zoneUrl = app.baseUrl + "zone/loadZoneView";
        var zoneTable;

        function reload() {
            var panel = _self.body.find(".right-panel");
            panel.loader();
            bm.ajax({
                url: zoneUrl,
                dataType: "html",
                response: function () {
                    panel.loader(false);
                }, success: function (resp) {
                    zoneTable = $(resp);
                    panel.find(".body").html(zoneTable);
                    bm.table(zoneTable, {
                        url: zoneUrl,
                        onload: function () {
                        },
                        menu_entries: [
                            {
                                text: $.i18n.prop("edit"),
                                ui_class: "edit",
                                action: "edit"
                            },
                            {
                                text: $.i18n.prop("remove"),
                                ui_class: "remove",
                                action: "remove"
                            }
                        ],
                        onActionClick: function (action, data, navigator) {
                            switch (action) {
                                case "edit":
                                    editZone(navigator.parents("tr"), data);
                                    break;
                                case "remove":
                                    app.tabs.zone.deleteZone(data.id, data.name, undefined, reload);
                                    break;
                            }
                        }
                    });
                    zoneTable.find(".add-zone-btn").on("click", function () {
                        var base = $(zoneTable.find("tr")[0]);
                        editZone(base);
                    });
                    _self.body.find(".toolbar-btn.save").hide();
                    panel.scrollbar();
                }
            });
        }

        function editZone(base, data) {
            var panel = _self.body.find(".right-panel");
            panel.loader();
            bm.ajax({
                url: app.baseUrl + "zone/createZone",
                data: data,
                dataType: "html",
                response: function () {
                    panel.loader(false);
                },
                success: function (resp) {
                    resp = $("<td colspan='5'>" + resp + "</td>");
                    var form = resp.find("form");
                    bm.countryChangeSelection(form.find("select[name=zone\\.country\\.id]"), true, {
                        isMultiple: "true",
                        stateName: 'zone.state.id'
                    });
                    form.find(".cancel-button").on("click", function () {
                        reload();
                    });
                    base.empty().append(resp);
                    form.form({
                        ajax: true,
                        preSubmit: function (ajaxSetting) {
                            $.extend(ajaxSetting, {
                                success: function (resp) {
                                    reload();
                                }
                            })
                        }
                    });
                    form.updateUi();
                }
            })
        }

        reload();
    },
    /**
     * 'this' should refer some sort of dom dependent object (element should have dom)
     */
    trigger: function (dom, base, name, args) {
        if (arguments.length == 5) {
            return bm.trigger.call(dom, base, name, args, arguments[4])
        }
        var returnValue;
        if (!$.isArray(args)) {
            args = [args]
        }
        if ($.isFunction(this["on" + name.capitalize()])) {
            returnValue = this["on" + name.capitalize()].apply(this, args)
        }
        if (returnValue === false) {
            return false
        }
        if (this.options && $.isFunction(this.options[name])) {
            returnValue = this.options[name].apply(this, args)
        }
        if (returnValue === false) {
            return false
        }
        return dom.trigger(base + ":" + name, args)
    },
    $wrap: function (property, clazz) {
        var plugin = property.camelCase(false);
        if (window.jquery_conflict_prefix) {
            plugin = window.jquery_conflict_prefix + plugin
        }
        $.prototype[plugin] = function (options) {
            if (!this.length) {
                return this;
            }
            var obj;
            if (typeof options == "string" || options instanceof String) {
                obj = this.data("wcui-" + property + "-obj");
                if (obj) {
                    if (options == "option") {
                        if (arguments.length == 2) {
                            if ($.isFunction(obj._getOption)) {
                                return obj._getOption(arguments[1])
                            }
                            return obj.options[arguments[1]]
                        }
                        if ($.isFunction(obj._setOption)) {
                            obj._setOption(arguments[1], arguments[2])
                        } else {
                            obj.options[arguments[1]] = arguments[2]
                        }
                        return this
                    }
                    if (options == "extendObjElement") {
                        arguments[1].data("wcui-" + property + "-obj", obj)
                    }
                    if (options == "removeObjElement") {
                        arguments[1].removeData("wcui-" + property + "-obj")
                    }
                    if (!$.isFunction(obj[options]) || options.startsWith("_")) {
                        throw new Error("Undefined function " + options)
                    }
                    var retCode = obj[options].apply(obj, Array.prototype.splice.call(arguments, 1));
                    if (retCode === undefined) {
                        return this;
                    }
                    return retCode
                }
            }
            options = $.extend({}, options, this.config(property));
            obj = new clazz(this, options);
            this.data("wcui-" + property + "-obj", obj);
            return this
        }
    },
    clearTextSelection: function (_document) {
        if (!_document) {
            _document = document
        }
        var selection = null;
        var _window = _document.defaultView;
        if (_window.getSelection) {
            selection = _window.getSelection();
        } else if (_document.selection) {
            selection = _document.selection;
        }
        if (selection) {
            if (selection.empty) {
                selection.empty();
            }
            if (selection.removeAllRanges) {
                selection.removeAllRanges();
            }
        }
    },
    disableTextSelection: function (_document) {
        if (!_document) {
            _document = document
        }
        if (browser.ff) {
            bm.clearTextSelection(_document);
            _document.body.style["MozUserSelect"] = "none"
        } else {
            _document.onselectstart = function () {
                return false;
            }
        }
    },
    enableTextSelection: function (_document) {
        if (!_document) {
            _document = document
        }
        if (browser.ff) {
            _document.body.style["MozUserSelect"] = ""
        } else {
            _document.onselectstart = null;
        }
    },
    getTopScroller: function (_document) {
        return $(/webkit/i.test(navigator.userAgent) || _document.compatMode == 'BackCompat' ? _document.body : _document.documentElement)
    },
    errorHighLight: function (item, time) {
        item.addClass("error-highlight");
        setTimeout(function () {
            item.removeClass("error-highlight");
        }, time ? time : 1000);
    },
    attachModernUiPanel: function () { // TODO: Should be removed
        var _self = this;
        var leftPanel = _self.body.find(".modern-list-left-panel");
        if (leftPanel.length) {
            var leftPanelConfig = $.extend({
                thumbPanel: $(),
                url: "",
                thumb_menu_entries: [],
                thumbMenuClick: function (config) {
                },
                leftNavigationClick: function (type) {
                }
            }, _self.leftPanelConfig || {});

            leftPanel.find(".left-panel-btn button").click(function () {
                var newItem = leftPanel.find(".item-thumb.new-item");
                var selected = leftPanel.find(".body .selected").attr("item-id");
                if (newItem.length) {
                    newItem.find(".item-title").focus();
                    return;
                }
                var newDom = leftPanelConfig.thumbPanel.clone();
                _self.body.find(".left-panel .item-wrapper").prepend(newDom);
                var field = ValidationField.createDetachedField(newDom.find("input"), {
                    error_position: "after"
                });
                var applyClicked = false;
                newDom.find(".item-title").focus().on("keyup.key_return", function () {
                    updateName();
                }).on("blur", function () {
                    if (!applyClicked) {
                        _self.reload(selected, true);
                    }
                    applyClicked = false;
                });

                newDom.find(".tool-icon.apply").mousedown(function () {
                    applyClicked = true
                }).click(function () {
                    updateName();
                });

                function updateName() {
                    if (field.validate()) {
                        var name = field.elm.val();
                        bm.ajax({
                            show_response_status: false,
                            url: leftPanelConfig.url,
                            data: {name: name},
                            success: function (resp) {
                                _self.reload(resp.id, true, function (id) {
                                    leftPanel.find(".item-thumb[item-id=" + id + "]").trigger("click");
                                });
                            },
                            error: function (a, b, resp) {
                                field.elm.focus();
                                field.showError({
                                    msg_template: resp.message,
                                    msg_params: [],
                                    rule: undefined
                                })
                            }
                        })
                    } else {
                        field.elm.focus()
                    }
                }
            });
            bm.menu(leftPanelConfig.thumb_menu_entries, _self.body.find(".left-panel .float-menu-navigator"), null, {
                hide: function (entity) {
                    entity.parent().removeClass("float-menu-opened");
                },
                click: function (action, navigator) {
                    var config = navigator.closest(".item-thumb").config("item");
                    leftPanelConfig.thumbMenuClick.call(_self, action, config);
                }
            }, "click", ["center bottom", "right+22 top+7"]);

            _self.body.find(".item-thumb").on("click", function (e) {
                var $this = $(this);
                if ($(e.target).is(".float-menu-navigator")) { //prevent firing for childs
                    return;
                }
                _self.body.find(".item-thumb.selected").removeClass("selected");
                $this.addClass("selected");
                var selected = $this.attr("item-id");
                _self.reload(selected);
            });
            leftPanel.find(".one-line-scroll-content").scrollbar({
                show_vertical: false,
                show_horizontal: true,
                use_bar: false,
                visible_on: "auto",
                horizontal: {
                    handle: {
                        left: leftPanel.find(".left-scroller"),
                        right: leftPanel.find(".right-scroller")
                    },
                    step: "auto"
                }
            });
            leftPanel.find(".navigation-button").on("click", function () {
                var $this = $(this);
                leftPanelConfig.leftNavigationClick.call(_self, $this.attr("item-type"))
            });
            //Modern List Left Panel End
        }
    },
    instantSearch: function (searchContext, searchForm, nodeSelector, textSelector, callback) {
        var searchBox = searchForm.find(".search-text");

        function filter() {
            var searchText = searchBox.val();
            if (searchText) {
                searchContext.addClass("search-active")
            } else {
                searchContext.removeClass("search-active")
            }
            searchContext.find(nodeSelector).each(function () {
                var $this = $(this), name = $this.find(textSelector).text().trim();
                if (name.match(new RegExp(searchText, "i"))) {
                    $this.show()
                } else {
                    $this.hide()
                }
            });
        }

        searchBox.ichange(800, function () {
            filter()
        });
        searchForm.form({
            preSubmit: function () {
                filter();
                return false
            }
        })
    },
    floatingPopup: function (_content, _config) {
        _content.find(".popup-body").hide();
        var _popup = undefined;
        _content.find(".floating-action-dropper").click(function () {
            var config = $.extend({}, _config)
            var navigator = this.jqObject;
            var popup = navigator.data("popup")
            if(popup) {
                popup.show();
                return;
            }
            var body = navigator.next(".popup-body");
            config = $.extend({
                content: body,
                position_collison: "none",
                events: {
                    content_loaded: function (popup) {
                        body.show();
                    }
                }
            }, config);
            _popup = popup = bm.floatingPanel(navigator, null, {}, config);
            popup.close = popup.hide;
            navigator.data("popup", popup)
        });
        _content.find(".close-popup").click(function () {
            if(_popup) {
                _popup.close();
            }
        });
        return _content
    },
    renderSitePopup: function (url, title, emphasized, data, config) {
        config = $.extend({}, {
            auto_close_on_success: true,
            disable_on_submit: true,
            disable_on_invalid: true,
            disable_button_text: undefined,
            modify_ui: true,
            width: 500,
            form_selector: ".site-popup-form",
            buttonLine: "button-line",
            scroll: true
        }, config);
        var events = (config.events || {});
        var _caller_content_loaded = events ? events.content_loaded : undefined;
        events.content_loaded = function (popup) {
            var form = this.find(config.form_selector);
            var newSubmit;
            if (typeof(config.buttonLine) == "string") {
                var buttonLine = form.find("." + config.buttonLine);
                newSubmit = buttonLine.clone();
                buttonLine.remove();
            } else {
                newSubmit = config.buttonLine
            }
            var button = newSubmit.find(".submit-button, button[type=submit]").addClass("site-popup-form-submit").on("click", function () {
                form.submit();
            });
            this.append(newSubmit);
            var _success = (config.ajax && config.ajax.success) || config.success;
            form.form({
                ajax: this.find(".site-popup-form").attr("no-ajax") == null,
                disable_on_submit: config.disable_on_submit,
                disable_on_invalid: config.disable_on_invalid,
                disable_button_text: config.disable_button_text,
                submitButton: button,
                preSubmit: function (ajaxSettings) {
                    $.extend(ajaxSettings, {
                        response: config.response,
                        error: config.error
                    }, config.ajax, {
                        success: function () {
                            if (config.auto_close_on_success) {
                                popup.close();
                            }
                            if (_success) {
                                _success.apply(this, arguments);
                            }
                        }
                    });
                    if (config.beforeSubmit) {
                        return config.beforeSubmit.call(popup.getDom(), form, ajaxSettings ? (ajaxSettings.data = ajaxSettings.data || {}) : null, popup);
                    }
                }
            });
            form.find("[default-focus]").focus();
            this.find(".cancel-button").click(function () {
                popup.close(1);
            });
            popup.on("content-change", function (ev, added, removed) {
                if (added) {
                    form.obj(ValidationPanel).attach(added.filter("[validation]").add(added.find("[validation]")));
                }
                if (removed) {
                    form.obj(ValidationPanel).detach(removed.filter("[validation]").add(removed.find("[validation]")));
                }
            });
            if (_caller_content_loaded) {
                _caller_content_loaded.apply(this, [popup, form]);
            }
        };
        delete config.events;
        return new POPUP($.extend({
            title: title + (emphasized ? " - <span class='emphasized'>" + bm.htmlEncode(emphasized) + "</span>" : ""),
            width: 430,
            ajax_url: url,
            ajax_settings: {
                data: data
            }
        }, config, {clazz: "site-popup" + (config.clazz ? " " + config.clazz : ""), events: events}))
    }
});

$.extend(app, {
    isPermitted: function (key, params) {
        if (app.permissions === undefined) {
            return true;
        }
        var admin = app.admin_id;
        var permission = app.permissions[key] ? app.permissions[key] : {};
        var allowed = permission.general == true;
        var policyEntityParam = params ? params.id : undefined;
        policyEntityParam = policyEntityParam ? (policyEntityParam instanceof Array ? policyEntityParam : [policyEntityParam]) : [];
        if (policyEntityParam.length && (permission.general == null || allowed)) {
            if (permission.allowed_entity) {
                var allowed_set = permission.allowed_entity.intersect(policyEntityParam);
                if (Object.keys(permission.allowed_entity).length && allowed_set.length == policyEntityParam.length) {
                    allowed = true;
                }
            }
            if (permission.denied_entity) {
                var denied_set = permission.denied_entity.intersect(policyEntityParam);
                if (Object.keys(permission.denied_entity).length && denied_set.length != 0) {
                    allowed = false;
                }
            }
        }

        if (!allowed) {
            if (permission.owner) {
                if (admin == params.owner_id) {
                    allowed = true;
                }
            }
        }
        return allowed;
    },
    checkPermission: function (menu, menuItems, config) {
        if (app.permissions === undefined) {
            return;
        }
        if (menu) {
            $.each(menuItems, function (i, v) {
                var cfg = v.isEntity ? config : {};
                if (app.isPermitted(v.key, cfg)) {
                    if (menu.enable) {
                        menu.enable(v['class']);
                    } else {
                        menu.find("." + v['class']).removeClass("disabled");
                    }
                } else {
                    if (menu.disable) {
                        menu.disable(v['class']);
                    } else {
                        menu.find("." + v['class']).addClass("disabled");
                    }
                }
            });
        }
    },
    updatePermissions: function () {
        bm.ajax({
            url: app.baseUrl + "adminBase/updatePermissions",
            data: {},
            success: function (resp) {
                if (app.permissions !== undefined) {
                    app.permissions = resp.permissions;
                }
            }
        })
    }
});