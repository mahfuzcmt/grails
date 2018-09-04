app.tabs.sitemap = {
    sitemap: function () {
        app.tabs.sitemap.sitemap._super.constructor.apply(this, arguments)
        this.text = $.i18n.prop("sitemap")
        this.tip = $.i18n.prop("manage.sitemap");
        this.ui_class = "sitemap"
        this.ui_body_class = "simple-tab";
    }
}

var _vsm = app.tabs.sitemap.sitemap.inherit(app.Tab);

app.ribbons.web_marketing.push(app.tabs.sitemap.sitemap.ribbon_data = {
    text: $.i18n.prop("sitemap"),
    ui_class: "sitemap",
    processor: app.tabs.sitemap.sitemap
});

_vsm.init = function () {
    var _self = this;
    app.tabs.sitemap.sitemap._super.init.call(this);
};

_vsm.onActionMenuClick = function(type) {
    var _self = this;
    if(type == "createSitemap") {
        var found = _self.body.find(".tool-text").attr("sitemap")
        _self.createSitemap(found);
    } else {
        app.Tab.changeView(_self, "sitemap", type);
    }
}

_vsm.ajax_url = app.baseUrl + "siteMap/loadAppView";

_vsm.action_menu_entries = [
    {
        text: $.i18n.prop("generate.sitemap"),
        ui_class: "generate-sitemap",
        action: "createSitemap"
    },
    {
        text: $.i18n.prop("edit.sitemap"),
        ui_class: "edit-sitemap",
        action: "editSitemap"
    }
];

_vsm.siteMapCreatePopUp = function() {
    var _self = this;
    bm.editPopup(app.baseUrl + "siteMap/showCreatePopUp", $.i18n.prop("generate.sitemap"), "", {}, {
        width: 250,
        events: {
            content_loaded: function (popup) {

            }
        },
        success: function (resp) {
            _self.reload(resp.xml)
        }
    })
};

_vsm.createSitemap = function(found) {
    var _self = this;
    if(found == "yes") {
        bm.confirm($.i18n.prop("existing.sitemap.override"), function() {
            _self.siteMapCreatePopUp()
        } , function () {
        })
    }
    else {
        _self.siteMapCreatePopUp()
    }

}

_vsm.reload = function(xml) {
    this.body.find(".sitemap-area").val(xml);
}

 bm.onReady(app.tabs, "scriptEditor", function() {
    app.tabs.sitemap.editSitemap = function () {
        this.file = {
            path: "siteMap/loadXml",
            name: "sitemap.xml",
            mode: "xml",
            loadUrl: "siteMap/loadXml",
            saveUrl: "siteMap/saveXml",
            method: "post"
        }
        app.tabs.sitemap.editSitemap._super.constructor.apply(this, arguments);
    }

    var _esm = app.tabs.sitemap.editSitemap.inherit(app.tabs.scriptEditor);

    (function() {
        function attachEvent() {
            var _self = this
        }

        _esm.init = function () {
            var toolSave = this.body.find(".toolbar-item.save").parent(".tool-group");
            toolSave.after('<div class="tool-group">' +
                    '<span class="toolbar-item  switch-menu collapsed" title="' + $.i18n.prop("switch.view") + '"><i></i></span>' +
                '</div>');
            app.tabs.sitemap.editSitemap._super.init.call(this);
            attachEvent.call(this);
        };
    })();

    _esm.switch_menu_entries = [
        {
            text: $.i18n.prop("view.mode"),
            ui_class: "view-sitemap",
            action: "sitemap"
        }
    ];

    _esm.switchHandler = function(type) {
        app.Tab.changeView(this, "sitemap", type);
    }
})