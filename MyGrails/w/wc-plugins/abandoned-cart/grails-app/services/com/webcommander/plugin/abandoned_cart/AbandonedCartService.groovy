package com.webcommander.plugin.abandoned_cart

import com.webcommander.admin.Customer
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommanderMailService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.converter.json.JSON
import com.webcommander.events.AppEventManager
import com.webcommander.manager.CartManager
import com.webcommander.manager.HookManager
import com.webcommander.models.Cart
import com.webcommander.models.CartItem
import com.webcommander.models.CartObject
import com.webcommander.plugin.abandoned_cart.job.ScheduleJob
import com.webcommander.util.AppUtil
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.quartz.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@Initializable
class AbandonedCartService {
    CommanderMailService commanderMailService
    Scheduler quartzScheduler
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    com.webcommander.ApplicationTagLib app

    static void initialize() {
        AppEventManager.on("before-customer-delete", { id ->
            List<AbandonedCart> carts = AbandonedCart.createCriteria().list {
                eq("customer.id", id)
            }
            carts.each {
                AppEventManager.fire("before-abandoned-cart-delete", [it.id])
                it.delete()
            }
        })
        
        AppEventManager.on("before-abandoned-cart-delete", { id ->
            AbandonedCart cart = AbandonedCart.proxy(id)
            AbandonedCartItem.where {
                abandonedCart == cart
            }.list().each {
                it.variations = []
            }*.delete()
        })
        AppEventManager.on("before-product-put-in-trash", { id ->
            List<AbandonedCartItem> items = AbandonedCartItem.createCriteria().list {
                eq("itemId", id)
                eq("itemType", "product")
            }
            items*.delete()
        })
    }

    static {
        AppEventManager.on("session-terminate") { session ->
            println("------------------ Debug Abandoned Cart Log ------------------")
            AbandonedCartService abandonedCartService = Holders.applicationContext.getBean("abandonedCartService")
            if(session.customer) {
                abandonedCartService.addToAbandonedCart(session)
            }
        }
        AppEventManager.on(DomainConstants.SITE_CONFIG_TYPES.ABANDONED_CART + "-after-settings-updated", { config ->
            AbandonedCartService abandonedCartService = Holders.applicationContext.getBean("abandonedCartService")
            abandonedCartService.reScheduleJob()
        })
    }

    def startScheduler() {
        int interval = AppUtil.getAppConfig("webcommander.scheduler.tick", 6)
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(interval).repeatForever()
        JobDetail job = JobBuilder.newJob(ScheduleJob.class).withIdentity("abandonedCartEmailJob", "abandonedCart").build()
        Trigger trigger = TriggerBuilder
            .newTrigger()
            .withIdentity("abandonedCartEmailJob", "abandonedCart")
            .withSchedule(scheduleBuilder)
            .build()
        quartzScheduler.scheduleJob(job, trigger)
    }

    private Closure getCriteriaClosure(Map params) {
        def session = AppUtil.session
        Closure closure = {
            createAlias("customer", "c")
            if(params.searchText) {
                or {
                    def text = params.searchText.trim().encodeAsLikeText()
                    ilike("c.firstName", "%${text}%")
                    ilike("c.lastName", "%${text}%")
                    ilike("c.userName", "%${text}%")
                    createAlias("c.address", "a")
                    ilike("a.email", "%${text}%")
                    sqlRestriction "CONCAT(c1_.first_name, ' ', c1_.last_name) like '%${text}%'"
                }
            }
            if (params.name) {
                or {
                    def name = params.name.trim().encodeAsLikeText()
                    ilike("c.firstName", "%${name}%")
                    ilike("c.lastName", "%${name}%")
                    sqlRestriction "CONCAT(c1_.first_name, ' ', c1_.last_name) like '%${name}%'"
                }
            }
            if(params.email) {
                createAlias("c.address", "a")
                ilike("a.email", "%${params.email.trim().encodeAsLikeText()}%")
            }
            if (params.createdFrom) {
                Date date = params.createdFrom.dayStart.gmt(session.timezone)
                ge("created", date)
            }
            if (params.createdTo) {
                Date date = params.createdTo.dayEnd.gmt(session.timezone)
                le("created", date)
            }
            if (params.notificationStatus) {
                eq("notificationStatus", params.notificationStatus)
            }
            if (params.notificationSentCount) {
                eq("notificationSentCount", params.notificationSentCount.toInteger())
            }
        }
        return closure
    }

    public void reScheduleJob() {
        Map scheduleConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ABANDONED_CART)
        Integer minute = AppUtil.getIntervalInMinute(scheduleConfig) ?: 1
        def startTime = new Date(System.currentTimeMillis() + (minute.toLong() * 60000L))
        Integer repeatCount = scheduleConfig['no_of_max_time']?.toInteger(0) ?: 0
        Scheduler quartzScheduler = Holders.applicationContext.getBean("quartzScheduler")
        Trigger oldTrigger = quartzScheduler.getTrigger(TriggerKey.triggerKey("abandonedCartEmailJob", "abandonedCart"))
        if(oldTrigger) {
            TriggerBuilder tb = oldTrigger.getTriggerBuilder()
            Trigger newTrigger = tb.withSchedule(
                    SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(minute)
                        .withRepeatCount(repeatCount))
                    .startAt(startTime)
                    .build()
            quartzScheduler.rescheduleJob(oldTrigger.getKey(), newTrigger)
        }
    }

    public Integer getAbandonedCartCount(Map params) {
        return AbandonedCart.createCriteria().count {
            and getCriteriaClosure(params)
        }
    }

    public List<AbandonedCart> getAbandonedCart(Map params) {
        def listMap = [max: params.max, offset: params.offset]
        return AbandonedCart.createCriteria().list(listMap) {
            and getCriteriaClosure(params)
            order(params.sort ?: "id", params.dir ?: "desc")
        }
    }

    @Transactional
    def addToAbandonedCart(session) {
        def sessionId = session.id
        Cart cart = CartManager.cartList[sessionId]
        List<CartItem> cartItem = cart?.cartItemList
        if(cartItem) {
            AbandonedCart abandonedCart = new AbandonedCart(customer: Customer.proxy(session.customer))
            cartItem.each {
                CartObject cartObject = it.object
                AbandonedCartItem item = new AbandonedCartItem(
                    itemType: cartObject.type,
                    itemId: cartObject.id,
                    quantity: it.quantity,
                    variations: it.variations
                )
                def params = cartObject.requestedParams ? cartObject.requestedParams.findAll {it.key != "action" && it.key != "controller"} : [:]
                params = HookManager.hook("abandonedCartItemParam-${cartObject.type}", params, cartObject)
                if(params) {
                    item.params = params as JSON
                }
                abandonedCart.addToCartItems(item)
            }
            abandonedCart.save()
        }
    }

    @Transactional
    Boolean removeAbandonedCart(Map params) {
        AbandonedCart cart = AbandonedCart.get(params.cartId)
        AppEventManager.fire("before-abandoned-cart-delete", [cart.id])
        cart.delete()
        AppEventManager.fire("abandoned-cart-delete", [cart.id])
        return !cart.hasErrors()
    }

    @Transactional
    Boolean abandonedAddToCart(Map params) {
        AbandonedCart abandonedCart = AbandonedCart.get(params.cartId)
        CartManager.clearCart(AppUtil.session.id)
        abandonedCart.cartItems.each {
            HookManager.hook("addToCart-${it.itemType}", it)
        }
        AbandonedCart.withNewTransaction {
            removeAbandonedCart(params)
        }
        return true
    }

    @Transactional
    Boolean changeNotification(Map params) {
        AbandonedCart cart = AbandonedCart.get(params.id)
        if(params.disable) {
            cart.notificationStatus = "disabled"
        } else {
            cart.notificationStatus = (cart.notificationSentCount < 1) ? "pending" : "sent"
        }
        cart.save()
        return !cart.hasErrors()
    }

    Boolean sendNotification(Map params) {
        AbandonedCart abandonedCart = AbandonedCart.get(params.id)
        sendMail(abandonedCart)
        abandonedCart.notificationStatus = "sent"
        abandonedCart.notificationSentCount = abandonedCart.notificationSentCount + 1
        abandonedCart.save()
        return !abandonedCart.hasErrors()
    }

    Boolean sendBatchNotification(Map params) {
        List ids = params.list("ids").collect {it.toLong(0)}
        List<AbandonedCart> carts = ids ? AbandonedCart.findAllByIdInList(ids) : []
        carts.each {
            sendBatchNotification([id: it.id])
        }
        return true
    }

    def sendMail(AbandonedCart abandonedCart, Boolean admin = false) {
        if(admin) {
            AppUtil.initialDummyRequest()
        }
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("abandoned-cart")
        if(!macrosAndTemplate.emailTemplate.active) {
            return true
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        Customer customer = abandonedCart.customer
        Map scheduleConfig = getCartConfig()
        Long minute = AppUtil.getIntervalInMinute(scheduleConfig)?.toLong() ?: 1L
        Long maxTime = scheduleConfig['no_of_max_time']?.toLong(0) ?: 0L
        def lastDate = new Date(System.currentTimeMillis() + (minute * maxTime * 60000L))
        List cartDetails = []
        abandonedCart.cartItems.each { item ->
            Map nameUrl = item.findNameAndUrl()
            if(nameUrl) {
                cartDetails.push([
                    url: nameUrl.url,
                    product_name: nameUrl.name.encodeAsBMHTML(),
                    variations: item.variations?.join(", "),
                    quantity: item.quantity
                ])
            }
        }

        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "cart_id":
                    refinedMacros[it.key] = abandonedCart.id
                    break
                case "cart_date":
                    refinedMacros[it.key] = abandonedCart.created.toEmailFormat()
                    break
                case "customer_name":
                    refinedMacros[it.key] = customer.fullName().encodeAsBMHTML()
                    break
                case "cart_details":
                    refinedMacros[it.key] = cartDetails
                    break
                case "last_checkout_date":
                    refinedMacros[it.key] = lastDate.toEmailFormat()
                    break
            }
        }
        String email = customer.address.email
        try{
            commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, email)
        } catch (Exception e) {
            e.printStackTrace()
            return false
        }

        return true
    }

    def sendScheduleEmail() {
        Map config = getCartConfig()
        Integer count =  config["no_of_max_time"]?.toInteger(0)
        AbandonedCart.withNewSession {
            List<AbandonedCart> abandonedCartList = AbandonedCart.all
            abandonedCartList.each { cart ->
                Integer notifyCount = cart.notificationSentCount
                AbandonedCart newCart = AbandonedCart.get(cart.id)
                if(notifyCount < count) {
                    if(cart.notificationStatus != "disabled" && sendMail(newCart, true)) {
                        AbandonedCart.withNewTransaction {
                            AbandonedCart aCart = AbandonedCart.get(cart.id)
                            aCart.notificationStatus = "sent"
                            aCart.notificationSentCount = notifyCount + 1
                            aCart.save()
                        }
                    }
                } else {
                    AbandonedCart.withNewTransaction {
                        removeAbandonedCart([cartId: newCart.id])
                    }
                }
            }
        }
    }

    Map getCartConfig() {
        Map scheduleConfig = [:]
        SiteConfig.withNewSession {
            List<SiteConfig> configs = SiteConfig.findAllByType(DomainConstants.SITE_CONFIG_TYPES.ABANDONED_CART)
            configs.each {
                scheduleConfig.putAll([(it.configKey): it.value])
            }
        }
        return scheduleConfig
    }

}
