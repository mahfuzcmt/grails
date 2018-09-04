package com.webcommander.plugin.swipe_box_slider

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.Galleries
import com.webcommander.constants.NamedConstants
import com.webcommander.tenant.TenantContext

import javax.servlet.ServletContext
import com.webcommander.plugin.PluginMeta

class BootStrap {

    private static final PLUGIN_UNDERSCORE_NAME = "swipeBoxSlider"

    List named_constants = [
            [constant:"GALLERY_TYPES", key: "SWIPE_BOX_SLIDER", value: PLUGIN_UNDERSCORE_NAME],
            [constant:"GALLERY_NAMES", key: PLUGIN_UNDERSCORE_NAME, value: "SwipeBox Slider"],
    ]

    List gallery_support_constants = [
            [constant:"CONTENT_SUPPORT", key: DomainConstants.GALLERY_CONTENT_TYPES.ALBUM, value: PLUGIN_UNDERSCORE_NAME]
    ]

    List gallery_constants = [
            [constant:"GALLERY_LICENSE", key: PLUGIN_UNDERSCORE_NAME, value: "allow_swipe_box_slider_feature"],
            [constant:"TYPES", key: PLUGIN_UNDERSCORE_NAME, value: [
                    thumb: "plugins/swipe-box-slider/images/thumb.png",
                    config: "/plugins/swipe_box_slider/admin/config",
                    render: "/plugins/swipe_box_slider/render.gsp"
            ]],
    ]


    def tenantInit = { tenant ->
        NamedConstants.addConstant(named_constants)
        Galleries.addConstant(gallery_constants)
        Galleries.addContentSupportConstant(gallery_support_constants)
    }

    def tenantDestroy = { tenant ->
        Galleries.removeContentSupportConstant(gallery_support_constants)
        Galleries.removeConstant(gallery_constants)
        NamedConstants.removeConstant(named_constants)
    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)
    }

    def destroy = {
    }
}