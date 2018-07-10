package com.webcommander.payment

import com.webcommander.admin.Customer
import com.webcommander.admin.StoreCreditHistory
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.LicenseConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.LicenseManager
import com.webcommander.models.Cart
import com.webcommander.models.DefaultPaymentMetaData
import com.webcommander.models.PaymentInfo
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.Payment
import com.webcommander.webcommerce.PaymentGatewayService
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import java.util.concurrent.ConcurrentHashMap

class DefaultPaymentService {
    PaymentGatewayService paymentGatewayService
    private Map<String, Double> cachedStoreCreditRequestedAmount = new ConcurrentHashMap<String, Double>()

    def processDefaultPaymentsBeforeConfirmStep(Map model) {
        HttpServletRequest request = AppUtil.request
        DomainConstants.DEFAULT_PAYMENT_GATE_WAYS.sort {
            it.value.ORDER
        }.each { gw ->
            try {
                String licenseKey = LicenseConstants.PAYMENT_GATEWAY[gw.value.PAYMENT_GATEWAY_CODE]
                if(this.respondsTo("process${gw.value.IDENTIFIER}PaymentBeforeOrderConfirm") && (!licenseKey || LicenseManager.isAllowed(licenseKey))) {
                    this."process${gw.value.IDENTIFIER}PaymentBeforeOrderConfirm"(model)
                }
            } catch (ApplicationRuntimeException ex) {
                if(request.xhr) {
                    throw ex;
                } else {
                    model.error = model.error + ex.message
                }
            }
        }
    }

    def processDefaultPaymentsForCalculationAPI(Map model) {
        HttpServletRequest request = AppUtil.request
        DomainConstants.DEFAULT_PAYMENT_GATE_WAYS.sort {
            it.value.ORDER
        }.each { gw ->
            String licenseKey = LicenseConstants.PAYMENT_GATEWAY[gw.value.PAYMENT_GATEWAY_CODE]
            if(this.respondsTo("process${gw.value.IDENTIFIER}PaymentForCalculationAPI") && (!licenseKey || LicenseManager.isAllowed(licenseKey))) {
                this."process${gw.value.IDENTIFIER}PaymentForCalculationAPI"(model)
            }
        }
    }

    @Transactional
    def processDefaultPayments(Map paymentAsset) {
        Cart cart = paymentAsset.cart
        DomainConstants.DEFAULT_PAYMENT_GATE_WAYS.sort {
            it.value.ORDER
        }.each { gw ->
            "process${gw.value.IDENTIFIER}Payment"(paymentAsset)
        }
        cart.paid = paymentAsset.payments ? paymentAsset.payments.sum { it.amount } : 0.0;
    }

    @Transactional
    def processDefaultPaymentsForAPI(Map paymentAsset) {
        Cart cart = paymentAsset.cart
        DomainConstants.DEFAULT_PAYMENT_GATE_WAYS.sort {
            it.value.ORDER
        }.each { gw ->
            if(this.respondsTo("process${gw.value.IDENTIFIER}PaymentForAPI")) {
                this."process${gw.value.IDENTIFIER}PaymentForAPI"(paymentAsset)
            }
        }
        cart.paid = paymentAsset.payments ? paymentAsset.payments.sum { it.amount } : 0.0;
    }

    def processStoreCreditPaymentBeforeOrderConfirm(Map model) {
        Boolean isActive = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.STORE_CREDIT, "apply_by_default").toBoolean();
        HttpSession session = AppUtil.session
        Customer customer = session.customer ? Customer.get(session.customer) : null
        Double payable = model.payable;
        if(isActive && customer && customer.storeCredit > 0 && payable > 0) {
            GrailsParameterMap params = AppUtil.params;
            Cart cart = model.cart
            DefaultPaymentMetaData storeCreditPayment = new DefaultPaymentMetaData(name: "payment.from.store.credit", identifier: "storeCredit");
            Double requestedStoreCredit = params.containsKey("storeCreditPayment") ? params.double("storeCreditPayment") : (cachedStoreCreditRequestedAmount[cart.sessionId] ?: null);
            Double surCharge = paymentGatewayService.calculateSurchargeAmount(DomainConstants.PAYMENT_GATEWAY_CODE.STORE_CREDIT, payable);
            Double applicableAmount;
            if (params.containsKey("storeCreditPayment") || cachedStoreCreditRequestedAmount.containsKey(cart.sessionId)) {
                requestedStoreCredit = requestedStoreCredit ?: 0.0;
                if(requestedStoreCredit > customer.storeCredit) {
                    removeCachedAmount(cart.sessionId)
                    throw new ApplicationRuntimeException("insufficient.store.credit")
                }
                applicableAmount = requestedStoreCredit;
                surCharge = paymentGatewayService.calculateSurchargeAmount(DomainConstants.PAYMENT_GATEWAY_CODE.STORE_CREDIT, applicableAmount);
                storeCreditPayment.amount = applicableAmount
                if(payable + surCharge < applicableAmount) {
                    removeCachedAmount(cart.sessionId)
                    throw new ApplicationRuntimeException("payment.should.not.greater.then.total")
                } else {
                    cachedStoreCreditRequestedAmount[cart.sessionId] = requestedStoreCredit;
                }
                payable = payable + surCharge
            } else if(payable + surCharge > customer.storeCredit) {
                applicableAmount = customer.storeCredit;
                surCharge = paymentGatewayService.calculateSurchargeAmount(DomainConstants.PAYMENT_GATEWAY_CODE.STORE_CREDIT, applicableAmount);
                storeCreditPayment.amount = applicableAmount
                payable = payable + surCharge
            } else {
               applicableAmount = payable
               storeCreditPayment.amount = applicableAmount + surCharge
            }
            model.surcharge = model.surcharge + surCharge;
            model.grandTotal = model.grandTotal + surCharge
            model.payable = payable - applicableAmount
            storeCreditPayment.max = customer.storeCredit
            model.defaultPayments.add(storeCreditPayment)
        }
    }

    @Transactional
    def processStoreCreditPayment(Map paymentAsset) {
        Boolean isActive = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.STORE_CREDIT, "apply_by_default").toBoolean();
        HttpSession session = AppUtil.session
        Customer customer = session.customer ? Customer.proxy(session.customer) : null
        Double payable = paymentAsset.payable;
        Cart cart = paymentAsset.cart;
        PaymentInfo info = new PaymentInfo();
        if(isActive && customer && customer.storeCredit > 0 && payable > 0) {
            Order order = Order.get(cart.orderId);
            Double applicableAmount
            Double surCharge = paymentGatewayService.calculateSurchargeAmount(DomainConstants.PAYMENT_GATEWAY_CODE.STORE_CREDIT, payable);
            if(cachedStoreCreditRequestedAmount.containsKey(cart.sessionId)) {
                applicableAmount = cachedStoreCreditRequestedAmount[cart.sessionId];
                surCharge = paymentGatewayService.calculateSurchargeAmount(DomainConstants.PAYMENT_GATEWAY_CODE.STORE_CREDIT, applicableAmount);
                info.amount = applicableAmount
            } else if(payable + surCharge > customer.storeCredit) {
                applicableAmount = customer.storeCredit;
                surCharge = paymentGatewayService.calculateSurchargeAmount(DomainConstants.PAYMENT_GATEWAY_CODE.STORE_CREDIT, applicableAmount);
                info.amount = applicableAmount
            } else {
                applicableAmount = payable;
                info.amount = applicableAmount + surCharge
            }
            if(info.amount > customer.storeCredit) {
                throw new ApplicationRuntimeException("insufficient.store.credit")
            }
            if(info.amount > 0) {
                customer.storeCredit = customer.storeCredit - info.amount
                customer.merge();
                StoreCreditHistory history = new StoreCreditHistory(customer: customer, deltaAmount: -1 * info.amount, note: "After Order#${order.id} payment")
                history.save()
                Payment payment = new Payment();
                payment.amount = info.amount
                payment.surcharge = surCharge
                payment.payerInfo = "Customer# " + customer.id;
                payment.trackInfo = "Paid by Store Credit"
                payment.gatewayCode = DomainConstants.PAYMENT_GATEWAY_CODE.STORE_CREDIT;
                payment.status = DomainConstants.PAYMENT_STATUS.SUCCESS
                payment.payingDate = new Date().gmt()
                payment.order = order
                payment.save();
                order.addToPayments(payment)
                order.totalSurcharge = order.totalSurcharge + surCharge;
                order.merge();
                if (!customer.hasErrors() && !payment.hasErrors()) {
                    info.trackInfo = ""
                    info.gatewayResponse = ""
                    info.success = true;
                    info.paymentRef = payment.id
                    paymentAsset.payable = payable - info.amount + surCharge;
                    paymentAsset.payments.add(info)
                }
            }
        }
        removeCachedAmount(cart.sessionId)
    }

    void removeCachedAmount(String key) {
        cachedStoreCreditRequestedAmount.remove(key)
    }

    static {
        AppEventManager.on("cart-cleared cart-removed cart-item-add cart-item-quantity-update cart-modified", { cart ->
            DefaultPaymentService defaultPaymentService = Holders.grailsApplication.mainContext.getBean(DefaultPaymentService);
            defaultPaymentService.removeCachedAmount(cart.sessionId)
        })
    }
}
