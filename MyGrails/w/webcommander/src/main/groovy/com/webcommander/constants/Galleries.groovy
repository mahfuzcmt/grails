package com.webcommander.constants

import com.webcommander.tenant.TenantContext

class Galleries {

    private static Map _DYNAMIC_CONSTANT_HOLDER = [:]

    private static Map getDYNAMIC_CONSTANT() {
        Map dynamic = _DYNAMIC_CONSTANT_HOLDER[TenantContext.currentTenant]
        if(!dynamic) {
            dynamic = _DYNAMIC_CONSTANT_HOLDER[TenantContext.currentTenant] = [:].withDefault {[:]}
        }
        return dynamic
    }

    static addConstant(List list) {
        list.each { config ->
            DYNAMIC_CONSTANT[config.constant].put(config.key, config.value)
        }
    }

    static removeConstant(List list) {
        list.each { config ->
            DYNAMIC_CONSTANT[config.constant].remove(config.key)
        }
    }

    static addContentSupportConstant(List list) {
        list.each {
            if (_CONTENT_SUPPORT[it.key] == null){
                _CONTENT_SUPPORT[it.key] = []
            }
            List value = CONTENT_SUPPORT[it.key] + [it.value]
            DYNAMIC_CONSTANT[it.constant].put(it.key, value)
        }
    }

    static removeContentSupportConstant(List list) {
        list.each {
            DYNAMIC_CONSTANT[it.constant][it.key].remove(it.value)
        }
    }

    static getTYPES() {
        return [
                nivoSlider: [
                        thumb: "galleries/nivoSlider/thumb.jpg",
                        config: "/galleries/nivoSlider/config",
                        render: "/galleries/nivoSlider/render.gsp"
                ]
        ].with {it + getDYNAMIC_CONSTANT().TYPES}
    }

    private static _CONTENT_SUPPORT = [
            (DomainConstants.GALLERY_CONTENT_TYPES.ALBUM): ["nivoSlider"],
            (DomainConstants.GALLERY_CONTENT_TYPES.CATEGORY): [],
            (DomainConstants.GALLERY_CONTENT_TYPES.PRODUCT): [],
            (DomainConstants.GALLERY_CONTENT_TYPES.ARTICLE): [],
    ]

    static getCONTENT_SUPPORT() {
        return _CONTENT_SUPPORT + DYNAMIC_CONSTANT.CONTENT_SUPPORT
    }

    static getGALLERY_LICENSE() {
        return [:].with {it + getDYNAMIC_CONSTANT().GALLERY_LICENSE}
    }
}
