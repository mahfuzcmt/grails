package com.webcommander.webcommerce

import com.webcommander.constants.DomainConstants
import com.webcommander.models.PaymentInfo
import com.webcommander.util.HttpUtil
import grails.gorm.transactions.Transactional

@Transactional
class PayPalService {
    String getQueryStringFromMap(Map params) {
        if(params.size() == 0) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        for (Map.Entry param: params.entrySet()) {
            buffer.append("&").append(param.key).append("=").append(param.value.encodeAsURL());
        }
        return buffer.substring(1);
    }

    public Boolean validatePayment(Map parameterMap) {
        String serviceUrl;
        String email
        def configs = PaymentGatewayMeta.findAllByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.PAYPAL)
        configs.each {
            if(it.name == "mode") {
                if (it.value == 'live') {
                    serviceUrl = DomainConstants.PPL_SERVICE_URL.LIVE;
                } else {
                    serviceUrl = DomainConstants.PPL_SERVICE_URL.TEST;
                }
            } else if(it.name == "emailAddress") {
                email = it.value
            }
        }
        String queryToSend = serviceUrl;
        if(parameterMap.containsKey("receiver_email")) {
            parameterMap['receiver_email'] = email;
        }
        if(parameterMap.containsKey("business")) {
            parameterMap['business'] = email;
        }
        String validateData = "cmd=_notify-validate&" + getQueryStringFromMap(parameterMap);
        String responseContent =  HttpUtil.doPostRequest(queryToSend, validateData, ["Content-Length": "" + validateData.getBytes().length])
        if (responseContent == "VERIFIED") {
            return true;
        }
        return false;
    }

    public PaymentInfo resolveInfo(Map params) {
        PaymentInfo info = new PaymentInfo();
        String status = params.payment_status == "Completed" ? DomainConstants.PAYMENT_STATUS.SUCCESS : (params.payment_status == "Pending" ?
                DomainConstants.PAYMENT_STATUS.PENDING : DomainConstants.PAYMENT_STATUS.FAILED)
        info.amount = params.mc_gross.toDouble()
        info.paymentRef = params.long("custom");
        info.success = status != DomainConstants.PAYMENT_STATUS.FAILED
        info.gatewayResponse = params.payment_status;
        info.payerInfo = params.payer_id
        info.trackInfo = params.txn_id
        return info
    }
}
