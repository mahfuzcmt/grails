package com.webcommander.content

import com.webcommander.ApplicationTagLib
import com.webcommander.Page
import com.webcommander.admin.Operator
import com.webcommander.admin.TrashService
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.design.Layout
import com.webcommander.events.AppEventManager
import com.webcommander.installation.template.TemplateContent
import com.webcommander.manager.HookManager
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import com.webcommander.util.TrashUtil
import com.webcommander.widget.WidgetContent
import grails.gorm.transactions.Transactional
import org.hibernate.sql.JoinType

@Initializable
class ContentService {
    CommonService commonService;
    TrashService trashService;
    ApplicationTagLib g

    static void initialize() {
        HookManager.register("section-delete-at1-count") { response, id ->
            int sectionCount = Section.where {
                parent.id == id
            }.count()
            if(sectionCount) {
                response.sections = sectionCount
            }
            return response;
        }

        HookManager.register("section-delete-at1-list") { response, id ->
            List sections = Section.createCriteria().list {
                projections {
                    property("name")
                }
                eq("parent.id", id)
            }
            if(sections.size()) {
                response.sections = sections
            }
            return response;
        }

        AppEventManager.on("before-section-delete", { id, at1WithChildren ->
            if(at1WithChildren=="include") {
                Section.where {
                    parent == Section.proxy(id)
                }.list().each {
                    def dataList = [it.id, "include"];
                    AppEventManager.fire("before-section-delete", dataList);
                    it.delete();
                    AppEventManager.fire("section-delete", dataList);
                }
            } else {
                Section.where {
                    parent == Section.proxy(id);
                }.updateAll([parent: null])
            }
        })

        AppEventManager.on("before-operator-delete", { Long id ->
            Article.executeUpdate("update Article a set a.createdBy = null where a.createdBy.id = :uid", [uid: id])
        })
        HookManager.register("section-delete-at1-count") { response, id ->
            int articleCount = Article.where {
                section.id == id
            }.count()
            if(articleCount) {
                response.articles = articleCount
            }
            return response
        }

        HookManager.register("section-delete-at1-list") { response, id ->
            List articles = Article.createCriteria().list {
                projection{
                    property("name")
                }
                eq("section.id", id)
            }
            if(articles.size()) {
                response.articles = articles
            }
            return response
        }

        AppEventManager.on("before-section-delete", { id, at1_reply ->
            if(at1_reply == "include") {
                Article.where {
                    section == Section.proxy(id)
                }.list().each {
                    Map vetos = HookManager.hook("article-delete-veto", [:], it.id)
                    if(vetos.size()) {
                        it.section = null;
                    } else {
                        AppEventManager.fire("before-article-delete", [id, at1_reply])
                        it.delete()
                        AppEventManager.fire("article-delete", [id, at1_reply])
                    }
                }
            } else {
                Article.where {
                    section == Section.proxy(id);
                }.updateAll([section: null])
            }
        })

        AppEventManager.on("before-template-install", {
            List<TemplateContent> contents = TemplateContent.createCriteria().list {
                eq("contentType", DomainConstants.WIDGET_CONTENT_TYPE.ARTICLE)
            }
            if(contents) {
                Article.where {
                    inList("id", contents.contentId)
                }.updateAll(isDisposable: true)
                contents*.delete()
            }
        })
    }

    Article getArticle(Long id) {
        return Article.get(id);
    }

    Section getSection(Long id) {
        return Section.get(id);
    }

    private Closure getCriteriaClosureForWidget(Map params) {
        def session = AppUtil.session;
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.name) {
                ilike("name", "%${params.name.trim().encodeAsLikeText()}%")
            }
            if (params.section) {
                if (params.section == "root" || params.section == "0") {
                    isNull("section")
                } else {
                    eq("section.id", params.section.toLong(0))
                }
            }
            if(params.ids) {
                inList("id", params.list("ids").collect {it.toLong()})
            }
            eq("isInTrash", false);
            eq("isPublished", true);
            if (params.sortBy) {
                switch (params.sortBy) {
                    case "ALPHA_ASC": order("name", "asc");
                        break;
                    case "ALPHA_DESC": order("name", "desc");
                        break;
                    case "CREATED_ASC": order("created", "asc");
                        break;
                    case "CREATED_DESC": order("created", "desc")
                        break;
                }
            }
            if(params.isDisposable != "true") {
                eq("isDisposable", false)
            } else {
                eq("isDisposable", true)
            }
        }
    }

    private Closure getCriteriaClosure(Map params) {
        def session = AppUtil.session;
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.name) {
                ilike("name", "%${params.name.trim().encodeAsLikeText()}%")
            }
            if(params.ids) {
                inList("id", params.list("ids").collect {it.toLong()})
            }
            if (params.section) {
                if (params.section == "root" || params.section == "0") {
                    isNull("section")
                } else {
                    eq("section.id", params.section.toLong(0))
                }
            }
            if (params.createdFrom) {
                Date date = params.createdFrom.dayStart.gmt(session.timezone);
                ge("created", date);
            }
            if (params.createdTo) {
                Date date = params.createdTo.dayEnd.gmt(session.timezone);
                le("created", date);
            }
            if (params.updatedFrom) {
                Date date = params.updatedFrom.dayStart.gmt(session.timezone);
                ge("updated", date);
            }
            if (params.updatedTo) {
                Date date = params.updatedTo.dayEnd.gmt(session.timezone);
                le("updated", date);
            }
            if(params.isPublished) {
                eq("isPublished", params.isPublished.toBoolean())
            }
            if(params.isDisposable != "true") {
                eq("isDisposable", false)
            } else {
                eq("isDisposable", true)
            }
            eq("isInTrash", false);
        }
    }

    private Closure getCriteriaClosureForSection(Map params) {
        def session = AppUtil.session;
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.name) {
                ilike("name", "%${params.name.trim().encodeAsLikeText()}%")
            }
            if (params.createdFrom) {
                Date date = params.createdFrom.dayStart.gmt(session.timezone);
                ge("created", date);
            }
            if (params.createdTo) {
                Date date = params.createdTo.dayEnd.gmt(session.timezone);
                le("created", date);
            }
            if (params.updatedFrom) {
                Date date = params.updatedFrom.dayStart.gmt(session.timezone);
                ge("updated", date);
            }
            if (params.updatedTo) {
                Date date = params.updatedTo.dayEnd.gmt(session.timezone);
                le("updated", date);
            }
        }
    }

    private Closure getCriteriaClosureForTrash(Map params) {
        def session = AppUtil.session;
        Closure closure = {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.deletedFrom) {
                Date date = params.deletedFrom.dayStart.gmt(session.timezone);
                ge("updated", date);
            }
            if (params.deletedTo) {
                Date date = params.deletedTo.dayEnd.gmt(session.timezone);
                le("updated", date);
            }
            eq("isInTrash", true);
        }
        return closure;
    }

    Integer getArticlesCount(Map params) {
        return Article.createCriteria().count {
            and getCriteriaClosure(params)
        }
    }

    Integer getSectionCount(Map params) {
        return Section.createCriteria().count {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
        }
    }

    List<Article> getArticles(Map params) {
        def listMap = [offset: params.offset, max: params.max]
        return Article.createCriteria().list(listMap) {
            and getCriteriaClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc")
        }
    }

    List<Article> getArticlesForWidget(Map params) {
        def listMap = [offset: params.offset, max: params.max]
        return Article.createCriteria().list(listMap) {
            and getCriteriaClosureForWidget(params)
            order(params.sort ?: "name", params.dir ?: "asc")
        }
    }

    List<Section> getSections(Map params) {
        def listMap = [offset: params.offset, max: params.max]
        return Section.createCriteria().list(listMap) {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            order(params.sort ?: "name", params.dir ?: "asc")
        }
    }

    def checkArticleForConflict(String name, Long id) {
        Integer count = Article.createCriteria().count {
            if (id != 0) {
                ne("id", id)
            }
            eq("name", name)
        }
        if (count > 0) {
            throw new ApplicationRuntimeException("article.name.exists")
        }
    }

    def checkSectionForConflict(String name, Long id) {
        Integer count = Section.createCriteria().count {
            if (id != 0) {
                ne("id", id)
            }
            eq("name", name)
        }
        if (count > 0) {
            throw new ApplicationRuntimeException("provided.field.value.exists", [g.message(code: "name"), name])
        }
    }

    @Transactional
    Article saveArticle(Map params, Long admin) {
        Long id = params.id.toLong(0);
        checkArticleForConflict(params.name, id);
        Article article = id ? Article.get(id) : new Article()

        article.name = params.name.trim()
        article.url = commonService.getUrlForDomain(article)
        article.content = params.content
        article.summary = params.summary
        article.section = params.section != "" ? Section.proxy(params.section) : null
        article.isPublished = (params.isPublished == "true") ? true : false
        article.isDisposable = false
        if (id) {
            AppEventManager.fire("article-update", [id])
            article.merge();
            return article.hasErrors() ? null : article;
        } else {
            article.createdBy = Operator.proxy(admin);
            article.save();
            return article.hasErrors() ? null : article;
        }
    }

    @Transactional
    public boolean deleteTrashItemAndSaveCurrent(String name) {
        Article article = Article.createCriteria().get {
            eq("name", name)
        }
        if(article) {
            AppEventManager.fire("before-article-delete", [article.id])
            deleteArticle(article.id)
            AppEventManager.fire("article-delete", [article.id])
        }
    }

    @Transactional
    boolean saveSection(Map params, Long admin) {
        Long id = params.id.toLong(0);
        checkSectionForConflict(params.name, id);
        Map properties = [
                name: params.name,
                parent: null
        ];
        if (params.parent != "") {
            Long parent = params.parent.toLong(0);
            if (id) {
                commonService.checkCircularReferenceOfParent(Section, id, parent)
            }
            properties.parent = Section.proxy(parent);
        }
        if (id) {
            return Section.where {
                id == id
            }.updateAll(properties) > 0
        } else {
            properties.createdBy = Operator.proxy(admin)
            Section section = new Section(properties).save()
            return !section.hasErrors()
        }
    }

    @Transactional
    boolean copyArticle(Long id, Long admin) {
        Article article = Article.get(id)
        Article copiedArticle = new Article()
        copiedArticle.name = commonService.getCopyNameForDomain(article)
        copiedArticle.url = commonService.getUrlForDomain(copiedArticle)
        copiedArticle.content = article.content
        copiedArticle.summary = article.summary
        copiedArticle.section = article.section
        copiedArticle.createdBy = Operator.proxy(admin)
        copiedArticle.isPublished = article.isPublished
        copiedArticle = copiedArticle.save()
        return !copiedArticle.hasErrors()
    }

    @Transactional
    boolean deleteArticle(Long id) {
        return Article.where {
            id == id
        }.deleteAll() > 0
    }

    @Transactional
    boolean copySelectedArticles(List<Long> ids, Long admin) {
        Operator user = Operator.get(admin);
        boolean copied = true;
        ids.each {
            Article article = Article.get(it)
            Article copiedArticle = new Article();
            copiedArticle.name = commonService.getCopyNameForDomain(article)
            copiedArticle.url = commonService.getUrlForDomain(copiedArticle)
            copiedArticle.content = article.content
            copiedArticle.summary = article.summary
            copiedArticle.section = article.section
            copiedArticle.createdBy = user
            copiedArticle = copiedArticle.save()
            copied &= !copiedArticle.hasErrors()
        }
        return copied;
    }

    @Transactional
    boolean deleteSelectedSections(List<Long> ids) {
        List<Long> deletedIds = []
        try {
            ids.each {
                if (it in deletedIds) {
                    return
                }
                Section section = Section.proxy(it)
                if (section) {
                    List<Long> dIds = deleteSectionWithChildren(section);
                    deletedIds.addAll(dIds);
                } else {
                    return false;
                }
            }
        } catch (Throwable t) {
            return false;
        }
        return true;
    }

    @Transactional
    List<Long> deleteSectionWithChildren(Section section) {
        List<Long> ids = []
        Section.where {
            parent == section
        }.list().each {
            ids.addAll(deleteSectionWithChildren(it))
        }
        Article.where {
            section == section
        }.deleteAll();

        ids.add(section.id);
        section.delete()
        return ids;
    }

    @Transactional
    boolean deleteSection(Long id, String at2_reply, String at1_reply) {
        TrashUtil.preProcessFinalDelete("section", id, at2_reply != null, at1_reply != null)
        AppEventManager.fire("before-section-delete", [id, at1_reply])
        Section sectionObj = Section.proxy(id)
        sectionObj.delete()
        AppEventManager.fire("section-delete")
        return true
    }

    Integer getChildSectionCount(Long id, Map params) {
        return Section.createCriteria().count {
            if (id == 0) {
                isNull("parent")
            } else {
                eq("parent.id", id)
            }
            and getCriteriaClosureForSection(params)
        }
    }

    Integer getChildArticleCount(Long id, Map params) {
        return Article.createCriteria().count {
            if (id == 0) {
                isNull("section")
            } else {
                eq("section.id", id)
            }
            and getCriteriaClosure(params)
        }
    }

    List<Section> getSections(Map params, Map listMap) {
        Long id = params.id.toLong(0);
        return Section.createCriteria().list(listMap) {
            if (id == 0) {
                isNull("parent")
            } else {
                eq("parent.id", id)
            }
            and getCriteriaClosureForSection(params);
        }
    }

    List<Article> getArticleForExplorer(Map params, Map listMap) {
        Long id = params.id.toLong(0);
        return Article.createCriteria().list(listMap) {
            if (id == 0) {
                isNull("section")
            } else {
                eq("section.id", id)
            }
            and getCriteriaClosure(params)
        }
    }

    public getChildSections(Long parent = null) {
        return Section.where {
            if (parent) {
                parent.id == parent
            } else {
                parent == null
            }
        }.list().collect {
            return [id: it.id, name: it.name, parent: parent, hasChild: Section.countByParent(Section.proxy(it.id)) > 0]
        }
    }

    static {
        HookManager.register("article-put-trash-at2-count") { response, id ->
            int widgetCount = WidgetContent.createCriteria().get {
                projections {
                    countDistinct("w.containerId")
                }
                eq("contentId", id)
                eq("type", DomainConstants.WIDGET_CONTENT_TYPE.ARTICLE)
                createAlias("widget", "w", JoinType.INNER_JOIN)
                eq("w.containerType", "page")
            };
            if(widgetCount) {
                response.pages = widgetCount
            }
            widgetCount = WidgetContent.createCriteria().get {
                projections {
                    countDistinct("w.containerId")
                }
                eq("contentId", id)
                eq("type", DomainConstants.WIDGET_CONTENT_TYPE.ARTICLE)
                createAlias("widget", "w", JoinType.INNER_JOIN)
                eq("w.containerType", "layout")
            };
            if(widgetCount) {
                response.layouts = widgetCount
            }
            return response;
        }
        HookManager.register("article-put-trash-at2-list") { response, id ->
            List<Long> pageIds = WidgetContent.createCriteria().list {
                projections {
                    distinct("w.containerId")
                }
                eq("contentId", id)
                eq("type", DomainConstants.WIDGET_CONTENT_TYPE.ARTICLE)
                createAlias("widget", "w", JoinType.INNER_JOIN)
                eq("w.containerType", "page")
            };
            if(pageIds.size()) {
                response.pages = Page.createCriteria().list {
                    projections {
                        property("name")
                    }
                    inList("id", pageIds)
                }
            }
            List<Long> layoutIds = WidgetContent.createCriteria().list {
                projections {
                    distinct("w.containerId")
                }
                eq("contentId", id)
                eq("type", DomainConstants.WIDGET_CONTENT_TYPE.ARTICLE)
                createAlias("widget", "w", JoinType.INNER_JOIN)
                eq("w.containerType", "layout")
            };
            if(layoutIds.size()) {
                response.layouts = Layout.createCriteria().list {
                    projections {
                        property("name")
                    }
                    inList("id", layoutIds)
                }
            }
            return response;
        }
        AppEventManager.on("before-article-delete", { id ->
            WidgetContent.where {
                contentId == id
                type == DomainConstants.WIDGET_CONTENT_TYPE.ARTICLE
            }.deleteAll()
        })
    }

    @Transactional
    public boolean putArticleInTrash(Long id, String at2_reply, String at1_reply) {
        TrashUtil.preProcessPutInTrash("article", id, at2_reply != null, at1_reply != null)
        Article article = Article.get(id);
        return trashService.putObjectInTrash("article", article, at1_reply)
    }

    @Transactional
    public boolean putArticleInTrash(Long id, boolean force = false) {
        if(force) {
            return putArticleInTrash(id, "yes", "with_children");
        } else {
            return putArticleInTrash(id, null, null);
        }
    }

    @Transactional
    int putSelectedArticlesInTrash(List<String> ids) {
        int removeCount = 0;
        ids.each { id ->
            try {
                if(putArticleInTrash(id, true)) {
                    removeCount++;
                }
            } catch(AttachmentExistanceException att) {
            }
        }
        return removeCount
    }

    public Long countArticlesInTrash() {
        return Article.createCriteria().count {
            eq("isInTrash", true)
        }
    }

    public Map getArticlesInTrash(int offset, int max, String sort, String dir) {
        return [Article: Article.createCriteria().list(offset: offset, max: max) {
            eq("isInTrash", true)
            order(sort ?: "name", dir ?: "asc")
        }.collect {
            [id: it.id, name: it.name, updated: it.updated]
        }]
    }

    public Long countArticlesInTrash(Map params) {
        return Article.createCriteria().count {
            and getCriteriaClosureForTrash(params)
        }
    }

    public Map getArticlesInTrash(Map params) {
        def listMap = [offset: params.offset, max: params.max];
        return [Article: Article.createCriteria().list(listMap) {
            and getCriteriaClosureForTrash(params)
            order(params.sort ?: "name", params.dir ?: "asc")
        }.collect {
            [id: it.id, name: it.name, updated: it.updated]
        }]
    }

    @Transactional
    public boolean restoreArticleFromTrash(Long id) {
        Article article = Article.get(id);
        return trashService.restoreObjectFromTrash(article)
    }

    @Transactional
    public Long restoreArticleFromTrash(def field, def value) {
        Article article = Article.createCriteria().get {
            eq(field, value)
        }
        article.isInTrash = false;
        article.merge();
        return article.id;
    }

    def getArticlesInOrder(def articleIds) {
        Map emptyMap = articleIds.collectEntries { [(it): null] }
        List<Article> articles = articleIds.size() ? Article.createCriteria().list {
            inList("id", articleIds)
        } : [];
        articles.each {
            emptyMap[it.id] = it;
        }
        return emptyMap.values().findAll {it != null} as List;
    }

    def getEntitiesInPages(String likeText, Integer offset, Integer max) {
        return _getEntitiesInPages(likeText, false, offset, max)
    }

    def getEntitiesInPages(String likeText, Boolean isCount) {
        return _getEntitiesInPages(likeText, isCount, null, null)
    }

    private def _getEntitiesInPages(String likeText, Boolean isCount, Integer offset, Integer max) {
        likeText = likeText.encodeAsLikeText()
        Long cid = AppUtil.session.customer;
        String customerSql = cid ? ("or (P.visibility = '${DomainConstants.PAGE_VISIBILITY.RESTRICTED}' and (exists (select C.id from P.customers C where $cid = C.id) or " +
                "exists (select G.id from P.customerGroups G where exists (select GC.id from G.customers GC where $cid = GC.id))))") : ""
        String pageAvailabilitySql = "(select P.id from Page P where P.id = WC.widget.containerId and (P.visibility = " +
                "'${DomainConstants.PAGE_VISIBILITY.OPEN}' $customerSql))"
        String customerForLayoutSql = cid ? ("or (LP.visibility = '${DomainConstants.PAGE_VISIBILITY.RESTRICTED}' and (exists (select LPC.id from LP.customers LPC where $cid = LPC.id) or exists " +
                "(select LPG" + ".id from LP.customerGroups LPG where exists (select LPGC.id from LPG.customers LPGC where $cid = LPGC.id))))") : ""
        String layoutForPageAvailabilitySql = "(select LP.id from Page LP where LP.layout.id = WC.widget.containerId and (LP.visibility = " +
                "'${DomainConstants.PAGE_VISIBILITY.OPEN}' $customerForLayoutSql))"
        String layoutForAutoPageAvailabilitySql = "(select LAP.id from AutoGeneratedPage LAP where LAP.layout.id = WC.widget.containerId)"
        String projection = isCount ? "count(A)" : "A"
        if(max == -1) {
            max = Integer.MAX_VALUE
        }
        Map limit = isCount ? [:] : [offset: offset, max: max]
        def result = Article.executeQuery("select $projection from Article A where A.isPublished = 1 and A.isInTrash = 0 and A.isDisposable = 0 and A.name like '%$likeText%' and A.id in (select WC.contentId from " +
                "WidgetContent WC where WC.type = '${DomainConstants.WIDGET_CONTENT_TYPE.ARTICLE}' and (exists $pageAvailabilitySql or exists $layoutForPageAvailabilitySql or " +
                "exists $layoutForAutoPageAvailabilitySql))", limit)
        return isCount ? result[0] : result
    }
}