package com.webcommander.plugin.variation

import com.webcommander.manager.HookManager
import com.webcommander.models.ProductData
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService

public class ProductVariation {
    Long id

    Boolean active = false
    Boolean isBase = false
    Product product
    Collection<VariationOption> options = []
    VariationDetails details

    static hasMany = [options : VariationOption]

    static constraints = {
        details(nullable: true)
    }

    static fieldMarshaller = [
            details: {ProductVariation variation ->
                Map details = [:]
                details = HookManager.hook("${variation.details.model}-variation-details-for-api", details, variation)
                return details
            }
    ]
    
    static marshallerExclude = ["product"]

    static transients = ['lookUpVariation', 'findData']

    public ProductData findData() {
        ProductService productService = ProductService.getInstance()
        return productService.getProductData(product, [options: options.id])
    }
    
    static ProductVariation lookUpVariation(Long pId, List optionsId) {
        List<VariationOption> optionList = optionsId.collect { VariationOption.get(it) }
        List<ProductVariation> productVariations = ProductVariation.createCriteria().list {eq("product.id", pId)}
        return productVariations.find {it.options.collect{it.id}.containsAll(optionList.collect {it.id})}
    }
    
}