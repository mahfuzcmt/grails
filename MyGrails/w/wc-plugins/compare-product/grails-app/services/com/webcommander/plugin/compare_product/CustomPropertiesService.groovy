package com.webcommander.plugin.compare_product

import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import com.webcommander.events.AppEventManager
import grails.gorm.transactions.Transactional
import grails.util.TypeConvertingMap

@Transactional
class CustomPropertiesService {
    CustomProperties customProperties, alterRowProperties
    Product product
    ProductService productService

    List<CustomProperties> getCustomProperties(Long productId) {
        List<CustomProperties> customProperties = CustomProperties.createCriteria().list {
            eq("product.id", productId)
            order("idx", "asc")
        }
        return customProperties
    }

    Long saveBasics(TypeConvertingMap params) {
        Long productId = params.long("productId");
        List<CustomProperties> productList = getCustomProperties(productId)
        Long idx = 1
        if (productList.size() > 0)
            idx = productList.last().idx + 1;

        CustomProperties customProperties = new CustomProperties()
        customProperties.idx = idx
        customProperties.product = Product.get(productId)
        customProperties.label = params.label
        customProperties.description = params.description
        if (customProperties.save()) {
            return idx
        } else
            return 0
    }

    boolean removeProperties(TypeConvertingMap params) {
        Long id = params.long("id")
        customProperties = CustomProperties.findById(id)
        customProperties.delete()
        return !customProperties.hasErrors()
    }

    boolean updateBasics(TypeConvertingMap params) {
        Long id = params.long("id")
        customProperties = CustomProperties.findById(id)
        if (params.type == "keyEdit")
            customProperties.label = params.newValue
        else
            customProperties.description = params.newValue

        if (customProperties.save())
            return true
        else
            return false
    }

    boolean updateRank(Map params) {
        customProperties = CustomProperties.findById(params.long("thisRow"))
        alterRowProperties = CustomProperties.findById(params.long("alterRow"))

        Long tempIdx = customProperties.idx
        customProperties.idx = alterRowProperties.idx
        alterRowProperties.idx = tempIdx

        if (!customProperties.save())
            return false
        if (!alterRowProperties.save())
            return false

        return true
    }

    List<String> autoComplete(TypeConvertingMap params) {
        Long pId = params.long("id")
        return CustomProperties.where {
            def c1 = CustomProperties
            like("label", "%" + params.query.trim().encodeAsLikeText() + "%");
            notExists CustomProperties.where {
                def c2 = CustomProperties
                eqProperty "c1.label", "c2.label"
                eq "product.id", pId
            }.id()
        }.distinct("label").list()
    }

    List<CustomProperties> findSimilarProduct(TypeConvertingMap params) {
        List<CustomProperties> customPropertieses = CustomProperties.createCriteria().list {
            ne("product.id", params.long("productId"))
            eq("label", params.label)
        }
        return customPropertieses
    }

    boolean importProductProperties(TypeConvertingMap params) {
        Long sproductId = params.long("similarProductGroup");
        Long ret = 0
        String matchedLabel = params.matchedLabel
        product = productService.getProduct(sproductId)

        List<CustomProperties> propertyList = CustomProperties.createCriteria().list {
            ne("label", matchedLabel )
            eq("product.id", sproductId)
            order("idx", "asc")
        }

        TypeConvertingMap data = [:]
        data.productId = params.productId
        propertyList.each {
             data.label = it.label;
             data.description = it.description
             ret = saveBasics(data)
        }

        return ret
    }
}
