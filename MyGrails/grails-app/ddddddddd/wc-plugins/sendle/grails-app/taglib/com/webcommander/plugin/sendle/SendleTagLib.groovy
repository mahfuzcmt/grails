package com.webcommander.plugin.sendle

class SendleTagLib {

    static namespace = "sendle"

    def addShipmentBlock = { attrs, body ->
        out << body()
        out << g.include(view: "/plugins/sendle/admin/addShipment.gsp", model: []).toString()
    }

}
