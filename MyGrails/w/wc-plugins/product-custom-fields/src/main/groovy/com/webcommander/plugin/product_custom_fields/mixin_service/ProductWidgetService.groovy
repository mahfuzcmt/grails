package com.webcommander.plugin.product_custom_fields.mixin_service

import com.webcommander.plugin.product_custom_fields.ProductCustomFieldService
import com.webcommander.webcommerce.Product
import grails.util.Holders

/**
 * Created by zobair on 15/04/2014.*/
class ProductWidgetService {

    private static ProductCustomFieldService _service;
    private ProductCustomFieldService getService() {
        if(_service) {
            return _service
        }
        return _service = Holders.grailsApplication.mainContext.getBean(ProductCustomFieldService)
    }

    def renderCustomFieldWidget(Map attrs, Writer writer) {
        Product product = attrs.product;
        Map model = service.getFieldsNTitle(product)
        renderService.renderView("/plugins/product_custom_fields/productWidget/customFieldWidget", model, writer)
    }

    def renderCustomFieldWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/plugins/product_custom_fields/productWidget/editor/customFieldWidget",[:], writer)
    }
}