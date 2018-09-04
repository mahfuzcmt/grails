 bm.onReady(app.tabs, "edit_content", function() {
    bm.onReady(app.tabs.edit_content, "page", function() {
        app.tabs.edit_content.product_page = function (config) {
            config.containerType = "product-page"
            _super.constructor.apply(this, arguments);
            this.ajax_url = app.baseUrl + "productAdmin/editProductPage?id=" + config.containerId;
        }

        var _p = app.tabs.edit_content.product_page.inherit(app.tabs.edit_content.page)

        var _super = app.tabs.edit_content.product_page._super;

        _p.widgetClass = "product-widget";
        _p.contentSaveUrl = "productWidget/save"
        _p.modifyFavoriteWidgetUrl = app.baseUrl + "app/modifyFavoriteProductWidget"
        _p.getWidgetTag = function (uuid, widgetType) {
            return "<wi:productwidget type='" + widgetType + "'/>"
        }

        _p.populateWidgetSelector = function () {
            var _self = this;
            this.widgetSelector.find("option").remove();
            var widgets = this.activeSection.find(_self.supportedWidgetSelector).not("." + _self.widgetClass + " ." + _self.widgetClass);
            var newOptions = [];
            newOptions.push("<option value=''>" + $.i18n.prop("active.section.widgets") + "</option>");
            $.each(widgets, function () {
                var _this = $(this);
                var widgetType = _this.attr("widget-type");
                var uuid = _this.attr("id")
                if(!uuid) {
                    uuid = "wi-" + bm.getUUID();
                    _this.attr("id", uuid)
                }
                newOptions.push("<option value='" + uuid + "'> " + widgetType + "-" + uuid.substring(3) + " </option>");
            });
            for (var i = 0; i < newOptions.length; i++) {
                this.widgetSelector.append($(newOptions[i]));
            }
            this.widgetSelector.trigger("chosen:updated");
        }

        _p.getEmptyWidgetDom = function (widgetType) {
            return "<div class='" + this.widgetClass + " widget-" + widgetType + "' widget-type='" + widgetType + "'> " + $.i18n.prop(widgetType.minusCase()) + " " + $.i18n.prop("widget") + " </div>"
        }

        _p.updateSidebar = function(uuid, tab) {
        }

        _p.getWidgetRenderAjaxIOptions = function(options) {
            options.controller = "productWidget"
            return options;
        }
    })
});

