app.FrontEndEditor.prototype.snippet = function () {
    var _self = this;
    var popupElement;
    var widgetPanel;
    var widgetUUID;
    var hostElement;
    var snippetElement;
    var categoryElement;
    var templateElement;
    var widgetInstance;

    var defaults = {
        columnWidth: 420
    };

    var snippetEvent = {
        snippetFilter: function () {
            templateElement.masonry({
                columnWidth: defaults.columnWidth,
                itemSelector: '.fee-snippet-template'
            });
            categoryElement.find('.category').on("click", function () {
                var currEl = $(this);
                var categoryName = currEl.attr('data-category');
                snippetElement.find('.fee-snippet-template').show();
                var selector = 'fee-snippet-template';
                if (categoryName != 'all') {
                    snippetElement.find('.fee-snippet-template:not(.' + categoryName + ')').hide();
                    selector = categoryName;
                }
                templateElement.masonry({
                    columnWidth: defaults.columnWidth,
                    itemSelector: '.' + selector
                });
                var category_title = currEl.find(".label").text();
                snippetElement.find(".category-wrapper").find(".fee-title").text(category_title);
                categoryElement.slideToggle();
            });
        },
        snippetInsert: function () {
            snippetElement.on("click", ".fee-snippet-template", function () {
                var templateUUID = $(this).attr('uuid');
                var cachedData = _self.cachedWidgetData[widgetUUID];

                bm.ajax({
                    data: {uuid: templateUUID},
                    url: app.baseUrl + "frontEndEditor/snippetContent",
                    dataType: "html"
                }).done(function (resp) {

                    bm.ajax({
                        url: app.baseUrl + 'widget/saveSnippetWidgetAndContent',
                        data: {
                            name: widgetUUID,
                            templateUUID: templateUUID || '',
                            repositoryType: 'local',
                            description: 'no description',
                            content: resp,
                            widgetType: 'snippet',
                            uuid: widgetUUID,
                            containerId: $('body').find('.page-id').val(),
                            containerType: 'page'
                        },
                        success: function (resp) {
                            if (resp.status = "success") {

                                var rowPanel = hostElement.closest('.fee-widget-row');
                                var widgetDom = $(resp['html']);
                                var parentElement;
                                if(widgetPanel.parents('.fee-widget-chooser:first').length > 0){
                                    parentElement = widgetPanel.parents('.fee-widget-chooser:first');
                                }else{
                                    parentElement = widgetPanel;
                                    widgetDom.removeAttr('class id widget-id widget-type');
                                }
                                var widgetElement = _self.layout.widget.update(parentElement, widgetDom[0], "snippet", widgetUUID, typeof cachedData !== 'undefined');
                                widgetInstance.widgetContentPlacement(hostElement, widgetElement);

                                widgetElement.data('data-cache', resp['serialized']);
                                _self.cachedWidgetData[widgetUUID] = resp;
                                var widgetData = JSON.parse(resp.serialized);
                                _self.cachedWidgetData[widgetUUID]['serialized'] = widgetData;

                                _self.events.save(function (){
                                    _self.layout.setColumnAsEqualHeight(rowPanel);
                                    //_self.portlet.restoreSortable(true);
                                    if (_self.popupInstance) {
                                        _self.popupInstance.close();
                                        widgetElement.closest('.fee-widget-chooser').find('.fee-add-widget').text('+ ' + $.i18n.prop("add.content"));
                                        widgetElement.closest('.fee-widget-chooser').find('.fee-item-list').hide();
                                    }
                                });
                            }
                        }
                    });
                });
            });
        }
    };

    var postInit = {
        initVars: function () {
            snippetElement = popupElement.find(".fee-snippet-wrapper");
            categoryElement = snippetElement.find(".template-category-list");
            templateElement = snippetElement.find(".fee-snippet-templates");
        },
        _bind: function () {

            snippetElement.find(".category-wrapper").find(".fee-title").on("click", function () {
                categoryElement.slideToggle();
            });

            snippetEvent.snippetFilter();
            snippetEvent.snippetInsert();

        }
    };

    return {
        init: function (_popupElement, _widgetPanel, _widgetUUID, _hostElement, _widgetInstance) {
            popupElement = _popupElement;
            widgetPanel = _widgetPanel;
            widgetUUID = _widgetUUID;
            hostElement = _hostElement;
            widgetInstance = _widgetInstance;
            postInit.initVars();
            postInit._bind();
        }
    };
};
