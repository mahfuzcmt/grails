package com.webcommander.design

import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.content.AlbumService
import com.webcommander.content.Article
import com.webcommander.content.ContentService
import com.webcommander.content.NavigationService
import com.webcommander.util.AppUtil
import com.webcommander.util.SortAndSearchUtil
import com.webcommander.webcommerce.CategoryService
import com.webcommander.webcommerce.ProductService
import com.webcommander.widget.Widget
import grails.converters.JSON

class GalleryWidgetService {
    AlbumService albumService
    CommonService commonService
    NavigationService navigationService
    ProductService productService
    CategoryService categoryService
    ContentService contentService

    Map getGalleryModelForAlbum(Map model, Map params) {
        Long albumId = model.widget.widgetContent ? model.widget.widgetContent[0].contentId : 0
        model.config.album = albumId
        params.id = albumId + ""
        model.totalCount = albumService.getAlbumImageCount(params)
        model.items = commonService.withOffset(params.max, params.offset, model.totalCount) { max, offset, _albumImageCount ->
            params.offset = offset
            params.max = max
            return albumService.getAlbumImagesList(params, [offset: offset, max: max])
        }
        model.links = []
        model.items.each {
            model.links.add(navigationService.getUrl(it.linkType, it.linkTo))
        }
        return model
    }

    Map getGalleryModelForProduct(Map model, Map params) {
        Map config = model.config
        def productIds = []
        if(!config["filter-by"] || config["filter-by"] == "none") {
            productIds = model.widget.widgetContent.contentId.collect { it.longValue() };
        } else {
            productIds = productService.filterSpecialProductIds(config);
        }
        def productList = productService.getProductData(productIds, [:])
        productList = SortAndSearchUtil.sortInCustomOrder(productList, "id", productIds)
        model.items = productList
        return model
    }

    Map getGalleryModelForCategory(Map model, Map params) {
        Map config = model.config
        List categoryIds = model.widget.widgetContent.contentId.collect { it.longValue() };
        List categoryList = categoryService.filterOutAvailableCategories(categoryIds, [:]);
        model.items = SortAndSearchUtil.sortInCustomOrder(categoryList, "id", categoryIds);
        return model
    }

    Map getGalleryModelForArticle(Map model, Map params) {
        List articleIds = model.widget.widgetContent.contentId.collect { it.longValue() };
        List<Article> articleList = contentService.getArticlesInOrder(articleIds);
        model.items = SortAndSearchUtil.sortInCustomOrder(articleList, "id", articleIds);
        return model
    }

    Map getGalleryWidgetMap(Map model) {
        Widget widget = model.widget
        Map config = widget.params ? JSON.parse(widget.params) : [:]
        config.galleryContentType = config.galleryContentType ?: DomainConstants.GALLERY_CONTENT_TYPES.ALBUM
        model.config = config;
        model.totalCount = 0
        model.max = config.max ?: "-1"
        String prefix = model.url_prefix ? model.url_prefix + '-' : ''
        Map params = AppUtil.params
        model.max = params.max = params[prefix + 'max'] ?: model.max;
        model.offset = params.offset = params[prefix + 'offset'] ?: "0"
        model = this."getGalleryModelFor${config.galleryContentType.capitalize()}"(model, params)
        return model
    }

}
