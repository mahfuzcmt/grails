package com.webcommander.plugin.reorder

import com.webcommander.constants.DomainConstants
import com.webcommander.models.ProductData
import com.webcommander.webcommerce.ProductService

class BootStrap {

    private final String REORDER = "reorder"

    List domain_constants = [
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "reorder", value: true],
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
    }

    def tenantDestroy = { tenant ->
        DomainConstants.removeConstant(domain_constants)
    }

    def init = { servletContext ->
        ProductData.metaClass.with {
            getIncludedProducts = {
                Long id = delegate.id
                ProductService productService = ProductService.getInstance()
                return productService.getIncludedProducts([id: id])
            }
        }
    }
}
