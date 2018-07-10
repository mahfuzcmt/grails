package com.webcommander.controllers.admin

import com.webcommander.admin.RedirectService
import com.webcommander.listener.SessionManager
import com.webcommander.task.Task
import grails.converters.JSON
import grails.validation.ValidationException
import org.apache.commons.io.FilenameUtils
import org.springframework.web.multipart.MultipartFile

class RedirectController {
    RedirectService redirectService

    def mapping301Redirect() {
        try {
            Map result = redirectService.mapping301Redirect(params);
            String message;
            if (params.id) {
                message = g.message(code: "301.url.updated");
            } else {
                message = g.message(code: "301.url.saved");
            }
            result.status = "success";
            result.message = message;
            render(result as JSON)
        } catch (ValidationException e) {
            render([status: "error", message: g.message(code: "old.url.already.exist")] as JSON)
        } catch(Exception e) {
            render([status: "error", message: g.message(code: e.message)] as JSON)
        }
    }

    def remove301Redirect() {
        if(redirectService.remove301Redirect(params.long("mappingId"))) {
            render([status: "success", message: g.message( code: "301.url.removed")] as JSON)
        }
    }

    def export() {
        File file = redirectService.export();
        String fileName = "301-redirects(" + new Date().gmt().toZone(session.timezone).toString() + ").csv"
        response.setHeader("Content-disposition", "attachment; filename=\"${fileName}\"")
        response.setHeader("Content-Type", "text/csv")
        OutputStream stream = response.outputStream
        stream << file.bytes
        stream.flush()
    }

    def uploadImportFile() {
        render(view: "/admin/redirect/uploadForm")
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
        Task task = redirectService.initImport(importFile, fileExtension)
        task.meta.logger_dump_folder = "redirect-import"
        task.meta.logger_dump_file = new Date().toFormattedString("dd.MM.yyyy", true, "HH.mm.ss", false, null);
        task.meta.task_cache_location = SessionManager.protectedTempFolder.absolutePath + "/" + task.token + "/redirect_import.job"
        task.meta.cache_file = "redirect_import.job"
        render([token: task.token, name: task.name] as JSON)
    }
}
