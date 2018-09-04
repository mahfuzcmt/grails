package com.webcommander.plugin.discount.mixin_service

import com.webcommander.plugin.discount.models.DiscountData
import com.webcommander.util.AppUtil


class ProductWidgetService {

    def renderDiscountWidget(Map attrs, Writer writer) {

        //renderService.renderView("/plugins/discount/productWidget/discount", [selectedDiscountData: AppUtil.session.selectedDiscountData], writer)

        Boolean isShowDiscountInformation = true

        if (AppUtil.session.selectedDiscountData) {
            DiscountData data = AppUtil.session.selectedDiscountData
            if ((AppUtil.params.action && AppUtil.params.action.equals("product"))
                    && (AppUtil.params.controller && AppUtil.params.controller.equals("page"))
                    && !data.discount.isDisplayDiscountInformationProdDetail) {
                isShowDiscountInformation = false
            }
        }

        if (isShowDiscountInformation) {
            renderService.renderView("/plugins/discount/productWidget/discount", [selectedDiscountData: AppUtil.session.selectedDiscountData], writer)
        }

    }

    def renderDiscountWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/plugins/discount/productWidget/editor/discount", [:], writer)
    }
}
