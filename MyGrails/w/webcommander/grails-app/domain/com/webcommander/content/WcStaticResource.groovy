package com.webcommander.content

import com.webcommander.models.blueprints.AbstractStaticResource

class WcStaticResource extends AbstractStaticResource {
    Long id

    String resourceId
    String resourceName
    String baseUrl
    String relativeUrl

    static constraints = {
        resourceId unique: true, blank: false
        baseUrl nullable: true
    }
}