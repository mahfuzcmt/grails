package com.webcommander.controllers.rest.admin.webcommerce

import com.webcommander.authentication.annotations.License
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.common.FileService
import com.webcommander.constants.DomainConstants
import com.webcommander.converter.json.JSON
import com.webcommander.rest.ApiCommonService
import com.webcommander.rest.throwable.ApiException
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.RestProcessor
import com.webcommander.web.multipart.WebCommanderMultipartFile
import com.webcommander.webcommerce.Category
import com.webcommander.webcommerce.CategoryService
import com.webcommander.webcommerce.ProductService
import org.apache.commons.httpclient.HttpStatus

class ApiCategoryAdminController extends RestProcessor{
    CategoryService categoryService
    FileService fileService
    ProductService productService
    ApiCommonService apiCommonService

    static allowedMethods = [create: "POST", setProfile: "POST"]

    @Restriction(permission = "category.view.list")
    def list() {
        params.max = params.max ?: "-1";
        params.offset = params.offset ?: 0;
        List<Category> categories = categoryService.getCategories(params);
        rest categories: categories
    }

    @Restriction(permission = "category.view.list")
    def count() {
        def count = categoryService.getCategoriesCount(params);
        rest count: count
    }


    @Restriction(permission = "category.view.list")
    def info() {
        Category category = Category.get(params.id)
        rest category: category
    }

    @Restrictions([
            @Restriction(permission = "category.view.list"),
            @Restriction(permission = "product.view.list")
    ])
    def children() {
        Category category = Category.get(params.id)
        if(!category) {
            throw ApiException('category.not.found', 404)
        }
        List categoryList = categoryService.getCategories([parent: category.id]);
        List productList = productService.getProducts([parent: category.id])
        String productJSON = new JSON(productList, [
                marshallerExclude: ["displayPrice", "previousPrice", "isPurchaseRestricted"]
        ]);
        String categoryJSON = new JSON(categoryList);
        rest "{\"categories\": ${categoryJSON}, \"products\": ${productJSON}}"
    }

    @Restrictions([
            @Restriction(permission = "category.create", params_not_exist = "id"),
            @Restriction(permission = "product.view.list", params_match_key = "property", params_match_value = "products")
    ])
    def create() {
        Map data =  request.getAttribute(DomainConstants.REQUEST_ATTR_KEYS.REQUEST_BODY);
        def imgFile;
        if(data.image_url) {
            File file = fileService.downloadFile(data.image_url)
            imgFile = new WebCommanderMultipartFile(file.name, file.name, URLConnection.guessContentTypeFromName(file.getName()), file.newInputStream())
        }
        Category category = categoryService.saveBasic(params, imgFile)
        if(category){
            rest([status: "success", id: category.id])
        } else {
            throw new ApiException("category.could.not.save", HttpStatus.SC_BAD_REQUEST)
        }
    }

    @Restriction(permission = "category.remove", entity_param = "id", domain = Category, owner_field = "createdBy")
    def delete() {
        Long id = params.long("id");
        try {
            if(categoryService.putCategoryInTrash(id, "yes", "include")){
                rest([status: "success", message: g.message(code: "category.delete.success")])
            }
        } catch (AttachmentExistanceException ex) {
            throw new ApiException("attachment.exists", HttpStatus.SC_FORBIDDEN)
        }
    }

    @Restriction(permission = "category.edit", entity_param = "id", domain = Category, owner_field = "createdBy")
    def setProfile() {
        Map data =  request.getAttribute(DomainConstants.REQUEST_ATTR_KEYS.REQUEST_BODY);
        categoryService.saveCategoryProfile(data)
    }

    @Restriction(permission = "category.edit", entity_param = "id", domain = Category, owner_field = "createdBy")
    def update() {
        Category category = Category.get(params.id);
        if (apiCommonService.saveEntity(category, params)) {
            rest status: "success"
        } else {
            throw new ApiException("category.update.error")
        }
    }
}
