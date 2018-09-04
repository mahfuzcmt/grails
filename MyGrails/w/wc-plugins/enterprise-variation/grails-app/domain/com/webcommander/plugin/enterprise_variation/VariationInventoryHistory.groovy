package com.webcommander.plugin.enterprise_variation

import com.webcommander.admin.Operator

class VariationInventoryHistory {

    Long id
    Integer changeQuantity
    String note
    Date created
    Operator createdBy

    static belongsTo = [evariationDetails: EvariationDetails]

    static constraints = {
        note(nullable: true)
        createdBy(nullable: true)
    }

    def beforeValidate(){
        if(!created){
            created = new Date().gmt()
        }
    }

    public static void onUserDelete(Long id) {
        VariationInventoryHistory.executeUpdate("update VariationInventoryHistory p set p.createdBy = null where p.createdBy.id = :uid", [uid: id]);
    }

    @Override
    boolean equals(Object obj) {
        if (!obj instanceof VariationInventoryHistory) {
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
            return ("EnterpriseProductInventoryHistory: " + id).hashCode()
        }
        return super.hashCode();
    }
}
