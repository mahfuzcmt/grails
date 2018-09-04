package com.webcommander.plugin.product_custom_information.controllers.admin

import com.webcommander.plugin.product_custom_information.ProductCustomInformationService
import grails.converters.JSON

class ProductCustomInformationController {
    ProductCustomInformationService productCustomInformationService;

    def saveCustomFields() {
        Map result = productCustomInformationService.save(params);
        if (result) {
            result.status = "success"
            result.message = g.message(code: "custom.information.${params.id ? "update" : "save"}.success" )
            render(result as JSON)
        } else {
            render([status: "error", message: g.message(code:  "custom.information.save.error")] as JSON)
        }
    }

    def removeCustomField() {
        if (productCustomInformationService.delete(params.informationID as Long)) {
            render([status: "success", message: g.message(code: "custom.information.remove.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "custom.information.remove.error")] as JSON)
        }
    }
}
