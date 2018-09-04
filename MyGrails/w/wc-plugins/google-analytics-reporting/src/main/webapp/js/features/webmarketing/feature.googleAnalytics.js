app.tabs.googleAnalytics = function() {
    this.constructor_args = arguments;
    this.text = $.i18n.prop("manage.google.analytics");
    this.tip = $.i18n.prop("google.analytics");
    this.ui_class = "googleAnalytics";
    app.tabs.googleAnalytics._super.constructor.apply(this, arguments);
    this.ajax_url = app.baseUrl + "googleAnalytics/loadAppView";
}

app.tabs.googleAnalytics.inherit(app.MultiTab);
var _ga = app.tabs.googleAnalytics.prototype;
(function () {
    function attachEvents() {
        var _self = this;
        var multiTab = _self.body.find(".bmui-tab")
        _self.body.find(".reload").on("click", function() {
            var activeIndex = multiTab.tabify("option", "active");
            multiTab.tabify("reload", activeIndex)
        })

        _self.body.find(".bmui-tab-header").on("click", function() {
            var activeIndex = multiTab.tabify("option", "active");
            multiTab.tabify("reload", activeIndex)
        })

        _self.body.find(".toolbar .from-date, .toolbar .to-date").on("change", function() {
            var to = _self.body.find(".toolbar .to-date").val();
            var from = _self.body.find(".toolbar .from-date").val();
            if(to && from) {
                app.tabs.googleAnalytics.dateTo = to
                app.tabs.googleAnalytics.dateFrom = from
                var activeIndex = multiTab.tabify("option", "active");
                multiTab.tabify("reload", activeIndex)
            } else {
                if(app.tabs.googleAnalytics.dateFrom != null) {
                    app.tabs.googleAnalytics.dateTo = null
                    app.tabs.googleAnalytics.dateFrom = null
                    var activeIndex = multiTab.tabify("option", "active");
                    multiTab.tabify("reload", activeIndex)
                }
            }
        })

        _self.body.find(".datefield-between").each(function() {
            var group = $(this);
            group.find('.datefield-from').date({
                direction: false,
                show_select_today: false,
                lang_clear_date: $.i18n.prop("clear"),
                pair: group.find(".datefield-to").date({
                    direction: [true, new Date().toString("yyyy-MM-dd")],
                    show_select_today: false,
                    lang_clear_date: $.i18n.prop("clear")
                })
            });
        })

        _self.body.find(".toolbar .analytics-type").on("click",function() {
            app.tabs.googleAnalytics.analyticsType = $(this).attr("value");
            $(this).attr("disabled",true);
            $(this).siblings().attr("disabled",false)
            var activeIndex = multiTab.tabify("option", "active");
            multiTab.tabify("reload", activeIndex)
        })

        _self.body.find(".toolbar .chart-type").change(function() {
            app.tabs.googleAnalytics.chartType = this.value
            _self.body.find(".chart-block").chart("rerender", this.value)
        })

        multiTab.on("tabsbeforeload", function(ev, evObj) {
            var url = evObj.ajaxSettings.url
            url = bm.path(url)
            url.query.chartType = app.tabs.googleAnalytics.chartType
            url.query.dateForm = app.tabs.googleAnalytics.dateFrom
            url.query.dateTo = app.tabs.googleAnalytics.dateTo
            url.query.analyticsType= app.tabs.googleAnalytics.analyticsType
            url.query.searchType= app.tabs.googleAnalytics.searchType
            evObj.ajaxSettings.url = url.full()
        })
    }

    _ga.init = function () {
        app.tabs.googleAnalytics._super.init.call(this)
        attachEvents.call(this)
    }
})();

_ga.onActionMenuClick = function(action) {
    ComponentManager.openTab(ComponentManager.getRibbonData(app.ribbons.administration, "settings"), {active: "googleAnalytics"})
}

app.tabs.googleAnalytics.chartType = 'line'
app.tabs.googleAnalytics.analyticsType = 'daily'
app.tabs.googleAnalytics.searchType='all'

app.ribbons.report.push(app.tabs.googleAnalytics.ribbon_data = {
    text: $.i18n.prop("google.analytics"),
    ui_class: "google-analytics",
    processor: app.tabs.googleAnalytics,
    license: "allow_google_analytics_reporting_feature",
    ecommerce: true
});

_ga.action_menu_entries = [
    {
        text: $.i18n.prop("google.analytics.configuration"),
        ui_class: "google-analytics-config config",
        action: "google-analytics-config"
    }
];

_ga.onContentLoad = function(data) {
    var panel = data.panel
    panel.find(".analytics-config-link").on("click", function() {
        ComponentManager.openTab(ComponentManager.getRibbonData(app.ribbons.administration, "settings"), {active: "googleAnalytics"})
    })
    var _self = this
    this.on_global("google-analytics-configurations-updated", function() {
        _self.body.tabify("reload", data.index)
    })
    if(this[data.index + "TabInit"]) {
        this[data.index + "TabInit"](data)
    }
}

_ga.locationTabInit = function(data) {
    var _self = this;
    var container =  data.panel.find(".analytics-report-location")
    container.find(".toolbar-button-group .analytics-type").on("click",function() {
        var data = {searchType: $(this).attr("value")};
        $(this).attr("disabled",true);
        $(this).siblings().attr("disabled",false)
        bm.ajax({
            controller: "googleAnalytics",
            action: "loadDataForLocation",
            data: data,
            dataType: "html",
            success: function(html) {
                _self.body.find(".tabular-data-location").replaceWith($(html));
            }

        })
    })
}

_ga.keywordTabInit = function(data) {
    var _self = this;
    var container =  data.panel.find(".analytics-report-keyword")
    container.find(".toolbar-button-group .analytics-type-key").on("click",function() {
        var data = {searchType: $(this).attr("value")};
        $(this).attr("disabled",true);
        $(this).siblings().attr("disabled",false)
        bm.ajax({
            controller: "googleAnalytics",
            action: "loadDataForLandingKeyword",
            data: data,
            dataType: "html",
            success: function(html) {
                _self.body.find(".tabular-data-landing-keyword").replaceWith($(html));
            }
        })
    })

    container.find(".toolbar-button-group .analytics-type").on("click",function() {
        var data = {searchType: $(this).attr("value")};
        $(this).attr("disabled",true);
        $(this).siblings().attr("disabled",false)
        bm.ajax({
            controller: "googleAnalytics",
            action: "loadDataForKeyword",
            data: data,
            dataType: "html",
            success: function(html) {
                _self.body.find(".tabular-data-keyword").replaceWith($(html));
            }
        })
    })
};

_ga.reload = function() {}