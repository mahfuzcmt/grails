package com.webcommander.plugin.filter

import com.webcommander.common.CommonService
import com.webcommander.events.AppEventManager
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product
import grails.gorm.transactions.Transactional
import grails.util.TypeConvertingMap
import grails.web.databinding.DataBindingUtils
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.hibernate.sql.JoinType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

/**
 * Created by sharif ul islam on 09/04/2018.
 */
@Transactional
class FilterGroupService {

    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g

    CommonService commonService

    FilterGroup saveFilterGroup(TypeConvertingMap params) {
        def session = AppUtil.session
        FilterGroup group = params.id ? FilterGroup.get(params.id) : new FilterGroup(type: params.type);
        def checkBoxList = ["isActive"]
        if(!commonService.isUnique(group, "name")) {
            throw new ApplicationRuntimeException("filter.group.name.exists")
        }
        DataBindingUtils.bindObjectToInstance(group, params, null, ["id"] + checkBoxList, null)

        for(String checkBoxItem : checkBoxList) {
            group."$checkBoxItem" = params."$checkBoxItem" ? true : false
        }

        group.description = params.description

        group.save()

        AppEventManager.fire("filter-group-update")

        return group
    }

    List<FilterGroup> getFilterGroups (Map params) {
        def listMap = [max: params.max, offset: params.offset];
        return FilterGroup.createCriteria().list(listMap) {
            and getFilterGroupCriteriaClosure(params);
            order(params.sort ?: "id", params.dir ?: "desc");
        }
    }

    Integer getFilterGroupCount (Map params) {
        return FilterGroup.createCriteria().get {
            and getFilterGroupCriteriaClosure(params);
            projections {
                rowCount();
            }
        }
    }

    boolean deleteFilterGroup(Long id) {
        try {

            FilterGroup group = FilterGroup.get(id)

            if (group.items) {
                FilterGroupProductAssoc.createCriteria().list {
                    inList("item.id", group.items.id)
                }*.delete();
            }

            List<FilterProfile> profileList = FilterProfile.createCriteria().list {
                createAlias("filterGroups", "fgs")
                eq('fgs.id', group.id)
            }

            profileList.each { FilterProfile profile ->
                int index = profile.filterGroups.findIndexOf {it.id == group.id}
                profile.filterGroups.remove(index)
                profile.merge()
            }

            group.items*.delete()
            group.delete()

            return !group.hasErrors()

        } catch (Throwable t) {
            return false
        }
    }

    boolean deleteSelectedFilterGroups (List ids) {
        boolean result = true
        ids.each { id->
            if (!deleteFilterGroup(id)) {
                result = false
            }
        }
        return result
    }

    List<FilterGroupItem> getFilterGroupItems (Long filterGroupId) {
        List items = FilterGroupItem.createCriteria().list {
            eq("filterGroup.id", filterGroupId)
            order("idx", "asc")
        }

    }

    public updateItemImage (Long id, String image) {
        return FilterGroupItem.where {
            id == id
        }.updateAll([
                image : image
        ]) > 0
    }

    def saveItems(List updatedItems, List removedItems, Long filterGroupId) {
        Map negativeIdCache = [:]
        FilterGroup filterGroup = FilterGroup.get(filterGroupId)
        //filterGroup.items.clear()
        updatedItems.each { it ->
            Long id = it.id.toLong();
            FilterGroupItem filterGroupItem = id > 0 ? FilterGroupItem.get(id) : new FilterGroupItem(filterGroup: filterGroup);
            filterGroupItem.idx =  it.placement.toInteger();
            filterGroup.addToItems(filterGroupItem);
            if(it.update_cache) {
                filterGroupItem.title = it.update_cache.title;
                filterGroupItem.itemUrl = it.update_cache.itemUrl;
                filterGroupItem.heading = it.update_cache.heading;
                filterGroupItem.detailDescription = it.update_cache.detailDescription;
                filterGroupItem.shortDescription = it.update_cache.shortDescription;
                filterGroupItem.imageAlt = it.update_cache.imageAlt;
                filterGroupItem.externalId = it.update_cache.externalId;
            }

            if(!filterGroupItem.id || !commonService.isUnique(filterGroupItem, "url")) {
                filterGroupItem.url = commonService.getUrlForDomain(filterGroupItem.getClass(), filterGroupItem.heading)
            }

            filterGroupItem.save()
            if(filterGroupItem.hasErrors()){
                throw new ApplicationRuntimeException("filter.group.save.failed");
            }
            if (id < 0) {
                negativeIdCache["" + id] = filterGroupItem.id;
            }
        }
        if(removedItems.size() > 0) {
            removedItems.each {
                FilterGroupItem item = FilterGroupItem.load(it)
                filterGroup.removeFromItems(item)
                item.delete()
            }

        }
        filterGroup.save()
        AppEventManager.fire("filter-group-update", [filterGroup.id])
        return negativeIdCache;
    }

    Boolean mapProductFilterGroup(Map params) {
        Product product = Product.get(params.productId);

        List fgi = params.list("filter-group-item")
        List<Long> groupItems = []
        if (fgi) {
            fgi.each {
                if (it) {
                    groupItems.add(Long.parseLong(it))
                }
            }
        }

        if (params.isClearAssoc) {
            FilterGroupProductAssoc.createCriteria().list {
                eq("product.id", product.id)
            }*.delete();
        }

        int successCount = 0
        if (groupItems) {
            groupItems.each {
                FilterGroupItem item = FilterGroupItem.get(it)
                FilterGroupProductAssoc productAssoc = new FilterGroupProductAssoc()
                productAssoc.product = product
                productAssoc.item = item
                productAssoc.save()

                if (!productAssoc.hasErrors()) {
                    successCount++
                }
            }
        }

        return successCount ? true : false
    }

    FilterGroup getFilterGroup(Long filterGroupId) {
        if (filterGroupId) {
            return FilterGroup.get(filterGroupId)
        }
        return null
    }

    FilterGroup getActiveFilterGroup(Long filterGroupId) {
        if (filterGroupId) {
            return FilterGroup.findByIdAndIsActive(filterGroupId, true)
        }
        return null
    }

    FilterGroupItem getFilterGroupItem(Long filterGroupItemId) {
        if (filterGroupItemId) {
            return FilterGroupItem.get(filterGroupItemId)
        }
        return null
    }

    /////////////////////////////////////////////////////

    private Closure getFilterGroupCriteriaClosure(Map params) {
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.name) {
                ilike("name", "%${params.name.trim().encodeAsLikeText()}%")
            }
            if (params.createdFrom) {
                Date date = params.createdFrom.dayStart.gmt(session.timezone);
                ge("created", date);
            }
            if (params.createdTo) {
                Date date = params.createdTo.dayEnd.gmt(session.timezone);
                le("created", date);
            }
            if (params.updatedFrom) {
                Date date = params.updatedFrom.dayStart.gmt(session.timezone);
                ge("updated", date);
            }
            if (params.updatedTo) {
                Date date = params.updatedTo.dayEnd.gmt(session.timezone);
                le("updated", date);
            }
            if (params.ids) {
                inList("id", params.list("ids").collect { it.toLong() })
            }

            if (params.isActive) {
                eq("isActive", params.isActive)
            }

        }
    }

}
