package com.webcommander.installation.template

import com.webcommander.AppResourceTagLib
import com.webcommander.manager.CloudStorageManager
import com.webcommander.tenant.TenantContext
import com.webcommander.webcommerce.CloudConfig

class FindReplaceUrl {

    private String oldTenantId = ""
    private CloudConfig systemDefaultCloud
    private CloudConfig defaultCloud
    private InstallationDataHolder installationDataHolder

    private String templateReplaceText
    private String pubReplaceText
    private String resourceReplaceText

    private String templateFindText
    private String pubFindText
    private String resourceFindText

    FindReplaceUrl(InstallationDataHolder installationDataHolder){
         if (installationDataHolder.templateData.sourceTenantId){
            oldTenantId = installationDataHolder.templateData.sourceTenantId + "/"
        }
        this.systemDefaultCloud = CloudStorageManager.isCloudEnable()
        this.defaultCloud =  CloudStorageManager.getDefaultCloudConfig()
        this.installationDataHolder = installationDataHolder
    }


    private setReplaceBaseURLs() {
        if (systemDefaultCloud) {
            templateReplaceText = "${systemDefaultCloud.baseUrl}${AppResourceTagLib.TEMPLATE}/"
            pubReplaceText = "${systemDefaultCloud.baseUrl}${AppResourceTagLib.PUB}/"
            resourceReplaceText = "${defaultCloud.baseUrl}${AppResourceTagLib.RESOURCES}/"
        } else {
            templateReplaceText = "/${AppResourceTagLib.TEMPLATE}/${TenantContext.currentTenant}/"
            pubReplaceText = "/${AppResourceTagLib.PUB}/${TenantContext.currentTenant}/"
            resourceReplaceText = "/${AppResourceTagLib.RESOURCES}/${TenantContext.currentTenant}/"
        }
    }


    private String replaceURL(String text){
        text = text.replaceAll(templateFindText, templateReplaceText)
        text = text.replaceAll(pubFindText, pubReplaceText)
        text = text.replaceAll(resourceFindText, resourceReplaceText)
        return text
    }


    static String replace(File file, InstallationDataHolder installationDataHolder){
        FindReplaceUrl findReplaceUrl = new FindReplaceUrl(installationDataHolder)
        return findReplaceUrl.replace(file)
    }


    String replace(File file){
        String text = file.text
        setReplaceBaseURLs()
        if (installationDataHolder.templateData.cloudUrls.size() == 0) {
            templateFindText = "/${AppResourceTagLib.TEMPLATE}/${oldTenantId}"
            pubFindText = "/${AppResourceTagLib.PUB}/${oldTenantId}"
            resourceFindText = "/${AppResourceTagLib.RESOURCES}/${oldTenantId}"
            text = replaceURL(text)
        } else {
            installationDataHolder.templateData.cloudUrls.each {
                templateFindText = "${it}${AppResourceTagLib.TEMPLATE}/"
                pubFindText = "${it}${AppResourceTagLib.PUB}/"
                resourceFindText = "${it}${AppResourceTagLib.RESOURCES}/"
                text =  replaceURL(text)
            }
        }
        return text
    }



}
