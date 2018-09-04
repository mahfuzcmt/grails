app.widget.liveChat = function(config) {
    app.widget.liveChat._super.constructor.apply(this, arguments);
}

var _lc = app.widget.liveChat.inherit(app.widget.base);

(function(){
    _lc.init = function() {
        app.widget.liveChat._super.init.call(this);

    }
})();