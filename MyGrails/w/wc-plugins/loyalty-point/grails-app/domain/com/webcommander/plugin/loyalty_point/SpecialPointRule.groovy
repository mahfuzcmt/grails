package com.webcommander.plugin.loyalty_point

import com.webcommander.admin.Customer
import com.webcommander.admin.CustomerGroup

class SpecialPointRule {
    Long id
    String name
    Integer point
    String ruleType

    List<Customer> customers = []
    List<CustomerGroup> customerGroups = []

    static transients = ['customerIds', 'customerGroupIds']

    static hasMany = [
        customers : Customer,
        customerGroups : CustomerGroup
    ]

    static mapping = {
        customers fetch: 'join'
        customerGroups fetch: 'join'
    }

    static constraints = {
        name(nullable: false)
        point(nullable: false)
        ruleType(nullable: false)
        customers(nullable: true)
        customerGroups(nullable: true)
    }

    List<Long> getCustomerIds() {
        return customers.collect() {
            it.id
        }
    }

    List<Long> getCustomerGroupIds() {
        return customerGroups.collect() {
            it.id
        }
    }
}
