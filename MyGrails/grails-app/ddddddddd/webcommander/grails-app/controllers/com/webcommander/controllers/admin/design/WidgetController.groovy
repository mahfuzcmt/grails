package com.webcommander.controllers.admin.design

import com.webcommander.AppResourceTagLib
import com.webcommander.ApplicationTagLib
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.common.ImageService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.Galleries
import com.webcommander.constants.NamedConstants
import com.webcommander.content.ContentService
import com.webcommander.content.NavigationService
import com.webcommander.design.Resolution
import com.webcommander.design.WidgetService
import com.webcommander.events.AppEventManager
import com.webcommander.manager.CloudStorageManager
import com.webcommander.models.MockStaticResource
import com.webcommander.util.AppUtil
import com.webcommander.util.FileUtil
import com.webcommander.webcommerce.CategoryService
import com.webcommander.webcommerce.ProductService
import com.webcommander.widget.Widget
import grails.converters.JSON
import grails.util.Holders
import org.springframework.web.multipart.MultipartFile

class WidgetController {

    WidgetService widgetService
    ProductService productService
    ContentService contentService
    ImageService imageService
    CategoryService categoryService
    NavigationService navigationService

    def bareShortConfig() {
        render(view: "/admin/widget/shortConfig/bare", model: [noAdvance: true])
    }

    @Restriction(permission = "article.view.list")
    def editArticle() {
        def articles = contentService.getArticlesInOrder(params.widget.widgetContent.contentId)
        render(view: "/admin/widget/loadArticle", model: [articles: articles])
    }

    def articleShortConfig() {
        render(view: "/admin/widget/shortConfig/loadArticle", model: [advanceText: g.message(code: 'select.article')])
    }

    def editCart() {
        render(view: "/admin/widget/loadCart", model: [:]);
    }

    def cartShortConfig() {
        List resolutions = Resolution.getResolutionForSelection()
        render(view: "/admin/widget/shortConfig/loadCart", model: [noAdvance: true, resolutions: resolutions])
    }

    @Restriction(permission = "category.view.list")
    def editCategory() {
        render(view: "/admin/widget/loadCategory", model: [categories: categoryService.getCategoriesInOrder(params.widget.widgetContent.contentId)]);
    }

    def categoryShortConfig() {
        render(view: "/admin/widget/shortConfig/loadCategory", model: [advanceText: g.message(code: 'select.category')])
    }

    def editCurrency() {
        render(view: "/admin/widget/loadCurrency", model: [:])
    }

    def currencyShortConfig() {
        render(view: "/admin/widget/shortConfig/loadCurrency", model: [noAdvance: true])
    }

    def editGallery() {
        if (!params.config.gallery) {
            params.config.gallery = NamedConstants.GALLERY_TYPES.findResult {
                return it.value
            }
        }
        params.config.galleryContentType = params.config.galleryContentType ?: DomainConstants.GALLERY_CONTENT_TYPES.ALBUM
        render(view: "/admin/widget/loadGallery", model: [albumId: params.widget.widgetContent.size() ? params.widget.widgetContent[0].contentId : null])
    }

    def galleryShortConfig() {
        render(view: "/admin/widget/shortConfig/loadGallery", model: [advanceText: g.message(code: 'configure.gallery')])
    }

    def editHtml() {
        render(view: "/admin/widget/loadHtml", model: [:])
    }

    def htmlShortConfig() {
        render(view: "/admin/widget/shortConfig/loadHtml", model: [advanceText: g.message(code: 'configure.html')])
    }

    def editImage() {
        render(view: "/admin/widget/loadImage", model: [:])
    }

    def imageShortConfig() {
        render(view: "/admin/widget/shortConfig/loadImage", model: [advanceText: g.message(code: 'select.image')])
    }

    def storeLogoShortConfig() {
        render(view: "/admin/widget/shortConfig/loadLogo", model: [noAdvance: true])
    }

    def spacerShortConfig() {
        render(view: "/admin/widget/shortConfig/loadSpacer", model: [noAdvance: true, noTitle: true])
    }

    def editLogin() {
        render(view: "/admin/widget/loadLogin", model: [:])
    }

    def loginShortConfig() {
        render(view: "/admin/widget/shortConfig/loadLogin", model: [noAdvance: true])
    }

    def editNavigation() {
        def navigations = navigationService.getNavigatiosForWidget();
        render(view: "/admin/widget/loadNavigation", model: [navigation: params.widget.widgetContent.size() ? params.widget.widgetContent[0].contentId : null, navigations: navigations]);
    }

    def navigationShortConfig() {
        def navigations = navigationService.getNavigatiosForWidget();
        List resolutions = Resolution.getResolutionForSelection()
        render(view: "/admin/widget/shortConfig/loadNavigation", model: [noAdvance: true, navigation: params.widget.widgetContent.size() ? params.widget.widgetContent[0].contentId : null, navigations: navigations, resolutions: resolutions])
    }

    def editNews() {
        render(view: "/admin/widget/loadNews", model: [:]);
    }

    def newsletterShortConfig() {
        render(view: "/admin/widget/shortConfig/loadNewsletter", model: [noAdvance: true])
    }

    @Restriction(permission = "product.view.list")
    def editProduct() {
        if (!params.widget.params) {
            params.config = ["show-pagination": "none", item_per_page: "10", price: "true", description: "true", add_to_cart: "true", "item-per-page-selection": "false", sortable: "false"];
        }
        render(view: "/admin/widget/loadProduct", model: [products: productService.getProductsInOrder(params.widget.widgetContent.contentId)]);
    }

    def productShortConfig() {
        render(view: "/admin/widget/shortConfig/loadProduct", model: [noAdvance: true, advanceText: g.message(code: 'select.product')])
    }

    def searchShortConfig() {
        List resolutions = Resolution.getResolutionForSelection()
        render(view: "/admin/widget/shortConfig/loadSearch", model: [noAdvance: true, resolutions: resolutions])
    }

    def socialMediaLinkShortConfig() {
        render(view: "/admin/widget/shortConfig/loadSocialMediaLink", model: [noAdvance: true])
    }

    def socialMediaShareShortConfig() {
        if (!params.config.socialMediaConfig) {
            params.config.socialMediaConfig = []
        }
        if (params.config.socialMediaConfig instanceof String) {
            params.config.socialMediaConfig = [params.config.socialMediaConfig]
        }
        render(view: "/admin/widget/shortConfig/loadSocialMediaShare", model: [noAdvance: true])
    }

    def socialMediaLikeShortConfig() {
        if (params.config.socialMediaConfig instanceof String) {
            params.config.socialMediaConfig = [params.config.socialMediaConfig]
        }
        render(view: "/admin/widget/shortConfig/loadSocialMediaLike", model: [noAdvance: true])
    }

    protected renderAnyWidget(type, widget) {
        if (widget) {
            def widgetContent = widgetService.renderWidget(type, widget);
            render([status: "success", html: widgetContent, serialized: widget.serialize()] as JSON);
            return;
        }
        render([status: "error"] as JSON);
    }

    public saveAnyWidget(type) {
        render(widgetService.saveAnyWidget(type, params))
    }

    def saveNewsletterWidget() {
        saveAnyWidget("Newsletter")
    }

    def saveArticleWidget() {
        saveAnyWidget("Article")
    }

    def saveHtmlWidget() {
        saveAnyWidget("Html")
    }

    def saveSpacerWidget() {
        saveAnyWidget("Spacer")
    }

    def saveCurrencyWidget() {
        saveAnyWidget("Currency");
    }

    def saveLoginWidget() {
        saveAnyWidget("Login")
    }

    def saveNavigationWidget() {
        saveAnyWidget("Navigation")
    }

    def saveProductWidget() {
        saveAnyWidget("Product")
    }

    def saveCategoryWidget() {
        saveAnyWidget("Category")
    }

    def saveCartWidget() {
        saveAnyWidget("Cart")
    }

    def saveYoutubeWidget() {
        saveAnyWidget("Youtube")
    }

    def saveImageWidget() {
        AppEventManager.off("widget-" + params.uuid + "-before-save")
        if (params.upload_type == "local") {
            MultipartFile uploadedImage = request.getFile('localImage')
            if (uploadedImage?.originalFilename) {
                String originalName = uploadedImage.originalFilename;
                String filePath = appResource.getWidgetTempRelativePath(type: 'image', uuid: params.uuid)
                MockStaticResource mockResource = new MockStaticResource(relativeUrl: filePath, resourceName: originalName)
                imageService.uploadImage(uploadedImage, NamedConstants.IMAGE_RESIZE_TYPE.IMAGE_WIDGET, mockResource);
                uploadWidgetLocalResource(params, originalName)
            }
        } else {
            AppEventManager.off("widget-" + params.uuid + "-before-save")
            AppEventManager.one("widget-" + params.uuid + "-before-save", "session-" + session.id, { widget ->
                File resource = new File(Holders.servletContext.getRealPath("${appResource.getWidgetRelativeUrl(uuid: widget.uuid, type: 'image')}"))
                if (resource.exists()) {
                    resource.deleteDir()
                }
                CloudStorageManager.deleteData("${appResource.getWidgetCloudRelativeUrl(uuid: widget.uuid, type: 'image')}", NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT)
            })
        }
        saveAnyWidget("Image")
    }

    void uploadWidgetLocalResource(Map params, String originalName) {
        def appResource = AppUtil.getBean(AppResourceTagLib)
        String relativeResourcePath = appResource.getWidgetTempPath(type: params.widgetType, uuid: params.uuid)
        params.local_url = relativeResourcePath + originalName
        AppEventManager.off("widget-" + params.uuid + "-before-save")
        AppEventManager.one("widget-" + params.uuid + "-before-save", "session-" + session.id, { widget ->
            String relativeUrl = "${appResource.getWidgetRelativeUrl(uuid: widget.uuid, type: widget.widgetType)}"
            String modifiedLocalUrl = relativeUrl + originalName
            File targetFile = new File(Holders.servletContext.getRealPath(relativeUrl))
            if (targetFile.exists()) {
                targetFile.deleteDir()
            }
            if (!targetFile.parentFile.exists()) {
                targetFile.parentFile.mkdirs()
            }
            File sourceFile = new File(Holders.servletContext.getRealPath(relativeResourcePath))
            if (sourceFile.exists()) {
                FileUtil.move(sourceFile, targetFile)
                String uploadLocation = appResource.getWidgetCloudRelativeUrl(uuid: widget.uuid, type: widget.widgetType)
                CloudStorageManager.uploadData(new File(targetFile, originalName), NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT, uploadLocation + originalName)
            }
            Map _params = JSON.parse widget.params
            widget.content = AppUtil.getBean(ApplicationTagLib).customResourceBaseUrl() + modifiedLocalUrl
            widget.params = _params;
        })
    }

    def saveContents() {
        def result = widgetService.savePageContents(params.containerType, params.long("containerId"), params.removed ? JSON.parse(params.removed) : [], params.removedDock ? JSON.parse(params.removedDock) : [], params.added_in_header ? JSON.parse(params.added_in_header) : [], params.added_in_footer ? JSON.parse(params.added_in_footer) : [], params.added_in_body ? JSON.parse(params.added_in_body) : [], params.added_in_dock ? JSON.parse(params.added_in_dock) : [], params.docks ? JSON.parse(params.docks) : [], params.modified ? JSON.parse(params.modified) : [:], params.bodyContent, params.containerCss, params.containerJs, params.section);
        if (result == null) {
            render([status: "error", message: g.message(code: "content.failure.update")] as JSON);
        } else {
            render([status: "success", message: g.message(code: "content.successful.update"), newWidgets: result.newWidgets, containerId: result.containerId, newDocks: result.newDocks] as JSON);
        }
    }

    def saveGalleryWidget() {
        saveAnyWidget("Gallery")
    }

    def saveWidget() {
        saveAnyWidget(params.widgetType.camelCase())
    }

    def loadConfigForSlider() {
        def widgetConfig = [:]
        if (params.widgetId && !params.data) {
            def widget = Widget.get(params.long("widgetId"))
            if (widget.params) {
                widgetConfig = JSON.parse(widget.params)
            }
        } else if (params.data) {
            def param = JSON.parse(params.data).params
            widgetConfig = JSON.parse(param)
        }
        render(view: Galleries.TYPES[params.gallery].config, model: [widgetConfig: widgetConfig])
    }

    def editJs() {
        String js = params.overwrite.toBoolean() ? params.js : widgetService.getJSByUuid(params.uuid);
        render(view: "/admin/widget/editJs", model: [js: js]);
    }

    def editClazz() {
        String clazz = params.overwrite.toBoolean() ? params.clazz : widgetService.getClazzByUuid(params.uuid);
        render(view: "/admin/widget/editClazz", model: [clazz: clazz]);
    }

    def widgetTypes() {
        Map widgets = [:]
        DomainConstants.WIDGET_TYPE.each {
            widgets.put("${it.value}", g.message(code: NamedConstants.WIDGET_MESSAGE_KEYS[it.value + ".title"]))
        }
        render widgets as JSON
    }

    def renderWidget() {
        renderAnyWidget(params.widget.widgetType.capitalize(), params.widget)
    }

    def copyFrontEndWidget(){
        Long id = params.long("widgetId")
        String uuid = params['uuid']
        Long containerId = params.long('containerId')
        String containerType = params['containerType']
        render([status: "success", widget: widgetService.copyFrontEndWidget(id, containerId, containerType, uuid).serialize()] as JSON)
    }
}
