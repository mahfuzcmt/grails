var tabAccordion = function() {
    $(".widget.widget-tabAccordion").each(function() {
        var widget = $(this);
        var tab = widget.find(".tab-accordion-tab");
        var loader = "<div class='loader-mask div-mask'><span class='vertical-aligner'></span><img src='" + app.baseUrl + "plugins/tab-accordion/images/site/loading.gif'></div>";
        if(tab.length) {
            tab.tabify({
                load: function(data) {
                    var panel = data.panel;
                    bindEvent(panel);
                },
                loader_template: loader
            });
        } else {
            var accordionPanel = widget.find(".tab-accordion-accordion");
            accordionPanel.accordion({}).on("load", function(evt, item) {
                bindEvent(item);
            });
        }
    });

    function bindEvent(panel) {
        var config = panel.config("content");
        if(config.type == 'product') {
            initializeProductInfoView(config.id, panel, "short");
        }
    };
}

$(function() {
    tabAccordion();
});