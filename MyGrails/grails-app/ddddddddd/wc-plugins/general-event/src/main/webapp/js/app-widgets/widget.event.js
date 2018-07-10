app.widget.generalEvent = function(config) {
    app.widget.generalEvent._super.constructor.apply(this, arguments);
}

var _e = app.widget.generalEvent.inherit(app.widget.base);

_e.init = function() {
    var _self = this;
    app.widget.generalEvent._super.init.call(this);
    var container = _self.content;

    var selectionType = container.find('.selection-type').val();
    var leftPanelUrl = app.baseUrl + 'generalEventAdmin/loadEventsForSelection';
    var selectorPanel = bm.twoSideSelection(container.find('.selection-panel'), 10, selectionType, leftPanelUrl, {view: false, edit: false, "column-sort": false}, [selectionType]);
    container.find(".selection-type").on("change", function() {
        var val = $(this).val();
        if(val == "event") {
            selectionType = 'event';
            leftPanelUrl = app.baseUrl + 'generalEventAdmin/loadEventsForSelection';
            bm.unmask(container.find('.left-right-selector-panel'));
            selectorPanel.setUrl(leftPanelUrl, selectionType, {view: false, edit: false, "column-sort": false}, [selectionType])
            container.find('.actions-column .remove-all').trigger('click');
        } else {
            bm.mask(container.find('.left-right-selector-panel'), '<div></div>');
        }
    });
};

_e.beforeSubmit = function(form) {
    this.config = form.serializeObject();
};

_e.afterContentChange = function(widget, cache) {
    var _app = widget.editor.iframeWindow.app;
    cache = JSON.parse(cache)
    var wconfig = JSON.parse(cache.params)
    widget.elm.find("paginator").paginator();
    if(widget.elm.find('.calendar').length) {
        bm.onReady(_app, "initCalendarWidgetForGeneralEvent", {
            ready: function() {
                var config = {};
                config.dateFormat = widget.elm.find('.dateFormat').val().replaceAll('E', 'd');
                config.timeFormat = widget.elm.find('.timeFormat').val().replace('aaa', 'TT');
                config.dateTimeFormat = config.dateFormat + ' ' + config.timeFormat;
                config.isBasicCalendar = wconfig.displayType.indexOf('basic-') == 0;
                config.showPrice = wconfig.showPrice;
                config.showRequestInfo = wconfig.showRequestInfo;
                config.showBookNow = wconfig.showBookNow;
                config.labelForBookNow = wconfig.labelForBookNow;
                config.detailsOn = wconfig.detailsOn;
                config.dayChar = wconfig.dayChar;
                var container = widget.elm.find('.calendar');
                var dayKeys = ['sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday'];
                var dayKeysSuffix = config.dayChar == -1 ? '' : (config.dayChar == 1 ? '.single' : '.short');
                var dayNames = [];
                for(var i = 0; i < dayKeys.length; i++) {
                    dayNames.push($.i18n.prop(dayKeys[i] + dayKeysSuffix))
                }
                var eventDataList = JSON.parse(widget.elm.find('.eventDataList').val());
                container.html('');
                _app.initCalendarWidgetForGeneralEvent(container, config, eventDataList);
            },
            not: function() {
                var head = widget.editor.iframeWindow.$("head");
                head.append("<script src='" + app.systemResourceUrl + "plugins/general-event/fullcalendar/fullcalendar.min.js' type='text/javascript'></script>");
                head.append("<script src='" + app.systemResourceUrl + "plugins/general-event/js/widget/calendar-widget.js' type='text/javascript'></script>");
                head.append("<script src='" + app.systemResourceUrl + "plugins/general-event/js/widget/event-widget.js' type='text/javascript'></script>");
                head.append('<link rel="stylesheet" type="text/css" href="' + app.systemResourceUrl + 'plugins/general-event/fullcalendar/fullcalendar.css">');
            }
        });
    }
};
