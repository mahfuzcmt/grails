package com.webcommander.controllers.admin

import com.webcommander.admin.TrashService
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import grails.converters.JSON
import grails.util.Holders

class TrashController {
    CommonService commonService

    @Restriction(permission = "trash.view.list")
    def loadAppView() {
        int max = params.max = params.int("max")?: 10;
        int offset = params.offset = params.int("offset")?: 0;
        def effectiveDomains;
        Long allTotal = 0
        def trashList = [:];
        effectiveDomains = params.selectedDomain ? [params.selectedDomain] : TrashService.domains.collect {it.key}.sort();
        if (params.sort  && params.dir == "desc") {
            effectiveDomains.sort { a,b ->
                return b <=> a;
            }
        }
        int paramMax = params.max, paramOffset = params.offset;
        effectiveDomains.each {
            def domain = it;
            def service = Holders.applicationContext.getBean(TrashService.domains[domain]);

            domain += "s";
            if(domain.endsWith("ys")) {
                domain = domain.replaceAll(/ys$/, "ies");
            } else if(domain.endsWith("xs")) {
                domain = domain.replaceAll(/xs$/, "xes");
            }
            Long elementCount = service.getClass().getMethod("count" + domain + "InTrash", Map as Class[]).invoke(service, [params] as Object[]);
            allTotal += elementCount;

            params.sort = params.sort ?: "name"
            params.dir = params.dir ?: "asc"
            if(max != 0){
                if(allTotal > offset) {
                    def elementList;
                    if(params.searchText || params.advanceSearch){
                        params.max = max;
                        params.offset = offset;
                        elementList = service.getClass().getMethod("get" + domain + "InTrash", Map as Class[]).invoke(service, [params] as Object[]);
                    }else {
                        elementList = service.getClass().getMethod("get" + domain + "InTrash", [int.class, int.class, String.class, String.class] as Class[]).invoke(service,[offset, max, params.sort, params.dir] as Object[]);
                    }
                    trashList.putAll(elementList);
                    max -= elementList[it]? elementList[it].size() : 0;
                }
                offset -= elementCount;
                if(offset<0){
                    offset = 0;
                }
            }
        }
        params.max = paramMax
        params.offset = paramOffset
        def domains = TrashService.domains.collect {it.key}.sort()
        if(!AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce").toBoolean()) {
            domains.remove("Category")
            domains.remove("Product")
        }
        render view: "/admin/trash/appView", model: [domains: domains, trashMap: trashList, count: allTotal, domainNames: TrashService.domainNames];
    }

    @Restriction(permission = "trash.restore")
    def restore() {
        Long id = params.long("id")?:0
        boolean hasParent = params.boolean("hasParent")
        try{
            if(params.type) {
                def type = params.type
                def service = Holders.applicationContext.getBean(TrashService.domains[type]);
                AppEventManager.fire("before-" + type.toLowerCase() + "-restore", [id])
                boolean success
                if(hasParent) {
                    success = service.getClass().getDeclaredMethod("restore" + type + "FromTrash" , [Long.class, Boolean.class] as Class[]).invoke(service, [id, hasParent] as Object[])
                } else {
                    success = service.getClass().getDeclaredMethod("restore" + type + "FromTrash" , [Long.class] as Class[]).invoke(service, [id] as Object[])
                }
                if(success) {
                    AppEventManager.fire(type.toLowerCase() + "-restore", [id])
                    render([status: "success", message: g.message(code: "restore.successful", args: [params.type, params.name])] as JSON)
                } else {
                    render([status: "error", message: g.message(code: "restore.error",args: [params.type, params.name])] as JSON)
                }
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    @Restriction(permission = "trash.restore")
    def restoreSelectedItems() {
        def success = false
        def types = params.list("types");
        Integer l = params.list("ids").size();
        Integer i;
        for(i=0; i<l; i++) {
            Long id =  l == 1 ? params.ids.toLong(0) : params.ids[i].toLong(0)
            def type = types[i]
            def service = Holders.applicationContext.getBean(TrashService.domains[type]);
            AppEventManager.fire("before-" + type.toLowerCase() + "-restore", [id])
            if(service.getClass().getDeclaredMethod("restore" + type + "FromTrash" , [Long.class] as Class[]).invoke(service, [id] as Object[])){
                AppEventManager.fire(type.toLowerCase() + "-restore", [id])
                success = true
            }
        }
        if(success) {
            render([status: "success", message: g.message(code: "selected.restore.successful")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "selected.restore.error")] as JSON)
        }
    }

    @Restriction(permission = "trash.remove")
    def delete() {
        Long id = params.id.toLong(0);
        def type = params.type
        def service = Holders.applicationContext.getBean(TrashService.domains[type])
        AppEventManager.fire("before-" + type.toLowerCase() + "-delete", [id])
        if(service.getClass().getDeclaredMethod("delete" + type, [Long.class] as Class[]).invoke(service, [id] as Object[])) {
            AppEventManager.fire(type.toLowerCase() + "-delete", [id])
            render([status: "success", message: g.message(code: "trash.item.delete.successful", args: [params.type, params.name])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "trash.item.delete.error", args: [params.type, params.name])] as JSON)
        }
    }

    @Restriction(permission = "trash.remove")
    def deleteSelectedItems() {
        def success = false
        def types = params.list("types");
        Integer l = params.list("ids").size();
        Integer i;
        try {
            for(i=0; i<l; i++) {
                Long id =  l == 1 ? params.ids.toLong(0) : params.ids[i].toLong(0)
                def type = types[i]
                def service = Holders.applicationContext.getBean(TrashService.domains[type])
                AppEventManager.fire("before-" + type.toLowerCase() + "-delete", [id])
                if(service.getClass().getDeclaredMethod("delete" + type, [Long.class] as Class[]).invoke(service, [id] as Object[])) {
                    AppEventManager.fire(type.toLowerCase() + "-delete", [id])
                    success = true
                }
            }
        } catch(Throwable r) {}
        if(success) {
            render([status: "success", message: g.message(code: "selected.delete.successful")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "selected.delete.error")] as JSON)
        }
    }

    def advanceFilter(){
        render(view: "/admin/trash/filter", model: [domains: TrashService.domains.collect {it.key}.sort()]);
    }
}
