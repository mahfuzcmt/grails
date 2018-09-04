 bm.onReady(app, "editProduct", function () {
    app.editProduct.tabInitFunctions.customProperties = function (panel) {
        var editor = new app.customProperties(panel, this)
        editor.init();
    }
})

app.customProperties = function (panel, appTab) {
    this.tool = panel.tool;
    this.body = panel;
    this.appTab = appTab;
    this.ajax_url = app.baseUrl + "compareProductAdmin/loadCustomProperties?productId=" + appTab.product.id;
    app.customProperties._super.constructor.call(this, arguments);
};


(function () {
    var _cp = app.customProperties.inherit(app.SingleTableView);
    var _super = app.customProperties._super;

    _cp.init = function () {
        _super.init.apply(this, arguments);
        this.attachEvents();
    }


    _cp.attachEvents = function (ev) {
        var _self = this;
        var lastRow = this.body.find("tr.last-row");
        var productId = _self.appTab.product.id;
        _self.autoCompleteSelected = false

        lastRow.find(".key").autocomplete({
            serviceUrl: app.baseUrl + "compareProductAdmin/autoComplete",
            type: 'post',
            params: { id: productId },
            onSelect: function (suggestion) {
                _self.autoCompleteSelected = true
                _self.matchedLabel = suggestion.value
            }
        });

        lastRow.find(".add-row").on("click", function () {
            var isValid = lastRow.valid({
                show_error: false
            });
            if (!isValid) {
                return;
            }
            var key = lastRow.find("[name=key]").val();
            if (!key) {
                _self.errorHighlight(lastRow.find("[name=key]"))
                return;
            }
            var value = lastRow.find("[name=value]").val();
            if (!value) {
                _self.errorHighlight(lastRow.find("[name=value]"))
                return;
            }

            _self.save(_self.appTab.product.id, key, value)

        });

        lastRow.on("invalid", function (evnent, obj) {
            bm.notify($.i18n.prop(obj.msg_template, obj.msg_params), "error");
            _self.errorHighlight(obj.validator.elm)
        });

        lastRow.on("keypress", function (e) {
            if (e.which == 13) {
                lastRow.find(".add-row").trigger("click")
            }
        });

        this.body.find("span.tool-icon.remove").on("click", function () {
            _self.remove($(this).attr("entity-id"));
        });

        this.body.find(".move-up").on("click", function (ev) {
            var thisRow = $(ev.target).closest("tr")
            var prevRow = thisRow.prev()
            if (prevRow.length && prevRow.has("th").length == 0)
                _self.moveUp(thisRow.attr("entity-id"), prevRow.attr("entity-id"));
        });

        this.body.find(".move-down").on("click", function (ev) {
            var thisRow = $(ev.target).closest("tr")
            var nextRow = thisRow.next()
            if (nextRow.length && !nextRow.is(".last-row"))
                _self.moveUp(thisRow.attr("entity-id"), nextRow.attr("entity-id"));
        });

        this.body.find("td.editable.key").on("cell-edit", function () {
            var $this = $(this);
            $this.find("input").autocomplete({
                serviceUrl: app.baseUrl + "compareProductAdmin/autoComplete",
                type: 'post',
                params: { id: productId }
            });
        })

        this.body.on("change", function() {
           _self.body.clearDirty();
        });
    }

    _cp.onContentLoad = function () {
        this.attachEvents();
        this.body.clearDirty();
    }

    _cp.errorHighlight = function (item) {
        item.addClass("error-highlight");
        setTimeout(function () {
            item.removeClass("error-highlight")
        }, 1000);
    }

    _cp.save = function (id, key, value) {
        var _self = this;
        var productId = _self.appTab.product.id
        bm.ajax({
            url: app.baseUrl + "compareProductAdmin/saveCustomProperties?productId=" + productId,
            data: {label: key, description: value, id: id},
            success: function (resp) {
                if (resp.idx == 1 && _self.autoCompleteSelected == true && _self.matchedLabel == key) {
                    _self.initPopUp(productId, key, _self.matchedLabel)
                }
                _self.body.reload();
            }
        })
    };

    _cp.initPopUp = function(productId, label, matchedLabel) {
        var _self = this
        var data = {productId: productId, label: label, matchedLabel: matchedLabel}
        bm.editPopup(app.baseUrl + "compareProductAdmin/importProperties", $.i18n.prop("import.all.properties"), "", data, {
            width: 450,
            success : function() {
                _self.body.reload()
            },
            error : function() {
                _self.body.reload()
            }
        })
    };

    _cp.remove = function (id) {
        var _self = this;
        bm.ajax({
            url: app.baseUrl + "compareProductAdmin/removeCustomProperties?productId=" + _self.appTab.product.id,
            data: {id: id},
            success: function () {
                _self.body.reload();
            }
        })
    };

    _cp.moveUp = function (thisRowId, prevRowId) {
        var _self = this;
        bm.ajax({
            url: app.baseUrl + "compareProductAdmin/updateRank?productId=" + _self.appTab.product.id,
            data: {thisRow: thisRowId, alterRow: prevRowId},
            success: function () {
                _self.body.reload();
            }
        })
    };

    _cp.moveDown = function (thisRowId, nextRowId) {
        var _self = this;
        bm.ajax({
            url: app.baseUrl + "compareProductAdmin/updateRank?productId=" + _self.appTab.product.id,
            data: {thisRow: thisRowId, alterRow: nextRowId},
            success: function () {
                _self.body.reload();
            }
        })
    };

    _cp.afterCellEdit = function (td, newValue, oldValue) {

        if (td.is(".key")) {
            td.find("input").autocomplete().dispose();
        }

        var tr = td.parent();
        var id = tr.attr("entity-id");

        var len = 100;
        var type = "keyEdit"
        if (td.is(".value")) {
            len = 1000
            type = "valueEdit"
        }

        if (!newValue || newValue.length > len) {
            if (newValue.length > len) {
                bm.notify($.i18n.prop("enter.no.more.characters", [len]), "error")
            }
            this.errorHighlight(td.find("input"));
            return false
        }
        var _self = this;
        bm.ajax({
            url: app.baseUrl + "compareProductAdmin/updateCustomProperties?productId=" + this.appTab.product.id,
            data: {id: id, newValue: newValue, type: type},
            success: function () {
                _self.body.reload();
            }
        })
    };

})()