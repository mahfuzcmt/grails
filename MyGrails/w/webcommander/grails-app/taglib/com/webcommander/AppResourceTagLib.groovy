package com.webcommander

import com.webcommander.common.FileService
import com.webcommander.common.ImageService
import com.webcommander.constants.DomainConstants
import com.webcommander.content.NavigationItem
import com.webcommander.listener.SessionManager
import com.webcommander.manager.CloudStorageManager
import com.webcommander.manager.HookManager
import com.webcommander.manager.PathManager
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.util.StringUtil
import com.webcommander.webcommerce.Category
import com.webcommander.webcommerce.Product

class AppResourceTagLib {

    static namespace = "appResource"

    static final String RESOURCES = "resources"
    static final String RESTRICTED_RESOURCES = "system-resources"
    static final String MODIFIABLE_RESOURCES = "modifiable-resources"
    static final String WEB_INF = "WEB-INF"
    static final String WEB_INF_MODIFIABLE_RESOURCES = "${WEB_INF}/${MODIFIABLE_RESOURCES}"
    static final String PUB = "pub"
    static final String PRODUCT_FILE = "product-file"
    static final String EDITOR_UPLOADED_IMAGE = "editor-uploaded-image"
    static final String EDITOR_UPLOADED_FILE = "editor-uploaded-file"
    static final String FRONT_END_EDITOR_UPLOADED_IMAGE = "fee-uploaded-image"
    static final String EMAIL_TEMPLATES = "email-templates"
    static final String TEMPLATE = "template"
    static final String STORE = "store"
    static final String STORE_LOGO = "store-logo"
    static final String SIMPLIFIED_EVENT = "simplified-event"
    static final String PRODUCT = "product"
    static final String CATEGORY = "category"
    static final String NAVIGATION_ITEM = "navigation-item"
    static final String PRODUCT_SPEC = "spec"
    static final String IMAGES = "images"
    static final String SOCIAL_MEDIA_ICONS = "social-media-icons"
    static final String PNG = "png"
    static final String SOCIAL_MEDIA_LIKE = "-like"
    static final String DEFAULT = "default"
    static final String DEFAULT_PNG = "default.png"
    static final String ALBUMS = "albums"
    static final String BRAND = "brand"
    static final String MANUFACTURER = "manufacturer"
    static final String SEO_UPLOAD = "seo-upload"
    static final String SITEMAP_RESOURCE_URI = "seo-upload/sitemap.xml"
    static final String DEFAULT_HTML = "default.html"
    static final String DEFAULT_TXT = "default.txt"
    static final String VIDEO_THUMB = "video-thumb"
    static final String TEMP = "temp"
    static final String DOCUMENT = "document"
    static final String IMAGE_WIDGET = "image-widget"
    static final String LARGE_JPG = "large.jpg"
    static final String FORM_SUBMISSIONS = "form-submissions"
    static final String SUBMISSION = "submission"
    static final String ADMIN= "admin"
    static final String FAVICON = "favicon"
    static final String SITE_FAVICON_ICO = "site.ico"
    static final String THUMB_DEFAULT_PNG = "thumb-default.png"
    static final String CSS = "css"
    static final String COLORS = "colors"
    static final String VARIATION = "variation"
    static final String PAYMENT_GATEWAY = "payment-gateway"
    static final String CREDIT_CARD = "CRD"
    static final String AFTER_PAY = "APY"
    static final String CUSTOM_LOGO_PNG = "custom_logo.png"



    FileService fileService


    static String getDefaultImageRelativePath(String prefix){
        return "${DEFAULT}/" + getImagePrefix(prefix) + "${DEFAULT_PNG}"
    }

    public String getDefaultImageWithPrefix(String prefix, String entity) {
        return "${app.relativeBaseUrl()}${RESOURCES}/${TenantContext.currentTenant}/${entity}/" + getDefaultImageRelativePath(prefix)
    }

    public String getResourcePath(String baseUrl, String tenantId = TenantContext.currentTenant, String extension = "") {
        return "${baseUrl}${RESOURCES}/${tenantId}${extension}"
    }

    static String getImagePrefix(String prefix) {
        prefix = prefix ? prefix + "-" : ""
        return prefix
    }

    public String concatImagePrefix(String name, String prefix, String url = "") {
        return url + getImagePrefix(prefix) + name
    }

    public String getAbstractStaticResourceImageURL(def image, String sizeOrPrefix = "") {
        return concatImagePrefix(image.getResourceName(), sizeOrPrefix, getResourcePath(image.getBaseUrl(), image.getTenantId(), image.getRelativeUrl()))
    }


    public String getProductDefaultImageWithPrefix(String prefix) {
        return getDefaultImageWithPrefix(prefix, PRODUCT)
    }

    public String getCategoryDefaultImageWithPrefix(String prefix) {
        return getDefaultImageWithPrefix(prefix, CATEGORY)
    }

    public String getProductRelativeUrl(def productId) {
        return "${PRODUCT}/product-${productId}/"
    }

    public String getSimplifiedEventRelativeUrl(def eventId) {
        return eventId ? "${SIMPLIFIED_EVENT}/event-${eventId}/" : "${SIMPLIFIED_EVENT}/"
    }

    public String getProductSpecRelativeUrl(def productId, String fileName = "") {
        return "${getProductRelativeUrl(productId)}${PRODUCT_SPEC}/${fileName}"
    }

    public String generateProductImageUrl(def image, String imageSize = null) {
        imageSize = imageSize == null ? "" : imageSize + "-"
        return image.baseUrl + image.findUrlInfix() + imageSize + image.getResourceName()
    }

    public String getProductImageInfix(def tenantId, def productId) {
        return "${RESOURCES}/${tenantId}${getProductRelativeUrl(productId)}"
    }

    public String getProductSpecInfix(def tenantId, def productId) {
        return "${RESOURCES}/${tenantId}${getProductSpecRelativeUrl(productId)}"
    }


    def getProductImageURL = { attrs, body ->
        def image = attrs.image
        String size = attrs.size
        String url = generateProductImageUrl(image, size)
        out << url
    }

    def getProductDefaultImageURL = { attrs, body ->
        String imageSize = attrs.imageSize?:""
        out << getProductDefaultImageWithPrefix(imageSize)
    }

    def getProductImagesFullUrl = { attrs, body ->
        def image = attrs.image
        String size = attrs.size
        String url = generateProductImageUrl(image, size)
        out << (url.startsWith("" + app.relativeBaseUrl()) ? app.siteBaseUrl() + url : url)
    }

    def getProductImageFullUrl = { attrs, body ->
        def product = attrs.product
        String imageSize = attrs.imageSize
        String fullUrl = ""
        if (product instanceof Product) {
            fullUrl = getAdminPanelProductImageURL(product: product, imageSize: imageSize)
        } else {
            fullUrl = getSiteProductImageURL(productData: product, imageSize: imageSize)
        }
        fullUrl = fullUrl.startsWith("" + app.relativeBaseUrl()) ? concatURL("" + app.siteBaseUrl(), fullUrl) : fullUrl
        out << fullUrl
    }

    def getSiteProductImageURL = { attrs, body ->
        def product = attrs.productData
        String imageSize = attrs.imageSize
        String url = product.image ? (product.images[0].baseUrl + product.images[0].urlInfix + getImagePrefix(imageSize) + product.image) : getProductDefaultImageWithPrefix(imageSize)
        out << url
    }

    def getAdminPanelProductImageURL = { attrs, body ->
        def product = attrs.product
        String imageSize = attrs.imageSize
        String url = product.images ? generateProductImageUrl(product.images[0], imageSize) : getProductDefaultImageWithPrefix(imageSize)
        out << url
    }

    def getPaymentGatewayCardRelativePath = { attrs, body ->
        out << "${PAYMENT_GATEWAY}/${attrs.card}/"
    }

    public String getProductVideoRelativeUrl(def productId) {
        return getProductRelativeUrl(productId)
    }

    public String generateProductVideoUrl(def video, String thumb = null) {
        thumb = thumb ? thumb + "/" : ""
        return video.getBaseUrl() + video.findUrlInfix() + thumb + video.getResourceName(thumb)
    }

    public String getProductVideoInfix(def tenantId, def productId) {
        return getProductImageInfix(tenantId, productId)
    }

    def getProductVideoURL = { attrs, body ->
        def video = attrs.video
        String thumb = attrs.thumb ?: ""
        String url = generateProductVideoUrl(video, thumb)
        out << url
    }


    public String getAlbumRelativeUrl(def dbId) {
        return "${ALBUMS}/album-${dbId}/"
    }

    def getAlbumImageURL = { attrs, body ->
        def image = attrs.image
        String sizeOrPrefix = attrs.sizeOrPrefix
        String url = getAbstractStaticResourceImageURL(image, sizeOrPrefix)
        out << url
    }

    public String getStoreRelativeUrl() {
        return "${STORE}/"
    }


    String concatURL(String prefix, String postfix){
        if (prefix != null && prefix.endsWith("/")){
            prefix =  prefix.substring(0, prefix.lastIndexOf("/"))
        }

        if (postfix != null && postfix.startsWith("/")){
            return prefix + postfix
        }else{
            return prefix + "/" + postfix
        }
    }

    def getStoreLogoURL = { attrs, body ->
        def storeDetails = attrs.storeDetails
        String sizeOrPrefix = attrs.sizeOrPrefix
        Boolean isDefault = attrs.isDefault
        Boolean fullURL = attrs.isFullUrl?:false
        String defaultImage = isDefault ? getDefaultImageWithPrefix("", STORE) : ""
        String url = storeDetails.image ? getAbstractStaticResourceImageURL(storeDetails, sizeOrPrefix) : defaultImage
        if(fullURL && !CloudStorageManager.isCloudEnable()){
            String baseUrl = app.siteBaseUrl()
           out << concatURL(baseUrl, url)
        }else{
            out << url
        }
    }

    static String getFaviconRelativePath(){
        return "${FAVICON}/${SITE_FAVICON_ICO}"
    }

    def getResourceURL(){
        if (CloudStorageManager.isCloudEnable()){
            return "${getResourceBaseURL()}${RESOURCES}/"
        }else{
            return "${getResourceBaseURL()}${RESOURCES}/${TenantContext.currentTenant}/"
        }
    }

    def getFaviconURL = { attrs, body ->
        Boolean isEnable = attrs.isEnable
        String favicon = "${getResourceURL()}${getFaviconRelativePath()}?uuid=${StringUtil.uuid}"
        String url = isEnable ? favicon : getDefaultImageWithPrefix("", STORE)
        out << url
    }

    def getSnippetThumbDefaultImage = { attrs, body ->
        String url = "${app.systemResourceBaseUrl()}${IMAGES}/${ADMIN}/${THUMB_DEFAULT_PNG}"
        out << url
    }

    public String getBrandRelativeUrl(def dbId) {
        return "${BRAND}/brand-${dbId}/"
    }

    def getBrandImageURL = { attrs, body ->
        def brand = attrs.image
        String sizeOrPrefix = attrs.sizeOrPrefix ?: ""
        String url = brand.image ? getAbstractStaticResourceImageURL(brand, sizeOrPrefix) : getDefaultImageWithPrefix(sizeOrPrefix, BRAND)
        out << url
    }

    public String getManufacturerRelativeUrl(def dbId) {
        return "${MANUFACTURER}/manufacturer-${dbId}/"
    }

    def getManufacturerImageURL = { attrs, body ->
        def manufacturer = attrs.image
        String sizeOrPrefix = attrs.sizeOrPrefix ?: ""
        String url = manufacturer.image ? getAbstractStaticResourceImageURL(manufacturer, sizeOrPrefix) : getDefaultImageWithPrefix(sizeOrPrefix, MANUFACTURER)
        out << url
    }

    String getResourceRelativeUrl(String resourceNamePrefix) {
        return "${RESOURCES}/${getResourceRelativePath(resourceNamePrefix)}"
    }

    def getPubUrl = { attrs, body ->
        String pubUrl = "${getResourceBaseURL()}" + (CloudStorageManager.isCloudEnable() ? "${PUB}" : "${PUB}/${TenantContext.currentTenant}")
        out << pubUrl
    }

    def getSystemPubUrl = { attrs, body ->
        String pubUrl = "${app.relativeBaseUrl()}" + "${PUB}/${TenantContext.currentTenant}"
        out << pubUrl
    }

    String getTemplateLocalRelativePath(){
        return "${TEMPLATE}/${TenantContext.currentTenant}/"
    }

    String getImageWidgetDefaultUrl() {
        return "images/widgets/default/image.jpg"
    }
    def getFormSubmissionFilePath = { attrs, body ->
       out << "${RESOURCES}/${TenantContext.currentTenant}/${FORM_SUBMISSIONS}/${SUBMISSION}-${attrs.id}${File.separator}${attrs.fileName}"
    }
    def getWidgetTempRelativePath = { attrs, body ->
        out << SessionManager.getRelativeTempPath() + "/${attrs.type}-widget/${attrs.uuid}/"
    }

    def getImageWidgetTempResourceURL = { attrs, body ->
        if (CloudStorageManager.isCloudEnable()){
            out << "${getResourceBaseURL()}${RESOURCES}/"
        }else{
            out << "${getResourceBaseURL()}${RESOURCES}/${TenantContext.currentTenant}/${attrs.widgetTempRelativePath}"
        }
    }

    def getWidgetTempPath = { attrs, body ->
        out << getResourcePath("" + app.relativeBaseUrl()) + getWidgetTempRelativePath(attrs)
    }
     def getDocumentPath = { attrs, body ->
        out << getResourcePath("" + app.relativeBaseUrl()) + getDocumentRelativePath(attrs)
    }

    def  getDocumentRelativePath = { attrs, body ->
        out << "/${DOCUMENT}/${DEFAULT}/"+ attrs.name ?: ""
    }



    def getWidgetRelativeUrl = { attrs, body ->
        out << getResourcePath("", TenantContext.currentTenant, "/${attrs.type}-widget/${attrs.uuid}/")
    }

    def getWidgetCloudRelativeUrl = { attrs, body ->
        out << getResourcePath("", "", "${attrs.type}-widget/${attrs.uuid}/")
    }

    def getSeoUplodRoot = { attrs, body ->
        out << PathManager.getResourceRoot("seo-upload")
    }

    def getCustomRestrictedResourcePath = { attrs, body ->
        String relativePath = attrs.relativePath
        out << PathManager.getCustomRestrictedResourceRoot(relativePath)
    }

    def getCloudCustomRestrictedResourceUrl = { attrs, body ->
        String relativePath = attrs.relativePath
        out << "/${MODIFIABLE_RESOURCES}/${relativePath}"
    }

    def getDownloadableProductTypeFileUrl = { attrs, body ->
        String productId = attrs.productId
        String productFileName = attrs.productFileName
        if(attrs.isVariationFile){
            out << "/${VARIATION}/${PRODUCT_FILE}/${PRODUCT}-${productId}/${productFileName}"
        }
        else {
            out << "/${PRODUCT_FILE}/${PRODUCT}-${productId}/${productFileName}"
        }
    }

    def getRestrictedResourcePath = { attrs, body ->
        String relativePath = attrs.relativePath
        out << PathManager.getRestrictedResourceRoot(relativePath)
    }

    def getCloudRestrictedResourceUrl = { attrs, body ->
        String relativePath = attrs.relativePath
        out << "/${RESTRICTED_RESOURCES}/${relativePath}"
    }

    def getDefaultImageURL = { attrs, body ->
        String entity = attrs.entity ?: "store"
        String prefix = attrs.prefix ?: (ImageService.RESIZABLE_IMAGE_SIZES[DomainConstants.DEFAULT_IMAGES[attrs.entity]]?.keySet()?.first() ?: "")
        out << getDefaultImageWithPrefix(prefix, entity)
    }

    def getResourceBaseURL = { attrs, body ->
        if (CloudStorageManager.isCloudEnable()){
            out << CloudStorageManager.getCloudBaseURL()
        }else{
            out << app.relativeBaseUrl()
        }
    }


    def getEditorUploadedImageUrl = { attrs, body ->
        String resourceName = attrs.resourceName?:""
        out << "${getPubUrl()}/${EDITOR_UPLOADED_IMAGE}/${resourceName}"
    }

    def getEditorUploadedFileUrl = { attrs, body ->
        String resourceName = attrs.resourceName?:""
        out << "${getPubUrl()}/${EDITOR_UPLOADED_FILE}/${resourceName}"
    }

    def getFrontEndEditorUploadedImageUrl = { attrs, body ->
        String resourceName = attrs.resourceName ?: ""
        out << "${getPubUrl()}/${FRONT_END_EDITOR_UPLOADED_IMAGE}/${resourceName}"
    }

    def getProductSpecFileURL = { attrs, body ->
        def productData = attrs.productData
        productData = HookManager.hook("product-data-adjustment", productData, params)
        String url = "${productData.specUrl}${productData.specUrlInfix}${productData.spec}"
        out << url
    }

    def getSocialMediaIconURL = { attrs, body ->
        def profileName = attrs.profileName
        def type = attrs.type ?: ""
        String url = "${app.systemResourceBaseUrl()}${IMAGES}/${SOCIAL_MEDIA_ICONS}/${profileName}${type}.${PNG}"
        out << url
    }

    def getSocialMediaLikeIconURL = { attrs, body ->
        out << getSocialMediaIconURL(profileName: attrs.profileName, type: SOCIAL_MEDIA_LIKE)
    }

    def getRootPhysicalPath = { attrs, body ->
        String extension = attrs.extension ?: ""
        out << PathManager.getRoot(extension)
    }

    def getResourcePhysicalPath = { attrs, body ->
        String extension = attrs.extension ? attrs.extension + "/" : ""
        out << "${getRootPhysicalPath(extension: "${RESOURCES}/${TenantContext.currentTenant}")}/${extension}"
    }

    def getWebInfPhysicalPath = { attrs, body ->
        String extension = attrs.extension ? attrs.extension + "/" : ""
        out << "${getRootPhysicalPath(extension: "${WEB_INF}/${TenantContext.currentTenant}")}/${extension}"
    }

    def getTempPhysicalPath = { attrs, body ->
        String extension = attrs.extension ? attrs.extension + "/" : ""
        out << "${getRootPhysicalPath(extension: "${WEB_INF}/${TEMP}/${TenantContext.currentTenant}")}/${extension}"
    }

    String getCategoryRelativeUrl(def categoryId) {
        return "${CATEGORY}/${CATEGORY}-${categoryId}/"
    }

    String getNavigationItemRelativeUrl(def navigationId) {
        return "${NAVIGATION_ITEM}/${NAVIGATION_ITEM}-${navigationId}/"
    }

    public String getImageWidgetRelativeUrl(def uuid) {
        return "${IMAGE_WIDGET}/${uuid}/"
    }

    def getCategoryImageURL = { attrs, body ->
        Category category = attrs.category
        String imageSize = attrs.imageSize
        String url = category.image ? "${category.baseUrl}${RESOURCES}/${category.getTenantId()}${category.getRelativeUrl()}${getImagePrefix(imageSize)}${category.getResourceName()}" : getCategoryDefaultImageWithPrefix(imageSize)
        out << url
    }

    def getNavigationItemImageURL = { attrs, body ->
        NavigationItem navigationItem = attrs.navigationItem
        String imageSize = attrs.imageSize
        String url = navigationItem.image ? "${navigationItem.baseUrl}${RESOURCES}/${navigationItem.getTenantId()}${navigationItem.getRelativeUrl()}${getImagePrefix(imageSize)}${navigationItem.getResourceName()}" : getCategoryDefaultImageWithPrefix(imageSize)
        out << url
    }

    def getTemplateLeftPanelImageURL = { attrs, body ->
        String url = "${getResourceBaseURL()}${getTemplateLocalRelativePath()}/${IMAGES}/${LARGE_JPG}"
        out << url
    }
    def isExistTemplateLeftPanelImage = { attrs, body ->
        String url = getRootPhysicalPath(extension: "${app.relativeBaseUrl()}${getTemplateLocalRelativePath()}/${IMAGES}/${LARGE_JPG}")
        boolean isExist =  new File(url).exists()
        out << isExist
    }


    String jsLinkGenerator(String src) {
        String link = "<script type='text/javascript' src='"
        link += src
        link += "'></script>"
        return link
    }

    def frontEndConfigJS = { attrs, body ->
        boolean isExist =  fileService.isExistTemplateResource("js/config.js")
        if (isExist){
            String link = "${getResourceBaseURL()}${TEMPLATE}/"
            if (!CloudStorageManager.isCloudEnable()){
                link += TenantContext.currentTenant + "/"
            }
            out << jsLinkGenerator("${link}js/config.js?id=${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "template_uuid")}")
        }
    }


    String getTemplateColorAbsulatePath(){
        return  getRootPhysicalPath(extension: "${app.relativeBaseUrl()}${getTemplateLocalRelativePath()}${CSS}/${COLORS}")
    }

    def getTemplateCssRelativePath = { attrs, body ->
        out << "${app.relativeBaseUrl()}${getTemplateLocalRelativePath()}${CSS}"
    }

    def getProductResourceRelativePath = { attrs, body ->
        out << "${getResourcePath(app.relativeBaseUrl().toString(), TenantContext.currentTenant, "/${getProductRelativeUrl(attrs.productId)}")}"
    }

    def getSimplifiedEventRelativePath = { attrs, body ->
        out << "${getResourcePath(app.relativeBaseUrl().toString(), TenantContext.currentTenant, "/${getSimplifiedEventRelativeUrl(attrs.eventId)}")}"
    }

    def getPaymentGatewayCardLogoPath = { attrs, body ->
        out << "${app.customResourceBaseUrl()}${RESOURCES}/${TenantContext.currentTenant}/${PAYMENT_GATEWAY}/${attrs.cardType}/${CUSTOM_LOGO_PNG}/"
    }
}
