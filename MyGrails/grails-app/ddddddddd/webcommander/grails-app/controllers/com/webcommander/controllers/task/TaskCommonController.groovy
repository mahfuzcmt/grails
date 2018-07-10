package com.webcommander.controllers.task

import com.webcommander.constants.NamedConstants
import com.webcommander.listener.SessionManager
import com.webcommander.task.MultiLoggerTask
import com.webcommander.task.Task
import com.webcommander.task.TaskService
import com.webcommander.util.AppUtil
import grails.converters.JSON
import grails.util.Holders

class TaskCommonController {
    TaskService taskService

    def progressView(){
        String token = params.token;
        Task task = taskService.getByToken(token);
        render(
                view: "/taskCommon/progressView",
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
        if(task) {
            Map data = [
                token: token,
                status: task.status,
                totalRecord: task.totalRecord,
                complete: task.recordComplete,
                totalProgress: task.progress,
                successCount: task.meta.successCount,
                warningCount: task.meta.warningCount,
                errorCount: task.meta.errorCount,
                cacheFile: task.meta.cache_file
            ]
            data.putAll(task.meta)
            render(data as JSON)

        } else {
            render([status: "error", message: g.message(code: "no.task.found")] as JSON)
        }
    }

    def successLogSummary() {
        String token = params.token
        String cacheFile = params.cacheFile
        String tempLocation = SessionManager.protectedTempFolder.absolutePath + "/$token/$cacheFile"
        Task task = MultiLoggerTask.getInstance(new File(tempLocation))
        if (task) {
            render(view: "/taskCommon/summaryView", model: [task: task, operation: NamedConstants.TASK_LOGGER_STATUS.SUCCESS])
        } else {
            render(view: "/taskCommon/summaryView", model: [emptyTask: true])
        }
    }

    def warningLogSummary() {
        String token = params.token
        String cacheFile = params.cacheFile
        String tempLocation = SessionManager.protectedTempFolder.absolutePath + "/$token/$cacheFile"
        Task task = MultiLoggerTask.getInstance(new File(tempLocation))
        if (task) {
            render(view: "/taskCommon/summaryView", model: [task: task, operation: NamedConstants.TASK_LOGGER_STATUS.WARNING])
        } else {
            render(view: "/taskCommon/summaryView", model: [emptyTask: true])
        }
    }

    def errorLogSummary() {
        String token = params.token
        String cacheFile = params.cacheFile
        String tempLocation = SessionManager.protectedTempFolder.absolutePath + "/$token/$cacheFile"
        MultiLoggerTask task = MultiLoggerTask.getInstance(new File(tempLocation))
        if (task) {
            render(view: "/taskCommon/summaryView", model: [task: task, operation: "error"])
        } else {
            render(view: "/taskCommon/summaryView", model: [emptyTask: true])
        }
    }

    def download() {
        String token = params.token
        String cacheFile = params.cacheFile
        String tempLocation = SessionManager.protectedTempFolder.absolutePath + "/$token/$cacheFile";
        try {
            Task task = MultiLoggerTask.getInstance(new File(tempLocation));
            String path = Holders.servletContext.getRealPath("pub/$task.meta.logger_dump_folder")
            File file = new File(path + File.separator + task.meta.logger_dump_file + ".xlsx")
            response.setHeader("Content-disposition", "attachment; filename=\"${file.name}\"")
            response.setHeader("Content-Type", "application/vnd.ms-excel")
            response.outputStream << file.bytes
            response.outputStream.flush()
        } catch(Exception e) {
            render(text: g.message(code: "report.not.available"))
        }
    }

}
