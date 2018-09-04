package com.webcommander.plugin.discount.webcommerce

import com.webcommander.admin.Customer
import com.webcommander.admin.CustomerGroup
import com.webcommander.webcommerce.Category
import com.webcommander.webcommerce.Product

/**
 * Created by sharif ul islam on 06/03/2018.
 */
class DiscountAssoc {

    Long id

    Boolean isAppliedAllCustomer = false
    Boolean isAppliedAllProduct = false

    Collection<Customer> customers = []
    Collection<CustomerGroup> customerGroups = []
    Collection<Product> products = []
    Collection<Category> categories = []

    static copy_reference = ["customers", "customerGroups", "products", "categories"]

    static constraints = {
        customers(nullable: true)
        customerGroups(nullable: true)
        products(nullable: true)
        categories(nullable: true)
    }

    static hasMany = [customers: Customer, customerGroups: CustomerGroup, products: Product, categories: Category]

    static mapping = {
        customers cache: true
        customerGroups cache: true
        products cache: true
        categories cache: true
    }

    def initiate() {
        customers.clear()
        customerGroups.clear()
        products.clear()
        categories.clear()
    }
}
