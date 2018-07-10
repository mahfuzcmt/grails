package com.webcommander.plugin.my_shopping

class MyShoppingMapping {

    Long id
    Long categoryId
    String myShoppingCategory
    String path

    static constraints = {
        categoryId(unique: true, blank: false)
        myShoppingCategory(blank: false)
        path(blank: false)
    }
}
