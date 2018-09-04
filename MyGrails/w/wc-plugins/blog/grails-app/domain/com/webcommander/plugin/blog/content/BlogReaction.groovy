package com.webcommander.plugin.blog.content

import com.webcommander.admin.Customer
import com.webcommander.plugin.blog.constants.DomainConstants

class BlogReaction {
    Long id
    String type = DomainConstants.BLOG_REACTION.LIKE

    Date created
    Date updated

    BlogComment blogComment
    BlogPost blogPost
    Customer customer

    static constraints = {
        blogComment(nullable: true)
        blogPost(nullable: true)

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



}