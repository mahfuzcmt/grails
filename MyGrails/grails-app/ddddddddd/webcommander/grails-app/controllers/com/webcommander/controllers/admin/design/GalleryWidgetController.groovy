package com.webcommander.controllers.admin.design

import com.webcommander.JSONSerializable
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.Galleries
import com.webcommander.content.Article
import com.webcommander.content.ContentService
import com.webcommander.webcommerce.Category
import com.webcommander.webcommerce.CategoryService
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import com.webcommander.widget.Widget
import grails.converters.JSON

class GalleryWidgetController {
    ProductService productService
    CategoryService categoryService
    ContentService contentService

    def loadAlbumConfig(Widget widget) {
        render(view: "/admin/widget/galleryConfig/album", model: [albumId: widget && widget.widgetContent.size() ? widget.widgetContent[0].contentId : null])
    }

    def loadProductConfig(Widget widget) {
        Map config = JSON.parse(widget.params)
        List<Product> products =  config.galleryContentType == DomainConstants.GALLERY_CONTENT_TYPES.PRODUCT ? productService.getProductsInOrder(widget.widgetContent.contentId) : []
        render(view: "/admin/widget/galleryConfig/product", model: [products: products, config: config])
    }

    def loadCategoryConfig(Widget widget) {
        Map config = JSON.parse(widget.params)
        List<Category> categories =  config.galleryContentType == DomainConstants.GALLERY_CONTENT_TYPES.CATEGORY ? categoryService.getCategoriesInOrder(widget.widgetContent.contentId) : []
        render(view: "/admin/widget/galleryConfig/category", model: [categories: categories, config: config])
    }

    def loadArticleConfig(Widget widget) {
        Map config = JSON.parse(widget.params)
        List<Article> articles =  config.galleryContentType == DomainConstants.GALLERY_CONTENT_TYPES.ARTICLE ? contentService.getArticlesInOrder(widget.widgetContent.contentId) : []
        render(view: "/admin/widget/galleryConfig/article", model: [articles: articles, config: config])
    }

    def loadContentConfig() {
        Widget widget
            if(params.widgetId  && !params.data) {
                widget = Widget.get(params.long("widgetId"))
            } else if(params.data) {
                widget = JSONSerializable.deSerialize(params.data, Widget);
                widget.discard()
            }
        this."load${params.contentType.capitalize()}Config"(widget);
    }

    def loadSupportedGallery() {
        Widget widget
        if(params.widgetId  && !params.data) {
            widget = Widget.get(params.long("widgetId"))
        } else if(params.data) {
            widget = JSONSerializable.deSerialize(params.data, Widget);
            widget.discard()
        }
        Map config = widget ? JSON.parse(widget.params) : [:]
        def supportedGalleries = Galleries.CONTENT_SUPPORT[params.contentType]
        String contentType = params.contentType.substring(0, 1).toUpperCase() + params.contentType.substring(1).toLowerCase()
        render(view: "/admin/widget/galleryConfig/supportedGallery", model: [supportedGalleries: supportedGalleries, config: config, contentType: contentType])
    }
}
