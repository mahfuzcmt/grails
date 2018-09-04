bm.onReady(app.tabs, "liveChat", function() {
    app.tabs.liveChat.tabInitFunctions.profile = function(panel) {
        var tab = new app.tabs.liveChat.profile(this, panel);
        tab.init();
    };

    app.tabs.liveChat.profile = function(parentTab, panel) {
        this.parentTab = parentTab;
        this.body = panel;
        this.tool = panel.tool;
    };


    (function() {
        var _pt = app.tabs.liveChat.profile.prototype;
        _pt.init = function() {
            var _self = this;
            _self.attachEvents();
        };

        _pt.attachEvents = function() {
            var _self = this;
            this.body.find(".update-chat-operator-profile").on("click", function() {
                _self.updateProfile();
            });
        };

        _pt.updateProfile = function() {
            var _self = this;
            var profileForm =  this.body.find(".operator-profile-form");
            profileForm.form({
                ajax: {
                    success: function() {
                        _self.body.reload()
                    }
                }
            })
        };

    })();
});
