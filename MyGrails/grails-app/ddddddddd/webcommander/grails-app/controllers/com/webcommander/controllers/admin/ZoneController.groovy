package com.webcommander.controllers.admin

import com.webcommander.admin.AdministrationService
import com.webcommander.admin.Zone
import com.webcommander.admin.ZoneService
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.PaymentGateway
import com.webcommander.webcommerce.ShippingRule
import com.webcommander.webcommerce.TaxRule
import grails.converters.JSON

class ZoneController {
    ZoneService zoneService;
    CommonService commonService;
    AdministrationService administrationService

    @Restriction(permission = "zone.view.list")
    def loadAppView() {
        Integer count = zoneService.getZoneCount(params)
        params.max = params.max ?: "10";
        List<Zone> zones = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.max = max;
            params.offset = offset
            params.isDefault = false
            zoneService.getZones(params)
        }
        render(view: "/admin/zone/appView", model: [count: count, zones: zones])
    }

    def loadZoneView() {
        List<Zone> zones = Zone.all?.sort {-it.id};
        render(view: "/admin/zone/zoneView", model: [zones: zones ?: [] ]);
    }

    def create() {
        render(view: "/admin/zone/createZone")
    }

    def save() {
        def zoneParam = params.zone;
        Long id = params.long("id");
        def result = zoneService.saveZone(zoneParam, id);
        if (result) {
            if(params.addZone) {
                params.id = result.id
                forward(controller: "zone", action: "addZone");
            } else {
                render([status: "success", message: g.message(code: "zone.save.success")] as JSON)
            }
        } else {
            render([status: "error", message: g.message(code: "zone.save.failure")] as JSON)
        }
    }

    def delete() {
        Long id = params.long("id");
        try {
            if (zoneService.delete(id, params.at1_reply, params.at2_reply)) {
                render([status: "success", message: g.message(code: "zone.delete.success")] as JSON);
            } else {
                render([status: "error", message: g.message(code: "zone.delete.failure")] as JSON);
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def deleteSelected() {
        def ids = params.list("ids").collect{ it.toLong() };
        Integer count = 0;
        count += TaxRule.where {
            zones {
                inList("id", ids)
            }
        }.count();
        count += 0
        count += PaymentGateway.where {
            zone.id in ids
        }.count();
        if(count > 0){
            render([status: "alert", message: g.message(code: "selected.zone.delete.failure.association")] as JSON);
        } else {
            Boolean result = zoneService.deleteSelected(ids)
            if(result) {
                render([status: "success", message: g.message(code: "selected.zone.delete.success")] as JSON);
            } else {
                render([status: "error", message: g.message(code: "selected.zone.delete.failure")] as JSON);
            }
        }
    }

    def edit() {
        Zone zone = Zone.get(params.long("id"));
        def states = [];
        if(zone?.countries?.size() == 1) {
            states = administrationService.getStatesForCountry(zone.countries[0].id);
        }
        render(view: "/admin/zone/createZone", model: [states: states, zone: zone])
    }

    def createZone() {
        Zone zone = Zone.get(params.long("id"));
        def states = [];
        if(zone?.countries?.size() == 1) {
            states = administrationService.getStatesForCountry(zone.countries[0].id);
        }
        render(view: "/admin/zone/edit", model: [states: states, zone: zone])
    }

    def fields() {
        def states = administrationService.getStatesForCountry(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, 'default_country').toLong());
        render(view: "/admin/zone/create", model: [states: states, size: params.fieldClass ?: "large"])
    }

    def viewZone() {
        Zone zone = Zone.get(params.id);
        render(view: "/admin/zone/viewZone", model: [zone: zone]);
    }

    def viewTaxShippingRuleAndPaymentGateway() {
        Zone z = Zone.proxy(params.id);
        List<TaxRule> taxRules = TaxRule.where {
            zones {
                eq("id", z.id)
            }
        }.list();
        List<ShippingRule> shippingRules = null //todo
        List<PaymentGateway> paymentGateways = PaymentGateway.where {
            zone == z
        }.list();
        render(view: "/admin/zone/taxShippingRuleAndPaymentGateway", model: [taxRules: taxRules, shippingRules: shippingRules, paymentGateways: paymentGateways]);
    }

    def advanceFilter() {
        render(view: "/admin/zone/advanceSearchFilter", model: [d: false])
    }

    def isZoneUnique() {
        params['field'] = "name";
        if (commonService.isUnique(Zone, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }

    def addZone() {
        List<Zone> zones = Zone.createCriteria().list {
            if(params.id) {
                eq("id", params['id'].toLong())
            }
            if(params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
        }
        render(view: "/admin/zone/addZonePop", model: [zones: zones])
    }

    def loadZoneForSelection() {
        params.max = params.max ?: "10"
        Integer count = zoneService.getZoneCount(params)
        List<Zone> zones = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset;
            params.isDefault = false
            zoneService.getZones(params);
        }
        render(view: "/admin/zone/selectionPanel", model: [count: count, zones: zones]);
    }

    def zoneSelectionPopup() {
        List ids = params.list("zone");
        List<Zone> zones = ids ? zoneService.getZones([ids: ids]) : []
        render(view: "/admin/zone/selectionPopup", model: [fieldName: params.fieldName ?: "zone", zones: zones]);
    }

}
