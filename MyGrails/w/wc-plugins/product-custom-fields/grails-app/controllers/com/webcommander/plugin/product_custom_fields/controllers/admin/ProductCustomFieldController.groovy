package com.webcommander.plugin.product_custom_fields.controllers.admin

import com.webcommander.authentication.annotations.License
import com.webcommander.plugin.product_custom_fields.CategoryCheckoutField
import com.webcommander.plugin.product_custom_fields.CategoryCheckoutFieldsTitle
import com.webcommander.plugin.product_custom_fields.ProductCheckoutField
import com.webcommander.plugin.product_custom_fields.ProductCheckoutFieldsTitle
import com.webcommander.plugin.product_custom_fields.ProductCustomFieldService
import com.webcommander.plugin.product_custom_fields.domain.CheckoutField
import com.webcommander.webcommerce.Product
import grails.converters.JSON
import com.webcommander.webcommerce.Category as CAT

class ProductCustomFieldController {

    ProductCustomFieldService productCustomFieldService

    def productEditorTabView() {
        Long pid = params.long("id") ?: 0L
        Product product = Product.get(pid)
        def extraFields = ProductCheckoutField.createCriteria().list {
            eq("product.id", pid)
        }
        boolean sortDesc = params.dir == "desc"
        render(view: "/plugins/product_custom_fields/customfields", model: [product: product, fields: sortDesc ? extraFields.sort {a, b -> b.label <=> a.label} : extraFields.sort {a, b -> a.label <=> b.label}, count: extraFields.size()])
    }

    def categoryEditorTabView() {
        Long cid = params.long("id") ?: 0L
        CAT category = CAT.get(cid)
        def extraFields = CategoryCheckoutField.createCriteria().list {
            eq("category.id", cid)
        }
        boolean sortDesc = params.dir == "desc"
        render(view: "/plugins/product_custom_fields/customfields", model: [category: category, fields: sortDesc ? extraFields.sort {a, b -> b.label <=> a.label} : extraFields.sort {a, b -> a.label <=> b.label}, count: extraFields.size()])
    }

    @License(required = "allow_product_custom_fields_feature")
    def createField() {
        CheckoutField field
        if(params.type == "product") {
            if(params.id) {
                field = ProductCheckoutField.get(params.id)
            } else {
                field = new ProductCheckoutField(product: Product.proxy(params.productId))
            }
        } else {
            if(params.id) {
                field = CategoryCheckoutField.get(params.id)
            } else {
                field = new CategoryCheckoutField(category: CAT.proxy(params.categoryId))
            }
        }
        render(view: "/plugins/product_custom_fields/fieldInfoEdit", model: [field: field])
    }

    @License(required = "allow_product_custom_fields_feature")
    def editProductFieldLabel() {
        ProductCheckoutFieldsTitle title = ProductCheckoutFieldsTitle.findByProduct(Product.proxy(params.productId))
        render(view: "/plugins/product_custom_fields/editGroupTitle", model: [title: title, productId: params.productId, type: "product"])
    }

    @License(required = "allow_product_custom_fields_feature")
    def editCategoryFieldLabel() {
        CategoryCheckoutFieldsTitle title = CategoryCheckoutFieldsTitle.findByCategory(CAT.proxy(params.categoryId))
        render(view: "/plugins/product_custom_fields/editGroupTitle", model: [title: title, categoryId: params.categoryId, type: "category"])
    }

    @License(required = "allow_product_custom_fields_feature")
    def saveField() {
        def result = productCustomFieldService.saveField(params)
        if (result) {
            render([status: "success", message: g.message(code: "field.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "field.could.not.save")] as JSON)
        }
    }

    @License(required = "allow_product_custom_fields_feature")
    def saveFieldTitle() {
        def result = productCustomFieldService.saveFieldGroupTitle(params)
        if (result) {
            render([status: "success", message: g.message(code: "title.group.field.set.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "title.group.field.could.not.update")] as JSON)
        }
    }

    def productFieldDelete() {
        def result = productCustomFieldService.deleteField(params.long("id"), "product")
        if (result) {
            render([status: "success", message: g.message(code: "field.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "field.could.not.delete")] as JSON)
        }
    }

    def categoryFieldDelete() {
        def result = productCustomFieldService.deleteField(params.long("id"), "category")
        if (result) {
            render([status: "success", message: g.message(code: "field.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "field.could.not.delete")] as JSON)
        }
    }
}
