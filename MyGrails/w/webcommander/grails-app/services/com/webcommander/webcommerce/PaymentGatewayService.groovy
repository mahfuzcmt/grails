package com.webcommander.webcommerce

import com.webcommander.AppResourceTagLib
import com.webcommander.admin.Customer
import com.webcommander.admin.Zone
import com.webcommander.admin.ZoneService
import com.webcommander.annotations.Initializable
import com.webcommander.common.ImageService
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.manager.LicenseManager
import com.webcommander.manager.PathManager
import com.webcommander.models.AddressData
import com.webcommander.models.MockStaticResource
import com.webcommander.util.AppUtil
import com.webcommander.util.Base64Coder
import grails.gorm.transactions.Transactional
import grails.transaction.NotTransactional
import grails.web.databinding.DataBindingUtils
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.multipart.MultipartFile

@Initializable
@Transactional
class PaymentGatewayService {

    ImageService imageService
    ZoneService zoneService

    @Autowired
    @Qualifier("com.webcommander.AppResourceTagLib")
    AppResourceTagLib appResource

    static void initialize() {
        HookManager.register("zone-delete-veto") {response, id ->
            Integer count = PaymentGateway.where {
                zone.id == id
            }.count();
            if (count) {
                response."payment.gateway" = count
            }
            return response
        }
        HookManager.register("zone-delete-veto-list") { response, id ->
            List<PaymentGateway> gateways = PaymentGateway.createCriteria().list {
                eq("zone.id", id)
            }
            if(gateways.size()) {
                response."payment.gateway" = gateways.collect { gateway ->
                    getG().message(code: gateway.name)
                }
            }
            return response
        }
    }

    @NotTransactional
    Integer getPaymentGatewayCount(Map params) {
        return PaymentGateway.createCriteria().count(getCriteriaClosure(params))
    }

    @NotTransactional
    List<PaymentGateway> getPaymentGateways(Map params) {
        def listMap = [max: params.max, offset: params.offset];
        return PaymentGateway.createCriteria().list(listMap) {
            and getCriteriaClosure(params)
        }
    }

    private Closure getCriteriaClosure(Map params) {
        return {
            ne("code", DomainConstants.PAYMENT_GATEWAY_CODE.API)
            if(params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if(params.notPromotional) {
                eq("isPromotional", false)
            }
        }
    }

    public boolean update(GrailsParameterMap params) {
        if(params."zone.id" == "create-zone") {
            params."zone.id" = zoneService.saveZone(params.zone, null).id;
        }
        Long id = params.long("id")
        PaymentGateway paymentGateway = PaymentGateway.get(id)
        if(!params.isEnabled.toBoolean() && params.isDefault.toBoolean()) {
            params.isDefault = "false"
        } else if(params.isDefault.toBoolean()) {
            def paymentGateways = PaymentGateway.getAll();
            paymentGateways.each {
                it.isDefault = false
            }
        }
        params.flatSurcharge = params.flatSurcharge ?: 0.0
        DataBindingUtils.bindObjectToInstance(paymentGateway, params, null, ["id"], null)

        if (!updateMetaFields(params.metafield)) {
            return false
        }

        if(params.surchargeType == DomainConstants.SURCHARGE_TYPE.SURCHARGE_ON_AMOUNT_RANGE) {
            def fromList = params.list("from")
            def toList = params.list("to")
            def amountList = params.list("surcharge-amount")
            paymentGateway.surchargeRange*.delete()
            paymentGateway.surchargeRange.clear()
            int surchargeRangeCount = fromList.size()
            for (int i=0; i<surchargeRangeCount; i++) {
                SurchargeRange surchargeRange = new SurchargeRange(paymentGateway: paymentGateway)
                surchargeRange.orderAmountFrom = Double.parseDouble(fromList[i])
                surchargeRange.orderAmountTo = Double.parseDouble(toList[i])
                surchargeRange.surchargeAmount = Double.parseDouble(amountList[i])
                paymentGateway.addToSurchargeRange(surchargeRange)
            }
        }

        return !paymentGateway.hasErrors()
    }

    boolean updateMetaFields(Map fields) {
        String cardRelativePath
        String cardType
        Map cardData = [:]
        try {
            fields.each { f ->
                if (f.key.indexOf(".") > -1) {
                    return
                }
                def allMeta = PaymentGatewayMeta.findAllByFieldFor(f.key)
                def paramValues = f.value
                allMeta.each { meta ->
                    def value = paramValues[meta.name] ?: ""
                    meta.value = value instanceof Object[] ? value.join(",") : value
                }
                AppEventManager.fire(f.key + "-payment-gateway-configuration-update", [paramValues])
            }
        } catch (Throwable throwable) {
            return false
        }
        if (fields?.CRD?.customLogo) {
            cardType = AppResourceTagLib.CREDIT_CARD
            cardData += [uploadedFile: fields.CRD.customLogo, cardCustomLogoField: "customLogo", cardType: cardType]
        }
        if (fields?.APY?.afterpayImage) {
            cardType = AppResourceTagLib.AFTER_PAY
            cardData += [uploadedFile: fields.APY.afterpayImage, cardCustomLogoField: "afterpayImage", cardType: cardType]
        }
        if (cardData) {
            cardRelativePath = appResource.getPaymentGatewayCardRelativePath(card: cardType)
            cardData += [cardRelativePath: cardRelativePath]
            return uploadPaymentGatewayCardLogo(cardData)
        }
        return true
    }

    boolean uploadPaymentGatewayCardLogo(Map cardData) {
        String filePath = appResource.getResourcePhysicalPath(extension: cardData.cardRelativePath)
        File f = new File(filePath)
        if (f.exists()) {
            f.deleteDir()
            f.mkdirs()
        } else {
            f.mkdirs()
        }
        MockStaticResource mockResource = new MockStaticResource(relativeUrl: cardData.cardRelativePath, resourceName: AppResourceTagLib.CUSTOM_LOGO_PNG)
        imageService.uploadImage(cardData.uploadedFile, "", mockResource)
        PaymentGatewayMeta.findByFieldForAndName(cardData.cardType, cardData.cardCustomLogoField).value = cardData.uploadedFile.originalFilename
        return true
    }

    @NotTransactional
    public double calculateSurchargeAmount(String gatewayCode, Double payable) {
        PaymentGateway gateway = gatewayCode ? PaymentGateway.findByCode(gatewayCode) : null
        if(gateway && payable) {
            switch(gateway.surchargeType) {
                case DomainConstants.SURCHARGE_TYPE.SURCHARGE_ON_AMOUNT_RANGE:
                    return gateway.surchargeRange.find {
                        it.orderAmountFrom <= payable && it.orderAmountTo >= payable
                    }?.surchargeAmount ?: 0
                    break;
                case DomainConstants.SURCHARGE_TYPE.NO_SURCHARGE:
                    return 0
                    break;
                case DomainConstants.SURCHARGE_TYPE.FLAT_SURCHARGE:
                    return gateway.flatSurcharge
                    break;
            }
        }
        return 0;
    }

    public PaymentGateway getPaymentGatewayByCode(String gatewayCode) {
        return PaymentGateway.where {
            code == gatewayCode
        }.get()
    }

    public List<PaymentGateway> getAvailablePaymentGateways() {
        def gateways = PaymentGateway.findAllByIsEnabledAndIsPromotional(true, false)

        AddressData address = AppUtil.session.effective_billing_address;
        gateways = gateways.findAll {
            Zone zone = it.zone
            return !zone || AppUtil.matchAddressWithZone(address, zone)
        }
        if(!AppUtil.session.customer || (LicenseManager.isProvisionActive() && !LicenseManager.license("allow_store_credit_feature"))) {
            gateways.removeAll {
                it.code == DomainConstants.PAYMENT_GATEWAY_CODE.STORE_CREDIT
            }
        }
        return gateways
    }

    def changeStatus(List<Long> ids, Boolean status) {
        Integer count = 0;
        ids.each {
            PaymentGateway paymentGateway= PaymentGateway.get(it)
            paymentGateway.isEnabled = status
            count++;
        }
        return count;
    }

    @NotTransactional
    List<CreditCard> getAvailableCreditCard(Map params) {
        return CreditCard.createCriteria().list {
            eq("isActive", true)
            if(params.customerId) {
                eq("customer.id", params.customerId)
            }
            eq("gatewayName", activeCreditCardGateway)
        }
    }

    @NotTransactional
    String getActiveCreditCardGateway() {
        return PaymentGatewayMeta.findByNameAndFieldFor("creditCardProcessor", "CRD").value
    }

    @NotTransactional
    Boolean getEnableWalletPayment() {
        def gateway = PaymentGateway.findByCode("CRD")
        if(gateway.isEnabled) {
            String activeCard = DomainConstants.CARD_PAYMENT_PROCESSOR_CODE[activeCreditCardGateway]
            if(!activeCard) {
                return false
            }
            List metaList = PaymentGatewayMeta.findAllByFieldFor(activeCard)
            String type = metaList.find {it.name == "type"}?.value
            def apiEnable = type == "API" || type == "DIRECT" || type == "merchant"
            if(!apiEnable) {
                return false
            }
            return metaList.find {it.name == "enableWallet" && it.value == "true"} != null
        } else {
            return false
        }
    }

    @NotTransactional
    def getCreditCardType(String cardNumber) {
        cardNumber = "" + cardNumber
        cardNumber = cardNumber.trim()
        cardNumber = cardNumber.replaceAll("\\D+", "")

        if(cardNumber.matches(("^4[0-9]{12}(?:[0-9]{3})?\$"))) {
            return DomainConstants.CREDIT_CARD_TYPES.VISA
        } else if(cardNumber.matches(("^5[1-5][0-9]{14}\$"))) {
            return DomainConstants.CREDIT_CARD_TYPES.MASTERCARD
        } else if(cardNumber.matches(("^3[47][0-9]{13}\$"))) {
            return DomainConstants.CREDIT_CARD_TYPES.AMEX
        } else if(cardNumber.matches(("^3(?:0[0-5]|[68][0-9])[0-9]{11}\$"))) {
            return DomainConstants.CREDIT_CARD_TYPES.DINERS
        } else if(cardNumber.matches(("^6(?:011|5[0-9]{2})[0-9]{12}\$"))) {
            return DomainConstants.CREDIT_CARD_TYPES.DISCOVER
        } else if(cardNumber.matches(("^(?:2131|1800|35\\d{3})\\d{11}\$"))) {
            return DomainConstants.CREDIT_CARD_TYPES.JCB
        } else if(cardNumber.matches(("^62[0-5]\\d{13,16}\$"))) {
            return DomainConstants.CREDIT_CARD_TYPES.UNION_PAY
        }

        return null
    }

    CreditCard saveCreditCard(Map params) {
        CreditCard card = params.id ? CreditCard.load(params.id) : new CreditCard()
        card.customer = Customer.load(AppUtil.loggedCustomer)
        card.cardType = getCreditCardType(params.cardNumber)

        card = HookManager.hook("before-${activeCreditCardGateway}-wallet-save", card, params)

        String cardNumber = params.cardNumber
        card.gatewayToken = Base64Coder.encode(card.gatewayToken)
        card.cardNumber = cardNumber.substring(0, 4) + " XXXX XXXX - " + cardNumber.substring(cardNumber.length() - 4)
        return card.save()
    }

    void removeCreditCard(Map params) {
        Customer customer = Customer.load(AppUtil.loggedCustomer)
        CreditCard card = CreditCard.findByCustomerAndId(customer, params.long("id"))
        card.delete()
    }
}
