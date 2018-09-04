package com.webcommander.oauth2

import com.webcommander.admin.Customer
import com.webcommander.admin.Operator

class OAuthAccess {
    Long id
    String uuid
    String code
    String accessToken
    String refreshToken

    Date created
    Date updated

    static belongsTo = [
            client: OAuthClient,
            operator: Operator,
            customer: Customer
    ]

    static constraints = {
        uuid(nullable: true, unique: true)
        accessToken(nullable: true)
        refreshToken(nullable: true)
        operator(nullable: true)
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

    static mapping = {
        cache(false)
    }
}