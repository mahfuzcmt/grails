package com.webcommander.plugin.snippet.mixin_service

import com.webcommander.AppResourceTagLib
import com.webcommander.common.CommonService
import com.webcommander.installation.template.InstallationDataHolder
import com.webcommander.models.TemplateData
import com.webcommander.plugin.snippet.Snippet
import com.webcommander.plugin.snippet.SnippetResourceTagLib
import com.webcommander.util.DomainUtil

class TemplateInstallationService {
    private static CommonService _commonService
    private static CommonService getCommonService() {
        return _commonService ?: (_commonService = CommonService.getInstance())
    }

    Long saveSnippetTypeWidgetContent(TemplateData templateData, InstallationDataHolder installationDataHolder, Map data) {
        Snippet snippet = new Snippet()
        DomainUtil.populateDomainInst(snippet, data);
        if(!commonService.isUnique(snippet, "name")) {
            snippet.name = commonService.getCopyNameForDomain(snippet)
        }
        if(snippet.hasErrors()) {
            return null
        }
        snippet.save()
        String srcPath = SnippetResourceTagLib.getResourceRelativePath(data.id.toString());
        String destPath = SnippetResourceTagLib.getResourceRelativePath(snippet.id.toString());
        moveTemplateData(installationDataHolder, "${AppResourceTagLib.RESOURCES}/" + srcPath,  destPath)
        return snippet.id
    }
}
