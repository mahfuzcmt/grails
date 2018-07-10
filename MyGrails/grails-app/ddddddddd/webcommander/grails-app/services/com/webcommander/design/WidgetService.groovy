package com.webcommander.design

import com.webcommander.JSONSerializable
import com.webcommander.Page
import com.webcommander.RenderService
import com.webcommander.RenderTagLib
import com.webcommander.annotations.Initializable
import com.webcommander.admin.Customer
import com.webcommander.beans.SiteMessageSource
import com.webcommander.common.CommonService
import com.webcommander.config.SiteConfig
import com.webcommander.config.StoreDetail
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.content.*
import com.webcommander.events.AppEventManager
import com.webcommander.manager.CartManager
import com.webcommander.manager.CloudStorageManager
import com.webcommander.manager.HookManager
import com.webcommander.models.Cart
import com.webcommander.util.AppUtil
import com.webcommander.util.FileUtil
import com.webcommander.util.SortAndSearchUtil
import com.webcommander.util.StringUtil
import com.webcommander.util.TemplateMatcher
import com.webcommander.util.widget.WidgetDropper
import com.webcommander.webcommerce.Category
import com.webcommander.webcommerce.CategoryService
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import com.webcommander.widget.Widget
import com.webcommander.widget.WidgetContent
import grails.converters.JSON
import grails.util.Holders
import grails.web.servlet.mvc.GrailsHttpSession
import grails.web.servlet.mvc.GrailsParameterMap
import org.apache.commons.io.FileUtils
import org.grails.buffer.FastStringWriter
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.web.util.WebUtils
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import java.nio.file.Files
import java.nio.file.StandardCopyOption

@Initializable
class WidgetService {

    static transactional = false

    RenderService renderService
    ProductService productService
    CategoryService categoryService
    ContentService contentService
    NavigationService navigationService
    CommonService commonService
    GalleryWidgetService galleryWidgetService
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g
    @Autowired
    @Qualifier("com.webcommander.RenderTagLib")
    RenderTagLib render
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    com.webcommander.ApplicationTagLib app
    @Autowired
    @Qualifier("com.webcommander.AppResourceTagLib")
    com.webcommander.AppResourceTagLib appResource

    SiteMessageSource siteMessageSource

    static void initialize() {
        Closure widgetDelete = { widgets ->
            widgets.each {
                Long widgetId = it.id
                AppEventManager.fire("before-widget-delete", [widgetId])
                it.delete()
                AppEventManager.fire("widget-delete", [widgetId])
                AppEventManager.fire(it.widgetType + "-widget-after-drop", [it])
            }
        }

        HookManager.register("album-put-trash-veto-count") { response, id ->
            def count = Widget.createCriteria().list {
                projections {
                    distinct "containerId"
                }
                widgetContent {
                    eq("type", "album")
                    eq("contentId", id)
                }
                eq("containerType", "page")
            }
            if(count) {
                response.page = count.size()
            }
            return response
        }
        HookManager.register("album-put-trash-veto-list") { response, id ->
            List pageIds =  Widget.createCriteria().list {
                projections {
                    distinct "containerId"
                }
                widgetContent {
                    eq("type", "album")
                    eq("contentId", id)
                }
                eq("containerType", "page")
            }
            List pageName = []
            pageIds.eachWithIndex {it, i ->
                Page page = Page.get(it)
                pageName[i] = page.name
            }
            if(pageName.size()) {
                response.page = pageName
            }
            return response
        }

        AppEventManager.on("before-layout-delete") { id ->
            widgetDelete(Widget.createCriteria().list({
                eq "containerId", id
                eq "containerType", 'layout'
            }))
        }
        AppEventManager.on("before-page-delete") {id ->
            widgetDelete(Widget.createCriteria().list({
                eq "containerId", id
                eq "containerType", 'page'
            }))
        }
        AppEventManager.on("before-dock-section-delete", { id ->
            DockSection dockSection = DockSection.get(id)
            widgetDelete(dockSection.widgets)
            dockSection.widgets.clear()
        })
        HookManager.register("navigation-put-trash-at2-count") { response, id ->
            def pages = Widget.createCriteria().list {
                projections {
                    distinct(["containerId", "containerType"])
                }
                widgetContent {
                    eq("type", DomainConstants.WIDGET_CONTENT_TYPE.NAVIGATION)
                    eq("contentId", id)
                }
            }
            if(pages.size()) {
                int pageCount = pages.count {it[1] == "page"}
                int layoutCount = pages.count {it[1] == "layout"}
                if(pageCount) {
                    response.page = pageCount
                }
                if(layoutCount) {
                    response.layout = layoutCount
                }
            }
            return response
        }
        HookManager.register("navigation-put-trash-at2-list") { response, id ->
            def widgets = Widget.createCriteria().list {
                projections {
                    distinct(["containerId", "containerType"])
                }
                widgetContent {
                    eq("type", DomainConstants.WIDGET_CONTENT_TYPE.NAVIGATION)
                    eq("contentId", id)
                }
            }
            if(widgets.size()) {
                int pageCount = widgets.count {it[1] == "page"}
                int layoutCount = widgets.count {it[1] == "layout"}
                if(pageCount) {
                    List pages = Page.createCriteria().list {
                        projections {
                            property("name")
                        }
                        inList("id", widgets.findResults {it[1] == "page" ? it[0] : null})
                    }
                    response.page = pages
                }
                if(layoutCount) {
                    List layouts = Layout.createCriteria().list {
                        projections {
                            property("name")
                        }
                        inList("id", widgets.findResults {it[1] == "layout" ? it[0] : null})
                    }
                    response.layout = layouts
                }
            }
            return response
        }
        HookManager.register("product-put-trash-at2-count") { response, id ->
            def pages = Widget.createCriteria().list {
                projections {
                    distinct(["containerId", "containerType"])
                }
                widgetContent {
                    eq("type", DomainConstants.WIDGET_CONTENT_TYPE.PRODUCT)
                    eq("contentId", id)
                }
            }
            if(pages.size()) {
                int pageCount = pages.count {it[1] == "page"}
                int layoutCount = pages.count {it[1] == "layout"}
                if(pageCount) {
                    response.page = pageCount
                }
                if(layoutCount) {
                    response.layout = layoutCount
                }
            }
            return response
        }
        HookManager.register("product-put-trash-at2-list") { response, id ->
            def widgets = Widget.createCriteria().list {
                projections {
                    distinct(["containerId", "containerType"])
                }
                widgetContent {
                    eq("type", DomainConstants.WIDGET_CONTENT_TYPE.PRODUCT)
                    eq("contentId", id)
                }
            }
            if(widgets.size()) {
                int pageCount = widgets.count {it[1] == "page"}
                int layoutCount = widgets.count {it[1] == "layout"}
                if(pageCount) {
                    List pages = Page.createCriteria().list {
                        projections {
                            property("name")
                        }
                        inList("id", widgets.findResults {it[1] == "page" ? it[0] : null})
                    }
                    response.page = pages
                }
                if(layoutCount) {
                    List layouts = Layout.createCriteria().list {
                        projections {
                            property("name")
                        }
                        inList("id", widgets.findResults {it[1] == "layout" ? it[0] : null})
                    }
                    response.layout = layouts
                }
            }
            return response
        }

        AppEventManager.on("before-navigation-put-in-trash", { id ->
            WidgetContent.createCriteria().list {
                eq("type", DomainConstants.WIDGET_CONTENT_TYPE.NAVIGATION)
                eq("contentId", id)
            }.each {
                it.delete()
            }
        })
        AppEventManager.on("before-product-put-in-trash", { id ->
            WidgetContent.createCriteria().list {
                eq("type", DomainConstants.WIDGET_CONTENT_TYPE.PRODUCT)
                eq("contentId", id)
            }.each {
                it.delete()
            }
        })
    }

    public Map initWidget(params) {
        Widget widget
        Long widgetId = params.long("widgetId")
        Map widgetMap = [:]
        Widget.withSession { hsession ->
            if (widgetId && !params.cache) {
                widget = Widget.get(widgetId)
            } else if (params.cache) {
                widget = JSONSerializable.deSerialize(params.cache, Widget)
                widget.discard()
            } else {
                widget = new Widget(widgetType: params.type, widgetContent: [], uuid: params.uuid)
                widget.params = "{}"
                widget = Widget.findByUuid(params.uuid) ?: widget
                this."populate${params.type.camelCase()}InitialContentNConfig"(widget)
            }
        }
        widgetMap.widget = widget
        Map config = [:]
        if (widget.params) {
            config = JSON.parse(widget.params)
        }
        widgetMap.config = config
        return widgetMap
    }

    def getWidget(Long id) {
        return Widget.get(id)
    }

    def getWidget(String uuid) {
        return Widget.findByUuid(uuid)
    }

    def saveWidget(String type, GrailsParameterMap params) {
        Widget widget
        def widgetId = params.long("widgetId")
        if (widgetId) {
            widget = Widget.get(widgetId)
            widget.widgetContent.retainAll([])
        } else {
            widget = new Widget()
            widget.widgetContent = []
        }
        widget.containerType = params.containerType
        widget.containerId = params.long("containerId")
        widget.uuid = params.uuid
        widget.widgetType = params.widgetType
        widget.title = params.title
        widget.clazz = params.containsKey("clazz") ? params.clazz : widget.clazz

        if (params.fromFrontEnd && this.respondsTo("save${widget.widgetType.capitalize()}WidgetContent")) {
            this."save${widget.widgetType.capitalize()}WidgetContent"(widget, params)
        }

        this."save${type}Widget"(widget, params);
        widget.discard()
        widget.widgetContent*.discard()
        return widget
    }

    def populateArticleInitialContentNConfig(Widget widget) {
        widget.params = '{"article_title": "hide", "display_option": "full"}'
        Article article = Article.findByIsPublishedAndIsInTrashAndIsDisposable(true, false, false)
        if (article && !widget.widgetContent) {
            widget.widgetContent.add(new WidgetContent(type: DomainConstants.WIDGET_CONTENT_TYPE.ARTICLE, contentId: article.id, widget: widget))
        }
    }

    def renderArticleWidget(Widget widget, Writer writer) {
        def articleIds = widget.widgetContent.contentId.collect { it.longValue() }
        def articleList = contentService.getArticlesInOrder(articleIds)
        def config = JSON.parse(widget.params)
        renderService.renderView("/widget/articleWidget", [widget: widget, articleList: articleList, config: config], writer)
    }

    def saveArticleWidget(Widget widget, GrailsParameterMap params) {
        def paramsMap = [display_option: params.display_option, article_title: params.article_title]
        widget.params = JSON.use("deep") {
            paramsMap as JSON
        }.toString()
        def ids = params.list("article").collect { it.toLong() }
        ids.each {
            WidgetContent widgetContent = new WidgetContent(contentId: it)
            widgetContent.widget = widget
            widgetContent.type = DomainConstants.WIDGET_CONTENT_TYPE.ARTICLE
            widget.widgetContent.add(widgetContent)
        }
    }

    private def saveArticleWidgetContent(Widget widget, GrailsParameterMap params) {
        Long id = params.article.toLong(0);

        Article article = id ? Article.get(id) : new Article();
        article.content = params.content;

        article.merge(flush:true);
    }


    def populateCartInitialContentNConfig(Widget widget) {
        widget.params = ([quick_cart: "false", text: "s:cart.count.items"] as JSON).toString()
    }

    def renderCartWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params)
        GrailsHttpSession session = WebUtils.retrieveGrailsWebRequest().session
        Cart cart = CartManager.getCart(session.id)
        if (config.text) {
            TemplateMatcher engine = new TemplateMatcher("%", "%")
            Map results = [
                    item_count         : cart ? (cart.cartItemList.sum { it.quantity } ?: 0) : 0,
                    distinct_item_count: cart ? cart.cartItemList.size() : 0,
                    total_amount       : cart ? cart.total.toCurrency().toPrice() : 0.0,
            ]
            config.text = engine.replace(siteMessageSource.convert(config.text), results)
        }
        List<Resolution> resolutions = []
        Boolean hasGlobal = false
        if (config.responsive_menu == "true") {
            List resolutionIds = config.resolutions instanceof List ? config.resolutions : [config.resolutions]
            hasGlobal = resolutionIds.contains("global")
            resolutionIds = resolutionIds.collect { it.toLong(0) }
            resolutions = Resolution.createCriteria().list {
                inList("id", resolutionIds)
            }
        }
        renderService.renderView("/widget/cartWidget", [widget: widget, config: config, cart: cart, resolutions: resolutions, hasGlobal: hasGlobal], writer)
    }

    def saveCartWidget(Widget widget, GrailsParameterMap params) {
        Map pramMap = [
                quick_cart: params.quick_cart,
                text      : params.text
        ]
        widget.params = (pramMap as JSON).toString()
    }

    def populateCategoryInitialContentNConfig(Widget widget) {
        widget.params = '{"show-pagination": "none", "item_per_page": "10", "description": "true", "item-per-page-selection": "false"}'
        List<Long> categories = Category.createCriteria().list {
            projections {
                property("id")
            }
            eq("isDisposable", false)
            maxResults(5)
        }
        categories.each {
            widget.widgetContent.add(new WidgetContent(type: DomainConstants.WIDGET_CONTENT_TYPE.CATEGORY, contentId: it, widget: widget))
        }
    }

    def renderCategoryWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params)
        def categoryIds = widget.widgetContent.contentId.collect { it.longValue() }
        GrailsParameterMap params = AppUtil.params
        Map filterMap = [:]
        int max = -1
        int offset = 0
        if (config["show-pagination"] != "none") {
            offset = params.int("crwd-" + widget.id + "-offset") ?: 0
            max = params.int("crwd-" + widget.id + "-max") ?: (config["item_per_page"].toInteger(null) ?: -1)
        }
        Integer totalCount = categoryService.filterOutAvailableCategoryCount(categoryIds, filterMap)
        if (max != -1) {
            List filteredIds = categoryService.filterOutAvailableCategoryIds(categoryIds, filterMap)
            filteredIds = filteredIds.sort {
                categoryIds.indexOf(it)
            }
            categoryIds = filteredIds.subList(offset, (offset + max) > filteredIds.size() ? filteredIds.size() : (offset + max))
        }
        def categoryList = categoryService.filterOutAvailableCategories(categoryIds, filterMap)
        categoryList = SortAndSearchUtil.sortInCustomOrder(categoryList, "id", categoryIds)
        renderService.renderView("/widget/categoryWidget", [widget: widget, categoryList: categoryList, config: config, max: max, offset: offset, totalCount: totalCount], writer)
    }

    def saveCategoryWidget(Widget widget, GrailsParameterMap params) {
        widget.params = params.params
        def ids = params.list("category").collect { it.toLong() }
        ids.each {
            WidgetContent widgetContent = new WidgetContent(contentId: it)
            widgetContent.widget = widget
            widgetContent.type = DomainConstants.WIDGET_CONTENT_TYPE.CATEGORY
            widget.widgetContent.add(widgetContent)
        }
    }

    def populateCurrencyInitialContentNConfig(Widget widget) {
        widget.params = ([label: 's:Currency', displayOption: "code"] as JSON).toString()
    }

    def renderCurrencyWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params)
        renderService.renderView("/widget/currency", [widget: widget, config: config], writer)
    }

    def saveCurrencyWidget(Widget widget, GrailsParameterMap params) {
        Map paramMap = [label: parems.label, displayOption: params.displayOption]
        widget.params = JSON.use("deep") {
            paramMap as JSON
        }.toString()
    }

    def populateGalleryInitialContentNConfig(Widget widget) {
        Map paramMap = [:]
        paramMap.galleryContentType = DomainConstants.GALLERY_CONTENT_TYPES.ALBUM
        paramMap.directionNav = "false"
        paramMap.prevText = 's:prev'
        paramMap.nextText = 's:next'
        paramMap.manualAdvance = "false"
        paramMap.controlNav = "false"
        paramMap.controlNavThumbs = "false"
        paramMap.pauseOnHover = "true"
        paramMap.animSpeed = 1000
        paramMap.slideTime = 4000
        paramMap.effect = 'random'
        paramMap.customLink = 'true'
        paramMap.gallery = NamedConstants.GALLERY_TYPES.NIVO_SLIDER
        Album album = Album.findByIsInTrashAndIsDisposable(false, false)
        if (album) {
            paramMap.album = "" + album.id
            WidgetContent widgetContent = new WidgetContent(contentId: album.id)
            widgetContent.widget = widget
            widgetContent.type = DomainConstants.WIDGET_CONTENT_TYPE.ALBUM
            widget.widgetContent.add(widgetContent)
        }
        widget.params = (paramMap as JSON).toString()
    }

    def renderGalleryWidget(Widget widget, Writer writer) {
        Map model = galleryWidgetService.getGalleryWidgetMap([widget: widget, url_prefix: "gall_" + widget.id])
        renderService.renderView("/widget/galleryWidget", model, writer)
    }

    def saveGalleryWidget(Widget widget, GrailsParameterMap params) {
        Map paramMap = new LinkedHashMap(params)
        paramMap.remove("controller")
        paramMap.remove("action")
        params.galleryContentType = params.galleryContentType ?: DomainConstants.GALLERY_CONTENT_TYPES.ALBUM
        widget.params = JSON.use("deep") {
            paramMap as JSON
        }.toString()
        widget = HookManager.hook("before-save-gallery-widget", widget, params)
        switch (params.galleryContentType) {
            case DomainConstants.GALLERY_CONTENT_TYPES.ALBUM:
                if (params.album) {
                    WidgetContent widgetContent = new WidgetContent(contentId: params.album)
                    widgetContent.widget = widget
                    widgetContent.type = DomainConstants.WIDGET_CONTENT_TYPE.ALBUM
                    widget.widgetContent.add(widgetContent)
                }
                break
            case DomainConstants.GALLERY_CONTENT_TYPES.PRODUCT:
            case DomainConstants.GALLERY_CONTENT_TYPES.ARTICLE:
            case DomainConstants.GALLERY_CONTENT_TYPES.CATEGORY:
                params.list(params.galleryContentType).each {
                    WidgetContent widgetContent = new WidgetContent(contentId: it)
                    widgetContent.widget = widget
                    widgetContent.type = params.galleryContentType
                    widget.widgetContent.add(widgetContent)
                }
                break
        }

    }

    def saveHtmlWidget(Widget widget, GrailsParameterMap params) {
        widget.content = params.content
    }

    def saveSpacerWidget(Widget widget, GrailsParameterMap params) {
        widget.content = params.content
        widget.params = params.params
    }

    static {
        AppEventManager.on("image-widget-after-copy") { widget, newWidget ->
            File resource = new File(Holders.servletContext.getRealPath("resources") + "/image-widget/" + widget.uuid)
            File targetResource = new File(Holders.servletContext.getRealPath("resources") + "/image-widget/" + newWidget.uuid)
            if (resource.exists()) {
                FileUtils.copyDirectory(resource, targetResource)
                newWidget.content = newWidget.content?.replace(widget.uuid, newWidget.uuid)
            }
        }

        AppEventManager.on("image-widget-after-drop") { widget ->
            WidgetDropper.afterDropImageWidget(widget.uuid)
        }
    }

    def saveImageWidget(Widget widget, GrailsParameterMap params) {
        Map paramMap = [alt_text: params.alt_text, hype_url: params.hype_url, link_target: params.link_target, upload_type: params.upload_type]
        def _content = params[params.upload_type + "_url"]
        widget.content = _content?:widget.content
        widget.params = JSON.use("deep") {
            paramMap as JSON
        }.toString()
    }

    def saveLoginWidget(Widget widget, GrailsParameterMap params) {
        Map paramMap = [
                name_label           : params.name_label,
                password_label       : params.password_label,
                reset_password_active: params.reset_password_active,
                reset_password_label : params.reset_password_label,
                reg_link_active      : params.reg_link_active,
                reg_link_label       : params.reg_link_label,
                after_failure        : params.after_failure,
                after_login          : params.after_login
        ]
        if (widget.title?.contains("<>")) {
            String[] parts = widget.title.split("<>")
            widget.title = parts[0]
            paramMap.logout_title = parts[1]
        }
        widget.params = JSON.use("deep") {
            paramMap as JSON
        }.toString()
    }

    def populateNavigationInitialContentNConfig(Widget widget) {
        widget.params = ([
                orientation: "V",
                showImage  : 'hide'
        ] as JSON).toString()
        Navigation navigation = Navigation.createCriteria().get({
            eq "isInTrash", false
            maxResults 1
        })
        if (navigation) {
            WidgetContent widgetContent = new WidgetContent(contentId: navigation.id)
            widgetContent.widget = widget
            widgetContent.type = DomainConstants.WIDGET_CONTENT_TYPE.NAVIGATION
            widget.widgetContent.add(widgetContent)
        }
    }

    def renderNavigationWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params)
        def navigationDom = ""
        if (widget.widgetContent.size()) {
            Long navigationId = widget.widgetContent[0].contentId
            navigationDom = getNavigationDom(navigationId, config)
        }
        List<Resolution> resolutions = []
        Boolean hasGlobal = false
        if (config.responsive_menu == "true") {
            List resolutionIds = config.resolutions instanceof List ? config.resolutions : [config.resolutions]
            hasGlobal = resolutionIds.contains("global")
            resolutionIds = resolutionIds.collect { it.toLong(0) }
            resolutions = Resolution.createCriteria().list {
                inList("id", resolutionIds)
            }
        }

        renderService.renderView("/widget/navigation", [widget: widget, dom: navigationDom, config: config, resolutions: resolutions, hasGlobal: hasGlobal], writer)
    }

    def saveNavigationWidget(Widget widget, GrailsParameterMap params) {
        def paramsMap = [
                orientation: params.orientation,
                showImage  : params.showImage
        ]
        WidgetContent widgetContent = new WidgetContent(contentId: params.navigation)
        widgetContent.widget = widget
        widgetContent.type = DomainConstants.WIDGET_CONTENT_TYPE.NAVIGATION
        widget.widgetContent.add(widgetContent)
        widget.params = JSON.use("deep") {
            paramsMap as JSON
        }.toString()
    }

    def saveProductWidget(Widget widget, GrailsParameterMap params) {
        widget.params = params.params
        def ids = params.list("product").collect { it.toLong() }
        ids.each {
            WidgetContent widgetContent = new WidgetContent(contentId: it)
            widgetContent.widget = widget
            widgetContent.type = DomainConstants.WIDGET_CONTENT_TYPE.PRODUCT
            widget.widgetContent.add(widgetContent)
        }
    }

    def populateSearchInitialContentNConfig(Widget widget) {
        widget.params = ([searchType: "product", category: "hide", buttonText: "s:search", placeholderText: "s:search.for.product.etc"] as JSON).toString()
    }

    def renderSearchWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params)
        List<Resolution> resolutions = []
        Boolean hasGlobal = false
        if (config.responsive_menu == "true") {
            List resolutionIds = config.resolutions instanceof List ? config.resolutions : [config.resolutions]
            hasGlobal = resolutionIds.contains("global")
            resolutionIds = resolutionIds.collect { it.toLong(0) }
            resolutions = Resolution.createCriteria().list {
                inList("id", resolutionIds)
            }
        }
        renderService.renderView("/widget/searchWidget", [widget: widget, config: config, resolutions: resolutions, hasGlobal: hasGlobal], writer)
    }

    def saveSearchWidget(Widget widget, GrailsParameterMap params) {
        Map paramMap = [searchType     : params.searchType,
                        category       : params.category,
                        buttonText     : params.buttonText,
                        placeholderText: params.placeholderText
        ]
        widget.params = JSON.use("deep") {
            paramMap as JSON
        }.toString()
    }

    def saveNewsletterWidget(Widget widget, GrailsParameterMap params) {
        Map paramMap = [
                inplace    : params.inplace,
                labelText  : params.labelText,
                buttonText : params.buttonText,
                placeHolder: params.placeHolder,
                hasName    : params.hasName
        ]
        widget.params = JSON.use("deep") {
            paramMap as JSON
        }.toString()
    }

    def renderWidget(String type, Widget widget) {
        Writer writer = new FastStringWriter()
        AppUtil.request.editMode = true
        this."render${type}Widget"(widget, writer)
        return writer.toString()
    }

    def renderBreadcrumbWidget(Widget widget, Writer writer) {
        List breadcrumbItems = []
        def request = AppUtil.request
        Page page = request.page
        String currentItem
        if (request.isAutoPage && page) {
            if (page.name == DomainConstants.AUTO_GENERATED_PAGES.PRODUCT_PAGE) {
                Product product = request.product
                currentItem = product?.name
                Category parent = product?.parent
                while (parent) {
                    Map item = [:]
                    item.title = parent.name.encodeAsBMHTML()
                    item.url = app.relativeBaseUrl() + "category/" + parent.url
                    breadcrumbItems.add(item)
                    parent = parent.parent
                }
            } else if (page.name == DomainConstants.AUTO_GENERATED_PAGES.CATEGORY_PAGE) {
                Category category = request.category
                currentItem = category.name
                Category parent = category.parent
                while (parent) {
                    Map item = [:]
                    item.title = parent.name
                    item.url = app.relativeBaseUrl() + "category/" + parent.url
                    breadcrumbItems.add(item)
                    parent = parent.parent
                }
            } else if (page.name == DomainConstants.AUTO_GENERATED_PAGES.ARTICLE_DETAILS_PAGE) {
                Article article = request.article
                currentItem = article.name
            } else {
                def response = HookManager.hook(page.name + "-breadcrumb", [breadcrumbItems: breadcrumbItems, currentItem: currentItem])
                currentItem = response.currentItem
            }
        } else {
            currentItem = request.page?.name
        }
        Map homeItem = [:]
        Page landingPage = Page.findByUrl(SiteConfig.findByTypeAndConfigKey(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "landing_page").value)
        homeItem.title = landingPage.title
        homeItem.url = app.relativeBaseUrl()

        List reversedBreadcrumbItems = breadcrumbItems.reverse()
        renderService.renderView("/widget/breadcrumb", [widget: widget, homeItem: homeItem, breadcrumbItems: reversedBreadcrumbItems, currentItem: currentItem.encodeAsBMHTML()], writer)
    }

    def renderHtmlWidget(Widget widget, Writer writer) {
        renderService.renderView("/widget/html", [widget: widget, config: [:]], writer)
    }

    def renderImageWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params)
        renderService.renderView("/widget/imageWidget", [widget: widget, config: config], writer)
    }

    def renderSpacerWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params)
        renderService.renderView("/widget/spacerWidget", [widget: widget, config: config], writer)
    }

    def renderStoreLogoWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params)
        String logoUrl = null
        if (config.type == "image") {
            StoreDetail storeDetail = StoreDetail.first()
            logoUrl = appResource.getStoreLogoURL(storeDetails: storeDetail, isDefault: true)
        }
        renderService.renderView("/widget/logoWidget", [widget: widget, config: config, logoUrl: logoUrl], writer)
    }

    def renderLoginWidget(Widget widget, Writer writer) {
        GrailsHttpSession session = WebUtils.retrieveGrailsWebRequest().session;
        if(session.customer) {
            Customer customer = Customer.get(session.customer)
            if(!customer) {
                session.customer = null
            }
        }
        Long configuredFailCount = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_LOGIN_SETTINGS, "fail_count").toLong()
        Boolean useCaptcha = false
        if (session.login_attempt_failed_count > configuredFailCount) {
            useCaptcha = true
        }
        def config = JSON.parse(widget.params)
        if (config.logout_title && AppUtil.session.customer) {
            widget.title = config.logout_title
        }
        config.useCaptcha = useCaptcha
        renderService.renderView("/widget/loginWidget", [widget: widget, config: config], writer)
    }

    def renderNewsletterWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params)
        renderService.renderView("/widget/newsletterWidget", [widget: widget, config: config], writer)
    }

    def renderProductWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params)
        def productIds = []
        if (!config["filter-by"] || config["filter-by"] == "none") {
            productIds = widget.widgetContent.contentId.collect { it.longValue() }
        } else {
            productIds = productService.filterSpecialProductIds(config)
        }
        GrailsParameterMap params = AppUtil.params
        Map filterMap = [:]
        config["product-sorting"] = filterMap["product-sorting"] = params["prwd-" + widget.id + "-sort"]
        config["max"] = -1
        config["offset"] = 0
        if (config["show-pagination"] != "none" && config['display-type'] != NamedConstants.PRODUCT_WIDGET_VIEW.SCROLLABLE) {
            config["offset"] = params.int("prwd-" + widget.id + "-offset") ?: 0
            config["max"] = params.int("prwd-" + widget.id + "-max") ?: (config["item_per_page"].toInteger(null) ?: -1)
        }
        config = HookManager.hook("productWidgetConfigWithRequestContribution", config)
        Integer totalCount = productService.filterOutAvailableProductCount(productIds, filterMap)
        boolean filtered = false
        if (config["max"] != -1 && !filterMap["product-sorting"]) {
            def filteredIds = productService.filterAvailableProducts(productIds, filterMap)
            filteredIds = filteredIds.sort {
                productIds.indexOf(it)
            }
            filtered = true
            productIds = filteredIds.subList(config["offset"], (config["offset"] + config["max"]) > filteredIds.size() ? filteredIds.size() : (config["offset"] + config["max"]))
        }
        if (config["max"] != -1 && filterMap["product-sorting"]) {
            filterMap["max"] = config["max"]
            filterMap["offset"] = config["offset"]
        }
        config["product_listing_id"] = "product-widget-product-listing-${widget.id}"
        def productList = productService.getProductData(productIds, filterMap, filtered)
        if (!filterMap["product-sorting"] && config["max"] == -1) {
            productList = SortAndSearchUtil.sortInCustomOrder(productList, "id", productIds)
        }
        renderService.renderView("/widget/productWidget", [widget: widget, productList: productList, config: config, max: config["max"], offset: config["offset"], totalCount: totalCount],
                writer)
    }

    def renderSocialMediaLikeWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params)
        if (config.socialMediaConfig instanceof String) {
            config.socialMediaConfig = [config.socialMediaConfig]
        }
        renderService.renderView("/widget/likeWidget", [widget: widget, config: config], writer)
    }

    def renderSocialMediaLinkWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params)
        renderService.renderView("/widget/socialMediaLinkWidget", [widget: widget, config: config], writer)
    }

    def renderSocialMediaShareWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params)
        if (!config.socialMediaConfig) {
            config.socialMediaConfig = []
        }
        if (config.socialMediaConfig instanceof String) {
            config.socialMediaConfig = [config.socialMediaConfig]
        }
        renderService.renderView("/widget/socialMediaShareWidget", [widget: widget, config: config], writer)
    }

    def populateBreadcrumbInitialContentNConfig(Widget widget) {}

    def populateSpacerInitialContentNConfig(Widget widget) {
        widget.params = '{"height": 30, "height_in_tab": 25, "height_in_mobile": 20}'
    }

    def populateHtmlInitialContentNConfig(Widget widget) {
        widget.content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut tristique pulvinar ipsum id euismod. Mauris facilisis risus varius, ullamcorper ante quis, pharetra erat. Mauris justo dolor, pellentesque id ex facilisis, efficitur porttitor risus. Phasellus ultricies diam faucibus nisl porttitor, vel aliquet ante volutpat. Interdum et malesuada fames ac ante ipsum primis in faucibus. Aenean et turpis vitae libero sodales sollicitudin ut in turpis. Pellentesque sagittis risus elit, interdum convallis dolor dignissim ut. In suscipit ligula a scelerisque rhoncus. Etiam sodales egestas ante, sit amet hendrerit lectus congue et. Cras pretium vitae dui et euismod. Nam cursus, nibh eget semper ultrices, metus orci tristique nunc, non mattis lectus neque at mi. Nam enim odio, accumsan at est facilisis, varius lobortis justo. Quisque pellentesque erat sed luctus facilisis. Curabitur placerat consectetur ex eu laoreet. Fusce condimentum volutpat efficitur.\n" +
                "\n" +
                "Etiam id ligula vitae urna scelerisque volutpat. Aliquam porttitor quam sit amet est tempus porttitor. Curabitur hendrerit mattis mi a placerat. Interdum et malesuada fames ac ante ipsum primis in faucibus. Nulla facilisi. Nulla tincidunt tristique ultricies. Vivamus pellentesque cursus quam, quis fermentum tellus efficitur nec. Morbi id congue tellus, sit amet imperdiet leo. Sed placerat eros in consectetur venenatis."
    }

    def populateImageInitialContentNConfig(Widget widget) {
        widget.params = '{"upload_type": "direct", "is_default": true}'
        widget.content = app.systemResourceBaseUrl() + appResource.getImageWidgetDefaultUrl()
    }

    def populateStoreLogoInitialContentNConfig(Widget widget) {
        widget.params = '{"type": "image", "alt_text": ""}'
    }

    def populateLoginInitialContentNConfig(Widget widget) {
        widget.params = (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_LOGIN_SETTINGS) as JSON).toString()
    }

    def populateNewsletterInitialContentNConfig(Widget widget) {
        widget.params = '{"inplace": "true", "labelText": "s:newsletter.signup", "buttonText": "s:subscribe", "placeHolder": "s:email", "hasName": "false", "nameLabelText": "s:name", "namePlaceHolder": "s:name"}'
    }

    def populateProductInitialContentNConfig(Widget widget) {
        widget.params = '{"display-type": "image", "show-pagination": "none", "item_per_page": "10", "show_view_switcher": "true", "price": "true", "description": "true", "add_to_cart": "true", "show_on_hover": "false", "item-per-page-selection": "false", "sortable": "false", "expect_to_pay_price": "true", "label_for_price": "s:today", "label_for_call_for_price": "s:call.for.price", "label_for_expect_to_pay": "s:expect.to.pay", "stike_through_previous_price": "false"}'
        List<Long> products = Product.createCriteria().list {
            projections {
                property("id")
            }
            eq("isInTrash", false)
            eq("isParentInTrash", false)
            eq("isDisposable", false)
            maxResults(5)
        }
        products.each {
            widget.widgetContent.add(new WidgetContent(type: DomainConstants.WIDGET_CONTENT_TYPE.PRODUCT, contentId: it, widget: widget))
        }
    }

    def populateSocialMediaLikeInitialContentNConfig(Widget widget) {
        widget.params = '{"orientation": "H", "socialMediaConfig": ["facebook"]}'
    }

    def populateSocialMediaLinkInitialContentNConfig(Widget widget) {
        widget.params = '{"display_option": "H"}'
    }

    def populateSocialMediaShareInitialContentNConfig(Widget widget) {
        widget.params = '{"display_option": "H", "socialMediaConfig": ["facebook", "twitter"]}'
    }

    private def getNavigationDom(Long navigationId, Map params) {
        def navigationItems = navigationService.getNavigationItems(navigationId)
        def items = navigationService.populateAvailableNavigationItemsChildList(navigationId, navigationItems)
        def itemSize = items.size()
        StringWriter tempOut = new StringWriter()
        def isNavHidden = navigationService.isNavigationHidden(navigationId)
        if (itemSize && !isNavHidden) {
            tempOut << "<div class='nav-wrapper " + (params.orientation == "V" ? "vertical" : "horizontal") + (params.showImage == "show" ? " with-image" : "") + "'>"
            Closure recursiveRenderer
            recursiveRenderer = { v, i, isActive ->
                def subNav = v.childItems
                def clazz = "navigation-item-${i} navigation-item et_ecommerce_navigation"
                if (i == 0) {
                    clazz += " first"
                }
                Closure lidom = navigationService.getAnchorTag(v)
                boolean selected = navigationService.isActive(v.itemType, v.itemRef)
                if (selected) {
                    clazz += " active"
                    isActive.active = true
                }
                if (i == itemSize - 1) {
                    clazz += " last"
                }
                if (subNav.size()) {
                    clazz += " has-child"
                    StringWriter cacheOut = tempOut
                    tempOut = new StringWriter()
                    Map childActive = [:]
                    subNav.eachWithIndex { v2, i2 ->
                        recursiveRenderer(v2, i2, childActive)
                    }
                    if (childActive.active) {
                        clazz += " child-active active"
                        isActive.active = true
                    }
                    clazz = " class='" + clazz + "'"
                    cacheOut << "<div${clazz} et-category='link'>"
                    def imageDom = new StringWriter()
                    if (params.showImage == "show") {
                        imageDom << "<span class='image-wrapper " + (v.image ? "" : "no-image") + "'>"
                        if (v.image) {
                            def imageAlt = v.imageAlt ?: v.image
                            imageDom << "<img src='" + appResource.getNavigationItemImageURL(navigationItem: v) + "' alt='" + imageAlt + "'   >"
                        }
                        imageDom << "</span>"
                    }
                    cacheOut << lidom(imageDom)
                    cacheOut << "<span class='child-opener'></span>"
                    cacheOut << "<div class='navigation-item-child-container" + (childActive.active ? ' active' : '') + "'>"
                    cacheOut << tempOut.toString()
                    cacheOut << "</div>"
                    cacheOut << "</div>"
                    tempOut = cacheOut
                } else {
                    clazz = " class='" + clazz + "'"
                    tempOut << "<div${clazz} et-category='link'>"
                    def imageDom = new StringWriter()
                    if (params.showImage == "show") {
                        imageDom << "<span class='image-wrapper " + (v.image ? "" : "no-image") + "'>"
                        if (v.image) {
                            def imageAlt = v.imageAlt ?: v.image
                            imageDom << "<img src='" + appResource.getNavigationItemImageURL(navigationItem: v) + "' alt='" + imageAlt + "' >"
                        }
                        imageDom << "</span>"
                    }
                    tempOut << lidom(imageDom)
                    tempOut << "</div>"
                }
            }
            items.eachWithIndex { v, i ->
                recursiveRenderer(v, i, [:])
            }
            tempOut << "</div>"
        }
       return tempOut.toString()
    }

    def renderPageWidget(Widget widget, Writer writer) {
        String pageBody
        if (AppUtil.request.isAutoPage && !AppUtil.request.hasContent) {
            pageBody = g.layoutBody().toString()
        } else {
            def page = AppUtil.request.page
            pageBody = render.renderPageContent([value: page.body])
        }
        pageBody = "<div class='page-content'>" + pageBody + "</div>"
        writer << pageBody
    }

    def savePageContents(String containerType, Long containerId, List removed, List removedDock, List addedInHeader, List addedInFooter, List addedInBody, List addedInDock, List docks, Map modified, String bodyContent, String containerCss, String containerJs, String section) {
        def objClass = containerType == "layout" ? Layout : Page
        objClass = HookManager.hook("save-page-container-type", objClass, containerType)
        List newWidgets = []
        List newDocks = []
        Closure onDelete = { widget ->
            AppEventManager.fire(widget.widgetType + "-widget-after-drop", [widget])
        }
        objClass.withNewTransaction { transaction ->
            def contentObj = objClass.get(containerId)
            if (!contentObj) {
                return false
            }
            removedDock.each {
                DockSection dockSection = DockSection.get(it)
                AppEventManager.fire("before-dock-section-delete", [dockSection.id])
                contentObj.removeFromDockableSections(dockSection)
                dockSection.delete()
                AppEventManager.fire("dock-section-delete", [dockSection.id])
            }
            def removeList = []
            removed.each { wi ->
                Widget widget
                widget = getWidget(wi.uuid)
                if (wi.section == "header" && widget) {
                    contentObj.removeFromHeaderWidgets(widget)
                } else if (wi.section == "footer" && widget) {
                    contentObj.removeFromFooterWidgets((widget))
                } else if (wi.section != "body") {
                    if (widget) { // may be dock deleted earlier
                        DockSection dockSection = DockSection.createCriteria().get {
                            widgets {
                                eq "uuid", wi.uuid
                            }
                        }
                        if (dockSection) {
                            dockSection.removeFromWidgets(widget)
                            dockSection.save()
                        }
                    }
                }
                if (widget) {
                    onDelete(widget)
                    widget.delete()
                } else {
                    removeList.add(wi.uuid)
                }
            }
            if (removeList.size() > 0) {
                Widget.createCriteria().list {
                    inList("uuid", removeList)
                }.each {
                    onDelete(it)
                    it.delete()
                }
            }
            docks.each {
                DockSection dockSection
                if (it.dockId) {
                    dockSection = DockSection.get(it.dockId)
                } else {
                    dockSection = new DockSection()
                }
                dockSection.uuid = it.uuid
                dockSection.css = it.css
                if (!it.dockId) {
                    contentObj.addToDockableSections(dockSection.save())
                    newDocks.add([uuid: dockSection.uuid, id: dockSection.id])
                } else {
                    dockSection.merge()
                }
            }
            Closure addWidget = { wjson, type ->
                Widget widget = new Widget()
                Long widgetId = widget.id
                if (wjson.cache) {
                    widget.deSerialize(wjson.cache)
                }
                widget.id = widgetId
                widget.widgetType = wjson.type
                widget.uuid = wjson.uuid
                widget.containerId = containerId
                widget.containerType = containerType
                widget.css = wjson.css
                widget.js = wjson.js
                widget.groupId = wjson.groupId
                AppEventManager.fire("widget-" + widget.uuid + "-before-save", [widget])
                widget.save()
                if (type == "dock") {
                    DockSection dockSection = DockSection.findByUuid(wjson.dockUUID)
                    dockSection.addToWidgets(widget)
                } else if (type == "header") {
                    contentObj.addToHeaderWidgets(widget)
                } else if (type == "footer") {
                    contentObj.addToFooterWidgets(widget)
                }
                newWidgets.add([uuid: widget.uuid, id: widget.id])
            }
            addedInDock.each {
                addWidget it, 'dock'
            }
            addedInHeader.each {
                addWidget it, "header"
            }
            addedInFooter.each {
                addWidget it, "footer"
            }
            addedInBody.each {
                addWidget it, "body"
            }
            modified.each {
                def data = it.value
                Widget widget = Widget.findByUuid(it.key)
                if (!widget) { // may be deleted for dock delete above
                    return
                }
                if (data.cache) {
                    List oldContents = new ArrayList<WidgetContent>(widget.widgetContent)
                    widget.widgetContent.clear()
                    widget.deSerialize(data.cache)
                    oldContents.removeAll(widget.widgetContent)
                    oldContents*.delete()
                }
                widget.js = data.js != null ? data.js : widget.js
                widget.clazz = data.clazz != null ? data.clazz : widget.clazz
                widget.css = data.css
                widget.groupId = data.groupId
                AppEventManager.fire("widget-" + widget.uuid + "-before-save", [widget])
                widget.merge(flush: true) //merge is nescessary here to invoke beforeValidate for contents
            }
            if (bodyContent != null) {
                contentObj.body = bodyContent
            }
            if(containerCss != null) {
                contentObj.css = containerCss
            }
            if (containerJs != null) {
                contentObj.js = containerJs
            }
            contentObj.merge()
        }
        AppEventManager.fire(containerType + "-update", [containerId])
        return [newWidgets: newWidgets, containerId: containerId, newDocks: newDocks]
    }

    Widget copyWidget(Long id, Long containerId, String containerType) {
        Widget widget = Widget.get(id)
        String uuid = StringUtil.uuid
        Widget newWidget = copyWidgetProperties(widget, containerId, containerType, uuid)
        newWidget.save()
        widget.widgetContent.each {
            newWidget.addToWidgetContent(new WidgetContent(contentId: it.contentId, type: it.type, widget: newWidget).save())
        }
        AppEventManager.fire(widget.widgetType + "-widget-after-copy", [widget, newWidget])
        return newWidget
    }

    Widget copyFrontEndWidget(Long id, Long containerId, String containerType, String uuid) {
        Widget widget = Widget.get(id)
        Widget newWidget = copyWidgetProperties(widget, containerId, containerType, uuid)
        widget.widgetContent.each {
            newWidget.addToWidgetContent(new WidgetContent(contentId: it.contentId, type: it.type, widget: newWidget))
        }
        newWidget.discard()
        newWidget.widgetContent*.discard()
        AppEventManager.fire(widget.widgetType + "-widget-after-copy", [widget, newWidget])
        return newWidget
    }

    def copyWidgetProperties(Widget widget, Long containerId, String containerType, String uuid){
        String css = widget.css?.replace("#wi-" + widget.uuid, "#wi-" + uuid)
        Widget newWidget = new Widget(containerId: containerId, uuid: uuid, containerType: containerType, css: css)
        BeanUtils.copyProperties(widget, newWidget, "class", "containerId", "uuid", "containerType", "css", "widgetContent", "id", "created", "updated", "properties", "constraints", "errors", "hasMany", "metaClass")
        return newWidget
    }

    String getJSByUuid(String uuid) {
        Widget widget = Widget.findByUuid(uuid)
        return widget ? widget.js : ""
    }

    String getClazzByUuid(String uuid) {
        Widget widget = Widget.findByUuid(uuid)
        return widget ? widget.clazz : ""
    }

    def saveAnyWidget(String type, def params){
        def widget = saveWidget(type, params)
        if (widget) {
            def widgetContent = renderWidget(type, widget)
            return [status: "success", html: widgetContent, serialized: widget.serialize()] as JSON
        }
        return  [status: "error"] as JSON
    }


    boolean moveFileContents(File sourceFile, File targetFile) {
        FileInputStream inputStream
        File[] paths
        try {
            if (sourceFile.isDirectory()) {      // Folder to Folder move
                paths = sourceFile.listFiles()
                if (!targetFile.exists()) targetFile.mkdir()
                for (File path : paths) {
                    inputStream = new FileInputStream(path)
                    Files.copy(inputStream, targetFile.toPath().resolve(path.toPath().getFileName()), StandardCopyOption.REPLACE_EXISTING)
                    inputStream.close()
                }
                sourceFile.deleteDir()
            } else {                            // File to File move
                inputStream = new FileInputStream(sourceFile)
                Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                inputStream.close()
                sourceFile.delete()
            }
            return true
        } catch (Exception ex) {
            ex.printStackTrace()
            return false
        }
    }

}
