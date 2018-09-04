package com.webcommander.config

import com.webcommander.webcommerce.Product

class StoreProductAssoc {
    Long id
    Boolean isActive = true
    Double price = 0.0 // Price & available stock effect will implement later for now hide
    Integer availableStock = 0
    StoreDetail store
    Product product
}
