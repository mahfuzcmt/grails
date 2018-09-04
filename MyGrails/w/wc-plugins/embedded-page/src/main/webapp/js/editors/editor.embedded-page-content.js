 bm.onReady(app.tabs, "edit_content", function() {
    bm.onReady(app.tabs.edit_content, "page", function() {
        app.tabs.edit_content.embebbed_page = function(config) {
            config.containerType = "embedded"
            _super.constructor.apply(this, arguments);
            this.has_layout = true //made it true to pretend to be a page with layout
            this.ajax_url = app.baseUrl + "embeddedPage/editContent?id=" + config.containerId;
            this.section = "body"
        }

        var _ep = app.tabs.edit_content.embebbed_page.inherit(app.tabs.edit_content.page)

        var _super = app.tabs.edit_content.embebbed_page._super;

        _ep.editContainerJsUrl = app.baseUrl + 'embeddedPage/editJs';

        _ep.loadCsss = function() {
            this.pageBody.append("<input class='layout-id' type='hidden'>");
            _super.loadCsss.call(this)
        }
    })
})