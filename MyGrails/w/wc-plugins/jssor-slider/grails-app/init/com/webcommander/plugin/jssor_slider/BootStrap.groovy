package com.webcommander.plugin.jssor_slider

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.Galleries
import com.webcommander.constants.NamedConstants
import com.webcommander.content.AlbumImage
import com.webcommander.events.AppEventManager
import com.webcommander.installation.template.InstallationDataHolder
import com.webcommander.manager.HookManager
import com.webcommander.models.TemplateData
import com.webcommander.tenant.TenantContext
import com.webcommander.util.DomainUtil
import grails.web.servlet.mvc.GrailsParameterMap

import javax.servlet.ServletContext
import com.webcommander.plugin.PluginMeta

class BootStrap {

    private static final PLUGIN_UNDERSCORE_NAME = "jssorSlider"
    private static final BLOG_POST = "blogPost"

    List named_constants = [
            [constant:"GALLERY_TYPES", key: "JSSOR_SLIDER", value: PLUGIN_UNDERSCORE_NAME],
            [constant:"GALLERY_NAMES", key: PLUGIN_UNDERSCORE_NAME, value: "Jssor Slider"]
    ]

    List gallery_constants = [
            [constant:"GALLERY_LICENSE", key: PLUGIN_UNDERSCORE_NAME, value: "allow_jssor_slider_feature"],
            [constant:"TYPES", key: PLUGIN_UNDERSCORE_NAME, value: [
                    thumb: "plugins/jssor-slider/images/thumb.png",
                    config: "/plugins/jssor_slider/config",
                    render: "/plugins/jssor_slider/render.gsp"
            ]],
    ]


    List gallery_support_constants = [
            [constant:"CONTENT_SUPPORT", key: DomainConstants.GALLERY_CONTENT_TYPES.ALBUM, value: PLUGIN_UNDERSCORE_NAME],
            [constant:"CONTENT_SUPPORT", key: DomainConstants.GALLERY_CONTENT_TYPES.CATEGORY, value: PLUGIN_UNDERSCORE_NAME],
            [constant:"CONTENT_SUPPORT", key: DomainConstants.GALLERY_CONTENT_TYPES.PRODUCT, value: PLUGIN_UNDERSCORE_NAME],
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
        AlbumImage.metaClass.with {
            captionList = {
                Long id = delegate.id
                return JssorSliderCaption.createCriteria().list {
                    eq("image.id", id)
                }
            }
        }
        AppEventManager.on("copy-template-data", {TemplateData templateData, InstallationDataHolder installationDataHolder ->
            List captions = templateData.otherContents["jssor_slider_captions"] ?: []
            captions.each { Map data ->
                try {
                    JssorSliderCaption caption = new  JssorSliderCaption()
                    data.image = installationDataHolder.getContentMapping("album_image", data.image, "id")
                    DomainUtil.populateDomainInst(caption, data)
                    if (caption.validate()) caption.save()
                } catch (Throwable t) {}
            }
        })

        HookManager.register("provide-template-data", { TemplateData templateData ->
            templateData.otherContents["jssor_slider_captions"] = JssorSliderCaption.list().collect {
                return DomainUtil.toMap(it)
            }
            return templateData
        })
    }

    def destroy = {
    }
}
