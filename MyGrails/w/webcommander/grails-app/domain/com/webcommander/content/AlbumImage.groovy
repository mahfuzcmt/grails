package com.webcommander.content

import com.webcommander.models.blueprints.AbstractStaticResource
import grails.util.Holders

class AlbumImage extends AbstractStaticResource {

    Long id
    String name
    String baseUrl
    Album parent

    String altText
    String description
    String linkType
    String linkTo
    String linkTarget

    Integer idx

    Date created
    Date updated

    static constraints = {
        altText(nullable: true)
        description(nullable: true, maxSize: 255)
        linkType(nullable: true)
        linkTo(nullable: true)
        linkTarget(nullable: true)
    }

    static transients = ['imageLink', 'baseUrl', 'resourceName', 'relativeUrl']

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

    public String imageLink() {
        return Holders.grailsApplication.mainContext.getBean("navigationService").getUrl(linkType, linkTo)
    }

    @Override
    String getBaseUrl(){
        return super.getBaseUrl()
    }

    @Override
    String getResourceName() {
        return this.name
    }

    @Override
    void setResourceName(String resourceName) {
        this.name = resourceName
    }

    @Override
    String getRelativeUrl() {
        return appResource.getAlbumRelativeUrl(this.parent.id)
    }
}
