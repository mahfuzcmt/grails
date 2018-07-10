package com.webcommander.plugin.filter.controllers.admin

import com.webcommander.authentication.annotations.License
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.filter.FilterGroup
import com.webcommander.plugin.filter.FilterGroupService
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.plugin.filter.FilterProfile
import com.webcommander.util.AppUtil
import grails.converters.JSON
import com.webcommander.plugin.filter.Filter
import com.webcommander.plugin.filter.FilterService


class FilterAdminController {

    CommonService commonService
    FilterService filterService
    FilterGroupService filterGroupService

    @License(required = "allow_filter_feature")
    def loadAppView() {
        params.max = params.max ?: "10";
        Integer count = filterService.getFilterCount(params);
        List<Filter> filters = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.max = max;
            params.offset = offset;
            filterService.getFilters(params);
        }
        render(view: "/plugins/filter/admin/filter/appView", model: [filters: filters, count: count]);
    }

    def isFilterUnique() {
        if (commonService.isUnique(FilterProfile, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }

    @License(required = "allow_filter_feature")
    def saveFilterProfile() {
        if (filterService.saveFilterProfile(params)) {
            render([status: "success", message: g.message(code: "filter.profile.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "filter.profile.could.not.save")] as JSON)
        }
    }

    @License(required = "allow_filter_feature")
    def deleteFilterProfile() throws Throwable {
        Long id = params.long("id");
        try {
            if (filterService.deleteFilterProfile(id)) {
                render([status: "success", message: g.message(code: "filter.profile.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "filter.profile.could.not.delete")] as JSON)
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    @License(required = "allow_filter_feature")
    def assignFilter() {
        if(filterService.assignFilter(params)) {
            render([status: "success", message: g.message(code: "filter.update.success")] as JSON)
        }else {
            render([status: "success", message: g.message(code: "filter.update.failed")] as JSON)
        }
    }

    def loadLeftPanel() {
        List<FilterProfile> profiles = filterService.getFilterProfiles([:])
        render(view: "/plugins/filter/admin/profile/leftPanel", model: [profiles: profiles])
    }

    def explorerView() {
        FilterProfile profile = params?.id ? FilterProfile.findById(params.id as Long) : null
        render(view: "/plugins/filter/admin/profile/explorerView", model: [profile: profile])
    }

    @License(required = "allow_filter_feature")
    def createProfileForm() {
        FilterProfile profile = params.id ? FilterProfile.get(params.id) : new FilterProfile()
        render view: "/plugins/filter/admin/profile/createProfile", model: [profile: profile]
    }

    def addFilterPopup() {
        Long profileId = params?.profile_id ? params.profile_id.toLong() : null
        FilterProfile profile = profileId ? FilterProfile.get(profileId) : null

        List<Filter> filters = Filter.getAll()
        List<FilterGroup> filterGroups = filterGroupService.getFilterGroups([isActive: true]);

        render(view: "/plugins/filter/admin/filter/createPopup", model: [filters: filters, filterGroups: filterGroups, profile: profile])
    }

    @License(required = "allow_filter_feature")
    def setDefaultFilterProfile() {
        if (filterService.setDefaultFilterProfile(FilterProfile.load(params.id))) {
            render([status: "success", message: g.message(code: "filter.profile.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "filter.profile.could.not.save")] as JSON)
        }
    }

    @License(required = "allow_filter_feature")
    def copyFilterProfile() {
        Long id = params?.id ? params.id as Long : null
        if(filterService.cloneFilterProfile(id)) {
            render([status: "success", message: g.message(code: "filter.profile.clone.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "filter.profile.clone.error")] as JSON)
        }
    }

    @License(required = "allow_filter_feature")
    def removeFilterFromProfile() {
        Long profileId = params?.profileId ? params.profileId as Long : null
        Long filterId = params?.filterId ? params.filterId as Long : null
        if (filterService.deleteFilterFromProfile(profileId, filterId)) {
            render([status: "success", message: g.message(code: "filter.remove.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "filter.remove.failed")] as JSON)
        }
    }

    @License(required = "allow_filter_feature")
    def removeFilterGroupFromProfile() {
        Long profileId = params?.profileId ? params.profileId as Long : null
        Long filterGroupId = params?.filterGroupId ? params.filterGroupId as Long : null
        if (filterService.deleteFilterGroupFromProfile(profileId, filterGroupId)) {
            render([status: "success", message: g.message(code: "filter.remove.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "filter.remove.failed")] as JSON)
        }
    }

    /* functions need to review for removal */
    def loadFilterSetting () {
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.FILTER_PAGE)
        render (view: "/plugins/filter/admin/setting/filterSetting", model: [config: config])
    }
}
