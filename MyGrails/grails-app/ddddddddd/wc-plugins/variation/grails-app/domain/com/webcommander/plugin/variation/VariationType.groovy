package com.webcommander.plugin.variation

public class VariationType {
    Long id

    String name
    String standard //text, color, image

    Boolean isDisposable = false

    Collection<VariationOption> options = []

    static hasMany = [options : VariationOption]

    static constraints = {
        name(maxSize: 100, unique: true)
    }
}