app.tabs.navigationItem = function() {
    app.tabs.navigationItem._super.constructor.apply(this, arguments);
    $.extend(this, {
        text: $.i18n.prop("edit.navigation"),
        name: this.navigation.name,
        tip: $.i18n.prop("edit.navigation") + " - " + this.navigation.name,
        ui_class: "edit-navigation edit",
        ui_body_class: "simple-tab",
        ajax_url: app.baseUrl + "navigation/loadNavigationEditor?id=" + this.navigation.id,
        strict_layout: false
    });
}

app.tabs.navigationItem.inherit(app.ExplorerPanelTab);

var _en = app.tabs.navigationItem.prototype;

_en.imageMap = {};
_en.resize_disabled = true;
_en.tree_disabled = true;
_en.left = 400;
_en.crateFuncSpecificEvent = null;
(function(){
    function attachEvent(){
        var _self = this;
        if(_self.body.find(".bmui-stl-entry").length){
            _self.menuStatus.autoPopulate = false;
        } else {
            _self.menuStatus.autoPopulate = true;
        }
        this.body.find(".toolbar .save").click(function() {
            _self.save(function(newIdMaps, removedItems) {
                var formdata = new FormData();
                _self.body.find("[update_cache]").removeAttr("update_cache");
                $.each(newIdMaps, function(k, v) {
                    var file = _self.imageMap[k] ? _self.imageMap[k] : [];
                    delete _self.imageMap[k];
                    formdata.append("id", v);
                    formdata.append("item-images", file[0]);
                    formdata.append("imageData", file[1] ? true : false);
                    _self.body.find("[item_id='" + k + "']").attr("item_id", v);
                    _self.body.find("[parent='" + k + "']").attr("parent", v);
                });

                $.each(removedItems, function(v) {
                    formdata.append("removedId", v);
                })
                $.each(_self.imageMap, function(key, value) {
                    var file = value
                    formdata.append("id", key);
                    if(value) {
                        file = value[0] && value[0].type ? file[0] : null;
                        formdata.append("item-images", file);
                        formdata.append("imageData", value[1] ? true : false);
                    } else {
                        formdata.append("item-images", null);
                        formdata.append("imageData", false);
                    }


                });
                bm.ajax({
                    url: "navigation/updateItemImage",
                    data : formdata,
                    processData: false,
                    contentType: false
                });
                _self.body.find(".removed-repository").empty();
            });
        });

        this.body.find(".right-panel").scrollbar({
            vertical: {
                offset: 3
            }
        });

        this.body.find(".scroll-item-wrapper").on("click", ".edit", function(ev) {
            _self.createNavigationItem($(ev.target).closest(".bmui-stl-entry"));
        });
        this.body.find(".scroll-item-wrapper").on("click", ".remove", function(ev) {
            var deleteItem = $(ev.target).closest(".bmui-stl-entry");
            var repository = _self.body.find(".removed-repository");
            var deleteId = +deleteItem.attr("item_id");
            if(deleteId > 0) {
                delete _self.imageMap[deleteId]
                repository.append("<input value='" + deleteId + "'>");
            }
            var childs = deleteItem.siblings(".bmui-stl-sub-container").find(".bmui-stl-entry");
            childs.each(function() {
                deleteId = +$(this).attr("item_id");
                if(deleteId > 0) {
                    delete _self.imageMap[deleteId]
                    repository.append("<input value='" + deleteId + "'>");
                }
            });
            deleteItem.closest(".bmui-stl-entry-container").remove();
            if(_self.body.find(".bmui-stl-entry").length == 0) {
                _self.menuStatus.autoPopulate = true;
            }
            _self.setDirty();
        });
    }
    _en.init = function() {
        var _self = this
        _self.createNavigationItem();
        app.tabs.navigationItem._super.init.call(this);
        this.body.find(".scroll-item-wrapper").sortableTreeList({
            change: function(info) {
                info.item.attr("parent", info.parent.attr("item_id") || "");
                _self.setDirty();
            }
        });
        this.clearDirty()
        attachEvent.call(this);
    }
})()

_en.action_menu_entries = [
    {
        text: $.i18n.prop("auto.populate.navigation"),
        ui_class: "auto-populate-navigation",
        action: "auto-populate-navigation"
    },
    {
        ui_class: "item-separator"
    },
    {
        text: $.i18n.prop("revert"),
        ui_class: "revert",
        action: "revert"
    }
];

_en.menuStatus = {
    autoPopulate: true,
    revert: true
};

_en.onActionMenuOpen = function(entity) {
    if(this.menuStatus.autoPopulate) {
        this.action_menu.show("auto-populate-navigation");
        this.action_menu.show("item-separator");
    } else {
        this.action_menu.hide("auto-populate-navigation");
        this.action_menu.hide("item-separator");
    }
    if(this.menuStatus.revert) {
        this.action_menu.enable("revert");
    } else {
        this.action_menu.disable("revert");
    }
}

_en.onActionMenuClick = function(action) {
    switch (action) {
        case "auto-populate-navigation":
            this.autoPopulateNavigation();
            break;
        case "revert":
            this.reloadEditor();
            break;
    }
}
_en.offCrateFuncSpecificEvent = function() {
    if(this.crateFuncSpecificEvent) {
        this.off_global(this.crateFuncSpecificEvent)
    }
}

_en.createNavigationItem = function (selectedRow) {
   this.offCrateFuncSpecificEvent();
    var data = {};
    var _self = this;
    if(selectedRow) {
        data.cache = selectedRow.attr("update_cache");
        data.id = selectedRow.attr("item_id");
        data.parent = selectedRow.attr("parent");
    }
    var serialized = _self.serializeNames(_self.body);
    data.parents = JSON.stringify(_self.getItemsForSelections(serialized, data));
    data.title = data.id || data.cache ? $.i18n.prop("edit.item") : $.i18n.prop("add.a.new.item");

    bm.ajax({
        url: app.baseUrl + "navigation/createNavigation",
        dataType: "html",
        data: data,
        success: function (resp) {
            resp = $(resp)
            var _form = _self.body.find(".navigation-item-form");
            if (_form.length) {
                _form.replaceWith(resp);
            }
            else {
                _self.body.find(".left-panel").append(resp);
            }
            _form = _self.body.find(".navigation-item-form");
            var image = _self.imageMap[data.id];
            if(image) {
                _form.find("[name=item-image]").attr("remove-support", "true")
                _form.find("#navigation-logo-preview").attr("src", image[1]);
            }
            _form.find(".cancel-button").on("click", function () {
                _self.createNavigationItem()
            });

            _form.find("[name=item-image]").on("file-remove", function () {
                _form.find("#navigation-logo-preview").attr("src", null);
            });
            var typeSelector = _form.find("#itemType")
            typeSelector.change(function () {
                var combo = this;
                _self.offCrateFuncSpecificEvent();
                var typeSelectorRow = $(combo).closest(".form-row");
                setTimeout(function () {
                    if (!combo.options[0].value) {
                        $(combo).chosen("remove", combo.options);
                    }
                }, 50)
                var currentType = this.value;
                bm.ajax({
                    url: app.baseUrl + "navigation/loadReferenceSelectorBasedOnType",
                    data: {type: currentType},
                    dataType: "html",
                    success: function (resp) {
                        var refSelectorRow = typeSelectorRow.next(".ref-selector-row");
                        if (refSelectorRow.length) {
                            refSelectorRow.replaceWith(resp)
                        } else {
                            typeSelectorRow.after(resp)
                        }
                        typeSelectorRow.next(".ref-selector-row").updateUi()
                        typeSelectorRow.next(".ref-selector-row").find(".create-new").on("click", function() {
                            _self.offCrateFuncSpecificEvent()
                            app.navigation_item_ref_create_func[currentType].call(_self)
                            var event = currentType + "-create"
                            _self.one_global(event, function() {
                                _self.crateFuncSpecificEvent = null
                                typeSelector.trigger("change")
                            })
                            _self.crateFuncSpecificEvent = event
                        })
                        _form.updateValidator();
                    }
                });
            });
            _self.body.find(".left-panel").scrollbar({
                vertical: {
                    offset: 3
                }
            });
            _form.updateUi();

            _form.form({
                ajax: {
                    beforeSubmit: function (args, form) {
                        var selectedParentId = args[4].value
                        var cache = {};
                        $.each(args, function (i, v) {
                            if (v.name != "item-image") {
                                cache[v.name] = v.value;
                            }
                        });
                        var itemType = cache["itemType"]
                        if (!cache["itemRef"]) {
                            bm.notify($.i18n.prop("no." + itemType.dotCase() + ".is.selected"), "alert")
                            var formInst = _form.data("formInst");
                            formInst.submitButton.text(formInst.submitButton[0].orgText);
                            formInst.submitButton.removeAttr("disabled", "disabled")
                            return false
                        }
                        cache["parent"] = selectedParentId;
                        var currentParentId, negativeId;
                        var sortableWraper = _self.body.find(".scroll-item-wrapper");
                        var tempRow = selectedRow;
                        var imageData = form.find("#navigation-logo-preview").attr("src");
                        if (selectedRow) {
                            currentParentId = sortableWraper.sortableTreeList("getParent", selectedRow).attr("item_id") || "0";
                            if(data.id < 0) {
                                _self.imageMap[data.id] = (args[5].value.type) ? [args[5].value, imageData] : (imageData && _self.imageMap[data.id] ? _self.imageMap[data.id]: null)
                            }
                            else {
                                _self.imageMap[data.id] = (args[5].value.type) ? [args[5].value, imageData] : (imageData ? [null, imageData] : null)
                            }
                        } else {
                            var newItem = $('<div class="bmui-stl-entry"><div class="table-actions action-column"><span class="tool-icon edit" title="' + $.i18n.prop("edit") + '"></span><span class="tool-icon remove" title="' + $.i18n.prop("delete") + '"></span></div><div class="name-column"></div></div>');
                            var freeFirstNegativeIdEl = _self.body.find(".freeFirstNegativeId");
                            var freeFirstNegativeId = freeFirstNegativeIdEl.val();
                            freeFirstNegativeIdEl.val(+freeFirstNegativeId + 1);
                            negativeId = (-1 * freeFirstNegativeId)
                            newItem.attr("item_id", "" + negativeId);
                            tempRow = newItem;
                            _self.imageMap[negativeId] = (args[5].value.type) ? [args[5].value, imageData] : null;
                        }
                        cache["item_id"] = negativeId;
                        tempRow.attr("update_cache", JSON.stringify(cache));
                        tempRow.find(".name-column").text(cache.label);
                        if (currentParentId !== cache.parent) {
                            tempRow.attr("parent", cache.parent);
                            var parent
                            if(cache.parent) {
                                sortableWraper.sortableTreeList("each", function(entry) {
                                    var id = entry.attr("item_id")
                                    if(id == cache.parent) {
                                        parent = entry
                                        return false;
                                    }
                                })
                            }
                            if(tempRow.parent().length) {
                                sortableWraper.sortableTreeList("setParent", tempRow, parent);
                            } else {
                                sortableWraper.sortableTreeList("addHandle", tempRow, parent);
                            }
                        }
                        _self.setDirty();
                        _self.menuStatus.autoPopulate = false;
                        tempRow.updateUi();
                        _self.createNavigationItem()
                        return false;
                    }

                }
            })
        }
    });
};

_en.serialize = function(wrapper) {
    var items = [];
    wrapper.find(".bmui-stl-entry").each(function(placement) {
        var item = $(this);
        var updateCache = item.attr("update_cache");
        items.push({
            id: item.attr("item_id"),
            parent: item.attr("parent"),
            placement: placement,
            update_cache: updateCache ? JSON.parse(updateCache) : undefined
        });
    });
    return items;
}

_en.serializeNames = function (wrapper) {
    var items = [];
    wrapper.find(".bmui-stl-entry").each(function() {
        var item = $(this);
        items.push({
            id: item.attr("item_id"),
            parentId: item.attr("parent"),
            title: item.find(".name-column").text().trim()
        });
    });
    return items;
}

_en.getItemsForSelections = function (items, data) {
    var map = []
    var node = {};
    node.name = $.i18n.prop("none")
    node.id = 0;
    node.depth = 1;
    node.children = [];
    node.expand = [];
    map.push(node)
    var selectedId
    if (data.id) {
        selectedId = data.id
    }
    items.every(function () {
        var newNode = { name : this.title, id : this.id, children : []}
        map[this.id] = newNode;
        if(this.parentId) {
            if(this.parentId != selectedId && this.id != selectedId) {
                map[this.parentId].children.push(newNode)
            }
        } else {
            if(this.id != selectedId){
                map[0].children.push(newNode);
            }
        }
    })
    var entityList = [];
    function addChildren (childList, depth){
        childList.every(function(){
            this.depth  = depth;
            entityList.push(this);
            addChildren(this.children, depth + 1)
        })
    }
    entityList.push(map[0]);
    addChildren(map[0].children, 1)
    return entityList;
}

_en.autoPopulateNavigation = function () {
    var _self = this;
    bm.editPopup("navigation/autoPopulate", $.i18n.prop("populate.navigation"), "", {}, {
        events: {
            content_loaded: function (popup) {
                var _form = this
                _form.find(".auto-populate-option").click(function(){
                    var type = $(this).attr("item-type")
                    _self.autoPopulate(type)
                    popup.close();
                })
            }
        }
    })
}

_en.autoPopulate = function (type) {
    var _self = this
    var repository = _self.body.find(".removed-repository");
    var removedItems = [];
    repository.children().each(function() {
        removedItems.push(this.value);
    });
    bm.ajax({
        url : app.baseUrl + "navigation/autoPopulateWithItems",
        dataType:"html",
        data : {id : _self.navigation.id, type: type, removedItems: removedItems},
        success: function (resp) {
            _self.body.html(resp)
            _self.init()
            _self.setDirty();
        }
    })
}

_en.save = function(afterSave) {
    if(!this.isDirty()) {
        return;
    }
    var _self = this;
    var serializedObject = _self.serialize(this.body);

    var repository = _self.body.find(".removed-repository");
    var removedItems = [];
    repository.children().each(function() {
        removedItems.push(this.value);
    });
    var data = {navigationId: this.body.find("#navigationId").val(), updatedJSON: JSON.stringify(serializedObject)};

    if(removedItems.length) {
        data.removedItems = removedItems;
    }
    bm.ajax({
        url: app.baseUrl + "navigation/saveNavigationItems",
        data: data,
        success: function(resp) {
            _self.clearDirty()
            if(afterSave) {
                afterSave(resp.newIdMaps, removedItems);
            }
            _self.reloadEditor();
            app.tabs.navigation.tab.reload()
        },
        complete: function() {
        }
    })
}

_en.setDirty = function () {
    this.body.find(".toolbar .save").removeClass("disabled");
    this.menuStatus.revert = true;
    app.tabs.navigationItem._super.setDirty.call(this);
}

_en.clearDirty = function () {
    this.body.find(".toolbar .save").addClass("disabled");
    this.menuStatus.revert = false;
    app.tabs.navigationItem._super.clearDirty.call(this);
}

_en.reloadEditor = function () {
    var _self = this;
    this.imageMap = {};
    bm.ajax({
        url : this.ajax_url,
        dataType: "html",
        success : function (resp) {
            _self.body.html(resp)
            _self.init()
        }
    })
}