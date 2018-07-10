package com.webcommander.report

class FavouriteReport {
    Long id

    String filters
    String name
    String type

    Date created
    Date updated

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
