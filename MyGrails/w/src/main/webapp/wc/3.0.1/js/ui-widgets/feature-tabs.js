var _u = app.tab_utils = {};
_u.searchEvent  = (function() {
    function SearchEvent(tabObj, searchTool, options) {
        var _self = this;
        this.tab = tabObj;
        this.searchTool = searchTool;
        this.options = options;
        var simpleSearchForm = searchTool.find(".search-form");
        var removeSearchBtn = $('<span class="remove-search" title="' + $.i18n.prop("remove.search") + '">').tooltipster();
        simpleSearchForm.prepend(removeSearchBtn);
        if(tabObj.simpleSearchText) {
            removeSearchBtn.show();
        } else {
            removeSearchBtn.hide();
        }
        removeSearchBtn.on("click", function() {
            _self.clearSimpleSearchFilters();
            tabObj.reload.apply(tabObj, options.reload_args);
        });
        simpleSearchForm.form({
            preSubmit: function() {
                var searchText = $(this).find(".search-text").val();
                _self.clearAdvanceSearchFilters();
                tabObj.simpleSearchText = searchText;
                $(this).find(".search-text").val(searchText);
                tabObj.reload.apply(tabObj, options.reload_args);
                return false;
            }
        });
        searchTool.find(".add-filter").on("click", function () {
           _self.showAdvanceSearchPopup()
        });
        searchTool.find(".remove-filter").on("click", function () {
            if (!$(this).hasClass("disabled")) {
                tabObj.advanceSearchFilter = false;
                searchTool.find(".remove-filter").addClass("disabled");
                tabObj.reload.apply(tabObj, options.reload_args);
            }
        });
    }

    var _s = SearchEvent.prototype;

    _s.showAdvanceSearchPopup = function() {
        var _self = this, tabObj = _self.tab;
        bm.editPopup(tabObj.advanceSearchUrl, $.i18n.prop("advanced.search"), tabObj.advanceSearchTitle, {searchText: tabObj.simpleSearchText}, {
            width: tabObj.advance_search_popup_width || 480,
            clazz: tabObj.advance_search_popup_clazz || '',
            events: {
                content_loaded: function() {
                    this.countryChange = bm.countryChange(this, {
                        inputClass: "medium",
                        stateLabel: "state",
                        stateName: "state",
                        noSelection: $.i18n.prop("all.states")
                    });
                    app.global_event.trigger("advanced-filter-loaded", [this])
                }
            },
            beforeSubmit: function (form, data, popup) {
                _self.clearSimpleSearchFilters();
                tabObj.advanceSearchFilter = form.serializeObject();
                popup.close();
                _self.searchTool.find(".search-text").val("");
                _self.searchTool.find(".remove-filter").removeClass("disabled");
                tabObj.reload.apply(tabObj, _self.options.reload_args);
                return false;
            }
        });
    };

    _s.clearSimpleSearchFilters = function clearSimpleSearchFilters() {
        var _self = this;
        _self.tab.simpleSearchText = "";
        _self.searchTool.find(".search-text").val("");
        if (typeof _self.tab.clearFilters == "function") {
            _self.tab.clearFilters.call(_self.tab);
        }
    };

    _s.clearAdvanceSearchFilters = function() {
        var _self = this;
        _self.tab.advanceSearchFilter = false;
        _self.searchTool.find(".remove-filter").addClass("disabled");
    };

    return {
        init: function(tabObj, searchTool, options) {
            return new SearchEvent(tabObj, searchTool, options || {})
        }
    }
})();

//region TAB GENERAL
(function () {
    var tabHeader;
    var activeTab;
    var tabBodyContainer;
    var activeTabStack = [];

    window.ptp = activeTabStack;

    $(function () {
        tabHeader = $("#tab-header-line");
        tabHeader = tabHeader.find(".one-line-scroll-content").scrollbar({
            show_vertical: false,
            show_horizontal: true,
            use_bar: false,
            visible_on: "auto",
            horizontal: {
                handle: {
                    left: tabHeader.find(".left-scroller"),
                    right: tabHeader.find(".right-scroller")
                }
            }
        });
        $(window).resize(function() {
            tabHeader.scrollbar("update", true)
        });
        tabBodyContainer = $("#workspace .content-tabs-container");
    });

    function getIndexInActiveStack() {
        var index = -1;
        var _self = this;
        $.each(activeTabStack, function(i) {
            if (this.id == _self.id) {
                index = i;
                return false;
            }
        });
        return index;
    }

    function deActive() {
        this.header.removeClass("current");
        this.body.hide();
        this.trigger("deActive");
        activeTab = null;
    }

    function bringInView() {
        var hLeft = this.header.position().left;
        var sLeft = tabHeader.scrollLeft();
        if (hLeft < 0) {
            tabHeader.animate({scrollLeft: sLeft + hLeft - 100}, 200);
        } else {
            var hRight = hLeft + this.header.outerWidth(true);
            if (hRight > tabHeader.width()) {
                tabHeader.animate({scrollLeft: hRight - tabHeader.width() + sLeft + 100}, 200);
            }
        }
    }

    function buildTabHeader() {
        var iconBlock = "";
        if (this.icon) {
            iconBlock = "<span class='icon'></span>";
        }
        var header = $('<span class="tab-header ' + this.ui_class + '">' + iconBlock + '<span class="title"></span><span class="close">&nbsp;</span></span>');
        var title = header.find(".title");
        title.text(this.text).attr("title", bm.htmlEncode(this.tip));
        attachHeaderEvent.call(this, header);
        tabHeader.append(header);
        this.header = header;
        tabHeader.scrollbar("update", true)

    }

    function attachHeaderEvent(header) {
        var _self = this;
        header.bind("click." + this.id, function (ev) {
            var tag = $(ev.target);
            if (tag.is(".close")) {
                _self.close(1);
            } else {
                _self.setActive();
            }
        });
        header.find(".title").tooltipster();
    }

    function loadAjaxContents(data) {
        this.body.addClass("tab-loading").append('<span class="loader"></span>');
        var _self = this;
        bm.ajax({
            url: this.ajax_url,
            dataType: "html",
            data: this.ajax_data instanceof Function ? this.ajax_data(data) : this.ajax_data,
            response: function() {
                _self.body.removeClass("tab-loading");
            },
            success: function (resp) {
                _self.body.removeClass("tab-loading");
                _self.body.html(resp);
                _self.body.updateUi();
                _self.init(data);
            }, error: function (a, b, resp) {
                _self.body.removeClass("tab-loading");
                _self.body.html(resp).addClass("ajax-loading-error");
                _self.header.addClass("ajax-loading-error");
                if (_self.error) {
                    _self.error();
                }
            }
        });
    }

    function buildTabBody() {
        if (!this.id) {
            this.id = bm.getUUID();
        }
        this.body = $('<div id="' + this.id + '"></div>');
        if(this.ui_body_class) {
            this.body.addClass(this.ui_body_class)
        }
        tabBodyContainer.append(this.body.hide());
    }

    /**
     * @param params possible params are {ribbon - (optional)[HTML Element] the ribbon that is attached to this tab <br>
     * id - (optional)[string] An id for the tab body <br>
     * url - (optional)[string] If url provided then content will be loaded at render
     * clazz - [object reference having init method with signature (WCTab tab, object data)] the class which will process tab content <br>
     * title - [string] title that will be placed as tab header label <br>
     * tip - (optional)[string] This text will be shown as tooltip on hover of tab header <br>
     * icon - (optional)[string] icon url(must start with http or https) or class name that will be used as tab header icon <br>
     * data - (optional)[object] this object will be passed to clazz init method as second argument}
     */
    app.Tab = function(params) {
        if (!params) {
            return;
        }
        this.icon = true;
        $.extend(this, params);
        if (!this.tip) {
            this.tip = this.text;
        }
        this.events = $({});
        if (this.body && this.header) {
            this.body.empty();
            this.body.data("tab-inst", this);
        }
        this._create_forms = {};
        this.dirty = false;
    };

    app.Tab.changeView = function (tab, base, type, view) {
        tab.trigger("before-change-view", [type]);
        var baseInst = app.tabs[base].prototype;
        if(view && !(app.tabs[base][type].prototype instanceof app[view] && baseInst instanceof app[view])) {
            app.tabs[base].inherit(app[view]);
            app.tabs[base][type].inherit(app.tabs[base])
        }
        var processor = app.tabs[base][type];
        if(app.tabs[base].ribbon_data) {
            app.tabs[base].ribbon_data.processor = processor
        }
        app.global_event.off("." + tab.id);
        tab.off("." + tab.id);
        var dirtyState = undefined;
        if(tab.hasOwnProperty("dirty") && tab.dirty) {
            tab.clearDirty();
            dirtyState = true
        }
        var newTab = new processor($.extend((tab.constructor_args ? tab.constructor_args[0] : {}), {
            header: tab.header,
            body: tab.body,
            data: tab.data
        }));
        tab.header.find("*").addBack().off();
        var title = tab.header.find(".title");
        title.tooltipster("destroy");
        title.text(newTab.text).attr("title", bm.htmlEncode(newTab.tip));
        attachHeaderEvent.call(newTab, newTab.header);
        app.Tab.eventCopy(tab, newTab);
        if(activeTab == tab) {
            activeTab = newTab
        }
        newTab.render();
        if(dirtyState && newTab.hasOwnProperty("dirty")) {
            newTab.unsaved = true
        }
        return newTab
    };

    var _t = app.Tab.prototype;

    _t.reload = function(data) {
        this.body.addClass("tab-loading").append('<span class="loader"></span>');
        var _self = this;
        bm.ajax({
            url: this.ajax_url,
            dataType: "html",
            data: this.ajax_data instanceof Function ? this.ajax_data(data) : this.ajax_data,
            response: function() {
                _self.body.removeClass("tab-loading");
            },
            success: function (resp) {
                _self.body.html(resp);
                _self.body.updateUi();
                _self.clearDirty();
                _self.reinit(data);
            }
        });
    };

    _t.reload.virtual = true;

    _t.reinit = function() {
        var _self = this;
        var toolbar = this.body.find(".toolbar");
        var actionMenu = toolbar.find(".action-menu");
        function onOpen(navigator) {
            if(_self.onActionMenuOpen) {
                _self.onActionMenuOpen(this, navigator)
            }            
        }
        if(actionMenu.length) {
            _self.action_menu = bm.menu(_self.action_menu_entries, actionMenu, null, {
                click: $.proxy(_self, "onActionMenuClick"),
                open: onOpen,
                hide: $.proxy(_self, "onActionMenuHide")
            }, "click", ["right bottom+7", "right top"]);
        }
        var crateMenu = toolbar.find(".create.menu");
        if(crateMenu.length) {
            _self.create_menu = bm.menu(_self.create_menu_entries, crateMenu, null, {
                click: $.proxy(_self, "onCreateMenuClick"),
                open: onOpen,
                hide: $.proxy(_self, "onActionMenuHide")
            }, "click", ["right bottom+7", "right top"]);
        }
        var switchMenu = toolbar.find(".switch-menu");
        if(switchMenu.length) {
            _self.switch_menu = bm.menu(_self.switch_menu_entries, switchMenu,  null, {
                click: $.proxy(_self, "onSwitchMenuClick"),
                open: onOpen,
                hide: $.proxy(_self, "onActionMenuHide")
            }, "click", ["right bottom+7", "right top"]);
        }
        this.body.find(".header .toolbar-item.reload").click(function () {
            _self.reload();
        });
        this.body.addClass(this.ui_class);
    };

    _t.getTitle = function() {
        return this.text;
    };

    _t.setTitle = function(text) {
        this.text = text;
        this.header.find(".title").text(this.text)
    };

    _t.setName = function(name) {
        this.name = name;
        var nameBox = this.header.find(".entity-name");
        if(!nameBox.length) {
            this.header.find(".title").append(" - <span class='entity-name'></span>");
            nameBox = this.header.find(".entity-name")
        }
        nameBox.text(this.name)
    };

    _t.setData = function() {
        throw $.error("Data setting not supported in this tab")
    };

    _t.setData.virtual = true;

    _t.render = function(data) {
        this.trigger("render");
        if(this.header) {
            var title = this.header.find(".title").text(this.text);
            tabHeader.scrollbar("update", true);
            this.body.attr("class", this.ui_body_class || "");
            title.tooltipster("content", this.tip);
        }
        if (!this.isRendered()) {
            buildTabHeader.call(this);
            buildTabBody.call(this);
        }
        this.body.data("tab-inst", this);
        this.body.addClass("app-tab-container");
        if (this.name) {
            this.header.find(".title").append(" - <span class='entity-name'></span>");
            this.header.find(".entity-name").text(this.name);
        }
        if (this.ajax_url) {
            loadAjaxContents.call(this, data);
        } else {
            if (typeof this.init == "function") {
                this.init(data);
            }
        }
    };

    _t.on_global = function(eventName, handler) {
        if($.isArray(eventName)) {
            eventName = eventName.join("." + this.id + " ") + "." + this.id;
        } else {
            eventName = eventName + "." + this.id;
        }
        app.global_event.on(eventName, handler);
    };

    _t.one_global = function(eventName, handler) {
        if($.isArray(eventName)) {
            eventName = eventName.join("." + this.id + " ") + "." + this.id;
        } else {
            eventName = eventName + "." + this.id;
        }
        app.global_event.one(eventName, handler);
    };

    _t.off_global = function(eventName) {
        eventName = eventName + "." + this.id;
        app.global_event.off(eventName);
    };

    _t.getId = function() {
        return this.id;
    };

    _t.updateTitle = function(title) {
        this.header.find("a.title").html(title);
    };

    _t.updateTip = function(headertip) {
        this.header.attr("title", headertip);
    };

    _t.on = function() {
        arguments[1] = $.proxy(arguments[1], this);
        $.fn.on.apply(this.events, arguments);
        return this;
    };

    _t.one = function() {
        arguments[1] = $.proxy(arguments[1], this);
        $.fn.one.apply(this.events, arguments);
        return this;
    };

    _t.off = function() {
        $.fn.off.apply(this.events, arguments);
        return this;
    };

    _t.trigger = function() {
        return $.fn.trigger.apply(this.events, arguments);
    };

    _t.setActive = function() {
        if (activeTab == this) {
            return false;
        }
        if (activeTab) {
            if (this.trigger("beforeDeactive") === false) {
                return false;
            }
            deActive.call(activeTab);
        }
        this.header.addClass("current");
        this.body.show();
        var activeIndex = getIndexInActiveStack.call(this);
        if (activeIndex > -1) {
            activeTabStack.splice(activeIndex, 1);
        }
        activeTabStack.push(this);
        activeTab = this;
        bringInView.call(this);
        this.trigger("active");
    };

    _t.isRendered = function() {
        return !!this.header;
    };

    _t.init = function() {
        if(this.unsaved) { // initialy unsaved means dirty state not rendered
            this.setDirty()
        }
        this.reinit();
        this.on("close", function() {
            this.off();
        })
    };

    /**
     * @param status [number] closing status code
     */
    _t.confirmCloseIfDirty = function(status) {
        var _self = this;
        if (this.dirty) {
            bm.confirm($.i18n.prop("there.unsaved.content.wanna.save"), function () {
                _self.save(function () {
                    _self.close(status);
                })
            }, function () {
                _self.clearDirty();
                _self.close(status);
            }, function () {});
            return false;
        }
        return true
    };

    _t.close = function(status) {
        var _self = this;
        if(_self.confirmCloseIfDirty(status)) {
            if (status != -1 && !this.isRendered()) {
                return false;
            }
            if (status != -1) {
                if (this.trigger("beforeClose", [status]) === false) {
                    return false;
                }
            }
            app.global_event.off("." + this.id);
            $(window).off("." + this.id);
            this.header.remove();
            tabHeader.scrollbar("update", true);
            this.body.remove();
            this.header = null;
            this.trigger("close", [status]);
            $.each(_self._create_forms, function(k, form) {
                if(form) {
                    form.trigger("close");
                }
            });
            var activeIndex = getIndexInActiveStack.call(this);
            if (activeIndex > -1) {
                activeTabStack.splice(activeIndex, 1);
            }
            if (activeTab == this) {
                activeTab = null;
                var nextActive = activeTabStack[activeTabStack.length - 1];
                if (nextActive) {
                    nextActive.setActive();
                }
            }
            this.isClosed = true;
            this.off();
            return true;
        }
        return false

    };

    _t.save = function(callback){
        var _self = this;
        $.each(_self._create_forms, function(k, form) {
            if(form) {
                form.form("submit", {
                    success: function() {
                        _self.clearDirty();
                        callback();
                    }
                })
            }
        });
    };

    _t.isActive = function() {
        return this.header.is(".current");
    };

    _t.createPanelTemplate = $('<div class="embedded-edit-form-panel create-panel fade-in-up"><div class="header"><span class="header-title"></span><span class="toolbar toolbar-right"><span class="tool-group toolbar-btn save">' + $.i18n.prop("save")+ '</span><span class="tool-group toolbar-btn cancel">' + $.i18n.prop("cancel") + '</span></span></div><div class="body"></div></div>');

    _t.clearDirty = function() {
        this.dirty = false;
        this.header.find(".dirty-mark").remove();
        this.trigger("dirty", [false]);
    };

    _t.setDirty = function() {
        if (!this.dirty) {
            this.dirty = true;
            this.header.find(".title").before("<sup class='dirty-mark'>* </sup>");
            this.trigger("dirty", [true]);
        }
    };

    _t.isDirty = function() {
        return this.dirty;
    };

    _t.renderCreatePanel = function(url, title, emphasized, data, config) {
        var _self = this, _caller_content_loaded = config.content_loaded;
        var uuid = bm.getUUID();
        var template = config.createPanelTemplate ? config.createPanelTemplate : _self.createPanelTemplate.clone();
        _self.body.loader();
        title = '<span class="title">' + title + '</span>';
        title = emphasized ? (title + ' - <span class="emphasized"> ' + bm.htmlEncode(emphasized) + '</span>') : title;
        config = $.extend({}, {
            auto_clear_dirty: true,
            auto_close_on_success: true,
            disable_on_submit: true,
            disable_on_invalid: true,
            disable_button_text: undefined,
            submit_n_cancle: true,
            modify_ui: true,
            scrollable: true
        }, config);
        function closeCreatePanel() {
            if(config.auto_clear_dirty) {
                _self.clearDirty();
            }
            _self._create_forms[uuid].trigger("close");
            template.remove();
            _self.body.addClass("fade-in-down");
            setTimeout(function() {
                _self.body.removeClass("fade-in-down");
            }, 700);
            delete _self._create_forms[uuid];
        }
        var content_loaded = function (content) {
            content.updateUi();
            var _success = (config.ajax && config.ajax.success) || config.success;
            var form = content.find("form").not(".search-form"), submitButtons = form.find(".submit-button");
            if(form.length > 0) {
                form.attr("class").split(" ").every(function() {
                    var button = submitButtons.filter("." + this + "-submit");
                    if(button.length) {
                        submitButtons = button;
                        return false;
                    }
                });
            }
            if(!_self._create_forms) {
                _self._create_forms = {};
            }
            _self._create_forms[uuid] = form;
            var toolbarSubmitBtn = template.find(".toolbar-btn.save");
            if(config.toolbar_btn_text) {
                toolbarSubmitBtn.text(config.toolbar_btn_text)
            }
            form = form.form({
                submitButton: toolbarSubmitBtn.add(submitButtons),
                ajax: content.find(".edit-popup-form").attr("no-ajax") == null,
                disable_on_submit: config.disable_on_submit,
                disable_on_invalid: config.disable_on_invalid,
                disable_button_text: config.disable_button_text,
                preSubmit: function (ajaxSettings) {
                    $.extend(ajaxSettings, {
                        response: config.response,
                        error: config.error
                    }, config.ajax, {
                        success: function() {
                            if (config.auto_close_on_success) {
                                closeCreatePanel()
                            }
                            if (_success) {
                                _success.apply(this, arguments);
                            }
                        }
                    });
                    if (config.beforeSubmit) {
                        return config.beforeSubmit.call(content, form, ajaxSettings ? (ajaxSettings.data = ajaxSettings.data || {}) : null, template);
                    }
                }
            });
            content.submit = function() {
                form.form("submit");
            };
            form.find("[default-focus]").focus();
            var tabs = form.find(".bmui-tab:first");
            if(tabs.length) {
                tabs.find("input,textarea,select").on("invalid", function(ev, field) {
                   tabs.tabify("activate", field.validator.elm)
                })
            }
            if(config.submit_n_cancle) {
                content.find(".header .cancel,.cancel-button").click(function () {
                    closeCreatePanel()
                });
                content.find(".header .save").on("click", function() {
                    content.submit();
                })
            }
            content.on("content-change", function (ev, added, removed) {
                if (added) {
                    form.obj(ValidationPanel).attach(added.filter("[validation]").add(added.find("[validation]")));
                }
                if (removed) {
                    form.obj(ValidationPanel).detach(removed.filter("[validation]").add(removed.find("[validation]")));
                }
            });
            form.on("trash-restore", function () {
                closeCreatePanel();
            });
            if (_caller_content_loaded) {
                if(content.find(".bmui-tab.create-edit-panel").length) {
                    _caller_content_loaded.apply(content, [template]);
                } else {
                    _caller_content_loaded.apply(content, [form, template]);
                }
            }
            form.on("change",function() {
                _self.setDirty();
            })
        };

        function processContent(content) {
            template.find(".body").html(content);
            template.find(".header .header-title").html(title);
            _self.body.append(template);
            if(config.scrollable) {
                template.find(".body").scrollbar();
            }
            content_loaded(template);
            template.close = template.closePanel = closeCreatePanel;
        }
        if(url == undefined) {
            processContent(config.content);
            this.body.loader(false)
        } else {
            bm.ajax({
                url: url,
                data: data,
                dataType: "html",
                success: function(resp) {
                    var content = $(resp);
                    processContent(content)
                },
                error: function(xhr, status, resp) {
                    bm.notify(resp.textify(), "error")
                },
                complete: function() {
                    _self.body.loader(false)
                }
            })
        }
    };

    app.Tab.getTab = function(id) {
        return tabBodyContainer.find("> #" + id).obj(app.Tab);
    };

    app.Tab.eventCopy = function(from, to) {
        from.events.copyEvents(to.events);
    }
})();
//endregion

//region SINGLE TABLE
(function() {
    app.SingleTableView = function() {};

    var _s = app.SingleTableView.prototype;

    function bindEvents() {
        var _self = this;
        app.tab_utils.searchEvent.init(_self, _self.body);
        if (this.body.find("table.content .select-column :checkbox").length > 0) {
            bm.tableCheckAll(this.body.find("table"), function() {
                _self.body.find(".action-header").show();
            }, function() {
                _self.body.find(".action-header").show();
            }, function() {
                _self.body.find(".action-header").hide();
            });
            this.body.find(".action-header .action-on-selection").change(function () {
                var selecteds = _self.getSelectedEntities()
                var action = $(this).val();
                _self.onSelectedActionClick(action, selecteds);
                $(this).chosen("val", "");
            });
        }
    }

    _s.getSelectedEntities = function() {
        var selected = this.body.find("td.select-column :checkbox:checked, td.select-column :radio:checked");
        var selecteds = [];
        selected.each(function () {
            selecteds.push($(this).config("entity"))
        });
        return selecteds;
    };

    _s.init = function() {
        var _self = this;
        this.table = this.body.find(this.isNonTable ? ">.app-tab-content-container>.body" : ">.app-tab-content-container>table");
        var container = this.body.addClass("table-view");
        this.tabulator = bm.table(container, $.extend({
            url: this.ajax_url,
            isNonTable: _self.isNonTable, // will remove later
            menu_entries: this.menu_entries,
            beforeReloadRequest: function () {
                if (_self.beforeReloadRequest) {
                    _self.beforeReloadRequest(this.param)
                }
            },
            afterLoad: function() {
                _self.body.find(".action-header:visible").hide();
                _self.table.tscrollbar("content");
                if(_self.simpleSearchText) {
                    _self.body.find(".search-form .remove-search").show();
                } else {
                    _self.body.find(".search-form .remove-search").hide();
                }
                if (_self.afterTableReload) {
                    _self.afterTableReload()
                }
            },
            afterCellEdit: this.afterCellEdit ? $.proxy(this.afterCellEdit, this) : null,
            selectableCellSelect: this.selectableCellSelect,
            afterCellSelect: this.afterCellSelect ? $.proxy(this.afterCellSelect, this) : null,
            beforeCellSelect: this.beforeCellSelect ? $.proxy(this.beforeCellSelect, this) : null
        }, this.sortable ? {
            sortable: this.sortable.list,
            sorted: this.sortable.sorted,
            sortedDir: this.sortable.dir
        } : {}));
        this.tabulator.onMenuOpen = $.proxy(this.onMenuOpen, this);
        this.tabulator.onActionClick = $.proxy(this.onActionClick, this);
        this.table.tscrollbar();
        bindEvents.call(this);
    };

    _s.reload = function() {
        this.tabulator.reload();
    };

    _s.reload.override = true;

    _s.beforeReloadRequest = function (param) {
        var searchFilter = {searchText: this.simpleSearchText};
        if (this.advanceSearchFilter !== false) {
            searchFilter = this.advanceSearchFilter;
        }
        $.extend(param, searchFilter);
    };

    _s.destroy = function() {
    };

    var _super;

    app.SingleTableTab = function() {
        _super.constructor.apply(this, arguments);
    };

    app.SingleTableTab.inherit(app.Tab, app.SingleTableView);
    _super = app.SingleTableTab._super;
})();
//endregion

//region TWO PANEL
(function () {
    var _super;
    app.TwoPanelResizeable = function () {
        _super.constructor.apply(this, arguments);
    };
    app.TwoPanelResizeable.inherit(app.Tab);
    _super = app.TwoPanelResizeable._super;

    var _t = app.TwoPanelResizeable.prototype;

    function bindEvents() {
        this.on("before-change-view." + this.id, function() {
            destroyStuff.call(this)
        });
        this.attachModernUiPanel();
    }

    _t.attachModernUiPanel = function() {
        bm.attachModernUiPanel.call(this);
    };

    function destroyStuff() {
        if(!this.resize_disabled) {
            this.scroller.draggable("destroy")
        }
    }

    _t.init = function () {
        var _self = this;
        var left_position;

        this.scroller = this.body.find(".wcui-resizable-layout-scroller");
        var left_panel = this.body.find(".left-panel");
        _super.init.call(this);
        this.body.find(".app-tab-content-container").addClass("two-panel-resizable" + (this.resize_disabled ? " without-resize-bar" : ""));
        function setLeftWidth() {
            left_panel.css({
                flexBasis: bm.innerWidth(left_panel, _self.left)
            })
        }
        setLeftWidth();
        if (!this.left_boundary) {
            this.left_boundary = [100, Number.MAX_VALUE]
        }
        if (!this.right_boundary) {
            this.right_boundary = [100, Number.MAX_VALUE]
        }
        if (this.shim_required) {
            this.shim = $("<div class='iframe-shim'></div>").appendTo(document.body).hide();
        }
        if(!this.resize_disabled) {
            this.scroller.draggable({
                axis: "x",
                containment: [0, 0, 0, 0],
                start: function (ev, ui) {
                    left_position = ui.position.left;
                    if(_self.shim_reqired) {
                        _self.shim.show();
                    }
                },
                drag: function (ev, ui) {
                    var increase = ui.position.left - left_position;
                    left_position = ui.position.left;
                    _self.left += increase;
                    setLeftWidth()
                },
                stop: function() {
                    if (_self.shim_required) {
                        _self.shim.hide();
                    }
                }
            });
            this.scroller.css({
                left: left_panel.position().left + left_panel.outerWidth()
            });
            function updateContainment() {
                var combined_width = _self.body.width();
                var scroller_position = _self.scroller.position().left;
                var min1 = scroller_position - _self.left + _self.left_boundary[0];
                var max1 = scroller_position - _self.left + _self.left_boundary[1];
                var min2 = scroller_position + combined_width - _self.left - _self.right_boundary[1];
                var max2 = scroller_position + combined_width - _self.left - _self.right_boundary[0];
                _self.scroller.draggable("option", "containment", [Math.max(min1, min2), 0, Math.min(max1, max2), 0])
            }

            $(window).on("resize." + this.id, function() {
                updateContainment()
            });
            updateContainment()
        }

        bindEvents.call(this);
    };

    _t.close = function() {
        if(this.confirmCloseIfDirty()) {
            try {
                destroyStuff.call(this)
            } catch (ex){}
            return _super.close.call(this);
        }
        return false
    }
})();
//endregion

//region TAB PANEL
(function() {
    var _super;
    function editorPanel(tab, panelData) {
        var editor = this,
            panel = panelData.panel,
            header = panelData.tab,
            index = panelData.index;
        panel.isDirty = false;
        panel.reload = function(urlParam) {
            panel.clearDirty();
            var url = bm.path(header.attr("data-tabify-url"));
            if(urlParam) {
                $.extend(url.query, urlParam)
            }
            url = header[0].load_url = url.full();
            if(urlParam) {
                tab.tabify("reload", index, url);
            } else {
                tab.tabify("reload", index);
            }
        };
        panel.setDirty = function() {
            if(editor.notEditable || panel.isDirty) {
                return;
            }
            panel.isDirty = true;
            header.find(".title").before("<sup class='dirty-mark'>* </sup>");
            editor.setDirty();
        };
        panel.clearDirty = function() {
            if(!panel.isDirty){
                return
            }
            panel.isDirty = false;
            header.find(".dirty-mark").remove();
            var isDirty = false;
            $.each(editor.panels, function(key, panel){
                isDirty = isDirty || panel.isDirty;
            });
            if(!isDirty){
                editor.clearDirty();
            }
        };
        panel.change(function(ev) {
            if(editor.notEditable) {
                return
            }
            if(ev.ignore || (ev.originalEvent && ev.originalEvent.ignore)) {
                return;
            }
            panel.setDirty();
        });
        return panel;
    }

    app.MultiTab = function() {
        this.panels = {};
        this.tabs = {};
        this.tab_objs = {};
        _super.constructor.apply(this, arguments)
    };
    app.MultiTab.inherit(app.Tab);
    _super = app.MultiTab._super;

    var _t = app.MultiTab.prototype;

    function bindSearchEvents(data) {
        var _self = this;
        var _tab = _self.tab_objs[data.index];
        var tool = data.panel.tool;
        if(tool) {
            app.tab_utils.searchEvent.init(_tab, tool);
            var panel = data.panel;
            if (panel.find("table.content .select-column :checkbox").length > 0) {
                bm.tableCheckAll(panel.find("table"), function() {
                    tool.find(".action-header").show();
                }, function() {
                    tool.find(".action-header").show();
                }, function() {
                    tool.find(".action-header").hide();
                });
                tool.find(".action-header .action-on-selection").change(function () {
                    var selected = panel.find("td.select-column :checkbox:checked");
                    var selecteds = [];
                    selected.each(function () {
                        selecteds.push($(this).config("entity").id)
                    });

                    var action = $(this).val();
                    _tab.onSelectedActionClick(action, selecteds);
                    $(this).chosen("val", "");
                });
            }
        }

    }

    _t.save = function(callback) {
        var _self = this;
        function onComplete(handle) {
            if (!_self.isDirty()) {
                if(callback) {
                    callback()
                }
            } else {
                handle.next()
            }
        }
        bm.iterate(this.panels, function (handle, index) {
            var panel = this;
            if(panel.isDirty) {
                if(panel.save) {
                    panel.save(function() {
                        onComplete(handle)
                    });
                    return;
                }
                panel.find("form").form("submit", {
                    ajax: {
                        show_success_status: false
                    },
                    success: function() {
                        panel.clearDirty();
                        onComplete(handle)
                    },
                    error: function() {
                        _self.setActiveTab(index)
                    },
                    invalid: function() {
                        _self.setActiveTab(index);
                        panel.find("form").valid("position")
                    }
                })
            } else {
                onComplete(handle)
            }
        })
    };

    _t.render = function(data) {
        if (data && data.active) {
            var url = bm.path(this.ajax_url);
            url.query.active = data.active;
            this.ajax_url = url.full()
        }
        _super.render.apply(this, arguments)
    };

    _t.setActiveTab = function (index) {
        var tab = this.body.find(".bmui-tab");
        tab.tabify("activate", index);
    };

    _t.setData = function(data) {
        if(data && data.active) {
            this.setActiveTab(data.active);
        }
    };

    _t.init = function() {
        var _self = this;
        _super.init.apply(this, arguments);
        var toolbar = this.toolbar = this.body.find(".multi-tab-shared-header, .header");
        var toolbarHeader =  toolbar.find(".header-title");
        var toolbarRight = toolbar.find(".toolbar-right");
        var headerWrapper = this.body.find(".bmui-tab-header-container");
        var bodyWrapper = this.body.find(".bmui-tab-body-container");
        var tab = this.body.find(".bmui-tab");
        this.body.find(".toolbar .save-all").on("click", function() {
            if(_self.isDirty()) {
                _self.save(function() {
                    _self.close()
                })
            } else {
                _self.close()
            }
        });
        this.body.find(".toolbar .cancel").on("click", function() {
            _self.close(1)
        });

        tab.on("tab:load", function (ev, data) {
            _self.tabs[data.index] = data.panel;
            _self.panels[data.index] = editorPanel.call(_self, tab, data);
            if(data.panel.tool) {
                data.panel.tool.remove()
            }
            var toolBar = data.panel.find(".toolbar-share");
            var headerTitle = toolBar.find(".header-title");
            var toolGroupRight = toolBar.find(".toolbar.toolbar-right").clone();
            if(headerTitle.length) {
                data.panel.headerTitle = headerTitle.text();
            }
            if(toolGroupRight) {
                if(toolbarRight.length) {
                    toolGroupRight.removeClass("toolbar-right");
                    if(toolGroupRight.is(".before")) {
                        toolbarRight.prepend(toolGroupRight)
                    } else {
                        toolbarRight.append(toolGroupRight)
                    }
                } else {
                    toolbar.append(toolGroupRight)
                }
                toolGroupRight.updateUi();
                data.panel.tool = toolGroupRight;
                toolGroupRight.find(".reload").on("click", function () {
                   data.panel.reload()
                });
                toolBar.remove()
            }
            if (_self.onContentLoad) {
                _self.onContentLoad(data, _self.panels[data.index]);
            }
            if(Object.keys(_self.tab_objs).length) {
                bindSearchEvents.call(_self, data);
            }
            if (_self.afterTableReload) {
                _self.afterTableReload(data, _self.panels[data.index]);
            }
            data.panel.reload = function(){
                var header = data.tab;
                header[0].load_url = header.attr("url");
                tab.tabify("reload", data.index);
                data.panel.find(".toolbar-share").remove()
            };
            if(data.panel.headerTitle) {
                toolbarHeader.text(data.panel.headerTitle);
            } else {
                toolbarHeader.text(data.tab.text().trim());
            }
            data.panel.find(".table-view").on("tabulator-load-success", function(evt, resp) {
                if(resp.find(".header-title").length) {
                    data.panel.headerTitle = resp.find(".header-title").text();
                    toolbarHeader.text(data.panel.headerTitle);
                }
            })
        }).on("tab:error", function (ev, data) {
            if (_self.onContentError) {
                _self.onContentError(data);
            }
        }).on("tab:activate", function (ev, data) {
            if(data.oldPanel.tool) {
                data.oldPanel.tool.chide();
            }
            var toolGroup = data.newPanel.tool;
            if(toolGroup) {
                toolGroup.cshow();
            }
            _self.currentPanel = data.newPanel;
            if(_self.onTabActive) {
                _self.onTabActive(data);
            }
            if(data.newPanel.headerTitle) {
                toolbarHeader.text(data.newPanel.headerTitle);
            } else if(data.tab) {
                toolbarHeader.text(data.tab.text().trim());
            }
        });
        if(this.targetTab) {
            this.setActiveTab(this.targetTab)
        }
        var hWrapper = headerWrapper.add(bodyWrapper);
        if(!this.disableScroll) {
            hWrapper.scrollbar({
                vertical: {
                    offset: -2
                }
            });
        }
    };

    _t.reload = function() {
        var _self = this;
        var tabWrapper = _self.body.find(".bmui-tab");
        _self.tab_objs[tabWrapper.tabify("option", "active")].reload();
    }
})();
//endregion

//region EXPLORER 1
(function () {
    var _super;

    function getExplorer(gridPanel, treePanel, _config) {
        var config = {
            url: '',
            appendUrl: null,
            type: 'get',
            treeOptions: {},
            parent_key: "id"
        };

        var current_parent = 0;
        var current_parent_type;
        var _self = this;

        var paginator = gridPanel.find(".pagination").obj(),
            gridBody = gridPanel.find(".body"),
            treeBody = treePanel.find(".body");
        var explorer = $.extend({}, {
            paginator: paginator,
            reload: function (isRight) {
                if (isRight) {
                    gridBody.scrollbar("destroy");
                    paginator.onPageClick(paginator.getCurrentPage());
                } else {
                    treeBody.scrollbar("destroy");
                    loadLeftPanel()
                }
            },
            setParent: function (newParent, type) {
                current_parent = newParent;
                if (typeof type != "undefined") {
                    current_parent_type = type;
                }
                paginator.setCurrentPage(0);
                this.reload(true);
            },
            getCurrentParent: function() {
                return current_parent;
            }
        });

        var menu_opened_from_tree = false;
        explorer.menu = bm.menu(_config.menu_entries, gridBody, ".float-menu-navigator", {
            open: function(entity) {
                if(entity.parent().is(".grid-item")) {
                    entity = entity.parent();
                    entity.addClass("float-menu-opened");
                    menu_opened_from_tree = false;
                    var data = entity.config("content");
                    var type = data.type || _self.tree_node_type;
                    if(explorer.onMenuOpen) {
                        explorer.onMenuOpen(type, data, entity);
                    }
                }
            },
            hide: function(entity) {
                entity = menu_opened_from_tree ? entity.parent(".tree-node") : entity.parent(".grid-item");
                entity.removeClass("float-menu-opened");
            },
            click: function(type, action, entity) {
                var data;
                if(menu_opened_from_tree) {
                    data = treeOptions.treeObj.data;
                    entity = undefined
                } else {
                    data = entity.parent(".grid-item").config("content");
                }
                explorer.onActionClick(type, action, data, entity)
            },
            multitype: true
        }, "click", ["center bottom", "right+21 top+2"]);

        var treeInitialized = false, firstTime = true;
        var treeOptions = {
            auto_load_lazy_nodes: true,
            tree_root_data: _self.root_node_name ? {id: 0, name: _self.root_node_name, type: 'grand-root', icon: false} : undefined,
            load_url: _self.tree_node_load_url,
            node_type: _self.tree_node_type,
            type_prop: _self.tree_node_type_prop,
            onRender: function(tree, node) {
                var $node = $(node);
                var contextDropper = $('<span class="float-menu-navigator"></span>');
                contextDropper.on("click", function(evt) {
                    $node.addClass("float-menu-opened");
                    var type = tree.data.type || _self.tree_node_type;
                    menu_opened_from_tree = true;
                    treeOptions.treeObj = tree;
                    explorer.menu[type].show($(this), evt);
                    if(explorer.onMenuOpen) {
                        explorer.onMenuOpen(type, tree.data, tree);
                    }
                    return false;
                });

                if($node.is(".grand-root")) {
                    if(treeOptions.grand_root_context) {
                        $node.append(contextDropper);
                    }
                } else {
                    $node.append(contextDropper);
                }
            },
            onActivate: function(node) {
                explorer.setParent(node.data[config.parent_key], node.data.type)
            }
        };

        if(_self.tree_options) {
            $.extend(treeOptions, _self.tree_options)
        }

        function loadLeftPanel() {
            var canonical_selection = [0];
            if (treeInitialized) {
                canonical_selection = treeBody.tree("inst").getCanonicalActive();
                treeBody.tree("destroy");
                treeInitialized = false
            }
            treeBody.loader();
            if(_self.onLazyRead) {
                _self.onLazyRead(function(children){
                    buildTree(children);
                });
            } else {
                bm.ajax({
                    url: _self.tree_node_load_url,
                    response: function () {
                        treeBody.removeClass("updating").loader(false)
                    },
                    success: function (x) {
                        buildTree(x);
                    }
                });
            }

            function buildTree(x) {
                treeBody.removeClass("updating").loader(false)
                var options = $.extend(treeOptions, config.treeOptions, {children: x});
                if(_self.beforeBuildTree) {
                    _self.beforeBuildTree(options);
                }
                treeBody.tree(options);
                if(canonical_selection.length) {
                    treeBody.tree("inst").setCanonicalActive(canonical_selection, {
                        fail: function () {
                            treeBody.tree("inst").activateKey("0", firstTime)
                        }
                    }, firstTime)
                }
                treeInitialized = true;
                firstTime = false;
                treeBody.scrollbar({
                    vertical: {
                        offset: 8
                    }
                });
                treeBody.loader(false);
            }
        }
        if(!_self.tree_disabled) {
            loadLeftPanel();
        }

        function _sendReloadRequest() {
            config.param = {max: paginator.all ? -1 : paginator.itemsPerPage, offset: (paginator.currentPage - 1) * paginator.itemsPerPage};
            if (current_parent != 0) {
                config.param.id = current_parent;
            }
            if(typeof current_parent_type != "undefined"){
                config.param.type = current_parent_type;
            }
            if (config.sortable && config.sorted) {
                $.extend(_config.param, {
                    sort: config.sortable[config.sorted],
                    dir: config.sortedDir == "down" ? "desc" : "asc"
                });
            }
            if (config.beforeReloadRequest) {
                config.beforeReloadRequest();
            }
            gridBody.loader();
            bm.ajax({
                url: config.url + (config.appendUrl || ""),
                type: config.type,
                data: config.param,
                dataType: 'html',
                success: onReloadSuccess,
                response: function () {
                    gridBody.removeClass("updating");
                    gridBody.loader(false);
                },
                complete: function () {
                    if (config.afterLoad) {
                        config.afterLoad();
                    }
                }
            });
        }

        $.extend(config, _config);

        gridPanel.find(".per-page-count").change(function (ev, oldValue, newValue) {
            paginator.update(undefined, +newValue, 1);
            _sendReloadRequest();
        });
        if(paginator) {
            paginator.onPageClick = _sendReloadRequest;
        }
        function initRightScroll() {
            gridBody.scrollbar({
                vertical: {
                    offset: 4
                }
            });
        }
        function onReloadSuccess(resp) {
            resp = $(resp);
            gridBody.html(resp.find(".body").html()).updateUi();
            gridPanel.find(".header .title").replaceWith(resp.find(".header .title"));
            var paginatorTag = resp.find("paginator");
            var total = +paginatorTag.attr("total");
            var max = +paginatorTag.attr("max");
            var offset = +paginatorTag.attr("offset");
            paginator.update(total, max, Math.floor(offset / max) + 1);
            initRightScroll()
        }
        initRightScroll();
        return explorer;
    }

    app.ExplorerPanelTab = function () {
        _super.constructor.apply(this, arguments);
    };

    var _e = app.ExplorerPanelTab.inherit(app.TwoPanelResizeable);
    _super = app.ExplorerPanelTab._super;

    _e.left = 300;
    _e.left_boundary = [300, 800];
    _e.right_boundary = [300, Number.MAX_VALUE];
    _e.treeOptions = {};

    _e.init = function () {
        var _self = this;
        _super.init.call(this);
        this.treePanel = this.body.find(".left-panel");
        this.gridPanel = this.body.find(".right-panel");
        this.paginator = this.body.find(".right-panel>.footer>.pagination").obj();
        this.explorer = getExplorer.call(this, this.gridPanel, this.treePanel, {
            url: this.explorer_url,
            parent_key: this.parent_key,
            menu_entries: this.menu_entries,
            treeOptions: _self.tree_options,
            tree_disabled: _self.tree_disabled ? _self.tree_disabled : false,
            beforeReloadRequest: function () {
                if (_self.beforeReloadRequest) {
                    _self.beforeReloadRequest(this.param)
                }
            },
            afterLoad: function () {
                if(_self.simpleSearchText) {
                    _self.body.find(".search-form .remove-search").show();
                } else {
                    _self.body.find(".search-form .remove-search").hide();
                }
                if (_self.afterTableReload) {
                    _self.afterTableReload()
                }
            }
        });
        this.explorer.onMenuOpen = this.onMenuOpen ? $.proxy(this.onMenuOpen, this) : null;
        this.explorer.onActionClick = this.onActionClick ? $.proxy(this.onActionClick, this) : null;
        this.explorer.onMoveTreeNode = this.onMoveTreeNode ? $.proxy(this.onMoveTreeNode, this) : null;
        app.tab_utils.searchEvent.init(_self, _self.body, {reload_args: [true]})
    };

    _e.reload = function (isRight) {
        this.explorer.reload(isRight);
    };

    _e.beforeReloadRequest = function (param) {
        var searchFilter = {searchText: this.simpleSearchText};
        if (this.advanceSearchFilter !== false) {
            searchFilter = this.advanceSearchFilter;
        }
        $.extend(param, searchFilter);
    }
})();
//endregion

//region MULTI SECTION
(function() {
    var _super;
    app.MultiSectionView = function () {};
    var _m = app.MultiSectionView.prototype;
    _m.sectionList = {};
    _m.init = function () {
        var _self = this;
        $.each(this.sectionList, function(key, secInfo) {
            if(_self["init" + key.capitalize() + "Section"]) {
                _self["init" + key.capitalize() + "Section"](_self.body.find("[section='" + key + "']"))
            }
        })
    };

    _m.reload = function(sectionKey) {
        var _self = this, section = _self.body.find("[section='" + sectionKey + "']");
        section.loader();
        var settings = {
            url: _self.sectionList[sectionKey].ajax_url,
            data: {},
            dataType: "html",
            success: function(resp) {
                section.html(resp);
                _self["init" + sectionKey.capitalize() + "Section"](section);
                if(_self["after" + sectionKey.capitalize() + "SectionReload"]) {
                    _self["after" + sectionKey.capitalize() + "SectionReload"]()
                }
                section.loader(false)
            }
        };
        if(_self["before" + sectionKey.capitalize() + "SectionReload"]) {
            _self["before" + sectionKey.capitalize() + "SectionReload"](settings)
        }
        bm.ajax(settings)
    };

    app.MultiSectionTab = function() {
        app.MultiSectionTab._super.constructor.apply(this, arguments)
    };

    _m = app.MultiSectionTab.inherit(app.Tab, app.MultiSectionView);
    _super = app.MultiSectionTab._super;
    _m.reload = function(sectionKey) {
        if(sectionKey == undefined) {
            _super.reload.apply(this);

        }
        app.MultiSectionView.prototype.reload.apply(this, arguments)
    }
})();
//endregion

//region EXPLORER 2
(function() {
    app.TwoPanelExplorerTab = function(params) {
        app.TwoPanelExplorerTab._super.constructor.apply(this, arguments)
    };

    _t = app.TwoPanelExplorerTab.inherit(app.Tab);
    _t.init = function() {
        var _self = this, leftPanel = this.leftPanel = this.body.find(".left-panel");
        _self.right_panel_views || (_self.right_panel_views = {
            "default": {
                ajax_url: _self.right_panel_url
            }
        });
        _self.current_right_panel_view || (_self.current_right_panel_view = "default");
        _self.body.find(".app-tab-content-container").addClass("two-panel-explorer");
        _self.initLeftPanel(leftPanel)
    };

    _t.initLeftPanel = function(leftPanel) {
        var _self = this;
        _self.body.find(".header .entity-count").replaceWith(leftPanel.find(".entity-count"));
        leftPanel.find(".body").scrollbar({
            vertical: {
                offset: -3
            }
        });
        _self.initRootSelector(leftPanel);
        var menu = bm.menu(_self.menu_entries, leftPanel.find(".float-menu-navigator"), null, {
            open: function(entity) {
                entity.parents(".explorer-item").addClass("float-menu-opened");
            },
            hide: function(entity) {
                entity.parents(".explorer-item").removeClass("float-menu-opened");
            },
            click: function(action, navigator) {
                var data = [];
                var data = navigator.closest(".explorer-item").config("entity");
                _self.onActionClick(action, data)
            }
        }, "click", ["center bottom", "right+22 top+7"]);
        leftPanel.find(".explorer-item").on("click", function() {
            var $this = $(this), data = $this.config("entity");
            leftPanel.find(".explorer-item.selected").removeClass("selected");
            $this.addClass("selected");
            _self.changeRightPanelView("default")
        });
        bm.instantSearch(leftPanel, leftPanel.find(".search-form"), ".explorer-item", ".title")

    };

    _t.initRootSelector = function (leftPanel) {
        var _self = this, rootSelector = leftPanel.find(".root-selector");
        if(rootSelector.length === 0) return
        var chosen = rootSelector.data("wcuiChosen");
        var rootMenus = "<span class='actions'>";
        _self.root_selector_menu_entries.forEach(function (menu) {
            rootMenus += '<span class="tool-icon ' + menu.ui_class+ '" action="' + menu.action + '"></span>'
        });
        rootMenus += "</span>";
        chosen.results_data.forEach(function(item) {
            item.html = '<span class="text">' + item.html + '</span>' + rootMenus
        });
        var result_select = chosen.result_select;
        chosen.result_select = function(evt) {
            var $this = this, target = $(evt.target);
            if(target.is(".tool-icon")) {
                var item = $this.results_data[$this.result_highlight.data("optionArrayIndex")]
                _self.onRootSelectorActionClick(target.attr("action"), item)
                $this.results_hide();
            } else {
                return result_select.call(this, arguments);
            }
        };
    };

    _t.initRightPanel = function(rightPanel) {
        var _self = this, view = _self.right_panel_views[_self.current_right_panel_view],
            Processor = view.processor;
        Processor && (_self.right_view_processor =  new Processor(rightPanel, _self, view.ajax_url))
    };

    _t.reloadRightPanel = function() {
        var _self = this, rightPanel = this.body.find(".right-panel"), data= {}, selected = this.leftPanel.find(".explorer-item.selected");
        if(selected.length > 0) {
            data = selected.config("entity")
        }
        rightPanel.loader();
        _self.beforeRightPanelReload && _self.beforeRightPanelReload(data);
        bm.ajax({
            url: app.baseUrl + _self.right_panel_views[_self.current_right_panel_view].ajax_url,
            data: data,
            dataType: "html",
            success: function(resp) {
                resp = $(resp).updateUi()
                rightPanel.replaceWith(resp);
                _self.initRightPanel(resp)
            }
        })
    };

    _t.changeRightPanelView = function(view) {
        var _self = this;
        _self.current_right_panel_view = view;
        _self.reloadRightPanel()
    };

    _t.reloadLeftPanel = function() {
        var _self = this, leftPanel = this.leftPanel, selected = leftPanel.find(".explorer-item.selected");
        leftPanel.loader();
        var data = {selected: selected.attr("entity-id")};
        _self.beforeLeftPanelReload && _self.beforeLeftPanelReload(data);
        bm.ajax({
            url: app.baseUrl + _self.left_panel_url,
            dataType: "html",
            data: data,
            success: function(resp) {
                resp = $(resp).updateUi();
                leftPanel.replaceWith(resp);
                _self.leftPanel = resp;
                _self.initLeftPanel(resp);
                _self.afterReloadLeftPanel();
            }
        })
    };

    _t.afterReloadLeftPanel = function() {
        this.reloadRightPanel()
    };

    _t.reload =  function(isRight) {
        if(isRight) {
            this.reloadRightPanel()
        } else {
            this.reloadLeftPanel()
        }
    }
})();
//endregion