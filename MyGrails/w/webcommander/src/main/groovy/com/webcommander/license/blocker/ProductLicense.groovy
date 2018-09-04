package com.webcommander.license.blocker

import com.webcommander.models.License
import com.webcommander.webcommerce.Product
import grails.web.servlet.mvc.GrailsParameterMap

/**
 * Created by zobair on 08/02/2015.
 */
class ProductLicense {
    public static isApplicable(String license, GrailsParameterMap params) {
        if(!params.id) {
            return true
        } else if(params.action == "copyProduct") {
            return Product.findByIdAndIsActive(params.long("id") ?: 0, true)
        } else if(params.active.toBoolean(true)) {
            Product product = Product.findById(params.long("id") ?: 0)
            if(product && !product.isActive) {
                return true
            }
        }
    }

    public static int limitCheck(License license, GrailsParameterMap params) {
        int currentCount = Product.countByIsActive(true)
        if(currentCount < license.limit) {
            return 0
        }
        if(license.isLimitExtensible) {
            return 2
        }
        return 1
    }
}