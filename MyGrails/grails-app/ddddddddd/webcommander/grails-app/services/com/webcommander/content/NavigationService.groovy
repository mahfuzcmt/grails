package com.webcommander.content

import com.webcommander.Page
import com.webcommander.SiteTagLib
import com.webcommander.admin.TrashService
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.installation.template.TemplateContent
import com.webcommander.manager.HookManager
import com.webcommander.manager.PathManager
import com.webcommander.tenant.TenantContext
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.TrashUtil
import com.webcommander.webcommerce.*
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@Initializable
class NavigationService {
    static void initialize() {
        AppEventManager.on("before-template-install", {
            List<TemplateContent> contents = TemplateContent.createCriteria().list {
                eq("contentType", DomainConstants.WIDGET_CONTENT_TYPE.NAVIGATION)
            }
            if(contents) {
                Navigation.where {
                    inList("id", contents.contentId)
                }.updateAll(isDisposable: true)
                contents*.delete()
            }
        })

        HookManager.register("navigation-put-trash-at2-count", { response, id ->
            Navigation navigation = Navigation.proxy(id)
            int navigationItemCount = navigation.items.size()
            if(navigationItemCount) {
                response."navigation.item" = navigationItemCount
            }
            return response
        })
        HookManager.register("navigation-put-trash-at2-list", { response, id ->
            List navigationItem = NavigationItem.createCriteria().list {
                projections {
                    property("label")
                }
                navigation {
                    eq("id", id)
                }
            }
            if(navigationItem.size()) {
                response."navigation.item" = navigationItem
            }
            return response
        })
        AppEventManager.on("before-navigation-delete", { id ->
            NavigationItem.createCriteria().list {
                navigation {
                    eq("id", id)
                }
            }.each {
                it.parent = null
                it.delete()
            }
        })
    }

    static  {
        AppEventManager.on("navigation-update", { id ->
            Navigation navigation = Navigation.get(id)
            navigation.isDisposable = false
            navigation.save()
        })
    }

    private static Map _DYNAMIC_CONSTANT_HOLDER = [:]

    private static Map getDYNAMIC_CONSTANT() {
        Map dynamic = _DYNAMIC_CONSTANT_HOLDER[TenantContext.currentTenant]
        if(!dynamic) {
            dynamic = _DYNAMIC_CONSTANT_HOLDER[TenantContext.currentTenant] = [:].withDefault { key ->
                if(key == "auto_population_item_type") {
                    return []
                } else {
                    return [:]
                }
            }
        }
        return dynamic
    }

    static addConstant(List list) {
        list.each { config ->
            def constant = DYNAMIC_CONSTANT[config.constant]
            if(constant instanceof Map) {
                constant.put(config.key, config.value)
            } else {
                constant.add(config.key)
            }
        }
    }

    static removeConstant(List list) {
        list.each { config ->
            DYNAMIC_CONSTANT[config.constant].remove(config.key)
        }
    }

    public def static getDomains(){
       return  [
                (DomainConstants.NAVIGATION_ITEM_TYPE.PAGE): "pageService",
                (DomainConstants.NAVIGATION_ITEM_TYPE.PRODUCT): "productService",
                (DomainConstants.NAVIGATION_ITEM_TYPE.CATEGORY): "categoryService",
        ].with {it + getDYNAMIC_CONSTANT().domains}
    }

    public static Map getDomains_filer_params() {
        return [
                (DomainConstants.NAVIGATION_ITEM_TYPE.PRODUCT): [parent: "all"],
                (DomainConstants.NAVIGATION_ITEM_TYPE.CATEGORY): [:],
        ].with {it + getDYNAMIC_CONSTANT().domains_filer_params}
    }
    public static List getAuto_population_item_type() {
        return [
                (DomainConstants.NAVIGATION_ITEM_TYPE.PAGE),
                (DomainConstants.NAVIGATION_ITEM_TYPE.CATEGORY)
        ].with {it + getDYNAMIC_CONSTANT().auto_population_item_type}
    }

    TrashService trashService;
    CommonService commonService
    PageService pageService
    CategoryService categoryService
    ProductService productService
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g
    @Autowired
    @Qualifier("com.webcommander.SiteTagLib")
    SiteTagLib site
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    com.webcommander.ApplicationTagLib app;

    private Closure getCriteriaClosure(Map params) {
        def session = AppUtil.session;
        Closure closure = {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
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

    List<Navigation> getNavigations (Map params) {
        def listMap = [max: params.max, offset: params.offset];
        Closure closure = getCriteriaClosure(params);
        return Navigation.createCriteria().list(listMap) {
            and closure
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }

    List<Navigation> getNavigatiosForWidget() {
        return Navigation.createCriteria().list {
            eq("isInTrash", false)
            eq("isDisposable", false)
        }
    }

    Integer getNavigationCount (Map params) {
        Closure closure = getCriteriaClosure(params);
        return Navigation.createCriteria().get {
            projections {
                and closure;
                rowCount();
            }
        }
    }

    List<NavigationItem> getNavigationItems (Long id) {
        List items = NavigationItem.createCriteria().list {
            eq("navigation.id", id)
            order("idx", "asc")
        }

    }

    List<Integer> getNavigationIds(long pageId){
        def navigationItems = NavigationItem.createCriteria().list{
            eq("itemRef", pageId.toString())
        }
        def navigationIds = []
        navigationItems.each {
            navigationIds.add(it.navigation.id)
        }
        return navigationIds
    }

    public List<NavigationItem> populateAvailableNavigationItemsChildList(Long id, List<NavigationItem> items) {
        Navigation navigation = Navigation.get(id)
        Long customerId = AppUtil.session.customer
        List<NavigationItem> roots = [];
        items.each { item ->
            item.childItems = [] //may be list is generated earlier, so should be cleared
        }
        items.each { item ->
            if(navigation.hideRestrictedItem && !isPermitted(item, customerId)) {
                return;
            }
            if(item.parent) {
                item.parent.childItems.add(item);
            } else {
                roots.add(item);
            }
        }
        return roots;
    }

    public List<NavigationItem> populateNavigationItemsChildList(def items) {
        List<NavigationItem> roots = [];
        items.each { item ->
            item.childItems = [] //may be list is generated earlier, so should be cleared
        }
        items.each { item ->
            if(item.parent) {
                item.parent.childItems.add(item);
            } else {
                roots.add(item);
            }
        }
        return roots;
    }

    public String getUrl(type, ref) {
        switch (type) {
            case null :
            case "" :
                return ""
            case "page" :
                Page page = Page.get(ref.toLong())
                return page ? app.relativeBaseUrl() + page.url : ""
            case "product" :
                Product product = Product.get(ref.toLong())
                return product ? app.relativeBaseUrl() + "product/" + product.url : ""
            case "category" :
                Category category = Category.get(ref.toLong())
                return category ? app.relativeBaseUrl() + "category/" + category.url : ""
            case "url" :
                if(ref.startsWith("/")) {
                    ref = app.relativeBaseUrl() + ref.substring(1);
                }
                return ref;
            case "email":
                return "mailto:" + ref;
            case "autoGeneratedPage":
                switch(ref) {
                    case 'login':
                        if(AppUtil.session.customer) {
                            return app.relativeBaseUrl() + 'customer/logout';
                        } else {
                            return app.relativeBaseUrl() + 'customer/login';
                        }
                        break;
                    case 'register':
                        return app.relativeBaseUrl() + 'customer/register';
                        break;
                    case 'profile':
                        return app.relativeBaseUrl() + 'customer/profile';
                        break;
                    case 'cart':
                        return app.relativeBaseUrl() + 'cart/details';
                        break;
                    case 'checkout':
                        return app.relativeBaseUrl() + 'shop/checkout';
                }
            default:
                def consumer = Holders.applicationContext.getBean(domains[type])
                String domain = type.capitalize()
                return consumer.getClass().getDeclaredMethod("getNavigationLinkFor" + domain, [String.class] as Class[]).invoke(consumer, [ref] as Object[])
        }
    }

    public Closure getAnchorTag(NavigationItem item) {
        return { navigationDom ->
            String label = item.label
            StringWriter linkDom = new StringWriter();
            linkDom << "<a target='" + item.target + "' class='" + item.itemType + "' href='";
            String ref = item.itemRef
            String type = item.itemType
            linkDom << getUrl(type, ref)
            if(type == "autoGeneratedPage" && ref == "login") {
                String[] labels = label.split("<>");
                if(AppUtil.session.customer) {
                    label = labels.size() > 1 ? labels[1] : g.message(code: "logout");
                } else {
                    label = labels[0];
                }
            }
            linkDom <<  "'>"
            linkDom << navigationDom
            linkDom << "<span class='label'>${site.message(code: label)}</span></a>"
            return linkDom.toString();
        }
    }

    public isActive(String type, String ref) {
        def request = AppUtil.request
        switch(type) {
            case "page":
                return "" + request.page?.id == ref;
            case "product":
                return "" + request.product?.id == ref;
            case "category":
                return "" + request.category?.id == ref;
            case "autoGeneratedPage":
                if(request.isAutoPage == true) {
                    switch(ref) {
                        case 'login':
                            return request.page.name == DomainConstants.AUTO_GENERATED_PAGES.CUSTOMER_LOGIN;
                        case 'register':
                            return request.page.name == DomainConstants.AUTO_GENERATED_PAGES.CUSTOMER_REGISTRATION
                        case 'cart':
                            return request.page.name == DomainConstants.AUTO_GENERATED_PAGES.CART_PAGE
                        case 'checkout':
                            return request.page.name == DomainConstants.AUTO_GENERATED_PAGES.CHECKOUT
                    }
                }
                break
            case "url":
                break;
            case "email":
                break;
            default:
                if(domains[type]) {
                    def consumer = Holders.applicationContext.getBean(domains[type])
                    return consumer.getClass().getDeclaredMethod("isActive", [String.class] as Class[]).invoke(consumer, [ref] as Object[])
                }
        }
        return false;
    }

    @Transactional
    def save(Map params) {
        Long id = params.id.toLong(0)
        Navigation navigation = id ? Navigation.get(id) : new Navigation();
        navigation.name = navigation.name == "Main Menu" ? "Main Menu" : params.name;
        navigation.hideRestrictedItem = params.restrictedItem == "hide"
        if(!commonService.isUnique(navigation, "name")){
            throw new ApplicationRuntimeException("navigation.name.exists")
        }
        navigation.save()
        if (navigation.hasErrors()) {
            return null
        }
        if (id) AppEventManager.fire("navigation-update", [navigation.id])
        return navigation
    }

    @Transactional
    def deleteNavigation(Long id) {
        Navigation navigation = Navigation.get(id);
        navigation.items.each {
            deleteNavigationImage(it)
        }
        navigation.delete(flush: true);
        return true;
    }

    def isNavigationHidden(Long navigationId) {
        return Navigation.get(navigationId).hideRestrictedItem
    }

    @Transactional
    def deleteNavigationImage(NavigationItem navigationItem) {
        def filePath = PathManager.getResourceRoot("navigation-items");
        if(navigationItem.image) {
            File file = new File(filePath + "/navigation-item-" + navigationItem.id + "-" + navigationItem.image)
            file.delete();
        }
    }

    @Transactional
    def saveItems(List updatedItems, List removedItems, Long navigationId) {
        Map negativeIdCache = [:]
        Navigation navigation = Navigation.get(navigationId)
        updatedItems.each { it ->
            Long id = it.id.toLong();
            NavigationItem navigationItem = id > 0 ? NavigationItem.get(id) : new NavigationItem(navigation: navigation);
            if(it.parent) {
                Long parent = it.parent.toLong();
                if(parent < 0) {
                    navigationItem.parent = NavigationItem.get(negativeIdCache["" + parent]);
                } else {
                    navigationItem.parent = NavigationItem.get(parent);
                }
            } else {
                navigationItem.parent = null
            }
            navigationItem.idx =  it.placement.toInteger();
            navigation.addToItems(navigationItem);
            if(it.update_cache) {
                navigationItem.label = it.update_cache.label;
                navigationItem.itemType = it.update_cache.itemType;
                navigationItem.target = it.update_cache.target;
                navigationItem.itemRef = it.update_cache.itemRef;
                navigationItem.imageAlt = it.update_cache.imageAlt;
            }
            navigationItem.save()
            if(navigationItem.hasErrors()){
                throw new ApplicationRuntimeException("navigation.save.failed");
            }
            if (id < 0) {
                negativeIdCache["" + id] = navigationItem.id;
            }
        }
        if(removedItems.size() > 0) {
            removedItems.each {
                navigation.removeFromItems(NavigationItem.load(it))
            }
            removeNavigationItems(removedItems)
        }
        AppEventManager.fire("navigation-update", [navigation.id])
        return negativeIdCache;
    }

    @Transactional
    def removeNavigationItems(List<Long> ids) {
        NavigationItem.createCriteria().list {
            inList("parent.id", ids)
        }.collect {
            it.parent = null;
            return it
        }*.merge();
        ids.each {
            NavigationItem navigationItem = NavigationItem.get(it);
            navigationItem.delete();
            if(navigationItem.hasErrors()) {
                throw new ApplicationRuntimeException("navigation.save.failed");
            }
        }
    }

    @Transactional
    def putNavigationInTrash(Long id, String at2_reply, String at1_reply){
        TrashUtil.preProcessPutInTrash("navigation", id, at2_reply != null, at1_reply != null)
        Navigation navigation = Navigation.proxy(id)
        if(navigation.name == "Main Menu") {
            return false
        }
        return trashService.putObjectInTrash("navigation", navigation, at1_reply);
    }

    @Transactional
    def putSelectedNavigationsInTrash(List<String> ids) {
        boolean deleted = true;
        ids.each {
            deleted = putNavigationInTrash(it.toLong(), "yes", "include")
            if(!deleted){
                return false;
            }
        }
        return deleted;
    }

    public Long countNavigationsInTrash() {
        return Navigation.createCriteria().count {
            eq("isInTrash", true)
        }
    }

    public Map getNavigationsInTrash(int offset, int max, String sort, String dir) {
        return [Navigation: Navigation.createCriteria().list(offset: offset, max: max) {
            eq("isInTrash", true)
            order(sort?:"name", dir?:"asc")
        }.collect {
            [id: it.id, name: it.name, updated: it.updated]
        }]
    }

    public Long countNavigationsInTrash(Map params){
        return Navigation.createCriteria().count {
            and getCriteriaClosureForTrash(params)
        }
    }

    public Map getNavigationsInTrash(Map params) {
        def listMap = [ offset: params.offset, max: params.max];
        return [Navigation: Navigation.createCriteria().list(listMap) {
            and getCriteriaClosureForTrash(params)
            order(params.sort?:"name", params.dir?:"asc")
        }.collect{
            [id: it.id, name: it.name, updated: it.updated]
        }]
    }

    @Transactional
    public boolean restoreNavigationFromTrash(Long id) {
        Navigation navigation = Navigation.get(id);
        if (!navigation) {
            return false;
        }
        navigation.isInTrash = false;
        navigation.merge();
        return !navigation.hasErrors();
    }

    @Transactional
    public updateItemImage (Long id, String image) {
        return NavigationItem.where {
            id == id
        }.updateAll([
                image : image
        ]) > 0
    }

    @Transactional
    public Long restoreNavigationFromTrash(String field,String value){
        Navigation navigation = Navigation.createCriteria().get{
            eq(field, value);
        }
        navigation.isInTrash = false;
        navigation.merge();
        return navigation.id;
    }

    @Transactional
    public boolean deleteTrashItemAndSaveCurrent(def field, def value){
        Navigation navigation = Navigation.createCriteria().get{
            eq(field, value)
        }
        deleteNavigation(navigation.id);
        return !navigation.hasErrors();
    }

    private List<NavigationItem> sortBasedOnIndex(Map parentBasedStorage) {
        def ultimateList = [];
        int indexCounter = 0;
        Closure addChilds;
        def currentParent;
        addChilds = { child ->
            if(currentParent) {
                child.parent = currentParent;
            }
            child.idx = indexCounter++;
            ultimateList.add(child);
            List childList = parentBasedStorage["" + child.itemRef]
            if(childList) {
                def cacheParent = currentParent;
                currentParent = child;
                childList.each addChilds
                currentParent = cacheParent;
            }
        }
        parentBasedStorage["0"].each addChilds

        return ultimateList;
    }

    Boolean isPermitted(NavigationItem item, Long customerId) {
        Boolean isPermitted = true
        switch (item.itemType) {
            case DomainConstants.NAVIGATION_ITEM_TYPE.PAGE:
                isPermitted = pageService.isPermitted(item.itemRef.toLong(0), customerId)
                break
            case DomainConstants.NAVIGATION_ITEM_TYPE.PRODUCT:
                isPermitted = productService.isPermitted(item.itemRef.toLong(0), customerId)
                break
            case DomainConstants.NAVIGATION_ITEM_TYPE.CATEGORY:
                isPermitted = categoryService.isPermitted(item.itemRef.toLong(0), customerId)
                break
        }
        return isPermitted
    }

    public def getNavigationDom(Long navigationId, Map params) {
        def navigationItems = getNavigationItems(navigationId)
        def items = populateNavigationItemsChildList(navigationItems);
        def itemSize = items.size();
        StringWriter tempOut = new StringWriter();
        if (itemSize) {
            tempOut << "<div class='" + (params.orientation == "V" ? "vertical" : "horizontal") + (params.showImage == "show" ? " with-image" : "") + "'>";
            Closure recursiveRenderer;
            recursiveRenderer = { v, i, isActive ->
                def subNav = v.childItems;
                def clazz = "navigation-item-${i} navigation-item";
                if (i == 0) {
                    clazz += " first";
                }
                Closure lidom = getAnchorTag(v)
                boolean selected = this.isActive(v.itemType, v.itemRef);
                if (selected) {
                    clazz += " active";
                    isActive.active = true;
                }
                if (i == itemSize - 1) {
                    clazz += " last";
                }
                if (subNav.size()) {
                    clazz += " has-child";
                    StringWriter cacheOut = tempOut;
                    tempOut = new StringWriter();
                    Map childActive = [:]
                    subNav.eachWithIndex { v2, i2 ->
                        recursiveRenderer(v2, i2, childActive)
                    }
                    if (childActive.active) {
                        clazz += " child-active active";
                        isActive.active = true;
                    }
                    clazz = " class='" + clazz + "'";
                    cacheOut << "<div${clazz}>";
                    def imageDom = new StringWriter();
                    if (params.showImage == "show") {
                        imageDom << "<span class='image-wrapper " + (v.image ? "" : "no-image") + "'>";
                        if (v.image) {
                            def imageAlt =  v.imageAlt ?:v.image
                            imageDom << "<img src='" + appResource.getNavigationItemImageURL(navigationItem: v) + "' alt='" + imageAlt  + "' >"
                        }
                        imageDom << "</span>";
                    }
                    cacheOut << lidom(imageDom);
                    cacheOut << "<div class='navigation-item-child-container" + (childActive.active ? ' active' : '') + "'>";
                    cacheOut << tempOut.toString();
                    cacheOut << "</div>"
                    cacheOut << "</div>";
                    tempOut = cacheOut;
                } else {
                    clazz = " class='" + clazz + "'";
                    tempOut << "<div${clazz}>";
                    def imageDom = new StringWriter();
                    if (params.showImage == "show") {
                        imageDom << "<span class='image-wrapper " + (v.image ? "" : "no-image") + "'>";
                        if (v.image) {
                            def imageAlt =  v.imageAlt ?:v.image
                            imageDom << "<img src='" + appResource.getNavigationItemImageURL(navigationItem: v) + "'  alt='" + imageAlt + "'  >"
                        }
                        imageDom << "</span>";
                    }
                    tempOut << lidom(imageDom);
                    tempOut << "</div>"
                }
            }
            items.eachWithIndex { v, i ->
                recursiveRenderer(v, i, [:])
            }
            tempOut << "</div>"
        }
        return tempOut.toString();
    }

    public saveRestrictedItemOption(def params) {
        List<Long> ids = params.list("id")*.toLong();
        int count = 0;
        ids.each {
            Navigation navigation = Navigation.get(it);
            navigation.hideRestrictedItem = params.restrictedItem == "hide";
            navigation.save(flush: true)
            if(!navigation.hasErrors()) {
                count++
            }
        }
        return count;
    }

}
