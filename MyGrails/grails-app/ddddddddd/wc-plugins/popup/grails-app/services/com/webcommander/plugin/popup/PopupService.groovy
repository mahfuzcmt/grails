package com.webcommander.plugin.popup

import com.webcommander.events.AppEventManager
import grails.gorm.transactions.Transactional

@Transactional
class PopupService {
    private Closure getCriteriaClosure(Map params) {
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if(params.isDisposable != "true") {
                eq("isDisposable", false)
            } else {
                eq("isDisposable", true)
            }
        }
    }

    Integer getPopupCount(Map params) {
        return Popup.createCriteria().count({
            and getCriteriaClosure(params)
        })
    }

    List<Popup> getPopups(Map params) {
        return Popup.createCriteria().list([max: params.max, offset: params.offset], {
            and getCriteriaClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc")
        })
    }

    Boolean save(Map params) {
        Popup popup = params.id ? Popup.get(params.id) : new Popup()
        popup.name = params.name;
        popup.contentType = params.contentType
        popup.contentId = params.contentId.toLong(0)
        popup.content = params.content
        popup.identifier = params.identifier
        popup.isDisposable = false
        popup.save()
        return !popup.hasErrors()
    }

    Boolean delete(Long id) {
        Popup popup = Popup.get(id)
        AppEventManager.fire("before-delete-popup", [id])
        popup.delete()
        return true
    }

    Integer deleteSelected(List<Long> ids) {
        int removeCount = 0;
        ids.each { id ->
            try {
                if(delete(id)) {
                    removeCount++;
                }
            } catch(Throwable ignored) {}
        }
        return removeCount
    }


}
