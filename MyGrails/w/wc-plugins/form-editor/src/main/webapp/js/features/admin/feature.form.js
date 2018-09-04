var form_tab_reference;

app.tabs.form = function () {
    app.tabs.form._super.constructor.apply(this, arguments);
    form_tab_reference = this;
    this.text = $.i18n.prop("form");
    this.tip = $.i18n.prop("manage.form");
    this.ui_class = "form";
    this.ajax_url = app.baseUrl + "formAdmin/loadAppView";
    this.right_panel_url = "formAdmin/explorerView";
    this.left_panel_url = "formAdmin/leftPanel"
};

app.ribbons.web_content.push({
    text: $.i18n.prop("form"),
    processor: app.tabs.form,
    ui_class: "form",
    license: "allow_form_builder_feature"
});

app.tabs.form.inherit(app.TwoPanelExplorerTab);
var _f = app.tabs.form.prototype;
_f.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit",
        action: "edit"
    } ,
    {
        text: $.i18n.prop("view.submissions"),
        ui_class: "view",
        action: "view"
    },
    {
        text: $.i18n.prop("copy"),
        ui_class: "copy"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "delete"
    }

];

_f.onActionClick = function (action, data) {
    switch (action) {
        case "edit" :
            this.editForm(data.id, data.name);
            break;
        case "view" :
            this.viewSubmittedData(data.id, data.name);
            break;
        case "copy":
            this.copyForm(data.id, data.name);
            break;
        case "delete":
            this.deleteForm(data.id, data.name);
            break;
    }
};

(function () {
    function attachEvents() {
        var _self = this;
        this.on_global("form-restore", function () {
            _self.reload();
        });
        this.on("close", function () {
            form_tab_reference = null;
        })
    }
    _f.init = function () {
        var _self = this;
        app.tabs.form._super.init.call(this);
        attachEvents.call(this);
        this.body.find(".create-from").on('click', function() {
            _self.initLoadForms();
        })
    };

    _f.initLeftPanel = function() {
        var _self = this;
        app.tabs.form._super.initLeftPanel.apply(this, arguments);
        this.body.find(".toolbar .create").on('click', function() {
            _self.initLoadForms();
        })
    }
})();

_f.initLoadForms = function () {
    var _self = this;
    bm.editPopup(app.baseUrl + 'formAdmin/loadCreateForm', $.i18n.prop("create.form"), null , null,  {
        width: 600,
        events: {
            content_loaded: function() {
                var $this = $(this);
                $this.find(".form-template").first().addClass("selected")
                $this.find(".form-template").on("click", function() {
                    $this.find(".form-template.selected").removeClass("selected");
                    $(this).addClass("selected")
                })
            }
        },
        beforeSubmit: function (form, data, popup) {
            var fieldType = form.find(".form-template.selected").attr("template-id");
            _self.createForm(fieldType, null, "template");
            popup.close();
            return false
        }
    });
};

_f.initRightPanel = function(rightPanel){
    var iframe = rightPanel.find("iframe");
    iframe.on("load", function() {
        bm.maskIframe(iframe)
    })
};
_f.createPanelTemplate = $('<div class="embedded-edit-form-panel create-panel create-form-editor"><div class="header"><span class="header-title"></span><span class="toolbar toolbar-right"><span class="tool-group toolbar-btn add">+</span><span class="tool-group toolbar-btn save save-web-form">' + $.i18n.prop("save")+ '</span><span class="tool-group toolbar-btn cancel">' + $.i18n.prop("cancel") + '</span></span></div><div class="body"></div></div>');

_f.createForm = function (id, name, type) {
    var title = $.i18n.prop(id && type != "template" ? name : 'untitled'), _self = this;;
    var data = {id: id, type: type}
    this.renderCreatePanel(app.baseUrl + 'formAdmin/edit', title, null, data, {
        clazz: "form-editor",
        content_loaded: function () {
            var panel = this;
            var formBuilder = new bm.WcFormBuilder(this, this, title);
            _self.create_panel = panel;
            this.on("success.form-builder", function () {
                if (form_tab_reference) {
                    form_tab_reference.reload();
                }
                if (id) {
                    app.global_event.trigger("form-update", [id]);
                } else {
                    app.global_event.trigger("form-create");
                }
                panel.closePanel();
            })

        }
    });
};

_f.editForm = _f.createForm;

_f.viewSubmittedData = function (id, name) {
    var tab = app.Tab.getTab("tab-form-data-" + id);
    var isNew = false;
    if (!tab) {
        tab = new app.tabs.formData({
            form: {
                id: id,
                name: name
            },
            id: "tab-form-data-" + id
        });
        tab.render();
        isNew = true
    }
    tab.setActive();
    if (!isNew) {
        tab.reload();
    }
};

_f.copyForm = function (id, name) {
    var _self = this;
    bm.ajax({
        url: app.baseUrl + "formAdmin/copy",
        data: {id: id},
        success: function () {
            _self.reload();
        }
    })
};

_f.deleteForm = function (id, name) {
    var _self = this;
    bm.remove("form", "Form", $.i18n.prop("confirm.delete.form", [name]), app.baseUrl + "formAdmin/delete", id, {
        success: function () {
            _self.reload();
        }
    })
};
