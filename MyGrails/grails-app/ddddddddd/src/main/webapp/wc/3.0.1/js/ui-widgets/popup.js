var POPUP_STATICS = {
    z_index: {
        active: 8000,
        modal_base: 3000,
        no_modal: 1000,
        always_up: 50000
    },
    active_stack: [],
    next_instance_identifier: 1,
    modal_mask: undefined,
    minimized_placeholder: undefined
};

var _popup_defaults = {
    clazz: '', //extra classes for popup
    minimized_clazz: '',  //TODO: yet to add support
    animation_clazz: '',
    closing_animation_clazz: '',
    closing_animation_duration: 300,
    show_title: true,
    title: null,
    minimized_title: null,  //TODO: yet to add support
    draggable: false,
    drag_handle: ".title-bar",
    drag_cancel: ":input",
    disable_text_selection: false,
    resizable: false,    //TODO: yet to add support
    maximizable: false,  //TODO: yet to add support
    minimizable: false,  //TODO: yet to add support
    modal: true,
    show_close: true,
    masking: true,
    close_on_mask_click: false,
    close_on_blur: false,
    close_on_escape: true,
    auto_close: false,
    reset_close_timer_on_hover: true,
    width: "auto",
    max_width: null,
    height: "auto",
    top: null, //will supersede ui_position
    left: null, //will supersede ui_position
    ui_position: null,
    is_fixed: false,
    is_center: true,
    is_always_up: false,
    always_up_z_index: undefined,
    auto_active: true,
    auto_active_on_focus: true,
    ajax_url: undefined,
    ajax_caller: bm.ajax,
    ajax_settings: {},
    on_load_loader: true,
    content: '',
    content_clazz: '', // extra class in content container
    template: '<div>\
        <div class="title-bar">\
            <span class="title"></span>\
            <span class="window-corner-toolbar">\
                <span class="minimize"></span>\
                <span class="maximize"></span>\
                <span class="close"></span>\
            </span>\
        </div>\
        <div class="content"></div>\
    </div>',
    minimized_content: '',        //TODO: yet to add support
    minimized_template: '<div>\
        <span class="title"></span>\
        <span class="content"></span>\
    </div>',                     //TODO: yet to add support
    parent: undefined,
    events: {
        beforeClose: undefined, //preventable
        close: undefined,
        beforeMinimize: undefined, //preventable
        minimize: undefined,
        beforeRestore: undefined, //preventable
        restore: undefined,
        maximize: undefined,
        beforeMaximize: undefined, //preventable
        render: undefined,
        beforeRender: undefined,
        beforeInactive: undefined,  //preventable
        inactive: undefined,
        beforeActive: undefined,
        active: undefined,
        content_loaded: undefined
    }
};

//props for internal use
/*
 * el, instance_identifier, z_index, childs, is_maximized, is_minimized, is_rendered, is_active, auto_close_timer, event_handlers, is_closed, is_hidden
 */

var POPUP = function (settings) {
    $.extend(this, _popup_defaults, settings);
    this.instance_identifier = POPUP_STATICS.next_instance_identifier++;
    this.z_index = this.is_always_up ? (this.always_up_z_index || POPUP_STATICS.z_index.always_up) : (this.modal ? (POPUP_STATICS.z_index.modal_base += 10) : POPUP_STATICS.z_index.no_modal);
    this.is_rendered = false;
    this.is_active = false;
    this.event_handlers = $({});
    if (this.parent) {
        this.parent.addChild(this);
    }
    this.createPopup();
    this.render();
    if (this.auto_active) {
        this.setActive();
    }
};

var _p = POPUP.prototype;

_p.addChild = function (popup) {
    if (!this.childs) {
        this.childs = [];
    }
    this.childs.push(popup);
    var _self = this;
    popup.on("close", function () {
        var index = _self.childs.indexOf(this);
        _self.childs.splice(index, 1);
    });
};

_p.one = function (evName, handler) {
    this.event_handlers.one(evName, $.proxy(handler, this.el));
};

_p.on = function (evName, handler) {
    this.event_handlers.on(evName, $.proxy(handler, this.el));
};

_p.off = function (evName) {
    this.event_handlers.off(evName);
};

_p.trigger = function (evName, params) {
    if (!params) {
        params = [this];
    }
    var triggerReturn;
    if (this.events[evName]) {
        triggerReturn = this.events[evName].apply(this.el, params);
    }
    if (triggerReturn !== false) {
        triggerReturn = this.event_handlers.trigger(evName, params)
    }
    if (triggerReturn !== false) {
        return this.el.trigger("wcui-popup-" + evName, params)
    }
    return false;
};

_p.render = function () {
    this.trigger("beforeRender");
    this.setMask();
    this.setContent();
    this.bindEvents();
    this.trigger("render");
    this.is_rendered = true;
};

_p.createPopup = function () {
    if (!this.el) {
        this.el = $(this.template);
    }
    if (this.content_clazz) {
        this.el.find('>.content').addClass(this.content_clazz);
    }
    this.el.css({
        position: this.is_fixed ? "fixed" : "absolute",
        left: this.left || -10000,
        top: this.top || -10000,
        width: this.width,
        maxWidth: this.max_width,
        height: this.height,
        zIndex: this.z_index
    }).appendTo(this.parent ? this.parent.el : document.body);
    this.el.addClass(this.clazz + " popup " + this.animation_clazz);
    if (!this.show_title) {
        if (this.show_close || this.maximizable || this.minimizable) {
            this.el.find(".title-bar").removeClass("title-bar").addClass("no-title-bar")
        } else {
            this.el.find(".title-bar").remove();
        }
    }
    if (!this.show_close) {
        this.el.find("span.close").remove();
    }
    if (!this.maximizable) {
        this.el.find("span.maximize").remove();
    }
    if (!this.minimizable) {
        this.el.find("span.minimize").remove();
    }
    if (this.show_title && this.title) {
        this.el.find(".title").html(this.title)
    }
    this.el.attr("id", "popup-" + this.instance_identifier);
    this.el.attr("tabindex", "-1");
};

_p.setMask = function () {
    if (this.modal) {
        if (!POPUP_STATICS.modal_mask) {
            POPUP_STATICS.modal_mask = $("<div></div>").appendTo(document.body);
        }
        var classes = ["popup-mask"];
        this.clazz.split(" ").every(function () {
            if ($.trim(this)) {
                classes.push(this + "-mask")
            }
        });
        POPUP_STATICS.modal_mask.attr("class", classes.join(" ")).css({
            zIndex: this.z_index - 5
        }).show();
    }
};

_p.hideMask = function () {
    if (this.modal) {
        if (POPUP_STATICS.active_stack.count("this.modal && !this.is_hidden") >= 1) {
            POPUP_STATICS.modal_mask.hide();
        }
    }
};

_p.setContent = function (content) {
    var _self = this;
    var contentContainer = this.el.find(".content:first");
    if (!contentContainer.length) {
        contentContainer = this.el;
    }
    if (content || !this.ajax_url) {
        content = content || this.content;
        if (content) {
            contentContainer.html(content);
        }
        this.position();
        _self.trigger("content_loaded")
    } else {
        function resetWidthHeight() {
            if (_self.width == "auto") {
                _self.el.width("auto")
            }
            if (_self.height == "auto") {
                _self.el.height("auto")
            }
        }

        this.el.addClass("ajax-loading");
        if (this.on_load_loader) {
            contentContainer.html('<span class="loader"></span>');
        }
        if (!this.is_rendered) {
            var width = this.width;
            if (width == "auto") {
                width = 300;
            }
            var height = this.height;
            if (height == "auto") {
                height = 300;
            }
            this.position(undefined, width, height);
        }
        this.ajax_caller($.extend({
            url: this.ajax_url,
            dataType: "html",
            response: function () {
                _self.el.removeClass("ajax-loading");
            },
            success: function (content) {
                contentContainer.html(content);
                _self.trigger("ajax_loaded");
                resetWidthHeight();
                _self.position();
                _self.trigger("content_loaded");
            },
            error: function (xhr, shortid, message) {
                _self.el.addClass("http-" + xhr.status + "-error");
                content = $("<div class='ajax-loading-error'></div>").append(message);
                contentContainer.html(content);
                _self.trigger("content_loading_error")
            }
        }, this.ajax_settings))
    }
};

/**
 * used for apply position for POPUP in viewPort
 * @public
 */
_p.position = function (region, force_width, force_height) {
    var method = this.el.offset().left < -2000 ? "css" : "animate";
    if (region) {
        this.el[method](region)
    } else if (this.ui_position && !this.left && !this.top) {
        var position = this.ui_position;
        if (!position.using && method == "animate") {
            var _self = this;
            position = $.extend({}, position, {
                using: function (css) {
                    _self.el.animate(css)
                }
            })
        }
        this.el.position(position);
    } else if (this.is_center) {
        var scrHeight = $(window).height();
        var scrWidth = $(window).width();
        var popHeight = force_height || this.el.outerHeight();
        var popWidth = force_width || this.el.outerWidth();
        var left = this.left || (scrWidth / 2 - popWidth / 2);
        var top = this.top || (scrHeight / 2 - popHeight / 2);
        if (!this.is_fixed) {
            var scrollTop = $(window).scrollTop();
            var scrollLeft = $(window).scrollLeft();
            left = left + scrollLeft;
            top = top + scrollTop
        }
        var position = {
            left: Math.max(20, left),
            top: Math.max(20, top)
        };
        if (force_width) {
            position.width = force_width;
        }
        if (force_height) {
            position.height = force_height;
        }
        this.el[method](position)
    } else {
        this.el[method]({
            left: this.left || 100,
            top: this.top || 100,
            width: force_width || this.width,
            height: force_height || this.height
        })
    }
};

_p.setActive = function () {
    if (this.is_active) {
        return;
    }
    var _self = this;
    if (POPUP_STATICS.active_stack.length > 0) {
        var veto = true;
        if (this.masking) {
            veto = POPUP_STATICS.active_stack[POPUP_STATICS.active_stack.length - 1].setInactive();
        }
        if (veto === false) {
            return;
        }
    }
    this.trigger("beforeActive");
    this.el.find(".inactive-mask").hide();
    _self.el.removeClass("inactive");
    setTimeout(function () {
        _self.el.addClass("active");
    }, 200);
    this.el.css({
        zIndex: Math.max(this.z_index, POPUP_STATICS.z_index.active)
    });
    var index_in_active_stack = POPUP_STATICS.active_stack.find(this.instance_identifier + " == this.instance_identifier");
    if (index_in_active_stack > -1) {
        POPUP_STATICS.active_stack.splice(index_in_active_stack, 1);
    }
    POPUP_STATICS.active_stack.push(this);
    this.trigger("active");
    this.is_active = true;
    var modal = this;
    while (!modal.modal) {
        if (!modal.parent) {
            break;
        }
        modal = modal.parent
    }
    if (modal.modal) {
        modal.setMask();
    }
    this.el.focus();
};

_p.setInactive = function () {
    if (!this.is_active) {
        return;
    }
    var okToActive = this.trigger("beforeInactive");
    if (okToActive === false) {
        return false;
    }
    var _self = this;
    this.el.removeClass("active").addClass("inactive");
    this.el.css({
        zIndex: this.z_index
    });
    var mask = this.el.find(".inactive-mask");
    if (!mask.length) {
        mask = $("<div class='inactive-mask'></div>").appendTo(this.el);
        mask.on("mousedown", function () {
            _self.setActive()
        })
    }
    mask.show();
    this.trigger("inactive");
    this.is_active = false;
    this.hideMask();
};

_p.bindEvents = function () {
    var _self = this;
    this.el.find(".close").click(function () {
        _self.close(1);
    });
    if (this.close_on_mask_click === true && POPUP_STATICS.modal_mask) {
        POPUP_STATICS.modal_mask.bind("click.popup-mask-click-" + this.instance_identifier).click(function () {
            if (_self.is_active) {
                _self.close(1);
            }
        })
    }
    if (this.close_on_blur === true) {
        $(document).bind("mousedown.popup-out-click-" + this.instance_identifier, function (e) {
            var container = _self.el;
            if (!container.is(e.target) && container.has(e.target).length === 0) {
                if (_self.is_active) {
                    _self.close(1);
                }
            }
        })
    }
    if (this.close_on_escape) {
        $(document).bind("keyup.key_esc.popup-escape-" + this.instance_identifier, function () {
            if (_self.is_active) {
                _self.close(1);
            }
        })
    }
    if (this.auto_close) {
        this.resetCloseTimer();
        if (this.reset_close_timer_on_hover) {
            this.el.hover(function () {
                _self.clearCloseTimer();
            }, function () {
                _self.resetCloseTimer();
            })
        }
    }
    if (this.auto_active_on_focus) {
        this.el.bind("focus", function () {
            _self.setActive();
        })
    }
    if (this.draggable) {
        var _self = this;
        var drag_el = this.drag_handle == "*" ? this.el : this.el.find(this.drag_handle);
        var el_offset = this.el.offset();
        var drag_el_offset = drag_el.offset();
        var top_diff = drag_el_offset.top - el_offset.top;
        var left_diff = drag_el_offset.left - el_offset.left;
        this.el.draggable({
            handle: this.drag_handle == "*" ? undefined : drag_el,
            cancel: this.drag_cancel,
            disable_text_selection: this.disable_text_selection,
            stop: function () {
                var offset = drag_el.offset();
                var adj = false;
                if (offset.left < -1 * drag_el.outerWidth() + 50) {
                    offset.left = 5;
                    adj = true;
                }
                if (offset.top < -1 * drag_el.outerHeight() + 50) {
                    offset.top = 5;
                    adj = true;
                }
                if (adj) {
                    _self.el.offset({
                        left: offset.left - left_diff,
                        top: offset.top - top_diff
                    });
                }
                if (_self.animation_clazz) {
                    //_self.el.addClass(_self.animation_clazz); // NO need this because we can't again create the popup, we just moving. so in that time no need to add animation class
                }
            },
            start: function () {
                if (_self.animation_clazz) {
                    _self.el.removeClass(_self.animation_clazz);
                }
            }
        })
    }
};

_p.clearCloseTimer = function () {
    if (this.auto_close_timer) {
        clearTimeout(this.auto_close_timer);
        this.auto_close_timer = null;
    }
};

_p.resetCloseTimer = function () {
    this.clearCloseTimer();
    var _self = this;
    this.auto_close_timer = setTimeout(function () {
        _self.auto_close_timer = null;
        _self.close(0);
    }, this.auto_close);
};

_p.unbindEvents = function () {
    if (this.close_on_mask_click === true && POPUP_STATICS.modal_mask) {
        POPUP_STATICS.modal_mask.unbind("click.popup-mask-click-" + this.instance_identifier)
    }
    if (this.close_on_blur === true) {
        $(document).unbind("mousedown.popup-out-click-" + this.instance_identifier);
    }
    if (this.close_on_escape) {
        $(document).unbind("keyup.key_esc.popup-escape-" + this.instance_identifier);
    }
    this.clearCloseTimer()
};

(function () {
    function setNextActive() {
        if (this.is_active && POPUP_STATICS.active_stack.length) {
            var nextActive;
            while (true) {
                nextActive = POPUP_STATICS.active_stack[POPUP_STATICS.active_stack.length - 1];
                if (nextActive) {
                    if (nextActive.is_closed || nextActive.is_hidden) {
                        POPUP_STATICS.active_stack.pop();
                    } else {
                        nextActive.setActive();
                        break;
                    }
                } else {
                    break;
                }
            }
        }
    }

    _p.close = function (exitCode) {
        var _self = this;
        if (this.trigger("beforeClose", [exitCode]) === false) {
            return;
        }
        this.unbindEvents();
        if (this.closing_animation_clazz) {
            this.el.removeClass("active").removeClass(this.animation_clazz).addClass(this.closing_animation_clazz);
            setTimeout(function () {
                _self.el.remove();
            }, this.closing_animation_duration);
        } else {
            this.el.remove();
        }
        this.hideMask();
        if (this.is_active) {
            POPUP_STATICS.active_stack.pop();
        }
        this.is_closed = true;
        setNextActive.call(this);
        this.trigger("close", [exitCode]);
    };

    _p.hide = function () {
        this.el.hide();
        this.hideMask();
        this.is_hidden = true;
    }
})();

_p.show = function () {
    this.el.show();
    this.setActive();
    this.is_hidden = false;
};

_p.getDom = function () {
    return this.el;
};

$.prototype.popup = function (settings) {
    var obj = this.data("popup-inst");
    if (typeof settings == "string" && obj) {
        if ($.isFunction(obj[settings])) {
            return obj[settings](Array.prototype.splice.call(arguments, 1))
        } else {
            return obj[settings];
        }
    }
    if (!obj) {
        obj = new POPUP($.extend({}, settings, {el: this}));
        this.data("popup-inst", obj);
    }
    return this;
};