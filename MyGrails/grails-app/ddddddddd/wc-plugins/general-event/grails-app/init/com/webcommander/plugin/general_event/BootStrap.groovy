package com.webcommander.plugin.general_event

import com.webcommander.AppResourceTagLib
import com.webcommander.AutoGeneratedPage
import com.webcommander.acl.Permission
import com.webcommander.acl.RolePermission
import com.webcommander.admin.MessageSource
import com.webcommander.admin.Role
import com.webcommander.common.ImageService
import com.webcommander.config.EmailTemplate
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.Layout
import com.webcommander.design.WidgetService
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.models.CartItem
import com.webcommander.plugin.general_event.mixin_service.WidgetService as ECWS
import com.webcommander.plugin.general_event.model.CartGeneralEventTicket
import com.webcommander.plugin.general_event.model.CartRecurringEventTicket
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.Order
import grails.util.Holders

class BootStrap {
    private final String GENERAL_EVENT_MANAGEMENT = "general_event_management"
    private final String WIDGET_TYPE = "generalEventManagement"
    private final String GENERAL_EVENT = "generalEvent"
    private final String GENERAL_EVENT_DETAILS = "general.event"

    List domain_constants = [
            [constant:"SITE_CONFIG_TYPES", key: "GENERAL_EVENT_WIDGET", value: "general_event_widget"],
            [constant:"WIDGET_TYPE", key: "GENERAL_EVENT", value: WIDGET_TYPE],
            [constant:"WIDGET_CONTENT_TYPE", key: "GENERAL_EVENT", value: "general_event"],
            [constant:"EMAIL_TYPE", key: "GENERAL_EVENT_MANAGEMENT", value: GENERAL_EVENT_MANAGEMENT],
            [constant:"AUTO_GENERATED_PAGES", key: "GENERAL_EVENT_DETAILS", value: GENERAL_EVENT_DETAILS],
            [constant:"AUTO_GENERATED_PAGES", key: "VENUE_LOCATION_DETAILS_PAGE", value: "venue"],
            [constant:"DEFAULT_IMAGES", key: "general-event", value: "event-image"],
            [constant:"DEFAULT_IMAGES", key: "venue-location", value: "location-image"]
    ]

    List named_constants = [
            [constant:"IMAGE_RESIZE_TYPE", key: "EVENT_IMAGE", value:"general.event.management"],
            [constant:"IMAGE_RESIZE_TYPE", key: "LOCATION_IMAGE", value:"general.event.management"],
            [constant:"EMAIL_SETTING_MESSAGE_KEYS", key: GENERAL_EVENT_MANAGEMENT, value:"general.event.management"],
            [constant:"WIDGET_MESSAGE_KEYS", key: GENERAL_EVENT + ".title", value:"general.event.widget"],
            [constant:"WIDGET_MESSAGE_KEYS", key: GENERAL_EVENT + ".label", value:GENERAL_EVENT_DETAILS],
            [constant:"CART_OBJECT_TYPES", key: "GENERAL_EVENT_TICKET", value:"general_event_ticket"],
            [constant:"CART_OBJECT_TYPES", key: "RECURRING_EVENT_TICKET", value:"recurring_event_ticket"],
            [constant:"CART_OBJECT_TYPES", key: "GENERAL_EVENT_VENUE_TICKET", value:"general_event_venue_ticket"],
            [constant:"CART_OBJECT_TYPES", key: "RECURRING_EVENT_VENUE_TICKET", value:"recurring_event_venue_ticket"]
    ]
    List permissions =  [
            ["edit", false], ["remove", false], ["create", false], ["view.list", false], ["edit.permission", false]
    ]

    def templates = [
            [label: "new.purchase.ticket", identifier: "new-purchase-ticket-general-event", subject: "Your ticket(s) for %event_name%", isActiveReadonly: true, type: GENERAL_EVENT_MANAGEMENT],
            [label: "personalized.program", identifier: "personalized-program-for-general-event", subject: "Personalized Program for %event_name%", isActiveReadonly: true, type: GENERAL_EVENT_MANAGEMENT, contentType: DomainConstants.EMAIL_CONTENT_TYPE.TEXT_HTML]
    ]

    def event_setting_data = [
            'update_general_event_ticket_stock' : 'after_payment'
    ]
    Map siteMessages = [
            "book.now": "Book Now",
            "your.requested.event.date.expired": "Your requested event %event_name% is out of date.",
            "requested.quantity.ticket.not.available": "Your requested %requested_quantity% tickets not available.",
            "you.can.buy.maximum.quantity.for.event" : "You can buy maximum %maximum_quantity% tickets for event."
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
        if(!AutoGeneratedPage.findByName(DomainConstants.AUTO_GENERATED_PAGES.GENERAL_EVENT_DETAILS)) {
            new AutoGeneratedPage(name: DomainConstants.AUTO_GENERATED_PAGES.GENERAL_EVENT_DETAILS, title: "Event Details - %GENERAL_EVENT_NAME%", layout: Layout.first()).save()
        }

        if(!AutoGeneratedPage.findByName(DomainConstants.AUTO_GENERATED_PAGES.VENUE_LOCATION_DETAILS_PAGE)) {
            new AutoGeneratedPage(name: DomainConstants.AUTO_GENERATED_PAGES.VENUE_LOCATION_DETAILS_PAGE, title: "Venue Location Details Page - %LOCATION%",
                    layout: Layout.first()).save()
        }

        if(SiteConfig.findAllByTypeAndConfigKey(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "update_general_event_ticket_stock").size() == 0) {
            event_setting_data.each { entry ->
                new SiteConfig(type: "e_commerce", configKey: entry.key, value: entry.value).save();
            }
        }
        siteMessages.each {
            if (!MessageSource.findByMessageKeyAndLocale(it.key, 'all')) {
                new MessageSource(messageKey: it.key, message: it.value, locale: "all").save();
            }
        }
        if(!Permission.findByType("general_event")) {
            Role role = Role.findByName("Admin")
            permissions.each { entry ->
                Permission permission =  new Permission(name: entry[0], label: entry[0], applicableOnEntity: entry[1], type: "general_event").save()
                new RolePermission(role: role, permission: permission, isAllowed: true).save()
            }
        }
        if(!EmailTemplate.findAllByType(DomainConstants.EMAIL_TYPE.GENERAL_EVENT_MANAGEMENT).size()) {
            templates.each {
                new EmailTemplate(it).save()
            }
        }
        AppUtil.initializeDefaultImages(['general-event', 'venue-location'])
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeEmailTemplates(*templates.identifier)
            util.removePermission("general_event")
            siteMessages.each {
                util.removeSiteMessage(it.key)
            }
            util.removeSiteConfig("e_commerce")
            util.removeAutoPage(GENERAL_EVENT_DETAILS, "venue")
            NamedConstants.removeConstant(named_constants)
            DomainConstants.removeConstant(domain_constants)
            GeneralEventResourceTaglib.RESOURCES_PATH.each { resource ->
                util.deleteResourceFolders(resource.value)
            }
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin general event From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(WidgetService).metaClass.mixin ECWS
        Holders.grailsApplication.mainContext.getBean(AppResourceTagLib).metaClass.mixin GeneralEventResourceTaglib
        ImageService.RESIZABLE_IMAGE_SIZES.put("event-image", [
                150: [150, 150],
                300: [300, 300],
                600: [600, 600]
        ])
        ImageService.RESIZABLE_IMAGE_SIZES.put("location-image", [
                100: [150, 150],
                300: [300, 300]
        ])

        HookManager.register(GENERAL_EVENT_DETAILS + "-breadcrumb") { response ->
            response.currentItem = AppUtil.request.macros.GENERAL_EVENT_NAME
            return response
        }

        TenantContext.eachParallelWithWait(tenantInit)

        AppEventManager.on("order-confirm", { cart ->
            def session = AppUtil.session
            Order order = Order.proxy(cart.orderId)
            def cartObject
            for(CartItem item : cart.cartItemList) {
                cartObject = item.object
                if(cartObject instanceof CartGeneralEventTicket || cartObject instanceof CartRecurringEventTicket) {
                    def event
                    Boolean isRecurring = cartObject instanceof CartGeneralEventTicket ? false : true
                    Long id = isRecurring ? cartObject.parentEventId : cartObject.eventId
                    int quantity = item.quantity;
                    event = isRecurring ? RecurringEvents.proxy(cartObject.eventId) : GeneralEvent.proxy(id)
                    Map params = session[id.toString()];
                    if(params) {
                        for(int m = 1; m <= quantity; m++) {
                            params.remove("ticket"+m);
                        }
                        params.each { k, v ->
                            if(v instanceof Map) {
                                return;
                            }
                            String[] names = k.toString().split("\\.");
                            String ticket = names[0]
                            String name = names[1]
                            isRecurring ? new GeneralEventCustomFieldData(order: order, recurringEvent: event, fieldName: name, fieldValue: v, ticket: ticket ).save() :
                                    new GeneralEventCustomFieldData(order: order, generalEvent: event, fieldName: name, fieldValue: v, ticket: ticket ).save();
                        }
                    }
                }
            }
        })

        AppEventManager.on("order-cancelled before-order-update", { orderId ->
            List dataList = GeneralEventCustomFieldData.findAllByOrder(Order.get(orderId));
            dataList*.delete();
        })
    }
}
