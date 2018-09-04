package com.webcommander.controllers.admin.webcommerce

import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.constants.NamedConstants
import com.webcommander.listener.SessionManager
import com.webcommander.task.MultiLoggerTask
import com.webcommander.task.Task
import com.webcommander.task.TaskService
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.ImportService
import grails.converters.JSON
import grails.util.Holders
import org.apache.commons.io.FilenameUtils
import org.apache.poi.ss.usermodel.Workbook
import org.springframework.web.multipart.MultipartFile

class ItemImportController {

    ImportService importService
    TaskService taskService

    @Restrictions([
            @Restriction(permission = "product.import.excel"),
            @Restriction(permission = "category.import.excel")
    ])
    def uploadFileView() {
        render(view: "/admin/item/import/uploadFileView", model: [dummy: true])
    }

    def uploadFile() {
        MultipartFile file = request.getFile("itemImportFile");
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
        session.item_import_ref = ref;
        ref = ImportService.IMPORT_READER_REF_HOLDER[ref]

        Map sheetRows = [:];
        ref.each {
            sheetRows[it.getSheetName()] = it.getPhysicalNumberOfRows();
        }
        def sheetNames = importService.readSheetNames(ref);

        String productSheet = sheetNames.find {
            if(it.toUpperCase().contains('PROD')) {
                return it
            }
        }
        List<String> productColumns = importService.getColumnsBySheetName(ref, productSheet)

        String categorySheet = sheetNames.find {
            if(it.toUpperCase().contains('CAT')) {
                return it
            }
        }
        List<String> categoryColumns = importService.getColumnsBySheetName(ref, categorySheet);
        String html = g.include(
            view: "/admin/item/import/configImportedDataMap.gsp",
            model: [
                sheetNames: sheetNames,
                productSheet: productSheet,
                categorySheet: categorySheet,
                productColumns: productColumns,
                categoryColumns: categoryColumns,
                categoryFields: NamedConstants.CATEGORY_IMPORT_FIELDS,
                productFields: NamedConstants.PRODUCT_IMPORT_FIELDS + NamedConstants.PRODUCT_IMPORT_EXTRA_FIELDS,
                sheetRows: sheetRows
            ]
        ).toString();
        render([status: "success", html: html]  as JSON)
    }

    def categoryMappingFields() {
        String workSheetName = params.workSheetName
        Workbook ref = (Workbook) ImportService.IMPORT_READER_REF_HOLDER[session.item_import_ref]
        List<String> columnNames = null;
        if(!workSheetName.equals("")) {
            columnNames = importService.getColumnsBySheetName(ref, workSheetName)
        }
        Map sheetRows = [:];
        ref.each {
            sheetRows[it.getSheetName()] = it.getPhysicalNumberOfRows();
        }
        render (
            view: "/admin/item/import/categoryMappingFields",
            model: [
                categoryFields: NamedConstants.CATEGORY_IMPORT_FIELDS,
                columnNames: columnNames,
                categorySheetRows: sheetRows[workSheetName]
            ]
        )
    }

    def productMappingFields() {
        String workSheetName = params.workSheetName
        Workbook ref = (Workbook) ImportService.IMPORT_READER_REF_HOLDER[session.item_import_ref]
        List<String> columnNames = null;
        if(!workSheetName.equals("")) {
            columnNames = importService.getColumnsBySheetName(ref, workSheetName)
        }
        Map sheetRows = [:];
        ref.each {
            sheetRows[it.getSheetName()] = it.getPhysicalNumberOfRows();
        }
        render (
            view: "/admin/item/import/productMappingFields",
            model: [
                productFields: NamedConstants.PRODUCT_IMPORT_FIELDS + NamedConstants.PRODUCT_IMPORT_EXTRA_FIELDS,
                columnNames: columnNames,
                productSheetRows: sheetRows[workSheetName]
            ]
        )
    }

    @Restrictions([
        @Restriction(permission = "product.import.excel", params_exist = "productWorkSheet"),
        @Restriction(permission = "category.import.excel", params_exist = "categoryWorkSheet")
    ])
    def initImport() {
        Workbook ref = (Workbook) ImportService.IMPORT_READER_REF_HOLDER[session.item_import_ref]
        Task task = importService.initImport(params, ref)
        task.meta.logger_dump_file = session.item_import_ref
        task.meta.task_cache_location = SessionManager.protectedTempFolder.absolutePath + "/" + task.token + "/product_category_import.job"
        render([token: task.token, name: task.name] as JSON)
    }

    def progressView() {
        String token = params.token
        Task task = taskService.getByToken(token)
        render(
            view: "/admin/item/import/progressView",
            model: [
                task: task,
                totalSuccessCount: taskService.countTotalSuccess(task),
                totalWarningCount: taskService.countTotalWarning(task),
                totalErrorCount: taskService.countTotalError(task)
            ]
        )
    }

    def progressStatus() {
        String token = params.token
        Task task = taskService.getByToken(token)
        if(task) {
            render([
                token: token,
                status: task.status,
                totalCategoryRecord: task.meta.totalCategoryRecord,
                categoryComplete: task.meta.categoryComplete,
                categoryProgress: task.meta.categoryProgress,
                categorySuccessCount: task.meta.categorySuccessCount,
                categoryWarningCount: task.meta.categoryWarningCount,
                categoryErrorCount: task.meta.categoryErrorCount,
                totalProductRecord: task.meta.totalProductRecord,
                productComplete: task.meta.productComplete,
                productProgress: task.meta.productProgress,
                productSuccessCount: task.meta.productSuccessCount,
                productWarningCount: task.meta.productWarningCount,
                productErrorCount: task.meta.productErrorCount,
                totalRecord: task.totalRecord,
                recordComplete: task.recordComplete,
                totalProgress: task.progress,
                totalSuccessCount: taskService.countTotalSuccess(task),
                totalWarningCount: taskService.countTotalWarning(task),
                totalErrorCount: taskService.countTotalError(task)
            ] as JSON)
        } else {
            render([status: "error", message: g.message(code: "no.task.found")] as JSON)
        }
    }

    def successLogSummary() {
        String token = params.token
        String tempLocation = SessionManager.protectedTempFolder.absolutePath + "/" + token + "/product_category_import.job"
        Task task = MultiLoggerTask.getInstance(new File(tempLocation))
        if(task) {
            Map importLogs = importService.getImportLogs(task, "success")
            List<Map> categoryImportLogs = importLogs.category
            List<Map> productImportLogs = importLogs.product
            render(view: "/admin/item/import/summaryView", model: [categoryImportLogs: categoryImportLogs, productImportLogs: productImportLogs])
        } else {
            render(view: "/admin/item/import/summaryView", model: [emptyTask: true])
        }
    }

    def warningLogSummary() {
        String token = params.token
        String tempLocation = SessionManager.protectedTempFolder.absolutePath + "/" + token + "/product_category_import.job"
        Task task = MultiLoggerTask.getInstance(new File(tempLocation))
        if(task) {
            Map importLogs = importService.getImportLogs(task, "warning")
            List<Map> categoryImportLogs = importLogs.category
            List<Map> productImportLogs = importLogs.product
            render(view: "/admin/item/import/summaryView", model: [categoryImportLogs: categoryImportLogs, productImportLogs: productImportLogs])
        } else {
            render(view: "/admin/item/import/summaryView", model: [emptyTask: true])
        }
    }

    def errorLogSummary() {
        String token = params.token
        String tempLocation = SessionManager.protectedTempFolder.absolutePath + "/" + token + "/product_category_import.job"
        Task task = MultiLoggerTask.getInstance(new File(tempLocation))
        if(task) {
            Map importLogs = importService.getImportLogs(task, "error")
            List<Map> categoryImportLogs = importLogs.category
            List<Map> productImportLogs = importLogs.product
            render(view: "/admin/item/import/summaryView", model: [categoryImportLogs: categoryImportLogs, productImportLogs: productImportLogs])
        } else {
            render(view: "/admin/item/import/summaryView", model: [emptyTask: true])
        }
    }

    def download() {
        String token = params.token
        String tempLocation = SessionManager.protectedTempFolder.absolutePath + "/" + token + "/product_category_import.job"
        try {
            Task task = MultiLoggerTask.getInstance(new File(tempLocation))
            String path = Holders.servletContext.getRealPath("pub/item-import")
            File file = new File(path + "/Product Category Import - " + task.meta.logger_dump_file + ".xlsx")
            response.setHeader("Content-disposition", "attachment; filename=\"${file.name}\"")
            response.setHeader("Content-Type", "application/vnd.ms-excel")
            response.outputStream << file.bytes
            response.outputStream.flush()
        } catch(Exception e) {
            render(text: g.message(code: "report.not.available"))
        }
    }
}