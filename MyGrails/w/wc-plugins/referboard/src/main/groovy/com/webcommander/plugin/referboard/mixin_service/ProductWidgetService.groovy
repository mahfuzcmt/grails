package com.webcommander.plugin.referboard.mixin_service

import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.referboard.ReferboardLib
import com.webcommander.util.AppUtil


class ProductWidgetService {
    def renderReferboardWidget(Map attrs, Writer writer) {
        Map configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.REFERBOARD)
        if(configs.is_enabled == 'true') {
            try {
                ReferboardLib.captureReferboardParams(AppUtil.params, 'cookie')
            } catch (Exception ignore) {}
            renderService.renderView("/plugins/referboard/productWidget/referboard", [configs: configs, productData: attrs.productData], writer)
        }
    }

    def renderReferboardWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/plugins/referboard/productWidget/editor/referboard", [:], writer)
    }
}
