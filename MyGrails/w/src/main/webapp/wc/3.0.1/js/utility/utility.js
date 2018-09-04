var bm = {
    cache: {}
};

if (!window.console || !window.console.log) {
    (window.console || (window.console = {})).log = function (message) {
        var logContainer = $(".log-container")
        if (!logContainer.length) {
            logContainer = $("<div class='log-container'></div>").appendTo(document.body)
        }
        logContainer.append($("<div class='log-entry'></div>").append("" + message))
    }
}
window.log = function () {
    Function.prototype.apply.call(console.log, console, arguments) //console.log is not a true function in ie
}

if (!console.dir) {
    console.dir = console.log
}
window.dir = function () {
    Function.prototype.apply.call(console.dir, console, arguments)
}

function QueueManager(queueThreshold, afterPop, synchronous) {
    var _running_count = 0
    var _queue = []
    var timer

    function checkFreeAndCall() {
        timer = null
        while (this.hasFreeSlot() && _queue.length) {
            var obj = this.pop()
            if (synchronous) {
                instance.inc()
                setTimeout(function () {
                    afterPop(obj)
                    instance.dec()
                }, 1)
            } else {
                afterPop(obj)
            }
        }
        if (_queue.length) {
            timer = setTimeout($.proxy(checkFreeAndCall, this), 1000)
        }
    }

    var instance = {
        push: function (obj, placeInTop) {
            if (placeInTop) {
                _queue.unshift(obj)
            } else {
                _queue.push(obj)
            }
            if (!this.hasFreeSlot()) {
                if (timer) {
                    return
                }
                timer = setTimeout($.proxy(checkFreeAndCall, this), 1000)
            } else if (synchronous) {
                checkFreeAndCall()
            }
        },
        pop: function () {
            return _queue.shift()
        },
        inc: function () {
            _running_count++
        },
        dec: function () {
            _running_count--
        },
        hasFreeSlot: function () {
            return _running_count < queueThreshold
        },
        index: function (matcher) {
            var foundIndex = null
            $.each(_queue, function (index) {
                if (matcher.call(this, index)) {
                    foundIndex = index
                    return false
                }
            })
            return foundIndex
        },
        top: function (index) {
            if (_queue.length > index && index >= 0) {
                var obj = _queue[index]
                _queue.splice(index, 1)
                _queue.unshift(obj)
            }
        },
        remove: function (matcher) {
            $.each(_queue, function (index) {
                if (matcher.call(this, index)) {
                    _queue.splice(index, 1)
                    return false
                }
            })
        },
        clear: function () {
            if (timer) {
                clearTimeout(timer)
            }
            _queue = []
        }
    }

    if (this instanceof QueueManager) {
        $.extend(this, instance)
    } else {
        return instance
    }
}

bm.ajax_queue_manager = QueueManager(30, function (entry) {
    bm.ajax_queue_manager.inc()
    var settings = entry.settings
    var xhr = $.ajax(settings)
    bm.chainDeffereds(xhr, settings.deferred_interceptor)
})

function CacheManager() {
    this.cache = {}
}

CacheManager.prototype = {
    get: function (flag) {
        var entry = this.cache[flag]
        if (entry) {
            if (entry.duration) {
                var timeDiff = Date.now().getTime() - entry.time
                if (timeDiff > entry.duration) {
                    delete this.cache[flag]
                    return undefined
                } else {
                    return entry.entry
                }
            }
            return entry.entry
        }
        return undefined
    },
    set: function (flag, duration, entry) {
        this.cache[flag] = {duration: duration, entry: entry, time: Date.now().getTime()}
    }
}

bm.ajax_cache_manager = new CacheManager()
bm.show_response_status = true
window.on_login_call_queue = []

$.extend(bm, {
    _modified_ajax_settings: function (settings) {
        var defaults = {
            traditional: true,
            show_response_status: bm.show_response_status,
            show_error_status: true,
            show_success_status: true,
            dataType: "json",
            type: settings.type || (settings.data ? "post" : "get")
        }
        settings = $.extend(defaults, settings)
        if (settings.fill) {
            settings.dataType = "html"
        }
        var caller_error = settings.error
        var caller_success = settings.success
        settings.error = function (xhr, status) {
            function jsonParse(json) {
                try {
                    return JSON.parse(json)
                } catch (k) {
                    return {status: "error", message: json, code: xhr.status}
                }
            }

            if (xhr.status == 310) {
                if (settings.dataType == "json") {
                    var response = jsonParse(xhr.responseText)
                    window.location.href = response.url
                } else {
                    window.location.href = xhr.responseText
                }
                throw $.error("Redirect Action Processing")
            }
            if (xhr.status == 403) {
                var message
                if (settings.dataType == "json") {
                    var response = jsonParse(xhr.responseText)
                    message = response.message
                } else {
                    message = xhr.responseText
                }
                bm.notify(message, "error")
            }
            var response = xhr.responseText
            if (settings.fill) {
                if (settings.loader !== false) {
                    settings.fill.loader(false)
                }
                if (!app.production) {
                    settings.fill.html(response)
                }
                delete settings.fill.nat.filling_xhr
            }
            if (xhr.status == 401) {
                if (settings.dataType == "json") {
                    response = JSON.parse(response).login_html
                }
                if (settings.on_login) {
                    window.on_login_call_queue.push(function () {
                        setInterceptor()
                        settings.on_login()
                    })
                }
                if (window.inline_login_displaying) {
                    return
                }
                window.inline_login_displaying = true
                var willEmbeddedLogin = true
                var embeddedLoginConfig = {content: response}
                if (app.is_front_end) {
                    if (!app.edit_mode) {
                        willEmbeddedLogin = false
                        bm.alert($.i18n.prop("your.session.timed.out"), "time-out", function () {
                            location.href = app.baseUrl + "customer/login?referer=" + location.href
                        })
                    } else {
                        embeddedLoginConfig = {ajax_url: app.baseUrl + "page/embeddedLogin", title: null}
                    }
                }
                if (willEmbeddedLogin) {
                    $(response).find("input[name=email]").val(app.login_email).prop('disabled', true)
                    bm.embeddedLogin(embeddedLoginConfig, function () {
                        window.on_login_call_queue.every(function () {
                            this.call(window)
                        })
                        window.on_login_call_queue = []
                        window.inline_login_displaying = false
                    })
                }
                return
            }
            //if any success response get jquery error then status will be 200
            if (settings.dataType == "json") {
                if (xhr.status == 200) {
                    response = $.i18n.prop("server.response.unrecongnized")
                    response = {status: "error", code: "500", message: response}
                } else {
                    if (response) {
                        response = jsonParse(response)
                    } else {
                        response = {status: "error", code: "500"}
                    }
                }
                if (settings.response) {
                    settings.response.apply(this, ["error", response, status, xhr])
                }
                if (settings.show_response_status && settings.show_error_status && response.message) {
                    bm.notify(response.message, response.status)
                }
            } else if (settings.response) {
                settings.response.apply(this, ["error", response, status, xhr])
            }
            if (caller_error) {
                caller_error.apply(this, [xhr, status, response])
            }
        }
        settings.success = function (response, status, xhr) {
            if (settings.response) {
                settings.response.apply(this, [settings.dataType == "json" && response.status == "error" ? "error" : "success", response, status, xhr])
            }
            if (settings.dataType == "json" && settings.show_response_status && response.message) {
                if ((response.status == "error" && settings.show_error_status) || settings.show_success_status) {
                    bm.notify(response.message, response.status)
                }
            }
            if (settings.dataType == "json" && response.status == "error") {
                if (caller_error) {
                    caller_error.apply(this, [xhr, status, response])
                }
                return
            }
            if (settings.fill) {
                if (settings.loader !== false) {
                    settings.fill.loader(false)
                }
                delete settings.fill.nat.filling_xhr
                settings.fill.html(response).updateUi()
                if (settings.processor) {
                    if (settings.processor.isString) {
                        var category, processor
                        if (settings.processor.contains(":")) {
                            var splitted = settings.processor.split(":")
                            category = splitted[0]
                            processor = splitted[1]
                        } else {
                            processor = category = settings.processor
                        }
                        bm.onReady(app.processors, processor, {
                            ready: function () {
                                app.processors[processor](settings.fill)
                            },
                            not: function () {
                                bm.loadScript(app.base + "js/app/processors/" + (category || processor) + ".js")
                            }
                        })
                    } else if ($.isFunction(settings.processor)) {
                        settings.processor(settings.fill)
                    }
                }
                bm.handleUrlNavigation(settings.url || settings.controller + "/" + settings.action, settings.fill)
            }
            if (caller_success) {
                caller_success.apply(this, [response, status, xhr])
            }
        }

        function setInterceptor() {
            settings.deferred_interceptor = $.Deferred()
            settings.deferred_interceptor.done(function (response) {
                if (window.inline_login_displaying) {
                    return
                }
                var returns = Array.prototype.slice.call(arguments)
                returns.push(settings)
                if (settings.dataType == "json" && response.status == "error") {
                    settings.consumer_deferred.reject.apply(settings.consumer_deferred, returns)
                } else {
                    settings.consumer_deferred.resolve.apply(settings.consumer_deferred, returns)
                }
            }).fail(function () {
                if (window.inline_login_displaying) {
                    return
                }
                var returns = Array.prototype.slice.call(arguments)
                var response = returns[0].responseText
                if (settings.dataType == "json") {
                    if (returns[0].status == 404) {
                        response = {error: true, message: $.i18n.prop("expected.resource.not.found"), code: "404"}
                    } else if (returns[0].status == 200) {
                        response = {error: true, message: $.i18n.prop("server.response.unrecongnized"), code: "500"}
                    } else {
                        if (response) {
                            response = JSON.parse(response)
                        } else {
                            response = {error: true, message: $.i18n.prop("error.occurred.in.server"), code: "500"}
                        }
                    }
                }
                returns[2] = response
                returns.push(settings)
                settings.consumer_deferred.reject.apply(settings.consumer_deferred, returns)
            }).progress(function () {
                var returns = Array.prototype.slice.call(arguments)
                returns.push(settings)
                settings.consumer_deferred.notify.apply(settings.consumer_deferred, returns)
            })
        }

        setInterceptor()
        return settings
    },
    addScript: function (src, async) {
        var head = $("head")
        var script = document.createElement('script')
        script.setAttribute("type", "text/javascript")
        if (!src.startsWith("http") && !src.startsWith("/")) {
            src = app.baseUrl + src
        }
        script.setAttribute("src", src)
        if (async) {
            script.setAttribute("async", "")
        }
        head[0].appendChild(script)
    },
    addStyle: function (href) {
        var head = $("head"), link = document.createElement("link")
        link.setAttribute("rel", "stylesheet")
        link.setAttribute("type", "text/css")
        link.setAttribute("href", app.baseUrl + href)
        head[0].appendChild(link)
    },
    ajax: function (settings) {
        var caller_response = settings.response
        settings.response = function () {
            bm.ajax_queue_manager.dec()
            if (caller_response) {
                caller_response.apply(this, arguments)
            }
        }
        settings = bm._modified_ajax_settings(settings)
        if (settings.cache_flag || settings.cache_duration) {
            var flag = settings.cache_flag
            if (!flag) {
                flag = settings.url
                if (settings.data) {
                    flag += "?" + $.param(settings.data)
                }
            }
            var deferred = bm.ajax_cache_manager.get(flag)

            function xhrCopy(xhr) {
                return {status: xhr.status, responseText: xhr.responseText}
            }

            if (deferred) {
                var newDeffered = $.Deferred()
                deferred.done(function (data, status, xhr) {
                    settings.response = caller_response
                    if (settings.success) {
                        settings.success.call(settings, data, status)
                    }
                    if (settings.complete) {
                        settings.complete.call(settings, xhrCopy(xhr), status)
                    }
                    newDeffered.resolve(data, status)
                })
                deferred.fail(function (xhr, status) {
                    settings.response = caller_response
                    var copyXhr = xhrCopy(xhr)
                    if (settings.error) {
                        settings.error.call(settings, copyXhr, status)
                    }
                    if (settings.complete) {
                        settings.complete.call(settings, copyXhr, status)
                    }
                    newDeffered.reject(copyXhr, status)
                })
                return newDeffered
            }
        }
        var cache_deferred
        if (settings.cache_flag || settings.cache_duration) {
            cache_deferred = $.Deferred()
            var duration = settings.cache_duration
            var flag = settings.cache_flag
            if (!flag) {
                flag = settings.url
                if (settings.data) {
                    flag += "?" + $.param(settings.data)
                }
            }
            if (!duration) {
                duration = 400
            }
            if (duration == -1) {
                duration = undefined
            }
            bm.ajax_cache_manager.set(flag, duration, cache_deferred)
        }
        var queueId = bm.getUUID()
        bm.ajax_queue_manager.push({id: queueId, settings: settings})
        if (!bm.ajax_queue_manager.hasFreeSlot()) {
            var deferred = cache_deferred || $.Deferred()
            settings.consumer_deferred = $.extend(deferred, {
                aborted: 0,
                responseText: null,
                responseXML: null,
                status: 0,
                statusText: 'n/a',
                getAllResponseHeaders: function () {
                },
                getResponseHeader: function () {
                },
                setRequestHeader: function () {
                },
                abort: function () {
                    bm.ajax_queue_manager.remove(function () {
                        return this.id == queueId
                    })
                }
            })
            return settings.consumer_deferred
        }
        var entry = bm.ajax_queue_manager.pop()
        bm.ajax_queue_manager.inc()
        settings = entry.settings
        if ((browser.ie || browser.ed) && settings.type.toLowerCase().equals("get", false)) {
            if (settings.data) {
                settings.data._ = Date.now()
            } else {
                settings.data = {_: Date.now()}
            }
        }
        settings.on_login = function () {
            if (settings.url) {
                xhr = $.ajax(settings)
            } else {
                xhr = $.ajax(app.baseUrl + settings.controller + "/" + (settings.action || 'index'), settings)
            }
            bm.chainDeffereds(xhr, settings.deferred_interceptor)
        }
        settings.consumer_deferred = $.Deferred()
        var xhr
        if (settings.fill) {
            if (settings.fill.isString) {
                settings.fill = $(settings.fill)
            }
        }
        if (settings.beforeAjax) {
            settings.beforeAjax(settings)
        }
        if (settings.url) {
            xhr = $.ajax(settings)
        } else {
            xhr = $.ajax(app.baseUrl + settings.controller + "/" + (settings.action || 'index'), settings)
        }
        if (settings.fill) {
            if (settings.fill.nat.filling_xhr) {
                settings.fill.nat.filling_xhr.abort()
            }
            settings.fill.nat.filling_xhr = xhr
            settings.fill.one("remove.xhr", function () {
                xhr.abort()
            })
            xhr.always(function () {
                settings.fill.off("remove.xhr")
            })
            if (settings.loader !== false) {
                settings.fill.loader()
            }
        }
        bm.chainDeffereds(xhr, settings.deferred_interceptor)
        if (cache_deferred) {
            bm.chainDeffereds(settings.consumer_deferred, cache_deferred)
        }
        xhr.success = function (handler) {
            settings.consumer_deferred.done(handler)
            return xhr
        }
        xhr.error = function (handler) {
            settings.consumer_deferred.fail(handler)
            return xhr
        }
        xhr.complete = function (handler) {
            settings.consumer_deferred.always(handler)
            return xhr
        }
        return $.extend({}, xhr, settings.consumer_deferred)
    },
    autoType: function (value) {
        if (value == "{}") {
            return {}
        }
        if (value == "[]") {
            return []
        }
        if (/^\[("[^"]+"[^=:]|\d+)/.test(value)) {
            return JSON.parse(value)
        }
        if (/^({|\[{)"[^"]+":/.test(value)) {
            return JSON.parse(value)
        }
        if (/^\s*-?\d+(\.\d*)?\s*$/.test(value)) {
            return +value
        }
        if (/^\s*(true|yes|on)\s*$/.test(value)) {
            return true
        }
        if (/^\s*(false|no|off)\s*$/.test(value)) {
            return false
        }
        return value
    },
    baseUrl: (window.app && app.baseUrl) || "/",
    buildQuery: function (queryMap) {
        var isFirst = true, query = ""
        $.each(queryMap, function (k, v) {
            if (isFirst) {
                isFirst = false
            } else {
                query += "&"
            }
            query += k + "=" + encodeURIComponent(v)
        })
        return query
    },
    center: function (width, height, is_fixed) {
        var scrHeight = $(window).height()
        var scrWidth = $(window).width()
        var popHeight = height
        var popWidth = width
        var left = scrWidth / 2 - popWidth / 2
        var top = scrHeight / 2 - popHeight / 2
        if (!is_fixed) {
            var scrollTop = $(window).scrollTop()
            var scrollLeft = $(window).scrollLeft()
            left = left + scrollLeft
            top = top + scrollTop
        }
        return {
            left: Math.max(20, left),
            top: Math.max(20, top)
        }
    },
    chainDeffereds: function (from, to) {
        from.done(to.resolve).fail(to.reject).progress(to.notify)
    },
    collect: function (map, prop) {
        var ret = []
        if ($.isFunction(prop)) {
            $.each(map, function (k, v) {
                ret.push(prop.call(v, k, v))
            })
        } else {
            $.each(prop, function () {
                ret.push(map[this])
            })
        }
        return ret
    },
    convertMilliToHMS: function (mili) {
        var h = Math.floor(mili / 3600000.0)
        mili -= h * 3600000
        var m = Math.floor(mili / 60000.0)
        mili -= m * 60000
        var s = mili / 1000.0
        return h + "h " + m + "m " + s + "s"
    },
    creteCookie: function (name, value, expires, path, domain) {
        var cookie = name + "=" + value + ""
        if (expires) {
            if (!expires instanceof Date) {
                expires = new Date(new Date().getTime() + parseInt(expires) * 1000 * 60 * 60 * 24)
            }
            cookie += "expires=" + expires.gmtString() + ""
        }
        if (path) {
            cookie += "path=" + path + ""
        }
        if (domain) {
            cookie += "domain=" + domain + ""
        }
        document.cookie = cookie
    },
    download: function (url) {
        var iframe = $.find("iframe#dynamic-download-iframe")
        if (iframe.length == 0) {
            var newIframe = $('<iframe id="dynamic-download-iframe">')
            newIframe.appendTo('body')
            newIframe.css({
                position: "absolute",
                left: "-10000px",
                top: "-10000px"
            })
            newIframe.attr("src", url)
        } else {
            $(iframe).attr("src", url)
        }
    },
    dumpInForm: function (form, data) {
        for (var k in data) {
            var input = form.find("[name='" + k + "']")
            var value = data[k] || []
            if (value.isString) {
                value = [value]
            } else {
                value = value.clone()
            }
            var cachedRadios = $()
            input.each(function () {
                var single = this.jqObject
                if (single.is(":text") || single.is("textarea")) {
                    if (value.length) {
                        single.val(value.shift())
                    } else {
                        single.val("")
                    }
                } else if (single.is("select[multiple]")) {
                    single.find("option:selected").prop("selected", false)
                    single.find("option").each(function () {
                        if (value.contains(this.value)) {
                            this.selected = true
                            value.remove(this.value)
                        }
                    })
                    single.select("refresh")
                } else if (single.is("select")) {
                    if (value.length) {
                        single.select("val", value.shift())
                    } else {
                        var v = single.find("option:first").val()
                        single.select("val", v)
                    }
                } else if (single.is(":radio")) {
                    if (!cachedRadios) {
                        return
                    }
                    if (value.contains(this.value)) {
                        single.prop("checked", true)
                        cachedRadios = null
                        value.remove(this.value)
                    } else {
                        cachedRadios = cachedRadios.add(single)
                    }
                } else if (single.is(":checkbox")) {
                    if (value.contains(this.value)) {
                        single.prop("checked", true)
                        value.remove(this.value)
                    } else {
                        single.prop("checked", false)
                        var uncheck = single.attr("data-uncheck-value")
                        if (uncheck && value.contains(uncheck)) {
                            value.remove(uncheck)
                        }
                    }
                }
            })
            if (cachedRadios) {
                cachedRadios.filter("[checked]").prop("checked", true)
            }
            value.every(function () {
                form.append("<input type='hidden'>".jqObject.attr("name", k).val(this))
            })
        }
    },
    dumpInMemory: function (form) {
        return form.serializeObject()
    },
    encodePath: function (path) {
        return path.replace(/%/g, "%25").replace(/ /g, "%20").replace(/#/g, "%23")
    },
    embeddedLogin: function (config, success) {
        return new POPUP($.extend({
            clazz: "login-popup",
            title: $.i18n.prop("session.timed.out"),
            show_close: false,
            close_on_escape: false,
            is_always_up: true,
            events: {
                content_loaded: function (popup) {
                    var form = this.find("form")
                    if (app.login_email) {
                        form.find("input[name=email]").prop('disabled', true).val(app.login_email)
                        form.append('<input type="hidden" name="email" value="' + app.login_email + '">')
                        form.find(".not-admin").html($.i18n.prop("not.x.operator", ["<span class='user'>" + app.login_email + "</span>"]))
                    }
                    form.form({
                        ajax: {
                            success: function (resp) {
                                if (resp.status == "error") {
                                    bm.notify($.i18n.prop("invalid.email.password"), "alert")
                                    return
                                }
                                popup.close()
                                success(resp)
                            },
                            error: function () {
                                bm.notify($.i18n.prop("invalid.email.password"), "alert")
                            }
                        }
                    })
                }
            }
        }, config))

    },
    equal: function (a, b) {
        var mapCheck = function (a, b) {
            var eq = true
            $.each(a, function (k, v) {
                eq = bm.equal(v, b[k])
                if (!eq) {
                    return false
                }
            })
            return eq
        }

        if (!a) {
            if (b) {
                return false
            }
            return true
        }
        if ($.isFunction(a.equals)) {
            return a.equals(b)
        }
        if ($.isArray(a) || a instanceof $) {
            if ($.isArray(b) || b instanceof $) {
                if (a.length == b.length) {
                    return mapCheck(a, b)
                } else {
                    return false
                }
            } else {
                return false
            }
        }
        if ($.isPlainObject(a)) {
            if ($.isPlainObject(b)) {
                return mapCheck(a, b)
            } else {
                return false
            }
        }
        return a == b
    },
    fileExtension: function (name) {
        var indexof = name.lastIndexOf(".")
        if (indexof > 0) {
            return name.substring(indexof + 1)
        }
        return ""
    },
    filter: function (_this, filter) {
        var rt = {}
        $.each(_this, function (k, v) {
            if (filter(k, v)) {
                rt[k] = v
            }
        })
        return rt
    },
    filterOwnProperties: function (obj) {
        var props = {}
        for (key in obj) {
            if (obj.hasOwnProperty(key)) {
                props[key] = obj[key]
            }
        }
        return props
    },
    getAbsoluteURL: function (relativeURL) {
        var port = location.port
        var host = location.host
        return host + bm.baseUrl + (port ? ":" + port : "") + relativeURL
    },
    getBaseURLProtocol: function () {
        return location.protocol
    },
    getCookie: function (name) {
        var regexp = new RegExp("(?:^" + name + "|\s*" + name + ")=(.*?)(?:|$)", "g")
        var result = regexp.exec(document.cookie)
        return (result === null) ? null : result[1]
    },
    getUUID: function () {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
            var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8)
            return v.toString(16).toUpperCase()
        })
    },
    handleUrlNavigation: function (url, panel) {
        var route
        var params
        if (url.startsWith(app.base)) {
            url = url.substring(app.base.length)
        }
        Object.keys(app.routes || {}).revery(function () {
            params = url.match(new RegExp(this))
            if (params) {
                route = app.routes[this]
                return false
            }
        })
        if (route) {
            if (route.isString) {
                params.shift()
                params.every(function (ind, v) {
                    route = route.replace("{" + ind + "}", v)
                })
                if (app.processors && $.isFunction(app.processors[route])) {
                    app.processors[route](panel)
                }
            } else if ($.isFunction(route)) {
                route(panel)
            } else {
                params.shift()
                route = $.extend({}, route)
                $.each(route, function (k, v) {
                    params.every(function (ind, _v) {
                        v = v.replace("{" + (ind + 1) + "}", _v)
                    })
                    route[k] = v
                })
                if (route.controller && route.action) {
                    var controller = route.controller.capitalize() + "Controller"
                    if (app.controllers[controller] && app.controllers[controller].prototype[route.action]) {
                        new app.controllers[controller]()[route.action](panel, route)
                    }
                }
            }
        }
    },
    hash: function (a) {
        var mapHash = function (a) {
            var hash = 0
            $.each(a, function (k, v) {
                hash += bm.hash(k)
                hash += bm.hash(v)
            })
            return hash
        }
        var hash = 0
        if (!a) {
            return hash
        }
        if ($.isFunction(a)) {
            return hash
        }
        if ($.isFunction(a.hash)) {
            return a.hash()
        }
        if ($.isArray(a) || $.isPlainObject(a)) {
            return mapHash(a)
        }
        return ("" + a).hash()
    },
    htmlEncode: function (toEncode) {
        return ("" + toEncode).htmlEncode()
    },
    i18n: function (prop, args) {
        return $.i18n.prop(prop, args)
    },
    intersect: function (rect1, rect2) {
        var x_total = rect2.right - rect2.left
        var x_match = rect1.right - rect1.left + rect2.right - rect2.left - (rect1.right > rect2.right ? rect1.right : rect2.right) + (rect1.left < rect2.left ? rect1.left : rect2.left)
        if (x_match <= 0) {
            return 0
        }
        x_match = x_match / x_total * 100

        var y_total = rect2.bottom - rect2.top
        var y_match = rect1.bottom - rect1.top + rect2.bottom - rect2.top - (rect1.bottom > rect2.bottom ? rect1.bottom : rect2.bottom) + (rect1.top < rect2.top ? rect1.top : rect2.top)
        if (y_match <= 0) {
            return 0
        }
        y_match = y_match / y_total * 100

        return x_match < y_match ? x_match : y_match
    },
    isOverAxis: function (x, reference, size) {
        return (x >= reference) && (x <= (reference + size))
    },
    isString: function (m) {
        return m instanceof String || typeof m == "string"
    },
    iterate: function (obj, iterator) {
        var indices = []
        $.each(obj, function (ind) {
            indices.push(ind)
        })
        var iter_caller = function (handler, index) {
            iterator.call(obj[index], handler, index)
        }
        indices.iterate(function (handler) {
            iter_caller(handler, this)
        })
    },
    /**
     * @param {} name
     * @param {} clazz
     * @param {} configs options are {common_for_multi_element, markup_config_key}
     * @returns {}
     */
    jquerify: function (name, clazz, configs) {
        if (!configs) {
            configs = {}
        }
        var plugin = name.camelCase(false)
        if (window.jquery_conflict_prefix) {
            plugin = window.jquery_conflict_prefix + plugin
        }
        var destroyer = clazz.prototype.destroy
        var _destroyer = function () {
            this.elm.tag("bmui-" + name + "-obj", undefined)
            this.elm.trigger(name + "-uninitialized")
        }
        if (destroyer) {
            destroyer = destroyer.blend(_destroyer)
        } else {
            destroyer = _destroyer
        }
        clazz.prototype.destroy = destroyer
        $.prototype[plugin] = function (options) {
            if (!this.length) {
                if (options == "inst") {
                    return null
                }
                if (options == "isInitialized") {
                    return false
                }
                return this
            }
            if (options && options.isString) {
                let returnable = undefined
                let invokerForEach = function() {
                    var obj = this.tag("bmui-" + name + "-obj")
                    if (options == "inst") {
                        return obj
                    }
                    if (obj) {
                        if (options == "isInitialized") {
                            return true
                        }
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
                                bm.prop(obj.options, arguments[1], arguments[2])
                            }
                            return this
                        }
                        if (options == "extendObjElement") {
                            arguments[1].tag("bmui-" + name + "-obj", obj)
                        }
                        if (options == "removeObjElement") {
                            arguments[1].tag("bmui-" + name + "-obj", undefined)
                        }
                        if (!$.isFunction(obj[options]) || options.startsWith("_")) {
                            throw new Error("Undefined function " + options)
                        }
                        return obj[options].apply(obj, Array.prototype.splice.call(arguments, 1))
                    } else if (options == "isInitialized") {
                        return false
                    } else {
                        throw $.error("Function is called prior to initialization")
                    }
                }
                let copiedArgs = Array.prototype.splice.call(arguments, 0)
                this.each(function() {
                    let retCode = invokerForEach.apply(this.jqObject, copiedArgs)
                    if (retCode !== undefined) {
                        returnable = retCode
                        return false
                    }
                })
                if(returnable === undefined) {
                    return this
                }
                return returnable
            }
            var create = function () {
                var _options = $.extend({}, options, this.config(configs.markup_config_key || name))
                var obj = new clazz(this, _options)
                obj.elm = this
                if (!obj.options) {
                    obj.options = _options
                }
                this.tag("bmui-" + name + "-obj", obj)
                this.trigger(name + "-initialized")
            }
            if (this.length > 1 && !configs.common_for_multi_element) {
                this.each(function () {
                    create.call($(this))
                })
            } else {
                create.call(this)
            }
            return this
        }
        if (!clazz.prototype.trigger) {
            clazz.prototype.trigger = function () {
                Array.prototype.unshift.call(arguments, name)
                bm.trigger.apply(this, arguments)
            }
        }
    },
    keyCode: {
        BACKSPACE: 8,
        COMMA: 188,
        DELETE: 46,
        DOWN: 40,
        END: 35,
        ENTER: 13,
        ESCAPE: 27,
        HOME: 36,
        LEFT: 37,
        PAGE_DOWN: 34,
        PAGE_UP: 33,
        PERIOD: 190,
        RIGHT: 39,
        SPACE: 32,
        TAB: 9,
        UP: 38
    },
    /**
     * @param elem
     * @returns {number}
     * 0 left top
     * 1 right top
     * 2 left right top
     * 3 left bottom
     * 4 right bottom
     * 5 left right bottom
     * 6 left top bottom
     * 7 right top bottom
     */
    positionReference: function (elem) {
        var ref = 0
        var left = elem.acss("left")
        var right = elem.acss("right")
        var top = elem.acss("top")
        var bottom = elem.acss("bottom")
        if (left == "auto") {
            if (right != "auto") {
                ref = 1
            }
        } else {
            if (right != "auto") {
                ref = 2
            }
        }
        if (top == "auto") {
            if (bottom != "auto") {
                ref += 3
            }
        } else {
            if (bottom != "auto") {
                ref += 6
            }
        }
        if (elem.css("position") == "static") {
            if (ref > 5) {
                ref -= 6
            } else if ((ref + 1) % 3 == 0) {
                ref -= 2
            }
        }
        return ref
    },
    omit: function (map, props, excludeFunctions) {
        var nmap = {}
        for (var k in map) {
            if (!props.contains(k) && (!excludeFunctions || !$.isFunction(nmap[k]))) {
                nmap[k] = map[k]
            }
        }
        return nmap
    },
    onReady: function (obj, prop, callback, maxAttempt) {
        if (typeof maxAttempt == "undefined") {
            maxAttempt = 10
        }
        if (maxAttempt > 0) {
            if (typeof obj[prop] == "undefined") {
                if ($.isPlainObject(callback) && callback.not) {
                    callback = $.extend({}, callback)
                    callback.not.call(obj)
                    callback.not = undefined
                }
                setTimeout(function () {
                    bm.onReady(obj, prop, callback, --maxAttempt)
                }, 2000)
            } else {
                ($.isPlainObject(callback) ? callback.ready : callback).call(obj[prop])
            }
        } else {
            if ($.isPlainObject(callback) && callback.fail) {
                callback.fail.call(obj)
            }
        }
    },
    path: function (path) {
        var parts = path.split(/[\\\/]/)
        var protocol
        if (!parts.length) {
            return {}
        }
        if (parts[0].endsWith(":")) {
            protocol = parts[0].substring(0, parts[0].length - 1)
        }
        var namePart = parts[parts.length - 1]
        var queryIndex = namePart.indexOf("?")
        var queryPart
        if (queryIndex > -1) {
            queryPart = namePart.substring(queryIndex + 1)
            namePart = namePart.substring(0, queryIndex)
        }
        var fragmentPart = queryPart || namePart
        var fragmentIndex = fragmentPart.indexOf("#")
        if (fragmentIndex > -1 && queryPart) {
            queryPart = fragmentPart.substring(0, fragmentIndex)
        }
        if (fragmentIndex > -1) {
            fragmentPart = fragmentPart.substring(fragmentIndex + 1)
        } else {
            fragmentPart = null
        }
        var dotIndex = namePart.lastIndexOf(".")
        var ext = dotIndex > -1 ? namePart.substring(dotIndex + 1) : null
        var name = dotIndex > -1 ? namePart.substring(0, dotIndex) : namePart
        var query = queryPart ? bm.query(queryPart) : {}
        var path_obj
        var host_exists = protocol || (parts.length > 2 && parts[0] == "" && parts[1] == "")
        return path_obj = {
            host: host_exists ? parts[2] : null,
            protocol: protocol,
            full: function () {
                var url = path_obj.protocol ? path_obj.protocol + "://" : ""
                url = url + (path_obj.host ? (url ? "" : "//") + path_obj.host + "/" : "")
                var joinableParts = parts.slice(host_exists ? 3 : 0, parts.length - 1)
                url = url + (joinableParts.length ? (joinableParts.join("/") + "/") : "") + path_obj.file_name()
                var query = $.param(path_obj.query)
                return url + (query ? "?" + query : "") + (path_obj.fragmentPart ? "#" + path_obj.fragmentPart : "")
            },
            name: name,
            file_name: function () {
                return this.name + (this.ext ? "." + this.ext : "")
            },
            ext: ext,
            query: query,
            fragment: fragmentPart
        }
    },
    pick: function (map, props) {
        var nmap = {}
        for (var k in map) {
            if (props.contains(k)) {
                nmap[k] = map[k]
            }
        }
        return nmap
    },
    prop: function (obj, name, value) {
        var ids = name.split(/\./)
        var isSet = arguments.length == 3
        for (var g = 0; g < ids.length - 1; g++) {
            var nProp = "" + ids[g]
            if (!(nProp in obj)) {
                if (isSet) {
                    obj[nProp] = {}
                } else {
                    return
                }
            }
            obj = obj[nProp]
            if (typeof obj != "object") {
                return
            }
        }
        if (value == undefined) {
            return obj[ids[ids.length - 1]]
        } else {
            obj[ids[ids.length - 1]] = value
        }
    },
    query: function (query) {
        var returnMap = {}
        query = decodeURIComponent(query)
        query = query.split("&")
        $(query).each(function (i) {
            var nameValue = query[i].split("=")
            nameValue[1] = nameValue[1].replace(/\+/g, " ")
            if (returnMap[nameValue[0]] == undefined) {
                returnMap[nameValue[0]] = nameValue[1]
            } else {
                if ($.isArray(returnMap[nameValue[0]])) {
                    returnMap[nameValue[0]].push(nameValue[1])
                } else {
                    returnMap[nameValue[0]] = [returnMap[nameValue[0]], nameValue[1]]
                }
            }
        })
        return returnMap
    },
    queryParams: function (name) {
        var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href)
        if (results == null) {
            return null
        }
        else {
            return results[1] || 0
        }
    },
    removeAll: function (target, object) {
        var flag
        for (var key in target) {
            flag = false
            for (var objectKey in object) {
                if (key == objectKey) {
                    flag = true
                    break
                }

            }
            if (flag) {
                delete target[key]
            }
        }
    },
    serializeMix: function (a, b) {
        var k = {}
        $.each(a, function (i, v) {
            if (typeof b[i] == "undefined") {
                k[i] = v
            } else {
                if ($.isArray(v)) {
                    k[i] = v.concat(b[i])
                } else if ($.isArray(b[i])) {
                    k[i] = b[i].concat(v)
                } else {
                    k[i] = [v, b[i]]
                }
            }
        })
        $.each(b, function (i, v) {
            if (typeof k[i] == "undefined") {
                k[i] = v
            }
        })
        return k
    },
    saveSiteConfig: function (configs, success, error) {
        var data = {type: []}
        $.each(configs, function (type, config) {
            data.type.push(type)
            $.each(config, function (key, value) {
                data[type + "." + key] = value
            })
        })
        bm.ajax({
            url: app.baseUrl + "setting/saveConfigurations",
            data: data,
            show_response_status: false,
            success: success,
            error: error
        })
    },
    synchronized: function (synchronizer, type) {
        if (!type) {
            type = "skip-if-loaded"
        }
        if (type == "queue-if-loaded") {
            var manager = new QueueManager(1, function (_arg) {
                synchronizer.apply(window, _arg)
            }, true)
            return function () {
                var _arg = arguments
                manager.push(_arg)
            }
        } else if (type == "skip-if-loaded") {
            var processing = false
            return function () {
                if (processing) {
                    return
                }
                var _arg = arguments
                processing = true
                setTimeout(function () {
                    synchronizer.apply(window, _arg)
                    processing = false
                }, 1)
            }
        }
        return function () {
        }
    },
    template: function (template, args) {
        if (!args) {
            return template
        }
        var l = args.length;
        for (var f = 0; f < l; f++) {
            template = template.replaceAll("{" + f + "}", args[f])
        }
        return template
    },
    /**
     * 'this' should refer some sort of dom dependent object (element should have dom)
     */
    trigger: function (dom, base, name, args) {
        if (base instanceof $) {
            return bm.trigger.call(dom, base, name, args, arguments[4]);
        }
        var returnValue;
        if (!$.isArray(args)) {
            args = [args];
        }
        if (dom.isString) {
            args = name
            name = base
            base = dom
            dom = this.elm
        }
        if ($.isFunction(this["on" + name.capitalize()])) {
            returnValue = this["on" + name.capitalize()].apply(this, args);
        }
        if (returnValue === false) {
            return false;
        }
        if (this.options) {
            if (this.options.events) {
                if ($.isFunction(this.options.events[name])) {
                    returnValue = this.options.events[name].apply(this, args);
                }
            } else if ($.isFunction(this.options[name])) {
                returnValue = this.options[name].apply(this, args);
            }
            if (returnValue === false) {
                return false;
            }
        } else if (this.events) {
            if ($.isFunction(this.events[name])) {
                returnValue = this.events[name].apply(this, args);
            }
            if (returnValue === false) {
                return false;
            }
        }
        return dom.trigger(base + ":" + name, args);
    },
    editInlineText: function (container) {
        var deferred = $.Deferred();
        var text = container.text().trim();
        var parent = container.parents(".editable").addClass("active");
        container.html("<input type='text' class='inline-edit-input'><span class='tool-icon reset' tabindex='0'></span>");
        container.find(".reset").on("mousedown", function () {
            container.text(text);
            deferred.reject();
            parent.removeClass("active");
        })
        var input = container.find("input").on("blur keydown.key_return", function () {
            var input = this;
            var validation = container.attr("data-editable-validation");
            var doLater = function () {
                var url = container.attr("data-editable-submit-url");
                if (!url) {
                    container.text(input.value).trigger("inlinechange");
                    deferred.resolve(input.value);
                    parent.removeClass("active");
                    return;
                }
                bm.ajax({
                    url: url,
                    data: {value: input.value},
                    show_success_status: false
                }).always(function () {
                    parent.removeClass("active");
                }).done(function () {
                    container.text(input.value).trigger("inlinechange");
                    deferred.resolve(input.value);
                }).fail(function () {
                    container.text(text);
                    deferred.reject();
                });
            }
            if (validation) {
                var error = ValidationField.validateAs(this.jqObject, validation, container.config("editable-validation"))
                if (error != null) {
                    if (error.promise) {
                        error.done(function (_error) {
                            if (_error != null) {
                                bm.notify(bm.template(_error.msg_template, _error.msg_params), "alert")
                            } else {
                                doLater()
                            }
                        })
                    } else {
                        if (container.data("showError") == "after") {
                            var errorBlock = ("<div class='errorlist after'><div class='message-block error-message'>" + bm.template(error.msg_template, error.msg_params) + "</div></div>").jqObject;
                            if (container.next(".message-block.error-message").length) {
                                container.next(".message-block.error-message").replaceWith(errorBlock);
                            } else {
                                container.after(errorBlock);
                            }

                            function removeError() {
                                errorBlock.remove();
                            }

                            deferred.done(function () {
                                removeError();
                            }).fail(function () {
                                removeError();
                            })
                        } else {
                            bm.notify(bm.template(error.msg_template, error.msg_params), "alert")
                        }
                    }
                } else {
                    doLater()
                }
            } else {
                doLater()
            }
        }).val(text);
        input.nat.focus()
        var attrs = container.nat.attributes;
        for (var f = 0; f < attrs.length; f++) {
            var attr = attrs[f].name
            var v = attrs[f].value
            if (attr.startsWith("data-editable-validation")) {
                input.attr("data" + attr.substring(13), v)
            }
        }
        return deferred;
    },
    editInlinePrice: function (container) {
        var deferred = $.Deferred();
        var text = container.text().trim();
        container.html("<input type='text' class='inline-edit-input'><span class='tool-icon reset' tabindex='0'></span>");
        container.find(".reset").on("mousedown", function () {
            container.text($text);
            deferred.reject();
        })
        var input = container.find("input").on("blur keydown.key_return", function () {
            var input = this
            var validation = container.attr("data-editable-validation")
            var doLater = function () {
                var url = container.attr("data-editable-submit-url");
                bm.ajax({
                    url: url,
                    data: {value: input.value},
                    show_success_status: false
                }).done(function () {
                    container.text('$' + input.value).trigger("inlinechange");
                    deferred.resolve(input.value);
                }).fail(function () {
                    container.text(text);
                    deferred.reject();
                });
            }
            if (validation) {
                var error = ValidationField.validateAs(this.jqObject, validation, container.config("editable-validation"))
                if (error != null) {
                    if (error.promise) {
                        error.done(function (_error) {
                            if (_error != null) {
                                bm.notify(bm.template(_error.msg_template, _error.msg_params), "alert")
                            } else {
                                doLater()
                            }
                        })
                    } else {
                        bm.notify(bm.template(error.msg_template, error.msg_params), "alert")
                    }
                } else {
                    doLater()
                }
            } else {
                doLater()
            }
        }).val(text.substring(1));
        input.nat.focus()
        var attrs = container.nat.attributes;
        for (var f = 0; f < attrs.length; f++) {
            var attr = attrs[f].name
            var v = attrs[f].value
            if (attr.startsWith("data-editable-validation")) {
                input.attr("data" + attr.substring(13), v)
            }
        }
        return deferred;
    },
    copy: function (textToCopy, options) {
        var $temp = $("<input>");
        $("body").append($temp);
        $temp.val(textToCopy).select();
        var status = document.execCommand("copy");
        $temp.remove();
        return status
    }
})

bm.expando = "BM" + bm.getUUID()