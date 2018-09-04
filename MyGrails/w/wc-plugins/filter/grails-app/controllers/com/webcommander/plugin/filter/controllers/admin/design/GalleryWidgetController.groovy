package com.webcommander.plugin.filter.controllers.admin.design

import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.filter.Filter
import com.webcommander.plugin.filter.FilterGroupItem
import com.webcommander.plugin.filter.FilterService
import com.webcommander.widget.Widget
import grails.converters.JSON
import grails.util.Holders

class GalleryWidgetController {

    FilterService filterService = Holders.grailsApplication.mainContext.getBean(FilterService)

    def loadFilterConfig(Widget widget) {
        Map config = JSON.parse(widget.params)
        List<FilterGroupItem> filterGroupItems =  config.galleryContentType == DomainConstants.GALLERY_CONTENT_TYPES.filter ? filterService.getFilterGroupItemsInOrder(widget.widgetContent.contentId) : []
        render(view: "/plugins/filter/admin/widget/galleryConfig/filter", model: [filters: filterGroupItems, config: config])
    }
}
