package com.webcommander.item

class ImportConf {
    String sheetName
    boolean isOverwrite
    String matchBy
    String parentMatchBy
    String imageMatchBy
    String videoMatchBy
    String imageSource
    String videoSource
    Map<String, Integer> fieldsMap

    Map extraData = [:]
}
