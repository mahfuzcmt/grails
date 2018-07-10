package com.webcommander.controllers.rest.elastic

import com.webcommander.Page
import com.webcommander.constants.DomainConstants
import com.webcommander.content.Article
import com.webcommander.content.ContentService
import com.webcommander.content.PageService
import com.webcommander.design.WidgetService
import com.webcommander.manager.HookManager
import com.webcommander.util.AppUtil
import com.webcommander.util.RestProcessor
import com.webcommander.webcommerce.Category
import com.webcommander.webcommerce.CategoryService
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import com.webcommander.widget.Widget
import org.grails.buffer.FastStringWriter

class ElasticCrawlerHelperController extends RestProcessor {
    ProductService productService
    CategoryService categoryService
    PageService pageService
    ContentService contentService
    WidgetService widgetService

    def productList() {
        String submitRestricted = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SEARCH_PAGE, "submit_restricted_item")
        List<Product> products = productService.getProducts([
                lookup: "recursive",
                max: -1, offset: 0,
                availableFor: submitRestricted == "true" ? null : DomainConstants.PRODUCT_AVAILABLE_FOR.EVERYONE,
                isAvailable: "true",
                isActive: "true"
        ]);
        List fields = ["id", "name", "sku", "url", "model", "summary", "description", "globalTradeItemNumber", "title", "heading", "metaTags", "updated"]
        List results = products.collect { Product product ->
            Map result = [:]
            fields.each {
                result[it] = product[it]
            }
            return result
        }
        rest products: results
    }

    def categoryList() {
        String submitRestricted = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SEARCH_PAGE, "submit_restricted_item")
        List<Category> categories = categoryService.getCategories([
                max: -1,
                offset: 0,
                availableFor: submitRestricted == "true" ? null : DomainConstants.CATEGORY_AVAILABLE_FOR.EVERYONE,
                isAvailable: "true"
        ]);
        List fields = ["id", "name", "sku", "url", "summary", "description", "title", "heading", "metaTags", "updated", ]
        List results = categories.collect { Category product ->
            Map result = [:]
            fields.each {
                result[it] = product[it]
            }
            return result
        }
        rest categories: results
    }

    private String pageContent(Page page, Map result) {
        Closure widgetRenderer
        FastStringWriter writer = new FastStringWriter()
        widgetRenderer = {Widget widget->
            switch (widget.widgetType) {
                case DomainConstants.WIDGET_TYPE.ARTICLE:
                    def articleIds = widget.widgetContent.contentId.collect { it.longValue() };
                    List<Article> articleList = contentService.getArticlesInOrder(articleIds);
                    articleList.each {
                        if(result.updated < it.updated) {
                            result.updated = it.updated
                        }
                    }
                    widgetService.renderArticleWidget(widget, writer)
                    break
                case DomainConstants.WIDGET_TYPE.HTML:
                    widgetService.renderHtmlWidget(widget, writer)
                    break
                default:
                    HookManager.hook("render-${widget.widgetType}-widget", result, widget, writer, widgetRenderer)

            }
        }
        List headerFooterWidgetIds = page.headerWidgets.id + page.footerWidgets.id
        List<Widget> bodyWidgets = Widget.createCriteria().list {
            eq("containerType", "page")
            eq("containerId", page.id)
            if(headerFooterWidgetIds) {
                not {
                    inList("id", )
                }
            }
        }
        bodyWidgets.each {Widget widget->
            widgetRenderer(widget)
        }
        writer.toString()
    }

    def pageList() {
        String submitRestricted = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SEARCH_PAGE, "submit_restricted_item")
       List<Page> pages = pageService.getPages([
               max: -1,
               offset: 0,
               visibility: submitRestricted == "true" ? null : DomainConstants.PAGE_VISIBILITY.OPEN,
               isActive: "true"
       ]);
       List results = pages.collect {
           Map result = [id: it.id, name: it.name, title: it.title, url: it.url, metaTagsL: it.metaTags, updated: it.updated]
           result.pageContent = pageContent(it, result)
           return result
       }
       rest(pages: results)
    }
}
