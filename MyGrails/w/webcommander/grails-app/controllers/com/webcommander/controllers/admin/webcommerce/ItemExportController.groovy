package com.webcommander.controllers.admin.webcommerce

import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.webcommerce.ExportService

class ItemExportController {
    ExportService exportService

    @Restrictions([
            @Restriction(permission = "product.export.excel"),
            @Restriction(permission = "category.export.excel")
    ])
    def initExport() {
        render(view: "/admin/item/export/exportForm")
    }

    @Restrictions([
            @Restriction(permission = "product.export.excel"),
            @Restriction(permission = "category.export.excel")
    ])
    def export() {
        response.setHeader("Content-disposition", "attachment; filename=\"${new Date().gmt().toZone(session.timezone).toString()}.xlsx\"")
        response.setHeader("Content-Type", "application/vnd.ms-excel")
        exportService.export(params, response.outputStream)
    }
}
