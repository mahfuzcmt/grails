package com.webcommander.plugin.gift_registry

import com.webcommander.ApplicationTagLib
import com.webcommander.admin.Country
import com.webcommander.admin.Customer
import com.webcommander.admin.State
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommanderMailService
import com.webcommander.common.Email
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.CartManager
import com.webcommander.manager.HookManager
import com.webcommander.models.ProductData
import com.webcommander.models.ProductInCart
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Address
import com.webcommander.webcommerce.CombinedProduct
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.Product
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.util.TypeConvertingMap
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@Initializable
@Transactional
class GiftRegistryService {
    CommanderMailService commanderMailService
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    ApplicationTagLib app

    static void initialize() {
        AppEventManager.on("before-gift-registry-delete", { id ->
            OrderAndRegistryMapping.createCriteria().list {
                eq("registry.id", id)
            }*.delete()
        })

        AppEventManager.on("before-customer-delete", {id ->
            Customer customer = Customer.proxy(id)
            GiftRegistry.where {
                customer == customer
            }.list().each {
                AppEventManager.fire("before-gift-registry-delete")
                it.delete()
            }
        })

        AppEventManager.on("before-product-delete", {productId ->
            GiftRegistryItem.createCriteria().list {
                eq("product.id", productId)
            }*.delete()
        })

        AppEventManager.on("before-gift-registry-delete", {id ->
            def giftRegistry = GiftRegistry.proxy(id)
            if(giftRegistry) {
                giftRegistry.giftItems*.delete()
                giftRegistry.giftItems.clear()
            }
        })
    }

    static {
        AppEventManager.on("order-confirm", {cart ->
            def session = AppUtil.session;
            Order order = Order.get(cart.orderId);
            if(session.cart_for_gift_registry && !OrderAndRegistryMapping.findByOrder(order)) {
                GiftRegistry registry = GiftRegistry.get(session.cart_for_gift_registry);
                OrderAndRegistryMapping mapping = new OrderAndRegistryMapping();
                mapping.order = order;
                mapping.registry = registry;
                mapping.save();
            }
        });

        AppEventManager.on("paid-for-cart", { carts ->
            carts.each { cart ->
                Order order = Order.get(cart.orderId);
                OrderAndRegistryMapping mapping = OrderAndRegistryMapping.findByOrder(order);
                if (mapping) {
                    GiftRegistryService service = Holders.applicationContext.getBean(GiftRegistryService)
                    service.updateStatus(mapping)
                }
            }
        });

        AppEventManager.on('paid-for-order', {Order order ->
            OrderAndRegistryMapping mapping = OrderAndRegistryMapping.findByOrder(order);
            if (mapping && order.paymentStatus == DomainConstants.ORDER_PAYMENT_STATUS.PAID) {
                GiftRegistryService service = Holders.applicationContext.getBean(GiftRegistryService)
                service.updateStatus(mapping)
            }
        });
    }

    Boolean save(GrailsParameterMap params) {
        def session = AppUtil.session
        Customer customer = Customer.get(session.customer)
        GiftRegistry giftRegistry = params.id ? GiftRegistry.get(params.id) : new GiftRegistry();
        giftRegistry.name = params.name
        giftRegistry.eventName = params.eventName;
        giftRegistry.eventDate = params.eventDate.toDate().gmt(session.timezone);
        giftRegistry.eventDetails = params.eventDetails;
        Address address = giftRegistry.address ?: new Address();
        address.firstName = params.firstName
        address.lastName = params.lastName
        address.addressLine1 = params.addressLine1
        address.addressLine2 = params.addressLine2
        address.postCode = params.postCode
        address.phone = params.phone
        address.mobile = params.mobile
        address.fax = params.fax
        address.email = params.email
        address.country = Country.get(params.countryId.toLong(0))
        address.state = State.get(params.state ? params.state.id : 0)
        address.city = params.city
        address.save();
        giftRegistry.address = address;
        giftRegistry.customer = customer;
        giftRegistry.save();
        if(giftRegistry.hasErrors()) {
            return false
        }
        return true;
    }

    def addToGiftRegistry(Product product, ProductData productData, Integer quantity, TypeConvertingMap params) {
        GiftRegistry giftRegistry = GiftRegistry.get(params.giftRegistry);
        GiftRegistryItem giftItem;
        List variations = [];
        if(productData.isCombined && !productData.isCombinationPriceFixed && productData.isCombinationQuantityFlexible) {
            Map included = JSON.parse(params.included)
            included.each { id, count ->
                variations.add(CombinedProduct.get(id).includedProduct.name + " (" + count + ")")
            }
        }
        variations = HookManager.hook("variationsForGiftRegistryAdd", variations, productData, params)
        List<GiftRegistryItem> items = []
        if(giftRegistry) {
            items = giftRegistry.giftItems.findAll {
                it.product.id == productData.id;
            }
        }
        giftItem = items ?  items.find {
            CartManager.isVariationMatch(it.variations, variations)
        } : null;
        ProductInCart productInCart = null
        productInCart = (ProductInCart) HookManager.hook("get-product-in-cart", productInCart, productData, params)
        productInCart = productInCart ?: new ProductInCart(productData, params)
        if (giftItem) {
            items.remove(giftItem);
            productInCart.validate(giftItem.quantity + quantity + (items ? items.sum {it.quantity} : 0))
            giftItem.quantity = giftItem.quantity + quantity;
        } else {
            giftItem = new GiftRegistryItem();
            productInCart.validate(quantity + (items ? items.sum {it.quantity} : 0))
            giftItem.quantity = quantity;
            giftItem.included = params.included;
            if (productData.attrs.selectedVariation) {
                giftItem.variation = productData.attrs.selectedVariation.toString();
            }
            if (params.combination) {
               giftItem.combination =  params.list("combination");
            }
            giftItem.product= product;
            giftItem.variations = variations;
            giftRegistry.addToGiftItems(giftItem)
        }
        giftItem.save();
        return giftItem;
    }

    Integer getAddedQuantity(ProductData productData, TypeConvertingMap params) {
        GiftRegistry giftRegistry = GiftRegistry.get(params.giftRegistry)
        List<GiftRegistryItem> items = giftRegistry.giftItems.findAll {
            it.product.id == productData.id;
        }
        return items ? items.sum {
            it.quantity
        } : 0
    }

    Boolean remove(Long id) {
        GiftRegistry registry = GiftRegistry.get(id);
        AppEventManager.fire("before-gift-registry-delete", [id])
        registry.delete();
        return true;
    }

    Boolean removeItem(Long id) {
        GiftRegistryItem item = GiftRegistryItem.get(id);
        item.delete();
        return true;
    }

    Boolean share(GrailsParameterMap params) {
        List names = params.list("names[]");
        List emails = params.list("emails[]");
        GiftRegistry registry = GiftRegistry.get(params.id);
        emails.eachWithIndex {def it, def i ->
            Email email = new Email(name: names[i], email: it)
            email.save();
            registry.addToEmails(email);
        }
        sendShareMail(registry, emails.join(","), params.name ?: "", params.comment);
        registry.save();
        return !registry.hasErrors()
    }

    void sendShareMail(GiftRegistry registry, String recipient, String to, String comment) {
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("gift-registry-share")
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "gift_registry_name":
                    refinedMacros[it.key] = registry.name.encodeAsBMHTML();
                    break;
                case "event_name":
                    refinedMacros[it.key] = registry.eventName.encodeAsBMHTML();
                    break;
                case "event_date":
                    refinedMacros[it.key] = registry.eventDate.toEmailFormat();
                    break;
                case "gift_registry_link":
                    refinedMacros[it.key] = app.baseUrl() + "gift-registry/products/" + registry.id;
                    break;
                case "customer_first_name":
                    refinedMacros[it.key] = registry.customer.firstName.encodeAsBMHTML();
                    break;
                case "customer_last_name":
                    refinedMacros[it.key] = registry.customer.lastName.encodeAsBMHTML();
                    break;
                case "to_name":
                    refinedMacros[it.key] = to;
                    break;
                case "comment":
                    refinedMacros[it.key] = comment.encodeAsBMHTML();
                    break;
            }
        }
        commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, recipient)
    }

    def updateStatus(OrderAndRegistryMapping mapping) {
        Order order = mapping.order
        GiftRegistry registry = mapping.registry
        order.items.each {orderItem ->
            GiftRegistryItem giftItem;
            List<GiftRegistryItem> items = registry.giftItems.findAll {
                it.product.id == orderItem.productId;
            }
            giftItem = items ?  items.find {
                CartManager.isVariationMatch(it.variations, orderItem.variations)
            } : null;
            if (giftItem) {
                giftItem.purchased = giftItem.purchased + orderItem.quantity
                giftItem.save();
            }
        }
        mapping.delete();
    }

}
