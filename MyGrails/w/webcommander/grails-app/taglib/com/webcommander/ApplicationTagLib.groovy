package com.webcommander

import com.webcommander.admin.Operator
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.ResourceList
import com.webcommander.design.Layout
import com.webcommander.manager.CloudStorageManager
import com.webcommander.manager.HookManager
import com.webcommander.manager.PathManager
import com.webcommander.plugin.PluginManager
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.widget.Widget
import grails.util.Holders
import groovy.io.FileType

import javax.annotation.PostConstruct

class ApplicationTagLib {

    static namespace = "app"

    String wcVersion
    Map config

    @PostConstruct
    void init() {
        config = Holders.config
        wcVersion = config.webcommander.version.number
    }

    def message = { attrs, body ->
        if (attrs.code.startsWith("f:")) {
            out << attrs.code.substring(2)
            return
        }
        out << g.message(attrs, body)
    }

    def siteBaseUrl = { attrs, body ->
        def scheme = attrs.scheme
        out << AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "baseurl")
    }

    def baseUrl = { attrs, body ->
        if (request.IS_DUMMY) {
            out << siteBaseUrl(attrs)
            return
        }
        String scheme = request.getScheme()
        if (!attrs.scheme) {
            attrs.scheme = scheme
        }
        int port = getServerPort()
        if (attrs.scheme != scheme) {
            scheme = attrs.scheme
        }
        String hostPart = scheme + '://' + getServerName()
        if ((scheme == "http" && port == 80) || (scheme == "https" && port == 443)) {
            out << hostPart
        } else {
            out << hostPart + ":" + port + "/"
        }
        out << getContextPath() + "/"
    }

    def currentURL = { attrs, body ->
        String scheme = attrs.scheme ?: request.scheme
        String hostPart = scheme + '://' + (attrs.host ?: getServerName())
        out << hostPart
        out << request.forwardURI + (request.queryString ? "?" + request.queryString : "")
    }

    def schemedBaseUrl = { attrs, body ->
        if (request.IS_DUMMY) {
            out << siteBaseUrl(attrs, body)
            return
        }
        attrs.scheme = request.scheme
        out << baseUrl(attrs)
    }

    def relativeBaseUrl = { attrs, body ->
        out << getContextPath() + "/"
    }

    def systemResourceBaseUrl = { attrs, body ->
        String baseUrl = CloudStorageManager.staticS3Url ?: app.relativeBaseUrl()
        out << baseUrl + PathManager.staticResourceURLRoot
    }

    def javascript = { attrs, body ->
        out << "<script type='text/javascript' src='"
        out << systemResourceBaseUrl()
        out << attrs.src
        out << "'></script>"
    }

    def stylesheet = { attrs, body ->
        out << "<link rel='stylesheet' type='text/css' href='"
        out << systemResourceBaseUrl()
        out << attrs.href
        out << "'>"
    }

    def customResourceBaseUrl = {
        String scheme = request.scheme
        Map customResourceUrl = [
                scheme: scheme,
                url   : app.relativeBaseUrl()
        ]
        customResourceUrl = HookManager.hook("customResourceUrl", customResourceUrl)
        out << customResourceUrl.url
    }


    def allTestJS = { attrs, body ->
        String jsPath = PathManager.getSystemResourceRoot("js/test/cases")
        File file = new File(jsPath)
        int cutLength = file.toURI().path.length()
        file.traverse { _file ->
            if (!_file.directory) {
                out << app.javascript(src: "js/test/cases/" + _file.toURI().path.substring(cutLength))
            }
        }
    }

    private List<String> getAllPluginJsOfCategory(String category) {
        def files = []
        PluginManager.activePlugins.each { plugin ->
            String jsPath = PathManager.getSystemResourceRoot("plugins/$plugin.identifier/js/$category")
            File file = new File(jsPath)
            int cutLength = file.toURI().path.length()
            if (file.exists()) {
                file.traverse([type: FileType.FILES]) { _file ->
                    files.add("plugins/$plugin.identifier/js/$category/" + _file.toURI().path.substring(cutLength))
                }
            }
        }
        files.sort { a, b ->
            a <=> b
        }
    }

    def allPluginWidgetJSs = { attrs, body ->
        getAllPluginJsOfCategory("app-widgets").each { _path ->
            out << app.javascript(src: _path)
        }
    }

    def allPluginFeatureJSs = { attrs, body ->
        getAllPluginJsOfCategory("features").each { _path ->
            out << app.javascript(src: _path)
        }
    }

    def allPluginEditorsJSs = { attrs, body ->
        getAllPluginJsOfCategory("editors").each { _path ->
            out << app.javascript(src: _path)
        }
    }

    def activeTemplateCSSs = { attrs, body ->
        String color, uuid = ""
        String cssRelativePath = appResource.getTemplateCssRelativePath()
        String templateCssAbsulatePath = appResource.getRootPhysicalPath(extension: cssRelativePath)
        if (new File(templateCssAbsulatePath).exists()) {
            color = session.template_demo ? session.template_demo_color : AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "template_color")
            uuid = pageScope.templateUUID
        }
        out << "<link id='template-base' rel='stylesheet' type='text/css' href='$cssRelativePath/style.css?v=${wcVersion}&id=${uuid}'>"
        if (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.RESPONSIVE, "is_responsive") == "true") {
            out << "<link rel='stylesheet' type='text/css' href='$cssRelativePath/responsive.css?id=${uuid ?: ""}'>"
        } else {
            out << "<link rel='stylesheet' type='text/css' href='$cssRelativePath/non-responsive.css?id=${uuid}'>"
        }

        if (color) {
            out << "<link rel='stylesheet' type='text/css' href='$cssRelativePath/colors/${color}.css'>"
        }
        if (new File(PathManager.getSystemResourceRoot("pub/${TenantContext.currentTenant}/sitecss")).exists()) {
            out << "<link rel='stylesheet' type='text/css' href='${appResource.getSystemPubUrl()}/sitecss/stylesheet.css'>"
        }
    }

    def autoPageJS = { attrs, body ->
        if (request.isAutoPage) {
            switch (request.page.name) {
                case DomainConstants.AUTO_GENERATED_PAGES.PRODUCT_PAGE:
                    out << app.javascript(src: "js/site/product.js")
                    break
                case DomainConstants.AUTO_GENERATED_PAGES.CUSTOMER_REGISTRATION:
                case DomainConstants.AUTO_GENERATED_PAGES.CUSTOMER_LOGIN:
                case DomainConstants.AUTO_GENERATED_PAGES.CUSTOMER_RESET_PASSWORD:
                case DomainConstants.AUTO_GENERATED_PAGES.CUSTOMER_NEW_PASSWORD:
                case DomainConstants.AUTO_GENERATED_PAGES.ARTICLE_DETAILS_PAGE:
                case DomainConstants.AUTO_GENERATED_PAGES.SEARCH_RESULT:
                    break
                case DomainConstants.AUTO_GENERATED_PAGES.CART_PAGE:
                    out << app.javascript(src: "js/site/cart.js")
                    break
                case DomainConstants.AUTO_GENERATED_PAGES.CUSTOMER_PROFILE:
                    out << app.javascript(src: "js/site/customer-profile.js")
                    break
                case DomainConstants.AUTO_GENERATED_PAGES.CHECKOUT:
                    out << app.javascript(src: "js/site/checkout.js")
                    break
                default:
                    PluginManager.activePlugins.each { plugin ->
                        plugin.autoPageJS.each { name, js ->
                            if (request.page.name.sanitize() == name) {
                                out << app.javascript(src: js)
                            }
                        }
                    }
                    break
            }
            List scripts = []
            HookManager.hook("auto-page-js", scripts, request.page.name)
            scripts.each { src ->
                out << app.javascript(src: src)
            }
            if (request.autopage_js) {
                request.autopage_js.each { js ->
                    out << app.javascript(src: js)
                }
            }
        }
    }

    def autoPageCSS = { attrs, body ->
        if (request.isAutoPage) {
            switch (request.page.name) {
                case DomainConstants.AUTO_GENERATED_PAGES.CUSTOMER_REGISTRATION:
                case DomainConstants.AUTO_GENERATED_PAGES.CUSTOMER_LOGIN:
                case DomainConstants.AUTO_GENERATED_PAGES.CUSTOMER_RESET_PASSWORD:
                case DomainConstants.AUTO_GENERATED_PAGES.CUSTOMER_NEW_PASSWORD:
                case DomainConstants.AUTO_GENERATED_PAGES.CART_PAGE:
                case DomainConstants.AUTO_GENERATED_PAGES.ARTICLE_DETAILS_PAGE:
                case DomainConstants.AUTO_GENERATED_PAGES.SEARCH_RESULT:
                case DomainConstants.AUTO_GENERATED_PAGES.CHECKOUT:
                case DomainConstants.AUTO_GENERATED_PAGES.CUSTOMER_PROFILE:
                    break
                case DomainConstants.AUTO_GENERATED_PAGES.PRODUCT_PAGE:
                    out << app.stylesheet(href: "css/auto_page/product.css")
                    break
                default:
                    PluginManager.activePlugins.each { plugin ->
                        plugin.autoPageCSS.each { name, css ->
                            if (request.page.name.sanitize() == name) {
                                out << app.stylesheet(href: css)
                            }
                        }
                    }
                    break
            }
            if (request.autopage_css) {
                request.autopage_css.each { css ->
                    out << app.stylesheet(href: css)
                }
            }
        }
    }

    def getContextPath = { attrs, body ->
        String contextPath = Holders.servletContext.contextPath
        out << (contextPath == "/" ? "" : contextPath)
    }

    private int getServerPort(String scheme) {
        if(request && !request.IS_DUMMY && (!scheme || scheme == request.scheme)) {
            return request.serverPort
        }
        String baseurl = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "baseurl")
        URL url = new URL(baseurl)
        return url.getPort()
    }

    private String getServerName() {
        if(request && !request.IS_DUMMY) {
            return request.serverName
        }
        String baseurl = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "baseurl")
        baseurl = baseurl.substring(7)
        return baseurl.split(":")[0]
    }

    def embeddableCss = { attr, body ->
        def productImageConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE)
        def categoryImageConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CATEGORY_IMAGE)
        out << """<style>
            .list-view .product-view-height-width img {
                 max-width: ${productImageConfig.listview_width}px;
                 max-height: ${productImageConfig.listview_height}px;
            }
            .image-view .product-view-height-width,
            .scrollable-view .product-view-height-width {
                width: ${productImageConfig.gridview_width}px;
            }
            .image-view .product-view-height-width .image,
            .scrollable-view .product-view-height-width .image {
                height: ${productImageConfig.gridview_height}px;
            }  
            .list-view .product-view-height-width {
                height: ${productImageConfig.listview_height}px;
            }
            .list-view .product-view-height-width .image {
                width: ${productImageConfig.listview_width}px;
            }
            .product-view.scrollable-view img,
            .product-view.image-view img {
                max-width: ${productImageConfig.gridview_width}px;
                max-height: ${productImageConfig.gridview_height}px;
            }
            .product-thumb-height {
                height: ${productImageConfig.thumbnail_height}px;
            }
            .product-thumb-width {
                width: ${productImageConfig.thumbnail_width}px;
            }
            .product-thumb-view img {
                max-width: ${productImageConfig.thumbnail_width}px;
                max-height: ${productImageConfig.thumbnail_height}px;
            }
            .product-detail-height {
                height: ${productImageConfig.details_height}px;
            }
            .product-detail-width {
                width: ${productImageConfig.details_width}px;
            }
            .product-detail-view img {
                max-width: ${productImageConfig.details_width}px;
                max-height: ${productImageConfig.details_height}px;
            }
            .category-image-view-height {
                height: ${categoryImageConfig.gridview_height}px;
            }
            .category-image-view-width {
                width: ${categoryImageConfig.gridview_width}px;
            }
            .category-image-view img {
                max-width: ${categoryImageConfig.gridview_width}px;
                max-height: ${categoryImageConfig.gridview_height}px;
            }
            .category-detail-height {
                height: ${categoryImageConfig.details_height}px;
            }
            .category-detail-width {
                width: ${categoryImageConfig.details_width}px;
            }
            .category-detail-view img {
                max-width: ${categoryImageConfig.details_width}px;
                max-height: ${categoryImageConfig.details_height}px;
            }
         </style>"""
        if (!productImageConfig.popup_use_original.toBoolean()) {
            out << """<style>
                img.popup-image {
                    max-width: ${productImageConfig.popup_width}px;
                    max-height: ${productImageConfig.popup_height}px;
                }
            </style>"""
        }
        PluginManager.hookTag("embeddableCss", attr, body, out)
    }

    def editorStyleSheets = { attr, body ->
        Boolean isFrontEndEditor = pageScope.isFrontEndEditor
        if (isFrontEndEditor) {
            out << '<link rel="stylesheet" type="text/css" href="//cdn.linearicons.com/free/1.0.0/icon-font.min.css">'

            ResourceList.fixedFrontEndEditorCss.each {
                out << app.stylesheet(href: it);
            }

            plugin.frontEndEditorCSSs()

        } else {
            out << app.stylesheet(href: "css/admin/edit-content.css")
        }
    }

    def embedCss = { attr, body ->
        def pageId = attr["pageId"]
        def layoutId = attr["layoutId"]
        Page page
        def appendCss = { def elm ->
            out << "<style id='style-store-"
            out << elm.uuid
            out << "'>"
            out << elm.css
            out << "</style>"
        }
        Layout layout
        if (layoutId) {
            layout = Layout.get(layoutId)
            if (pageId || request.isAutoPage) {
                out << "<style id='stored-layout-css'>"
                out << layout.css
                layout.dockableSections.each {
                    out << it.css
                }
                Widget.createCriteria().list {
                    eq("containerId", layoutId)
                    eq("containerType", "layout")
                }.each {
                    out << it.css
                }
                out << "</style>"
            } else {
                out << "<style id='stored-css'>"
                out << layout.css
                out << "</style>"
                layout.dockableSections.each {
                    appendCss.call(it)
                }
                Widget.createCriteria().list {
                    eq("containerId", layoutId)
                    eq("containerType", "layout")
                }.each {
                    appendCss.call(it)
                }
            }
        }
        if (pageId) {
            page = Page.get(pageId)
            out << "<style id='stored-css'>"
            out << page.css
            out << "</style>"
            page.dockableSections.each {
                appendCss.call(it)
            }
            Widget.createCriteria().list {
                eq("containerId", pageId)
                eq("containerType", "page")
            }.each {
                appendCss.call(it)
            }
        }
        if (request.isAutoPage) {
            String css = AutoPageContent.createCriteria().get {
                projections {
                    property "css"
                }
                belong {
                    eq "name", request.page.name
                }
            }
            if (css) {
                out << "<style id='stored-css'>"
                out << css
                out << "</style>"
            }
        }
        StringWriter hookCss = new StringWriter()
        out << HookManager.hook("embedcss-for-editor", hookCss, request)
    }

    def enqueueSiteJs = { attr, body ->
        if (!request.page || request.getAttribute("$attr.scriptId-loaded")) {
            return
        }
        if (request.js_cache == null) {
            request.js_cache = []
        }
        request.js_cache.push(attr.src)
        request.setAttribute("$attr.scriptId-loaded", true)
    }

    def editorJSs = { attr, body ->
        Boolean isFrontEndEditor = pageScope.isFrontEndEditor
        if (isFrontEndEditor) {
            Operator admin = Operator.load(session.admin)
            out.println("<script type='text/javascript' src='//feather.aviary.com/imaging/v3/editor.js'> </script>")

            out << "<script type='text/javascript'>"
            out.println("app.admin_id = ${admin ? admin.id : 'null'};")
            out.println("app.login_email = ${admin ? "'${admin.email}'" : 'null'};")
            out.println("app.edit_mode = true;")
            out << "</script>"

            ResourceList.fixedFrontEndEditorJs.each {
                out << app.javascript(src: it);
            }

            out << plugin.frondEndEditorJSs()
        }
    }

    def pluginInformation = { attr, body ->
        def isProvisioningEnable = Holders.config.webcommander.provision.enabled
        out << "<div section='myPackage' class='my-package section'>"
        out << g.include(controller: "plugin", action: "myPackagePlugins")
        out << "</div>"
        if (isProvisioningEnable){
            out << "<div section='allPackage' class='all-package section'>"
            out << g.include(controller: "plugin", action: "allPackagePlugins")
            out << "</div>"
        }
    }

}
