package com.webcommander.design

class Resolution {
    Long id

    Integer min
    Integer max

    static constraints = {
        min(unique: "max", nullable: true)
        max nullable: true
    }

    public static initialize() {
        List resolution = [
            [992, 1200],
            [768, 991],
            [null, 767]
        ]
        if(Resolution.count == 0) {
            resolution.each {
                new Resolution(min: it[0], max: it[1]).save()
            }
        }
    }

    public static List getResolutionForSelection() {
        List resolutions = [[key: "global", label: "global"]];
        resolutions.addAll(Resolution.list().collect {
            String value = it.min ? "Min ${it.min} ->" : ""
            value += it.max ? ("Max ${it.max}") : ""
            return [key: it.id + "", label: value]
        })
        return resolutions
    }
}
