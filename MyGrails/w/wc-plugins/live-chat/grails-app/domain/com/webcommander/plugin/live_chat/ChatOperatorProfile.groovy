package com.webcommander.plugin.live_chat

import com.webcommander.models.blueprints.AbstractStaticResource

class ChatOperatorProfile extends AbstractStaticResource{
    Long id
    Long operatorId
    String displayName
    String profileImage
    String baseUrl
    Integer chatLimit

    Date created
    Date updated

    static constraints = {
        operatorId(unique: true)
        displayName(nullable: true);
        chatLimit(nullable: true);
        profileImage(nullable: true);
        baseUrl(nullable: true)
    }

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
        if (!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }

    @Override
    String getBaseUrl() {
        return super.getBaseUrl()
    }

    @Override
    String getResourceName() {
        return this.profileImage
    }

    @Override
    void setResourceName(String resourceName) {
        this.profileImage = resourceName
    }

    @Override
    String getRelativeUrl() {
        return appResource.getLiveChatOperatorImageRelativeUrl(id)
    }
}