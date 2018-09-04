package com.webcommander.plugin.multi_level_pricing

import com.webcommander.admin.CustomerGroup

class ProductPrice {
    Long id
    Double price
    Collection<CustomerGroup> customerGroups = []

    static hasMany = [customerGroups: CustomerGroup]

    static belongsTo = [multiLevelPrice: ProductMultiLevelPrice]
}
