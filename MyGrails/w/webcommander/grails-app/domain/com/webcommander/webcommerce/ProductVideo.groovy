package com.webcommander.webcommerce

import com.webcommander.AppResourceTagLib
import com.webcommander.models.blueprints.AbstractStaticResource

class ProductVideo extends AbstractStaticResource {

    Long id
    String name
    String baseUrl
    String title
    String description
    Integer idx

    Product product

    static belongsTo = [Product]

    static transients = ['findUrlInfix', "thumbImage"]

    static constraints = {
        title(nullable: true)
        baseUrl(nullable: true)
        description(nullable: true, maxSize: 255)
    }

    @Override
    boolean equals(Object obj) {
        if (!obj instanceof ProductVideo) {
            return false;
        }
        if (this.id) {
            return id == obj.id
        }
        return super.equals(obj)
    }

    @Override
    int hashCode() {
        if (this.id) {
            return ("ProductVideo: " + id).hashCode()
        }
        return super.hashCode();
    }

    String findUrlInfix() {
        return appResource.getProductVideoInfix(getTenantId(), product.id)
    }

    String getThumbImage() {
        String thumb = name.substring(0, name.lastIndexOf(".")) + ".jpg"
        return app.customResourceBaseUrl() + findUrlInfix() + AppResourceTagLib.VIDEO_THUMB + "/" + thumb
    }

    @Override
    String getResourceName() {
        return name
    }

    @Override
    void setResourceName(String resourceName) {
        this.name = resourceName
    }

    @Override
    String getRelativeUrl() {
        return appResource.getProductRelativeUrl(product.id)
    }
}
