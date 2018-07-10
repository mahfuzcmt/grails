package com.webcommander.plugin.xero

class XeroTrack {
    Long id
    Long componentId
    String xeroId
    String linkComponent
    String xeroVersion
    String xeroOrganisationId

    Date created
    Date updated

    static constraints = {
        componentId unique: "linkComponent"
    }

    def beforeValidate() {
        if(!created) {
            created = new Date()
        }
        updated = new Date()

        if(!xeroOrganisationId) {
            xeroOrganisationId = XeroClient.currentOrganisation
        }
    }

    def beforeUpdate() {
        updated = new Date()
    }
}
