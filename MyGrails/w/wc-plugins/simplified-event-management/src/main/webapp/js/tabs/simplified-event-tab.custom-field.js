app.tabs.customField = function(config) {
    this.id = config.id;
    this.eventId = config.eventId;
    this.text = $.i18n.prop('event.custom.field');
    this.tip = $.i18n.prop('manage.event.custom.field');
    this.ui_class = "event-custom-field edit-tab";
    this.ajax_url = app.baseUrl + "simplifiedEventAdmin/loadCustomFieldAppView?eventId=" + this.eventId;
    app.tabs.customField._super.constructor.apply(this, arguments);
}

var _cf = app.tabs.customField.inherit(app.SingleTableTab);

_cf.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit",
        action: "edit"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "remove"
    }
];

_cf.onActionClick = function(action, data) {
    switch(action) {
        case "edit":
            this.edit(data.id, data.label);
            break;
        case "remove":
            this.deleteCustomField(data.id, data.label);
            break;
    }
};

_cf.action_menu_entries = [
    {
        text: $.i18n.prop("edit.group.label"),
        ui_class: "edit-label",
        action: "edit-label"
    }
];

_cf.onActionMenuClick = function(action) {
    switch(action) {
        case "edit-label":
            this.editFieldLabel();
            break;
    }
};

(function() {
    function attachEvents() {
        var _self = this;
        this.on_global("event-custom-field-updated", function (evt) {
            _self.reload();
        });
        this.on("close", function () {
            app.tabs.customField.tab = null;
        });
        this.body.find(".toolbar .create").on("click", function() {
            _self.createEventCustomField(undefined, "");
        });
    };

    _cf.init = function () {
        var _self = this;
        app.tabs.customField._super.init.call(this);
        app.tabs.customField.tab = this;
        attachEvents.call(this);
    };
})();

_cf.createEventCustomField = _cf.edit = function(id, name) {
    var data = {id: id, eventId: this.eventId},
        title = $.i18n.prop("edit.event.custom.field");
    if(typeof id == "undefined") {
        data = {eventId: this.eventId};
        title = $.i18n.prop("create.event.custom.field");
    }
    this.renderCreatePanel(app.baseUrl + "simplifiedEventAdmin/editCustomField", title, name, data, {
        success: function() {
            if(app.tabs.customField.tab) {
                app.tabs.customField.tab.reload();
            }
            app.global_event.trigger("event-custom-field-updated", [id]);
        }
    });
};

_cf.deleteCustomField = function(id, name) {
    var _self = this
    bm.remove("eventCustomField", $.i18n.prop("event.custom.field"), $.i18n.prop("confirm.delete.event.custom.field", [name]), app.baseUrl + "simplifiedEventAdmin/deleteCustomField", id, {
        is_final: true,
        success: function () {
            _self.reload()
        }
    })
};

_cf.editFieldLabel = function() {
    var title = $.i18n.prop("set.group.title");
    var data = {eventId: this.eventId};
    bm.editPopup(app.baseUrl + "simplifiedEventAdmin/editCustomFieldLabel", title, "", data, {})
}