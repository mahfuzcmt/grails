package com.webcommander.plugin.filter.mixin_service

import com.webcommander.plugin.filter.Filter
import com.webcommander.plugin.filter.FilterGroup
import com.webcommander.plugin.filter.FilterGroupItem
import com.webcommander.plugin.filter.FilterGroupService
import com.webcommander.plugin.filter.FilterProfile
import com.webcommander.plugin.filter.resolver.FilterProfileResolver
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Category
import com.webcommander.widget.Widget
import grails.converters.JSON
import grails.util.Holders

class WidgetService {

    static def _filterGroupService

    def getFilterGroupService() {
        if(_filterGroupService) {
            return _filterGroupService
        }
        return _filterGroupService = Holders.grailsApplication.mainContext.getBean("filterGroupService")
    }

    def populateFilterInitialContentNConfig(Widget widget) {

    }

    def populateFilterGroupInitialContentNConfig(Widget widget) {

    }

    def renderFilterWidget(Widget widget, Writer writer) {
        def params = AppUtil.params
        Category category = null

        if (params.categoryId) {
            category = Category.load(params.categoryId.trim())
        } else if (params.url) {
            category = Category.findByUrl(params.url)
        }

        Map resolverContext = [:]
        resolverContext.categoryId = category?.id
        FilterProfileResolver resolver = new FilterProfileResolver()
        FilterProfile profile = resolver.resolve(resolverContext)

        if(profile) {
            Collection<String> filterProperties = Filter.all.collect{it.property}
            Collection<Filter> filters = profile.filters
            Map config = [:]
            Map propertyValues = [:]

            filterProperties.each {
                if(params[it]) {
                    propertyValues[it] = params[it]
                }
            }

            filters.each {
                if(it.property.matches("productCondition")) {
                    config[it.property] = ["none"] + getPropertyList(category, it.property)
                }
            }

            config["category_id"] = category?.id

            if(params.action.equals("category") || params.controller.matches("widget|layout|filterPage|page")) {
                renderService.renderView("/plugins/filter/widget/filter", [widget: widget, filterProfile: profile, propertyValues: propertyValues, config: config, filterGroupSelectedValues: prepareFilterGroupSelectedValues()], writer)
            }

        } else {
            renderService.renderView("/plugins/filter/widget/filter", [widget: widget, isAdmin: true], writer)
        }
    }

    def getPropertyList(Category category, String property) {
        def list = []
        if (category) {
            list = category.products[property].unique() - null
        }

        return list
    }

    def prepareFilterGroupSelectedValues() {
        def params = AppUtil.params
        List fgi = params.list("filter-group-item")
        List<Long> groupItems = []
        if (fgi) {
            fgi.each {
                if (it) {
                    groupItems.add(Long.parseLong(it))
                }
            }
        }

        Map filterGroupSelectedValues = [:]
        groupItems.each {
            FilterGroupItem groupItem = FilterGroupItem.get(it)
            filterGroupSelectedValues[groupItem.filterGroup.id] = groupItem.id
        }

        return filterGroupSelectedValues
    }

    def populateShopByFilterGroupInitialContentNConfig(Widget widget) {
        //widget.params = ([shopBy: "B"] as JSON).toString()
    }

    def renderShopByFilterGroupWidget(Widget widget, Writer writer) {
        def config = [:]
        List shopData = []
        config = JSON.parse(widget.params)

        def filterGroup
        if (config.shopBy) {
            filterGroup = filterGroupService.getActiveFilterGroup(Long.parseLong(config.shopBy))
            if (filterGroup) {
                shopData = filterGroupService.getFilterGroupItems(Long.parseLong(config.shopBy))
            }
        }

        //shopData.sort {it.title}

        renderService.renderView("/plugins/filter/widget/shopByFilterGroup", [widget: widget, config: config, shopData: shopData, filterGroup: filterGroup], writer)
    }

}
