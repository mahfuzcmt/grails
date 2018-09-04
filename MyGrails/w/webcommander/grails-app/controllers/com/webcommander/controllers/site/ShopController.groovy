package com.webcommander.controllers.site

import com.sun.org.apache.xpath.internal.operations.Bool
import com.webcommander.admin.*
import com.webcommander.authentication.annotations.AutoGeneratedPage
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.LicenseConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.content.NavigationService
import com.webcommander.events.AppEventManager
import com.webcommander.manager.CartManager
import com.webcommander.manager.HookManager
import com.webcommander.manager.LicenseManager
import com.webcommander.models.AddressData
import com.webcommander.models.Cart
import com.webcommander.models.DefaultPaymentMetaData
import com.webcommander.models.PaymentInfo
import com.webcommander.payment.DefaultPaymentService
import com.webcommander.payment.PaymentService
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.Base64Coder
import com.webcommander.webcommerce.*
import com.webcommander.webmarketing.NewsletterService
import com.webcommander.webmarketting.NewsletterSubscriber
import grails.converters.JSON
import grails.util.TypeConvertingMap
import org.apache.commons.httpclient.HttpStatus
import com.webcommander.tenant.Thread

class ShopController {
    AdministrationService administrationService
    ProductService productService
    CustomerService customerService
    OrderService orderService
    CommonService commonService
    ConfigService configService
    PaymentGatewayService paymentGatewayService
    PaymentService paymentService
    NavigationService navigationService
    NewsletterService newsletterService
    DefaultPaymentService defaultPaymentService

    def chooseCheckoutOption() {
        if(params["checkout-type"] == NamedConstants.CUSTOMER_CHECKOUT_TYPE.REGISTRATION) {
            flash.param = [referer: "/shop/checkout"];
            redirect(controller: "customer", action: "register");
        } else {
            session.checkout_as_guest = true
            session.redirect_for_guest_checkout = true
            redirect(controller: "shop", action: "checkout");
        }
    }

    @AutoGeneratedPage("checkout")
    def checkout() {
        AppEventManager.fire("before-checkout");
        if(!CartManager.hasCart(session.id)) {
            redirect(url: "/")
            return;
        }
        Cart cart = CartManager.getCart(session.id, false)

        if((AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "enable_multi_model") == "true") && session.customer) {
            Map cartItemListMap = cart.cartItemList.groupBy { _cartItem -> _cartItem.storeId}
            Cart cacheCart
            for(int i = 1; i < cartItemListMap.size(); i++) {
                def otherStoreCartItems = cartItemListMap.values()[i]
                otherStoreCartItems.each{
                    CartManager.removeFromCart(session.id, it.id)
                }
                cacheCart = CartManager.hasCart(session.customer.encodeAsMD5()) ? CartManager.getCart(session.customer.encodeAsMD5(), false) : new Cart(sessionId: session.customer.encodeAsMD5())
                cacheCart.cartItemList.addAll(otherStoreCartItems)
                CartManager.cartList[session.customer.encodeAsMD5()] = cacheCart
            }
        }

        if(cart.isDirty) {
            redirect(controller: "cart", action: "details")
            return;
        }
        if(cart?.cartItemList.removeAll {it.quantity <= 0}) {
            cart.isDirty = true
            cart = CartManager.getCart(session.id)
            if(!cart || cart.cartItemList.size() == 0) {
                redirect(url: "/")
                return;
            }
        }
        Map model;
        String view = "/site/siteAutoPage"
        Map configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE)
        if (configs.allow_sign_up == "false" && configs.show_login == "false") {
            session.checkout_as_guest = true
        } else {
            if (!session.redirect_for_guest_checkout) {
                session.checkout_as_guest = false
            }
            session.redirect_for_guest_checkout = false
        }
        if(session.checkout_as_guest != true && AppUtil.loggedCustomer == null && session.effective_billing_address == null) {
            model = [name: DomainConstants.AUTO_GENERATED_PAGES.CHECKOUT, configs: configs, view: "site/checkout/loginRegister.gsp"]
            view = HookManager.hook("auto-page-view-model", view, model);
            render (view: view, model: model)
            return;
        }

        if(configs["enable_"+ cart.deliveryType] != "true") {
            cart.initDeliveryType()
        }

        Boolean shouldHaveShipping = cart.cartItemList.find {
            it.isShippable
        }
        cart = CartManager.getCart(session.id)
        model = [
                name: DomainConstants.AUTO_GENERATED_PAGES.CHECKOUT,
                view: "site/checkout/checkout.gsp", cart: cart,
                shouldHaveShipping: shouldHaveShipping,
        ];
        view = HookManager.hook("auto-page-view-model", view, model);
        render (view: view, model: model)
    }

    def loadAddressStep() {
        if(!CartManager.hasCart(session.id)) {
            throw new ApplicationRuntimeException("your.shopping.cart.empty", HttpStatus.SC_PRECONDITION_FAILED)
        }
        if(request.method.equalsIgnoreCase("post")) {
            session.is_different_shipping = params.is_different_shipping == "true"
            switch (params.operation) {
                case "saveOrSelectAddress":
                    this.saveOrSelectAddress()
                    break
            }
        }
        Boolean isTaxAvailable = true
        Cart cart = CartManager.getCart(session.id)
        cart.cartItemList.each {
            if (!it.object.product.exitTaxCode){
                isTaxAvailable = false
            }
        }
        if(params.delivery_type) {
            cart.deliveryType = params.delivery_type
            cart.isDirty = true
        }
        String mode = params.mode
        Customer customer = AppUtil.loggedCustomer ? Customer.get(AppUtil.loggedCustomer) : null
        AddressData effectiveBilling = session.effective_billing_address
        if(customer && effectiveBilling == null) {
            effectiveBilling = session.effective_billing_address = new AddressData(customer.activeBillingAddress);
            AppEventManager.fire("effective-billing-change", [session.id])
        }
        AddressData effectiveShipping = session.effective_shipping_address
        if(customer && effectiveShipping == null) {
            effectiveShipping = session.effective_shipping_address = new AddressData(customer.activeShippingAddress);
            AppEventManager.fire("effective-shipping-change", [session.id])
        }
        if(effectiveBilling == null) {
            mode = "init"
        }
        mode = mode ?: "edit"
        Boolean shouldHaveShipping = cart.cartItemList.find {
            it.isShippable
        }
        render(view: "/site/checkout/addressStep/${mode}", model: [
                isTaxAvailable: isTaxAvailable,
                cart: cart,
                customer: customer,
                configs: AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE),
                shouldHaveShipping: shouldHaveShipping,
                effectiveBilling: effectiveBilling,
                effectiveShipping: effectiveShipping
        ]);
    }

    def loadAddressEditor() {
        String addressType = params.type ?: "billing"
        AddressData address = params.isNew ? null : session."effective_${addressType}_address"
        if(!address) { // guest customer
            Address _address = new Address();
            Long defaultCountryId = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "default_country").toLong();
            Long defaultStateId = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "default_state").toLong();
            _address.country = Country.proxy(defaultCountryId);
            if(defaultStateId) {
                _address.state = State.proxy(defaultStateId)
            }
            address = new AddressData(_address)
        }
        def states = administrationService.getStatesForCountry(address.countryId)
        Map fieldsConfigs = (Map) AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES["${addressType.toUpperCase()}_ADDRESS_FIELD"]);
        List sortedFields = configService.getSortedFields(fieldsConfigs)
        List activeFields = configService.getActiveFields(sortedFields, fieldsConfigs)
        render(view: "/site/checkout/editAddress", model: [
            fields: activeFields,
            fieldsConfigs: fieldsConfigs,
            address: address,
            states: states,
            addressType: addressType
        ]);
    }

    def addressSelectionPopup() {
        if(!CartManager.hasCart(session.id)) {
            throw new ApplicationRuntimeException("your.shopping.cart.empty", HttpStatus.SC_PRECONDITION_FAILED)
            return;
        }
        String addressType = params.addressType ?: "billing"
        AddressData address = session."effective_${addressType}_address" ?: new AddressData()
        Customer customer = session.customer ? Customer.get(session.customer) : null
        List<Address> addresses = []
        if(customer) {
            addresses = customer."${addressType}Addresses"
        }
        render(view: "/site/checkout/addressSelectionPopup", model: [
            address: address,
            addresses: addresses,
            addressType: addressType
        ])
    }

    private void saveOrSelectAddress() {
        Customer customer = AppUtil.loggedCustomer ? Customer.get(AppUtil.loggedCustomer) : null
        Address address;
        String addressType = params.addressType ?: "billing"
        List<Address> addresses = [];
        if(customer) {
            addresses = addressType == "shipping" ? customer.shippingAddresses : customer.billingAddresses
        }
        if(params.selectedAddress) {
            address = addresses.find { it.id == params.long("selectedAddress")}
        } else if(!params.selectedAddress) {
            address = params.id ? Address.get(params.id) : new Address()
            address.properties = params
        }
        if(address == null || address.hasErrors()) { return }
        AddressData addressData = new AddressData(address)
        if(addresses.find { it.id == address.id}) {
            address.merge()
        }
        if(customer && address.id == null && params.saveInProfile) {
            try {
                customerService.saveAddress(customer, address, addressType)
            } catch (Exception ignore) {}
        }
        session."effective_${addressType}_address" = addressData
        AppEventManager.fire("effective-${addressType}-change", [session.id])

        if(addressType == "billing" && session.effective_shipping_address == null) {
            session.effective_shipping_address = session.effective_billing_address
            AppEventManager.fire("effective-shipping-change", [session.id])
        }
    }

    def loadShippingStep() {
        [].sort()
        Cart cart = CartManager.getCart(session.id)
        TypeConvertingMap selectedMethods = params.shippingMethod
        if (cart && selectedMethods) {
            cart.cartItemList.each {
                it.selectedShippingMethod = selectedMethods.long(it.id.toString())
            }
        }
        if(cart && params.removedItem) {
            CartManager.removeFromCart(session.id, params.long("removedItem"))
        }
        if(!CartManager.hasCart(session.id)) {
            throw new ApplicationRuntimeException("your.shopping.cart.empty", HttpStatus.SC_PRECONDITION_FAILED)
        }
        cart = CartManager.getCompleteCart(session.id)

        List<ShippingClass> shippingClasses = ShippingClass.list()
        Boolean isClassEnabled = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "enable_shipping_class") == "true"
        String mode = params.mode ?: "edit"
        if (mode == "view" && cart.shippingCost.shipping == null) {
            throw new ApplicationRuntimeException("not.support.shipping.choose.address")
        }
        render(view: "/site/checkout/shippingStep/${mode}", model: [
                cart: cart,
                isClassEnabled: isClassEnabled,
                shippingClasses: shippingClasses
        ])
    }

    def loadConfirmStep() {

        if(!CartManager.hasCart(session.id)) {
            throw new ApplicationRuntimeException("your.shopping.cart.empty", HttpStatus.SC_PRECONDITION_FAILED)
        }
        AppEventManager.fire("before-confirm-step-load", [params])
        if(params.payment_gateway) {
            session.payment_gateway = params.payment_gateway
        }
        Cart cart = CartManager.getRefreshedCart(session.id)
        if(cart.cartItemList.removeAll {it.quantity <= 0}) {
            cart.isDirty = true
            cart = CartManager.getCompleteCart(session.id)
        }
        Map shippingMap = CartManager.resolveShippingMap(cart)
        Boolean shouldHaveShipping = cart.cartItemList.find { it.isShippable }
        Double payable = cart.total + (shouldHaveShipping ? (shippingMap.handling ?: 0) + (shippingMap.shipping ?: 0) + (shippingMap.tax ?: 0) - cart.discountOnShipping : 0)
        if(!shouldHaveShipping) {
            shippingMap = [handling: 0, shipping: 0, tax: 0]
        }
        def configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE)
        String termsMessage = null
        if(configs.terms_and_condition == 'on') {
            switch (configs.terms_and_condition_type){
                case DomainConstants.TERMS_AND_CONDITION_TYPE.PAGE:
                    termsMessage = g.message(code: "i.agree.to") + ' <a target="_blank" href="' +  navigationService.getUrl("page", configs.terms_and_condition_ref) + '">' + g.message(code: "terms.condition") + '</a>';
                    break
                case DomainConstants.TERMS_AND_CONDITION_TYPE.EXTERNAL_LINK:
                    termsMessage = g.message(code: "i.agree.to") + ' <a target="_blank" href="' + configs.terms_and_condition_ref + '">' + g.message(code: "terms.condition") + '</a>';
                    break
                default:
                    termsMessage = g.message(code: "i.agree.to") + " " + g.message(code: "terms.condition")
                    break
            }
        }
        List<DefaultPaymentMetaData> defaultPayments = new ArrayList<DefaultPaymentMetaData>()
        Customer customer = session.customer ? Customer.get(session.customer) : null
        String enableComment = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE, "enable_comment")

        Map model = [
            params: params, payable: payable,
            surcharge: 0, configs: configs, cart: cart,
            grandTotal: payable, shippingMap: shippingMap, customer: customer,
            termsMessage: termsMessage, defaultPayments: defaultPayments, error: "",
            enableComment: enableComment
        ]

        if(CartManager.hasCart(session.customer.encodeAsMD5())) {
            model.otherStoreCart = CartManager.getCart(session.customer.encodeAsMD5(), false)
        }

        List<PaymentGateway> gateways = paymentGatewayService.getAvailablePaymentGateways()
        model.gateways = gateways.collect { [key: it.code, label: it.name] };
        String selectedGateway = session.payment_gateway ?:  gateways.find { it.isDefault }?.code;
        if(selectedGateway == null && gateways) {
            selectedGateway = gateways.iterator().next().code
        }
        model.selected = session.payment_gateway = selectedGateway
        model = (Map) HookManager.hook("checkout-confirm-step-model", model)
        defaultPaymentService.processDefaultPaymentsBeforeConfirmStep(model)
        Double surcharge = paymentGatewayService.calculateSurchargeAmount(session.payment_gateway, model.payable)
        model.surcharge =  model.surcharge + surcharge
        model.grandTotal += surcharge
        Map error = commonService.checkMinimumPurchaseAmount(payable)
        model.isValidAmount = error == null
        if(error) {
            model.error = site.message(code: error.message, macros: error.macros)
        }
        model.due = model.grandTotal - (defaultPayments ? defaultPayments.sum { it.amount } : 0)

        model.enableWallet = paymentGatewayService.enableWalletPayment
        List<CreditCard> cards = paymentGatewayService.getAvailableCreditCard([
                customerId   : AppUtil.loggedCustomer
        ])
        model.wallets = [[key: "", label: g.message(code: "none")]] + cards.collect {[key: it.id, label: it.cardType.toUpperCase() + " [" + it.cardNumber + "]"]}

        render(view: "/site/checkout/confirmStep", model: model)
    }

    @AutoGeneratedPage("credit.card.payment")
    def payment() {
        if(!CartManager.hasCart(session.id)) {
            flash.model = [error: g.message(code: "session.timed.out")]
            redirect(uri: "/")
            return;
        }
        AppEventManager.fire("before-order-confirm", [params])
        if(!params.confirmed) {
            redirect(action: "checkout")
            return;
        }
        Customer customer;
        if(session.customer) {
            customer = Customer.get(session.customer)
        }
        try {
            Cart cart = CartManager.getRefreshedCart(session.id)
            if(cart.isDirty) {
                redirect(controller: "cart", action: "details")
                return;
            }
            if(cart.cartItemList.removeAll {it.quantity <= 0}) {
                cart.isDirty = true
                cart = CartManager.getRefreshedCart(session.id)
            }
            Map shippingMap = cart.getShippingCost()
            Boolean shouldHaveShipping = cart.cartItemList.find {
                it.isShippable
            }
            Double grandTotal = cart.total + (shouldHaveShipping ? (shippingMap.handling ?: 0) + (shippingMap.shipping ?: 0) + (shippingMap.tax ?: 0) - cart.discountOnShipping : 0)
            Map error = commonService.checkMinimumPurchaseAmount(grandTotal)
            if(error){
                flash.model = [error: site.message(code: error.message, macros: error.macros)]
                redirect(action: "checkout")
                return;
            }
            if(cart.deliveryType == DomainConstants.ORDER_DELIVERY_TYPE.SHIPPING && cart.shippingCost.shipping == null) {
                redirect(action: "checkout")
                return
            }
            String comment = params.comment
            Long orderId = orderService.saveOrder(cart, session.effective_billing_address, session.effective_shipping_address, customer)
            Order order = Order.get(orderId)
            if(comment) {
                new OrderComment(order: order, content: comment, isAdmin: false).save()
            }
            Boolean sendMail = !cart.orderId
            cart.orderId = orderId
            AppEventManager.fire("order-confirm", [cart])
            if(sendMail) {
                Thread.start {
                    AppUtil.initialDummyRequest()
                    orderService.sendEmailForOrder(orderId, "create-order", comment)
                }
            }
            if(cart.deliveryType == DomainConstants.ORDER_DELIVERY_TYPE.OTHERS_SHIPPING) {
                Payment payment = new Payment(order: order, status: DomainConstants.PAYMENT_STATUS.PENDING)
                order.discard()
                payment.discard()
                flash.param = [paymentInstance: payment, pending: true]
                redirect(controller: "payment", action: "success")
                return;
            }
            if(!session.payment_gateway) {
                session.payment_gateway = PaymentGateway.findAll().find {it.isDefault}?.code
            }
            Double payable =  grandTotal - cart.paid
            List<PaymentInfo> payments = new ArrayList<PaymentInfo>()
            Map paymentAsset = [params: params, cart: cart, payable: payable, payments: payments]
            paymentAsset = (Map)HookManager.hook("payment-asset", paymentAsset)
            defaultPaymentService.processDefaultPayments(paymentAsset)
            payable = paymentAsset.payable
            Double surcharge = paymentGatewayService.calculateSurchargeAmount(session.payment_gateway, payable)
            if(payable <= 0.0) {
                if(!paymentAsset.payments) {
                    Payment payment = new Payment(amount: 0.0, status: DomainConstants.PAYMENT_STATUS.SUCCESS, order: Order.proxy(orderId))
                    flash.param = [paymentInstance: payment];
                } else {
                    flash.param = [payments: paymentAsset.payments]
                }
                redirect(controller: "payment", action: "success")
                return
            }
            Payment payment = new Payment()
            payment.amount = surcharge + payable
            payment.surcharge = surcharge
            payment.gatewayCode = session.payment_gateway
            payment.status = DomainConstants.PAYMENT_STATUS.AWAITING
            payment.payingDate = new Date().gmt()
            payment.order = Order.proxy(orderId)
            payment.save()
            // TODO: Need to Address for Payment Relation
            order.addToPayments(payment)
            order.save(flush: true)
            cart.tagged.payment = payment
            cart.tagged.payable = payable
            cart.tagged.surcharge = surcharge
            paymentService.storePaymentEntry(payment.id)
            if(params.paymentMethod == DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD && params.walletPayment) {
                paymentService."process${session.payment_gateway}WalletPayment"(params, {
                    redirect(it)
                })
                return
            }
            paymentService."render${session.payment_gateway}PaymentPage"(cart) {
                render(it)
            }
            return;
        } catch(ApplicationRuntimeException exc) {
            log.error("Error Processing Payment : " + exc)
            flash.model = [error: exc.localizedMessage]
        } catch(Throwable t) {
            log.error("Error Processing Payment : " + t)
            flash.model = [error: g.message(code: "could.not.process.payment.contact.with.vendor")]
        }
        flash.param = [step: NamedConstants.CHECKOUT_PAGE_STEP.CONFIRM_STEP]
        redirect(action: "checkout")
    }

    def paymentInfo() {
        PaymentGateway gateway = PaymentGateway.findByCode(params.code);
        render gateway.information ?: "";
    }

    def tellFriend() {
        render(view: "/site/tellFriend")
    }

    def sendMailToFriend() {
        try {
            productService.sendMailToFriend(params)
            def successPopup = g.include(view: "/site/tellFriendSuccess.gsp", model: [email: params.receiver])
            render([status: "success", html: successPopup.toString()] as JSON);
        } catch (e) {
            render([status: "error", message: g.message(code: "email.could.not.be.sent")] as JSON);
        }
    }

    def newsletterSubscription() {
        def id = NewsletterSubscriber.where {
            email == params.email
            isSubscribed == true
        }.count()

        if (id) {
            render([status: "error", message: g.message(code: "already.subscribed")] as JSON)
        } else {
            if(newsletterService.subscribeNewsletter(params)) {
                render([status: "success", message: g.message(code: "subscription.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "subscription.error")] as JSON)
            }
        }
    }

    def newsletterUnsubscription() {
        try {
            byte[] valueDecoded= Base64Coder.decode(params.sid)
            params.email = new String(valueDecoded).split("::::")[0]
        } catch (Exception e) {
            redirect(url: app.baseUrl())
        }
        def id = NewsletterSubscriber.where {
            email == params.email
            isSubscribed == true
        }.count()

        if (id) {
            if(newsletterService.unsubscribeNewsletter(params)) {
                render([status: "success", html: g.include(view: "/site/resubscribe.gsp"), model: [:]] as JSON)
            } else {
                render([status: "error", message: g.message(code: "unsubscription.error")] as JSON)
            }
        } else {
            render([status: "error", message: g.message(code: "already.unsubscribed")] as JSON)
        }
    }

    def newsletterResubscription() {
        byte[] valueDecoded= Base64Coder.decode(params.sid)
        params.email = new String(valueDecoded).split("::::")[0]
        def id = NewsletterSubscriber.where {
            email == params.email
            isSubscribed == false
        }.count()

        if (id) {
            if(newsletterService.subscribeNewsletter(params)) {
                render([status: "success", message: g.message(code: "resubscription.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "subscription.error")] as JSON)
            }
        } else {
            render([status: "error", message: g.message(code: "already.subscribed")] as JSON)
        }
    }
}