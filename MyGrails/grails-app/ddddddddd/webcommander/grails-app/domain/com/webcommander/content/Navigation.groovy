package com.webcommander.content

class Navigation {
    Long id
    String name

    Date created
    Date updated

    Boolean hideRestrictedItem = false
    Boolean isInTrash = false
    Boolean isDisposable = false

    Collection<NavigationItem> items = []

    static hasMany = [items: NavigationItem]

    static constraints = {
        name unique: true, maxSize: 100
        items nullable: true
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

    public static void initialize() {
        if(Navigation.countByName("Main Menu") == 0) {
            new Navigation(name: "Main Menu").save()
        }
    }
}
