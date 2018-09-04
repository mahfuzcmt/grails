package com.webcommander.plugin.get_price.controllers.admin

import com.webcommander.authentication.annotations.License
import com.webcommander.constants.DomainConstants
import com.webcommander.listener.SessionManager
import com.webcommander.plugin.get_price.GetPriceCategoryMapping
import com.webcommander.plugin.get_price.GetPriceService
import com.webcommander.plugin.get_price.constants.Constants
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import grails.converters.JSON

class GetPriceAdminController {
    GetPriceService getPriceService
    ProductService productService

    def updateCategory() {
        Boolean result
        String getPriceCategoryUrl = Constants.CATEGORY_FETCH_URL
        def htmlCategory = new URL(getPriceCategoryUrl).getText()
        String categories =  params.categoryJSON
        if(categories) {
            result = getPriceService.updateCategory(categories.split("\n")?.toList())
        } else {
            render([status: "success", htmlCategory: htmlCategory] as JSON)
        }
        if (result) {
            render([status: "success", message: g.message(code: "getprice.category.update.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "getprice.category.update.failure")] as JSON)
        }
    }

    @License(required = "allow_getprice_feature")
    def downloadFeed() {
        List<GetPriceCategoryMapping> mappings = GetPriceCategoryMapping.list()
        List<Product> productList
        Boolean show_out_of_stock_product = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GET_PRICE, "show_out_of_stock_product") == "true"
        Map config = [max: "-1", offset: "0", parentList: mappings.categoryId]
        if(!show_out_of_stock_product) {
            config.stock = "in"
        }
        if(mappings.size()) {
            productList = productService.getProducts(config);
        } else {
            productList = []
        }
        String path = SessionManager.protectedTempFolder.absolutePath
        if(params.category == "true") {
            def categoryXml =  getPriceService.getCategoryXmlFeed()
            File categoryFile = new File(path, "Category-Feed")
            categoryFile.write(categoryXml)
            response.setHeader("Content-disposition", "attachment; filename=\"GetPrice-Category.xml\"")
            response.setHeader("Content-Type", "text/xml")
            response.outputStream << categoryFile.bytes
        } else if(params.product == "true") {
            def productXml =  getPriceService.getProductXmlFeed(productList)
            File productFile = new File(path, "Product-Feed")
            productFile.write(productXml)
            response.setHeader("Content-disposition", "attachment; filename=\"GetPrice-Product.xml\"")
            response.setHeader("Content-Type", "text/xml")
            response.outputStream << productFile.bytes
        }
        response.outputStream.flush()
    }

    @License(required = "allow_getprice_feature")
    def loadAppView() {
        render(view: "/plugins/get_price/admin/appView")
    }

    def getPriceCategoryTree() {
        List list = getPriceService.excelFileToMap()
        render(list as JSON)
    }

    def categoryTree() {
        String type = params.type ?: "";
        List children = getPriceService.getCategoryInfoAsTree(type)
        render(children as JSON)
    }

    def mapCategory() {
        Boolean result = getPriceService.mapCategory(params);
        if (result) {
            render([status: "success"] as JSON)
        } else {
            render([status: "error", message: g.message(code: "mapping.failure")] as JSON)
        }
    }

    def removeMapping() {
        Boolean result = getPriceService.removeMapping(params);
        if (result) {
            render([status: "success"] as JSON)
        } else {
            render([status: "error", message: g.message(code: "remove.mapping.failure")] as JSON)
        }
    }

    def loadConfig() {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GET_PRICE)
        render(view: "/plugins/get_price/admin/config", model: [config: config])
    }
}
