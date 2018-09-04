(function () {
    var _MOUSEHANDLER_DEFAULTS = {
        disable_text_selection: true, //option applicable if user want to disable text selection when mousedown
        skip: undefined //jquery selector | jquery | element that are skip from drag target
    };
    bm.MOUSEHANDLER_INSTANCE_COUNT = 1;

    function bind() {
        var _self = this;
        var initial_mouse_y;
        var last_mouse_y;
        var initial_mouse_x;
        var last_mouse_x;
        var mouseupHandler = this.mouseup_handler = function () {
            bm.enableTextSelection(this.ownerDocument);
            _self.mouse_on = false;
            if (_self.dragging) {
                _self.dragging = false;
                if (_self.shim) {
                    _self.shim.remove()
                }
                var elm = $(this);
                var data = {
                    elm: elm
                };
                if (_self.stop) {
                    _self.stop(data)
                }
                bm.trigger(_self, elm, _self.jevent_prefix, "stop", [data])
            }
            var _document = _self.element[0].ownerDocument;
            _document.removeEventListener("mouseup", _self.mouseup_handler, true);
            _document.removeEventListener("mousemove", _self.mousemove_handler, true)
        };
        var mousemoveHandler = this.mousemove_handler = function (ev) {
            var y_diff = ev.pageY - initial_mouse_y;
            var x_diff = ev.pageX - initial_mouse_x;
            if (_self.mouse_on && !_self.dragging) {
                if (Math.abs(x_diff) > 3 || Math.abs(y_diff) > 3) {
                    if (_self.beforeStart) {
                        var status = _self.beforeStart({elm: $(this)});
                        if (status === false) {
                            return status;
                        }
                    }
                    if (_self.options.shim) {
                        var shim = $("body > .iframe-shim");
                        if (!shim.length) {
                            shim = $("<div class='iframe-shim'></div>").appendTo(document.body)
                        }
                        _self.shim = shim.show()
                    }
                    var elm = $(this);
                    var data = {
                        elm: elm,
                        iX: initial_mouse_x,
                        iY: initial_mouse_y,
                        dX: 0,
                        dY: 0,
                        pX: ev.pageX,
                        pY: ev.pageY
                    };
                    if (_self.start) {
                        _self.start(data)
                    }
                    bm.trigger(_self, elm, _self.jevent_prefix, "start", [data]);
                    _self.dragging = true
                }
            }
            if (_self.dragging) {
                var data = {
                    elm: $(this),
                    iX: initial_mouse_x,
                    iY: initial_mouse_y,
                    dX: x_diff,
                    dY: y_diff,
                    dlX: ev.pageX - last_mouse_x,
                    dlY: ev.pageY - last_mouse_y
                };
                if (_self.move) {
                    _self.move(data)
                }
                bm.trigger(_self, $(this), _self.jevent_prefix, "move", [data]);
                last_mouse_y = ev.pageY;
                last_mouse_x = ev.pageX
            }
        };
        this.element.on("mousedown." + this.eventNamespace, this.mousedown_handler = function (ev) {
            if(_self.skip_elements && (_self.skip_elements.is(ev.target) || _self.skip_elements.isParentOf(ev.target))) {
                return;
            }
            if (_self.options.disable_text_selection) {
                bm.disableTextSelection(this.ownerDocument);
            }
            $(this).one("mouseup.prevent_selection", function () {
                bm.enableTextSelection(this.ownerDocument)
            });
            if (_self.options.cancel) {
                var hitOn = $(ev.target);
                if (hitOn.is(_self.options.cancel)) {
                    return;
                } else if (hitOn.closest(_self.options.cancel, _self.element).length) {
                    return;
                }
            }
            last_mouse_y = initial_mouse_y = ev.pageY;
            last_mouse_x = initial_mouse_x = ev.pageX;
            var _document = this.ownerDocument;
            _document.addEventListener("mouseup", _self.mouseup_handler = $.proxy(mouseupHandler, this), true);
            _document.addEventListener("mousemove", _self.mousemove_handler = $.proxy(mousemoveHandler, this), true);
            _self.mouse_on = true;
        })
    }

    bm.MouseHandler = function (element, options) {
        this.hash = bm.MOUSEHANDLER_INSTANCE_COUNT;
        this.eventNamespace = "mouse-handler-" + this.hash;
        this.element = element;
        this.options = $.extend({}, _MOUSEHANDLER_DEFAULTS, options);
        if(this.options.skip) {
            this.skip_elements = this.options.skip.jqObject
        }
        bind.apply(this)
    };

    var _m = bm.MouseHandler.prototype;

    _m.addElement = function (element) {
        element.on("mousedown." + this.eventNamespace, this.mousedown_handler)
    };

    _m.removeElement = function (element) {
        element.off("mousedown." + this.eventNamespace)
    };

    _m.destroy = function () {
        this.element.off("mousedown." + this.eventNamespace)
    };

    _m.restore = function () {
        this.element.on("mousedown." + this.eventNamespace, this.mousedown_handler)
    }
})();

(function () {
    var _DRAGGABLE_DEFAULTS = {
        helper: "self", // "clone" | function(<dragging jquery element>) | {parent: "same" | selector | jquery object (optional), offset: {left: n, top: n} (optional)}
        drop: undefined, // selector | jquery object | {element: selector | jquery object, offset: {left: n, top: n} (optional), ignore_scroll: false, intersect: "fit" (def) | "half" | n, area: [left, right, top, bottom] | jquery object (optional) }
        axis: undefined, // "x" | "y"
        sort: undefined, // jquery object | bm.Sortable object,
        sort_offset: {left: 0, top: 0},
        sort_area: undefined, // [left, right, top, bottom] | jquery object
        sort_ignore_scroll: false,
        containment: undefined, // "offset" | "parent" | {left: 0, top: 0, right: 0, bottom: 0}
        proxy: undefined, //jquery object | function returning a jquery object (draggee element is passed)
        shim: false,
        append_on_drop: true,
        sortable_on_drop: true,
        helper_position_adjust: true,
        remove_helper_after_done: true //option applicable if no drop and sort target
    };

    bm.Draggable = function (element, options) {
        bm.MouseHandler.apply(this, arguments);
        this.jevent_prefix = "drag";
        $.extend(this.options, _DRAGGABLE_DEFAULTS, options);
        if (this.element.css("position") == "static") {
            this.element.css({
                position: "relative"
            })
        }
        this.element.addClass("bmui-draggable");
        this.adjustOptions();
        this.temp = {}
    };

    bm.jquerify("draggable", bm.Draggable);
    var _d = bm.Draggable.inherit(bm.MouseHandler);

    _d.adjustOptions = function () {
        this.adjustDropOption();
        this.adjustSortOption()
    };

    _d.addElement = function (element) {
        element.addClass("bmui-draggable");
        bm.Draggable._super.addElement.apply(this, arguments);
        this.element = this.element.add(element)
    };

    _d.removeElement = function (element) {
        element.removeClass("bmui-draggable");
        bm.Draggable._super.removeElement.apply(this, arguments);
        this.element = this.element.not(element)
    };

    _d.adjustSortOption = function () {
        if (this.options.sort) {
            if (this.options.sort instanceof bm.Sortable) {
                this.sort = this.options.sort
            } else if (this.options.sort instanceof $) {
                this.sort = this.options.sort.obj(bm.Sortable)
            } else {
                this.sort = null
            }
        } else {
            this.sort = null
        }
    };

    _d.adjustDropOption = function () {
        if (this.options.drop) {
            var dropOption = this.options.drop;
            if (typeof dropOption == "string" || dropOption instanceof $) {
                this.drop = {
                    element: dropOption,
                    intersect: 100
                }
            } else {
                this.drop = Object.create(this.options.drop)
            }
            if (!this.drop.intersect || typeof this.drop.intersect == "string") {
                this.drop.intersect = this.drop.intersect == "fit" ? 100 : 50
            }
        } else {
            this.drop = undefined
        }
    };

    _d.getDropArea = function () {
        var area = this.drop.area;
        if (area) {
            if (area instanceof $) {
                area = area.rect()
            }
            return area
        }
        return false
    };

    _d.getSortArea = function () {
        var area = this.options.sort_area;
        if (area) {
            if (area instanceof $) {
                area = area.rect()
            }
            return area
        }
        return false
    };

    _d._getProxy = function () {
        if (this.options.proxy) {
            if (this.options.proxy instanceof $) {
                return this.options.proxy
            }
            return this.options.proxy(this.draggee)
        }
        return this.draggee
    };

    _d._calculateContainment = function () {
        if (this.options.containment) {
            var offset = $(this.mobile[0].offsetParent);
            var totalWidth = offset.outerWidth();
            var totalHeight = offset.outerHeight();
            var mobileWidth = this.mobile.outerWidth();
            var mobileHeight = this.mobile.outerHeight();
            var containment;
            if (this.options.containment == "offset") {
                containment = [0, 0, 0, 0]; //left, right, top, bottom
            } else if (this.options.containment == "parent") {
                var parent = this.mobile.parent();
                if (parent[0] == offset[0]) {
                    containment = [0, 0, 0, 0]
                } else {
                    var position = offset.position();
                    containment = [position.left, totalWidth - position.left - mobileWidth, position.top, totalHeight - position.top - mobileHeight]
                }
            } else if ($.isFunction(this.options.containment)) {
                containment = this.options.containment.call(this, this.mobile)
            } else {
                containment = [this.options.containment.left, this.options.containment.right, this.options.containment.top, this.options.containment.bottom]
            }
            this.containment = [[containment[0], totalWidth - mobileWidth - containment[1]], [containment[1], totalWidth - mobileWidth - containment[0]], [containment[2], totalHeight - mobileHeight - containment[3]], [containment[3], totalHeight - mobileHeight - containment[2]]]
        } else {
            this.containment = undefined
        }
    };

    _d.start = function (hash) {
        if (this.options.helper == "self" || this.options.helper instanceof $ || $.isFunction(this.options.helper)) {
            if (this.options.helper instanceof $) {
                this.draggee = this.options.helper
            } else if ($.isFunction(this.options.helper)) {
                this.draggee = this.options.helper.call(this, hash.elm)
            } else {
                this.draggee = hash.elm;
                this.draggee_position = hash.elm[0].style.position
            }
            if (!this.draggee.parent().length) {
                hash.elm.after(this.draggee);
                this.draggee.css({position: "absolute"});
                this.remove_helper_after_done = true
            }
            this.mobile = this._getProxy();
            this.draggee.addClass("bmui-draggable-cloned-helper");
            if (this.options.helper != "self" && this.options.helper_position_adjust !== false) {
                this.draggee.offset({left: hash.pX - this.draggee.outerWidth() / 2, top: hash.pY - this.draggee.outerHeight() / 2})
            }
        } else {
            this.remove_helper_after_done = true;
            this.draggee = hash.elm.clone();
            this.draggee.css({position: "absolute"}).removeClass("bmui-draggable").addClass("bmui-draggable-cloned-helper");
            var parent = $(document.body);
            if (this.options.helper.parent) {
                if (this.options.helper.parent instanceof $) {
                    parent = this.options.helper.parent
                } else if (this.options.helper.parent != "same") {
                    parent = $(this.options.helper.parent)
                } else {
                    parent = hash.elm.parent()
                }
            }
            this.draggee.appendTo(parent);
            var elementOffset = hash.elm.offset();
            var draggeeOffset = this.options.helper.offset ? {
                left: this.options.helper.offset.left + elementOffset.left,
                top: this.options.helper.offset.top + elementOffset.top
            } : elementOffset;
            this.draggee.offset(draggeeOffset);
            this.mobile = this._getProxy()
        }
        this.positionReference = bm.positionReference(this.mobile);
        if ((this.positionReference - 1) % 3 != 0) {
            var left = this.mobile.css("left");
            if (left == "auto") {
                left = 0;
            }
            this.startX = parseFloat(left)
        } else {
            this.startX = parseFloat(this.mobile.css("right"))
        }
        if ((this.positionReference + 1) % 3 == 0) {
            this.startX2 = parseFloat(this.mobile.css("right"))
        }
        if (this.positionReference < 3 || this.positionReference > 5) {
            var top = this.mobile.css("top");
            if (top == "auto") {
                top = 0;
            }
            this.startY = parseFloat(top)
        } else {
            this.startY = parseFloat(this.mobile.css("bottom"))
        }
        if (this.positionReference > 5) {
            this.startY2 = parseFloat(this.mobile.css("bottom"))
        }
        this._initDrop();
        this._calculateContainment()
    };

    _d.stop = function (hash, evt) {
        this.draggee.removeClass("bmui-draggable-cloned-helper");
        if (this.dropee) {
            this.dropee.removeClass("bmui-droppable-target bmui-droppable-active-target");
            if (this.drop_target) {
                var evData = {
                    drag: this.draggee,
                    drop: this.drop_target
                };
                if (this.options.append_on_drop) {
                    this.drop_target.append(this.draggee);
                    var drageeStyle = this.draggee[0].style;
                    if (this.draggee[0] == hash.elm[0]) {
                        drageeStyle.position = this.draggee_position
                    } else {
                        drageeStyle.position = hash.elm[0].style.position || "";
                        var pOffset = $(this.draggee[0].offsetParent).offset();
                        evData.position = {
                            left: parseInt(drageeStyle.left) - pOffset.left - (this.drop.offset ? this.drop.offset.left : 0),
                            top: parseInt(drageeStyle.top) - pOffset.top - (this.drop.offset ? this.drop.offset.top : 0)
                        };
                        drageeStyle.left = drageeStyle.top = ""
                    }
                }
                bm.trigger(this, hash.elm, "drag", "drop", evData);
                if (!this.options.append_on_drop && this.remove_helper_after_done) {
                    this.draggee.remove()
                }
            }
        }
        if (this.sort_target_on) {
            this.sort_target_on = false;
            var sortable = this.sort;
            sortable.draggable.options.stop.call(sortable.draggable, {elm: this.draggee}, true, true);
            var placeholder = sortable.placeholder;
            placeholder.replaceWith(this.draggee);
            this.draggee.css({
                position: '',
                left: '',
                top: '',
                right: '',
                bottom: ''
            });
            bm.trigger(this, hash.elm, "drag", "sort", {
                sort: this.draggee,
                obj: sortable
            });
            if (this.options.helper == "self") {
                this.element = this.element.not(this.draggee);
                this.removeElement(this.draggee)
            }
            if (this.options.sortable_on_drop) {
                sortable.addHandle(this.draggee)
            }
        } else if (!this.drop_target) {
            if (this.draggee[0] != hash.elm[0] && this.options.remove_helper_after_done !== false) {
                this.draggee.remove()
            }
        }
        this.drop_target = undefined
    };

    _d._initDrop = function () {
        if (this.drop) {
            var dropOption = this.drop;
            var dropSelector = dropOption.element;
            if (typeof dropSelector == "string") {
                dropOption.element = this.dropee = $(dropSelector)
            } else {
                dropOption.element = this.dropee = dropSelector
            }
            this.dropee.addClass("bmui-droppable-target");
            if (!this.dropee.length) {
                this.dropee = null
            }
        } else {
            this.dropee = null
        }
    };

    _d._posInContainment = function (dim, pos) {
        if (this.containment) {
            var index = {left: 0, right: 1, top: 2, bottom: 3}[dim];
            var value = this.containment[index];
            if (pos < value[0]) {
                return value[0]
            }
            if (pos > value[1]) {
                return value[1]
            }
        }
        return pos
    };

    _d.move = function (hash) {
        if (!this.options.axis || this.options.axis == "x") {
            switch (this.positionReference) {  //left top, right top, left bottom, right bottom
                case 2:
                case 5:
                case 8:
                    this.mobile.css("right", this._posInContainment("right", this.startX2 - hash.dX));
                case 0:
                case 3:
                case 6:
                    this.mobile.css("left", this._posInContainment("left", this.startX + hash.dX));
                    break;
                case 1:
                case 4:
                case 7:
                    this.mobile.css("right", this._posInContainment("right", this.startX - hash.dX));
                    break;
            }
        }
        if (!this.options.axis || this.options.axis == "y") {
            switch (this.positionReference) {  //left top, right top, left bottom, right bottom
                case 6:
                case 7:
                case 8:
                    this.mobile.css("bottom", this._posInContainment("bottom", this.startY2 - hash.dY));
                case 0:
                case 1:
                case 2:
                    this.mobile.css("top", this._posInContainment("top", this.startY + hash.dY));
                    break;
                case 3:
                case 4:
                case 5:
                    this.mobile.css("bottom", this._posInContainment("bottom", this.startY - hash.dY));
                    break;
            }
        }
        var dropped;
        if (!this.sort_target_on && this.dropee) {
            dropped = this._dropIntersect()
        }
        if (this.sort && !dropped) {
            this._sortIntersect(hash)
        }
    };

    _d._dropIntersect = function () {
        var option = this.drop;
        this.dropee.removeClass("bmui-droppable-active-target");
        this.drop_target = undefined;
        var helperRect = this.draggee.rect();
        var highestIntersect = 0;
        var highestDropee;
        var area = this.getDropArea();
        if (area) {
            var _in = bm.intersect(area, helperRect);
            if (_in < 50) {
                return;
            }
        }
        var offset = option.offset;
        if (offset && !option.ignore_scroll && this.dropee[0].ownerDocument != this.draggee[0].ownerDocument) {
            var scroller = bm.getTopScroller(this.dropee[0].ownerDocument);
            offset = Object.create(offset);
            offset.left -= scroller.scrollLeft();
            offset.top -= scroller.scrollTop()
        }
        this.dropee.each(function () {
            var dropee = $(this);
            var dropeeRect = dropee.rect(offset);
            var intersect = bm.intersect(dropeeRect, helperRect);
            if (intersect > highestIntersect) {
                highestIntersect = intersect;
                highestDropee = dropee
            }
        });
        if (highestDropee && highestIntersect >= this.drop.intersect) {
            highestDropee.addClass("bmui-droppable-active-target");
            this.drop_target = highestDropee;
            return true
        }
    };

    _d._sortIntersect = function (hash) {
        if (!this.sort.element.length) {
            return
        }
        var helperRect = this.draggee.rect();
        var area = this.getSortArea();
        if (area) {
            var _in = bm.intersect(area, helperRect);
            if (_in < 50) {
                return;
            }
        }
        var optionOffset = this.options.sort_offset;
        if ($.isFunction(optionOffset)) {
            optionOffset = optionOffset(this.sort.element)
        }
        if (optionOffset && !this.options.sort_ignore_scroll && this.sort.element[0].ownerDocument != this.draggee[0].ownerDocument) {
            var scroller = bm.getTopScroller(this.sort.element[0].ownerDocument);
            optionOffset = Object.create(optionOffset);
            optionOffset.left -= scroller.scrollLeft();
            optionOffset.top -= scroller.scrollTop()
        }
        var intersect;
        var highestIntersect = 0;
        var highestDropee;
        var _self = this;
        this.sort.element.each(function () {
            var dropee = $(this);
            var dropeeRect = dropee.rect(optionOffset);
            intersect = bm.intersect(dropeeRect, helperRect);
            if (intersect) {
                if (_self.sort_target_on) {
                    return false
                }
                if (highestIntersect < intersect) {
                    highestIntersect = intersect;
                    highestDropee = dropee
                }
            }
        });
        if (intersect || highestIntersect) {
            var modifiedHash = {
                elm: hash.elm,
                iX: hash.iX + hash.dX - hash.dlX,
                iY: hash.iY + hash.dY - hash.dlY,
                dX: 0,
                dY: 0,
                dlX: hash.dlX,
                dlY: hash.dlY
            };
            if (!this.sort_target_on) {
                this.sort.draggable.draggee = this.draggee;
                this.sort.draggable.start(modifiedHash, true, highestDropee);
                this.sort_target_on = true
            }
            this.sort.draggable.options.move.call(this.sort.draggable, modifiedHash, true, optionOffset);
            return true
        } else if (this.sort_target_on) {
            this.sort_target_on = false;
            this.sort.draggable.options.stop.call(this.sort.draggable, {elm: hash.elm}, true)
        }
    };

    _d.dropTarget = function (newTarget) {
        if (this.dropee) {
            this.dropee.removeClass("bmui-droppable-target");
            this.dropee = null
        }
        this.options.drop = newTarget;
        this.adjustDropOption();
        this._initDrop()
    };

    _d.sortElement = function (sortable, offset, area, ignore_scroll) {
        if (!sortable) {
            this.options.sort = this.sort = null;
            return;
        }
        this.options.sort = sortable;
        this.options.sort_offset = offset;
        this.options.sort_area = area;
        this.options.sort_ignore_scroll = ignore_scroll;
        this.adjustSortOption()
    };

    _d.destroy = function () {
        this.element.removeClass("bmui-draggable");
        bm.MouseHandler.prototype.destroy.apply(this, arguments)
    };

    _d.restore = function () {
        this.element.addClass("bmui-draggable");
        bm.MouseHandler.prototype.restore.apply(this, arguments)
    }
})();

(function () {
    var _SORTABLE_DEFAULTS = {
        axis: undefined, //"x" | "y",
        placeholder: true,  // must be true for table sort
        placeholder_size: false, // whether placeholder will have element's size
        offset: undefined, // {left: n, top: n}
        intersect: 10, // n (pixel overlapped)
        helper: "self", // "clone" | function(<dragging jquery element>) | {parent: "same" | selector | jquery object (optional), offset: {left: n, top: n} (optional)}
        handle: undefined, // selector (children),
        shim: false,
        helper_position_adjust: true
    };

    bm.Sortable = function (element, options) {
        var _self = this;
        this.options = $.extend({}, _SORTABLE_DEFAULTS, options);
        this.element = element;
        this.handles = this.createHandles();
        var table_sort = false;
        if (this.handles.is("tr") && this.options.helper == "self") {
            table_sort = true;
            this.options.helper = function (elm) {
                var helper_table = $("<table class='tr-sortable-proxy-table' style='table-layout: fixed; position: absolute;'></table>");
                var original_table = elm.closest("table");
                original_table.after(helper_table);
                helper_table.width(original_table.width());
                helper_table.position({
                    my: "left top",
                    at: "left top",
                    of: elm
                });
                helper_table.append(elm);
                return helper_table
            };
            this.options.helper_position_adjust = false
        }
        var dirag = this.draggable = new bm.Draggable(this.handles, {
            axis: this.options.axis,
            helper: this.options.helper,
            helper_position_adjust: this.options.helper_position_adjust,
            remove_helper_after_done: !table_sort,
            stop: function (hash, from_draggable, is_final) {
                if (!from_draggable) {
                    if (_self.options.placeholder) {
                        var doSort = true;
                        if (_self.options.beforeSort) {
                            doSort = _self.options.beforeSort(_self.placeholder, _self.placeholder_target)
                        }
                        if (doSort) {
                            if (table_sort) {
                                var proxy_table = hash.elm.closest("table");
                                _self.placeholder.after(hash.elm).remove();
                                proxy_table.remove()
                            } else {
                                _self.placeholder.replaceWith(hash.elm)
                            }
                        } else {
                            _self.placeholder.remove()
                        }

                    }
                    $.each(this.element_cache, function (k, v) {
                        hash.elm[0].style[k] = v
                    });
                    bm.trigger(_self, hash.elm, "sort", "sort", {elm: hash.elm, obj: _self});
                    if (_self.options.stop) {
                        _self.options.stop.call(_self);
                    }
                } else if (_self.options.placeholder && !is_final) {
                    _self.placeholder.remove()
                }
                if (_self.placeholder_target) {
                    _self.placeholder_target.removeClass("bmui-sortable-active-target")
                }
            },
            move: function (hash, from_draggable, offset) {
                _self.placeholderPosition(hash, from_draggable, offset);
                bm.trigger(_self, hash.elm, "sort", "drag", {elm: hash.elm, obj: _self})
            },
            shim: this.options.shim
        });
        dirag.jevent_prefix = "sort";
        dirag.beforeStart = function (hash, from_draggable) {
            if (!from_draggable) {
                return bm.trigger(_self, hash.elm, "sort", "beforeStart", {elm: hash.elm, obj: _self})
            }
        };
        dirag.start = function (hash, from_draggable, sortable_target) {
            if (table_sort) {
                _self.elm_parent = hash.elm.parent();
                _self.elm_index = hash.elm.index()
            }
            if (!from_draggable) {
                this.element_cache = {
                    position: hash.elm[0].style.position || "",
                    left: hash.elm[0].style.left || "",
                    top: hash.elm[0].style.top || "",
                    right: hash.elm[0].style.right || "",
                    bottom: hash.elm[0].style.bottom || ""
                };
                bm.Draggable.prototype.start.apply(this, arguments)
            }
            if (_self.options.placeholder) {
                _self.placeholder = table_sort ? $("<tr class='bmui-sortable-placeholder'><td colspan='" + hash.elm.find(">td").toArray().sum('+(this.getAttribute("colspan") || "1")') + "'></td></tr>") : $("<div class='bmui-sortable-placeholder'></div>");

                if (table_sort) {
                    if (_self.elm_index == 0) {
                        _self.elm_parent.prepend(_self.placeholder)
                    } else {
                        _self.elm_parent.children().eq(_self.elm_index - 1).after(_self.placeholder)
                    }
                } else if (sortable_target) {
                    sortable_target.append(_self.placeholder)
                } else {
                    hash.elm.before(_self.placeholder)
                }
                if (_self.options.placeholder_size) {
                    _self.placeholder.outerWidth(hash.elm.outerWidth());
                    _self.placeholder.outerHeight(hash.elm.outerHeight())
                }
            } else {
                _self.placeholder = hash.elm
            }
            _self.placeholder_target = _self.placeholder.parent().addClass("bmui-sortable-active-target");
            _self.draggee = this.draggee;
            _self.candidate = hash.elm;
            if (!from_draggable) {
                bm.trigger(_self, hash.elm, "sort", "start", {elm: hash.elm, obj: _self})
            }
        }
    };

    bm.jquerify("sortable", bm.Sortable);

    var _s = bm.Sortable.prototype;

    _s.placeholderPosition = function (hash, from_draggable, offset) {
        var _self = this;
        var helperRect = this.draggee.rect();
        var option = this.options;
        var intersect = option.intersect;
        var placeholderReplacee;
        var replaceFunc;
        var emptyTargetSelector = ":not(:has(>" + (this.options.handle || "") + ":visible))";
        var appendIntersect = intersect || 20;
        this.element.filter(emptyTargetSelector).each(function () {
            var dropee = $(this);
            var dropeeRect = dropee.rect(from_draggable ? offset : option.offset);
            var intersect = bm.intersect(dropeeRect, helperRect);
            if (intersect > appendIntersect) {
                placeholderReplacee = dropee;
                replaceFunc = "append";
                appendIntersect = intersect
            }
        });
        var center_point = {x: (helperRect.left + helperRect.right) / 2, y: (helperRect.top + helperRect.bottom) / 2};
        this.handles.not(this.candidate).each(function () {
            var dropee = $(this);
            var dropeeRect = dropee.rect(from_draggable ? offset : option.offset);
            var intersect = bm.intersect(dropeeRect, helperRect);
            if (intersect > appendIntersect) {
                placeholderReplacee = dropee;
                appendIntersect = intersect;
                var sign = (dropeeRect.right - dropeeRect.left) * (center_point.y - dropeeRect.bottom) - (dropeeRect.top - dropeeRect.bottom) * (center_point.x - dropeeRect.left);
                replaceFunc = sign < 0 ? "before" : "after"
            }
        });
        if (replaceFunc) {
            placeholderReplacee[replaceFunc](_self.placeholder);
            if (!_self.placeholder_target.is(_self.placeholder.parent())) {
                _self.placeholder_target.removeClass("bmui-sortable-active-target");
                _self.placeholder_target = _self.placeholder.parent().addClass("bmui-sortable-active-target")
            }
        }
    };

    _s.extendObjElement = _s.addElement = function (element) {
        $.merge(this.element, element);
        var addeds = this.createHandles(element);
        this.handles = this.handles.add(addeds);
        this.draggable.addElement(addeds)
    };

    _s.removeObjElement = _s.removeElement = function (element) {
        var _self = this;
        element.each(function () {
            _self.element.splice(_self.element.index(this), 1)
        });
        var addeds = this.createHandles(element);
        this.handles = this.handles.not(addeds);
        this.draggable.removeElement(addeds)
    };

    _s.addHandle = function (element) {
        this.handles = this.handles.add(element);
        this.draggable.addElement(element)
    };

    _s.createHandles = function (element) {
        var handle = (element || this.element).children(":visible");
        if (this.options.handle) {
            return handle.filter(this.options.handle)
        }
        return handle
    };

    _s.destroy = function () {
        this.draggable.destroy()
    };

    _s.restore = function () {
        this.draggable.restore()
    }
})();

(function () {
    var _RESIZABLE_DEFAULTS = {
        direction: ["t", "r", "b", "l"],
        reverse: undefined, // jquery element | function returning element
        containment: undefined, // "offset" | "parent" | {left: 0, top: 0, right: 0, bottom: 0} | [[t_min, t_max], [r_min, r_max], [b_min, b_max], [l_min, l_max]]
        proxy: undefined, //jquery object | function returning a jquery object (draggee element is passed)
        shim: false,
        limit: undefined // [width_min, width_max, height_min, height_max]
    };

    bm.Resizable = function (element, options) {
        this.options = $.extend({}, _RESIZABLE_DEFAULTS, options);
        this.element = element.addClass("bmui-resizable");
        this.initHandles()
    };

    var _r = bm.Resizable.prototype;

    bm.jquerify("resizable", bm.Resizable);

    _r._posInContainment = function (dim, pos) {
        var afterContain = pos;
        if (this.containment) {
            var value = this.containment[dim];
            if (pos < value[0]) {
                afterContain = value[0]
            } else if (pos > value[1]) {
                afterContain = value[1]
            }
        }
        if (this.limit) {
            if (dim == "width" || dim == "height") {
                if (afterContain < this.limit[0]) {
                    return this.limit[0]
                }
                if (afterContain > this.limit[1]) {
                    return this.limit[1]
                }
            } else {
                if (afterContain < this.limit["min" + dim + "point"]) {
                    return this.limit["min" + dim + "point"]
                }
                if (afterContain > this.limit["max" + dim + "point"]) {
                    return this.limit["max" + dim + "point"]
                }
            }
        }
        return afterContain
    };

    _r._calculateContainment = function (axis, isLeft) {
        this.containment = undefined;
        if (this.options.containment) {
            var element = this.resize_element;
            var offset = $(element[0].offsetParent);
            var containment;
            var total;
            var dim_props;
            var occopied;
            if (axis == "x") {
                total = offset.outerWidth();
                occopied = element.outerWidth();
                dim_props = ["left", "right", "width"]
            } else {
                total = offset.outerHeight();
                occopied = element.outerHeight();
                dim_props = ["top", "bottom", "height"]
            }
            if (this.options.containment == "offset") {
                containment = [0, 0]; //left, right | top, bottom
            } else if (this.options.containment == "parent") {
                var parent = element.parent();
                if (parent[0] == offset[0]) {
                    containment = [0, 0]
                } else {
                    var position = offset.position();
                    containment = [position[dim_props[0]], total - position[dim_props[0]] - occopied]
                }
            } else if ($.isFunction(this.options.containment)) {
                containment = this.options.containment.call(this, element, axis)
            } else {
                containment = [this.options.containment[dim_props[0]], this.options.containment[dim_props[1]]]
            }
            if (!this.containment) {
                this.containment = {};
                var positionReference = bm.positionReference(element);
                var mod = axis == "x" ? positionReference % 3 : (positionReference < 3 ? 0 : 1);
                switch (mod) {
                    case 0:
                        if (isLeft) {
                            this.containment[dim_props[0]] = [containment[0], this.startIX + occopied - 10];
                            this.containment[dim_props[2]] = [10, this.startIX + occopied - containment[0]]
                        } else {
                            this.containment[dim_props[2]] = [10, occopied + parseFloat(element.pposition(dim_props[1])) - containment[1]]
                        }
                        break;
                    case 1:
                        if (isLeft) {
                            this.containment[dim_props[2]] = [10, occopied + parseFloat(element.pposition(dim_props[0])) - containment[0]]
                        } else {
                            this.containment[dim_props[1]] = [containment[1], this.startDX + occopied - 10];
                            this.containment[dim_props[2]] = [10, occopied + this.startDX - containment[1]]
                        }
                        break;
                    default:
                        if (isLeft) {
                            this.containment[dim_props[0]] = [containment[0], this.startIX + occopied - 10]
                        } else {
                            this.containment[dim_props[1]] = [containment[1], this.startDX + occopied - 10]
                        }
                }
            }
        }
        this.limit = undefined;
        if (this.options.limit) {
            var element = this.resize_element;
            this.limit = axis == "x" ? [this.options.limit[0], this.options.limit[1]] : [this.options.limit[2], this.options.limit[3]];
            var props = axis == "x" ? ["left", "right"] : ["top", "bottom"];
            var total = axis == "x" ? element.outerWidth() : element.outerHeight();
            var current_firstpoint = parseFloat(element.pposition(props[0]));
            var current_secondpoint = parseFloat(element.pposition(props[1]));
            if (this.limit[0] != undefined) {
                this.limit["max" + props[0] + "point"] = total - this.limit[0] + current_firstpoint;
                this.limit["max" + props[1] + "point"] = total - this.limit[0] + current_secondpoint
            }
            if (this.limit[1] != undefined) {
                this.limit["min" + props[0] + "point"] = total - this.limit[1] + current_firstpoint;
                this.limit["min" + props[1] + "point"] = total - this.limit[1] + current_secondpoint
            }
        }
    };

    _r.initHandles = function () {
        var _self = this;
        this.options.direction.every(function () {
            var div = $("<div class='bmui-resize-handle'></div>");
            if (this == "r") {
                div.addClass("resize-right")
            } else if (this == "l") {
                div.addClass("resize-left")
            } else if (this == "t") {
                div.addClass("resize-top")
            } else {
                div.addClass("resize-bottom")
            }
            _self.element.append(div)
        });
        var dragStart = function (hash, axis) {
            var element = hash.elm.parent();
            _self.reverse_element = undefined;
            if (_self.options.proxy) {
                if (_self.options.proxy instanceof $) {
                    _self.resize_element = _self.options.proxy
                } else {
                    _self.resize_element = _self.options.proxy(this.draggee.closest(".bmui-resizable"))
                }
            } else {
                _self.resize_element = this.draggee.closest(".bmui-resizable")
            }
            if (_self.options.reverse) {
                if (_self.options.reverse instanceof $) {
                    _self.reverse_element = _self.options.reverse
                } else if ($.isFunction(_self.options.reverse)) {
                    _self.reverse_element = _self.options.reverse(element)
                }
            }
            bm.trigger(_self, hash.elm.parent(), "resize", "prestart", $.extend({}, hash, {elm: hash.elm.parent(), obj: _self}));
            var isLeft = axis == "x" ? this.draggee.is(".resize-left") : this.draggee.is(".resize-top");
            var position = _self.resize_element.css("position");
            _self.iproperty = undefined;
            _self.dproperty = undefined;
            if (position == "static" || position == "relative") {
                if (isLeft) {
                    _self.dproperty = _self.riproperty = axis == "x" ? "width" : "height"
                } else {
                    _self.rdproperty = _self.iproperty = axis == "x" ? "width" : "height"
                }
            } else {
                var dim_props = axis == "x" ? ["left", "right", "width"] : ["top", "bottom", "height"];

                function defineProperty(element, ip, dp, isLeft) {
                    var positionReference = bm.positionReference(element);
                    var mod = axis == "x" ? positionReference % 3 : (positionReference < 3 ? 0 : 1);
                    switch (mod) {
                        case 0:
                            if (isLeft) {
                                _self[ip] = dim_props[0];
                                _self[dp] = dim_props[2]
                            } else {
                                _self[ip] = dim_props[2]
                            }
                            break;
                        case 1:
                            if (isLeft) {
                                _self[dp] = dim_props[2]
                            } else {
                                _self[dp] = dim_props[1];
                                _self[ip] = dim_props[2]
                            }
                            break;
                        default:
                            if (isLeft) {
                                _self[ip] = dim_props[0]
                            } else {
                                _self[dp] = dim_props[1]
                            }
                    }
                }

                defineProperty(_self.resize_element, "iproperty", "dproperty", isLeft);
                if (_self.reverse_element) {
                    defineProperty(_self.reverse_element, "riproperty", "rdproperty", !isLeft)
                }
            }
            _self.startIX = _self.iproperty ? parseFloat(_self.resize_element.css(_self.iproperty)) : undefined;
            _self.startDX = _self.dproperty ? parseFloat(_self.resize_element.css(_self.dproperty)) : undefined;
            if (_self.reverse_element) {
                _self.startRIX = _self.riproperty ? parseFloat(_self.reverse_element.css(_self.riproperty)) : undefined;
                _self.startRDX = _self.rdproperty ? parseFloat(_self.reverse_element.css(_self.rdproperty)) : undefined
            }
            _self.temp = this.temp;
            this.mobile = _self.resize_element;
            _self._calculateContainment(axis, isLeft);
            bm.trigger(_self, hash.elm.parent(), "resize", "start", $.extend({}, hash, {elm: hash.elm.parent(), obj: _self}))
        };
        this.x_draggable = new bm.Draggable(this.element.find(">.bmui-resize-handle.resize-left, >.bmui-resize-handle.resize-right"), {
            axis: "x",
            shim: this.options.shim,
            containment: this.options.containment,
            start: function (hash) {
                dragStart.call(this, hash, "x")
            }
        });
        this.y_draggable = new bm.Draggable(this.element.find(">.bmui-resize-handle.resize-top, >.bmui-resize-handle.resize-bottom"), {
            axis: "y",
            shim: this.options.shim,
            containment: this.options.containment,
            start: function (hash) {
                dragStart.call(this, hash, "y")
            }
        });
        this.x_draggable.move = this.y_draggable.move = function (hash) {
            var diff = hash.elm.is(".resize-left, .resize-right") ? hash.dX : hash.dY;
            var inc_value = _self.startIX;
            var dec_value = _self.startDX;
            var dims = {};
            if (inc_value !== undefined) {
                inc_value += diff;
                var new_inc = _self._posInContainment(_self.iproperty, inc_value);
                if (new_inc != inc_value) {
                    diff -= inc_value - new_inc
                }
                dims[_self.iproperty] = new_inc;
                _self.resize_element.css(_self.iproperty, new_inc)
            }
            if (dec_value !== undefined) {
                dec_value -= diff;
                var new_inc = _self._posInContainment(_self.dproperty, dec_value);
                if (new_inc != inc_value) {
                    diff += dec_value - new_inc
                }
                dims[_self.dproperty] = new_inc;
                _self.resize_element.css(_self.dproperty, new_inc)
            }
            if (_self.reverse_element) {
                inc_value = _self.startRIX;
                dec_value = _self.startRDX;
                if (inc_value !== undefined) {
                    inc_value += diff;
                    _self.reverse_element.css(_self.riproperty, inc_value)
                }
                if (dec_value !== undefined) {
                    dec_value -= diff;
                    _self.reverse_element.css(_self.rdproperty, dec_value)
                }
            }
            bm.trigger(_self, hash.elm.parent(), "resize", "resize", $.extend({}, hash, {elm: hash.elm.parent(), dims: dims}))
        };
        this.x_draggable.element.add(this.y_draggable.element).on("drag:stop", function (ev, hash) {
            bm.trigger(_self, hash.elm.parent(), "resize", "stop", $.extend({}, hash, {elm: hash.elm.parent()}))
        })
    };

    _r.destroy = function () {
        this.element.removeClass("bmui-resizable");
        this.x_draggable.destroy();
        this.x_draggable.element.remove();
        this.y_draggable.destroy();
        this.y_draggable.element.remove()
    }
})();