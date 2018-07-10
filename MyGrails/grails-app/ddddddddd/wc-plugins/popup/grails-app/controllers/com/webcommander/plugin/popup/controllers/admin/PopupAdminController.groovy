package com.webcommander.plugin.popup.controllers.admin

import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.PluginManager
import com.webcommander.plugin.PluginManager
import com.webcommander.plugin.popup.Popup
import com.webcommander.plugin.popup.PopupService
import com.webcommander.plugin.popup.constants.Constants
import com.webcommander.util.AppUtil
import grails.converters.JSON

class PopupAdminController {
    CommonService commonService
    PopupService popupService

    @Restriction(permission = "popup.view.list")
    def loadAppView() {
        params.max = params.max ?: "10";
        Integer count = popupService.getPopupCount(params);
        List<Popup> popups = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.max = max;
            params.offset = offset;
            popupService.getPopups(params);
        }
        render(view: "/plugins/popup/admin/loadAppView", model: [popups: popups, count: count]);
    }

    @Restrictions([
            @Restriction(permission = "popup.create", params_not_exist = "id"),
            @Restriction(permission = "popup.edit", params_exist = "id")
    ])
    def infoEdit() {
        Popup popup = params.id ? Popup.get(params.id) : new  Popup()
        Map contentTypes = Constants.CONTENT_TYPE.clone()
        if(PluginManager.isInstalled("snippet")) {
            contentTypes["snippet"] = "snippet"
        }
        render(view: "/plugins/popup/admin/infoEdit", model: [popup: popup, contentTypes: contentTypes])
    }

    @Restrictions([
            @Restriction(permission = "popup.create", params_not_exist = "id"),
            @Restriction(permission = "popup.edit", params_exist = "id")
    ])
    def save() {
        if(popupService.save(params)) {
            render([status: "success", message: g.message(code: "popup.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "popup.save.failure")] as JSON)
        }
    }

    @Restriction(permission = "popup.remove", params_not_exist = "id")
    def delete() {
        if(popupService.delete(params.long("id"))) {
            render([status: "success", message: g.message(code: "popup.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "popup.delete.failure")] as JSON)
        }
    }

    @Restriction(permission = "popup.remove", params_not_exist = "id")
    def deleteSelected() {
        List<Long> ids = []
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        int deleteCount = popupService.deleteSelected(ids);
        int total = ids.size();
        if (deleteCount == total) {
            render([status: "success", message: g.message(code: "selected.popups.delete.success")] as JSON)
        } else if(deleteCount == 0) {
            render([status: "error", message: g.message(code: "selected.popups.could.not.delete")] as JSON)
        } else {
            render([status: "warning", message: g.message(code: "selected.not.deleted", args: [total - deleteCount, total, g.message(code: "popup")])] as JSON)
        }
    }

    def isUnique() {
        render(commonService.responseForUniqueField(Popup, params.long("id"), params.field, params.value) as JSON)
    }

    def loadConfig() {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.POPUP)
        config.config_version = new Date().time
        render(view: "/plugins/popup/admin/config", model: [config: config])
    }

    def advanceFilter() {
        render view: "/plugins/popup/admin/filter"
    }
}
