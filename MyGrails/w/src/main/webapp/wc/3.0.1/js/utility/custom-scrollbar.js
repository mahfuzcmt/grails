$.scrollbar_horizontal_without_bar_step = 100;

(function() {
    $.scrollbar = {
        defaults: {
            show_vertical: true,
            show_horizontal: false,
            visible_on: "hover", //"always", "auto"
            hide_after: 8000,
            use_bar: true,
            reduce_margin_from_offset: false,
            add_padding_with_offset: true,
            vertical: {
                position: "right",
                offset: undefined,
                top_offset: 5,
                right_offset: 5,
                left_offset: undefined,
                add_margin_with_height: false,
                reduce_padding_from_height: false
            },
            horizontal: {
                position: "bottom",
                offset: undefined,
                top_offset: undefined,
                bottom_offset: 5,
                left_offset: 5,
                add_margin_with_width: false,
                reduce_padding_from_width: true,
                width: -10
            },
            swipe_scroll: false
        },
    }
    $.tablescrollbar = {
        defaults: {
            use_parent_as_wrapper: true
        }
    }

    function _vPosition() {
        var height = this.configs.vertical.add_margin_with_height ? this.elm.outerHeight(true) : (this.configs.vertical.reduce_padding_from_height ? this.elm.height() : this.elm.innerHeight())
        this.v_bar.height(height)
        let signFix = (dir, plus, min) => {
            let offset = this[`v_${dir}_offset`]
            if(offset > 0) {
                return plus + offset
            } else {
                return min + (-1 * offset)
            }
        }
        var left = signFix("left", "+", "-")
        var top = signFix("top", "+", "-")
        var right = signFix("right", "-", "+")
        switch (this.configs.vertical.position) {
            case "left":
                this.v_bar.position({
                    my: "left top",
                    at: `left${left} top${top}`,
                    of: this.elm
                });
                break;
            case "right":
                bm.posRefChangeLtoR(this.v_bar.position({
                    my: "right top",
                    at: `right${right} top${top}`,
                    of: this.elm
                }))
                break;
        }
    }

    function _hPosition() {
        var width = this.configs.horizontal.add_margin_with_width ? this.elm.outerWidth(true) : (this.configs.horizontal.reduce_padding_from_width ? this.elm.width() : this.elm.innerWidth())
        if(this.configs.horizontal.width) {
            width += this.configs.horizontal.width
        }
        this.v_bar.width(width)
        let signFix = (dir, plus, min) => {
            let offset = this[`v_${dir}_offset`]
            if(offset > 0) {
                return plus + offset
            } else {
                return min + (-1 * offset)
            }
        }
        var left = signFix("left", "+", "-")
        var bottom = signFix("bottom", "-", "+")
        switch (this.configs.horizontal.position) {
            case "bottom":
                this.h_bar.position({
                    my: "bottom left",
                    at: `bottom${bottom} left${left}`,
                    of: this.elm
                });
                break;
        }
    }

    function _vRender() {
        this.elm.addClass("vertical-scroll").css("overflow-y", "hidden")
        var bar = this.v_bar = "<div class='vertical scrollbar'></div>".jqObject.css('position', 'absolute').hide();
        var handler = this.v_handler = "<div class='scrollbar-handle'></div>".jqObject;
        bar.append(handler);
        this.elm.after(bar);
        if(bar.css("position") == "static") {
            bar.css("position", "absolute")
        }
        if(this.configs.vertical.position != "auto") {
            _vPosition.call(this);
        }
    }

    function _hRender() {
        this.elm.addClass("horizontal-scroll").css("overflow-x", "hidden")
        var bar = this.h_bar = "<div class='horizontal scrollbar'></div>".jqObject.css('position', 'absolute').hide();
        var handler = this.h_handler = "<div class='scrollbar-handle'></div>".jqObject;
        bar.append(handler);
        this.elm.after(bar);
        if(this.configs.horizontal.position != "auto") {
            _hPosition.call(this);
        }
    }

    function _vBarUpdate() {
        if (this.ready_for_show) {
            var containerHeight = this.elm.innerHeight();
            var requiredHeight = this.elm[0].scrollHeight;
            if(this.configs.visible_on == "always" || containerHeight < requiredHeight) {
                if(this.v_bar_shown) {
                    _vPosition.call(this);
                } else {
                    showBar.call(this, true, false);
                }
                if(this.v_bar_shown) {
                    if(this.mouse_on_v || this.v_dragging) {
                        clearTimer.call(this, true, false)
                    } else {
                        resetTimer.call(this, true, false);
                    }
                }
                var barInnerHeight = this.v_bar.innerHeight();
                var handlerHeight = containerHeight / requiredHeight * barInnerHeight;
                this.v_handler.css("height", bm.innerHeight(this.v_handler, handlerHeight, true));
                var scrollTop = this.elm.scrollTop()
                var handlerTop = scrollTop / requiredHeight * barInnerHeight;
                this.v_handler.css("top", handlerTop);
            } else {
                if(this.v_bar_shown) {
                    hideBar.call(this, true, false, true);
                }
            }
        } else {
            if(this.v_bar_shown) {
                _vPosition.call(this);
            }
        }
    }

    var isVerticalScrollEnable = false;
    var isHorizontalScrollEnable = false;

    function _hNavigatorUpdate() {
        if(this.configs.visible_on == "auto") {
            var containerWidth = this.elm.innerWidth();
            var requiredWidth = this.elm[0].scrollWidth;

            var containerHeight = this.elm.innerHeight();
            var requiredHeight = this.elm[0].scrollHeight;

            if(containerHeight < requiredHeight && containerWidth < containerHeight) {
                isVerticalScrollEnable = true;
            }
            else {
                isVerticalScrollEnable  = false;
            }

            if(containerWidth < requiredWidth && containerWidth > containerHeight) {
                isHorizontalScrollEnable = true;
            }
            else {
                isHorizontalScrollEnable  = false;
            }

            if (isHorizontalScrollEnable || isVerticalScrollEnable) {

                if (this.configs.horizontal.handle.left.is(":hidden")) {
                    this.elm.addClass("content-overflow")
                    this.configs.horizontal.handle.left.show();
                    this.configs.horizontal.handle.right.show();
                }
            } else {
                if (this.configs.horizontal.handle.left.is(":visible")) {
                    this.elm.removeClass("content-overflow")
                    this.configs.horizontal.handle.left.hide();
                    this.configs.horizontal.handle.right.hide();
                }
            }
        }
    }

    function _hBarUpdate() {
        if (this.ready_for_show) {
            var containerWidth = this.elm.innerWidth();
            var requiredWidth = this.elm[0].scrollWidth;
            if(this.configs.visible_on == "always" || containerWidth < requiredWidth) {
                if(this.h_bar_shown) {
                    _hPosition.call(this);
                } else {
                    showBar.call(this, false, true);
                }
                if(this.h_bar_shown) {
                    if(this.mouse_on_h || this.h_dragging) {
                        clearTimer.call(this, false, true)
                    } else {
                        resetTimer.call(this, false, true);
                    }
                }
                var barInnerWidth = this.h_bar.innerWidth();
                var handlerWidth = containerWidth / requiredWidth * barInnerWidth;
                this.h_handler.css("width", bm.innerWidth(this.h_handler, handlerWidth, true));
                var scrollTop = this.elm.scrollTop()
                var handlerTop = scrollTop / requiredWidth * barInnerWidth;
                this.h_handler.css("top", handlerTop);
            } else {
                if(this.h_bar_shown) {
                    hideBar.call(this, true, false, true);
                }
            }
        } else {
            if(this.h_bar_shown) {
                _hPosition.call(this);
            }
        }
    }

    function _hBarEvents() {
        var _self = this;
        var has_capture_support = this.elm[0].setCapture
        this.elm.mousemove(function(ev) {
            var scrollerOffset = _self.elm.offset().top;
            if (_self.configs.horizontal.position == "bottom") {
                scrollerOffset += _self.elm.height() - _self.h_bar.css("bottom").int()
            } else {
                scrollerOffset += _self.h_bar.css("top").int()
            }
            var mouseY = ev.pageY;
            if (Math.abs(scrollerOffset - mouseY) < 100) {
                _self.h_bar_in_area = true
                clearTimer.call(_self, false, true)
                if (!_self.h_bar_shown) {
                    _hBarUpdate.call(_self, true)
                }
            } else {
                _self.h_bar_in_area = false
                if (!_self.h_hide_timer && _self.h_bar_shown && !_self.h_dragging) {
                    resetTimer.call(_self, false, true)
                }
            }
        })
        this.h_bar.hover(function () {
            _self.mouse_on_h = true;
        }, function () {
            _self.mouse_on_h = false;
        });
        var initial_mouse_x
        var initial_handler_left
        var handler_container_width_ratio
        var handler_width
        var bar_width
        var mouseupHandler = function (ev) {
            _self.h_dragging = false;
            if (has_capture_support) {
                ev.target.releaseCapture()
            } else {
                document.removeEventListener("mouseup", mouseupHandler, true)
                document.removeEventListener("mousemove", mousemoveHandler, true)
            }
            if (!_self.h_bar_in_area) {
                hideBar.call(_self, false, true, true);
            }
        }
        var mousemoveHandler = function (ev) {
            if (_self.h_dragging) {
                bm.clearTextSelection()
                var x_diff = ev.pageX - initial_mouse_x
                var currentLeft = initial_handler_left + x_diff
                if (currentLeft < 0) {
                    initial_mouse_x = ev.pageX
                    initial_handler_left = 0
                }
                if (currentLeft + handler_width > bar_width) {
                    initial_mouse_x = ev.pageX
                    initial_handler_left = bar_width - handler_width
                }
                var handlerLeft = Math.round(currentLeft * handler_container_width_ratio);
                _self.elm.scrollLeft(handlerLeft);
                _self.update();
            }
        }
        this.h_handler.mousedown(function (ev) {
            initial_mouse_x = ev.pageX
            initial_handler_left = _self.h_handler.position().left
            bar_width = _self.h_bar.innerWidth()
            handler_container_width_ratio = _self.elm[0].scrollWidth / bar_width
            if (has_capture_support) {
                ev.target.setCapture()
            } else {
                document.addEventListener("mouseup", mouseupHandler, true)
                document.addEventListener("mousemove", mousemoveHandler, true)
            }
            _self.h_dragging = true;
            handler_width = _self.h_handler.outerWidth()
        })
        if (has_capture_support) {
            this.h_handler.mouseup(mouseupHandler).mousemove(mousemoveHandler)
        }
    }

    function _hEvents() {
        var _self = this;
        if (this.configs.use_bar) {
            _hBarEvents.call(this)
        } else {
            var step = this.configs.horizontal.step || $.scrollbar_horizontal_without_bar_step
            step = step == "auto" ? this.elm.width() : $.scrollbar_horizontal_without_bar_step;
            this.configs.horizontal.handle.left.click(function() {
                if(isVerticalScrollEnable){
                    _self.elm.animate({scrollTop: "-=" + step});
                }
                else {
                    _self.elm.animate({scrollLeft: "-=" + step});
                }

            });
            this.configs.horizontal.handle.right.click(function() {
                if(isVerticalScrollEnable) {
                    _self.elm.animate({scrollTop: "+=" + step});
                }
                else {
                    _self.elm.animate({scrollLeft: "+=" + step});
                }
            });
        }
    }

    function _vEvents() {
        var _self = this;
        var has_capture_support = this.elm[0].setCapture
        this.elm.bind("mousewheel.scrollbar", function(ev) {
            var container = _self.elm
            var scrollTop = container.scrollTop()
            var scrollHeight = container[0].scrollHeight
            var innerHeight = container.innerHeight();
            if ((scrollTop == 0 && ev.deltaY > 0) || (ev.deltaY < 0 && scrollTop + innerHeight == scrollHeight)) {
                return true
            }
            var to_be_scroll = scrollTop - ev.deltaY * ev.deltaFactor
            container.scrollTop(to_be_scroll).trigger('vscroll');
            _self.update();
            return false
        });
        this.v_bar.hover(function() {
            _self.mouse_on_v = true;
        }, function() {
            _self.mouse_on_v = false;
        });
        var initial_mouse_y
        var initial_handler_top
        var handler_container_height_ratio
        var handler_height
        var bar_height
        var mouseupHandler = function (ev) {
            _self.v_dragging = false;
            if(has_capture_support) {
                ev.target.releaseCapture()
            } else {
                document.removeEventListener("mouseup", mouseupHandler, true)
                document.removeEventListener("mousemove", mousemoveHandler, true)
            }
            if (!_self.mouse_on_v) {
                resetTimer.call(_self, true, false)
            }
        }
        var mousemoveHandler = function(ev) {
            if(_self.v_dragging) {
                bm.clearTextSelection()
                var y_diff = ev.pageY - initial_mouse_y
                var currentTop = initial_handler_top + y_diff
                if(currentTop < 0) {
                    initial_mouse_y = ev.pageY
                    initial_handler_top = 0
                }
                if(currentTop + handler_height > bar_height) {
                    initial_mouse_y = ev.pageY
                    initial_handler_top = bar_height - handler_height
                }
                var handlerTop = Math.round(currentTop * handler_container_height_ratio);
                _self.elm.scrollTop(handlerTop);
                _self.update();
            }
        }
        this.v_handler.mousedown(function(ev) {
            initial_mouse_y = ev.pageY
            initial_handler_top = _self.v_handler.position().top
            bar_height = _self.v_bar.innerHeight()
            handler_container_height_ratio = _self.elm[0].scrollHeight / bar_height
            if(has_capture_support) {
                ev.target.setCapture()
            } else {
                document.addEventListener("mouseup", mouseupHandler, true)
                document.addEventListener("mousemove", mousemoveHandler, true)
            }
            _self.v_dragging = true;
            handler_height = _self.v_handler.outerHeight()
        })
        if(has_capture_support) {
            this.v_handler.mouseup(mouseupHandler).mousemove(mousemoveHandler)
        }
    }

    function swipeEvents() {
        var _self = this;
        if(!$.fn.swipe) {
            return
        }
        _self.elm.swipe({
            swipe: function(event, direction) {
                if(_self.configs.show_horizontal && (direction == "left" || direction == "right")) {
                    var step = _self.configs.horizontal.step || $.scrollbar_horizontal_without_bar_step
                    step = step == "auto" ? _self.elm.width() : $.scrollbar_horizontal_without_bar_step;
                    if(direction == "right") {
                        _self.elm.animate({scrollLeft: "-=" + step});
                    } else {
                        _self.elm.animate({scrollLeft: "+=" + step});
                    }
                }
                if(_self.configs.show_vertical &&  (direction == "up" || direction == "down")) {
                    var delta = direction == "up" ? 1 : -1;
                    var scrollTop = _self.elm.scrollTop();
                    _self.elm.scrollTop(scrollTop - delta * 40);
                    this.ready_for_show = true
                    _self.update();
                    return false;
                }
            },
            threshold: 30
        });
    }

    window.Scrollbar = function(container, settings) {
        this.elm = container instanceof $ ? container : $(container);
        this.configs = $.extend(true, {}, $.scrollbar.defaults, settings);
        this.init();
        this.render();
        this.bindEvents();
        this.ready_for_show = this.configs.visible_on == "always" || this.configs.visible_on == "auto"
        this.update();
    };

    var _s = Scrollbar.prototype;

    _s.init = function() {
        this.elm.addClass("scrollable")
        let ribArgs = this.configs.reduce_margin_from_offset ? [true, true, false] : [false, false, this.configs.add_padding_with_offset]
        var rib = this.configs.reduce_margin_from_offset ? {
            left: -1 * this.elm.leftRib(...ribArgs),
            top: -1 * this.elm.topRib(...ribArgs),
            right: -1 * this.elm.rightRib(...ribArgs),
            bottom: -1 * this.elm.bottomRib(...ribArgs)
        } : {
            left: this.elm.leftRib(...ribArgs),
            top: this.elm.topRib(...ribArgs),
            right: this.elm.rightRib(...ribArgs),
            bottom: this.elm.bottomRib(...ribArgs)
        }
        this.v_top_offset = (this.configs.vertical.offset == null ? this.configs.vertical.top_offset : this.configs.vertical.offset) + rib.top
        this.v_right_offset = (this.configs.vertical.offset == null ? this.configs.vertical.right_offset : this.configs.vertical.offset) + rib.right
        this.v_left_offset = (this.configs.vertical.offset == null ? this.configs.vertical.left_offset : this.configs.vertical.offset) + rib.left
        this.h_top_offset = (this.configs.horizontal.offset == null ? this.configs.horizontal.top_offset : offset) + rib.top
        this.h_bottom_offset = (this.configs.horizontal.offset == null ? this.configs.horizontal.bottom_offset : offset) + rib.bottom
        this.h_left_offset = (this.configs.horizontal.offset == null ? this.configs.horizontal.left_offset : offset) + rib.left
    }

    _s.update = function() {
        if(this.configs.show_vertical) {
            if(this.configs.use_bar) {
                _vBarUpdate.call(this)
            }
        }
        if(this.configs.show_horizontal) {
            if(this.configs.use_bar) {
                _hBarUpdate.call(this, false)
            } else {
                _hNavigatorUpdate.call(this)
            }
        }
        return this;
    }

    _s.render = function () {
        if(this.configs.show_vertical) {
            if(this.configs.use_bar) {
                _vRender.call(this);
            }
        }
        if(this.configs.show_horizontal) {
            if(this.configs.use_bar) {
                _hRender.call(this);
            }
        }
        if(this.configs.swipe_scroll) {
            swipeEvents.call(this)
        }
    }

    _s.destroy = function() {
        if(this.h_hide_timer) {
            clearTimeout(this.h_hide_timer);
        }
        if(this.v_hide_timer) {
            clearTimeout(this.v_hide_timer);
        }
        this.elm.removeClass("scrollable vertical horizontal").off("hover").off("mousewheel.scrollbar")
        if (!this.elm.is(this.elm)) {
            this.elm.parent().unwrapInner()
        }
        if(this.v_bar) {
            this.v_bar.remove()
        }
        if(this.h_bar) {
            this.h_bar.remove()
        }
    }

    function showBar(v, h) {
        if(this.configs.show_vertical && v) {
            this.v_bar.stop(true, true).show()
            if(this.v_bar_shown) {
                _vPosition.call(this)
            }
            this.v_bar_shown = true;
        }
        if(this.configs.show_horizontal && h) {
            this.h_bar.stop(true, true).show()
            if(this.h_bar_shown) {
                _hPosition.call(this)
            }
            this.h_bar_shown = true;
        }
    }

    function clearTimer(v, h) {
        if(v && this.v_hide_timer) {
            clearTimeout(this.v_hide_timer);
            this.v_hide_timer = null;
            if(this.v_bar_shown) {
                showBar.call(this, v)
            }
        }
        if(h && this.h_hide_timer) {
            clearTimeout(this.h_hide_timer);
            this.h_hide_timer = null;
            if(this.h_bar_shown) {
                showBar.call(this, false, h)
            }
        }
    }

    function resetTimer(v, h) {
        clearTimer(v, h)
        if(v) {
            if(this.configs.hide_after) {
                showBar.call(this, v)
                this.v_hide_timer = setTimeout(function() {
                    this.v_hide_timer = null;
                    hideBar.call(this, true, false);
                }.bind(this), this.configs.hide_after);
            } else {
                hideBar.call(this, true, false);
            }
        }
        if(h) {
            if(this.configs.hide_after) {
                showBar.call(this, false, h)
                this.h_hide_timer = setTimeout(function() {
                    this.h_hide_timer = null;
                    hideBar.call(this, false, true);
                }.bind(this), this.configs.hide_after);
            } else {
                hideBar.call(this, false, true);
            }
        }
    }

    function hideBar(v, h, immediate) {
        if(!arguments.length) {
            v = h = true;
        }
        if(this.v_bar_shown && this.configs.show_vertical && v) {
            var anim = function () {
                this.v_bar_shown = false;
            }.bind(this)
            if(immediate) {
                this.v_bar.hide()
                anim()
            } else {
                this.v_bar.fadeOut(1500, anim);
            }
        }
        if(this.h_bar_shown && this.configs.show_horizontal && h) {
            var anim = function () {
                this.h_bar_shown = false;
            }.bind(this)
            if(immediate) {
                this.h_bar.hide()
                anim()
            } else {
                this.h_bar.fadeOut(1500, anim);
            }
        }
    }

    _s.bindEvents = function () {
        var _self = this;
        if(this.configs.visible_on == "hover") {
            this.elm.add((this.h_bar && this.v_bar) ? this.h_bar.add(this.v_bar) : (this.h_bar || this.v_bar)).hover(function () {
                this.ready_for_show = true
                clearTimer.call(this, true, true)
                setTimeout(function() {
                    this.update()
                }.bind(this), 0)
            }.bind(this), function () {
                if (!(this.v_dragging || this.h_dragging)) {
                    this.ready_for_show = false
                    setTimeout(function() {
                        if(!(this.mouse_on_v || this.mouse_on_h)) {
                            hideBar.call(this)
                        }
                    }.bind(this), 0)
                }
            }.bind(this));
        }
        if(this.configs.show_vertical) {
            _vEvents.call(this);
        }
        if(this.configs.show_horizontal) {
            _hEvents.call(this)
        }
        this.elm.find(".scrollbar").on("remove", function() {
            _self.destroy()
        })
    }

    var _super = Scrollbar.prototype;

    window.TableScroll = function(container, settings) {
        if(settings) {
            settings = $.extend({}, $.tablescrollbar.defaults, settings)
        }
        _super.constructor.call(this, container, settings);
    }

    TableScroll.inherit(Scrollbar);

    var _t = TableScroll.prototype;

    _t.init = function() {

        this.configs.show_horizontal = false; //we do not support horizontal scroll for table
        this.configs.show_vertical = false;

        this.container = this.elm.wrap("<div class='scrollable-table-wrapper'></div>").parent();
        var clonedRow = header.call(this);
        var false_header = this.false_header_row.closest(".scrollable-table-header-wrapper");
        this.container.before(false_header).css({marginTop: -1 * parseFloat(clonedRow.find("th:first").css("border-bottom-width"))});
        this.top_row_height = false_header.outerHeight();
        if(this.configs.vertical.height == "auto") {
            var height = this.container.parent().height();
            this.configs.vertical.height = height - this.top_row_height;
        } else {
            this.configs.vertical.height -= this.top_row_height;
        }

        this.container.scrollbar({
            show_vertical: true,
            vertical: {
                offset: 0,
                step: "auto",
            }
        })

    }

    _t.height = function(height) {
        _super.height.call(this, height - this.top_row_height);
    }

    _t.destroy = function() {
        _super.destroy.call(this)
        this.false_header_row.closest(".scrollable-table-header-wrapper").remove()
        var visibleTr = this.container.find("tr:has(th)");
        var originalTable = this.container.find("table").prepend(visibleTr.show());
        originalTable.parent().replaceWith(originalTable);
    }

    function header() {
        var visibleTr = this.container.find("tr:has(th)");
        var clonedRow = visibleTr.clone();
        visibleTr.copyDeepEvents(clonedRow, true);
        var falseHeader;
        var fakeTable;
        var originalTable = this.container.find("table");
        if(!this.false_header_row) {
            falseHeader = $("<div class='scrollable-table-header-wrapper'><table class='scrollable-table-header'></table></div>");
            fakeTable = falseHeader.find("table");
            originalTable.copyEvents(fakeTable, true);
        } else {
            fakeTable = this.false_header_row.closest("table");
        }
        fakeTable.html(this.container.find("colgroup").clone()).append(clonedRow);
        this.false_header_row = fakeTable.find("tr:first");
        visibleTr.hide()
        return clonedRow;
    }

    _t.content = function(toChange) {
        if(toChange) {
            var parents = toChange.parentsUntil("tr");
            var toReplace = this.false_header_row;
            if(parents.length) {
                parents.each(function() {
                    var index = $(this).index();
                    toReplace = toReplace.children().eq(index);
                })
            }
            var index = toChange.index();
            toReplace = toReplace.children().eq(index);
            var clonedTo = toChange.clone();
            toChange.copyDeepEvents(clonedTo, true)
            toReplace.replaceWith(clonedTo)
        } else {
            header.call(this);
            this.update();
        }
        return this.container.find("table");
    }

    _t.header = function() {
        return this.false_header_row;
    }
})()

bm.jquerify("scrollbar", Scrollbar);
bm.jquerify("tscrollbar", TableScroll);

if (bm && bm.ui_updaters) {
    bm.ui_updaters.push(function () {
        this.find(".scrollable").addBack(".scrollable").each(function() {
            var _this = this.jqObject
            var scrollbar = _this.scrollbar("inst")
            if (scrollbar) {
                scrollbar.update()
                return;
            }
            var config = _this.config("scrollable")
            if(_this.is("table")) {
                _this.tscrollbar(config)
            } else {
                _this.scrollbar(config)
            }
        })
    })
}