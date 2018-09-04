package com.webcommander.plugin.snippet

import com.webcommander.ApplicationTagLib
import com.webcommander.plugin.snippet.constants.SnippetConstants
import com.webcommander.common.DeployService
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.io.FileType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier


@Transactional
class SnippetRepositoryService {



    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    ApplicationTagLib app
    DeployService deployService

   /*
    *  Repository Type: SnippetConstants.SNIPPET_REPOSITORY_TYPE
    */

    List<Map> getSnippetTemplates(String repositoryType, Map filter = [:]) {
        return this."get${repositoryType.capitalize()}SnippetTemplates"(filter)
    }

    List<Map> getSystemSnippetTemplates(Map filter = [:]) {
        List<Map> templates = [];
        String localRepositoryPath = SnippetResourceTagLib.getModifiableResourceWebInfAbsolutePath()
        File localRepository = new File(localRepositoryPath)
        if(localRepository.exists()) {
            localRepository.eachFile(FileType.DIRECTORIES, { template ->
                Map info = getLocalSnippetTemplate(template.name);
                if(info == null) return;
                Boolean isFilterFail = false;
                info.repositoryType = info.isSystemDefault ? SnippetConstants.SNIPPET_REPOSITORY_TYPE.ARCHIVE : SnippetConstants.SNIPPET_REPOSITORY_TYPE.LOCAL
                if(filter.category && filter.category != info.category) {
                    isFilterFail = true;
                }
                if(filter.isNotSystemDefault && info.isSystemDefault != false) {
                    isFilterFail = true
                }
                if(filter.isSystemDefault && info.isSystemDefault != true) {
                    isFilterFail = true
                }
                info.thumb = app.baseUrl() + "snippet/templateThumb?uuid=${template.name}"
                if (isFilterFail == false) {
                    templates.add(info);
                }
            });
        }
        return templates
    }

    List<Map> getLocalSnippetTemplates(Map filter = [:]) {
        filter.isNotSystemDefault = true
        return getSystemSnippetTemplates(filter)
    }

    List<Map> getTemplateSnippetTemplates(Map filter = [:]) {
        try {
            String response = deployService.getDataFromTemplate("deploy/snippetTemplateList", filter)
            List<Map> list = JSON.parse(response)
            for (Map info : list) {
                info.repositoryType = SnippetConstants.SNIPPET_REPOSITORY_TYPE.TEMPLATE
            }
            return list
        } catch (Throwable t) {
            return []
        }
    }

    List<Map> getArchiveSnippetTemplates(Map filter = [:]) {
        filter.isSystemDefault = true
        return getSystemSnippetTemplates(filter)
    }

    List<Map> getStandardSnippetTemplates(Map filter = [:]) {
        List<Map> templates = getTemplateSnippetTemplates(filter)
        templates.addAll getArchiveSnippetTemplates(filter)
        return templates
    }

    Map getLocalSnippetTemplate(String uuid, Boolean withContent = false) {
        String localRepositoryPath = SnippetResourceTagLib.getModifiableResourceWebInfAbsolutePath()
        File infoFile = new File(localRepositoryPath, uuid + "/${SnippetResourceTagLib.SNIPPET_INFO_JSON}")
        Map info
        if(infoFile.exists()) {
           info = JSON.parse(infoFile.text)
           if(withContent) {
               File html = new File(localRepositoryPath, uuid + "/${SnippetResourceTagLib.SNIPPET_HTML}")
               if(html.exists()) {
                   info.html = html.text
               }
               File css = new File(localRepositoryPath, uuid + "/${SnippetResourceTagLib.SNIPPET_CSS}")
               if(css.exists()) {
                   info.css = css.text
               }
           }
           info.thumb_file_name = info.thumb
           info.thumb = app.baseUrl() + "snippet/templateThumb?uuid=${uuid}"
        }
        return info
    }

    String getSnippetTemplateContent(String repositoryType, String uuid) {
        return this."get${repositoryType.capitalize()}SnippetTemplateContent"(uuid)
    }

    String getLocalSnippetTemplateContent(String uuid) {
        File contentFile = new File(SnippetResourceTagLib.getModifiableResourceWebInfAbsolutePath(), uuid + "/snippet.html");
        return contentFile.text
    }

    String getTemplateSnippetTemplateContent(String uuid) {
        return deployService.getDataFromTemplate("deploy/snippetContent", [uuid: uuid])
    }

    String getArchiveSnippetTemplateContent(String uuid) {
        File contentFile = new File(SnippetResourceTagLib.getModifiableResourceWebInfAbsolutePath(), uuid + "/${SnippetResourceTagLib.SNIPPET_HTML}");
        return contentFile.text
    }

    String getSnippetTemplateCss(String repositoryType, String uuid) {
        return this."get${repositoryType.capitalize()}SnippetTemplateCss"(uuid)
    }

    String getLocalSnippetTemplateCss(String uuid) {
        File cssFile = new File(SnippetResourceTagLib.getModifiableResourceWebInfAbsolutePath(), uuid + "/${SnippetResourceTagLib.SNIPPET_CSS}");
        return cssFile.text
    }

    String getTemplateSnippetTemplateCss(String uuid) {
        return deployService.getDataFromTemplate("deploy/snippetCss", [uuid: uuid])
    }

    String getArchiveSnippetTemplateCss(String uuid) {
        File cssFile = new File(SnippetResourceTagLib.getModifiableResourceWebInfAbsolutePath(), uuid + "/${SnippetResourceTagLib.SNIPPET_CSS}")
        return cssFile.text
    }
}
