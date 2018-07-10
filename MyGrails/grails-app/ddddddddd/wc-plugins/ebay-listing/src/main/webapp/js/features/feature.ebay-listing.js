app.tabs.ebayListingProfile = function () {
    this.text = $.i18n.prop("ebay.listing.profile");
    this.tip = $.i18n.prop("manage.ebay.listing.profile");
    this.ui_class = "ebay-listing";
    this.ajax_url = app.baseUrl + "ebayListingAdmin/loadAppView";
    app.tabs.ebayListingProfile._super.constructor.apply(this, arguments);
};

app.ribbons.web_marketing.push({
    text: $.i18n.prop("ebay.listing"),
    processor: app.tabs.ebayListingProfile,
    ui_class: "ebay-listing-profile",
    license: "allow_ebay_feature",
    ecommerce: true
});

app.tabs.ebayListingProfile.inherit(app.SingleTableTab);

var _ebay = app.tabs.ebayListingProfile.prototype;

_ebay.sortable = {
    list: {
        "1": "name",
        "3": "url"
    },
    sorted: "1",
    dir: "up"
};

_ebay.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit",
        action: "edit"
    },
    {
        text: $.i18n.prop("copy"),
        ui_class: "duplicate",
        action: "duplicate"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "delete"
    }
];

_ebay.onActionClick = function (action, data) {
    switch (action) {
        case "edit":
            app.tabs.ebayListingProfile.editProfile(data.id, data.name);
            break;
        case "duplicate":
            this.copyProfile(data.id)
            break;
        case "delete":
            this.deleteProfile(data.id, data.name);
            break
    }
};

_ebay.onSelectedActionClick = function (action, selecteds) {
    switch (action) {
        case "remove":
            this.deleteSelectedProfile(selecteds.collect("id"));
            break;
    }
};

(function () {
    function attachEvents() {
        var _self = this;
        _self.on_global('ebay-listing-profile-create, ebay-listing-profile-update', function() {
            _self.reload();
        })
        this.body.find(".toolbar .create").on("click", function() {
            _self.createProfile();
        });
    }

    _ebay.init = function () {
        app.tabs.ebayListingProfile._super.init.call(this);
        app.tabs.ebayListingProfile.tab = this;
        attachEvents.call(this);
    };

    app.tabs.ebayListingProfile.editProfile = function(id, name) {
        var tab = app.Tab.getTab("tab-edit-ebay-profile-" + id);
        if (!tab) {
            tab = new app.tabs.editEbayListingProfile({
                profile: {
                    id: id ? id : 0,
                    name: name ? name : ""
                },
                id: "tab-edit-ebay-profile-" + id
            });
            tab.render()
        }
        tab.setActive();
    };

    _ebay.createProfile = function() {
        this.renderCreatePanel(app.baseUrl + 'ebayListingAdmin/create', $.i18n.prop('create.ebay.listing.profile'), undefined, undefined, {
            success: function(resp) {
                app.Tab.getTab("tab-ebay-listing-profile").reload()
                app.global_event.trigger('ebay-listing-profile-create', {id: resp.data.id});
                app.tabs.ebayListingProfile.editProfile(resp.data.id, resp.data.name);
            }
        });
    };
})();

_ebay.viewProfile = function(id) {
    bm.viewPopup(app.baseUrl + "ebayListingAdmin/view", {id: id}, {width: 650});
}

_ebay.deleteProfile = function (id, name) {
    var _self = this;
    bm.remove("ebayListingProfile", $.i18n.prop("ebay.listing.profile"), $.i18n.prop("confirm.delete.ebay.listing.profile", [bm.htmlEncode(name)]), app.baseUrl + "ebayListingAdmin/deleteProfile", id, {
        success: function () {
            _self.reload();
        }
    })
}

_ebay.deleteSelectedProfile = function (selecteds) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.ebay.listing.profile"), function() {
        bm.ajax({
            url: app.baseUrl + "ebayListingAdmin/deleteSelectedProfile",
            data: {ids: selecteds},
            success: function () {
                _self.reload();
                _self.body.find(".action-header").hide();
            }
        })
    }, function () {
    })
}

_ebay.copyProfile= function(id) {
    var _self = this;
    bm.ajax({
        url: app.baseUrl + "ebayListingAdmin/copyProfile",
        data: {id: id},
        success: function() {
            _self.reload();
        }
    })
}