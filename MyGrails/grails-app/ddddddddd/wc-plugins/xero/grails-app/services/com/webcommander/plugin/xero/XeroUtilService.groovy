package com.webcommander.plugin.xero

import com.webcommander.task.MultiLoggerTask
import com.webcommander.task.Task
import com.webcommander.task.TaskLogger
import com.webcommander.util.AppUtil
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.context.MessageSource

@Transactional
class XeroUtilService {
    MessageSource messageSource

    private void saveLogStatusToFile(Task task) {
        File dumpFile = new File(task.meta.task_cache_location)
        dumpFile.parentFile.mkdirs()
        dumpFile.withOutputStream { out ->
            ObjectOutputStream oOut = new ObjectOutputStream(out)
            oOut.writeObject(task)
        }
    }

    private void addLogHeaderRow(XSSFSheet sheet, Map header) {
        Row row = sheet.createRow(0)
        Cell cell = row.createCell(0)
        cell.setCellValue(header.type)
        cell = row.createCell(1)
        cell.setCellValue(header.logFor)
        cell = row.createCell(2)
        cell.setCellValue(header.msg)
    }

    private void addLogRow(XSSFSheet sheet, TaskLogger.Log log, int rowCount) {
        Row row = sheet.createRow(rowCount)
        Cell cell = row.createCell(0)
        cell.setCellValue(log.type)
        cell = row.createCell(1)
        cell.setCellValue(log.logFor)
        cell = row.createCell(2)
        cell.setCellValue(messageSource.getMessage(log.msg, log.args ?: [] as Object[], log.msg, Locale.getDefault()))
    }

/*    public void dumpImportStatus(Task task) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Map header = [type: "Stauts", logFor: "Name", msg: "Remark"]
        XSSFSheet sheet = workbook.createSheet(task.meta.resourceType);
        addLogHeaderRow(sheet, header)
        task.taskLogger.logs.eachWithIndex{ TaskLogger.Log log, int i ->
            addLogRow(sheet, log, i + 1)
        }
        try {
            String path = Holders.servletContext.getRealPath("pub/xero-import-export")
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
    }*/

    public synchronized void dumpImportStatus(MultiLoggerTask multiLoggerTask) {
        try {
            String path = Holders.servletContext.getRealPath("pub/xero-import-export")
            File file = new File(path + "/" + multiLoggerTask.meta.logger_dump_file)
            XSSFWorkbook workbook;
            if(file.exists()) {
                file.withInputStream { istream ->
                    workbook = new XSSFWorkbook(istream)
                }
            } else {
                workbook = new XSSFWorkbook()
            }
            Map header = [type: "Stauts", logFor: "Name", msg: "Remark"]
            Map logs = getLogs(multiLoggerTask)
            logs.each {
                XSSFSheet sheet
                int startRowCount = 0
                if(file.exists()) {
                    sheet = workbook.getSheet(it.key)
                    startRowCount = sheet.getLastRowNum() - 1
                }
                if(!sheet) {
                    sheet = workbook.createSheet(it.key)
                    addLogHeaderRow(sheet, header)
                }
                it.value.eachWithIndex { TaskLogger.Log log, int i ->
                    addLogRow(sheet, log, startRowCount + i + 1)
                }
            }
            File dir = new File(path)
            if(!dir.exists()) {
                dir.mkdirs()
            }
            if(!file.exists()) {
                file.createNewFile();
            }
            file.withOutputStream { stream ->
                workbook.write(stream)
            }
        } catch(Throwable e) {
            log.warn("Import Log Dump Error", e)
        }
    }

    public Map getLogs(Task task, String type = null) {
        MultiLoggerTask multiLoggerTask = (MultiLoggerTask) task
        if(!multiLoggerTask) {
            return [:]
        }
        Map<String, List<Map>> importLogs = [:]
        TaskLogger customerLogger = multiLoggerTask.getLogger("customer")
        if(customerLogger) {
            importLogs.customer = [];
            List customerLogs = type ? customerLogger.logVsType[type] : customerLogger.logs;
            for(TaskLogger.Log log : customerLogs) {
                importLogs.customer.add(log)
            }
        }
        TaskLogger productLogger = multiLoggerTask.getLogger("product")
        if(productLogger) {
            importLogs.product = [];
            List productLogs = type ? productLogger.logVsType[type] : productLogger.logs
            for(TaskLogger.Log log : productLogs) {
                importLogs.product.add(log)
            }
        }
        TaskLogger taxLogger = multiLoggerTask.getLogger("tax")
        if(taxLogger) {
            importLogs.tax = [];
            List taxLogs = type ? taxLogger.logVsType[type] : taxLogger.logs
            for(TaskLogger.Log log : taxLogs) {
                importLogs.tax.add(log)
            }
        }
        TaskLogger orderLogger = multiLoggerTask.getLogger("order")
        if(orderLogger) {
            importLogs.order = [];
            List orderLogs = type ? orderLogger.logVsType[type] : orderLogger.logs
            for(TaskLogger.Log log : orderLogs) {
                importLogs.order.add(log)
            }
        }
        return importLogs
    }
}
