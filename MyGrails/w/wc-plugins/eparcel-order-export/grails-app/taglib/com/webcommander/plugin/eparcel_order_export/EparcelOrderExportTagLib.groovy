package com.webcommander.plugin.eparcel_order_export

class EparcelOrderExportTagLib {

    static namespace = "eparcel"

    def adminJss = { attrs, body ->
        out << body()
        out << "<script type='text/javascript' src='${app.systemResourceBaseUrl()}plugins/eparcel-order-export/js/admin/eparcel-order-export.js'></script>"
    }
}
