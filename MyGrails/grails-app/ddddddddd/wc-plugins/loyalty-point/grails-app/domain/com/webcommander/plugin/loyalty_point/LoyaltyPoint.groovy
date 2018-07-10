package com.webcommander.plugin.loyalty_point

import com.webcommander.webcommerce.Category
import com.webcommander.webcommerce.Product

class LoyaltyPoint {

    Long id
    Category category;
    Product product;
    Long variationDetailsId
    Long point = 0;

    static constraints = {
        category(nullable: true, unique: true);
        product(nullable: true, unique: true);
        variationDetailsId(nullable: true, unique: true);
    }

}
