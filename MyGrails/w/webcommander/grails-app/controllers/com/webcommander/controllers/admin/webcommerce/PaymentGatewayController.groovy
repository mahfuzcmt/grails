package com.webcommander.controllers.admin.webcommerce

import com.webcommander.admin.Zone
import com.webcommander.admin.ZoneService
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.webcommerce.PaymentGateway
import com.webcommander.webcommerce.PaymentGatewayMeta
import com.webcommander.webcommerce.PaymentGatewayService
import com.webcommander.webcommerce.SurchargeRange
import grails.converters.JSON

class PaymentGatewayController {

    CommonService commonService
    PaymentGatewayService paymentGatewayService
    ZoneService zoneService

    @Restriction(permission = "payment_gateway.view.list")
    def loadAppView() {
        params.max = params.max ?: "10";
        Integer count = paymentGatewayService.getPaymentGatewayCount(params);
        List<PaymentGateway> paymentGateways = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.max = max;
            params.offset = offset;
            paymentGatewayService.getPaymentGateways(params);
        }
        render(view: "/admin/paymentGateway/appView", model: [paymentGateways: paymentGateways, count: count])
    }

    def config() {
        PaymentGateway paymentGateway = PaymentGateway.findById(params.id)
        def surchargeTypes = DomainConstants.SURCHARGE_TYPE.values()
        def fieldMap = [ ]
        List<Zone> zoneList = zoneService.getZones([
                isDefault: false
        ])
        render(
            view: "/admin/paymentGateway/paymentGatewayConfig",
            model: [
                surchargeType: surchargeTypes,
                surchargeDetailsList: SurchargeRange.list(),
                surchargeView: "/admin/paymentGateway/editSurcharge.gsp",
                paymentGateway: paymentGateway,
                zoneList: zoneList,
                fields: fieldMap
            ]
        );
    }

    def update() {
        if (paymentGatewayService.update(params)) {
            render([status: "success", message: g.message(code: "payment.gateway.update.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "payment.gateway.update.error")] as JSON)
        }
    }

    def paymentProcessorFields() {
        List fieldsForProcessor = PaymentGatewayMeta.findAllByFieldFor(params.gateway);
        render (
            view: "/admin/paymentGateway/paymentGatewayMetaConfig",
            model: [
                fields: fieldsForProcessor
            ]
        )
    }

    def loadSurchargeDetails() {
        def surchargeRangeList = PaymentGateway.findById(params.id).surchargeRange;
        render (
            view: "/admin/paymentGateway/editSurcharge",
            model: [
                surchargeRangeList: surchargeRangeList
            ]
        )
    }

    def loadStatusOption() {
        render view: "/admin/paymentGateway/statusOption";
    }

    def changeStatus() {
        List<Long> ids = params.list("id")*.toLong();
        Boolean status = params.status == "true";
        if(paymentGatewayService.changeStatus(ids, status)) {
            render([status: "success", message: g.message(code: "payment.gateway.update.success")] as JSON)
        }else {
            render([status: "success", message: g.message(code: "payment.gateway.update.error")] as JSON)
        }
    }
}
