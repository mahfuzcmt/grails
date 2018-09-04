package com.webcommander.plugin.filter.controllers.admin.design



class WidgetController {

    def filterGroupService

    def filterGroupShortConfig() {

        List filterGroups = filterGroupService.getFilterGroups([isActive: true]);

        render(view: "/plugins/filter/admin/widget/loadFilterGroupSettingsShort", model: [noAdvance: true, filterGroups: filterGroups]);
    }

    def filterShortConfig() {
        if(params.config.filterConfig == null) {
            params.config.filterConfig = ['category']
        }
        if(params.config.filterConfig instanceof String) {
            params.config.filterConfig = [params.config.filterConfig]
        }
        render(view: "/plugins/filter/admin/widget/loadFilterSettingsShort", model: [noAdvance: true]);
    }

    def shopByFilterGroupShortConfig() {

        List filterGroups = filterGroupService.getFilterGroups([isActive: true]);

        render(view: "/plugins/filter/admin/widget/loadShopByFilterGroupSettingsShort", model: [noAdvance: true, filterGroups: filterGroups]);
    }
}