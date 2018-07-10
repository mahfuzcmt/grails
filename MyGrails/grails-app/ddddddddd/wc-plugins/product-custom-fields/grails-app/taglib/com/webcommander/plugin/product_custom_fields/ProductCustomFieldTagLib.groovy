package com.webcommander.plugin.product_custom_fields

import com.webcommander.webcommerce.Product
import com.webcommander.constants.DomainConstants
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class ProductCustomFieldTagLib {
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g
    ProductCustomFieldService productCustomFieldService

    static namespace = "productField"
    
    def adminJSs = { attrs, body ->
        out << body()
        out << app.javascript(src: 'plugins/product-custom-fields/js/customfields.js')
    }

    def productEditorTabHeader = { attrs, body ->
        out << body()
        if(attrs.product.productType != DomainConstants.PRODUCT_TYPE.GIFT_CARD) {
            out << '<div class="bmui-tab-header" data-tabify-tab-id="customfields" data-tabify-url="' + app.relativeBaseUrl() + 'productCustomField/productEditorTabView?id=' + pageScope.productId + '">'
            out << '<span class="title">' + g.message(code: "custom.fields.order") + '</span>'
            out << '</div>'
        }
    }

    def productEditorTabBody = { attrs, body ->
        out << body()
        out << '<div id="bmui-tab-customfields"></div>'
    }

    def categoryEditorTabHeader = { attrs, body ->
        out << body()
        out << '<div class="bmui-tab-header" data-tabify-tab-id="customfields" data-tabify-url="' + app.relativeBaseUrl() + 'productCustomField/categoryEditorTabView?id=' + pageScope.categoryId + '">'
        out << '<span class="title">' + g.message(code: "custom.fields.order") + '</span>'
        out << '</div>'
    }

    def categoryEditorTabBody = { attrs, body ->
        out << body()
        out << '<div id="bmui-tab-customfields"></div>'
    }

    def addCartPopup = { attrs, body ->
        out << body()
        Map model = productCustomFieldService.getFieldsNTitle(pageScope.product)
        out << g.include(view: "plugins/product_custom_fields/customFieldBlock.gsp", model: model)
    }

}
