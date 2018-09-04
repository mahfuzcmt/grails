/**
 * Created by sanjoy on 14/08/2014.
 */
app.widget.currency = function(config) {
    app.widget.html._super.constructor.apply(this, arguments);
};

app.widget.currency.config_width = 595;

var _c = app.widget.currency.inherit(app.widget.base);

_c.init = function() {
    app.widget.currency._super.init.call(this);
    var dom = this.content;

}