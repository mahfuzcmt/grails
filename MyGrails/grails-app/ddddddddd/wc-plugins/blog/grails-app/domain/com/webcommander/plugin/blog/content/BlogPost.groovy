package com.webcommander.plugin.blog.content

import com.webcommander.admin.Customer
import com.webcommander.admin.CustomerGroup
import com.webcommander.admin.Operator
import com.webcommander.common.MetaTag
import com.webcommander.models.blueprints.AbstractStaticResource
import com.webcommander.plugin.blog.app.BlogResourceTagLib
import com.webcommander.util.AppUtil

class BlogPost extends AbstractStaticResource {
    Long id;

    String name
    String url
    String content
    String visibility
    String visibleTo
    String image
    String baseUrl

    Boolean isPublished = false
    Boolean isInTrash = false
    Boolean isDisposable = false

    Operator author

    Date created
    Date updated
    Date date

    Collection<MetaTag> metaTags = []
    Collection<BlogCategory> categories = []
    Collection<BlogComment> comments = []
    Collection<Customer> customers = []
    Collection<CustomerGroup> groups = []

    static hasMany = [metaTags: MetaTag, categories : BlogCategory, comments : BlogComment, customers: Customer, groups: CustomerGroup]
    static belongsTo = [BlogCategory]


    static constraints = {
        name(blank: false, unique: true, size: 2..100)
        url(blank: false, unique: true, maxSize: 100)
        author(nullable: true)
        image(nullable: true)
        baseUrl(nullable: true)
        visibleTo(nullable: true)
        content(nullable: true)
        metaTags(nullable: true)
        categories(nullable: true)
        comments(nullable: true)
        customers(nullable: true)
        groups(nullable: true)
    }

    static mapping = {
        content type: "text"
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
        return this.image
    }

    @Override
    void setResourceName(String resourceName) {
        this.image = resourceName
    }

    @Override
    String getRelativeUrl() {
        return appResource.getBlogImageRelativeUrl(id)
    }
}
