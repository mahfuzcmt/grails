package com.webcommander.plugin.filter.controllers.admin

import com.webcommander.authentication.annotations.License
import com.webcommander.common.CommonService
import com.webcommander.common.ImageService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.plugin.filter.FilterGroup
import com.webcommander.plugin.filter.FilterGroupItem
import com.webcommander.plugin.filter.FilterGroupProductAssoc
import com.webcommander.plugin.filter.FilterGroupService
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product
import grails.converters.JSON
import org.springframework.web.multipart.MultipartFile

/**
 * Created by sharif ul islam on 09/04/2018.
 */
class FilterGroupController {

    CommonService commonService
    FilterGroupService filterGroupService
    ImageService imageService

    @License(required = "allow_filter_feature")
    def loadAppView() {
        params.max = params.max ?: "10";
        Integer count = filterGroupService.getFilterGroupCount(params);
        List<FilterGroup> groups = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.max = max;
            params.offset = offset;
            filterGroupService.getFilterGroups(params);
        }
        render(view: "/plugins/filter/admin/filterGroup/appView", model: [filterGroups: groups, count: count]);
    }

    @License(required = "allow_filter_feature")
    def edit() {
        FilterGroup group = params.id ? filterGroupService.getFilterGroup(params.long("id")) : new FilterGroup();
        render(view: "/plugins/filter/admin/filterGroup/infoEdit", model: [filterGroup: group]);
    }

    def saveFilterGroup() {
        FilterGroup group = filterGroupService.saveFilterGroup(params);
        if (!group.hasErrors()) {
            render([status: "success", message: g.message(code: "filter.group.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "filter.group.save.error")] as JSON)
        }
    }

    def deleteFilterGroup() {
        if(filterGroupService.deleteFilterGroup(params.long("id"))) {
            render([status: "success", message: g.message(code: "filter.group.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "filter.group.delete.failure")] as JSON)
        }
    }

    def deleteSelectedFilterGroup () {
        List<Long> ids = []
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        if(filterGroupService.deleteSelectedFilterGroups(ids)) {
            render([status: "success", message: g.message(code: "filter.group.delete.success")] as JSON)
        }else {
            render([status: "success", message: g.message(code: "filter.group.delete.failure")] as JSON)
        }
    }

    def isUnique() {
        render(commonService.responseForUniqueField(FilterGroup, params.long("id"), params.field, params.value) as JSON)
    }

    def loadFilterGroupEditor() {
        Long id = params.long("id");
        def items = filterGroupService.getFilterGroupItems(id)
        render view: "/plugins/filter/admin/filterGroup/itemEditor", model: [filterGroupId : id, items: items]
    }

    def updateItemImage() {
        List idList = params.list("id").collect{it.toLong(0)}
        List imageDataList = params.list("imageData").collect()
        List<MultipartFile> imageList = request.getMultiFileMap()."item-images"
        Boolean success = true
        idList.eachWithIndex { id, i ->
            Long itemId = id
            FilterGroupItem filterGroupItem = FilterGroupItem.get(itemId)
            MultipartFile uploadedImage = imageList ? imageList[i] : null
            def noimageData = imageDataList ? imageDataList[i]: null
            if(!uploadedImage && noimageData == "false") {
                if(filterGroupItem?.image) {
                    filterGroupItem.removeResource()
                    filterGroupService.updateItemImage(itemId, "")
                }
            }
            if(uploadedImage) {
                if(filterGroupItem.image) {
                    filterGroupItem.removeResource()
                }
                String originalName = uploadedImage.originalFilename
                if(filterGroupService.updateItemImage(itemId, originalName)) {
                    filterGroupItem.image = originalName
                    imageService.uploadImage(uploadedImage, NamedConstants.IMAGE_RESIZE_TYPE.FILTER_GROUP_ITEM, filterGroupItem, 2 * 1024 * 1024);
                    success = true
                } else {
                    success = false
                }
            }
        }

        if(success) {
            render([status: "success", message: g.message(code: "filter.group.item.image.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "filter.group.item.image.save.failure")] as JSON)
        }
    }

    def createFilterGroup() {
        FilterGroupItem filterGroupItem
        if(params.cache) {
            Map data = JSON.parse(params.cache);
            data.id = null;
            filterGroupItem = new FilterGroupItem(data)
        } else if(params.id) {
            filterGroupItem = FilterGroupItem.get(params.int("id") ?: 0)
        } else {
            filterGroupItem = [:]
        }
        render(view: "/plugins/filter/admin/filterGroup/createFilterGroupItem", model: [filterGroupItem: filterGroupItem, params: params]);
    }

    def saveFilterGroupItems() {
        if(params.updatedJSON || params.removedItems) {
            Long filterGroupId = params.long("filterGroupId") ?: 0;
            List<Map> updatedItems = JSON.parse(params.updatedJSON);
            List removedItems = params.list("removedItems").collect {it.toLong()};
            def negetiveIdCache = filterGroupService.saveItems(updatedItems, removedItems, filterGroupId)
            render([status: "success", message: g.message(code: "filter.group.save.success"), newIdMaps: negetiveIdCache] as JSON)
        }
    }

    def loadAdditionalProperties() {
        List<FilterGroup> filterGroups = filterGroupService.getFilterGroups([isActive: true]);
        Long pid = params.long("productId") ?: 0L
        Product product = Product.get(pid)

        if (product) {
            Map filterGroupSelectedValues = [:]
            List<FilterGroupProductAssoc> productAssocList = FilterGroupProductAssoc.createCriteria().list {
                eq("product.id", product.id)
            }
            productAssocList.each {FilterGroupProductAssoc productAssoc ->
                filterGroupSelectedValues[productAssoc.item.filterGroup.id] = productAssoc.item.id
            }

            render(view: "/plugins/filter/admin/filterGroup/additionalProperties", model: [filterGroups: filterGroups, productId: product.id, filterGroupSelectedValues: filterGroupSelectedValues])
        } else {
            render([status: "error", message: g.message(code: "product.not.available")] as JSON)
        }

    }

    def mapProductFilterGroup() {
        params.isClearAssoc = true
        if(filterGroupService.mapProductFilterGroup(params)) {
            render([status: "success", message: g.message(code: "product.filterGroup.assoc.success")] as JSON)
        } else {
            render([status: "warning", message: g.message(code: "product.filterGroup.assoc.failure")] as JSON)
        }
    }

    def loadFilterGroupSetting () {
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.FILTER_GROUP_PAGE)
        render (view: "/plugins/filter/admin/setting/filterGroupSetting", model: [configs: config])
    }

}
