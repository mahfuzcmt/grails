package com.webcommander.plugin.embedded_page.controllers.admin

import com.webcommander.authentication.annotations.License
import com.webcommander.Page
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.Layout
import com.webcommander.manager.HookManager
import com.webcommander.plugin.embedded_page.EmbeddedPage
import com.webcommander.plugin.embedded_page.EmbeddedPageService
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import grails.converters.JSON

class EmbeddedPageController {

    CommonService commonService
    EmbeddedPageService embeddedPageService

    @License(required = "allow_embedded_page_feature")
    def loadAppView() {
        params.max = params.max ?: "10"
        Integer count = embeddedPageService.getPageCount(params);
        List<EmbeddedPage> pages = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.max = max;
            params.offset = offset;
            embeddedPageService.getPages(params);
        }
        render(view: "/plugins/embedded_page/admin/appView", model: [pages: pages, count: count]);
    }

    def editJs() {
        EmbeddedPage page = EmbeddedPage.get(params.containerId.toLong(0))
        String containerJs = params.containerJs ?: page.js
        render(view: "/admin/layout/jsEditor", model: [containerJs: containerJs])
    }

    @License(required = "allow_embedded_page_feature")
    def edit() {
        Long id = params.id.toLong(0)
        EmbeddedPage page = id ? EmbeddedPage.get(id) : new EmbeddedPage()
        render(view: "/plugins/embedded_page/admin/infoEdit", model: [page: page])
    }

    @License(required = "allow_embedded_page_feature")
    def save() {
        if(embeddedPageService.save(params)) {
            render([status: "success", message: g.message(code: "embedded.page.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "embedded.page.save.failed")] as JSON)
        }
    }

    def delete() {
        try {
            boolean deleted = embeddedPageService.delete(params.long("id"), params.at2_reply, params.at1_reply)
            if (deleted) {
                render([status: "success", message: g.message(code: "embedded.page.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "embedded.page.delete.failed")] as JSON)
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def deleteSelected() {
        List<Long> ids = params.list("ids").collect {it.toLong(0)}
        if (embeddedPageService.removeSelected(ids)) {
            render([status: "success", message: g.message(code: "selected.embedded.page.remove.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "selected.embedded.page.remove.failed")] as JSON)
        }
    }

    def isUnique() {
        if (commonService.isUnique(EmbeddedPage, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }

    def renderEditorPage() {
        Long id = params.long("id")
        EmbeddedPage ePage = EmbeddedPage.get(id)
        Page page = new Page(body: ePage.body);
        page.layout = new Layout(body: "<wi:widget type=\"page\"/>", css: "> .header {display: none;} > .footer {display: none;}")
        request.page = page;
        request.embedded_page = id
        render(view: "/layouts/sitepage", model: [page: page, editMode: true]);
    }

    def editContent() {
        List favoriteWidgets = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "favorite_widgets")?.split(",") ?: []
        List widgets = new ArrayList<>(DomainConstants.WIDGET_TYPE.findAll {it.value != "sectionSlider" && it.value != "tabAccordion"}.values());
        for (String w : favoriteWidgets) {
            widgets.remove(w)
        }
        widgets = HookManager.hook("available-widgets", widgets)
        Long id = params.long("id")
        String editorUrl = app.relativeBaseUrl() + "embeddedPage/renderEditorPage?id=" + id + "&editMode=true"
        Map widgetLabels = NamedConstants.WIDGET_MESSAGE_KEYS.findAll { it.value != "section.slider" && it.value != "tab.accordion"}
        render(view: "/admin/contentEditor/editContent", model: [
                id: id, widgets: widgets, editorUrl: editorUrl, isPage: true, favoriteWidgets: favoriteWidgets,
                layoutId: "0", noResponsive: true, widgetLabels: widgetLabels, widgetLicense: NamedConstants.WIDGET_LICENSE
        ]) //layoutId is sent just to pretend to have a layout
    }

    def advanceFilter() {
        render(view: "/plugins/embedded_page/admin/filter")
    }
}
