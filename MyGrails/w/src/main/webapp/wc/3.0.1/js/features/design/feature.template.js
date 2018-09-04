app.tabs.template = function(configs) {
    this.text = $.i18n.prop("templates")
    this.tip = $.i18n.prop("manage.template");
    this.ui_class = "template";
    this.ajax_url = app.baseUrl + "templateAdmin/loadAppView";
    app.tabs.layout._super.constructor.apply(this, arguments);
}

app.ribbons.web_design.push({
    text: $.i18n.prop("template"),
    processor: app.tabs.template,
    ui_class: "template"
})

app.tabs.template.inherit(app.TwoPanelResizeable);

var _t = app.tabs.template.prototype;
_t.resize_disabled = true
_t.left = 600;
_t.left_boundary = [300, Number.MAX_VALUE];
_t.right_boundary = [300, Number.MAX_VALUE];

(function() {
    function attachEvent() {
        var _self = this, body = this.body, filters = body.find("select.filter");
        filters.on("change", function() {
            _self.reload();
        });
        function rightPanelEvents() {
            _self.paginator = _self.body.find(".pagination").obj();
            _self.paginator.onPageClick = _rightPanelReload;
            body.find(".floating-mask").hide();
            body.find(".right-panel .body").scrollbar();
            body.find(".template-thumb .install").click(function() {
                var $this = $(this), thumb = $this.closest(".template-thumb"), id = thumb.attr("template-id"),
                    selectedColor = thumb.find(".color.selected");
                var color = "";
                if(selectedColor.length) {
                    var colorName = selectedColor.attr("name").trim(), colorCode = selectedColor.attr("color").trim();
                    color = colorName + "-" + colorCode.substring(1)
                }
                _self.install(id, color, thumb.attr("template-name"));
            });
            body.find(".template-thumb .color-block").on("click", function() {
                var $this = $(this), colorName = $this.attr("name"), colorCode = $this.attr("color"), color = colorName + "-" + colorCode.substring(1), thumb = $this.closest(".template-thumb"), liveURL = thumb.attr("live-url"), previewUrl = app.baseUrl + "templateAdmin/preview?liveURL=" + encodeURIComponent(liveURL) + "&color=" + color;
                liveURL = liveURL.endsWith("/") ? liveURL : (liveURL + "/");
                thumb.find(".color-block.selected").removeClass("selected");
                $this.addClass("selected")
                thumb.find("a.demo-link").attr("href", liveURL + "app/demoTemplate?color=" + color);
                thumb.find("a.preview-link").attr("href", previewUrl);
            });
        }
        function _rightPanelReload() {
            var reloadPanel = _self.body.find(".right-panel");
            reloadPanel.loader();
            var paginator = _self.paginator, data = {max: paginator.getItemsPerPage(), offset: (paginator.currentPage - 1) * paginator.itemsPerPage}
            filters.each(function() {
                var $this = $(this)
                data[$this.attr("name")] = $this.val()
            });
            bm.ajax({
                url: app.baseUrl + "templateAdmin/reloadTemplate",
                dataType: "html",
                data: data,
                response: function() {
                    reloadPanel.loader(false)
                },
                success: function(resp) {
                    var res = $(resp);
                    reloadPanel.find(".body").html(res.filter(".body").html());
                    reloadPanel.find(".footer").html(res.filter(".footer").html());
                    reloadPanel.updateUi();
                    rightPanelEvents.call(_self);
                }
            });
        }
        rightPanelEvents.call(_self)
    }

    _t.init = function() {
        app.tabs.template._super.init.call(this);
        this.attachLeftPanelEvents();
        attachEvent.call(this);
    }
})();

_t.reload = function() {
    this.paginator.onPageClick(this.paginator.getCurrentPage())
}

_t.attachLeftPanelEvents = function() {
    var _self = this, body = this.body
    body.find(".installed-colors .color").on("click", function() {
        var $this = $(this), colorPanel = $this.closest(".installed-colors");
        if(!$this.is(".selected")) {
            _self.changeColor($this.attr("name"), function() {
                colorPanel.find(".color.selected").removeClass("selected");
                $this.addClass("selected")
            })
        }
    });

    body.find("[name=templateContainerClass]").on("change", function() {
        _self.changeTemplateContainerClass($(this).val())
    });
    body.find(".template-image.body").scrollbar()
}

_t.reloadLeftPanel = function() {
    var _self = this, reloadPanel = this.body.find(".left-panel");
    reloadPanel.loader();
    bm.ajax({
        url: app.baseUrl + "templateAdmin/leftPanel",
        dataType: "html",
        response: function() {
            reloadPanel.loader(false)
        },
        success: function(resp) {
            var res = $(resp);
            reloadPanel.html(res.html());
            reloadPanel.updateUi();
            _self.attachLeftPanelEvents()
        }
    });
}

_t.install = function(id, color, name) {
    var _self = this, popupDom;
    bm.editPopup(app.baseUrl + "templateAdmin/installTemplatePopup", $.i18n.prop("confirm.install.template", [name]), null, {id: id, color: color}, {
        width: 620,
        clazz: "template-confirm",
        beforeSubmit: function(form, settings, popup) {
            $.extend(settings, {timeout: 3600000})
            popupDom = this
            bm.mask(this, '<div class="bg-white"><div class="loader2"></div><div class="wait-message">' + $.i18n.prop('this.may.take.some.time') + '</div></div>');
        },
        success: function () {
            _self.body.find(".template-image > img").clearCache(function() {
                _self.reloadLeftPanel()
                _self.reload()
                app.global_event.trigger("template-install")
            });
        },
        error: function() {
            popupDom.unmask();
        },
        complete: function() {
            popupDom.unmask();
        }
    });
}

_t.changeColor = function(color, success) {
    var _self = this;
    this.maskLeftPanel()
    bm.ajax({
        url: app.baseUrl + "templateAdmin/changeColor",
        data: {color: color},
        success: function() {
            success()
        },
        complete: function() {
            _self.maskLeftPanel(false)
        }
    })
};

_t.changeTemplateContainerClass = function(clazz) {
    var _self = this;
    this.maskLeftPanel()
    bm.ajax({
        url: app.baseUrl + "templateAdmin/changeTemplateContainerClass",
        data: {templateContainerClass: clazz},
        complete: function() {
            _self.maskLeftPanel(false)
        }
    })
};

_t.maskLeftPanel = function(flag) {
    var leftPanel = this.body.find(".left-panel")
    if(flag == false) {
      leftPanel.unmask()
    } else {
      leftPanel.mask()
    }
};

app.tabs.template.preview = function(path) {
    var imgUrl = app.baseUrl + "templates/" + path + "/images/large.jpg";
    var preview = $("<img src='" + imgUrl + "'>");
    var popUp = bm.viewPopup(undefined, undefined, {content: preview, clazz: "view-popup template-preview"});
    preview.on("load", function() {
        popUp.position();
    })
}
