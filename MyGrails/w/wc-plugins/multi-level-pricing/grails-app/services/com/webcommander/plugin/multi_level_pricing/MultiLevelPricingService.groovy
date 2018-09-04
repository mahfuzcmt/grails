package com.webcommander.plugin.multi_level_pricing

import com.webcommander.admin.Customer
import com.webcommander.admin.CustomerGroup
import com.webcommander.annotations.Initializable
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product
import grails.gorm.transactions.Transactional
import grails.util.TypeConvertingMap

@Initializable
@Transactional
class MultiLevelPricingService {

    static void initialize() {
        AppEventManager.on("before-product-delete", { id ->
            ProductMultiLevelPrice.createCriteria().list {
                eq("product.id", id)
            }*.delete();
        })
    }

    Boolean save(TypeConvertingMap params) {
        Product product;
        ProductMultiLevelPrice multiLevelPrice = null
        Long variationId = params.long("variationId")
        if(variationId) {
            product = Product.get(params.productId)
            multiLevelPrice = ProductMultiLevelPrice.findByProductAndVariationId(product, variationId)
        } else {
            product = Product.get(params.id)
            multiLevelPrice = ProductMultiLevelPrice.findByProduct(product)
        }
        multiLevelPrice = multiLevelPrice ?: new ProductMultiLevelPrice(product: product);
        if(multiLevelPrice){
            multiLevelPrice.isActive = params.boolean("isMultiLevelPriceActive")
        }
        if(variationId) {
            multiLevelPrice.variationId = variationId;
        }
        if(!multiLevelPrice.id) {
            multiLevelPrice.save();
        }
        multiLevelPrice.prices*.delete();
        multiLevelPrice.prices = [];
        params.mlp.each{ String k, value ->
            if(k.contains(".")) return ;
            ProductPrice productPrice = new ProductPrice()
            productPrice.price = value.double("price")
            productPrice.customerGroups = [];
            if(value.customerGroup) {
                productPrice.customerGroups = CustomerGroup.where {
                    id in value.list("customerGroup").collect { it.toLong() }
                }.list();
            }
            multiLevelPrice.addToPrices(productPrice)
        }
        multiLevelPrice.save();
        return true;
    }

    Double getMultiLevelPriceForProduct(Long productId, Long variationId = null) {
        Double price
        if(!AppUtil.loggedCustomer) {
            return price
        }
        Map configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MULTI_LEVEL_PRICING);
        Product product = Product.findById(productId);
        Customer customer = Customer.findById(AppUtil.loggedCustomer)
        ProductMultiLevelPrice multiLevelPrice = ProductMultiLevelPrice.findByProductAndVariationId(product, variationId);
        List<Double> prices = []
        multiLevelPrice?.prices.each { productPrice ->
            productPrice.customerGroups.each { customerGroup->
                if((customer.groups.collect {it.id}.contains(customerGroup.id)) && (customerGroup.status == 'A')) {
                    prices.add(productPrice.price)
                }
            }
        }
        price = configs.lowest_or_highest_price == "lowest" ? prices?.min() : prices?.max()
        return price
    }
}
