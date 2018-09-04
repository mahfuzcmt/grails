package com.webcommander.webcommerce

import com.webcommander.admin.Operator

class ProductInventoryAdjustment {
    Long id
    Integer changeQuantity
    String note
    Date created
    Operator createdBy

    Product product

    static belongsTo = [product: Product]

    static constraints = {
        note(nullable: true)
        createdBy(nullable: true)
    }

    def beforeValidate(){
        if(!created){
            created = new Date().gmt()
        }
    }

    @Override
    boolean equals(Object obj) {
        if (!obj instanceof ProductInventoryAdjustment) {
            return false;
        }
        if (this.id) {
            return id == obj.id
        }
        return super.equals(obj)
    }

    @Override
    int hashCode() {
        if (this.id) {
            return ("ProductInventoryAdjustment: " + id).hashCode()
        }
        return super.hashCode();
    }
}
