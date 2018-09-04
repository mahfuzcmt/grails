package com.webcommander.plugin.product_review

import com.webcommander.acl.RolePermission
import com.webcommander.admin.ConfigService
import com.webcommander.acl.Permission
import com.webcommander.admin.Role
import com.webcommander.config.EmailTemplate
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.plugin.PluginManager
import com.webcommander.plugin.PluginManager
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.Product
import com.webcommander.design.ProductWidgetService
import com.webcommander.plugin.product_review.mixin_service.ProductWidgetService as PRWS
import grails.util.Holders
import grails.util.TypeConvertingMap

class BootStrap {

    private final String PRODUCT_REVIEW = "productReview"
    private final String REVIEW_RATING = "review_rating"
    private final String REVIEW = "review"

    List domain_constants = [
            [constant:"PRODUCT_WIDGET_TYPE", key: "PRODUCT_REVIEW", value: PRODUCT_REVIEW],
            [constant:"SITE_CONFIG_TYPES", key: "REVIEW_RATING", value: REVIEW_RATING],
            [constant:"ECOMMERCE_DASHLET_CHECKLIST", key: "product_review", value: true],
            [constant:"ECOMMERCE_EMAIL_TEMPLATE_CHECKLIST", key: "product_review_notification", value: true],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "product_review", value: true],
    ]

    List named_constants = [
            [constant:"PRODUCT_WIDGET_MESSAGE_KEYS", key: PRODUCT_REVIEW + ".title", value:"product.review.widget"],
            [constant:"PRODUCT_WIDGET_MESSAGE_KEYS", key: PRODUCT_REVIEW + ".label", value:"product.review"],
    ]

    Map initialData = SiteConfig.INITIAL_DATA[REVIEW_RATING] = [
            product_review: "on",
            who_can_review: "every_one",
            show_review: "wait",
            review_per_page: "5"
    ]
    List permissions =  [
            ["remove", true],  ["view.list", false], ["change.status", false]
    ]

    ProductReviewService productReviewService

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
        ConfigService.addTab(REVIEW, [
                url: "productReviewAdmin/config",
                message_key: "product.review",
                ecommerce  : true
        ])
        if (SiteConfig.countByType(REVIEW_RATING) == 0) {
            initialData.each { entry ->
                new SiteConfig(type: REVIEW_RATING, configKey: entry.key, value: entry.value).save()
            }
        }
        if(SiteConfig.countByTypeAndConfigKey(DomainConstants.SITE_CONFIG_TYPES.CATEGORY_PAGE, "is_rating_active") == 0) {
            new SiteConfig(type: DomainConstants.SITE_CONFIG_TYPES.CATEGORY_PAGE, configKey: "is_rating_active", value: "false").save()
        }
        if(PluginManager.isInstalled("compare-product")) {
            if(!SiteConfig.findByTypeAndConfigKey("compare_product", "is_rating_active")) {
                new SiteConfig(type: "compare_product", configKey: "is_rating_active", value: "true").save()
            }
        }
        if(!SiteConfig.findByTypeAndConfigKey(DomainConstants.SITE_CONFIG_TYPES.CATEGORY_PAGE, "is_rating_active")) {
            new SiteConfig(type: DomainConstants.SITE_CONFIG_TYPES.CATEGORY_PAGE, configKey: "is_rating_active", value: "true").save()
        }

        if(!EmailTemplate.findByIdentifier("product-review-notification")) {
            Map emailTemplate = [
                    label: "product.review.notification",
                    identifier: "product-review-notification",
                    subject: "%customer_name%, has reviewed the %product_name%",
                    isActiveReadonly: true,
                    type: DomainConstants.EMAIL_TYPE.CUSTOMER
            ]
            new EmailTemplate(emailTemplate).save()
        }
        if(!Permission.findByType("product_review")) {
            Role role = Role.findByName("Admin")
            permissions.each { entry ->
                Permission permission = new Permission(name: entry[0], label: entry[0], applicableOnEntity: entry[1], type: "product_review").save()
                new RolePermission(role: role, permission: permission, isAllowed: true).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(REVIEW)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeAutoGeneratePageWidget(NamedConstants.AUTO_GENERATED_PAGE_WIDGET.PRODUCT_WIDGET, PRODUCT_REVIEW)
            util.removePermission("product_review")
            util.removeEmailTemplates("product-review-notification")
            util.removeSiteConfig("compare_product")
            util.removeSiteConfig(DomainConstants.SITE_CONFIG_TYPES.CATEGORY_PAGE)
            util.removeSiteConfig(REVIEW_RATING)
            NamedConstants.removeConstant(named_constants)
            DomainConstants.removeConstant(domain_constants)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin product-review From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }
    
    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(ProductWidgetService).metaClass.mixin PRWS

        AppEventManager.on("plugin-installed", { id ->
            TenantContext.each {
                if(id == "compare-product" && !SiteConfig.findByTypeAndConfigKey("compare_product", "is_rating_active")) {
                    new SiteConfig(type: "compare_product", configKey: "is_rating_active", value: "true").save()
                }
            }

        })
        
        TenantContext.eachParallelWithWait(tenantInit)

        Product.marshallerInclude.add("avgRating")
        Product.fieldMarshaller["avgRating"] = { Product product ->
            return  productReviewService.getAvgRatingByProduct(new TypeConvertingMap([forRender: true, productId: product.id]))
        }
    }
}
