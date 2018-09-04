package com.webcommander.controllers.rest.admin.webcommerce

import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.common.FileService
import com.webcommander.models.ProductData
import com.webcommander.rest.ApiCommonService
import com.webcommander.rest.throwable.ApiException
import com.webcommander.util.RestProcessor
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import org.apache.commons.httpclient.HttpStatus
import org.springframework.web.multipart.MultipartFile

class ApiProductAdminController extends RestProcessor {
    ProductService productService
    FileService fileService
    ApiCommonService apiCommonService

    @Restriction(permission = "product.view.list")
    def list() {
        params.max = params.max ?: "-1";
        params.offset = params.offset ?: 0;
        if(!params.parent) {
            params.lookup = "recursive"
        }
        if(params.parent == "root") {
            params.parent = null
        }
        List<Product> products = productService.getProducts(params);
        Map config = [
           marshallerExclude: ["displayPrice", "previousPrice", "isPurchaseRestricted"]
        ]
        rest products: products, config
    }

    @Restriction(permission = "product.view.list")
    def count() {
        def count = productService.getProductsCount(params);
        rest count: count
    }

    @Restriction(permission = "product.view.list")
    def info() {
        Product product = Product.get(params.id)
        if(!product) {
            new ApiException("product.not.found", HttpStatus.SC_NOT_FOUND)
        }
        Map config = [
                marshallerExclude: ["displayPrice", "previousPrice", "isPurchaseRestricted"]
        ]
        rest product: product, config
    }

    @Restrictions([
            @Restriction(permission = "product.create", params_not_exist = "id"),
            @Restriction(permission = "product.edit.properties", entity_param = "id", domain = Product, owner_field = "createdBy")
    ])
    def create() {
        Product product = productService.saveBasics(params)
        if(product) {
            rest status: "success", id: product.id
        } else {
            throw new ApiException("product.could.not.save")
        }
    }

    @Restriction(permission = "product.remove", entity_param = "id", domain = Product, owner_field = "createdBy")
    def delete() {
        if(productService.putProductInTrash(params.long("id"), "yes", "include")) {
            rest status: "success", message: g.message(code: "product.delete.success")
        } else {
            throw new ApiException("product.delete.failure")
        }
    }

    @Restriction(permission = "product.edit.properties", entity_param = "id", domain = Product, owner_field = "createdBy")
    def updateProfile() {
        if(productService.saveProfiles(params)) {
            rest status: "success"
        } else {
            throw new ApiException("profile.update.failed")
        }
    }

    @Restriction(permission = "product.edit.properties", entity_param = "productId", domain = Product, owner_field = "createdBy")
    def imageAdd() {
        List<MultipartFile> files = []
        if(params.image_url) {
            files.add(fileService.downloadAsMultipartFile(params.image_url))
        }
        Product product = productService.getProduct(params.long("productId"))
        if(productService.saveImages(product, files)) {
            rest status: "success", message: g.message(code: "product.image.save.success")
        } else {
            throw new ApiException("product.image.save.error")
        }
    }

    @Restriction(permission = "product.edit.properties", entity_param = "id", domain = Product, owner_field = "createdBy")
    def imageDelete() {
        List<Long> ids = [];
        ids.add(params.long("id"))
        if(productService.removeProductImages(ids)) {
            rest status: "success", g.message(code: "product.image.remove.success")
        } else {
            throw new ApiException("product.image.remove.error")
        }
    }

    @Restriction(permission = "product.edit.properties", entity_param = "id", domain = Product, owner_field = "createdBy")
    def imageSetDefault() {
        if(productService.setDefaultImage(params.long("productId"), params.long("id"))) {
            rest status: "success"
        } else {
            throw new ApiException("set.default.image.failed")
        }
    }

    @Restriction(permission = "product.edit.properties", entity_param = "productId", domain = Product, owner_field = "createdBy")
    def inventoryAdjust() {
        Long productId = params.long("productId");
        Integer quantity = params.int("changeQuantity") * -1;
        if(productService.updateStock(productId, quantity, params.note)) {
            rest status: "success"
        } else {
            throw new ApiException("inventory.adjust.error")
        }
    }

    @Restriction(permission = "product.edit.properties", entity_param = "id", domain = Product, owner_field = "createdBy")
    def update() {
       Product product = Product.get(params.id);
       Boolean result = apiCommonService.saveEntity(product, params)
       if(result) {
           rest status: "success"
       } else {
           throw  new ApiException("product.update.error")
       }
    }
}
