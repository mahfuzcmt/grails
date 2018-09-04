bm.onReady(app.tabs, "edit_content", function() {
    app.tabs.edit_content.page = function(config) {
        config.containerType = config.containerType || "page"
        this.ajax_url = app.baseUrl + "pageAdmin/editPageContent?id=" + config.containerId;
        this.initial_section = "body"
        _super.constructor.apply(this, arguments);
    }

    var _p = app.tabs.edit_content.page.inherit(app.tabs.edit_content)

    var _super = app.tabs.edit_content.page._super;

    _p.editContainerJsUrl = app.baseUrl + 'pageAdmin/editJs';

    _p.initEditor = function() {
        this.body.find(".toolbar-item.view-site").on("click", function(){
            var url = app.siteBaseUrl + $(this).attr("url") +"?adminView=true"
            window.open(url,'_blank');
        });
        if(this.has_layout) {
            if(this.layout_name) {
                this.status_block = this.status_block || StatusBarManager.allocate(this.id + "-layout-name", "attached-layout-name");
                this.status_block.set($.i18n.prop("attached.layout.name") + " - <span class='layout-name-info'>" + bm.htmlEncode(this.layout_name) + "</span>")
            }
            this.pageBody.addClass("edit-page-with-layout")
            var editableArea = this.pageBody.find(".body[section] .page-content");
            if(editableArea.length) {
                var parent = editableArea.parent()
                while(!parent.is(".body")) {
                    parent.siblings().append("<div class=\"noneditable-area-overlay\"></div>");
                    parent = parent.parent()
                }
            }
            this.sectionHeight.stepper("disable")
            this.sectionWidth.stepper("disable")
            this.supportedWidgetSelector = ".page-content ." + this.widgetClass
            this.gridSelector = ".page-content .grid-block"
            this.editableBlockSelector = this.gridSelector + ", .page-content .widget-container:not(:has(>.grid-block)), .page-content:not(:has(>.grid-block, >.widget-container))"
            this.sortable_page_content = true
            this.settingBlock.find(".body-sections-selector, .section-tools").hide()
        }
        this.on("deActive", function() {
            if (this.status_block) {
                this.status_block.hide()
            }
        })
    }

    _p.loadCsss = function() {
        var defaultCss;
        if (this.isResponsive) {
            defaultCss = ".header > .widget-container {height: 200px;} .footer > .widget-container {height: 200px;}"
        } else {
            defaultCss = ".body > .widget-container {width: 1000px;} .header > .widget-container {width: 1000px; height: 200px;} .footer > .widget-container {width: 1000px; height: 200px;}"
        }
        var layoutId = this.pageBody.find(".layout-id");
        if (layoutId.length) {
            this.has_layout = true;
            this.layout_id = layoutId.val();
            this.layout_name = this.pageBody.find(".layout-name").val();
            this.leafGridBlockSelectorInContentMode = ".page-content:not(:has(>.grid-block, >div>.grid-block, >.widget-container)), .page-content .grid-block:not(:has(>.grid-block)), .page-content .widget-container:not(:has(>.grid-block))"
        } else {
            this.has_layout = false;
            this.leafGridBlockSelectorInContentMode = ".widget-container:not(:has(>.grid-block)), .grid-block:not(:has(>.grid-block))"
        }
        var style_page = this.iframeContent.find("#stored-css");
        var cssString = style_page.length ? style_page[0].innerHTML : ""
        if(cssString == "" && !this.has_layout) {
            this.have_to_add_default_css = false
            cssString = defaultCss
            this.iframeContent.find("head #stored-css").text(cssString)
            if(this.pageBody.find(".body > .widget-container").length == 0) {
                this.pageBody.find(".body").prepend("<div class='widget-container'></div>")
            }
        }
        if(this.have_to_add_default_css) {
            cssString += defaultCss
            this.iframeContent.find("head #stored-css").text(cssString)
            this.have_to_add_default_css = false
        }
        this.css = new CssParser(cssString);
        this.css.parse();
        if(this.post_attach_initialization) {
            this.css.removeRule(".header .widget-container")
            this.css.removeRule(".body .widget-container")
            this.css.removeRule(".footer .widget-container")
            this.iframeContent.find("head #stored-css").text(this.css.toString())
            this.pageBody.find(".page-content > .widget-container").removeClass("widget-container")
        }
        if(this.post_detach_initialization) {
            var container = this.pageBody.find(".body > div");
            if(container.not(".widget-container").length) {
                container.addClass("widget-container")
            }
        }
    }

    _p.close = function() {
        if(_super.close.apply(this, arguments) !== false) {
            if(this.status_block) {
                this.status_block.remove()
            }
        }
    }

    _p.setActive = function() {
        if(_super.setActive.apply(this, arguments) !== false) {
            if(this.status_block) {
                this.status_block.show()
            }
        }
    }

    _p.initSectionMasks = function() {
        _super.initSectionMasks.apply(this, arguments)
        if (this.has_layout) {
            this.pageBody.find(".header .section-overlay, .footer .section-overlay").addClass("disabled");
        }
    }

    _p.getBodyContent = function() {
        if (this.has_layout) {
            return this.pageBody.find(".page-content").clone();
        }
        return this.pageBody.find(".body").clone();
    }

    _p.setDirty = function() {
        this.body.find(".detach-layout, .attach-layout").addClass("disabled")
        _super.setDirty.apply(this, arguments)
    }

    _p.clearDirty = function() {
        this.body.find(".attach-layout").removeClass("disabled")
        if(this.has_layout) {
            this.body.find(".detach-layout").removeClass("disabled")
        }
        _super.clearDirty.apply(this, arguments)
        this.post_attach_initialization = false
        this.post_detach_initialization = false
    }

    _p.onSectionChange = function(old_active, section) {
        if(this.has_layout) {
            if (section.length < 7) { // not a dock
                this.sectionHeight.stepper("disable")
                this.sectionWidth.stepper("disable")
            } else {
                this.sectionWidth.stepper("enable")
                this.sectionHeight.stepper("enable")
            }
        } else {
            this.sectionWidth.stepper("enable")
        }
    }

    _p.splitterDropped = function(parent) {
        var children = parent.children()
        if(parent.is(".page-content.l-fixed-container, .page-content.r-fixed-container, .page-content.v-split-container")) {
            if(parent.is(".l-fixed-container")) {
                parent.append('<div class="l-fixed-container"></div>')
            } else if(parent.is(".r-fixed-container")) {
                parent.append('<div class="r-fixed-container"></div>')
            } else {
                parent.append('<div class="v-split-container"></div>')
            }
            parent.removeClass("l-fixed-container r-fixed-container v-split-container")
            parent.find("> .l-fixed-container, > .r-fixed-container, > .v-split-container").append(children)
        }
    }

    _p.editableWidgetsInSection = function(section) {
        if(this.has_layout) {
            return section.find(".page-content ." + this.widgetClass)
        } else {
            return _super.editableWidgetsInSection.apply(this, arguments)
        }
    }
})