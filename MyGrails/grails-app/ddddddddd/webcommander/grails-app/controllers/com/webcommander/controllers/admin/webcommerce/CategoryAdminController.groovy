package com.webcommander.controllers.admin.webcommerce

import com.webcommander.admin.Customer
import com.webcommander.admin.CustomerGroup
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.common.CommonService
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.webcommerce.Category
import com.webcommander.webcommerce.CategoryService
import com.webcommander.webcommerce.ProductService
import grails.converters.JSON
import groovy.json.JsonSlurper;

class CategoryAdminController {
    CommonService commonService
    CategoryService categoryService
    ProductService productService

    @Restriction(permission = "category.edit", entity_param = "id", domain = Category, owner_field = "createdBy")
    def loadCategoryEditor() {
        Long categoryId = params.id ? params.long("id") : null;
        render(view: "/admin/item/category/editor", model: [categoryId: categoryId]);
    }

    def loadCategoryBulkEditor() {
        JsonSlurper jsonSlurper = new JsonSlurper()
        List<Long> ids = jsonSlurper.parseText(params.ids).collect{it.toLong()}
        render(view: "/admin/item/category/bulkEdit/bulkEditor", model: [categoryIds: ids]);
    }

    @Restrictions([
        @Restriction(permission = "category.create", params_not_exist = "id"),
        @Restriction(permission = "product.view.list", params_match_key = "property", params_match_value = "products")
    ])
    def loadCategoryProperties() {
        Category category = params.id ? categoryService.getCategory(params.long("id")) : new Category(sku: commonService.getSKUForDomain(Category));
        def parentCategory = 0
        if(params.categoryId) {
            parentCategory = params.long("categoryId")
        }
        switch (params.property) {
            case "basic":
                render(view: "/admin/item/category/basic", model: [category: category, parentCategory: parentCategory])
                break;
            case "metatags":
                render(view: "/admin/item/category/metatag", model: [category: category])
                break;
            case "products":
                render(view: "/admin/item/category/link", model: [category: category, products: category.products])
                break;
            case "productSettings":
                render(view: "/admin/item/category/productSettings", model: [category: category])
                break;
            case "advanced":
                render(view: "/admin/item/category/advanced", model: [category: category])
                break;
        }
    }

    def loadCategoryBulkProperties() {
        JsonSlurper jsonSlurper = new JsonSlurper()
        params.ids = jsonSlurper.parseText(params.ids).collect{it.toLong()}
        List<Category> categories = params.ids ? Category.findAllByIdInList(params.ids) : []
        Integer count = categories.size()
        switch (params.property) {
            case "basic":
                render(view: "/admin/item/category/bulkEdit/basic", model: [categories: categories, count: count])
                break;
            case "advanced":
                render(view: "/admin/item/category/bulkEdit/advanced", model: [categories: categories, count: count])
                break;
        }
    }

    def saveBasicBulkProperties() {
        def save = categoryService.saveBasicBulkProperties(params);
        if(save) {
            render([status: "success", message: g.message(code: "category.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "category.could.not.save")] as JSON)
        }
    }

    def saveAdvancedBulkProperties() {
        def save = categoryService.saveAdvancedBulkProperties(params);
        if(save) {
            render([status: "success", message: g.message(code: "category.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "category.could.not.save")] as JSON)
        }
    }

    def customerSelectionPopup() {
        Category category = Category.get(params.id)
        Set<Customer> customers = category.availableToCustomers
        Set<CustomerGroup> groups = category.availableToCustomerGroups
        render(view: "/admin/common/customerAndGroupSelection", model: [customers: customers, customerGroups: groups])
    }

    def saveBasic() {
        def imgFile = request.getFile("image");
        if(params.deleteTrashItem){
            def field = params.deleteTrashItem.collect{it.key}[0];
            def value = params[field];
            categoryService.deleteTrashItemAndSaveCurrent(field,value);
        }
        if(categoryService.saveBasic(params, imgFile)){
            render([status: "success", message:g.message(code: "category.save.success")] as JSON)
        }else {
            render([status: "error", message:g.message(code: "category.could.not.save")] as JSON)
        }
    }

    def saveMetatags() {
        if (categoryService.saveMetatags(params)) {
            render([status: "success", message: g.message(code: "category.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "category.could.not.save")] as JSON)
        }
    }

    def saveLinked() {
        if (categoryService.saveLinkedProducts(params)) {
            render([status: "success", message: g.message(code: "category.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "category.could.not.save")] as JSON)
        }
    }

    def saveProductSettings() {
        if (categoryService.saveProductSettings(params)) {
            render([status: "success", message: g.message(code: "category.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "category.could.not.save")] as JSON)
        }
    }

    def saveSeoProperties() {
        def result = categoryService.saveSeo(params)
        if (result) {
            render([status: "success", message: g.message(code: "category.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "category.could.not.save")] as JSON)
        }
    }

    def view() {
        Category category = categoryService.getCategory(params.long("id"));
        render(view: "/admin/item/category/view", model: [category: category])
    }

    @Restriction(permission = "category.remove", entity_param = "id", domain = Category, owner_field = "createdBy")
    def deleteCategory() {
        try {
            Long id = params.long("id");
            if(categoryService.putCategoryInTrash(id, params.at2_reply, params.at1_reply)) {
                render([status: "success", message: g.message(code: "category.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "category.could.not.delete")] as JSON)
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    @Restriction(permission = "category.remove", entity_param = "ids", domain = Category, owner_field = "createdBy")
    def deleteSelected() {
        def ids = [];
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        int deleteCount = categoryService.putSelectedCategoriesInTrash(ids)
        if (deleteCount == ids.size()) {
            render([status: "success", message: g.message(code: "selected.categories.delete.success")] as JSON)
        } else if(deleteCount == 0) {
            render([status: "error", message: g.message(code: "selected.categories.could.not.delete")] as JSON)
        } else {
            render([status: "warning", message: g.message(code: "selected.not.deleted", args: [total - deleteCount, total, g.message(code: "categories")])] as JSON)
        }
    }

    def isUnique(){
        if(params.field == "name"){
            Category parent = params.compositeValue ? Category.get(params.compositeValue) : null
            String existenceStatus = categoryService.isNameUnique(params.long("id"), parent, params.value)
            render(commonService.generateResponseForUniqueCheck(existenceStatus, params.field, params.value) as JSON)
        } else {
            render(commonService.responseForUniqueField(Category, params.long("id"), params.field, params.value) as JSON)
        }
    }

    @Restriction(permission = "category.view.list")
    def loadCategoriesForSelection() {
        params.max = params.max ?: "10"
        Integer count = categoryService.getCategoriesCount(params)
        List<Category> categories = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset;
            categoryService.getCategories(params);
        }
        render(view: "/admin/item/category/selectionPanel", model: [count: count, categories: categories]);
    }

    def restoreFromTrash(){
        def field = params.field;
        def value = params.value;
        Long id = categoryService.restoreCategoryFromTrash(field, value, params.compositeField, params.compositeValue);
        if(id){
            render([status: "success", message: g.message(code: "restored.successfully", args: ["Category"]), type: "category", id: id] as JSON)
        }
    }

    def changeOrder() {
        Long id = params.long("id");
        int value = params.int("value");
        categoryService.changeOrder(id, value)
        render([status: "success"] as JSON)
    }

    def saveCurrentOrder() {
        if(categoryService.saveCurrentOrder(params)){
            render([status: "success", message:"order.could.not.save"] as JSON)
        } else {
            render([status: "success", message:"order.save.failure"] as JSON)
        }
    }

    def saveAdvanced(){
        if (categoryService.saveAdvanced(params)) {
            render([status: "success", message: g.message(code: "category.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "category.could.not.save")] as JSON)
        }
    }
}
