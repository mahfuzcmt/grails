package com.webcommander.controllers.admin.content

import com.webcommander.Page
import com.webcommander.admin.ConfigService
import com.webcommander.admin.StoreService
import com.webcommander.authentication.annotations.License
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.common.CommonService
import com.webcommander.config.StoreDetail
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.content.PageService
import com.webcommander.design.Layout
import com.webcommander.design.Resolution
import com.webcommander.license.blocker.PageLicense
import com.webcommander.manager.HookManager
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import grails.converters.JSON

class PageAdminController {
    CommonService commonService
    PageService pageService
    ConfigService configService
    StoreService storeService

    @Restriction(permission = "page.view.list")
    def loadAppView() {
        Integer count = pageService.getPagesCount(params)
        params.max = params.max ?: "10"
        params.offset = params.offset ?: "0"
        params.isNotAssignedInStore = true
        List<Page> pages = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            pageService.getPages(params)
        }
        String landingPage = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "landing_page")
        Map childPages = storeService.childPagesMap()
        render(view: "/admin/page/appView", model: [count: count, pages: pages, landingPage: landingPage, childPages: childPages])
    }

    @Restrictions([
        @Restriction(permission = "page.create", params_not_exist = "id"),
        @Restriction(permission = "page.edit.properties", params_exist = "id", entity_param = "id", domain = Page, owner_field = "createdBy")
    ])
    @License(required = "page_limit", checker = PageLicense.Edit)
    def edit() {
        Long defaultLayoutId = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "default_layout").toLong(0)
        Page page = params.id ? pageService.getPage(params.long("id")) : new Page(layout: Layout.get(defaultLayoutId))
        if (page == null) {
            throw new ApplicationRuntimeException("page.not.found")
        }
        Map model = [:]
        Boolean enabled = (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "enable_multi_model")) == "true" ? true : false
        if(enabled && params.addPageForStore) {
            List<StoreDetail> stores = storeService.getUnassignedStoreForPage(params, page);
            model.stores = stores
            params.storeId ? model.store = StoreDetail.findById(params.storeId) : ""
            model.isMultiModelEnabled = enabled
        }
        model.page = page
        render(view: "/admin/page/infoEdit", model: model)
    }

    def listCustomerAndGroups() {
        Page pg = Page.get(params.long("id") ?: 0)
        if (params.id) {
            render(view: "/admin/common/customerAndGroupSelection", model: [customers: pg.customers, customerGroups: pg.customerGroups])
        } else {
            render(view: "/admin/common/customerAndGroupSelection")
        }
    }

    def view() {
        Long id = params.long('id')
        Page page = Page.get(id)
        render(view: "/admin/page/infoView", model: [page: page])
    }

    @Restriction(permission = "page.remove", entity_param = "id", domain = Page, owner_field = "createdBy")
    def delete() {
        Long id = params.int('id')
        try {
            if (pageService.putPageInTrash(id, params.at2_reply, params.at1_reply)) {
                render([status: "success", message: g.message(code: "page.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "page.delete.failure")] as JSON)
            }
        } catch (AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    @Restriction(permission = "page.remove", entity_param = "ids", domain = Page, owner_field = "createdBy")
    def deleteSelected() {
        List<Long> ids = params.list("ids").collect {it.toLong(0)}
        def total = pageService.putSelectedPagesInTrash(ids)
        if(total == ids.size()){
            render([status: "success", message: g.message(code: "selected.pages.delete.success")] as JSON)
        } else if(total == 0) {
            render([status: "error", message: g.message(code: "selected.pages.could.not.delete")] as JSON)
        } else {
            render([status: "warning", message: g.message(code: "selected.not.deleted", args: [ids.size() - total, ids.size(), g.message(code: "page(s)")])] as JSON)

        }
    }

    @Restrictions([
        @Restriction(permission = "page.create", params_not_exist = "id"),
        @Restriction(permission = "page.edit.properties", params_exist = "id", entity_param = "id", domain = Page, owner_field = "createdBy")
    ])
    @License(required = "page_limit", checker = PageLicense.Save)
    def save() {
        params.remove("action")
        params.remove("controller")
        if(params.deleteTrashItem){
            def field = params.deleteTrashItem.collect{it.key}[0]
            def value = params[field]
            pageService.deleteTrashItemAndSaveCurrent(field, value)
        }
        if (pageService.save(params, session.admin)) {
            render([status: "success", message: g.message(code: "page.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "page.save.failure")] as JSON)
        }
    }

    def advanceFilter() {
        render(view: "/admin/page/filter", model: [get: 0])
    }

    @Restriction(permission = "page.create")
    @License(required = "page_limit", checker = PageLicense.Copy)
    def copy() {
        Long id = params.long('id')
        if (pageService.copy(id, session.admin)) {
            render([status: "success", message: g.message(code: "page.copy.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "page.copy.failure")] as JSON)
        }
    }

    @Restriction(permission = "page.create")
    @License(required = "page_limit", checker = PageLicense.Copy)
    def copySelected() {
        if (pageService.copySelected(params.list("ids").collect{ it.toLong() }, AppUtil.session.admin)) {
            render([status: "success", message: g.message(code: "selected.pages.copy.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "selected.pages.could.not.copy")] as JSON)
        }
    }

    def setLandingPage() {
        String url = params.url
        def config = [
            [type: DomainConstants.SITE_CONFIG_TYPES.GENERAL,
            configKey: "landing_page",
            value: url]
        ]
        if (configService.update(config)) {
            render([status: "success", message: g.message(code: "page.set.as.landing")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "page.not.set.as.landing")] as JSON)
        }
    }

    def isUnique() {
        render(commonService.responseForUniqueField(Page, params.long("id"), params.field, params.value) as JSON)
    }

    def restoreFromTrash() {
        def field = params.field
        def value = params.value
        Long id = pageService.restorePageFromTrash(field, value)
        if(id) {
            render([status: "success", message: g.message(code: "restored.successfully", args: ["Page"]), type: "page", id: id] as JSON)
        }
    }

    @Restriction(permission = "page.edit.content", entity_param = "id", domain = Page, owner_field = "createdBy")
    def editPageContent() {
        List favoriteWidgets = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "favorite_widgets")?.split(",") ?: []
        List widgets = new ArrayList<>(DomainConstants.WIDGET_TYPE.values())
        for (String w : favoriteWidgets) {
            widgets.remove(w)
        }
        widgets = HookManager.hook("available-widgets", widgets)
        List<Resolution> resolutions = Resolution.list()
        Long id = params.long("id")
        String editorUrl = app.relativeBaseUrl() + "pageAdmin/renderPage?id=" + id + "&editMode=true&section=body"
        Page page = Page.get(params.long("id"))
        Long layoutId = page.layout?.id
        render(view: "/admin/contentEditor/editContent", model: [id: id, isPage: true, widgets: widgets, favoriteWidgets: favoriteWidgets, widgetLabels: NamedConstants.WIDGET_MESSAGE_KEYS,
            resolutions: resolutions, editorUrl: editorUrl, layoutId: layoutId, page: page, pageFlag: true, widgetLicense: NamedConstants.WIDGET_LICENSE])
    }

    def editJs() {
        Page page = Page.get(params.containerId.toLong(0))
        String containerJs = params.containerJs ?: page.js
        render(view: "/admin/layout/jsEditor", model: [containerJs: containerJs])
    }

    def changeLayout() {
        String pageHtml = g.include(controller: 'pageAdmin', action: 'editPageContent', params: [id: params.long("pageId"), layoutId: params.layoutId, editMode: "true"])
        render([status: "success", html: pageHtml] as JSON)
    }

    def renderPage() {
        Page page = Page.get(params.long("id"))
        request.page = page
        Boolean editMode = params.editMode == "true" ? true : false
        render(view: "/layouts/sitepage", model: [page: page, editMode: editMode])
    }

    def loadStatusOption() {
        render view: "/admin/page/statusOption"
    }

    def changeStatus() {
        List<Long> ids = params.list("id")*.toLong()
        Boolean isActive = params.active == "true"
        if(pageService.changeStatus(ids, isActive)) {
            render([status: "success", message: g.message(code: "page.update.success")] as JSON)
        }else {
            render([status: "success", message: g.message(code: "page.update.failed")] as JSON)
        }
    }

    @Restriction(permission = "page.create")
    @License(required = "page_limit", checker = PageLicense.Copy)
    def addPageForStore() {
        params.remove("action")
        params.remove("controller")
        Long id = params.long('id')
        if (pageService.addPageForStore(id, params)) {
            render([status: "success", message: g.message(code: "add.page.for.store.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "add.page.for.store.failure")] as JSON)
        }
    }
}
