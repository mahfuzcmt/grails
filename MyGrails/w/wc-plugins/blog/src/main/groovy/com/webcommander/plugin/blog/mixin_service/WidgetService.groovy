package com.webcommander.plugin.blog.mixin_service

import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.blog.content.BlogService
import com.webcommander.throwables.UnconfiguredWidgetExceptions
import com.webcommander.util.AppUtil
import com.webcommander.widget.Widget
import com.webcommander.widget.WidgetContent
import grails.converters.JSON
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.transaction.annotation.Transactional

class WidgetService {
    static BlogService _blogService

    private BlogService getBlogService() {
        if(_blogService) {
            return _blogService
        }
        return _blogService = Holders.grailsApplication.mainContext.getBean(BlogService)
    }

    def populateBlogPostInitialContentNConfig(Widget widget) {
        widget.params = '{"post_content": "S", "content_length": 160, "read_more": "' + g.message(code: "read.more") + '", "selection": "recent_top", "post_count": 5, "image": "true", "date": "true", "comment_count": "true", "pagination": "true"}'
    }

    @Transactional(readOnly = true)
    def saveBlogPostWidget(Widget widget, GrailsParameterMap params) {
        def paramMap = [
            title : params.title,
            post_content : params.post_content,
            content_length : params.content_length,
            read_more : params.read_more,
            selection : params.selection,
            post_count : params.post_count,
            date : params.date,
            author : params.author,
            categories : params.categories,
            comment_count : params.comment_count,
            image : params.image,
            height : params.height,
            pagination: params.pagination
        ]

        widget.params = JSON.use("deep") {
            paramMap as JSON
        }.toString();
        def ids = params.list("post").collect { it.toLong() };
        ids.each {
            WidgetContent widgetContent = new WidgetContent(contentId: it);
            widgetContent.widget = widget;
            widgetContent.type = DomainConstants.WIDGET_CONTENT_TYPE.BLOG_POST
            widget.widgetContent.add(widgetContent);
        }

        return widget;
    }

    def renderBlogPostWidget(Widget widget, Writer writer) {
        def config = [:];
        if (widget.params) {
            config = JSON.parse(widget.params)
        } else {
            throw new UnconfiguredWidgetExceptions()
        }
        GrailsParameterMap params = AppUtil.params
        Map filterMap = [:]
        filterMap["offset"] = params.int("bw-" + widget.id + "-offset") ?: 0;
        filterMap["max"] = params.int("bw-" + widget.id + "-max") ?: (config["post_count"]?.toInteger() ?: -1)
        def posts;
        Integer total;
        if(config.selection == "custom") {
            def postIds = widget.widgetContent.contentId.collect { it.longValue() };
            posts = blogService.getPostsInOrder(postIds, filterMap)
            total = postIds ? blogService.getCountPosts(postIds) : 0;
        } else {
            filterMap = populateFilterMap(config, filterMap)
            total = countPostsForWidget(filterMap)
            posts = getPostsForWidget(filterMap)
        }
        renderService.renderView("/plugins/blog/widget/blogWidget", [widget: widget, posts: posts, config: config, url_prefix: "bw-" + widget.id, filterMap: filterMap, total: total], writer)
    }

    private Map populateFilterMap(Map config, Map filterMap) {
        String selectionType = config.selection
        def timezone = AppUtil.session.timezone

        Date today = new Date().gmt().toZone(timezone).dayStart
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(today)
        switch(selectionType) {
            case "current_month":
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                Date startTime = calendar.getTime().gmt(timezone)
                calendar.add(Calendar.MONTH, 1);
                calendar.add(Calendar.DATE, -1);
                Date endTime = calendar.getTime().dayEnd.gmt(timezone)
                filterMap.dateRange = [start: startTime, end: endTime]
                break;
            case "current_week":
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek);
                Date startTime = calendar.getTime().gmt(timezone)
                calendar.add(Calendar.DATE, 6)
                Date endTime = calendar.getTime().dayEnd.gmt(timezone)
                filterMap.dateRange = [start: startTime, end: endTime]
                break;
            case "recent_top":
                filterMap.recent = true
                break;
        }
        return filterMap
    }

    private def getPostsForWidget(Map filterMap) {
        return blogService.filterOutAvailableBlogPost(null, AppUtil.session.customer, filterMap)
    }

    private def countPostsForWidget(Map filterMap) {
        return blogService.countAvailableBlogPost(null, AppUtil.session.customer, filterMap)
    }

}
