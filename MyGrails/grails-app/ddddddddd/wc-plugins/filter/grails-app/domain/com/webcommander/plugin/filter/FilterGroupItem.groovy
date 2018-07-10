package com.webcommander.plugin.filter

import com.webcommander.models.blueprints.AbstractStaticResource
import grails.converters.JSON

/**
 * Created by sharif ul islam on 09/04/2018.
 */
class FilterGroupItem extends AbstractStaticResource {

    Long id
    Long externalId

    Integer idx

    String title
    String itemUrl
    String url
    String heading
    String detailDescription
    String shortDescription
    String image
    String imageBaseUrl
    String imageAlt

    Date created
    Date updated

    static transients = ['baseUrl', 'resourceName', 'relativeUrl']

    static belongsTo = [filterGroup: FilterGroup]

    static constraints = {
        detailDescription nullable: true
        shortDescription nullable: true
        image nullable: true
        imageBaseUrl nullable: true
        title maxSize: 100
        heading maxSize: 200
        url(blank: false, unique: true, maxSize: 200)
        itemUrl(nullable: true)
        imageAlt maxSize: 100
        externalId(nullable: true)
    }

    static mapping = {
        shortDescription type: "text"
        detailDescription type: "text"
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

    String toJSON() {
        return [id: id, idx: idx, title: title, url: url, heading: heading, detailDescription: detailDescription, shortDescription: shortDescription, imageAlt: imageAlt] as JSON
    }

    @Override
    boolean equals(Object o) {
        if(!(o instanceof FilterGroupItem)) {
            return false;
        }
        if(id && o.id) {
            return id == o.id
        }
        if(itemUrl && o.itemUrl) {
            return itemUrl == o.itemUrl
        }

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
        return appResource.getFilterGroupItemRelativeUrl(id)
    }

    String getImageAlt() {
        return imageAlt ? imageAlt : title
    }

}
