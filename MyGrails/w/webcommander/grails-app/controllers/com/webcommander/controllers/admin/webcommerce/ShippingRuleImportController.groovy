package com.webcommander.controllers.admin.webcommerce

import com.webcommander.listener.SessionManager
import com.webcommander.task.Task
import com.webcommander.webcommerce.ImportService
import com.webcommander.webcommerce.ShippingService
import grails.converters.JSON
import org.apache.commons.io.FilenameUtils
import org.apache.poi.ss.usermodel.Workbook
import org.springframework.web.multipart.MultipartFile

class ShippingRuleImportController {
    ImportService importService
    ShippingService shippingService

    def uploadFileView() {
        render(view: "/admin/shipping/rule/import/uploadFileView", model: [dummy: true])
    }

    def uploadFile() {
        MultipartFile file = request.getFile("ruleImportFile");
        Long fileSize = Math.ceil(file.getSize() / (1024*1024));
        String fileExtension = FilenameUtils.getExtension(file.originalFilename);
        if(file.empty) {
            render([status: "error", message: g.message(code: "file.cannot.be.empty")] as JSON)
            return;
        } else if (fileSize > 100) {
            render([status: "error", message: g.message(code: "uploaded.file.size.larger.than.expected", args: [fileSize.toString() + "MB", "100MB"])] as JSON)
            return;
        } else if(!["xlsx", "xls", "csv"].contains(fileExtension)) {
            render([status: "error", message: g.message(code: "uploaded.file.extension.is.expected", args: [fileExtension , "xlsx"])] as JSON)
            return;
        }
        def ref = importService.getImportRefForFile(file, fileExtension);
        session.rule_import_ref = ref;
        ref = ImportService.IMPORT_READER_REF_HOLDER[ref]

        Map sheetRows = [:];
        ref.each {
            sheetRows[it.getSheetName()] = it.getPhysicalNumberOfRows();
        }
        def sheetNames = importService.readSheetNames(ref);

        String ruleSheet = sheetNames.find {
            if(it.toUpperCase().contains('RULE')) {
                return it
            }
        }

        String rateSheet = sheetNames.find {
            if(it.toUpperCase().contains('RATE')) {
                return it
            }
        }

        String zoneSheet = sheetNames.find {
            if(it.toUpperCase().contains('ZONE')) {
                return it
            }
        }

        String html = g.include(
                view: "/admin/shipping/rule/import/configImportedDataMap.gsp",
                model: [
                        sheetNames: sheetNames,
                        ruleSheet: ruleSheet,
                        rateSheet: rateSheet,
                        zoneSheet: zoneSheet,
                        sheetRows: sheetRows
                ]
        ).toString();
        render([status: "success", html: html]  as JSON)
    }

    def initImport() {
        Workbook ref = (Workbook) ImportService.IMPORT_READER_REF_HOLDER[session.rule_import_ref]
        Task task = shippingService.initImport(params, ref)
        task.meta.logger_dump_file = session.rule_import_ref
        task.meta.task_cache_location = SessionManager.protectedTempFolder.absolutePath + "/" + task.token + "/rule_import.job"
        task.meta.cache_file = "rule_import.job"
        render([token: task.token, name: task.name] as JSON)
    }

    def export() {
        response.setHeader("Content-disposition", "attachment; filename=\"${new Date().gmt().toZone(session.timezone).toString()}.xlsx\"")
        response.setHeader("Content-Type", "application/vnd.ms-excel")
        shippingService.export(response.outputStream)
    }
}
