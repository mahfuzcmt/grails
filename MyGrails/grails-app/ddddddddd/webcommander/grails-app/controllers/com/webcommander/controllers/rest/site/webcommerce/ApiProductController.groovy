package com.webcommander.controllers.rest.site.webcommerce

import com.webcommander.constants.DomainConstants
import com.webcommander.manager.HookManager
import com.webcommander.rest.throwable.ApiException
import com.webcommander.util.AppUtil
import com.webcommander.util.RestProcessor
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService

//TODO: will improve code later
class ApiProductController extends RestProcessor {
    ProductService productService

    def count() {
        List ids = params.list("ids")
        ids = ids ? ids.collect {it.toString().toLong(0)} : null
        if(params.parentId) {
            ids = Product.createCriteria().list {
                projections {
                    property("id")
                }
                or {
                    eq("parent.id", params.long("parentId"))
                    parents {
                        eq("id", params.long("parentId"))
                    }
                }
                if(ids) {
                    "in"("id", ids)
                }
                eq("isInTrash", false)
                eq("isParentInTrash", false)
            }
        }
        def count = productService.filterOutAvailableProductCount(ids, [name: params.name]);
        rest count: count
    }

    def list() {
        Map filterMap = [rawProduct: true, name: params.name]
        filterMap.max = params.max ?: -1;
        filterMap.offset = params.offset ?: 0;
        String sort = null;
        if (params.sort == "name") {
            sort = params.dir == "desc" ? "ALPHA_DESC" : "ALPHA_ASC"
        } else if (params.sort == "price") {
            sort = params.dir == "desc" ? "PRICE_DESC" : "PRICE_ASC"
        }
        List ids = params.list("ids")
        ids = ids ? ids.collect {it.toString().toLong(0)} : null
        if(params.parentId) {
            ids = Product.createCriteria().list {
                projections {
                    property("id")
                }
                or {
                    eq("parent.id", params.long("parentId"))
                    parents {
                        eq("id", params.long("parentId"))
                    }
                }
                if(ids) {
                    "in"("id", ids)
                }
                eq("isInTrash", false)
                eq("isParentInTrash", false)
            }
        }
        filterMap["product-sorting"] = sort
        filterMap.rawProduct = true
        List<Product> products = productService.filterAvailableProducts(ids, filterMap)
        rest([products: products], [
            marshallerExclude: [
                "relatedProducts", "availableToCustomerGroups", "availableToCustomers", "inventoryAdjustments", "createdBy", "videos",
                "basePrice", "costPrice", "salePrice", "expectToPayPrice",
                "restrictPriceFor", "restrictPriceExceptCustomers", "restrictPriceExceptCustomerGroups",
                "restrictPurchaseFor", "restrictPurchaseExceptCustomers", "restrictPurchaseExceptCustomerGroups",
                "calculatedRestrictPriceFor", "calculatedRestrictPriceExceptCustomerGroups", "calculatedRestrictPriceExceptCustomers",
                "calculatedRestrictPurchaseFor", "calculatedRestrictPurchaseExceptCustomers", "calculatedRestrictPurchaseExceptCustomerGroups"
            ]
        ])
    }

    def info() {
        Product product = productService.getProductIfAvailable(params.long("id"), [:])
        if (!product) {
            throw new ApiException("product.not.found", 404)
        }
        List marshallerExclude = [
            "availableToCustomerGroups", "availableToCustomers", "inventoryAdjustments", "createdBy",
            "basePrice", "costPrice", "salePrice", "expectToPayPrice",
            "restrictPriceFor", "restrictPriceExceptCustomers", "restrictPriceExceptCustomerGroups",
            "restrictPurchaseFor", "restrictPurchaseExceptCustomers", "restrictPurchaseExceptCustomerGroups",
            "calculatedRestrictPriceFor", "calculatedRestrictPriceExceptCustomerGroups", "calculatedRestrictPriceExceptCustomers",
            "calculatedRestrictPurchaseFor", "calculatedRestrictPurchaseExceptCustomers", "calculatedRestrictPurchaseExceptCustomerGroups"
        ]
        Map config = [
            marshallerExclude: marshallerExclude,
            marshallerInclude: [],
            relatedProducts: [details: true, marshallerExclude: marshallerExclude]
        ]
        config = HookManager.hook("api-product-details-marshaller-config", config, product)
        rest([product: product], config)
    }

    def settings() {
        Map config = (Map) AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT)
        Map result = [
            label_for_call_for_price: site.message(code: config.label_for_call_for_price).toString(),
            label_for_expect_to_pay: site.message(code: config.label_for_expect_to_pay).toString(),
            label_for_base_price: site.message(code: config.label_for_base_price).toString(),
            add_to_cart: config.add_to_cart
        ]
        result = HookManager.hook("productConfigForFrontEndAPI", result, config)
        rest(result)
    }
}