package com.webcommander.common

class LargeData {
    Long id
    String name
    String identifire
    byte[] content

    static constraints = {
        identifire(unique: true)
        name(nullable: true)
    }

    static mapping = {
        content length: 16777300
    }
}