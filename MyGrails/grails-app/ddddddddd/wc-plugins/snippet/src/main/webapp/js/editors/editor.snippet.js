(function() {
    app.tabs.edit_snippet = function(config) {
        this.text = $.i18n.prop("edit.snippet");
        this.name = config.containerName;
        this.tip = config.containerName;
        this.ajax_url = app.baseUrl + "snippetAdmin/editContent?id=" + config.containerId;
        this.ui_body_class = this.ui_class = "snippet-editor edit-tab";
        this.snippetId = config.containerId;
        app.tabs.edit_snippet._super.constructor.apply(this, arguments);

    };

    app.tabs.edit_snippet.inherit(app.Tab);

    var _e = app.tabs.edit_snippet.prototype;

    _e.init = function() {
        var _self = this;
        app.tabs.edit_snippet._super.init.apply(this, arguments);
        this.iframe = this.body.find("iframe");
        this.iframeWrap = this.body.find(".app-tab-content-container")
        this.iframe.on("sure-load", $.proxy(this.afterLoad, this));
        this.configBar = this.body.find(".config-bar");
        this.templateRepository = this.body.find(".snippet-template-repository");
        this.initTemplateRepository();
        this.configBulder = new app.snippet_config_builder(this);
        this.saveBtn = this.body.find(".header .toolbar .save").on("click", function() {
            if(!$(this).is(".disabled")) {
                _self.save()
            }
        });
        this.body.find(".header .toolbar .cancel").on("click", function() {
           _self.close();
        });
        this.body.find(".toolbar .back").on("click", function() {
            _self.activateTemplateRepo()
        })
        this.templateRepository.scrollbar()
    };

    _e.afterLoad = function() {
        var _self = this;
        this.iframeContent = this.iframe.contents();
        window.iframeWindow = this.iframeWindow = this.iframe[0].contentWindow;
        var pageBody = window.pageBody = this.pageBody = this.iframeContent.find("body");
        var hasChild = this.pageBody.children().length != 0;
        this.hasSnippetContent = !this.selectedTemplateUUID && hasChild;
        if(hasChild) {
            _self.deactivateTemplateRepo()
        } else {
            _self.activateTemplateRepo()
        }
        pageBody.append('<div class="editable-area-overlay"></div>');
        this.editorOverlay = this.pageBody.find('.editable-area-overlay');
        this.attachContentEvents();
    };

    _e.save = function(callback) {
        this.deactivateElement();
        var _self = this, content = this.pageBody.clone();
        content.find(".editable-area-overlay").remove();
        bm.ajax({
            url: app.baseUrl + "snippetAdmin/saveContent",
            data: {
                content: content.html(),
                id: _self.snippetId,
                repositoryType: _self.selectedTemplateRepoType,
                templateUUID: _self.selectedTemplateUUID
            },
            success: function() {
                _self.clearDirty();
                _self.hideSideConfig();
                _self.selectedTemplateUUID = null;
                app.global_event.trigger("snippet-" + _self.snippetId + "-content-update");
                _self.hasSnippetContent = true;
                if(callback) {
                    callback();
                }
            }
        })
    };

    _e.setDirty = function() {
        this.saveBtn.removeClass("disabled");
        app.tabs.edit_snippet._super.setDirty.apply(this, arguments);
    };

    _e.clearDirty = function() {
        this.saveBtn.addClass("disabled");
        app.tabs.edit_snippet._super.clearDirty.apply(this, arguments);
    };

    _e.activateTemplateRepo = function() {
        this.hideSideConfig()
        this.templateRepository.addClass("active")
    };

    _e.deactivateTemplateRepo = function() {
        this.templateRepository.removeClass("active")
    };

    _e.initTemplateRepository = function() {
        var _self = this, templateRepository = this.templateRepository;
        this.repositorySelector = templateRepository.find(".snippet-repository-selector");
        this.repositorySelector.on("change", function() {
            _self.loadTemplates();
        });
        templateRepository.find(".clear-filter").on("click", function() {
            _self.clearTemplateFilter()
        })
        this.loadTemplates()
    };

    _e.filterTemplates = function(category) {
        var templateRepository = this.templateRepository, list = templateRepository.find(".template-list")
        templateRepository.scrollTop(0)
        list.find(".template:not(." + category + ")").hide()
        if(this.masonryActive) {
            //list.masonry()
        }
        list.addClass("filter-active")
        templateRepository.find(".clear-filter").addClass("active")
        templateRepository.scrollbar("update")
    };

    _e.clearTemplateFilter = function() {
        var templateRepository = this.templateRepository, list = templateRepository.find(".template-list")
        list.find(".template").show()
        list.removeClass("filter-active")
        if(this.masonryActive) {
            //list.masonry()
        }
        templateRepository.find(".clear-filter").removeClass("active")
    };

    _e.loadTemplates = function() {
        var _self = this, templateRepository = this.templateRepository;
        templateRepository.loader();
        function attachEvents(templates) {
            templates.find(".btn.use").on("click", function() {
                var $this = $(this).parents(".template");
                _self.loadTemplateContent($this.attr("uuid"), $this.attr("repository-type"))
            });
            templates.find(".btn.show-more").on("click", function() {
                var $this = $(this).parents(".template");
                _self.filterTemplates(category = $this.attr("category"))
            });
        }
        bm.ajax({
            url: app.baseUrl + "snippetAdmin/templateThumbView",
            data: {
                repositoryType: _self.repositorySelector.val(),
            },
            dataType: "html",
            success: function(resp) {
                resp = $(resp);
                attachEvents(resp);
                var list = templateRepository.find(".template-list")
                if(_self.masonryActive) {
                    list.masonry('destroy')
                }
                templateRepository.find(".clear-filter").removeClass("active")
                var width = list.width()
                width = width > 1200 ? width / 4 : width / 3
                resp.filter(".template").css("width", width)
                list.html(resp);
                if (list.is(".scrollable")){
                    list.scrollbar("destroy");
                }
                list.scrollbar();
                list.imagesLoaded(function() {
                    /*list.masonry({
                        columnWidth: width,
                        itemSelector: '.template'
                    });*/
                    _self.masonryActive = true
                    templateRepository.loader(false)
                });
            }
        })
    };

    _e.loadTemplateContent = function(uuid, repositoryType) {
        var _self = this;
        function load() {
            var url = app.baseUrl + "snippetAdmin/renderSnippet?templateUUID=" + uuid + "&repositoryType=" + repositoryType + "&id=" + _self.snippetId;
            bm.ajax({
                url: app.baseUrl + "layout/isAdminLoggedIn",
                success: function () {
                    _self.iframe.attr("src", url);
                    _self.selectedTemplateUUID = uuid;
                    _self.selectedTemplateRepoType = repositoryType;
                    _self.hasSnippetContent = false;
                    _self.setDirty();
                    _self.deactivateTemplateRepo()
                }
            });
        }
        if(this.hasSnippetContent || _self.isDirty()) {
            var message = this.hasSnippetContent ? "confirm.snippet.has.content" : "there.changed.content.wanna.discard";
            bm.confirm($.i18n.prop(message), function() {
                load();
            }, function() {})
        } else if (_self.selectedTemplateUUID != uuid) {
            load();
        }
    };

    _e.attachContentEvents = function() {
       var _self = this;
        this.editorOverlay.forwardevents({document: _self.iframeWindow.document});
        this.pageBody.find("[data-style-type]").on("mousemove", function() {
            _self.highlightElement($(this));
            return false
        });
        this.pageBody.find("[data-style-type]").on("mouseout", function() {
            _self.removeHighlightElement()
        });
        this.editorOverlay.on("click", function() {
            if(_self.highlightedElement) {
                _self.activateElement()
            }
        });
        this.configBar.find(".config-bar-head .close").on("click", function() {
           _self.hideSideConfig();
        });

    };

    _e.highlightElement = function(elm) {
        if(this.highlightedElement) {
            this.removeHighlightElement()
        }
        elm.addClass('snippet-highlighted-elm');
        this.highlightedElement = elm
    };

    _e.removeHighlightElement = function() {
        if(this.highlightedElement) {
            this.highlightedElement.removeClass("snippet-highlighted-elm");
            this.highlightedElement = null
        }
    };

    _e.activateElement = function() {
        var _self = this;
        _self.deactivateElement();
        _self.renderSideConfig(_self.highlightedElement);
        _self.activatedElement = _self.highlightedElement;
        this.activatedElement.addClass('snippet-activated-elm');
        var nodeType = this.activatedElement.attr('data-style-type');
        if(nodeType == "text" || nodeType == "html" || _self.activatedElement.attr("data-editable") == "true") {
            this.attachEditBtn()
        }
    };

    _e.attachEditBtn = function() {
        var _self = this;
        var editBtn = $('<span class="tool-icon edit snippet-elm-edit-btn"></span>');
        this.activatedElement.append(editBtn);
        editBtn.position({
            my: "right bottom",
            at: "right top",
            of: _self.activatedElement
        });
        editBtn.on("click", function(ev) {
           ev.preventDefault();
            _self.editElmContent();
        });
    };

    _e.editElmContent = function() {
        this.activatedElement.find(".snippet-elm-edit-btn").remove();
        var _self = this, content = this.activatedElement.html(), nodeType = this.activatedElement.attr('data-style-type');
        var popupDom = $("<form class='edit-popup-form snippet-elm-edit'>" +
            "<div class='form-row'><label></label>" +
            "<textarea class='" + (nodeType == "html" ? "wceditor" : "wcredactor") + "' toolbar-type='simple' name='content'>" + content+ "</textarea>" +
            "</div>" +
            "<div class='button-line'>" +
            "<button type='button' class='cancel-button'> " + $.i18n.prop("cancel") + "</button> &nbsp; &nbsp;" +
            "<button type='submit' class='submit-button'>" + $.i18n.prop("enter") + "</button>" +
            "</div>" +
            "</form>");
        bm.editPopup(undefined, $.i18n.prop("edit.content"), undefined, undefined, {
            content: popupDom,
            width: 500,
            draggable: false,
            events: {
                content_loaded: function(popup) {
                    popup.on("close", function() {
                        _self.attachEditBtn()
                    })
                }
            },
            beforeSubmit: function(form, data, popup) {
                _self.activatedElement.html(form.find("textarea").val());
                _self.setDirty();
                popup.close();
                return false;
            }
        })
    };

    _e.deactivateElement = function() {
        if(this.activatedElement) {
            this.activatedElement.removeClass('snippet-activated-elm');
            this.activatedElement.find('.snippet-elm-edit-btn').remove();
            this.activatedElement = null
        }
    };

    _e.renderSideConfig = function(elm) {
        var nodeType = elm.attr('data-style-type'),
            config = this.configBulder.buildConfig(nodeType, elm);
        this.configBar.find(".side-bar-config").replaceWith(config);
        this.configBar.find(".side-bar-config").scrollbar();
        this.attachConfigEvents(config);
        this.iframeWrap.addClass("with-config")
        this.configBar.addClass('active')

    };

    _e.hideSideConfig = function() {
        this.deactivateElement();
        this.configBar.removeClass('active');
        this.iframeWrap.removeClass("with-config")
    };

    _e.attachConfigEvents = function(config) {
        var _self = this;
        config.find('.config').on("change", function() {
            _self.configBulder.onConfigChange($(this), _self.activatedElement);
            _self.setDirty()
        });
        config.find('.clone-remove-button-holder .clone').on("click", function() {
            _self.cloneElm()
        });
        config.find('.clone-remove-button-holder .move').on("click", function() {
            _self.moveElm($(this).attr("direction"))
        });
        config.find('.clone-remove-button-holder .remove').on("click", function() {
            var activeElm = _self.activatedElement;
            _self.hideSideConfig();
            activeElm.remove();
            _self.setDirty()
        })

    };

    _e.cloneElm = function() {
        var cloneElm = this.activatedElement.clone(true);
        cloneElm.removeClass('snippet-activated-elm');
        cloneElm.find('.snippet-activated-elm').removeClass('snippet-activated-elm');
        cloneElm.find('.snippet-elm-edit-btn').remove();
        this.activatedElement.after(cloneElm);
        this.setDirty()
    };

    _e.moveElm = function(direction) {
        var swapElm = this.activatedElement[direction]("[data-cloneable='true']"), groupId = this.activatedElement.attr("group-id");
        if(swapElm.length == 0 || (groupId && !swapElm.is("[group-id='" + groupId + "']") )) {
            return
        }
        if(direction == "prev") {
            swapElm.before(this.activatedElement)
        } else {
            swapElm.after(this.activatedElement)
        }
    }

})();




