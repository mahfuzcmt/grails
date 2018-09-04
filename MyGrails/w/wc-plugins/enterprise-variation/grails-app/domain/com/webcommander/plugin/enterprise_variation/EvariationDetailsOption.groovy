package com.webcommander.plugin.enterprise_variation

class EvariationDetailsOption {

    Long id
    String field
    String value
    EvariationDescription description

    static belongsTo = [evariationDetails: EvariationDetails]

    static constraints = {
        field(maxSize: 100)
        value(nullable: true, maxSize: 510)
        description(nullable: true)
    }

}
