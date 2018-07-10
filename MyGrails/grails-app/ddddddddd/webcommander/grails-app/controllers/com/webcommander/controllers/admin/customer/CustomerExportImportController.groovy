package com.webcommander.controllers.admin.customer

import com.webcommander.admin.CustomerExportImportService
import com.webcommander.listener.SessionManager
import com.webcommander.task.MultiLoggerTask
import com.webcommander.task.Task
import com.webcommander.task.TaskService
import com.webcommander.util.AppUtil
import grails.converters.JSON
import grails.util.Holders
import org.apache.commons.io.FilenameUtils
import org.springframework.web.multipart.MultipartFile

class CustomerExportImportController {
    CustomerExportImportService customerExportImportService
    TaskService taskService

    def initExport() {
        render(view: "/admin/customer/initExport")
    }

    def export() {
        response.setHeader("Content-disposition", "attachment; filename=\"${new Date().gmt().toZone(session.timezone).toString()}.xlsx\"")
        response.setHeader("Content-Type", "application/vnd.ms-excel")
        customerExportImportService.export(params, response.outputStream)
    }

    def uploadImportFile() {
        render(view: "/admin/customerExportImport/uploadFileForm")
    }

    def initImport() {
        MultipartFile importFile = request.getFile("importFile");
        Long fileSize = Math.ceil(importFile.getSize() / (1024*1024));
        String fileExtension = FilenameUtils.getExtension(importFile.originalFilename);
        if(importFile.empty) {
            render([status: "error", message: g.message(code: "file.cannot.be.empty")] as JSON)
            return;
        } else if (fileSize > 100) {
            render([status: "error", message: g.message(code: "uploaded.file.size.larger.than.expected", args: [fileSize.toString() + "MB", "100MB"])] as JSON)
            return;
        } else if(!["xlsx", "xls", "csv"].contains(fileExtension)) {
            render([status: "error", message: g.message(code: "uploaded.file.extension.is.expected", args: [fileExtension , "zip"])] as JSON)
            return;
        }
        Task task = customerExportImportService.initImport(importFile, fileExtension)
        task.meta.logger_dump_file = new Date().toFormattedString("dd.MM.yyyy", true, "HH.mm.ss", false, null);
        task.meta.task_cache_location = SessionManager.protectedTempFolder.absolutePath + "/" + task.token + "/customer_import.job"
        render([token: task.token, name: task.name] as JSON)
    }

    def progressView(){
        String token = params.token;
        Task task = taskService.getByToken(token);
        def type = params.type;
        render(
                view: "/admin/customerExportImport/progressView",
                model: [
                        task: task,
                        totalSuccessCount: task.meta.successCount,
                        totalWarningCount: task.meta.warningCount,
                        totalErrorCount: task.meta.errorCount,
                        type: task.meta.resourceType,
                        total: task.totalRecord,
                        complete: task.recordComplete
                ]
        )
    }


    def progressStatus(){
        String token = params.token;
        Task task = taskService.getByToken(token);
        if(task){
            Map data = [
                    token: token,
                    status: task.status,
                    totalRecord: task.totalRecord,
                    complete: task.recordComplete,
                    totalProgress: task.progress,
                    successCount: task.meta.successCount,
                    warningCount: task.meta.warningCount,
                    errorCount: task.meta.errorCount
            ]
            data.putAll(task.meta)
            render(data as JSON)

        }else {
            render([status: "error", message: g.message(code: "no.task.found")] as JSON)
        }
    }

    def successLogSummary() {
        String token = params.token
        String tempLocation = SessionManager.protectedTempFolder.absolutePath + "/" + token + "/customer_import.job"
        Task task = MultiLoggerTask.getInstance(new File(tempLocation))
        if (task) {
            List logs = task.taskLogger.getSuccess()
            render(view: "/admin/customerExportImport/summaryView", model: [logs: logs, operation: "success"])
        } else {
            render(view: "/admin/customerExportImport/summaryView", model: [emptyTask: true])
        }
    }

    def warningLogSummary() {
        String token = params.token
        String tempLocation = SessionManager.protectedTempFolder.absolutePath + "/" + token + "/customer_import.job"
        Task task = MultiLoggerTask.getInstance(new File(tempLocation))
        if (task) {
            List logs = task.taskLogger.getWarning();
            render(view: "/admin/customerExportImport/summaryView", model: [logs: logs, operation: "warning"])
        } else {
            render(view: "/admin/customerExportImport/summaryView", model: [emptyTask: true])
        }
    }

    def errorLogSummary() {
        String token = params.token
        String tempLocation = SessionManager.protectedTempFolder.absolutePath + "/" + token + "/customer_import.job"
        Task task = MultiLoggerTask.getInstance(new File(tempLocation))
        if (task) {
            List logs = task.taskLogger.getError();
            render(view: "/admin/customerExportImport/summaryView", model: [logs: logs, operation: "error"])
        } else {
            render(view: "/admin/customerExportImport/summaryView", model: [emptyTask: true])
        }
    }

    def download() {
        String token = params.token
        String tempLocation = SessionManager.protectedTempFolder.absolutePath + "/" + token + "/customer_import.job"
        try {
            Task task = MultiLoggerTask.getInstance(new File(tempLocation))
            String path = Holders.servletContext.getRealPath("pub/customer-import")
            File file = new File(path + "/Customer Import -" + task.meta.logger_dump_file + ".xlsx")
            response.setHeader("Content-disposition", "attachment; filename=\"${file.name}\"")
            response.setHeader("Content-Type", "application/vnd.ms-excel")
            response.outputStream << file.bytes
            response.outputStream.flush()
        } catch(Exception e) {
            render(text: g.message(code: "report.not.available"))
        }
    }

}
