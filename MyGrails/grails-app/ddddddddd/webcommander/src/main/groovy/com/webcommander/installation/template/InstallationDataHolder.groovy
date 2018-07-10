package com.webcommander.installation.template

import com.webcommander.models.TemplateData

class InstallationDataHolder {
    private Map CONTENT_FIELD_MAPPINGS
    List<String> COPIED_STATIC_CONTENTS
    public TemplateData templateData

    public InstallationDataHolder() {
        CONTENT_FIELD_MAPPINGS = [:]
        COPIED_STATIC_CONTENTS = []
    }

    public void setContentMapping(def contentType, def contentId, String fieldName, def newValue) {
        if (!CONTENT_FIELD_MAPPINGS[contentType]) {
            CONTENT_FIELD_MAPPINGS[contentType] = [:]
        }
        if(!CONTENT_FIELD_MAPPINGS[contentType][contentId + ""]) {
            CONTENT_FIELD_MAPPINGS[contentType][contentId + ""] = [:]
        }
        CONTENT_FIELD_MAPPINGS[contentType][contentId + ""][fieldName] = newValue
    }

    public def getContentMapping(String contentType, def contentId, String fieldName) {
        if(contentId) {
            return CONTENT_FIELD_MAPPINGS?.get(contentType)?.get(contentId + "")?.get(fieldName)
        } else {
            return null
        }
    }

    public def getContentMappings(String contentType, List contentIds, String filedName) {
        List fields = []
        if(!contentIds) {
           return fields
        }
        contentIds.each {
            def field = this.getContentMapping(contentType, it, filedName)
            if(field) {
                fields.add(field)
            }
        }
        return fields
    }
}
