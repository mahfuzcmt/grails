package com.webcommander.content

import com.webcommander.models.blueprints.AbstractStaticResource
import grails.converters.JSON

class NavigationItem extends AbstractStaticResource {

    Long id
    Integer idx
    String itemType
    String itemRef
    String label
    String target
    String image
    String imageAlt
    String imageBaseUrl
    Date created
    Date updated

    NavigationItem parent

    Collection<NavigationItem> childItems = []

    static hasMany = [childItems: NavigationItem]

    static transients = ['childItems', 'baseUrl', 'resourceName', 'relativeUrl']

    static belongsTo = [navigation: Navigation]

    static constraints = {
        parent nullable: true
        image nullable: true
        imageAlt nullable: true
        imageBaseUrl nullable: true
        label maxSize: 100
        navigation nullable: true
    }

    def beforeValidate() {
        if(!this.created) {
            this.created = new Date().gmt()
        }
        if(!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }

    public String toJSON() {
        return [id: id, idx: idx, itemType: itemType, itemRef: itemRef, label: label, parent: parent?.id, target: target] as JSON
    }

    @Override
    String getBaseUrl() {
        return super.getBaseUrl()
    }

    @Override
    void setBaseUrl(String baseUrl) {
        this.imageBaseUrl = baseUrl
    }

    @Override
    String getResourceName() {
        return image
    }

    @Override
    void setResourceName(String resourceName) {
        image = resourceName
    }

    @Override
    String getRelativeUrl() {
        return appResource.getNavigationItemRelativeUrl(id)
    }
}
