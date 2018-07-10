(function () {
    var _DEFAULT_TAB_OPTIONS = {
        active: null,
        collapsible: false,
        event: "click",
        heightStyle: "content",
        hide: null,
        show: null,
        disabled: [],
        load_on_activate: false,
        all_deactivable: false,

        //extended
        ajax_caller: bm.ajax,
        loader_template: "",
        content_on_load_error: "",

        // callbacks
        beforeActivate: null,
        beforeLoad: null,
        load: null,

        //extended
        loadError: null,
        hashnavigation: false
    }
    var INSTANCE_COUNTER = 0;

    var Tab = function (element, options) {
        options = this.options = $.extend({}, _DEFAULT_TAB_OPTIONS, options);
        this.running = false;
        this.element = element;
        this.bindings = $();
        this.hoverable = $();
        this.focusable = $();
        this._handlers = {}
        this.eventNamespace = "tabs-" + INSTANCE_COUNTER++
        this.element.toggleClass("bmui-tab-collapsible", options.collapsible).delegate(".bmui-tab-header", "focus." + this.eventNamespace, function () {
            if ($(this).closest(".bmui-tab-header").is(".bmui-state-disabled")) {
                this.blur();
            }
        });

        this._processTabs();
        options.disabled = $.unique(options.disabled.concat(
            $.map(this.tabs.filter("[data-tabify-disabled], .bmui-state-disabled"), function (tab) {
                return tab.index;
            })
        ));
        options.active = this._initialActive();

        if (this.options.active) {
            this.active = this._findTab(options.active);
        } else {
            this.active = $();
        }

        this._refresh();

        if (this.active.length) {
            this._load(options.active);
        }

        var _self = this
        if (this.options.hashnavigation) {
            $(window).on("hashchange", function () {
                var hash = window.location.hash
                if (hash) {
                    hash = hash.substring(1)
                    if (_self.options.disabled === true) {
                        return;
                    }
                    if (_self.options.disabled && _self.options.disabled.contains(hash)) {
                        var index = _self.getTabIndex(hash) - 1
                        hash = undefined
                        for (; index >= 0; index--) {
                            var key = _self.tabs.eq(index)[0].index
                            if (!_self.options.disabled.contains(key)) {
                                hash = key
                                break;
                            }
                        }
                        if (hash) {
                            var path = bm.path(location)
                            path.fragment = hash
                            history.pushState("tabify", "expected tab disabled", path.full);
                        }
                    } else {
                        var tab = _self._getTab(hash)
                        if (!tab.length) {
                            hash = undefined
                        }
                    }
                    if (hash) {
                        _self.activate(hash)
                    }
                }
            })
        }
    };

    Tab.prototype = {
        activate: function (index, silent) {
            var active = this._findTab(index);
            if (!active || active[0] === this.active[0]) {
                return;
            }
            if (!active.length) {
                active = this.active;
            }

            this._eventHandler({
                target: active,
                currentTarget: active,
                preventDefault: $.noop,
                silent: silent
            });
        },
        add: function (options) {
            var tab
            var index;
            if(options.template instanceof $) {
                tab = options.template
                index = this.tabs.index(tab);
            } else {
                tab = $(options.template || '<div> \
                    <span class="icon"></span>   \
                    <span class="title"></span> \
                    <span class="close"></span>   \
                </div>');
                if (options.title) {
                    tab.find(".title").text(options.title)
                }
                if (options.tip) {
                    tab.attr("title", options.tip);
                }
                if (!options.close_on_header) {
                    tab.find(".close").remove();
                }
                if (!options.icon_on_header) {
                    tab.find(".icon").remove();
                }
                tab.addClass("bmui-tab-header " + (options.clazz || ""));
                if (options.load_url) {
                    tab.attr("data-tabify-url", options.load_url);
                }
                if (options.index) {
                    tab.attr("data-tabify-tab-id", options.index);
                } else {
                    options.index = new Date().getTime();
                    tab.attr("data-tabify-tab-id", options.index);
                }
                if (options.after) {
                    var _tab = this._findTab(options.after);
                    if (_tab && _tab.length) {
                        _tab.after(tab);
                        index = this.tabs.index(_tab) + 1;
                    } else {
                        this.headercontainer.append(tab);
                        index = this.tabs.length;
                    }
                } else if (options.before) {
                    var _tab = this._findTab(options.before);
                    if (_tab && _tab.length) {
                        _tab.before(tab);
                        index = this.tabs.index(_tab);
                    } else {
                        this.headercontainer.prepend(tab);
                        index = 0;
                    }
                } else {
                    this.headercontainer.append(tab);
                    index = this.tabs.length - 1;
                }
            }
            if (options.panel && options.panel.length) {
                options.panel.addClass("bmui-tab-panel")
                tab[0].panel = options.panel;
                if (!options.panel.parent().length) {
                    this.panelcontainer.append(tab[0].panel)
                }
            } else {
                tab[0].panel = $("<div class='bmui-tab-panel' id='bmui-tab-" + options.index + "'></div>");
                this.panelcontainer.append(tab[0].panel)
            }
            Array.prototype.splice.call(this.tabs, index, 0, tab[0]);
            this._processTab(tab[0]);
            this._refresh();
            if (options.active) {
                this._setOption("active", tab[0].index)
            }
            var panel = tab[0].panel
            return {
                tab: tab,
                panel: panel,
                index: tab[0].index
            }
        },
        destroy: function() {
            this._destroy();
            this.element.unbind( "." + this.eventNamespace ).removeData( this.widgetFullName ).removeData( $.camelCase( this.widgetFullName ) );
            this.widget().unbind( "." + this.eventNamespace ).removeAttr( "aria-disabled" ).removeClass(this.widgetFullName + "-disabled " + "bmui-state-disabled" );
            this.bindings.unbind( "." + this.eventNamespace );
            this.hoverable.removeClass( "bmui-state-hover" );
            this.focusable.removeClass( "bmui-state-focus" );
        },
        disable: function (index) {
            if (index) {
                if (this.options.disabled === true) {
                    this.options.disabled = []
                }
                if (this.tabs.filter("[data-tabify-tab-id='" + index + "']").length && !this.options.disabled.contains(index)) {
                    this.options.disabled.push(index)
                    this._setupDisabled()
                }
                return;
            }
            return this._setOptions({disabled: true});
        },
        each: function (iterator) {
            this.tabs.each(function (i, tab) {
                iterator.call({
                    tab: $(tab),
                    panel: tab.panel,
                    index: tab.index
                }, i)
            })
            return this;
        },
        enable: function (index) {
            if (index) {
                if (this.options.disabled === false) {
                    return;
                }
                if (this.tabs.filter("[data-tabify-tab-id='" + index + "']").length && this.options.disabled.contains(index)) {
                    this.options.disabled.remove(index)
                    this._setupDisabled()
                }
                return;
            }
            return this._setOptions({disabled: false});
        },
        getActive: function() {
            if(this.active) {
                return this.active.attr("data-tabify-tab-id")
            }
            return null
        },
        getTabHash: function(index) {
            return {
                tab: this.tabs.filter("[data-tabify-tab-id='" + index + "']"),
                panel: this.element.find(this._sanitizeSelector("#bmui-tab-" + index))
            }
        },
        getTabIndex: function(tabId) {
            return this._getTabIndex(this._getTab(tabId));
        },
        hasIndex: function (index) {
            var tab = this._findTab(index)
            return tab && tab.length
        },
        isContentLoaded: function(index) {
            var tab = this._findTab(index)
            return !tab.nat.load_url;
        },
        option: function (key, value) {
            var options = key,
                parts,
                curOption,
                i;

            if (typeof key === "string") {
                options = {};
                parts = key.split(".");
                key = parts.shift();
                if (parts.length) {
                    curOption = options[key] = $.extend({}, this.options[key]);
                    for (i = 0; i < parts.length - 1; i++) {
                        curOption[parts[i]] = curOption[parts[i]] || {};
                        curOption = curOption[parts[i]];
                    }
                    key = parts.pop();
                    if (arguments.length === 1) {
                        return curOption[key] === undefined ? null : curOption[key];
                    }
                    curOption[key] = value;
                } else {
                    if (arguments.length === 1) {
                        return this.options[key] === undefined ? null : this.options[key];
                    }
                    options[key] = value;
                }
            }

            this._setOptions(options);
        },
        remove: function (index) {
            var tab = this._findTab(index);
            if (!tab || !tab.length) {
                return;
            }
            var panel = this._getPanelForTab(tab);
            var tabIndex = this.tabs.index(tab);
            var panelIndex = this.panels.index(panel);
            Array.prototype.splice.call(this.tabs, tabIndex, 1);
            Array.prototype.splice.call(this.panels, panelIndex, 1);
            if (!this.options.all_deactivable && this.active[0] == tab[0]) {
                var next = this.tabs.eq(tabIndex);
                if (!next.length) {
                    next = this.tabs.eq(tabIndex - 1);
                }
                if (next.length) {
                    this._setOption("active", next[0].index)
                }
            }
            tab.add(panel).remove();
        },
        refresh: function () {
            var options = this.options;
            options.disabled = $.map(this.tabs.filter(".bmui-state-disabled"), function (tab) {
                return tab.index;
            });

            this._processTabs();
            if (options.active === false || !this.tabs.length) {
                options.active = false;
                this.active = $();
            } else if (this.active.length && !$.contains(this.tabs[0], this.active[0])) {
                if (this.tabs.length === options.disabled.length) {
                    options.active = false;
                    this.active = $();
                } else {
                    this.activate(this._findNextTab(this.tabs.index(options.active) - 1, false));
                }
            } else {
                options.active = this.tabs.index(this.active) > -1 ? this.active : null;
            }

            this._refresh();
        },
        reload: function (index, url) {
            var active = this._findTab(index);
            active.addClass("reloading");
            active[0].load_url = url || active.attr("data-tabify-url")
            if (this.active[0] == active[0]) {
                this._eventHandler({
                    target: active,
                    currentTarget: active,
                    preventDefault: $.noop
                });
            }
        },
        _ajaxSettings: function (tab, event, eventData) {
            var that = this;
            return {
                url: tab.load_url,
                dataType: 'html',
                beforeSend: function (jqXHR, settings) {
                    return bm.trigger(that, that.element, "tab", "beforeLoadRequestSend", [$.extend({jqXHR: jqXHR, ajaxSettings: settings}, eventData)]);
                }
            };
        },
        _findNextTab: function (index, goingForward) {
            var lastTabIndex = this.tabs.length - 1;

            function constrain() {
                if (index > lastTabIndex) {
                    index = 0;
                }
                if (index < 0) {
                    index = lastTabIndex;
                }
                return index;
            }

            while ($.inArray(constrain(), this.options.disabled) !== -1) {
                index = goingForward ? index + 1 : index - 1;
            }

            return index;
        },
        _createPanel: function (id) {
            return $("<div>")
                .attr("id", "bmui-tab-" + id)
                .addClass("bmui-tab-panel")
                .data("bmui-tab-destroy", true);
        },
        _delay: function( handler, delay ) {
            function handlerProxy() {
                return ( typeof handler === "string" ? instance[ handler ] : handler )
                    .apply( instance, arguments );
            }
            var instance = this;
            return setTimeout( handlerProxy, delay || 0 );
        },
        _destroy: function () {
            if (this.xhr) {
                this.xhr.abort();
            }

            this.element.removeClass("bmui-tab bmui-tab-collapsible");

            this.headercontainer
                .removeClass("bmui-tab-header-container")
                .removeAttr("role");

            this.tabs.add(this.panels).each(function () {
                if ($.data(this, "bmui-tab-destroy")) {
                    $(this).remove();
                } else {
                    $(this)
                        .removeClass("bmui-state-default bmui-state-active bmui-state-disabled " +
                            "bmui-corner-top bmui-corner-bottom bmui-widget-content bmui-tab-active bmui-tab-panel")
                        .removeAttr("tabIndex")
                        .removeAttr("aria-live")
                        .removeAttr("aria-busy")
                        .removeAttr("aria-selected")
                        .removeAttr("aria-labelledby")
                        .removeAttr("aria-hidden")
                        .removeAttr("aria-expanded")
                        .removeAttr("role");
                }
            });

            this.tabs.each(function () {
                var li = $(this),
                    prev = li.data("bmui-tab-aria-controls");
                if (prev) {
                    li
                        .attr("aria-controls", prev)
                        .removeData("bmui-tab-aria-controls");
                } else {
                    li.removeAttr("aria-controls");
                }
            });

            this.panels.removeClass("bmui-state-visible");

            if (this.options.heightStyle !== "content") {
                this.panels.css("height", "");
            }
        },
        _focusable: function( element ) {
            this.focusable = this.focusable.add( element );
            element.on("focusin." + this.eventNamespace, function( event ) {
                $( event.currentTarget ).addClass( "bmui-state-focus" );
            })
            element.on("focusout." + this.eventNamespace, function( event ) {
                $( event.currentTarget ).removeClass( "bmui-state-focus" );
            })
        },
        _focusNextTab: function (index, goingForward) {
            index = this._findNextTab(index, goingForward);
            this.tabs.eq(index).focus();
            return index;
        },
        _getCreateEventData: function () {
            return {
                tab: this.active,
                panel: this.active.length ? this._getPanelForTab(this.active) : $()
            };
        },
        _getHederContainer: function () {
            var container = this.options.header_container ? (this.options.header_container instanceof $ ? this.options.header_container : this.element.find(this.options.header_container)) : this.element.find("> .bmui-tab-header-container");
            if(!container.length) {
                container = this.element
            }
            return container;
        },
        _getPanelContainer: function () {
            return this.options.bodies_container ? (this.options.bodies_container instanceof $ ? this.options.bodies_container : this.element.find(this.options.bodies_container)) : this.element.find("> .bmui-tab-body-container");
        },
        _getPanelForTab: function (tab) {
            if (!tab || !tab.length) {
                return null;
            }
            if (tab[0].panel) {
                return tab[0].panel;
            }
            var id = tab.attr("data-aria-controls");
            return this.panelcontainer.find("> " + this._sanitizeSelector("#bmui-tab-" + id));
        },
        _getTab: function(tabId) {
            return this.tabs.filter("[data-tabify-tab-id='" + tabId + "']")
        },
        _getTabIndex: function (tab) {
            if (!tab.length) {
                return -1;
            }
            return this.tabs.index(tab);
        },
        _handlePageNav: function (event) {
            if (event.altKey && event.keyCode === browser.key.PAGE_UP) {
                this.activate(this._focusNextTab(this.tabs.index(this.active) - 1, false));
                return true;
            }
            if (event.altKey && event.keyCode === browser.key.PAGE_DOWN) {
                this.activate(this._focusNextTab(this.tabs.index(this.active) + 1, true));
                return true;
            }
        },
        _hoverable: function( element ) {
            this.hoverable = this.hoverable.add( element );
            element.on("mouseenter." + this.eventNamespace, function( event ) {
                $( event.currentTarget ).addClass( "bmui-state-hover" );
            })
            element.on("mouseleave." + this.eventNamespace, function( event ) {
                $( event.currentTarget ).removeClass( "bmui-state-hover" );
            });
        },
        _eventHandler: function (event) {
            var options = this.options,
                active = this.active,
                anchor = $(event.currentTarget),
                tab = anchor.closest(".bmui-tab-header"),
                clickedIsActive = tab[0] === active[0],
                collapsing = clickedIsActive && options.collapsible,
                toShow = collapsing ? $() : this._getPanelForTab(tab),
                toHide = !active.length ? $() : this._getPanelForTab(active),
                eventData = {
                    oldIndex: active.length ? active[0].index : -1,
                    oldTab: active,
                    oldPanel: toHide,
                    newIndex: collapsing ? "" : tab[0].index,
                    newTab: collapsing ? $() : tab,
                    newPanel: toShow
                };
            if(!event.silent) {
                if (tab.hasClass("bmui-state-disabled") || tab.hasClass("bmui-tab-loading") || this.running || ( clickedIsActive && !options.collapsible && !anchor.hasClass("reloading")) || ( bm.trigger(this, this.element, "tab", "beforeActivate", [eventData]) === false )) {
                    return;
                }
            }
            options.active = collapsing ? false : tab[0].index;
            this.active = (clickedIsActive && !anchor.hasClass("reloading")) ? $() : tab;
            anchor.removeClass("reloading");
            if (this.xhr) {
                this.xhr.abort();
            }
            if (!toHide.length && !toShow.length) {
                $.error("jQuery UI Tabs: Mismatching fragment identifier.");
            }
            if (toShow.length) {
                this._load(tab, event);
            }
            this._toggle(event, eventData);
        },
        _findTab: function (index) {
            if (typeof index == "number") {
                return this.tabs.eq(index)
            }
            if (typeof index === "string" || index instanceof String) {
                return this.tabs.filter("[data-tabify-tab-id='" + index + "']");
            }
            if (index instanceof $) {
                var tab = index.closest(".bmui-tab-header", this.element)
                tab = tab.length ? tab : index.closest(".bmui-tab-panel", this.element)
                if (tab.length) {
                    var _tab;
                    this.tabs.each(function () {
                        if (tab[0] == this || tab[0] == this.panel[0]) {
                            _tab = $(this);
                            return false;
                        }
                    })
                    if (_tab) {
                        return _tab;
                    }
                }
            }
            return index || null;
        },
        _initialActive: function () {
            var active = this.options.active, locationHash = location.hash.substring(1);
            if (!active && locationHash) {
                if (this.options.disabled.contains(locationHash)) {
                    var index = this.getTabIndex(locationHash) - 1
                    for (; index >= 0; index--) {
                        var key = this.tabs.eq(index)[0].index
                        if (!this.options.disabled.contains(key)) {
                            active = key
                            var path = bm.path(location)
                            path.fragment = active
                            history.pushState("tabify", "expected tab disabled", path.full);
                            break;
                        }
                    }
                } else {
                    this.tabs.each(function (i, tab) {
                        if ($(tab).attr("aria-controls") === locationHash) {
                            active = tab.index;
                            return false;
                        }
                    });
                }
            }
            if (!active) {
                var _active = this.tabs.filter(".bmui-tab-active");
                if (_active.length) {
                    active = _active[0].index
                }
            }
            if (!active) {
                active = this.tabs.length ? this.tabs[0].index : null;
            }
            return active;
        },
        _load: function (index, event) {
            var that = this,
                tab = this._findTab(index),
                panel = this._getPanelForTab(tab),
                eventData = {
                    index: tab[0].index,
                    tab: tab,
                    panel: panel
                };

            if (!tab[0].load_url) {
                return;
            }

            if (this.options.loader_template) {
                panel.append(this.options.loader_template)
            }
            var settings = this._ajaxSettings(tab[0], event, eventData);
            bm.trigger(that, that.element, "tab", "beforeLoad", [$.extend(settings, eventData)]);
            this.xhr = this.options.ajax_caller(settings);
            if (this.xhr && this.xhr.statusText !== "canceled") {
                tab.addClass("bmui-tab-loading");
                panel.addClass("bmui-tab-loading");
                panel.attr("aria-busy", "true");

                this.xhr
                    .always(function () {
                        setTimeout(function () {
                            if (status === "abort") {
                                that.panels.stop(false, true);
                            }

                            tab.addClass("bmui-tab-loading-error");
                            tab.removeClass("bmui-tab-loading");
                            panel.removeClass("bmui-tab-loading");
                            panel.removeAttr("aria-busy");
                        }, 1);
                    })
                    .done(function (response) {
                        setTimeout(function () {
                            panel.html(response).updateUi();
                            bm.handleUrlNavigation(settings.url, panel)
                            bm.trigger(that, that.element, "tab", "load", [eventData]);
                            if (that.options.load_on_activate) {
                                return;
                            }
                            tab[0].load_url = null;
                        }, 1);
                    })
                    .fail(function (xhr, status, response) {
                        setTimeout(function () {
                            panel.html(that.options.content_on_load_error || (app.production ? "" : response));
                            bm.trigger(that, that.element, "tab", "error", [eventData]);
                        }, 1);
                    })
                    .always(function (jqXHR) {
                        setTimeout(function () {
                            if (jqXHR === that.xhr) {
                                delete that.xhr;
                            }
                        }, 1);
                    });
            }
        },
        _panelKeydown: function (event) {
            if (this._handlePageNav(event)) {
                return;
            }
            if (event.ctrlKey && event.keyCode === browser.key.UP) {
                this.active.focus();
            }
        },
        _processTab: function (tab) {
            var selector, panel,
                anchorId = $(tab).attr("id"),
                originalAriaControls = $(tab).attr("aria-controls");
            tab.load_url = $(tab).attr("data-tabify-url");
            tab.index = selector = $(tab).attr("data-tabify-tab-id");
            if (!tab.index) {
                tab.index = selector = "index-" + bm.getUUID();
                $(tab).attr("data-tabify-tab-id", selector);
            }
            tab.panel = panel = tab.panel || this.panelcontainer.find("> #bmui-tab-" + selector);
            if (!panel.length) {
                panel = this._createPanel(selector);
                panel.appendTo(this.panelcontainer);
                panel.attr("aria-live", "polite");
                tab.panel = panel
            }
            this.panels = this.panels.add(panel);
            tab = $(tab)
            if (originalAriaControls) {
                tab.data("bmui-tab-aria-controls", originalAriaControls);
            }
            tab.attr({
                "aria-controls": selector,
                "aria-labelledby": anchorId
            });
            panel.attr("aria-labelledby", anchorId);
        },
        _processTabs: function () {
            var that = this;
            this.headercontainer = this._getHederContainer().attr("role", "tablist");
            this.panelcontainer = this._getPanelContainer()
            this.tabs = this.headercontainer.find("> .bmui-tab-header-group > .bmui-tab-group-headers > .bmui-tab-header, > .bmui-tab-header").addClass("bmui-state-default").attr({
                role: "tab",
                tabIndex: -1
            });
            this.panels = $();
            this.tabs.each(function (i, tab) {
                that._processTab(tab)
            });
            this.panels.addClass("bmui-tab-panel").attr("role", "tabpanel");
        },
        _refresh: function () {
            this._setupDisabled();
            this._setupEvents(this.options.event);
            this._setupHeightStyle(this.options.heightStyle);

            this.tabs.not(this.active).attr({
                "aria-selected": "false",
                tabIndex: -1
            });
            this.panels.not(this._getPanelForTab(this.active)).removeClass("bmui-state-visible").attr({
                "aria-expanded": "false",
                "aria-hidden": "true"
            });
            if (!this.active.length) {
                this.tabs.eq(0).attr("tabIndex", 0);
            } else {
                this.active.addClass("bmui-tab-active bmui-state-active").attr({
                    "aria-selected": "true",
                    tabIndex: 0
                });
                if (this.options.hashnavigation) {
                    if (location.hash != "#" + this.active[0].index) {
                        var path = bm.path(location)
                        path.fragment = this.active[0].index
                        history.pushState("tabify", path.fragment + "tab activated", path.full);
                    }
                }
                this._getPanelForTab(this.active).addClass("bmui-state-visible").attr({
                    "aria-expanded": "true",
                    "aria-hidden": "false"
                });
            }
        },
        _sanitizeSelector: function (hash) {
            return hash ? hash.replace(/[!"$%&'()*+,.\/:;<=>?@\[\]\^`{|}~]/g, "\\$&") : "";
        },
        _setOption: function( key, value ) {
            this.options[ key ] = value;

            if ( key === "disabled" ) {
                this._setupDisabled()
            }

            if (key === "active") {
                this.activate(value);
                return;
            }

            if (key === "disabled") {
                this._setupDisabled(value);
                return;
            }

            if (key === "collapsible") {
                this.element.toggleClass("bmui-tab-collapsible", value);
                if (!value && this.options.active === false) {
                    this.activate(0);
                }
            }

            if (key === "event") {
                this._setupEvents(value);
            }

            if (key === "heightStyle") {
                this._setupHeightStyle(value);
            }

            return this;
        },
        _setOptions: function (options) {
            var key;

            for (key in options) {
                this._setOption(key, options[key]);
            }

            return this;
        },
        _setupDisabled: function (disabled) {
            disabled = typeof disabled == "undefined" ? this.options.disabled : disabled;
            var disableds = $()
            var ndisableds = $()
            if (disabled === true) {
                disableds = this.tabs;
            } else if (disabled === false) {
                ndisableds = this.tabs;
            } else {
                this.tabs.each(function (i) {
                    if ($.inArray(this.index, disabled) > -1 || $.inArray(i, disabled) > -1) {
                        disableds = disableds.add($(this));
                    } else {
                        ndisableds = ndisableds.add($(this));
                    }
                });
            }
            disableds.addClass("bmui-state-disabled")
                .attr("aria-disabled", "true");
            ndisableds.removeClass("bmui-state-disabled")
                .removeAttr("aria-disabled");
            if (disableds.length) {
                if (disableds.length == this.tabs.length) {
                    this.options.disabled = true;
                } else {
                    this.options.disabled = disabled;
                }
            } else {
                this.options.disabled = false;
            }
        },
        _setupEvents: function (event) {
            var that = this;
            var events = {keydown: $.proxy(this._tabKeydown, this)};
            if (event) {
                $.each(event.split(" "), function (index, eventName) {
                    events[eventName] = $.proxy(that._eventHandler, that);
                });
            }
            $.extend(this._handlers, events);
            this.tabs.add(this.panels).off("." + this.eventNamespace)
            this.tabs.find(".close").off("." + this.eventNamespace).on("click." + this.eventNamespace, function () {
                var tab = $(this).closest(".bmui-tab-header")
                var panel = that._getPanelForTab(tab)
                var index = tab[0].index
                var response = bm.trigger(that, that.element, "tab", "beforeRemove", [{
                    tab: tab,
                    panel: panel,
                    index: index
                }])
                if(response === false) {
                    return;
                }
                that.remove(index);
                bm.trigger(that, that.element, "tab", "remove", [{
                    tab: tab,
                    panel: panel,
                    index: index
                }])
            });
            this.panels.on("keydown." + this.eventNamespace, $.proxy(this._panelKeydown, this));
            this._setupTabEvents(this.tabs);
        },
        _setupHeightStyle: function (heightStyle) {
            var maxHeight,
                parent = this.element.parent();

            if (heightStyle === "fill") {
                maxHeight = parent.height();
                maxHeight -= this.element.outerHeight() - this.element.height();

                this.element.siblings(":visible").each(function () {
                    var elem = $(this),
                        position = elem.css("position");

                    if (position === "absolute" || position === "fixed") {
                        return;
                    }
                    maxHeight -= elem.outerHeight(true);
                });

                this.element.children().not(this.panels).each(function () {
                    maxHeight -= $(this).outerHeight(true);
                });

                this.panels.each(function () {
                    $(this).height(Math.max(0, maxHeight -
                        $(this).innerHeight() + $(this).height()));
                })
                    .css("overflow", "auto");
            } else if (heightStyle === "auto") {
                maxHeight = 0;
                this.panels.each(function () {
                    maxHeight = Math.max(maxHeight, $(this).height("").height());
                }).height(maxHeight);
            }
        },
        _setupTabEvents: function (tabs) {
            var _this = this;
            $.each(this._handlers, function(k, v) {
                tabs.on(k + "." + _this.eventNamespace, $.proxy(v, _this))
            })
            this._focusable(tabs);
            this._hoverable(tabs);
        },
        _tabKeydown: function (event) {
            var focusedTab = $(this[0].document.activeElement).closest(".bmui-tab-header"), selectedIndex = this.tabs.index(focusedTab), goingForward = true;

            if (this._handlePageNav(event)) {
                return;
            }

            switch (event.keyCode) {
                case browser.key.RIGHT:
                case browser.key.DOWN:
                    selectedIndex++;
                    break;
                case browser.key.UP:
                case browser.key.LEFT:
                    goingForward = false;
                    selectedIndex--;
                    break;
                case browser.key.END:
                    selectedIndex = this.tabs.length - 1;
                    break;
                case browser.key.HOME:
                    selectedIndex = 0;
                    break;
                case browser.key.SPACE:
                    event.preventDefault();
                    clearTimeout(this.activating);
                    this.activate(selectedIndex);
                    return;
                case browser.key.ENTER:
                    event.preventDefault();
                    clearTimeout(this.activating);
                    this.activate(selectedIndex === this.tabs.index(this.active) ? false : selectedIndex);
                    return;
                default:
                    return;
            }

            clearTimeout(this.activating);
            selectedIndex = this._focusNextTab(selectedIndex, goingForward);

            if (!event.ctrlKey) {
                focusedTab.attr("aria-selected", "false");
                var tab = this.tabs.eq(selectedIndex).attr("aria-selected", "true");

                this.activating = this._delay(function () {
                    this.option("active", tab[0].index);
                }, this.delay);
            }
        },
        _toggle: function (event, eventData) {
            var that = this,
                toShow = eventData.newPanel,
                toHide = eventData.oldPanel;
            this.running = true;
            eventData.oldTab.closest(".bmui-tab-header").removeClass("bmui-tab-active bmui-state-active");
            toHide.removeClass("bmui-state-visible");
            eventData.newTab.closest(".bmui-tab-header").addClass("bmui-tab-active bmui-state-active");
            if (this.options.hashnavigation) {
                if (location.hash != "#" + eventData.newTab[0].index) {
                    var path = bm.path(location)
                    path.fragment = eventData.newTab[0].index
                    history.pushState("tabify", path.fragment + "tab activated", path.full);
                }
            }
            toShow.addClass("bmui-state-visible");
            that.running = false;
            if(!event.silent) {
                bm.trigger(that, that.element, "tab", "activate", [eventData]);
            }
            toHide.attr({
                "aria-expanded": "false",
                "aria-hidden": "true"
            });
            eventData.oldTab.attr("aria-selected", "false");
            if (toShow.length && toHide.length) {
                eventData.oldTab.attr("tabIndex", -1);
            } else if (toShow.length) {
                this.tabs.filter(function () {
                    return $(this).attr("tabIndex") === 0;
                })
                    .attr("tabIndex", -1);
            }
            toShow.attr({
                "aria-expanded": "true",
                "aria-hidden": "false"
            });
            eventData.newTab.attr({
                "aria-selected": "true",
                tabIndex: 0
            });
        }
    }

    bm.jquerify("tabify", Tab)
})();

if(bm.ui_updaters) {
    bm.ui_updaters.push(function () {
        this.find(".bmui-tab").tabify({
            loader_template: '<div class="mask-overlay"><span class="loader"></span></div>'
        }).on("tab:beforeLoad", function(e, hash) {
            hash.panel.addClass("masked-div")
        }).on("tab:load tab:error", function(e, hash) {
            hash.panel.removeClass("masked-div")
        });
    })
}