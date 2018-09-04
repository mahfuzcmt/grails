package com.webcommander.controllers.rest.site.webcommerce

import com.webcommander.converter.json.JSON
import com.webcommander.rest.throwable.ApiException
import com.webcommander.util.RestProcessor
import com.webcommander.webcommerce.Category
import com.webcommander.webcommerce.CategoryService
import com.webcommander.webcommerce.ProductService
import grails.gorm.DetachedCriteria

class ApiCategoryController extends RestProcessor{
    CategoryService categoryService;
    ProductService productService;

    def count() {
        Map filterMap = [name: params.name]
        Long count = categoryService.filterOutAvailableCategoryCount(null, filterMap)
        rest count: count
    }

    def list(){
        Map filterMap = [name: params.name]
        filterMap.max = params.max ?: -1;
        filterMap.offset = params.offset ?: 0;
        List<Category> categories = categoryService.filterOutAvailableCategories(null, filterMap)
        rest([categories: categories], [
            marshallerExclude: [
                "products", "availableToCustomers", "availableToCustomerGroups",
                "restrictPriceFor", "restrictPriceExceptCustomers", "restrictPriceExceptCustomerGroups",
                "restrictPurchaseFor", "restrictPurchaseExceptCustomers", "restrictPurchaseExceptCustomerGroups"
            ]
        ])
    }

    def children() {
        Integer max = params.int("max", -1), offset = params.int("offset", 0)
        DetachedCriteria criteria = categoryService.getAvailablityFilterCriteria(null, [:])
        criteria.with {
            eq("id", params.long("id"))
        }
        Category category = criteria.get();
        if(!category) {
            throw new ApiException('category.not.found', 404)
        }
        def subCategoryIds = Category.where {
            parent.id == category.id
        }.list().id
        def productIds = category.products.id
        List categoryList = categoryService.filterOutAvailableCategories(subCategoryIds, [max: max, offset: offset, name: params.name]);
        Long count = categoryService.filterOutAvailableCategoryCount(subCategoryIds, [name: params.name])
        if(max > 0) {
            max = (max - categoryList.size()) > 0 ? max - categoryList.size() : 0
        }
        if(offset > 0) {
            offset = (offset - count) >= 0 ? (offset - count) : 0
        }
        List productList = [];
        if(max) {
            productList = productService.filterAvailableProducts(productIds, [rawProduct: true, max: max, offset: offset, name: params.name])
        }
        String productJSON = new JSON(productList, [
            marshallerExclude: [
                "relatedProducts", "availableToCustomerGroups", "availableToCustomers", "inventoryAdjustments", "createdBy", "videos",
                "basePrice", "costPrice", "salePrice", "expectToPayPrice",
                "restrictPriceFor", "restrictPriceExceptCustomers", "restrictPriceExceptCustomerGroups",
                "restrictPurchaseFor", "restrictPurchaseExceptCustomers", "restrictPurchaseExceptCustomerGroups",
                "calculatedRestrictPriceFor", "calculatedRestrictPriceExceptCustomerGroups", "calculatedRestrictPriceExceptCustomers",
                "calculatedRestrictPurchaseFor", "calculatedRestrictPurchaseExceptCustomers", "calculatedRestrictPurchaseExceptCustomerGroups"
            ]
        ]);
        String categoryJSON = new JSON(categoryList, [marshallerExclude: ["products", "availableToCustomers", "availableToCustomerGroups"]]);
        rest "{\"categories\": ${categoryJSON}, \"products\": ${productJSON}}"
    }

    def info() {
        DetachedCriteria criteria = categoryService.getAvailablityFilterCriteria(null, [:])
        criteria.with {
            eq("id", params.long("id"))
        }
        Category category = criteria.get();
        if(!category) {
            throw new ApiException('category.not.found', 404)
        }
        rest([category: category], [
            marshallerExclude: [
                "products", "availableToCustomers", "availableToCustomerGroups",
                "restrictPriceFor", "restrictPriceExceptCustomers", "restrictPriceExceptCustomerGroups",
                "restrictPurchaseFor", "restrictPurchaseExceptCustomers", "restrictPurchaseExceptCustomerGroups"
            ]
        ])
    }
}
