package com.webcommander.plugin.news

import com.webcommander.annotations.Initializable
import com.webcommander.constants.DomainConstants
import com.webcommander.content.Article
import com.webcommander.events.AppEventManager
import com.webcommander.installation.template.TemplateContent
import com.webcommander.manager.HookManager
import com.webcommander.models.blueprints.DisposableUtilServiceModel
import com.webcommander.task.MultiLoggerTask
import com.webcommander.task.TaskService
import com.webcommander.util.AppUtil
import grails.gorm.transactions.Transactional
import org.springframework.dao.DataIntegrityViolationException

@Initializable
class NewsService implements DisposableUtilServiceModel {
    TaskService taskService

    static void initialize() {
        HookManager.register("article-put-trash-veto-count") { response, id ->
            int newsCount = News.createCriteria().count {
                eq("article.id", id)
            }
            if(newsCount) {
                response.news = newsCount
            }
            return response;
        }
        HookManager.register("article-put-trash-veto-list") { response, id ->
            List<News> news = News.createCriteria().list {
                eq("article.id", id)
            }
            if(news.size()) {
                response.news = news.collect {it.title}
            }
            return response;
        }

        HookManager.register("before-article-delete-veto") { response, id ->
            int newsCount = News.createCriteria().count {
                eq("article.id", id)
            }
            if(newsCount) {
                response.news = newsCount
            }
            return response;
        }

        AppEventManager.on("before-template-install", {
            List<TemplateContent> contents = TemplateContent.createCriteria().list {
                eq("contentType", DomainConstants.WIDGET_CONTENT_TYPE.NEWS)
            }
            if(contents) {
                News.where {
                    inList("id", contents.contentId)
                }.updateAll(isDisposable: true)
                contents*.delete()
            }
        })
    }

    private Closure getCriteriaClosure(Map params) {
        def session = AppUtil.session
        return {
            if (params.searchText) {
                ilike("title", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.article) {
                article {
                    ilike("name", "%${params.article.trim().encodeAsLikeText()}%")
                }
            }
            if(params.isDisposable != "true") {
                eq("isDisposable", false)
            } else {
                eq("isDisposable", true)
            }
            eq("isInTrash", false);

            if (params.newsFrom) {
                Date date = params.newsFrom.dayStart.gmt(session.timezone);
                ge("newsDate", date);
            }
            if (params.newsTo) {
                Date date = params.newsTo.dayEnd.gmt(session.timezone);
                le("newsDate", date);
            }
        }
    }

    News getNews(Map params) {
        return News.get(params.id)
    }

    List<News> getNewses(Map params) {
        def listMap = [max: params.max, offset: params.offset];
        return News.createCriteria().list(listMap) {
            and getCriteriaClosure(params)
            order(params.sort ?: "title", params.dir ?: "asc")
        }
    }

    Integer getNewsCount (Map params) {
        return News.createCriteria().get {
            and getCriteriaClosure(params)
            projections {
                rowCount();
            }
        }
    }

    @Transactional
    Boolean save(Map params) {
        def session = AppUtil.session
        Map properties = [
            title: params.title,
            newsDate: params.newsDate.toDate().gmt(session.timezone),
            summary: params.summary,
            article: Article.get(params.article),
            isDisposable: false
        ];
        if (params.id) {
            News news = News.get(params.id)
            news.setProperties(properties)
            AppEventManager.fire("news-update", [news.id])
            news.merge()
            return !news.hasErrors()
        } else {
            News news = new News(properties)
            news.save()
            return !news.hasErrors()
        }
    }

    @Transactional
    Boolean deleteNews(Long id) {
        News news = News.get(id)
        try {
            news.delete(flush: true)
            return true
        } catch (DataIntegrityViolationException e) {
            return false
        }
    }

    @Transactional
    Boolean deleteSelected(List<Long> ids) {
        List<News> newses = ids ? News.findAllByIdInList(ids) : []
        try {
            newses*.delete(flush: true)
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    List<News> getNewsesInOrder(List<Long> newsesIds) {
        Map emptyMap = newsesIds.collectEntries { [(it): null] }
        List<News> newses = newsesIds ? News.findAllByIdInList(newsesIds) : []
        newses.each {
            emptyMap[it.id] = it;
        }
        return emptyMap.values() as List;
    }

    List<News> getNewsesForWidget(Map params) {
        def session = AppUtil.session;
        def c = News.createCriteria();
        String sort = params.transition_direction == "descending_of_news_date" ? "desc" : "asc";
        List<News> result = c.list {
            eq("isDisposable", false)
            if (params.selection == "date_range") {
                if (params.news_from) {
                    Date date = params.news_from.dayStart.gmt(session.timezone);
                    ge("newsDate", date);
                }
                if (params.news_to) {
                    Date date = params.news_to.dayStart.gmt(session.timezone);
                    le("newsDate", date);
                }
            } else if (params.selection == "last_one_week") {
                Date date = new Date() - 7
                ge("newsDate", date);
            } else if (params.selection == "last_one_month") {
                Date date = new Date() - 30
                ge("newsDate", date);
            }

            order("newsDate", sort);
        }
        return result;

    }

    @Override
    Integer countDisposableItems(String itemType) {
        return this.getNewsCount([isDisposable: "true"])
    }


    @Override
    void removeDisposableItems(String type, MultiLoggerTask task) {
        News.withNewSession {session ->
            List<News> newses = getNewses([isDisposable: "true"])
            for (News news: newses) {
                try {
                    deleteNews(news.id)
                    session.flush()
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.success("News: $news.title", "")
                    task.meta.successCount++
                } catch (Exception e) {
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("News: $news.title", e.message)
                    task.meta.errorCount++;
                }
            }
        }
    }
}
