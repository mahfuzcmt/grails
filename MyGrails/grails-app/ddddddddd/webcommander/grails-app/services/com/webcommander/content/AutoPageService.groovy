package com.webcommander.content

import com.webcommander.AutoGeneratedPage
import com.webcommander.AutoPageContent
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommonService
import com.webcommander.common.MetaTag
import com.webcommander.design.Layout
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

@Initializable
class AutoPageService {
    CommonService commonService;

    static void initialize() {
        //Checking AT3 with news
        HookManager.register("layout-delete-veto") { response, id ->
            int layoutCount = AutoGeneratedPage.createCriteria().count {
                eq("layout.id", id)
            }
            if(layoutCount) {
                response."auto.pages" = layoutCount
            }
            return response;
        }
        HookManager.register("layout-delete-veto-list") { response, id ->
            List<AutoGeneratedPage> pages = AutoGeneratedPage.createCriteria().list {
                eq("layout.id", id)
            }
            if(pages.size()) {
                response."auto.pages" = pages.collect { it.name }
            }
            return response;
        }
    }

    Integer getAutoGeneratedPagesCount(Map params) {
        return AutoGeneratedPage.count();
    }

    List<AutoGeneratedPage> getAutoGeneratedPages(GrailsParameterMap params) {
        def listMap = [max: params.max, offset: params.offset];
        return AutoGeneratedPage.createCriteria().list(listMap) {
            if(params.searchText) {
                like("name", "%${params.searchText.trim().encodeAsLikeText()}%");
            }
            order(params.sort ?: "name", params.dir ?: "asc")
        }
    }

    AutoGeneratedPage getAutoGeneratedPage(Long id) {
        return AutoGeneratedPage.get(id);
    }

    AutoGeneratedPage getAutoGeneratedPage(String name) {
        return AutoGeneratedPage.findByName(name);
    }

    @Transactional
    boolean saveAutoGeneratedPage(GrailsParameterMap params) {
        Long id = params.id.toLong(0);
        AutoGeneratedPage fixedPage = id ? AutoGeneratedPage.get(id) : new AutoGeneratedPage();
        fixedPage.title = params.title;
        fixedPage.isHttps = params.boolean("isHttps");
        fixedPage.disableGooglePageTracking = params.disableTracking.toBoolean()
        fixedPage.metaTags*.delete()
        fixedPage.metaTags = [];
        String[] tag_names = params.list("tag_name");
        String[] tag_values = params.list("tag_content");
        fixedPage.layout = Layout.get(params["layout-id"]);
        for (int i = 0; i < tag_names.size(); i++) {
            MetaTag metaTag = new MetaTag(name: tag_names[i], value: tag_values[i]).save();
            fixedPage.metaTags.add(metaTag)
        }
        fixedPage.save()
        if (!fixedPage.hasErrors()) {
            AppEventManager.fire("auto-page-saved", [fixedPage.id]);
            return true
        }
        return false
    }

    String getPageContent(String pageType) {
        return AutoPageContent.createCriteria().get {
            projections {
                property("body")
            }
            belong {
                eq("name", pageType)
            }
        }
    }
}
