package com.webcommander.plugin.get_price.controllers.site

import com.webcommander.authentication.annotations.License
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.get_price.GetPriceCategoryMapping
import com.webcommander.plugin.get_price.GetPriceService
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService

class GetPriceController {
    GetPriceService getPriceService
    ProductService productService

    @License(required = "allow_getprice_feature")
    def product() {
        List<GetPriceCategoryMapping> mappings = GetPriceCategoryMapping.list()
        List<Product> productList
        Boolean show_out_of_stock_product = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GET_PRICE, "show_out_of_stock_product") == "true"
        Map config = [max: "-1", offset: "0"]
        if(!show_out_of_stock_product) {
            config.stock = "in"
        }
        if(params.id) {
            Long id = params.long("id")
            config.parentList = [id]
            productList = productService.getProducts(config)
        } else {
            if(mappings.size()) {
                config.parentList = mappings.categoryId
                productList = productService.getProducts(config)
            } else {
                productList = []
            }
        }
        def xml =  getPriceService.getProductXmlFeed(productList)
        render(text: xml, contentType: "text/xml", encoding: "UTF-8")
    }

    @License(required = "allow_getprice_feature")
    def category() {
        def xml =  getPriceService.getCategoryXmlFeed()
        render(text: xml, contentType: "text/xml", encoding: "UTF-8")
    }

}
