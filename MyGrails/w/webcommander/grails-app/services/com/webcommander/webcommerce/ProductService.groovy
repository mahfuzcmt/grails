package com.webcommander.webcommerce

import com.webcommander.AppResourceTagLib
import com.webcommander.ApplicationTagLib
import com.webcommander.admin.ConfigService
import com.webcommander.admin.Customer
import com.webcommander.admin.CustomerGroup
import com.webcommander.admin.Operator
import com.webcommander.admin.TrashService
import com.webcommander.annotations.Initializable
import com.webcommander.common.*
import com.webcommander.config.StoreDetail
import com.webcommander.config.StoreProductAssoc
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.conversion.LengthConversions
import com.webcommander.conversion.MassConversions
import com.webcommander.events.AppEventManager
import com.webcommander.hibernate.CaseExpressionOrder
import com.webcommander.hibernate.ExpressionOrderSupportedDetachedCriteria
import com.webcommander.installation.template.TemplateContent
import com.webcommander.manager.CacheManager
import com.webcommander.manager.HookManager
import com.webcommander.manager.PathManager
import com.webcommander.models.Cart
import com.webcommander.models.ProductData
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.DomainUtil
import com.webcommander.util.SortAndSearchUtil
import com.webcommander.util.TrashUtil
import com.webcommander.util.security.InformationEncrypter
import grails.converters.JSON
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.util.TypeConvertingMap
import groovy.json.JsonSlurper
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.hibernate.SessionFactory
import org.hibernate.engine.spi.Status
import org.hibernate.sql.JoinType
import org.hibernate.type.DoubleType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.multipart.MultipartFile

@Initializable
class ProductService {
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    ApplicationTagLib app
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    org.grails.plugins.web.taglib.ApplicationTagLib g
    @Autowired
    @Qualifier("com.webcommander.AppResourceTagLib")
    AppResourceTagLib appResource
    CommonService commonService
    TrashService trashService
    ImageService imageService
    VideoService videoService
    CommanderMailService commanderMailService
    FileService fileService
    ConfigService configService
    private static SessionFactory sessionFactory
    private static ProductService _instance = null

    private static SessionFactory getSessionFactory() {
        return sessionFactory ?: (sessionFactory = Holders.grailsApplication.mainContext.sessionFactory)
    }

    static void initialize() {
        AppEventManager.on("before-operator-delete", { id ->
            Product.executeUpdate("update Product p set p.createdBy = null where p.createdBy.id = :uid", [uid: id])
        })

        HookManager.register("shippingProfile-delete-at2-count", { response, id ->
            int productCount = Product.createCriteria().count {
                eq("shippingProfile.id", id)
            }
            if (productCount) {
                response."product(s)" = productCount
            }
            return response
        })
        HookManager.register("shippingProfile-delete-at2-list", { response, id ->
            List products = Product.createCriteria().list {
                projections {
                    property("name")
                }
                eq("shippingProfile.id", id)
            }
            if (products.size()) {
                response."product(s)" = products
            }
            return response
        })
        AppEventManager.on("before-shippingProfile-delete", { id ->
            Product.createCriteria().list {
                eq("shippingProfile.id", id)
            }.each {
                it.shippingProfile = null
            }*.save()
        })

        AppEventManager.on("before-customer-delete", { id ->
            Customer customer = Customer.proxy(id)
            Product.createCriteria().list {
                or {
                    inList("id", Product.where {
                        projections {
                            distinct("id")
                        }
                        availableToCustomers {
                            eq("id", id)
                        }
                    })
                    inList("id", Product.where {
                        projections {
                            distinct("id")
                        }
                        restrictPriceExceptCustomers {
                            eq("id", id)
                        }
                    })
                    inList("id", Product.where {
                        projections {
                            distinct("id")
                        }
                        restrictPurchaseExceptCustomers {
                            eq("id", id)
                        }
                    })
                    inList("id", Product.where {
                        projections {
                            distinct("id")
                        }
                        calculatedRestrictPriceExceptCustomers {
                            eq("id", id)
                        }
                    })
                    inList("id", Product.where {
                        projections {
                            distinct("id")
                        }
                        calculatedRestrictPurchaseExceptCustomers {
                            eq("id", id)
                        }
                    })
                }
            }.each {
                it.availableToCustomers.remove(customer)
                it.restrictPriceExceptCustomers.remove(customer)
                it.restrictPurchaseExceptCustomers.remove(customer)
                it.calculatedRestrictPriceExceptCustomers.remove(customer)
                it.calculatedRestrictPurchaseExceptCustomers.remove(customer)
                it.merge()
                AppEventManager.fire("product-update", [it.id])
            }
        })
        AppEventManager.on("before-category-put-in-trash", { id, at1WithChildren ->
            if (at1WithChildren == "include") {
                Product.createCriteria().list {
                    parents {
                        eq("id", id)
                    }
                }.each {
                    Integer count = it.parents.count { category ->
                        return category.isInTrash || category.isParentInTrash
                    }
                    if (count + 1 == it.parents.size()) {
                        it.isParentInTrash = true
                    }
                    it.merge()
                }
            } else {
                Product.createCriteria().list {
                    parents {
                        eq("id", id)
                    }
                }.each {
                    if (it.parent?.id == id) {
                        it.parent = null
                    }
                    it.removeFromParents(Category.proxy(id))
                    it.merge()
                    AppEventManager.fire("product-update", [it.id])
                }
            }
        })
        AppEventManager.on("before-category-delete") { id ->
            List products = Product.createCriteria().list {
                createAlias("parents", "p", JoinType.LEFT_OUTER_JOIN)
                or {
                    eq("parent.id", id)
                    eq("p.id", id)
                }
            }

            products.each {
                if (it.parents.size() <= 1 && it.isParentInTrash) {
                    AppEventManager.fire("before-product-delete", [it.id])
                    it.delete()
                    AppEventManager.fire("product-delete", [it.id])

                } else {
                    if (it.parent?.id == id) {
                        it.parent = null
                    }
                    it.removeFromParents(Category.proxy(id))
                    it.merge()
                }
            }
        }
        AppEventManager.on("category-restore") { id ->
            Product.createCriteria().list {
                parents {
                    eq("id", id)
                }
            }.each {
                it.isParentInTrash = false
            }
        }

        HookManager.register("category-put-trash-at1-count") { response, id ->
            int productCount = Product.createCriteria().count {
                parents {
                    eq("id", id)
                }
                eq("isInTrash", false)
            }
            if (productCount) {
                response."product(s)" = productCount
            }
            return response
        }
        HookManager.register("category-put-trash-at1-list") { response, id ->
            List products = Product.createCriteria().list {
                projections {
                    property("name")
                }
                parents {
                    eq("id", id)
                }
                eq("isInTrash", false)
            }
            if (products.size()) {
                response."product(s)" = products
            }
            return response
        }
        HookManager.register("taxProfile-delete-at2-count") { response, id ->
            int productCount = Product.where {
                taxProfile.id == id
            }.count()
            if (productCount) {
                response."product(s)" = productCount
            }
            return response
        }
        HookManager.register("taxProfile-delete-at2-list") { response, id ->
            List products = Product.createCriteria().list {
                projections {
                    property("name")
                }
                eq("taxProfile.id", id)
            }
            if (products.size()) {
                response."product(s)" = products
            }
            return response
        }
        AppEventManager.on("before-taxProfile-delete", { id ->
            TaxProfile profile = TaxProfile.proxy(id)
            Product.where {
                taxProfile == profile
            }.updateAll([taxProfile: null])
        })

        HookManager.register("customer-group-delete-at2-count", { response, id ->
            Integer count = Product.createCriteria().count {
                or {
                    inList("id", Product.where {
                        projections {
                            distinct("id")
                        }
                        availableToCustomerGroups {
                            eq("id", id)
                        }
                    })
                    inList("id", Product.where {
                        projections {
                            distinct("id")
                        }
                        restrictPriceExceptCustomerGroups {
                            eq("id", id)
                        }
                    })
                    inList("id", Product.where {
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
                response."product" = count
            }
            return response;
        })

        HookManager.register("customer-group-delete-at2-list", { response, id ->
            List products = Product.createCriteria().list {
                or {
                    inList("id", Product.where {
                        projections {
                            distinct("id")
                        }
                        availableToCustomerGroups {
                            eq("id", id)
                        }
                    })
                    inList("id", Product.where {
                        projections {
                            distinct("id")
                        }
                        restrictPriceExceptCustomerGroups {
                            eq("id", id)
                        }
                    })
                    inList("id", Product.where {
                        projections {
                            distinct("id")
                        }
                        restrictPurchaseExceptCustomerGroups {
                            eq("id", id)
                        }
                    })
                }
            }
            if (products.size() > 0) {
                response."product" = products.collect { it.name }
            }
            return response
        })

        AppEventManager.on("before-customer-group-delete", { id ->
            CustomerGroup customerGroup = CustomerGroup.proxy(id)
            Product.createCriteria().list {
                or {
                    inList("id", Product.where {
                        projections {
                            distinct("id")
                        }
                        availableToCustomerGroups {
                            eq("id", id)
                        }
                    })
                    inList("id", Product.where {
                        projections {
                            distinct("id")
                        }
                        restrictPriceExceptCustomerGroups {
                            eq("id", id)
                        }
                    })
                    inList("id", Product.where {
                        projections {
                            distinct("id")
                        }
                        restrictPurchaseExceptCustomerGroups {
                            eq("id", id)
                        }
                    })
                    inList("id", Product.where {
                        projections {
                            distinct("id")
                        }
                        calculatedRestrictPriceExceptCustomerGroups {
                            eq("id", id)
                        }
                    })
                    inList("id", Product.where {
                        projections {
                            distinct("id")
                        }
                        calculatedRestrictPurchaseExceptCustomerGroups {
                            eq("id", id)
                        }
                    })
                }
            }.each {
                it.availableToCustomerGroups.remove(customerGroup)
                it.restrictPriceExceptCustomerGroups.remove(customerGroup)
                it.restrictPurchaseExceptCustomerGroups.remove(customerGroup)
                it.calculatedRestrictPriceExceptCustomerGroups.remove(customerGroup)
                it.calculatedRestrictPurchaseExceptCustomerGroups.remove(customerGroup)
                it.merge()
                AppEventManager.fire("product-update", [it.id])
            }
        })

        AppEventManager.on("before-product-delete", { id ->
            Product product = Product.get(id);
            Product.createCriteria().list {
                relatedProducts {
                    eq("id", id)
                }
            }.each {
                if (getSessionFactory().currentSession.persistenceContext.getEntry(it).status != Status.DELETED) {
                    it.refresh()
                    it.removeFromRelatedProducts(product)
                    it.merge()
                }
            }
        })

        AppEventManager.on("before-template-install", {
            List<TemplateContent> contents = TemplateContent.createCriteria().list {
                eq("contentType", DomainConstants.WIDGET_CONTENT_TYPE.PRODUCT)
            }
            if (contents) {
                Product.where {
                    inList("id", contents.contentId)
                }.updateAll(isDisposable: true)
                contents*.delete()
            }
        })

        AppEventManager.on("before-operator-delete", { id ->
            ProductInventoryAdjustment.executeUpdate("update ProductInventoryAdjustment p set p.createdBy = null where p.createdBy.id = :uid", [uid: id])
        })

        AppEventManager.on("before-product-delete", { id ->
            Product product = Product.proxy(id)
            ProductInventoryAdjustment.createCriteria().list {
                eq("product.id", id)
            }.each {
                AppEventManager.fire("before-productInventoryAdjustment-delete", [it.id])
                product.removeFromInventoryAdjustments(it)
                it.delete()
                AppEventManager.fire("productInventoryAdjustment-delete", [it.id])
            }
            product.save()
        })

        AppEventManager.on("before-product-put-in-trash", { id, at1WithChildren ->
            Product product = Product.proxy(id)
            if(!product.isCombined) {
                CombinedProduct.where {
                    includedProduct == product
                }.deleteAll()
            }
        })
        AppEventManager.on("before-product-delete", { id, at1_reply ->
            Product product = Product.get(id);
            CombinedProduct.createCriteria().list {
                or {
                    eq("baseProduct", product)
                    eq("includedProduct", product)
                }
            }*.delete()
        })

        AppEventManager.on("cart-removed", { Cart cart ->
            if (AppUtil.session && AppUtil.session.checkout_as_guest && AppUtil.session.effective_billing_address) {
                AppUtil.session.effective_billing_address = null
            }
        })
    }

    static getInstance() {
        return _instance ?: (_instance = Holders.applicationContext.getBean("productService"))
    }

    static {
        HookManager.register("load-product-search-product-ids", { productIds, params ->
            if (params.type == "product-search") {
                params["spx-max"] = -1
                def model = Holders.applicationContext.productService.getProductSearchReasult(params)
                productIds = model["productList"].id
            }
            return productIds
        })

        AppEventManager.on("product-update", { id ->
            Product product = Product.get(id)
            product.isDisposable = false
            instance.setCalculatedFields(product)
        })

        AppEventManager.on("${DomainConstants.SITE_CONFIG_TYPES.TAX}-configuration-update ${DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE}-configuration-update product-update category-update tax-profile-update tax-code-update tax-rule-update zone-update store-detail-update", {
            CacheManager.removeCache(NamedConstants.CACHE.SCOPE_APP, "product")
        })
        AppEventManager.on("product-delete") { id ->
            File resDir = new File(PathManager.getResourceRoot("product/product-${id}"))
            if (resDir.exists()) {
                resDir.deleteDir()
            }
        }
        HookManager.register("resolveNameAndUrl-product", { map, id ->
            Product.withNewSession {
                Product product = Product.get(id)
                return product ? [name: product.name, url: "product/" + product.url] : null
            }
        })
    }

    private DetachedCriteria<Product> getCriteriaQuery(Map params) {
        def session = AppUtil.session
        ArrayList<Long> categoryList = new ArrayList<Long>()
        if (!params.lookup) {
            params.lookup = "non-recursive"
        }
        if (params.ids) {
            params.ids = (params.ids instanceof List || params.ids instanceof Object[]) ? params.ids : [params.ids]
        }
        if (params.parent == "all") {
            params.lookup = "recursive"
        } else if (params.parent == "root") {
            params.parent = null
        } else if (params.parent) {
            Long catId = params.parent.toLong()
            categoryList.add(catId)
            if (params.lookup == "recursive") {
                Closure addRecursive
                addRecursive = { _catId ->
                    Category.createCriteria().list {
                        projections {
                            property("id")
                        }
                        eq("parent.id", _catId)
                        eq("isInTrash", false)
                        eq("isParentInTrash", false)
                    }.each { id ->
                        categoryList.add(id)
                        addRecursive(id)
                    }
                }
                addRecursive(catId)
            }
        } else if (params.parentList) {
            categoryList = params.parentList
        }
        return Product.where {
            def p1 = Product
            if (params.exclude) {
                def excludeList = params.list("exclude").collect { it.toLong() }
                not {
                    inList("id", excludeList)
                }
            }
            if (params.ids) {
                inList("id", params.list("ids").collect { it.toLong() })
            }
            if (params.searchText) {
                or {
                    like("name", "%${params.searchText.trim().encodeAsLikeText()}%")
                    like("sku", "%${params.searchText.trim().encodeAsLikeText()}%")
                }
            }
            if (params.name) {
                like("name", "%${params.name.trim().encodeAsLikeText()}%")
            }
            if (params.availableFor) {
                eq("availableFor", params.availableFor)
            }
            if (params.sku) {
                like("sku", "%${params.sku.encodeAsLikeText()}%")
            }
            if (params.isAvailable) {
                eq("isAvailable", params.isAvailable == "true")
            }
            if (params.isActive) {
                eq("isActive", params.isActive == "true")
            }
            if (params.isInventoryEnabled) {
                eq("isInventoryEnabled", params.isInventoryEnabled == "true")
            }
            if (params.productType) {
                eq("productType", params.productType)
            }
            if (params.isDisposable != "true") {
                eq("isDisposable", false)
            } else {
                eq("isDisposable", true)
            }
            if (params.stock) {
                if (params.stock == 'low') {
                    eq("isInventoryEnabled", true)
                    gt("availableStock", 0)
                    leProperty("availableStock", "lowStockLevel")
                } else if (params.stock == 'out') {
                    eq("isInventoryEnabled", true)
                    lt("availableStock", 1)
                } else if (params.stock == 'in') {
                    or {
                        eq("isInventoryEnabled", false)
                        or {
                            and {
                                isNull("lowStockLevel")
                                gt("availableStock", 0)
                            }
                            gtProperty("availableStock", "lowStockLevel")
                        }
                    }
                }
            }
            if (params.priceFrom || params.priceTo) {
                Double from = params.priceFrom ? params.priceFrom.toDouble() : 0
                Double to = params.priceTo ? params.priceTo.toDouble() : Double.MAX_VALUE
                between("basePrice", from, to)
            }
            if (params.costPriceFrom || params.costPriceTo) {
                Double from = params.costPriceFrom ? params.costPriceFrom.toDouble() : 0
                Double to = params.costPriceTo ? params.costPriceTo.toDouble() : Double.MAX_VALUE
                between("costPrice", from, to)
            }
            if (params.createdFrom) {
                Date date = params.createdFrom.dayStart.gmt(session.timezone)
                ge("created", date)
            }
            if (params.createdTo) {
                Date date = params.createdTo.dayEnd.gmt(session.timezone)
                le("created", date)
            }
            if (params.updatedFrom) {
                Date date = params.updatedFrom.dayStart.gmt(session.timezone)
                ge("updated", date)
            }
            if (params.updatedTo) {
                Date date = params.updatedTo.dayEnd.gmt(session.timezone)
                le("updated", date)
            }
            if (params.createdBy) {
                eq("createdBy.id", params.createdBy.toLong())
            }
            if (categoryList.size() > 0) {
                if (params.category == "primary") {
                    inList("parent.id", categoryList)
                } else {
                    exists Product.where {
                        def p2 = Product
                        p1.id == p2.id
                        p2.parents {
                            id in categoryList
                        }
                    }.id()
                }
            } else if (params.lookup == "non-recursive") {
                isNull "parent"
                parents.size() == 0
            }
            if (params.isCombined) {
                eq("isCombined", true)
            }
            if (params.notCombined) {
                eq("isCombined", false)
            }
            if (params.isNew) {
                eq("isNew", true)
            }
            if (params.sortBy) {
                switch (params.sortBy) {
                    case "ALPHA_ASC": order("name", "asc")
                        break
                    case "ALPHA_DESC": order("name", "desc")
                        break
                    case "SKU_ASC": order("sku", "asc")
                        break
                    case "PRICE_DESC": order("basePrice", "desc")
                        break
                    case "PRICE_ASC": order("basePrice", "asc")
                        break
                    case "CREATED_ASC": order("created", "asc")
                        break
                    case "CREATED_DESC": order("created", "desc")
                        break
                }
            }
            if (params.specialProductsFilter) {
                switch (params.specialProductsFilter) {
                    case "ON_SALE": eq("isOnSale", true)
                        break
                    case "CALL_FOR_PRICE": eq("isCallForPriceEnabled", true)
                        break
                    case "FEATURED": eq("isFeatured", true)
                        break
                }
            }

            eq("isInTrash", false)
            eq("isParentInTrash", false)
        }
    }

    private Closure getCriteriaClosureForIncludedProducts(Map params) {
        return {
            if (params.id) {
                Product base = Product.proxy(params.id.toLong())
                eq("baseProduct", base)
            }
            if (params.searchText) {
                like("label", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
        }
    }

    private Closure getCriteriaClosureForTrash(Map params) {
        def session = AppUtil.session
        return {
            if (params.searchText) {
                like("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.deletedFrom) {
                Date date = params.deletedFrom.dayStart.gmt(session.timezone)
                ge("updated", date)
            }
            if (params.deletedTo) {
                Date date = params.deletedTo.dayEnd.gmt(session.timezone)
                le("updated", date)
            }
            eq "isInTrash", true
        }
    }

    Integer getProductsCount(Map params) {
        return getCriteriaQuery(params).count()
    }

    List<Product> getProducts(Map params) {
        Map listMap = [max: params.max, offset: params.offset]
        DetachedCriteria<Product> query = getCriteriaQuery(params)
        if (params.sort && params.sort != "name") {
            query.order(params.sort, params.dir ?: "asc")
        } else if (params.sort && params.sort != "name") {
            query.order("idx", "asc")
        }
        query.order("name", params.dir ?: "asc")
        return query.list(listMap)
    }

    List<Product> getProductsInOrder(List<Long> productIds) {
        List<Product> products = productIds ? Product.findAllByIdInList(productIds) : []
        return SortAndSearchUtil.sortInCustomOrder(products, "id", productIds)
    }

    Product getProduct(Long id) {
        return Product.get(id)
    }

    Product getProductReadonly(Long id) {
        return Product.read(id)
    }

    boolean isUnique(Product product) {
        return !Product.createCriteria().count {
            eq("name", product.name)
            eq("parent", product.parent)
        }
    }

    @Transactional
    Product saveBasics(Map params) {
        Product product = params.id ? Product.get(params.id) : new Product(sku: commonService.getSKUForDomain(Product))
        product.name = params.name.trim()
        product.sku = params.sku
        if (!commonService.isUnique(product, "sku")) {
            throw new ApplicationRuntimeException("product.sku.exists", "alert")
        }
        product.url = params.url?.sanitize() ?: commonService.getUrlForDomain(product)
        if (!commonService.isUnique(product, "url")) {
            throw new ApplicationRuntimeException("product.url.exists", "alert")
        }
        product.title = params.title
        product.heading = params.heading
        product.productType = product.id ? product.productType : params.productType
        product.isActive = params.active.toBoolean(true)
        if (params.isCombinationPriceFixed == 'true' || params.isCombined != 'true') {
            Double basePrice = params.basePrice.toDouble()
            product.basePrice = basePrice
            product.costPrice = params.costPrice.toDouble(0)
            product.isCombinationPriceFixed = true
            product.isCombinationQuantityFlexible = false
        } else {
            product.isCombinationQuantityFlexible = params.isCombinationQuantityFlexible == 'true' && product.productType != DomainConstants.PRODUCT_TYPE.DOWNLOADABLE
            product.isCombinationPriceFixed = false
            product.basePrice = 0.0
            product.costPrice = 0.0
        }
        product.isCombined = params.isCombined.toBoolean()
        product.isAvailable = params.isAvailable.toBoolean(true)
        if (product.isAvailable) {
            def session = AppUtil.session
            if (params.isAvailableOnDateRange) {
                product.isAvailableOnDateRange = true
                if (params.availableToDate) {
                    product.availableToDate = params.availableToDate.dayEnd.gmt(session.timezone)
                }
                if (params.availableFromDate) {
                    product.availableFromDate = params.availableFromDate.dayStart.gmt(session.timezone)
                }
            } else {
                product.isAvailableOnDateRange = false
                product.availableFromDate = product.availableToDate = null
            }
            product.availableFor = params.availableFor
            if (params.availableFor == DomainConstants.PRODUCT_AVAILABLE_FOR.SELECTED) {
                product.availableToCustomers = []
                product.availableToCustomerGroups = []
                if (params.customer) {
                    product.availableToCustomers = Customer.where {
                        id in params.list("customer").collect { it.toLong() }
                    }.list()
                }
                if (params.customerGroup) {
                    product.availableToCustomerGroups = CustomerGroup.where {
                        id in params.list("customerGroup").collect { it.toLong() }
                    }.list()
                }
            } else {
                product.availableToCustomers = []
                product.availableToCustomerGroups = []
            }

        } else {
            product.availableToDate = product.availableFromDate = null
            product.availableToCustomerGroups = []
            product.availableToCustomers = []
        }
        product.summary = params.summary
        product.description = params.description
        product.createdBy = product.id ? product.createdBy : Operator.proxy(AppUtil.loggedOperator)
        List<Category> existentCategories = product.parents
        List<Category> newCategories = params.list('categories').collect { Category.proxy(it) }
        product.parent = params.parent ? Category.proxy(params.parent.toLong(0)) : null
        existentCategories.each {
            if (!newCategories.contains(it)) {
                it.products.remove(product)
            }
        }
        newCategories.each {
            if (!existentCategories.contains(it)) {
                it.products.add(product)
            }
        }
        product.save()
        if (!product.hasErrors()) {
            getSessionFactory().cache.evictCollectionRegions()
            if (params.id) {
                AppEventManager.fire("product-update", [params.id])
            } else {
                setCalculatedFields(product)
            }
            return product
        }
        return null
    }

    @Transactional
    boolean saveImages(Product product, List<MultipartFile> images) {
        String filePath = processFilePath(product)
        Integer currentImages = product.images?.size() ?: 0
        images.each {
            String name = processImageName(filePath, it.originalFilename)
            ProductImage image = new ProductImage(name: name, product: product, idx: ++currentImages)
            imageService.uploadImage(it, NamedConstants.IMAGE_RESIZE_TYPE.PRODUCT_IMAGE, image)
            image.save()
            product.addToImages(image)
        }
        product.merge()
        if (!product.hasErrors()) {
            AppEventManager.fire("product-update", [product.id])
            return true
        }
        return false
    }

    String processFilePath(Product product) {
        String filePath = PathManager.getResourceRoot("product/product-${product.id}")
        File dir = new File(filePath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return filePath
    }

    String processImageName(String filePath, String originalFilename) {
        String name = FilenameUtils.getBaseName(originalFilename)
        String attempt = name
        Integer tryCount = 0
        String extension = FilenameUtils.getExtension(originalFilename)
        while (true) {
            File targetFile = new File(filePath, attempt + "." + extension)
            if (!targetFile.exists()) {
                break
            }
            attempt = name + "_" + (++tryCount)
        }
        return attempt + "." + extension
    }

    @Transactional
    boolean saveVideos(Product product, List<MultipartFile> videos) {
        def filePath = PathManager.getResourceRoot("product/product-${product.id}")
        File dir = new File(filePath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        Integer currentVideos = product.videos.size()
        videos.each {
            String name = FilenameUtils.getBaseName(it.originalFilename)
            String attempt = name
            Integer tryCount = 0
            String extension = FilenameUtils.getExtension(it.originalFilename)
            while (true) {
                File targetFile = new File(filePath, attempt + "." + extension)
                if (!targetFile.exists()) {
                    break
                }
                attempt = name + "_" + (++tryCount)
            }
            name = attempt + "." + extension
            videoService.uploadVideo(it, filePath, extension, name, 50 * 1024 * 1024)
            ProductVideo video = new ProductVideo(name: name, title: name, product: product, idx: ++currentVideos)
            video.save()
            product.addToVideos(video)
        }
        product.merge()
        return !product.hasErrors()
    }

    @Transactional
    boolean updateImageProperty(Map params) {
        ProductImage image = ProductImage.get(params.id)
        if (!image) {
            return false
        }
        image.altText = params.altText
        image.merge()
        return !image.hasErrors()
    }

    @Transactional
    Boolean removeProductImages(List<Long> imgIds) {
        boolean success
        if (imgIds.size() > 0) {
            def dCriteria = ProductImage.where {
                id in imgIds
            }
            List<ProductImage> productImages = dCriteria.list()
            success = dCriteria.deleteAll() > 0
            productImages*.afterDelete()
        } else {
            success = true
        }
        return success
    }

    @Transactional
    boolean updateImages(Map params) {
        Product product = Product.get(params.id)
        if (!product) {
            return false
        }
        List<Long> imgIds = params.list("remove-images")*.toLong()
        List<String> names = imgIds.size() > 0 ? ProductImage.where {
            id in imgIds
        }.list().name : []
        boolean success = removeProductImages(imgIds)
        List orderSeq = params.list("imageId")
        List ids = params.list("altTextId")
        List altTexts = params.list("altText")
        ids.eachWithIndex { def entry, int i ->
            ProductImage img = ProductImage.get(entry)
            img.altText = altTexts[i]
        }
        orderSeq.eachWithIndex { def entry, int i ->
            ProductImage img = ProductImage.get(entry)
            img.idx = i + 1
            img.merge(flush: true)
        }
        if (success) {
            AppEventManager.fire("product-update", [params.id])
            getSessionFactory().cache.evictCollectionRegions()
            return true
        }
        return false
    }

    @Transactional
    Boolean setDefaultImage(Long productId, Long imageId) {
        Product product = Product.get(productId)
        Boolean notFound = true
        product.images.sort { it.idx }.each {
            if (it.id == imageId) {
                it.idx = 1
                notFound = false
            } else if (notFound) {
                it.idx++
            } else {
                it.idx--
            }
            it.merge(flush: true)
        }
        if (notFound) {
            throw new ApplicationRuntimeException("image.not.found")
        }
    }

    @Transactional
    boolean updateVideos(Map params) {
        Product product = Product.get(params.id)
        if (!product) {
            return false
        }
        List<Long> videosIds = params.list("remove-videos")*.toLong()
        List<String> names = videosIds.size() > 0 ? ProductVideo.where {
            id in videosIds
        }.list().name : []

        boolean success
        if (videosIds.size() > 0) {
            success = ProductVideo.where {
                id in videosIds
            }.deleteAll() > 0
            if (success) {
                names.each { name ->
                    File videoFile = new File(PathManager.getResourceRoot("product/product-${product.id}/${name}"))
                    videoFile.delete()

                    String thumb = name.substring(0, name.lastIndexOf(".")) + ".jpg"
                    File imageFile = new File(PathManager.getResourceRoot("product/product-${product.id}/$AppResourceTagLib.VIDEO_THUMB/${thumb}"))
                    if (imageFile && imageFile.exists()) {
                        imageFile.delete()
                    }

                }
            }
        } else {
            success = true
        }
        if (success) {
            AppEventManager.fire("product-update", [params.id])
            return true
        }
        return false
    }

    @Transactional
    Map savePriceNQuantity(TypeConvertingMap params) {
        Product product = Product.get(params.id)
        if (!product) {
            return [status: false]
        }
        if (!params.isInventoryEnabled) {
            product.isInventoryEnabled = false
        } else {
            product.isInventoryEnabled = true
            product.lowStockLevel = params.int("lowStockLevel")
        }
        if (params.isMultipleOrderQuantity) {
            product.isMultipleOrderQuantity = true
            product.multipleOfOrderQuantity = params.int("multipleOrderQuantity")
        } else {
            product.isMultipleOrderQuantity = false
        }
        product.minOrderQuantity = params.int("minOrderQuantity")
        product.maxOrderQuantity = params.int("maxOrderQuantity")
        if (params.adjust.changeQuantity) {
            Integer changeQuantity = params.int("adjust.changeQuantity")
            String note = params.adjust.note ?: null
            Operator createdBy = Operator.get(AppUtil.session.admin)
            if (changeQuantity != 0) {
                ProductInventoryAdjustment pia = new ProductInventoryAdjustment(changeQuantity: changeQuantity, createdBy: createdBy, note: note, product: product)
                pia.save()
                if (!pia.hasErrors()) {
                    product.availableStock += changeQuantity
                    product.addToInventoryAdjustments(pia)
                }
            }
        }
        if (params.taxProfile) {
            product.taxProfile = TaxProfile.proxy(params.taxProfile)
        } else {
            product.taxProfile = null
        }
        if (params['shipping-profile']) {
            product.shippingProfile = ShippingProfile.proxy(params["shipping-profile"])
        } else {
            product.shippingProfile = null
        }
        if(product.productType != DomainConstants.PRODUCT_TYPE.DOWNLOADABLE) {
            def generalSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL)
            String unitLength = generalSettings.unit_length
            String unitWeight = generalSettings.unit_weight
            product.length = LengthConversions.convertLengthToSI(unitLength, params.double("length", 0)).toDouble()
            product.width = LengthConversions.convertLengthToSI(unitLength, params.double("width", 0)).toDouble()
            product.height = LengthConversions.convertLengthToSI(unitLength, params.double("height", 0)).toDouble()
            product.weight = MassConversions.convertMassToSI(unitWeight, params.double("weight", 0)).toDouble()
        }
        product.isCallForPriceEnabled = params.isCallForPriceEnabled.toBoolean()
        if (params.isExpectToPay) {
            product.isExpectToPay = true
            product.expectToPayPrice = params.expectToPayPrice.toDouble()
            if(product.expectToPayPrice <= product.basePrice) {
                throw new ApplicationRuntimeException("x.should.greater.then.y", [g.message(code: "expect.to.pay"), g.message(code: "base.price")])
            }
            params.isOnSale = false
        } else {
            product.isExpectToPay = false
        }
        if (params.isOnSale) {
            product.isOnSale = true
            product.salePrice = params.salePrice.toDouble()
        } else {
            product.isOnSale = false
        }
        product.restrictPriceFor = params.restrictPrice ? params.restrictPriceFor : DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.NONE
        if (params.restrictPrice && params.restrictPriceFor == DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.EXCEPT_SELECTED) {
            product.restrictPriceExceptCustomers = []
            product.restrictPriceExceptCustomerGroups = []
            if (params.restrictPriceExceptCustomer) {
                product.restrictPriceExceptCustomers = Customer.where {
                    id in params.list("restrictPriceExceptCustomer").collect { it.toLong() }
                    order("id", "asc")
                }.list()
            }
            if (params.restrictPriceExceptCustomerGroup) {
                product.restrictPriceExceptCustomerGroups = CustomerGroup.where {
                    id in params.list("restrictPriceExceptCustomerGroup").collect { it.toLong() }
                    order("id", "asc")
                }.list()
            }
        } else {
            product.restrictPriceExceptCustomers = []
            product.restrictPriceExceptCustomerGroups = []
        }

        product.restrictPurchaseFor = params.restrictPurchase ? params.restrictPurchaseFor : DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.NONE
        if (params.restrictPurchase && params.restrictPurchaseFor == DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.EXCEPT_SELECTED) {
            product.restrictPurchaseExceptCustomers = []
            product.restrictPurchaseExceptCustomerGroups = []
            if (params.restrictPurchaseExceptCustomer) {
                product.restrictPurchaseExceptCustomers = Customer.where {
                    id in params.list("restrictPurchaseExceptCustomer").collect { it.toLong() }
                    order("id", "asc")
                }.list()
            }
            if (params.restrictPurchaseExceptCustomerGroup) {
                product.restrictPurchaseExceptCustomerGroups = CustomerGroup.where {
                    id in params.list("restrictPurchaseExceptCustomerGroup").collect { it.toLong() }
                    order("id", "asc")
                }.list()
            }
        } else {
            product.restrictPurchaseExceptCustomers = []
            product.restrictPurchaseExceptCustomerGroups = []
        }
        product.model = params.model
        product.isFeatured = params.isFeatured.toBoolean()
        product.isNew = params.isNew.toBoolean()
        product.merge()
        if (!product.hasErrors()) {
            AppEventManager.fire("product-update", [params.id])
        }

        Map resp = [status: true]
        resp = HookManager.hook("savePriceNQuantity", resp, params)

        return [status: !product.hasErrors(), availableStock: product.availableStock]
    }

    @Transactional
    Map saveMultiStore(TypeConvertingMap params) {
        Product product = Product.get(params.id.toLong())
        List<Long> ids = params.list("selected").collect { it.toLong() }
        if (!product) {
            return [status: false]
        }

        Map resp = [status: false]
        def storeAssocMap = params.storeAssoc
        try {
            StoreProductAssoc.createCriteria().list {
                eq("product", product)
            }*.delete()

            ids.each { id ->
                /*double price = storeAssocMap["${id}.price"]?.toDouble()
                Integer availableStock = storeAssocMap["${id}.quantity"]?.toLong()*/
                StoreDetail store = StoreDetail.get(id)

                StoreProductAssoc storeProductAssoc = new StoreProductAssoc()
                storeProductAssoc.product = product
                storeProductAssoc.store = store
                /*storeProductAssoc.price = price
                storeProductAssoc.availableStock = availableStock*/
                storeProductAssoc.save()
            }
            resp = [status: true]
        } catch (Exception e) {
            resp = [status: false]
        }
        return resp
    }

    @Transactional
    boolean saveRelated(TypeConvertingMap params) {
        Product product = Product.get(params.id)
        if (!product) {
            return false
        }
        List<Long> products = params.list("related").collect { it.toLong(0) }
        product.relatedProducts.clear()
        def tempProduct
        products.each {
            tempProduct = Product.get(it)
            product.relatedProducts.add(tempProduct)
        }
        product.merge()
        if (!product.hasErrors()) {
            AppEventManager.fire("product-update", [params.id])
            return true
        }
        return false
    }

    @Transactional
    Boolean saveIncluded(Map params) {
        Product base = Product.proxy(params.long("id"))
        try {
            CombinedProduct.where {
                baseProduct == base
            }.deleteAll()
            List includedProducts = params.list("included")
            List labels = params.list("label")
            List quantities = params.list("quantity")
            List prices = params.list("price")
            includedProducts.eachWithIndex { def entry, int i ->
                CombinedProduct combinedProduct = new CombinedProduct()
                combinedProduct.label = labels[i]
                combinedProduct.baseProduct = base
                combinedProduct.includedProduct = Product.proxy(entry)
                combinedProduct.quantity = base.productType != DomainConstants.PRODUCT_TYPE.DOWNLOADABLE ? quantities[i].toInteger() : 1
                if (!base.isCombinationPriceFixed) {
                    combinedProduct.price = prices[i] ? prices[i].toDouble() : null
                }
                combinedProduct.save()
            }
            return true
        } catch (Exception e) {
            return false
        }
    }

    @Transactional
    Boolean saveProfiles(Map params) {
        Product product = Product.get(params.id)
        if (params.taxProfile) {
            product.taxProfile = TaxProfile.proxy(params.taxProfile)
        } else {
            product.taxProfile = null
        }
        if (params['shipping-profile']) {
            product.shippingProfile = ShippingProfile.proxy(params["shipping-profile"])
        } else {
            product.shippingProfile = null
        }
        product.merge()
        return !product.hasErrors()
    }

    @Transactional
    boolean saveAdvanced(TypeConvertingMap params) {
        Product product = Product.get(params.id)
        if (!product) {
            return false
        }
        product.globalTradeItemNumber = params.globalTradeItemNumber
        if (product.productType != DomainConstants.PRODUCT_TYPE.DOWNLOADABLE) {
            product.productCondition = params.condition
        }
        product.disableGooglePageTracking = params.disableTracking.toBoolean(false)
        product.metaTags*.delete()
        product.metaTags = []
        List<String> names = params.list("tag_name")
        List<String> values = params.list("tag_content")
        for (int i = 0; i < names.size(); i++) {
            MetaTag mt = new MetaTag(name: names[i], value: values[i])
            mt.save()
            product.metaTags.add(mt)
        }
        product.merge()
        Map response = [
                success: true
        ]
        HookManager.hook("saveCategoryAdvancedData", response, params)
        if (response.success) {
            product.merge()
        }
        if (response.success && !product.hasErrors()) {
            AppEventManager.fire("product-update", [params.id])
            return true
        }
        return false
    }

    void setCalculatedFields(Product product) {
        def resolver = product
        while (resolver && resolver.restrictPriceFor == DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.NONE) {
            resolver = resolver.parent
        }
        product.calculatedRestrictPriceExceptCustomers = []
        product.calculatedRestrictPriceExceptCustomerGroups = []
        if (resolver && resolver.restrictPriceFor == DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.EXCEPT_SELECTED) {
            product.calculatedRestrictPriceExceptCustomers.addAll(resolver.restrictPriceExceptCustomers)
            product.calculatedRestrictPriceExceptCustomerGroups.addAll(resolver.restrictPriceExceptCustomerGroups)
        }
        product.calculatedRestrictPriceFor = resolver ? resolver.restrictPriceFor : DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.NONE

        resolver = product
        while (resolver && resolver.restrictPurchaseFor == DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.NONE) {
            resolver = resolver.parent
        }
        product.calculatedRestrictPurchaseExceptCustomers = []
        product.calculatedRestrictPurchaseExceptCustomerGroups = []
        if (resolver && resolver.restrictPurchaseFor == DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.EXCEPT_SELECTED) {
            product.calculatedRestrictPurchaseExceptCustomers.addAll(resolver.restrictPurchaseExceptCustomers)
            product.calculatedRestrictPurchaseExceptCustomerGroups.addAll(resolver.restrictPurchaseExceptCustomerGroups)
        }
        product.calculatedRestrictPurchaseFor = resolver ? resolver.restrictPurchaseFor : DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.NONE
        product.save()
    }

    ProductData getProductData(Product product, Map config = null) {
        List cacheKeys = []
        cacheKeys = HookManager.hook("keys-for-product-data-cache", cacheKeys, product, config)

        // It will resolved after cached issue fixed

        /*ProductData data = CacheManager.get(NamedConstants.CACHE.SCOPE_APP, "product", "product-" + product.id, *cacheKeys)
        if (AppUtil.session.customer) {
            data = CacheManager.get(NamedConstants.CACHE.SCOPE_APP, "product", "product-" + product.id, "customer-" + AppUtil.session.customer, *cacheKeys)
        }*/

        ProductData data = null
        if (data) {
            return data
        }

        data = new ProductData(product, config)

        data = HookManager.hook("cached-product-data", data, product, config)

        // It will resolved after cached issue fixed
        /*
        if (AppUtil.session.customer) {
            CacheManager.cache(NamedConstants.CACHE.SCOPE_APP, data, "product", "product-" + product.id, "customer-" + AppUtil.session.customer, *cacheKeys)
        } else {
            CacheManager.cache(NamedConstants.CACHE.SCOPE_APP, data, "product", "product-" + product.id, *cacheKeys)
        }*/

        data.calculatePrice()

        return data
    }

    List<ProductData> getProductData(List ids, Map filterMap, Boolean isFiltered = false) {
        if (!ids) {
            return []
        }
        List<ProductData> productDataList = new ArrayList<ProductData>()
        def filteredIds = isFiltered ? ids : filterAvailableProducts(ids, filterMap)
        int count = filteredIds.size()
        for (int i = 0; i < count; i++) {
            def object = filteredIds[i]
            Product product = object instanceof Product ? object : Product.get(object)
            def session = AppUtil.session
            StoreDetail store = session.currentStore
            if ((AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "enable_multi_model") == "true") && store) {
                StoreProductAssoc productForStore =  StoreProductAssoc.findByProductAndStore(product, store)
                if (productForStore) {
                    productDataList.add(getProductData(product))
                }
            } else {
                productDataList.add(getProductData(product))
            }
        }
        return productDataList
    }

    List getIncludedProducts(Map params) {
        Map listMap = [max: params.max, offset: params.offset]
        return CombinedProduct.createCriteria().list(listMap) {
            and getCriteriaClosureForIncludedProducts(params)
            order(params.sort ?: "id", params.dir ?: "asc")
        }
    }

    Integer getIncludedProductsCount(Map params) {
        return CombinedProduct.createCriteria().count {
            and getCriteriaClosureForIncludedProducts(params)
        }
    }

    @Transactional
    Product copyProduct(Long id) {
        return _copyProduct(id, false)
    }

    private Product _copyProduct(Long id, boolean skuFromBase, boolean copyForEvariation = false) {
        Integer maxIdx = Product.createCriteria().get {
            projections {
                max("idx")
            }
        }
        Product product = Product.get(id)
        String copiedName = commonService.getCopyNameForDomain(product)
        String copyUrl = commonService.getUrlForDomain(Product, copiedName)
        String newSku = commonService.getSKUForDomain(Product, skuFromBase ? product.sku : null)
        Product newProduct = (Product) DomainUtil.clone(product)
        newProduct.sku = newSku
        newProduct.url = copyUrl
        newProduct.name = copiedName
        newProduct.createdBy = Operator.proxy(AppUtil.session.admin)
        newProduct.idx = maxIdx + 1
        newProduct.save()
        product.parents.each {
            it.products.add(newProduct)
        }
        newProduct.images = []
        Map imageIdMap = [:]
        product.images.each {
            ProductImage image = new ProductImage(name: it.name, altText: it.altText, product: newProduct, idx: it.idx).save()
            imageIdMap["" + it.id] = image
            newProduct.addToImages(image)
        }
        newProduct.videos = []
        product.videos.each {
            ProductVideo video = new ProductVideo(name: it.name, title: it.title, product: newProduct, idx: it.idx, description: it.description).save()
            newProduct.addToVideos(video)
        }
        newProduct.merge()
        if (newProduct.id) {
            AppEventManager.fire("product-copy", [product, newProduct])
            copyProductResourceDirectory(product, newProduct)
        }
        return newProduct.hasErrors() ? null : newProduct
    }

    private void copyProductResourceDirectory(Product source, Product target) {
        File targetDir = new File(PathManager.getResourceRoot("product/product-${target.id}"))
        File srcDir = new File(PathManager.getResourceRoot("product/product-${source.id}"))
        if (srcDir.exists()) {
            targetDir.mkdirs()
            FileUtils.copyDirectory(srcDir, targetDir)
        }
    }

    @Transactional
    boolean deleteProduct(Long id) {
        Product product = Product.get(id)
        if (!product) {
            return false
        }
        product.delete()
        return !product.hasErrors()
    }

    @Transactional
    boolean putProductInTrash(Long id, String at2_reply, String at1_reply) {
        TrashUtil.preProcessPutInTrash("product", id, at2_reply != null, at1_reply != null)
        return trashService.putObjectInTrash("product", Product.get(id), at2_reply)
    }

    @Transactional
    def putSelectedProductsInTrash(List<String> ids) {
        def result = true
        ids.each { id ->
            Product product = Product.proxy(id)
            if (!putProductInTrash(id, "yes", "include")) {
                result = false
                return true
            }
        }
        return result
    }

    Long countProductsInTrash() {
        return Product.createCriteria().count {
            eq("isInTrash", true)
        }
    }

    Long countProductsInTrash(Map params) {
        return Product.createCriteria().count(getCriteriaClosureForTrash(params))
    }

    Map getProductsInTrash(int offset, int max, String sort, String dir) {
        Map listMap = [max: max, offset: offset, sort: sort, dir: dir]
        return getProductsInTrash(listMap)
    }

    Map getProductsInTrash(Map params) {
        Map listMap = [max: params.max, offset: params.offset]
        return [Product: Product.createCriteria().list(listMap) {
            and getCriteriaClosureForTrash(params)
            order(params.sort ?: "name", params.dir ?: "asc")
        }.collect {
            [id: it.id, name: it.name, updated: it.updated]
        }]
    }

    @Transactional
    boolean restoreProductFromTrash(Long id) {
        Product product = Product.get(id)
        if (!product) {
            return false
        }
        product.isInTrash = false
        product.merge()
        return !product.hasErrors()
    }

    @Transactional
    Long restoreProductFromTrash(String sku) {
        Product product = Product.findBySku(sku)
        product.isInTrash = false
        product.merge()
        return product.id
    }

    @Transactional
    boolean deleteTrashItemAndSaveCurrent(def field, def value) {
        Product product = Product.createCriteria().get {
            eq(field, value)
        }
        deleteProduct(product.id)
        return !product.hasErrors()
    }

    @Transactional
    void changeOrder(Long id, Integer newOrderValue) {
        Product product = Product.get(id)
        product.idx = newOrderValue
        product.merge()
    }

    @Transactional
    boolean saveCurrentOrder(Map params) {
        Integer idx = 1
        Product.createCriteria().list {
            order(params.sort, params.dir)
            if (params.parent == "root") {
                isNull("parent")
            } else {
                eq("parent", Category.proxy(params.parent))
            }
        }.each {
            it.idx = idx++
            it.merge()
        }
        return true
    }

    Product getAvailableProductByUrl(String url, Boolean forAdmin) {
        return getAvailabilityFilterCriteria(null, [url: url, forAdmin: forAdmin, noCustomerCheck: true]).get()
    }

    Product getProductIfAvailable(Long id, filterMap) {
        return getAvailabilityFilterCriteria([id], filterMap).get()
    }

    DetachedCriteria getAvailabilityFilterCriteria(List<Long> ids, Map filterMap) {
        Long customerId = filterMap.customer ?: AppUtil.loggedCustomer
        Date currentGmtDate = new Date().gmt()
        boolean isOutOfStockHidden = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "show_out_of_stock_products") == "false"
        def query = Product.where {
            if (ids) {
                id in ids
            }
            if (!AppUtil.request.editMode && !filterMap.forAdmin) {
                if (filterMap.isOutOfStock) {
                    (isInventoryEnabled == true) && (availableStock < 0)
                } else if (isOutOfStockHidden) {
                    (isInventoryEnabled == false) || (availableStock > 0)
                }
                isInTrash == false
                isParentInTrash == false
                isActive == true
                isDisposable == false
                isAvailable == true && (
                        isAvailableOnDateRange == false || (
                                (availableFromDate == null || availableFromDate <= currentGmtDate) && (
                                        availableToDate == null || availableToDate >= currentGmtDate
                                )
                        )
                )
                if (!filterMap.noCustomerCheck) {
                    if (customerId) {
                        (
                                availableFor == DomainConstants.CATEGORY_AVAILABLE_FOR.EVERYONE ||
                                        availableFor == DomainConstants.CATEGORY_AVAILABLE_FOR.CUSTOMER ||
                                        exists(Product.where {
                                            def pd1 = Product
                                            eqProperty "pd1.id", "prdt.id"
                                            availableToCustomers.id == customerId
                                        }.id()) ||
                                        exists(Product.where {
                                            def pd2 = Product
                                            eqProperty "pd2.id", "prdt.id"
                                            availableToCustomerGroups.id in CustomerGroup.where {
                                                customers.id == customerId
                                            }.id()
                                        }.id())
                        )
                    } else {
                        availableFor == DomainConstants.CATEGORY_AVAILABLE_FOR.EVERYONE
                    }
                }
            }
            if (filterMap.onSale) {
                isOnSale == true
            }
            if (filterMap.isFeatured) {
                isFeatured == true
            }
            if (filterMap.isCallForPrice) {
                isCallForPriceEnabled == true
            }
            if (filterMap.name) {
                like "name", "%" + filterMap.name.trim().encodeAsLikeText() + "%"
            }
            if (filterMap.url) {
                url == filterMap.url
            }
        }
        query.alias = "prdt"
        query = HookManager.hook("availableProductsFilterCriteria", query, filterMap)
        return query
    }

    List filterAvailableProducts(List<Long> ids, Map filterMap) {
        if (ids != null && !ids.size()) {
            return []
        }
        DetachedCriteria query
        if (filterMap.rawProduct) {
            query = getAvailabilityFilterCriteria(ids, filterMap)
        } else {
            query = getAvailabilityFilterCriteria(ids, filterMap).id()
        }
        query.with {
            if (filterMap["product-sorting"]) {
                if (filterMap["product-sorting"] == "ALPHA_ASC") {
                    order("name", "asc")
                } else if (filterMap["product-sorting"] == "ALPHA_DESC") {
                    order("name", "desc")
                } else if (filterMap["product-sorting"] == "PRICE_ASC" || filterMap["product-sorting"] == "PRICE_DESC") {
                    boolean asc = filterMap["product-sorting"] == "PRICE_ASC"
                    query = new ExpressionOrderSupportedDetachedCriteria(query)
                    query.addOrder(new CaseExpressionOrder({
                        check {
                            or {
                                eq "isCombined", false
                                eq "isCombinationPriceFixed", true
                            }
                        }
                        match {
                            "case" {
                                check {
                                    eq "isOnSale", true
                                }
                                match {
                                    property "salePrice"
                                }
                                otherwise {
                                    property "basePrice"
                                }
                            }
                        }
                        otherwise {
                            sub(CombinedProduct.where {
                                def c = CombinedProduct
                                eqProperty "c.baseProduct.id", "prdt.id"
                            }.sum(DoubleType.INSTANCE) {
                                "case" {
                                    check {
                                        isNull "c.price"
                                    }
                                    match {
                                        sub(Product.where {
                                            def pincl = Product
                                            eqProperty "pincl.id", "c.includedProduct.id"
                                        }.iff(DoubleType.INSTANCE) {
                                            check {
                                                eq "pincl.isOnSale", true
                                            }
                                            match {
                                                property "pincl.salePrice"
                                            }
                                            otherwise {
                                                property "pincl.basePrice"
                                            }
                                        })
                                    }
                                    otherwise {
                                        property "c.price"
                                    }
                                }
                            })
                        }
                    }, asc))
                }
            } else {
                order('idx', "asc")
                order('name', "asc")
            }
        }
        query = HookManager.hook("availableProductsFilterList", query, filterMap)
        return query.list([offset: filterMap["offset"] ?: 0, max: filterMap["max"] ?: -1])
    }

    Integer filterOutAvailableProductCount(List<Long> ids, Map filterMap) {
        if (ids != null && !ids.size()) {
            return 0
        }
        DetachedCriteria query = getAvailabilityFilterCriteria(ids, filterMap)
        query = HookManager.hook("availableProductsFilterCount", query, filterMap)
        query.count()
    }

    def getEntitiesInPages(String likeText, Integer offset, Integer max) {
        return getEntitiesInPages(likeText, null, offset, max, false)
    }

    def getEntitiesInPages(String likeText, Boolean isCount) {
        return getEntitiesInPages(likeText, null, 0, -1, isCount)
    }

    def getEntitiesInPages(String likeText, Long categoryId, Integer offset, Integer max, Boolean isCount = false) {
        def searchConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SEARCH_PAGE)
        Boolean forWidget = searchConfig.entities == "added_to_widget"
        if (forWidget) {
            return _getEntitiesInPages(likeText, categoryId, isCount, offset, max)
        } else {
            return getAllMatchedProducts(likeText, categoryId, isCount, offset, max)
        }
    }

    def getEntitiesInPages(String likeText, Long categoryId, Boolean isCount) {
        return getEntitiesInPages(likeText, categoryId, 0, -1, isCount)
    }

    private def _getEntitiesInPages(String likeText, Long categoryId, Boolean isCount, Integer offset, Integer max) {
        likeText = likeText.encodeAsHQLLikeText()
        List cateIds = []
        if (categoryId) {
            cateIds.add(categoryId)
            Closure addRecursive
            addRecursive = { _catId ->
                Category.createCriteria().list {
                    eq("parent.id", _catId)
                    projections {
                        property("id")
                    }
                }.each { id ->
                    cateIds.add(id)
                    addRecursive(id)
                }
            }
            addRecursive(categoryId)
        }
        Date currentGmtDate = new Date().gmt()
        Long cid = AppUtil.session.customer
        String customerSql = cid ? ("or (PG.visibility = '${DomainConstants.PAGE_VISIBILITY.RESTRICTED}' and (exists (select C.id from PG.customers C where $cid = C.id) or exists (select G.id from PG.customerGroups G where exists (select GC.id from G.customers GC where $cid = GC.id))))") : ""
        String pageAvailabilitySql = "(select PG.id from Page PG where PG.id = WC.widget.containerId and (PG.visibility = '${DomainConstants.PAGE_VISIBILITY.OPEN}' $customerSql))"
        String customerForLayoutSql = cid ? ("or (LP.visibility = '${DomainConstants.PAGE_VISIBILITY.RESTRICTED}' and (exists (select LPC.id from LP.customers LPC where $cid = LPC.id) or exists (select LPG.id from LP.customerGroups LPG where exists (select LPGC.id from LPG.customers LPGC where $cid = LPGC.id))))") : ""
        String layoutForPageAvailabilitySql = "(select LP.id from Page LP where LP.layout.id = WC.widget.containerId and (LP.visibility = '${DomainConstants.PAGE_VISIBILITY.OPEN}' $customerForLayoutSql))"
        String layoutForAutoPageAvailabilitySql = "(select LAP.id from AutoGeneratedPage LAP where LAP.layout.id = WC.widget.containerId)"
        String projection = isCount ? "count(P)" : "P"
        if (max == -1) {
            max = Integer.MAX_VALUE
        }
        Map limit = isCount ? [:] : [offset: offset, max: max]
        String productForCustomer = cid ? ("or P.availableFor = '${DomainConstants.PRODUCT_AVAILABLE_FOR.CUSTOMER}' or (P.availableFor = '${DomainConstants.PRODUCT_AVAILABLE_FOR.SELECTED}' and (exists (select AC.id from P.availableToCustomers " + "AC where AC.id = $cid) or exists (select AG.id from P.availableToCustomerGroups AG where (exists (select AGC.id from AG.customers AGC where AGC.id = $cid)))))") : ""
        String productForDate = "and (P.isAvailableOnDateRange = 0 or ((P.availableFromDate is NULL or P.availableFromDate <= :fdate) and (P.availableToDate is NULL or P.availableToDate >= :tdate)))"
        boolean isOutOfStockHidden = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "show_out_of_stock_products") == "false"
        String availableStock = isOutOfStockHidden ? "and (P.isInventoryEnabled = false OR P.availableStock > 0)" : ""
        def result = Product.executeQuery("select $projection from Product P where P.isActive = 1 and P.isParentInTrash = 0 and P.isDisposable = 0 and P.isInTrash = 0 $productForDate and P.isAvailable = 1 and (P.availableFor = '" + DomainConstants.PRODUCT_AVAILABLE_FOR.EVERYONE + "' $productForCustomer)" + (cateIds.size() ? " and P.parent.id in (" + cateIds.join(",") + ")" : "") + " and P.name like '%$likeText%' $availableStock and P.id in (select WC.contentId from WidgetContent WC where WC.type = '${DomainConstants.WIDGET_CONTENT_TYPE.PRODUCT}' and (exists $pageAvailabilitySql or exists $layoutForPageAvailabilitySql or exists $layoutForAutoPageAvailabilitySql))", [fdate: currentGmtDate, tdate: currentGmtDate + 1], limit)
        return isCount ? result[0] : result
    }

    def getAllMatchedProducts(String likeText, Long categoryId, Boolean isCount, Integer offset, Integer max) {
        likeText = likeText.encodeAsHQLLikeText()
        List cateIds = []
        if (categoryId) {
            cateIds.add(categoryId)
            Closure addRecursive
            addRecursive = { _catId ->
                Category.createCriteria().list {
                    eq("parent.id", _catId)
                    projections {
                        property("id")
                    }
                }.each { id ->
                    cateIds.add(id)
                    addRecursive(id)
                }
            }
            addRecursive(categoryId)
        }
        Date currentGmtDate = new Date().gmt()
        Long cid = AppUtil.session.customer
        String projection = isCount ? "count(P)" : "P"
        if (max == -1) {
            max = Integer.MAX_VALUE
        }

        Map limit = isCount ? [:] : [offset: offset, max: max]
        String productForCustomer = cid ? ("or P.availableFor = '${DomainConstants.PRODUCT_AVAILABLE_FOR.CUSTOMER}' or (P.availableFor = '${DomainConstants.PRODUCT_AVAILABLE_FOR.SELECTED}' and (exists (select AC.id from P.availableToCustomers "
                + "AC where AC.id = $cid) or exists (select AG.id from P.availableToCustomerGroups AG where (exists (select AGC.id from AG.customers AGC where AGC.id = $cid)))))") : ""
        String productForDate = "and (P.isAvailableOnDateRange = 0 or ((P.availableFromDate is NULL or P.availableFromDate <= :fdate) and (P.availableToDate is NULL or P.availableToDate >= :tdate)))"
        boolean isOutOfStockHidden = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "show_out_of_stock_products") == "false"
        String availableStock = isOutOfStockHidden ? "and (P.isInventoryEnabled = false OR P.availableStock > 0)" : ""

        String parentQuery = cateIds.size() ? " and (P.parent.id in (${cateIds.join(",")}) or exists(from P.parents pa where pa.id in (${cateIds.join(",")})) )" : ""

        def result = Product.executeQuery("select $projection from Product P where P.isActive = 1 and P.isParentInTrash = 0 and P.isDisposable = 0 and P.isInTrash = 0 $productForDate $availableStock and P.isAvailable = 1 and (P.availableFor = '" +
                DomainConstants.PRODUCT_AVAILABLE_FOR.EVERYONE + "' $productForCustomer)" + parentQuery +
                " and P.name like '%$likeText%'", [fdate: currentGmtDate, tdate: currentGmtDate + 1], limit)
        return isCount ? result[0] : result
    }

    def sendMailToFriend(Map params) {
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("tell-friend")
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "customer_message":
                    refinedMacros[it.key] = params.message
                    break
                case "recommended_url":
                    refinedMacros[it.key] = app.baseUrl() + "product/" + Product.get(params.id.toLong()).url
                    break
                case "from_email":
                    refinedMacros[it.key] = params.sender
                    break
            }
        }
        commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, params.receiver, params.sender)
    }

    @Transactional
    def updateStock(Long productId, Integer quantity, String note, Map config = null) {
        Product product = Product.get(productId)
        Boolean updated = false
        updated = HookManager.hook("before-update-stock", updated, product, config, quantity, note)
        if (!product) {
            return false
        }
        if (!updated  && product.isInventoryEnabled) {
            ProductInventoryAdjustment adjustment = new ProductInventoryAdjustment()
            adjustment.changeQuantity = quantity * -1
            adjustment.product = product
            adjustment.note = note
            adjustment.save()
            product.availableStock = product.availableStock - quantity
            product.save()
            updated = true
        }
        if (!product.hasErrors() && updated) {
            AppEventManager.fire("product-update", [product.id])
            checkProductStockForNotification(product)
            return true
        }
    }

    def checkProductStockForNotification(Product product){
        Boolean enabledLowStockNotification = (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "low_stock_notification") == "true")
        Boolean enabledOutOfStockNotification = (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "out_of_stock_notification") == "true")

        if(enabledLowStockNotification){
            Integer availableStock = product.availableStock
            Integer lowStock = product.lowStockLevel
            if(availableStock <= lowStock){
                sendStockNotification(product, "low-stock-notification", "low_stock_notification_json")
            }
        }
        if(enabledOutOfStockNotification){
            Integer availableStock = product.availableStock
            if(availableStock < 1){
                sendStockNotification(product, "out-of-stock-notification", "out_of_stock_notification_json")
            }
        }
    }

    def sendStockNotification(Product product, String emailIdentifier, String configKey){

        Long productId = product.id

        def json = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, configKey)

        if(!json){
            sendStockNotificationMail(emailIdentifier, product)
            updateStockNotificationJson(configKey, ["${productId}": productId])
        } else {
            JsonSlurper slurper = new JsonSlurper()
            def retrievedJsonData = slurper.parseText(json)
            Map retrievedProducts = retrievedJsonData.products

            Date updated = new Date(retrievedJsonData.date.toLong())
            Date today = new Date()

            Boolean isSameDay = (today.calendarDate.dayOfMonth == updated.calendarDate.dayOfMonth)
            Boolean isSameMonth = (today.calendarDate.month == updated.calendarDate.month)
            Boolean isSameYear = (today.calendarDate.year == updated.calendarDate.year)

            if(isSameDay && isSameMonth && isSameYear){
                String key = productId.toString()
                if(retrievedProducts.containsKey(key)){
                    return
                } else{
                    sendStockNotificationMail(emailIdentifier, product)
                    Map productData = retrievedProducts
                    productData.put("${productId}", productId)
                    updateStockNotificationJson(configKey, productData)
                }
            } else{
                sendStockNotificationMail(emailIdentifier, product)
                updateStockNotificationJson(configKey, ["${productId}": productId])
            }
        }
    }

    def sendStockNotificationMail(String emailIdentifier, Product product){
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier(emailIdentifier)
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "product_name":
                    refinedMacros[it.key] = product.name
                    break
                case "product_quantity":
                    refinedMacros[it.key] = product.availableStock
                    break
            }
        }
        String receiver = StoreDetail.first().address.email
        String sender = null
        commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, receiver, sender)
    }

    def updateStockNotificationJson(String configKey, Map productData){
        Map data = [:]
        data.put("date", new Date().getTime())
        data.put("products", productData)

        def configs = []
        configs.add([type: DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, configKey: configKey, value: (data as JSON)])
        configService.update(configs)
    }

    def checkToSendStockReport(){

        Boolean enabledLowStockNotification = (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "low_stock_notification") == "true")
        Boolean enabledOutOfStockNotification = (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "out_of_stock_notification") == "true")

        if(enabledLowStockNotification){
            sendStockReportMail("low-stock-report", getProducts([stock: 'low']))
        }

        if(enabledOutOfStockNotification){
            sendStockReportMail("out-of-stock-report", getProducts([stock: 'out']))
        }
    }

    def sendStockReportMail(String emailIdentifier, List<Product> products){

        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier(emailIdentifier)
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "products":
                    List productList = []
                    products.each {
                        Map product = [:]
                        product.name= it.name
                        product.quantity = (it.availableStock == 0) ? "0" : it.availableStock
                        productList.add(product)
                    }
                    refinedMacros[it.key] = productList
                    break
                case "product_count":
                    refinedMacros[it.key] = products.size()
                    break
            }
        }
        String receiver = StoreDetail.first().address.email
        commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, receiver, null)
    }

    Double getCombinationPrice(Product product, Map quantityMap = [:]) {
        if (product.isCombinationPriceFixed) {
            return null
        }
        List<CombinedProduct> combinedProducts = CombinedProduct.where {
            baseProduct == product
        }.list()
        Double price = 0
        combinedProducts.eachWithIndex { CombinedProduct it, int i ->
            int quantity = (quantityMap["" + it.id] ?: ("" + it.quantity)).toInteger()
            if (it.price) {
                price += it.price * quantity
            } else {
                Product _product = it.includedProduct
                price += (_product.isOnSale ? _product.salePrice : _product.basePrice) * quantity
            }
        }
        return price
    }

    List getInventoryHistory(Map params) {
        Long id = params.long("id")
        Product product = Product.get(id)
        Map listMap = [max: params.max, offset: params.offset]
        List<ProductInventoryAdjustment> histories = ProductInventoryAdjustment.createCriteria().list(listMap) {
            and {
                eq("product", product)
            }
            order("id", "desc")
        }
        return histories
    }

    Integer getInventoryHistoryCount(Map params) {
        Long id = params.long("id")
        Product product = Product.get(id)
        Integer count = ProductInventoryAdjustment.createCriteria().count {
            and {
                eq("product", product)
            }
        }
        return count
    }

    @Transactional
    boolean specUpload(Product product, MultipartFile specFile) {
        Map params = AppUtil.params
        if (params["remove_spec"] == "true" && product.spec) {
            fileService.removeProductSpec(product.id.toString(), product.spec)
            product.spec.delete()
            product.spec = null
            product.merge()
            return !product.hasErrors()
        }
        String fileName = specFile?.originalFilename
        if (specFile) {
            if (fileName.lastIndexOf('.') < 0) {
                throw new Exception("file.should.contain.extension")
            }
            if (fileName) {
                if (product.spec) {
                    fileService.removeProductSpec(product.id.toString(), product.spec)
                }else{
                    product.spec = new Resource()
                }
                product.spec.name = fileName
                product.spec.cloudConfig = fileService.putProductSpec(specFile.getInputStream(), product)
                try {
                    product.merge()
                } catch (Exception e) {
                    return false
                }
            }
            return !product.hasErrors()
        } else {
            return false
        }
    }

    @Transactional
    boolean updateProductFile(Map params, MultipartFile uploadFile) {
        Product product = Product.get(params.id)
        String productOutdatedRelativeFilePath = appResource.getDownloadableProductTypeFileUrl(productId: product.id, productFileName: product?.productFile?.name, isVariationFile: false)
        if (params.fileRemoved == "true") {
            product.productFile = null
            fileService.removeModifiableResource(productOutdatedRelativeFilePath)
            product.merge()
            return !product.hasErrors()
        } else if (uploadFile) {
            if(product.productFile){
                fileService.removeModifiableResource(productOutdatedRelativeFilePath)
            } else{
                product.productFile = new Resource()
            }
            String fileName = uploadFile?.originalFilename
            String productUpdatedRelativeFilePath = appResource.getDownloadableProductTypeFileUrl(productId: product.id, productFileName: fileName, isVariationFile: false)
            product.productFile.name = fileName
            product.productFile.cloudConfig = fileService.putProductDownloadableFile(uploadFile.inputStream, productUpdatedRelativeFilePath)
        } else {
            return false
        }
        product.merge()
        AppEventManager.fire("product-update", [product.id])
        return !product.hasErrors()
    }

    List<Long> getTopSellingProductIds(Integer max = -1) {
        List<Long> productIds = OrderItem.executeQuery("SELECT item.productId FROM OrderItem item, Product product where item.productId = product.id and product.isInTrash = false and product.isParentInTrash = false group by item.productId order by sum(item.price * item.quantity) * sum(item.quantity) DESC", [max: max])
        return productIds.unique()
    }

    def getProductSearchReasult(Map params) {
        def productDataList, searchConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SEARCH_PAGE)
        int offset = params.int("spx-offset") ?: 0
        int max = params.int("spx-max") ?: searchConfig.item_per_page.toInteger()
        int count = getEntitiesInPages(params.name, params.long("categoryId"), true)
        if (offset >= count) {
            offset = (Math.floor((count - 1) / max) * max).intValue()
        }
        def products = getEntitiesInPages(params.name, params.long("categoryId"), offset, max)
        productDataList = getProductData(products, [:], true)
        Map model = [name: DomainConstants.AUTO_GENERATED_PAGES.SEARCH_RESULT, searchCriteria: params.name, config: searchConfig, productList: productDataList, offset: offset, max: max, count: count, view: "/site/searchedProductListings.gsp"]
        return model
    }

    String getProductFileDownloadToken(OrderItem item,Long variationId = null) {
        InformationEncrypter encrypter = new InformationEncrypter()
        encrypter.hideInfo(item.order.id + "")
        encrypter.hideInfo(item.id + "")
        encrypter.hideInfo(variationId + "")
        return encrypter.toString().encodeAsURL()
    }

    List getCustomDomainList(def domain, boolean rejectTrash = false) {
        List domainList = []
        try {
            domain.createCriteria().list {
                if (rejectTrash) {
                    eq("isInTrash", false)
                }
            }.collect {
                domainList.add([id: it.id, name: it.name])
            }
            return domainList
        } catch (Exception e) {
            return domainList
        }
    }

    @Transactional
    Integer saveBasicBulkProperties(Map properties) {
        List<Long> ids = properties.list("id").collect { it.toLong() }
        List productList = []
        int count = 0
        ids.each {
            Map product = [id: it]
            product << properties[it.toString()]
            productList << product
        }
        productList.each { params ->
            Product product = Product.get(params.id)
            product.name = params.name.trim()
            product.url = params.url.trim()
            if (!commonService.isUnique(product, "url")) {
                throw new ApplicationRuntimeException("product.url.exists", "alert")
            }
            product.heading = params.heading.trim()
            product.isActive = params.isActive.toBoolean(true)
            product.isAvailable = params.isAvailable.toBoolean(true)
            if (!product.isAvailable) {
                product.availableToDate = product.availableFromDate = null
                product.availableToCustomerGroups = []
                product.availableToCustomers = []
            }
            if (product.isCombinationPriceFixed || !product.isCombined) {
                Double basePrice = params.basePrice.toDouble(0.0)
                product.basePrice = basePrice
                product.isCombinationPriceFixed = true
                product.isCombinationQuantityFlexible = false
            } else {
                product.isCombinationQuantityFlexible = product.isCombinationQuantityFlexible && product.productType != DomainConstants.PRODUCT_TYPE.DOWNLOADABLE
                product.isCombinationPriceFixed = false
                product.basePrice = 0.0
            }
            List<Category> existentCategories = product.parents
            def categories = params['categories']
            List<Category> newCategories = categories ? (categories.class.isArray() ? categories.collect {
                Category.proxy(it)
            } : [Category.proxy(categories)]) : []
            product.parent = params.parent ? Category.proxy(params.parent.toLong(0)) : null
            existentCategories.each {
                if (!newCategories.contains(it)) {
                    it.products.remove(product)
                }
            }
            newCategories.each {
                if (!existentCategories.contains(it)) {
                    it.products.add(product)
                }
            }
            product.save()
            if (!product.hasErrors()) {
                getSessionFactory().cache.evictCollectionRegions()
                AppEventManager.fire("product-update", [params.id])
                count++
            }
        }
        return count
    }

    @Transactional
    Integer savePriceStockBulkProperties(Map properties) {
        List<Long> ids = properties.list("id").collect { it.toLong() }
        List productList = []
        int count = 0
        ids.each {
            TypeConvertingMap product = [id: it]
            product << properties[it.toString()]
            productList << product
        }

        productList.each { params ->
            Product product = Product.get(params.id)
            if (!product) {
                return [status: false]
            }
            product.name = params.name.trim()
            product.heading = params.heading.trim()
            if (product.productType != DomainConstants.PRODUCT_TYPE.DOWNLOADABLE) {
                product.minOrderQuantity = params.int("minOrderQuantity")
                product.maxOrderQuantity = params.int("maxOrderQuantity")
                if (params.taxProfile) {
                    product.taxProfile = TaxProfile.proxy(params.taxProfile)
                } else {
                    product.taxProfile = null
                }
                if (params.shippingProfile) {
                    product.shippingProfile = ShippingProfile.proxy(params.shippingProfile)
                } else {
                    product.shippingProfile = null
                }
            } else {
                if (params.taxProfile) {
                    product.taxProfile = TaxProfile.proxy(params.taxProfile)
                } else {
                    product.taxProfile = null
                }
            }
            if (params.isOnSale == "true") {
                product.isOnSale = true
                product.salePrice = params.salePrice.toDouble(0.0)
            } else {
                product.isOnSale = false
            }
            product.restrictPriceFor = params.restrictPriceFor ?: DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.NONE
            if (params.restrictPriceFor == DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.EXCEPT_SELECTED) {
                product.restrictPriceExceptCustomers = []
                product.restrictPriceExceptCustomerGroups = []
                if (properties.customer) {
                    product.restrictPriceExceptCustomers = Customer.where {
                        id in properties.list("customer").collect { it.toLong() }
                        order("id", "asc")
                    }.list()
                }
                if (properties.customerGroup) {
                    product.restrictPriceExceptCustomerGroups = CustomerGroup.where {
                        id in properties.list("customerGroup").collect { it.toLong() }
                        order("id", "asc")
                    }.list()
                }
            } else {
                product.restrictPriceExceptCustomers = []
                product.restrictPriceExceptCustomerGroups = []
            }

            product.restrictPurchaseFor = params.restrictPurchaseFor ?: DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.NONE
            if (params.restrictPurchaseFor == DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.EXCEPT_SELECTED) {
                product.restrictPurchaseExceptCustomers = []
                product.restrictPurchaseExceptCustomerGroups = []
                if (properties.customer) {
                    product.restrictPurchaseExceptCustomers = Customer.where {
                        id in properties.list("customer").collect { it.toLong() }
                        order("id", "asc")
                    }.list()
                }
                if (properties.customerGroup) {
                    product.restrictPurchaseExceptCustomerGroups = CustomerGroup.where {
                        id in properties.list("customerGroup").collect { it.toLong() }
                        order("id", "asc")
                    }.list()
                }
            } else {
                product.restrictPurchaseExceptCustomers = []
                product.restrictPurchaseExceptCustomerGroups = []
            }
            product.merge()
            if (!product.hasErrors()) {
                AppEventManager.fire("product-update", [params.id])
                count++
            }
        }
        return count
    }

    @Transactional
    Integer saveSeoBulkProperties(Map properties) {
        List<Long> ids = properties.list("id").collect { it.toLong() }
        List productList = []
        int count = 0
        ids.each {
            TypeConvertingMap product = [id: it]
            product << properties[it.toString()]
            productList << product
        }

        productList.each { params ->
            Product product = Product.get(params.id)
            product.disableGooglePageTracking = params.disableGooglePageTracking.toBoolean(false)
            product.name = params.name
            product.heading = params.heading
            product.merge()
            if (!product.hasErrors()) {
                AppEventManager.fire("product-update", [params.id])
                count++
            }
        }

        return count
    }

    @Transactional
    Integer saveAdvancedBulkProperties(Map properties) {
        List<Long> ids = properties.list("id").collect { it.toLong() }
        List productList = []
        int count = 0
        ids.each {
            TypeConvertingMap product = [id: it]
            product << properties[it.toString()]
            productList << product
        }

        productList.each { params ->
            Product product = Product.get(params.id)
            product.name = params.name.trim()
            product.heading = params.heading.trim()
            product.productCondition = params.condition.trim()
            Map response = [
                    success: true
            ]
            HookManager.hook("saveCategoryAdvancedData", response, params)
            if (response.success) {
                product.merge()
            }
            if (response.success && !product.hasErrors()) {
                AppEventManager.fire("product-update", [params.id])
                count++
            }
        }
        return count
    }

    List filterSpecialProductIds(Map config) {
        List ids = []
        switch (config["filter-by"]) {
            case DomainConstants.PRODUCT_WIDGET_FILTER.FEATURED:
                ids = Product.createCriteria().list {
                    projections {
                        property("id")
                    }
                    eq("isFeatured", true)
                    eq("isInTrash", false)
                    eq("isParentInTrash", false)
                }
                break
            case DomainConstants.PRODUCT_WIDGET_FILTER.TOP_SELLING:
                ids = getTopSellingProductIds(0)
                break
            case DomainConstants.PRODUCT_WIDGET_FILTER.CATEGORY:
                Category category = Category.get(config.category.toLong(0))
                ids = category?.products?.id
                break
        }
        return ids
    }

    Boolean isPermitted(Long productId, Long customerId) {
        return getProductIfAvailable(productId, [customer: customerId]) != null
    }

    List<ProductData> getSpecialProduct(Map params) {
        Map listMap = [max: params.max ?: 10]
        def criteria = Product.createCriteria()
        List productIds = []
        switch (params.type) {
            case "onSale":
                productIds = criteria.list(listMap) {
                    eq("isOnSale", true)
                    projections {
                        distinct('id')
                    }
                }
                break
            case "featured":
                productIds = criteria.list(listMap) {
                    eq("isFeatured", true)
                    projections {
                        distinct('id')
                    }
                }
                break
            case "new":
                productIds = criteria.list(listMap) {
                    eq("isNew", true)
                    projections {
                        distinct('id')
                    }
                }
                break
            case "topSelling":
                productIds = getTopSellingProductIds(listMap.max)
                break
            case "lastBought":
                productIds = OrderItem.createCriteria().list(listMap) {
                    projections {
                        distinct('productId')
                    }
                    order {
                        eq("customerId", params.customerId)
                    }
                }
                break
            default:
                productIds = criteria.list(listMap) {
                    projections {
                        distinct('id')
                    }
                }
                break
        }

        List<ProductData> productDataList = getProductData(productIds, [max: listMap.max])
        return SortAndSearchUtil.sortInCustomOrder(productDataList, "id", productIds)
    }
}