package com.webcommander.webcommerce

import com.webcommander.ApplicationTagLib
import com.webcommander.admin.Customer
import com.webcommander.admin.CustomerGroup
import com.webcommander.admin.Operator
import com.webcommander.common.MetaTag
import com.webcommander.common.Resource
import com.webcommander.constants.DomainConstants
import com.webcommander.models.ProductData
import com.webcommander.util.AppUtil
import grails.util.Holders
import org.hibernate.SessionFactory

class Product {

    Long id

    String name
    String sku
    String url
    String model
    String summary
    String description
    String globalTradeItemNumber
    String availableFor = DomainConstants.PRODUCT_AVAILABLE_FOR.EVERYONE;
    String restrictPriceFor = DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.NONE;
    String restrictPurchaseFor = DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.NONE;
    String calculatedRestrictPriceFor = DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.NONE;
    String calculatedRestrictPurchaseFor = DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.NONE;
    String title
    String heading
    String productCondition = DomainConstants.PRODUCT_CONDITION.NEW
    Resource spec
    String productType
    Resource productFile

    Double basePrice
    Double salePrice = 0
    Double costPrice = 0
    Double expectToPayPrice = 0
    Double weight = 0
    Double height = 0
    Double length = 0
    Double width = 0

    Boolean isNew = false;
    Boolean isOnSale = false;
    Boolean isExpectToPay = false
    Boolean isActive = true
    Boolean isInTrash = false
    Boolean isVirtual = false
    Boolean isFeatured = false;
    Boolean isCombined = false
    Boolean isAvailable = true
    Boolean isParentInTrash = false
    Boolean isInventoryEnabled = false
    Boolean isCallForPriceEnabled = false
    Boolean isAvailableOnDateRange = false
    Boolean isMultipleOrderQuantity = false
    Boolean isCombinationPriceFixed = false
    Boolean disableGooglePageTracking = false
    Boolean isCombinationQuantityFlexible = false
    Boolean isDisposable = false

    Integer idx = 1
    Integer lowStockLevel = 0
    Integer availableStock = 0
    Integer minOrderQuantity = 1
    Integer maxOrderQuantity = null
    Integer multipleOfOrderQuantity = 1

    Category parent
    TaxProfile taxProfile
    ShippingProfile shippingProfile
    Operator createdBy

    Date availableFromDate
    Date availableToDate
    Date created
    Date updated

    Collection<MetaTag> metaTags = []
    Collection<Category> parents = []
    Collection<ProductInventoryAdjustment> inventoryAdjustments = []
    Collection<ProductImage> images = []
    Collection<ProductVideo> videos = []
    Collection<Product> relatedProducts = []

    Collection<Customer> availableToCustomers = []
    Collection<CustomerGroup> availableToCustomerGroups = []
    Collection<Customer> restrictPriceExceptCustomers = []
    Collection<CustomerGroup> restrictPriceExceptCustomerGroups = []
    Collection<Customer> restrictPurchaseExceptCustomers = []
    Collection<CustomerGroup> restrictPurchaseExceptCustomerGroups = []
    Collection<Customer> calculatedRestrictPriceExceptCustomers = []
    Collection<CustomerGroup> calculatedRestrictPriceExceptCustomerGroups = []
    Collection<Customer> calculatedRestrictPurchaseExceptCustomers = []
    Collection<CustomerGroup> calculatedRestrictPurchaseExceptCustomerGroups = []

    static marshallerExclude = ["inventoryAdjustments"]
    static marshallerNameMapping = [parent: "category", parents: "categories"]
    static marshallerListEntryNames = [inventoryAdjustments: "inventoryAdjustment"]

    private static ApplicationTagLib _app
    private static SessionFactory _sessionFactory
    private static ProductService _productService

    private static ApplicationTagLib getApp() {
        return _app ?: (_app = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib))
    }

    private static SessionFactory getSessionFactory() {
        return _sessionFactory ?: (_sessionFactory = Holders.grailsApplication.mainContext.sessionFactory)
    }

    private static ProductService getProductService() {
        return _productService ?: (_productService = Holders.grailsApplication.mainContext.getBean(ProductService))
    }

    static fieldMarshaller = [
        images: { Product product ->
            List imageList = [];
            String nonRequestBaseUrl = app.siteBaseUrl()
            product.images.each {
                Map imageMap = [:]
                imageMap["id"] = it.id
                imageMap["thumbnail"] = "${nonRequestBaseUrl}resources/product/product-${product.id}/150-${it.name}";
                imageMap["url"] = "${nonRequestBaseUrl}resources/product/product-${product.id}/${it.name}";
                imageList.add(imageMap)
            }
            if(imageList.size() == 0) {
                imageList.add([
                    thumbnail: "${nonRequestBaseUrl}resources/product/default/150-default.png",
                    url: "${nonRequestBaseUrl}resources/product/default/default.png"
                ])
            }
            return imageList
        }
    ]

    static marshallerInclude = [] //Required For API

    static belongsTo = Category

    static hasMany = [
            parents                                       : Category,
            metaTags                                      : MetaTag,
            inventoryAdjustments                          : ProductInventoryAdjustment,
            images                                        : ProductImage,
            videos                                        : ProductVideo,
            availableToCustomers                          : Customer,
            availableToCustomerGroups                     : CustomerGroup,
            restrictPriceExceptCustomers                  : Customer,
            restrictPriceExceptCustomerGroups             : CustomerGroup,
            restrictPurchaseExceptCustomers               : Customer,
            restrictPurchaseExceptCustomerGroups          : CustomerGroup,
            calculatedRestrictPriceExceptCustomers        : Customer,
            calculatedRestrictPriceExceptCustomerGroups   : CustomerGroup,
            calculatedRestrictPurchaseExceptCustomers     : Customer,
            calculatedRestrictPurchaseExceptCustomerGroups: CustomerGroup,
            relatedProducts                               : Product
    ]

    static mappedBy = [parent: "none", parents: "products"]

    static clone_exclude = ["sku", "url", "availableStock", "createdBy", "created", "updated", "images", "videos", "inventoryAdjustments"]
    static copy_reference = [
            "parent", "taxProfile", "shippingProfile", "availableToCustomers", "availableToCustomerGroups",
            "restrictPriceExceptCustomers", "restrictPriceExceptCustomerGroups", "restrictPurchaseExceptCustomers", "restrictPurchaseExceptCustomerGroups",
            "calculatedRestrictPriceExceptCustomers", "calculatedRestrictPriceExceptCustomerGroups", "calculatedRestrictPurchaseExceptCustomers", "calculatedRestrictPurchaseExceptCustomerGroups",
            "relatedProducts"
    ]
    static non_owning_reference = [parents: "products"]

    static constraints = {
        name(blank: false, size: 2..100)
        sku(blank: false, maxSize: 50, unique: true)
        url(blank: false, maxSize: 100, unique: true)
        summary(nullable: true, maxSize: 500)
        description(nullable: true)
        title(nullable: true, maxSize: 200)
        heading(nullable: true, maxSize: 200)
        spec(nullable: true, maxSize: 200)
        minOrderQuantity(nullable: true)
        maxOrderQuantity(nullable: true)
        multipleOfOrderQuantity(min: 1)
        lowStockLevel(nullable: true)
        model(nullable: true)
        globalTradeItemNumber(nullable: true)
        productType(blank: false, maxSize: 50)
        productFile(nullable: true)
        availableFromDate(nullable: true);
        availableToDate(nullable: true);
        metaTags(nullable: true)
        parent(nullable: true)
        taxProfile(nullable: true)
        createdBy(nullable: true)
        shippingProfile(nullable: true)
        availableToCustomers(nullable: true)
        availableToCustomerGroups(nullable: true)
        restrictPriceExceptCustomers(nullable: true)
        restrictPriceExceptCustomerGroups(nullable: true)
        restrictPurchaseExceptCustomers(nullable: true)
        restrictPurchaseExceptCustomerGroups(nullable: true)
        calculatedRestrictPriceExceptCustomerGroups(nullable: true)
        calculatedRestrictPriceExceptCustomers(nullable: true)
        calculatedRestrictPurchaseExceptCustomerGroups(nullable: true)
        calculatedRestrictPurchaseExceptCustomers(nullable: true)
    }

    static mapping = {
        description type: "text"
        images sort: "idx", order: "asc"
        metaTags cache: true
        parents cache: true
        inventoryAdjustments cache: true
        images cache: true
        videos cache: true
        relatedProducts cache: true
    }

    Double getDisplayPrice() {
        ProductData data = productService.getProductData(this)
        if(data.isPriceRestricted(AppUtil.loggedCustomer, AppUtil.loggedCustomerGroupIds)) {
            return null
        }
        return data.priceToDisplay    }

    Double getPreviousPrice() {
        ProductData data = productService.getProductData(this)
        if(data.isPriceRestricted(AppUtil.loggedCustomer, AppUtil.loggedCustomerGroupIds)) {
            return null
        }
        return data.previousPriceToDisplay
    }

    Boolean getIsPurchaseRestricted() {
        return productService.getProductData(this).isPriceOrPurchaseRestricted(AppUtil.loggedCustomer, AppUtil.loggedCustomerGroupIds);
    }

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
        if (!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }

    @Override
    int hashCode() {
        if (id) {
            return ("Product: " + id).hashCode()
        }
        if (sku) {
            return ("Product: " + sku).hashCode()
        }
        if (name) {
            return ("Product: " + name).hashCode()
        }
        return super.hashCode();
    }

    @Override
    boolean equals(Object o) {
        if (!(o instanceof Product)) {
            return false
        }
        if (id && o.id) {
            return id == o.id
        }
        if (sku && o.sku) {
            return sku == o.sku
        }
        if (name && o.name) {
            return name == name
        }
        return super.equals(o);
    }
}