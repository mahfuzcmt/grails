/**
 * Created by sajedur on 21/07/2014.
 */
app.tabs.visitor_listing = function(config) {
    this.text = $.i18n.prop("visitor.listing");
    this.tip = $.i18n.prop("visitor.listing");
    this.ui_class = "visitor-listing";
    this.ajax_url = app.baseUrl + "visitorListing/loadAppView";
    app.tabs.visitor_listing._super.constructor.apply(this, arguments);
};
app.ribbons.administration.push({
    text: $.i18n.prop("visitor"),
    processor: app.tabs.visitor_listing,
    ui_class: "visitor-listing",
    license: "allow_live_visitor_feature"
});

app.tabs.visitor_listing.inherit(app.SingleTableTab);

var _vl = app.tabs.visitor_listing.prototype;

_vl.autoReload = function(duration) {
    var _self = this;
    if(_self.interval) {
        clearInterval(_self.interval)
    }
    if(!duration) {
        return;
    }
    this.interval = setInterval(function(){
        _self.reload();
    }, duration * 1000)
};

(function() {
    function attachEvent() {
        var _self = this;
        this.body.find("[name=autoReload]").on("change", function(){
            var duration = $(this).val();
            duration = duration == "off" ? false : duration;
            _self.autoReload(duration);
        }).trigger("change");
    }

    _vl.init = function() {
        app.tabs.visitor_listing._super.init.call(this)
        attachEvent.call(this);
    }
})()

_vl.destroy = function() {
    app.tabs.visitor_listing._super.destroy.call(this);
    clearInterval(this.interval)
}