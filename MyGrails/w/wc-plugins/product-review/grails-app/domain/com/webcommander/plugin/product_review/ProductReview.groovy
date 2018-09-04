package com.webcommander.plugin.product_review

import com.webcommander.admin.Customer
import com.webcommander.webcommerce.Product

class ProductReview {

    Long id
    String name
    String email
    String review
    Double rating = 0.0
    Boolean isActive = false
    Date created
    Date updated

    static belongsTo = [product: Product, customer: Customer]

    static mapping = {
        review type: "text"
    }

    static constraints = {
        name(nullable: true)
        email(nullable: true)
        review(nullable: true)
        customer(nullable: true)
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

    static fieldMarshaller = [
        email: { ProductReview review ->
            if(review.customer) {
                return review.customer.userName
            }
            return review.email
        },
        name: { ProductReview review ->
            if(review.customer) {
                return review.customer.fullName
            }
            return review.name
        }
    ]

    static marshallerExclude = ["customer"]
}
