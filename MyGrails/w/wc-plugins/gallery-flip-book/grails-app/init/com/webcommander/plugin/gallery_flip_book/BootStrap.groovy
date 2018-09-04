package com.webcommander.plugin.gallery_flip_book

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.Galleries
import com.webcommander.constants.NamedConstants
import com.webcommander.tenant.TenantContext

class BootStrap {

    private static final PLUGIN_UNDERSCORE_NAME = "flip_book"


    List gallery_support_constants = [
            [constant:"CONTENT_SUPPORT", key: DomainConstants.GALLERY_CONTENT_TYPES.ALBUM, value: PLUGIN_UNDERSCORE_NAME]
    ]

    List named_constants = [
            [constant:"GALLERY_TYPES", key: "FLIP_BOOK", value: PLUGIN_UNDERSCORE_NAME],
            [constant:"GALLERY_NAMES", key: PLUGIN_UNDERSCORE_NAME, value: "Flip Book"],
    ]

    List gallery_constants = [
            [constant:"GALLERY_LICENSE", key: PLUGIN_UNDERSCORE_NAME, value: "allow_gallery_flip_book_feature"],
            [constant:"TYPES", key: PLUGIN_UNDERSCORE_NAME, value: [
                    thumb: "plugins/gallery-flip-book/images/thumb.png",
                    config: "/plugins/gallery_flip_book/config",
                    render: "/plugins/gallery_flip_book/render.gsp"
            ]],
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