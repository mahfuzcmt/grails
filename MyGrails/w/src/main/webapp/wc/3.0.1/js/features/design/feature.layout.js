app.tabs.layout = function(configs) {
    this.text = $.i18n.prop("layout");
    this.tip = $.i18n.prop("manage.layout");
    this.ui_class = "layout";
    this.ajax_url = app.baseUrl + "layout/loadAppView";
    app.tabs.layout._super.constructor.apply(this, arguments);
}

app.ribbons.web_design.push({
    text: $.i18n.prop("layout"),
    processor: app.tabs.layout,
    ui_class: "layout"
})

app.tabs.layout.inherit(app.TwoPanelResizeable);

var _l = app.tabs.layout.prototype;
_l.resize_disabled = true;
_l.shim_required = true;

(function() {
    _l.init = function() {
        app.tabs.layout._super.init.call(this);
        var _self = this, typeSelector = _self.body.find("[name='layoutType']");
        this.on_global("layout-update", function(ev, id) {
            if(parseInt($(_self.body.find(".layout-thumb.selected")).attr("layout-id")) == id) {
                _self.reload();
            }
        });
        this.on_global("template-install", function() {
            _self.reload();
        })
        typeSelector.on("change", function() {
            _self.reload()
        })
        this.iframe = this.body.find("iframe");
        var reloadPanel = this.body.find(".right-panel .body");
        this.iframe.on("load", function () {
            bm.maskIframe(_self.iframe);
            reloadPanel.loader(false);
            _self.iframe.contents().find("#webcommander-page > .iframe-mask").mousedown(function() {
                $(document).trigger("mousedown")
            })
        })
        this.iframe.on("error", function() {
            reloadPanel.loader(false);
        })
        this.body.find(".left-panel").scrollbar({
            vertical: {
                offset: 3
            }
        })
        this.body.find('.toolbar .create').click(function() {
            _self.editLayoutName()
        })
        this.attachEvents.call(this);
    }

    _l.close = function() {
        app.tabs.layout._super.close.call(this);
    }
}())

_l.openContentEditor = function(layoutId, layoutName, section) {
    var tabId = "tab-edit-layout-" + layoutId
    var tab = app.Tab.getTab(tabId)
    if(!tab) {
        tab = new app.tabs.edit_content.layout({
            id: tabId,
            containerId: layoutId,
            containerName: layoutName,
            initial_section: section
        });
        tab.render();
    }
    tab.setActive();
};

_l.editLayoutName = function(layoutId, layoutName) {
    var _self = this;

    var newLayout = _self.body.find(".left-panel .layout-thumb.new-layout")
    if(newLayout.length) {
        newLayout.find(".layout-title").focus()
        return;
    }
    layoutName = layoutName || "";
    var newDom = $('<div layout-name="" layout-id="" class="layout-thumb blocklist-item new-layout fade-in-up"><input type="text" validation="required maxlength[100]" maxlength="100" class="layout-title full-width" value="' + layoutName + '"><span class="float-tooliconbar"><span class="tool-icon apply" title="' + $.i18n.prop("apply") + '"></span><span class="tool-icon discard" title="' + $.i18n.prop("discard") + '"></span></span></div>')
    var url = app.baseUrl + "layout/createLayout"
    if(layoutId) {
        url = app.baseUrl +"layout/rename"
        _self.body.find(".left-panel .body div[layout-id=" + layoutId + "]").replaceWith(newDom);
    } else {
        _self.body.find(".left-panel > .body").prepend(newDom)
    }
    var field = ValidationField.createDetachedField(newDom.find("input"), {
        error_position: "after"
    })
    var applyClicked = false
    newDom.find(".layout-title").focus().on("keyup.key_return", function() {
        updateName();
    }).on("blur", function() {
        if(!applyClicked) {
            _self.reload();
        }
        applyClicked = false;
    })

    newDom.find(".tool-icon.apply").mousedown(function() {
        applyClicked = true
    }).click(function() {
        updateName();
    });

    function updateName() {
        if(field.validate()) {
            var name = field.elm.val()
            var data = layoutId? {layoutId: layoutId, newName: name} : {name: name};
            bm.ajax({
                show_response_status: false,
                url: url,
                data: data,
                success: function(resp) {
                    _self.reload(resp.id);
                },
                error: function(a, b, resp) {
                    field.elm.focus()
                    field.showError({
                        msg_template: resp.message,
                        msg_params: [],
                        rule: undefined
                    })
                }
            })
        } else {
            field.elm.focus()
        }
    }
}

_l.setAsDefault = function(layoutId, layoutName) {
    var _self = this;
    bm.saveSiteConfig({
            general: {
                default_layout: layoutId
            }
    }, function(resp) {
        bm.notify($.i18n.prop('layout.set.as.default.success'), "success")
        _self.reload()
    }, function() {
        bm.notify($.i18n.prop('layout.set.as.default.failure'), "error")
    })
};

_l.attachEvents = function() {
    var _self = this;
    var menu = bm.menu([
        {
            text: $.i18n.prop("attached.pages"),
            ui_class: "attach-page attach",
            action: "attach-page"

        },
        {
            text: $.i18n.prop("copy"),
            ui_class: "copy"
        },
        {
            text: $.i18n.prop("edit.header.section"),
            ui_class: "edit edit-header",
            action: "content-edit-header"
        },
        {
            text: $.i18n.prop("edit.content.section"),
            ui_class: "edit edit-content",
            action: "content-edit-body"
        },
        {
            text: $.i18n.prop("edit.footer.section"),
            ui_class: "edit edit-footer",
            action: "content-edit-footer"
        },
        {
            text: $.i18n.prop("rename"),
            ui_class: "rename"
        },
        {
            text: $.i18n.prop("set.as.default.layout"),
            ui_class: "edit set-as-default",
            action: "set-as-default"
        },
        {
            text: $.i18n.prop("remove"),
            ui_class: "delete"
        }
    ], this.body.find(".left-panel .float-menu-navigator"), null, {
        open: function(entity) {
            entity.parent().addClass("float-menu-opened");
            if(entity.attr("is-default") == "true") {
                menu.disable("set-as-default")
                menu.disable("delete")
            } else {
                menu.enable("delete")
                menu.enable("set-as-default")
            }
        },
        hide: function(entity) {
            entity.parent().removeClass("float-menu-opened");
        },
        click: function(action, navigator) {
            var layoutId = navigator.closest(".layout-thumb").attr("layout-id");
            var layoutName = navigator.closest(".layout-thumb").attr("layout-name");

            switch(action) {
                case "copy":
                    _self.body.loader();
                    bm.ajax({
                        url: app.baseUrl + "layout/copyLayout",
                        data: {layoutId: layoutId},
                        response: function() {
                            _self.body.removeClass("updating");
                            _self.body.loader(false);
                        },
                        success: function() {
                            _self.reload();
                        }
                    });
                    break;
                case "delete":
                    var selectedLayout = $(_self.body.find(".layout-thumb.selected")).attr("layout-id")
                    bm.remove("layout", "layout", $.i18n.prop("confirm.delete.layout", [layoutName]), app.baseUrl + "layout/deleteLayout", layoutId, {
                        is_final: true,
                        start: function() {
                            _self.body.loader();
                        },
                        stop: function() {
                            _self.body.loader(false);
                            if(selectedLayout ==  layoutId) {
                                $(_self.body.find(".layout-thumb")[0]).trigger("click")
                            }
                        },
                        success: function () {
                            _self.reload();
                        }
                    })
                    break;
                case "attach-page":
                    bm.viewPopup(app.baseUrl + "layout/viewAttachPages", {
                        layoutId: layoutId
                    })
                    break;
                case "rename":
                    _self.editLayoutName(layoutId, layoutName);
                    break;
                case "set-as-default":
                    _self.setAsDefault(layoutId, layoutName);
                    break;
                case "content-edit-header":
                    _self.openContentEditor(layoutId, layoutName, "header");
                    break;
                case "content-edit-body":
                    _self.openContentEditor(layoutId, layoutName, "body");
                    break;
                case "content-edit-footer":
                    _self.openContentEditor(layoutId, layoutName, "footer");
                    break;
            }
        }
    }, "click", ["center bottom", "right+22 top+7"]);
    this.body.find(".layout-thumb").click(function() {
        var $this = $(this);
        if($this.is(".floating-menu")) { //prevent firing for childs
            return;
        }
        _self.body.find(".layout-thumb.selected").removeClass("selected");
        $this.addClass("selected");
        var reloadPanel = _self.body.find(".right-panel .body");
        var selected = $this.attr("layout-id")
        reloadPanel.loader();
        var url = app.baseUrl + "layout/renderLayout?id=" + selected + "&viewMode=true";
        bm.ajax({
            url: app.baseUrl + "layout/isAdminLoggedIn",
            response: function () {
                reloadPanel.loader(false)
            },
            success: function() {
                _self.iframe.attr("src", url);
            }
        })
    });
}

_l.reload = function(id, onlyLeftPanel) {
    var _self = this;
    var selected = this.body.find(".left-panel .body .selected").attr("layout-id");
    var reloadPanel = this.body.find(".left-panel .body");
        reloadPanel.loader();
        bm.ajax({
            url: app.baseUrl + "layout/loadLeftPanel",
            dataType: "html",
            data: {
                selected: id ? id : selected,
                new: id ? true : false,
                layoutType: _self.body.find("[name='layoutType']").val()
            },
            response: function() {
                reloadPanel.loader(false);
            },
            success: function(resp) {
                var rsp = $(resp);
                var countDom = _self.body.find(".item-group .count");
                var count = rsp.filter(".layout-thumb").length
                countDom.text(count);
                reloadPanel.find(".layout-thumb").remove();
                reloadPanel.append(resp);
                reloadPanel.updateUi();
                _self.attachEvents();
                //right panel reload
                if(onlyLeftPanel) {
                    return;
                }
                reloadPanel = _self.body.find(".right-panel .body");
                reloadPanel.loader();
                _self.iframe[0].contentDocument.location.reload(true)
            }
        });
}

