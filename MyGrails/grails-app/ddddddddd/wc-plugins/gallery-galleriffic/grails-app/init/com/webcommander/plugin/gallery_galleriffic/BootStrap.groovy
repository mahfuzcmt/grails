package com.webcommander.plugin.gallery_galleriffic

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.Galleries
import com.webcommander.constants.NamedConstants
import com.webcommander.tenant.TenantContext

import javax.servlet.ServletContext
import com.webcommander.plugin.PluginMeta

class BootStrap {

    private static final PLUGIN_UNDERSCORE_NAME = "galleriffic"

    List named_constants = [
            [constant:"GALLERY_TYPES", key: "GALLERIFFIC", value: PLUGIN_UNDERSCORE_NAME],
            [constant:"GALLERY_NAMES", key: PLUGIN_UNDERSCORE_NAME, value: "Galleriffic"],
    ]

    List gallery_constants = [
            [constant:"GALLERY_LICENSE", key: PLUGIN_UNDERSCORE_NAME, value: "allow_gallery_galleriffic_feature"],
            [constant:"TYPES", key: PLUGIN_UNDERSCORE_NAME, value: [
                    thumb: "plugins/gallery-galleriffic/images/thumb.png",
                    config: "/plugins/gallery_galleriffic/config",
                    render: "/plugins/gallery_galleriffic/render.gsp"
            ]]
    ]

    List gallery_support_constants = [
            [constant:"CONTENT_SUPPORT", key: DomainConstants.GALLERY_CONTENT_TYPES.ALBUM, value: PLUGIN_UNDERSCORE_NAME]
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