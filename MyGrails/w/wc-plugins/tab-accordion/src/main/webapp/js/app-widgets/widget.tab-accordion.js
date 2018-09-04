app.widget.tabAccordion = function(config) {
    app.widget.tabAccordion._super.constructor.apply(this, arguments);
}

app.widget.tabAccordion.config_width = 800;

var _ta = app.widget.tabAccordion.inherit(app.widget.base);

_ta.init = function() {
    var _self = this;
    app.widget.tabAccordion._super.init.call(this);
    var data = {};
    var form = this.content.find("form");
    var newEntry = form.find(".add-new-entry");
    var addContentCell = newEntry.find(".content-cell");
    form.on("click", ".tool-icon.add-content", function() {
        var row = $(this).parents(".div-table-row");
        data.contentType = row.find("select.content-type").val();
        bm.floatingPanel($(this), app.baseUrl + "tabAccordion/loadContentSelection", data, 450, null, {
            clazz: "tab-accordion-add-content-popup",
            masking: false,
            events: {
                content_loaded: function(popup) {
                    var element = popup.el;
                    var searchForm = element.find(".search-form");
                    element.find(".cancel-button").on("click", function() {
                        popup.close();
                    });
                    _self.popup.on("close", function() {
                        popup.close();
                    })
                    element.find("select.content-selector").on("change", function(evt, oldValue, newValue) {
                        data.parent = newValue
                        filter()
                    })
                    attachEvent();
                    element.find(".search-form").form({
                        preSubmit: function() {
                            filter(true);
                            return false;
                        }
                    });


                    function attachEvent() {
                        element.find(".content-table").scrollbar({
                            vertical: {
                                offset: 15
                            }
                        });
                        element.updateUi();
                        element.find(".add-content").on("click", function() {
                            var config = $(this).config("entity");
                            updateSelection(row, config.name, config.id);
                            popup.close();
                        });
                    }

                    function filter(search) {
                        var searchText = searchForm.find(".search-text").val();
                        if (search && popup.simpleSearchText == searchText) {
                            return false;
                        }
                        popup.simpleSearchText = searchText;
                        element.loader();
                        var searchData = data;
                        searchData.searchText = searchText;
                        bm.ajax({
                            url: app.baseUrl + "tabAccordion/loadContentSelection",
                            data: data,
                            dataType: "html",
                            response: function() {
                                element.removeClass("updating");
                                element.loader(false);
                            },
                            success: function(resp) {
                                element.find(".content-table").replaceWith($(resp).find(".content-table"));
                                attachEvent();
                            }
                        })
                    }
                }
            },
            position_collison: "none"
        })
    });

    form.on("change", "select.content-type", function(evt, oldValue, newValue) {
        updateSelection($(this).parents(".div-table-row"));
    })
    function updateSelection(row, name, id) {
        if(id) {
            row.find(".tool-icon.add-content").attr("class", 'tool-icon add-content edit');
        } else {
            row.find(".tool-icon.add-content").attr("class", 'tool-icon add-content add');
        }
        row.find(".content-cell .value").html(name ? name.htmlEncode():'');
        row.find(".content-id").val(id ? id:'');
    }

    form.on("click", ".tool-icon.remove", function() {
        $(this).parents(".div-table-row").remove();
    })
    form.find(".add-content-btn").on("click", function() {
        var name = newEntry.find(".content-name").val();
        var id = addContentCell.find(".content-id").val();
        var contentName = addContentCell.find(".value").html();
        if(!id) {
            bm.notify($.i18n.prop("content.missing"), "alert");
            return;
        }
        var contentRow = newEntry.find(".content-cell");
        var template = $('<div class="div-table-row col-3">\
            <div class="div-table-cell tab-name editable">\
                <span class="value">'+name.htmlEncode()+'</span>\
                <input type="hidden" class="name" name="contentName" maxlength="100" value="'+name.htmlEncode()+'">\
            </div>\
            <div class="div-table-cell content-cell">'+
                contentRow.find("select.content-id").clone().prop('outerHTML')+
            '</div>\
            <div class="div-table-cell">\
                <span class="tool-icon remove"></span>\
            </div></div>');
        var chosen = template.find("select.content-id");
        chosen.attr("name", "contentId");
        chosen.val(contentRow.find("select.content-id").val());
        form.find(".add-new-entry").before(template);
        template.updateUi();
        makeNameCellEditable(template.find(".div-table-cell.editable"));
        newEntry.reset();
    });
    makeNameCellEditable(form.find(".tab-name.editable"));
    function makeNameCellEditable(tds) {
        bm.makeTableCellEditable(tds, function(td, editFieldVal, oldVal) {
            var editField = td.find(".td-full-width");
            var errorObj = ValidationField.validateAs(editField, editField.attr("validation"));
            if (errorObj) {
                bm.notify($.i18n.prop(errorObj.msg_template, errorObj.msg_params), "alert");
                bm.errorHighlight(editField);
                return false;
            }
            td.find(".name").val(editFieldVal);
        });
    }

}

_ta.beforeSubmit = function(form, extraData) {
    var error = false;
    form.find("input[name=contentId]").each(function() {
        if(!this.value) {
            error = true;
            bm.notify($.i18n.prop("content.missing"), "alert");
            bm.errorHighlight($(this).parents(".content-cell"));
        }
    });
    if(error) {
        return false;
    }
}

_ta.afterContentChange = function(widget, cache, config) {
    var _self = this;
    if(typeof cache == "string") {
        cache = JSON.parse(cache)
        config = JSON.parse(cache.params)
    }
    var _window = widget.editor.iframeWindow
    function initTabAccordion() {
        _window.tabAccordion();
    }
    bm.onReady(_window, "tabAccordion", {
        ready: function() {
            initTabAccordion();
        },
        not: function() {
            var head = _window.$("head");
            head.append("<script src='" + app.systemResourceUrl + "plugins/tab-accordion/js/shared/tab-accordion.js' type='text/javascript'></script>");
        }
    });
}