package com.webcommander.controllers.admin.content

import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.common.CommonService
import com.webcommander.common.ImageService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.content.Navigation
import com.webcommander.content.NavigationItem
import com.webcommander.content.NavigationService
import com.webcommander.manager.PathManager
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import com.webcommander.util.StringUtil
import grails.converters.JSON
import grails.util.Holders
import org.springframework.web.multipart.MultipartFile

class NavigationController {
    NavigationService navigationService
    CommonService commonService
    ImageService imageService

    @Restriction(permission = "navigation.view.list")
    def loadAppView() {
        Integer count = navigationService.getNavigationCount(params)
        params.max = params.max ?: "10";
        List<Navigation> navigations = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            navigationService.getNavigations(params)
        }
        render view: "/admin/navigation/appView", model: [count: count, navigations: navigations];
    }

    @Restrictions([
        @Restriction(permission = "navigation.create", params_not_exist = "id"),
        @Restriction(permission = "navigation.edit", params_exist = "id", entity_param = "id", domain = Navigation)
    ])
    def create () {
        Navigation navigation = params.id ? Navigation.get(params.long("id")) : new Navigation()
        render view: "/admin/navigation/infoEdit", model: [navigation : navigation]
    }

    @Restrictions([
        @Restriction(permission = "navigation.create", params_not_exist = "id"),
        @Restriction(permission = "navigation.edit", params_exist = "id", entity_param = "id", domain = Navigation)
    ])
    def save () {
        params.remove("action")
        params.remove("controller")
        if(params.deleteTrashItem){
            def field = params.deleteTrashItem.collect{it.key}[0];
            def value = params[field];
            def error = navigationService.deleteTrashItemAndSaveCurrent(field,value)
        }
        if(navigationService.save(params)){
            render([status: "success", message: g.message(code: "navigation.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "navigation.save.failed")] as JSON)
        }
    }

    def advanceFilter() {
        render(view: "/admin/navigation/filter", model: [get: 0]);
    }

    @Restriction(permission = "navigation.edit", entity_param = "id", domain = Navigation)
    def loadNavigationEditor() {
        Long id = params.long("id");
        def items = navigationService.getNavigationItems(id)
        items = navigationService.populateNavigationItemsChildList(items);
        render view: "/admin/navigation/itemEditor", model: [navigationId : id, items: items]
    }

    @Restriction(permission = "navigation.edit.items", params_not_exist = "cache", entity_param = "navigationId", domain = Navigation)
    def createNavigation() {
        NavigationItem navigationItem
        if(params.cache) {
            Map data = JSON.parse(params.cache);
            data.id = null;
            if(data.parent && !null.equals(data.parent)) {
                data.parent = NavigationItem.get(data.parent.toInteger());
            } else {
                data.parent = null;
            }
            navigationItem = new NavigationItem(data)
            params.parent = null;
        } else if(params.id) {
            navigationItem = NavigationItem.get(params.int("id") ?: 0)
        } else {
            navigationItem = new NavigationItem()
        }
        if(params.parent) {
            navigationItem.parent = NavigationItem.get(params.int("parent") ?: 0)
        }
        def parents = params.parents ? JSON.parse(params.parents) : [:]
        render view: "/admin/navigation/createNavigationItem", model: [navigationItem: navigationItem, parents: parents, params: params]
    }

    @Restriction(permission = "navigation.remove", entity_param = "id", domain = Navigation)
    def delete () {
        try {
            if (navigationService.putNavigationInTrash(params.long("id"), params.at2_reply, params.at1_reply)) {
                render([status: "success", message: g.message(code: "navigation.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "navigation.delete.failure")] as JSON)
            }
        } catch (AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    @Restriction(permission = "navigation.remove", entity_param = "ids", domain = Navigation)
    def deleteSelected() {
        if (navigationService.putSelectedNavigationsInTrash(params.list("ids").collect{it.toLong()})) {
            render([status: "success", message: g.message(code: "selected.navigations.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "selected.navigations.could.not.delete")] as JSON)
        }
    }

    public def loadReferenceSelectorBasedOnType() {
        String type = params.type;
        def items;
        if (type && type != DomainConstants.NAVIGATION_ITEM_TYPE.EMAIL && type != DomainConstants.NAVIGATION_ITEM_TYPE.URL && type != DomainConstants.NAVIGATION_ITEM_TYPE.AUTO_GENERATED_PAGE) {
            def consumer = Holders.applicationContext.getBean(NavigationService.domains[type]);
            String domain = StringUtil.getCapitalizedAndPluralName(type)
            Map filerMap = NavigationService.domains_filer_params[type] ?: [:]
            items = consumer.getClass().getDeclaredMethod("get" + domain, Map as Class[]).invoke(consumer, [filerMap] as Object[])
        }else {
            items = [];
        }

        render(view: "/admin/navigation/referenceSelector", model: [type: type, items: items, ref: params.ref]);
    }

    public def saveNavigationItems() {
        if(params.updatedJSON || params.removedItems) {
            Long navigationId = params.long("navigationId") ?: 0;
            List<Map> updatedItems = JSON.parse(params.updatedJSON);
            List removedItems = params.list("removedItems").collect {it.toLong()};
            def negetiveIdCache = navigationService.saveItems(updatedItems, removedItems, navigationId)
            render([status: "success", message: g.message(code: "navigation.save.success"), newIdMaps: negetiveIdCache] as JSON)
        }
    }

    def updateItemImage() {
        List idList = params.list("id").collect{it.toLong(0)}
        List imageDataList = params.list("imageData").collect()
        List<MultipartFile> imageList = request.getMultiFileMap()."item-images"
        Boolean success = true
        idList.eachWithIndex { id, i ->
            Long itemId = id
            NavigationItem navigationItem = NavigationItem.get(itemId)
            MultipartFile uploadedImage = imageList ? imageList[i] : null
            def noimageData = imageDataList ? imageDataList[i]: null
            if(!uploadedImage && noimageData == "false") {
                if(navigationItem?.image) {
                    navigationItem.removeResource()
                    navigationService.updateItemImage(itemId, "")
                }
            }
            if(uploadedImage) {
                if(navigationItem.image) {
                    navigationItem.removeResource()
                }
                String originalName = uploadedImage.originalFilename
                if(navigationService.updateItemImage(itemId, originalName)) {
                    navigationItem.image = originalName
                    imageService.uploadImage(uploadedImage, NamedConstants.IMAGE_RESIZE_TYPE.NAVIGATION_ITEM, navigationItem, 20480l);
                    success = true
                } else {
                    success = false
                }
            }
        }

        if(success) {
            render([status: "success", message: g.message(code: "navigation.item.image.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "navigation.item.image.save.failure")] as JSON)
        }
    }

    def isUnique(){
        render(commonService.responseForUniqueField(Navigation, params.long("id"), params.field, params.value) as JSON)
    }

    def restoreFromTrash(){
        def field = params.field
        def value = params.value
        Long id = navigationService.restoreNavigationFromTrash(field,value);
        if(id){
            render([status: "success", message: g.message(code: "restored.successfully", args: ["Navigation"]), type: "navigation", id: id] as JSON)
        }
    }

    def autoPopulateWithItems() {
        Long id = params.long("id")
        String type = params.type
        String domain = StringUtil.getCapitalizedAndPluralName(type)
        def consumer = Holders.applicationContext.getBean(NavigationService.domains[type])
        def items = consumer.getClass().getDeclaredMethod("generateNavigationItemsFrom" + domain, [] as Class[]).invoke(consumer, [] as Object[])
        items = navigationService.populateNavigationItemsChildList(items)
        List removedItems = params.list("removedItems").collect {it.toLong()};
        render view: "/admin/navigation/itemEditor", model: [navigationId : id, items: items, removedItems: removedItems]
    }

    def autoPopulate() {
        render view: "/admin/navigation/autoPopulate", model: [d: true]
    }

    def loadRestrictedItemOption() {
        render view: "/admin/navigation/restrictedItemOption"
    }

    def saveRestrictedItemOption() {
        def result = navigationService.saveRestrictedItemOption(params)
        if(result) {
            render([status: "success", message: g.message(code: "navigation.item.update.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "navigation.item.update.failure")] as JSON)
        }
    }
}
