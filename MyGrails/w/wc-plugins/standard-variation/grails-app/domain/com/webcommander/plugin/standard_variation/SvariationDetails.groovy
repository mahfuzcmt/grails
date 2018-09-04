package com.webcommander.plugin.standard_variation

class SvariationDetails {

    Long id
    String priceAdjustableType = "b" // b, f, a, r
    Double price = 0.0
    String imageId

    static constraints = {
        priceAdjustableType(maxSize: 2)
        imageId(nullable: true, maxSize: 10)
    }
}