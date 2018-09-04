package com.webcommander.manager

import com.webcommander.beans.SiteMessageSource
import com.webcommander.calculator.ShippingCalculator
import com.webcommander.calculator.TaxCalculator
import com.webcommander.calculator.model.DiscountCalculator
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.models.*
import com.webcommander.models.blueprints.CartItemable
import com.webcommander.tenant.Thread
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.throwables.CartManagerException
import com.webcommander.throwables.CartManagerExceptionWrap
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.*
import grails.converters.JSON
import grails.util.Holders
import grails.util.TypeConvertingMap

import javax.servlet.http.HttpSession
import java.util.concurrent.ConcurrentHashMap

class CartManager {

    static ConcurrentHashMap<String, Cart> getCartList() {
        ConcurrentHashMap<String, Cart> cartList = CacheManager.get(NamedConstants.CACHE.TENANT_STATIC, "cart_list")
        if(cartList == null) {
            CacheManager.cache(NamedConstants.CACHE.TENANT_STATIC, cartList = new ConcurrentHashMap<String, Cart>(), (Long)null, "cart_list")
        }
        return cartList
    }

    static List<DiscountCalculator> discountCalculators = new ArrayList<com.webcommander.calculator.model.DiscountCalculator>();

    static {
        AppEventManager.on("session-terminate") { session ->
            println("------------------ Debug CART CLEAR  Log with session time out ------------------")
            clearCart(session.id)
        }

        AppEventManager.on("effective-billing-change") { sessionId ->
            if (AppUtil.session?.id == sessionId) {
                cartList[AppUtil.session.id]?.isDirty = true;
            }
        }

        AppEventManager.on("effective-shipping-change") { sessionId ->
            if (AppUtil.session?.id == sessionId) {
                cartList[AppUtil.session.id]?.isShipDirty = true;
            }
        }

        AppEventManager.on("customer-login customer-logout") { customerId ->
            if (AppUtil.session?.customer == customerId) {
                cartList[AppUtil.session.id]?.isDirty = true;
            }
        }

        AppEventManager.on(DomainConstants.SITE_CONFIG_TYPES.CART_PAGE + "-configuration-update") { customerId ->
            cartList.values().each {
                updateCartDisplay(it)
            }
        }

        AppEventManager.on(DomainConstants.SITE_CONFIG_TYPES.SHIPPING + "-configuration-update shipping-policy-update shipping-profile-update " +
                "shipping-rule-update $DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE-configuration-update") {
            cartList.values().each {
                it.isShipDirty = true
            }
        }

        AppEventManager.on("$DomainConstants.SITE_CONFIG_TYPES.DISCOUNT-configuration-update $DomainConstants.SITE_CONFIG_TYPES.TAX-configuration-update product-update tax-profile-update tax-code-update tax-rule-update zone-update store-detail-update " +
                "discount-update $DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE-configuration-update") {
            cartList.values().each {
                it.isDirty = true
            }
        }

        // TODO: Need Improvement
        HookManager.register("addToCart-product", { CartItemable cartItem ->
            ProductService productService = ProductService.getInstance()
            Product product = Product.get(cartItem.itemId)
            TypeConvertingMap paramsObj = cartItem.paramsObj()
            ProductData pData = productService.getProductData(product, paramsObj?.config)
            if(pData == null || !pData.isAvailable) {
                throw new ApplicationRuntimeException("product.not.available")
            }
            CartManager.addToCart(AppUtil.session.id, pData, cartItem.quantity, paramsObj)
        })

        HookManager.register("resolveCartObject-product", { object, CartItemable cartItem ->
            ProductService productService = Holders.applicationContext.getBean("productService")
            Product product = Product.get(cartItem.itemId)
            if(!product) {
                return
            }
            ProductData pData = productService.getProductData(product, cartItem.paramsObj()?.config)
            return CartManager.getProductCart(pData, cartItem.quantity, cartItem.paramsObj(), null, false)
        })
    }

    private static SiteMessageSource _siteMessageSource;
    private static SiteMessageSource getSiteMessageSource() {
        return _siteMessageSource ?: (_siteMessageSource = Holders.applicationContext.getBean("siteMessageSource"))
    }

    private static int _itemIdCounter = 1;

    static int getNextItemId() {
        return _itemIdCounter++
    }

    static Cart createCartForAdminOrder(List items, Map address) {
        Cart cart = new Cart();
        CartItem cartItem;
        List<CartManagerException> exceptions = []
        items.each {
            try {
                Long productId = it.id.toLong();
                Double price = it.price.toDouble();
                Integer quantity = it.quantity.toInteger()
                TypeConvertingMap params = it;
                List<CartItem> variants = cart.cartItemList.findAll {
                    it.object.id == productId
                }
                cartItem = populateCartItemForProduct(productId, price, quantity, params, variants)
                cart.cartItemList.add(cartItem)
            } catch (CartManagerException ex) {
                exceptions.add(ex)
            }
        }
        if(exceptions.size() > 0) {
            throw new CartManagerExceptionWrap(exceptions)
        }
        updateCartTotal(cart, address.shippingAddress, false)
        return cart
    }

    static Cart createCartByCartItems(List<CartItem> cartItems) {
        Cart cart = new Cart()
        cart.cartItemList = cartItems
        updateCartTotal(cart)
        return cart
    }

    static Integer getAddedQuantity(String sessionId, ProductData productData, TypeConvertingMap params) {
        Cart cart = getCart(sessionId);
        List variations = [];
        if (productData.isCombined && !productData.isCombinationPriceFixed && productData.isCombinationQuantityFlexible) {
            Map included = JSON.parse(params.included)
            included.each { id, count ->
                variations.add(CombinedProduct.get(id).includedProduct.name + " (" + count + ")")
            }
        }
        HookManager.hook("variationsForCartAdd", variations, productData, params)
        if (cart) {
            List<CartItem> items = cart.cartItemList.findAll {
                it.object.id == productData.id;
            }
            return items ? items.sum {
                it.quantity
            } : 0
        } else {
            return 0
        }
    }

    static List getProductVariationsForCartAdd(ProductData productData, TypeConvertingMap params) {
        List variations = [];
        if (productData.isCombined && !productData.isCombinationPriceFixed && productData.isCombinationQuantityFlexible) {
            Map included = JSON.parse(params.included)
            included.each { id, count ->
                variations.add(CombinedProduct.get(id).includedProduct.name + " (" + count + ")")
            }
        }
        variations = HookManager.hook("variationsForCartAdd", variations, productData, params)
        return variations
    }

    static CartItem addToCart(String sessionId, ProductData productData, Integer quantity, TypeConvertingMap params) {
        CartItem cartItem;
        Cart cart = getCart(sessionId);
        List variations = getProductVariationsForCartAdd(productData, params)
        Boolean isMultiStoreEnabled = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "enable_multi_model") == "true"
        if (cart) {
            List<CartItem> items = cart.cartItemList.findAll {
                it.object.type == NamedConstants.CART_OBJECT_TYPES.PRODUCT && it.object.id == productData.id && it.object.iEquals(productData)
            }
            cartItem = items ? items.find {
                isVariationMatch(it.variations, variations)
            } : null

            if(params.storeId && isMultiStoreEnabled && cartItem) {
                cartItem = (cartItem.storeId == params.storeId.toLong())? cartItem : null
            }

            if (cartItem) {
                items.remove(cartItem);
                cartItem.updateQuantity(cartItem.quantity + quantity, items)
                AppEventManager.fire("cart-item-quantity-update", [cart])
            } else {
                cartItem = populateCartItemForProduct(productData, quantity, params, items);
                cart.cartItemList.add(cartItem);
                AppEventManager.fire("cart-item-add", [cart])
                if (variations) {
                    cartItem.variations = variations
                }
                if(params.storeId && isMultiStoreEnabled) {
                    cartItem.storeId = params.storeId.toLong()
                }
            }
        } else {
            cart = new Cart(sessionId: sessionId)
            cartItem = populateCartItemForProduct(productData, quantity, params)
            cart.cartItemList.add(cartItem)
            if (variations) {
                cartItem.variations = variations
            }
            if(params.storeId && isMultiStoreEnabled) {
                cartItem.storeId = params.storeId.toLong()
            }
            cartList[sessionId] = cart
        }
        updateCartTotal(cart)
        return cartItem
    }

    static List<Map> addToCart(Collection<CartItemable> cartItemables) {
        CartManager.clearCart(AppUtil.session.id)
        List<Map> exceptions = []
        cartItemables.each {
            try {
                HookManager.hook("addToCart-${it.itemType}", it)
            } catch (CartManagerException ex) {
                Map replacerMap = ex.messageArgs ? [
                        requested_quantity: ex.messageArgs[0],
                        multiple_of_quantity: ex.messageArgs[0],
                        maximum_quantity: ex.messageArgs[0],
                        minimum_quantity: ex.messageArgs[0]
                ] : null
                String message = siteMessageSource.convert(ex.message, null, replacerMap).toString()
                exceptions.add([itemName: it.itemName, errorMessage: message])
            } catch(Exception ex) {
                exceptions.add([itemName: it.itemName, errorMessage: siteMessageSource.convert("s:item.not.available")])
            }
        }
        return exceptions
    }

    static Cart addToCartByItems(List<CartItem> cartItems) {
        CartManager.clearCart(AppUtil.session.id)
        Cart cart = new Cart(sessionId: AppUtil.session.id)
        cartItems.each {
            cart.cartItemList.add(it)
            updateCartTotal(cart)
        }
        cartList[AppUtil.session.id] = cart
        return cart
    }

    static Boolean isVariationMatch(List variation1, List variation2) {
        if (!variation1 && !variation2) {
            return true;
        }
        if ((variation1 && !variation2) || (!variation1 && variation2)) {
            return false;
        }
        if (variation1.size() != variation2.size()) {
            return false;
        }
        Boolean result = false
        if (variation1.intersect(variation2).size() == variation1?.size()) {
            result = true
        }
        return result
    }

    static CartItem addToCart(String sessionId, CartObject object, Integer quantity, List variations = null, Boolean addAsNewItem = false) {
        Cart cart = getCart(sessionId);
        if (!cart) {
            cart = new Cart(sessionId: sessionId);
            cartList[sessionId] = cart;
        }
        List<CartItem> items = cart.cartItemList.findAll {
            it.object.type == object.type && it.object.id == object.id
        }
        CartItem cartItem = items ? items.find {
            isVariationMatch(it.variations, variations);
        } : null
        object.validate(quantity)
        if (cartItem && !addAsNewItem) {
            items.remove(cartItem);
            cartItem.updateQuantity(cartItem.quantity + quantity, items)
            AppEventManager.fire("cart-item-quantity-update", [cart])
        } else {
            cartItem = new CartItem(object, quantity);
            if (variations) cartItem.variations = variations
            cart.cartItemList.add(cartItem);
        }
        updateCartTotal(cart);
        return cartItem;
    }

    private static void updateCartDisplay(Cart cart) {
        def updateProp = { type, individualTotalConfig, subTotalConfig ->
            cart.cartItemList.eachWithIndex { cartItem, i ->
                if (individualTotalConfig == DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITH_TAX_WITH_DISCOUNT) {
                    cartItem[type + "DisplayTotal"] = cartItem.total;
                } else if (individualTotalConfig == DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITH_TAX_WITHOUT_DISCOUNT) {
                    cartItem[type + "DisplayTotal"] = cartItem.total;
                } else if (individualTotalConfig == DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITHOUT_TAX_WITH_DISCOUNT) {
                    cartItem[type + "DisplayTotal"] = cartItem.total - cartItem.tax
                } else if (individualTotalConfig == DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITHOUT_TAX_WITHOUT_DISCOUNT) {
                    cartItem[type + "DisplayTotal"] = cartItem.baseTotal
                }
            }
            if (subTotalConfig == DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITH_TAX_WITH_DISCOUNT) {
                cart[type + "DisplaySubTotal"] = cart.actualTotal;
            } else if (subTotalConfig == DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITH_TAX_WITHOUT_DISCOUNT) {
                cart[type + "DisplaySubTotal"] = cart.actualTotal;
            } else if (subTotalConfig == DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITHOUT_TAX_WITH_DISCOUNT) {
                cart[type + "DisplaySubTotal"] = cart.actualTotal - cart.tax
            } else if (subTotalConfig == DomainConstants.SHOPPING_CART_TOTAL_PRICE.WITHOUT_TAX_WITHOUT_DISCOUNT) {
                cart[type + "DisplaySubTotal"] = cart.actualTotal - cart.tax
            }
        }
        String priceWithTaxConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CART_PAGE, "price_enter_with_tax")
        cart.cartItemList.each { CartItem cartItem ->
            if (priceWithTaxConfig == "true") {
                cartItem.displayUnitPrice = cartItem.unitPrice + cartItem.unitTax
            } else {
                cartItem.displayUnitPrice = cartItem.unitPrice
            }
        }
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CART_PAGE)
        updateProp("cartPage", config.individual_total_price, config.subtotal_price)
        config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE)
        updateProp("checkoutPage", config.individual_total_price, config.subtotal_price)
    }

    private static void updateCartTotal(Cart cart, Address applicableAddress = null, Boolean isThread = true) {
        synchronized (cart) {
            try {
                cart.updating = true;
                HttpSession session = AppUtil.session;

                // TODO need to remove this section as it depricated
                /*for (DiscountCalculator calculator : discountCalculators) {
                    calculator.calculate(cart)
                }*/
                AppEventManager.fire("before-update-cart-total", [cart])

                calcuateCartTotal(cart, applicableAddress, isThread)

                AppEventManager.fire("cart-total-updated", [cart])

            } catch (Throwable t) {
                throw t
            } finally {
                cart.updating = false;
            }
        }
    }

    static void calcuateCartTotal(Cart cart, Address applicableAddress = null, Boolean isThread = true) {
        try {
            HttpSession session = AppUtil.session;

            cart.cartItemList.eachWithIndex { cartItem, i ->
                cartItem.calculateTax()

                Double actualUnitPrice = new Double( (cartItem.actualUnitPrice).toConfigPrice() )
                cartItem.total = (actualUnitPrice * cartItem.quantity) - cartItem.actualDiscount
                cartItem.baseTotal = cartItem.total

            }
            cart.baseTotal = cart.cartItemList.sum { it.unitPrice * it.quantity } ?: 0
            cart.total = cart.cartItemList.sum { it.total } ?: 0
            cart.tax = cart.actualTax ? cart.actualTax : cart.cartItemList.sum { it.tax } ?: 0;
            cart.discount = cart.cartItemList.sum { it.discount } ?: 0
            updateCartDisplay(cart)
            AddressData address = applicableAddress ? new AddressData(applicableAddress) : session.effective_shipping_address;
            updateShippingTotal(cart, address, isThread)

        } catch (Throwable t) {
            throw t
        }
    }

    static void updateShippingTotal(Cart cart, AddressData address, Boolean isThread) {
        synchronized (cart) {
            if (cart.shippingProcessor) {
                cart.shippingProcessor.interrupt()
                cart.shippingProcessor.join()
                cart.shippingProcessor = null;
            }
            def session = AppUtil.session
            Closure threadClosure = {
                try {
                    ShippingProfile.withNewSession {
                        Map shippingCosts = cart.deliveryType == DomainConstants.ORDER_DELIVERY_TYPE.SHIPPING ? ShippingCalculator.getShippingCost(cart, address) : [shipping: 0, handling: 0]
                        Long taxProfileId = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "shipping_tax_profile").toLong(0);
                        TaxProfile profile = TaxProfile.get(taxProfileId);
                        if (profile && shippingCosts.shipping != null) {
                            shippingCosts.tax = TaxCalculator.getTax(profile, shippingCosts.shipping + shippingCosts.handling).toTax(profile.appliedRule, session)
                            shippingCosts.shippingTax = TaxCalculator.getTax(profile, shippingCosts.shipping).toTax(profile.appliedRule, session)
                            shippingCosts.handlingTax = TaxCalculator.getTax(profile, shippingCosts.handling).toTax(profile.appliedRule, session)
                        } else {
                            shippingCosts.tax = shippingCosts.shippingTax = shippingCosts.handlingTax = shippingCosts.shipping != null ? 0.0 : null;
                        }
                        cart.shippingCost = shippingCosts
                        cart.shippingProcessor = null;
                        cart.isShipDirty = false;
                    }
                } catch (InterruptedException i) {}
            }
            if (address) {
                if (isThread) {
                    cart.shippingProcessor = Thread.start(threadClosure);
                } else {
                    threadClosure();
                }
            }
        }
    }

    static boolean hasCart(String sessionId) {
        Cart cart = cartList[sessionId];
        if (cart) {
            if (cart.cartItemList.size()) {
                return true;
            }
            cartList.remove(sessionId)
        }
        return false;
    }

    static Cart removeCart(String sessionId) {
        Cart cart = cartList.remove(sessionId)
        AppEventManager.fire("cart-removed", [cart ?: new Cart(sessionId: sessionId)]);
        return cart
    }

    static Collection<Cart> removeCart(Closure condition) {
        Map carts = cartList.findAll { condition(it.value) }
        return carts.each {
            removeCart(it.key)
        }.values()
    }

    static Cart getCompleteCart(String sessionId, Boolean refreshIfDirty = true) {
        Cart cart = getCart(sessionId, refreshIfDirty);
        if (cart.shippingProcessor) {
            cart.shippingProcessor.join()
        }
        return cart;
    }

    static Cart getRefreshedCart(String sessionId) {
        Cart cart = cartList[sessionId];
        if (cart != null) {
            refreshCart(cart)
            if (cart.shippingProcessor) {
                cart.shippingProcessor.join()
            }
        }
        return cart;
    }

    static Cart getCart(String sessionId, Boolean refreshIfDirty = true) {
        Cart cart = cartList[sessionId];
        if (cart && refreshIfDirty) {
            if (cart.isDirty) {
                cart.isShipDirty = false; //to prevent double shipping calculation
                if (cart.updating) {
                    AppUtil.waitFor(cart, "updating", false)
                } else {
                    refreshCart(cart)
                }
            } else if (cart.isShipDirty) {
                if (!cart.shippingProcessor) {
                    updateShippingTotal(cart, AppUtil.session?.effective_shipping_address, true)
                }
            }
        }
        return cart;
    }

    static Cart getCart(Closure condition) {
        return cartList.find { condition(it.value) }?.value
    }

    static Map updateCart(Cart cart, Map idQtyMap) {
        Map errorResponseMap = [:]
        idQtyMap.each { idQty ->
            CartItem cartItem = getCartItem(idQty.key.toInteger(), cart)
            if (cartItem) {
                if (idQty.value.toBigInteger() > Integer.MAX_VALUE) {
                    errorResponseMap["error-" + idQty.key] = siteMessageSource.convert(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CART_PAGE, "invalid_quantity_message"))
                    return
                }
                Closure repeatUpdate
                repeatUpdate = { _quantity ->
                    List<CartItem> variations = cart.cartItemList.findAll {
                        it.id != cartItem.id && it.object.id == cartItem.object.id && it.object.type == cartItem.object.type && it.object.iEquals(cartItem.object)
                    }
                    try {
                        cartItem.updateQuantity(_quantity, variations)
                        AppEventManager.fire("cart-item-quantity-update", [cart])
                    } catch (CartManagerException exc) {
                        String error = exc.message
                        if (error == "ADD_AVAILABLE") {
                            Integer quantityFromOthers = variations ? variations.sum { it.quantity } : 0
                            String message = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "add_available_message");
                            Integer remainingToAdd = exc.messageArgs[0] - quantityFromOthers
                            Map replacerMap = [
                                    requested_quantity: idQty.value.toInteger(),
                                    available_quantity: remainingToAdd
                            ]
                            message = siteMessageSource.convert(message, null, replacerMap)
                            if (remainingToAdd != 0) {
                                repeatUpdate remainingToAdd
                            }
                            errorResponseMap["warning-" + idQty.key] = message
                        } else {
                            Map replacerMap = [
                                    requested_quantity  : exc.messageArgs[0],
                                    multiple_of_quantity: exc.messageArgs[0],
                                    maximum_quantity    : exc.messageArgs[0],
                                    minimum_quantity    : exc.messageArgs[0]
                            ]
                            errorResponseMap["error-" + idQty.key] = siteMessageSource.convert(exc.message, null, replacerMap)
                        }
                    }
                }
                repeatUpdate idQty.value.toInteger(0)
            }
        }
        updateCartTotal(cart);
        return errorResponseMap;
    }

    static CartItem getCartItem(Integer id, Cart cart = null) {
        cart = cart ?: getCart(AppUtil.session.id)
        CartItem cartItem = cart.cartItemList.find { it.id == id}
        return cartItem
    }

    static CartItem getCartItem(ProductData productData, TypeConvertingMap params, Cart cart = null) {
        cart = cart ?: getCart(AppUtil.session.id)
        List<CartItem> items = cart.cartItemList.findAll {
            it.object.type == NamedConstants.CART_OBJECT_TYPES.PRODUCT && it.object.id == productData.id && it.object.iEquals(productData)
        }
        List variations = getProductVariationsForCartAdd(productData, params)
        return items ? items.find {
            isVariationMatch(it.variations, variations);
        } : null
    }

    static List<CartItem> findVariantItems(CartItem cartItem, Cart cart = null) {
        cart = cart ?: getCart(AppUtil.session.id)
        return cart.cartItemList.findAll {
            it.object.type == cartItem.object.type && it.id != cartItem.id && it.object.id == cartItem.object.id && it.object.iEquals(cartItem.object)
        }
    }
    static CartItem populateCartItemForProduct(ProductData product, Integer quantity, TypeConvertingMap params, List<CartItem> variationsInCart = null) {
        ProductInCartBase productCart = getProductCart(product, quantity, params, variationsInCart)
        productCart.validate(quantity + (variationsInCart ? variationsInCart.sum { it.quantity } : 0))
        CartItem cartItem = new CartItem(productCart, quantity);
        return HookManager.hook("populateCartItem", cartItem, quantity, product)
    }

    static ProductInCartBase getProductCart(ProductData product, Integer quantity, TypeConvertingMap params, List<CartItem> variationsInCart = null, Boolean validate = true) {
        ProductInCartBase productInCart = null
        if (product.productType == DomainConstants.PRODUCT_TYPE.DOWNLOADABLE) {
            productInCart = new DownloadableProductInCart(product, params)
        } else {
            productInCart = HookManager.hook("get-product-in-cart", productInCart, product, params)
            if (!productInCart) {
                productInCart = new ProductInCart(product, params)
            }
        }
        if (validate) {
            productInCart.validate(quantity + (variationsInCart ? variationsInCart.sum { it.quantity } : 0))
        }
        return HookManager.hook("populateProductInCart", productInCart, product)
    }

    static CartItem populateCartItemForProduct(Long productId, Double price, Integer quantity, TypeConvertingMap params, List<CartItem> variationsInCart = null) {
        Product product = Product.get(productId);
        ProductData data = ProductService.instance.getProductData(product, params.config);
        ProductInCartBase productCart = getProductCart(data, quantity, params, variationsInCart, true)
        CartItem cartItem = new CartItem(productCart, quantity, price)
        List variations = []
        variations = HookManager.hook("variationsForCartAdd", variations, data, params)
        if (variations) {
            cartItem.variations = variations
        }
        return HookManager.hook("populateCartItem", cartItem, quantity, data)
    }

    static clearCart(String sessionId) {
        Cart cart = cartList[sessionId]
        if (cart) {
            cartList.remove(sessionId)
            AppEventManager.fire("cart-cleared", [cart])
        }
    }

    static Cart removeFromCart(String sessionId, Long cartItemId) {
        Cart cart = getCart(sessionId);
        if(cart != null) {
            CartItem item = cart.cartItemList.find { it.id == cartItemId };
            cart.cartItemList.remove(item)
            item.clearDiscounts()
            updateCartTotal(cart);
        }
        return cart
    }

    private static void refreshCart(Cart cart) {
        List<CartItem> oldItems = cart.cartItemList
        List<CartItem> removeList = []
        oldItems.each { _outerItem ->
            try {
                _outerItem.refresh()
                List<CartItem> variants = cart.cartItemList.findAll { _item ->
                    _outerItem.id != _item.id && _outerItem.object.id == _item.object.id && _item.object.iEquals(_outerItem);
                }
                Integer countFromVariants = variants ? variants.sum { it.quantity } : 0
                try {
                    _outerItem.object.validate(_outerItem.quantity + countFromVariants)
                } catch (CartManagerException exc) {
                    Integer availableQuantity = _outerItem.object.available(_outerItem.quantity + countFromVariants) - countFromVariants
                    def newQuantity = availableQuantity > 0 ? availableQuantity : 0
                    _outerItem.oldQuantity = newQuantity < _outerItem.quantity ? _outerItem.quantity : 0
                    _outerItem.quantity = newQuantity
                }
            } catch (CartManagerException exc) {
                removeList.add(_outerItem)
            }
        }
        oldItems.removeAll(removeList)
        cart.isShipDirty = false;
        if (cart.shippingProcessor) {
            cart.shippingProcessor.interrupt()
            cart.shippingProcessor.join()
            cart.shippingProcessor = null;
        }
        AppEventManager.fire("before-refresh-cart", [cart])
        updateCartTotal(cart)
        cart.isDirty = false;
    }

    static def resolveShippingMap(Cart cart) {
        return cart.getShippingCost()
    }

    static addAmountWithCartItemUnitPrice(String adjustFrom, Double adjustAmount, Integer cartItemId, def adjustAmountObject = null ){
        getCart(AppUtil.session.id).cartItemList.each {
            if(it.id == cartItemId ){
                it.addWithUnitPrice(adjustFrom, adjustAmount, adjustAmountObject)
                it.refresh()
            }
        }
        updateCartTotal(getCart(AppUtil.session.id))
    }

    static removeAddedAmountFromCartItemUnitPrice(String adjustFrom, Integer cartItemId){
        getCart(AppUtil.session.id).cartItemList.each {
            if(it.id == cartItemId ){
                it.removeWithUnitPrice(adjustFrom)
                it.refresh()
            }
        }
        updateCartTotal(getCart(AppUtil.session.id))
    }
}