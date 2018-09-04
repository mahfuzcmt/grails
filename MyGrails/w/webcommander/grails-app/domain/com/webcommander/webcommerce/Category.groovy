package com.webcommander.webcommerce

import com.webcommander.ApplicationTagLib
import com.webcommander.admin.Customer
import com.webcommander.admin.CustomerGroup
import com.webcommander.admin.Operator
import com.webcommander.common.MetaTag
import com.webcommander.constants.DomainConstants
import com.webcommander.models.blueprints.AbstractStaticResource
import grails.util.Holders

class Category extends AbstractStaticResource {

    Long id
    String name
    String sku
    String url
    String image
    String imageBaseUrl
    String summary
    String description
    String availableFor = DomainConstants.CATEGORY_AVAILABLE_FOR.EVERYONE;

    String restrictPriceFor = DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.NONE;
    String restrictPurchaseFor = DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.NONE;
    String title
    String heading

    Integer idx = 1

    Boolean isAvailable = true
    Boolean isAvailableOnDateRange = false
    Boolean isInTrash = false
    Boolean isParentInTrash = false
    Boolean isDisposable = false
    Boolean disableGooglePageTracking = false

    Category parent
    Operator createdBy
    TaxProfile taxProfile
    ShippingProfile shippingProfile

    Date availableFromDate
    Date availableToDate
    Date created
    Date updated

    Collection<MetaTag> metaTags = []
    Collection<Product> products = []

    Collection<Customer> availableToCustomers = []
    Collection<CustomerGroup> availableToCustomerGroups = []
    Collection<Customer> restrictPriceExceptCustomers = []
    Collection<CustomerGroup> restrictPriceExceptCustomerGroups = []
    Collection<Customer> restrictPurchaseExceptCustomers = []
    Collection<CustomerGroup> restrictPurchaseExceptCustomerGroups = []

    static hasMany = [
            metaTags: MetaTag, availableToCustomers: Customer, availableToCustomerGroups: CustomerGroup, restrictPriceExceptCustomers: Customer,
            restrictPriceExceptCustomerGroups: CustomerGroup, restrictPurchaseExceptCustomers: Customer, restrictPurchaseExceptCustomerGroups: CustomerGroup, products: Product
    ]

    static mappedBy = [products: 'parents']

    static constraints = {
        name(blank: false, unique: "parent", size: 2..255)
        sku(blank: false, unique: true)
        url(blank: false, unique: true, maxSize: 100)
        title(nullable: true, maxSize: 200)
        heading(nullable: true, maxSize: 200)
        parent(nullable: true)
        summary(nullable: true, maxSize: 500)
        description(nullable: true)
        image(nullable: true)
        imageBaseUrl(nullable: true)
        createdBy(nullable: true)
        taxProfile(nullable: true)
        shippingProfile(nullable: true)
        availableFromDate(nullable: true)
        availableToDate(nullable: true)
    }

    static mapping = {
        description(type: "text")
        products(cache: true)
    }

    static transients = ['productCount', 'baseUrl', 'resourceName', 'relativeUrl']

    static ApplicationTagLib _app

    static ApplicationTagLib getApp() {
        if (!_app) {
            return _app = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        }
        return _app
    }

    @Override
    String getBaseUrl() {
        return super.getBaseUrl()
    }

    @Override
    void setBaseUrl(String baseUrl) {
        this.imageBaseUrl = baseUrl
    }

    @Override
    String getResourceName() {
        return this.image
    }

    @Override
    void setResourceName(String resourceName) {
        this.image = resourceName
    }

    @Override
    String getRelativeUrl() {
        return appResource.getCategoryRelativeUrl(id)
    }

    static fieldMarshaller = [
            image: { Category category ->
                String nonRequestBaseUrl = app.siteBaseUrl();
                Map imageMap
                if (category.image) {
                    imageMap = [
                        url      : "${nonRequestBaseUrl}resources/category/category-${category.id}/${category.image}",
                        thumb_url: "${nonRequestBaseUrl}resources/category/category-${category.id}/150-${category.image}",
                    ]
                } else {
                    imageMap = [
                        url      : "${nonRequestBaseUrl}resources/category/default/default.png",
                        thumb_url: "${nonRequestBaseUrl}resources/category/default/150-default.png",
                    ]
                }
                return imageMap
            }
    ]

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
            return ("Category: " + id).hashCode()
        }
        if (sku) {
            return ("Category: " + sku).hashCode()
        }
        if (url) {
            return ("Category: " + url).hashCode()
        }
        return super.hashCode()
    }

    @Override
    boolean equals(Object o) {
        if (!(o instanceof Category)) {
            return false
        }
        if (id && o.id) {
            return id == o.id
        }
        if (url && o.url) {
            return url == url
        }
        return super.equals(o);
    }

    Integer getProductCount() {
        if (!id) {
            return 0
        }
        return Product.createCriteria().count {
            or {
                eq("parent.id", id)
                parents {
                    eq("id", id)
                }
            }
            eq("isInTrash", false)
            eq("isParentInTrash", false)
        }
    }

}
