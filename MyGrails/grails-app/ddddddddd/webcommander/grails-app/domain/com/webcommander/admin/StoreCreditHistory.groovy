package com.webcommander.admin


class StoreCreditHistory {

    Long id
    Double deltaAmount
    String note
    Date created
    Operator createdBy

    Customer customer

    static belongsTo = [customer: Customer]

    static constraints = {
        note(nullable: true, maxSize: 500)
        createdBy(nullable: true)
    }

    def beforeValidate(){
        if(!created){
            created = new Date().gmt()
        }
    }
}
