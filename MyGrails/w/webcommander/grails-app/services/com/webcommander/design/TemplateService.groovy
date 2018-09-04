package com.webcommander.design

import com.webcommander.admin.ConfigService
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommonService
import com.webcommander.common.FileService
import com.webcommander.config.EmailTemplate
import com.webcommander.constants.DomainConstants
import com.webcommander.content.PageService
import com.webcommander.events.AppEventManager
import com.webcommander.installation.template.TemplateContent
import com.webcommander.manager.LicenseManager
import com.webcommander.manager.PathManager
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.HttpUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.apache.commons.io.FilenameUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import static com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES

@Initializable
class TemplateService {
    CommonService commonService
    PageService pageService
    ConfigService configService
    FileService fileService
    @Autowired
    @Qualifier("com.webcommander.AppResourceTagLib")
    com.webcommander.AppResourceTagLib appResource

    static void initialize() {
        AppEventManager.on("product-update", { id ->
            TemplateContent.where {
                contentType == DomainConstants.WIDGET_CONTENT_TYPE.PRODUCT
                contentId == id.toLong()
            }.deleteAll()
        })
        AppEventManager.on("category-update", { id ->
            TemplateContent.where {
                contentType == DomainConstants.WIDGET_CONTENT_TYPE.CATEGORY
                contentId == id.toLong()
            }.deleteAll()
        })
        AppEventManager.on("article-update", { id ->
            TemplateContent.where {
                contentType == DomainConstants.WIDGET_CONTENT_TYPE.ARTICLE
                contentId == id
            }.deleteAll()
        })

        AppEventManager.on("album-update", { id ->
            TemplateContent.where {
                contentType == DomainConstants.WIDGET_CONTENT_TYPE.ALBUM
                contentId == id
            }.deleteAll()
        })
        AppEventManager.on("navigation-update", { id ->
            TemplateContent.where {
                contentType == DomainConstants.WIDGET_CONTENT_TYPE.NAVIGATION
                contentId == id
            }.deleteAll()
        })
        AppEventManager.on("page-update", { id ->
            TemplateContent.where {
                contentType == "page"
                contentId == id
            }.deleteAll()
        })
        AppEventManager.on("layout-update", { id ->
            TemplateContent.where {
                contentType == "layout"
                contentId == id
            }.deleteAll()
        })
    }

    def getAllTemplate(Integer max, Integer offset, String type, String category) {
        String url = AppUtil.getConfig(SITE_CONFIG_TYPES.ADMINISTRATION, "provision").url
        try {
            String json = HttpUtil.doGetRequest(url + "commander_provision/templates?offset=" + offset + "&max=" + max + "&type=" + type + "&category=" + category)
            Map map = JSON.parse(json)
            return [templateList: map.items, totalCount: map.total]
        } catch(Throwable t) {
            throw new ApplicationRuntimeException("could.not.collect.templates")
        }
    }



    Boolean validate(Map templateDetails) {
        if (LicenseManager.isProvisionActive()) {
            Integer myPackageWeight = AppUtil.getConfig(SITE_CONFIG_TYPES.LICENSE, "package_weight") ?: 0
            Integer templatePackageWeight = templateDetails.packageWeight.toInteger()
            if(templatePackageWeight > myPackageWeight) {
                throw new ApplicationRuntimeException("template.applicable.message", [templateDetails.packageName])
            }
            return true
        }
    }

    List<Map> getInstalledColors() {
        List<Map> colors = []
        String colorsAbsulatePath = appResource.getTemplateColorAbsulatePath()
        File colorDir = new File(colorsAbsulatePath)
        if (colorDir.exists()) {
            colorDir.eachFile { File file ->
                String ext = FilenameUtils.getExtension(file.name)
                if(ext == "css") {
                    String baseName = FilenameUtils.getBaseName(file.name)
                    Map color = [fullname: baseName]
                    List names = baseName.split("-")
                    color.code = "#" + names.pop()
                    color.name = names ? names.join(" ").capitalize() : color.code
                    colors.add(color)
                }
            }
        }
        return colors
    }

    @Transactional
    Boolean changeColor(String color) {
        color = color ? color.trim().replaceAll("\\s+", "-") : color
        return configService.update([
            [
              type     : SITE_CONFIG_TYPES.GENERAL,
              configKey: "template_color",
              value    : color
            ]
        ])
    }

    @Transactional
    Boolean changeTemplateContainerClass(String clazz) {
        return configService.update([
            [
              type     : SITE_CONFIG_TYPES.GENERAL,
              configKey: "template_container_class",
              value    : clazz
            ]
        ])
    }

    @Transactional
    boolean saveEmailTemplate(Map params) {
        EmailTemplate template = EmailTemplate.get(params.templateId)
        String relativePath = "email-templates/${template.identifier}"
        String localTemplatePath = appResource.getCustomRestrictedResourcePath(relativePath: relativePath)
        File pathDir = new File(localTemplatePath)
        if(!pathDir.exists()) {
            pathDir.mkdirs()
        }

        File activeHtml = new File(localTemplatePath + File.separator + "default.html")
        File activeText = new File(localTemplatePath + File.separator + "default.txt")
        activeHtml.write(params.html)
        activeText.write(params.txt)

        template.contentType = params.contentType
        template.subject = params.subject
        template.active = params.active.toBoolean()
        template.ccToAdmin = params.ccToAdmin.toBoolean()

        fileService.uploadModifiableResource(activeHtml, "${relativePath}/default.html")
        fileService.uploadModifiableResource(activeText, "${relativePath}/default.txt")

        template.save()
        return !template.hasErrors()
    }

    Map getEmailTemplateData(Serializable templateId) {
        EmailTemplate template = EmailTemplate.get(templateId)
        Map data = EmailTemplate.getMailBodies(template.identifier)
        data.template = template
        data.macros = JSON.parse( fileService.getRestrictedResourceStream("email-templates/${template.identifier}/macro.json").text)
        return data
    }
}
