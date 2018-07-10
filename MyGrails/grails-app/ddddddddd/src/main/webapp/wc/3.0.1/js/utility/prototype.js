//region FUNCTION
$.extend(Function.prototype, {
    /**
     * this() then extend()
     * @param extend
     * @returns {Function}
     */
    blend: function (extend) {
        if (!extend) {
            return this
        }
        var _this = this
        return function () {
            if (extend.isString) {
                extend = bm.prop(window, extend)
            }
            var ret = _this.apply(this, arguments)
            return extend.apply(this, arguments) || ret
        }
    },
    /**
     * usable for multiple inheritance. if order and virtual not defined then will work as blend
     * @param extend
     * @returns {Function}
     */
    mixin: function (extend) {
        if (!extend) {
            return this
        }
        if (extend.override || this.virtual) {
            return extend
        } else if (this.override || extend.virtual) {
            return this
        } else {
            var torder = this.order || 0
            var eorder = extend.order || 0
            return torder <= eorder ? this.blend(extend) : extend.blend(this)
        }
    },
    /**
     * inherits provided super classes. argument can have multiple functions. only first function will be attached with the callee as _super
     * @param superFunction
     * @returns a new prototype with combining all properties from callee and arguments
     */
    inherit: function (superFunction) {
        var mixins = []
        if (arguments.length > 1) {
            mixins = Array.prototype.slice.call(arguments, 1)
        }
        var middleMan = function () {
        }
        middleMan.prototype = superFunction.prototype
        var oldProto = this.prototype
        var newProto = this.prototype = new middleMan()
        $.extend(newProto, bm.filterOwnProperties(oldProto))
        if (mixins.length) {
            var extendProto = newProto
            middleMan = function () {
            }
            middleMan.prototype = newProto
            newProto = this.prototype = new middleMan()
            var overrideList
            mixins.every(function (key, value) {
                $.each(value.prototype || ($.isPlainObject(value) ? value : {}), function (k) {
                    extendProto[k] = extendProto[k] ? extendProto[k].mixin(this) : this
                })
                overrideList = undefined
                return true
            })
            this._super = middleMan.prototype
            this._super.constructor = function () {
                superFunction.apply(this, arguments)
                var _this = this
                var _arg = arguments
                mixins.every(function (key, value) {
                    if (typeof value == "function") {
                        value.apply(_this, _arg)
                    }
                })
            }
        } else {
            this._super = superFunction.prototype
            this._super.constructor = superFunction
        }
        newProto.constructor = this
        return newProto
    },
    /**
     * returns a new function where first the caller is called and then argument
     * @param extend
     * @returns {Function}
     */
    intercept: function (extend) {
        if (!extend) {
            return this
        }
        var _this = this
        return function () {
            if (extend.isString) {
                extend = bm.prop(window, extend)
            }
            var ret = extend.apply(this, arguments)
            return _this.apply(this, arguments) || ret
        }
    },
    later: (function () {
        var timer = 0
        return function (ms) {
            var data
            if(arguments.length > 1) {
                data = Array.prototype.splice.call(arguments, 1)
            }
            clearTimeout(timer)
            timer = setTimeout(data ? function() {this.apply(null, data)}.bind(this) : this, ms)
        }
    })(),
    on: function (ev) {
        app.event.on(ev, this)
    }
})
//endregion

//region ARRAY
Array.prototype.contains = function (item) {
    return this.indexOf(item) > -1
}

$.extend(Array.prototype, {
    all: function () {
        for (h in this) {
            if (!this[h]) {
                return false
            }
        }
        return true
    },
    any: function (filter) {
        var l = this.length
        if (!filter) {
            for (var h = 0; h < l; h++) {
                if (this[h]) {
                    return true
                }
            }
        } else if (filter instanceof Function) {
            for (var h = 0; h < l; h++) {
                if (filter(this[h])) {
                    return true
                }
            }
        } else {
            function doTheTrick() {
                return eval(filter)
            }

            for (var h = 0; h < l; h++) {
                if (doTheTrick.call(this[h])) {
                    return true
                }
            }
        }
        return false
    },
    /**
     * returns new collection using the value for given property of each entity/return value of calling function
     * @param prop {String | Function}
     * @returns {Array}
     */
    collect: function (prop) {
        var ret = []
        if ($.isFunction(prop)) {
            $.each(this, function () {
                ret.push(prop.call(this))
            })
        } else {
            $.each(this, function () {
                ret.push(this[prop])
            })
        }
        return ret
    },
    clone: function () {
        return this.slice(0)
    },
    count: function (condition) {
        var found = 0
        if ($.isFunction(condition)) {
            this.every(function (k, p) {
                (function () {
                    if (condition.apply(p) === true) {
                        found++
                    }
                }).apply(p, Array.prototype.splice.call(arguments, 1))
            })
        } else {
            this.every(function (k, p) {
                (function () {
                    if (eval(condition) === true) {
                        found++
                    }
                }).apply(p, Array.prototype.splice.call(arguments, 1))
            })
        }
        return found
    },
    distinct: function () {
        var res = []
        for (var i = 0; i < this.length; i++) {
            var itm = this[i]
            if ($.inArray(itm, res) === -1) {
                res.push(itm)
            }
        }
        return res
    },
    every: function (callback) {
        var array = this
        //native every does not call the function for unassigned index
        $.each(this, function (k, v) {
            var ret = callback.call(v, k, v, array)
            return ret !== false
        })
    },
    /**
     * reverses of string exSplit process
     * @returns {String}
     */
    exJoin: function (joinChar) {
        joinChar = joinChar || ','
        var result = ""
        this.every(function (i, value) {
            result += (i > 0 ? joinChar : '') + (value.indexOf(joinChar) > -1 || value.charAt(0) == "(" ? "(" + value + ")" : value)
        })
        return result
    },
    native_filter: Array.prototype.filter,
    filter: function (filter) {
        if ($.isFunction(filter)) {
            return this.native_filter.apply(this, arguments)
        }
        var found = []
        this.every(function () {
            if (eval(filter) === true) {
                found.push(this)
            }
        })
        return found
    },
    native_find: Array.prototype.find,
    /**
     * return found object
     * @param condition [String | Function]
     * @returns {object} entry that is matched
     */
    find: function (condition) {
        var found = undefined
        if($.isFunction(condition)) {
            return this.native_find.apply(this, arguments)
        }
        this.every(function (i, value) {
            if (eval(condition) === true) {
                found = this
                return false
            }
        })
        return found
    },
    native_indexOf: Array.prototype.indexOf,
    /*indexOf: function(condition) {
        if($.isFunction(condition)) {
            let index = -1
            this.every((i, value) => {
                if (condition.apply(value, [i, this]) === true) {
                    index = i
                    return false
                }
            })
            return index
        }
        return this.native_indexOf.apply(this, arguments)
    },*/
    intersect: function (array) {
        if (!array) {
            return this
        }
        var result = []
        var distinctArray = this.distinct()
        for (var i = 0; i < distinctArray.length; i++) {
            var item = distinctArray[i]
            var shouldAddToResult = true
            for (var j = 0; j < arguments.length; j++) {
                var array2 = arguments[j]
                if (array2.length == 0) return []
                if ($.inArray(item, array2) === -1) {
                    shouldAddToResult = false
                    break
                }
            }
            if (shouldAddToResult) {
                result.push(item)
            }
        }
        return result
    },
    /**
     * returns first matched entry
     * @param condition
     * @returns {*} matched entry
     */
    iterate: function (iterator) {
        var c_ind = 0
        var obj = this
        var iter_caller = function () {
            if (c_ind == obj.length) {
                return
            }
            iterator.call(obj[c_ind], {
                next: function () {
                    c_ind++
                    setTimeout(iter_caller, 1)
                }
            }, c_ind)
        }
        iter_caller()
    },
    last: function () {
        return this[this.length - 1]
    },
    minus: function (toRemove) {
        return this.filter(function (i) {
            return toRemove.indexOf(i) < 0
        })
    },
    pushAll: function (pushArray) {
        this.push.apply(this, pushArray)
    },
    remove: function (entry) {
        var index = this.indexOf(entry)
        if (index > -1) {
            this.splice(index, 1)
        }
    },
    removeAll: function (filter) {
        var _self = this
        this.filter(filter).every(function (key, value) {
            _self.remove(value)
            return true
        })
        return this
    },
    /**
     * performs a reverse each
     * @param loopHandler {Function (index, reversedIndex, element) - context element}
     * @param thisArg [object (optional)] if provided - it will be passed as this on callback invocation
     */
    revery: function (loopHandler, thisArg) {
        var total = this.length - 1
        for (var t = total; t >= 0; t--) {
            if(loopHandler.call(thisArg || this[t], this[t], total - t, t, this) === false) {
                break
            }
        }
    },
    sum: function (iterator) {
        var count = this.length
        var result = 0
        if (iterator.isString) {
            iterator = new Function("return " + iterator)
        }
        for (var g = 0; g < count; g++) {
            result += iterator.call(this[g], g)
        }
        return result
    }
})

for (var k in Array.prototype) {
    if (!$.prototype[k]) {
        $.prototype[k] = Array.prototype[k]
    }
}
//endregion

//region STRING
var string_org_index_of = String.prototype.indexOf
var string_org_last_index_of = String.prototype.lastIndexOf
$.extend(String.prototype, {
    array: function () {
        if (this == "[]") {
            return []
        }
        return JSON.parse(this)
    },
    bool: function () {
        return !/^\s*(false|no|off|0|inactive|negative|-(\d*\.)?\d+)\s*$/i.test(this)
    },
    byteCount: function () {
        var result = 0
        for (var n = 0; n < this.length; n++) {
            var charCode = this.charCodeAt(n)
            if (charCode < 128) {
                result = result + 1
                if (charCode == 10) {
                    result = result + 1
                }
            } else if (charCode < 2048) {
                result = result + 2
            } else if (charCode < 65536) {
                result = result + 3
            } else if (charCode < 2097152) {
                result = result + 4
            } else if (charCode < 67108864) {
                result = result + 5
            } else {
                result = result + 6
            }
        }
        return result
    },
    camelCase: function (initial) {
        var writer = new StringWriter()
        var reader = this.reader()
        var data
        if (initial === undefined) {
            initial = true
        }
        var nextUpper = initial
        while ((data = reader.read()) != -1) {
            if ((data > '`' && data < '{') || (data > '@' && data < '[') || (data > '/' && data < ':')) {
                if (nextUpper) {
                    writer.plus(data.toUpperCase())
                    nextUpper = false
                } else {
                    writer.plus(data)
                }
            } else {
                nextUpper = true
            }
        }
        return writer.toString()
    },
    capitalize: function () {
        return this.length == 0 ? "" : this[0].toUpperCase() + this.substring(1)
    },
    contains: function (match) {
        return this.indexOf(match) > -1
    },
    dotCase: function () {
        var writer = new StringWriter()
        var reader = this.reader()
        var data
        while ((data = reader.read()) != -1) {
            if (data > '@' && data < '[') {
                writer.plus(".")
                writer.plus(data.toLowerCase())
            } else {
                writer.plus(data)
            }
        }
        return writer.toString()
    },
    equals: function (c, matchCase) {
        if (arguments.length == 1) {
            matchCase = true
        }
        var a = matchCase ? this + "" : this.toLowerCase()
        var b = matchCase ? c + "" : c.toLowerCase()
        return a == b
    },
    /**
     * if seperator charactor is expected in a value then that value must be enclosed within ( and ). e.g. for 1 and 2,3 and 4 - 1,(2,3),4
     */
    exSplit: function (seperatorChar) {
        seperatorChar = seperatorChar || ','
        var params = []
        var resultIndex = 0
        var skipUntil = false
        var bufferCount = 0
        params[0] = ""
        for (var i = 0; i < this.length; i++) {
            var char = this.charAt(i)
            if (!skipUntil && char == seperatorChar) {
                params[++resultIndex] = ""
                bufferCount = 0
            } else {
                if (char == '(' && !bufferCount) {
                    skipUntil = true
                } else {
                    if (char == ')' && skipUntil && (i + 1 == this.length || this.charAt(i + 1) == seperatorChar)) {
                        skipUntil = false
                    } else {
                        bufferCount++
                        params[resultIndex] += char
                    }
                }
            }
        }
        return params
    },
    hash: function () {
        var hash = 0
        if (this.length == 0) return hash
        var i
        for (i = 0; i < this.length; i++) {
            var ch = this.charCodeAt(i)
            hash = ((hash << 5) - hash) + ch
            hash = hash & hash
        }
        return hash
    },
    htmlEncode: function () {
        return this.replace(/&/g, '&amp')
            .replace(/</g, '&lt')
            .replace(/>/g, '&gt')
            .replace(/"/g, '&quot')
            .replace(/'/g, '&#39')
            .replace(/\r\n/g, '<br>')
            .replace(/\n/g, '<br>')
            .replace(/  /g, ' &nbsp')
    },
    indexOf: function (regex, i) {
        if (regex instanceof RegExp) {
            return this.substr(i || 0).search(regex) + (i || 0)
        }
        return string_org_index_of.apply(this, arguments)
    },
    int: function (value) {
        if (/^\s*\d+(.\d*)?\s*$/.test(value)) {
            return +value
        }
        return parseInt(value) || 0
    },
    lastIndexOf: function (regex, i) {
        if (regex instanceof RegExp) {
            i = (i || (this.length - 1)) - (this.length - 1)
            var _this = this.reverse()
            var fIndex = _this.indexOf(regex, i)
            if (fIndex == -1) {
                return -1
            }
            return fIndex + this.length - 1
        }
        return string_org_last_index_of.apply(this, arguments)
    },
    minusCase: function () {
        var writer = new StringWriter()
        var reader = this.reader()
        var data
        while ((data = reader.read()) != -1) {
            if (data > '@' && data < '[') {
                writer.plus("-")
                writer.plus(data.toLowerCase())
            } else {
                writer.plus(data)
            }
        }
        return writer.toString()
    },
    reader: function (shouldThroughError) {
        var index = 0
        var _self = this
        return {
            read: function () {
                var c = _self.charAt(index++)
                if (!c) {
                    if (shouldThroughError) {
                        throw "EOF"
                    }
                    return -1
                }
                return c
            }
        }
    },
    replaceAll: function (search, replace, isRegex) {
        if (isRegex) {
            var reg = new RegExp(search, "g")
            return this.replace(reg, replace)
        } else {
            return this.split(search).join(replace)
        }
    },
    reverse: function () {
        return this.split('').reverse().join('')
    },
    sanitize: function () {
        return this.trim().toLowerCase().replace(/\s/g, "-").replace(/[^a-z0-9-\._]+/g, "-")
    },
    textify: function () {
        return this.replace(/<[^>]*>/g, "")
    },
    toNumber: function() {
        return parseFloat(this)
    },
    upto: function (char) {
        return this.substring(0, this.indexOf(char))
    },
    pxToVal: function () {
        var out = this.split("px")[0];
        out = parseFloat(out);
        return (out ? out : 0);
    }
})
//endregion

//region JQUERY
var $_height = $.prototype.height
var $_width = $.prototype.width
var $_text = $.prototype.text
var $_show = $.prototype.show
var $_hide = $.prototype.hide
$.extend($.prototype, {
    acss: function (prop) {
        try {
            return this.chide()[0].ownerDocument.defaultView.getComputedStyle(this[0])[prop]
        } finally {
            this.cshow()
        }
    },
    addAttrProp: function (attr, prop, newProp) {
        if (arguments.length < 2 || !this.is('[' + attr + ']')) {
            return this
        } else if (arguments.length > 2) {
            return this.removeAttrProp(attr, prop).addAttrProp(attr, newProp)
        }
        var cache = this.attr(attr)
        if (cache.toLowerCase().contains(prop.toLowerCase())) {
            return this
        }
        this.attr(attr, (cache + " " + prop).trim())
        return this
    },
    autoLoadDrop: function (data, config) {
        var element = this.data("autoLoadDrop")
        if (element) {
            return element
        } else {
            var $element = $([])
            this.each(function () {
                $element = bm.autoLoadDrop(this.jqObject, data, config)
            })
            return $element
        }
    },
    bottomRib: function (margin, border, padding) {
        return this.dirRib("bottom", margin, border, padding)
    },
    /**
     * stores current display information and then hide. requires for cshow
     */
    changeTag: function (tag) {
        var replacement = $('<' + tag + '>')
        var attributes = {}
        $.each(this.get(0).attributes, function (index, attribute) {
            attributes[attribute.name] = attribute.value
        })
        replacement.attr(attributes)
        replacement.data(this.data())
        var contents = this.children().clone(true, true)
        replacement.append(contents)
        this.replaceWith(replacement)
        return replacement
    },
    chide: function () {
        return this.each(function () {
            var display = this.style ? this.style.display : undefined
            $(this).data("display-cache", display).css({display: "none"})
        })
    },
    cleanWhitespace: function () {
        this.contents().filter(function () {
            if (this.nodeType != 3) {
                $(this).cleanWhitespace()
                return false
            }
            else {
                this.textContent = $.trim(this.textContent)
                return !/\S/.test(this.nodeValue)
            }
        }).remove()
        return this
    },
    clearCache: function (onClear) {
        if (!this.is("img")) {
            throw $.error("This function is only applicable for img tag")
        }
        var url = this.attr("src")
        var _self = this
        var iframe = $("<iframe style='position: absolute left: -20000px top: -20000px'></iframe>")
        iframe.appendTo(document.body)
        iframe.one("load", function () {
            iframe.one("load", function () {
                iframe.remove()
                _self.attr("src", url)
                if (onClear) {
                    onClear()
                }
            })[0].contentWindow.location.reload()
        }).attr("src", url).bind("error", function () {
            iframe.remove()
        })
    },
    _closestCache: $.prototype.closest,
    closest: function (filter) {
        if ($.isFunction(filter)) {
            var elm = this
            do {
                var ret = filter.call(elm[0])
                if (ret) {
                    return elm
                }
            } while ((elm = elm.parent()).length != 0)
            return null
        } else {
            return this._closestCache.apply(this, arguments)
        }
    },
    collect: function (prop) {
        return Array.prototype.collect.call(this, prop)
    },
    config: function (type, updates) {
        if(!this.length) {
            return
        }
        var cacheKey = "attr-parsed-cache#" + type
        var map = this.tag(cacheKey)
        if (!map) {
            map = {}
            this.tag(cacheKey, map)
            var cutLength = type.length + 1
            $.each(this.nat.attributes, function () {
                if (this.name.startsWith(type + "-")) {
                    map[this.name.substring(cutLength).toLowerCase()] = bm.autoType(this.value)
                }
            })
        }
        if (updates) {
            if (typeof updates == "string") {
                return map[updates.toLowerCase()]
            } else {
                var obj = this
                $.each(updates, function (k, v) {
                    k = k.toLowerCase()
                    map[k] = v
                    obj.attr(type + "-" + k, v)
                })
                return this
            }
        }
        return map
    },
    copyDeepEvents: function (to, keepContext) {
        this.copyEvents(to, keepContext)
        function repeat(from, to) {
            var toChildren = to.children()
            var thisChildren = from.children()
            if (toChildren.length == thisChildren.length) {
                thisChildren.each(function (ind) {
                    $(this).copyEvents(toChildren.eq(ind), keepContext)
                    repeat($(this), toChildren.eq(ind))
                })
            }
        }

        repeat(this, to)
    },
    copyEvents: function (to, keepContext) {
        var node = this
        var expando = this[0][$_priv_data.expando]
        if (expando) {
            var events = expando.events
            if (events) {
                $.each(events, function (key, ev) {
                    $.each(ev, function () {
                        var _event = this
                        var evKey = key
                        if (this.namespace) {
                            evKey = key + "." + this.namespace
                        }
                        var handler = keepContext ? function () {
                            if (_event.selector) {
                                _event.handler.apply(node.find(_event.selector), arguments)
                            } else {
                                _event.handler.apply(node, arguments)
                            }
                        } : this.handler
                        if (this.selector) {
                            to.on(evKey, this.selector, handler)
                        } else {
                            to.on(evKey, handler)
                        }
                    })
                })
            }
        }
    },
    /**
     * restores previous display information that stored by chide
     */
    cshow: function () {
        return this.each(function () {
            var display = $(this).data("display-cache")
            this.style.display = display
            $(this).removeData("display-cache")
        })
    },
    cssCodeEditor: function () {
        var $this = this
        return CodeMirror.fromTextArea($this[0], {
            lineNumbers: true,
            extraKeys: {"Ctrl-Space": "autocomplete"},
            mode: "css",
            theme: "eclipse",
            highlightSelectionMatches: {showToken: /\w/},
            showCursorWhenSelecting: true,
            matchBrackets: true,
            autoCloseBrackets: true
        })
    },
    /**
     * makes a input only positive decimal values supported
     */
    decimal: function () {
        return this.each(function () {
            var input = $(this)
            input.bind("keydown.restrict", function (e) {
                var key = e.keyCode
                if (e.shiftKey) {
                    return browser.key.isArrows(key)
                }
                if (key == browser.key.POINT || key == browser.key.NUM_POINT) {
                    return input.val().indexOf(".") == -1
                }
                return key == browser.key.BACKSPACE || key == browser.key.TAB || key == browser.key.DELETE || browser.key.isArrows(key) || browser.key.isDigit(key) || (e.ctrlKey && (browser.key.is('a', key) || browser.key.is('A', key) || browser.key.is('c', key) || browser.key.is('v', key) || browser.key.is('x', key) || browser.key.is('C', key) || browser.key.is('V', key) || browser.key.is('X', key)))
            }).on("paste", function () {
                var _self = this
                var oldValue = this.value
                setTimeout(function () {
                    if (isNaN(_self.value)) {
                        _self.value = oldValue
                    }
                }, 0)
            })
        })
    },
    dirRib: function (dir, margin, border, padding) {
        var doList = []
        if (padding === undefined) {
            padding = true
        }
        if (border === undefined) {
            border = true
        }
        if (margin === undefined) {
            margin = true
        }
        if (padding) {
            doList.push("padding")
        }
        if (border) {
            doList.push("border")
        }
        if (margin) {
            doList.push("margin")
        }
        var num = function (value) {
            try {
                let intValue = parseInt(value, 10)
                if(isNaN(intValue)) {
                    return 0
                }
                return intValue
            } catch (t) {
                return 0
            }
        }
        var _this = this
        return doList.sum(function () {
            return num(_this.css(this + '-' + dir + (this == 'border' ? '-width' : '')))
        })
    },
    dropify: function (dropBox, configs) {
        var instance_count = window.dropify_instance_count ? ++window.dropify_instance_count : (window.dropify_instance_count = 1)
        if (!this.length) {
            return this
        }
        configs = configs || {}
        var _self = this
        var drop_is_child = this.isParentOf(dropBox)
        var drop = function () {
            if (dropBox.is(":visible")) {
                return
            }
            if (!configs.no_position || !configs.animation) {
                _self.add(dropBox).addClass("bmui-state-visible")
            }
            var trigger = function () {
                _self.triggerHandler("dropify:expanded", [dropBox])
            }
            if ($.isFunction(configs.position)) {
                configs.position()
            } else if (!configs.no_position) {
                dropBox.position({
                    my: configs.my || "right top-1",
                    at: configs.at || "right bottom",
                    of: configs.position_of || _self,
                    collision: configs.position_collision || "flip",
                    within: configs.position_within
                })
                if (dropBox.offset().top < _self.offset().top) {
                    dropBox.add(_self).addClass("dropped-up")
                } else {
                    dropBox.add(_self).addClass("dropped-down")
                }
            } else if (configs.animation) {
                switch (configs.animation) {
                    case "slideright":
                        dropBox.toggle("slide", {direction: "left"}, undefined, function () {
                            _self.add(dropBox).addClass("bmui-state-visible")
                            trigger()
                        })
                }
            }
            if(configs.auto_hide !== false) {
                $(document).one("mousedown.dropify_" + instance_count, function (ev) {
                    if (!dropBox.add(_self).isParentOf(ev.target) && !dropBox.add(_self).is(ev.target)) {
                        var isHide = hide()
                        if (isHide == false) {
                            $(document).one("mousedown.dropify_" + instance_count, arguments.callee)
                        }
                    } else {
                        $(document).one("mousedown.dropify_" + instance_count, arguments.callee)
                    }
                })
            }
            if (!configs.no_position || !configs.animation) {
                trigger()
            }
        }
        var hide = function (clicked) {
            if (dropBox.is(":hidden")) {
                return
            }
            var proceed = true
            if (dropBox[0].dropify_hide_intercept) {
                proceed = dropBox[0].dropify_hide_intercept(clicked)
            }
            if (proceed === false) {
                return
            }
            var callback = function () {
                var collapsed = _self.triggerHandler("dropify:beforeCollapsed", [dropBox])
                if (collapsed == false) {
                    return false
                }
                dropBox.add(_self).removeClass("dropped-down dropped-up bmui-state-visible")
                if (configs.remove_on_hide) {
                    dropBox.remove()
                }
                _self.triggerHandler("dropify:collapsed", [dropBox])
            }
            if (configs.animation) {
                switch (configs.animation) {
                    case "slideright":
                        dropBox.toggle("slide", {direction: "left"}, undefined, callback)
                }
            } else {
                return callback()
            }
        }
        if (!configs.keep_on_click) {
            dropBox.off(".dropify_" + instance_count).on("click.dropify_" + instance_count, function (ev) { // off is called thus it can not respond to other dropify events
                hide(ev.target.jqObject)
            })
        }
        if (configs.drop_instant) {
            drop()
        } else {
            this.click(function (ev) {
                if (drop_is_child && dropBox.isParentOf(ev.target)) {
                    return
                }
                if (this.jqObject.is(".disabled")) {
                    return
                }
                if (dropBox.is(".bmui-state-visible")) {
                    hide()
                    $(document).off("mousedown.dropify_" + instance_count)
                } else {
                    drop()
                }
            })
            hide()
        }
        if (drop_is_child && configs.no_position == undefined) {
            configs.no_position = true
        }
        dropBox.find(".close-dropbox").on("click.dropify_" + instance_count, function () {
            hide()
        })
        dropBox[0].dropify_hide_function = hide
        dropBox[0].dropify_show_function = drop
        return this
    },
    editable: function () {
        var editBtn = "<span class='tool-icon edit inline-edit'></span>".jqObject
        this.each(function () {
            var span = this.jqObject
            span.hover(function () {
                if (this.edit_off) {
                    return
                }
                span.append(editBtn)
            }, function () {
                if (this.edit_off) {
                    return
                }
                editBtn.detach()
            })
        })
        editBtn.on("click", function () {
            var span = this.jqObject.closest(".inline-editable")
            var type = span.attr("data-editable-type") || "text"
            editBtn.detach()
            span.nat.edit_off = true
            span.addClass("editting")
            bm["editInline" + type.capitalize()](span).always(function () {
                span.nat.edit_off = false
                span.removeClass("editting")
            })
        })
        return this;
    },
    fill: function (settings) {
        return bm.ajax($.extend(settings, {fill: this}))
    },
    /**
     * blinks selected tags
     * @param {} flag
     * @returns {}
     */
    flash: function (flag) {
        function Flash(el) {
            this.opacity = 0
            this.el = el
        }

        Flash.prototype.start = function () {
            var _self = this
            this.interval = setInterval(function () {
                _self.el.animate({opacity: _self.opacity}, 600)
                _self.opacity = _self.opacity ? 0 : 1
            }, 600)
            this.isFlashing = true
        }
        Flash.prototype.stop = function () {
            if (this.isFlashing) {
                var _self = this
                this.opacity = 1
                clearInterval(_self.interval)
                this.el.animate({opacity: 1}, 600)
                this.isFlashing = false
            }
        }
        var flash = this.data("flash-inst")
        if (!flash) {
            flash = new Flash(this)
            this.on("click.flash", function () {
                if (flash.isFlashing) {
                    flash.stop()
                }
            })
            this.on("remove", function () {
                flash.stop()
            })
            this.data("flash-inst", flash)
        }
        if (!flash.isFlashing) {
            flash.start()
        }
    },
    /**
     * removes input restrictions from a input element
     */
    free: function () {
        return this.each(function () {
            var input = $(this)
            if (!input.is("input")) {
                return
            }
            input.unbind("keydown.restrict")
        })
    },
    hasAttr: function (attr) {
        if (bm.isString(attr)) {
            return this.is("[" + attr + "]")
        }
        var found = false
        $.each(this.nat.attributes, function () {
            found = attr.test(this.name)
            return !found
        })
        return found
    },
    hasparent: function (selector) {
        return this.parent().is(selector)
    },
    /**
     * extends jquery height function to set height considering element padding, margin
     */
    height: function (val/*Number(optional)*/, isOuter/*Boolean(optional)*/, includesMargin/*Boolean(optional)*/) {
        if (arguments.length < 2 || !(isOuter || includesMargin)) {
            return $_height.apply(this, arguments)
        } else {
            var reduceHeight = this.outerHeight(includesMargin || false) - this.height()
            return $_height.call(this, val - reduceHeight)
        }
    },
    hide: function () {
        $(this).find("*").each(function () {
            $(this).triggerHandler("hide")
        })
        return $_hide.apply(this, arguments)
    },
    htmlCodeEditor: function () {
        var $this = this, editor, mixedMode = {
            name: "htmlmixed",
            scriptTypes: [{
                matches: /\/x-handlebars-template|\/x-mustache/i,
                mode: null
            },
                {
                    matches: /(text|application)\/(x-)?vb(a|script)/i,
                    mode: "vbscript"
                }]
        }
        return CodeMirror.fromTextArea($this[0], {
            lineNumbers: true,
            extraKeys: {"Ctrl-Space": "autocomplete"},
            mode: mixedMode,
            theme: "eclipse"
        })

    },
    ichange: function (pauseDuration, handler) {
        return this.on("ichange", pauseDuration, handler)
    },
    intercept: function (ev, handler) {
        return this.on("before_" + ev, handler)
    },
    //works only for single namespace
    isBound: function (evType, namespace) {
        var expando = this[0][$_priv_data.expando]
        if (!expando) {
            return false
        }
        var events = expando.events
        var matchedEvents
        var matched = false
        if (events && (matchedEvents = events[evType])) {
            $.each(matchedEvents, function () {
                if (!namespace || this.namespace == namespace) {
                    matched = true
                }
                return !matched
            })
        }
        return matched
    },
    isChildOf: function (parent) {
        return this.parents(parent).length > 0
    },
    isSelfOrChildOf: function (parent) {
        return this.closest(parent).length > 0
    },
    isParentOf: function (child) {
        return this.find(child).length > 0
    },
    leafs: function () {
        return this.find("*").filter(function () {
            return $(this).children().length == 0
        })
    },
    leftRib: function (margin, border, padding) {
        return this.dirRib("left", margin, border, padding)
    },
    linkify: function () {
        return this.each(function () {
            var anchor = $(this)
            var target = anchor.attr("data-fill-target")
            if (target) {
                var target = $(target)
            } else {
                target = anchor.attr("data-target")
            }
            if (target || "#workspace".jqObject.length) {
                var processor = anchor.attr("data-processor")
                var url = app.base + anchor.click(function () {
                    bm.jsNavigate(url, target, processor, anchor)
                }).attr("data-url")
            }
        })
    },
    /**
     * @param show [boolean ! string] if string then it will be added in the div as a class otherwise if it is false then it will hide the mask
     */
    loader: function (show) {
        if(this.nat.mask_loadtimer) {
            clearTimeout(this.nat.mask_loadtimer)
        }
        if (show === false) {
            bm.unmask(this)
        } else {
            var template = '<div><span class="loader"></span></div>'.jqObject
            if(typeof show == "string") {
                template.addClass(show)
            }
            this.nat.mask_loadtimer = setTimeout(function() {
                bm.mask(this, template)
            }.bind(this), 300)
        }
        return this
    },
    mask: function (maskHtml) {
        maskHtml = maskHtml ? maskHtml : "<div></div>"
        bm.mask(this, maskHtml)
    },
    /**
     * makes a input only positive numeric values supported
     */
    numeric: function () {
        return this.each(function () {
            $(this).bind("keydown.restrict", function (e) {
                var key = e.keyCode
                if (e.shiftKey) {
                    return browser.key.isArrows(key)
                }
                return key == browser.key.BACKSPACE || key == browser.key.TAB || key == browser.key.DELETE || browser.key.isArrows(key) || browser.key.isDigit(key) || (e.ctrlKey && (browser.key.is('a', key) || browser.key.is('A', key) || browser.key.is('c', key) || browser.key.is('v', key) || browser.key.is('x', key) || browser.key.is('C', key) || browser.key.is('V', key) || browser.key.is('X', key)))
            }).on("paste", function () {
                var _self = this
                var oldValue = this.value
                setTimeout(function () {
                    var newValue = _self.value
                    if (isNaN(newValue) || newValue.contains(".") || newValue < 0) {
                        _self.value = oldValue
                    }
                }, 0)
            })
        })
    },
    /**
     * @deprecated use <plugin-name>('inst')
     * @param type
     * @returns {*}
     */
    obj: function (type) {
        var data = this.data()
        if (!data) {
            return null
        }
        if (typeof type == "string") {
            return data[type]
        }
        if (!$.isFunction(type)) {
            type = null
        }
        var obj = null
        $.each(data, function (key) {
            if (type) {
                if (this instanceof type) {
                    obj = this
                    return false
                }
                return true
            }
            if (key.endsWith("Inst")) {
                obj = this
                return false
            }
            if (key.startsWith("wcui")) {
                obj = this
                return false
            }
        })
        return obj
    },
    outer: function () {
        return this[0].outerHTML
    },
    overflowParent: function (direction) {
        var parent = this[0].ownerDocument
        this.parents().each(function () {
            var _parent = this.jqObject
            if (!direction || direction == "x") {
                if (/hidden|auto|scroll/.test(_parent.css("overflow-x"))) {
                    parent = _parent
                    return false
                }
            }
            if (!direction || direction == "y") {
                if (/hidden|auto|scroll/.test(_parent.css("overflow-y"))) {
                    parent = _parent
                    return false
                }
            }
        })
        return parent
    },
    //analogous to prop function - analogy created to be used for custom properties
    pairuncheck: function () {
        this.after(function () {
            var check = $(this)
            var input = $("<input style='display: none' class='single' type='checkbox' name='" + check.attr("name") + "' value='" + check.attr("uncheck-value") + "'>")
            check.bind("change.form", function () {
                if (!this.checked) {
                    input[0].checked = true
                } else {
                    input[0].checked = false
                }
            })
            check.triggerHandler("change.form")
            return input
        })
    },
    positionFromBody: function () {
        var el = this.get(0)
        var _x = 0
        var _y = 0
        while (el && !isNaN(el.offsetLeft) && !isNaN(el.offsetTop)) {
            _x += el.offsetLeft - el.scrollLeft + el.clientLeft
            _y += el.offsetTop - el.scrollTop + el.clientTop
            el = el.offsetParent
        }
        return {top: _y, left: _x}
    },
    pposition: function (which, size) {
        var pos = this.position()
        if (which == "left" || which == "top") {
            return pos[which]
        }
        var parent = $(this[0].offsetParent)
        if (!which || which == "right") {
            pos.right = parent.innerWidth() - pos.left - this.outerWidth(true)
        }
        if (!which || which == "bottom") {
            pos.bottom = parent.innerHeight() - pos.top - this.outerHeight(true)
        }
        if ((!which && size) || which == "width" || which == "height") {
            if (which) {
                pos[which] = this["outer" + which.capitalize()](true)
            } else {
                pos.width = this.outerWidth(true)
                pos.height = this.outerHeight(true)
            }
        }
        return which ? pos[which] : pos
    },
    /**
     * @param offset [boolean | {left: [number], top: [number]}] if boolean then it will be considered as includeMargin
     * @param includeMargin
     * @returns {*}
     */
    rect: function (offset, includeMargin) {
        var rect = $.extend({}, this.nat.getBoundingClientRect())
        var _document = this[0].ownerDocument
        var scroller = $(/webkit/i.test(navigator.userAgent) || _document.compatMode == 'BackCompat' ? _document.body : _document.documentElement)
        var s_top = scroller.scrollTop()
        var s_left = scroller.scrollLeft()
        rect.top += s_top
        rect.left += s_left
        rect.bottom += s_top
        rect.right += s_left
        if (arguments.length == 0) {
            return rect
        }
        if (offset instanceof Boolean || typeof offset == "boolean") {
            includeMargin = offset
            offset = undefined
        }
        if (offset instanceof $) {
            let _offset = offset.offset()
            _offset.left *= -1
            _offset.left -= offset.leftRib(false, true, false)
            _offset.top *= -1
            _offset.top -= offset.topRib(false, true, false)
            offset = _offset
        }
        if (offset) {
            rect.left += offset.left
            rect.top += offset.top
            rect.right += offset.left
            rect.bottom += offset.top
        }
        if (includeMargin) {
            var marginL = parseInt(this.css("margin-left"))
            var marginR = parseInt(this.css("margin-right"))
            var marginT = parseInt(this.css("margin-top"))
            var marginB = parseInt(this.css("margin-bottom"))
            rect.left -= marginL
            rect.top -= marginT
            rect.right += marginR
            rect.bottom += marginB
            rect.width += marginL + marginR
            rect.height += marginT + marginB
        }
        return rect
    },
    replaceClass: function (a, b) {
        return this.removeClass(a).addClass(b)
    },
    removeAttrProp: function (attr, prop) {
        if (arguments.length < 2 || !this.is('[' + attr + ']')) {
            return this
        }
        var cache = this.attr(attr)
        this.attr(attr, cache.replaceAll(prop, function (replace, idx, str) {
            var prev = idx ? str.substring(0, idx) : ""
            var nxt = str.substring(idx + replace.length, str.length)
            if (!(prev.contains("[") && nxt.contains("]"))) {
                return ""
            }
            return replace
        }).trim())
        return this
    },
    reset: function () {
        this.each(function () {
            var input = $(this)
            if(!input.is(":input")) {
                if(input.nat.reset) {
                    input.nat.reset()
                }
                return
            }
            var next = this.nextSibling
            var parent = input.parent()
            var form = $("<form></form>")
            form.append(input)
            form[0].reset()
            if (next) {
                $(next).before(input)
            } else {
                parent.append(input)
            }
        })
    },
    rightRib: function (margin, border, padding) {
        return this.dirRib("right", margin, border, padding)
    },
    scrollHere: function (scroller) {
        if (!scroller) {
            scroller = this.closest(".scrollable")
            if(!scroller.length) {
                var _document = this[0].ownerDocument
                scroller = bm.getTopScroller(_document)
            }
        } else if (scroller.nat instanceof Document) {
            scroller = bm.getTopScroller(scroller.nat)
        }
        if (this.is(":hidden")) {
            return
        }
        var height = scroller.height()
        var width = scroller.width()
        var scroller_offset = scroller.offset()
        var target_offset = this.offset()

        var bottom_dist = target_offset.top - scroller_offset.top - height
        if (bottom_dist > 0) {
            scroller.animate({scrollTop: "+=" + (bottom_dist + Math.min(height / 3, 150)) + "px"}, 800)
        } else {
            var top_dist = scroller_offset.top - target_offset.top
            if (top_dist > 0) {
                scroller.animate({scrollTop: "-=" + (top_dist + Math.min(height / 3, 150)) + "px"}, 800)
            }
        }

        var right_dist = target_offset.left - scroller_offset.left - width
        if (right_dist > 0) {
            scroller.animate({scrollLeft: "+=" + (right_dist + Math.min(width / 3, 150)) + "px"}, 800)
        } else {
            var left_dist = scroller_offset.left - target_offset.left
            if (left_dist > 0) {
                scroller.animate({scrollLeft: "-=" + (left_dist + Math.min(width / 3, 150)) + "px"}, 800)
            }
        }
        return this
    },
    /**
     * takes data from an form and generates an json object using those data
     * */
    serializeObject: function () {
        var o = {}
        var a = (this.is("form") ? this : this.find(":input")).serializeArray()
        $.each(a, function () {
            if (o[this.name] !== undefined) {
                if (!$.isArray(o[this.name])) {
                    o[this.name] = [o[this.name]]
                }
                o[this.name].push(this.value || '')
            } else {
                o[this.name] = this.value || ''
            }
        })
        return o
    },
    show: function () {
        var showHandler
        var afterShow = function () {
            $(this).find("*").each(function () {
                $(this).triggerHandler("show")
            })
            if (showHandler) {
                showHandler.apply(this, arguments)
            }
        }
        if (arguments.length == 1) {
            if ($.isPlainObject(arguments[0])) {
                showHandler = arguments[0].complete
                arguments[0].complete = afterShow
            }
        }
        var argumentPos = -1
        $.each(arguments, function (i) {
            if (typeof this == "function") {
                showHandler = this
                argumentPos = i
            }
        })
        if (argumentPos > -1) {
            arguments[argumentPos] = afterShow
        }
        $_show.apply(this, arguments)
        if (!showHandler) {
            afterShow.call(this[0])
        }
        return this
    },
    signed_decimal: function () {
        return this.each(function () {
            var input = $(this)
            input.bind("keydown.restrict", function (e) {
                var key = e.keyCode
                if (e.shiftKey) {
                    return browser.key.isArrows(key)
                }
                if (key == browser.key.MINUS || key == browser.key.NUM_MINUS) {
                    if (input.val().charAt(0) != "-") {
                        input.val("-" + input.val())
                    }
                    return false
                }
                if (key == browser.key.POINT || key == browser.key.NUM_POINT) {
                    return input.val().indexOf(".") == -1
                }
                return key == browser.key.BACKSPACE || key == browser.key.TAB || key == browser.key.DELETE || browser.key.isArrows(key) || browser.key.isDigit(key) || (e.ctrlKey && (browser.key.is('a', key) || browser.key.is('A', key) || browser.key.is('c', key) || browser.key.is('v', key) || browser.key.is('x', key) || browser.key.is('C', key) || browser.key.is('V', key) || browser.key.is('X', key)))
            }).on("paste", function () {
                var _self = this
                var oldValue = this.value
                setTimeout(function () {
                    if (isNaN(_self.value)) {
                        _self.value = oldValue
                    }
                }, 0)
            })
        })
    },
    /**
     * makes a input only signed numeric values supported
     */
    signed_numeric: function () {
        return this.each(function () {
            var input = $(this)
            input.bind("keydown.restrict", function (e) {
                var key = e.keyCode
                if (e.shiftKey) {
                    return browser.key.isArrows(key)
                }
                if (key == browser.key.MINUS || key == browser.key.NUM_MINUS) {
                    if (input.val().charAt(0) != "-") {
                        input.val("-" + input.val())
                    }
                    return false
                }
                return key == browser.key.BACKSPACE || key == browser.key.TAB || key == browser.key.DELETE || browser.key.isArrows(key) || browser.key.isDigit(key) || (e.ctrlKey && (browser.key.is('a', key) || browser.key.is('A', key) || browser.key.is('c', key) || browser.key.is('v', key) || browser.key.is('x', key) || browser.key.is('C', key) || browser.key.is('V', key) || browser.key.is('X', key)))
            }).on("paste", function () {
                var _self = this
                var oldValue = this.value
                setTimeout(function () {
                    var newValue = _self.value
                    if (isNaN(newValue) || newValue.contains(".")) {
                        _self.value = oldValue
                    }
                }, 0)
            })
        })
    },
    sortablecolumn: function (config) {
        var defaults = {
            empty: "always-below"
        }

        var functions = {
            resetSortState: function () {
                return this.removeAttr("sort-dir").removeClass("sort-down").removeClass("sort-up")
            }
        }

        if (typeof config == "string") {
            return functions[config].call(this)
        }

        $.extend(config, defaults)

        function sortIt(th, dir) {
            var index = th.index()
            var table = th.closest("table")
            var rows = table.find("tr").not(":first")
            rows.sort(function (a, b) {
                var keyA = $('td:eq(' + index + ')', a).text().trim().toUpperCase()
                var keyB = $('td:eq(' + index + ')', b).text().trim().toUpperCase()
                if (keyA == keyB) {
                    return 0
                }
                if (keyA == "") {
                    if (config.empty == "always-below") {
                        return 1
                    } else if (config.empty == "always-above") {
                        return -1
                    }
                }
                if (keyB == "") {
                    if (config.empty == "always-below") {
                        return -1
                    } else if (config.empty == "always-above") {
                        return 1
                    }
                }
                if ((dir == "up" && keyA > keyB) || (dir == "down" && keyA < keyB)) {
                    return 1
                }
                return -1
            })
            table.append(rows.remove())
        }

        var th = $(this)
        th.addClass("sortable").click(function () {
            var _self = $(this)
            var dir = _self.attr("sort-dir") || "down"
            if (dir == "down") {
                _self.attr("sort-dir", dir = "up")
                _self.addClass("sort-up").removeClass("sort-down")
            } else {
                _self.attr("sort-dir", dir = "down")
                _self.addClass("sort-down").removeClass("sort-up")
            }
            sortIt(_self, dir)
            _self.closest("table").trigger("sort")
            _self.closest("table").trigger("change")
        })
        var table = th.closest("table")
        table.bind("row-update", function () {
            var _self = $(this).find("th.sortable")
            _self.each(function () {
                var th = $(this)
                var dir = th.attr("sort-dir")
                if (dir) {
                    sortIt(th, dir)
                }
            })
            _self.trigger("sort")
            table.trigger("change")
        })
        return this
    },
    //analogous to prop function - analogy created to be used for custom properties
    store: function (name, value) {
        if (arguments.length == 2) {
            this.each(function () {
                this[name] = value
            })
            return this
        }
        return this.length ? this[0][name] : null
    },
    swap: function (to) {
        var aParent = this.parent()
        var aIndex = this.index()
        var bParent = to.parent()
        var bIndex = to.index()
        if (aParent[0] == bParent[0]) {
            if (aIndex < bIndex) {
                this.before(to)
                var eAtPos = bParent.find(">*:eq(" + bIndex + ")")
                if (eAtPos[0] == this[0]) {
                    return
                }
                if (eAtPos.length) {
                    eAtPos.after(this)
                } else {
                    bParent.append(this)
                }
            } else {
                this.after(to)
                var eAtPos = bParent.find(">*:eq(" + bIndex + ")")
                if (eAtPos[0] == this[0]) {
                    return
                }
                if (eAtPos.length) {
                    eAtPos.before(this)
                } else {
                    bParent.append(this)
                }
            }
        } else {
            this.before(to)
            var eAtPos = bParent.find(">*:eq(" + bIndex + ")")
            if (eAtPos.length) {
                eAtPos.before(this)
            } else {
                bParent.append(this)
            }
        }
    },
    /**
     * only name - retrieves save data
     * value is undefined - removes data
     * no arguments usage - tag().remove(name) [remove/get/set]
     * @param name
     * @param value
     * @returns {*}
     */
    tag: function (name, value) {
        var _this = this.nat
        if (name && !_this) {
            return null
        }
        var funcs = {
            remove: function (name) {
                if (_this[bm.expando]) {
                    var prev = _this[bm.expando][name]
                    delete _this[bm.expando][name]
                    return prev
                }
            },
            get: function (name) {
                return _this[bm.expando] && _this[bm.expando][name]
            },
            set: function (name) {
                if (!_this[bm.expando]) {
                    _this[bm.expando] = {}
                }
                var prev = _this[bm.expando][name]
                _this[bm.expando][name] = value
                return prev
            },
            iterate: function (func) {
                var all = _this[bm.expando]
                if (!all) {
                    return
                }
                $.each(all, func)
            }
        }
        if (!name) {
            return funcs
        }
        if (arguments.length == 1) {
            return funcs.get(name)
        }
        if (value === undefined) {
            return funcs.remove(name)
        }
        return funcs.set(name)
    },
    /**
     * extends jquery text function. It makes a space preceeded by space as &nbsp
     */
    text: function (text) {
        if (typeof text == "string" && (text.indexOf("\n") != -1 || text.indexOf("  ") != -1)) {
            text = text.htmlEncode()
            return this.each(function () {
                this.innerHTML = text
            })
        }
        return $_text.apply(this, arguments)
    },
    tip: function (config) {
        if (!config) {
            config = {}
        }
        this.mouseenter(function (ev) {
            $.global_tip_mouseevent_reference = ev
            var item = $(this)
            var _config = $.extend({}, config)
            _config.text = _config.text || item.attr("tip-text") || item.attr("title")
            item.removeAttr("title")
            item.trigger("tip-show", [_config])
            $.global_tip_timer_reference = setTimeout(function () {
                if ($.global_tip_inst_reference) {
                    $.global_tip_inst_reference.close()
                }
                $.global_tip_timer_reference = null
                $.global_tip_inst_reference = new POPUP({
                    content: _config.text,
                    template: "<div><div class='content'></div></div>",
                    clazz: "tip-popup",
                    modal: false,
                    is_always_up: true,
                    auto_active: false,
                    auto_active_on_focus: false,
                    ui_position: {
                        my: "left bottom",
                        at: "right top",
                        of: $.global_tip_mouseevent_reference
                    }
                })
                if (typeof config.render === 'function') {
                    config.render($.global_tip_inst_reference.content)
                }
            }, _config.delay || 350)
        }).mouseleave(function (ev) {
            var pop = $.global_tip_inst_reference
            if (pop && config.sustain_on_hover) {
                var popdom = pop.getDom()
                var offset = popdom.offset()
                offset.right = offset.left + popdom.outerWidth()
                offset.bottom = offset.top + popdom.outerHeight()
                if (ev.pageX > offset.left && ev.pageX < offset.right && ev.pageY > offset.top && ev.pageY < offset.bottom) {
                    pop.getDom().mouseleave(function () {
                        pop.close()
                        $.global_tip_inst_reference = null
                    })
                    return
                }
            }
            if ($.global_tip_timer_reference) {
                clearTimeout($.global_tip_timer_reference)
                $.global_tip_timer_reference = null
            }
            if (pop) {
                pop.close()
                $.global_tip_inst_reference = null
            }
        }).mousemove(function (ev) {
            $.global_tip_mouseevent_reference = ev
        })
        return this
    },
    _toggleClassCache: $.prototype.toggleClass,
    /**
     * overrides jquery default implementation. if a exists makes it b and vice versa
     * @param {} a
     * @param {} b
     * @returns {}
     */
    toggleClass: function (a, b) {
        if (arguments.length == 2 && b.isString) {
            if (this.hasClass(a)) {
                this.removeClass(a).addClass(b)
            } else {
                this.removeClass(b).addClass(a)
            }
            return this
        }
        return this._toggleClassCache(a, b)
    },
    topRib: function (margin, border, padding) {
        return this.dirRib("top", margin, border, padding)
    },
    triggerWithPropagation: function () {
        var eventName = arguments[0]
        var params = arguments[1]
        this.each(function () {
            var event = $.Event(eventName)
            event.target = this
            var parents = $(this).parents().addBack()
            var length = parents.length
            for (var k = length - 1; k >= 0; k--) {
                if (parents.eq(k).triggerHandler(event, params) === false) {
                    return false
                }
            }
        })
    },
    unmask: function () {
        this.removeClass("updating")
        this.find(".div-mask").detach()
    },
    /**
     * reverse of wrapInner
     * @returns {}
     */
    unwrapInner: function () {
        var wrapper = this.children().first()
        var contents = wrapper.contents()
        this.prepend(contents)
        wrapper.remove()
        return this
    },
    /**
     * extends jquery width function to set width considering element padding, margin
     */
    width: function (val/*Number(optional)*/, isOuter/*Boolean(optional)*/, includesMargin/*Boolean(optional)*/) {
        if (arguments.length < 2 || !(isOuter || includesMargin)) {
            return $_width.apply(this, arguments)
        } else {
            var reduceWidth = this.outerWidth(includesMargin || false) - this.width()
            return $_width.call(this, val - reduceWidth)
        }
    },
    /**
     * removes inline display property
     */
    xshow: function () {
        return this.each(function () {
            this.style.display = ''
        })
    }
})
Object.defineProperty($.prototype, "nat", {
    get: function () {
        return this.length ? this[0] : null
    }
})
//endregion

//region JQUERY EVENTS
bm.domnode_parent_remove_handler = []
bm.domnode_self_remove_handler = []
$(document).on("DOMNodeRemoved", function (ev) {
    var elm = ev.target
    var hash = elm.remove_key_hash || (elm.remove_key_hash = bm.getUUID())
    if (bm.domnode_self_remove_handler[hash]) {
        var _events = bm.domnode_self_remove_handler[hash]
        delete bm.domnode_self_remove_handler[hash]
        _events.every(function (key, handle) {
            elm == handle.element && handle.handler.call(elm, ev)
            return true
        })
    }
    bm.domnode_parent_remove_handler.every(function (key, value) {
        if ($(elm).find(value.element).length) {
            if (value.selector) {
                var _self = value
                $(value.element).find(value.selector).each(function () {
                    _self.handler.call(this, ev)
                })
            } else {
                value.handler.call(value.element, ev)
            }
        }
        return true
    })
})

bm.jquery_data_clean_handler = []
var original_clean = $.cleanData
$.cleanData = function (elements, acceptData) {
    if (!(elements instanceof Array)) {
        elements = $.makeArray(elements)
    }
    elements.every(function (k, elm) {
        elm.cleaning_data = true
        return true
    })
    bm.jquery_data_clean_handler.every(function (key, value) {
        if (elements.contains(value.element)) {
            if (value.selector) {
                var _self = value
                $(value.element).find(value.selector).each(function () {
                    _self.handler.call(this, $.Event("jqclean"))
                })
            } else {
                value.handler.call(value.element, $.Event("jqclean"))
            }
        }
    })
    original_clean.apply(this, arguments, acceptData)
    elements.every(function (key, elm) {
        delete elm.cleaning_data
        return true
    })
}

$.extend($.event.special, {
    //if multiple handler is bound then duration for successive handlers will be ignored
    ichange: {
        add: function (obj) {
            var selectors = obj.selector ? obj.selector.split(",") : ["no-selector"]
            var pauseDuration = obj.data || 400
            var _self = $(this)
            var namespaces = obj.namespace ? obj.namespace.split(".") : ["no-namespace"]
            var focused

            $.each(selectors, function (index, selector) {
                selector = selector.trim()
                var timer
                var initialValue

                var boundCollection = _self.data("ichange-bound")
                if (!boundCollection) {
                    boundCollection = {}
                    boundCollection[selector] = namespaces
                    _self.data("ichange-bound", boundCollection)
                } else if (boundCollection[selector] != null) {
                    $.each(namespaces, function () {
                        boundCollection[selector].push(this)
                    })
                    return
                }

                if (selector == "no-selector") {
                    selector = null
                }

                function checkForChange() {
                    var input = selector ? _self.find(selector) : _self
                    var currentValue = input.val()
                    if (currentValue != initialValue) {
                        var prevValue = initialValue
                        initialValue = currentValue
                        setTimeout(function () {
                            if (selector) {
                                _self.triggerHandler({type: "ichange", target: input[0]}, [currentValue, prevValue])
                            } else {
                                _self.triggerHandler("ichange", [currentValue, prevValue])
                            }
                        }, 5)
                    }
                }

                function startTimer() {
                    if (timer) {
                        clearInterval(timer)
                    }
                    timer = setInterval(checkForChange, pauseDuration)
                }

                var focusHandler = function () {
                    initialValue = this.value
                    focused = this
                    startTimer()
                }
                var blurHandler = function () {
                    if (timer) {
                        clearInterval(timer)
                    }
                    checkForChange()
                }
                var changeHandler = function (e, v1, v2) {
                    if (this == focused) {
                        return
                    }
                    if (selector) {
                        _self.triggerHandler({type: "ichange", target: this}, [this.value, v2])
                    } else {
                        _self.triggerHandler("ichange", [this.value, v2])
                    }
                }

                if (selector) {
                    _self.on("focus.ichange-event", selector, focusHandler)
                    _self.on("blur.ichange-event", selector, blurHandler)
                    _self.on("keydown.ichange-event", selector, startTimer)
                    _self.on("change.ichange-event", selector, changeHandler)
                } else {
                    _self.on("focus.ichange-event", focusHandler)
                    _self.on("blur.ichange-event", blurHandler)
                    _self.on("keydown.ichange-event", startTimer)
                    _self.on("change.ichange-event", changeHandler)
                    _self.each(function () {
                        this.isilent = function (value) {
                            if (this == focused) {
                                initialValue = value
                            }
                            this.value = value
                            return $(this)
                        }
                    })
                }
            })
        },
        remove: function (obj) {
            var selectors = obj.selector ? obj.selector.split(",") : ["no-selector"]
            var _self = $(this)
            var modifiedSelectors = ""
            $.each(selectors, function (index, selector) {
                selector = selector.trim()
                var boundCollection = _self.data("ichange-bound")
                if (!boundCollection) {
                    return
                } else if (boundCollection[selector] && obj.namespace) {
                    var namespaces = obj.namespace.split(".")
                    var addedNamespaces = boundCollection[selector]
                    $.each(namespaces, function (index, namespace) {
                        var indices = []
                        $.each(addedNamespaces, function (_index) {
                            if (namespace == this) {
                                indices.push(_index)
                            }
                        })
                        $.each(indices, function (n) {
                            boundCollection[selector].splice(this - n, 1)
                        })
                    })
                    if (boundCollection[selector].length == 0) {
                        modifiedSelectors += " " + selector
                        delete boundCollection[selector]
                    }
                    return
                } else {
                    modifiedSelectors += " " + selector
                }
            })
            if (modifiedSelectors == "") {
                return
            } else if (modifiedSelectors != "no-selector") {
                _self.off("focus.ichange-event", modifiedSelectors)
                _self.off("blur.ichange-event", modifiedSelectors)
                _self.off("keydown.ichange-event", modifiedSelectors)
            } else {
                _self.off("focus.ichange-event")
                _self.off("blur.ichange-event")
                _self.off("keydown.ichange-event")
            }
        }
    },
    remove: {
        add: function (obj) {
            var hash = {
                element: this,
                namespace: obj.namespace,
                handler: obj.handler
            }
            if (!obj.selector) {
                var hashKey = this.remove_key_hash || (this.remove_key_hash = bm.getUUID())
                if (!bm.domnode_self_remove_handler[hashKey]) {
                    bm.domnode_self_remove_handler[hashKey] = []
                }
                bm.domnode_self_remove_handler[hashKey].push(hash)
            }
            bm.domnode_parent_remove_handler.push(hash)
        },
        remove: function (obj) {
            var _self = this
            if (!obj.selector && !this.cleaning_data) {
                var hash = this.remove_key_hash || (this.remove_key_hash = bm.getUUID())
                if (bm.domnode_self_remove_handler[hash]) {
                    bm.domnode_self_remove_handler[hash].removeAll(function (handle) {
                        return _self == handle.element && (!obj.namespace || obj.namespace == handle.namespace) && (!obj.handler || obj.handler == handle.handler)
                    })
                    if (!bm.domnode_self_remove_handler[hash].length) {
                        delete bm.domnode_self_remove_handler[hash]
                    }
                }
            }
            bm.domnode_parent_remove_handler.removeAll(function (handle) {
                return _self == handle.element && (!obj.namespace || obj.namespace == handle.namespace) && (!obj.selector || obj.selector == handle.selector) && (!obj.handler || obj.handler == handle.handler)
            })
        }
    },
    jqclean: {
        add: function (obj) {
            bm.jquery_data_clean_handler.push({
                element: this,
                namespace: obj.namespace,
                selector: obj.selector,
                handler: obj.handler
            })
        },
        remove: function (obj) {
            var _self = this
            bm.jquery_data_clean_handler.removeAll(function (handle) {
                return _self == handle.element && (!obj.namespace || obj.namespace == handle.namespace) && (!obj.selector || obj.selector == handle.selector) && (!obj.handler || obj.handler == handle.handler)
            })
        }
    }
})
//endregion

//region NUMBER
var _to_fixed = Number.prototype.toFixed
$.extend(Number.prototype, {
    /**
     * removes trailing 0s from fixed values if true is passed as trimmed
     */
    toFixed: function (count, trimmed) {
        var fixed = _to_fixed.call(this, count)
        if (trimmed) {
            return fixed.replace(/\.?0+$/, '')
        }
        return fixed
    },
    /**
     * considers value as byte and returns its representation in KB/MB/GB whichever convenient
     */
    toByteNotation: function () {
        if (this < 1024) {
            return this.toFixed(3, true) + " B"
        }
        var kilo = this / 1024
        if (kilo < 1024) {
            return kilo.toFixed(3, true) + " KB"
        }
        var mega = kilo / 1024
        if (mega < 1024) {
            return mega.toFixed(3, true) + " MB"
        }
        var giga = mega / 1024
        if (giga < 1024) {
            return giga.toFixed(3, true) + " GB"
        }
        var tera = giga / 1024
        return tera.toFixed(3, true) + " TB"
    },
    loop: function (func, startFrom) {
        if (startFrom == null) {
            startFrom = 0
        }
        var endTo = this + startFrom
        for (var g = startFrom; g < endTo; g++) {
            func(g)
        }
    },
    rloop: function (func, startFrom) {
        if (startFrom == null) {
            startFrom = this
        }
        var endTo = this - startFrom
        for (var g = startFrom; g > endTo; g--) {
            var breakIt = func(g)
            if (breakIt === false) {
                break
            }
        }
    }
})
//endregion

//region STRINGWRITER
function StringWriter() {
    this.buffer = []
}

StringWriter.prototype.plus = function (j) {
    this.buffer.push(j)
    return this
}

StringWriter.prototype.toString = function () {
    return this.buffer.join("")
}
//endregion

//region DATE
$.extend(Date.prototype, {
    gmt: function () {
        var gmtDate = new Date(this.getTime() + (this.getTimezoneOffset() * 60000))
        return gmtDate
    },
    toZone: function () {
        var gmtDate = new Date(this.getTime() - (this.getTimezoneOffset() * 60000))
        return gmtDate
    },
    format: function (format) {
        return this.toString(format)
    }
})
//endregion

//region OBJECT
var _cachedObjectProtoToString = Object.prototype.toString
Object.defineProperty(Object.prototype, "isArray", {
    get: function () {
        return this.cached_is_array || _cachedObjectProtoToString.call(this) == "[object Array]"
    },
    set: function (v) {
        this.cached_is_array = v
    }
})
Object.defineProperty(Object.prototype, "isFunction", {
    get: function () {
        return this.cached_is_function || typeof this === "function"
    },
    set: function (v) {
        this.cached_is_function = v
    }
})
Object.defineProperty(Object.prototype, "isString", {
    get: function () {
        return _cachedObjectProtoToString.call(this) == "[object String]"
    }
})
Object.defineProperty(Object.prototype, "isNumber", {
    get: function () {
        return this.cached_is_number || _cachedObjectProtoToString.call(this) == "[object Number]"
    },
    set: function(v) {
        this.cached_is_number = v
    }
})
Object.defineProperty(Object.prototype, "isDate", {
    get: function () {
        return _cachedObjectProtoToString.call(this) == "[object Date]"
    }
})
Object.defineProperty(Object.prototype, "isUndefined", {
    get: function () {
        return typeof this === "undefined"
    }
})
Object.defineProperty(Object.prototype, "jqObject", {
    get: function () {
        return this instanceof String ? $(this.toString()) : $(this)
    }
})
//endregion

//region JQUERY EXPR
var sizzle_expando = Object.keys($.expr.pseudos.not)[0]
$.extend($.expr.pseudos, {
    hasparent: (function () {
        var fn = function (selector) {
            return function (elem) {
                return $(elem.parentNode).is(selector)
            }
        }
        fn[sizzle_expando] = true
        return fn
    })(),
    value: (function () {
        var fn = function (selector) {
            return function (elem) {
                return elem.value == selector
            }
        }
        fn[sizzle_expando] = true
        return fn
    })()
})
//endregion