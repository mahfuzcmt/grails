package com.webcommander.webcommerce

import com.webcommander.admin.Customer
import com.webcommander.admin.CustomerGroup
import com.webcommander.admin.Operator
import com.webcommander.admin.TrashService
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommonService
import com.webcommander.common.ImageService
import com.webcommander.common.MetaTag
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.content.NavigationItem
import com.webcommander.content.NavigationService
import com.webcommander.events.AppEventManager
import com.webcommander.installation.template.TemplateContent
import com.webcommander.manager.HookManager
import com.webcommander.manager.PathManager
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import com.webcommander.util.SortAndSearchUtil
import com.webcommander.util.TrashUtil
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import grails.util.TypeConvertingMap
import org.hibernate.SessionFactory
import org.springframework.web.multipart.MultipartFile

@Initializable
class CategoryService {
    CommonService commonService
    SessionFactory sessionFactory
    ImageService imageService
    TrashService trashService
    NavigationService navigationService
    ProductService productService

    public static void initialize() {
        AppEventManager.on("before-operator-delete", { id ->
            Category.executeUpdate("update Category c set c.createdBy = null where c.createdBy.id = :uid", [uid: id]);
        });

        HookManager.register("shippingProfile-delete-at2-count", { response, id ->
            int categoryCount = Category.createCriteria().count {
                eq("shippingProfile.id", id)
            }
            if (categoryCount) {
                response."category(s)" = categoryCount
            }
            return response
        });

        HookManager.register("shippingProfile-delete-at2-list", { response, id ->
            List categories = Category.createCriteria().list {
                projections {
                    property("name")
                }
                eq("shippingProfile.id", id)
            }
            if (categories.size()) {
                response."categories(s)" = categories
            }
            return response
        });

        AppEventManager.on("before-shippingProfile-delete", { id ->
            Category.createCriteria().list {
                eq("shippingProfile.id", id)
            }.each {
                it.shippingProfile = null
            }*.save();
        });

        AppEventManager.on("before-product-delete", { id ->
            Product product = Product.get(id)
            List<Category> categories = Category.createCriteria().list {
                products {
                    eq("id", id)
                }
            }
            categories.each { Category parent ->
                parent.lock()
                parent.refresh()
                parent.removeFromProducts(product)
                parent.merge()
            }
        });

        AppEventManager.on("before-customer-delete", { id ->
            Customer customer = Customer.proxy(id)
            Category.createCriteria().list {
                or {
                    inList("id", Category.where {
                        projections {
                            distinct("id")
                        }
                        availableToCustomers {
                            eq("id", id)
                        }
                    })
                    inList("id", Category.where {
                        projections {
                            distinct("id")
                        }
                        restrictPriceExceptCustomers {
                            eq("id", id)
                        }
                    })
                    inList("id", Category.where {
                        projections {
                            distinct("id")
                        }
                        restrictPurchaseExceptCustomers {
                            eq("id", id)
                        }
                    })

                }
            }.each {
                it.availableToCustomers.remove(customer)
                it.restrictPriceExceptCustomers.remove(customer)
                it.restrictPurchaseExceptCustomers.remove(customer)
                it.merge()
            }
        });

        AppEventManager.on("before-category-put-in-trash", { id, at1WithChildren ->
            if (at1WithChildren == "include") {
                Category.createCriteria().list {
                    eq("parent.id", id)
                }.each {
                    if (!it.isInTrash) {
                        it.isParentInTrash = true
                        it.isInTrash = true;
                        List dataList = [it.id, at1WithChildren]
                        AppEventManager.fire("before-category-put-in-trash", dataList)
                        it.isInTrash = true;
                        it.merge();
                        AppEventManager.fire("category-put-in-trash", dataList)
                    } else {
                        it.isParentInTrash = true
                        it.merge()
                    }
                }
            } else {
                Category.createCriteria().list {
                    eq("parent.id", id)
                }.each {
                    it.parent = null
                    it.merge()
                    AppEventManager.fire("category-update", [it.id])
                }
            }
        })

        AppEventManager.on("before-category-delete") { id ->
            Category.createCriteria().list {
                eq("parent.id", id)
            }.each {
                if (it.isInTrash) {
                    it.parent = null;
                    it.isParentInTrash = false;
                    it.merge();
                } else {
                    AppEventManager.fire("before-category-delete", [it.id])
                    it.delete()
                    AppEventManager.fire("category-delete", [it.id])
                }
            }
        }

        AppEventManager.on("category-restore") { id ->
            Category.createCriteria().list {
                eq("parent.id", id)
            }.each {
                it.isParentInTrash = false
                if (!it.isInTrash) {
                    AppEventManager.fire("category-restore", [it.id])
                }
                it.merge()
            }
        }


        HookManager.register("category-put-trash-at1-count") { response, id ->
            int categoryCount = Category.createCriteria().count() {
                eq("parent.id", id)
                eq("isInTrash", false)
            }
            if (categoryCount) {
                response.categories = categoryCount
            }
            return response;
        }

        HookManager.register("category-put-trash-at1-list") { response, id ->
            List categories = Category.createCriteria().list {
                projections {
                    property("name")
                }
                eq("parent.id", id)
                eq("isInTrash", false)
            }
            if (categories.size()) {
                response.categories = categories
            }
            return response;
        }

        HookManager.register("taxProfile-delete-at2-count") { response, id ->
            int categoryCount = Category.where {
                taxProfile.id == id
            }.count()
            if (categoryCount) {
                response.categories = categoryCount
            }
            return response;
        }

        HookManager.register("taxProfile-delete-at2-list") { response, id ->
            List categories = Category.createCriteria().list {
                projections {
                    property("name")
                }
                eq("taxProfile.id", id)
            }
            if (categories.size()) {
                response.categories = categories
            }
            return response;
        }

        AppEventManager.on("before-taxProfile-delete", { id ->
            TaxProfile profile = TaxProfile.proxy(id);
            Category.where {
                taxProfile == profile
            }.updateAll([taxProfile: null])
        });

        HookManager.register("customer-group-delete-at2-count", { response, id ->
            Integer count = Category.createCriteria().count {
                or {
                    inList("id", Category.where {
                        projections {
                            distinct("id")
                        }
                        availableToCustomerGroups {
                            eq("id", id)
                        }
                    })
                    inList("id", Category.where {
                        projections {
                            distinct("id")
                        }
                        restrictPriceExceptCustomerGroups {
                            eq("id", id)
                        }
                    })
                    inList("id", Category.where {
                        projections {
                            distinct("id")
                        }
                        restrictPurchaseExceptCustomerGroups {
                            eq("id", id)
                        }
                    })
                }
            }
            if (count) {
                response."category" = count
            }
            return response;
        })

        HookManager.register("customer-group-delete-at2-list", { response, id ->
            List categories = Category.createCriteria().list {
                or {
                    inList("id", Category.where {
                        projections {
                            distinct("id")
                        }
                        availableToCustomerGroups {
                            eq("id", id)
                        }
                    })
                    inList("id", Category.where {
                        projections {
                            distinct("id")
                        }
                        restrictPriceExceptCustomerGroups {
                            eq("id", id)
                        }
                    })
                    inList("id", Category.where {
                        projections {
                            distinct("id")
                        }
                        restrictPurchaseExceptCustomerGroups {
                            eq("id", id)
                        }
                    })
                }
            }
            if (categories.size() > 0) {
                response."category" = categories.collect { it.name }
            }
            return response
        })

        AppEventManager.on("before-customer-group-delete", { id ->
            CustomerGroup customerGroup = CustomerGroup.proxy(id)
            Category.createCriteria().list {
                or {
                    inList("id", Category.where {
                        projections {
                            distinct("id")
                        }
                        availableToCustomerGroups {
                            eq("id", id)
                        }
                    })
                    inList("id", Category.where {
                        projections {
                            distinct("id")
                        }
                        restrictPriceExceptCustomerGroups {
                            eq("id", id)
                        }
                    })
                    inList("id", Category.where {
                        projections {
                            distinct("id")
                        }
                        restrictPurchaseExceptCustomerGroups {
                            eq("id", id)
                        }
                    })
                }
            }.each {
                it.availableToCustomerGroups.remove(customerGroup)
                it.restrictPriceExceptCustomerGroups.remove(customerGroup)
                it.restrictPurchaseExceptCustomerGroups.remove(customerGroup)
                it.merge()
            }
        });

        AppEventManager.on("before-template-install", {
            List<TemplateContent> contents = TemplateContent.createCriteria().list {
                eq("contentType", DomainConstants.WIDGET_CONTENT_TYPE.CATEGORY)
            }
            if (contents) {
                Category.where {
                    inList("id", contents.contentId)
                }.updateAll(isDisposable: true)
                contents*.delete()
            }
        });
    }

    static {
        AppEventManager.on("category-update", { id ->
            ProductService productService = ProductService.instance
            Category category = Category.get(id)
            category.isDisposable = false
            category.save()
            List<Product> products = productService.getProducts([parent: category.id, lookup: "recursive"])
            products.each {
                productService.setCalculatedFields(it)
            }
        })
    }

    private getCriteriaClosure(Map params) {
        def session = AppUtil.session
        return {
            if(params.ids) {
                inList("id", params.list("ids").collect {it.toLong()})
            }
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.name) {
                ilike("name", "%${params.name.trim().encodeAsLikeText()}%")
            }
            if (params.sku) {
                ilike("sku", "%${params.sku.trim().encodeAsLikeText()}%")
            }
            if (params.parent) {
                if(params.parent == "root") {
                    isNull("parent")
                } else {
                    eq("parent.id", params.parent.toLong())
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
            if (params.createdBy) {
                eq("createdBy.id", params.createdBy.toLong())
            }
            if (params.isAvailable) {
                eq("isAvailable", params.isAvailable == "true")
            }
            if(params.availableFor) {
                eq("availableFor", params.availableFor)
            }
            if (params.sortBy) {
                switch (params.sortBy) {
                    case "ALPHA_ASC": order("name", "asc");
                        break;
                    case "ALPHA_DESC": order("name", "desc");
                        break;
                    case "SKU_ASC": order("sku", "asc");
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
            eq("isInTrash", false);
            eq("isParentInTrash", false);
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
            eq("isParentInTrash", false);
        }
        return closure;
    }

    public isNameUnique(Long id, Category parent, String name) {
        id = id ?: 0
         List categories = Category.createCriteria().list {
            ne("id", id)
            eq("name", name)
            if(parent) {
                eq("parent", parent)
            } else {
                isNull("parent")
            }
        }
        return commonService.fieldExistenceStatus(categories ? categories.first() : null)
    }

    Integer getCategoriesCount(Map params) {
        return Category.createCriteria().count {
            and getCriteriaClosure(params)
        }
    }

    List<Category> getCategories(Map params) {
        Map listMap = [max: params.max, offset: params.offset];
        return Category.createCriteria().list(listMap) {
            and getCriteriaClosure(params);
            order(params.sort ?: "name", params.dir ?: "asc")
        }
    }

    List getChildCategories(Long categoryId, Map params, Map listMap) {
        return Category.createCriteria().list(listMap) {
            if (categoryId == 0) {
                isNull("parent")
            } else {
                eq("parent.id", categoryId)
            }
            and getCriteriaClosure(params)
        }
    }

    Integer getChildCategoriesCount(Long categoryId, Map params) {
        return Category.createCriteria().count {
            if (categoryId == 0) {
                isNull("parent")
            } else {
                eq("parent.id", categoryId)
            }
            and getCriteriaClosure(params)
        }
    }

    Category getCategory(Long id) {
        return Category.get(id);
    }

    void updateImage(Category category, MultipartFile imgFile, Boolean removeOld) {
        if (removeOld) {
            category.removeResource()
            category.image = null;
        }
        if (imgFile) {
            if(!category.id){
                category.save();
            }
            category.removeResource()
            category.image = imgFile.originalFilename
            imageService.uploadImage(imgFile, NamedConstants.IMAGE_RESIZE_TYPE.CATEGORY_IMAGE, category, 2 * 1024 * 1024)
        }
    }

    @Transactional
    Category saveBasic(Map params, MultipartFile imgFile) {
        Category category = params.id ? Category.proxy(params.id) : new Category(sku: commonService.getSKUForDomain(Category))
        category.name = params.name;
        category.sku = params.sku;
        if (!commonService.isUnique(category, "sku")) {
            throw new ApplicationRuntimeException("category.sku.exists")
        }
        category.url = params.url ?:  commonService.getUrlForDomain(category);
        if (!commonService.isUnique(category, "url")) {
            throw new ApplicationRuntimeException("category.url.exists")
        }
        category.title = params.title;
        category.heading = params.heading
        category.parent = params.parent == "" ? null : Category.proxy(params.parent.toLong(0));
        category.isAvailable = params.isAvailable.toBoolean(true);
        if(category.isAvailable){
            def session = AppUtil.session;
            if(params.isAvailableOnDateRange){
                category.isAvailableOnDateRange = true;
                if(params.availableToDate){
                    category.availableToDate = params.availableToDate.dayEnd.gmt(session.timezone)
                }
                if(params.availableFromDate){
                    category.availableFromDate = params.availableFromDate.dayStart.gmt(session.timezone)
                }
            }else {
                category.isAvailableOnDateRange = false;
                category.availableFromDate = category.availableToDate = null;
            }
            category.availableFor = params.availableFor
            if(params.availableFor == DomainConstants.PRODUCT_AVAILABLE_FOR.SELECTED) {
                if(params.isCustomerSelectorDirty) {
                    if(params.customer) {
                        category.availableToCustomers = Customer.where {
                            id in params.list("customer").collect{it.toLong()}
                        }.list();
                    } else {
                        category.availableToCustomers = [];
                    }
                    if(params.customerGroup) {
                        category.availableToCustomerGroups = CustomerGroup.where {
                            id in params.list("customerGroup").collect{it.toLong()}
                        }.list()
                    } else {
                        category.availableToCustomerGroups = [];
                    }
                }
            } else {
                category.availableToCustomers = [];
                category.availableToCustomerGroups = [];
            }
        } else {
            category.availableToDate = category.availableFromDate = null;
            category.availableToCustomerGroups = [];
            category.availableToCustomers = []
        }
        category.summary = params.summary
        category.description = params.description;
        updateImage(category, imgFile, params["remove-image"] != null)
        if (category.id) {
            category.merge()
        } else {
            category.createdBy = Operator.get(AppUtil.loggedOperator)
            category.save()
        }

        Map response = [
            success: true
        ]
        HookManager.hook("saveCategoryInFilterProfile", response, params, category)
        if (response.success) {
            if (!category.hasErrors() && response.success) {
                sessionFactory.cache.evictCollectionRegions()
                if (params.id) {
                    AppEventManager.fire("category-update", [params.id])
                } else {
                    List<Product> products = productService.getProducts([parent: category.id, lookup: "recursive"])
                    products.each {
                        productService.setCalculatedFields(it)
                    }
                }
                return category
            }
        }
        return null
    }

    @Transactional
    boolean saveMetatags(Map params) {
        Category category = Category.get(params.id)
        if (!category) {
            return false
        }
        category.metaTags*.delete()
        category.metaTags = [];
        List<String> names = params.list("tag_name")
        List<String> values = params.list("tag_content")
        for (int i = 0; i < names.size(); i++) {
            MetaTag mt = new MetaTag(name: names[i], value: values[i]);
            mt.save()
            category.addToMetaTags(mt);
        }
        category.save()
        if(category.hasErrors()) {
            return false
        }
        AppEventManager.fire("category-update", [category.id])
        return true
    }

    @Transactional
    boolean saveLinkedProducts(Map params) {
        Category category = Category.proxy(params.id);
        if (!category) {
            return false
        }
        List<Product> products = params.list("linked").collect { Product.proxy(it.toLong(0)) };
        List<Product> existentProducts = category.products
        existentProducts.each {
            if(!products.contains(it)) {
                if(it.parent == category) {
                    it.parent = null;
                }
            }
        }
        existentProducts.clear()
        products.each {
            category.addToProducts(it)
        }
        category.merge()
        if(category.hasErrors()) {
            return false
        }
        sessionFactory.cache.evictQueryRegions()
        AppEventManager.fire("category-update", [category.id])
        return true
    }

    @Transactional
    boolean saveProductSettings(Map params) {
        Category category = Category.proxy(params.id);
        if (!category) {
            return false
        }
        if (params.taxProfile) {
            category.taxProfile = TaxProfile.proxy(params.taxProfile)
        } else {
            category.taxProfile = null;
        }
        if(params.shippingProfile) {
            category.shippingProfile = ShippingProfile.proxy(params["shippingProfile"]);
        } else {
            category.shippingProfile = null;
        }
        category.restrictPriceFor = params.restrictPrice ? params.restrictPriceFor : DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.NONE
        if (params.restrictPrice && params.restrictPriceFor == DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.EXCEPT_SELECTED) {
            category.restrictPriceExceptCustomers = []
            category.restrictPriceExceptCustomerGroups = []
            if(params.restrictPriceExceptCustomer) {
                category.restrictPriceExceptCustomers = Customer.where {
                    id in params.list("restrictPriceExceptCustomer").collect { it.toLong() }
                    order("id", "asc")
                }.list();
            }
            if(params.restrictPriceExceptCustomerGroup) {
                category.restrictPriceExceptCustomerGroups = CustomerGroup.where {
                    id in params.list("restrictPriceExceptCustomerGroup").collect { it.toLong() }
                    order("id", "asc")
                }.list()
            }
        } else {
            category.restrictPriceExceptCustomers = [];
            category.restrictPriceExceptCustomerGroups = [];
        }

        category.restrictPurchaseFor = params.restrictPurchase ? params.restrictPurchaseFor : DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.NONE
        if (params.restrictPurchase && params.restrictPurchaseFor == DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.EXCEPT_SELECTED) {
            category.restrictPurchaseExceptCustomers = []
            category.restrictPurchaseExceptCustomerGroups = []
            if(params.restrictPurchaseExceptCustomer) {
                category.restrictPurchaseExceptCustomers = Customer.where {
                    id in params.list("restrictPurchaseExceptCustomer").collect { it.toLong() }
                    order("id", "asc")
                }.list();
            }
            if(params.restrictPurchaseExceptCustomerGroup) {
                category.restrictPurchaseExceptCustomerGroups = CustomerGroup.where {
                    id in params.list("restrictPurchaseExceptCustomerGroup").collect { it.toLong() }
                    order("id", "asc")
                }.list()
            }
        } else {
            category.restrictPurchaseExceptCustomers = [];
            category.restrictPurchaseExceptCustomerGroups = [];
        }
        category.merge()
        if(category.hasErrors()) {
            return false
        }
        AppEventManager.fire("category-update", [category.id])
        return true
    }

    List getChildren(Long parentId = null) {
        List children = []
        Category.where {
            if (parentId) {
                parent.id == parentId
            } else {
                parent == null
            }
            isDisposable == false
            isInTrash == false
            isParentInTrash == false
        }.list().collect {
            children.add([
                    id: it.id, name: it.name.encodeAsBMHTML(),
                    parent: parentId,
                    owner_id: it.createdBy?.id,
                    hasChild: Category.countByParentAndIsInTrashAndIsParentInTrashAndIsDisposable(it, false, false, false) + Product.countByParentAndIsCombinedAndIsInTrashAndIsParentInTrashAndIsDisposable(it, true, false, false, false) > 0, type: "category", url: it.url]);
        };
        Product.where {
            if (parentId) {
                parent.id == parentId
            } else {
                parent == null
            }
            isCombined == true
            isInTrash == false
            isDisposable == false
            isParentInTrash == false
        }.list().collect {
            children.add([id: it.id, name: it.name.encodeAsBMHTML(), parent: parentId, hasChild: false, type: "combined", url: it.url]);
        }
        return children
    }

    @Transactional
    boolean deleteCategory(Long id) {
        Category category = Category.get(id);
        category.lock()
        category.refresh()
        category.delete()
        File resDir = new File(PathManager.getResourceRoot("category/category-${id}"));
        if (resDir.exists()) {
            resDir.deleteDir()
        }
        return !category.hasErrors()
    }

    @Transactional
    public boolean putCategoryInTrash(Long id, String at2_reply, String at1_reply) {
        TrashUtil.preProcessPutInTrash("category", id, at2_reply != null, at1_reply != null)
        return trashService.putObjectInTrash("category", Category.proxy(id), at1_reply)
    }

    @Transactional
    def putSelectedCategoriesInTrash(List<String> ids) {
        Integer removeCount = 0;
        ids.each { id ->
            if(putCategoryInTrash(id.toLong(),"yes", "include")){
                removeCount ++;
            }
        }
        return removeCount;
    }

    public Long countCategoriesInTrash() {
        return Category.createCriteria().count {
            eq("isInTrash", true)
        }
    }

    public Long countCategoriesInTrash(Map params) {
        return Category.createCriteria().count {
            and getCriteriaClosureForTrash(params)
        }
    }

    public Map getCategoriesInTrash(int offset, int max, String sort, String dir) {
        return [Category: Category.createCriteria().list(offset: offset, max: max) {
            and getCriteriaClosureForTrash([:])
            order(sort ?: "name", dir ?: "asc")
        }.collect {
            [id: it.id, name: it.name, updated: it.updated]
        }]
    }

    public Map getCategoriesInTrash(Map params) {
        Closure closure = getCriteriaClosureForTrash(params);
        def listMap = [offset: params.offset, max: params.max];
        return [Category: Category.createCriteria().list(listMap) {
            and closure
            order(params.sort ?: "name", params.dir ?: "asc")
        }.collect {
            [id: it.id, name: it.name, updated: it.updated]
        }]
    }

    @Transactional
    public boolean restoreCategoryFromTrash(Long id, Boolean hasParent = false) {
        Map responseMap = [:]
        Category category = Category.get(id);
        if (!category) {
            return false
        }
        if(!hasParent){
            if (!category.isParentInTrash){
                category.isInTrash = false
                category.merge()
                AppEventManager.fire("category-restore", [id])
                return !category.hasErrors()
            } else {
                responseMap.at1 = category.isParentInTrash;
                throw new AttachmentExistanceException(responseMap)
            }
        } else {
            category.parent = null
            category.isParentInTrash = false
            category.isInTrash = false
            category.merge()
            return !category.hasErrors()
        }
    }

    @Transactional
    public Long restoreCategoryFromTrash(String field, String value, String compositeField = null, String compositeFieldValue = null) {
        Category category = Category.createCriteria().get {
            eq(field, value)
            if(compositeField && compositeFieldValue) {

                eq(compositeField, Category.get(compositeFieldValue))
            } else if(compositeField) {
                isNull(compositeField)
            }
        }
        category.isInTrash = false;
        category.merge();
        return category.id;
    }

    @Transactional
    public boolean deleteTrashItemAndSaveCurrent(def field, def value) {
        Category category = Category.createCriteria().get {
            eq(field, value)
        }
        deleteCategory(category.id);
        return !category.hasErrors();
    }

    @Transactional
    public void changeOrder(Long id, String dir) {
        Category category = Category.get(id);
        Category parent = category.parent;
        List<Category> categories = Category.createCriteria().list {
            eq("parent", parent)
            order("idx", "asc")
        }
        int index = 0
        while (categories[index] != category) {
            index++;
        }
        Category swap;
        try {
            if (dir == "up") {
                swap = categories[index - 1]
            } else if (dir == "down") {
                swap = categories[index + 1]
            }
        } catch (ArrayIndexOutOfBoundsException err) {
            return
        }
        index = category.idx;
        category.idx = swap.idx;
        swap.idx = index;
        category.merge()
        swap.merge()
    }

    @Transactional
    public void changeOrder(Long id, Integer newOrder) {
        Category category = Category.get(id);
        category.idx = newOrder
        category.merge()
    }

    public boolean saveAdvanced(Map params){
        Map response = [
                success: true
        ]
        Category category = Category.get(params.id)
        category.disableGooglePageTracking = params.disableTracking.toBoolean(false);
        category.save()
        HookManager.hook("saveCategoryAdvancedData", response, params);
        if(response.success) {
            AppEventManager.fire("category-update", [params.id])
            return true
        }
        return false
    }

    @Transactional
    public boolean saveCurrentOrder(Map params) {
        Integer idx = 1;
        Category.createCriteria().list {
            order(params.sort, params.dir);
            if(params.parent == "root"){
                isNull("parent")
            }else {
                eq("parent", Category.proxy(params.parent))
            }
        }.each {
            it.idx = idx++;
            it.merge()
        }
        return true
    }

    public Category getCategoryByNameOrSku(String nameOrSku) {
        List<Category> categories = Category.createCriteria().list {
            or {
                like("name", nameOrSku.encodeAsLikeText())
                like("sku", nameOrSku.encodeAsLikeText())
            }
        }
        if(categories.size() > 0) {
            return categories.get(0)
        }
        return null
    }

    public List<NavigationItem> generateNavigationItemsFromCategories() {
        List<Category> categories = Category.createCriteria().list(){
            eq("isDisposable", false)
            eq("isInTrash", false)
            eq("isParentInTrash", false)
        }
        def parentBasedStorage = [:];
        categories.each { category ->
            String parentId;
            if(category.parent) {
                parentId = "" + category.parent.id
            } else {
                parentId = "0";
            }
            List childList = parentBasedStorage[parentId];
            String id = "" + category.id;
            NavigationItem navigationItem = new NavigationItem()
            navigationItem.itemType = DomainConstants.NAVIGATION_ITEM_TYPE.CATEGORY
            navigationItem.itemRef = id
            navigationItem.target = "_self"
            navigationItem.label = category.name;
            if(childList) {
                childList.add(navigationItem)
            } else {
                childList = [navigationItem];
                parentBasedStorage[parentId] = childList;
            }
        }

        return navigationService.sortBasedOnIndex(parentBasedStorage);
    }

    List<Category> getCategoriesInOrder(List<Long> categoryIds) {
        if(categoryIds.isEmpty()) { return [] }
        List<Category> categories = categoryIds ? Category.findAllByIdInList(categoryIds) : []
        return SortAndSearchUtil.sortInCustomOrder(categories, "id", categoryIds)
    }

    DetachedCriteria getAvailablityFilterCriteria(List<Long> ids, Map filterMap) {
        Long customerId = AppUtil.loggedCustomer
        Date currentGmtDate = new Date().gmt();
        Date endGmtDate = currentGmtDate + 1;
        def query = Category.where {
            if(ids) {
                id in ids
            }
            isInTrash == false
            isParentInTrash == false
            isDisposable == false
            if (!AppUtil.request.editMode && !filterMap.forAdmin) {
                isAvailable == true && (
                    availableFromDate == null || availableFromDate <= currentGmtDate
                ) && (
                    availableToDate == null || availableToDate > endGmtDate
                )
                if(customerId) {
                    (
                        availableFor == DomainConstants.CATEGORY_AVAILABLE_FOR.EVERYONE ||
                        availableFor == DomainConstants.CATEGORY_AVAILABLE_FOR.CUSTOMER ||
                        exists (Category.where {
                            def subcat1 = Category
                            eqProperty "subcat1.id", "cat.id"
                            availableToCustomers.id == customerId
                        }.id()) ||
                        exists (Category.where {
                            def subcat2 = Category
                            eqProperty "subcat2.id", "cat.id"
                            availableToCustomerGroups.id in CustomerGroup.where {
                                customers.id == customerId
                            }.id()
                        }.id())
                    )
                } else {
                    availableFor == DomainConstants.CATEGORY_AVAILABLE_FOR.EVERYONE
                }
            }
            if (filterMap.name) {
                like "name", "%" + filterMap.name.trim().encodeAsLikeText() + "%"
            }
            if(filterMap.order) {
                order filterMap.order
            }
        }
        query.alias = "cat";
        return query
    }

    public List filterOutAvailableCategories(List<Long> ids, Map filterMap) {
        if(ids != null && !ids.size()) {
            return [];
        }
        filterMap.order = "idx"
        return getAvailablityFilterCriteria(ids, filterMap).list(max: filterMap["max"] ?: -1, offset: filterMap["offset"] ?: 0)
    }

    public List filterOutAvailableCategoryIds(List<Long> ids, Map filterMap) {
        if (!ids.size()) {
            return [];
        }
        filterMap.order = "idx"
        return getAvailablityFilterCriteria(ids, filterMap).id().list(max: filterMap["max"] ?: -1, offset: filterMap["offset"] ?: 0)
    }

    public Integer filterOutAvailableCategoryCount(List<Long> ids, Map filterMap) {
        if (ids != null && !ids.size()) {
            return 0;
        }
        return getAvailablityFilterCriteria(ids, filterMap).count()
    }

    public Boolean isAvailable(Long id) {
        Date currentGmtDate = new Date().gmt();
        return Category.createCriteria().count {
            eq("id", id)
            eq("isAvailable", true)
            or {
                isNull("availableFromDate")
                and {
                    le("availableFromDate", currentGmtDate)
                    or {
                        isNull("availableToDate")
                        gt("availableToDate", currentGmtDate + 1)
                    }
                }
            }
        } > 0
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
        if(max == -1) {
            max = Integer.MAX_VALUE
        }
        Date currentGmtDate = new Date().gmt()
        String availabilitySQL = "and C.isAvailable = 1 " +
                "and (C.availableFor = '${DomainConstants.CATEGORY_AVAILABLE_FOR.EVERYONE}'"
        availabilitySQL += cid ? " or C.availableFor = '${DomainConstants.CATEGORY_AVAILABLE_FOR.CUSTOMER}'" : ""
        availabilitySQL += cid ? "or (C.availableFor = '${DomainConstants.CATEGORY_AVAILABLE_FOR.SELECTED}' and (exists(select AC.id from C.availableToCustomers AC where AC.id = ${cid}) " +
                "or exists(select AG.id from C.availableToCustomerGroups AG where exists(select AGC.id from AG.customers AGC where AGC.id = ${cid}) )) )" : ""
        availabilitySQL += ")"
        String dateAvailabilitySQL = "and (C.isAvailableOnDateRange = 0 or ((C.availableFromDate is NULL or C.availableFromDate <= :fdate) and (C.availableToDate is NULL or C.availableToDate >= :tdate)))"
        Map limit = isCount ? [:] : [offset: offset, max: max]
        String projection = isCount ? "count(C)" : "C"
        def result = Category.executeQuery("select $projection from Category C where C.isInTrash = 0 and C.isDisposable = 0 and C.isParentInTrash = 0 and C.name like '%$likeText%' $dateAvailabilitySQL $availabilitySQL"
                , [fdate: currentGmtDate, tdate: currentGmtDate + 1], limit)
        return isCount ? result[0] : result
    }

    Boolean isPermitted(Long categoryId, Long customerId) {
        return getAvailablityFilterCriteria([categoryId], [customer: categoryId]).get() != null
    }

    @Transactional
    Integer saveBasicBulkProperties(Map properties) {
        List<Long> ids = properties.list("id").collect { it.toLong() }
        List categoryList = []
        int count = 0
        ids.each {
            Map category = [id: it]
            category << properties[it.toString()]
            categoryList << category
        }
        categoryList.each { params ->
            Category category = Category.get(params.id)
            category.name = params.name.trim()

            category.parent = params.parent.trim()
            category.isAvailable = params.isAvailable.toBoolean(true)
            if (!category.isAvailable) {
                category.availableToDate = category.availableFromDate = null
                category.availableToCustomerGroups = []
                category.availableToCustomers = []
            }

            category.save()
            if (!category.hasErrors()) {
                sessionFactory.cache.evictCollectionRegions()
                AppEventManager.fire("category-update", [params.id])
                count++
            }
        }
        return count
    }

    @Transactional
    Integer saveAdvancedBulkProperties(Map properties) {
        List<Long> ids = properties.list("id").collect { it.toLong() }
        List categoryList = []
        int count = 0
        ids.each {
            TypeConvertingMap category = [id: it]
            category << properties[it.toString()]
            categoryList << category
        }

        categoryList.each { params ->
            Category category = Category.get(params.id)
            category.name = params.name.trim()
            category.disableGooglePageTracking = params.disableTracking.toBoolean(true)

            category.restrictPriceFor = params.restrictPriceFor ?: DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.NONE
            if (params.restrictPriceFor == DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.EXCEPT_SELECTED) {
                category.restrictPriceExceptCustomers = []
                category.restrictPriceExceptCustomerGroups = []
                if (properties.customer) {
                    category.restrictPriceExceptCustomers = Customer.where {
                        id in properties.list("customer").collect { it.toLong() }
                        order("id", "asc")
                    }.list();
                }
                if (properties.customerGroup) {
                    category.restrictPriceExceptCustomerGroups = CustomerGroup.where {
                        id in properties.list("customerGroup").collect { it.toLong() }
                        order("id", "asc")
                    }.list()
                }
            } else {
                category.restrictPriceExceptCustomers = [];
                category.restrictPriceExceptCustomerGroups = [];
            }

            category.restrictPurchaseFor = params.restrictPurchaseFor ?: DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.NONE
            if (params.restrictPurchaseFor == DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.EXCEPT_SELECTED) {
                category.restrictPurchaseExceptCustomers = []
                category.restrictPurchaseExceptCustomerGroups = []
                if (properties.customer) {
                    category.restrictPurchaseExceptCustomers = Customer.where {
                        id in properties.list("customer").collect { it.toLong() }
                        order("id", "asc")
                    }.list();
                }
                if (properties.customerGroup) {
                    category.restrictPurchaseExceptCustomerGroups = CustomerGroup.where {
                        id in properties.list("customerGroup").collect { it.toLong() }
                        order("id", "asc")
                    }.list()
                }
            } else {
                category.restrictPurchaseExceptCustomers = [];
                category.restrictPurchaseExceptCustomerGroups = [];
            }

            Map response = [
                    success: true
            ]
            HookManager.hook("saveCategoryAdvancedData", response, params);
            if (response.success) {
                category.merge()
            }
            if (response.success && !category.hasErrors()) {
                AppEventManager.fire("category-update", [params.id])
                count++
            }
        }
        return count;
    }
}
