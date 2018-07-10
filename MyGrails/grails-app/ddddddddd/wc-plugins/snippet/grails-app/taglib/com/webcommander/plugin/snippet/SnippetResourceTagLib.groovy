package com.webcommander.plugin.snippet

import com.webcommander.AppResourceTagLib
import com.webcommander.manager.PathManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class SnippetResourceTagLib {

    static namespace = "appResource"

    private static final SNIPPET_TEMPLATES = "snippet-templates"
    public static final SNIPPET_TEMPLATE = "snippet-template"
    public static final SNIPPET_INFO_JSON= "info.json"
    public static final SNIPPET_HTML = "snippet.html"
    public static final SNIPPET_CSS ="snippet.css"


    public static final RESOURCES_PATH = [
            "SNIPPET" : "snippet"   ,
    ]


    public static String getModifiableResourceWebInfRelativePath(String identity = ""){
        return "${AppResourceTagLib.WEB_INF_MODIFIABLE_RESOURCES}/${getModifiableResourceRelativePath(identity)}"
    }

    public static String getModifiableResourceWebInfAbsolutePath(String identity = ""){
        return PathManager.getCustomRestrictedResourceRoot(getModifiableResourceRelativePath(identity))
    }


    public static String getWebInfSnippetSystemResource(){
        return PathManager.getRestrictedResourceRoot(SNIPPET_TEMPLATES)
    }

    public static String getModifiableResourceRelativePath(String identity = ""){
        identity = identity.equals("") ? "" : "/${identity}"
        return "${SNIPPET_TEMPLATES}${identity}"
    }

    public static String getResourceRelativePath(String id, String fileName= ""){
        return "${RESOURCES_PATH.SNIPPET}/${RESOURCES_PATH.SNIPPET}-${id}/$fileName"
    }

    public static String getResourceCssURL(String id) {
        return "${PathManager.resourceURLGenerator(AppResourceTagLib.RESOURCES, getResourceRelativePath(id, SNIPPET_CSS))}"
    }

    def snippetCssLink = { Map attrs, body ->
        String identifier = attrs["id"]
        out << "<link rel='stylesheet' href='${getResourceCssURL(identifier)}'>"
    }

    def snippetCssUrl = { Map attrs, body ->
        String identifier = attrs["id"]
        out << getResourceCssURL(identifier)
    }





}
