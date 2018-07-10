package com.webcommander.plugin.product_custom_fields

import com.webcommander.annotations.Initializable
import com.webcommander.events.AppEventManager
import com.webcommander.models.ProductData
import com.webcommander.plugin.product_custom_fields.domain.CheckoutField
import com.webcommander.util.DomainUtil
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.Category as CAT
import com.webcommander.webcommerce.Product
import grails.gorm.transactions.Transactional
import grails.transaction.NotTransactional
import grails.util.TypeConvertingMap
import grails.web.databinding.DataBindingUtils

@Initializable
@Transactional
class ProductCustomFieldService {

    static void initialize() {
        AppEventManager.on("before-product-delete", { id ->
            ProductCheckoutField.createCriteria().list {
                eq("product.id", id)
            }*.delete()
        })

        AppEventManager.on("product-copy", { oldProduct, newProduct ->
            ProductCheckoutField.createCriteria().list {
                eq "product.id", oldProduct.id
            }.each { field ->
                ProductCheckoutField newField = DomainUtil.clone(field, ["product"])
                newField.product = newProduct
                newField.save()
            }
        })

        AppEventManager.on("before-product-delete", { id ->
            ProductCheckoutFieldsTitle.createCriteria().list {
                eq("product.id", id)
            }*.delete()
        })

        AppEventManager.on("before-category-delete", { id ->
            CategoryCheckoutField.createCriteria().list {
                eq("category.id", id)
            }*.delete()
        })

        AppEventManager.on("before-category-delete", { id ->
            CategoryCheckoutFieldsTitle.createCriteria().list {
                eq("category.id", id)
            }*.delete()
        })
    }

    boolean saveField(Map params) {
        CheckoutField field;
        if(params.entityType == "product") {
            if(params.id) {
                field = ProductCheckoutField.get(params.id)
                field.options.clear()
            } else {
                field = new ProductCheckoutField()
            }
        } else {
            if(params.id) {
                field = CategoryCheckoutField.get(params.id)
                field.options.clear()
            } else {
                field = new CategoryCheckoutField()
            }
        }
        if(params.type != DomainConstants.PRODUCT_CHECKOUT_FIELD_TYPE.LONG_TEXT && params.type != DomainConstants.PRODUCT_CHECKOUT_FIELD_TYPE.TEXT) {
            params.validation = ""
        }
        DataBindingUtils.bindObjectToInstance(field, params, null, ["id", "options"], null)
        params.list("options").each { option ->
            if(option) { //preventing addition of empty string
                field.options.add(option)
            }
        }
        field.save(flush: true)
        return !field.hasErrors()
    }

    boolean saveFieldGroupTitle(Map params) {
        def entity = params.type == "product" ? Product.proxy(params.productId) : CAT.proxy(params.categoryId)
        def title = params.type == "product" ? ProductCheckoutFieldsTitle.findByProduct(entity) : CategoryCheckoutFieldsTitle.findByCategory(entity)
        if(title && !params.title) {
            title.delete()
        } else if(params.title) {
            if(!title) {
                title = params.type == "product" ? new ProductCheckoutFieldsTitle() : new CategoryCheckoutFieldsTitle()
                title[params.type] = entity
            }
            title.title = params.title
            title.save()
        } else {
            return true
        }
        return !title.hasErrors()
    }

    boolean deleteField(Long id, String type) {
        CheckoutField field
        if(type == "product") {
            field = ProductCheckoutField.get(id)
        } else {
            field = CategoryCheckoutField.get(id)
        }
        field.delete()
        return true
    }

    @NotTransactional
    def getFieldsNTitle(Product product) {
        List<CheckoutField> fields = ProductCheckoutField.createCriteria().list {
            eq("product.id", product.id)
        }
        String title = ProductCheckoutFieldsTitle.findByProduct(product)?.title
        List<CAT> parents = product.parents
        Closure includeCats;
        includeCats = { category ->
            List<CheckoutField> _fields = CategoryCheckoutField.createCriteria().list {
                eq("category.id", category.id)
            }
            fields.addAll(_fields)
            if(!title) {
                title = CategoryCheckoutFieldsTitle.findByCategory(category)?.title
            }
            if(category.parent) {
                includeCats category.parent
            }
        }
        parents.each includeCats
        return [fields: fields.unique { a, b -> a.name <=> b.name}, title: title]
    }

    @NotTransactional
    int getFieldsCount(Product product) {
        int fields = ProductCheckoutField.createCriteria().count {
            eq("product.id", product.id)
        }
        List<CAT> parents = product.parents
        Closure includeCats;
        includeCats = { category ->
            fields += CategoryCheckoutField.createCriteria().count {
                eq("category.id", category.id)
            }
            if(category.parent) {
                includeCats category.parent
            }
        }
        parents.each includeCats
        return fields
    }

    def variationsForCartAdd(List variations, ProductData productData, TypeConvertingMap params) {
        Closure add
        add = { name, value ->
            if(value instanceof List) {
                add name, value.join(", ")
            } else {
                variations.add(name + ": " + value)
            }
        }
        params?.custom.each { name, value ->
            if(value && !(value instanceof Map)) {
                add name, value
            }
        }
        return variations
    }
}
