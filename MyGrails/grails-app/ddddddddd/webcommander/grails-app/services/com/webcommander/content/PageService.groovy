package com.webcommander.content

import com.webcommander.Page
import com.webcommander.admin.*
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommonService
import com.webcommander.common.MetaTag
import com.webcommander.constants.DomainConstants
import com.webcommander.design.DockSection
import com.webcommander.design.Layout
import com.webcommander.design.WidgetService
import com.webcommander.events.AppEventManager
import com.webcommander.installation.template.TemplateContent
import com.webcommander.manager.HookManager
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import com.webcommander.util.StringUtil
import com.webcommander.util.TrashUtil
import com.webcommander.webcommerce.ProductService
import com.webcommander.widget.Widget
import grails.gorm.transactions.Transactional
import grails.web.databinding.DataBindingUtils
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@Initializable
@Transactional
class PageService {
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g
    CommonService commonService;
    WidgetService widgetService
    TrashService trashService
    ConfigService configService
    ProductService productService
    NavigationService navigationService

    static void initialize() {
        HookManager.register("layout-delete-veto") { response, id ->
            int layoutCount = Page.createCriteria().count {
                eq("layout.id", id)
            }
            if(layoutCount) {
                response.pages = layoutCount
            }
            return response;
        }
        HookManager.register("layout-delete-veto-list") { response, id ->
            List<Page> pages = Page.createCriteria().list {
                eq("layout.id", id)
            }
            if(pages.size()) {
                response.pages = pages.collect { it.name }
            }
            return response;
        }

        AppEventManager.on("before-customer-delete", { id ->
            Customer customer = Customer.proxy(id)
            Page.createCriteria().list {
                customers {
                    eq("id", id)
                }
            }.each {
                it.customers.remove(customer)
                it.merge()
            }
        })
        //Checking AT3 with news
        AppEventManager.on("before-operator-delete", { id ->
            Page.executeUpdate("update Page p set p.createdBy = null where p.createdBy.id = :uid", [uid: id]);
        })

        AppEventManager.on("before-template-install", {
            List<TemplateContent> contents = TemplateContent.createCriteria().list {
                eq("contentType", "page")
            }
            if(contents) {
                Page.where {
                    inList("id", contents.contentId)
                }.updateAll(isDisposable: true)
                contents*.delete()
            }
        })
    }

    static {
        AppEventManager.on("page-update", { id ->
            Page page = Page.get(id)
            page.isDisposable = false
            page.save()
        })
    }

    def checkNameValidity(String field, String value, Long id) {
        if (field == "url" && (value.toLowerCase() == "admin" || value.toLowerCase() == "pub")) {
            throw new ApplicationRuntimeException("forbidden.page.url");
        }
        if (!commonService.isUnique(Page, [id: id, field: field, value: value])) {
            throw new ApplicationRuntimeException("page.${field}.exists");
        }
    }

    def copy(Long id, Long admin) {
        Page page = Page.get(id);
        Page copiedPage = new Page();
        def oldPagePropertyMap = [name: page.name, title: page.title, url: page.url, visibility: page.visibility, visibleTo: page.visibleTo, body: page.body, js: page.js, css: page.css, isActive: page.isActive, isInTrash: page.isInTrash, isDisposable: page.isDisposable, disableGooglePageTracking: page.disableGooglePageTracking, createdBy: page.createdBy, layout: page.layout, metaTags: page.metaTags, customers: page.customers, customerGroups: page.customerGroups, headerWidgets: page.headerWidgets, footerWidgets: page.footerWidgets, dockableSections: page.dockableSections]
        DataBindingUtils.bindObjectToInstance(copiedPage, oldPagePropertyMap, [], ['headerWidgets', 'footerWidgets', 'dockableSections', 'metaTags', "createdBy", "createdById", "layoutId"], null);
        copiedPage.name = commonService.getCopyNameForDomain(page);
        copiedPage.url = commonService.getUrlForDomain(copiedPage);
        copiedPage.createdBy = Operator.proxy(admin);
        copiedPage.save();
        def widgetList = Widget.createCriteria().list {
            eq("containerId", id)
            eq("containerType", "page")
        }
        def docksWidget = page.dockableSections.widgets;
        def dockableWidgetMap = [:];
        widgetList.each {
            Widget widget = widgetService.copyWidget(it.id, copiedPage.id, it.containerType)
            if (page.headerWidgets.contains(it)) {
                copiedPage.addToHeaderWidgets(widget)
            } else if (page.footerWidgets.contains(it)) {
                copiedPage.addToFooterWidgets(widget)
            } else if (docksWidget.contains(it)) {
                dockableWidgetMap[it.id] = widget;
            } else {
                copiedPage.body = copiedPage.body.replaceFirst("uuid=\"" + it.uuid + "\"", "uuid=\"" + widget.uuid + "\"")
            }
        }
        page.metaTags.each {
            MetaTag mt = new MetaTag(it.properties)
            copiedPage.addToMetaTags(mt)
        }
        page.dockableSections.each {
            DockSection dockSection = new DockSection(uuid: StringUtil.uuid, css: it.css).save();
            it.widgets.each {
                dockSection.addToWidgets(dockableWidgetMap[it.id]);
            }
            copiedPage.addToDockableSections(dockSection);
        }
        return !copiedPage.hasErrors();
    }

    def copySelected(List<Long> ids, admin) {
        boolean saved = true;
        ids.each { id ->
            if (!copy(id, admin)) {
                saved = false;
                return false
            }
        }
        return saved;
    }

    def save(Map params, Long admin) {
        Long id = params.id.toLong(0);
        ["name", "url"].each {
            checkNameValidity(it, params[it], id)
        }
        Page page = id ? Page.get(id) : new Page();
        String oldUrl = page.url
        if(params.name == "admin") {
            throw new ApplicationRuntimeException("restricted.name.used")
        }
        page.name = params.name
        page.title = params.title
        page.isActive = params.active.toBoolean(true)
        page.isDisposable = false
        page.disableGooglePageTracking = params.disableTracking.toBoolean(false)
        page.visibility = params.visibility
        page.visibleTo = params.visibility == DomainConstants.PAGE_VISIBILITY.RESTRICTED ?  params.visibleTo : null;
        page.url = params.url ? params.url : commonService.getUrlForDomain(page);
        page.metaTags*.delete()
        page.metaTags = [];
        def tag_names = params.list("tag_name");
        def tag_values = params.list("tag_content");
        for (int i = 0; i < tag_names.size(); i++) {
            MetaTag metaTag = new MetaTag(name: tag_names[i], value: tag_values[i]);
            metaTag.save()
            page.addToMetaTags(metaTag).save()
        }
        page.customers = []
        page.customerGroups = []
        if(page.visibleTo == DomainConstants.PAGE_VISIBLE_TO.SELECTED) {
            if(params.customer) {
                def customerIds = params.list("customer")
                customerIds.each {  it ->
                    page.addToCustomers(Customer.proxy(it.toLong()))
                }
            }
            if(params.customerGroup) {
                def groupIds = params.list("customerGroup");
                groupIds.each {  it ->
                    page.addToCustomerGroups(CustomerGroup.proxy(it.toLong()))
                }
            }
        }
        if(params.layoutid) {
            page.layout = Layout.get(params.layoutid)
        } else {
            page.layout = null
        }
        if (id) {
            page.merge()
            if (!page.hasErrors()) {
                checkPageUrlForEdit(oldUrl, page.url);
                AppEventManager.fire("page-update", [page.id])

                if(params.isFrontEndForm){
                    return  page.id
                }else{
                    return true
                }
            }
        } else {
            page.createdBy = Operator.proxy(admin)
            page.save()
        }
        if (!page.hasErrors()) {
            params.list("linkedNavigations").each {
                Navigation navigation = Navigation.get(it)
                navigationService.saveItems([
                    [update_cache: [
                        label: page.name,
                        itemType: DomainConstants.NAVIGATION_ITEM_TYPE.PAGE,
                        target: "_self",
                        itemRef: page.id,
                        placement: navigation.items.size()
                    ]]
                ], [], navigation.id)
            }
            AppEventManager.fire("page-saved", [page.id])
            if(params.isFrontEndForm){
                return  page.id
            }else{
                return true
            }

        }
        return false
    }

    Boolean updateProperties(Long id, String field, def value) {
        Page page = Page.get(id);
        page."${field}" = value
        page.merge()
        return !page.hasErrors()
    }

    Page getPage(Long id) {
        return Page.get(id);
    }

    Page getPage(String name){
        Page page = Page.findByName(name)
        return page
    }

    def checkPageUrlForEdit(String oldUrl, String newUrl) {
        def checkPages = ["landing_page", "page404", "page403"]
        checkPages.each {
            if (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, it) == oldUrl && oldUrl != newUrl) {
                def config = [
                    [type: DomainConstants.SITE_CONFIG_TYPES.GENERAL, configKey: it, value: newUrl]
                ]
                configService.update(config);
            }
        }
    }

    private Closure getCriteriaClosure(Map params) {
        def session = AppUtil.session;
        Closure closure = {
            if (params.searchText) {
                or {
                    ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
                    ilike("title", "%${params.searchText.trim().encodeAsLikeText()}%")
                }
            }
            if (params.name) {
                ilike("name", "%${params.name.trim().encodeAsLikeText()}%")
            }
            if (params.title) {
                ilike("title", "%${params.title.trim().encodeAsLikeText()}%")
            }
            if (params.visibility) {
                eq("visibility", params.visibility)
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
            if (params.hasLayout) {
                isNotNull("layout")
            }
            if (params.isActive) {
                eq("isActive", params.isActive == "true")
            }
            if(params.isDisposable != "true") {
                eq("isDisposable", false)
            } else {
                eq("isDisposable", true)
            }
            eq("isInTrash", false);
        }
        return closure;
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

    def deletePage(Long id) {
        Page page = Page.proxy(id);
        try {
            page.delete();
        }catch (Throwable t) {
            return false;
        }
        return true;
    }

    public int getPagesCount(Map params) {
        Closure closure = getCriteriaClosure(params)
        return Page.createCriteria().count {
            and closure
        }
    }

    public List<Page> getPages(Map params) {
        Closure closure = getCriteriaClosure(params);
        def listMap = [max: params.max, offset: params.offset];
        return Page.createCriteria().list(listMap) {
            and closure
            order(params.sort ?: "name", params.dir ?: "asc")
        }
    }

    public Page getSitePage(long _id) {
        return Page.where {
            id == _id
            isInTrash == false
            isActive == true
            visibility != DomainConstants.PAGE_VISIBILITY.HIDDEN
        }.get();
    }

    List<Page> getSitePagesById(List ids){
        def listOfPages = []
        ids.each {
            listOfPages.add(getSitePage(it))
        }
        return listOfPages
    }

    public Page getSitePage(String _url) {
        return Page.where {
            url == _url
            isInTrash == false
            isActive == true
            visibility != DomainConstants.PAGE_VISIBILITY.HIDDEN
        }.get();
    }

    public Map getPagesAndUrls() {
        def pages = []
        def urls = []
        Page.createCriteria().list {
            eq("isInTrash", false)
            projections {
                property("name")
                property("url")
            }
        }.each {
            pages.add(it[0])
            urls.add(it[1])
        }
        return [pageNames: pages, pageUrls: urls];
    }

    public boolean isModified(Long pageId, Long etag) {
        Date lastDate = new Date(etag).gmt();
        String query = "select count(p) from Page p inner join p.layout l where p.id = :pid and (p.updated > :date or (select count(w) from Widget w where w.containerType = 'page' and w.containerId" +
                " = :pid and w.updated > :date) > 0 or (l is not null and (l.updated > :date or (select count(w) from Widget w where w.containerType = 'layout' and w.containerId = l.id and l.updated" +
                " > :date) > 0)))";
        return Page.executeQuery(query, [pid: pageId, date: lastDate]).get(0) > 0;
    }

    public boolean putPageInTrash(Long id, String at2_reply, String at1_reply) throws AttachmentExistanceException {
        TrashUtil.preProcessPutInTrash("page", id, at2_reply != null, at1_reply != null)
        Page page = Page.get(id);
        return trashService.putObjectInTrash("page", page, at1_reply)
    }

    def putSelectedPagesInTrash(List ids) {
        int removeCount = 0
        ids.each { id ->
            try{
                if(putPageInTrash(id, "yes", "yes")) {
                    removeCount ++ ;
                }
            }catch (AttachmentExistanceException att) {
            }
        }
        return removeCount;
    }

    public Long countPagesInTrash() {
        return Page.createCriteria().count {
            eq("isInTrash", true)
        }
    }

    public Long countPagesInTrash(Map params){
        return Page.createCriteria().count {
            and getCriteriaClosureForTrash(params)
        }
    }

    public Map getPagesInTrash(int offset, int max, String sort, String dir) {
        return [Page: Page.createCriteria().list(offset: offset, max: max) {
            eq("isInTrash", true)
            order(sort?:"name", dir?:"asc")
        }.collect {
            [id: it.id, name: it.name, updated: it.updated]
        }]
    }

    public Map getPagesInTrash(Map params) {
        def listMap = [ offset: params.offset, max: params.max];
        return [Page: Page.createCriteria().list(listMap) {
            and  getCriteriaClosureForTrash(params)
            order(params.sort?:"name", params.dir?:"asc")
        }.collect{
            [id: it.id, name: it.name, updated: it.updated]
        }]
    }

    public boolean restorePageFromTrash(Long id) {
        Page page = Page.get(id);
        if (!page) {
            return false
        }
        page.isInTrash = false
        page.merge()
        return !page.hasErrors()
    }

    public Long restorePageFromTrash(String field,String value){
        Page page = Page.createCriteria().get{
            eq(field, value)
        }
        page.isInTrash = false;
        page.merge();
        return page.id;
    }

    public boolean deleteTrashItemAndSaveCurrent(def field, def value){
        Page page = Page.createCriteria().get{
            eq(field, value)
        }
        deletePage(page.id);
        return !page.hasErrors();
    }

    public List<NavigationItem> generateNavigationItemsFromPages() {
        List<Page> pages = Page.createCriteria().list(){
            eq("isInTrash", false)
            eq("isDisposable", false)
            inList("visibility", [DomainConstants.PAGE_VISIBILITY.OPEN, DomainConstants.PAGE_VISIBILITY.RESTRICTED])
        }
        List<NavigationItem> items = []
        int idx = 0;
        pages.each{ page ->
            NavigationItem navigationItem = new NavigationItem();
            navigationItem.itemType = DomainConstants.NAVIGATION_ITEM_TYPE.PAGE
            navigationItem.itemRef = page.id
            navigationItem.label = page.name
            navigationItem.target = "_self"
            navigationItem.idx = idx++;
            items.add(navigationItem)
        }
        return items;
    }

    public List<Page> filterAvailablePage(Map params) {
        Long customerId = params.customerId
        return Page.createCriteria().list {
            cache(false)
            def page = Page
            eq("isInTrash", false)
            eq("isDisposable", false)
            or {
                if(params.excludeOpen != true) {
                    eq("visibility", DomainConstants.PAGE_VISIBILITY.OPEN )
                }
                eq("visibleTo", "all")
                inList("id", Page.where {
                    customers.id == customerId
                }.id())
                inList("id", Page.where {
                        customerGroups.id in CustomerGroup.where {
                            customers.id == customerId
                        }.id()
                    }.id()
                )
            }
        }
    }

    Boolean isPermitted(Long pageId, Long customerId) {
        def result = Page.createCriteria().get {
            def page = Page
            projections {
                property("id")
            }
            eq("isInTrash", false)
            eq("isDisposable", false)
            eq("id", pageId)
            or {
                eq("visibility", DomainConstants.PAGE_VISIBILITY.OPEN)
                if(customerId) {
                    eq("visibleTo", "all")
                    inList("id", Page.where {
                        customers.id == customerId
                    }.id())
                    inList("id", Page.where {
                            customerGroups.id in CustomerGroup.where {
                                 customers.id == customerId
                            }.id()
                        }.id()
                    )
                }

            }
        }
        return result != null
    }

    def changeStatus(List<Long> ids, Boolean isActive) {
        Integer count = 0;
        ids.each {
            Page page = Page.get(it)
            page.isActive = isActive
            count++;
        }
        return count;
    }
}
