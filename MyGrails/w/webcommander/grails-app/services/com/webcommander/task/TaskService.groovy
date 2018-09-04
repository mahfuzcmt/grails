package com.webcommander.task

import com.webcommander.util.AppUtil
import grails.util.Holders
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.context.MessageSource

class TaskService {
    MessageSource messageSource
    static transactional = false

    public def runningTasks() {
        return TaskManager.getTasks()
    }

    public Task getByToken(String token) {
        return TaskManager.getByToken(token)
    }

    public int countProgress(Integer totalRecords, Integer recordsComplete) {
        return (Integer) Math.ceil((recordsComplete * 100) / totalRecords)
    }

    public int countTotalSuccess(Task task) {
        if(!task.meta.productSuccessCount) {
            return task.meta.categorySuccessCount
        } else if(!task.meta.categorySuccessCount) {
            return task.meta.productSuccessCount
        }
        return task.meta.categorySuccessCount + task.meta.productSuccessCount
    }

    public int countTotalWarning(Task task) {
        if(!task.meta.productWarningCount) {
            return task.meta.categoryWarningCount
        } else if(!task.meta.categoryWarningCount) {
            return task.meta.productWarningCount
        }
        return task.meta.categoryWarningCount + task.meta.productWarningCount
    }

    public int countTotalError(Task task) {
        if(!task.meta.productErrorCount) {
            return task.meta.categoryErrorCount
        } else if(!task.meta.categoryErrorCount) {
            return task.meta.productErrorCount
        }
        return task.meta.categoryErrorCount + task.meta.productErrorCount
    }

    public void addHeaderRow(XSSFSheet sheet, List header) {
        Row row = sheet.createRow(0)
        Integer i = 0;
        header.each { def entry ->
            Cell cell = row.createCell(i++)
            cell.setCellValue(entry)
        }
    }

    private void addLogRow(XSSFSheet sheet, TaskLogger.Log log, int rowCount) {
        Row row = sheet.createRow(rowCount)
        Cell cell = row.createCell(0)
        cell.setCellValue(log.type)
        cell = row.createCell(1)
        cell.setCellValue(log.logFor)
        cell = row.createCell(2)
        cell.setCellValue(messageSource.getMessage(log.msg, log.args ?: [] as Object[], log.msg, Locale.getDefault()));
    }

    public void saveMultiLoggerTaskLog(MultiLoggerTask task, List<String> header) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        task.loggers.each {String name, TaskLogger logger ->
            saveLog(logger, name, workbook, header)
        }
        saveWorkbook(workbook, task)
    }

    public void saveLog(TaskLogger logger, String loggerName, XSSFWorkbook workbook, List<String> header) {
        XSSFSheet sheet = workbook.createSheet(loggerName);
        addHeaderRow(sheet, header)
        logger.logs.eachWithIndex { TaskLogger.Log log, int i ->
            addLogRow(sheet, log, i + 1)
        }
    }

    public void saveWorkbook(XSSFWorkbook workbook, Task task) {
        try {
            String path = Holders.servletContext.getRealPath("pub/$task.meta.logger_dump_folder")
            File dir = new File(path)
            if(!dir.exists()) {
                dir.mkdirs()
            }
            File file = new File(path + "/" + task.meta.logger_dump_file + ".xlsx")
            FileOutputStream out = new FileOutputStream(file)
            workbook.write(out)
            out.close()
        } catch(Throwable e) {
            log.warn("Import Log Dump Error", e)
        }
    }

    public void saveLog(Task task, List<String> header) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        saveLog(task.taskLogger, "Logs", workbook, header)
        saveWorkbook(workbook, task)
    }

    void saveLogToSession(Task task) {
        File dumpFile = new File(task.meta.task_cache_location)
        dumpFile.parentFile.mkdirs()
        dumpFile.withOutputStream { out ->
            ObjectOutputStream oOut = new ObjectOutputStream(out)
            oOut.writeObject(task)
        }
    }
}
