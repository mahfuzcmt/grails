package com.webcommander.plugin.compare_product.controllers.admin

import com.webcommander.authentication.annotations.License
import com.webcommander.common.CommonService
import com.webcommander.plugin.compare_product.CustomProperties
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.compare_product.CustomPropertiesService
import com.webcommander.util.AppUtil
import grails.converters.JSON

class CompareProductAdminController {
    CustomPropertiesService customPropertiesService
    CommonService commonService

    def loadConfig() {
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.COMPARE_PRODUCT)
        render(view: "/plugins/compare_product/admin/config", model: [config: config])
    }

    def loadCustomProperties() {
        params.max = params.max ?: "10";
        params.offset = params.offset ?: "0";
        List<CustomProperties> customProperties = customPropertiesService.getCustomProperties(params.long("productId"))
        def count = customProperties.size()
        render(view: "/plugins/compare_product/admin/customProperties", model: [customProperties: customProperties, count: count])
    }

    @License(required = "allow_compare_product_feature")
    def saveCustomProperties() {
        Long productId = params.long("productId");
        boolean res = commonService.isUnique(CustomProperties, [field: "label", value: params.label, compositeField: "product.id", compositeValue: productId])
        if (!res) {
            render([status: "error", message: g.message(code: "product.label.duplicated")] as JSON)
            return
        }
        Long result = customPropertiesService.saveBasics(params)
        if (result != 0) {
            render([status: "success", message: g.message(code: "product.properties.save.success"), idx: result] as JSON)
        } else {
            render([status: "error", message: g.message(code: "product.properties.could.not.save")] as JSON)
        }
    }

    @License(required = "allow_compare_product_feature")
    def updateCustomProperties() {
        Long productId = params.long("productId");
        boolean res = commonService.isUnique(CustomProperties, [field: "label", value: params.newValue, compositeField: "product.id", compositeValue: productId])
        if (!res && params.type == "keyEdit") {
            render([status: "error", message: g.message(code: "product.label.duplicated")] as JSON)
            return
        }
        def result = customPropertiesService.updateBasics(params)
        if (result) {
            render([status: "success", message: g.message(code: "product.properties.updated.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "product.properties.could.not.updated")] as JSON)
        }
    }

    def removeCustomProperties() {
        def result = customPropertiesService.removeProperties(params)
        if (result) {
            render([status: "success", message: g.message(code: "product.properties.remove.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "product.properties.remove.fail")] as JSON)
        }
    }

    @License(required = "allow_compare_product_feature")
    def updateRank() {
        def result = customPropertiesService.updateRank(params)
        if (result) {
            render([status: "success", message: g.message(code: "product.properties.rank.updated")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "product.properties.rank.update.fail")] as JSON)
        }

    }

    def autoComplete() {
        List<String> suggestion = customPropertiesService.autoComplete(params)
        render(["query": "Unit", "suggestions": suggestion] as JSON)
    }

    def importProperties() {
        List<CustomProperties> similarProductList = customPropertiesService.findSimilarProduct(params)
        render(view: "/plugins/compare_product/admin/similarProductPopUp", model: [similarProductList: similarProductList, matchedLabel: params.matchedLabel, productId: params.productId])
    }

    def importProduct() {
        boolean result = customPropertiesService.importProductProperties(params)
        if (result) {
            render([status: "success", message: g.message(code: "product.properties.imported")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "product.properties.import.failed")] as JSON)
        }

    }
}
