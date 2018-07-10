package com.webcommander.plugin.jssor_slider

import com.webcommander.annotations.Initializable
import com.webcommander.content.AlbumImage
import com.webcommander.events.AppEventManager
import grails.gorm.transactions.Transactional

@Transactional
@Initializable
class JssorSliderService {

    public static void initialize() {
        AppEventManager.on("before-album-image-delete", { id ->
            JssorSliderCaption.createCriteria().list {
                eq("image.id", id)
            }*.delete()
        })
        AppEventManager.on("before-album-delete", { id ->
            AlbumImage.createCriteria().list {
                eq("parent.id", id)
            }.each {
                AppEventManager.fire("before-album-image-delete", [it.id])
            }
        })
    }

    private Closure getCriteriaClosure(Map params) {
        return {
            eq("image.id", params.imageId.toLong());
            if (params.searchText) {
                or {
                    def searchText = "%${params.searchText.trim().encodeAsLikeText()}%"
                    ilike("text", searchText)
                    ilike("url", searchText)
                    ilike("type", searchText)
                }
            }
        }
    }

    Integer getCaptiopnCount(Map params) {
        return JssorSliderCaption.createCriteria().count {
            and getCriteriaClosure(params)
        }
    }

    List<JssorSliderCaption> getCaptions(Map params) {
        def listMap = [max: params.max, offset: params.offset];
        return JssorSliderCaption.createCriteria().list(listMap) {
            and getCriteriaClosure(params)
            order(params.sort ?: "id", params.dir ?: "asc")
        }
    }

    @Transactional
    Boolean editCaption(Map params) {
        JssorSliderCaption caption
        if(params.id) {
            caption = JssorSliderCaption.get(params.id)
        } else {
            caption = new JssorSliderCaption()
        }
        caption.image = AlbumImage.proxy(params.imageId)
        caption.type = params.type
        caption.url = params.url
        caption.text = params.text
        caption.animation = params.animation
        caption.duration = params.duration.toLong()
        caption.delay = params.delay.toLong()
        caption.save()
        return !caption.hasErrors()
    }

    @Transactional
    Boolean remove(Map params) {
        JssorSliderCaption caption = JssorSliderCaption.get(params.id)
        AppEventManager.fire("before-jssor-slider-caption-delete", [caption.id])
        caption.delete()
        AppEventManager.fire("jssor-slider-caption-update")
        return true
    }

}
