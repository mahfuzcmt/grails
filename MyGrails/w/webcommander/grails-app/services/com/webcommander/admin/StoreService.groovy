package com.webcommander.admin

import com.webcommander.Page
import com.webcommander.config.StoreDetail
import com.webcommander.config.StorePageAssoc
import com.webcommander.events.AppEventManager
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Address
import grails.gorm.transactions.Transactional
import grails.web.databinding.DataBindingUtils
import org.springframework.web.multipart.MultipartFile

@Transactional
class StoreService {
    ConfigService configService

    StoreDetail getStore(String identifier) {
        return StoreDetail.findByIdentifire(identifier)
    }

    List<StoreDetail> getAllStore(Map params) {
        return StoreDetail.createCriteria().list([max: params.max, offset: params.offset]) {
            eq("isDefault", false)
            order(params.sort ?: "id", params.dir ?: "asc")
        }
    }

    List<StoreDetail> getUnassignedStoreForPage(Map params, page) {
        List<Long> ids = StorePageAssoc.findAllByPage(page)*.id
        return StoreDetail.createCriteria().list([max: params.max, offset: params.offset]) {
            eq("isDefault", false)
            if(ids.size()) {
                not{ 'in'('id', ids )}
            }
            order(params.sort ?: "id", params.dir ?: "asc")
        }
    }

    Integer getCount(Map params) {
        return StoreDetail.createCriteria().count({})
    }

    @Transactional
    StoreDetail save(Map params, MultipartFile uploadedFile) {
        StoreDetail store = params.id ? StoreDetail.get(params.id) : new StoreDetail();
        Long id = params.id.toLong(0)
        if(id) {
            store.address = saveStoreAddress(params, store.address.id)
        } else {
            store.address = saveStoreAddress(params)
        }
        store.identifire = params.identifire
        store.location = params.location
        store.url = params.url
        store.name = params.name
        store.abn = params.abn
        store.additionalInfo = params.additionalInfo
        if (params["remove-image"]) {
            store.removeResource()
            store.image = null
        }
        store.save()
        if(!store.hasErrors() && uploadedFile) {
            configService.saveStoreLogo(uploadedFile, store)
        }
        if(!store.hasErrors()) {
            AppEventManager.fire("store-detail-update")
            return store
        }
        return null
    }

    def saveStoreAddress(Map params, Long id = 0) {
        Address address = id ? Address.get(id) : new Address();
        address.firstName = params.name
        address.lastName = params.lastName
        address.addressLine1 = params.addressLine1
        address.addressLine2 = params.addressLine2
        address.postCode = params.postCode
        address.phone = params.phone
        address.mobile = params.mobile
        address.fax = params.fax
        address.email = params.email
        address.country = Country.get(params.country)
        address.state = State.get(params.state ? params.state : 0)
        address.city = params.city
        address.save();
        return address;
    }

    @Transactional
    def delete(Long id) {
        StoreDetail store = StoreDetail.get(id);
        AppEventManager.fire("before-store-delete", [id]);
        store.delete()
        return true
    }

    Boolean setCurrentStoreInSession(String identifier) {
        def session = AppUtil.session;
        StoreDetail store = StoreDetail.findByIdentifire(identifier)
        if (store) {
            session.setAttribute("currentStore", store)
            return true
        }
        return false
    }

    def addPageForStore(Page page, Page parent, params) {
        StoreDetail oldStore = StoreDetail.get(params.storeId)
        StoreDetail store = StoreDetail.get(params.store)
        StorePageAssoc storePageAssoc = StorePageAssoc.findByPageAndParentAndStore(page, parent, oldStore)?: new StorePageAssoc();
        storePageAssoc.page = page
        storePageAssoc.parent = parent
        storePageAssoc.store = store
        storePageAssoc.save()
    }

    Map childPagesMap() {
        Map childPages = [:]
        List<Page> distinctParentPages = StorePageAssoc.createCriteria().list {
            projections {distinct("parent")}
        }
        distinctParentPages.each {
            String key = it.id.toString()
            childPages.put(key, StorePageAssoc.findAllByParent(it).page)
        }
        return childPages
    }

}
