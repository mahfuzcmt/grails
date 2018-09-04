package com.webcommander.plugin.blog.content

import com.webcommander.models.blueprints.AbstractStaticResource

class BlogCategory extends AbstractStaticResource {
    Long id;

    String name
    String url
    String description
    String image
    String baseUrl

    Date created
    Date updated

    Boolean isInTrash = false
    Boolean isDisposable = false

    Collection<BlogPost> posts = []

    static hasMany = [posts : BlogPost]

    static constraints = {
        name(blank: false, unique: true, size: 2..100)
        url(blank: false, unique: true, maxSize: 100)
        image(nullable: true)
        baseUrl(nullable: true)
        description(nullable: true, maxSize: 2000)
        posts(nullable: true)
    }

    @Override
    String getBaseUrl() {
        return super.getBaseUrl()
    }

    @Override
    String getResourceName() {
        return this.image
    }

    @Override
    void setResourceName(String resourceName) {
        this.image = resourceName
    }

    @Override
    String getRelativeUrl() {
        return appResource.getBlogCategoryRelativeUrl(id)
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
    int hashCode() {
        if(id) {
            return ("BlogCategory: " + id).hashCode()
        }
        return super.hashCode()
    }

    @Override
    boolean equals(Object o) {
        if(! (o instanceof BlogCategory)) {
            return false
        }
        if(id && o.id) {
            return id == o.id
        }
        return super.equals(o);
    }

}
