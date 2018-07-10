package com.webcommander.content

class Album {

    Long id
    String name
    String description
    Integer thumbX = 200
    Integer thumbY = 200
    Boolean isInTrash = false
    Boolean isDisposable = false

    Date created
    Date updated

    static constraints = {
        name(unique: true, size: 2..100)
        description(nullable: true, maxSize: 255)
    }

    static transients = ['imageCount']

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

    Integer imageCount() {
        return AlbumImage.createCriteria().count {
            eq("parent.id", id)
        }
    }

}
