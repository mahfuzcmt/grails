package com.webcommander.controllers.admin.design

import com.webcommander.Page
import com.webcommander.authentication.annotations.License
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.Layout
import com.webcommander.design.LayoutService
import com.webcommander.design.Resolution
import com.webcommander.design.WidgetService
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import com.webcommander.util.StringUtil
import com.webcommander.widget.Widget
import com.webcommander.widget.WidgetContent
import grails.converters.JSON
import grails.util.Holders
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import grails.web.databinding.DataBindingUtils

class LayoutController {

    LayoutService layoutService
    CommonService commonService
    WidgetService widgetService

    def loadAppView() {
        List<Layout> layoutList = layoutService.getLayouts(params)
        def count = layoutService.getLayoutCount(params)
        Long defaultLayout = StringUtil.autoCast(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "default_layout"))
        render(view: "/admin/layout/appView", model: [layoutList: layoutList, selected: Layout.first()?.id, count: count, defaultLayout: defaultLayout])
    }

    def loadLeftPanel() {
        List<Layout> layoutList = layoutService.getLayouts(params);
        Integer size = layoutList.size()
        if(params.boolean("new")) {
            Layout temp = layoutList.first()
            layoutList[0] = layoutList.last()
            layoutList[size-1] = temp
        }
        Integer selected = params.int("selected");
        Long defaultLayout = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "default_layout").toLong(0)
        render(view: "/admin/layout/leftPanel", model: [layoutList: layoutList, selected: selected, defaultLayout: defaultLayout]);
    }

    def copyLayout() {
        Boolean result = layoutService.copyLayout(params.long("layoutId"));
        if(result) {
            render([status: "success", message: g.message(code: "layout.copy.successful")] as JSON);
        } else {
            render([status: "error", message: g.message(code: "layout.copy.failure")] as JSON);
        }
    }

    @License(required = "allow_layout_feature")
    def createLayout() {
        try {
            Long savedId = layoutService.createLayout(params.name);
            if(savedId) {
                render([status: "success", message: g.message(code: "layout.create.successful"), id: savedId] as JSON);
            } else {
                render([status: "error", message: g.message(code: "layout.create.failure")] as JSON);
            }
        } catch (ApplicationRuntimeException e) {
            render([status: "error", message: e.message] as JSON);
        }
    }

    def deleteLayout() {
        try {
            Boolean result = layoutService.deleteLayout(params.long("id"), params.at2_reply, params.at1_reply);
            if(result) {
                render([status: "success", message: g.message(code: "layout.delete.successful")] as JSON);
            } else {
                render([status: "error", message: g.message(code: "layout.delete.failure")] as JSON);
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def viewAttachPages() {
        def pageList = layoutService.getAttachedPages(params.long("layoutId"));
        render(view: "/admin/layout/pageList", model: [pageList: pageList])
    }

    def rename() {
        Long id = params.long("layoutId")
        String newName = params.newName
        def layouts = Layout.createCriteria().list {
            if(id) {
                ne("id", id)
            }
            eq("name", newName)
        }
        if(layouts.size() > 0) {
            render([status: "error", message: g.message(code: "layout.name.already.exists")] as JSON);
        } else {
            if(layoutService.rename(id, newName)) {
                render([status: "success", message: g.message(code: "layout.rename.successful")] as JSON);
            } else {
                render([status: "error", message: g.message(code: "layout.rename.failure")] as JSON);
            }
        }
    }

    def isNameUnique() {
        String newName = params.value
        def layouts = Layout.createCriteria().list {
            eq("name", newName)
        }
        if(layouts.size() > 0) {
            render([status: "error", message: g.message(code: "layout.name.already.exists")] as JSON);
        } else {
            render([status: "success", message: ""] as JSON);
        }
    }

    def renderLayout() {
        Long layoutId = params.long("id");
        Layout layout;
        if(layoutId) {
            layout = Layout.get(layoutId);
        } else {
            layout = new Layout();
        }
        Page page = new Page(title: g.message(code: "page.title"))
        page.layout = layout;
        request.page = page;
        Boolean editMode = params.editMode == "true" ? true : false;
        render(view:"/layouts/sitepage", model:[page: page, editMode: editMode, isLayout: true]);
    }

    def editLayout() {
        List favoriteWidgets = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "favorite_widgets")?.split(",") ?: []
        List widgets = new ArrayList<>(DomainConstants.WIDGET_TYPE.values());
        for (String w : favoriteWidgets) {
            widgets.remove(w)
        }
        List<Resolution> resolutions = Resolution.list();
        Long id = params.long("id")
        widgets = HookManager.hook("available-widgets", widgets)
        String editorUrl = app.relativeBaseUrl() + "layout/renderLayout?id=" + id + "&editMode=true&section=" + params.section
        render(view: "/admin/contentEditor/editContent", model: [
                id: id, isLayout: true, widgets: widgets, favoriteWidgets: favoriteWidgets,
                widgetLabels: NamedConstants.WIDGET_MESSAGE_KEYS,
                section: params.section, resolutions: resolutions,
                editorUrl: editorUrl, widgetLicense: NamedConstants.WIDGET_LICENSE
        ]);
    }

    def editJs() {
        Layout layout = Layout.get(params.containerId.toLong(0))
        String containerJs = params.containerJs ?: layout?.js ?: ""
        render(view: "/admin/layout/jsEditor", model: [containerJs: containerJs])
    }

    def layoutList() {
        List<Layout> layouts = Layout.all;
        render(view: "/admin/layout/layoutList", model: [layouts: layouts]);
    }

    def isLayoutUnique() {
        if (commonService.isUnique(Layout, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }

    def isAdminLoggedIn() {
        render([status: session.admin ? "success" : "error"] as JSON)
    }

    def layoutSelectionView() {
        Long destId = params.id.toLong()
        String section = params.section
        List<Layout> layouts = Layout.createCriteria().list {
            ne("id", destId)
            sizeGt(section + "Widgets", 0)
            eq('isDisposable', false)
        }
        render(view: "/admin/layout/layoutSelectorView", model: [layouts: layouts, section: section, destId: destId])
    }

    def cloneSection() {
        Layout layout = Layout.get(params.id)
        List<Widget> sectionWidgets = []
        Map wiCache = [:]
        Map wiCss = [:]
        layout[params.section+"Widgets"].each {
            Widget wi = it
            String newUuid = StringUtil.uuid
            Widget widget = new Widget()
            DataBindingUtils.bindObjectToInstance(widget, wi.properties, [], ["containerId", "uuid", "created", "updated", "widgetContent"], null)
            wi.widgetContent.each { cont ->
                WidgetContent content = new WidgetContent()
                DataBindingUtils.bindObjectToInstance(content, cont.properties, [], ["created", "updated", "widget"], null)
                content.widget = widget
                widget.addToWidgetContent(content)
            }
            widget.containerId = layout.id
            widget.uuid = newUuid
            widget.css = widget.css?.replaceAll(wi.uuid, newUuid)
            wiCss[newUuid] = widget.css
            wiCache[newUuid] = widget.serialize()
            sectionWidgets.push(widget)
            if(widget.widgetType == "image") {
                AppEventManager.off("widget-" + widget.uuid + "-before-save")
                AppEventManager.one("widget-" + widget.uuid + "-before-save", "session-" + session.id, { newWidget ->
                    Map params = JSON.parse(newWidget.params)
                    if(params.upload_type == "local" && newWidget.content) {
                        String modifiedLocalUrl = "${appResource.getWidgetRelativeUrl(uuid: newWidget.uuid, type: 'image')}${FilenameUtils.getName(newWidget.content)}"
                        String targetFileUrl = "${appResource.getWidgetRelativeUrl(uuid: newWidget.uuid, type: 'image')}"
                        String sourceFileUrl = "${appResource.getWidgetRelativeUrl(uuid:  wi.uuid, type: 'image')}"
                        File targetFile = new File(Holders.servletContext.getRealPath(targetFileUrl))
                        targetFile.parentFile.mkdirs()
                        File sourceFile = new File(Holders.servletContext.getRealPath(sourceFileUrl))
                        if(sourceFile.exists()) {
                            FileUtils.copyDirectory(sourceFile, targetFile)
                        }
                        newWidget.content = app.customResourceBaseUrl() + modifiedLocalUrl
                    }
                })
            }
        }
        def sectionHtml = g.include(view: "/admin/layout/sectionCopy.gsp", model: [widgets: sectionWidgets])
        sectionWidgets*.discard()

        render([status: "success", name: layout.name, section: sectionHtml, css: layout.css, wiCss: wiCss, wiCache: wiCache] as JSON);
    }

}
