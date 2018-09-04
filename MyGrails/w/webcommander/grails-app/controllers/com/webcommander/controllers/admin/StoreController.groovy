package com.webcommander.controllers.admin

import com.webcommander.admin.AdministrationService
import com.webcommander.admin.StoreService
import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import grails.converters.JSON
import com.webcommander.config.StoreDetail
import org.springframework.web.multipart.MultipartFile

class StoreController {
    StoreService storeService
    AdministrationService administrationService

    def list() {
        params.max = params.max ?: 10;
        params.offset = params.offset ?: 0;
        Integer count = storeService.getCount(params);
        List<StoreDetail> stores = storeService.getAllStore(params);
        render(view: "/admin/setting/store/appView", model: [stores: stores, count: count])
    }

    def edit() {
        StoreDetail store = params.id ? StoreDetail.get(params.id) : new StoreDetail();
        def states = store?.address ? administrationService.getStatesForCountry(store.address.country.id) : []
        render(view: "/admin/setting/store/infoEdit", model: [store: store, states: states])
    }

    def save() {
        MultipartFile uploadedFile = params.image ?: null
        Boolean result = storeService.save(params, uploadedFile);
        if(result) {
            render([status: "success", message: g.message(code: "store.save.success")] as JSON)
        } else  {
            render([status: "error", message: g.message(code: "store.save.error")] as JSON)
        }
    }

    def delete() {
        Boolean result = storeService.delete(params.long("id"));
        if(result) {
            render([status: "success", message: g.message(code: "application.delete.success")] as JSON)
        } else  {
            render([status: "error", message: g.message(code: "application.delete.error")] as JSON)
        }
    }

    def isMultiModelEnabled() {
        Boolean enabled = (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "enable_multi_model")) == "true" ? true : false
        render([enabled: enabled, success: "success"] as JSON)
    }

    def setCurrentStore() {
        String identifier = params.identifier
        Boolean setCurrentStoreInSession = storeService.setCurrentStoreInSession(identifier)
        if(setCurrentStoreInSession) {
            render([status: "success", message: g.message(code: "current.store.set.in.session.success")] as JSON)
        } else  {
            render([status: "error", message: g.message(code: "current.store.set.in.session.error")] as JSON)
        }
    }

}
