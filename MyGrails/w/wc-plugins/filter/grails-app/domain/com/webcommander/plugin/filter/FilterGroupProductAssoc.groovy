package com.webcommander.plugin.filter

import com.webcommander.webcommerce.Product

/**
 * Created by sharif ul islam on 09/04/2018.
 */
class FilterGroupProductAssoc {
    Long id

    Date created
    Date updated

    Product product
    FilterGroupItem item

    static constraints = {
        product nullable: false
        item nullable: false
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

}
