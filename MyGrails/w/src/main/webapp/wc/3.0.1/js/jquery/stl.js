(function() {
    var _HLS_DEFAULTS = {
        level_depth: 30,
        entry_class: "bmui-stl-entry",
        sub_entry_class: "bmui-stl-sub-container",
        entry_container_class: "bmui-stl-entry-container",
        shim: false,
        placeholder_size: true,
        intersect: 60
    }

    bm.SortableTreeList = function(element, options) {
        var _self = this;
        this.options = $.extend({}, _HLS_DEFAULTS, options)
        this.element = element
        this.handles = element.find("." + this.options.entry_class)
        this._assignLevels()
        var dirag = this.draggable = new bm.Draggable(this.handles, {
            helper: function(dragee) {
                var parent = dragee.parent()
                var offset = parent.offset()
                var width = parent.width()
                var height = parent.height()
                parent.css("position", "absolute")
                parent.offset(offset)
                parent.width(width)
                parent.height(height)
                return parent
            },
            remove_helper_after_done: false,
            helper_position_adjust: false,
            move: function() {
                _self.placeholderPosition()
            },
            stop: function(hash) {
                _self.placeholder.replaceWith(hash.elm.parent())
                hash.elm.parent().css(this.helper_cache)
                hash.elm.attr("entry-level", _self.set_level)
                __setLevel.bind(_self)(hash.elm.next(), _self.set_level + 1)
                bm.trigger(_self, hash.elm, "stl", "change", {item: hash.elm, parent: _self.getParent(hash.elm)})
            },
            shim: this.options.shim
        })
        dirag.jevent_prefix = "stl"
        dirag.start = function(hash) {
            var draggee_parent = hash.elm.parent() 
            this.helper_cache = {
                position: draggee_parent[0].style.position || "",
                left: draggee_parent[0].style.left || "",
                top: draggee_parent[0].style.top || "",
                right: draggee_parent[0].style.right || "",
                bottom: draggee_parent[0].style.bottom || "",
                width: draggee_parent[0].style.width || "",
                height: draggee_parent[0].style.height || ""
            }
            bm.Draggable.prototype.start.apply(this, arguments)
            _self.placeholder = $("<div class='bmui-sortable-placeholder'></div>")
            draggee_parent.before(_self.placeholder)
            if(_self.options.placeholder_size) {
                _self.placeholder.outerWidth(draggee_parent.outerWidth())
                _self.placeholder.outerHeight(draggee_parent.outerHeight())
            }
            _self.draggee = this.draggee
            _self.candidate = hash.elm
            _self.level_0_x = _self.element.offset().left
            _self.draggee_level = hash.elm.attr("entry-level")
        }
    }

    bm.jquerify("sortable-tree-list", bm.SortableTreeList)

    var _s = bm.SortableTreeList.prototype

    function __setLevel(parent, level) {
        var _self = this
        parent.children().each(function() {
            $(this).children("." + _self.options.entry_class).attr("entry-level", level)
            __setLevel.bind(_self)($(this).children("." + _self.options.sub_entry_class), level + 1)
        })
    }

    _s._assignLevels = function() {
        __setLevel.bind(this)(this.element, 0)
    }

    _s._setPlaceholderLevel = function(level) {
        var prev_sibling = this.placeholder.prev()

        function __getDecreasedValue(next_level) {
            var next_sibling = this.placeholder.next()
            if(next_sibling.is(this.draggee)) {
                next_sibling = this.draggee.next()
            }
            while(!next_sibling.length) {
                var top_entry = this.placeholder.parent().parent()
                top_entry.after(this.placeholder)
                if(!this.draggee.prev().length && !this.draggee.next().length) {
                    var remove_parent = this.draggee.parent()
                    this.placeholder.after(this.draggee)
                    this.draggee.attr("entry-level",next_level - 1)
                    __setLevel.bind(this)(this.draggee.next(), next_level)
                    remove_parent.remove()
                }
                next_level--
                next_sibling = this.placeholder.next()
                if(level == next_level) {
                    break;
                }
            }
            return next_level
        }

        if(prev_sibling.length) {
            var next_level = +prev_sibling.children("." + this.options.entry_class).attr("entry-level")
            if(level > next_level) {
                next_level++
                var sub = prev_sibling.children("." + this.options.sub_entry_class)
                if(!sub.length) {
                    sub = $("<div></div>")
                }
                sub.addClass(this.options.sub_entry_class)
                prev_sibling.append(sub)
                sub.append(this.placeholder)
            } else if(level != next_level) {
                next_level = __getDecreasedValue.bind(this)(next_level)
            }
            return next_level
        } else {
            var tobe_level
            var next_sibling = this.placeholder.next()
            if(next_sibling.length) {
                tobe_level = +next_sibling.children("." + this.options.entry_class).attr("entry-level")
            } else {
                tobe_level = +this.placeholder.parent().prev().attr("entry-level") + 1
            }            
            if(level < tobe_level) {
                tobe_level = __getDecreasedValue.bind(this)(tobe_level)
            }
            return tobe_level
        }
    }

    _s.placeholderPosition = function() {
        var _self = this
        var helperRect = this.candidate.rect()
        var placeholderReplacee
        var replaceFunc
        var center_point = {x: (helperRect.left + helperRect.right) / 2, y: (helperRect.top + helperRect.bottom) / 2}
        var appendIntersect = this.options.intersect
        this.handles.not(this.draggee.find(".bmui-stl-entry")).each(function() {
            var dropee = $(this)
            var dropeeRect = dropee.rect()
            var intersect = bm.intersect(dropeeRect, helperRect)
            if(intersect > appendIntersect) {
                placeholderReplacee = dropee
                appendIntersect = intersect
                var sign = (dropeeRect.right - dropeeRect.left) * (center_point.y - dropeeRect.bottom) - (dropeeRect.top - dropeeRect.bottom) * (center_point.x - dropeeRect.left)
                replaceFunc = sign < 0 ? "before" : "after"
            }
        })
        if(replaceFunc) {
            if(replaceFunc == "after" && placeholderReplacee.next().is("." + this.options.sub_entry_class)) {
                placeholderReplacee.next().prepend(_self.placeholder)
            } else {
                var replaceTarget = placeholderReplacee.parent()
                replaceTarget[replaceFunc](_self.placeholder)
            }
        }
        var proposed_level = Math.floor((this.draggee.offset().left - this.level_0_x) / this.options.level_depth)
        if(proposed_level < 0) {
            proposed_level = 0
        }
        this.set_level = this._setPlaceholderLevel(proposed_level)
    }

    _s.addHandle = function(element, parent) {
        this.handles = this.handles.add(element)
        this.draggable.addElement(element)
        var wrapper = $("<div></div>")
        wrapper.addClass(this.options.entry_container_class).append(element)
        this.setParent(element, parent)
    }

    _s.createHandles = function(element) {
        var handle = (element || this.element).children(":visible")
        if(this.options.handle) {
            return handle.filter(this.options.handle)
        }
        return handle
    }

    _s.destroy = function() {
        this.draggable.destroy()
    }

    _s.restore= function() {
        this.draggable.restore()
    }

    _s.getParent = function(entry) {
        var sub = entry.closest("." + this.options.sub_entry_class)
        if(sub.length) {
            return sub.prev()
        }
        return $()
    }

    _s.setParent = function(entry, parent) {
        var p_level
        if(!parent || !parent.length) {
            parent = this.element
            p_level = -1
        } else {
            p_level = +parent.attr("entry-level")
            var sub = parent.next()
            if(!sub.length) {
                sub = $("<div></div>")
                sub.addClass(this.options.sub_entry_class)
                parent.after(sub)
            }
            parent = sub
        }
        if(entry.parent().parent().is(parent)) {
            return;
        }
        var remove_sub
        if(!entry.parent().next().length && !entry.parent().prev().length) {
            remove_sub = entry.parent().parent()
        }
        parent.append(entry.parent())
        if(remove_sub) {
            remove_sub.remove()
        }
        entry.attr("entry-level", p_level + 1)
        __setLevel.bind(this)(entry.next(), p_level + 2)
    }

    _s.each = function(callback) {
        var _self = this
        var __each = function(element, parent) {
            var entries = element.children()
            var c_loop
            entries.each(function() {
                var entry = $(this).children("." + _self.options.entry_class)
                var loop = callback(entry, parent ? parent.children("." + _self.options.entry_class) : undefined)
                if(loop === false) {
                    c_loop = false
                    return false;
                }
                var sub = $(this).children("." + _self.options.sub_entry_class)
                if(sub.length) {
                    c_loop = __each(sub)
                    if(c_loop === false) {
                        return false;
                    }
                }
            })
            return c_loop
        }
        __each(this.element)
    }
})();