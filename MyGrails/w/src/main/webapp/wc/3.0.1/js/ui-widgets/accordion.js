var ACCORDION_DEFAULTS = {multiple: false, all_close: false}

var Accordion = function(elem, config) {
    this.container = elem;
    elem.addClass("accordion-panel");
    this.options = $.extend({}, ACCORDION_DEFAULTS, config);
    var _self = this;
    elem.find(".label-bar").click(function () {
        var bar = $(this);
        var ev = $.Event("beforeexpand")
        _self.container.trigger(ev, [_self.container.find(".accordion-item.expanded"), bar.next()])
        if(ev.isDefaultPrevented()) {
            return;
        }
        if(_self.options.all_close) {
            if (bar.hasClass("expanded")) {
                _self.collapse(bar);
                return;
            }
        }
        _self.expand(bar);
    }).each(function() {
        _self.collapse($(this), true)
    })
    $.each(config.expanded || [], function() {
        elem.find(".label-bar").eq(+this - 1).trigger("click");
    });
    if(!this.options.all_close && !elem.find(".label-bar.expanded").length) {
        _self.expand(elem.find(".label-bar:first"))
    }
};

var childAccordion = function(elem, config) {
    this.container = elem;
    elem.addClass("accordion-panel");
    this.options = $.extend(true,{}, ACCORDION_DEFAULTS, config);
    var _self = this;
    elem.find(".child.label-bar").click(function () {
        var bar = $(this);
        var ev = $.Event("beforeexpand")
        _self.container.trigger(ev, [_self.container.find(".accordion-item.expanded"), bar.next()])
        if(ev.isDefaultPrevented()) {
            return;
        }
        if(_self.options.all_close) {
            if (bar.hasClass("expanded")) {
                _self.collapse(bar);
                return;
            }
        }
        _self.expand(bar);
    }).each(function() {
        _self.collapse($(this), true)
    })
    $.each(config.expanded || [], function() {
        elem.find(".child.label-bar").eq(+this - 1).trigger("click");
    });
    if(!this.options.all_close && !elem.find(".child.label-bar.expanded").length) {
        _self.expand(elem.find(".child.label-bar:first"))
    }
};

(function() {
    $.extend(Accordion.prototype, {
        collapse: function(bar, force) {
            if (bar.hasClass("expanded") && !force) {
                if (!this.options.all_close && this.container.find(".label-bar.expanded").length == 1) {
                    return;
                }
            }
            bar.removeClass("expanded").addClass("collapsed");
            bar.next().removeClass("expanded").addClass("collapsed");
            bm.trigger(this, this.container, "accordion", "collapse", [this.container.find(".accordion-item.expanded"), bar.next()])
        },
        expand: function(bar, options) {
            if (!this.options.multiple) {
                var expanded = this.container.find(".label-bar.expanded")
                if(expanded.length) {
                    this.collapse(expanded, true)
                }
            }
            bar = this.bar(bar)
            bar.removeClass("collapsed").addClass("expanded");
            bar.next().removeClass("collapsed").addClass("expanded");
            if(bar.attr("item-url") && !bar.data("load")) {
                this.container.trigger("start-load", [bar.next()])
                var _self = this;
                var url = bar.attr("item-url");
                bm.ajax({
                    url: app.baseUrl + url,
                    dataType: "html",
                    data: options ? options.params : {},
                    success: function(resp) {
                        bar.next().html(resp)
                        bar.data("load", "done")
                        if(options && options.success instanceof Function) {
                            options.success(resp)
                        }
                        _self.container.trigger("load", [bar.next()])
                        bm.trigger(_self, _self.container, "accordion", "load", [_self.container.find(".accordion-item.expanded"), bar.next()])
                    },
                    error: function(xhr, status, resp) {
                        if(options && options.error instanceof Function) {
                            options.error(xhr, status, resp)
                        }
                        _self.container.trigger("error", [bar.next(), xhr, status, resp])
                    }
                })
            }
            bm.trigger(this, this.container, "accordion", "expand", [this.container.find(".accordion-item.expanded"), bar.next()])
        },
        current: function() {
            return this.container.find(".accordion-item.expanded")
        },
        item: function(block) {
            var bar = block.closest(".label-bar", this.container)
            if(bar.length) {
                return bar.next()
            }
            return block.closest(".accordion-item", this.container)
        },
        next: function(step) {
            if(step.is(".accordion-item")) {
                return step.next().next()
            } else {
                return this.current().next().next()
            }
        },
        bar: function(bar) {
            if(bar instanceof Number || typeof bar == "number") {
                bar = this.container.find(".label-bar:eq('" + bar + "')")
            } else if(bar instanceof String || typeof bar == "string") {
                bar = this.container.find(".label-bar." + bar)
            }
            if(bar.is(".accordion-item")) {
                bar = bar.prev()
            }
            return bar;
        },
        clearState: function(block) {
            var item = this.item(block);
            if(item) {
                item.html("")
                item.prev().removeData("load")
            }
        },
        reload: function(bar, options) {
            bar = this.bar(bar);
            this.clearState(bar);
            this.expand(bar, options)
        }
    })


    $.extend(childAccordion.prototype, {
        collapse: function(bar, force) {
            if (bar.hasClass("expanded") && !force) {
                if (!this.options.all_close && this.container.find(".child.label-bar.expanded").length == 1) {
                    return;
                }
            }
            bar.removeClass("expanded").addClass("collapsed");
            bar.next().removeClass("expanded").addClass("collapsed");
            bm.trigger(this, this.container, "childAccordion", "collapse", [this.container.find(".accordion-item.expanded"), bar.next()])
        },
        expand: function(bar, options) {
            if (!this.options.multiple) {
                var expanded = this.container.find(".child.label-bar.expanded")
                if(expanded.length) {
                    this.collapse(expanded, true)
                }
            }
            bar = this.bar(bar)
            bar.removeClass("collapsed").addClass("expanded");
            bar.next().removeClass("collapsed").addClass("expanded");
            if(bar.attr("item-url") && !bar.data("load")) {
                this.container.trigger("start-load", [bar.next()])
                var _self = this;
                var url = bar.attr("item-url");
                bm.ajax({
                    url: app.baseUrl + url,
                    dataType: "html",
                    data: options ? options.params : {},
                    success: function(resp) {
                        bar.next().html(resp)
                        bar.data("load", "done")
                        if(options && options.success instanceof Function) {
                            options.success(resp)
                        }
                        _self.container.trigger("load", [bar.next()])
                        bm.trigger(_self, _self.container, "childAccordion", "load", [_self.container.find(".child.label-bar.expanded"), bar.next()])
                    },
                    error: function(xhr, status, resp) {
                        if(options && options.error instanceof Function) {
                            options.error(xhr, status, resp)
                        }
                        _self.container.trigger("error", [bar.next(), xhr, status, resp])
                    }
                })
            }
            bm.trigger(this, this.container, "childAccordion", "expand", [this.container.find(".accordion-item.expanded"), bar.next()])
        },
        current: function() {
            return this.container.find(".accordion-item.expanded")
        },
        item: function(block) {
            var bar = block.closest(".child.label-bar", this.container)
            if(bar.length) {
                return bar.next()
            }
            return block.closest(".accordion-item", this.container)
        },
        next: function(step) {
            if(step.is(".accordion-item")) {
                return step.next().next()
            } else {
                return this.current().next().next()
            }
        },
        bar: function(bar) {
            if(bar instanceof Number || typeof bar == "number") {
                bar = this.container.find(".child.label-bar:eq('" + bar + "')")
            } else if(bar instanceof String || typeof bar == "string") {
                bar = this.container.find(".child.label-bar." + bar)
            }
            if(bar.is(".accordion-item")) {
                bar = bar.prev()
            }
            return bar;
        },
        clearState: function(block) {
            var item = this.item(block);
            if(item) {
                item.html("")
                item.prev().removeData("load")
            }
        },
        reload: function(bar, options) {
            bar = this.bar(bar);
            this.clearState(bar);
            this.expand(bar, options)
        }
    });


    bm.jquerify("accordion", Accordion);
    bm.jquerify("childAccordion", childAccordion);
})()