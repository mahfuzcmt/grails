/**
 * Created by sajed on 5/29/2014.
 */
app.tabs.awstats = function () {
    this.text = $.i18n.prop("awstats");
    this.tip = $.i18n.prop("manage.awstats");
    this.ui_class = "awstats";
    this.ui_body_class = "simple-tab";
    this.ajax_url = app.baseUrl + "awstats/loadAppView";
    app.tabs.awstats._super.constructor.apply(this, arguments);
};

app.ribbons.web_marketing.push({
    text: $.i18n.prop("awstats"),
    processor: app.tabs.awstats,
    ui_class: "awstats",
    license: "allow_awstats_feature",
    ecommerce: true
});

app.tabs.awstats.inherit(app.Tab);




