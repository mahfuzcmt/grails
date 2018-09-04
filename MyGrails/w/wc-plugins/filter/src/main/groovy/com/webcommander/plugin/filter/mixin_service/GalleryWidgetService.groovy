package com.webcommander.plugin.filter.mixin_service

import com.webcommander.plugin.filter.FilterGroupItem
import com.webcommander.util.SortAndSearchUtil
import grails.util.Holders

class GalleryWidgetService {

    static def _filterService

    def getFilterService() {
        if(_filterService) {
            return _filterService
        }
        return _filterService = Holders.grailsApplication.mainContext.getBean("filterService")
    }

    Map getGalleryModelForFilter(Map model, Map params) {
        List filterGroupItemIds = model.widget.widgetContent.contentId.collect { it.longValue() };
        List<FilterGroupItem> filterGroupItemList = filterService.getFilterGroupItemsInOrder(filterGroupItemIds);
        model.items = SortAndSearchUtil.sortInCustomOrder(filterGroupItemList, "id", filterGroupItemIds);
        return model
    }
}
