package com.webcommander.admin

class DashletContent {

    Long id

    Long idx
    String contentId
    String title
    String uiClass
    Dashlet holder
    Boolean isVisible = true

    static constraints = {
        title(nullable: true, maxSize: 100)
        contentId(nullable: true)
        uiClass (nullable: true)
    }

    @Override
    boolean equals(Object obj) {
        if (!obj instanceof DashletContent)
            return false;
        if (obj.id && id) {
            return id == obj.id
        }
        return super.equals(obj);
    }

    @Override
    int hashCode() {
        if (id) {
            return ("DashletContent: " + id).hashCode()
        }
        if (title) {
            return ("DashletContent: " + title).hashCode()
        }
        return super.hashCode()
    }

}
