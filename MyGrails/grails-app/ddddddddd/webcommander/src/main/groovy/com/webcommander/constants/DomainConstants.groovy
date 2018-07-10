package com.webcommander.constants

import com.webcommander.tenant.TenantContext

class DomainConstants {

    final static String DEPLOY_TOKEN = "DSFGH8765786SDGFFGJKSDFJ42307234453NJKSDBFH";

    final static String REST_OF_THE_WORLD = "REST_OF_THE_WORLD";

    final static String RESPONSE_CODE = "code";
    final static String RESPONSE_MESSAGE = "message";

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

    static getTAX_DEFAULT_COUNTRY_TYPE() {
        return [
                AUSTRALIA: "AU",
                CANADA   : "CA",
                EU       : "EU",
                NEWZELAND: "NZ",
                UK       : "GB",
                USA      : "US",
                REST     : "rest_of_world"
        ].with { it + getDYNAMIC_CONSTANT().TAX_DEFAULT_COUNTRY_TYPE }
    }

    static getTAX_CONFIGURATION_TYPE() {
        return [
                DEFAULT: "default",
                MANUAL : "manual"
        ].with { it + getDYNAMIC_CONSTANT().TAX_CONFIGURATION_TYPE }
    }

    static getROUNDING_TYPE() {
        return [
                HALF_EVEN: "nearest",
                UP       : "up",
                DOWN     : "down"
        ].with { it + getDYNAMIC_CONSTANT().ROUNDING_TYPE }
    }

    static getSITE_CONFIG_TYPES() {
        return [
                AWS_S3                        : "aws_s3",
                ADMINISTRATION                : "administration",
                BILLING_ADDRESS_FIELD         : "billing_address_field",
                CART_PAGE                     : "cart_page",
                CATEGORY_IMAGE                : "category_image",
                CATEGORY_PAGE                 : "category_page",
                CHECKOUT_PAGE                 : "checkout_page",
                CURRENCY                      : "currency",
                CUSTOMER_LOGIN_SETTINGS       : "customer_login",
                CUSTOMER_PROFILE_PAGE         : "customer_profile_page",
                CUSTOMER_REGISTRATION_FIELD   : "customer_registration_field",
                CUSTOMER_REGISTRATION_SETTINGS: "customer_registration",
                CUSTOMER_ACCOUNT_INFORMATION  : "customer_account_information",
                E_COMMERCE                    : "e_commerce",
                EMAIL                         : "email",
                FRONTEND_PAGES                : "frontend_pages",
                GENERAL                       : "general",
                GET_STARTED_WIZARD            : "get_started_wizard",
                GOOGLE_EVENT_TRACKING         : "google_event_tracking",
                LICENSE                       : "license",
                LOCALE                        : "locale",
                ORDER_PRINT_AND_EMAIL         : "order_print_and_email",
                PRODUCT                       : "product",
                PRODUCT_IMAGE                 : "product_image",
                PRODUCT_PAGE                  : "product_page",
                RELATED_PRODUCT               : "related_product",
                PRODUCT_PROPERTIES            : "product_properties",
                RESPONSIVE                    : "responsive",
                SEARCH_PAGE                   : "search_page",
                SHIPPING                      : "shipping",
                SHIPPING_ADDRESS_FIELD        : "shipping_address_field",
                STORE_CREDIT                  : "store_credit",
                TAX                           : "tax",
                WEBTOOL                       : "webtool",
                SEO_CONFIG                    : "seo_config",
                MY_ACCOUNT_PAGE               : "my_account_page",
                CLOUD_STORAGE                 : "cloud-storage",
                MANAGEMENT_HUB                : "management_hub",
                MANAGE_MY_ACCOUNT             : "manage_my_account",
                OVERVIEW                      : "overview",
                MY_ORDERS                     : "my_orders",
                MY_CARTS                      : "my_carts",
                MY_LISTS                      : "my_lists",
                MY_ENTITLEMENTS               : "my_entitlements",
                ELASTIC_SEARCH                : "elastic_search",

        ].with { it + getDYNAMIC_CONSTANT().SITE_CONFIG_TYPES }
    }

    static getDEFAULT_IMAGES() {
        return [
                product     : "product-image",
                category    : "category-image",
        ].with { it + getDYNAMIC_CONSTANT().DEFAULT_IMAGES }
    }

    static getEMAIL_CONTENT_TYPE() {
        return [
                TEXT     : "text",
                HTML     : "html",
                TEXT_HTML: "text/html"
        ].with { it + getDYNAMIC_CONSTANT().EMAIL_CONTENT_TYPE }
    }

    static getEMAIL_TYPE() {
        return [
                CUSTOMER: "customer",
                ADMIN   : "admin",
                ORDER   : "order",
                PAYMENT : "payment",
                LICENSE : "license"
        ].with { it + getDYNAMIC_CONSTANT().EMAIL_TYPE }
    }
    /*
    * getECOMMERCE_EMAIL_TYPE_CHECKLIST is used to check a email type show or hide on ecommerce off
    * This map contains EMAIL_TYPE value as key and true if that type only available in ecommerce mode
    * If a type available in both then no need to entry in this map
    * */
    static getECOMMERCE_EMAIL_TYPE_CHECKLIST() {
        return [
                order   : true,
                payment : true,
        ].with { it + getDYNAMIC_CONSTANT().ECOMMERCE_EMAIL_TYPE_CHECKLIST }
    }
/*
    * getECOMMERCE_EMAIL_TEMPLATE_CHECKLIST is used to check a email TEMPLATE show or hide on ecommerce off
    * This map contains EmailTemplate identifier as key in _ case and true if that type only available in ecommerce mode
    * If a type available in both then no need to entry in this map
    * */
    static getECOMMERCE_EMAIL_TEMPLATE_CHECKLIST() {
        return [
                admin_order_comment_notification   : true,
                store_credit_request: true,
                customer_order_comment_notification: true,
        ].with { it + getDYNAMIC_CONSTANT().ECOMMERCE_EMAIL_TEMPLATE_CHECKLIST }
    }

    static getPAGE_VISIBILITY() {
        return [
                OPEN      : "open",
                HIDDEN    : "hidden",
                RESTRICTED: "restricted"
        ].with { it + getDYNAMIC_CONSTANT().PAGE_VISIBILITY }
    }

    static getPAGE_VISIBLE_TO() {
        return [
                ALL     : "all",
                SELECTED: "selected"
        ].with { it + getDYNAMIC_CONSTANT().PAGE_VISIBLE_TO }
    }

    static getWIDGET_TYPE() {
        return [
                ARTICLE           : "article",
                BREADCRUMB        : "breadcrumb",
                CART              : "cart",
                CATEGORY          : "category",
                CURRENCY          : "currency",
                GALLERY           : "gallery",
                HTML              : "html",
                IMAGE             : "image",
                STORE_LOGO        : "storeLogo",
                SPACER            : "spacer",
                LOGIN             : "login",
                NAVIGATION        : "navigation",
                NEWSLETTER        : "newsletter",
                PRODUCT           : "product",
                SEARCH            : "search",
                SOCIAL_MEDIA_LIKE : "socialMediaLike",
                SOCIAL_MEDIA_LINK : "socialMediaLink",
                SOCIAL_MEDIA_SHARE: "socialMediaShare"
        ].with { it + getDYNAMIC_CONSTANT().WIDGET_TYPE }
    }

    static getECOMMERCE_WIDGET_TYPE_CHECKLIST() {
        return [
                cart              : true,
                category          : true,
                currency          : true,
                product           : true,
        ].with { it + getDYNAMIC_CONSTANT().ECOMMERCE_WIDGET_TYPE_CHECKLIST }
    }

    static getECOMMERCE_PERMISSION_CHECKLIST() {
        return [
                order             : true,
                category          : true,
                currency          : true,
                product           : true,
                shipping          : true,
                payment_gateway   : true,
                tax               : true,
        ].with { it + getDYNAMIC_CONSTANT().ECOMMERCE_PERMISSION_CHECKLIST }
    }

    static getECOMMERCE_CUSTOMER_PROFILE_CHECKLIST() {
        return [
                overview          : true,
                my_eorders        : true,
                my_carts          : true,
                my_lists          : true,
                my_entitlements   : true,
                my_wallet         : true,
                billing_address   : true,
                shipping_address  : true
        ].with { it + getDYNAMIC_CONSTANT().ECOMMERCE_CUSTOMER_PROFILE_CHECKLIST }
    }

    static getPRODUCT_WIDGET_TYPE() {
        return [
                PRODUCT_NAME             : "productName",
                PRODUCT_SUMMARY          : "productSummary",
                PRODUCT_SKU              : "productSku",
                PRODUCT_CATEGORY         : "productCategory",
                PRODUCT_DOWNLOADABLE_SPEC: "productDownloadableSpec",
                PRODUCT_MODEL            : "productModel",
                PRODUCT_IMAGE            : 'productImage',
                PRICE                    : 'price',
                STOCK_MARK               : "stockMark",
                COMBINED_PRODUCT         : "combinedProduct",
                ADD_CART                 : "addCart",
                LIKE_US                  : "likeus",
                SOCIAL_MEDIA_SHARE       : "socialMediaShare",
                INFOMATION               : "information",
                RELATED                  : "related",
                CONDITION                : "condition",
                PROPERTIES               : "properties",
        ].with { it + getDYNAMIC_CONSTANT().PRODUCT_WIDGET_TYPE }
    }

    static getWIDGET_CONTENT_TYPE() {
        return [ // Do not reorder this MAP
                 CATEGORY    : "category",
                 ARTICLE     : "article",
                 PRODUCT     : "product",
                 ALBUM       : "album",
                 NAVIGATION  : "navigation",
        ].with { it + getDYNAMIC_CONSTANT().WIDGET_CONTENT_TYPE }
    }

    static getAUTO_GENERATED_PAGES() {
        return [
                CUSTOMER_LOGIN              : "login",
                PAYMENT_PAGE                : "credit.card.payment",
                PAYMENT_SUCCESS_PAGE        : "post.payment",
                PRODUCT_PAGE                : "product",
                CATEGORY_PAGE               : "category",
                CUSTOMER_REGISTRATION       : "registration",
                CUSTOMER_PROFILE            : "profile",
                CUSTOMER_RESET_PASSWORD     : "reset.password",
                CART_PAGE                   : "cart",
                ARTICLE_DETAILS_PAGE        : "article",
                SEARCH_RESULT               : "search.result",
                CHECKOUT                    : "checkout",
                SUBSCRIBE_NEWSLETTER        : "newsletter.subscription",
                UNSUBSCRIBE_NEWSLETTER      : "newsletter.unsubscription",
                GUEST_CUSTOMER_ORDER_COMMENT: "guest.customer.order.comment",
                FILTER_DETAIL_PAGE          : "filter"
        ].with { it + getDYNAMIC_CONSTANT().AUTO_GENERATED_PAGES }
    }

    static getECOMMERCE_AUTO_GENERATED_PAGES_CHECKLIST() {
        return [
                credit_card_payment : true,
                post_payment        : true,
                cart                : true,
                checkout            : true,
                product             : true,
                category            : true,
                guest_customer_order_comment: true,
        ].with { it + getDYNAMIC_CONSTANT().ECOMMERCE_AUTO_GENERATED_PAGES_CHECKLIST }
    }

    static getECOMMERCE_DASHLET_CHECKLIST() {
        return [
                webCommerce             : true,
                latest_order            : true,
                latest_product          : true,
        ].with { it + getDYNAMIC_CONSTANT().ECOMMERCE_DASHLET_CHECKLIST }
    }

    static getECOMMERCE_PLUGIN_CHECKLIST() {
        return [:].with { it + getDYNAMIC_CONSTANT().ECOMMERCE_PLUGIN_CHECKLIST }
    }

    static getCUSTOMER_STATUS() {
        return [
                ACTIVE           : "A",
                INACTIVE         : "I",
                APPROVAL_AWAITING: "W"
        ].with { it + getDYNAMIC_CONSTANT().CUSTOMER_STATUS }
    }

    static getNAVIGATION_ITEM_TYPE() {
        return [
                NONE               : "",
                PAGE               : "page",
                PRODUCT            : "product",
                CATEGORY           : "category",
                URL                : "url",
                EMAIL              : "email",
                AUTO_GENERATED_PAGE: "autoGeneratedPage"
        ].with { it + getDYNAMIC_CONSTANT().NAVIGATION_ITEM_TYPE }
    }

    static getDISCOUNT_TYPES() {
        return [
                DISCUONT_BY_PRODUCT_PRICE : "dpp",
                DISCOUNT_BY_SALES_QUANTITY: "dsq",
                DISCOUNT_BY_PROFIT_MARGIN : "dpm",
                FLAT_DISCOUNT             : "fd"
        ].with { it + getDYNAMIC_CONSTANT().DISCOUNT_TYPES }
    }

    static getDISCOUNT_PROFILE_RULE_PRECEDENCE() {
        return [
                RULES_WITH_HIGHER_PRIORITY: "hp",
                RULES_WITH_LOWEST_DISCOUNT: "ldc",
                RULES_WITH_HIGHEST_DISOUNT: "hdc"
        ].with { it + getDYNAMIC_CONSTANT().DISCOUNT_PROFILE_RULE_PRECEDENCE }
    }

    static getSHIPPING_POLICY_TYPE() {
        return [
                FREE_SHIPPING   : "fs",
                FLAT_RATE       : "fr",
                SHIP_BY_PRICE   : 'sba',
                SHIP_BY_QUANTITY: 'sbq',
                SHIP_BY_WEIGHT  : 'sbw',
                API             : 'api'
        ].with { it + getDYNAMIC_CONSTANT().SHIPPING_POLICY_TYPE }
    }

    static getSHIPPING_API() {
        return [
                AUSPOST: 'auspost'
        ].with { it + getDYNAMIC_CONSTANT().SHIPPING_API }
    }

    static getPACKING_ALGORITHM() {
        return [
                INDIVIDUAL: "individual",
                COMBINED  : "combined"
        ].with { it + getDYNAMIC_CONSTANT().PACKING_ALGORITHM }
    }

    static getSHIPPING_API_SERVICE_TYPE() {
        return [
                REGULAR_PARCEL                                                           : 'rp',
                REGULAR_PARCEL_STANDARD                                                  : 'rps',
                REGULAR_PARCEL_REGISTER_POST                                             : 'rprp',
                REGULAR_PARCEL_REGISTER_POST_WITH_DELIVERY_CONFAFIRMATION                : 'rprpdc',
                REGULAR_PARCEL_REGISTER_POST_WITH_EXTRA_COVER                            : 'rprpec',
                REGULAR_PARCEL_REGISTER_POST_WITH_DELIVERY_CONFAFIRMATION_AND_EXTRA_COVER: 'rprpdcec',
                EXPRESS_PARCEL_SERVICE                                                   : 'eps',
                PLATINUM_PARCEL_SERVICE                                                  : 'pps',
                PLATINUM_PARCEL_SERVICE_WITH_EXTRA_COVER                                 : 'ppsec',
        ].with { it + getDYNAMIC_CONSTANT().SHIPPING_API_SERVICE_TYPE }
    }

    static getSHIPPING_PROFILE_RULE_PRECEDENCE() {
        return [
                RULES_WITH_HIGHER_PRIORITY      : "hp",
                RULES_WITH_LOWEST_SHIPPING_COST : "lsc",
                RULES_WITH_HIGHEST_SHIPPING_COST: "hsc"
        ].with { it + getDYNAMIC_CONSTANT().SHIPPING_PROFILE_RULE_PRECEDENCE }
    }

    static getCOPY_SHIPPING_PROFILE_OPTIONS() {
        return [
                USE_RULE_AND_CONFIG : "urc",
                COPY_RULE_AND_CONFIG: "crc"
        ].with { it + getDYNAMIC_CONSTANT().COPY_SHIPPING_PROFILE_OPTIONS }
    }

    static getCOPY_SHIPPING_RULE_OPTIONS() {
        return [
                USE_RULE : "ur",
                COPY_RULE: "cr"
        ].with { it + getDYNAMIC_CONSTANT().COPY_SHIPPING_RULE_OPTIONS }
    }

    static getPAYMENT_GATEWAY_CODE() {
        return [
                PAY_IN_STORE    : 'PIS',
                CHEQUE          : 'CHQ',
                MONEY_ORDER     : 'MOR',
                BANK_DEPOSIT    : 'BDP',
                ACCOUNTS_PAYABLE: 'ACP',
                STORE_CREDIT    : 'SCR',
                PAYPAL          : 'PPL',
                CREDIT_CARD     : 'CRD',
                API             : 'API'
        ].with { it + getDYNAMIC_CONSTANT().PAYMENT_GATEWAY_CODE }
    }

    static getCARD_PAYMENT_PROCESSOR_CODE() {
        return [:].with { it + getDYNAMIC_CONSTANT().CARD_PAYMENT_PROCESSOR_CODE }
    }

    static getSURCHARGE_TYPE() {
        return [
                FLAT_SURCHARGE           : 'flat_surcharge',
                NO_SURCHARGE             : 'no_surcharge',
                SURCHARGE_ON_AMOUNT_RANGE: 'surcharge_on_amount_range'
        ].with { it + getDYNAMIC_CONSTANT().SURCHARGE_TYPE }
    }

    static getCUSTOMER_REG_TYPE() {
        return [
                OPEN             : "open",
                AWAITING_APPROVAL: "awaiting_approval",
                CLOSED           : "closed"
        ].with { it + getDYNAMIC_CONSTANT().CUSTOMER_REG_TYPE }
    }

    static getSHOPPING_CART_TOTAL_PRICE() {
        return [
                WITHOUT_TAX_WITHOUT_DISCOUNT: 'without_tax_without_discount',
                WITHOUT_TAX_WITH_DISCOUNT   : 'without_tax_with_discount',
                WITH_TAX_WITHOUT_DISCOUNT   : 'with_tax_without_discount',
                WITH_TAX_WITH_DISCOUNT      : 'with_tax_with_discount'
        ].with { it + getDYNAMIC_CONSTANT().SHOPPING_CART_TOTAL_PRICE }
    }

    static getPRODUCT_AVAILABLE_FOR() {
        return [
                EVERYONE: "everyone",
                CUSTOMER: "customer",
                SELECTED: "selected"
        ].with { it + getDYNAMIC_CONSTANT().PRODUCT_AVAILABLE_FOR }
    }

    static getPRODUCT_RESTRICT_PRICE_FOR() {
        return [
                NONE           : "none",
                EVERYONE       : "everyone",
                EXCEPT_CUSTOMER: "except_customer",
                EXCEPT_SELECTED: "except_selected"
        ].with { it + getDYNAMIC_CONSTANT().PRODUCT_RESTRICT_PRICE_FOR }
    }

    static getPRODUCT_RESTRICT_PURCHASE_FOR() {
        return [
                NONE           : "none",
                EVERYONE       : "everyone",
                EXCEPT_CUSTOMER: "except_customer",
                EXCEPT_SELECTED: "except_selected"
        ].with { it + getDYNAMIC_CONSTANT().PRODUCT_RESTRICT_PURCHASE_FOR }
    }

    static getPRODUCT_CONDITION() {
        return [
                NEW        : "new",
                USED       : "used",
                REFURBISHED: "refurbished"
        ].with { it + getDYNAMIC_CONSTANT().PRODUCT_CONDITION }
    }

    static getPRODUCT_TYPE() {
        return [
                PHYSICAL    : "physical",
                DOWNLOADABLE: "downloadable",
        ].with { it + getDYNAMIC_CONSTANT().PRODUCT_TYPE }
    }

    static getCATEGORY_AVAILABLE_FOR() {
        return [
                EVERYONE: "everyone",
                CUSTOMER: "customer",
                SELECTED: "selected"
        ].with { it + getDYNAMIC_CONSTANT().CATEGORY_AVAILABLE_FOR }
    }

    static getORDER_STATUS() {
        return [
                PENDING  : "pending",
                CANCELLED: "cancelled",
                COMPLETED: "completed"
        ].with { it + getDYNAMIC_CONSTANT().ORDER_STATUS }
    }

    static getORDER_TOTAL() {
        return [
                GREATER: "greater",
                LESS   : "less"
        ].with { it + getDYNAMIC_CONSTANT().ORDER_TOTAL }
    }

    static getSHIPPING_STATUS() {
        return [
                AWAITING : "awaiting",
                COMPLETED: "completed",
                PARTIAL  : "partial"
        ].with { it + getDYNAMIC_CONSTANT().SHIPPING_STATUS }
    }

    static getORDER_DELIVERY_TYPE() {
        return [
                SHIPPING       : "shipping",
                STORE_PICKUP   : "store_pickup",
                OTHERS_SHIPPING: "others_shipping"
        ].with { it + getDYNAMIC_CONSTANT().ORDER_DELIVERY_TYPE }
    }

    static getORDER_PAYMENT_STATUS() {
        return [
                UNPAID        : 'unpaid',
                PAID          : 'paid',
                PARTIALLY_PAID: 'partial'
        ].with { it + getDYNAMIC_CONSTANT().ORDER_PAYMENT_STATUS }
    }

    static getSHIPPING_AMOUNT_TYPE() {
        return [
                FLAT   : 'f',
                PERCENT: 'p'
        ].with { it + getDYNAMIC_CONSTANT().SHIPPING_AMOUNT_TYPE }
    }

    static getPAYMENT_STATUS() {
        return [
                AWAITING : "awaiting",
                CANCELLED: "cancelled",
                SUCCESS  : "success",
                REFUNDED : "refunded",
                PENDING  : "pending",
                FAILED   : "failed"
        ].with { it + getDYNAMIC_CONSTANT().PAYMENT_STATUS }
    }

    static getTERMS_AND_CONDITION_TYPE() {
        return [
                PAGE         : "page",
                EXTERNAL_LINK: "link",
                SPECIFIC_TEXT: "text"
        ].with { it + getDYNAMIC_CONSTANT().TERMS_AND_CONDITION_TYPE }
    }

    static getUPDATE_STOCK() {
        return [
                AFTER_ORDER   : "after_order",
                AFTER_PAYMENT : "after_payment",
                AFTER_SHIPMENT: "after_shipment"
        ].with { it + getDYNAMIC_CONSTANT().UPDATE_STOCK }
    }

    static getOUT_OF_STOCK_MESSAGE_TYPE() {
        return [
                ADD_AVAILABLE: "add_available",
                SELL_AWAY    : "sell_away",
                DO_NOT_SELL  : "do_not_sell"
        ].with { it + getDYNAMIC_CONSTANT().OUT_OF_STOCK_MESSAGE_TYPE }
    }

    static getPPL_SERVICE_URL() {
        return [
                TEST: "https://www.sandbox.paypal.com/us/cgi-bin/webscr",
                LIVE: "https://www.paypal.com/us/cgi-bin/webscr"
        ].with { it + getDYNAMIC_CONSTANT().PPL_SERVICE_URL }
    }

    static getSTATUS() {
        return [
                POSITIVE     : "positive",
                NEGATIVE     : "negative",
                DIPLOMATIC   : "diplomatic",
                POSITIVEMINUS: "positive-minus",
                NEGATIVEPLUS : "negative-plus"
        ].with { it + getDYNAMIC_CONSTANT().STATUS }
    }

    static getREQUEST_ATTR_KEYS() {
        return [
                REQUEST_BODY  : "request_body",
                ADMIN         : "admin",
                CUSTOMER      : "customer",
                API_CLIENT    : "api_client",
                IS_API_REQUEST: "is_api_request"
        ].with { it + getDYNAMIC_CONSTANT().REQUEST_ATTR_KEYS }
    }

    static getSESSION_ATTR_KEYS() {
        return [
                ADMIN               : "admin",
                ONLY_API_USER       : "only_api_user",
                AFTER_LOGIN_URL     : "after_login_url",
                REQUEST_STORE_CREDIT: "requested_store_credit"
        ].with { it + getDYNAMIC_CONSTANT().SESSION_ATTR_KEYS }
    }

    static getOAUTH_CONSTANTS() {
        return [
                CLIENT_ID          : "client_id",
                CLIENT_SECRET      : "client_secret",
                REDIRECT_URI       : "redirect_uri",
                GRANT_TYPE         : "grant_type",
                RESPONSE_TYPE      : "response_type",
                SCOPE              : "scope",
                CODE               : "code",
                AUTHORIZATION_CODE : "authorization_code",
                ACCESS_DENIED      : "access_denied",
                ACCESS_TOKEN       : "access_token",
                REFRESH_TOKEN      : "refresh_token",
                RESOURCE_OWNER_TYPE: "resource_owner_type",
                EXPIRE_TIME        : 3600
        ].with { it + getDYNAMIC_CONSTANT().OAUTH_CONSTANTS }
    }

    static getOAUTH_ERROR_CODE() {
        return [
                INVALID_ACEESS_TOKEN  : "invalid_access_token",
                INVALID_REFRESH_TOKEN : "invalid_refresh_token",
                INVALID_CLIENT        : "invalid_client",
                INVALID_SECRET        : "invalid_secret",
                INVALID_REDIRET_URI   : "invalid_redirect_uri",
                INVALID_GRANT_REQUEST : "invalid_grant_request",
                INVALID_REQUEST       : "invalid_request",
                DISABLE_CLIENT        : "client_disabled",
                ACCESS_TOKEN_EXPIRE   : "token_expire",
                INVALID_RESOURCE_OWNER: "invalid_resource_owner"
        ].with { it + getDYNAMIC_CONSTANT().OAUTH_ERROR_CODE }
    }

    static getRESOURCE_OWNER_TYPES() {
        return [
                ANONYMOUS: "anonymous",
                CUSTOMER : "customer",
                OPERATOR : "operator"

        ].with { it + getDYNAMIC_CONSTANT().RESOURCE_OWNER_TYPES }
    }

    static getDEFAULT_PAYMENT_GATE_WAYS() {
        return [
                STORE_CREDIT: [
                        ORDER               : 1,
                        IDENTIFIER          : "StoreCredit",
                        PAYMENT_GATEWAY_CODE: PAYMENT_GATEWAY_CODE.STORE_CREDIT
                ]
        ].with { it + getDYNAMIC_CONSTANT().DEFAULT_PAYMENT_GATE_WAYS }
    }

    static getPRODUCT_WIDGET_FILTER() {
        return [
                NONE       : "none",
                FEATURED   : "featured",
                TOP_SELLING: "top_selling",
                CATEGORY   : "category"
        ].with { it + getDYNAMIC_CONSTANT().PRODUCT_WIDGET_FILTER }
    }

    static getGALLERY_CONTENT_TYPES() {
        return [
                ALBUM       : WIDGET_CONTENT_TYPE.ALBUM,
                ARTICLE     : WIDGET_CONTENT_TYPE.ARTICLE,
                PRODUCT     : WIDGET_CONTENT_TYPE.PRODUCT,
                CATEGORY    : WIDGET_CONTENT_TYPE.CATEGORY,
        ].with { it + getDYNAMIC_CONSTANT().GALLERY_CONTENT_TYPES }
    }

    static Map getPRODUCT_EXPORT_MANDATORY_FIELDS() {
        return [name: "name", sku: "sku", basePrice: "basePrice"].with { it + getDYNAMIC_CONSTANT().PRODUCT_EXPORT_MANDATORY_FIELDS }
    }

    static getCREDIT_CARD_TYPES() {
        return [
                VISA       : "visa",
                MASTERCARD : "mastercard",
                VISA_MASTER: "visa_master",
                AMEX       : "amex",
                DINERS     : "diners",
                DISCOVER   : "discover",
                JCB        : "jcb",
                UNION_PAY  : "union_pay"
        ].with { it + getDYNAMIC_CONSTANT().CREDIT_CARD_TYPES }
    }

    static getDOCUMENT_TYPES() {
        return [
                INVOICE        : 'invoice',
                SHIPPING       : 'shipping',
                PICKING_SLIP   : 'picking_slip',
                DELIVERY_DOCKET: 'delivery_docket',
                ORDER          : 'order'
        ].with { it + getDYNAMIC_CONSTANT().DOCUMENT_TYPES }
    }

    static getDOCUMENT_MAPPING() {
        return [
                'send-invoice'     : "invoice",
                'shipment-complete': "shipping",
                'picking-slip'     : "picking_slip",
                'delivery-docket'  : "delivery_docket",
                'create-order'     : "order"
        ].with { it + getDYNAMIC_CONSTANT().DOCUMENT_MAPPING }
    }

    static getCLOUD_TYPE() {
        return [
                "AWS_S3": "AWS_S3"
        ].with { it + getDYNAMIC_CONSTANT().CLOUD_TYPE }
    }

    static getDOMAIN_NAME() {
        return [
                BLOG_CATEGORY               : "BlogCategory",
                BLOG_POST                   : "BlogPost",
                VARIATION_OPTION            : "VariationOption",
                WCSTATIC_RESOURCE           : "WcStaticResource",
                VARIATION_PRODUCT_IMAGE     : "VariationProductImage",
                SIMPLIFIED_EVENT_IMAGE      : "SimplifiedEventImage",
                GENERAL_EVENT_IMAGE         : "GeneralEventImage",
                VENUE_LOCATION_IMAGE        : "VenueLocationImage",
                NAVIGATION_ITEM             : "NavigationItem",
                ALBUM_IMAGE                 : "AlbumImage",
                MOCK_STATIC_RESOURCE        : "MockStaticResource",
                CATEGORY                    : "Category",
                PRODUCT_IMAGE               : "ProductImage",
                STORE_DETAIL                : "StoreDetail"
        ].with { it + getDYNAMIC_CONSTANT().DOMAIN_NAME }
    }

}
