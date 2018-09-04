app.tabs.edit_content = function(config) {
    this.text = $.i18n.prop("edit.content");
    this.name = config.containerName;
    this.tip = config.containerName;
    this.ui_body_class = this.ui_class = "content-editor edit-tab";
    this.removedWidgets = [];
    this.cachedWidgets = {};
    this.cachedDock = {};
    this.newDocks = [];
    this.modifiedDocks = [];
    this.removedDock = [];
    this.removedHeader = [];
    this.removedFooter = [];
    this.supportedWidgetSelector = "." + this.widgetClass;
    app.tabs.edit_content._super.constructor.apply(this, arguments);
};

app.tabs.edit_content.inherit(app.Tab);

var _e = app.tabs.edit_content.prototype;

/**
 * Properties are
 * iframe
 * sectionHeight
 * activeSection
 * cachedDock
 * selectedWidget
 * sectionWidth
 * css
 * widgetXoffset
 * widgetYoffset
 * widgetHeight
 * widgetWidth
 * iframeContent
 * pageBody
 * isResponsive
 * iframeMask
 * activeMedia
 * sidebar
 * widgetSelector
 * supportedWidgetSelector //abstract
 * gridSelector
 */
_e.widgetClass = "widget";
_e.contentSaveUrl = "widget/saveContents";
_e.widgetConfigUrl = app.baseUrl + "widget/edit";
_e.editJsUrl = app.baseUrl + "widget/editJs";
_e.editContainerJsUrl; //Abstract Property
_e.gridSelector = ".grid-block";
_e.enableRevert = false;
_e.editableBlockSelector = _e.gridSelector + ", .widget-container:not(:has(.grid-block))";
_e.sortable_page_content = false;
_e.modifyFavoriteWidgetUrl = app.baseUrl + "app/modifyFavoriteWidget"

_e.init = function() {
    app.tabs.edit_content._super.init.apply(this, arguments);
    var _self = this;
    _self.mask();
    this.iframe = this.body.find("iframe");
    this.leafGridBlockSelectorInContentMode = ".widget-container:not(:has(>.grid-block, >.page-content)), .grid-block:not(:has(>.grid-block, >.page-content))";
    this.sectionHeight = _self.body.find("input.height").ichange(1000, function(event, currentValue) {
        var section = _self.activeSection;
        var currentHeight = _self.getSectionHeight(section);
        var changedHeight = _self.setSectionHeight(section, currentValue);
        if(currentValue != changedHeight) {
            this.isilent(changedHeight)
        }
        if(currentHeight != changedHeight) {
            if(_self.selectedWidget) {
                _self.getWidgetObject(_self.selectedWidget).positionReferenceLine()
            }
            _self.setDirty("section_height", currentHeight, changedHeight);
        }
    });
    this.sectionWidth = _self.body.find("input.width").ichange(1000, function(event, currentValue) {
        var section = _self.activeSection;
        var currentWidth = _self.getSectionWidth(section);
        var changedWidth = _self.setSectionWidth(section, currentValue);
        if(currentValue != changedWidth) {
            this.isilent(changedWidth)
        }
        if(currentWidth != changedWidth) {
            if(_self.selectedWidget) {
                _self.getWidgetObject(_self.selectedWidget).positionReferenceLine()
            }
            _self.setDirty("section_width", currentWidth, changedWidth);
        }
    });
    this.widgetXoffset = _self.body.find("input.x-offset").stepper( "disable" );
    this.widgetYoffset = _self.body.find("input.y-offset").stepper( "disable" );
    this.widgetHeight = _self.body.find("input.widget-height").stepper( "disable" );
    this.widgetWidth = _self.body.find("input.widget-width").stepper( "disable" );
    this.body.find(".widget-block-panel").scrollbar();
    this.body.find(".spinner:not('.x-offset, .y-offset')").numeric();
    this.body.find(".spinner.x-offset, .spinner.y-offset").signed_numeric();
    this.iframe.on("sure-load", $.proxy(this.afterLoad, this));         // sure-load triggered from iframe content
    this.leftbar_tab = this.body.find(".left-bar").tabify({
        activate: function(hash) {
            if(hash.newIndex == "setting") {
                var current = _self.settingBlock.find(".leftbar-accordion").accordion("current");
                if(current.is(".page-props")) {
                    _self.updatePageProperties()
                } else if(current.is(".widget-mode")) {
                    _self.updateWidgetConfig()
                } else if(current.is(".dock-css")) {
                    _self.editDockCss()
                }
            }
        }
    });
    this.isResponsive = this.iframe.parent().is(".responsive");
    this.initSidebar();
    this.attachEvent();
    this.initWidgetDimensionSpinners();
};

_e.action_menu_entries = [
    {
        text: $.i18n.prop("revert"),
        ui_class: "revert",
        action: "revert"
    },
    {
        ui_class: "item-separator"
    },
    {
        text: $.i18n.prop("clear.all.widgets"),
        ui_class: "clear-all",
        action: "clear-all"
    },
    {
        text: $.i18n.prop("copy.header"),
        ui_class: "copy copy-header",
        action: "copy-header"
    },
    {
        text: $.i18n.prop("copy.footer"),
        ui_class: "copy copy-footer",
        action: "copy-footer"
    }
];

_e.onActionMenuOpen = function() {
    var acMenu = this.action_menu;
    if(this.enableRevert) {
        acMenu.enable("revert");
    } else {
        acMenu.disable("revert");
    }
    if(this.activeSection.is(".header")) {
        acMenu.show("copy-header");
    } else {
        acMenu.hide("copy-header");
    }
    if(this.activeSection.is(".footer")) {
        acMenu.show("copy-footer");
    } else {
        acMenu.hide("copy-footer");
    }
};

_e.onActionMenuClick = function(action) {
    var _self = this;
    switch (action) {
        case "revert":
            _self.revert();
            break;
        case "clear-all":
            _self.removeAllWidgets(_self.activeSection);
            break;
        case "copy-header":
            _self.renderCopiedSection("header");
            break;
        case "copy-footer":
            _self.renderCopiedSection("footer");
            break;
    }
};

_e.getSectionHeight = function(section) {
    var height;
    if(section.hasClass("dockable")) {
        var uuid = section.attr("id").substring(5);
        var css = this.cachedDock[uuid].css;
        height = this.getCssValue(css, "#dock-" + uuid, "height")
    } else {
        height = this.getCssValue(this.css, "." + section.attr("section") + " > .widget-container", "height")
    }
    return parseFloat(height, 10)
};

_e.getSectionWidth = function(section) {
    var width;
    if(section.hasClass("dockable")) {
        var uuid = section.attr("id").substring(5);
        var css = this.cachedDock[uuid].css;
        width = this.getCssValue(css, "#dock-" + uuid, "width")
    } else {
        width = this.getCssValue(this.css, "." + section.attr("section") + " > .widget-container", "width")
    }
    return width == "auto" ? "" : parseFloat(width, 10)
};

_e.setSectionHeight = function(section, height) {
    var minHeight = this.getSectionMinHeight(section);
    if (minHeight && +height < minHeight) {
        height = minHeight
    }
    if(section.hasClass("dockable")) {
        var uuid = section.attr("id").substring(5);
        var css = this.cachedDock[uuid].css;
        this.updateCss(css, uuid, "#dock-" + uuid, {height: height + "px"})
    } else {
        this.updateCss(this.css, null, "." + section.attr("section") + " > .widget-container", {height: height + "px"})
    }
    return height
};

_e.setSectionWidth = function(section, width) { //it should not make editor dirty
    if(width) {
        var minWidth = section.is(".body") ? this.getGridMinResizable(section.find("> .widget-container")) : this.getSectionMinWidth(section);
        minWidth = Math.max(800, minWidth);
        if (+width < minWidth) {
            width = minWidth
        }
        if(section.is(".dockable")) {
            var uuid = section.attr("id").substring(5);
            var css = this.cachedDock[uuid].css;
            this.updateCss(css, uuid, "#dock-" + uuid, {width: width + "px"})
        } else {
            this.updateCss(this.css, null, "." + section.attr("section") + " > .widget-container", {width: width + "px"});
            if(section.is(".body")) {
                this.updateCss(this.css, null, ".body > .body-section > .widget-container", {width: width + "px"})
            }
        }
    } else {
        width = "auto";
        if(section.hasClass("dockable")) {
            var uuid = section.attr("id").substring(5);
            this.updateCss(this.cachedDock[uuid].css, uuid, "#dock-" + uuid, {width: width})
        } else {
            this.updateCss(this.css, null, "." + section.attr("section") + " > .widget-container", {width: width});
            if(section.is(".body")) {
                this.updateCss(this.css, null, ".body > .body-section > .widget-container", {width: width})
            }
        }
    }
    if(!section.is(".body")) {
        if(this.selectedWidget) {
            var obj = this.getWidgetObject(this.selectedWidget);
            obj.positionReferenceLine();
            this.setDimensionSpinnerValues(obj)
        }
    }
    return width == "auto" ? "" : width
};

_e.assignColorToGroups = function(group) {
    (group || this.activeSection.find(".widget-group")).each(function() {
        var uniColorR = Math.floor(Math.random() * 210 + 20);
        var uniColorG = Math.floor(Math.random() * 210 + 20);
        var uniColorB = Math.floor(Math.random() * 210 + 20);
        $(this).find(".widget-overlay").css("border-color", "rgba(" + uniColorR + "," + uniColorG + "," + uniColorB + ",1)")
    })
};

_e.detachSoratableElementEvents = function(sortable) {
    sortable.off("sortsort").off("click")
};

_e.attachSoratableElementEvents = function(section, sortable) {
    var _self = this;
    sortable.on("sort:sort", function(ev, data) {
        _self.setDirty("widget_relocate", $.extend(data.obj.sort_old_state, {widget: data.elm}), {
            parent: data.elm.parent(),
            index: data.elm.parent().children("." + _self.widgetClass).index(data.elm),
            widget: data.elm
        });
    }).on("click", function() {
        _self.selectGrid($(this))
    });
    if(!this.sortable_page_content) {
        section.find(".page-content").parent().click(function() {
            _self.selectGrid($(this))
        })
    }
};

_e.selectGrid = function(grid, updateSelector) {
    this.deselectGrid();
    grid.find("> .editable-area-overlay, >.page-content").addClass("selected");
    this.settingBlock.find(".layout-operators .tool-icon").removeClass("disabled");
    if(grid.is(".widget-container")) {
        this.settingBlock.find(".layout-operators .h-f-c").addClass("disabled")
    } else if(grid.is(".v-split")) {
        this.settingBlock.find(".layout-operators .h-f-v").addClass("disabled")
    }
    if(grid.is(".page-content > .v-split-container > .v-split")) {
        this.settingBlock.find(".layout-operators .fluid-v-split").removeClass("disabled")
    }
    if(updateSelector !== false) {
        var selector;
        if(grid.is(".page-content")) {
            selector = ".page-content"
        } else if(grid.is(".widget-container")) {
            if(grid.parent().is(".page-content")) {
                selector = ".page-content > .widget-container"
            } else if(grid.parent().is(".body-section")) {
                selector = "#" + grid.parent().attr("id") + " > .widget-container"
            } else {
                selector = "> .widget-container"
            }
        } else {
            selector = "#" + grid.attr("id")
        }
        this.settingBlock.find(".body-grids-selector select").chosen("val", selector)
    }
};

_e.clearEditableOverlay = function() {
    this.activeSection.find(".editable-area-overlay").remove()
};

_e.setEditableOverlay = function(section) {
    (section || this.activeSection).find(this.leafGridBlockSelectorInContentMode).append("<div class='editable-area-overlay'></div>")
};

_e.editableWidgetsInSection = function(section) {
    return section.find("." + this.widgetClass)
};

_e.initializeSectionForEditing = function(section) {
    var _self = this;
    this.updateSectionDimensionSpinners();
    this.populateWidgetSelector();
    this.setEditableOverlay();
    var widgets = this.editableWidgetsInSection(section);
    this.createWidgetOverlay(widgets);
    section.on("click", ".widget-overlay", function(ev) {
        if(ev.target != this) {
            return;
        }
        var widget = $(this).parent();
        if(_self.selectedWidget && _self.selectedWidget[0] == widget[0]) {
            return;
        }
        if(_self.multiSelectedWidget) {
            if(_self.multiSelectedWidget.filter(widget).length) {
                if(ev.ctrlKey) {
                    _self.unselectMultiWidget(widget)
                } else {
                    _self.activeSection.find(".multi-selection-master-widget").removeClass("multi-selection-master-widget");
                    widget.find("> .multi-selection-overlay").addClass("multi-selection-master-widget")
                }
                return;
            } else if(ev.ctrlKey) {
                _self.selectMultiWidget(widget);
                return;
            }
        }
        _self.selectWidget(widget);
    }).on("click", ".widget-overlay .remove", function() {
        var widget = $(this).closest("." + _self.widgetClass);
        _self.removeWidget(_self.activeSection, widget.attr("id").substring(3))
    });

    if(section.is(".body")) {
        section.find(".body-section").append("<div class='body-section-overlay'></div>");
        this.settingBlock.find(".tool-icon.add-section").removeClass("disabled");
        section.on("click", ".body-section", function() {
            _self.selectBodySection($(this))
        });
        this.initializeBodySectionSortResize(section);

        this.settingBlock.find(".layout-mode-header, .layout-mode").xshow();
        this.populateGridBlockSelector();
        this.populateBodySectionSelector()
    } else {
        section.on("click", ".widget-bounding-box", function(event) {
            var _this = $(this);
            var widget = _this.closest("." + _self.widgetClass);
            var widgetOverlay = widget.children(".widget-overlay");
            var wiElement = _self.getWidgetObject(widget);
            var selected = widgetOverlay.find(".widget-bounding-box.selected");
            if(_this.is(".selected") && selected.length == 1) {
                return;
            }
            var widgetPosition = widget.position();
            var height = widget.outerHeight(true);
            var width = widget.outerWidth(true);
            var totalWidth = $(wiElement.elm[0].offsetParent).innerWidth();
            var totalHeight = $(wiElement.elm[0].offsetParent).innerHeight();
            var newStyle = {
                left: "auto",
                top: "auto",
                right: "auto",
                bottom: "auto",
                width: wiElement.style.width,
                height: wiElement.style.height
            };
            var ctrl = false;
            if(event.ctrlKey) {
                ctrl = true;
                if(wiElement.isLeftTop() && _this.hasClass("top-right")) {
                    newStyle.left = wiElement.style.left;
                } else if(wiElement.isRightTop() && _this.hasClass("top-left")) {
                    newStyle.right = wiElement.style.right;
                } else if(wiElement.isLeftBottom() && _this.hasClass("bottom-right")) {
                    newStyle.left = wiElement.style.left;
                } else if(wiElement.isRightBottom() && _this.hasClass("bottom-left")) {
                    newStyle.right = wiElement.style.right;
                } else {
                    ctrl = false; // diagonal click
                }
            }
            if(ctrl) {
                newStyle.width = "auto"
            } else {
                if(newStyle.width == "auto") {
                    newStyle.width = widget.width() + "px";
                }
                selected.removeClass("selected");
            }
            _this.addClass("selected");
            if(_this.hasClass("top-left")) {
                newStyle.left = widgetPosition.left + "px";
                newStyle.top = widgetPosition.top + "px";
            } else if(_this.hasClass("top-right")) {
                newStyle.top = widgetPosition.top + "px";
                newStyle.right = (totalWidth - widgetPosition.left - width) + "px";
            } else if(_this.hasClass("bottom-right")) {
                newStyle.right = (totalWidth - widgetPosition.left - width) + "px";
                newStyle.bottom = (totalHeight - widgetPosition.top - height) + "px";
            } else if(_this.hasClass("bottom-left")) {
                newStyle.left = widgetPosition.left + "px";
                newStyle.bottom = (totalHeight - widgetPosition.top - height) + "px";
            }
            var old_style = wiElement.style;
            if(wiElement.percentage) {
                var total_w = wiElement.elm.parent().innerWidth();
                var total_h = wiElement.elm.parent().innerHeight();
                if(_this.hasClass("top-left") || _this.hasClass("bottom-left")) {
                    newStyle.left = parseFloat(newStyle.left) / total_w * 100 + "%"
                }
                if(_this.hasClass("top-right") || _this.hasClass("bottom-right")) {
                    newStyle.right = parseFloat(newStyle.right) / total_w * 100 + "%"
                }
                if(_this.hasClass("top-left") || _this.hasClass("top-right")) {
                    newStyle.top = parseFloat(newStyle.top) / total_h * 100 + "%"
                }
                if(_this.hasClass("bottom-right") || _this.hasClass("bottom-left")) {
                    newStyle.bottom = parseFloat(newStyle.bottom) / total_h * 100 + "%"
                }
            }
            wiElement.applyStyle(newStyle);
            _self.setDirty("widget_reposition", {widget: widget, style: old_style}, {widget: widget, style: newStyle}, widget);
        });

        this.body.find(".widget-item").draggable("dropTarget", {area: this.iframe, element: section.find(".widget-container:first"), offset: this.iframe.offset(), intersect: 62}).on("drag:drop", function(ev, data) {
            var droparea_height = data.drop.height();
            var initial_width = droparea_height > 150 ? 150 : (droparea_height > 100 ? 100 : 50);
            var widgetType = data.drag.attr("widget-type");
            var wiElement = _self.getNewDroppedWidget(widgetType);
            var widget = wiElement.elm;
            data.drag.replaceWith(widget);
            var left = data.position.left + ( _self.iframeWindow.scrollX || _self.iframeWindow.pageXOffset);
            var top = data.position.top + (_self.iframeWindow.scrollY || _self.iframeWindow.pageYOffset);
            if(left < 0) {
                left = 0
            }
            if(top < 0) {
                top = 0
            }
            if(left + initial_width > data.drop.width()) {
                left -= left + initial_width - data.drop.width()
            }
            if(top + initial_width > droparea_height) {
                top -= top + initial_width - droparea_height
            }
            wiElement.style = {
                width: initial_width + "px",
                height: initial_width + "px",
                top: top + "px",
                left: left + "px",
                right: "auto",
                bottom: "auto"
            };
            var css = wiElement.css;
            _self.updateCss(css, wiElement.uuid, "#wi-" + wiElement.uuid, wiElement.style, undefined, undefined, true);
            _self.cachedWidgets[wiElement.uuid] = wiElement;
            _self.onNewWidgetDrop(widget)
        });

        this.assignColorToGroups();

        if(section.is(".dockable")) {
            section.find(".dock-mask").mousedown(function() {
                _self.unselectWidget()
            })
        }
    }
};

_e.selectBodySection = function(section, preventRecursion) {
    if(section.find(".body-section-overlay.selected").length) {
        return;
    }
    this.activeSection.find('.body-section-overlay.selected').removeClass("selected");
    section.find(".body-section-overlay").addClass("selected");
    this.settingBlock.find(".tool-icon.remove-section, .tool-icon.move-section-up, .tool-icon.move-section-down, .body-section-fluid-fixed-toggle").removeClass("disabled");
    if(!preventRecursion) {
        this.settingBlock.find(".body-sections-selector select").chosen("val", section.attr("id"))
    }
};

_e.initializeBodySectionSortResize = function(section) {
    var _self = this;
    var widgetttableGrids = section.find(this.leafGridBlockSelectorInContentMode);
    this.grid_sortable = new bm.Sortable(widgetttableGrids, {
        shim: true,
        handle: "." + this.widgetClass,
        sortable_on_drop: false,
        helper: function(drag) {
            var type = drag.attr("widget-type");
            return _self.iframeWindow.$(_self.body.find(".widget-item[widget-type='" + type + "']").clone()[0])
        },
        start: function(data) {
            data.obj.sort_old_state = {
                parent: data.elm.parent(),
                index: data.elm.parent().children("." + _self.widgetClass).index(data.elm)
            }
        }
    });
    this.attachSoratableElementEvents(section, widgetttableGrids);
    this.body.find(".widget-item").draggable("sortElement", this.grid_sortable, this.iframe.offset(), this.iframe).on("drag:sort", function(ev, data) {
        var widgetType = data.sort.attr("widget-type");
        var wiElement = _self.getNewDroppedWidget(widgetType);
        var widget = wiElement.elm;
        data.sort.replaceWith(widget);
        _self.cachedWidgets[wiElement.uuid] = wiElement;
        _self.onNewWidgetDrop(widget);
        _self.grid_sortable.addHandle(widget)
    });
    this.addGridResize(section.find(this.editableBlockSelector))
};

_e.addGridResize = function(grids) {
    var _self = this;
    var no_fixed_total;
    grids.filter(".no-fixed-left").resizable({
        direction: ["r"],
        reverse: function(element) {
            return element.siblings('.grid-block')
        },
        resize: function(hash) {
            hash.elm.siblings(".grid-block").addBack().find(".editable-area-overlay, .page-content").each(function() {
                var width = $(this).innerWidth();
                $(this).find(".width-info").text((width / no_fixed_total * 100).toFixed(2))
            })
        },
        stop: function(hash) {
            var total = hash.elm.parent().width();
            var x_uuid = hash.elm.attr("id").substring(6);
            var x_width = hash.elm.width() / total * 100;
            var other_grid = hash.elm.siblings('.grid-block');
            var y_uuid = other_grid.attr("id").substring(6);
            var y_width = other_grid.width() / total * 100;
            var redo_op = function() {
                _self.updateCss(_self.css, null, "#spltr-" + x_uuid, {width: x_width + "%"});
                _self.updateCss(_self.css, null, "#spltr-" + y_uuid, {width: y_width + "%"})
            };
            _self.setDirty({redo: redo_op, undo: this.undo_op});
            redo_op();
            delete this.undo_op;
            hash.elm.css({width: ""});
            hash.elm.siblings().css({width: ""});
            hash.elm.siblings(".grid-block").addBack().find(".width-info").remove()
        },
        start: function(hash) {
            var total = hash.elm.parent().width();
            hash.obj.limit = [_self.getGridMinResizable(hash.elm), total - _self.getGridMinResizable(hash.elm.siblings(".grid-block"))];
            var x_uuid = hash.elm.attr("id").substring(6);
            var x_width = _self.getCssValue(_self.css, "#spltr-" + x_uuid, "width");
            var y_uuid = hash.elm.siblings().attr("id").substring(6);
            var y_width = _self.getCssValue(_self.css, "#spltr-" + y_uuid, "width");
            this.undo_op = function() {
                _self.updateCss(_self.css, null, "#spltr-" + x_uuid, {width: x_width});
                _self.updateCss(_self.css, null, "#spltr-" + y_uuid, {width: y_width})
            };
            no_fixed_total = hash.elm.parent().width();
            hash.elm.siblings(".grid-block").addBack().find(".editable-area-overlay, .page-content").each(function() {
                $(this).append("<span class='width-info'></span>")
            })
        },
        prestart: function(hash) {
            var width = hash.elm.css("width");
            hash.elm.css("width", width);
            var total = hash.elm.parent().width();
            hash.elm.siblings(".grid-block").css("width", total - parseFloat(width))
        }
    });
    var start = function(hash) {
        this.undo_width = hash.elm.width();
        var totalWidth = hash.elm.parent().width();
        hash.obj.limit[0] = _self.getGridMinResizable(hash.elm);
        hash.obj.limit[1] = totalWidth - _self.getGridMinResizable(hash.elm.siblings(".grid-block"));
        hash.elm.siblings(".grid-block").addBack().find(".editable-area-overlay, .page-content").each(function() {
            $(this).append("<span class='width-info'></span>")
        })
    };
    var resize = function(hash) {
        var width = hash.dims.width;
        hash.elm.css({
            "max-width": width + "px",
            flex: "0 0 " + width + "px"
        });
        hash.elm.siblings(".grid-block").addBack().find(".editable-area-overlay, .page-content").each(function() {
            $(this).find(".width-info").text($(this).parent().outerWidth())
        })
    };
    var stop = function(hash) {
        var elm = hash.elm;
        var x_uuid = elm.attr("id").substring(6);
        var redo_width = elm.width();
        var redo_op = function() {
            var width = redo_width;
            if(elm.css("box-sizing") == "border-box") {
                width -= elm.leftRib(false) + elm.rightRib(false)
            }
            _self.updateCss(_self.css, null, "#spltr-" + x_uuid, {
                width: width + "px",
                "max-width": width + "px",
                flex: "0 0 " + width + "px"
            })
        };
        var undo_width = this.undo_width;
        var undo_op = function() {
            var width = undo_width;
            if(elm.css("box-sizing") == "border-box") {
                width -= elm.leftRib(false) + elm.rightRib(false)
            }
            _self.updateCss(_self.css, null, "#spltr-" + x_uuid, {
                width: width + "px",
                "max-width": width + "px",
                flex: "0 0 " + width + "px"
            })
        };
        _self.setDirty({redo: redo_op, undo: undo_op});
        redo_op();
        delete this.undo_width;
        elm.css({
            width: "",
            maxWidth: "",
            flex: ""
        });
        hash.elm.siblings(".grid-block").addBack().find(".width-info").remove()
    };
    grids.filter(".l-fixed-left").resizable({
        direction: ["r"],
        resize: resize,
        start: start,
        stop: stop,
        limit: [100, undefined]
    });
    grids.filter(".r-fixed-right").resizable({
        direction: ["l"],
        resize: resize,
        start: start,
        stop: stop,
        limit: [100, undefined]
    })
};

_e.attachGlobalEvents = function() {
    var _self = this;
    this.pageBody.find(".iframe-mask, .section-overlay").mousedown(function() {
        _self.unselectWidget()
    });
    this.pageBody.find("[section]").mousedown(function(ev) {
        if($(ev.target).is(".editable-area-overlay")) {
            _self.unselectWidget()
        }
    });
    //for undo redo hot key
    $(this.iframeWindow.document).on("keyup." + this.id + ".ctrl_z", function(ev) {
        $(document).trigger(ev)
    }).on("keyup." + this.id + ".ctrl_y", function() {
        $(document).trigger(ev)
    })
};

_e.afterLoad = function() {
    var section = this.initial_section || "header";
    this.iframeContent = this.iframe.contents();
    this.iframeWindow = this.iframe[0].contentWindow;
    this.iframeMask = $("<div class='iframe-mask editor-with-solid-grid'></div>");
    this.pageBody = this.iframeContent.find("body");
    if(!this.reload_performed) {
        this.body.find(".widget-item").draggable({
            helper: "clone",
            shim: true
        });
    }
    this.pageBody.append("<div class='bounding-box-line-x'></div><div class='bounding-box-line-y'></div><div class='bounding-box-line-x-2'></div><div class='bounding-box-line-y-2'></div>");
    this.pageBody.append(this.iframeMask);
    this.loadCsss();
    if(this.activeSectionSelector.length) {
        var options = this.activeSectionSelector[0].options;
        var docks = this.pageBody.find(".dockable");
        docks.each(function() {
            var uuid = $(this).attr("id").substring(5);
            options[options.length] = new Option("Dock - " + uuid, uuid, false, section == uuid)
        });
        if(docks.length) {
            this.activeSectionSelector.chosen("update")
        }
        this.activeSectionSelector.chosen('val', section)
    }
    if(this.initEditor) {
        this.initEditor()
    }
    this.changeActiveSection(section, true);
    if(this.isResponsive) {
        this.resolutionChange(this.settingBlock.find(".change-resolution select").val());
    }
    this.unmask();
    this.appendSectionOverlays();
    this.attachGlobalEvents()
};

_e.appendSectionOverlays = function() {
    this.pageBody.find(".header, .footer, .body").append("<div class='section-overlay'></div>")
};

_e.getSectionMinWidth = function(section) {
    var _self = this;
    var widgets = section.find("." + _self.widgetClass).filter(":visible");
    var max = 0;
    $.each(widgets, function() {
        var wiElement = _self.getWidgetObject($(this));
        var temp = _self.findMinimumWidth(wiElement);
        if(max < temp) {
            max = temp;
        }
    });
    return max;
};

_e.findMinimumWidth = function(wiElement) {
    var pos = wiElement.getPos();
    var width = wiElement.elm.outerWidth(true);
    var temp ;
    if(!wiElement.isLeft()) {
        temp = +pos.right + width;
    } else if(!wiElement.isRight()) {
        temp = +pos.left + width;
    } else {
        temp = +pos.right + +pos.left + 50
    }
    return temp
};

_e.getSectionMinHeight = function(section) {
    var _self = this;
    var widgets = section.find("." + _self.widgetClass).filter(":visible");
    var max = 0;
    $.each(widgets, function() {
        var _this = $(this);
        var wiElement = _self.getWidgetObject(_this);
        var pos = wiElement.getPos();
        var height = _this.outerHeight(true);
        var temp ;
        if(wiElement.isTop()) {
            temp = pos.top + height;
        } else {
            temp = pos.bottom + height;
        }
        if(max < temp) {
            max = temp;
        }
    });
    return (max || (widgets.length ? 60 : 0));
};

_e.addCommonOverlays = function() {
    this.pageBody.find("> .header").append("<div class='section-overlay'></div>");
    this.pageBody.find("> .body").append("<div class='section-overlay'></div>");
    this.pageBody.find("> .footer").append("<div class='section-overlay'></div>");
};

_e.loadCsss; //abstract method

_e.deActiveSection = function(section) {
    section.off();
    section.removeClass("active-section");
    this.unselectWidget();
    section.find(".widget-overlay, .editable-area-overlay, .body-section-overlay, .bmui-resize-handle").remove();
    if(section.is(".dockable")) {
        section.hide();
        var current = this.settingBlock.find(".leftbar-accordion").accordion("current");
        if(current.is(".dock-css")) {
            if(this.selectedWidget) {
                this.settingBlock.find(".leftbar-accordion").accordion("expand", "widget-mode-header")
            } else {
                this.settingBlock.find(".leftbar-accordion").accordion("expand", "page-props-header")
            }
        }
        this.settingBlock.find(".dock-css-header").hide()
    }
    if(section.hasClass("body")) {
        this.deselectBodySection();
        this.settingBlock.find(".tool-icon.add-section").addClass("disabled");
        this._destroyBodySortResize(section);
        this.deselectGrid();
        section.find(".page-content").off();
        var currentActive = this.settingBlock.find(".leftbar-accordion").accordion("current");
        if(currentActive.is(".layout-mode")) {
            this.settingBlock.find(".leftbar-accordion").accordion("expand", 0)
        }
        this.settingBlock.find(".layout-mode-header, .layout-mode").hide()
    } else {
        this.body.find(".widget-item").draggable("dropTarget", undefined).off("dragdrop");
        this.destroyMultiSelection()
    }
};

_e._destroyBodySortResize = function(section) {
    this.body.find(".widget-item").draggable("sortElement", undefined).off("dragsort");
    var grids = section.find(this.leafGridBlockSelectorInContentMode).off();
    section.find(".widget-container").off();
    this.grid_sortable.destroy();
    if(grids.filter(".l-fixed-left").__proto__.hasOwnProperty("destroy")) {
        grids.filter(".l-fixed-left").resizable("destroy");
    }
    if(grids.filter(".no-fixed-left").__proto__.hasOwnProperty("destroy")) {
        grids.filter(".no-fixed-left").resizable("destroy");
    }
    if(grids.filter(".r-fixed-right").__proto__.hasOwnProperty("destroy")) {
        grids.filter(".r-fixed-right").resizable("destroy")
    }
};

_e.deselectBodySection = function() {
    this.activeSection.find(".body-section-overlay.selected").removeClass("selected");
    this.settingBlock.find(".tool-icon.remove-section").addClass("disabled");
    this.settingBlock.find(".tool-icon.move-section-up").addClass("disabled");
    this.settingBlock.find(".tool-icon.move-section-down").addClass("disabled");
    this.settingBlock.find(".body-sections-selector select").chosen("val", '');
    this.settingBlock.find(".tool-icon.body-section-fluid-fixed-toggle").addClass("disabled")
};

_e.changeActiveSection = function(section, preventRecursion, force) {
    if(section instanceof String || typeof section == "string") {
        section = this.pageBody.find(section.length > 6 ? "> #dock-" + section : "> ." + section).addClass("active-section");
    }
    if(this.activeSection) {
        if(this.activeSection[0] == section[0] && !force) {
            return;
        }
        this.deActiveSection(this.activeSection)
    }
    var old_active = this.activeSection;
    var removeDockDom = this.settingBlock.find(".tool-icon.remove-dock");
    this.activeSection = section;
    if(section.is(".dockable")) {
        section.show();
        removeDockDom.removeClass("disabled");
        if(!this.cachedDock[section]) {
            this.cacheDock(section.attr("id").substring(5));
        }
        this.settingBlock.find(".dock-css-header").show();
        this.editDockCss(true)
    } else {
        removeDockDom.addClass("disabled");
    }
    this.activeSection.scrollHere();
    this.activeSection.addClass("active-section");
    this.initializeSectionForEditing(this.activeSection);
    if(this.activeSection.hasClass("body")) {
        if(this.sectionHeight.length) {
            this.sectionHeight.stepper("disable")[0].isilent("");
        }
    } else {
        this.initWidgetMultiSelection();
        this.sectionHeight.stepper("enable")
    }
    if(this.onSectionChange) {
        this.onSectionChange(old_active, section)
    }
    if(!preventRecursion) {
        var val = section.is(".dockable") ? section.attr("id").substring(5) : section.attr("section");
        this.activeSectionSelector.chosen('val', val)
    }
};

_e.updatePageProperties = function(tab) {
    var container = this.settingBlock.find(".page-props .active-prop-view-container");
    container.empty();
    if(this.leftbar_tab.tabify("getActive") == "widget") {
        return
    }
    var current = this.settingBlock.find(".leftbar-accordion").accordion("current");
    if(!current.is(".page-props")) {
        return;
    }
    if(tab) {
        if(this.settingBlock.active_page_config_tab != tab) {
            return;
        }
    }
    var _self = this;
    if(!this.settingBlock.active_page_config_tab) {
        this.settingBlock.active_page_config_tab = "css";
        this.settingBlock.find(".page-props .tablike-button.css").addClass("active")
    }
    switch(this.settingBlock.active_page_config_tab) {
        case "css":
            if(this.isResponsive) {
                var operators = '<div class="sidebar-group page-css-operators">';
                if(this.activeMedia) {
                    operators += '<span class="tool-icon copy-other-media" title="' + $.i18n.prop("copy.all.from.other.media") + '"></span>'
                }
                operators += '<span class="tool-icon remove remove-media" title="' + $.i18n.prop("remove.media") + '"></span></div>';
                container.append(operators)
            }
            this._handleCssMediaTools(_self, container);
            _self.editCss(container, _self, "", undefined, [".body > .body-section > .widget-container", ".body > .widget-container", ".header > .widget-container, .footer > .widget-container"]);
            break;
        case "js":
            var data = {containerId: _self.containerId, containerJs: _self.js};
            bm.ajax({
                url: _self.editContainerJsUrl,
                data: data,
                dataType: "html",
                success: function(resp) {
                    resp = $(resp);
                    container.append(resp);
                    resp.find("textarea").change(function() {
                        var js = this.value;
                        _self.setDirty("js", {js: _self.js}, {js: js});
                        _self.js = js
                    })
                }
            })
    }
};

_e.updateWidgetConfig = function(uuid, tab, _switch) {
    var _self = this;
    var active_tab_content_container = this.settingBlock.find(".widget-prop-configure .active-prop-view-container");

    if(!this.selectedWidget) {
        return;
    } else {
        active_tab_content_container.empty();
    }
    if(this.leftbar_tab.tabify("getActive") == "widget") {
        if(!_switch) {
            return
        }
        this.leftbar_tab.tabify("activate", "setting", true)
    }
    var current = this.settingBlock.find(".leftbar-accordion").accordion("current");
    if(!current.is(".widget-mode")) {
        if(!_switch) {
            return;
        }
        this.settingBlock.find(".leftbar-accordion").accordion("expand", "widget-mode-header")
    }
    if(tab) {
        if(this.settingBlock.active_config_tab != tab) {
            return;
        }
        this.settingBlock.active_config_tab = tab
    } else if(!this.settingBlock.active_config_tab) {
        this.settingBlock.active_config_tab = tab = "basic";
        this.settingBlock.find(".widget-prop-configure .tablike-button.basic").addClass("active")
    }
    if(!uuid) {
        uuid = this.selectedWidget.attr("id").substring(3)
    }
    switch(this.settingBlock.active_config_tab) {
        case "basic":
            var widget = _self.selectedWidget;
            var selectedWidgetUuid = widget.attr("id").substring(3);
            var wiObj = _self.getWidgetObject(widget);
            var widgetId = widget.attr("widget-id");
            var data = {};
            if (widgetId) {
                data.widgetId = widgetId;
            }
            var cache = widget.data("data-cache");
            if (cache) {
                data.cache = cache;
            }
            var type = widget.attr("widget-type");
            var isIsTemplateWidget = widget.attr("external-widget") == "true";
            if(isIsTemplateWidget) {
                data.isTemplateWidget = isIsTemplateWidget;
                data.type = type;
                data.uuid = selectedWidgetUuid;
            }
            if (widgetId || cache) {
                bm.ajax({
                    controller: "widget",
                    action: !app.widget[type] || !app.widget[type].no_config ? type + "ShortConfig" : "bareShortConfig",
                    data: data,
                    dataType: "html",
                    success: function (resp) {
                        resp = $(resp);
                        active_tab_content_container.append(resp).updateUi();
                        var widgetSpecificConfig = resp.filter(".widget-specific-config");
                        bm.autoToggle(widgetSpecificConfig).attachValidator().find("input:checkbox[name][uncheck-value]").pairuncheck();
                        active_tab_content_container.find(".advance-config-btn input").click(function () {
                            _self.advanceConfigureWidget()
                        });
                        active_tab_content_container.find(".title-input input").change(function () {
                            wiObj.afterConfigChange();
                            var title = widget.find(">.widget-title");
                            if (!title.length) {
                                title = $("<div class=\"widget-title\"></div>").prependTo(widget)
                            } else if (!this.value) {
                                title.remove()
                            }
                            title.text(this.value);

                            var oldCache = widget.data("data-cache");
                            if (!oldCache) {
                                oldCache = resp.filter(".widget-cache").val()
                            }
                            var currentCache = JSON.parse(oldCache);
                            currentCache.title = this.value;
                            widget.data("data-cache", currentCache = JSON.stringify(currentCache));
                            _self.setDirty("widget_update", {
                                widget: widget,
                                cache: oldCache,
                                obj: wiObj
                            }, {widget: widget, cache: currentCache, obj: wiObj}, widget);

                            if(!_self.selectedWidget) {
                                active_tab_content_container.empty();
                            }
                        });
                        active_tab_content_container.find(".clazz-input input").change(function () {
                            wiObj.afterConfigChange();
                            var clazz = this.value;
                            widget.attr("class", "widget widget-" + type + " " + clazz.trim());
                            var oldCache = widget.data("data-cache");
                            if (!oldCache) {
                                oldCache = resp.filter(".widget-cache").val()
                            }
                            var currentCache = JSON.parse(oldCache);
                            currentCache.clazz = clazz;
                            widget.data("data-cache", currentCache = JSON.stringify(currentCache));
                            _self.setDirty("widget_update", {
                                widget: widget,
                                cache: oldCache,
                                obj: wiObj
                            }, {widget: widget, cache: currentCache, obj: wiObj}, widget);

                            if(!_self.selectedWidget) {
                                active_tab_content_container.empty();
                            }
                        });
                        active_tab_content_container.find(".widget-specific-config").change(function (ev) {
                            wiObj.afterConfigChange();
                            if (ev.ignore || (ev.originalEvent && ev.originalEvent.ignore)) {
                                return;
                            }
                            if (!widgetSpecificConfig.valid()) {
                                return;
                            }
                            var paramObj = $(this).serializeObject();
                            var oldCache = widget.data("data-cache");
                            if (!oldCache) {
                                oldCache = resp.filter(".widget-cache").val()
                            }
                            var appWidgetObj = app.widget[type];
                            if(!appWidgetObj) {
                                appWidgetObj = app.widget.base
                            }
                            var currentCache =  appWidgetObj.prototype.updateCacheForShortConfig(oldCache, paramObj, null, null, $(this), _self);
                            widget.data("data-cache", currentCache);
                            _self.setDirty("widget_update", {
                                widget: widget,
                                cache: oldCache,
                                obj: wiObj
                            }, {widget: widget, cache: currentCache, obj: wiObj}, widget);
                            wiObj.render({
                                widgetId: widgetId,
                                cache: currentCache,
                                type: type
                            })
                        });
                        if (app.widget[type] && app.widget[type].initShortConfig) {
                            app.widget[type].initShortConfig(active_tab_content_container, widget, _self)
                        }
                        if(!_self.selectedWidget) {
                            active_tab_content_container.empty();
                        }
                    },
                    error: function(xhr, status, resp) {
                        active_tab_content_container.append(resp)
                    }
                })
            }
            break;
        case "css":
            _self.editWidgetCss(_self.getWidgetObject(_self.selectedWidget));
            break;
        case "js":
            var wiElement = _self.cachedWidgets[uuid];
            var data = {js: wiElement.js, type: _self.selectedWidget.attr("widget-type"), uuid: uuid, overwrite: typeof wiElement.js != "undefined"};
            bm.ajax({
                url: _self.editJsUrl,
                data: data,
                dataType: "html",
                success: function(resp) {
                    resp = $(resp);
                    active_tab_content_container.append(resp);
                    resp.find("textarea").change(function() {
                        wiElement.setJs(this.value);
                        if(!_self.selectedWidget) {
                            active_tab_content_container.empty();
                        }
                    })
                }
            })
    }
};

_e._splitGrid = function(containerClass, firstGridClass, secondGridClass, emptyGridIndex, noCss, addBlock, isPercent) {
    var selected = this.activeSection.find(".editable-area-overlay.selected, .page-content.selected").parent();
    var section = this.activeSection;
    var _self = this;
    var old_class = selected.attr("class");
    function starter() {
        if(selected.find(".editable-area-overlay.selected, .page-content.selected").length) {
            _self.deselectGrid()
        }
        _self.clearEditableOverlay();
        _self._destroyBodySortResize(section)
    }
    var undo = starter.blend(function() {
        selected.attr("class", old_class);
    });
    var redo = starter.blend(function() {
        selected.addClass(containerClass);
    });
    var innerHTML = selected.children().not(".bmui-resize-handle, .editable-area-overlay");
    var grDom;
    var rule1;
    var rule2;
    var attributes = {};
    var attributes2 = {};
    if(addBlock) {
        var uuid = bm.getUUID();
        grDom = $("<div class='grid-block " + secondGridClass + "' id='spltr-" + uuid + "'></div>");
        redo = redo.blend(function() {
            selected.after(grDom);
        });
        undo = undo.blend(function() {
            grDom.remove()
        })
    } else {
        var leftUUID = bm.getUUID();
        var rightUUID = bm.getUUID();
        grDom = $("<div class='" + firstGridClass + " grid-block' id='spltr-" + leftUUID + "'></div><div class='" + secondGridClass + " grid-block' id='spltr-" + rightUUID + "'></div>");
        rule1 = "#spltr-" + (emptyGridIndex ? rightUUID : leftUUID);
        rule2 = "#spltr-" + (emptyGridIndex ? leftUUID : rightUUID);
        if(!noCss) {
            if(isPercent) {
                attributes["width"] = 50 + "%";
                attributes2["width"] = "50%";
            } else {
                attributes["width"] = 150 + "px";
                attributes["max-width"] = 150 + "px";
                attributes["flex"] = "0 0 " + 150 + "px";
            }
            undo = undo.blend(function() {
                _self.removeRule(_self.css, null, rule1, true);
                _self.removeRule(_self.css, null, rule2, true)
            });
            redo = redo.blend(function() {
                _self.updateCss(_self.css, null, rule1, attributes, undefined, undefined, true);
                _self.updateCss(_self.css, null, rule2, attributes2, undefined, undefined, true)
            })
        }
        redo = redo.blend(function() {
            grDom.eq(emptyGridIndex ? 0 : 1).html(innerHTML);
            selected.html(grDom);
        });
        undo = undo.blend(function() {
            selected.html(innerHTML);
            grDom.remove()
        })
    }
    var finer = function() {
        _self.populateGridBlockSelector();
        _self.initializeBodySectionSortResize(section);
        _self.setEditableOverlay()
    };
    undo = undo.blend(finer);
    redo = redo.blend(finer);
    redo();
    if(_self.splitterDropped) {
        _self.splitterDropped(selected)
    }
    this.setDirty({undo: undo, redo: redo})
};

_e._attachSectionToolEvents = function() {
    var _self = this;
    this.settingBlock.find(".tool-icon.add-section").click(function() {
        if($(this).is(".disabled")) {
            return;
        }
        var sections = _self.pageBody.find(".body > .body-section");
        var section;
        var new_section = $("<div class='body-section'><div class='widget-container'></div><div class='body-section-overlay'></div></div>");
        _self.setEditableOverlay(new_section);
        if(sections.length) {
            var selected = sections.filter(":has(.body-section-overlay.selected)");
            if(!selected.length) {
                selected = sections.last()
            }
            section = new_section;
            var state = {
                redo: function() {
                    section.attr("id", "bs-" + bm.getUUID());
                    selected.after(section);
                    section.find('> .body-section-overlay.selected').removeClass("selected");
                    section.find('.editable-area-overlay.selected, .page-content.selected').removeClass("selected");
                    _self.attachSoratableElementEvents(_self.activeSection, section.find('> .widget-container'));
                    _self.grid_sortable.addElement(section.find('> .widget-container'))
                },
                undo: function() {
                    _self.detachSoratableElementEvents(section.find('> .widget-container'));
                    section.detach()
                }
            }
        } else {
            section = $("<div class='body-section'></div>");
            section.attr("id", "bs-" + bm.getUUID());
            var _section = new_section;
            _section.attr("id", "bs-" + bm.getUUID());
            var state = {
                redo: function() {
                    section = _self.pageBody.find(".body > .widget-container").wrap(section).parent();
                    section.append("<div class='body-section-overlay'></div>");
                    section.after(_section);
                    _self.grid_sortable.addElement(_section.find('> .widget-container'));
                    _self.attachSoratableElementEvents(new_section, new_section.find('> .widget-container'))
                },
                undo: function() {
                    section.find(".body-section-overlay").remove();
                    section.find("> .widget-container").unwrap();
                    _section.remove()
                }
            }
        }
        function finer() {
            _self.populateBodySectionSelector()
        }
        state.redo = state.redo.blend(finer);
        state.undo = state.undo.blend(finer);
        _self.setDirty(state);
        state.redo();
        new_section.scrollHere()
    });
    this.settingBlock.find(".tool-icon.remove-section").click(function() {
        if($(this).is(".disabled")) {
            return;
        }
        var section = _self.pageBody.find(".body > .body-section:has('> .body-section-overlay.selected')");
        var unwrapped_section;
        var index = section.index();
        if(section.find('.page-content').length) {
            bm.notify($.i18n.prop("section.with.page.content.not.remove"), "alert");
            return;
        }

        _self.deselectBodySection();

        function redoHandler() {
            if(section.has("> .body-section-overlay.selected").length) {
                _self.deselectBodySection()
            }
            if(section.find(".editable-area-overlay.selected, .page-content.selected").length) {
                _self.deselectGrid()
            }
            if(section.siblings('.body-section').length == 1) {
                unwrapped_section = section.siblings('.body-section');
                var container = unwrapped_section.find(".widget-container");
                unwrapped_section.after(container);
                unwrapped_section.remove()
            }
            section.remove();
            _self.populateBodySectionSelector()
        }

        _self.removeAllWidgets(section, {
            old: function() {
                var parent = _self.pageBody.find(".body");
                if(index == 0) {
                    parent.prepend(section)
                } else {
                    parent.find("> :eq(" + (index - 1) + ")").after(section)
                }
                if(unwrapped_section) {
                    var container = section.siblings(".widget-container");
                    container.before(unwrapped_section);
                    unwrapped_section.append(container)
                }
                _self.populateBodySectionSelector()
            },
            new: redoHandler
        });

        redoHandler()
    });
    this.settingBlock.find(".tool-icon.move-section-up").click(function() {
        if($(this).is(".disabled")) {
            return;
        }
        var section = _self.pageBody.find(".body > .body-section:has('> .body-section-overlay.selected')");
        if(section.prev().length == 0) {
            return;
        }
        var state = {
            redo: function() {
                section.prev().before(section)
            },
            undo: function() {
                section.next().after(section)
            }
        };
        state.redo();
        _self.setDirty(state);
        section.scrollHere()
    });
    this.settingBlock.find(".tool-icon.body-section-fluid-fixed-toggle").click(function() {
        if($(this).is(".disabled")) {
            return;
        }
        var selector = "#" + _self.activeSection.find(".body-section:has(>.selected)").attr("id") + " > .widget-container";
        var media = _self.activeMedia, width = _self.getCssValue(_self.css, selector, "width", media);
        var state = {
            redo: function() {
                if(width == 'auto') {
                    _self.updateCss(_self.css, null, selector, undefined, ["width"], media)
                } else {
                    _self.updateCss(_self.css, null, selector, {width: "auto"}, undefined, media)
                }
            },
            undo: function() {
                if(width == 'auto') {
                    _self.updateCss(_self.css, null, selector, {width: "auto"}, undefined, media)
                } else {
                    _self.updateCss(_self.css, null, selector, undefined, ["width"], media)
                }
            }
        };
        state.redo();
        _self.setDirty(state)
    });
    this.settingBlock.find(".tool-icon.move-section-down").click(function() {
        if($(this).is(".disabled")) {
            return;
        }
        var section = _self.pageBody.find(".body > .body-section:has('> .body-section-overlay.selected')");
        if(section.next().length == 0) {
            return;
        }
        var state = {
            redo: function() {
                section.next().after(section)
            },
            undo: function() {
                section.prev().before(section)
            }
        };
        state.redo();
        _self.setDirty(state);
        section.scrollHere()
    })
};

_e.deselectGrid = function() {
    this.activeSection.find(".editable-area-overlay.selected, .page-content.selected").removeClass("selected");
    this.settingBlock.find(".layout-operators .tool-icon").addClass("disabled");
    this.settingBlock.find(".body-grids-selector select").chosen("val", "");
    this.settingBlock.find(".layout-operators .fluid-v-split").addClass("disabled")
};

(function() {
    function createDefinition(media) {
        if(/^(\d+)?-(\d+)?$/.test(media)) {
            var minMax = media.split("-");
            var max;
            if(minMax[0] == '') {
                return "(max-width: " + minMax[1] + "px)";
            }
            if(minMax[1] == '') {
                return "(min-width: " + minMax[0] + "px)";
            }
            return "(min-width: " + minMax[0] + "px) and (max-width: " + minMax[1] + "px)";
        } else {
            return "" + media
        }
    }

    function populateMediaPopup(ev, icon, isMultiple, ignoreCurrent, noGlobal, onlyUsed, wobj, position, collision) {
        if(!wobj) {
            var widget = this.selectedWidget;
            wobj = this.getWidgetObject(widget)
        }
        var medias = wobj.css.getMedias();
        var popupdom = $("<div class='floating-css-panel'><div class='sidebar-group-body'>\
            <select class=\"hidden-res-selector\" " + (isMultiple ? 'multiple="multiple"' : '') + "></select>\
            </div><div class='sidebar-group-body'>\
            <input type=\"button\" class=\"apply-button\" value='" + $.i18n.prop("apply") + "'>\
            </div></div>");
        $(icon).after(popupdom);
        var media_definitions = medias.collect("definition");
        var to_add = [];
        var select = popupdom.find(".hidden-res-selector");
        if(onlyUsed) {
            to_add = media_definitions;
            if(this.activeMedia && ignoreCurrent) {
                to_add.remove(this.activeMedia)
            }
        } else {
            var options = this.settingBlock.find(".change-resolution select option" + (noGlobal ? ':not(:first-child)' : '')).clone();
            var option_values = Array.prototype.collect.call(options, "value").collect(function() {
                return createDefinition(this)
            });
            media_definitions.every(function() {
                if(!option_values.contains("" + this)) {
                    to_add.push("" + this);
                }
            });
            select.append(options)
        }
        to_add.every(function() {
            var option = new Option(this, this);
            select.append(option);
            option.setAttribute("title", this)
        });
        popupdom.position({
            my: position ? position[0] : "left top",
            at: position ? position[1] : "right top",
            of: ev,
            collision: collision ? collision : "flip"
        });
        function hide() {
            popupdom.remove();
            $(document).off(".editor-responsive-hide")
        }
        $(document).on("mousedown.editor-responsive-hide", function(ev) {
            if(popupdom.has(ev.target).length) {
                return;
            }
            hide()
        });
        popupdom.hide = hide;
        return popupdom
    }

    _e.attachLayoutToolEvents = function() {
        var _self = this;
        this.settingBlock.find(".tool-icon.l-fixed-layout").click(function() {
            _self._splitGrid("l-fixed-container", "l-fixed-left", "l-fixed-right", 0)
        });
        this.settingBlock.find(".tool-icon.r-fixed-layout").click(function() {
            _self._splitGrid("r-fixed-container", "r-fixed-left", "r-fixed-right", 1)
        });
        this.settingBlock.find(".v-split-layout").click(function() {
            var selected = _self.activeSection.find(".editable-area-overlay.selected, .page-content.selected").parent();
            _self._splitGrid("v-split-container", "v-split", "v-split", 1, true, selected.hasClass("v-split"))
        });
        this.settingBlock.find(".tool-icon.fluid-splitted-layout").click(function() {
            _self._splitGrid("no-fixed-container", "no-fixed-left", "no-fixed-right", 1, false, false, true)
        });
        this.settingBlock.find(".tool-icon.merge-left").click(function() {
            var selected = _self.activeSection.find(".editable-area-overlay.selected, .page-content.selected").parent();
            var blocks = _self.getMergableGrids(selected, "left");
            if(blocks) {
                _self.mergeTwoBlocks(blocks[0], blocks[1])
            }
        });
        this.settingBlock.find(".tool-icon.merge-right").click(function() {
            var selected = _self.activeSection.find(".editable-area-overlay.selected, .page-content.selected").parent();
            var blocks = _self.getMergableGrids(selected, "right");
            if(blocks) {
                _self.mergeTwoBlocks(blocks[0], blocks[1])
            }
        });
        function toggleMe(hide, grid, selector, media) {
            if(hide) {
                _self.updateCss(_self.css, null, selector, {display: "none"}, undefined, media)
            } else {
                _self.updateCss(_self.css, null, selector, undefined, ["display"], media)
            }
            var siblingSelector = "#" + grid.siblings(".grid-block").attr("id");
            if(grid.is(".no-fixed-left, .no-fixed-right")) {
                if(hide) {
                    _self.updateCss(_self.css, null, siblingSelector, {display: "block", width: "auto"}, undefined, media)
                } else {
                    var width = 100 - parseFloat(_self.getCssValue(_self.css, selector, "width", media));
                    _self.updateCss(_self.css, null, siblingSelector, {width: width + "%"}, ["display"], media)
                }
            } else if(grid.is(".l-fixed-right, .r-fixed-left")) {
                if(hide) {
                    _self.updateCss(_self.css, null, siblingSelector, {width: "auto", "max-width": "unset", flex: "1 1 0px"}, undefined, media)
                } else {
                    _self.updateCss(_self.css, null, siblingSelector, {width: "150px", "max-width": "150px", flex: "0 0 150px"}, undefined, media)
                }
            }
        }
        function fluidMe(fluid, grid, selector, media) {
            var siblingSelector = "#" + grid.siblings(".grid-block").attr("id");
            if(grid.is(".r-fixed-left, .l-fixed-right")) {
                var t = selector;
                selector = siblingSelector;
                siblingSelector = t;
            }
            if(fluid) {
                if(grid.is(".no-fixed-left, .no-fixed-right")) {
                    _self.updateCss(_self.css, null, selector, {width: "50%"}, undefined, media);
                    _self.updateCss(_self.css, null, siblingSelector, {width: "50%"}, undefined, media)
                } else {
                    var parent = grid.parent();
                    var parentId = parent.attr("id");
                    if(!parentId) {
                        parentId = "container-" + bm.getUUID();
                        parent.attr("id", parentId)
                    }
                    _self.updateCss(_self.css, null, "#" + parentId, undefined, ["display"], media);
                    var display = _self.getCssValue(_self.css, "#" + parentId, "display", "");
                    if(display) {
                        if(parent.is(".no-fixed-left, .no-fixed-right")) {
                            if(display != "inline-flex") {
                                _self.updateCss(_self.css, null, "#" + parentId, {display: "inline-flex"}, undefined, media)
                            }
                        } else if(display != "flex") {
                            _self.updateCss(_self.css, null, "#" + parentId, {display: "flex"}, undefined, media)
                        }
                    }
                    var width = _self.getCssValue(_self.css, selector, "flex", media);
                    width = width.split(" ")[2];
                    _self.updateCss(_self.css, null, selector, {width: width, "max-width": width, flex: "0 0 " + width}, undefined, media)
                }
            } else {
                if(grid.is(".no-fixed-left, .no-fixed-right")) {
                    _self.updateCss(_self.css, null, selector, {width: "100%"}, undefined, media);
                    _self.updateCss(_self.css, null, siblingSelector, {width: "100%"}, undefined, media)
                } else {
                    var parent = grid.parent();
                    var parentId = parent.attr("id");
                    if(!parentId) {
                        parentId = "container-" + bm.getUUID();
                        parent.attr("id", parentId)
                    }
                    _self.updateCss(_self.css, null, "#" + parentId, {display: parent.is(".no-fixed-left, .no-fixed-right") ? "inline-block" : "block"}, undefined, media);
                    _self.updateCss(_self.css, null, selector, {width: "auto", "max-width": "unset"}, undefined, media)
                }
            }
        }
        function isFluid(grid, selector, media) {
            if(grid.is(".no-fixed-right, .no-fixed-left")) {
                var width = _self.getCssValue(_self.css, selector, "width", media);
                return width == "100%"
            }
            if(grid.is(".l-fixed-left, .r-fixed-right")) {
                var width = _self.getCssValue(_self.css, selector, "width", media);
                return width == "auto"
            }
            if(grid.is(".r-fixed-left, .l-fixed-right")) {
                var width = _self.getCssValue(_self.css, "#" + grid.siblings(".grid-block").attr("id"), "width", media);
                return width == "auto"
            }
        }
        this.settingBlock.find(".layout-operators .tool-icon.show-hide").click(function() {
            var selected = _self.activeSection.find(".editable-area-overlay.selected, .page-content.selected").parent();
            var cssSelector = "#" + selected.attr("id");
            var display = _self.getCssValue(_self.css, cssSelector, "display") || "initial";
            var old_cache = _self.css.toString();
            toggleMe(display == "initial", selected, cssSelector);
            _self.setDirty("css_change", {css: old_cache, obj: _self}, {css: _self.css.toString(), obj: _self});
        });
        this.settingBlock.find(".layout-operators .tool-icon.hide-in-resolutions").click(function(ev) {
            var hidden_medias = [];
            var selected = _self.activeSection.find(".editable-area-overlay.selected, .page-content.selected").parent();
            var cssSelector = "#" + selected.attr("id");
            var display = _self.css.getAttribute(cssSelector, "display");
            if(display == "none") {
                hidden_medias.push($.i18n.prop("global"))
            }
            _self.css.getMedias().every(function() {
                display = this.getAttribute(".responsive " + cssSelector, "display");
                if(display == "none") {
                    hidden_medias.push(this.definition)
                }
            });
            var popupdom = populateMediaPopup.call(_self, ev, $(this), true, false, false, false, _self, ["right+70 top+17", "left bottom"], "none");
            var select = popupdom.find("select");
            select.find("option").each(function() {
                var value = this.value || $.i18n.prop("global");
                if(hidden_medias.contains(createDefinition(value))) {
                    this.selected = true
                }
            });
            select.chosen({});

            var removed = [];
            select.on("selection_removed", function(ev, value) {
                if(added.contains(value)) {
                    added.remove(value)
                } else {
                    removed.push(value)
                }
            });
            var added = [];
            select.on("selection_added", function(ev, value) {
                if(removed.contains(value)) {
                    removed.remove(value)
                } else {
                    added.push(value)
                }
            });
            popupdom.find("input.apply-button").click(function() {
                popupdom.hide();
                if(!removed.length && !added.length) {
                    return;
                }
                var old_cache = _self.css.toString();
                removed.every(function() {
                    var media = createDefinition(this);
                    toggleMe(false, selected, cssSelector, media)
                });
                added.every(function() {
                    var media = createDefinition(this);
                    toggleMe(true, selected, cssSelector, media)
                });
                _self.setDirty("css_change", {css: old_cache, obj: _self}, {css: _self.css.toString(), obj: _self});
            })
        });
        this.settingBlock.find(".layout-operators .tool-icon.switch-width").click(function() {
            var selected = _self.activeSection.find(".editable-area-overlay.selected, .page-content.selected").parent();
            var cssSelector = "#" + selected.attr("id");
            var is_fluid = isFluid(selected, cssSelector);
            var old_cache = _self.css.toString();
            fluidMe(is_fluid, selected, cssSelector);
            _self.setDirty("css_change", {css: old_cache, obj: _self}, {css: _self.css.toString(), obj: _self});
        });
        this.settingBlock.find(".layout-operators .tool-icon.fluid-in-resolutions").click(function(ev) {
            var fluid_medias = [];
            var selected = _self.activeSection.find(".editable-area-overlay.selected, .page-content.selected").parent();
            var cssSelector = "#" + selected.attr("id");
            var is_fluid = isFluid(selected, cssSelector, "");
            if(is_fluid) {
                fluid_medias.push($.i18n.prop("global"))
            }
            _self.css.getMedias().every(function() {
                is_fluid = isFluid(selected, cssSelector, this.definition);
                if(is_fluid) {
                    fluid_medias.push(this.definition)
                }
            });
            var popupdom = populateMediaPopup.call(_self, ev, $(this), true, false, false, false, _self, ["right+100 top+17", "left bottom"], "none");
            var select = popupdom.find("select");
            select.find("option").each(function() {
                var value = this.value || $.i18n.prop("global");
                if(fluid_medias.contains(createDefinition(value))) {
                    this.selected = true
                }
            });
            select.chosen({});

            var removed = [];
            select.on("selection_removed", function(ev, value) {
                if(added.contains(value)) {
                    added.remove(value)
                } else {
                    removed.push(value)
                }
            });
            var added = [];
            select.on("selection_added", function(ev, value) {
                if(removed.contains(value)) {
                    removed.remove(value)
                } else {
                    added.push(value)
                }
            });
            popupdom.find("input.apply-button").click(function() {
                popupdom.hide();
                if(!removed.length && !added.length) {
                    return;
                }
                var old_cache = _self.css.toString();
                removed.every(function() {
                    var media = createDefinition(this);
                    fluidMe(true, selected, cssSelector, media)
                });
                added.every(function() {
                    var media = createDefinition(this);
                    fluidMe(false, selected, cssSelector, media)
                });
                _self.setDirty("css_change", {css: old_cache, obj: _self}, {css: _self.css.toString(), obj: _self});
            })
        });

        this.settingBlock.find(".layout-operators .fluid-v-split").click(function(ev) {
            if($(this).is(".disabled")) {
                return;
            }
            var selector = "#" + _self.activeSection.find(".v-split:has(>.editable-area-overlay.selected)").attr("id");
            var media = _self.activeMedia, width = _self.getCssValue(_self.css, selector, "width", media);
            var state = {
                redo: function() {
                    if(width == 'auto') {
                        _self.updateCss(_self.css, null, selector, undefined, ["width"], media)
                    } else {
                        _self.updateCss(_self.css, null, selector, {width: "auto"}, undefined, media)
                    }
                },
                undo: function() {
                    if(width == 'auto') {
                        _self.updateCss(_self.css, null, selector, {width: "auto"}, undefined, media)
                    } else {
                        _self.updateCss(_self.css, null, selector, undefined, ["width"], media)
                    }
                }
            };
            state.redo();
            _self.setDirty(state)
        });
    };

    _e._handleCssMediaTools = function(wobj, wrapper, uuid) {
        var _self = this;
        wrapper.find(".tool-icon.remove-media").click(function(ev) {
            var popupdom = populateMediaPopup.call(_self, ev, $(this), false, false, true, true, wobj);
            var select = popupdom.find("select");
            select.chosen({});
            popupdom.find("input.apply-button").click(function() {
                var value = select.chosen("val");
                if(!value) {
                    return;
                }
                popupdom.hide();
                var old_cache = wobj.css.toString();
                value = createDefinition(value);
                wobj.css.removeMedia(value);
                _self.persistCss(uuid, wobj.css);
                var handler = function() {
                    if(wobj instanceof app.tabs.edit_content.Widget) {
                        wobj.updateMemoryStyle();
                        wobj.positionReferenceLine()
                    }
                };
                _self.setDirty("css_change", {css: old_cache, uuid: uuid, obj: wobj, handler: handler}, {css: wobj.css.toString(), uuid: uuid, obj: wobj, handler: handler}, wobj.elm);
                handler()
            })
        });
        wrapper.find(".tool-icon.copy-other-media").click(function(ev) {
            var popupdom = populateMediaPopup.call(_self, ev, $(this), false, true, true, true, wobj);
            var select = popupdom.find("select");
            select.chosen({});
            popupdom.find("input.apply-button").click(function() {
                var value = select.chosen("val");
                if(!_self.activeMedia || !value || _self.activeMedia == value) {
                    return;
                }
                popupdom.hide();
                var old_cache = wobj.css.toString();
                value = createDefinition(value);
                var source_media = wobj.css.getMedia(value);
                var target_media = wobj.css.getMedia(_self.activeMedia);
                if(!target_media) {
                    target_media = wobj.css.addMedia(_self.activeMedia)
                }
                target_media.merge(source_media);
                _self.persistCss(uuid, wobj.css);
                var handler = function() {
                    if(wobj instanceof app.tabs.edit_content.Widget) {
                        wobj.updateMemoryStyle();
                        wobj.positionReferenceLine()
                    }
                };
                _self.setDirty("css_change", {css: old_cache, uuid: uuid, obj: wobj, handler: handler}, {css: wobj.css.toString(), uuid: uuid, obj: wobj, handler: handler}, wobj.elm);
                handler()
            })
        })
    };

    _e.handleCssEditorTools = function(wrapper) {
        var _self = this;
        var widget = this.selectedWidget;
        var uuid = widget.attr("id").substring(3);
        var wobj = this.getWidgetObject(widget);
        wrapper.find(".tool-icon.show-hide").click(function() {
            var display = _self.getCssValue(wobj.css, "#wi-" + uuid, "display") || "block";
            var old_cache = wobj.css.toString();
            if(display == "block") {
                _self.updateCss(wobj.css, uuid, "#wi-" + uuid, {display: "none"})
            } else {
                _self.updateCss(wobj.css, uuid, "#wi-" + uuid, undefined, ["display"]);
                if(wobj.elm.is(":hidden")) {
                    _self.updateCss(wobj.css, uuid, "#wi-" + uuid, {display: "block"})
                }
            }
            var handler = function() {
                wobj.positionReferenceLine()
            };
            _self.setDirty("css_change", {css: old_cache, uuid: uuid, obj: wobj, handler: handler}, {css: wobj.css.toString(), uuid: uuid, obj: wobj, handler: handler}, widget);
            handler()
        });
        wrapper.find(".tool-icon.hide-in-resolutions").click(function(ev) {
            var hidden_medias = [];
            var cssSelector = "#wi-" + uuid;
            var visible = wobj.css.getAttribute(cssSelector, "display");
            if(visible == "none") {
                hidden_medias.push($.i18n.prop("global"))
            }
            wobj.css.getMedias().every(function() {
                visible = this.getAttribute(".responsive " + cssSelector, "display");
                if(visible == "none") {
                    hidden_medias.push(this.definition)
                }
            });
            var popupdom = populateMediaPopup.call(_self, ev, $(this), true, false, false, false, wobj);
            var select = popupdom.find("select");
            select.find("option").each(function() {
                var value = this.value || $.i18n.prop("global");
                if(hidden_medias.contains(createDefinition(value))) {
                    this.selected = true
                }
            });
            select.chosen({});

            var removed = [];
            select.on("selection_removed", function(ev, value) {
                if(added.contains(value)) {
                    added.remove(value)
                } else {
                    removed.push(value)
                }
            });
            var added = [];
            select.on("selection_added", function(ev, value) {
                if(removed.contains(value)) {
                    removed.remove(value)
                } else {
                    added.push(value)
                }
            });
            popupdom.find("input.apply-button").click(function() {
                popupdom.hide();
                if(!removed.length && !added.length) {
                    return;
                }
                var old_cache = wobj.css.toString();
                removed.every(function() {
                    var media = createDefinition(this);
                    _self.updateCss(wobj.css, uuid, "#wi-" + uuid, undefined, ["display"], media);
                    if(wobj.elm.is(":hidden")) {
                        _self.updateCss(wobj.css, uuid, "#wi-" + uuid, {display: "block"}, undefined, media)
                    }
                });
                added.every(function() {
                    var media = createDefinition(this);
                    _self.updateCss(wobj.css, uuid, "#wi-" + uuid, {display: "none"}, undefined, media)
                });
                var handler = function() {
                    if(wobj instanceof app.tabs.edit_content.Widget) {
                        wobj.updateMemoryStyle();
                        wobj.positionReferenceLine()
                    }
                };
                _self.setDirty("css_change", {css: old_cache, uuid: uuid, obj: wobj, handler: handler}, {css: wobj.css.toString(), uuid: uuid, obj: wobj, handler: handler}, widget);
                handler()
            })
        });
        this._handleCssMediaTools(wobj, wrapper, uuid)
    }
})();

_e.mergeTwoBlocks = function(section1, section2) {
    var _self = this;
    var container = section1.parent();
    var container_removable_class;
    var child_container_class1;
    var child_container_class2;
    var section = this.activeSection;
    if(section1.is(".l-fixed-left")) {
        container_removable_class = "l-fixed-container"
    } else if(section1.is(".no-fixed-left")) {
        container_removable_class = "no-fixed-container"
    } else if(section1.is(".r-fixed-left")) {
        container_removable_class = "r-fixed-container"
    } else {
        if(section1.siblings(".v-split").length == 1) {
            container_removable_class = "v-split-container"
        }
    }
    var classes1 = section1.attr("class");
    var classes2 = section2.attr("class");
    classes1.split(" ").every(function() {
        if(this.endsWith("-container")) {
            child_container_class1 = this;
            return false;
        }
    });
    classes2.split(" ").every(function() {
        if(this.endsWith("-container")) {
            child_container_class2 = this;
            return false;
        }
    });
    var children1;
    var children2;
    var uuid1 = section1.attr("id");
    var uuid2 = section2.attr("id");
    var removed_rules = [];

    function starter() {
        if(section.find(".editable-area-overlay.selected, .page-content.selected").length) {
            _self.deselectGrid()
        }
        _self.clearEditableOverlay();
        _self._destroyBodySortResize(section)
    }

    function redo() {
        children1 = section1.children();
        children2 = section2.children();
        if(container_removable_class) {
            container.removeClass(container_removable_class)
        }
        if(child_container_class1 || child_container_class2) {
            container.addClass(child_container_class1 || child_container_class2)
        }
        if(section1.is(".v-split") && section1.siblings(".v-split").length > 1) {
            section1.append(children2);
            section2.remove();
            section1.addClass(child_container_class2)
        } else {
            container.html(children1).append(children2);
            var rules = _self.removeRule(_self.css, null, "#" + uuid1, true);
            if(rules) {
                removed_rules.pushAll(rules)
            }
        }
        var rules = _self.removeRule(_self.css, null, "#" + uuid2, true);
        if(rules) {
            removed_rules.pushAll(rules)
        }
    }

    function undo() {
        if(container_removable_class) {
            container.addClass(container_removable_class)
        }
        if(child_container_class1 || child_container_class2) {
            container.removeClass(child_container_class1 || child_container_class2)
        }
        if(section1.parent().length) {
            section1.after(section2);
            section2.append(children2);
            section1.removeClass(child_container_class2)
        } else {
            section1.append(children1);
            section2.append(children2);
            container.html(section1).append(section2)
        }
        if(removed_rules) {
            removed_rules.every(function() {
                this[0].addRule(this[1])
            });
            _self.persistCss(null, _self.css)
        }
    }

    function finer() {
        _self.populateGridBlockSelector();
        _self.initializeBodySectionSortResize(section);
        _self.setEditableOverlay()
    }

    var state = {redo: starter.blend(redo).blend(finer), undo: starter.blend(undo).blend(finer)};
    state.redo();

    this.setDirty(state);
};

_e.initSidebar = function() {
    var _self = this;
    this.settingBlock = this.body.find(".setting-block-panel");
    this.widgetSelector = this.body.find('select.active-widget-selector');
    this.widgetSelector.obj(Chosen).disable_search = true;
    this.settingBlock.find(".leftbar-accordion").accordion({
        expand: function(header, item) {
            if(item.is(".page-props")) {
                _self.updatePageProperties()
            } else if(item.is(".page-props")) {
                _self.updateWidgetConfig()
            } else if(item.is(".dpck-css")) {
                _self.editDockCss()
            }
        }
    });
    var removeDockDom = this.settingBlock.find(".remove-dock");
    var splitter = _self.body.find(".block-chooser .splitter");
    _self.activeSectionSelector = this.settingBlock.find("select.active-section-selector").change(function() {
        var newSection = $(this).val();
        _self.changeActiveSection(newSection, true)
    }).chosen({disable_search: true});
    _self.activeSectionSelector.chosen({disable_search: true});
    this.widgetSelector.change(function() {
        if(this.value == "") {
            _self.unselectWidget();
        } else {
            var widget = _self.activeSection.find("#" + this.value);
            _self.selectWidget(widget, {widget_selector_update: false});
            widget.scrollHere()
        }
    });
    this.settingBlock.find("select.active-widget-ref-unit-selector").change(function() {
        var widget = _self.selectedWidget;
        var wiElement = _self.getWidgetObject(widget);
        var old_style = $.extend({}, wiElement.style);
        var parent = wiElement.elm.parent();
        var total_w = parent.innerWidth();
        var total_h = parent.innerHeight();
        if(this.value == "%") {
            if(wiElement.isLeft()) {
                wiElement.style.left = parseFloat(wiElement.style.left) / total_w * 100 + "%"
            }
            if(wiElement.isRight()) {
                wiElement.style.right = parseFloat(wiElement.style.right) / total_w * 100 + "%"
            }
            if(wiElement.isTop()) {
                wiElement.style.top = parseFloat(wiElement.style.top) / total_h * 100 + "%"
            }
            if(wiElement.isBottom()) {
                wiElement.style.bottom = parseFloat(wiElement.style.bottom) / total_h * 100 + "%"
            }
        } else {
            if(wiElement.isLeft()) {
                wiElement.style.left = parseFloat(wiElement.style.left) / 100 * total_w + "px"
            }
            if(wiElement.isRight()) {
                wiElement.style.right = parseFloat(wiElement.style.right) / 100 * total_w + "px"
            }
            if(wiElement.isTop()) {
                wiElement.style.top = parseFloat(wiElement.style.top) / 100 * total_h + "px"
            }
            if(wiElement.isBottom()) {
                wiElement.style.bottom = parseFloat(wiElement.style.bottom) / 100 * total_h + "px"
            }
        }
        var newStyle = $.extend({}, wiElement.style);
        var value = this.value;
        wiElement.percentage = value == "%";
        wiElement.applyStyle(newStyle);
        _self.setDirty("widget_reposition", {widget: widget, style: old_style, handler: function() {
            _self.settingBlock.find(".widget-ref-unit-selector select").chosen("val", value == "%" ? "px" : "%")
        }}, {widget: widget, style: newStyle, handler: function() {
            _self.settingBlock.find(".widget-ref-unit-selector select").chosen("val", value)
        }}, widget);
    });
    this.settingBlock.find(".add-dock").click(function() {
        _self.addDock();
    });
    removeDockDom.click(function() {
        if($(this).is(".disabled")) {
            return;
        }
        _self.removeDock();
    });
    if(this.isResponsive) {
        this.settingBlock.find(".change-resolution select").change(function() {
            _self.resolutionChange(this.value);
        });
    }
    this.settingBlock.find(".tool-icon.remove-widget").click(function () {
        if($(this).is(".disabled")) {
            return;
        }
        var id = _self.selectedWidget.attr("id");
        var uuid;
        if(id) {
            uuid = _self.selectedWidget.attr("id").substring(3);
        }
        _self.removeWidget(_self.activeSection, uuid)
    });
    this.settingBlock.find(".widget-prop-configure .tablike-button").click(function() {
        if($(this).is(".active")) {
            return;
        }
        $(this).addClass("active").siblings(".active").removeClass("active");
        if($(this).is(".basic")) {
            _self.settingBlock.active_config_tab = "basic"
        } else if($(this).is(".css")) {
            _self.settingBlock.active_config_tab = "css"
        } else {
            _self.settingBlock.active_config_tab = "js"
        }
        _self.updateWidgetConfig()
    });
    this.settingBlock.find(".page-props .tablike-button").click(function() {
        if($(this).is(".active")) {
            return;
        }
        $(this).addClass("active").siblings(".active").removeClass("active");
        if($(this).is(".css")) {
            _self.settingBlock.active_page_config_tab = "css"
        } else {
            _self.settingBlock.active_page_config_tab = "js"
        }
        _self.updatePageProperties()
    });
    this.settingBlock.find(".accordion-item").scrollbar({reduce_margin_from_offset: false});
    this.body.find(".widget-block-panel .widget-item .favorite-mark").on("click", function() {
        var $this = $(this), wiItem = $this.parent(".widget-item");
        _self.modifyFavoriteWidget(wiItem, $this.is(".active") ? "remove" : "add")
    })
    bm.instantSearch(this.body.find('#bmui-tab-widget'), this.body.find('#bmui-tab-widget .search-form'), ".widget-item", ".title")
    this._attachSectionToolEvents();
    this.attachLayoutToolEvents()
};

_e.modifyFavoriteWidget = function(wiItem, operation) {
    var _self = this;
    bm.ajax({
        url: _self.modifyFavoriteWidgetUrl,
        data: {
            widgetType: wiItem.attr("widget-type"),
            operation: operation
        }
    });
    wiItem.fadeOut(500, function(){
        if(operation == "add") {
            wiItem.find(".favorite-mark").addClass("active")
            _self.body.find("#bmui-tab-widget .widget-separator.other").before(wiItem);
        } else {
            wiItem.find(".favorite-mark").removeClass("active")
            _self.body.find("#bmui-tab-widget").append(wiItem);
        }
        wiItem.fadeIn(500)
    })
};

_e.updateSectionDimensionSpinners = function() {
    var section = this.activeSection.attr("section");
    if(section == "body") {
        var width = this.getCssValue(this.css, ".body > .widget-container", "width");
        if(this.sectionWidth.length) {
            this.sectionWidth[0].isilent(width == "auto" || !width ? "" : parseFloat(width));
            this.sectionHeight.stepper("disable")[0].isilent("");
        }
    } else if(section == 'header' || section == 'footer') {
        var dims = this.getCssValue(this.css, "." + section + " > .widget-container", ["width", "height"]);
        if(this.sectionWidth.length) {
            this.sectionWidth[0].isilent(!dims || dims.width == "auto" || !dims.width ? "" : parseFloat(dims.width));
            this.sectionHeight.stepper("enable")[0].isilent(dims ? parseFloat(dims.height) : "");
        }
    } else if(this.activeSection.is(".dockable")) {
        var element = this.cachedDock[section];
        var dims = this.getCssValue(element.css, "#dock-" + section, ["width", "height"]);
        if(this.sectionWidth.length) {
            this.sectionWidth[0].isilent(!dims || dims.width == "auto" || !dims.width ? "" : parseFloat(dims.width));
            this.sectionHeight.stepper("enable")[0].isilent(dims ? parseFloat(dims.height) : "");
        }
    }
};

_e.populateGridBlockSelector = function() {
    var _self = this;
    var select = this.settingBlock.find(".body-grids-selector select");
    select.find("option").remove();
    select.append(new Option($.i18n.prop("select.grid.block"), '', true, true));
    this.activeSection.find(this.editableBlockSelector).each(function() {
        var _this = $(this);
        var id = "";
        var key;
        if(_this.is(".page-content")) {
            id = ".page-content";
            key = $.i18n.prop("widget.container")
        } else if(_this.is(".widget-container")) {
            if(_this.parent().is(".page-content")) {
                id = ".page-content > .widget-container"
            } else {
                id = _this.parent().attr("id");
                if(id) {
                    id = "#" + id + " > .widget-container"
                } else {
                    id = "> .widget-container"
                }
            }
            key = $.i18n.prop("widget.container")
        } else {
            id = "#" + _this.attr("id");
            key = id.substring(7)
        }
        select.append(new Option(key, id))
    });
    select.chosen('destroy');
    select.chosen({disable_search: true});
    select.off(".editor").on("change.editor", function() {
        var id = this.value;
        if(id) {
            _self.selectGrid(_self.activeSection.find(id), false)
        } else {
            _self.deselectGrid()
        }
    })
};

_e.populateBodySectionSelector = function() {
    var _self = this;
    var select = this.settingBlock.find(".body-sections-selector select");
    select.find("option").remove();
    select.append(new Option($.i18n.prop("select.body.section"), '', true, true));
    this.activeSection.find(".body-section").each(function() {
        var id = $(this).attr("id");
        var key = id.substring(3);
        select.append(new Option(key, id))
    });
    select.chosen('destroy');
    select.chosen({disable_search: true});
    select.off(".editor").on("change.editor", function() {
        var id = this.value;
        if(id) {
            _self.selectBodySection(_self.activeSection.find("#" + id), true)
        } else {
            _self.deselectBodySection()
        }
    })
};

_e.populateWidgetSelector = function() {
    var _self = this;
    this.widgetSelector.find("option").remove();
    var widgets = this.activeSection.find(_self.supportedWidgetSelector).not("." + _self.widgetClass + " ." + _self.widgetClass);
    var newOptions = [];
    newOptions.push("<option value=''>" + $.i18n.prop("active.section.widgets") + "</option>");
    $.each(widgets, function() {
        var _this = $(this);
        var widgetType = _this.attr("widget-type");
        var uuid = _this.attr("id");
        newOptions.push("<option value='" + uuid + "'> " + widgetType + "-" + uuid.substring(3) + " </option>");
    });
    for(var i = 0; i < newOptions.length; i++) {
        this.widgetSelector.append($(newOptions[i]));
    }
    this.widgetSelector.trigger("chosen:updated");
};

_e.getBodyContent = function() {
    return this.pageBody.find(".body").clone()
};

_e.getWidgetTag = function(uuid, widgetType) {
    return "<wi:widget uuid ='" + uuid + "' type='" + widgetType + "'/>"
};

_e.getSavedData = function() {
    var _self = this;
    var blockSelector = ["header", "body", "footer", "dockable"];
    var getWidgets = function(block) {
        var widgets = [];
        block.each(function() {
            var uuid = $(this).attr("id").substring(3);
            var wiElement = _self.cachedWidgets[uuid];
            var saveData = {
                uuid: wiElement.uuid,
                type: wiElement.type,
                cache: wiElement.elm.data("data-cache"),
                copied: true,
                css: wiElement.css.toString(),
                templateServerUuid: wiElement.templateServerUuid,
                groupId: wiElement.group
            };
            if(wiElement.js != undefined) {
                saveData.js = wiElement.js
            }
            var dock = wiElement.elm.closest(".dockable");
            if(dock.length) {
                saveData["dockUUID"] = dock.attr("id").substring(5);
            }
            widgets.push(saveData);
        });
        return widgets;
    };
    var added = {};
    blockSelector.every(function() {
        added[this] = getWidgets(_self.pageBody.find("." + this + " [new-widget]"))
    });
    var docks = [];
    _self.newDocks.every(function() {
        docks.push({uuid: this.uuid, css: this.css.toString()})
    });
    _self.modifiedDocks.every(function() {
        var cache = _self.cachedDock[this];
        var id = cache.elm.attr("dock-id");
        if(!_self.removedDock.contains(id)) {
            docks.push({uuid: cache.uuid, css: cache.css.toString(), dockId: id})
        }
    });
    var modified = {};
    this.pageBody.find("[modified-widget]").each(function() {
        var uuid = $(this).attr("id").substring(3);
        var wiElement = _self.cachedWidgets[uuid];
        var css = wiElement.css;
        modified[uuid] = {uuid: uuid, type: wiElement.type, cache: wiElement.elm.data("data-cache"), css: css.toString(), js: wiElement.js, groupId: wiElement.group}
    });
    var containerCss = this.css.toString();
    var containerJs = this.js;
    var removed = [];
    this.removedWidgets.every(function(key, value) {
        if(value instanceof Array) {
            removed.pushAll(value);
        } else {
            removed.push(value);
        }
    });
    var data = {
        modified: JSON.stringify(modified),
        removed: JSON.stringify(removed),
        removedDock: JSON.stringify(_self.removedDock),
        docks: JSON.stringify(docks),
        containerId: this.containerId,
        containerType: this.containerType,
        containerCss: containerCss,
        layoutId: _self.layout_id,
        added_in_header: JSON.stringify(added.header),
        added_in_footer: JSON.stringify(added.footer),
        added_in_body: JSON.stringify(added.body),
        added_in_dock: JSON.stringify(added.dockable)
    };
    var bodyContent = this.getBodyContent();
    bodyContent.find("*").each(function() {
        var _this = $(this);
        _this.removeAttr("style").removeClass("bmui-draggable bmui-resizable");
        _this.filter(".widget-overlay, .section-overlay, .editable-area-overlay, .body-section-overlay, .bmui-resize-handle").replaceWith("");
        if(_this.hasClass("page-content")) {
            _this.replaceWith("<wi:widget type='page'/>");
        }
        if(_this.hasClass(_self.widgetClass)) {
            var widgetType = _this.attr("widget-type");
            var id = _this.attr("id");
            var uuid = id ? id.substring(3) : null;
            _this.replaceWith(_self.getWidgetTag(uuid, widgetType));
        }
    });
    data.bodyContent = bodyContent.length ?  bodyContent.html().trim() : "";
    if(containerJs != undefined) {
        data.containerJs = containerJs
    }
    return data;
};

_e.save = function(callback) {
    var _self = this;
    var blockSelector = ["header", "body", "footer"].contains(this.section) ? this.section : "dockable";
    var templateWidget = this.pageBody.find("." + blockSelector + " [external-widget=true]");
    if(templateWidget.length) {
        bm.notify($.i18n.prop("you.have.unconfigured.widget"), "error");
        return false
    }
    var params = _self.getSavedData();
    _self.mask();
    bm.ajax({
        url: app.baseUrl + _self.contentSaveUrl,
        data: params,
        response: function() {
            _self.unmask();
        },
        success: function(resp) {
            _self.clearDirty();
            _self.clearSavedData(resp);
            _self.activeSection.find(".widget").removeAttr("cloned");
            if(callback) {
                callback()
            }
            _self.unselectWidget()
        }
    })
};

_e.revert = function(callback) {
    var _self = this;
    this.pageBody.loader();
    var currentUrl = this.iframe.attr("src");
    var newUrl = bm.path(currentUrl);
    this.reload_performed = true;
    this.iframe.one("sure-load", function() {
        _self.clearDirty();
        if(callback) {
            callback()
        }
    });
    this.iframe.attr("src", newUrl.full())
};

_e.attachEvent = function() {
    var _self = this;
    this.body.find(".toolbar .save").click(function() {
        if($(this).hasClass("disabled")) {
            return
        }
        _self.save()
    });
    var undoBtn = this.body.find(".toolbar .undo");
    var redoBtn = this.body.find(".toolbar .redo");
    this.undo_redo = UndoRedoFactory.createManager(undoBtn, redoBtn, {
        changeState: function(type, to_state, from_state) {
            switch(type) {
                case "section_height":
                    _self.setSectionHeight(_self.activeSection, to_state);
                    _self.sectionHeight[0].isilent(to_state);
                    break;
                case "section_width":
                    _self.setSectionWidth(_self.activeSection, to_state);
                    _self.sectionWidth[0].isilent(to_state);
                    break;
                case "js":
                    _self.js = to_state.js;
                    _self.updatePageProperties("js");
                    break;
                case "widget_relocate":
                    if(!$.isArray(to_state)) {
                        to_state = [to_state];
                        to_state.handler = to_state[0].handler;
                        from_state = [from_state];
                        from_state.handler = from_state[0].handler
                    }
                    to_state.revery(function(_to_state, i) {
                        var _from_state = from_state[i];
                        var uuid = this.widget.attr("id").substring(3);
                        if(this.cache) {
                            _self.cachedWidgets[uuid] = this.cache
                        }
                        if(this.index != undefined) {
                            if(this.index > 0) {
                                var to_index = this.index;
                                if(_from_state.parent && this.parent && _from_state.parent[0] == this.parent[0] && to_index > _from_state.index) {
                                    to_index++
                                }
                                var before = this.parent.find(">." + _self.widgetClass + ":eq(" + (to_index - 1) + ")");
                                before.after(this.widget)
                            } else {
                                this.parent.prepend(this.widget)
                            }
                        } else {
                            this.widget.detach()
                        }
                        if(_self.selectedWidget && _self.selectedWidget[0] == this.widget[0]) {
                            _self.unselectWidget()
                        }
                        if(!_from_state.parent || !this.parent) {
                            var cachedElement = _self.cachedWidgets[uuid];
                            var templateServerUuid = cachedElement.templateServerUuid ? cachedElement.templateServerUuid : (cachedElement.elm.is("[external-widget=true]") ? uuid : null);
                            if(!this.widget.is("[new-widget]") || templateServerUuid) {
                                if(_from_state.parent) {
                                    _self.removedWidgets.push({uuid : uuid, section: _self.activeSection.attr("section"), templateServerUuid: templateServerUuid})
                                } else {
                                    _self.removedWidgets.pop()
                                }
                            }
                            _self.populateWidgetSelector()
                        }
                    });
                    break;
                case "widget_js":
                    to_state.obj.js = to_state.js;
                    break;
                case "widget_reposition":
                    var wiElement = _self.getWidgetObject(to_state.widget);
                    wiElement.applyStyle(to_state.style);
                    break;
                case "widget_update":
                    var widgetType = to_state.widget.attr("widget-type");
                    var data = {cache: to_state.cache, type: widgetType, widgetId: to_state.widget.attr("widget-id")};
                    if(data.cache || data.widgetId) {
                        _self.getWidgetObject(to_state.widget).render(data)
                    } else {
                        _self.getWidgetObject(to_state.widget).updateContent(_self.getEmptyWidgetDom(widgetType))
                    }
                    break;
                case "css_change":
                    to_state.obj.css = new CssParser(to_state.css).parse();
                    _self.persistCss(to_state.uuid, to_state.obj.css);
                    if(to_state.obj instanceof app.tabs.edit_content) {
                        _self.updatePageProperties("css")
                    } else if(to_state.obj instanceof app.tabs.edit_content.Widget) {
                        _self.updateWidgetConfig(to_state.uuid, "css")
                    } else {
                        _self.editDockCss()
                    }
            }
            if(to_state.handler) {
                to_state.handler(from_state)
            }
        }
    });
    function crossSectionHandle(ev, state) {
        var section;
        if(state.doer && state.doer.section) {
            section = state.doer.section
        } else if(state.previous.section) {
            section = state.previous.section
        }
        if(section && section[0] != _self.activeSection[0]) {
            _self.changeActiveSection(section)
        }
    }
    redoBtn.intercept("redo", crossSectionHandle).on("redo", function() {
        if(!_self.isDirty()) {
            _self.setDirty()
        }
        if(_self.selectedWidget) {
            _self.updateWidgetConfig()
        }
    });
    undoBtn.intercept("undo", crossSectionHandle).on("undo_bottom_reached", function() {
        _self.clearDirty(true)
    }).on("undo", function() {
        if(_self.selectedWidget) {
            _self.updateWidgetConfig()
        }
    });
    this.body.find(".toolbar .align").click(function() {
        var button = $(this);
        if(button.is(".disabled")) {
            return;
        }
        var align;
        if(button.is(".align-left")) {
            align = "left"
        } else if(button.is(".align-right")) {
            align = "right"
        } else if(button.is(".align-top")) {
            align = "top"
        } else {
            align = "bottom"
        }
        var max = align == "left" || align == "top" ? true : false;
        var masterWidget = _self.multiSelectedWidget.find("> .multi-selection-master-widget").closest("." + _self.widgetClass);
        if(!masterWidget.length) {
            var tempMost = Number.MAX_VALUE * (max ? 1 : -1);
            _self.multiSelectedWidget.each(function() {
                var widget = $(this);
                var pos = widget.rect();
                if(max ? pos[align] < tempMost : pos[align] > tempMost) {
                    tempMost = pos[align];
                    masterWidget = widget
                }
            })
        }
        var masterObj = _self.getWidgetObject(masterWidget);
        var masterPos = masterObj.getPos();
        var adjustables = _self.multiSelectedWidget.not(masterWidget);
        var container = masterWidget.closest(".widget-container");
        var totalWidth = container.innerWidth();
        var totalHeight = container.innerHeight();
        if(!masterWidget.parent().is(container)) {
            var parent = masterWidget.parent();
            var parent_position = parent.pposition();
            masterPos.left += parent_position.left + parent.leftRib(true, true, false);
            masterPos.top += parent_position.top + parent.topRib(true, true, false);
            masterPos.right += parent_position.right + parent.rightRib(true, true, false);
            masterPos.bottom += parent_position.bottom + parent.bottomRib(true, true, false)
        }
        adjustables.each(function() {
            var widget = $(this);
            var obj = _self.getWidgetObject(widget);
            var widgetContainerOffset = {left: 0, top: 0, right: 0, bottom: 0};
            var wparent = widget.parent();
            var containerTotalW = totalWidth;
            var containerTotalH = totalHeight;
            if(!wparent.is(container)) {
                var parent = widget.parent();
                var parent_position = parent.pposition(undefined, true);
                containerTotalW = parent_position.width;
                containerTotalH = parent_position.height;
                widgetContainerOffset.left = parent_position.left + parent.leftRib(true, true, false);
                widgetContainerOffset.top = parent_position.top + parent.topRib(true, true, false);
                widgetContainerOffset.right = parent_position.right + parent.rightRib(true, true, false);
                widgetContainerOffset.bottom = parent_position.bottom + parent.bottomRib(true, true, false)
            }
            if(align == "left") {
                var newX = masterPos.left - widgetContainerOffset.left;
                if(!obj.isLeft()) {
                    newX = containerTotalW - newX - widget.outerWidth(true)
                }
                if(obj.percentage) {
                    var total = wparent.innerWidth();
                    newX = newX / total * 100
                }
                obj.setX(newX)
            } else if(align == "right") {
                var newX = masterPos.right - widgetContainerOffset.right;
                if(obj.isLeft()) {
                    newX = containerTotalW - newX - widget.outerWidth(true)
                }
                if(obj.percentage) {
                    var total = wparent.innerWidth();
                    newX = newX / total * 100
                }
                obj.setX(newX)
            } else if(align == "top") {
                var newY = masterPos.top - widgetContainerOffset.top;
                if(obj.isBottom()) {
                    newY = containerTotalH - newY - widget.outerHeight(true)
                }
                if(obj.percentage) {
                    var total = wparent.innerHeight();
                    newY = newY / total * 100
                }
                obj.setY(newY)
            } else {
                var newY = masterPos.bottom - widgetContainerOffset.bottom;
                if(obj.isTop()) {
                    newY = containerTotalH - newY - widget.outerHeight(true)
                }
                if(obj.percentage) {
                    var total = wparent.innerHeight();
                    newY = newY / total * 100
                }
                obj.setY(newY)
            }
        })
    });
    this.body.find(".toolbar .engroup").click(this.groupWidgets.bind(this));
    this.body.find(".toolbar .ungroup").click(this.ungroupWidgets.bind(this));
    this.on("active", function() {
        this.undo_redo.activateHotkey()
    }).on("deActive", function() {
        this.undo_redo.deactivateHotkey()
    }).on("close", function() {
        this.undo_redo.destroy();
        if(_self.iframeWindow) $(_self.iframeWindow.document).off("." + _self.id)
    })
};

_e.groupWidgets = function() {
    var _self = this;
    var groupId = bm.getUUID();
    var group = $("<div class='widget-group'></div>").attr("id", "group-" + groupId);
    var container = this.activeSection.find("> .widget-container");
    var redo = function() {
        container.append(group)
    };
    var undo = function() {
        group.remove()
    };
    var offset = container.offset();
    offset = {left: -1 * offset.left, top: -1 * offset.top};
    var group_offset = {
        min_x: Number.MAX_VALUE,
        min_y: Number.MAX_VALUE,
        max_x: 0,
        max_y: 0
    };
    var widgets = this.multiSelectedWidget;
    if(!widgets) {
        return;
    }
    widgets.each(function () {
        var rect = $(this).rect(offset);
        if (rect.left < group_offset.min_x) {
            group_offset.min_x = rect.left
        }
        if (rect.top < group_offset.min_y) {
            group_offset.min_y = rect.top
        }
        if (rect.right > group_offset.max_x) {
            group_offset.max_x = rect.right
        }
        if (rect.bottom > group_offset.max_y) {
            group_offset.max_y = rect.bottom
        }
    });
    var newCss = {left: group_offset.min_x + "px", top: group_offset.min_y + "px", width: (group_offset.max_x - group_offset.min_x) + "px", height: (group_offset.max_y - group_offset.min_y) + "px"};
    redo = redo.blend(function() {
        group.append(widgets);
        _self.updateCss(_self.css, undefined, "#group-" + groupId, newCss)
    });
    undo = undo.intercept(function() {
        _self.removeRule(_self.css, undefined, "#group-" + groupId)
    });
    var old_styles = {};
    var old_rects = {};
    this.multiSelectedWidget.each(function() {
        var widget = $(this);
        var wiElement = _self.getWidgetObject(widget);
        var style = wiElement.style;
        old_styles[widget.attr("id")] = $.extend({}, style);
        old_rects[widget.attr("id")] = widget.rect()
    });
    redo();
    var coordinate_adjust = group.rect();
    var newBorderColor;
    this.multiSelectedWidget.each(function() {
        var widget = $(this);
        var wiElement = _self.getWidgetObject(widget);
        var style = wiElement.style;
        var oldGroup = wiElement.group;
        var oldstyle = old_styles[widget.attr("id")];
        var pos = old_rects[widget.attr("id")];
        var borderColor = widget.find(".widget-overlay")[0].style.borderLeftColor;
        var total_w = widget.parent().innerWidth();
        var total_h = widget.parent().innerHeight();
        if(wiElement.isLeft()) {
            var left = pos.left - coordinate_adjust.left - group.leftRib(true, true, false);
            if(wiElement.percentage) {
                style.left = left / total_w * 100 + "%"
            } else {
                style.left = left + "px"
            }
        }
        if(wiElement.isRight()) {
            var right = pos.right - coordinate_adjust.right - group.rightRib(true, true, false);
            if(wiElement.percentage) {
                style.right = right / total_w * 100 + "%"
            } else {
                style.right = right + "px"
            }
        }
        if(wiElement.isTop()) {
            var top = pos.top - coordinate_adjust.top - group.topRib(true, true, false);
            if(wiElement.percentage) {
                style.top = top / total_h * 100 + "%"
            } else {
                style.top = top + "px"
            }
        }
        if(wiElement.isBottom()) {
            var bottom = pos.bottom - coordinate_adjust.bottom - group.bottomRib(true, true, false);
            if(wiElement.percentage) {
                style.bottom = bottom / total_h * 100 + "%"
            } else {
                style.bottom = bottom + "px"
            }
        }
        var applier = function(style, group, borderColor) {
            wiElement.applyStyle(style);
            wiElement.group = group;
            widget.find(".widget-overlay")[0].style.borderColor = borderColor
        };
        redo = redo.blend(function() {
            applier(style, groupId, newBorderColor)
        });
        undo = undo.intercept(function() {
            applier(oldstyle, oldGroup, borderColor);
            var _container = container;
            if(oldGroup) {
                _container = container.find("> #group-" + oldGroup)
            }
            _container.append(widget)
        });
        applier(style, groupId, newBorderColor)
    });
    this.setDirty({undo: undo, redo: redo}, undefined, undefined, widgets);
    this.assignColorToGroups(group);
    newBorderColor = widgets.eq(0).find(".widget-overlay")[0].style.borderLeftColor;
    this.body.find(".toolbar .multiselection .toolbar-item.ungroup").removeClass("disabled")
};

_e.ungroupWidgets = function() {
    var undo = function() {};
    var redo = function() {};
    var _self = this;
    var container = this.activeSection.find("> .widget-container");
    if(!this.multiSelectedWidget) {
        return;
    }
    this.multiSelectedWidget.each(function() {
        var widget = $(this);
        if(widget.parent().is(".widget-container")) {
            return;
        }
        var wiElement = _self.getWidgetObject(widget);
        var style = wiElement.style;
        var oldGroup = wiElement.group;
        var oldstyle = $.extend({}, style);
        var ogel = widget.parent();
        var pos = wiElement.getPos();
        var border_color = widget.find(".widget-overlay").css('border-left-color');
        var actual_offset = ogel.pposition();
        actual_offset.left += ogel.leftRib(true, true, false);
        actual_offset.top += ogel.topRib(true, true, false);
        actual_offset.right += ogel.rightRib(true, true, false);
        actual_offset.bottom += ogel.bottomRib(true, true, false);
        if(wiElement.isLeft()) {
            style.left = (actual_offset.left + pos.left) + "px"
        }
        if(wiElement.isRight()) {
            style.right = (actual_offset.right + pos.right) + "px"
        }
        if(wiElement.isTop()) {
            style.top = (actual_offset.top + pos.top) + "px"
        }
        if(wiElement.isBottom()) {
            style.bottom = (actual_offset.bottom + pos.bottom) + "px"
        }
        var newStyle = $.extend({}, style);
        if(wiElement.percentage) {
            var parent = widget.closest(".widget-container");
            var total_w = parent.innerWidth();
            var total_h = parent.innerHeight();
            if(wiElement.isLeft()) {
                newStyle.left = parseFloat(newStyle.left) / total_w * 100 + "%"
            }
            if(wiElement.isTop()) {
                newStyle.top = parseFloat(newStyle.top) / total_h * 100 + "%"
            }
            if(wiElement.isRight()) {
                newStyle.right = parseFloat(newStyle.right) / total_w * 100 + "%"
            }
            if(wiElement.isBottom()) {
                newStyle.bottom = parseFloat(newStyle.bottom) / total_h * 100 + "%"
            }
        }
        var applier = function(style, group) {
            wiElement.applyStyle(style);
            wiElement.group = group
        };
        redo = redo.blend(function() {
            applier(newStyle, undefined);
            container.append(widget);
            widget.find(".widget-overlay").css('border-color', '')
        });
        undo = undo.intercept(function() {
            applier(oldstyle, oldGroup);
            ogel.append(widget);
            widget.find(".widget-overlay").css('border-color', border_color)
        });
        applier(style, undefined)
    });
    redo();
    this.setDirty({undo: undo, redo: redo}, undefined, undefined, this.multiSelectedWidget);
    this.body.find(".toolbar .multiselection .toolbar-item.ungroup").addClass("disabled")
};

_e.removeAllWidgets = function(section, dirtyHandler) {
    var _self = this;
    var widgets = section.find("." + _self.widgetClass);
    var old_states = [];
    var old_handler;
    var new_handler;
    $.each(widgets, function() {
        var uuid = _self.getWidgetObject($(this)).uuid;
        var _state = _self.removeWidget(section, uuid, true);
        old_states.push(_state);
        if(old_handler) {
            old_handler = old_handler.blend(_state.handler)
        } else {
            old_handler = _state.handler
        }

        if(new_handler) {
            new_handler = new_handler.blend(_state.handler)
        } else {
            new_handler = _state.handler
        }
    });
    var cur_states = old_states.collect(function() {
        return {widget: this.widget}
    });
    if(dirtyHandler) {
        old_states.handler = dirtyHandler.old;
        cur_states.handler = dirtyHandler.new
    }

    if(old_handler) {
        if (old_states.handler) {
            old_states.handler = old_states.handler.blend(old_handler)
        } else {
            old_states.handler = old_handler
        }
    }

    if(new_handler) {
        if (cur_states.handler) {
            cur_states.handler = cur_states.handler.blend(new_handler)
        } else {
            cur_states.handler = new_handler
        }
    }
    _self.setDirty("widget_relocate", old_states, cur_states);
};

_e.clearSavedData = function(resp) {
    var _self = this;
    this.pageBody.find("[modified-widget]").removeData("data-cache").removeAttr("modified-widget");
    if(resp.newWidgets) {
        $.each(resp.newWidgets, function() {
            _self.pageBody.find("#wi-" + this.uuid).removeData("data-cache").removeAttr("new-widget").attr("widget-id", this.id);
        })
    }
    _self.containerId = resp.containerId;
    _self.removedWidgets = [];
    if(resp.newDocks) {
        $.each(resp.newDocks, function() {
            _self.pageBody.find("#dock-" + this.uuid).attr("dock-id", this.id);
        })
    }
    _self.newDocks = [];
    _self.modifiedDocks = [];
    _self.removedDock = [];

    _self.removedHeader = [];
    _self.removedFooter = [];
};

_e.mask = function() {
    this.body.loader()
};

_e.unmask = function() {
    this.body.loader(false);
};

_e.onNewWidgetDrop = function(widget, afterUndoRedo) {
    var _self = this;
    this.populateWidgetSelector();
    this.setDirty("widget_relocate", {widget: widget, parent: $("<div></div>"), index: 0, handler: afterUndoRedo}, {widget: widget, parent: widget.parent(), index: widget.parent().find(">." + this.widgetClass).index(widget), handler: afterUndoRedo}, widget);
    var widgetType = widget.attr("widget-type");
    this.getWidgetObject(widget).render({type: widgetType, uuid: widget.attr("id").substring(3)}, function() {
        _self.selectWidget(widget);
    })
};

_e.getEmptyWidgetDom = function(widgetType) {
    return "<div class='"+ this.widgetClass +" widget-" + widgetType + "' cached-widget new-widget widget-type='" + widgetType + "'> " + $.i18n.prop(widgetType.minusCase()) + " " + $.i18n.prop("widget") + " </div>"
};

_e.initWidgetMultiSelection = function() {
    var _self = this;
    var has_capture_support = this.pageBody[0].setCapture;
    var mouse_is_downed = false;
    var mouse_start_x;
    var mouse_start_y;
    var dragged_zone;
    var masks = this.activeSection.find(".editable-area-overlay");
    var last_position;
    var mouse_moved = false;
    var mousemoveHandler = function(ev) {
        if(mouse_is_downed) {
            if(has_capture_support) {
                ev.target.setCapture()
            }
            _self.mouse_capture_id = 1;
            mouse_moved = true;
            var mouse_diff_x = ev.pageX - mouse_start_x;
            var mouse_diff_y = ev.pageY - mouse_start_y;
            dragged_zone.css(last_position = {
                left: mouse_diff_x < 0 ? mouse_start_x + mouse_diff_x : mouse_start_x,
                top: mouse_diff_y < 0 ? mouse_start_y + mouse_diff_y : mouse_start_y,
                width: mouse_diff_x * (mouse_diff_x < 0 ? -1 : 1),
                height: mouse_diff_y * (mouse_diff_y < 0 ? -1 : 1)
            })
        }
    };
    var mouseupHandler = function(ev) {
        bm.enableTextSelection(this.ownerDocument);
        mouse_is_downed = false;
        if(!mouse_moved) {
            return;
        }
        _self.ignore_ev_timestamp = ev.timeStamp;
        var drag_rect = dragged_zone.rect();
        dragged_zone.remove();
        if(last_position) {
            if(has_capture_support) {
                ev.target.releaseCapture()
            } else {
                _self.iframe[0].contentWindow.document.removeEventListener("mouseup", mouseupHandler, true);
                _self.iframe[0].contentWindow.document.removeEventListener("mousemove", mousemoveHandler, true)
            }
            delete _self.mouse_capture_id;
            mouse_moved = false;
            var widgets = _self.activeSection.find(_self.supportedWidgetSelector).not("." + _self.widgetClass + " ." + _self.widgetClass);
            var selectables = [];
            for(var i = 0; i < widgets.length ; i++) {
                var _this = $(widgets[i]);
                if(_this.is(":visible")) {
                    var wid = _this.rect();
                    if(bm.intersect(drag_rect, wid) >= 80) {
                        selectables.push(_this);
                    }
                }
            }
            if(selectables.length) {
                if(selectables.length == 1) {
                    _self.selectWidget(selectables[0])
                } else {
                    _self.selectMultiWidget(selectables)
                }
            }
            last_position = null
        }
    };
    masks.on("mousedown.multiselection", function(ev) {
        bm.disableTextSelection(this.ownerDocument);
        $(this).one("mouseup.prevent_selection", function() {
            bm.enableTextSelection(this.ownerDocument)
        });
        mouse_is_downed = true;
        mouse_start_x = ev.pageX;
        mouse_start_y = ev.pageY;
        dragged_zone = $("<div class='dragged-over-zone'></div>").appendTo(_self.pageBody).css({
            left: mouse_start_x,
            top: mouse_start_y
        });
        if(!has_capture_support) {
            _self.iframe[0].contentWindow.document.addEventListener("mouseup", mouseupHandler, true);
            _self.iframe[0].contentWindow.document.addEventListener("mousemove", mousemoveHandler, true)
        }
    });
    if(has_capture_support) {
        masks.off("mousemove.multiselection").on("mousemove.multiselection", mousemoveHandler).off("mouseup.multiselection").on("mouseup.multiselection", mouseupHandler)
    }
};

_e.destroyMultiSelection = function() {
    this.pageBody.find(".editable-area-overlay").off(".multiselection")
};

_e.getNewDroppedWidget = function(widgetType) {
    var uuid = bm.getUUID();
    var widget = $(this.getEmptyWidgetDom(widgetType));
    widget.attr("id", "wi-" + uuid);
    widget.attr("widget-type", widgetType);
    return new app.tabs.edit_content.Widget(this, {
        uuid: uuid,
        elm: widget,
        type: widgetType,
        css: new CssParser("")
    });
};

_e.getCssRule = function(css, ruledef) {
    var rule;
    if(this.activeMedia) {
        rule = css.getRule(this.activeMedia, ".responsive " + ruledef);
        if(!rule) {
            rule = css.getRule(ruledef)
        }
    } else {
        rule = css.getRule(ruledef)
    }
    return rule
};

_e.getCssValue = function(css, ruledef, attribute, media) {
    var rules = [];
    media = media != undefined ? media : this.activeMedia;
    if(media) {
        var rule = css.getRule(media, ".responsive " + ruledef);
        if(rule) {
            rules.push(rule)
        }
    }
    var rule = css.getRule(ruledef);
    if(rule) {
        rules.push(rule)
    }
    function _getAttribute(attribute) {
        var value = rules[0].getAttribute(attribute);
        if(!value && rules.length == 2) {
            return rules[1].getAttribute(attribute)
        }
        return value
    }
    if(rules.length) {
        if($.isArray(attribute)) {
            var values = {};
            attribute.collect(function() {
                values["" + this] = _getAttribute(this)
            });
            return values;
        } else {
            return _getAttribute(attribute)
        }
    }
    return null;
};

_e.addRule = function(css, rule) {
    var medias = [];
    if(this.isResponsive) {
        if(this.activeMedia) {
            var media = css.getMedia(this.activeMedia);
            if(!media) {
                media = css.addMedia(this.activeMedia)
            }
            medias.push({media: media, rule: ".responsive " + rule})
        }

        var global_rule = css.getRule(rule);
        if(!this.activeMedia || !global_rule) {
            medias.push({media: css, rule: rule})
        }
    } else {
        medias.push({media: css, rule: rule})
    }
    medias.every(function() {
        this.media.addRule(this.rule)
    })
};

_e.removeRule = function(css, uuid, rule, allMedia) {
    var medias = [];
    if(allMedia) {
        medias.push(css);
        var all = css.getMedias();
        if(all.length) {
            medias.pushAll(all)
        }
    } else if(this.activeMedia) {
        var media = css.getMedia(this.activeMedia);
        if(media) {
            medias.push(media)
        }
    } else {
        medias.push(css)
    }
    var removes = [];
    medias.every(function() {
        var _rule = this.removeRule(rule);
        if(_rule) {
            removes.push([this, _rule])
        }
    });
    this.persistCss(uuid, css);
    return removes
};

_e.persistCss = function(uuid, css) {
    var tag_def = uuid ? "#style-store-" + uuid : "#stored-css";
    var tag = this.iframeContent.find("head " + tag_def);
    if(!tag.length) {
        tag = $("<style id='style-store-" + uuid + "'></style>");
        this.iframeContent.find("head").append(tag)
    }
    tag.text(css.toString())
};

_e.updateCss = function(css, uuid, rule, addedAttributes, removedAttributes, mediadef, copy_global) {
    var medias = [];
    var rules = [];
    mediadef = mediadef != undefined ? mediadef : this.activeMedia;
    if(mediadef) {
        var media = css.getMedia(mediadef);
        if(!media) {
            media = css.addMedia(mediadef)
        }
        medias.push({media: media, rule: ".responsive " + rule});

        if(copy_global) {
            medias.push({media: css, rule: rule})
        }
    } else {
        medias.push({media: css, rule: rule})
    }

    medias.every(function() {
        var _rule = this.media.getRule(this.rule);
        if(!_rule) {
            _rule = this.media.addRule(this.rule)
        }
        rules.push(_rule)
    });

    if(addedAttributes) {
        rules.every(function() {
            this.setAttribute(addedAttributes)
        })
    }

    if(removedAttributes) {
        rules.every(function() {
            this.removeAttribute(removedAttributes)
        })
    }

    this.persistCss(uuid, css)
};

_e.getMergableGrids = function(grid, direction) {
    function isMergable(a, b) {
        return (a.children(".widget").length ? b.children(".grid-block, .page-content").length == 0 : a.children(".grid-block, .page-content").length == 0) || (b.children(".widget").length ? a.children(".grid-block, .page-content").length == 0 : b.children(".grid-block, .page-content").length == 0)
    }

    if(direction == "left") {
        if(grid.is(".no-fixed-left, .v-split:first-child")) {
            return;
        }
        if(grid.is(".no-fixed-tight, .l-fixed-right, .r-fixed-right, .v-split")) {
            var omedar = grid.prev();
            if(isMergable(grid, omedar)) {
                return [omedar, grid]
            }
            return;
        }
        function LoopTillRight(grid) {
            if(grid.is(".no-fixed-left, .l-fixed-left, .r-fixed-left")) {
                var parent = grid.parent();
                return LoopTillRight(parent)
            }
            if(grid.is(".page-content, .widget-container")) {
                return;
            }
            if(grid.is(".no-fixed-right, .l-fixed-right, .r-fixed-right")) {
                var omedar = grid.prev();
                if(isMergable(grid, omedar)) {
                    return [omedar, grid]
                }
                return;
            }

        }
        return LoopTillRight(grid)
    } else {
        if(grid.is(".no-fixed-right, .v-split:last-child")) {
            return;
        }
        if(grid.is(".no-fixed-left, .l-fixed-left, .r-fixed-left, .v-split")) {
            var omedar = grid.next();
            if(isMergable(grid, omedar)) {
                return [grid, omedar]
            }
            return;
        }
        function LoopTillLeft(grid) {
            if(grid.is(".no-fixed-right, .l-fixed-right, .r-fixed-right")) {
                var parent = grid.parent();
                return LoopTillLeft(parent)
            }
            if(grid.is(".page-content, .widget-container")) {
                return;
            }
            if(grid.is(".no-fixed-left, .l-fixed-left, .r-fixed-left")) {
                var omedar = grid.prev();
                if(isMergable(grid, omedar)) {
                    return [grid, omedar]
                }
                return;
            }

        }
        return LoopTillLeft(grid)
    }
};

_e.getGridMinResizable = function(elm) {
    var width;
    var frames = elm.leftRib(false) + elm.rightRib(false);
    if(elm.hasClass("v-split-container")) {
        var children = elm.children();
        var max = 0;
        for(var i = 0; i < children.length; i++) {
            var temp = this.getGridMinResizable(children.eq(i));
            if(max < temp) {
                max = temp;
            }
        }
        width = max;
    } else if(elm.is(".l-fixed-container, .r-fixed-container")) {
        var fixed = elm.children(".l-fixed-left, .r-fixed-right");
        var f_display = this.getCssValue(this.css, "#" + fixed.attr("id"), "display");
        var display = this.getCssValue(this.css, "#" + elm.attr("id"), "display");
        if(display == "block") {
            var width1 = f_display != "none" ? this.getGridMinResizable(fixed, true) : 0;
            var width2 = this.getGridMinResizable(elm.children(".l-fixed-right, .r-fixed-left"), true);
            return width1 + width2
        } else {
            var fixed_flex = f_display != "none" ? this.getCssValue(this.css, "#" + fixed.attr("id"), "flex") : "0 0 0";
            width = parseFloat(fixed_flex.split(" ")[2]) + this.getGridMinResizable(elm.children(".l-fixed-right, .r-fixed-left"))
        }
    } else {
        width = 100
    }
    return frames + width;
};

_e.unselectMultiWidget = function(widget) {
    if(widget) {
        this.multiSelectedWidget = this.multiSelectedWidget.not(widget);
        widget.find("> .multi-selection-overlay").remove();
        if(this.multiSelectedWidget.length) {
            if(!this.multiSelectedWidget.parent().is(".widget-group")) {
                this.body.find(".toolbar .multiselection .toolbar-item.ungroup").addClass("disabled")
            }
            return;
        }
    }
    this.body.find(".toolbar .multiselection .toolbar-item").addClass("disabled");
    this.multiSelectedWidget.find("> .multi-selection-overlay").remove();
    delete this.multiSelectedWidget
};

_e.unselectWidget = function(options) {
    if(!this.selectedWidget && !this.multiSelectedWidget) {
        return;
    }
    if(this.multiSelectedWidget) {
        this.unselectMultiWidget();
        return;
    }
    this.selectedWidget.find(".widget-overlay").removeClass("selected");
    this.pageBody.find(".bounding-box-line-x, .bounding-box-line-y, .bounding-box-line-x-2, .bounding-box-line-y-2").hide();
    this.widgetXoffset.stepper("disable");
    this.widgetYoffset.stepper("disable");
    this.widgetHeight.stepper("disable");
    this.widgetWidth.stepper("disable");
    this.selectedWidget = undefined;
    this.widgetSelector.chosen("val", "");
    this.settingBlock.find(".tool-icon.remove-widget").addClass("disabled");
    this.enableWidgetDimensionSpinners(false);
    if(!options || !options.next_select_pending) {
        this.leftbar_tab.tabify("activate", "widget")
    }
    if(!this.activeSection.is(".body")) {
        this.settingBlock.find(".widget-ref-unit-selector").hide()
    }
    this.updateWidgetConfig()
};

_e.triggerWidgetConfigChange = function () {
    var active_tab_content_container = this.settingBlock.find(".widget-prop-configure .active-prop-view-container");

    active_tab_content_container.find(".title-input input").trigger("change");
    active_tab_content_container.find(".clazz-input input").trigger("change");
    active_tab_content_container.find(".widget-specific-config").trigger("change");

    var jsArea = active_tab_content_container.find("textarea")
    if(jsArea.length) {
        var uuid = this.selectedWidget.attr("id").substring(3);
        var wiElement = this.cachedWidgets[uuid];
        wiElement && wiElement.setJs(jsArea.value);
    }
}

_e.getWidgetObject = function(widget) {
    var _self = this;
    var wiElement;
    var uuid = widget.attr("id").substring(3);
    if(widget.is('[cached-widget]')) {
        wiElement = this.cachedWidgets[uuid];
    } else {
        var style = _self.iframeContent.find("#style-store-" + uuid);
        var css = new CssParser(style.length ? style[0].innerHTML : "");
        css.parse();
        wiElement = new app.tabs.edit_content.Widget(this, {uuid: uuid, elm: widget, type: widget.attr("widget-type"), style: null, css: css});
        wiElement.updateMemoryStyle();
        _self.cachedWidgets[uuid] = wiElement;
        widget.attr("cached-widget", true);
        if(widget.parent().is(".widget-group")) {
            wiElement.group = widget.parent().attr("id").substring(6)
        }
    }
    return wiElement;
};

_e.createWidgetOverlay = function(widget) {
    var _self = this;
    var widgetOverlay = "<div class='widget-overlay' tabindex='0'><div class='widget-toolbar'><span class='tool-icon remove'></span></div></div>";
    widget.append(widgetOverlay);
    widgetOverlay = widget.find(".widget-overlay");
    if(this.selectedWidget && this.selectedWidget[0] == widget[0]) {
        widgetOverlay.addClass("selected")
    }
    if(!this.activeSection.hasClass("body")) {
        widgetOverlay.append('<div class="widget-bounding-box top-left" title="' + $.i18n.prop("bind.widget.position.relative.top.left") + '"></div><div class="widget-bounding-box top-right" title="' + $.i18n.prop("bind.widget.position.relative.top.right") + '"></div><div class="widget-bounding-box bottom-right" title="' + $.i18n.prop("bind.widget.position.relative.bottom.right") + '"></div><div class="widget-bounding-box bottom-left" title="' + $.i18n.prop("bind.widget.position.relative.bottom.left") + '"></div>');

        widgetOverlay.bind("keydown.key_up", function() {
            setTimeout(function() {
                var obj = _self.getWidgetObject(_self.selectedWidget);
                var prevVal = _self.widgetYoffset.val();
                var currentVal = +prevVal + (obj.isTop() ? -1 : 1) * 1;
                _self.widgetYoffset[0].isilent(currentVal).triggerHandler("ichange", [currentVal, prevVal]);
            }, 2);
            return false
        }).bind("keydown.key_down", function() {
            setTimeout(function() {
                var obj = _self.getWidgetObject(_self.selectedWidget);
                var prevVal = _self.widgetYoffset.val();
                var currentVal = +prevVal + (obj.isTop() ? 1 : -1) * 1;
                _self.widgetYoffset[0].isilent(currentVal).triggerHandler("ichange", [currentVal, prevVal]);
            }, 2);
            return false
        }).bind("keydown.key_left", function() {
            setTimeout(function() {
                var obj = _self.getWidgetObject(_self.selectedWidget);
                var prevVal = _self.widgetXoffset.val();
                var currentVal = +prevVal + (obj.isLeft() ? -1 : 1) * 1;
                _self.widgetXoffset[0].isilent(currentVal).triggerHandler("ichange", [currentVal, prevVal]);
            }, 2);
            return false
        }).bind("keydown.key_right", function() {
            setTimeout(function() {
                var obj = _self.getWidgetObject(_self.selectedWidget);
                var prevVal = _self.widgetXoffset.val();
                var currentVal = +prevVal + (obj.isLeft() ? 1 : -1) * 1;
                _self.widgetXoffset[0].isilent(currentVal).triggerHandler("ichange", [currentVal, prevVal]);
            }, 2);
            return false
        });

        var common = {
            shim: true,
            proxy: function(overlay) {
                return _self.iframeWindow.$(overlay.closest("." + _self.widgetClass)[0])
            },
            start: function(hash) {
                var widget = hash.elm.closest("." + _self.widgetClass);
                var wiElement = _self.getWidgetObject(widget);
                this.temp.start_style = $.extend({}, wiElement.style)
            },
            stop: function(hash) {
                var widget = hash.elm.closest("." + _self.widgetClass);
                var wiElement = _self.getWidgetObject(widget);
                if(wiElement.percentage) {
                    var style = wiElement.style;
                    var total_w = wiElement.elm.parent().innerWidth();
                    var total_h = wiElement.elm.parent().innerHeight();
                    if(wiElement.isLeft()) {
                        style.left = parseFloat(style.left) / total_w * 100 + "%"
                    }
                    if(wiElement.isRight()) {
                        style.right = parseFloat(style.right) / total_w * 100 + "%"
                    }
                    if(wiElement.isTop()) {
                        style.top = parseFloat(style.top) / total_h * 100 + "%"
                    }
                    if(wiElement.isBottom()) {
                        style.bottom = parseFloat(style.bottom) / total_h * 100 + "%"
                    }
                }
                wiElement.applyStyle(wiElement.style);
                _self.setDirty("widget_reposition", {widget: widget, style: this.temp.start_style}, {widget: widget, style: $.extend({}, wiElement.style)}, widget);
                widget.css({
                    left: "",
                    right: "",
                    top: "",
                    bottom: "",
                    width: "",
                    height: ""
                });
                wiElement.positionReferenceLine();
                _self.setDimensionSpinnerValues(wiElement)
            },
            doing: function(hash) {
                var widget = hash.elm.closest("." + _self.widgetClass);
                var wiElement = _self.getWidgetObject(widget);
                var style = wiElement.style;
                if(wiElement.isLeft()) {
                    style.left = widget.css("left")
                }
                if(wiElement.isRight()) {
                    style.right = widget.css("right")
                }
                if(wiElement.isTop()) {
                    style.top = widget.css("top")
                }
                if(wiElement.isBottom()) {
                    style.bottom = widget.css("bottom")
                }
                if(style.width != "auto") {
                    style.width = widget.css("width")
                }
                style.height = widget.css("height");
                wiElement.positionReferenceLine();
                _self.setDimensionSpinnerValues(wiElement)
            }
        };

        widgetOverlay.draggable($.extend(common, {
            cancel: ".widget-toolbar, .widget-bounding-box, .bmui-resize-handle",
            move: common.doing,
            containment: function(widget) {
                if(widget.hasparent('.widget-container')) {
                    return [0, 0, 0, 0]
                }
                var position = widget.parent().pposition();
                return [-1 * position.left, -1 * position.right, -1 * position.top, -1 * position.bottom]
            }
        })).resizable($.extend(common, {
            resize: common.doing,
            limit: [50, undefined, 25, undefined],
            containment: function(widget, axis) {
                if(widget.hasparent('.widget-container')) {
                    return [0, 0]
                }
                var position = widget.parent().pposition();
                return axis == "x" ? [-1 * position.left, -1 * position.right] : [-1 * position.top, -1 * position.bottom]
            }
        }));

        if(this.selectedWidget && this.selectedWidget[0] == widget[0]) {
            var obj = this.getWidgetObject(this.selectedWidget);
            obj.highlightReferenceSelection()
        }
    }
};

_e.selectMultiWidget = function(widgets) {
    var selecteds = $();
    widgets.every(function() {
        $(this).append("<div class='multi-selection-overlay'></div>");
        selecteds = selecteds.add(this)
    });
    if(this.multiSelectedWidget) {
        this.multiSelectedWidget = this.multiSelectedWidget.add(widgets);
        if(widgets.parent().is(".widget-group")) {
            this.body.find(".toolbar .multiselection .toolbar-item.ungroup").removeClass("disabled")
        }
    } else {
        this.multiSelectedWidget = selecteds;
        this.body.find(".toolbar .multiselection .toolbar-item").removeClass("disabled");
        if(!this.multiSelectedWidget.parent().is(".widget-group")) {
            this.body.find(".toolbar .multiselection .toolbar-item.ungroup").addClass("disabled")
        }
    }
};

_e.selectWidget = function(widget, options) {
    if(this.selectedWidget && this.selectedWidget[0] == widget[0]) {
        return;
    }
    this.unselectWidget({next_select_pending: true});
    this.selectedWidget = widget;
    this.selectedWidget.find("> .widget-overlay").addClass("selected");
    var uuid = widget.attr("id");
    if(uuid) {
        uuid = uuid.substring(3)
    }
    this.settingBlock.find(".tool-icon.remove-widget").removeClass("disabled");
    if(!options || options.widget_selector_update == undefined || options.widget_selector_update) {
        this.widgetSelector.chosen("val", "wi-" + uuid);
    }
    if(this.activeSection.hasClass("body")) {
        var wiElement;
        var id = widget.attr("id");
        if(id) {
            if(!widget.is('[cached-widget]')) {
                var style = this.iframeContent.find("#style-store-" + uuid);
                var css = new CssParser(style.length ? style[0].innerHTML : "#style-store-" + uuid + "{}");
                css.parse();
                wiElement = new app.tabs.edit_content.Widget(this, {uuid: uuid, elm: widget, type: widget.attr("widget-type"), style: null, css: css});
                this.cachedWidgets[uuid] = wiElement;
                widget.attr("cached-widget", true);
            }
        }
    } else {
        this.enableWidgetDimensionSpinners(true);
        var wiElement = this.getWidgetObject(widget);
        if(!wiElement) {
            return;
        }
        this.settingBlock.find(".widget-ref-unit-selector").show();
        var refValue = wiElement.percentage ? "%" : "px";
        this.settingBlock.find(".widget-ref-unit-selector select").chosen("val", refValue);
        wiElement.positionReferenceLine();
        this.setDimensionSpinnerValues(wiElement);
        wiElement.highlightReferenceSelection()
    }
    this.updateWidgetConfig(uuid, undefined, true)
};

_e.setDimensionSpinnerValues = function(wiElement) {
    var widget = wiElement.elm;
    var pos = wiElement.getPos();
    if(wiElement.percentage) {
        var total_w = $(widget[0].offsetParent).innerWidth();
        var total_h = $(widget[0].offsetParent).innerHeight();
        pos.left = pos.left / total_w * 100;
        pos.top = pos.top / total_h * 100;
        pos.right = pos.right / total_w * 100;
        pos.bottom = pos.bottom / total_h * 100
    }
    this.widgetWidth[0].isilent(widget.outerWidth(true));
    this.widgetHeight[0].isilent(widget.outerHeight(true));
    var x_value = wiElement.isLeft() ? pos.left : pos.right;
    var y_value = wiElement.isTop() ? pos.top : pos.bottom;
    this.widgetXoffset[0].isilent(Math.round(x_value));
    this.widgetYoffset[0].isilent(Math.round(y_value));
};

_e.enableWidgetDimensionSpinners = function(enable) {
    this.widgetXoffset.stepper(enable ? "enable" : "disable");
    this.widgetYoffset.stepper(enable ? "enable" : "disable");
    this.widgetHeight.stepper(enable ? "enable" : "disable");
    this.widgetWidth.stepper(enable ? "enable" : "disable")
};

_e.initWidgetDimensionSpinners = function() {
    var _self = this;
    this.widgetXoffset.ichange(600, function(event, currentValue, prevValue) {
        currentValue = +currentValue;
        if(isNaN(currentValue)) {
            this.isilent(prevValue);
            return;
        }
        var widget = _self.selectedWidget;
        var widgetObj = _self.getWidgetObject(widget);
        widgetObj.setX(currentValue)
    });
    this.widgetYoffset.ichange(600, function(event, currentValue, prevValue) {
        currentValue = +currentValue;
        if(isNaN(currentValue)) {
            this.isilent(prevValue || "");
            return;
        }
        var widget = _self.selectedWidget;
        var widgetObj = _self.getWidgetObject(widget);
        widgetObj.setY(currentValue)
    });
    this.widgetHeight.ichange(600, function(event, currentValue, prevValue) {
        currentValue = +currentValue;
        if(isNaN(currentValue)) {
            this.isilent(prevValue || "");
            return;
        }
        var widget = _self.selectedWidget;
        var widgetObj = _self.getWidgetObject(widget);
        widgetObj._setWH(currentValue, 25, "top", "bottom", "height", "Top", "Bottom", "Height")
    });
    this.widgetWidth.ichange(600, function(event, currentValue, prevValue) {
        currentValue = +currentValue;
        if(isNaN(currentValue)) {
            this.isilent(prevValue || "");
            return;
        }
        var widget = _self.selectedWidget;
        var widgetObj = _self.getWidgetObject(widget);
        widgetObj._setWH(currentValue, 50, "left", "right", "width", "Left", "Right", "Width")
    });
};

_e.advanceConfigureWidget = function() {
    var _self = this;
    var widget = this.selectedWidget;
    var uuid = this.selectedWidget.attr("id").substring(3);
    var data;
    var widgetType = widget.attr("widget-type");
    var url = this.widgetConfigUrl + widgetType.camelCase();
    var widgetId = widget.attr("widget-id");
    data = {};
    if(widgetId) {
        data.widgetId = widgetId;
    }
    var cache = widget.data("data-cache");
    if(cache) {
        data.cache = cache;
    }
    var type = widget.attr("widget-type");
    var isIsTemplateWidget = widget.attr("external-widget") == "true";
    if(isIsTemplateWidget) {
        data.isTemplateWidget = isIsTemplateWidget;
        data.type = type;
        data.uuid = uuid;
    }
    var widgetInstance;
    var params = {status: true};
    app.global_event.trigger("before-render-"+widgetType+"-widget-config", [params, this]);
    if(params.status === false) {
        return
    }
    _self.renderCreatePanel(url, $.i18n.prop("configure.widget"), $.i18n.prop(widgetType.minusCase()), data, {
        ajax: {
            show_success_status: false
        },
        auto_clear_dirty: false,
        success: function(resp) {
            var widgetObject = _self.getWidgetObject(widget);
            widgetObject.afterConfigChange();
            widgetObject.updateContent(resp.html, resp.serialized);
            _self.setDirty("widget_update", {widget: widget, cache: cache, obj: widgetInstance}, {widget: widget, cache: resp.serialized, obj: widgetInstance}, widget);
            _self.updateWidgetConfig(uuid)
        },
        beforeSubmit: function(form, extraData) {
            var cacheWidget = cache ? JSON.parse(cache) : null;
            $.extend(extraData, {
                widgetId: widgetId,
                widgetType: widgetType,
                uuid: uuid,
                containerId: _self.containerId,
                containerType: _self.containerType,
                params: widgetInstance.getParams()
            });
            if(cacheWidget) {
               extraData.clazz = cacheWidget.clazz
            }
            if(typeof widgetInstance.beforeSubmit == "function") {
                var retVal = widgetInstance.beforeSubmit(form, extraData);
                if(retVal === false) {
                    return false;
                }
            }
        },
        clazz: "widget-edit widget-" + widgetType.minusCase() + "-config",
        content_loaded: function (form, panel) {
            var config = {content: this, widget: widget, popup: panel, editor: _self};
            widgetInstance = new app.widget[widgetType](config);
            widgetInstance.init(form);
        }
    });
};

_e.editWidgetCss = function(wiElement) {
    var container = this.settingBlock.find(".widget-mode.accordion-item .active-prop-view-container");
    container.append('<div class="sidebar-group widget-css-operators">' + (this.activeMedia ?
        '<span class="tool-icon copy-other-media" title="' + $.i18n.prop("copy.all.from.other.media") + '"></span>' : '') +
        (this.isResponsive ? '<span class="tool-icon remove remove-media" title="' + $.i18n.prop("remove.media") + '"></span><span class="tool-icon hide-in-resolutions" title="' + $.i18n.prop("hidden.in.resolutions") + '"></span>' : '') +
        '<span class="tool-icon show-hide" title="' + $.i18n.prop("show.hide.toggle") + '"></span>\
        </div>');
    this.handleCssEditorTools(container);
    this.editCss(container, wiElement, "<base>", "#wi-" + wiElement.uuid);
};

_e.editCss = function(editDom, element, baseNotation, selectorPrefix, restrictedRules) {
    var _self = this;
    var restrictedAttrs = ["position", "display", "left", "right", "top", "bottom", "height", "width", "flex", "max-width"];
    var _restrictedRules = [selectorPrefix, ".responsive " + selectorPrefix];
    if(restrictedRules) {
        _restrictedRules.pushAll(restrictedRules)
    }
    var ruleGroupDom = $("<div class='sidebar-group'><div class='sidebar-group-label'>" + $.i18n.prop("rules") + "</div><div class='sidebar-group-body'><select class='rule sidebar-input single-action' name='rule'></select> <span class='tool-icon remove remove-rule'></span></div></div>");
    var ruleSelector = editDom.append(ruleGroupDom).find("select.rule").chosen({disable_search: true});
    var media = this.activeMedia ? element.css.getMedia(this.activeMedia) : element.css;
    var prefixToReplace = this.activeMedia ? ".responsive " + selectorPrefix : selectorPrefix;
    if(media && media.rulesets.length) {
        $.each(media.rulesets, function() {
            if(this.type != "media") {
                var selectors = this.selectors.collect(function() {
                    return this.replace(prefixToReplace, baseNotation);
                }).join(", ");
                ruleSelector.chosen('add', {value: this.selectors.join(", "), text: selectors})
            }
        })
    } else {
        ruleSelector.chosen('add', {value: prefixToReplace, text: baseNotation})
    }
    var ruleRemove = ruleSelector.siblings('.remove-rule');
    ruleGroupDom.append("<div class='sidebar-group-body'><input type='text' placeholder='" + $.i18n.prop("add.new.rule") + "' name='new-rule' class='sidebar-input single-action new-rule'> <span class='add tool-icon add-rule'></span></div>");
    ruleRemove.click(function() {
        if($(this).is(".disabled")) {
            return;
        }
        var selector = ruleSelector.val();
        var old_cache = element.css.toString();
        _self.removeRule(element.css, element.uuid, selector.split(", "));
        if(ruleSelector[0].options.length == 1) {
            ruleSelector.chosen('add', {value: prefixToReplace, text: baseNotation})
        }
        ruleSelector.chosen('remove', selector).trigger("change");
        _self.setDirty("css_change", {css: old_cache, uuid: element.uuid, obj: element}, {css: element.css.toString(), uuid: element.uuid, obj: element}, element.elm);
    });
    var addRuleInput = editDom.find("input[name='new-rule']");
    editDom.find(".add-rule").click(function() {
        var newRule = addRuleInput.val();
        if(newRule == "") {
            return
        }
        var addedRules = [];
        ruleSelector.find("option").each(function() {
            addedRules.push(this.value)
        });
        addRuleInput.val("");
        var newRuleSelector;
        if(newRule.contains(",")) {
            newRuleSelector = newRule.split(", ").collect(function() {
                return (_self.activeMedia ? ".responsive " : "") + selectorPrefix + " " + this.trim();
            })
        } else {
            newRuleSelector = (_self.activeMedia ? ".responsive " : "") + selectorPrefix + " " + newRule.trim()
        }
        var selectValue = $.isArray(newRuleSelector) ? newRuleSelector.join(", ") : newRuleSelector;
        if(addedRules.contains(selectValue)) {
            ruleSelector.select("val", selectValue).trigger("change");
            return;
        }
        var old_cache = element.css.toString();
        _self.addRule(element.css, newRuleSelector);
        ruleSelector.chosen('add', {value: selectValue, text: newRule.split(", ").collect(function() {
            return baseNotation + " " + this.trim();
        }).join(", "), selected: true}).trigger("change");
        _self.setDirty("css_change", {css: old_cache, uuid: element.uuid, obj: element}, {css: element.css.toString(), uuid: element.uuid, obj: element}, element.elm);
    });
    addRuleInput.on("keyup.key_return", function() {
        editDom.find(".add-rule").trigger("click")
    });
    var attributes = $("<div class='sidebar-group'><div class='sidebar-group-label'>" + $.i18n.prop('attributes') + "</div><div class='attribute sidebar-group-body'><table></table></div></div>");
    editDom.append(attributes);
    var table = attributes.find("table");
    ruleSelector.change(function() {
        if(_restrictedRules.contains(this.value)) {
            ruleRemove.addClass("disabled")
        } else {
            ruleRemove.removeClass("disabled")
        }
        table.empty();
        var media = _self.activeMedia ? element.css.getMedia(_self.activeMedia) : element.css;
        if(media) {
            var selectors = this.value.split(", ");
            var rule = media.getRule(selectors);
            if(rule) {
                var attrs = rule.attributes;
                for(var i = 0; i < attrs.length ; i ++) {
                    if(restrictedAttrs.contains(attrs[i].key)) {
                        continue;
                    }
                    table.append("<tr class='attribute'><td> " + attrs[i].key + ": " + attrs[i].value +"; </td><td class='remove'><span class='tool-icon remove' attr-name = '" + attrs[i].key + "'></span></td></tr>")
                }
            }
        }
    }).trigger("change");
    table.delegate("span.remove", "click ", function() {
        var _this = $(this);
        var selector = ruleSelector.val();
        var attrName = _this.attr("attr-name");
        var old_cache = element.css.toString();
        _self.updateCss(element.css, element.uuid, selector.split(", ").collect(function() {
            return _self.activeMedia ? this.substring(12) : this
        }), null, attrName);
        _this.closest("tr").remove();
        if(table.find("tr").length == 0) {
            table.empty(); //in some browser tbody tag exists
        }
        _self.setDirty("css_change", {css: old_cache, uuid: element.uuid, obj: element}, {css: element.css.toString(), uuid: element.uuid, obj: element}, element.elm);
    });
    attributes.append("<div class='sidebar-group-body'><input type='text' placeholder='" + $.i18n.prop("add.new.attribute") + "' name='new-attribute' class='sidebar-input single-action new-attribute'> <span class='add tool-icon add-attribute'></div>");
    var addAttributeInput = editDom.find("input[name='new-attribute']");
    editDom.find(".add-attribute").click(function() {
        var newAttribute = addAttributeInput.val().match(/([^:]+):([^;]+);?/);
        if(!newAttribute) {
            return;
        }
        addAttributeInput.val("");
        var attrName = newAttribute[1].trim();
        var attrValue = newAttribute[2].trim();
        if(restrictedAttrs.contains(attrName)) {
            bm.notify($.i18n.prop("specified.attribute.restricted"), "error");
            return;
        }
        var selector = ruleSelector.val();
        var attr = {};
        attr[attrName] = attrValue;
        var old_cache = element.css.toString();
        _self.updateCss(element.css, element.uuid, selector.split(", ").collect(function() {
            return _self.activeMedia ? this.substring(12) : "" + this
        }), attr);
        var exists = false;
        table.find("tr.attribute span.remove").each(function() {
            if($(this).attr("attr-name") == attrName) {
                $(this).parent().siblings().text(attrName + ": " + attrValue + ";");
                exists = true;
                return false;
            }
        });
        if(!exists) {
            table.append("<tr class='attribute'><td> " + attrName + ": " + attrValue +"; </td><td class='remove'><span class='tool-icon remove' attr-name = '" + attrName + "'></td></tr>");
        }
        _self.setDirty("css_change", {css: old_cache, uuid: element.uuid, obj: element}, {css: element.css.toString(), uuid: element.uuid, obj: element}, element.elm);
    });
    addAttributeInput.on("keyup.key_return", function() {
        editDom.find(".add-attribute").trigger("click")
    })
};

_e.removeWidget = function(section, uuid, group) {
    var _self = this;
    var state = {};
    var cachedElement = this.cachedWidgets[uuid];
    var widget = this.pageBody.find('#wi-' + uuid);
    if(this.selectedWidget && this.selectedWidget[0] == widget[0]) {
        this.unselectWidget()
    }
    if(cachedElement) {
        state.cache = this.cachedWidgets[uuid];
        var templateServerUuid = cachedElement.templateServerUuid ? cachedElement.templateServerUuid : (cachedElement.elm.is("[external-widget=true]") ? uuid : null);
        if(!cachedElement.elm.is("[new-widget]") || templateServerUuid) {
            this.removedWidgets.push({uuid : uuid, section: section.attr("section"), templateServerUuid: templateServerUuid});
            var topIndex = this.removedWidgets.length;
            state.removeCacheIndex = topIndex
        }
    }
    state.index = widget.parent().find("> ." + this.widgetClass).index(widget);
    state.parent = widget.parent();
    widget.detach();
    var handler = function() {
        if(section[0] == _self.activeSection[0]) {
            _self.populateWidgetSelector();
        }
    };
    handler();
    state.handler = handler;
    state.widget = widget;
    if(group) {
        return state
    } else {
        var states = [state, {widget: state.widget, handler: handler}];
        this.setDirty("widget_relocate", states[0], states[1]);
        return states
    }
};

_e.setDirty = function(type, state_old, state_new, widget) {
    var _self = this;
    if(arguments.length > 0) {
        if(state_old && state_new) {
            state_old.section = this.activeSection;
            state_new.section = this.activeSection
        } else {
            type.section = this.activeSection
        }
        this.undo_redo.push(type, state_old, state_new);
        if(widget) {
            widget.each(function() {
                var _widget = $(this);
                if(_widget.is("." + _self.widgetClass)) {
                    if(!_widget.is("[new-widget]")) {
                        _widget.attr("modified-widget", true);
                    }
                } else {
                    var uuid = widget.attr("id").substring(5);
                    if(widget.attr("dock-id") && !_self.modifiedDocks.contains(uuid)) {
                        _self.modifiedDocks.push(uuid)
                    }
                }
            })
        }
    }
    this.enableRevert = true;
    this.body.find(".toolbar .save").removeClass("disabled");
    app.tabs.edit_content._super.setDirty.call(this);
};

_e.clearDirty = function(persistUndoRedo) {
    if(!persistUndoRedo) {
        this.undo_redo.reset()
    }
    app.tabs.edit_content._super.clearDirty.call(this);
    this.enableRevert = false;
    this.body.find(".toolbar .save").addClass("disabled");
};

_e.addDock = function() {
    var _self = this;
    var uuid = bm.getUUID();
    var dockElement = {};
    dockElement.uuid = uuid;
    var style = {
        top: "0px",
        left: "0px",
        right: "0px",
        bottom: "auto",
        width: "auto",
        height: "200px"
    };
    var css = new CssParser();
    dockElement.css = css;
    var dockDom = dockElement.elm = $("<div class='dockable' cached-dock new-dock section='" + uuid + "' id='dock-" + uuid + "'><div class='widget-container'></div><div class='dock-mask'></div></div>");
    _self.updateCss(css, uuid, "#dock-" + dockElement.uuid, style, undefined, undefined, true);
    _self.updateCss(css, uuid, "#dock-" + dockElement.uuid + " > .widget-container", {height: "100%"}, undefined, undefined, true);
    var currentSectionUUID = _self.activeSectionSelector.chosen('val');
    var state = {
        undo: function() {
            _self.activeSectionSelector.chosen('val', currentSectionUUID).chosen('remove', uuid).trigger("change");
            dockDom.detach();
            _self.newDocks.pop()
        }, redo: function() {
            _self.pageBody.append(dockDom);
            _self.cacheDock(uuid, dockElement);
            _self.newDocks.push(dockElement);
            _self.activeSectionSelector.chosen('add', {value: uuid, text: "Dock - " + uuid, selected: true}).trigger("change");
        }
    };
    _self.setDirty(state);
    state.redo()
};

_e.editDockCss = function(_switch) {
    var container = this.settingBlock.find(".dock-css.accordion-item .dock-css-view-container").empty();
    if(!this.activeSection.is(".dockable")) {
        return;
    }
    if(this.leftbar_tab.tabify("getActive") == "widget") {
        if(!_switch) {
            return
        }
        this.leftbar_tab.tabify("activate", "setting")
    }
    var current = this.settingBlock.find(".leftbar-accordion").accordion("current");
    if(!current.is(".dock-css")) {
        if(!_switch) {
            return;
        }
        this.settingBlock.find(".leftbar-accordion").accordion("expand", "dock-css-header")
    }
    var _self = this;
    var uuid = this.activeSection.attr("id").substring(5);
    var dockElement = this.cachedDock[uuid];
    var mainStyle = dockElement.css;
    var baseRule = this.getCssRule(mainStyle, "#dock-" + dockElement.uuid);
    var containerRule = this.getCssRule(mainStyle, "#dock-" + dockElement.uuid + " > .widget-container");
    var editDom = $("<div class='edit-dock-css'>" +
    "<div class='sidebar-group'><div class='sidebar-group-label'>" + $.i18n.prop("position") + "</div><div class='sidebar-group-body'><select class='sidebar-input' name='position'>" +
    "<option value='fixed' selected>" + $.i18n.prop("fixed") + "</option>" +
    "<option value='absolute'>" + $.i18n.prop("absolute") + "</option>" +
    "</select></div></div>" +
    "<div class='sidebar-group'><div class='sidebar-group-label'>" + $.i18n.prop("left") + "</div><div class='sidebar-group-body'><input type='text' class='sidebar-input' name='left' value='" + (baseRule.getAttribute("left") || "0") + "'></div></div>" +
    "<div class='sidebar-group'><div class='sidebar-group-label'>" + $.i18n.prop("top") + "</div><div class='sidebar-group-body'><input type='text' class='sidebar-input' name='top' value='" + (baseRule.getAttribute("top") || "0") + "'></div></div>" +
    "<div class='sidebar-group'><div class='sidebar-group-label'>" + $.i18n.prop("right") + "</div><input type='text' class='sidebar-input' name='right' value='" + (baseRule.getAttribute("right") || 0) + "'></div></div>" +
    "<div class='sidebar-group'><div class='sidebar-group-label'>" + $.i18n.prop("bottom") + "</div><div class='sidebar-group-body'><input type='text' class='sidebar-input' name='bottom' value='" + (baseRule.getAttribute("bottom") || "auto") + "'></div></div>" +
    "<div class='sidebar-group'><div class='sidebar-group-label'>" + $.i18n.prop("widget.placeholder.width") + "</div><div class='sidebar-group-body'><input type='text' class='sidebar-input container' name='width' value='" + ((containerRule && containerRule.getAttribute("width")) || "100%") + "'></div></div>" +
    "<div class='sidebar-group'><div class='sidebar-group-label'>" + $.i18n.prop("widget.placeholder.height") + "</div><div class='sidebar-group-body'><input type='text' class='sidebar-input container' name='height' value='" + ((containerRule && containerRule.getAttribute("height")) || "100%") + "'></div></div>" +
    "</div>");
    editDom.find("select[name='position']").val(baseRule.getAttribute("position")).chosen({disable_search: true}).change(function() {
        var old_cache = mainStyle.toString();
        _self.updateCss(mainStyle, uuid, "#dock-" + dockElement.uuid, {position: this.value});
        _self.setDirty("css_change", {css: old_cache, uuid: dockElement.uuid, obj: dockElement}, {css: dockElement.css.toString(), uuid: dockElement.uuid, obj: dockElement}, dockElement.elm);
    });
    editDom.find("input").change(function() {
        var rule = "#dock-" + dockElement.uuid;
        if($(this).is(".container")) {
            rule += " > .widget-container"
        }
        var attr = {};
        attr[this.name] = this.value;
        var old_cache = mainStyle.toString();
        _self.updateCss(mainStyle, uuid, rule, attr);
        _self.setDirty("css_change", {css: old_cache, uuid: dockElement.uuid, obj: dockElement}, {css: dockElement.css.toString(), uuid: dockElement.uuid, obj: dockElement}, dockElement.elm);
    });
    container.append(editDom);
    var selectorPrefix = "#dock-" + dockElement.uuid;
    if(this.isResponsive) {
        var operators = '<div class="sidebar-group dock-css-operators">';
        if(this.activeMedia) {
            operators += '<span class="tool-icon copy-other-media" title="' + $.i18n.prop("copy.all.from.other.media") + '"></span>'
        }
        operators += '<span class="tool-icon remove remove-media" title="' + $.i18n.prop("remove.media") + '"></span></div>';
        container.append(operators)
    }
    this._handleCssMediaTools(dockElement, container, dockElement.uuid);
    _self.editCss(container, dockElement, "<dock>", selectorPrefix, [selectorPrefix + " > .widget-container", ".responsive " + selectorPrefix + " > .widget-container"]);
};

_e.cacheDock = function(uuid, dockElement) {
    if(!dockElement) {
        var style = this.iframeContent.find("#style-store-" + uuid);
        var css = new CssParser(style[0].innerHTML);
        css.parse();
        dockElement = {uuid: uuid, style: null, css: css, elm: this.pageBody.find("#dock-" + uuid)};
    }
    this.cachedDock[uuid] = dockElement;
    return dockElement
};

_e.getDockElement = function (dock) {
    var id = dock.attr("dock-id");
    var uuid = dock.attr("id").substring(5);
    if(this.cachedDock[uuid]) {
        return this.cachedDock[uuid]
    }
    return this.cacheDock(uuid)
};

_e.removeDock = function() {
    var _self = this;
    var dock = _self.activeSection;
    if(!dock.is(".dockable")) {
        return;
    }
    var uuid = dock.attr("id").substring(5);
    var id = dock.attr("dock-id");
    var state = {
        undo: function() {
            if(id) {
                _self.removedDock.pop();
            }
            _self.pageBody.append(dock);
            _self.activeSectionSelector.chosen('add', {value: uuid, text: "Dock - " + uuid, selected: true}).trigger("change");
        }, redo: function() {
            if(id) {
                _self.removedDock.push(id);
            }
            dock.detach();
            _self.activeSectionSelector.chosen('remove', uuid).trigger("change");
        }
    };
    state.redo();
    this.activeSectionSelector.chosen("remove", uuid).chosen("val", "body").trigger("change");
    _self.setDirty(state);
};

_e.resolutionChange = function(value) {
    var _self = this;
    var media = "";
    if(value) {
        var minMax = value.split("-");
        var max;
        if(minMax[1] == '') {
            media = "(min-width: " + minMax[0] + "px)";
            _self.iframe.css({"min-width": minMax[0] + "px", "width": ''});
        } else {
            max = minMax[1];
            _self.iframe.css({"min-width": "", "width": max + "px"});
            media = "(max-width: " + max + "px)";
            if(minMax[0]) {
                media = "(min-width: " + minMax[0] + "px) and " + media
            }
        }
    } else {
        _self.iframe.css({"min-width": "", "width": ""});
    }
    this.activeMedia = media;
    var allSectionSelector = [];
    this.activeSectionSelector.find("option").each(function() {
        if(this.value == "body") {
            return;
        }
        if(this.value == "header" || this.value == "footer") {
            allSectionSelector.push("." + this.value)
        } else {
            allSectionSelector.push("#" + this.value);
        }
    });
    for(var i = 0; i < allSectionSelector.length; i++ ) {
        var widgets = _self.pageBody.find(allSectionSelector[i] + " ." + _self.widgetClass);
        $.each(widgets, function() {
            var widget = $(this);
            if (widget.is('[cached-widget]')) {
                var uuid = widget.attr("id").substring(3);
                var obj = _self.cachedWidgets[uuid];
                obj.updateMemoryStyle()
            }
        })
    }
    this.updateSectionDimensionSpinners();
    if(this.activeSection.is('.dockable')) {
        this.editDockCss()
    }
    if(this.selectedWidget) {
        var widget = this.getWidgetObject(this.selectedWidget);
        widget.updateMemoryStyle();
        this.updateWidgetConfig(this.selectedWidget.attr("id").substring(3), 'css');
        if(!this.activeSection.is(".body")) {
            widget.positionReferenceLine();
            widget.highlightReferenceSelection();
            this.setDimensionSpinnerValues(widget);
            this.settingBlock.find(".widget-ref-unit-selector select").chosen("val", widget.percentage ? "%" : "px")
        }
    }
    this.updatePageProperties('css')
};

_e.getWidgetRenderAjaxIOptions = function(options) {
    return options;
};

_e.renderCopiedSection = function(section) {
    var _self = this;
    var layouts;
    var template;
    _self.renderCreatePanel(app.baseUrl + "layout/layoutSelectionView", $.i18n.prop("clone."+section), undefined, {id: _self.containerId, section: section}, {
        createPanelTemplate: $('<div class="embedded-edit-form-panel create-panel fade-in-up"><div class="header"><span class="header-title"></span>' +
            '<span class="toolbar toolbar-right"><span class="tool-group toolbar-btn save">' + $.i18n.prop("update")+ '</span>' +
            '<span class="tool-group toolbar-btn cancel">' + $.i18n.prop("cancel") + '</span></span></div><div class="body"></div></div>'),
        auto_close_on_success: false,
        auto_clear_dirty: false,
        content_loaded: function(form, _template) {
            template = _template;
            layouts = form.find(".layout-list");
            layouts.find(".layout-thumb").on("click", function() {
                var $this = $(this);
                if(!$this.is(".selected")) {
                    $this.addClass("selected").siblings().removeClass("selected");
                }
            })
        },
        beforeSubmit: function(form, data) {
            var layout = layouts.find(".selected");
            if(layout.length) {
                data.id = layout.attr("layout-id");
            } else{
                bm.notify($.i18n.prop("select.a.layout.first"), "alert");
                return false;
            }
        },
        success: function (resp) {
            bm.confirm($.i18n.prop("confirm.clone."+section, [resp.name]), function() {
                resp.section = $(resp.section);
                resp.type = section;
                resp.section.find(".widget").each(function() {
                    var widget = $(this);
                    var id = widget.attr("id").substring(3);
                    widget.attr("cloned", "true");
                });
                template.close();
                _self.copySection(resp);
            },function() {});
        }
    });
};

_e.copySection = function(data) {
    var _self = this;
    var newSection = $(data.section);
    var oldCss = new CssParser(_self.css.toString()).parse();
    var sectionCss = new CssParser(data.css).parse();
    var removedSections = _self["removed" + data.type.capitalize()]
    var state = {
        undo: function() {

            var removed = _self.activeSection.find(".widget-container");
            var redoSection = removedSections.pop();

            removed.detach();
            _self.activeSection.prepend(redoSection);
            _self.activeSection = redoSection.parent();
            _self.updateSection(data.type, oldCss);
            _self.removedWidgets.pop();

        }, redo: function() {

            var removed = _self.activeSection.find(".widget-container");
            removed.detach();
            _self.activeSection.prepend(newSection);
            _self.activeSection = newSection.parent();

            _self.attachSection(data);
            removedSections.push(removed);
            _self.updateSection(data.type, sectionCss, data.wiCss);

            if(removedSections[0][0] == removed[0]) {
                var widRemove = [];
                removed.find(".widget").each(function() {
                    var uuid = $(this).attr("id").substring(3);
                    var cachedElement = _self.cachedWidgets[uuid];
                    var templateServerUuid = cachedElement ? (cachedElement.templateServerUuid ? cachedElement.templateServerUuid : (cachedElement.elm.is("[external-widget=true]") ? uuid : null)) : null;
                    widRemove.push({uuid : uuid, section: _self.activeSection.attr("section"), templateServerUuid: templateServerUuid});
                });
                _self.removedWidgets.push(widRemove);
            }

        }
    };
    state.redo();
    _self.setDirty(state);
};

_e.attachSection = function(data) {
    var _self = this;
    var section = "";
    _self.loadCsss();
    if(_self.initEditor) {
        _self.initEditor();
    }
    _self.changeActiveSection(data.type, true, true);
    if(_self.isResponsive) {
        _self.resolutionChange(this.settingBlock.find(".change-resolution select").val());
    }
    _self.activeSection.find(".widget").attr("new-widget", "true").each(function() {
        var widget = $(this);
        widget.data("data-cache", data.wiCache[widget.attr("id").substring(3)]);
    });
};

_e.updateSection = function(type, sectionCss, widgetCss) {
    var _self = this;
    var ruleSelector = "."+type+" > .widget-container";
    _self.updateSectionCss(sectionCss, ruleSelector, type);
    _self.updateSectionDimensionSpinners();
    if(widgetCss) {
        var section = _self.activeSection;
        section.find(".widget").each(function() {
            var widget = $(this);
            var uuid = widget.attr("id").substring(3);
            _self.persistCss(uuid, new CssParser(widgetCss[uuid]).parse())
            _self.getWidgetObject(widget);//caching the widget
        });
    }
};

_e.updateSectionCss = function(sectionCss, ruleSelector, type) {
    var _self = this;
    _self.css.removeRule(ruleSelector);
    _self.css.removeMediaRules(ruleSelector);
    $.each(sectionCss.getMedias(), function(i, mediaObj) {
        $.each(mediaObj.rulesets, function(j, rule) {
            $.each(rule.selectors, function(r, selector) {
                if (selector && (selector.startsWith("." + type) || selector.startsWith(".responsive ." + type))) {
                    var media = _self.css.getMedia(mediaObj.definition);
                    if(media){
                        _self.css.removeMedia(media);
                        media = _self.css.addMedia(mediaObj.definition, media.rulesets);
                    }else{
                        media = _self.css.addMedia(mediaObj.definition);
                    }
                    var _rule = media.getRule(selector);
                    if (_rule) {
                        media.removeRule(_rule);
                    }
                    _rule = media.addRule(selector);
                    var attrs = {};
                    $.each(rule.attributes, function (i, v) {
                        attrs[v.key] = v.value;
                    });
                    _rule.setAttribute(attrs);
                }
            });
        });
    });
    _self.css.setAttribute(ruleSelector, "height", sectionCss.getAttribute(ruleSelector, "height"));
    _self.css.setAttribute(ruleSelector, "width", sectionCss.getAttribute(ruleSelector, "width"));
    _self.persistCss(undefined, _self.css);
};

app.tabs.edit_content.Widget = function(editor, props) {
    //properties style, css, uuid, elm
    this.editor = editor;
    $.extend(this, props);
    this.type = this.elm.attr("widget-type")
};

var _w = app.tabs.edit_content.Widget.prototype;

_w.render = function(data, afterRender) {
    var _self = this;
    bm.ajax(this.editor.getWidgetRenderAjaxIOptions({
        show_success_status: false,
        controller: "widget",
        action: "renderWidget",
        data: data,
        success: function(resp) {
            _self.updateContent(resp.html, resp.serialized);
            if(afterRender) {
                afterRender()
            }
        }
    }))
};

_w.updateContent = function(html, cache) {
    var content = $(html);
    content.find("script").remove();
    this.elm.html(content.html());
    if(cache) {
        this.elm.data("data-cache", cache)
    } else {
        this.elm.removeData("data-cache")
    }
    this.editor.createWidgetOverlay(this.elm);
    if(app.widget[this.type] && typeof app.widget[this.type].prototype.afterContentChange == "function") {
        app.widget[this.type].prototype.afterContentChange(this, cache);
    }
};

_w.getPos = function() {
    var pos = this.elm.position();
    var parent = $(this.elm[0].offsetParent);
    pos.right = parent.innerWidth() - pos.left - this.elm.outerWidth(true);
    pos.bottom = parent.innerHeight() - pos.top - this.elm.outerHeight(true);
    return pos;
};

_w.updateMemoryStyle = function() {
    var rule;
    if (this.editor.isResponsive) {
        if (this.editor.activeMedia) {
            var media = this.css.getMedia(this.editor.activeMedia);
            rule = media ? media.getRule(".responsive #wi-" + this.uuid) : this.css.getRule("#wi-" + this.uuid);
        } else {
            rule = this.css.getRule("#wi-" + this.uuid);
        }
    } else {
        rule = this.css.getRule("#wi-" + this.uuid);
    }
    if (rule) {
        this.style = {
            left: rule.getAttribute("left"),
            right: rule.getAttribute("right"),
            top: rule.getAttribute("top"),
            bottom: rule.getAttribute("bottom"),
            width: rule.getAttribute("width"),
            height: rule.getAttribute("height")
        };
        this.percentage = this.style[this.isLeft() ? "left" : "right"] ? this.style[this.isLeft() ? "left" : "right"].contains("%") : false; // TODO:  this line is changed temporarily, Need to change.
    }
};

_w.positionReferenceLine = function() {
    var xLine = this.editor.pageBody.find(".bounding-box-line-x").hide();
    var yLine = this.editor.pageBody.find(".bounding-box-line-y").hide();
    var xLine2 = this.editor.pageBody.find(".bounding-box-line-x-2").hide();
    var yLine2 = this.editor.pageBody.find(".bounding-box-line-y-2").hide();
    if(!this.elm.is(":visible")) {
        return;
    }
    var pos = this.getPos();
    var width;
    var height;
    var position = {
        of: this.elm,
        collision: "none"
    };
    var adjust_values = {left: 0, right: 0, top: 0, bottom: 0};
    var parent = this.elm.parent();
    if(!parent.is(".widget-container")) {
        var _pos = parent.pposition();
        adjust_values.left = _pos.left + parent.leftRib(true, true, false);
        adjust_values.top = _pos.top + parent.topRib(true, true, false);
        adjust_values.right = _pos.right + parent.rightRib(true, true, false);
        adjust_values.bottom = _pos.bottom + parent.bottomRib(true, true, false)
    }
    function show(adir, bdir) {
        width = pos[adir];
        height = pos[bdir];
        xLine.css({width: width + adjust_values[adir]});
        yLine.css({height: height + adjust_values[bdir]});
        var lmargin = position.of[adir + "Rib"](true, false, false);
        var tmargin = position.of[bdir + "Rib"](true, false, false);
        if(lmargin > 0) {
            lmargin = adir + (adir == "left" ? "-" : "+") + lmargin
        } else {
            lmargin = adir + (adir == "left" ? "+" : "-") + (-1 * lmargin)
        }
        if(tmargin > 0) {
            tmargin = bdir + (bdir == "top" ? "-" : "+") + tmargin
        } else {
            tmargin = bdir + (bdir == "top" ? "+" : "-") + (-1 * tmargin)
        }
        position.my = (adir == "left" ? "right " : "left ") + (bdir == "top" ? "bottom " : "top ");
        position.at = lmargin + " " + tmargin;
        xLine.show().position(position);
        yLine.show().position(position);
    }
    if(this.isLeftTop()) {
        show("left", "top");
        if(this.isRight()) {
            xLine = xLine2;
            yLine = yLine2;
            show("right", "top")
        }
    } else if(this.isRightTop()) {
        show("right", "top")
    } else if(this.isRightBottom()) {
        show("right", "bottom");
        if(this.isLeft()) {
            xLine = xLine2;
            yLine = yLine2;
            show("left", "bottom")
        }
    } else {
        show("left", "bottom")
    }
};

_w.highlightReferenceSelection = function() {
    var widgetOverlay = this.elm.find(".widget-overlay");
    var topLeftButton = widgetOverlay.find('.widget-bounding-box.top-left').removeClass('selected');
    var topRightButton = widgetOverlay.find('.widget-bounding-box.top-right').removeClass('selected');
    var bottomRightButton = widgetOverlay.find('.widget-bounding-box.bottom-right').removeClass('selected');
    var bottomLeftButton = widgetOverlay.find('.widget-bounding-box.bottom-left').removeClass('selected');
    if(this.isLeftTop()) {
        topLeftButton.addClass("selected");
        if(this.isRight()) {
            topRightButton.addClass("selected");
        }
    } else if(this.isRightTop()) {
        topRightButton.addClass("selected");
    } else if(this.isLeftBottom()) {
        bottomLeftButton.addClass("selected");
        if(this.isRight()) {
            bottomRightButton.addClass("selected");
        }
    } else {
        bottomRightButton.addClass("selected");
    }
};

_w.isLeftTop = function() {
    return /(-?\d+(\.\d+)?(px|%)){2}/.test(this.style.left + this.style.top);
};

_w.isRight = function() {
    return /-?\d+(\.\d+)?(px|%)/.test(this.style.right);
};

_w.isLeft = function() {
    return /-?\d+(\.\d+)?(px|%)/.test(this.style.left);
};

_w.isTop = function() {
    return /-?\d+(\.\d+)?(px|%)/.test(this.style.top);
};

_w.isBottom = function() {
    return /-?\d+(\.\d+)?(px|%)/.test(this.style.bottom);
};

_w.isRightTop = function() {
    return /(-?\d+(\.\d+)?(px|%)){2}/.test(this.style.right + this.style.top);
};

_w.isLeftBottom = function() {
    return /(-?\d+(\.\d+)?(px|%)){2}/.test(this.style.left + this.style.bottom);
};

_w.isRightBottom = function() {
    return /(-?\d+(\.\d+)?(px|%)){2}/.test(this.style.right + this.style.bottom);
};

_w.pushStyle = function() {
    this.editor.updateCss(this.css, this.uuid, "#wi-" + this.uuid, this.style)
};

_w.applyStyle = function(style) {
    this.style = style;
    this.pushStyle();
    if(this.editor.selectedWidget && this.editor.selectedWidget[0] == this.elm[0]) {
        this.positionReferenceLine();
        this.editor.setDimensionSpinnerValues(this);
        this.highlightReferenceSelection()
    }
};

_w.setJs = function(js) {
    var old = this.js;
    this.js = js;
    var widget = this.elm;
    this.editor.setDirty("widget_js", {obj: this, js: old}, {obj: this, js: js}, widget);
};

_w._setXY = function(pos, width, x1, x2, X1, X2) {
    var widgetTotal = this.elm["outer" + width](true);
    var parentTotal = this.elm.parent()["inner" + width]();
    var pos_limit = [0, parentTotal - widgetTotal];
    if(!this.elm.parent().is('.widget-container')) {
        var parent = this.elm.parent();
        var position = parent.pposition();
        if(!this["is" + X1]()) {
            pos_limit[0] = -1 * (position[x2] + parent[x2 + "Rib"](true, true, false));
            pos_limit[1] += position[x1] + parent[x1 + "Rib"](true, true, false)
        } else {
            pos_limit[0] = -1 * (position[x1] + parent[x1 + "Rib"](true, true, false));
            pos_limit[1] += position[x2] + parent[x2 + "Rib"](true, true, false)
        }
    }
    var pixel_value = pos;
    if(this.percentage) {
        pixel_value = pos / 100 * parentTotal
    }
    var changed = false;
    if(pixel_value < pos_limit[0]) {
        pixel_value = pos_limit[0];
        changed = true
    }
    if(pixel_value > pos_limit[1]) {
        pixel_value = pos_limit[1];
        changed = true
    }
    if(changed) {
        if(this.percentage) {
            pos = pixel_value / parentTotal * 100
        } else {
            pos = pixel_value
        }
    }
    var old_style = $.extend({}, this.style);
    if(this["is" + X1]()) {
        if(this["is" + X2]()) {
            var prev = parseFloat(this.style[x1]);
            var diff = pos - prev;
            this.style[x2] = (parseFloat(this.style[x2]) - diff) + (this.percentage ? "%" : "px")
        }
        this.style[x1] = pos + (this.percentage ? "%" : "px")
    } else if(this["is" + X2]()) {
        this.style[x2] = pos + (this.percentage ? "%" : "px")
    }
    this.applyStyle(this.style);
    this.editor.setDirty("widget_reposition", {widget: this.elm, style: old_style}, {widget: this.elm, style: $.extend({}, this.style)}, this.elm);
    return pos
};

_w._setWH = function(value, minValue, left, right, width, Left, Right, Width) {
    var parentTotal = this.elm.parent()["inner" + Width]();
    var dim_limit = [minValue, parentTotal, 0]; //dimension limit
    if(!this.elm.parent().is('.widget-container')) {
        var parent = this.elm.parent();
        dim_limit[1] = parent.parent()["inner" + Width]()
    }
    if(value < dim_limit[0]) {
        value = dim_limit[0];
    } else if(value > dim_limit[1]) {
        value = dim_limit[1]
    }
    var c_style = $.extend({}, this.style);
    if(this["is" + Right]()) {
        if(!this.elm.parent().is('.widget-container')) {
            var parent = this.elm.parent();
            var position = parent.pposition();
            dim_limit[2] -= position[right] + parent[right + "Rib"](true, true, false)
        }
        var c_value = parseFloat(this.style[right]);
        if(this.percentage) {
            c_value = c_value / 100 * parentTotal
        }
        var nvalue = c_value - value + this.elm[width]();
        if(nvalue < dim_limit[2]) {
            nvalue = dim_limit[2]
        }
        if(this.percentage) {
            nvalue = nvalue / parentTotal * 100
        }
        this.style[right] = nvalue + (this.percentage ? "%" : "px")
    }
    if(this["is" + Left]()) {
        var c_value = parseFloat(this.style[left]);
        if(this.percentage) {
            c_value = c_value / 100 * parentTotal
        }
        var applied_c_value = c_value;
        if(!this.elm.parent().is('.widget-container')) {
            var parent = this.elm.parent();
            var position = parent.pposition();
            applied_c_value -= position[left] + parent[left + "Rib"](true, true, false)
        }
        if(applied_c_value + value > dim_limit[1]) {
            var set_value = c_value - applied_c_value - value + dim_limit[1];
            if(this.percentage) {
                set_value = set_value / 100 * parentTotal
            }
            this.style[left] = set_value + (this.percentage ? "%" : "px")
        }
    }
    if(!this["is" + Right]() || !this["is" + Left]()) {
        this.style[width] = value + "px"
    }
    this.applyStyle(this.style);
    this.editor.setDirty("widget_reposition", {widget: this.elm, style: c_style}, {widget: this.elm, style: $.extend({}, this.style)}, this.elm);
    return this.elm.outerWidth(true)
};

_w.setX = function(xpos) {
    return this._setXY(xpos, "Width", "left", "right", "Left", "Right")
};

_w.setY = function(ypos) {
    return this._setXY(ypos, "Height", "top", "bottom", "Top", "Bottom")
};

_w.afterConfigChange = function() {
    if(this.elm.attr("external-widget") == "true") {
        var uuid = bm.getUUID();
        var oldUuid = this.uuid;
        this.templateServerUuid = oldUuid;
        var style = this.editor.iframeContent.find("#style-store-" + oldUuid).attr("id", "style-store-" + uuid);
        var newCss = style.text().replaceAll(oldUuid, uuid);
        style.text(newCss);
        this.uuid = uuid;
        this.css = newCss;
        this.elm.attr("id", "wi-" + uuid);
        this.elm.removeAttr("external-widget");
        this.elm.removeAttr("modified-widget");
        this.elm.attr("new-widget", "true");
        this.elm.attr("widget-id", "");
        this.editor.cachedWidgets[uuid] = this
    }
    return this.uuid;
};