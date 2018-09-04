(function() {
    let _super

    app.tabs.document = function () {
        this.constructor_args = arguments
        this.text = $.i18n.prop("document")
        this.tip = $.i18n.prop("documents")
        this.ui_class = "doc"
        this.ajax_url = app.baseUrl + "document/loadAppView"
        _super.constructor.apply(this, arguments)
    }

    let _ = app.tabs.document.inherit(app.SingleTableTab)
    _super = _.constructor._super

    app.ribbons.web_content.push(app.tabs.document.ribbon_data = {
        text: $.i18n.prop("document"),
        ui_class: "doc",
        processor: app.tabs.document
    })

    _.menu_entries = [
        {
            text: $.i18n.prop("edit"),
            ui_class: "edit"
        },
        {
            text: $.i18n.prop("copy"),
            ui_class: "copy"
        },
        {
            text: $.i18n.prop("set.as.active.document"),
            ui_class: "active"
        },
        {
            text: $.i18n.prop("remove"),
            ui_class: "delete"
        }
    ]

    _.onActionClick = function(action, data) {
        switch (action) {
            case "edit":
                this.create(data.id);
                break;
            case "copy":
                this.copy(data.id)
                break;
            case "active":
                this.setActiveTemplate(data.id)
                break;
            case "delete":
                this.delete(data.id, data.name)
                break;
        }
    }

    function attachEvents() {
        this.body.find(".toolbar .create").on("click", () => {
            this.create()
        })
    }

    _.init = function() {
        _super.init.call(this)
        attachEvents.call(this)
    }

    _.create = function(id) {
        var tabId = "tab-edit-create-document"
        var tab = app.Tab.getTab(tabId)
        if(!tab) {
            tab = new app.tabs.documentEditor({
                id: tabId,
                ajax_data: {id: id}
            });
            tab.render();
        } else {
            tab.ajax_data = {id: id}
            tab.reload();
        }
        tab.setActive();
    }

    _.copy = function(id) {
        var _self = this;
        bm.ajax({
            url: app.baseUrl + "document/copy",
            data: {id: id},
            success: function () {
                _self.reload();
            }
        })
    }

    _.onSelectedActionClick = function (action, selecteds) {
        switch (action) {
            case  "remove":
                this.deleteSelected(selecteds.collect("id"));
                break;
        }
    };

    _.delete = function(id, name) {
        var _self = this;
        bm.remove("document", "Document", $.i18n.prop("confirm.delete.document", [name]), app.baseUrl + "document/delete", id, {
            is_final: true,
            success: function() {
                _self.reload();
            }
        })
    }

    _.deleteSelected = function(selecteds) {
        var _self = this;
        bm.confirm($.i18n.prop("confirm.delete.selected.document"), function () {
            bm.ajax({
                url: app.baseUrl + "document/deleteSelected",
                data: {ids: selecteds},
                success: function () {
                    _self.reload()
                }
            })
        }, function () {
        });
    }

    _.setActiveTemplate = function(id) {
        var _self = this;
        bm.ajax({
            url: app.baseUrl + "document/setActiveTemplate",
            data: {id: id},
            success: function () {
                _self.reload();
            }
        })
    }

})()