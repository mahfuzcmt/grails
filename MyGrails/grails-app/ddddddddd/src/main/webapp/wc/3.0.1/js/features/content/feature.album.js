app.tabs.album = function (configs) {
    this.text = $.i18n.prop("albums");
    this.tip = $.i18n.prop("manage.albums");
    this.ui_class = "album";
    app.tabs.album._super.constructor.apply(this, arguments);
}

app.tabs.album.inherit(app.ExplorerPanelTab);

var _a = app.tabs.album.prototype;

_a.initLeftPanel = function(selected) {
    var _self = this;
    var leftPanel = this.body.find(".left-panel");
    leftPanel.scrollbar({
        vertical: {
            offset: 3
        }
    });

    var menu = bm.menu([
        {
            text: $.i18n.prop("upload.images"),
            ui_class: "upload-images upload",
            action: "uploadImages"
        },
        {
            text: $.i18n.prop("edit"),
            ui_class: "edit",
            action: "editAlbum"
        },
        {
            text: $.i18n.prop("remove"),
            ui_class: "delete",
            action: "deleteAlbum"
        }
    ], this.body.find(".left-panel .float-menu-navigator"), null, {
        open: function(entity) {
            entity.parent().addClass("float-menu-opened");
        },
        hide: function(entity) {
            entity.parent().removeClass("float-menu-opened");
        },
        click: function(action, navigator) {
            var data = [];
            var clicked = navigator.closest(".album-thumb");
            data.id = clicked.attr("album-id");
            data.name = clicked.attr("album-name")
            _self.onActionClick(null, action, data)

        }
    }, "click", ["center bottom", "right+22 top+7"]);
    if(!leftPanel.find(".album-thumb.selected").length) {
        leftPanel.find(".album-thumb").first().addClass("selected")
    }
    this.body.find(".album-thumb").click(function() {
        var $this = $(this);
        if($this.is(".floating-menu")) { //prevent firing for childs
            return;
        }
        _self.body.find(".album-thumb.selected").removeClass("selected");
        _self.selectedAlbum = $this.attr("album-id");
        $this.closest(".album-thumb").addClass("selected");
        _self.reloadRightPanel()
    });
    _self.selectedAlbum = leftPanel.find('.album-thumb.selected').attr("album-id");
};

_a.onSwitchMenuClick = function(type) {
    app.Tab.changeView(this, "album", type, app.tabs.album.ribbon_data.supers[type]);
}

_a.menu_entries = {
    albumImage: [
        {
            text: $.i18n.prop("open.with.editor"),
            ui_class: "edit",
            action: "editImage"
        },
        {
            text: $.i18n.prop("remove"),
            ui_class: "delete",
            action: "deleteImage"
        },
        {
            text: $.i18n.prop("properties"),
            ui_class: "properties",
            action: "imageProperties"
        },
        {
            text: $.i18n.prop("rename"),
            ui_class: "rename",
            action: "renameImage"
        }
    ]
};

(function () {
    function attachEvents() {
        var _self = this
        this.on_global("album-restore", function() {
            _self.reload()
        })
        this.on("close", function () {
            app.tabs.album.tab = null;
        });
        this.body.find(".toolbar .create").on("click", function() {
            _self.createAlbum();
        });
    }
    _a.init = function () {
        app.tabs.album._super.init.call(this);
        app.tabs.album.tab = this
        attachEvents.call(this);
    }
})();

_a.action_menu_entries = [
    {
        text: $.i18n.prop("import.pdf"),
        ui_class: "import import-pdf",
        action: "import-pdf"
    }
];

_a.switch_menu_entries = [
    {
        text: $.i18n.prop("list.view"),
        ui_class: "view-switch list",
        action: "list"
    }
];

_a.importPdf = function () {
    var _self = this
    bm.editPopup(app.baseUrl + "album/importPdf", $.i18n.prop("import.pdf"), "", {}, {
        success: function () {
            _self.reload()
        }
    });
}

_a.createAlbum = function() {
    this.renderCreatePanel(app.baseUrl + "album/create", $.i18n.prop("create.album"), null, {}, {
        success: function (resp) {
            var id = resp.id
            var name = resp.name
            if(app.tabs.album.tab) {
                app.tabs.album.tab.reload()
            }
            app.tabs.album.uploadImages(id, name)
        }
    })
}

_a.onActionMenuClick = function(action) {
    switch (action) {
        case "import-pdf":
            this.importPdf();
    }
}

_a.onActionClick = function (type, action, data) {
    switch (action) {
        case "uploadImages":
            app.tabs.album.uploadImages(data.id, data.name)
            break;
        case "deleteAlbum":
            this.deleteAlbum(data.id, data.name)
            break;
        case "editAlbum":
            this.editAlbum(data.id, data.name)
            break;
        case "viewImage":
            this.viewImage(data.id, data.name)
            break;
        case "editImage":
            this.editImage(data.id, data.url)
            break;
        case "deleteImage":
            this.deleteImage(data.id, data.name)
            break;
        case "imageProperties":
            this.imageProperties(data.id, data.name)
            break;
        case "renameImage":
            this.renameImage(data.id, data.name)
            break;
    }
}

app.tabs.album.uploadImages = function (id, name) {
    var data = {id: id},
        title = $.i18n.prop("upload.images");
    bm.editPopup(app.baseUrl + "album/uploadImages", title, name, data, {
        width: 650,
        beforeSubmit: function($popup) {
            if($popup.find('#album-image-previewer').children().length == 0) {
                return false;
            }
        },
        success: function() {
            $(".edit-popup-mask.popup-mask").css("visibility", "hidden");
            if(app.tabs.album.tab){
                app.tabs.album.tab.reload();
            }
        }
    });
}

_a.renameImage = function (id, name) {
    var _self = this;
    var title = $.i18n.prop("rename")
    bm.editPopup(app.baseUrl + "album/renameImage", title, name, {id: id, name: name}, {
        success: function () {
            _self.reload()
        }
    })
}

_a.deleteAlbum = function (id, name) {
    var _self = this
    bm.remove("album", $.i18n.prop("album"), $.i18n.prop("confirm.delete.album", [name]), app.baseUrl + "album/delete", id,{
        success: function () {
            app.global_event.trigger("send-trash", ["album", id])
            _self.reload()
            if(_self.body.find("[name='selectedAlbum']").val().equals(id)) {
                _self.reload()
            }
        }
    })
}

_a.editAlbum = function (id, name) {
    var data = {id: id},
        title = $.i18n.prop("edit.album"),
        _self = this;

    _self.renderCreatePanel(app.baseUrl + "album/editAlbum", title, name, data, {
        success: function () {
            _self.reload()
        }
    })
}

_a.editImage = function (id, url) {
    var _self = this;
    Aviary.launchEditor(url, app.baseUrl + "album/updateImageContent?imageId="+id, function() {
        _self.body.find("#album-image-" + id).clearCache();
        var originalSrc = $('<img id="album-image-' + id + '" src="' + _self.body.find("[name='originalImageSrc-" + id + "']").val() + '">');
        originalSrc.clearCache()
    });
}

_a.deleteImage = function (id, name, afterDelete) {
    var _self = this
    bm.confirm($.i18n.prop("confirm.delete.image", [name]), function () {
        bm.ajax({
            url: app.baseUrl + "album/deleteImage",
            data: {id: id},
            success: function () {
                _self.reload()
                if(typeof afterDelete == "function") {
                    afterDelete()
                }
            }
        })
    }, function () {
    })
}

_a.imageProperties = function (id, name) {
    var _this = this
    var title = $.i18n.prop("properties")
    _this.renderCreatePanel(app.baseUrl + "album/imageProperties", title, name, {id: id}, {
        scrollable: false,
        content_loaded: function () {
            var _self = this;
            if(_self.find("select.link-type").val() == "") {
                _self.find("#custom-link").hide(200);
                _self.find("#link-target").hide(200);
            }
            var refSelector = _self.find(".ref-selector-row");
            var currentType = _self.find("select.link-type").val();
            if(currentType == "") {
                refSelector.hide(200);
                _self.find("#link-target").hide(200);
            }
            if(refSelector.is(".product-selector")) {
                refSelector.find("select.category-selector").on("change", function() {
                    _this.bindProductSelector(refSelector, this.value);
                })
            }
            _self.find("select.link-type").change(function() {
                var combo = this;
                var typeSelectorRow = $(combo).closest(".form-row");
                var currentType = combo.value;
                if(currentType == "") {
                    _self.find(".ref-selector-row").hide(200);
                    _self.find("#link-target").hide(200);
                } else {
                    bm.ajax({
                        url: app.baseUrl + "album/loadReferenceSelectorBasedOnType",
                        data: {linkType: currentType},
                        dataType: "html",
                        success: function(resp) {
                            resp = $(resp);
                            var refSelectorRow = typeSelectorRow.next(".ref-selector-row");
                            if(refSelectorRow.length) {
                                refSelectorRow.replaceWith(resp);
                            } else {
                                typeSelectorRow.after(resp);
                            }
                            resp.find("select.category-selector").on("change", function() {
                                _this.bindProductSelector(resp, this.value);
                            })
                            resp.updateUi();
                        }
                    });
                    _self.find("#link-target").show(300);
                }
            });
            _self.find("#bmui-tab-basic").scrollbar({
                vertical: {
                    offset: -2
                }
            });
            app.global_event.trigger("album-image-property-loaded", [_this, _self, id]);
        },
        success: function () {
            _this.reload()
        },
        beforeSubmit: function(popup) {
            var valid = true;
            var addedRow = $(popup).find(".ref-selector-row");
            if (!addedRow.valid()) {
                valid = false;
            }
            app.global_event.trigger("before-album-image-property-submit", [_this, valid]);
            return valid;
        }
    })
}

_a.bindProductSelector = function(refSelector, category) {
    bm.ajax({
        url: app.baseUrl + "productAdmin/loadProductSelector",
        data: {category: category},
        dataType: "html",
        success: function(resp) {
            refSelector.find(".product-row").replaceWith(resp);
            refSelector.find(".product-row").updateUi();
        }
    })
}

app.tabs.album.explorer = function () {
    app.tabs.album.explorer._super.constructor.apply(this, arguments);
}

app.ribbons.web_content.push(app.tabs.album.ribbon_data = {
    text: $.i18n.prop("album"),
    processor: app.tabs.album.explorer,
    ui_class: "album",
    supers: {
        explorer: "ExplorerPanelTab",
        list: "SingleTableTab"
    }

});
var _e = app.tabs.album.explorer.inherit(app.tabs.album);

_e.resize_disabled = true;
_e.tree_disabled = true;
_e.left = 350;
_e.ajax_url = app.baseUrl + "album/loadAppView";
_e.explorer_url = app.baseUrl + "album/explorerView";
_e.advanceSearchUrl = app.baseUrl + "album/advanceFilter";

_e.init = function () {
    var _self = this;
    app.tabs.album.explorer._super.init.call(this);
    this.typeSelector = this.body.find("[name=albumType]").on("change", function() {
        _self.reload()
    })
    this.initLeftPanel()
    this.reloadRightPanel()
};

_e.beforeReloadRequest = function (params) {
    app.tabs.album._super.beforeReloadRequest.call(this, params);
    params.id = this.selectedAlbum
};

_e.reload = function() {
    var _self = this;
    var reloadPanel = _self.body.find(".left-panel .body");
    reloadPanel.loader();
    bm.ajax({
        url: app.baseUrl + "album/leftPanel",
        data: {selected: _self.selectedAlbum, isDisposable: _self.typeSelector.val() == "disposable"},
        dataType: "html",
        response: function() {
            reloadPanel.loader(false);
        },
        success: function(resp) {
            var rsp = $(resp);
            var countDom = _self.body.find(".item-group .count");
            var count = rsp.find(".album-thumb").length;
            countDom.text(count);
            var selected = reloadPanel.find(".selected").attr('album-id');
            if(selected) {
                rsp.find(".album-thumb[album-id=" + selected + "]").addClass("selected");
            }
            reloadPanel.find(".album-thumb").remove()
            reloadPanel.append(rsp.find(".album-thumb"));
            reloadPanel.updateUi();
            _self.initLeftPanel(selected)
            _self.explorer.reload(true)
        }
    })
};

_e.reloadRightPanel = function() {
    this.explorer.reload(true);
}

app.tabs.album.list = function() {
    app.tabs.album.list._super.constructor.apply(this, arguments);
}
var _l = app.tabs.album.list.inherit(app.tabs.album);
_l.ajax_url = app.baseUrl + "album/listView";
_l.switch_menu_entries = [
    {
        text: $.i18n.prop("explorer.view"),
        ui_class: "view-switch explorer",
        action: "explorer"
    }
];

_l.sortable = {
    list: {
        "4": "idx"
    },
    sorted: "4",
    dir: "up"
};

_l.menu_entries = _a.menu_entries.albumImage

_l.init = function () {
    var _self = this;
    app.tabs.album.list._super.init.call(this);
    this.albumSelector = this.body.find(".album-selector").on("change", function() {
        _self.selectedAlbum = _self.albumSelector.val()
        _self.reload()
    })
    this.selectedAlbum = this.albumSelector.val()
    this.body.find(".toolbar .upload").on("click", function() {
        var id = _self.albumSelector.val()
        if(id) {
            app.tabs.album.uploadImages(id, _self.albumSelector.find(":selected").text());
        } else {
            bm.notify($.i18n.prop('no.album.selected'), "alert")
        }
    });
};

_l.beforeReloadRequest = function (params) {
    app.tabs.album.list._super.beforeReloadRequest.call(this, params);
    params.id = this.albumSelector.val();
};

_l.onActionClick = function(action, data) {
    app.tabs.album.list._super.onActionClick.apply(this, ["albumImage", action, data])
}

_l.afterCellEdit = function(cell, newValue) {
    var _self = this
    bm.ajax({
        url: app.baseUrl  + "album/changeImageOrder",
        data: {id: cell.attr("entity-id"), value: newValue},
        complete: function () {
            _self.reload();
        }
    })
};

_l.onSelectedActionClick = function (action, selecteds) {
    switch (action) {
        case "remove":
            this.deleteSelectedBrands(selecteds.collect("id"));
            break;
    }
};

_l.deleteSelectedBrands = function (selecteds) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.images"), function() {
        bm.ajax({
            url: app.baseUrl + "album/deleteSelectedImage",
            data: {ids: selecteds},
            success: function () {
                _self.reload();
            }
        })
    }, function () {
    })
};
VALIDATION_RULES.validateName = {
    check: function (value) {
        var rg1=/^[^\\/:\*\?"<>\|]+$/; // forbidden characters \ / : * ? " < > |
        var rg2=/^\./; // cannot start with dot (.)
        var rg3=/^(nul|prn|con|lpt[0-9]|com[0-9])(\.|$)/i; // forbidden file names
        if (rg1.test(value)&&!rg2.test(value)&&!rg3.test(value)&&value.indexOf("%")<0) {
            return true
        } else {
            return {msg_template: "Invalid file name!"};
        }
    }
}