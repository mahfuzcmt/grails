package com.webcommander.plugin.loyalty_point

import com.webcommander.admin.Customer
import com.webcommander.admin.CustomerGroup
import com.webcommander.admin.StoreCreditHistory
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommanderMailService
import com.webcommander.constants.DomainConstants as DC
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.manager.LicenseManager
import com.webcommander.models.Cart
import com.webcommander.models.CartItem
import com.webcommander.models.ProductInCart
import com.webcommander.plugin.loyalty_point.constants.DomainConstants
import com.webcommander.plugin.loyalty_point.constants.NamedConstants as LoyaltyPointNamedConstant
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.SortAndSearchUtil
import com.webcommander.util.security.InformationEncrypter
import com.webcommander.webcommerce.*
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@Initializable
@Transactional
class LoyaltyPointService {
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    com.webcommander.ApplicationTagLib app
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g
    CommanderMailService commanderMailService

    static void initialize() {
        AppEventManager.on("before-customer-delete", { id ->
            Customer customer = Customer.proxy(id)
            PointHistory.where {
                customer == customer
            }.deleteAll()
        })

        AppEventManager.on("before-customer-delete", { id ->
            SpecialPointRule.createCriteria().list() {
                customers {
                    eq('id', id)
                }
            }.each {
                it.customers.removeElement(Customer.load(id))
                it.merge()
            }
        })

        AppEventManager.on("before-category-delete", {id ->
            LoyaltyPoint.createCriteria().list {
                eq("category.id", id)
            }*.delete();
        })

        AppEventManager.on("before-product-delete", {id ->
            LoyaltyPoint.createCriteria().list {
                eq("product.id", id)
            }*.delete();
        })
    }

    static {
        AppEventManager.on("paid-for-cart", { Collection<Cart> carts ->
            if (LicenseManager.isProvisionActive() && !LicenseManager.license("allow_loyalty_program_feature")) {
                return
            }
            LoyaltyPointService loyaltyPointService = Holders.applicationContext.getBean("loyaltyPointService")
            def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT)
            if (configs.is_enabled == "true" && configs.earning_enabled == "true") {
                carts.each { Cart cart ->
                    Long totalLoyaltyPoint = 0;
                    cart.cartItemList.each { CartItem cartItem ->
                        if (cartItem.object instanceof ProductInCart) {
                            Product product = Product.proxy(cartItem.object.product.id);
                            totalLoyaltyPoint += (loyaltyPointService.findPointByVariation(product, cartItem) ?: loyaltyPointService.findLoyaltyPoint(product)) * cartItem.quantity;
                        } else if (configs.point_policy == DomainConstants.POINT_POLICY.SPECIFIED_CONVERSION_RATE) {
                            totalLoyaltyPoint += cartItem.baseTotal * cartItem.quantity;
                        }
                    }
                    OrderReferral orderReferral = OrderReferral.findByOrder(Order.proxy(cart.orderId));
                    if (configs.enable_referral == "true" && configs.enable_refer_product == "true" && orderReferral) {
                        Customer referrerCustomer = Customer.findByReferralCode(orderReferral.referralCode)
                        if (referrerCustomer) {
                            int used = referrerCustomer.countReferralCodeUsed
                            referrerCustomer.countReferralCodeUsed = used + 1;
                        }
                        Long referrerlLoyaltyPoint = configs.refer_product_on_purchase_referrer_loyalty_point.toLong()
                        PointHistory pointHistory = new PointHistory([customer: referrerCustomer, pointCredited: referrerlLoyaltyPoint, type: com.webcommander.plugin.loyalty_point.constants.NamedConstants.POINT_HISTORY_TYPE.ON_PURCHASE_REFERRAL]).save()
                        totalLoyaltyPoint += configs.refer_product_on_purchase_referree_loyalty_point.toLong()
                        orderReferral.delete()
                    }
                    loyaltyPointService.saveOrderHistory(Order.proxy(cart.orderId), totalLoyaltyPoint)
                }
            }
        });

        AppEventManager.on('paid-for-order', { Order order ->
            if (LicenseManager.isProvisionActive() && !LicenseManager.license("allow_loyalty_program_feature")) {
                return
            }
            LoyaltyPointService loyaltyPointService = Holders.applicationContext.getBean("loyaltyPointService")
            def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT)
            if (configs.is_enabled == "true" && configs.earning_enabled == "true") {
                Long totalLoyaltyPoint = 0;
                order.items.each { OrderItem orderItem ->
                    if (orderItem.productType == NamedConstants.CART_OBJECT_TYPES.PRODUCT) {
                        Product product = Product.get(orderItem.productId);
                        if (product) {
                            totalLoyaltyPoint += (loyaltyPointService.findPointByVariation(product, orderItem) ?: loyaltyPointService.findLoyaltyPoint(product)) * orderItem.quantity;
                        }
                    } else if (configs.point_policy == DomainConstants.POINT_POLICY.SPECIFIED_CONVERSION_RATE) {
                        totalLoyaltyPoint += orderItem.quantity * orderItem.price;
                    }
                    OrderReferral orderReferral = OrderReferral.findByOrder(order);
                    if (configs.enable_referral == "true" && configs.enable_refer_product == "true" && orderReferral) {
                        Customer referrerCustomer = Customer.findByReferralCode(orderReferral.referralCode)
                        if (referrerCustomer) {
                            int used = referrerCustomer.countReferralCodeUsed
                            referrerCustomer.countReferralCodeUsed = used + 1;
                        }
                        Long referrerlLoyaltyPoint = configs.refer_product_on_purchase_referrer_loyalty_point.toLong()
                        PointHistory pointHistory = new PointHistory([customer: referrerCustomer, pointCredited: referrerlLoyaltyPoint, type: com.webcommander.plugin.loyalty_point.constants.NamedConstants.POINT_HISTORY_TYPE.ON_PURCHASE_REFERRAL]).save()
                        totalLoyaltyPoint += configs.refer_product_on_purchase_referree_loyalty_point.toLong()
                        orderReferral.delete()
                    }
                }
                loyaltyPointService.saveOrderHistory(order, totalLoyaltyPoint)
            }
        });

        AppEventManager.on('after-review-active', { Customer customer, Long reviewId ->
            if (LicenseManager.isProvisionActive() && !LicenseManager.license("allow_loyalty_program_feature")) {
                return
            }
            LoyaltyPointService loyaltyPointService = Holders.applicationContext.getBean("loyaltyPointService")
            def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT)
            if (configs.is_enabled == "true" && configs.earning_enabled == "true") {
                Long totalLoyaltyPoint = 0;
                if (configs.enable_product_review_point == "true") {
                    totalLoyaltyPoint += (configs.on_product_review_amount).toLong()
                }
                loyaltyPointService.saveReviewHistory(customer, totalLoyaltyPoint, reviewId)
            }
        })

        AppEventManager.on('customer-create', { Customer customer ->
            if (customer.status != 'A' || (LicenseManager.isProvisionActive() && !LicenseManager.license("allow_loyalty_program_feature"))) {
                return
            }
            LoyaltyPointService loyaltyPointService = Holders.applicationContext.getBean("loyaltyPointService")
            def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT)
            if (configs.is_enabled == "true" && configs.earning_enabled == "true") {
                Long totalLoyaltyPoint = 0;
                if (configs.enable_signup_registration_point == "true") {
                    totalLoyaltyPoint += (configs.on_signup_registration_amount).toLong()
                }
                loyaltyPointService.saveRegistrationHistory(customer, totalLoyaltyPoint)
            }
        })

        AppEventManager.on('order-confirm', { Cart cart ->
            if (LicenseManager.isProvisionActive() && !LicenseManager.license("allow_loyalty_program_feature")) {
                return
            }
            def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT)
            if (configs.is_enabled == "true" && configs.earning_enabled == "true") {
                if (AppUtil.session.shareid) {
                    String encryptedShareId = AppUtil.session.shareid
                    InformationEncrypter encrypter = new InformationEncrypter(encryptedShareId, false)
                    def decryptedData = encrypter.getHiddenInfos()
                    Long productId = decryptedData[0].toLong() ?: null
                    Long customerId = decryptedData[1].toLong() ?: null
                    String shareMedium = decryptedData[2] ?: null
                    LoyaltyPointOnShareHistory shareHistory = LoyaltyPointOnShareHistory.findByOrderId(cart.orderId)
                    if (productId && customerId && shareMedium && !shareHistory) {
                        LoyaltyPointOnShareHistory history = new LoyaltyPointOnShareHistory()
                        history.sharingCustomerId = customerId
                        history.productId = productId
                        history.shareMedium = shareMedium
                        history.shareId = encryptedShareId
                        history.orderId = cart.orderId
                        history.save()
                    }
                }
            }
        })

        AppEventManager.on('after-payment-done', { Order order   ->
            if (LicenseManager.isProvisionActive() && !LicenseManager.license("allow_loyalty_program_feature")) {
                return
            }
            LoyaltyPointService loyaltyPointService = Holders.applicationContext.getBean("loyaltyPointService")
            def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT)
            if (configs.is_enabled == "true" && configs.earning_enabled == "true") {
                if (configs.enable_purchase_point == "true") {
                    def customerCompletedOrder = Order.createCriteria().list() {
                        and {
                            eq('customerId', order.customerId)
                            eq('paymentStatus', "paid")
                        }
                    }
                    Long purchaseCount = customerCompletedOrder.size()
                    Long loyaltyPointEarnLimit = configs.on_first_purchase_amount ? configs.on_first_purchase_amount.toLong() : 0L
                    if (configs.purchase_valid_for == "every" || (configs.purchase_valid_for == "first" && purchaseCount <= loyaltyPointEarnLimit)) {
                        Customer customer = Customer.findById(order.customerId)
                        Long totalLoyaltyPoint = 0;
                        totalLoyaltyPoint += (configs.on_purchase_amount).toLong()
                        loyaltyPointService.savePurchaseHistory(customer, totalLoyaltyPoint, order.id)
                    }
                }

                LoyaltyPointOnShareHistory shareHistory = LoyaltyPointOnShareHistory.findByOrderId(order.id)
                if (shareHistory && !shareHistory?.isUsed) {
                    Long totalLoyaltyPoint = 0;
                    order.items.each {
                        if (it.productId == shareHistory.productId) {
                            switch (shareHistory.shareMedium) {
                                case "fb":
                                    totalLoyaltyPoint += (configs.on_facebook_share_amount).toLong()
                                    break;
                                case "tw":
                                    totalLoyaltyPoint += (configs.on_twitter_share_amount).toLong()
                                    break;
                                case "gp":
                                    totalLoyaltyPoint += (configs.on_googleplus_share_amount).toLong()
                                    break;
                                case "ln":
                                    totalLoyaltyPoint += (configs.on_linkedin_share_amount).toLong()
                                    break;
                            }
                        }
                    }
                    loyaltyPointService.saveShareHistory(totalLoyaltyPoint, order, shareHistory)
                }
            }
        })
    }

    def saveLoyaltyPoint(Map response, Map parameters) {
        Map params = parameters.loyaltyPoint
        if (AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT, "is_enabled") != "true") {
            return response;
        }
        if (AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT, "point_policy") == "specified.conversion.rate") {
            return response;
        }
        LoyaltyPoint loyaltyPoint;
        switch (params.target) {
            case "product":
                Product p = Product.proxy(parameters.id);
                loyaltyPoint = LoyaltyPoint.findByProduct(p) ?: new LoyaltyPoint(product: p);
                break
            case "variation":
                Long id = params.id
                loyaltyPoint = LoyaltyPoint.findByVariationDetailsId(id) ?: new LoyaltyPoint(variationDetailsId: id);
                break;
            case "category":
                Category c = Category.proxy(parameters.id)
                loyaltyPoint = LoyaltyPoint.findByCategory(c) ?: new LoyaltyPoint(category: c);
                break
        }
        loyaltyPoint.point = params.point.toLong(0);
        loyaltyPoint.save();
        response.success = !loyaltyPoint.hasErrors();
        return response
    }

    def customerAndGroupListForSpecialRule(Map response, Map parameters) {
        Map params = parameters
        Long ruleId = params.rule_id ? params.rule_id.toLong() : null
        if(ruleId) {
            SpecialPointRule rule = SpecialPointRule.load(ruleId)
            response.customer = rule?.customerIds ?: []
            response.customerGroup = rule?.customerGroupIds ?: []
            response.success = true
            return
        }
        response.success = false
    }

    private LoyaltyPoint findLoyaltyPointRecursive(List<Category> categories, boolean findHighest, LoyaltyPoint currentCandidate = null) {
        LoyaltyPoint lp
        if (categories.size() != 0) {
            lp = LoyaltyPoint.createCriteria().get {
                inList("category", categories)
                if (currentCandidate) {
                    if (findHighest) {
                        gt("point", currentCandidate.point)
                    } else {
                        lt("point", currentCandidate.point)
                    }
                }
                maxResults(1)
                order("point", findHighest ? "desc" : "asc")
            };
        }
        lp = lp?.point ? lp : currentCandidate;
        List<Category> nxtLevelCategories = [];
        categories.each {
            if (it.parent) {
                nxtLevelCategories.add(it.parent);
            }
        }
        if (nxtLevelCategories.size() == 0) {
            return lp;
        } else {
            return findLoyaltyPointRecursive(nxtLevelCategories, findHighest, lp);
        }
    }

    Long findLoyaltyPoint(Product product, LoyaltyPoint loyaltyPoint = null) {
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT);
        loyaltyPoint = loyaltyPoint ?: LoyaltyPoint.findByProduct(product)

        switch (configs.point_policy) {
            case DomainConstants.POINT_POLICY.ASSIGNED_TO_PRODUCTS:
                break
            case DomainConstants.POINT_POLICY.FROM_PRIMARY_CATEGORY:
                if (!(loyaltyPoint?.point > 0) && product.parent) {
                    loyaltyPoint = LoyaltyPoint.where { category == product.parent }.get()
                }
                break;
            case DomainConstants.POINT_POLICY.HIGHEST_FROM_CATEGORIES:
            case DomainConstants.POINT_POLICY.LOWEST_FROM_CATEGORIES:
                if (!(loyaltyPoint?.point > 0)) loyaltyPoint = findLoyaltyPointRecursive(product.parents, configs.point_policy == DomainConstants.POINT_POLICY.HIGHEST_FROM_CATEGORIES, null)
                break
            case DomainConstants.POINT_POLICY.HIGHEST_IN_PRODUCT_AND_CATEGORIES:
            case DomainConstants.POINT_POLICY.LOWEST_IN_PRODUCT_AND_CATEGORIES:
                loyaltyPoint = findLoyaltyPointRecursive(product.parents, configs.point_policy == DomainConstants.POINT_POLICY.HIGHEST_IN_PRODUCT_AND_CATEGORIES, loyaltyPoint)
                break
            case DomainConstants.POINT_POLICY.SPECIFIED_CONVERSION_RATE:
                return (Long) (configs.conversion_rate_earning.toDouble(0) * (product.isOnSale ? product.salePrice : product.basePrice))
                break
        }

        return loyaltyPoint?.point ?: 0;
    }

    Long findLoyaltyPointFromOrder(Long orderId) {
        Long point = 0;
        Order order = Order.proxy(orderId);
        def orderItem = order.items;
        orderItem.each {
            if (it.productType == NamedConstants.CART_OBJECT_TYPES.PRODUCT) {
                def product = Product.proxy(it.productId)
                point += findPointByVariation(product, it) ?: findLoyaltyPoint(product)
            }
        }
        return point;
    }

    boolean isCustomerExistInSpecialPointRule(SpecialPointRule specialPointRule, Customer customer) {
        if (SortAndSearchUtil.binarySearch(specialPointRule.customerIds, customer.id) != -1) return true
        else {
            customer.groups.find {
                if (SortAndSearchUtil.binarySearch(specialPointRule.customerGroupIds, it.id) != -1) return true
            }
        }
        return false
    }

    Long calculateLoyaltyPointForSpecialRule(Customer customer, Long point) {
        Long maxPoint = 0
        getSpecialPointRule([]).each {
            Long currentPoint = 0
            if (isCustomerExistInSpecialPointRule(it, customer)) {
                if (LoyaltyPointNamedConstant.RULE_TYPE[it.ruleType] == "+") {
                    currentPoint = point + it.point
                } else if (LoyaltyPointNamedConstant.RULE_TYPE[it.ruleType] == "x") {
                    currentPoint = point * it.point
                }
            }
            maxPoint = Math.max(maxPoint, currentPoint)
        }
        return Math.max(maxPoint, point)
    }

    boolean saveOrderHistory(Order order, Long points) {
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT)
        PointHistory history = PointHistory.findByOrder(order)
        if (history != null) {
            return false
        }
        Customer customer = order.customerId ? Customer.get(order.customerId) : null
        points = calculateLoyaltyPointForSpecialRule(customer, points)
        if (customer && configs.is_enabled && configs.earning_enabled) {
            PointHistory orderHistory = new PointHistory([
                    customer     : customer,
                    order        : order,
                    pointCredited: points,
                    comment      : "${order.id}",
                    type         : com.webcommander.plugin.loyalty_point.constants.NamedConstants.POINT_HISTORY_TYPE.ORDER
            ])
            orderHistory.save()
            return !orderHistory.hasErrors()
        }
        return true;
    }

    boolean saveReviewHistory(Customer customer, Long points, Long reviewId) {
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT)
        def type = com.webcommander.plugin.loyalty_point.constants.NamedConstants.POINT_HISTORY_TYPE.PRODUCT_REVIEW
        PointHistory history = PointHistory.findByCommentAndType("${reviewId}", type)
        if (history != null) {
            return false
        }
        points = calculateLoyaltyPointForSpecialRule(customer, points)
        if (customer && configs.is_enabled && configs.earning_enabled) {
            PointHistory reviewHistory = new PointHistory([
                    customer     : customer,
                    pointCredited: points,
                    comment      : "${reviewId}",
                    type         : type
            ])
            reviewHistory.save()
            return !reviewHistory.hasErrors()
        }
        return true;
    }

    boolean saveRegistrationHistory(Customer customer, Long points) {
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT)
        def type = com.webcommander.plugin.loyalty_point.constants.NamedConstants.POINT_HISTORY_TYPE.REGISTRATION
        PointHistory history = PointHistory.findByCommentAndType("${customer.id}", type)
        if (history != null) {
            return false
        }
        points = calculateLoyaltyPointForSpecialRule(customer, points)
        if (customer && configs.is_enabled && configs.earning_enabled) {
            PointHistory reviewHistory = new PointHistory([
                    customer     : customer,
                    pointCredited: points,
                    comment      : "${customer.id}",
                    type         : type
            ])
            reviewHistory.save()
            return !reviewHistory.hasErrors()
        }
        return true;
    }

    boolean savePurchaseHistory(Customer customer, Long points, Long orderId) {
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT)
        def type = com.webcommander.plugin.loyalty_point.constants.NamedConstants.POINT_HISTORY_TYPE.PURCHASE
        PointHistory history = PointHistory.findByCommentAndType("${orderId}", type)
        if (history != null) {
            return false
        }
        points = calculateLoyaltyPointForSpecialRule(customer, points)
        if (customer && configs.is_enabled && configs.earning_enabled) {
            PointHistory reviewHistory = new PointHistory([
                    customer     : customer,
                    pointCredited: points,
                    comment      : "${orderId}",
                    type         : type
            ])
            reviewHistory.save()
            return !reviewHistory.hasErrors()
        }
        return true;
    }

    boolean saveShareHistory(Long points, Order order, LoyaltyPointOnShareHistory history) {
        Customer redirectedCustomer = Customer.findById(order.customerId)
        Customer sharingCustomer = Customer.findById(history.sharingCustomerId)
        OrderItem orderItem = order.items.find() {
            it.productId == history.productId
        }
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT)
        def type = ""
        switch (history.shareMedium) {
            case "fb":
                type = com.webcommander.plugin.loyalty_point.constants.NamedConstants.POINT_HISTORY_TYPE.FB_SHARE
                break;
            case "tw":
                type = com.webcommander.plugin.loyalty_point.constants.NamedConstants.POINT_HISTORY_TYPE.TW_SHARE
                break;
            case "gp":
                type = com.webcommander.plugin.loyalty_point.constants.NamedConstants.POINT_HISTORY_TYPE.GP_SHARE
                break;
            case "ln":
                type = com.webcommander.plugin.loyalty_point.constants.NamedConstants.POINT_HISTORY_TYPE.LN_SHARE
                break;
        }
        if (redirectedCustomer && sharingCustomer && configs.is_enabled && configs.earning_enabled) {
            boolean error = false
            PointHistory reviewHistory = new PointHistory([
                    customer     : redirectedCustomer,
                    pointCredited: calculateLoyaltyPointForSpecialRule(redirectedCustomer, points * orderItem.quantity),
                    comment      : "${order.id}",
                    type         : type
            ])
            reviewHistory.save()
            error = !reviewHistory.hasErrors()
            reviewHistory = new PointHistory([
                    customer     : sharingCustomer,
                    pointCredited: calculateLoyaltyPointForSpecialRule(sharingCustomer, points),
                    comment      : "${order.id}",
                    type         : type
            ])
            reviewHistory.save()
            error = error && !reviewHistory.hasErrors()
            history.isUsed = true
            history.merge()
            return error && !history.hasErrors()
        }
        return true;
    }

    Long findPointFromHistory(Long orderId) {
        PointHistory pointHistory = PointHistory.findByOrder(Order.proxy(orderId));
        return pointHistory ? pointHistory.pointCredited.toLong() : 0;
    }

    Map getConversionOptions() {
        Map conversionOptions = [:];
        String config = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT, "enable_store_credit");
        if (config == "true") {
            conversionOptions["STORE_CREDIT"] = [
                    message_key: "store.credit",
                    handler    : "loyaltyPointService"
            ]
        }
        HookManager.hook("loyalty-point-convert-options", conversionOptions);
        return conversionOptions;
    }

    Map convertLoyaltyPoint(Long convertAmount) {
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT);
        Customer customer = Customer.get(AppUtil.session.customer);
        Double deltaCredit = (Double.parseDouble(configs.conversion_rate_store_credit) / 100) * convertAmount;
        customer.storeCredit += deltaCredit;
        StoreCreditHistory storeCreditHistory = new StoreCreditHistory(deltaAmount: deltaCredit, note: "Loyalty Point converted to Store Credit", customer: customer)
        storeCreditHistory.save()
        customer.merge();
        if (customer.hasErrors()) {
            return [
                    success    : false,
                    message_key: "loyalty.point.convert.store.credit.failure"
            ]
        } else {
            return [
                    success    : true,
                    message_key: "loyalty.point.convert.store.credit.success"
            ]
        }
    }

    private Date getLastValidEarningDate() {
        def configs = AppUtil.getConfig(DC.SITE_CONFIG_TYPES.LOYALTY_POINT)
        return new Date() - Integer.parseInt(configs.expire_in_value)[configs.expire_in_offset]
    }

    Long getCustomerLoyaltyPoint(Customer customer) {
        def configs = AppUtil.getConfig(DC.SITE_CONFIG_TYPES.LOYALTY_POINT)
        Date lastValidEarningDate = getLastValidEarningDate();
        Long loyaltyPoints = 0;
        PointHistory.createCriteria().list {
            eq("customer", customer)
            if (configs.enable_expire == DomainConstants.ENABLE_EXPIRE.AFTER_PERIOD) {
                ge("created", lastValidEarningDate.gmt())
            }
        }.each {
            loyaltyPoints += (it.pointCredited - it.pointDebited);
        }
        return loyaltyPoints;
    }

    Double getCustomerLoyaltyPointToCurrency(Customer customer) {
        Long availableLoyaltyPoint = getCustomerLoyaltyPoint(customer);
        Double convertRate = Double.parseDouble(PaymentGatewayMeta.findByFieldFor(DC.PAYMENT_GATEWAY_CODE.LOYALTY_POINT).value);
        return convertRate * availableLoyaltyPoint / 100;
    }

    void cutLoyaltyPointsFormCustomerAccount(Customer customer, Long amount) {
        Date lastValidDate = getLastValidEarningDate();
        def configs = AppUtil.getConfig(DC.SITE_CONFIG_TYPES.LOYALTY_POINT)
        List<PointHistory> pointList = PointHistory.createCriteria().list {
            eq("customer", customer)
            if (configs.enable_expire == DomainConstants.ENABLE_EXPIRE.AFTER_PERIOD) {
                ge("created", lastValidDate.gmt())
            }
            order("created", "asc")
        }
        Long target = amount;
        for (PointHistory pointHistory : pointList) {
            Long cutableAmount = (pointHistory.pointCredited - pointHistory.pointDebited)
            if (cutableAmount < amount) {
                pointHistory.pointDebited += cutableAmount
                amount -= cutableAmount
            } else {
                pointHistory.pointDebited += amount
                break
            }
            pointHistory.merge()
        }
    }

    @Transactional
    Map convert(GrailsParameterMap params) {
        Map convertOptions = getConversionOptions();
        Customer customer = Customer.get(AppUtil.session.customer);
        Long convertAmount = params.long("convertAmount");
        Long availableLoyaltyPoint = getCustomerLoyaltyPoint(customer);
        if (convertAmount > availableLoyaltyPoint) {
            throw new ApplicationRuntimeException("insufficient.loyalty.points")
        }
        String beanName = convertOptions[params.convertTo].handler;
        def bean = Holders.applicationContext.getBean(beanName);
        Map status = bean.convertLoyaltyPoint(convertAmount)
        if (status.success) {
            cutLoyaltyPointsFormCustomerAccount(customer, convertAmount);
            Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("loyalty-point-reward-notification");
            Map refinedMacros = macrosAndTemplate.commonMacros;
            macrosAndTemplate.macros.each {
                switch (it.key.toString()) {
                    case "customer_name":
                        refinedMacros[it.key] = customer.firstName + (customer.lastName ? " ${customer.lastName}" : "");
                        break
                    case "loyalty_point_amount":
                        refinedMacros[it.key] = convertAmount
                        break
                    case "reward_name":
                        refinedMacros[it.key] = g.message(code: convertOptions[params.convertTo].message_key)
                        break
                }
            }
            try {
                commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, customer.address.email);
            } catch (Exception err) {

            }
            return [status: "success", message: g.message(code: status.message_key)]

        }
        return [status: "error", message: g.message(code: status.message_key)];
    }

    Long findPointByVariation(Product product, CartItem cartItem) {
        Long detailsId = HookManager.hook("resolveVariationForCartItem", 0l, cartItem)
        if (detailsId) {
            return findLoyaltyPoint(product, LoyaltyPoint.findByVariationDetailsId(detailsId))
        }
        return 0
    }

    Long findPointByVariation(Product product, OrderItem orderItem) {
        Long detailsId = HookManager.hook("resolveVariationForOrderItem", 0l, orderItem)
        if (detailsId) {
            return findLoyaltyPoint(product, LoyaltyPoint.findByVariationDetailsId(detailsId))
        }
        return 0
    }

    def sendMailToFriend(Map params, Customer customer) {
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("invite-friend")
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "invitation_message":
                    refinedMacros[it.key] = params.message;
                    break;
                case "recommended_url":
                    refinedMacros[it.key] = app.baseUrl() + "customer/register?referralCode=" + customer.referralCode;
                    break;
                case "from_email":
                    refinedMacros[it.key] = customer.userName
                    break;
            }
        }
        commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, params.receiver, customer.userName)
    }

    def applyReferralCode(String referralCode) {
        def request = AppUtil.request
        if (Customer.findByReferralCode(referralCode)) {
            AppUtil.session.referralCode = referralCode
            request.referralStatusMsgType = "success"
            request.referralStatusMsg = g.message(code: "referral.code.apply.success")
        } else {
            request.referralStatusMsgType = "error"
            request.referralStatusMsg = g.message(code: "invalid.referral.code")
        }
    }

    def saveOrderReferral(orderId, referralCode) {
        Order order = Order.proxy(orderId);
        OrderReferral orderReferral = OrderReferral.findByOrder(order);
        if (orderReferral) {
            return false;
        }
        orderReferral = new OrderReferral([order: order, referralCode: referralCode]).save()
        return orderReferral
    }

    def validateReferralCode(params) {
        Customer customer = Customer.findByReferralCode(params.referralCode)
        if (!customer) {
            return [status: "error", message: g.message(code: "invalid.referral.code")];
        }
        return true;
    }

    @Transactional
    SpecialPointRule saveSpecialPointRule(Map data) {
        SpecialPointRule specialPointRule = new SpecialPointRule()
        specialPointRule.name = data.rule_name
        specialPointRule.point = data.rule_point as Integer
        specialPointRule.ruleType = data.rule_type
        specialPointRule.customers = data.customer ? data.list("customer").collect { Customer.load(it) } : []
        specialPointRule.customerGroups = data.customerGroup ? data.list("customerGroup").collect {
            CustomerGroup.load(it)
        } : []
        specialPointRule.save()
        if (specialPointRule.hasErrors()) return null
        return specialPointRule
    }

    @Transactional
    boolean removeSpecialPointRule(Long id) {
        try {
            SpecialPointRule.findById(id).delete()
            return true
        } catch (Exception ex) {
            return false
        }
    }

    Collection<SpecialPointRule> getSpecialPointRule(List<Long> ids = []) {
        if (ids) {
            return SpecialPointRule.getAll(ids)
        } else {
            return SpecialPointRule.list()
        }
    }

    Integer getPointHistoryCount(Map params) {
        return PointHistory.createCriteria().count {
            and getCriteriaClosure(params)
        }
    }

    List<PointHistory> getPointHistory(Map params) {
        def listMap = [max: params.max, offset: params.offset]
        return PointHistory.createCriteria().list(listMap) {
            and getCriteriaClosure(params)
            if (params.sort == "fullName") {
                and {
                    order("c.firstName", params.dir ?: "asc")
                    order("c.lastName", params.dir ?: "asc")
                }
            } else order(params.sort ?: "id", params.dir ?: "asc")
        }
    }

    private Closure getCriteriaClosure(Map params) {
        def session = AppUtil.session
        Closure closure = {
            createAlias("customer", "c")
            if (params.name) {
                or {
                    def name = params.name.trim().encodeAsLikeText()
                    ilike("c.firstName", "%${name}%")
                    ilike("c.lastName", "%${name}%")
                    sqlRestriction "CONCAT(c1_.first_name, ' ', c1_.last_name) like '%${name}%'"
                }
            }
            if (params.source) {
                def type = params.source.trim().encodeAsLikeText()
                ilike("type", type)
            }

            if (params.min_point && params.max_point) {
                def min_point = params.min_point.trim() as Long
                def max_point = params.max_point.trim() as Long
                between("pointCredited", min_point, max_point)
            } else if (params.min_point) {
                def min_point = params.min_point.trim() as Long
                ge("pointCredited", min_point)
            } else if (params.max_point) {
                def max_point = params.max_point.trim() as Long
                le("pointCredited", max_point)
            }

            if (params.earnedFrom && params.earnedTo) {
                and {
                    Date startDate = params.earnedFrom.dayStart.gmt(session.timezone);
                    Date endDate = params.earnedTo.dayEnd.gmt(session.timezone);
                    ge("created", startDate)
                    le("created", endDate)
                }
            } else if (params.earnedFrom) {
                Date date = params.earnedFrom.dayEnd.gmt(session.timezone);
                ge("created", date)
            } else if (params.earnedTo) {
                Date date = params.earnedTo.dayEnd.gmt(session.timezone);
                le("created", date)
            }
        }
        return closure
    }
}
