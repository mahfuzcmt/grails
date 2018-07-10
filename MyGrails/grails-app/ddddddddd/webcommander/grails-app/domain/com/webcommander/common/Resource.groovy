package com.webcommander.common

import com.webcommander.models.blueprints.AbstractStaticResource

class Resource extends AbstractStaticResource{

    String name
    String baseUrl

    static constraints = {
        name(nullable: false)
        baseUrl(nullable: true)
    }

    @Override
    String getResourceName() {
        return name
    }

    @Override
    void setResourceName(String resourceName) {
        name = resourceName
    }

    @Override
    String getRelativeUrl() {
        return null
    }

    @Override
    String getBaseUrl(){
        return super.getBaseUrl()
    }
}
