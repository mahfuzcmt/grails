package com.webcommander.content

import com.webcommander.admin.Operator

class Article {

    Long id
    Boolean isPublished = true
    String name
    String url
    String content
    String summary
    Date created
    Date updated
    Boolean isInTrash = false
    Boolean isDisposable = false

    Operator createdBy
    Section section

    static constraints = {
        name(blank: false, unique: true, size: 2..100)
        url(unique: true, blank: false)
        content(nullable: true)
        summary(nullable: true, maxSize: 500)
        section(nullable: true)
        createdBy(nullable: true)
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
    boolean equals(Object o){
        if(!o instanceof Article){
            return false
        }
        if(id && o.id){
            return id == o.id
        }
        if(name){
            return name == o.name
        }
        return super.equals(o);
    }

    @Override
    int hashCode(){
        if (id) {
            return ("Article: " + id).hashCode();
        }
        if (name) {
            return ("Article: " + name).hashCode();
        }
        return super.hashCode()
    }

}
