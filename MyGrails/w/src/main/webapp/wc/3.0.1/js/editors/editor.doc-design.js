(function () {
    app.tabs.documentEditor = class _ extends app.Tab {
        constructor(params) {
            super(params);
            this.constructor_args = arguments
            this.text = $.i18n.prop("document.designer")
            this.tip = $.i18n.prop("document.designer")
            this.ui_class = "doc-designer basic-panel"
            this.ajax_url = "document/editDocument";
        }

        init() {
            super.init()
        }

        reinit() {
            super.reinit();
            this.attachEvents();
        }

        attachEvents() {
            var _self = this;
            if(_self.ajax_data && _self.ajax_data.id) {
                _self.documentId = _self.ajax_data.id;
            } else {
                delete _self.documentId
            }
            _self.documentSizes = {
                letter: [215.9, 279.4],
                legal: [215.9, 355.6],
                ledger: [279.4, 431.8],
                a0: [841, 1189],
                a1: [594, 841],
                a2: [420, 594],
                a3: [297, 420],
                a4: [210, 297]
            }
            _self.itemTemplate = "<div class='filler-row each-component' style='position: absolute'><span class='remove'></span></div>";

            var popup = bm.floatingPopup(_self.body.find(".create-new.floating-popup"), {
                clazz: "create-new-document"
            });

            _self.body.find(".sample-layouts-container").on("click", ".layout", function () {
                var layoutId = $(this).data().id
                var type = $(this).data().type
                _self.ajax_data = $.extend(_self.ajax_data, {id: layoutId, layoutUsed: true, type: type})
                _self.documentId = layoutId
                _self.editLayout();
            })

            popup.find(".action-item").click(function () {
                var data = this.jqObject.data();
                _self.ajax_data = $.extend(_self.ajax_data, {type: data.action})
                _self.editLayout();
            });
            if(_self.documentId) {
                _self.editLayout();
            }

            _self.body.find(".sample-layouts-container .body").scrollbar();
        }

        editLayout() {
            var _self = this;
            let container = this.body.find(".app-tab-content-container")
            this.body.loader();
            container.fill({
                data: _self.ajax_data,
                controller: "document",
                action: "renderEditor",
                response: () => {this.body.loader(false)},
                success: () => {
                    let editor = container.find(".editing-area")
                    container.replaceClass("layout-panel", "editor-panel")
                    var detailsForm = container.find(".document-details-from");

                    let rows = []

                    function placeInRow(row, component) {
                        row.append(component.addClass("sorted"))
                    }

                    function getNearestRow(pos) {
                        let nearest
                        rows.every(function(row) {
                            let dist = pos.top - row.pos.top
                            if(dist > 0 && (!nearest || nearest.dist > dist)) {
                                nearest = {dist: dist, row: row}
                            }
                        })
                        return nearest
                    }

                    function setTopMarginForRow(row, pos) {
                        let prevRowPos = row.prev().length ? row.prev().tag("rowData").pos : null
                        let top = pos.top - (prevRowPos ? prevRowPos.top + prevRowPos.height : 0)
                        row.css("margin-top", top - editor.css("padding-top"))
                    }

                    function placeComponent(component) {
                        let pos = component.pposition()
                        let row = findIntersectingRow(pos)
                        let newRow = !row
                        if(newRow) {
                            let nearest = getNearestRow(pos)
                            row = {elm: "<div class='filler-row'></div>".jqObject}
                            if(nearest) {
                                nearest.row.elm.after(row.elm)
                            } else {
                                editor.prepend(row.elm)
                            }
                            setTopMarginForRow(row.elm, pos)
                            rows.push(row)
                        }
                        placeInRow(row.elm, component)
                        if(newRow) {
                            row.pos = row.elm.pposition(null, true)
                            row.elm.tag("rowData", row)
                            let topAdjust = row.elm.css("margin-top").toNumber() + row.pos.height
                            row.elm.nextAll().each(function() {
                                this.jqObject.css("margin-top", "-=" + topAdjust)
                            })
                        }
                    }

                    function findIntersectingRow(pos) {
                        return rows.find(function(row) {
                            return bm.isOverAxis(pos.top, row.pos.top, row.pos.height)
                        })
                    }

                    let excludedComponents = $()
                    container.find(".document-component.area").each(function () {
                        let childComponents = $()
                        let area = this.jqObject
                        let areaPos = area.pposition(null, true)
                        container.find(".document-component").not(area).each(function() {
                            let child = this.jqObject
                            let pos = child.pposition()
                            if(bm.isOverAxis(pos.top, areaPos.top, areaPos.height) && bm.isOverAxis(pos.left, areaPos.left, areaPos.width)) {
                                childComponents = childComponents.add(child)
                            }
                        })
                        area.tag("children", childComponents)
                        excludedComponents = excludedComponents.add(childComponents)
                    })

                    let componentToPlace = container.find(".document-component").not(excludedComponents)
                    function placeComponents() {
                        componentToPlace.each(function () {
                            let component = this.jqObject
                            placeComponent(component)
                            if(component.is(".area")) {
                                let cachedEditor = editor
                                editor = component
                                let cachedRows = rows
                                rows = []
                                let cachedComponentsToPlace = componentToPlace
                                componentToPlace = component.tag("children")
                                placeComponents()
                                rows = cachedRows
                                editor = cachedEditor
                                componentToPlace = cachedComponentsToPlace
                            }
                        })
                    }
                    placeComponents()

                    var cssText = editor.attr("style");
                    cssText = cssText ? "#document-root{"+cssText+"}" : "#document-root{}"
                    var cssObj = new CssParser(cssText).parse();
                    var rule = cssObj.getRule("#document-root");
                    $.each(rule.attributes, function (idx, item) {
                        if(item.key.startsWith("padding")) {
                            if(item.key == "padding") {
                                var paddingTop, paddingRight, paddingBottom, paddingLeft
                                if(item.value.split(" ").length == 1) {
                                    paddingTop = paddingRight = paddingBottom = paddingLeft = item.value.split(" ")[0]
                                } else if(item.value.split(" ").length == 2) {
                                    paddingTop = paddingBottom = item.value.split(" ")[0], paddingRight = paddingLeft = item.value.split(" ")[1]
                                } else if(item.value.split(" ").length == 3) {
                                    paddingTop = item.value.split(" ")[0], paddingRight = paddingLeft = item.value.split(" ")[1], paddingBottom = item.value.split(" ")[2]
                                } else if(item.value.split(" ").length == 4) {
                                    paddingTop = item.value.split(" ")[0], paddingRight = item.value.split(" ")[1], paddingBottom = item.value.split(" ")[2], paddingLeft = item.value.split(" ")[3]
                                }
                                detailsForm.find("input[name='padding-top']").val(paddingTop.split("mm")[0])
                                detailsForm.find("input[name='padding-bottom']").val(paddingBottom.split("mm")[0])
                                detailsForm.find("input[name='padding-left']").val(paddingLeft.split("mm")[0])
                                detailsForm.find("input[name='padding-right']").val(paddingRight.split("mm")[0])
                            }
                            detailsForm.find("[name='"+item.key+"']").val(item.value.split("mm")[0])
                        } else if(item.key == "height") {
                            var height = item.value.split("mm")[0];
                            $.each(_self.documentSizes, function (idx, it) {
                                var value = it[1];
                                if(value == height) {
                                    detailsForm.find("select[name=paperSize]").val(idx).trigger("chosen:updated");
                                    return;
                                }
                            })
                        } else if(item.key == "font-size") {
                            detailsForm.find("select[name=fontSize]").val(item.value.split("px")[0]).trigger("chosen:updated");
                        } else if(item.key == "font-family") {
                            var fontFamily = item.value.split(",")[0];
                            fontFamily = fontFamily.split('"')
                            if(fontFamily.length > 1) {
                                fontFamily = fontFamily[1];
                            }
                            detailsForm.find("select[name=fontFamily]").val(fontFamily).trigger("chosen:updated");
                        }
                    })

                    this.attachEditorEvents(container)
                    _self.clearDirty();
                }
            })
        }

        attachEditorEvents(container) {
            var _self = this;
            _self.bindToolBar();
            container.find(".document-details-from").form({
                ajax: true,
                validation_config: {
                    validate_on_call_only: true
                }
            })

            container.find(".document-details-from").on("change", function(evt) {
                var field = evt.target.jqObject;
                if(field.valid()) {
                    var name = field.attr("name");
                    _self.changeDetails(name, field.val(), evt);
                }
            })

            container.find(".left-tab-container").each(function () {
                this.jqObject.scrollbar();
            })
            container.find(".editor-main-panel").scrollbar();
            container.on("click", ".component-config .close-config", function () {
                _self.removeComponentConfig();
                _self.body.find(".editing-area .filler-row").removeClass("selected");
            });

            container.on("change", ".component-config", function (evt) {
                var panel = this.jqObject;
                if(evt.target.jqObject.is("input.inline-edit-input")) {
                    return;
                }
                var data = panel.serializeObject();
                var image = panel.find("input[name=image]");
                _self.updateTemplateCache(_self.body.find(".editing-area .filler-row.selected"), data, image);
            });
            container.on("click", ".add-column", function () {
                var tbody = this.jqObject.prev("table").find("tbody");
                tbody.sortable( "destroy" )
                var select = tbody.find("tr:first").find("select").clone();
                select.attr("name", "value-#LABEL#");
                select.removeAttr("disabled");
                select.prop("selectedIndex", 0);
                var column = '<tr class="' + _self.ajax_data.type + '-column">' +
                    '<td class="name"><span class="inline-editable" data-editable-validation="required maxlength[100]">#LABEL#</span><input class="column-name" type="hidden" name="column-#LABEL#" value="#LABEL#"></td>' +
                    '<td class="value">'+select.prop('outerHTML')+'</td>' +
                    '<td class="action"><span class="remove remove-column"></span></td>' +
                    '</tr>';
                var label = "New Column " + tbody.find("tr").length;
                column = column.replaceAll("#LABEL#", label).jqObject;
                tbody.append(column);
                tbody.sortable({
                    axis: "y",
                    placeholder: true,
                    stop: function() {
                        tbody.closest(".component-config").trigger("change")
                    }
                })
                column.updateUi();
                _self.bindInlineEditable(column.find(".inline-editable"));
                column.trigger("change");
            });
            container.on("click", ".component-config .remove-column", function () {
                var table = this.jqObject.parents("table");
                this.jqObject.parents("tr").remove();
                table.trigger("change");
            });

            container.on("click", ".component-config .multi-button .button", function () {
                _self.alignComponent(this.jqObject.data("action"));
            })

            this.attachComponent();
            this.bindDragDrop();
            this.bindMacroSearchEvents()
        }

        alignComponent(action) {
            var _self = this;
            var editingArea = _self.body.find(".editing-area");
            var component = editingArea.find(".filler-row.selected");
            switch (action) {
                case "send-back":
                    editingArea.prepend(component);
                    break;
                case "back":
                    component.prev().before(component);
                    break;
                case "front":
                    component.next().after(component);
                    break;
                case "bring-front":
                    editingArea.append(component);
                    break;
            }
        }

        bindToolBar() {
            var _self = this;
            var detailsForm = _self.body.find(".document-details-from");
            var toolBar = _self.body.find(".toolbar.toolbar-right");
            var toolButtons = _self.body.find(".toolbar-template");
            toolBar.html(toolButtons.html());
            toolButtons.remove();

            toolBar.find(".document-details-from-submit").on("click", function () {
                if(detailsForm.valid()) {
                    _self.save();
                }
            });
            toolBar.find(".toolbar-btn.reset").click(function () {
                bm.confirm($.i18n.prop("unsaved.content.will.reset.are.you.sure"), function () {
                    _self.reload();
                }, function () {})
            });
            toolBar.find(".toolbar-btn.cancel").on("click", function() {
                if(_self.isDirty()) {
                    bm.confirm($.i18n.prop("there.unsaved.content.wanna.save"), function () {
                        _self.save(function () {
                            _self.ajax_data = undefined;
                            _self.reload()
                        });
                    }, function () {
                        _self.ajax_data = undefined;
                        _self.reload();
                    })
                } else {
                    _self.ajax_data = undefined;
                    _self.reload();
                }
            });
            toolBar.find(".toolbar-btn.preview").click(function () {
                var previewPaperType = _self.body.find(".document-details-from").find("#paperSize").val()
                var previewHeight = _self.documentSizes[previewPaperType][1] + "mm"
                var previewCss = {position: "relative", height: previewHeight}
                var editingContent = _self.body.find(".editing-area").clone()
                editingContent.css(previewCss)
                editingContent.find(".editable").each(function (i, elm) {
                    elm.jqObject.removeAttr("contenteditable");
                });
                bm.editPopup(undefined, $.i18n.prop("preview"), undefined, undefined, {
                    content: editingContent
                });
            });
        }

        bindInlineEditable(editable) {
            editable.editable().on("inlinechange", function () {
                var span = this.jqObject;
                span.next(".column-name").val(span.text()).trigger("change");
            });
        }

        attachComponent() {
            var _self = this;
            var editor = _self.body.find(".editing-area");
            if(_self.documentId) {
                _self.decodeDocument(editor);
            }

            var leftTab = _self.body.find(".left-detail-panel").tabify("inst");
            editor.on("click", ".filler-row", function (evt) {
                if(evt.target.jqObject.is(".remove")) {
                    return;
                }
                var template = this.jqObject;
                var config = _self.getTemplateCache(template);
                _self.body.loader();
                bm.ajax({
                    url: app.baseUrl + "document/componentConfig",
                    data: config,
                    dataType: "html",
                    response: function () {
                        _self.body.loader(false)
                    },
                    success: function (resp) {
                        resp = resp.jqObject;
                        editor.find(".filler-row").removeClass("selected");
                        template.addClass("selected").focus();
                        _self.removeComponentConfig();
                        _self.body.find("#bmui-tab-doc-designer-left-tab-component-list").append(resp);
                        resp.updateUi();

                        //table config sortable
                        resp.find("tbody").sortable({
                            axis: "y",
                            placeholder: true,
                            stop: function() {
                                resp.trigger("change")
                            }
                        })

                        _self.bindInlineEditable(resp.find(".inline-editable"));
                        var image = template.find("img.value");
                        if(image.length) {
                            resp.find("#doc-image-preview").attr("src", image.attr("src"));
                            image.attr("src") && resp.find(".dropzone-wrapper").addClass("file-added");
                        }
                        _self.bindColorPicker(resp);
                        leftTab.activate("doc-designer-left-tab-component-list");
                        resp.find(".config-body").scrollbar();
                        bm.autoToggle(resp);
                    }
                });
            }).blur(function () {});

            editor.on("click", ".filler-row .remove", function () {
                var template = this.jqObject.parents(".filler-row");
                template.remove();
                _self.removeComponentConfig();
                _self.setDirty();
            });
        }

        decodeDocument(editor) {
            var _self = this;
            var fillerRow = editor.find(".filler-row");
            var components = editor.find(".components");
            components.css("position", "absolute");
            fillerRow.css("position", "absolute");

            var fullWithComp = fillerRow.filter(".full-width-component");
            fullWithComp.each(function () {
                var $this = this.jqObject;
                $this.css("left", $this.data("left"));
                $this.css("top", $this.data("top"));

                var value = $this.find(".value").clone()
                $this.html(value);
            })

            components.each(function () {
                var $this = this.jqObject;
                var compTop = $this.css("top").pxToVal();
                var children = $this.find(".filler-row");
                children.each(function () {
                    var child = this.jqObject;
                    child.css("left", child.data("left"));
                    child.css("top", child.data("top"));
                })
                $this.replaceWith(children);
            })
            //fillerRow.find(".editable").prop("contenteditable", true);
            fillerRow.prepend('<span class="remove"></span>');
            _self.rebindDragResize(fillerRow);

            fillerRow.each(function () {
                var $this = this.jqObject;
                if($this.is(".data_table")) {
                    _self.bindDataTableEvents($this)
                } else if($this.is(".text")) {
                    $this.find(".editable")[0].contentEditable = true;
                }
            })
        }

        removeComponentConfig() {
            var panel = this.body.find(".component-config")
            panel.find(".color-picker").spectrum("destroy");
            panel.remove();
        }

        bindColorPicker(componentConfig) {
            componentConfig.find(".color-picker").spectrum({
                preferredFormat: "hex",
                allowEmpty: true,
                showInput: true,
                showInitial: true,
                showButtons: false,
                clickoutFiresChange: true,
                change: function(color) {
                    var $this = this.jqObject;
                    $this.prev(".value").text(color?color.toHexString():"transparent")
                    $this.val(color?color.toHexString():"transparent");
                    $this.trigger("change");
                }
            });
        }

        changeDetails(type, val) {
            var _self = this;
            var detailsForm = _self.body.find(".document-details-from");
            var editingArea = _self.body.find(".editing-area");
            switch (type) {
                case "paperSize":
                    var size = this.documentSizes[val];
                    editingArea.css({width: size[0] + "mm", height: size[1] + "mm"});
                    break;
                case "fontFamily":
                    var size = detailsForm.find("#fontSize").val();
                    editingArea.css({"font": size + "px '" + val + "', sans-serif"});
                    break;
                case "fontSize":
                    var font = detailsForm.find("#fontFamily").val();
                    editingArea.css({"font": val + "px '" + font + "', sans-serif"});
                    break;
                default:
                    if(type.startsWith("padding")) {
                        var css = {}
                        css[type] = val + "mm";
                        editingArea.css(css);
                    }
                    break;
            }
            _self.setDirty();

        }

        save(success) {
            var _self = this;
            var container = this.body.find(".app-tab-content-container");
            var detailsForm = container.find(".document-details-from");
            var data = {name: detailsForm.find("input[name=name]").val(), description: detailsForm.find("[name=description]").val()};
            $.extend(data, {
                id: _self.documentId,
                content: _self.encodeDocument()
            })
            if(!_self.documentId) {
                data.type = _self.ajax_data.type;
            }
            if(_self.ajax_data.layoutUsed) {
                data.type = _self.ajax_data.type;
                data.layoutUsed = _self.ajax_data.layoutUsed
            }
            this.body.loader();
            bm.ajax({
                url: app.baseUrl + "document/save",
                data: data,
                response: function () {
                    _self.body.loader(false);
                },
                success: function (resp) {
                    delete _self.ajax_data.layoutUsed
                    _self.ajax_data = resp.id
                    _self.documentId = resp.id
                    _self.reload();
                    var tab = app.Tab.getTab('tab-doc');
                    if(tab) {
                        tab.reload();
                    }
                    success && success.apply(this, arguments);
                }
            })
        }

        encodeDocument() {
            var _self = this;
            var componentDom = '<div class="components" style="position: relative;"></div>';
            var editingArea = _self.body.find(".editing-area");
            var editAreaPaddingTop = editingArea.css("padding-top").pxToVal()
            var editAreaPaddingLeft = editingArea.css("padding-left").pxToVal()
            var content = _self.body.find(".editing-area")//.clone();
            var fillerRow = _self.sortByPosition(content.find(".filler-row"));

            var fullWidth = fillerRow.filter(".full-width-component");
            var htmlObj = content.clone().empty();
            if(fullWidth.length) {
                var components = fillerRow.not(".full-width-component");
                fullWidth.each(function () {
                    var table = this.jqObject;
                    var tableTop = table.css("top").pxToVal();
                    var wrapperTop = $(componentDom);
                    var wrapperBottom = wrapperTop.clone();
                    components.each(function () {
                        var filler = this.jqObject;
                        var fillerTop = filler.css("top").pxToVal();
                        var cloneFiller = filler.clone()//.wrap(wrapDom).parent();
                        if(fillerTop < tableTop) {
                            wrapperTop.append(cloneFiller);
                            components = components.not(filler);
                            cloneFiller.data("original", filler);
                            cloneFiller.attr("data-left", filler.css("left"));
                            cloneFiller.attr("data-top", filler.css("top"));

                        } else if(table.nat == fullWidth.last().nat) {
                            wrapperBottom.append(cloneFiller);
                            components = components.not(filler);
                            cloneFiller.data("original", filler);
                            cloneFiller.attr("data-left", filler.css("left"));
                            cloneFiller.attr("data-top", filler.css("top"));
                        }
                    });

                    if(wrapperTop.children().length) {
                        htmlObj.append(wrapperTop);
                        var prevTable = wrapperTop.prev(".full-width-component");
                        var originalTable = editingArea.find(".full-width-component[data-uuid="+prevTable.data("uuid")+"]");
                        var wrapperTopPosition = editAreaPaddingTop//wrapperTop.find(".filler-row:first").css("top").pxToVal()
                        if(prevTable.length) {
                            wrapperTopPosition = prevTable.css("top").pxToVal() + originalTable.outerHeight(true);
                        }
                        wrapperTop.find(".filler-row").each(function () {
                            var $this = this.jqObject;
                            $this.css("top", ($this.css("top").pxToVal() - wrapperTopPosition));
                            $this.css("left", ($this.css("left").pxToVal() - editAreaPaddingLeft));
                        })
                        wrapperTop.css("height", editAreaPaddingTop + _self.getOuterHeightByInnerPosition(wrapperTop.find(".filler-row")));
                    }
                    var fullWidthElement = table.clone();
                    fullWidthElement.attr("data-left", fullWidthElement.css("left"));
                    fullWidthElement.attr("data-top", fullWidthElement.css("top"));
                    var eachRow = fullWidthElement.find(".row");
                    if(eachRow.data("each")) {
                        eachRow.before("%each:" + eachRow.data("each") + "%")
                        eachRow.after("%each%")
                    }
                    fullWidthElement.css({position: "relative", left: "auto", top: "auto"});
                    htmlObj.append(fullWidthElement);
                    if(wrapperBottom.children().length) {
                        htmlObj.append(wrapperBottom);
                        var originalTable = editingArea.find(".full-width-component[data-uuid="+table.data("uuid")+"]");
                        var wrapperBottomPosition = table.css("top").pxToVal() + originalTable.outerHeight(true);
                        wrapperBottom.find(".filler-row").each(function () {
                            var $this = this.jqObject;
                            $this.css("top", ($this.css("top").pxToVal() - wrapperBottomPosition));
                            $this.css("left", ($this.css("left").pxToVal() - editAreaPaddingLeft));
                        })
                        wrapperBottom.css("height", editAreaPaddingTop + _self.getOuterHeightByInnerPosition(wrapperBottom.find(".filler-row")));
                    }
                });
            } else if(fillerRow.length) {
                var comp = componentDom.jqObject;
                fillerRow.each(function () {
                    var row = this.jqObject;
                    row.css("top", (row.css("top").pxToVal()));
                })
                comp.append(fillerRow);
                htmlObj.html(comp);
            }

            htmlObj.find(".editable").removeAttr("contenteditable");
            var row = htmlObj.find(".filler-row");
            row.removeClass("selected bmui-resizable bmui-draggable");
            row.find(".remove").remove();
            row.find(".bmui-resize-handle").remove();

            return htmlObj.prop('outerHTML');
        }

        getOuterHeightByInnerPosition(fillerRow) {
            var _self = this;
            var originals = $();
            fillerRow.each(function () {
                originals = originals.add(this.jqObject.data("original"));
            })
            originals = _self.sortByPosition(originals);
            var height = 0;
            var first = originals.first();
            var ftop = first.css("top").pxToVal();
            var last = _self.sortByBottom(originals).first();
            var ltop = last.css("top").pxToVal();
            var lheight = last.outerHeight(true);
            height = (ltop - ftop) + lheight;
            return height;
        }

        sortByBottom(elements) {
            var items = elements.toArray();
            items = items.sort(function (a, b) {
                var posA = a.jqObject.pposition().bottom;
                var posB = b.jqObject.pposition().bottom;
                return posA - posB
            })
            return items.jqObject;
        }

        sortByPosition(elements) {
            var items = elements.toArray();
            items = items.sort(function (a, b) {
                var posA = a.jqObject.css("top").pxToVal();
                var posB = b.jqObject.css("top").pxToVal();
                return posA - posB
            })
            return items.jqObject;
        }

        bindDragDrop() {
            var _self = this;
            var editor = _self.body.find(".editing-area");
            var componentBody = _self.body.find(".component-body");
            var componentItem = componentBody.find(".component-item");

            var paddingTop = parseFloat(editor.css("padding-top").split("px")[0]);
            var paddingLeft = parseFloat(editor.css("padding-left").split("px")[0]);
            var paddingBottom = parseFloat(editor.css("padding-bottom").split("px")[0]);
            var paddingRight = parseFloat(editor.css("padding-right").split("px")[0]);

            componentItem.draggable({
                append_on_drop: false,
                helper: "clone",
                drop: editor,
                skip: ".bmui-resize-handle",
                containment: {top: paddingTop, right: paddingRight, bottom: paddingBottom, left: paddingLeft},
                //events: {
                stop: function (data) {
                    var component = data.elm;
                    var dropArea = _self.body.find(".editing-area");

                    var template = _self.collectTemplate(component);
                    dropArea.append(template);
                    _self.setTemplatePosition(component, template);
                    _self.rebindDragResize(template);
                    template.trigger("click");
                }/*,
                stop: function (data) {
                    var c = 0;
                }*/
                //}
            });

        }

        setTemplatePosition(component, template) {
            var _self = this;
            var editor = _self.body.find(".editing-area");
            template = template || component;
            var editorOffset = editor.offset();
            var offset = component.offset();
            var position = component.offset();

            var editorRight = editorOffset.left + editor.outerWidth(false)
            var editorBottom = editorOffset.top + editor.outerHeight(false)

            var templateRight = (offset.left + template.outerWidth(true))
            var templateBottom = (offset.top + template.outerHeight(true))

            var paddingTop = parseFloat(editor.css("padding-top").split("px")[0]);
            var paddingLeft = parseFloat(editor.css("padding-left").split("px")[0]);
            var paddingBottom = parseFloat(editor.css("padding-bottom").split("px")[0]);
            var paddingRight = parseFloat(editor.css("padding-right").split("px")[0]);

            if(!((offset.left > (editorOffset.left + paddingLeft)) && (templateRight < (editorRight - paddingRight)))) {
                if(offset.left < (editorOffset.left + paddingLeft)) {
                    position.left = paddingLeft//(editorOffset.left + paddingLeft)
                }
                if(templateRight > (editorRight - paddingRight)) {
                    position.left = (editor.width() + paddingLeft) - template.outerWidth(true)
                }
            } else {
                position.left = offset.left - editorOffset.left
            }

            if(!((offset.top > (editorOffset.top + paddingTop)) && (templateBottom < (editorBottom - paddingBottom)))) {
                if(offset.top < (editorOffset.top + paddingTop)) {
                    position.top = paddingTop
                }
                if(templateBottom > (editorBottom - paddingBottom)) {
                    position.top = (editor.height() + paddingBottom) - template.outerHeight(true)
                }
            } else {
                position.top = offset.top - editorOffset.top
            }

            if(component.is(".full-width-component")) {
                position.left = paddingLeft;
                template.css({'width': 'calc(100% - ' + (paddingLeft * 2) + 'px)'});
            }

            template.css(position);
            _self.setDirty();
            return position;
        }

        rebindDragResize(template) {
            var _self = this;
            var editor = _self.body.find(".editing-area");
            template.resizable({
                containment: "parent",
                //events: {
                    stop: function () {
                        _self.setDirty();
                        var image = template.find("img.value");
                        if(image.length) {
                            image.css("width", template.width());
                            image.css("height", template.height());
                        }
                    }
                //}
            })
            template.draggable({
                drop: editor,
                containment: "offset",
                disable_text_selection: false,
                skip: ".bmui-resize-handle",
                //events: {
                    stop: function (data) {
                        var component = data.elm;
                        _self.setTemplatePosition(component);
                    }
                //}
            });
        }

        collectTemplate(component) {
            var _self = this;
            var template = $(this.itemTemplate).attr("data-uuid", bm.getUUID());
            var componentConfig = component.data();
            var type = componentConfig.type;
            var editor = _self.body.find(".editing-area");
            var paddingLeft = parseInt(editor.css("padding-left").split("px")[0]);

            template.attr("data-type", type);
            template.data("type", type);
            template.addClass(type);
            template.css({'min-width': "50px"});
            template.find(".value").text(component.find(".title").text());
            switch (type) {
                case "area":
                    template.append("<div class='value' style='width: 640px; height: 150px; background-color: rgb(238, 238, 238)'></div>");
                    break;
                case "line":
                    template.css({'width': "200px"});
                    template.append("<div class='value' style='border-top: 1px;border-top-width: 1px;border-top-style: solid'></div>");
                    break;
                case "text":
                    template.css({'width': "120px"});
                    template.append('<div class="value editable">Text</div>');
                    template.dblclick(function () {
                        template.find(".editable")[0].contentEditable = true;
                    })
                    break;
                case "invoice_table":
                    _self.populateInvoiceTable(template);
                    break;
                case "order_table":
                    _self.populateOrderTable(template);
                    break;
                case "delivery_docket_table":
                    _self.populateDeliveryDocketTable(template);
                    break;
                case "picking_slip_table":
                    _self.populatePickingSlipTable(template);
                    break;
                case "data_table":
                    _self.populateDataTable(template);
                    _self.bindDataTableEvents(template)
                    break;
                case "image":
                    template.append('<img class="value" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAJYAAACWCAYAAAA8AXHiAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAA2ZpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMy1jMDExIDY2LjE0NTY2MSwgMjAxMi8wMi8wNi0xNDo1NjoyNyAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDoxMUUzOTkyQURDOUVFNDExQTgyNThFMzNGNjA1M0Q3NSIgeG1wTU06RG9jdW1lbnRJRD0ieG1wLmRpZDo0N0Y3RjRFQzlFRTcxMUU0OUZGOEMyQzM1NDFDQzBCQyIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDo0N0Y3RjRFQjlFRTcxMUU0OUZGOEMyQzM1NDFDQzBCQyIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgQ1M2IChXaW5kb3dzKSI+IDx4bXBNTTpEZXJpdmVkRnJvbSBzdFJlZjppbnN0YW5jZUlEPSJ4bXAuaWlkOjEyRTM5OTJBREM5RUU0MTFBODI1OEUzM0Y2MDUzRDc1IiBzdFJlZjpkb2N1bWVudElEPSJ4bXAuZGlkOjExRTM5OTJBREM5RUU0MTFBODI1OEUzM0Y2MDUzRDc1Ii8+IDwvcmRmOkRlc2NyaXB0aW9uPiA8L3JkZjpSREY+IDwveDp4bXBtZXRhPiA8P3hwYWNrZXQgZW5kPSJyIj8+9/GMdwAAB79JREFUeNrsnVlTFFcYhg+4gSKyTAAFxSWWGo2mVEyZXKTKq1T+Uq6T/JHkJ5jKlVW5SCwX3AggIjsow86IoCyS87Zzxp6mZwEaGZjnqTo69uDM9Omnv+87p4fTJfF43FiO2/aLbT/ZVmsA1s+kbX/a9rNtQyVWrEb7oNW2evoGIkCR6nqp/eM3pIIIkUu/KmIphNXQHxAh06VIBVtAdSl9AFsBYgFiAWIBYgEgFiAWIBYAYgFiAWIBIBYgFiAWAGIBYgFiASAWIBYgFgBiAWIBYgEgFiAWIBYAYgFiAWIBIBYgFiAWAGIBYgFiASAWIBYgFgBiAWIBYgGsj73FtsNLy8tmaWlpW977YHk5Yu1WhoaHzYve3m157x9v3UKs3cyhgwfNtStXPtv7JRIJ86S9nVS46wvL0tLPmpbev39P8Q6bE2hmdtasrKxQvKNDNHTbuq13YMCsrq6afXv3mm8uXTK1NcV7xz4iVgaW1xF1pmdmTE9/vyeVG3k+7egwHz58QCz4hNJZ2zqKbYkVZHFx0bydn0cs+MSr0VETn5gwvTYK5UNZWdmabSUlJebA/v2IBR9R+no9NvaxburrM5NTUzn/T0NdnTlcUZG2rbmpyewvYrEo3gOMT06mZuZVM6lW+q6lxZQdOJB1+uLbq1fN8KtXZn5hwdRUVZmG+npGhZCeBoO10pO2NnPDiiOBMnakHQmePHGCDiQVrkWRShFrTTGfSJjn3d10EGJtjFFbW2WaIhgcGVkTzRyJN2/oPMTKzMjr11mfb+/qMm/m5lL/Xl5eNo9tmrzX2mrmi3hqAbGyoKJbKS8bulQjkSSUBPv34UMTHx83KzbK/dfZSSdSvIcU7TmilV/AB48fmzkbofzXBKdmZ82QTZfHGxvpTCKWT6x4PO+fnbU1VdiF5q6XL827IvwmA2JlQJdkFIk2i64vtpMSESsVrTKM9jbC+NRUpK+HWDsUTS+MJi/hRIXmvDSxilhFzNjEhPc1lyhZXFoynS9eMCosZvQVZX0pb6uiYbbLQIi1i6k8fNhrQCoEIlZholnzv+7c4egjVnQcbWgwRyorOfKIFS3lZWVeA2osQCwAxALEAsQCQCxALEAsAMQCxALEAsQCQCxALEAsgIjZ1u9j/X33burxmeZm03TsWNrzbZ2dZiq5vue5M2e8lfMc+o3j0XjcDIyMpLZVVlSYo/X1aT+XCS2S1jMw4D1ubGgwX5465a3d8DSw9uj3LS3e2ld+tHbDPw8epG27cvGiqQp8gXDu7VvT+uxZ1n30v6Z+Dc19JrFvzx5zoqkpa79kQou/fX3hAhFrcHjY69x8kAD3Hj1Kk0ok5uZMV0+P1/FRMRGyVGS+v4c4EVhra3p2NvTnJOB9uz9+qcTSyoq3zS1EQsTaAOpERZFcq+IpUnU8f57698Vz50yspsbr+M7ubu9MVusfHIxkhT2toByMgBIhH4Ii6XPpcwYjYEdXl7f/ormxMfW5XWTSCdM/NORF1SDZoiA1VhJFoFyLakg+dxDUqbHkIv06WGdPn057rSjO8qnp6bTX0WOtqJwLyZdIrqWllJYpAir6LST3WancfzJcOHs29XhsfJzifTNInFwH2lF15Ejac1qAVrVFtjSWL5XJVZAlsX/dLP9j/3uFRTqH6qSw7cHo90VtbXo6sSeLTh41vcZOSocFI5YKaDEyOpo11Sz4IlowpQj/L0ps5kDotZ1cc75V/JwYlYHlt4P41zJVKnVRKywCZkNpzrWw/aXGykHMnq3qdInTZ+uj7RzR+COI0plWj3EpyqUkPZepGJcsLg1KQAlxzJ44Ss8uAroUvtl1I1TcB4v+7R4RFpRY4vTJk946nypYcy3buNVIDpdqJYOiqNa/cvWdngtbYTk4aqxOvkaFL8Ip6sVy3MDJPxWTbUqDiJVP1LKdrbNNYvXZs3A7Q7+W5q44dMiLOIo+ksGlLW3Tc5luAexP5U4ovxCKem6Et2+T+1ioo8KCS9paw9MNscMotwW6q7O8Ax24Y8TCu3dZa7D1ooijz+K/Y0WwyA5GOv+oURE4bGpFEVmyZbrjxQ83b3p/a35rYQcuP1lwo0J1dn0slvH5murqjCMsTVX4Z6Rjm7hfoDuYsaREksttcykyrPDON4XPJD+n/wZPmVLrTqQgrxVmm9hU2HcjLBWtbkpBB7nbdxNxTTRGEbGU8sp9UUWPtc1FniD+2XZN3iryuHbt8uVP6TAZ1TRidK/vTYTagYtf0p02416wqdDNR2n6YSRkLU8999X586lremGpRnValPe1qbMR1F0+qssSTdfMswUKbSepIp+aajFtu2yL8md2f7RN7xO8VOWmY8IK97BRYSGMDAv22w3+yBSWLnW3rebAmuoqqnWxOuoOjflqqliW+kqjQRfF3DRDmKTBVK6T5aqNZirEg/usfdRoMOxyTiFTEo/HVw1AsUQsQCwAxALEAsQCQCxALEAsAMQCxALEAkAsQCxALADEAsQCxAJALEAsQCxALLoAEAsQCxALALEAsQCxABALEAsQCwCxALEAsQAQCxALEAsAsQCxALEAEAsQCxALIAKxpugGiJhpiXWbfoCIua2bNOn+a/dtq6c/IALitl1XxNKdF1ts+10hjH6BjaY/2/6w7YZtw/8LMADsuV+f/9GuvQAAAABJRU5ErkJggg=="/>');
                    break;
                case "macro":
                    template.append('<div class="value">%' + componentConfig.macro + '%</div>');
                    template.attr("data-macro", componentConfig.macro);
                    break;
                default:
                    template.append('<div class="value"></div>');
                    break;
            }
            template.attr("config", JSON.stringify({type: type}));

            return template;
        }

        updateTemplateCache(template, config, image) {
            var _self = this;
            var type = template.data("type");
            var cache = $.extend({type: type}, config);
            var css = {};
            cache.width && (css.width = cache.width);
            cache.height && (css.height = cache.height);
            cache.borderSize && (css['border-width'] = cache.borderSize);
            cache.borderStyle && (css['border-style'] = cache.borderStyle);
            cache.borderColor && (css['border-color'] = cache.borderColor);
            cache.backgroundColor && (css['background-color'] = cache.backgroundColor);
            cache.lineStyle && (css['border-top-style'] = cache.lineStyle);
            cache.borderTopWidth && (css['border-top-width'] = cache.borderTopWidth + 'px');
            cache.lineColor && (css['color'] = cache.lineColor);

            var prevOrientation = JSON.parse(template.attr("config")).orientation
            template.attr("config", JSON.stringify(cache));

            switch(type) {
                case "image":
                    if(image && image.length && image.nat.files.length) {
                        var reader = new FileReader();
                        reader.addEventListener("load", function(e) {
                            template.find("img").attr("src", e.target.result);
                        });
                        reader.readAsDataURL(image.nat.files[0]);
                    }
                    template.find("img").css(css);
                    break;
                case "invoice_table":
                    _self.updateInvoiceTable(template, cache);
                    break;
                case "order_table":
                    _self.updateOrderTable(template, cache);
                    break;
                case "delivery_docket_table":
                    _self.updateDeliveryDocketTable(template, cache);
                    break;
                case "picking_slip_table":
                    _self.updatePickingSlipTable(template, cache);
                    break;
                case "data_table":
                    _self.updateDataTable(template, cache);
                    break;
                case "text":
                case "macro":
                    _self.updateText(template, cache);
                    break;
                case "line":
                    _self.updateLine(template, cache, css, prevOrientation)
                    break;
                default:
                    template.find(".value").css(css);
                    break;
            }
            _self.setDirty();
            return cache
        }

        updateLine(template, cache, css, prevOrientation) {
            if(cache.orientation == 'vertical') {
                //template.css( {'transform': 'rotate(90deg)', 'transform-origin': '0% 0%'})
                css.height = "inherit"
                css['border-top-width'] = ""
                css['border-top-style'] = ""
                cache.lineStyle && (css['border-left-style'] = cache.lineStyle);
                cache.borderTopWidth && (css['border-left-width'] = cache.borderTopWidth + 'px');
                var templateWidth = template.css("width")
                var templateHeight = template.css("height")
                var templateMinheight = template.css("min-height")
                var templateMinWidth = template.css("min-width")
                if(prevOrientation != "vertical") {
                    template.css({'min-height': templateMinWidth, 'min-width': templateMinheight, 'height': templateWidth, 'width': templateHeight})
                }
            } else if(cache.orientation == 'horizontal'){
                //template.css( {'transform': '', 'transform-origin': ''})
                css.height = ""
                css['border-left-width'] = ""
                css['border-left-style'] = ""
                cache.lineStyle && (css['border-top-style'] = cache.lineStyle);
                cache.borderTopWidth && (css['border-top-width'] = cache.borderTopWidth + 'px');
                var templateWidth = template.css("width")
                var templateHeight = template.css("height")
                var templateMinheight = template.css("min-height")
                var templateMinWidth = template.css("min-width")
                if((prevOrientation != undefined) && (prevOrientation != "horizontal")) {
                    template.css({'min-height': templateMinWidth, 'min-width': templateMinheight, 'height': templateWidth, 'width': templateHeight})
                }
            }
            template.find(".value").css(css);
        }

        updateText(template, data) {
            var _self = this;
            var text = template.find(".value");

            data.fontSize && text.css("font-size", data.fontSize + "px");
            if(data.bold) {
                text.css("font-weight", "bold");
            } else {
                text.css("font-weight", "normal");
            }
            if(data.italic) {
                text.css("font-style", "italic");
            } else {
                text.css("font-style", "normal");
            }
            if(data.underline) {
                text.css("text-decoration", "underline");
            } else {
                text.css("text-decoration", "none");
            }
            if(data.fontFamily) {
                text.css("font-family", data.fontFamily);
            } else {
                text.css("font-family", "inherit");
            }
            data.align && text.css('text-align', data.align);
            data.color && text.css('color', data.color);
            data.backgroundColor && text.css('background-color', data.backgroundColor);

            if(data.link) {
                if(text.find("a").length) {
                    text.html(text.find("a").html());
                }
                text.html('<a href="'+data.linkUrl+'" target="_blank">'+text.html()+'</a>');
            } else if(text.find("a").length) {
                text.html(text.find("a").html());
            }
        }

        getTemplateCache(template) {
            var data = template.attr("config");
            data = data || "{}";
            data = JSON.parse(data);
            data = $.extend({value: template.data("macro"), componentType: template.data("type")}, data);
            return data;
        }


        updateInvoiceTable(template, data) {
            var _self = this;
            var table = template.find("table");
            var headers = [];
            var values = [];
            $.each(data, function (key, value) {
                if(key.startsWith("column-")) {
                    headers.push(value)
                } else if(key.startsWith("value-")) {
                    values.push(value)
                }
            })
            if(headers.length) {
                var tableRow = _self.populateInvoiceTableRow(headers, values);
                table.find("tbody").html(tableRow);
            }

            data.rowBorder && table.find("tr").css("border", data.rowBorder + 'px');
            data.columnBorder && table.find("td").css("border-width", data.columnBorder);
            data.borderStyle && table.find("tr, td").css('border-style', data.borderStyle);
            data.borderColor && table.find("tr, td").css('border-color', data.borderColor);
            data.headerBackground && table.find(".header").css('background-color', data.headerBackground);
            data.rowBackground && table.find(".row").css('background-color', data.rowBackground);
        }

        populateInvoiceTableRow(headers, values) {
            var _self = this;
            var html = [];
            html.push('<tr class="header" style="background-color: #3997D6">');
            headers.forEach(function (elm) {
                html.push(_self.getTableHeader(elm));
            });
            html.push('</tr>');

            html.push('<tr class="row" data-each="order_details.items:item:index">');
            values.forEach(function (elm) {
                html.push(_self.getTableRow(elm));
            });
            html.push('</tr>');

            return html.join("");
        }

        getTableHeader(header) {
            var thStyle = "padding:5px; background-color: transparent;  border-left: 1px solid #3997D6; border-right: 1px solid #3997D6; border-top: 1px solid #3997D6; color: #FFFFFF; text-align:center; font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;";
            return '<th style="'+thStyle+'">'+header+'</th>';
        }

        getTableRow(value) {
            var tdStyle = "padding:5px; border:1px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;";
            return '<td style="'+tdStyle+'">'+value+'</td>';
        }

        populateInvoiceTable(template) {
            var _self = this;
            template.addClass("full-width-component");
            var html = ['<table class="value" style="border-collapse:collapse;width:100%;" border="0" cellspacing="0" cellpadding="0">'];

            var rows = _self.populateInvoiceTableRow(["Name", "Unit Price", "Ordered Quantity", "Discount", "Tax", "Price"], [
                '%if:item.url%<a href="%item.url%">%if% %item.product_name% %if:item.variations%(%item.variations%)%if%%if:item.url%</a>%if%',
                '%currency_symbol%%item.price%',
                '%item.quantity%',
                '%item.tax%',
                '%item.total_with_tax_with_discount%',
                '%currency_symbol%%item.total_with_tax_with_discount%'
            ]);
            html.push(rows);
            html.push("</table>")
            template.append(html.join(""))
            return template;
        }

        populateOrderTable(template) {
            var _self = this;
            template.addClass("full-width-component");
            var html = ['<table class="value" style="border-collapse:collapse;width:100%;" border="0" cellspacing="0" cellpadding="0">'];

            var rows = _self.populateOrderTableRow(["Name", "Unit Price", "Ordered Quantity", "Discount", "Tax", "Price"], [
                '%if:item.url%<a href="%item.url%">%if% %item.product_name% %if:item.variations%(%item.variations%)%if%%if:item.url%</a>%if%',
                '%currency_symbol%%item.price%',
                '%item.quantity%',
                '%item.tax%',
                '%item.total_with_tax_with_discount%',
                '%currency_symbol%%item.total_with_tax_with_discount%'
            ]);
            html.push(rows);
            html.push("</table>")
            template.append(html.join(""))
            return template;
        }

        populateOrderTableRow(headers, values) {
            var _self = this;
            var html = [];
            html.push('<tr class="header" style="background-color: #3997D6">');
            headers.forEach(function (elm) {
                html.push(_self.getTableHeader(elm));
            });
            html.push('</tr>');

            html.push('<tr class="row" data-each="order_details.items:item:index">');
            values.forEach(function (elm) {
                html.push(_self.getTableRow(elm));
            });
            html.push('</tr>');

            return html.join("");
        }

        updateOrderTable(template, data) {
            var _self = this;
            var table = template.find("table");
            var headers = [];
            var values = [];
            $.each(data, function (key, value) {
                if(key.startsWith("column-")) {
                    headers.push(value)
                } else if(key.startsWith("value-")) {
                    values.push(value)
                }
            })
            if(headers.length) {
                var tableRow = _self.populateOrderTableRow(headers, values);
                table.find("tbody").html(tableRow);
            }

            data.rowBorder && table.find("tr").css("border", data.rowBorder + 'px');
            data.columnBorder && table.find("td").css("border-width", data.columnBorder);
            data.borderStyle && table.find("tr, td").css('border-style', data.borderStyle);
            data.borderColor && table.find("tr, td").css('border-color', data.borderColor);
            data.headerBackground && table.find(".header").css('background-color', data.headerBackground);
            data.rowBackground && table.find(".row").css('background-color', data.rowBackground);
        }

        populateDeliveryDocketTable(template) {
            var _self = this;
            template.addClass("full-width-component");
            var html = ['<table class="value" style="border-collapse:collapse;width:100%;" border="0" cellspacing="0" cellpadding="0">'];

            var rows = _self.populateDeliveryDocketTableRow(["Product Name", "SKU", "Ordered Quantity", "Back Ordered", "Quantity Shipped"], [
                '%if:item.url%<a href="%item.url%">%if% %item.product_name% %if:item.variations%(%item.variations%)%if%%if:item.url%</a>%if%',
                '%item.sku%',
                '%item.quantity%',
                '%item.back_ordered%',
                '%item.quantity_shipped%'
            ]);
            html.push(rows);
            html.push("</table>")
            template.append(html.join(""))
            return template;
        }

        populateDeliveryDocketTableRow(headers, values) {
            var _self = this;
            var html = [];
            html.push('<tr class="header" style="background-color: #3997D6">');
            headers.forEach(function (elm) {
                html.push(_self.getTableHeader(elm));
            });
            html.push('</tr>');

            html.push('<tr class="row" data-each="order_details.items:item:index">');
            values.forEach(function (elm) {
                html.push(_self.getTableRow(elm));
            });
            html.push('</tr>');

            return html.join("");
        }

        updateDeliveryDocketTable(template, data) {
            var _self = this;
            var table = template.find("table");
            var headers = [];
            var values = [];
            $.each(data, function (key, value) {
                if(key.startsWith("column-")) {
                    headers.push(value)
                } else if(key.startsWith("value-")) {
                    values.push(value)
                }
            })
            if(headers.length) {
                var tableRow = _self.populateDeliveryDocketTableRow(headers, values);
                table.find("tbody").html(tableRow);
            }

            data.rowBorder && table.find("tr").css("border", data.rowBorder + 'px');
            data.columnBorder && table.find("td").css("border-width", data.columnBorder);
            data.borderStyle && table.find("tr, td").css('border-style', data.borderStyle);
            data.borderColor && table.find("tr, td").css('border-color', data.borderColor);
            data.headerBackground && table.find(".header").css('background-color', data.headerBackground);
            data.rowBackground && table.find(".row").css('background-color', data.rowBackground);
        }

        populatePickingSlipTable(template) {
            var _self = this;
            template.addClass("full-width-component");
            var html = ['<table class="value" style="border-collapse:collapse;width:100%;" border="0" cellspacing="0" cellpadding="0">'];

            var rows = _self.populatePickingSlipTableRow(["Product Name", "sku", "Ordered Quantity", "Back Ordered", "Pick Task", "Quantity Picked"], [
                '%if:item.url%<a href="%item.url%">%if% %item.product_name% %if:item.variations%(%item.variations%)%if%%if:item.url%</a>%if%',
                '%item.sku%',
                '%item.quantity%',
                '%item.back_ordered%',
                '%item.pick_task%',
                '<span style="border: 1px #000 solid;height: 20px;width: 20px;display: inline-block;"></span>'
            ]);
            html.push(rows);
            html.push("</table>")
            template.append(html.join(""))
            return template;
        }

        populatePickingSlipTableRow(headers, values) {
            var _self = this;
            var html = [];
            html.push('<tr class="header" style="background-color: #3997D6">');
            headers.forEach(function (elm) {
                html.push(_self.getTableHeader(elm));
            });
            html.push('</tr>');

            html.push('<tr class="row" data-each="order_details.items:item:index">');
            values.forEach(function (elm) {
                html.push(_self.getTableRow(elm));
            });
            html.push('</tr>');

            return html.join("");
        }

        updatePickingSlipTable(template, data) {
            var _self = this;
            var table = template.find("table");
            var headers = [];
            var values = [];
            $.each(data, function (key, value) {
                if(key.startsWith("column-")) {
                    headers.push(value)
                } else if(key.startsWith("value-")) {
                    values.push(value)
                }
            })
            if(headers.length) {
                var tableRow = _self.populatePickingSlipTableRow(headers, values);
                table.find("tbody").html(tableRow);
            }

            data.rowBorder && table.find("tr").css("border", data.rowBorder + 'px');
            data.columnBorder && table.find("td").css("border-width", data.columnBorder);
            data.borderStyle && table.find("tr, td").css('border-style', data.borderStyle);
            data.borderColor && table.find("tr, td").css('border-color', data.borderColor);
            data.headerBackground && table.find(".header").css('background-color', data.headerBackground);
            data.rowBackground && table.find(".row").css('background-color', data.rowBackground);
        }

        populateDataTable(template) {
            template.css("width", "640px");
            var html = ['<table class="value" cellpadding="10" style="border-collapse: collapse;width: inherit;table-layout: fixed;" border="0" cellspacing="0" cellpadding="0">'];

            var rows = this.populateDataTableRow([], [], {rows: 3, columns: 3});
            html.push(rows);

            html.push("</table>")
            template.append(html.join(""))
            return template;
        }

        populateDataTableRow(header, rows, config) {
            var _self = this;
            var html = [];
            html.push('<tr class="header">');
            for(var i = 0; i < config.columns; i++) {
                var title = header[i] || 'column'+i
                html.push('<th class="editable" style="background-color: transparent">'+title+'</th>');
            }
            html.push('</tr>');

            for(var j = 0; j < config.rows; j++) {
                html.push('<tr class="data-row">');
                var _row = rows[j] || []
                for(var k = 0; k < config.columns; k++) {
                    var value = _row[k] || ''//'value'+k
                    html.push('<td class="editable">'+value+'</td>');
                }
                html.push('</tr>');
            }

            return html.join("");
        }

        bindDataTableEvents(template) {
            var _self = this;
            template.find(".value .header th,.data-row td").dblclick(function () {
                var element = $(this)
                template.find(".value .header th,.data-row td").each(function (i, elm) {
                    elm.jqObject.removeAttr("contenteditable");
                });
                element[0].contentEditable = true;
                var fillerRow = element.closest(".filler-row.each-component.data_table.selected")
                var config = JSON.parse(fillerRow.attr("config"))
                config.cellEditing = true
                config.cellTextAlign = element.css('text-align')
                config.cellBackground = element.css('background-color')
                config.cellFontColor = element.css('color')
                config.cellFontFamily = element.css('font-family')
                config.cellFontSize = element.css('font-size').split("p")[0]

                fillerRow.attr('config', JSON.stringify(config))
            }).blur(function () {
                var element = $(this)
                var fillerRow = element.closest(".filler-row.each-component.data_table.selected")
                var config = JSON.parse(fillerRow.attr("config"))
                config.cellEditing = false
                fillerRow.attr('config', JSON.stringify(config))
            })
        }

        changeDataTable(template, data, o_row, o_col) {
            var table = template.find(".value")
            if((eval(data.rows) + 1) > o_row) {
                for (var i=0; i < (eval(data.rows)+1-o_row); i++) {
                    var newRow = []
                    newRow.push('<tr class="data-row">');
                    for(var k = 0; k < data.columns; k++) {
                        newRow.push('<td class="editable"></td>');
                    }
                    newRow.push('</tr>');
                    template.find(".value").find("tbody").append(newRow.join(""))
                }

                if (data.columns > o_col) {
                    template.find(".value").find("tr").each(function(){
                        for (var j=0; j < (data.columns-o_col); j++) {
                            if($(this).is(".header")) {
                                $(this).append('<th class="editable">Column</th>');
                            } else {
                                $(this).append('<td class="editable"></td>');
                            }
                        }
                    })
                } else {
                    template.find(".value tr").find('td:gt('+(data.columns -1)+'),th:gt('+(data.columns -1)+')').each(function(){
                        $(this).remove();
                    })
                }
            } else {
                template.find(".value").find("tr:gt("+data.rows+")").each(function(){
                    $(this).remove();
                })
                if (data.columns > o_col) {
                    template.find(".value").find("tr").each(function(){
                        for (var j=0; j < (data.columns-o_col); j++) {
                            if($(this).is(".header")) {
                                $(this).append('<th class="editable">Column</th>');
                            } else {
                                $(this).append('<td class="editable"></td>');
                            }
                        }
                    })
                } else {
                    template.find(".value tr").find('td:gt('+(data.columns -1)+'),th:gt('+(data.columns -1)+')').each(function(){
                        $(this).remove();
                    })
                }
            }
        }

        updateDataTable(template, data) {
            var _self = this;
            var table = template.find("table");

            !data.rows && (data.rows = 3);
            !data.columns && (data.columns = 3);

            var o_row = table.find("tr").length, o_col = table.find("tr")[0].jqObject.find("th").length
            if(!((data.rows == (o_row - 1)) && (data.columns == o_col))) {
                _self.changeDataTable(template, data, o_row, o_col)
            }

            data.rowBorder && table.find("tr").css("border", data.rowBorder + 'px');
            if(data.columnBorder) {
                table.find("td, th").css("border-left", data.columnBorder + 'px');
                table.find("td, th").css("border-right", data.columnBorder + 'px');
                table.find("td, th").css("border-top", '0px');
                table.find("td, th").css("border-bottom", '0px');
            }

            data.borderStyle && table.find("tr, td, th").css('border-style', data.borderStyle);
            data.borderColor && table.find("tr, td, th").css('border-color', data.borderColor);
            data.headerBackground && table.find(".header").css('background-color', data.headerBackground);
            data.rowBackground && table.find(".data-row").css('background-color', data.rowBackground);

            var cell = table.find(".header th[contenteditable=true],.data-row td[contenteditable=true]")
            if(cell.length) {
                data.cellTextAlign && cell.css('text-align', data.cellTextAlign);
                data.cellBackground && cell.css('background-color', data.cellBackground);
                data.cellFontColor && cell.css('color', data.cellFontColor);
                if(data.cellFontFamily) {
                    cell.css("font-family", data.cellFontFamily);
                } else {
                    cell.css("font-family", "inherit");
                }
                data.cellFontSize && cell.css('font-size', data.cellFontSize + 'px');
            }
            _self.bindDataTableEvents(template)
        }

        bindMacroSearchEvents() {
            var _self = this;
            var editor = _self.body.find(".editing-area")

            editor.on("keypress", ".filler-row.each-component.selected .value.editable, .value .editable", function (ev) {
                var _editable = $(this), keyCode = ev.keyCode ?  ev.keyCode : ev.which
                var ie = (typeof document.selection != "undefined" && document.selection.type != "Control") && true;
                var w3 = (typeof window.getSelection != "undefined") && true;
                var caretPos = getCaretPosition(_editable[0])
                var editableTxt = _editable.text();

                if(_editable.attr("contenteditable") != "true" || keyCode != 37) {
                    return
                }
                //Macro dropdown suggestion
                bm.floatingPanel(_editable, app.baseUrl + "document/loadMacroList", {type: _self.ajax_data.type, id: _self.ajax_data.id}, {
                    clazz: "document-macro-search-popup",
                    masking: false,
                    position_collison: "none",
                    events: {
                        content_loaded: function(popup) {
                            var element = popup.el;
                            var searchForm = element.find(".search-form");
                            var macroDropdownWrapper = element.find(".macro-dropdown-wrapper")
                            searchForm.form({
                                preSubmit: function() {
                                    return false;
                                }
                            });
                            searchForm.find(".search-text").focus()
                            bm.instantSearch(element.find(".macro-dropdown-wrapper"), searchForm, ".each-macro", ".searchable-text")
                            element.find(".macro-wrapper").scrollbar({
                                vertical: {
                                    offset: 15
                                }
                            });

                            macroDropdownWrapper.on("click", ".each-macro", function () {
                                var _macroElement = $(this), macroText = _macroElement.find(".macro-value").text()
                                _editable.text(editableTxt.substring(0, caretPos) + macroText + editableTxt.substring(caretPos) );
                                popup.close();
                            })

                            element.updateUi();
                        }
                    }
                })

                function getCaretPosition(element) {
                    var caretOffset = 0;
                    if (w3) {
                        var range = window.getSelection().getRangeAt(0);
                        var preCaretRange = range.cloneRange();
                        preCaretRange.selectNodeContents(element);
                        preCaretRange.setEnd(range.endContainer, range.endOffset);
                        caretOffset = preCaretRange.toString().length;
                    } else if (ie) {
                        var textRange = document.selection.createRange();
                        var preCaretTextRange = document.body.createTextRange();
                        preCaretTextRange.moveToElementText(element);
                        preCaretTextRange.setEndPoint("EndToEnd", textRange);
                        caretOffset = preCaretTextRange.text.length;
                    }
                    return caretOffset;
                }
            })
        }
    }

})()