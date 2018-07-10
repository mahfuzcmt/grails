package com.webcommander.plugin.gallery_owl_carousel

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.Galleries
import com.webcommander.constants.NamedConstants
import com.webcommander.tenant.TenantContext

import javax.servlet.ServletContext
import com.webcommander.plugin.PluginMeta

class BootStrap {

    private static final PLUGIN_UNDERSCORE_NAME = "owlCarousel"
    private static final BLOG_POST = "blogPost"

    List named_constants = [
            [constant:"GALLERY_TYPES", key: "OWL_CAROUSEL", value: PLUGIN_UNDERSCORE_NAME],
            [constant:"GALLERY_NAMES", key: PLUGIN_UNDERSCORE_NAME, value: "Owl Carousel"],
    ]

    List gallery_constants = [
            [constant:"GALLERY_LICENSE", key: PLUGIN_UNDERSCORE_NAME, value: "allow_gallery_owl_carousel_feature"],
            [constant:"TYPES", key: PLUGIN_UNDERSCORE_NAME, value: [
                    thumb: "plugins/gallery-owl-carousel/images/thumb.png",
                    config: "/plugins/gallery_owl_carousel/config",
                    render: "/plugins/gallery_owl_carousel/render.gsp"
            ]],
    ]

    List gallery_support_constants = [
            [constant:"CONTENT_SUPPORT", key: DomainConstants.GALLERY_CONTENT_TYPES.ALBUM, value: PLUGIN_UNDERSCORE_NAME],
            [constant:"CONTENT_SUPPORT", key: DomainConstants.GALLERY_CONTENT_TYPES.CATEGORY, value: PLUGIN_UNDERSCORE_NAME],
            [constant:"CONTENT_SUPPORT", key: DomainConstants.GALLERY_CONTENT_TYPES.PRODUCT, value: PLUGIN_UNDERSCORE_NAME],
            [constant:"CONTENT_SUPPORT", key: DomainConstants.GALLERY_CONTENT_TYPES.ARTICLE, value: PLUGIN_UNDERSCORE_NAME],
            [constant:"CONTENT_SUPPORT", key: BLOG_POST, value: PLUGIN_UNDERSCORE_NAME],
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