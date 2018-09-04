app.tabs.messageSource = function(config) {
    this.text = $.i18n.prop("message.source");
    this.tip = $.i18n.prop("manage.message.source");
    this.ui_class = "message-source";
    this.ajax_url = app.baseUrl + "messageSource/loadAppView";
    app.tabs.messageSource._super.constructor.apply(this, arguments);
}

app.ribbons.administration.push({
    text: $.i18n.prop("message.source"),
    processor: app.tabs.messageSource,
    ui_class: "message-source"
});

app.tabs.messageSource.inherit(app.SingleTableTab);
var _ms = app.tabs.messageSource.prototype;

_ms.errorHighlight = function (item, message) {
    bm.notify(message, "alert")
    item.addClass("error-highlight");
    setTimeout(function() {
        item.removeClass("error-highlight")
    }, 3000);
}

_ms.attachTypeTableEvents = function(lastRow) {
    var _self = this;
    lastRow.find(".add-row").on("click", function() {
        var isValid = lastRow.valid({
            show_error: false
        });
        if(!isValid) {
            return;
        }
        var key =  lastRow.find("[name=key]").val(), message = lastRow.find("[name=message]").val();
        if(!key) {
            _self.errorHighlight(lastRow.find("[name=key]"), $.i18n.prop("this.field.is.required"))
            return;
        }
        if(!message) {
            _self.errorHighlight(lastRow.find("[name=message]"), $.i18n.prop("this.field.is.required"))
            return;
        }
        _self.save(null, key, message)
    });

    lastRow.on("invalid", function(evnent, obj) {
        _self.errorHighlight(obj.validator.elm, $.i18n.prop(obj.msg_template, obj.msg_params))
    });

    lastRow.on("keypress", function(e) {
        if (e.which == 13){
            lastRow.find(".add-row").trigger("click")
        }
    });
};

_ms.attachEvents = function() {
    var _self = this;
    this.attachTypeTableEvents(this.body.find("tr.last-row"));
    this.body.find("span.tool-icon.remove").on("click", function(){
        var message = $(this).closest("tr").find(".key").text().trim();
        _self.remove($(this).attr("entity-id"), message);
    });
}

_ms.afterCellEdit = function(td, value, oldValue) {
    var tr = td.parent();
    var id = tr.attr("entity-id"), key = tr.find("td.key .value").text(), message = tr.find("td.message .value").text();
    if(!value) {
        this.errorHighlight(td.find("input"), $.i18n.prop("this.field.is.required"));
        return false
    } else if(value.length > 200 ) {
        this.errorHighlight(td.find("input"), $.i18n.prop("enter.no.more.characters", [200]));
        return false
    }
    this.save(id, key, message, function() {
        td.find(".value").text(oldValue);
    });
};

(function() {
    _ms.init = function() {
        var _self = this
        app.tabs.messageSource._super.init.call(this);
        _self.attachEvents();
        _self.localeSelector = _self.body.find("select[name=locale]");
        _self.localeSelector.on("change", function(){
            _self.reload();
        })
    }

})();

_ms.save = function(id, key, message, errorCallBack)  {
    var _self = this;
    var params = {
        id: id,
        key: key,
        message: message,
        locale: _self.localeSelector.val()
    }
    bm.ajax({
        url: app.baseUrl + "messageSource/save",
        data: params,
        success: function(resp) {
            _self.reload()
        },
        error: function() {
            if(errorCallBack) {
                errorCallBack();
            }
        }
    })
};

_ms.remove = function(id, message) {
    var _self = this;
    bm.confirm($.i18n.prop("do.you.want.to.remove.message", [message]), function() {
        bm.ajax({
            url: app.baseUrl + "messageSource/remove",
            data: {id: id},
            success: function(resp){
                _self.reload();
            }
        })
    }, function() {

    });
}

_ms.afterTableReload = function() {
    this.attachEvents();
}

_ms.beforeReloadRequest = function(params) {
    app.tabs.messageSource._super.beforeReloadRequest.call(this, params);
    params.locale = this.localeSelector.val();
}