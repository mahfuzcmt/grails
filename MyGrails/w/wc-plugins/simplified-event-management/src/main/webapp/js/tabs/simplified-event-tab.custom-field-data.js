app.tabs.customFieldData = function(config) {
    this.id = config.id;
    this.eventId = config.eventId;
    this.text = $.i18n.prop('event.custom.field.data');
    this.tip = $.i18n.prop('manage.event.custom.field.data');
    this.ui_class = "event-custom-field-data";
    this.ajax_url = app.baseUrl + "simplifiedEventAdmin/loadCustomFieldDataAppView?eventId=" + this.eventId;
    app.tabs.customFieldData._super.constructor.apply(this, arguments);
}

var _cfd = app.tabs.customFieldData.inherit(app.SingleTableTab);