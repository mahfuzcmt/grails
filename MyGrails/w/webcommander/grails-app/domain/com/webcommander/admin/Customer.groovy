package com.webcommander.admin

import com.webcommander.webcommerce.Address

class Customer {

    Long id

    String firstName
    String lastName
    String sex
    String userName
    String password
    String status
    String abn
    String abnBranch
    String companyName
    String howDoYouKnow // how he know about us
    String referralCode // his won code, that he can share with other
    String usedReferralCode // he used in sign up
    Integer countReferralCodeUsed = 0; // count how many times used his referral code
    String defaultTaxCode

    Boolean isCompany = false
    Boolean isInTrash = false

    Double storeCredit = 0.0

    Date created
    Date updated

    Collection<Address> billingAddresses
    Collection<Address> shippingAddresses
    Collection<CustomerGroup> groups
    Collection<StoreCreditHistory> storeCreditHistories

    Address address
    Address activeBillingAddress
    Address activeShippingAddress

    static marshallerExclude = ["isInTrash", "groups", "created", "updated", "storeCreditHistories", "password"]

    static hasMany = [billingAddresses: Address, shippingAddresses: Address, groups: CustomerGroup, storeCreditHistories: StoreCreditHistory]
    static belongsTo = [CustomerGroup]
    static transients = ['fullName']

    static constraints = {
        firstName(blank: false, size: 2..100, maxSize: 100)
        userName(blank: false, unique: true, maxSize: 100)
        lastName(maxSize: 100, nullable: true)
        password(blank: false, size: 4..50)
        sex(nullable: true, maxSize: 10)
        abn(nullable: true)
        abnBranch(nullable: true)
        status(maxSize: 1)
        storeCredit(nullable: true, max: 99999999D)
        companyName(nullable: true)
        howDoYouKnow(nullable: true)
        referralCode(nullable: true)
        usedReferralCode(nullable: true)
        billingAddresses(nullable: true)
        shippingAddresses(nullable: true)
        defaultTaxCode(nullable: true)
    }

    def beforeValidate() {
        if(!this.created) {
            this.created = new Date().gmt()
        }
        if(!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }

    @Override
    boolean equals(Object obj) {
        return obj instanceof Customer ? (this.id ? this.id == obj.id : false) : super.equals(obj)
    }

    @Override
    int hashCode() {
        return this.id ? ("Customer#" + id).hashCode() : super.hashCode()
    }


    String fullName() {
        return (firstName ?: "") + (lastName? " " + lastName : "")
    }

    String getFullName() {
        return firstName + ( lastName ? " " + lastName : "")
    }

    String getName() {
        return fullName
    }

}