app.tabs.customerBulkEditor = function () {
    app.tabs.customerBulkEditor._super.constructor.apply(this, arguments);
    this.text = $.i18n.prop("customer.bulk.edit", [app.ecommerce.bool()?"Customer":"Member"]);
    this.tip = $.i18n.prop("customer.bulk.edit", [app.ecommerce.bool()?"Customer":"Member"]);
    this.ui_class = "customer-bulk-editor edit";
    this.ids = JSON.stringify(this.customerIds);
    this.ajax_url = app.baseUrl + "customerAdmin/loadCustomerBulkEditor?ids=" + this.ids;
    this.tab_objs = {}
};

var _be = app.tabs.customerBulkEditor.inherit(app.MultiTab);

_be.init = function () {
    var _self = this;
    var tabWrapper = this.body.find(".bmui-tab");
    app.tabs.customerBulkEditor._super.init.apply(this, arguments)
}

_be.changeTabUrl = function (index, url) {
    this.body.find(".bmui-tab").tabify("reload", index, url)
}

_be.onContentLoad = function (data) {
    var _self = this;
    var index = data.index;
    var tab = app.tabs.customerBulkEditor[index.capitalize()]
    if (tab) {
        var subTab = new tab(data.panel, this);
        _self.tab_objs[index] = subTab;
        subTab.ids = _self.ids;
        subTab.init();
        var form = data.panel.find(".bulk-editor-form");
        form.find(".submit-button").on("click", function () {
            var dataMap = form.serializeObject();
            bm.ajax({
                url: form.attr("action"),
                data: dataMap,
                success: function (resp) {
                    app.global_event.trigger("customer-update");
                    _self.panels[index].reload();
                    _self.clearDirty();
                },
                error: function (a, b, resp) {
                    bm.notify(resp.message, "error");
                }
            });
        })
    }
}

_be.close = function(arg) {
    $("body").find(".bulkedit-parent-td-proxy").remove();
    app.tabs.customerBulkEditor._super.close.call(this);
}

_be.save = function (callback) {
    var _self = this;
    var dirtyPanels = {};
    $.each(_self.panels, function(key, value) {
        if(value.isDirty) {
            dirtyPanels[key] = value
        }
    })
    var i = 1;
    var count = Object.keys(dirtyPanels).length;
    var last = false;
    if(!count) {
        _self.close(true);
    }
    bm.iterate(dirtyPanels, function (handle, index) {
        index = index.toString();
        var panel = this;
        panel.trigger("before-editor-close", callback);
        var form = panel.find(".bulk-editor-form");
        var dataMap = form.serializeObject();
        bm.ajax({
            url: form.attr("action"),
            data: dataMap,
            success: function () {
                app.global_event.trigger("customer-update", [_self.ids]);
                panel.clearDirty();
                if (!_self.isDirty() && callback) {
                    callback();
                } else {
                    handle.next();
                }
                if(last) {
                    _self.close(true);
                }
            },
            error: function () {
                _self.setActiveTab(index);
                handle.next();
            },
            invalid: function () {
                _self.setActiveTab(index);
                panel.find("form").valid("position");
                handle.next();
            }
        });
        if(i == count) {
            last = true;
        }
        i++;
    });
}

_be.activePanel = function () {
    var active;
    $.each(this.panels, function (i, panel) {
        if (panel.is(":visible")) {
            active = panel;
            return;
        }
    });
    return active;
}

_be.allPanel = function () {
    return this.body.find(".bmui-tab-panel");
}