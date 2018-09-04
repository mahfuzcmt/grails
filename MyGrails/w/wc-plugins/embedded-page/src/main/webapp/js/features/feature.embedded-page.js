app.tabs.embeddedPage = function () {
    this.text = $.i18n.prop("embedded.pages");
    this.tip = $.i18n.prop("manage.embedded.pages");
    this.ui_class = "embedded-page";
    this.ajax_url = app.baseUrl + "embeddedPage/loadAppView";
    app.tabs.embeddedPage._super.constructor.apply(this, arguments);
};

app.ribbons.web_content.push(app.tabs.embeddedPage.ribbon_data = {
    text: $.i18n.prop("embedded.page"),
    ui_class: "embedded-page",
    processor: app.tabs.embeddedPage,
    license: "allow_embedded_page_feature"
});

var _ep = app.tabs.embeddedPage.inherit(app.SingleTableTab);
_ep.advanceSearchUrl = app.baseUrl + "embeddedPage/advanceFilter";
_ep.advanceSearchTitle = $.i18n.prop("embedded.page");
_ep.sortable = {
    list: {
        "1": "name",
        "2": "domId"
    },
    sorted: "1",
    dir: "up"
};

_ep.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit",
        action: "edit"
    },
    {
        text: $.i18n.prop("edit.content"),
        ui_class: "edit edit-content",
        action: "editContent"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "delete"
    }
];

_ep.onActionClick = function (action, data) {
    switch (action) {
        case "edit":
            this.edit(data.id, data.name);
            break;
        case "delete":
            this.delete(data.id, data.name);
            break;
        case "editContent":
            this.editContent(data.id, data.name)
            break;
    }
};

(function () {
    function attachEvents() {
        var _self = this;
        this.on_global("embedded-page-restore", function() {
            _self.reload();
        });
        this.body.find(".toolbar .create").on("click", function() {
            _self.createPage();
        });
    }

    _ep.init = function () {
        app.tabs.embeddedPage._super.init.call(this);
        attachEvents.call(this);
    }
})();

_ep.edit = function(id, name) {
    var _self = this;
    var data = {id: id},
        title = $.i18n.prop("edit.embedded.page");
    if (typeof id == "undefined") {
        data = {};
        name = "";
        title = $.i18n.prop("create.embedded.page");
    }
    this.renderCreatePanel(app.baseUrl + "embeddedPage/edit", title, name, data, {
        success: function (resp) {
            _self.reload();
            if(id){
                app.global_event.trigger("embedded-page-update", [id]);
            } else {
                app.global_event.trigger("embedded-page-create");
            }
        }
    });
};

_ep.onSelectedActionClick = function(action, selecteds) {
    var _self = this;
    switch (action) {
        case "remove":
            _self.removeSelected(selecteds.collect("id"));
            break;
    }
};

_ep.createPage = _ep.edit;

_ep.delete = function(id, name) {
    var _self = this;
    bm.remove("embedded-page", $.i18n.prop("embedded.page"), $.i18n.prop("confirm.delete.embedded.page", [name]), app.baseUrl + "embeddedPage/delete", id, {
        success: function () {
            _self.reload();
        },
        is_final: true
    });
};

_ep.editContent = function(id, name) {
    var tabId = "tab-edit-embedded-page-" + id
    var tab = app.Tab.getTab(tabId)
    if(!tab) {
        tab = new app.tabs.edit_content.embebbed_page({
            id: tabId,
            containerId : id,
            containerName: name
        });
        tab.render();
    }
    tab.setActive();
}

_ep.removeSelected = function(ids) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.embedded.page"), function() {
        bm.ajax({
            url: app.baseUrl + "embeddedPage/deleteSelected",
            data: {ids: ids},
            success: function () {
                _self.reload();
                _self.body.find(".action-header").hide();
            }
        })
    }, function () {
    });
};