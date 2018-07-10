package com.webcommander.plugin.filter

import com.webcommander.common.CommonService
import com.webcommander.events.AppEventManager
import com.webcommander.manager.PathManager
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.DomainUtil
import com.webcommander.webcommerce.Category
import grails.gorm.transactions.Transactional
import grails.web.databinding.DataBindingUtils
import grails.web.servlet.mvc.GrailsParameterMap
import org.hibernate.SessionFactory

class FilterService {
    CommonService commonService
    SessionFactory sessionFactory

    private Closure getCriteriaClosure(Map params) {
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.ids) {
                inList("id", params.list("ids").collect { it.toLong() })
            }
        }
    }

    Integer getFilterCount(Map params) {
        return Filter.createCriteria().count {
            and getCriteriaClosure(params)
        }
    }

    List<Filter> getFilters(Map params) {
        def listMap = [max: params.max, offset: params.offset];
        return Filter.createCriteria().list(listMap) {
            and getCriteriaClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc")
        }
    }

    Filter getFilter(Long id) {
        return Filter.get(id);
    }

    @Transactional
    boolean deleteFilter(Long id) {
        try {
            Filter filter = Filter.get(id);
            def profiles = FilterProfile.all
            profiles?.each {
                if (it.filters.contains(filter)) {
                    it.filters.remove(filter)
                    it.merge()
                }
            }
            filter.delete();
            File imgDir = new File(PathManager.getResourceRoot("filter/filter-${id}"))
            if (imgDir.exists()) {
                imgDir.deleteDir();
            }
            return true;
        } catch (Throwable t) {
            return false
        }
    }

    def deleteSelectedFilters(List<String> ids) {
        boolean deleted = true
        ids.each {
            deleted = deleteFilter(it)
            if (!deleted) {
                return false;
            }
        }
        return deleted;
    }

    def deleteSelectedFilterProfiles(List<String> ids) {
        List<FilterProfile> profiles = ids ? FilterProfile.findAllByIdInList(ids) : []
        profiles?.each {
            it.filters = []
        }
        try {
            profiles*.delete(flush: true)
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    boolean saveFilter(Map params, def uploadedFile) {
        Long id = params.id ? params.id.toLong(0) : null;

        Map properties = [
                name       : params.name,
                filterUrl  : params.filterUrl,
                description: params.description
        ];
        Filter filter = params.id ? Filter.proxy(id) : new Filter();
        if (params["remove-image"]) {
            properties.image = null;
        }
        filter.setProperties(properties)
        if (!commonService.isUnique(filter, "name")) {
            throw new ApplicationRuntimeException("filter.name.exists")
        }
        filter.url = commonService.getUrlForDomain(filter)
        filter.id ? filter.merge() : filter.save()
        if (!filter.hasErrors()) {
            if (uploadedFile?.originalFilename) {
                def filePath = AppUtil.session.servletContext.getRealPath("resources/filter/filter-${filter.id}");
                File f = new File(filePath);
                if (!f.exists()) {
                    f.mkdirs();
                }
                if (filter.image) {
                    List<File> prevImgFiles = [new File(filePath, filter.image), new File(filePath, "thumb-${filter.image}")];
                    prevImgFiles.each {
                        it.delete();
                    }
                }
                imageService.uploadImage(uploadedFile, filePath, NamedConstants.IMAGE_RESIZE_TYPE.FILTER_LOGO, uploadedFile.originalFilename, 2 * 1024 * 1024);
                filter.image = uploadedFile.originalFilename;
                filter.merge();
            }
            if (params["remove-image"]) {
                if (filter.id) {
                    File imgDir = new File(PathManager.getResourceRoot("filter/filter-${filter.id}"));
                    if (imgDir.exists()) {
                        imgDir.traverse {
                            it.delete();
                        }
                        imgDir.delete()
                    }
                }
            }
            return !filter.hasErrors()
        } else {
            return false;
        }
    }

    FilterProfile getFilterProfile(Long id) {
        return FilterProfile.get(id);
    }

    @Transactional
    boolean saveFilterProfile(Map params) {
        FilterProfile profile = params.id ? FilterProfile.get(params.id) : new FilterProfile();
        if (!profile) {
            return false
        }
        DataBindingUtils.bindObjectToInstance(profile, params.subMap(["name", "description"]))
        if (!commonService.isUnique(profile, "name")) {
            throw new ApplicationRuntimeException("filter.profile.name.in.use")
        }
        profile.isDefault = false
        if ((params.isDefault).equals("true") || !getDefaultFilterProfile()) {
            setDefaultFilterProfile(profile)
        }
        if (profile.id) {
            profile.merge()
        } else {
            profile.save()
        }
        if (!profile.hasErrors()) {
            if (params.id) {
                AppEventManager.fire("filter-profile-update", [params.id])
            }
            return true
        }
        return false
    }

    @Transactional
    def setDefaultFilterProfile(FilterProfile profile) {
        List<FilterProfile> filterProfiles = FilterProfile.createCriteria().list() {
            eq('isDefault', true)
        }
        filterProfiles.each {
            it.isDefault = false
            it.save()
        }
        profile.isDefault = true
        return profile.save()
    }

    FilterProfile getDefaultFilterProfile() {
        List<FilterProfile> filterProfiles = FilterProfile.createCriteria().list() {
            eq('isDefault', true)
        }

        return filterProfiles.size() > 0 ? filterProfiles.get(0) : null
    }

    Integer getFilterProfilesCount(Map params) {
        FilterProfile.createCriteria().count(filterProfileClosure(params))
    }

    private filterProfileClosure(Map params) {
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
        }
    }

    List<FilterProfile> getFilterProfiles(Map params) {
        FilterProfile.createCriteria().list(offset: params.offset, max: params.max) {
            and filterProfileClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }

    @Transactional
    boolean deleteFilterProfile(Long id) {
        FilterProfile profile = FilterProfile.proxy(id);
        profile.delete()
        return true;
    }

    @Transactional
    def saveFilterProfileForCategory(Map response, GrailsParameterMap parameters, Category category) {
        Long profileId = parameters?.filterProfile.isLong() ? parameters.filterProfile.toLong() : null
        FilterProfile profile = profileId ? FilterProfile.proxy(profileId) : null
        def filterProfiles = FilterProfile.all
        if (profile) {
            removeCategoryFromFilterProfile(filterProfiles, category)
            profile.addToCategories(category)
            profile.merge()
            sessionFactory.cache.evictAllRegions()
            if (!profile.hasErrors()) response.success = true
            else response.success = false
        } else {
            removeCategoryFromFilterProfile(
            filterProfiles,category)
            if(parameters.filterProfile.equals("default")) {
                FilterProfile defaultProfile = FilterProfile.createCriteria().get {
                    eq("isDefault", true)
                }
                if (defaultProfile) {
                    defaultProfile.addToCategories(category)
                    defaultProfile.merge()
                    if (!defaultProfile.hasErrors()) response.success = true
                    else response.success = false
                }
            }
        }
    }

    public void removeCategoryFromFilterProfile(def filterProfiles, Category category) {
        filterProfiles.each {
            if(it.categories.contains(category)) {
                it.removeFromCategories(category)
                it.merge()
            }
        }
    }

    @Transactional
    def assignFilter(params) {
        Long id = params?.profile_id ? params.profile_id.toLong() : null
        List filters = params.list("selected_filter").collect { Filter.proxy(it.toLong()) };
        List filterGroups = params.list("selected_filter_group").collect { FilterGroup.proxy(it.toLong()) };
        FilterProfile filterProfile = id ? FilterProfile.get(id) : null
        if (filterProfile) {
            filterProfile.filters = filters
            filterProfile.filterGroups = filterGroups
            filterProfile.save()
            if (!filterProfile.hasErrors()) return true
        }
        return false
    }

    @Transactional
    boolean cloneFilterProfile(Long profileId) {
        FilterProfile profile = FilterProfile.get(profileId)
        if (profile) {
            String newName = commonService.getCopyNameForDomain(profile)
            FilterProfile newProfile = DomainUtil.clone(profile, ["isDefault", "name", "categories", "filters", "filterGroups"])
            newProfile.name = newName
            copyFilters(profile,newProfile)
            newProfile.save()
            if (!newProfile.hasErrors()) return true
        }
        return false
    }

    void copyFilters(FilterProfile profile,FilterProfile newProfile){
        profile.filters.each { filter ->
            newProfile.filters.add(Filter.get(filter.id))
        }
        profile.filterGroups.each { filterGroup ->
            newProfile.filterGroups.add(FilterGroup.get(filterGroup.id))
        }
    }

    @Transactional
    boolean deleteFilterFromProfile(Long profileId, Long filterId) {
        FilterProfile profile = profileId ? FilterProfile.load(profileId) : null
        Filter filter = filterId ? Filter.load(filterId) : null
        if (profile && filter) {
            if (profile.filters.contains(filter)) {
                profile.removeFromFilters(filter)
                profile.merge()
                if (!profile.hasErrors()) return true
            }
        }
        return false
    }

    @Transactional
    boolean deleteFilterGroupFromProfile(Long profileId, Long filterGroupId) {
        FilterProfile profile = profileId ? FilterProfile.load(profileId) : null
        FilterGroup filterGroup = filterGroupId ? FilterGroup.load(filterGroupId) : null
        if (profile && filterGroup) {
            if (profile.filterGroups.contains(filterGroup)) {
                profile.removeFromFilterGroups(filterGroup)
                profile.merge()
                if (!profile.hasErrors()) return true
            }
        }
        return false
    }
}
