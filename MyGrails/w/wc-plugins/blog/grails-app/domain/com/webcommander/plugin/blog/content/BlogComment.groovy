package com.webcommander.plugin.blog.content

class BlogComment {
    Long id
    String name
    String email
    String status = 'pending'

    String content

    Boolean isSpam = false
    Date created
    Date updated
    BlogComment parent
    Collection<BlogReaction> reactions = []

    static belongsTo = [post: BlogPost]
    static hasMany = [replies: BlogComment, reactions : BlogReaction]

    static constraints = {
        name (nullable: true, size: 2..100)
        email (nullable: true, maxSize: 50)
        content (blank: false, maxSize: 1000)
    }

    static mapping = {
        reactions (cascade: 'all-delete-orphan')
        replies (cascade: 'all-delete-orphan')
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