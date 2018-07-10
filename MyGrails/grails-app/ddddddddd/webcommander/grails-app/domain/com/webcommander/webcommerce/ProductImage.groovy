package com.webcommander.webcommerce

import com.webcommander.models.blueprints.AbstractStaticResource


class ProductImage extends AbstractStaticResource {

    Long id
    String baseUrl
    String name
    String altText
    Integer idx

    Product product

    static belongsTo = [Product]

    static transients = ['findUrlInfix', 'resourceName', 'relativeUrl']

    static constraints = {
        altText(nullable: true)
        baseUrl(nullable: true)
    }

    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof ProductImage)) {
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
            return ("ProductImage: " + id).hashCode()
        }
        return super.hashCode()
    }

    String findUrlInfix() {
        return appResource.getProductImageInfix(getTenantId(), product.id)
    }

    @Override
    String getBaseUrl() {
        return super.getBaseUrl()
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

    @Override
    List<String> getPrefixes() {
        return ["150", "300", "450", "600", "900"]
    }
}
