package com.webcommander.constants

import com.webcommander.tenant.TenantContext

class NamedConstants {

    private static Map _DYNAMIC_CONSTANT_HOLDER = [:]

    private static Map getDYNAMIC_CONSTANT() {
        Map dynamic = _DYNAMIC_CONSTANT_HOLDER[TenantContext.currentTenant]
        if (!dynamic) {
            dynamic = _DYNAMIC_CONSTANT_HOLDER[TenantContext.currentTenant] = [:].withDefault { [:] }
        }
        return dynamic
    }

    static addConstant(List list) {
        list.each { config ->
            DYNAMIC_CONSTANT[config.constant].put(config.key, config.value)
        }
    }

    static removeConstant(List list) {
        list.each { config ->
            DYNAMIC_CONSTANT[config.constant].remove(config.key)
        }
    }

    static getSERVICE_NAME() { return [:].with { getDYNAMIC_CONSTANT().TAX_DEFAULT_COUNTRY_TYPE } }

    static WC_BEHAVIOUR_TYPE = [
            E_COMMERCE: "e-commerce",
            CONTENT   : "content"
    ]

    static AUTO_GENERATED_PAGE_WIDGET = [
            PRODUCT_WIDGET: "productwidget"
    ]

    static getRESOURCE_TYPE() {
        return [
                RESOURCE: "resources",
                PUBLIC  : "pub",
                TEMPLATE: "template"
        ].with { it + getDYNAMIC_CONSTANT().RESOURCE_TYPE }
    }

    static getPAYMENT_STATUS() {
        return [
                AWAITING : "awaiting",
                REFUNDED : "refunded",
                REFIUNDED: "failed",
                SUCCESS  : "success"
        ].with { it + getDYNAMIC_CONSTANT().PAYMENT_STATUS }
    }

    static getBACKUP_STATUS() {
        return [
                "000": "internal.error",
                "041": "access.forbidden",
                "019": "surver.busy",
                "129": "parameter.missing",
                "130": "getting.backup",
                "131": "restoring.backup",
                "133": "backup.done",
                "134": "restore.done"
        ].with { it + getDYNAMIC_CONSTANT().BACKUP_STATUS }
    }

    static getSHIPPING_METHOD() {
        return [
                AUS_POST: "australianpost",
                OTHERS  : "others"
        ].with { it + getDYNAMIC_CONSTANT().SHIPPING_METHOD }
    }

    static getPAYMENT_GATEWAY_META_FIELD_TYPE() {
        return [
                TEXT           : "text",
                PASSWORD       : "password",
                SELECT         : "select",
                CHECK_BOX      : 'check_box',
                MULTI_CHECK_BOX: 'multi_check_box',
                FILE           : "file"
        ].with { it + getDYNAMIC_CONSTANT().PAYMENT_GATEWAY_META_FIELD_TYPE }
    }

    static getCUSTOMER_PROFILE_OVERVIEW_PRODUCT_TYPE() {
        return [
                TOP_SELLING: "top.selling",
                FEATURED   : "featured",
                ON_SALE    : "on.sale",
                NEW        : "new"
        ].with { it + getDYNAMIC_CONSTANT().CUSTOMER_PROFILE_OVERVIEW_PRODUCT_TYPE }
    }

    static getDISCOUNT_TYPES_MESSAGE_KEYS() {
        return [
                (DomainConstants.DISCOUNT_TYPES.DISCUONT_BY_PRODUCT_PRICE) : "product.price",
                (DomainConstants.DISCOUNT_TYPES.DISCOUNT_BY_SALES_QUANTITY): "sales.quantity",
                (DomainConstants.DISCOUNT_TYPES.DISCOUNT_BY_PROFIT_MARGIN) : "profit.margin",
                (DomainConstants.DISCOUNT_TYPES.FLAT_DISCOUNT)             : "flat.discount"
        ].with { it + getDYNAMIC_CONSTANT().DISCOUNT_TYPES_MESSAGE_KEYS }
    }

    static getDISCOUNT_PROFILE_RULE_PRECEDENCE_MESSAGE_KEYS() {
        return [
                (DomainConstants.DISCOUNT_PROFILE_RULE_PRECEDENCE.RULES_WITH_HIGHER_PRIORITY): "rules.higher.priority",
                (DomainConstants.DISCOUNT_PROFILE_RULE_PRECEDENCE.RULES_WITH_LOWEST_DISCOUNT): "rules.lowest.discount",
                (DomainConstants.DISCOUNT_PROFILE_RULE_PRECEDENCE.RULES_WITH_HIGHEST_DISOUNT): "rules.highest.discount"
        ].with { it + getDYNAMIC_CONSTANT().DISCOUNT_PROFILE_RULE_PRECEDENCE_MESSAGE_KEYS }
    }

    static getMASSTYPE() {
        return [
                POUNDS   : "pounds",
                OUNCES   : "ounces",
                GRAMS    : "grams",
                TONNES   : "tonnes",
                KILOGRAMS: "kilograms"
        ].with { it + getDYNAMIC_CONSTANT().MASSTYPE }
    }

    static getLENGTHTYPE() {
        return [
                INCHES     : "inches",
                CENTIMETERS: "centimeters",
                KILOMETERS : "kilometers",
                MILES      : "miles",
                MILLIMETERS: "millimeters",
                FOOTS      : "foots",
                METERS     : "meters"
        ].with { it + getDYNAMIC_CONSTANT().LENGTHTYPE }
    }

    static getSHIPPING_POLICY_TYPE_MESSAGE_KEYS() {
        return [
                (DomainConstants.SHIPPING_POLICY_TYPE.FREE_SHIPPING)   : "free.shipping",
                (DomainConstants.SHIPPING_POLICY_TYPE.FLAT_RATE)       : "flat.rate",
                (DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_PRICE)   : "ship.by.price",
                (DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_QUANTITY): "ship.by.quantity",
                (DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_WEIGHT)  : "ship.by.weight",
                (DomainConstants.SHIPPING_POLICY_TYPE.API)             : "api"
        ].with { it + getDYNAMIC_CONSTANT().SHIPPING_POLICY_TYPE_MESSAGE_KEYS }
    }

    static getHANDLING_COST_FILTER_KEYS() {
        return [
                "WITH_HANDLING"   : "with.handling.cost",
                "WITHOUT_HANDLING": "without.handling.cost"
        ].with { it + getDYNAMIC_CONSTANT().SHIPPING_POLICY_TYPE_MESSAGE_KEYS }
    }

    static getSHIPPING_API() {
        return [
                (DomainConstants.SHIPPING_API.AUSPOST): 'auspost'
        ].with { it + getDYNAMIC_CONSTANT().SHIPPING_API }
    }

    static getPACKING_ALGORITHM() {
        return [
                (DomainConstants.PACKING_ALGORITHM.INDIVIDUAL): "individual",
//        (DomainConstants.PACKING_ALGORITHM.COMBINED): "combined"
        ].with { it + getDYNAMIC_CONSTANT().PACKING_ALGORITHM }
    }

    static getSHIPPING_API_SERVICE_TYPE() {
        return [
                (DomainConstants.SHIPPING_API_SERVICE_TYPE.REGULAR_PARCEL)                                                           : "regular.parcel",
                (DomainConstants.SHIPPING_API_SERVICE_TYPE.REGULAR_PARCEL_STANDARD)                                                  : "regular.parcel.standard",
                (DomainConstants.SHIPPING_API_SERVICE_TYPE.REGULAR_PARCEL_REGISTER_POST)                                             : "regular.parcel.register.post",
                (DomainConstants.SHIPPING_API_SERVICE_TYPE.REGULAR_PARCEL_REGISTER_POST_WITH_DELIVERY_CONFAFIRMATION)                : "regular.parcel.register.post.with" +
                        ".delivery.confirmation",
                (DomainConstants.SHIPPING_API_SERVICE_TYPE.REGULAR_PARCEL_REGISTER_POST_WITH_EXTRA_COVER)                            : "regular.parcel.register.post.with.extra.cover",
                (DomainConstants.SHIPPING_API_SERVICE_TYPE.REGULAR_PARCEL_REGISTER_POST_WITH_DELIVERY_CONFAFIRMATION_AND_EXTRA_COVER): "regular.parcel" +
                        ".register.post.with.delivery.confirmation.and.extra.cover",
                (DomainConstants.SHIPPING_API_SERVICE_TYPE.EXPRESS_PARCEL_SERVICE)                                                   : "express.parcel.service",
                (DomainConstants.SHIPPING_API_SERVICE_TYPE.PLATINUM_PARCEL_SERVICE)                                                  : "platinum.parcel.service",
                (DomainConstants.SHIPPING_API_SERVICE_TYPE.PLATINUM_PARCEL_SERVICE_WITH_EXTRA_COVER)                                 : "platinum.parcel.service.with.extra.cover",
        ].with { it + getDYNAMIC_CONSTANT().SHIPPING_API_SERVICE_TYPE }
    }

    static getSHIPPING_PROFILE_RULE_PRECEDENCE_MESSAGE_KEYS() {
        return [
                (DomainConstants.SHIPPING_PROFILE_RULE_PRECEDENCE.RULES_WITH_HIGHER_PRIORITY)      : "rules.higher.priority",
                (DomainConstants.SHIPPING_PROFILE_RULE_PRECEDENCE.RULES_WITH_LOWEST_SHIPPING_COST) : "rules.lowest.shipping.cost",
                (DomainConstants.SHIPPING_PROFILE_RULE_PRECEDENCE.RULES_WITH_HIGHEST_SHIPPING_COST): "rules.highest.shipping.cost",
        ].with { it + getDYNAMIC_CONSTANT().SHIPPING_PROFILE_RULE_PRECEDENCE_MESSAGE_KEYS }
    }

    static getCOPY_SHIPPING_PROFILE_KEYS() {
        return [
                (DomainConstants.COPY_SHIPPING_PROFILE_OPTIONS.USE_RULE_AND_CONFIG) : "use.rule.and.config",
                (DomainConstants.COPY_SHIPPING_PROFILE_OPTIONS.COPY_RULE_AND_CONFIG): "copy.rule.and.config"
        ].with { it + getDYNAMIC_CONSTANT().COPY_SHIPPING_PROFILE_KEYS }
    }

    static getPRODUCT_IMAGE_SETTINGS() {
        return [
                ADMIN    : "admin",
                GRIDVIEW : "gridview",
                DETAILS  : "details",
                LISTVIEW : "listview",
                POPUP    : "popup",
                THUMBNAIL: "thumbnail",
        ].with { it + getDYNAMIC_CONSTANT().PRODUCT_IMAGE_SETTINGS }
    }

    static getCATEGORY_IMAGE_SETTINGS() {
        return [
                ADMIN    : "admin",
                GRIDVIEW : "gridview",
                DETAILS  : "details",
                LISTVIEW : "listview",
                POPUP    : "popup",
                THUMBNAIL: "thumbnail"
        ].with { it + getDYNAMIC_CONSTANT().CATEGORY_IMAGE_SETTINGS }
    }

    static getCATEGORY_IMAGE_SIZES() {
        return [
                ADMIN_WIDTH     : "admin_width",
                ADMIN_HEIGHT    : "admin_height",
                GRIDVIEW_WIDTH  : "gridview_width",
                GRIDVIEW_HEIGHT : "gridview_height",
                LISTVIEW_WIDTH  : "listview_width",
                LISTVIEW_HEIGHT : "listview_height",
                DETAILS_WIDTH   : "details_width",
                DETAILS_HEIGHT  : "details_height",
                POPUP_WIDTHT    : "popup_width",
                POPUP_HEIGHT    : "popup_height",
                THUMBNAIL_WIDTH : "thumbnail_width",
                THUMBNAIL_HEIGHT: "thumbnail_height"
        ].with { it + getDYNAMIC_CONSTANT().CATEGORY_IMAGE_SIZES }
    }

    static getPRODUCT_IMAGE_SIZES() {
        return [
                ADMIN_WIDTH     : "admin_width",
                ADMIN_HEIGHT    : "admin_height",
                GRIDVIEW_WIDTH  : "gridview_width",
                GRIDVIEW_HEIGHT : "gridview_height",
                LISTVIEW_WIDTH  : "listview_width",
                LISTVIEW_HEIGHT : "listview_height",
                DETAILS_WIDTH   : "details_width",
                DETAILS_HEIGHT  : "details_height",
                POPUP_WIDTHT    : "popup_width",
                POPUP_HEIGHT    : "popup_height",
                THUMBNAIL_WIDTH : "thumbnail_width",
                THUMBNAIL_HEIGHT: "thumbnail_height"
        ].with { it + getDYNAMIC_CONSTANT().PRODUCT_IMAGE_SIZES }
    }

    static getSHOPPING_CART_TOTAL_PRICE_MESSAGE_KEYS() {
        return [
                (DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITHOUT_TAX_WITHOUT_DISCOUNT): "without.tax.without.discount",
                (DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITHOUT_TAX_WITH_DISCOUNT)   : "without.tax.with.discount",
                (DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITH_TAX_WITHOUT_DISCOUNT)   : "with.tax.without.discount",
                (DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITH_TAX_WITH_DISCOUNT)      : "with.tax.with.discount"
        ].with { it + getDYNAMIC_CONSTANT().SHOPPING_CART_TOTAL_PRICE_MESSAGE_KEYS }
    }

    static getPAGINATION_TYPE() {
        return [
                NONE          : "none",
                TOP           : "top",
                BOTTOM        : "bottom",
                TOP_AND_BOTTOM: "top_and_bottom"
        ].with { it + getDYNAMIC_CONSTANT().PAGINATION_TYPE }
    }

    static getTAX_DEFAULT_COUNTRY() {
        return [
                (DomainConstants.TAX_DEFAULT_COUNTRY_TYPE.AUSTRALIA): "australia",
                (DomainConstants.TAX_DEFAULT_COUNTRY_TYPE.CANADA)   : "canada",
                (DomainConstants.TAX_DEFAULT_COUNTRY_TYPE.EU)       : "eu",
                (DomainConstants.TAX_DEFAULT_COUNTRY_TYPE.NEWZELAND): "newzeland",
                (DomainConstants.TAX_DEFAULT_COUNTRY_TYPE.UK)       : "uk",
                (DomainConstants.TAX_DEFAULT_COUNTRY_TYPE.USA)      : "usa",
                (DomainConstants.TAX_DEFAULT_COUNTRY_TYPE.REST)     : "rest.of.world"
        ].with { it + getDYNAMIC_CONSTANT().TAX_DEFAULT_COUNTRY }
    }

    static getTAX_DEFAULT_COUNTRY_CUSTOM() {
        return [
                (DomainConstants.TAX_DEFAULT_COUNTRY_TYPE.AUSTRALIA): "australia",
                (DomainConstants.TAX_DEFAULT_COUNTRY_TYPE.CANADA)   : "canada",
                //(DomainConstants.TAX_DEFAULT_COUNTRY_TYPE.EU)       : "eu",
                (DomainConstants.TAX_DEFAULT_COUNTRY_TYPE.NEWZELAND): "newzeland",
                (DomainConstants.TAX_DEFAULT_COUNTRY_TYPE.UK)       : "uk",
                (DomainConstants.TAX_DEFAULT_COUNTRY_TYPE.USA)      : "usa"
        ].with { it + getDYNAMIC_CONSTANT().TAX_DEFAULT_COUNTRY_CUSTOM }
    }

    static getTAX_CONFIGURATION() {
        return [
                (DomainConstants.TAX_CONFIGURATION_TYPE.DEFAULT): "default",
                (DomainConstants.TAX_CONFIGURATION_TYPE.MANUAL) : "manual"
        ].with { it + getDYNAMIC_CONSTANT().TAX_CONFIGURATION }
    }

    static getPRODUCT_CONDITION() {
        return [
                (DomainConstants.PRODUCT_CONDITION.NEW)        : "new",
                (DomainConstants.PRODUCT_CONDITION.USED)       : "used",
                (DomainConstants.PRODUCT_CONDITION.REFURBISHED): "refurbished"
        ].with { it + getDYNAMIC_CONSTANT().PRODUCT_CONDITION }
    }

    static getPRODUCT_TYPE() {
        return [
                (DomainConstants.PRODUCT_TYPE.PHYSICAL)    : "physical",
                (DomainConstants.PRODUCT_TYPE.DOWNLOADABLE): "downloadable"
        ].with { it + getDYNAMIC_CONSTANT().PRODUCT_TYPE }
    }

    static getPAGINATION_MESSAGE() {
        return [
                (PAGINATION_TYPE.NONE)          : "none",
                (PAGINATION_TYPE.TOP)           : "top",
                (PAGINATION_TYPE.BOTTOM)        : "bottom",
                (PAGINATION_TYPE.TOP_AND_BOTTOM): "top.and.bottom"
        ].with { it + getDYNAMIC_CONSTANT().PAGINATION_MESSAGE }
    }

    static getPRODUCT_WIDGET_VIEW() {
        return [
                IMAGE     : "image",
                SCROLLABLE: 'scrollable',
                LIST      : 'list'
        ].with { it + getDYNAMIC_CONSTANT().PRODUCT_WIDGET_VIEW }
    }

    static getPRODUCT_WIDGET_VIEW_MESSAGE() {
        return [
                (PRODUCT_WIDGET_VIEW.IMAGE)     : "image",
                (PRODUCT_WIDGET_VIEW.SCROLLABLE): "scrollable",
                (PRODUCT_WIDGET_VIEW.LIST)      : "list"
        ].with { it + getDYNAMIC_CONSTANT().PRODUCT_WIDGET_VIEW_MESSAGE }
    }

    static getWIDGET_MESSAGE_KEYS() {
        return [
                (DomainConstants.WIDGET_TYPE.ARTICLE + ".title")           : "article.widget",
                (DomainConstants.WIDGET_TYPE.ARTICLE + ".label")           : "article",
                (DomainConstants.WIDGET_TYPE.BREADCRUMB + ".title")        : "breadcrumb.widget",
                (DomainConstants.WIDGET_TYPE.BREADCRUMB + ".label")        : "breadcrumb",
                (DomainConstants.WIDGET_TYPE.CART + ".label")              : "cart",
                (DomainConstants.WIDGET_TYPE.CART + ".title")              : "cart.widget",
                (DomainConstants.WIDGET_TYPE.HTML + ".title")              : "html.widget",
                (DomainConstants.WIDGET_TYPE.HTML + ".label")              : "text.html",
                (DomainConstants.WIDGET_TYPE.SPACER + ".title")            : "spacer.widget",
                (DomainConstants.WIDGET_TYPE.SPACER + ".label")            : "spacer",
                (DomainConstants.WIDGET_TYPE.LOGIN + ".title")             : "login.widget",
                (DomainConstants.WIDGET_TYPE.LOGIN + ".label")             : "login",
                (DomainConstants.WIDGET_TYPE.NAVIGATION + ".title")        : "navigation.widget",
                (DomainConstants.WIDGET_TYPE.NAVIGATION + ".label")        : "navigation",
                (DomainConstants.WIDGET_TYPE.PRODUCT + ".title")           : "product.widget",
                (DomainConstants.WIDGET_TYPE.PRODUCT + ".label")           : "product",
                (DomainConstants.WIDGET_TYPE.CATEGORY + ".title")          : "category.widget",
                (DomainConstants.WIDGET_TYPE.CATEGORY + ".label")          : "category",
                (DomainConstants.WIDGET_TYPE.SOCIAL_MEDIA_LINK + ".title") : "social.media.link.widget",
                (DomainConstants.WIDGET_TYPE.SOCIAL_MEDIA_LINK + ".label") : "social.media.link",
                (DomainConstants.WIDGET_TYPE.SOCIAL_MEDIA_SHARE + ".title"): "social.media.share.widget",
                (DomainConstants.WIDGET_TYPE.SOCIAL_MEDIA_SHARE + ".label"): "social.media.share",
                (DomainConstants.WIDGET_TYPE.SOCIAL_MEDIA_LIKE + ".title") : "social.media.like.widget",
                (DomainConstants.WIDGET_TYPE.SOCIAL_MEDIA_LIKE + ".label") : "social.media.like",
                (DomainConstants.WIDGET_TYPE.SEARCH + ".title")            : "search.widget",
                (DomainConstants.WIDGET_TYPE.SEARCH + ".label")            : "search",
                (DomainConstants.WIDGET_TYPE.IMAGE + ".title")             : "image.widget",
                (DomainConstants.WIDGET_TYPE.IMAGE + ".label")             : "image",
                (DomainConstants.WIDGET_TYPE.STORE_LOGO + ".title")        : "logo.widget",
                (DomainConstants.WIDGET_TYPE.STORE_LOGO + ".label")        : "logo",
                (DomainConstants.WIDGET_TYPE.NEWSLETTER + ".title")        : "newsletter.widget",
                (DomainConstants.WIDGET_TYPE.NEWSLETTER + ".label")        : "newsletter",
                (DomainConstants.WIDGET_TYPE.GALLERY + ".title")           : "gallery.widget",
                (DomainConstants.WIDGET_TYPE.GALLERY + ".label")           : "gallery",
                (DomainConstants.WIDGET_TYPE.CURRENCY + ".title")          : "currency.widget",
                (DomainConstants.WIDGET_TYPE.CURRENCY + ".label")          : "currency"
        ].with { it + getDYNAMIC_CONSTANT().WIDGET_MESSAGE_KEYS }
    }

    static getWIDGET_LICENSE() { return [:].with { it + getDYNAMIC_CONSTANT().WIDGET_LICENSE } }

    static getPRODUCT_WIDGET_MESSAGE_KEYS() {
        return [
                (DomainConstants.PRODUCT_WIDGET_TYPE.PRODUCT_NAME + ".title")             : "product.name.widget",
                (DomainConstants.PRODUCT_WIDGET_TYPE.PRODUCT_NAME + ".label")             : "product.name",
                (DomainConstants.PRODUCT_WIDGET_TYPE.PRODUCT_SUMMARY + ".title")          : "product.summary.widget",
                (DomainConstants.PRODUCT_WIDGET_TYPE.PRODUCT_SUMMARY + ".label")          : "product.summary",
                (DomainConstants.PRODUCT_WIDGET_TYPE.PRODUCT_SKU + ".title")              : "product.sku.widget",
                (DomainConstants.PRODUCT_WIDGET_TYPE.PRODUCT_SKU + ".label")              : "product.sku",
                (DomainConstants.PRODUCT_WIDGET_TYPE.PRODUCT_CATEGORY + ".title")         : "product.category.widget",
                (DomainConstants.PRODUCT_WIDGET_TYPE.PRODUCT_CATEGORY + ".label")         : "product.category",
                (DomainConstants.PRODUCT_WIDGET_TYPE.PRODUCT_DOWNLOADABLE_SPEC + ".title"): "product.downloadable.spec.widget",
                (DomainConstants.PRODUCT_WIDGET_TYPE.PRODUCT_DOWNLOADABLE_SPEC + ".label"): "product.downloadable.spec",
                (DomainConstants.PRODUCT_WIDGET_TYPE.PRODUCT_MODEL + ".title")            : "product.model.widget",
                (DomainConstants.PRODUCT_WIDGET_TYPE.PRODUCT_MODEL + ".label")            : "product.model",
                (DomainConstants.PRODUCT_WIDGET_TYPE.PRICE + ".title")                    : "price.widget",
                (DomainConstants.PRODUCT_WIDGET_TYPE.PRICE + ".label")                    : "price",
                (DomainConstants.PRODUCT_WIDGET_TYPE.PRODUCT_IMAGE + ".title")            : "product.image",
                (DomainConstants.PRODUCT_WIDGET_TYPE.PRODUCT_IMAGE + ".label")            : "product.image",
                (DomainConstants.PRODUCT_WIDGET_TYPE.STOCK_MARK + ".title")               : "stock.mark.widget",
                (DomainConstants.PRODUCT_WIDGET_TYPE.STOCK_MARK + ".label")               : "stock.mark",
                (DomainConstants.PRODUCT_WIDGET_TYPE.COMBINED_PRODUCT + ".title")         : "combined.product.widget",
                (DomainConstants.PRODUCT_WIDGET_TYPE.COMBINED_PRODUCT + ".label")         : "combined.product",
                (DomainConstants.PRODUCT_WIDGET_TYPE.ADD_CART + ".title")                 : "add.cart.widget",
                (DomainConstants.PRODUCT_WIDGET_TYPE.ADD_CART + ".label")                 : "add.to.cart",
                (DomainConstants.PRODUCT_WIDGET_TYPE.LIKE_US + ".title")                  : "like.us.widget",
                (DomainConstants.PRODUCT_WIDGET_TYPE.LIKE_US + ".label")                  : "like.us",
                (DomainConstants.PRODUCT_WIDGET_TYPE.SOCIAL_MEDIA_SHARE + ".title")       : "social.media.share.widget",
                (DomainConstants.PRODUCT_WIDGET_TYPE.SOCIAL_MEDIA_SHARE + ".label")       : "social.media.share",
                (DomainConstants.PRODUCT_WIDGET_TYPE.INFOMATION + ".title")               : "information.widget",
                (DomainConstants.PRODUCT_WIDGET_TYPE.INFOMATION + ".label")               : "information",
                (DomainConstants.PRODUCT_WIDGET_TYPE.RELATED + ".title")                  : "related.widget",
                (DomainConstants.PRODUCT_WIDGET_TYPE.RELATED + ".label")                  : "related.products",
                (DomainConstants.PRODUCT_WIDGET_TYPE.CONDITION + ".title")                : "product.condition.widget",
                (DomainConstants.PRODUCT_WIDGET_TYPE.CONDITION + ".label")                : "product.condition",
                (DomainConstants.PRODUCT_WIDGET_TYPE.PROPERTIES + ".title")               : "product.properties",
                (DomainConstants.PRODUCT_WIDGET_TYPE.PROPERTIES + ".label")               : "product.properties"
        ].with { it + getDYNAMIC_CONSTANT().PRODUCT_WIDGET_MESSAGE_KEYS }
    };

    static getNAVIGATION_ITEM_MESSAGE_KEYS() {
        return [
                (DomainConstants.NAVIGATION_ITEM_TYPE.NONE)               : "none",
                (DomainConstants.NAVIGATION_ITEM_TYPE.PAGE)               : "page",
                (DomainConstants.NAVIGATION_ITEM_TYPE.PRODUCT)            : "product",
                (DomainConstants.NAVIGATION_ITEM_TYPE.CATEGORY)           : "category",
                (DomainConstants.NAVIGATION_ITEM_TYPE.URL)                : "url",
                (DomainConstants.NAVIGATION_ITEM_TYPE.EMAIL)              : "email",
                (DomainConstants.NAVIGATION_ITEM_TYPE.AUTO_GENERATED_PAGE): "auto.generated.page"
        ].with { it + getDYNAMIC_CONSTANT().NAVIGATION_ITEM_MESSAGE_KEYS }
    }

    static getEMAIL_CONTENT_TYPE() {
        return [
                (DomainConstants.EMAIL_CONTENT_TYPE.TEXT)     : "text",
                (DomainConstants.EMAIL_CONTENT_TYPE.HTML)     : "html",
                (DomainConstants.EMAIL_CONTENT_TYPE.TEXT_HTML): "text.html"
        ].with { it + getDYNAMIC_CONSTANT().EMAIL_CONTENT_TYPE }
    }

    static getEMAIL_SETTING_MESSAGE_KEYS() {
        return [
                (DomainConstants.EMAIL_TYPE.CUSTOMER): "customer.emails",
                (DomainConstants.EMAIL_TYPE.ADMIN)   : "admin.emails",
                (DomainConstants.EMAIL_TYPE.ORDER)   : "order.emails",
                (DomainConstants.EMAIL_TYPE.PAYMENT) : "payment.emails",
                (DomainConstants.EMAIL_TYPE.LICENSE) : "license"
        ].with { it + getDYNAMIC_CONSTANT().EMAIL_SETTING_MESSAGE_KEYS }
    }

    static getCUSTOMER_CHECKOUT_TYPE() {
        return [
                REGISTRATION: "registration",
                GUEST       : "guest"
        ].with { it + getDYNAMIC_CONSTANT().CUSTOMER_CHECKOUT_TYPE }
    }

    static getTASK_STATUS() {
        return [
                COMPLETE: "complete",
                RUNNING : "running",
                STOPPED : "stopped",
                ABORTED : "aborted"
        ].with { it + getDYNAMIC_CONSTANT().TASK_STATUS }
    }

    static getTASK_LOGGER_STATUS() {
        return [
                SUCCESS: "success",
                WARNING: "warning",
                ERROR  : "error"
        ].with { it + getDYNAMIC_CONSTANT().TASK_LOGGER_STATUS }
    }

    static getORDER_DELIVERY_TYPE() {
        return [
                (DomainConstants.ORDER_DELIVERY_TYPE.SHIPPING)       : "shipping",
                (DomainConstants.ORDER_DELIVERY_TYPE.STORE_PICKUP)   : "store.pickup",
                (DomainConstants.ORDER_DELIVERY_TYPE.OTHERS_SHIPPING): "others"
        ].with { it + getDYNAMIC_CONSTANT().ORDER_DELIVERY_TYPE }
    }

    static getORDER_STATUS() {
        return [
                (DomainConstants.ORDER_STATUS.PENDING)  : "pending",
                (DomainConstants.ORDER_STATUS.CANCELLED): "cancelled",
                (DomainConstants.ORDER_STATUS.COMPLETED): "completed"
        ].with { it + getDYNAMIC_CONSTANT().ORDER_STATUS }
    }

    static getORDER_TOTAL() {
        return [
                (DomainConstants.ORDER_TOTAL.GREATER): "greater",
                (DomainConstants.ORDER_TOTAL.LESS)   : "less"
        ].with { it + getDYNAMIC_CONSTANT().ORDER_TOTAL }
    }

    static getSHIPPING_STATUS() {
        return [
                (DomainConstants.SHIPPING_STATUS.AWAITING) : "awaiting",
                (DomainConstants.SHIPPING_STATUS.PARTIAL)  : "partial",
                (DomainConstants.SHIPPING_STATUS.COMPLETED): "completed"
        ].with { it + getDYNAMIC_CONSTANT().SHIPPING_STATUS }
    }

    static getORDER_PAYMENT_STATUS() {
        return [
                (DomainConstants.ORDER_PAYMENT_STATUS.UNPAID)        : 'unpaid',
                (DomainConstants.ORDER_PAYMENT_STATUS.PAID)          : 'paid',
                (DomainConstants.ORDER_PAYMENT_STATUS.PARTIALLY_PAID): 'partially.paid'
        ].with { it + getDYNAMIC_CONSTANT().ORDER_PAYMENT_STATUS }
    }

    static getSURCHARGE_TYPE() {
        return [
                (DomainConstants.SURCHARGE_TYPE.NO_SURCHARGE)             : 'no.surcharge',
                (DomainConstants.SURCHARGE_TYPE.FLAT_SURCHARGE)           : 'flat.surcharge',
                (DomainConstants.SURCHARGE_TYPE.SURCHARGE_ON_AMOUNT_RANGE): 'surcharge.on.amount.range'
        ].with { it + getDYNAMIC_CONSTANT().SURCHARGE_TYPE }
    }

    static getCUSTOMER_REG_TYPE() {
        return [
                (DomainConstants.CUSTOMER_REG_TYPE.OPEN)             : "open",
                (DomainConstants.CUSTOMER_REG_TYPE.AWAITING_APPROVAL): "awaiting_approval",
                (DomainConstants.CUSTOMER_REG_TYPE.CLOSED)           : "closed"
        ].with { it + getDYNAMIC_CONSTANT().CUSTOMER_REG_TYPE }
    }

    static getCACHE() {
        return [
                SCOPE_APP     : "app",
                SCOPE_SESSION : "session",
                ACCESS_CONTROL: "access-control",
                LOCAL_STATIC  : "local-static",
                TENANT_STATIC : "tenant-static"
        ].with { it + getDYNAMIC_CONSTANT().CACHE }
    }

    static getIMAGE_RESIZE_TYPE() {
        return [
                STORE_LOGO          : "store-logo",
                ALBUM_IMAGE         : "album-image",
                PRODUCT_IMAGE       : "product-image",
                CATEGORY_IMAGE      : "category-image",
                FAVICON_IMAGE       : "favicon-image",
                NAVIGATION_ITEM     : "navigation-item",
                IMAGE_WIDGET        : "image-widget",
                PAYMENT_GATEWAY_LOGO: "paymentGateway-logo",
                BLOG_CATEGORY_IMAGE : "blog-category-image",
                BLOG_POST_IMAGE     : "blog-post-image",
                VARIATION_IMAGE     : "variation-image"
        ].with { it + getDYNAMIC_CONSTANT().IMAGE_RESIZE_TYPE }
    }

    static getCART_OBJECT_TYPES() {
        return [
                PRODUCT: "product"
        ].with { it + getDYNAMIC_CONSTANT().CART_OBJECT_TYPES }
    }

    static getGALLERY_TYPES() {
        return [
                NIVO_SLIDER: "nivoSlider"
        ].with { it + getDYNAMIC_CONSTANT().GALLERY_TYPES }
    }

    static getGALLERY_NAMES() {
        return [
                nivoSlider: "Nivo Slider"
        ].with { it + getDYNAMIC_CONSTANT().GALLERY_NAMES }
    }

    static getGALLERY_LOCATION() {
        return [
                nivoSlider: "core"
        ].with { it + getDYNAMIC_CONSTANT().GALLERY_LOCATION }
    }

    static getNIVO_SLIDER_EFFECTS() {
        return [
                random            : "random",
                boxRain           : "box.rain",
                boxRainGrow       : "box.rain.grow",
                boxRainGrowReverse: "box.rain.grow.reverse",
                boxRainReverse    : "box.rain.reverse",
                boxRandom         : "box.random",
                fade              : "fade",
                fold              : "fold",
                sliceDown         : "slice.down",
                sliceDownLeft     : "slice.down.left",
                sliceUp           : "slice.up",
                sliceUpDown       : "slice.up.down",
                sliceUpDownLeft   : "slice.up.down.left",
                sliceUpLeft       : "slice.up.left",
                slideInLeft       : "slide.in.left",
                slideInRight      : "slide.in.right"
        ].with { it + getDYNAMIC_CONSTANT().NIVO_SLIDER_EFFECTS }
    }

    static getCHECKOUT_PAGE_STEP() {
        return [
                LOGIN_REGISTRATION: "login",
                BILLING_ADDRESS   : "billing",
                SHIPPING_ADDRESS  : "shipping_address",
                SHIPPING_METHOD   : "shipping_method",
                PAYMENT_METHOD    : "payment",
                ORDER_COMMENT     : "order_comment",
                CONFIRM_STEP      : "confirm"
        ].with { it + getDYNAMIC_CONSTANT().CHECKOUT_PAGE_STEP }
    }

    static getTERMS_AND_CONDITION_TYPE() {
        return [
                (DomainConstants.TERMS_AND_CONDITION_TYPE.PAGE)         : "page",
                (DomainConstants.TERMS_AND_CONDITION_TYPE.EXTERNAL_LINK): "external.link",
                (DomainConstants.TERMS_AND_CONDITION_TYPE.SPECIFIC_TEXT): "specific.text"
        ].with { it + getDYNAMIC_CONSTANT().TERMS_AND_CONDITION_TYPE }
    }

    static getUPDATE_STOCK() {
        return [
                (DomainConstants.UPDATE_STOCK.AFTER_ORDER)   : 'after.order',
                (DomainConstants.UPDATE_STOCK.AFTER_PAYMENT) : 'after.payment',
                (DomainConstants.UPDATE_STOCK.AFTER_SHIPMENT): 'after.shipment'
        ].with { it + getDYNAMIC_CONSTANT().UPDATE_STOCK }
    }

    static getZOOM_TYPE() {
        return [
                standard        : 'standard',
                tints           : 'tints',
                inner           : 'inner.zoom',
                lens            : 'lens.zoom',
                fade_in_out     : 'fade.in.out',
                easing          : 'easing',
                mousewheel      : 'mousewheel',
                mousewheel_inner: 'mousewheel.inner.zoom',
                mousewheel_lens : 'mousewheel.lens.zoom',

        ].with { it + getDYNAMIC_CONSTANT().ZOOM_TYPE }
    }

    public static getCATEGORY_IMPORT_FIELDS() {
        return [
                name           : "name",
                sku            : "sku",
                parent         : "parent.category",
                summary        : "summary",
                description    : "description",
                image          : "image",
                idx            : "display.order",
                shippingProfile: "shipping.profile",
                taxProfile     : "tax.profile",
                metaTags       : "meta.tags"
        ].with { it + getDYNAMIC_CONSTANT().CATEGORY_IMPORT_FIELDS }
    }

    public static getPRODUCT_IMPORT_FIELDS() {
        return [
                name                 : "name",
                sku                  : "sku",
                productType          : "product.type",
                parent               : "category",
                isAvailable          : "availability",
                summary              : "summary",
                description          : "description",
                image                : "image",
                basePrice            : "base.price",
                costPrice            : "cost.price",
                idx                  : "display.order",
                salePrice            : "sale.price",
                isCallForPriceEnabled: "call.for.price",
                isInventoryEnabled   : "track.inventory",
                availableStock       : "available.stock",
                lowStockLevel        : "low.stock.level",
                minOrderQuantity     : "minimum.order.quantity",
                maxOrderQuantity     : "maximum.order.quantity",
                model                : "model",
                height               : "height",
                width                : "width",
                weight               : "weight",
                length               : "length",
                taxProfile           : "tax.profile",
                shippingProfile      : "shipping.profile",
                metaTags             : "meta.tags",
                videos               : "video"
        ].with { it + getDYNAMIC_CONSTANT().PRODUCT_IMPORT_FIELDS }
    }

    public static getRULE_IMPORT_FIELDS() {
        return [
                name          : "rule.name",
                description   : "rule.description",
                shippingPolicy: "rate",
                zoneList      : "zone",
                shippingClass : "shipping.class"
        ].with { it + getDYNAMIC_CONSTANT().RULE_IMPORT_FIELDS }
    }

    public static getRATE_IMPORT_FIELDS() {
        return [
                name            : "rate.name",
                method          : "method",
                shippingCost    : "shipping.cost",
                handlingCost    : "handling.cost",
                packetWeight    : "packet.weight",
                from            : "min",
                to              : "max",
                isAdditional    : "enable.additional.rule",
                isCumulative    : "cumulative",
                additionalAmount: "for.each.additional",
                additionalCost  : "additional.cost"
        ].with { it + getDYNAMIC_CONSTANT().RATE_IMPORT_FIELDS }
    }

    public static getZONE_IMPORT_FIELDS() {
        return [
                name     : "zone.name",
                countries: "country",
                states   : "state",
                postCodes: "post.code"
        ].with { it + getDYNAMIC_CONSTANT().ZONE_IMPORT_FIELDS }
    }

    public static getPRODUCT_IMPORT_EXTRA_FIELDS() {
        return [:].with { it + getDYNAMIC_CONSTANT().PRODUCT_IMPORT_EXTRA_FIELDS }
    } // for plugins

    public static getLOCALES() {
        return [
                'all'                              : "Default Locale",
                'sq'                               : 'Albanian',
                'sq-AL'                            : 'Albanian (Albania)',
                'ar'                               : 'Arabic',
                'ar-DZ'                            : 'Arabic (Algeria)',
                'ar-BH'                            : 'Arabic (Bahrain)',
                'ar-EG'                            : 'Arabic (Egypt)',
                'ar-IQ'                            : 'Arabic (Iraq)',
                'ar-JO'                            : 'Arabic (Jordan)',
                'ar-KW'                            : 'Arabic (Kuwait)',
                'ar-LB'                            : 'Arabic (Lebanon)',
                'ar-LY'                            : 'Arabic (Libya)',
                'ar-MA'                            : 'Arabic (Morocco)',
                'ar-OM'                            : 'Arabic (Oman)',
                'ar-QA'                            : 'Arabic (Qatar)',
                'ar-SA'                            : 'Arabic (Saudi Arabia)',
                'ar-SD'                            : 'Arabic (Sudan)',
                'ar-SY'                            : 'Arabic (Syria)',
                'ar-TN'                            : 'Arabic (Tunisia)',
                'ar-AE'                            : 'Arabic (United Arab Emirates)',
                'ar-YE'                            : 'Arabic (Yemen)',
                'be'                               : 'Belarusian',
                'be-BY'                            : 'Belarusian (Belarus)',
                'bg'                               : 'Bulgarian',
                'bg-BG'                            : 'Bulgarian (Bulgaria)',
                'ca'                               : 'Catalan',
                'ca-ES'                            : 'Catalan (Spain)',
                'zh'                               : 'Chinese',
                'zh-CN'                            : 'Chinese (China)',
                'zh-HK'                            : 'Chinese (Hong Kong)',
                'zh-SG'                            : 'Chinese (Singapore)',
                'zh-TW'                            : 'Chinese (Taiwan)',
                'hr'                               : 'Croatian',
                'hr-HR'                            : 'Croatian (Croatia)',
                'cs'                               : 'Czech',
                'cs-CZ'                            : 'Czech (Czech Republic)',
                'da'                               : 'Danish',
                'da-DK'                            : 'Danish (Denmark)',
                'nl'                               : 'Dutch',
                'nl-BE'                            : 'Dutch (Belgium)',
                'nl-NL'                            : 'Dutch (Netherlands)',
                'en'                               : 'English',
                'en-AU'                            : 'English (Australia)',
                'en-CA'                            : 'English (Canada)',
                'en-IN'                            : 'English (India)',
                'en-IE'                            : 'English (Ireland)',
                'en-MT'                            : 'English (Malta)',
                'en-NZ'                            : 'English (New Zealand)',
                'en-PH'                            : 'English (Philippines)',
                'en-SG'                            : 'English (Singapore)',
                'en-ZA'                            : 'English (South Africa)',
                'en-GB'                            : 'English (United Kingdom)',
                'en-US'                            : 'English (United States)',
                'et'                               : 'Estonian',
                'et-EE'                            : 'Estonian (Estonia)',
                'fi'                               : 'Finnish',
                'fi-FI'                            : 'Finnish (Finland)',
                'fr'                               : 'French',
                'fr-BE'                            : 'French (Belgium)',
                'fr-CA'                            : 'French (Canada)',
                'fr-FR'                            : 'French (France)',
                'fr-LU'                            : 'French (Luxembourg)',
                'fr-CH'                            : 'French (Switzerland)',
                'de'                               : 'German',
                'de-AT'                            : 'German (Austria)',
                'de-DE'                            : 'German (Germany)',
                'de-LU'                            : 'German (Luxembourg)',
                'de-CH'                            : 'German (Switzerland)',
                'el'                               : 'Greek',
                'el-CY'                            : 'Greek (Cyprus)',
                'el-GR'                            : 'Greek (Greece)',
                'he'                               : 'Hebrew',
                'he-IL'                            : 'Hebrew (Israel)',
                'hi-IN'                            : 'Hindi (India)',
                'hu'                               : 'Hungarian',
                'hu-HU'                            : 'Hungarian (Hungary)',
                'is'                               : 'Icelandic',
                'is-IS'                            : 'Icelandic (Iceland)',
                'id'                               : 'Indonesian',
                'id-ID'                            : 'Indonesian (Indonesia)',
                'ga'                               : 'Irish',
                'ga-IE'                            : 'Irish (Ireland)',
                'it'                               : 'Italian',
                'it-IT'                            : 'Italian (Italy)',
                'it-CH'                            : 'Italian (Switzerland)',
                'ja'                               : 'Japanese',
                'ja-JP'                            : 'Japanese (Japan)',
                'ja-JP-u-ca-japanese-x-lvariant-JP': 'Japanese (Japan,JP)',
                'ko'                               : 'Korean',
                'ko-KR'                            : 'Korean (South Korea)',
                'lv'                               : 'Latvian',
                'lv-LV'                            : 'Latvian (Latvia)',
                'lt'                               : 'Lithuanian',
                'lt-LT'                            : 'Lithuanian (Lithuania)',
                'mk'                               : 'Macedonian',
                'mk-MK'                            : 'Macedonian (Macedonia)',
                'ms'                               : 'Malay',
                'ms-MY'                            : 'Malay (Malaysia)',
                'mt'                               : 'Maltese',
                'mt-MT'                            : 'Maltese (Malta)',
                'no'                               : 'Norwegian',
                'no-NO'                            : 'Norwegian (Norway)',
                'nn-NO'                            : 'Norwegian (Norway,Nynorsk)',
                'pl'                               : 'Polish',
                'pl-PL'                            : 'Polish (Poland)',
                'pt'                               : 'Portuguese',
                'pt-BR'                            : 'Portuguese (Brazil)',
                'pt-PT'                            : 'Portuguese (Portugal)',
                'ro'                               : 'Romanian',
                'ro-RO'                            : 'Romanian (Romania)',
                'ru'                               : 'Russian',
                'ru-RU'                            : 'Russian (Russia)',
                'sr'                               : 'Serbian',
                'sr-BA'                            : 'Serbian (Bosnia and Herzegovina)',
                'sr-Latn'                          : 'Serbian (Latin)',
                'sr-Latn-BA'                       : 'Serbian (Latin,Bosnia and Herzegovina)',
                'sr-Latn-ME'                       : 'Serbian (Latin,Montenegro)',
                'sr-Latn-RS'                       : 'Serbian (Latin,Serbia)',
                'sr-ME'                            : 'Serbian (Montenegro)',
                'sr-CS'                            : 'Serbian (Serbia and Montenegro)',
                'sr-RS'                            : 'Serbian (Serbia)',
                'sk'                               : 'Slovak',
                'sk-SK'                            : 'Slovak (Slovakia)',
                'sl'                               : 'Slovenian',
                'sl-SI'                            : 'Slovenian (Slovenia)',
                'es'                               : 'Spanish',
                'es-AR'                            : 'Spanish (Argentina)',
                'es-BO'                            : 'Spanish (Bolivia)',
                'es-CL'                            : 'Spanish (Chile)',
                'es-CO'                            : 'Spanish (Colombia)',
                'es-CR'                            : 'Spanish (Costa Rica)',
                'es-DO'                            : 'Spanish (Dominican Republic)',
                'es-EC'                            : 'Spanish (Ecuador)',
                'es-SV'                            : 'Spanish (El Salvador)',
                'es-GT'                            : 'Spanish (Guatemala)',
                'es-HN'                            : 'Spanish (Honduras)',
                'es-MX'                            : 'Spanish (Mexico)',
                'es-NI'                            : 'Spanish (Nicaragua)',
                'es-PA'                            : 'Spanish (Panama)',
                'es-PY'                            : 'Spanish (Paraguay)',
                'es-PE'                            : 'Spanish (Peru)',
                'es-PR'                            : 'Spanish (Puerto Rico)',
                'es-ES'                            : 'Spanish (Spain)',
                'es-US'                            : 'Spanish (United States)',
                'es-UY'                            : 'Spanish (Uruguay)',
                'es-VE'                            : 'Spanish (Venezuela)',
                'sv'                               : 'Swedish',
                'sv-SE'                            : 'Swedish (Sweden)',
                'th'                               : 'Thai',
                'th-TH'                            : 'Thai (Thailand)',
                'th-TH-u-nu-thai-x-lvariant-TH'    : 'Thai (Thailand,TH)',
                'tr'                               : 'Turkish',
                'tr-TR'                            : 'Turkish (Turkey)',
                'uk'                               : 'Ukrainian',
                'uk-UA'                            : 'Ukrainian (Ukraine)',
                'vi'                               : 'Vietnamese',
                'vi-VN'                            : 'Vietnamese (Vietnam)'
        ].with { it + getDYNAMIC_CONSTANT().LOCALES }
    }

    static getCUSTOMER_IMPORT_FIELD_MAPPING() {
        return [
                firstName   : "First Name",
                lastName    : "Last Name",
                customerType: "Customer Type",
                sex         : "Sex",
                userName    : "Email",
                addressLine1: "Address Line 1",
                addressLine2: "Address Line 2",
                city        : "City",
                country     : "Country",
                fax         : "Fax",
                state       : "State",
                postCode    : "Post Code",
                phone       : "Phone",
                mobile      : "Mobile",
                abn         : "ABN",
                abnBranch   : "ABN Branch",
                storeCredit : "Store Credit",
                status      : "Status",
                groups      : "Groups",
                companyName : "Company Name",
                password    : "Password"
        ].with { it + getDYNAMIC_CONSTANT().CUSTOMER_IMPORT_FIELD_MAPPING }
    }

    public static getCUSTOMER_EXPORT_IMPORT_FIELDS() {
        return [
                firstName   : "first.name",
                lastName    : "last.name",
                customerType: "customer.type",
                sex         : "sex",
                userName    : "email",
                addressLine1: "address.line.1",
                addressLine2: "address.line.2",
                city        : "city",
                country     : "country",
                state       : "state",
                postCode    : "post.code",
                phone       : "phone",
                mobile      : "mobile",
                fax         : "fax",
                status      : "status",
                abn         : "abn",
                abnBranch   : "abn.branch",
                storeCredit : "store.credit",
                groups      : "groups",
                companyName : "company.name"
        ].with { it + getDYNAMIC_CONSTANT().CUSTOMER_EXPORT_IMPORT_FIELDS }
    }


    static getREDIRECT_IMPORT_FIELD_MAPPING() {
        return [
                oldUrl: "Old URL",
                newUrl: "New URL"
        ].with { it + getDYNAMIC_CONSTANT().REDIRECT_IMPORT_FIELD_MAPPING }
    }

    static getLICENSE_KEYS() {
        return [
                API: 'api',
                ACL: 'allow_acl_feature'
        ].with { it + getDYNAMIC_CONSTANT().LICENSE_KEYS }
    }

    static getINTERVAL_TYPE() {
        return [
                min: "minute",
                hr : "hour",
                day: "day"
        ].with { it + getDYNAMIC_CONSTANT().INTERVAL_TYPE }
    }

    static getEVENT_TRACKING_TYPE() {
        return [
                ecommerce: "ecommerce.event.tracking",
                pdp      : "product.details.page",
                cartp    : "cart.page",
                billing  : "edit.billing.address",
                shipping : "edit.shipping.address",
                payment  : "payment.method",
                orderp   : "order"
        ].with { it + getDYNAMIC_CONSTANT().EVENT_TRACKING_TYPE }
    }

    static getPRODUCT_WIDGET_FILTER() {
        return [
                (DomainConstants.PRODUCT_WIDGET_FILTER.NONE)       : "all",
                (DomainConstants.PRODUCT_WIDGET_FILTER.FEATURED)   : "featured",
                (DomainConstants.PRODUCT_WIDGET_FILTER.TOP_SELLING): "top.selling",
                (DomainConstants.PRODUCT_WIDGET_FILTER.CATEGORY)   : "category"
        ].with { it + getDYNAMIC_CONSTANT().PRODUCT_WIDGET_FILTER }
    }

    static getGALLERY_CONTENT_TYPES() {
        return [
                (DomainConstants.GALLERY_CONTENT_TYPES.ALBUM)   : "album",
                (DomainConstants.GALLERY_CONTENT_TYPES.PRODUCT) : "product",
                (DomainConstants.GALLERY_CONTENT_TYPES.ARTICLE) : "article",
                (DomainConstants.GALLERY_CONTENT_TYPES.CATEGORY): "category",
        ].with { it + getDYNAMIC_CONSTANT().GALLERY_CONTENT_TYPES }
    }

    static getTEMPLATE_CONTAINER_CLASS() {
        return [
                "full-width" : "full.width",
                "fixed-width": "fixed.width",
        ].with { it + getDYNAMIC_CONSTANT().TEMPLATE_CONTAINER_CLASS }
    }

    static getCREDIT_CARD_TYPES() {
        return [
                visa       : "visa",
                mastercard : "master.card",
                visa_master: "visa.master",
                amex       : "american.express",
                diners     : "diners",
                discover   : "discover",
                jcb        : "jcb",
                union_pay  : "china.union.pay"
        ].with { it + getDYNAMIC_CONSTANT().CREDIT_CARD_TYPES }
    }

    static getDOCUMENT_LAYOUT_NAME_MAP() {
        return [
                WC_SAMPLE_INVOICE_1        : "Sample Invoice 1",
                WC_SAMPLE_INVOICE_2        : "Sample Invoice 2",
                WC_SAMPLE_ORDER_1          : "Sample Order 1",
                WC_SAMPLE_ORDER_2          : "Sample Order 2",
                WC_SAMPLE_SHIPPING_LABEL_1 : "Sample Shipping Label 1",
                WC_SAMPLE_SHIPPING_LABEL_2 : "Sample Shipping Label 2",
                WC_SAMPLE_PICKING_SLIP_1   : "Sample Picking Slip 1",
                WC_SAMPLE_PICKING_SLIP_2   : "Sample Picking Slip 2",
                WC_SAMPLE_DELIVERY_DOCKET_1: "Sample Delivery Docket 1",
                WC_SAMPLE_DELIVERY_DOCKET_2: "Sample Delivery Docket 2",
        ]
    }

    static getDOCUMENT_PAPER_SIZE() {
        return [
                letter: "Letter (US) (215.9mm x 279.4mm)",
                legal : "Legal (US) (215.9mm x 355.6mm)",
                ledger: "Ledger (US) (279.4mm x 431.8mm)",
                a0    : "A0 (841mm x 1189mm)",
                a1    : "A1 (594mm x 841mm)",
                a2    : "A2 (420mm x 594mm)",
                a3    : "A3 (297mm x 420mm)",
                a4    : "A4 (210mm x 297mm)"
        ].with { it + getDYNAMIC_CONSTANT().DOCUMENT_PAPER_SIZE }
    }

    static getDOCUMENT_FONT_FAMILIES() {
        return [
                Roboto           : "Roboto",
                'Open Sans'      : "Open Sans",
                Lato             : "Lato",
                Montserrat       : "Montserrat",
                'Source Sans Pro': "Source Sans Pro"
        ].with { it + getDYNAMIC_CONSTANT().DOCUMENT_FONT_FAMILIES }
    }

    private static COMMON_MACROS = [
            payment_details : [
                    "payment_details.date",
                    "payment_details.amount",
                    "payment_details.track_info",
                    "payment_details.payment_method",
                    "payment_details.payer_info",
                    "payment_details.status"
            ],
            shipping_details: [
                    "shipped_quantity",
                    "shipment_method",
                    "order_quantity",
                    "track_info",
                    "product_name",
            ],
            billing_address : [
                    "billing_address.country",
                    "billing_address.customer_name",
                    "billing_address.address_line1",
                    "billing_address.city",
                    "billing_address.phone",
                    "billing_address.post_code",
                    "billing_address.mobile",
                    "billing_address.state",
                    "billing_address.fax",
                    "billing_address.email"
            ],
            shipping_address: [
                    "shipping_address.country",
                    "shipping_address.customer_name",
                    "shipping_address.address_line1",
                    "shipping_address.city",
                    "shipping_address.phone",
                    "shipping_address.post_code",
                    "shipping_address.mobile",
                    "shipping_address.state",
                    "shipping_address.fax",
                    "shipping_address.email"
            ],
            order_details   : [
                    "total",
                    "total_discount",
                    "handling_cost",
                    "due",
                    "sub_total",
                    "paid",
                    "total_tax",
                    "payment_surcharge",
                    "total_shipping_cost",
                    "shipping_tax",
            ],
            store_details   : [
                    "store_name",
                    "store_url",
                    "store_logo",
                    "store_address",
                    "store_phone",
                    "store_mobile",
                    "store_fax",
                    "store_email",
                    "store_city",
                    "store_country",
                    "store_state",
                    "store_post_code",
                    "link_to_admin_panel",
                    "customer_login_url",
                    "store_address_line1",
                    "store_address_line2",
                    "currency_symbol"
            ]
    ]

    static getDOCUMENT_MACROS() {
        return [
                invoice        : [
                        "operator_full_name",
                        "order_date",
                        "customer_name",
                        "payment_info",
                        "order_id",
                        "url"
                ].with {
                    it + COMMON_MACROS.billing_address + COMMON_MACROS.shipping_address + COMMON_MACROS.order_details + COMMON_MACROS.payment_details + COMMON_MACROS.store_details
                },
                shipping       : [
                        "date",
                        "customer_name",
                        "order_id",
                        "track_info"
                ].with {
                    it + COMMON_MACROS.shipping_address + COMMON_MACROS.shipping_details + COMMON_MACROS.store_details
                },
                picking_slip   : [
                        "order_date",
                        "customer_name",
                        "order_id",
                        "shipment_method",
                        "order_comment"
                ].with {
                    it + COMMON_MACROS.shipping_details + COMMON_MACROS.order_details + COMMON_MACROS.billing_address + COMMON_MACROS.shipping_address + COMMON_MACROS.store_details
                },
                delivery_docket: [
                        "order_id",
                        "order_date",
                        "customer_name",
                        "order_comment"
                ].with {
                    it + COMMON_MACROS.shipping_details + COMMON_MACROS.order_details + COMMON_MACROS.billing_address + COMMON_MACROS.shipping_address + COMMON_MACROS.store_details
                },
                order          : [
                        "order_date",
                        "customer_name",
                        "payment_info",
                        "order_id",
                        "url",
                        "operator_full_name",
                        "shipment_method"
                ].with {
                    it + COMMON_MACROS.payment_details + COMMON_MACROS.shipping_details + COMMON_MACROS.billing_address + COMMON_MACROS.shipping_address + COMMON_MACROS.order_details + COMMON_MACROS.store_details
                }
        ].with { it + getDYNAMIC_CONSTANT().DOCUMENT_MACROS }
    }

    static CSS_BORDER_STYLE = [
            "none", "hidden", "dotted", "dashed",
            "solid", "double", "groove", "ridge",
            "inset", "outset", "initial", "inherit"
    ]

    static ORIENTATION = [
            "horizontal", "vertical"
    ]

    static getINVOICE_TABLE_COLUMN() {
        return [
                "Name"            : '%if:item.url%<a href="%item.url%">%if% %item.product_name% %if:item.variations%(%item.variations%)%if%%if:item.url%</a>%if%',
                "Unit Price"      : '%currency_symbol%%item.price%',
                "Ordered Quantity": '%item.quantity%',
                "Discount"        : '%item.discount%',
                "Tax"             : '%currency_symbol%%item.tax%',
                "Price"           : '%currency_symbol%%item.total_with_tax_with_discount%'
        ].with { it + getDYNAMIC_CONSTANT().INVOICE_TABLE_COLUMN }
    }

    static getORDER_TABLE_COLUMN() {
        return [
                "Name"            : '%if:item.url%<a href="%item.url%">%if% %item.product_name% %if:item.variations%(%item.variations%)%if%%if:item.url%</a>%if%',
                "Unit Price"      : '%currency_symbol%%item.price%',
                "Ordered Quantity": '%item.quantity%',
                "Discount"        : '%item.discount%',
                "Tax"             : '%currency_symbol%%item.tax%',
                "Price"           : '%currency_symbol%%item.total_with_tax_with_discount%'
        ].with { it + getDYNAMIC_CONSTANT().ORDER_TABLE_COLUMN }
    }

    static getPICKING_SLIP_TABLE_COLUMN() {
        return [
                "Product Name"    : '%if:item.url%<a href="%item.url%">%if% %item.product_name% %if:item.variations%(%item.variations%)%if%%if:item.url%</a>%if%',
                "sku"             : '%item.sku%',
                "Ordered Quantity": '%item.quantity%',
                "Back Ordered"    : '%item.back_ordered%',
                "Pick Task"       : '%item.pick_task%',
                "Quantity Picked" : '<span style="border: 1px #000 solid;height: 20px;width: 20px;display: inline-block;"></span>'
        ].with { it + getDYNAMIC_CONSTANT().PICKING_SLIP_TABLE_COLUMN }
    }

    static getDELIVERY_DOCKET_TABLE_COLUMN() {
        return [
                "Product Name"    : '%if:item.url%<a href="%item.url%">%if% %item.product_name% %if:item.variations%(%item.variations%)%if%%if:item.url%</a>%if%',
                "SKU"             : '%item.sku%',
                "Ordered Quantity": '%item.quantity%',
                "Back Ordered"    : '%item.back_ordered%',
                "Quantity Shipped": '%item.quantity_shipped%'
        ].with { it + getDYNAMIC_CONSTANT().DELIVERY_DOCKET_TABLE_COLUMN }
    }


    static getCLOUD_CONFIG() {
        return [
                "SYSTEM_DEFAULT": "SYSTEM_DEFAULT",
                "DEFAULT"       : "DEFAULT"
        ].with { it + getDYNAMIC_CONSTANT().CLOUD_CONFIG }
    }

    static getDEFAULT_COUNTRY_WITH_DEFAULT_TAX_PROFILE_MAPPING() {
        return [
                (DomainConstants.TAX_DEFAULT_COUNTRY_TYPE.AUSTRALIA): "BUILT_IN_GST_AU_GST",
                (DomainConstants.TAX_DEFAULT_COUNTRY_TYPE.CANADA)   : "BUILT_IN_GST_CA_GST",
                //(DomainConstants.TAX_DEFAULT_COUNTRY_TYPE.EU)       : "USA Tax Profile",
                (DomainConstants.TAX_DEFAULT_COUNTRY_TYPE.NEWZELAND): "BUILT_IN_GST_NZ",
                (DomainConstants.TAX_DEFAULT_COUNTRY_TYPE.UK)       : "BUILT_IN_VAT_UK",
                (DomainConstants.TAX_DEFAULT_COUNTRY_TYPE.USA)      : "BUILT_IN_SALES_TAX_US",
                //(DomainConstants.TAX_DEFAULT_COUNTRY_TYPE.REST)     : "USA Tax Profile"
        ].with { it + getDYNAMIC_CONSTANT().DEFAULT_COUNTRY_WITH_DEFAULT_TAX_PROFILE_MAPPING }
    }

    static getROUNDING_TYPE() {
        return [
                (DomainConstants.ROUNDING_TYPE.HALF_EVEN): "nearest",
                (DomainConstants.ROUNDING_TYPE.UP)       : "up",
                (DomainConstants.ROUNDING_TYPE.DOWN)     : "down"
        ].with { it + getDYNAMIC_CONSTANT().ROUNDING_TYPE }
    }

    static getDOMAIN_IMAGE_RESIZE_TYPE() {
        return [
                (DomainConstants.DOMAIN_NAME.BLOG_CATEGORY)          : NamedConstants.IMAGE_RESIZE_TYPE.BLOG_CATEGORY_IMAGE,
                (DomainConstants.DOMAIN_NAME.BLOG_POST)              : NamedConstants.IMAGE_RESIZE_TYPE.BLOG_POST_IMAGE,
                (DomainConstants.DOMAIN_NAME.VARIATION_OPTION)       : NamedConstants.IMAGE_RESIZE_TYPE.VARIATION_IMAGE,
                (DomainConstants.DOMAIN_NAME.WCSTATIC_RESOURCE)      : NamedConstants.IMAGE_RESIZE_TYPE.FAVICON_IMAGE,
                (DomainConstants.DOMAIN_NAME.VARIATION_PRODUCT_IMAGE): NamedConstants.IMAGE_RESIZE_TYPE.PRODUCT_IMAGE,
                (DomainConstants.DOMAIN_NAME.SIMPLIFIED_EVENT_IMAGE) : NamedConstants.IMAGE_RESIZE_TYPE.EVENT_IMAGE,
                (DomainConstants.DOMAIN_NAME.GENERAL_EVENT_IMAGE)    : NamedConstants.IMAGE_RESIZE_TYPE.EVENT_IMAGE,
                (DomainConstants.DOMAIN_NAME.VENUE_LOCATION_IMAGE)   : NamedConstants.IMAGE_RESIZE_TYPE.LOCATION_IMAGE,
                (DomainConstants.DOMAIN_NAME.NAVIGATION_ITEM)        : NamedConstants.IMAGE_RESIZE_TYPE.NAVIGATION_ITEM,
                (DomainConstants.DOMAIN_NAME.ALBUM_IMAGE)            : NamedConstants.IMAGE_RESIZE_TYPE.ALBUM_IMAGE,
                (DomainConstants.DOMAIN_NAME.MOCK_STATIC_RESOURCE)   : NamedConstants.IMAGE_RESIZE_TYPE.IMAGE_WIDGET,
                (DomainConstants.DOMAIN_NAME.CATEGORY)               : NamedConstants.IMAGE_RESIZE_TYPE.CATEGORY_IMAGE,
                (DomainConstants.DOMAIN_NAME.PRODUCT_IMAGE)          : NamedConstants.IMAGE_RESIZE_TYPE.PRODUCT_IMAGE,
                (DomainConstants.DOMAIN_NAME.STORE_DETAIL)           : NamedConstants.IMAGE_RESIZE_TYPE.STORE_LOGO,
        ].with { it + getDYNAMIC_CONSTANT().DOMAIN_IMAGE_RESIZE_TYPE }
    }

}
