package com.webcommander.plugin.filter.mixin_service

import com.webcommander.plugin.filter.FilterGroup
import com.webcommander.plugin.filter.FilterGroupProductAssoc
import com.webcommander.plugin.filter.FilterGroupService
import com.webcommander.util.AppUtil
import com.webcommander.widget.Widget
import grails.util.Holders

/**
 * Created by sharif ul islam on 18/04/2018.
 */
class ProductWidgetService {

    static def _filterGroupService

    def getFilterGroupService() {
        if(_filterGroupService) {
            return _filterGroupService
        }
        return _filterGroupService = Holders.grailsApplication.mainContext.getBean("filterGroupService")
    }

    def renderFilterGroupWidget(Map attrs, Writer writer) {
        def params = AppUtil.params
        def product = attrs.get("product")
        //List<FilterGroup> filterGroups = filterGroupService.getFilterGroups([isActive: true]);
        //def selectedFilterGroupItem = filterGroupService.getFilterGroupItem(new Long(3))
        List<FilterGroupProductAssoc> productAssocList = FilterGroupProductAssoc.createCriteria().list {
            eq("product.id", product.id)
        }

        renderService.renderView("/plugins/filter/widget/filterGroup", [productAssocList: productAssocList], writer)

    }

    def renderFilterGroupWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/plugins/filter/admin/widget/editor/filterGroup", [:], writer)
    }

}
