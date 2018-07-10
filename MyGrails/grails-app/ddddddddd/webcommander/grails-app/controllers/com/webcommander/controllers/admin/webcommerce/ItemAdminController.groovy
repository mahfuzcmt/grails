package com.webcommander.controllers.admin.webcommerce

import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.common.CommonService
import com.webcommander.webcommerce.CategoryService
import com.webcommander.webcommerce.CombinedProduct
import com.webcommander.webcommerce.ProductService
import com.webcommander.webcommerce.Category
import com.webcommander.webcommerce.Product
import grails.converters.JSON

class ItemAdminController {
    CommonService commonService
    ProductService productService
    CategoryService categoryService

    @Restriction(permission = "product.view.list")
    def loadProductView() {
        if (!params.isCombined) {
            params.notCombined = true;
        }
        params.max = params.max ?: "10";
        if(!params.parent) {
            params.lookup = "recursive"
        }
        if(params.parent == "root") {
            params.parent = null
        }
        Integer count = productService.getProductsCount(params)
        List<Product> products = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            return productService.getProducts(params)
        }
        render(view: "/admin/item/product/appView", model: [products: products, count: count]);
    }

    @Restriction(permission = "category.view.list")
    def loadCategoryView() {
        Long orderTarget = params.long("orderTarget");
        if(orderTarget) {
            categoryService.changeOrder(orderTarget, params.orderAction);
        }
        params.max = params.max ?: "10";
        Integer count = categoryService.getCategoriesCount(params);
        List<Category> categories = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            return categoryService.getCategories(params)
        }
        render(view: "/admin/item/categoryView", model: [categories: categories, count: count])
    }

    def productAdvanceFilter() {
        render(view: "/admin/item/product/filter", model: [d: false])
    }

    def categoryAdvanceFilter() {
        render(view: "/admin/item/category/filter", model: [d: false])
    }

    def loadExplorerView() {
        render(view: "/admin/item/explorerView", model: [d: true]);
    }

    @Restrictions([
        @Restriction(permission = "category.view.list"),
        @Restriction(permission = "product.view.list")
    ])
    def explorePanel() {
        Long id = params.parent = params.id ? params.long("id") : 0;
        String type = params.type;
        params.max = params.max?.toInteger() ?: 10;
        params.offset = params.offset?.toInteger() ?: 0
        List<Category> categories = [];
        List<Product> combinedProducts = [];
        List<Product> products = [];
        List<CombinedProduct> includedProducts = [];
        Integer productCount, categoriesCount, combinedProductCount, count;
        if(type == "combined") {
            count = productService.getIncludedProductsCount(params);
            includedProducts = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
                params.max = max;
                params.offset = offset;
                productService.getIncludedProducts(params)
            }
        } else {
            params.notCombined = true
            if(params.searchText) {
                params.lookup = "recursive"
            }
            productCount = productService.getProductsCount(params);
            categoriesCount = categoryService.getChildCategoriesCount(id, params);
            params.remove("notCombined");
            params.isCombined = true;
            combinedProductCount = productService.getProductsCount(params);
            count = productCount + categoriesCount + combinedProductCount;
            commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
                if (max == -1 || categoriesCount > offset) {
                    categories = categoryService.getChildCategories(id, params, [max: max, offset: offset])
                }
                if (max == -1 || categoriesCount + combinedProductCount > offset && (categories.size() < max)) {
                    Map pCopy = new LinkedHashMap(params)
                    pCopy.max = max == -1 ? max : max - categories.size();
                    pCopy.offset = offset = offset - categoriesCount;
                    combinedProducts = productService.getProducts(pCopy)
                }
                int pickedCount = categories.size() + combinedProducts.size()
                if (max == -1 || pickedCount < max) {
                    Map pCopy = new LinkedHashMap(params)
                    pCopy.max = max - pickedCount;
                    pCopy.offset = offset - combinedProductCount - categoriesCount;
                    pCopy.isCombined = false;
                    pCopy.notCombined = true;
                    products = productService.getProducts(pCopy)
                }
            }
        }
        render(view: "/admin/item/explorePanel", model: [products: products, categories: categories, combinedProducts: combinedProducts, count: count, productCount: productCount, categoriesCount: categoriesCount, combinedProductCount: combinedProductCount, includedProducts: includedProducts])
    }

    def categoryTree() {
        Long parentId = params.long("key");
        List children = categoryService.getChildren(parentId)
        render(children as JSON)
    }

    def loadProductAndCategorySelector() {
        List<Long> productIds = params.list("selectedProducts").collect { it.toLong(0) }
        List<Long> categoryIds = params.list("selectedCategories").collect { it.toLong(0) }
        List<Product> selectedProducts = productIds ? Product.createCriteria().list {
            inList("id", productIds)
        } : []
        List<Category> selectedCategories = categoryIds ? Category.createCriteria().list {
            inList("id", categoryIds)
        } : []
        render(view: "/admin/item/productAndCategorySelector", model: [selectedProducts: selectedProducts, selectedCategories: selectedCategories])
    }
}
