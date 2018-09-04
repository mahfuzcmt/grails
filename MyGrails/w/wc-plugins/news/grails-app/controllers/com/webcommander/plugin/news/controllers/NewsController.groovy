package com.webcommander.plugin.news.controllers

import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.common.CommonService
import com.webcommander.plugin.news.News
import com.webcommander.plugin.news.NewsService
import com.webcommander.webcommerce.ProductService
import grails.converters.JSON

class NewsController {
    NewsService newsService
    ProductService productService
    CommonService commonService

    @Restriction(permission = "news.view.list")
    def loadAppView() {
        Integer count = newsService.getNewsCount(params)
        params.max = params.max ?: "10";
        List<News> newses = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            newsService.getNewses(params)
        }
        render(view: "/plugins/news/admin/appView", model: [newses: newses, count: count]);
    }

    @Restrictions([
            @Restriction(permission = "news.create", params_not_exist = "id"),
            @Restriction(permission = "news.edit", entity_param = "id", domain = News)
    ])
    def edit() {
        News news = params.id ? newsService.getNews(params) : new News()
        render(view: "/plugins/news/admin/infoEdit", model: [news: news]);
    }

    def save() {
        if (newsService.save(params)) {
            render([status: "success", message: g.message(code: "news.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "news.save.failure")] as JSON)
        }
    }

    def view () {
        News news = newsService.getNews(params)
        render(view: "/plugins/news/admin/infoView", model: [news: news])
    }

    @Restriction(permission = "news.remove", entity_param = "id", domain = News)
    def delete() {
        Long id = params.long("id");
        if (newsService.deleteNews(id)) {
            render([status: "success", message: g.message(code: "news.delete.success")] as JSON);
        } else {
            render([status: "error", message: g.message(code: "news.delete.failure")] as JSON);
        }
    }

    def advanceFilter() {
        render(view: "/plugins/news/admin/filter", model: [d: true])
    }

    def loadNewsesForSelection () {
        params.max = params.max ?: "10"
        List<News> newses = newsService.getNewses(params)
        render(view: "/plugins/news/admin/selectionPanel", model: [newses: newses, count: newses.size()])
    }

    @Restriction(permission = "news.remove", entity_param = "id", domain = News)
    def deleteSelectedNewses() {
        def ids = [];
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        if (newsService.deleteSelected(ids)) {
            render([status: "success", message: g.message(code: "selected.newses.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "selected.newses.could.not.delete")] as JSON)
        }
    }

    def isUnique() {
        if (commonService.isUnique(News, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }
}
