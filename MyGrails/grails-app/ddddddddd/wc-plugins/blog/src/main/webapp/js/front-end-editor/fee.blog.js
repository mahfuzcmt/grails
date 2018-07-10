app.FrontEndEditor.widget.blogPost = function () {
    app.FrontEndEditor.widget.blogPost._super.constructor.apply(this, arguments);
};

var _blog = app.FrontEndEditor.widget.blogPost.inherit(app.FrontEndEditor.widgetBase);

_blog.init = function () {
    var $this = this;
    var panel = this.configPanel;
    $this.widgetId = panel.find("[name='widgetId']").val();
    panel.updateUi();

    var $selectionContainer = panel.find(".blog-selection-config"), $selectBlogPostButton = panel.find("button.select-blog-post");
    panel.find("select[name='selection']").change(function() {
        if ($(this).val() == "custom") {
            $selectionContainer.addClass("custom-selection");
            $selectBlogPostButton.show();
        }
        else {
            $selectionContainer.removeClass("custom-selection");
            $selectBlogPostButton.hide();
        }        
    }).trigger("change");

    $selectBlogPostButton.click(function () {
        $this.initPostSelection(postSelectionSave);
    });

    function postSelectionSave(widgetInst) {
        var selectedPosts = widgetInst.configPanel.find("[name='selectedPosts']").val();
        console.log(selectedPosts);
    }
};

_blog.initPostSelection = function (save_callback, configs) {
    var $this = this;
    var paginationConfigs = $.extend({
        uuid: $this.widgetUUID, type: "blogPost", widgetId: $this.widgetId, max: 10
    }, configs === undefined || configs == null ? {} : configs);
    var maskPanel = $('body');
    bm.mask(maskPanel, '<div><span class="loader"></span></div>');
    bm.ajax({
        data: paginationConfigs,
        url: app.baseUrl + "frontEndEditor/blogSelection",
        dataType: "html"
    }).done(function (resp) {
        bm.unmask(maskPanel);
        var popupConfig = {};
        popupConfig.content = $(resp);
        popupConfig.width = '900px';
        popupConfig.title = $.i18n.prop("select.a.blog");
        popupConfig.content_clazz = 'fee-noMargin';
        popupConfig.events = {
            content_loaded: function (contentEl) {
                var widgetInst = new app.FrontEndEditor.widget["blogPost"]($(contentEl.content), "blogPost", $this.widgetUUID, $this, $this.widgetItemParent, contentEl);
                var panel = widgetInst.configPanel;
                function getPaginationParams() {
                    var sortBy = panel.find("table.sortable-table").data("sortby");
                    var sortsBy = sortBy !== undefined && sortBy.length > 0 ? sortBy.split("_") : ["", ""];
                    var sort = sortsBy[0].toLowerCase(), dir = sortsBy[1].toLowerCase();
                    return $.extend(paginationConfigs, {
                        sortBy: sortBy, sort: sort, dir: dir, tableOnly: true, category: panel.find("#category").val(),
                        searchText: panel.find("[name='searchText']").val()
                    });
                }
                panel.find(".blog-search-header").updateUi();

                function afterPaginationOrSorting(response) {
                    widgetInst.configPanel.find(".blog-list-container").html($(response).find(".blog-list-container").html());
                    widgetInst.initSortable(app.baseUrl + "frontEndEditor/blogSelection", afterPaginationOrSorting, getPaginationParams);
                    widgetInst.initPagination(app.baseUrl + "frontEndEditor/blogSelection", afterPaginationOrSorting, getPaginationParams);
                    selectionInit();
                }

                widgetInst.initSearch(app.baseUrl + "frontEndEditor/blogSelection", afterPaginationOrSorting, getPaginationParams);
                widgetInst.initPagination(app.baseUrl + "frontEndEditor/blogSelection", afterPaginationOrSorting, getPaginationParams);
                widgetInst.initSortable(app.baseUrl + "frontEndEditor/blogSelection", afterPaginationOrSorting, getPaginationParams);
                panel.on("keypress", "form", function (event) {
                    return event.keyCode != 13;
                });

                var $selectedPosts = panel.find("[name='selectedPosts']");
                function selectionInit() {
                    panel.find(".fee-table").updateUi();
                    var selected = $selectedPosts.val();
                    widgetInst.initRowCheckboxMultipleSelection(selected == "" ? [] : selected.split(","), selectionDone);

                    function selectionDone(selected) {
                        $selectedPosts.val(selected.join(","));
                    }
                }
                selectionInit();

                contentEl.el.find(".fee-cancel").click(function () {
                    contentEl.close();
                });

                contentEl.el.find(".fee-save").click(function () {
                    var response = save_callback(widgetInst);
                    if (response === undefined || response) {
                        contentEl.close();
                    }
                });
            }
        };
        var popup = $this.frontEndInstance.renderPopup(popupConfig);
        popup.el.css("zIndex", popup.z_index + 10);
    });
};

_blog.beforeSave = function (ajaxSettings) {
    return true;
};