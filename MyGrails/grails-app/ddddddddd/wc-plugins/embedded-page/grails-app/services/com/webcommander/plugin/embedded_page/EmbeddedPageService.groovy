package com.webcommander.plugin.embedded_page

import com.webcommander.Page
import com.webcommander.admin.Operator
import com.webcommander.annotations.Initializable
import com.webcommander.constants.DomainConstants
import com.webcommander.design.Layout
import com.webcommander.events.AppEventManager
import com.webcommander.installation.template.TemplateContent
import com.webcommander.models.blueprints.DisposableUtilServiceModel
import com.webcommander.task.MultiLoggerTask
import com.webcommander.task.TaskService
import com.webcommander.util.AppUtil
import com.webcommander.util.TrashUtil
import com.webcommander.widget.Widget
import grails.gorm.transactions.Transactional
import grails.util.TypeConvertingMap
import grails.web.servlet.mvc.GrailsParameterMap
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@Transactional
@Initializable
class EmbeddedPageService implements DisposableUtilServiceModel {

    static {
        AppEventManager.on("embedded-update", { id ->
            EmbeddedPage page = EmbeddedPage.get(id)
            page.isDisposable = false
            page.save()
        })
    }

    TaskService taskService
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g


    static void initialize() {
        def dockDelete = { sections ->
            sections.collectEntries {[(it.id): it]}.each {
                AppEventManager.fire("before-dock-section-delete", [it.key])
                it.value.delete()
                AppEventManager.fire("dock-section-delete", [it.key])
            }
        }
        AppEventManager.on("before-layout-delete", { id ->
            Layout layout = Layout.get(id);
            dockDelete(layout.dockableSections);
            layout.dockableSections.clear()
        })
        AppEventManager.on("before-page-delete", {id ->
            Page page = Page.get(id)
            dockDelete(page.dockableSections)
            page.dockableSections.clear()
        })

        AppEventManager.on("before-operator-delete", { id ->
            EmbeddedPage.executeUpdate("update EmbeddedPage s set s.createdBy = null where s.createdBy.id = :uid", [uid: id])
        });
        AppEventManager.on("before-template-install", {
            List<TemplateContent> contents = TemplateContent.createCriteria().list {
                eq("contentType", DomainConstants.WIDGET_CONTENT_TYPE.EMBEDDED_PAGE)
            }
            if(contents) {
                EmbeddedPage.where {
                    inList("id", contents.contentId)
                }.updateAll(isDisposable: true)
                contents*.delete()
            }
        });
    }

    private Closure getCriteriaClosure(TypeConvertingMap params) {
        return {
            if(params.searchText || params.name) {
                String name = params.searchText ?: params.name
                ilike("name", "%${name.trim().encodeAsLikeText()}%")
            }
            if (params.createdBy) {
                eq("createdBy.id", params.createdBy.toLong())
            }
            if(params.isDisposable != "true") {
                eq("isDisposable", false)
            } else {
                eq("isDisposable", true)
            }
        }
    }

    public Integer getPageCount(TypeConvertingMap params) {
        return EmbeddedPage.createCriteria().count {
            and getCriteriaClosure(params)
        }
    }

    public List<EmbeddedPage> getPages(TypeConvertingMap params) {
        def listMap = [max: params.max, offset: params.offset]
        return EmbeddedPage.createCriteria().list(listMap) {
            and getCriteriaClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc")
        }
    }

    public Boolean save(GrailsParameterMap params) {
        Long id = params.id.toLong(0)
        EmbeddedPage page = id ? EmbeddedPage.get(id) : new EmbeddedPage()
        page.name = params.name
        page.domId = params.domId
        if(!id) {
            page.createdBy = Operator.get(AppUtil.session.admin)
        }
        page.save()
        if(page.hasErrors()) {
            return false
        }
        if(id) {
            AppEventManager.fire("embedded-update", [id])
        }
        return true
    }

    boolean delete(Long id, String at2_reply, String at1_reply) {
        TrashUtil.preProcessFinalDelete("embedded-page", id, at2_reply != null, at1_reply != null)
        AppEventManager.fire("before-embedded-page-delete", [id, at1_reply])
        EmbeddedPage page = EmbeddedPage.proxy(id)
        Widget.createCriteria().list {
            eq "containerType", "embedded"
            eq "containerId", id
        }.each {
            Long widgetId = it.id
            AppEventManager.fire("before-widget-delete", [widgetId])
            it.delete()
            AppEventManager.fire("widget-delete", [widgetId])
            AppEventManager.fire(it.widgetType + "-widget-after-drop", [it])
        }
        page.delete()
        AppEventManager.fire("embedded-page-delete", [id])
        return true
    }

    def removeSelected(List ids) {
        boolean deleted = true;
        ids.each {
            deleted = delete(it, "yes", "include")
            if(!deleted){
                return false;
            }
        }
        return deleted;
    }

    @Override
    Integer countDisposableItems(String itemType) {
        return getPageCount(new TypeConvertingMap([isDisposable: "true"]))
    }

    @Override
    void removeDisposableItems(String itemType, MultiLoggerTask task) {
        EmbeddedPage.withNewSession {session ->
            List<EmbeddedPage> pages  = this.getPages(new TypeConvertingMap([isDisposable: "true"]))
            for (EmbeddedPage page : pages) {
                try {
                    this.delete(page.id, "yes", "include")
                    session.flush()
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.success("Embedded Page: $page.name", "")
                    task.meta.successCount++
                } catch (Exception e) {
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("Embedded Page: $page.name", e.message)
                    task.meta.errorCount++;
                }
            }
        }
    }
}
