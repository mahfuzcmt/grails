package com.webcommander.common

class MetaTag {

    Long id

    String name
    String value

    static constraints = {
        name(maxSize: 100)
        value(maxSize: 1000)
    }

    @Override
    boolean equals(Object obj) {
        if (!obj instanceof MetaTag) {
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
            return ("MetaTag: " + id).hashCode()
        }
        return super.hashCode();
    }
}
