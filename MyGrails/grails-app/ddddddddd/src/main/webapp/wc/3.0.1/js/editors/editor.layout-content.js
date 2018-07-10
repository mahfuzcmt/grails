bm.onReady(app.tabs, "edit_content", function() {
    app.tabs.edit_content.layout = function(config) {
        config.containerType = "layout";
        this.ajax_url = app.baseUrl + "layout/editLayout?id=" + config.containerId;
        _super.constructor.apply(this, arguments);
    }

    var _l = app.tabs.edit_content.layout.inherit(app.tabs.edit_content)
    var _super = app.tabs.edit_content.layout._super
    _l.editContainerJsUrl = app.baseUrl + 'layout/editJs';

    _l.loadCsss = function() {
        if (this.containerId) {
            var style = this.iframeContent.find("#stored-css");
            this.css = new CssParser(style[0].innerHTML).parse();
        } else {
            this.css = new CssParser(".body > .widget-container {width: 1000px;} .header > .widget-container {width: 1000px; height: 200px;} .footer > .widget-container {width: 1000px; height: 200px;}").parse();
            this.iframeContent.find("head").append("<style id='stored-css'>" + this.css.toString() + "</style>")
        }
    }

    _l.save = function(callback) {
        var _self = this
        var _callback = function() {
            app.global_event.trigger("layout-update", [_self.containerId])
        }
        if(callback) {
            callback = _callback.blend(callback)
        } else {
            callback = _callback
        }
        return _super.save.call(this, callback)
    }

    function pageButtonClick(_self) {
        var currentHolder = _self.pageBody.find(".page-content")
        var oldBlock = currentHolder.parent()
        var newBlock = this.closest(".grid-block, .widget-container") //as same reference is just changing parent. not using overlay
        var state = {
            undo: function() {
                oldBlock.append(currentHolder)
                newBlock.append(oldBlock.find(">.editable-area-overlay"))
                var selectedBlock = oldBlock.add(newBlock).filter(":has(.selected)")
                if(selectedBlock.length) {
                    _self.selectGrid(selectedBlock)
                }
                _self.grid_sortable.addElement(newBlock)
                _self.grid_sortable.removeElement(oldBlock)
            }, redo: function() {
                newBlock.append(currentHolder)
                oldBlock.append(newBlock.find(">.editable-area-overlay"))
                var selectedBlock = oldBlock.add(newBlock).filter(":has(.selected)")
                if(selectedBlock.length) {
                    _self.selectGrid(selectedBlock)
                }
                _self.grid_sortable.addElement(oldBlock)
                _self.grid_sortable.removeElement(newBlock)
            }
        }
        state.redo()
        _self.setDirty(state)
    }

    _l.setEditableOverlay = function(section) {
        var _self = this;
        _super.setEditableOverlay.apply(this, arguments)
        section = section || this.activeSection
        if(!this.activeSection.is(".body")) {
            return
        }
        var pageButton = $("<span class='set-content-block'></span>").text($.i18n.prop("make.page.content.placeholder"));
        section.find("div:has(>.editable-area-overlay):not(:has(>.grid-block, >.page-content, >.widget)) .editable-area-overlay").append(pageButton.click(function() {
            pageButtonClick.call($(this), _self)
        }))
    }

    _l.onNewWidgetDrop = function(widget, afterUndoRedo) {
        if(this.activeSection.is(".body")) {
            if(!widget.siblings("." + this.widgetClass).length) {
                var overlay = widget.siblings(".editable-area-overlay")
                var pageButton = overlay.find(".set-content-block")
                var redo = function() {
                    pageButton.detach()
                }
                var undo = function() {
                    overlay.append(pageButton)
                }
                redo()
                function after() {
                    if(this[0].parent.parent().length) {
                        redo()
                    } else {
                        undo()
                    }
                }
                if(afterUndoRedo) {
                    afterUndoRedo = afterUndoRedo.blend(after)
                } else {
                    afterUndoRedo = after
                }
            }
        }
        _super.onNewWidgetDrop.call(this, widget, afterUndoRedo)
    }

    _l.initializeSectionForEditing = function(section) {
        var _self = this
        _super.initializeSectionForEditing.call(this, section)
        if(section.is(".body")) {
            section.find(".editable-area-overlay, .page-content").mousedown(function() {
                _self.unselectWidget()
            })
        }        
    }

    _l.removeWidget = function(section, uuid, group) {
        var widget = this.activeSection.find("#wi-" + uuid)
        var overlay = widget.siblings(".editable-area-overlay")
        var no_has_others = widget.siblings("." + this.widgetClass).length == 0
        var returned = _super.removeWidget.call(this, section, uuid, group)
        if(this.activeSection.is(".body") && no_has_others) {
            var pageButton = $("<span class='set-content-block'></span>").text($.i18n.prop("make.page.content.placeholder"));
            pageButton.click(pageButtonClick.bind(pageButton, this))
            var redo = function () {
                overlay.append(pageButton)
            }
            var undo = function () {
                pageButton.detach()
            }
            redo()
            function handler() {
                if (this[0].parent) {
                    undo()
                } else {
                    redo()
                }
            }
            if($.isArray(returned)) {
                returned[0].handler = returned[0].handler.blend(handler)
                returned[1].handler = returned[1].handler.blend(handler)
            }
        }
        return returned
    }
})