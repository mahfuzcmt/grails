package com.webcommander.controllers.admin.design

import com.webcommander.Page
import com.webcommander.admin.ConfigService
import com.webcommander.authentication.annotations.License
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.common.CommonService
import com.webcommander.common.ImageService
import com.webcommander.config.SiteConfig
import com.webcommander.config.StoreDetail
import com.webcommander.constants.DomainConstants
import com.webcommander.content.*
import com.webcommander.design.FrontEndEditorService
import com.webcommander.design.Layout
import com.webcommander.design.WidgetService
import com.webcommander.license.blocker.PageLicense
import com.webcommander.license.blocker.ProductLicense
import com.webcommander.manager.PathManager
import com.webcommander.models.MockStaticResource
import com.webcommander.tenant.TenantContext
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import com.webcommander.util.Base64DataInputStream
import com.webcommander.util.StringUtil
import com.webcommander.webcommerce.Category
import com.webcommander.webcommerce.CategoryService
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import com.webcommander.widget.Widget
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.io.FileType
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.io.FilenameUtils
import org.springframework.web.multipart.MultipartFile


class FrontEndEditorController {
    WidgetService widgetService
    FrontEndEditorService frontEndEditorService
    ContentService contentService
    CommonService commonService
    PageService pageService
    ProductService productService
    CategoryService categoryService
    NavigationService navigationService
    ImageService imageService
    ConfigService configService

    def galleryConfig() {
        render(view: "/frontEndEditor/loadGallery", model: [])
    }

    def imageConfig() {
        render(view: "/frontEndEditor/loadImage", model: [])
    }

    def articleConfig() {
        params.max = params.max ?: "10";
        params.oldSection = params.section
        params.section = params.sectionFilter
        params.isPublished = "true"
        params.isInTrash = "false"
        Integer count = params.createFormOnly ? 0 : contentService.getArticlesCount(params)
        List<Article> articles = params.createFormOnly || !count ? [] : commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            contentService.getArticlesForWidget(params)
        }
        params.section = params.oldSection
        def defaultArticle = params.widget.widgetContent.contentId
        if (params.article) {
            defaultArticle = [params.long("article")]
        }
        if (params.selectedArticleId) {
            Article article = Article.read(params.long("selectedArticleId"))
            params.newContent = article?.content
            params.articleName = article?.name
            params.section = article?.sectionId
        }
        params.articleTitle = params.widget.title ?: params.articleTitle

        render(view: "/frontEndEditor/${params.onlyBody ? 'loadInnerArticle' : 'loadArticle'}", model: [
                count         : count, articles: articles, defaultArticle: defaultArticle, selectFormOnly: params.selectFormOnly == "true",
                createFormOnly: params.createFormOnly == "true", singleSelect: params.singleSelect == "true", hideCreateSection: params.hideCreateSection == "true"
        ])
    }

    def productConfig() {
        if (!params.widget.params) {
            params.config = ["show-pagination": "none", item_per_page: "10", price: "true", description: "true", add_to_cart: "true", "item-per-page-selection": "false", sortable: "false"];
        }

        params.max = params.max ?: "10"
        if (!params.containsKey('parent')) {
            params.parent = "all"
        }
        Integer count = productService.getProductsCount(params)
        List<Product> products = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset;
            productService.getProducts(params);
        }
        render(view: "/frontEndEditor/${params.onlyLeft ? "loadLeftProductSection" : "loadProduct"}", model: [count: count, allProducts: products, products: productService.getProductsInOrder(params.widget.widgetContent.contentId)]);
    }

    def categoryConfig() {
        params.max = params.max ?: "10"
        Integer count = categoryService.getCategoriesCount(params)
        List<Category> categories = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset;
            categoryService.getCategories(params);
        }
        render(view: "/frontEndEditor/${params.onlyLeft ? "loadLeftCategorySection" : "loadCategory"}", model: [count: count, allCategories: categories, categories: categoryService.getCategoriesInOrder(params.widget.widgetContent.contentId)]);
    }

    @Restriction(permission = "navigation.edit", entity_param = "id", domain = Navigation)
    def navigationConfig() {
        Long id = params.navigationId ? params.long('navigationId') : (params.widget && params.widget?.widgetContent ? params.widget?.widgetContent?.getAt(0)?.contentId : 0);
        def items = navigationService.getNavigationItems(id)
        items = navigationService.populateNavigationItemsChildList(items);
        render view: "/frontEndEditor/editNavigation", model: [navigationId: id, items: items]
    }

    def saveWidget() {
        Widget widget = params.widgetType == 'image' ? frontEndEditorService.saveImageWidget(params, request, session, app.customResourceBaseUrl()) : frontEndEditorService.saveWidget(params)
        if (widget) {
            def widgetContent = widgetService.renderWidget(widget.widgetType.capitalize(), widget);
            render([status: "success", html: widgetContent, serialized: widget.serialize()] as JSON);
            return;
        }
        render([status: "error"] as JSON);
    }

    @Restrictions([
            @Restriction(permission = "page.create", params_not_exist = "id"),
            @Restriction(permission = "page.edit.properties", params_exist = "id", entity_param = "id", domain = Page, owner_field = "createdBy")
    ])
    @License(required = "page_limit", checker = PageLicense.Edit)
    def addPage() {
        Long defaultLayoutId = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "default_layout").toLong(0)
        Page page = new Page(layout: Layout.get(defaultLayoutId))
        render(view: "/frontEndEditor/editPage", model: [page: page]);
    }

    @Restrictions([
            @Restriction(permission = "product.create", params_not_exist = "id"),
            @Restriction(permission = "product.edit.properties", params_exist = "id", entity_param = "id", domain = Product, owner_field = "createdBy")
    ])
    @License(required = "product_limit", checker = ProductLicense)
    def addProduct() {
        Product product = new Product(sku: commonService.getSKUForDomain(Product));
        render(view: "/frontEndEditor/editProduct", model: [product: product]);
    }

    @Restrictions([
            @Restriction(permission = "product.create", params_not_exist = "id"),
            @Restriction(permission = "product.edit.properties", params_exist = "id", entity_param = "id", domain = Product, owner_field = "createdBy")
    ])
    @License(required = "product_limit", checker = ProductLicense)
    def saveProduct() {
        params.isCombined = "false"
        params.isAvailable = "true"
        params.productType = DomainConstants.PRODUCT_TYPE.PHYSICAL
        params.availableFor = DomainConstants.PRODUCT_AVAILABLE_FOR.EVERYONE
        def productInstance = productService.saveBasics(params)
        if (productInstance) {
            params.id = productInstance.id;
            if ((productService.updateImages(params))) {
                def images = request.getMultiFileMap().images;
                if (images && images.size()) {
                    productService.saveImages(productInstance, images);
                }
            }
            render([status: "success", message: g.message(code: "product.save.success"), id: productInstance.id, name: params.name] as JSON)
        } else {
            render([status: "error", message: g.message(code: "product.could.not.save")] as JSON)
        }
    }

    @Restrictions([
            @Restriction(permission = "category.create", params_not_exist = "id")
    ])
    def addCategory() {
        Category category = new Category(sku: commonService.getSKUForDomain(Category));
        render(view: "/frontEndEditor/editCategory", model: [category: category]);
    }

    @Restrictions([
            @Restriction(permission = "category.create", params_not_exist = "id")
    ])
    def saveCategory() {
        params.isAvailable = "true"
        params.parent = ""
        params.availableFor = DomainConstants.PRODUCT_AVAILABLE_FOR.EVERYONE
        def imgFile = request.getFile("image");
        if (categoryService.saveBasic(params, imgFile)) {
            render([status: "success", message: g.message(code: "category.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "category.could.not.save")] as JSON)
        }
    }

    def isUnique() {
        def response = commonService.responseForUniqueField(params.field == 'sku' ? Product : Page, params.long("id"), params.field, params.value)
        if(response.status == "error"){
            if(response.existenceStatus == "in-trash"){
                response.errorFlag = "inTrash"
                response.remove("existenceStatus")
            }
        }
        render(response as JSON)
    }

    def restoreFromTrash() {
        def field = params.field
        def value = params.value
        Long id = pageService.restorePageFromTrash(field, value)
        if(id) {
            render([status: "success", message: g.message(code: "restored.successfully", args: ["Page"]), type: "page", id: id] as JSON)
        }
    }

    @Restrictions([
            @Restriction(permission = "page.create", params_not_exist = "id"),
            @Restriction(permission = "page.edit.properties", params_exist = "id", entity_param = "id", domain = Page, owner_field = "createdBy")
    ])
    @License(required = "page_limit", checker = PageLicense.Save)
    def savePage() {
        params.remove("action")
        params.remove("controller")
        params.active = "true";
        params.disableTracking = "false";
        params.url = '';
        params.isFrontEndForm = true;
        params.visibility = DomainConstants.PAGE_VISIBILITY.OPEN;

        def pageAvailability = commonService.responseForUniqueField(Page, params.long("id"), "name", params.name)
        if(pageAvailability.status == "error" && pageAvailability.existenceStatus == "in-trash"){
            Page page = pageService.getPage(params.name)
            if(page && !frontEndEditorService.deletePageFromTrash(page, params)){
                render([status: "error", message: g.message(code: "page.save.failure")] as JSON)
                return;
            }
        }

        def pageId = pageService.save(params, session.admin)
        if (pageId) {
            render([status: "success", message: g.message(code: "page.save.success"), "pageId": pageId] as JSON)
        } else {
            render([status: "error", message: g.message(code: "page.save.failure")] as JSON)
        }
    }

    def getPage(){
        Long pageId = params.long("id");
        render([page: pageService.getPage(pageId), navigationIds: navigationService.getNavigationIds(pageId)] as JSON);
    }

    def deletePage(){
        Long id = params.int('id')
        try {
            if (pageService.putPageInTrash(id, params.at2_reply, params.at1_reply)) {
                render([status: "success", message: "Page has been successfully removed"] as JSON)
            } else {
                render([status: "error", message: "Page could no be removed" ] as JSON)
            }
        } catch (AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    @Restriction(permission = "navigation.edit.items", params_not_exist = "cache", entity_param = "navigationId", domain = Navigation)
    def createNavigation() {
        NavigationItem navigationItem
        if (params.cache) {
            Map data = JSON.parse(params.cache);
            data.id = null;
            if (data.parent && !null.equals(data.parent)) {
                data.parent = NavigationItem.get(data.parent.toInteger());
            } else {
                data.parent = null;
            }
            navigationItem = new NavigationItem(data)
            params.parent = null;
        } else if (params.id) {
            navigationItem = NavigationItem.get(params.int("id") ?: 0)
        } else {
            navigationItem = new NavigationItem()
        }
        if (params.parent) {
            navigationItem.parent = NavigationItem.get(params.int("parent") ?: 0)
        }
        def parents = params.parents ? JSON.parse(params.parents) : [:]
        render view: "/frontEndEditor/createNavigationItem", model: [navigationItem: navigationItem, parents: parents, params: params]
    }

    def saveNavigation() {
        def navigationInstance = navigationService.save(params)
        if (navigationInstance) {
            render([status: "success", message: g.message(code: "navigation.save.success"), instance: navigationInstance] as JSON)
        } else {
            render([status: "error", message: g.message(code: "navigation.save.failed"), instance: null] as JSON)
        }
    }

    def saveNavigationWidget() {
        Widget widget = frontEndEditorService.saveNavigationWidget(params)
        if (widget) {
            def widgetContent = widgetService.renderWidget(widget.widgetType.capitalize(), widget);
            render([status: "success", html: widgetContent, serialized: widget.serialize()] as JSON);
            return;
        }
        render([status: "error"] as JSON);
    }

    def saveNavigationItem() {
        if (params.updatedJSON || params.removedItems) {
            Long navigationId = params.long("navigationId") ?: 0;
            List<Map> updatedItems = JSON.parse(params.updatedJSON);
            List removedItems = params.list("removedItems").collect { it.toLong() };
            navigationService.saveItems(updatedItems, removedItems, navigationId)
            saveNavigationWidget();
        }
    }

    def loadReferenceSelectorBasedOnType() {
        String type = params.type;
        def items;
        if (type && type != DomainConstants.NAVIGATION_ITEM_TYPE.EMAIL && type != DomainConstants.NAVIGATION_ITEM_TYPE.URL && type != DomainConstants.NAVIGATION_ITEM_TYPE.AUTO_GENERATED_PAGE) {
            def consumer = Holders.applicationContext.getBean(NavigationService.domains[type]);
            String domain = StringUtil.getCapitalizedAndPluralName(type)
            Map filerMap = NavigationService.domains_filer_params[type] ?: [:]
            items = consumer.getClass().getDeclaredMethod("get" + domain, Map as Class[]).invoke(consumer, [filerMap] as Object[])
        }else {
            items = [];
        }

        render(view: "/frontEndEditor/referenceSelector", model: [type: type, items: items, ref: params.ref]);
    }

    def uploadFileAndGetContent() {
        Map content = frontEndEditorService.uploadFileAndGetContent(params, request)
        render(content as JSON)
    }

    def addContentPopup() {
        render(view: "/frontEndEditor/addWidgetPopup", model: [:])
    }

    def editStoreLogo(){
        StoreDetail storeDetail = StoreDetail.first() ?: new StoreDetail()
        MultipartFile uploadedFile = params.file
        Integer status = HttpStatus.SC_NOT_ACCEPTABLE
        Map responses = null
        if(uploadedFile.empty) {
            responses = [status: 'error', message:"File cannot be empty"]
        } else if(uploadedFile.size > 51200) {
            responses = [status: 'error', message: "File must be less than 50KB"]
        } else if(!uploadedFile.contentType.startsWith('image')) {
            responses = [status: 'error', message:"Incompatible file type"]
        } else {
            if(configService.saveStoreLogo(uploadedFile, storeDetail)) {
                responses = [url: appResource.getStoreLogoURL(storeDetails:storeDetail) ]
                status = HttpStatus.SC_OK
            } else{
                responses = [status: 'error', message:"Could not save store logo"]
            }
        }
        response.setStatus(status)
        render(responses as JSON)
    }

    def setPageAsLanding() {
        String url = (Page.get(params.long("id"))).url
        def config = [
                [type: DomainConstants.SITE_CONFIG_TYPES.GENERAL,
                 configKey: "landing_page",
                 value: url]
        ]
        if (configService.update(config)) {
            render([status: "success", message: g.message(code: "page.set.as.landing")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "page.not.set.as.landing")] as JSON)
        }
    }

    def modifyImage() {
        Map responses = null
        Integer status = HttpStatus.SC_NOT_ACCEPTABLE
        def fileName = params.name
        String sourceImageUrl = params.sourceImageUrl;
        String uuid = params.widgetUUId ?: StringUtil.uuid

        String relativePath = appResource.getWidgetTempRelativePath(type: 'image', uuid: uuid)
        String resourcePath = appResource.getImageWidgetTempResourceURL(widgetTempRelativePath: relativePath)
        String filePath = PathManager.getRoot(resourcePath)


        def content = sourceImageUrl.toURL().getBytes();
        File f = new File(filePath);
        if (!f.exists()) {
            f.mkdirs();
        }

        String originalFilePath = "${filePath}/${fileName}";

        File file = new File(originalFilePath);
        file.setBytes(content)

        status = HttpStatus.SC_OK
        responses = [url: resourcePath + fileName , name: fileName, status: status]

        render(responses as JSON)
    }
}