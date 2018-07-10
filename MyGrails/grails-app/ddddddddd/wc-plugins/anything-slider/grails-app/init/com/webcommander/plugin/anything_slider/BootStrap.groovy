package com.webcommander.plugin.anything_slider

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.Galleries
import com.webcommander.constants.NamedConstants
import com.webcommander.tenant.TenantContext

class BootStrap {

    private static final ANYTHING_SLIDER = "anythingSlider"

    List named_constants = [
            [constant:"GALLERY_TYPES", key: "ANYTHING_SLIDER", value: ANYTHING_SLIDER],
            [constant:"GALLERY_NAMES", key: "anythingSlider", value: "Anything Slider"],
    ]

    List gallery_constants = [
            [constant:"GALLERY_LICENSE", key: ANYTHING_SLIDER, value: "allow_anything_slider_feature"],
            [constant:"TYPES", key: "anythingSlider", value: [
                    thumb: "plugins/anything-slider/images/thumb.png",
                    config: "/plugins/anything_slider/config",
                    render: "/plugins/anything_slider/render.gsp"]],
    ]

    List gallery_support_constants = [
            [constant:"CONTENT_SUPPORT", key: DomainConstants.GALLERY_CONTENT_TYPES.ALBUM, value: ANYTHING_SLIDER]
    ]

    def tenantInit = { tenant ->
        NamedConstants.addConstant(named_constants)
        Galleries.addConstant(gallery_constants)
        Galleries.addContentSupportConstant(gallery_support_constants)
    }

    def tenantDestroy = { tenant ->
        NamedConstants.removeConstant(named_constants)
        Galleries.removeConstant(gallery_constants)
        Galleries.removeContentSupportConstant(gallery_support_constants)
    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)
    }

    def destroy = {
    }
}
