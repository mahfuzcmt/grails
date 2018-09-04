package com.webcommander.plugin.eparcel_order_export.controllers

import com.webcommander.authentication.annotations.License
import com.webcommander.plugin.eparcel_order_export.EparcelOrderExportService

class EparcelOrderExportController {

    EparcelOrderExportService eparcelOrderExportService

    def loadExportPrerequisite() {
        render(view: "/plugins/eparcel_order_export/admin/loadExportPrerequisite", model: [a:"a"])
    }

    @License(required = "allow_eparcel_order_export_feature")
    def export() {
        try {
            eparcelOrderExportService.writeToOutputStream(params, response)
        } catch(RuntimeException exc) {
            render(text: g.message(code: exc.message))
        } catch(Exception exc) {
            render(text: g.message(code: "export.failed"))
        }
    }
}
