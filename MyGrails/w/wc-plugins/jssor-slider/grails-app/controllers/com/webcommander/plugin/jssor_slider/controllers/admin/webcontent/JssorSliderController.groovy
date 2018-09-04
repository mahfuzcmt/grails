package com.webcommander.plugin.jssor_slider.controllers.admin.webcontent

import com.webcommander.common.CommonService
import com.webcommander.plugin.jssor_slider.JssorSliderCaption
import com.webcommander.plugin.jssor_slider.JssorSliderService
import com.webcommander.plugin.jssor_slider.constant.DomainConstant
import grails.converters.JSON

class JssorSliderController {
    JssorSliderService jssorSliderService
    CommonService commonService

    def loadCaption() {
        params.max = params.max ?: "10";
        Integer count = jssorSliderService.getCaptiopnCount(params)
        List<JssorSliderCaption> captions = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.max = max
            params.offset = offset
            jssorSliderService.getCaptions(params)
        }
        Map captionTransitions = [:]
        DomainConstant.CAPTION_TRANSITIONS.values().each {
            it.each {
                captionTransitions[it.value] = it.key
            }
        }
        render(view: "/plugins/jssor_slider/admin/captions", model: [captions: captions, count: count,captionTransitions: captionTransitions])
    }

    def edit() {
        def caption = JssorSliderCaption.get(params.id) ?: new JssorSliderCaption()
        render(view: "/plugins/jssor_slider/admin/editCaption", model: [caption: caption])
    }

    def updateCaption() {
        def saved = jssorSliderService.editCaption(params)
        if(saved) {
            render([status: "success", message: g.message(code: "caption.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "caption.could.not.save")] as JSON)
        }
    }

    def addCaption() {
        Map caption = params.caption ?: [:]
        render(view: "/plugins/jssor_slider/admin/addNewCaption", model: [caption: caption])
    }

    def remove() {
        Boolean deleted = jssorSliderService.remove(params)
        if(deleted) {
            render([status: "success", message: g.message(code: "caption.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "caption.delete.failure")] as JSON)
        }
    }
}