app.tabs.autoPage = function () {
    this.text = $.i18n.prop("auto.pages");
    this.tip = $.i18n.prop("manage.auto.pages");
    this.ui_class = "fixed-pages";
    this.ajax_url = app.baseUrl + "autoPage/loadAppView";
    app.tabs.autoPage._super.constructor.apply(this, arguments);
};

app.ribbons.web_content.push({
    text: $.i18n.prop("auto.gen.page"),
    processor: app.tabs.autoPage,
    ui_class: "fixed-page"
});

app.tabs.autoPage.inherit(app.SingleTableTab);

var _p = app.tabs.autoPage.prototype;

_p.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "fixed-page-edit edit",
        action: "edit"
    },
    {
        text: $.i18n.prop("edit.content"),
        ui_class: "content-edit edit",
        action: "edit-content"
    }
];

_p.onMenuOpen = function(nav, floatingMenu) {
    var attr = nav.attr('disabled-menu-entries');
    floatingMenu.find(".menu-item.fixed-page-edit").removeClass("last-menu-item")
    floatingMenu.find(".disabled").removeClass("disabled").show();
    if(attr) {
        var list = attr.split(' ');
        floatingMenu.find(".menu-item.fixed-page-edit").addClass("last-menu-item")
        floatingMenu.find("." + list.join(", .")).addClass("disabled").hide();
    }
};

_p.onActionClick = function (action, data) {
    switch (action) {
        case "edit":
            this.editPage(data.id, data.name);
            break;
        case "edit-content":
            this.editContent(data.id, data.name);
            break;
    }
};

_p.editPage = function (id, name) {
    var _self = this;
    _self.renderCreatePanel(app.baseUrl + "autoPage/edit", $.i18n.prop("edit.auto.gen.page"), name, {id: id}, {
        success: function () {
            _self.reload();
        },
        content_loaded: function () {
            bm.metaTagEditor(this.find("#bmui-tab-metatag"));
        }
    });
};

_p.editContent = function(pageId, pageName) {
    var tabId = "tab-edit-auto-page-" + pageId;
    var tab = app.Tab.getTab(tabId);
    if(!tab) {
        tab = new app.tabs.edit_content.product_page({
            id: tabId,
            containerId : pageId,
            containerName: pageName
        });
        tab.render();
    }
    tab.setActive();
};

_p.sortable = {
    list: {
        "0": "name",
        "1": "title"
    },
    sorted: "0",
    dir: "up"
};
