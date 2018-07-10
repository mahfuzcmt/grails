package com.webcommander.webcommerce

import com.webcommander.ApplicationTagLib
import com.webcommander.events.AppEventManager
import com.webcommander.item.CategoryRawData
import com.webcommander.item.ImportConf
import com.webcommander.item.ProductRawData
import com.webcommander.task.MultiLoggerTask
import com.webcommander.task.Task
import com.webcommander.task.TaskLogger
import com.webcommander.util.AppUtil
import grails.util.Holders
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.web.multipart.MultipartFile
import org.supercsv.io.CsvMapReader
import org.supercsv.prefs.CsvPreference

import javax.servlet.http.HttpServletRequest
import java.util.concurrent.ConcurrentHashMap


class ImportService {

    static transactional = false

    @Autowired()
    @Qualifier("com.webcommander.ApplicationTagLib")
    ApplicationTagLib app
    MessageSource messageSource
    ProductService productService
    CategoryImportService categoryImportService
    ProductImportService productImportService

    public final static Map IMPORT_READER_REF_HOLDER = new ConcurrentHashMap();

    String getImportRefForFile(MultipartFile file, String fileExtension) {
        def reader;
        if(fileExtension == "xls") {
            reader = new HSSFWorkbook(file.inputStream)
        } else if(fileExtension == "xlsx") {
            reader = new XSSFWorkbook(file.inputStream)
        } else {
            reader = new CsvMapReader(new InputStreamReader(file.inputStream), CsvPreference.EXCEL_PREFERENCE)
        }
        String ref = new Date().toFormattedString("dd.MM.yyyy", true, "HH.mm.ss", false, null)
        IMPORT_READER_REF_HOLDER[ref] = reader
        AppEventManager.one("session-terminate-" + AppUtil.session.id, {
            IMPORT_READER_REF_HOLDER.remove(ref)
        })
        return ref
    }

    List<String> readSheetNames(def importRef) {
        if(importRef instanceof HSSFWorkbook) {
            return (0..importRef.getNumberOfSheets()-1).collect {return importRef.getSheetName(it)}
        } else if(importRef instanceof Workbook) {
            return (0..importRef.getNumberOfSheets()-1).collect {return importRef.getSheetName(it)}
        }
        return []
    }

    List<String> getColumnsBySheetName(def ref, String workSheetName) {
        if(ref && workSheetName) {
            Sheet sheet = ref.getSheet(workSheetName)
            Row row = sheet.getAt(0)
            Iterator<Cell> cellIterator = row.iterator()
            List<String> columnNames = new ArrayList<String>()
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next()
                cell.setCellType(Cell.CELL_TYPE_STRING)
                columnNames.add(cell.getStringCellValue())
            }
            return columnNames
        }
        return []
    }

    public Task initImport(Map params, Workbook ref) {
        ImportConf categoryImportConf = new ImportConf()
        ImportConf productImportConf = new ImportConf()
        categoryImportService.setImportConfig(categoryImportConf, params)
        productImportService.setImportConfig(productImportConf, params)

        List<CategoryRawData> categoryRawDataList = []
        List<ProductRawData> productRawDataList = []
        MultiLoggerTask itemImportTask = new MultiLoggerTask("Item Import")
        itemImportTask.detail_url = app.relativeBaseUrl() + "itemImport/progressView";
        itemImportTask.detail_status_url = app.relativeBaseUrl() + "itemImport/progressStatus";
        itemImportTask.detail_viewer = "app.tabs.item.import_status_viewer"
        itemImportTask.meta = [
            totalCategoryRecord : 0,
            categoryComplete : 0,
            categoryProgress : 0,
            categorySuccessCount : 0,
            categoryWarningCount : 0,
            categoryErrorCount : 0,
            totalProductRecord : 0,
            productComplete : 0,
            productProgress : 0,
            productSuccessCount : 0,
            productWarningCount : 0,
            productErrorCount : 0
        ]
        categoryImportConf.fieldsMap = categoryImportService.getCategoryFieldsMap(params)
        if(categoryImportConf.sheetName != "") {
            Sheet sheet = ref.getSheet(categoryImportConf.sheetName)
            categoryRawDataList = categoryImportService.getAllCategoryRawData(categoryImportConf.fieldsMap, sheet)
            itemImportTask.totalRecord += categoryRawDataList.size()
            itemImportTask.meta.totalCategoryRecord = categoryRawDataList.size()
        }

        productImportConf.fieldsMap = productImportService.getProductFieldsMap(params)
        if(productImportConf.sheetName != "") {
            Sheet sheet = ref.getSheet(productImportConf.sheetName)
            productRawDataList = productImportService.getAllProductRawData(productImportConf, sheet)
            itemImportTask.totalRecord += productRawDataList.size()
            itemImportTask.meta.totalProductRecord = productRawDataList.size()
        }
        itemImportTask.onError { Throwable t ->
            log.error("Import Exception Occurred", t)
            saveLogStatusToSession(itemImportTask)
            dumpImportStatus(itemImportTask)
            Thread.sleep(5000) // to ensure calling of last status seek
        }
        itemImportTask.onComplete {
            saveLogStatusToSession(itemImportTask)
            dumpImportStatus(itemImportTask)
            Thread.sleep(5000) // to ensure calling of last status seek
        }
        itemImportTask.async {
            if(categoryImportConf.sheetName) {
                category {
                    categoryImportService.saveAll(categoryRawDataList, categoryImportConf, itemImportTask)
                }
            }
            if(productImportConf.sheetName) {
                product {
                    productImportService.saveAll(productRawDataList, productImportConf, itemImportTask)
                }
            }
        }
        return itemImportTask
    }

    public void dumpImportStatus(MultiLoggerTask multiLoggerTask) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Map header = [type: "Stauts", logFor: "Name", msg: "Remark"]
        TaskLogger categoryLogger = multiLoggerTask.getLogger("category")
        if(categoryLogger) {
            XSSFSheet sheet = workbook.createSheet("Category")
            addLogHeaderRow(sheet, header)
            categoryLogger.logs.eachWithIndex { TaskLogger.Log log, int i ->
                addLogRow(sheet, log, i + 1)
            }
        }
        TaskLogger productLogger = multiLoggerTask.getLogger("product")
        if(productLogger) {
            XSSFSheet sheet = workbook.createSheet("Product")
            addLogHeaderRow(sheet, header)
            productLogger.logs.eachWithIndex { TaskLogger.Log log, int i ->
                addLogRow(sheet, log, i + 1)
            }
        }
        try {
            String path = Holders.servletContext.getRealPath("pub/item-import")
            File dir = new File(path)
            if(!dir.exists()) {
                dir.mkdirs()
            }
            File file = new File(path + "/Product Category Import - " + multiLoggerTask.meta.logger_dump_file + ".xlsx")
            FileOutputStream out = new FileOutputStream(file)
            workbook.write(out)
            out.close()
        } catch(FileNotFoundException e) {
            log.warn("Import Log Dump Error", e)
        } catch(IOException e) {
            log.warn("Import Log Dump Error", e)
        }
    }

    private void saveLogStatusToSession(MultiLoggerTask multiLoggerTask) {
        File dumpFile = new File(multiLoggerTask.meta.task_cache_location)
        dumpFile.parentFile.mkdirs()
        dumpFile.withOutputStream { out ->
            ObjectOutputStream oOut = new ObjectOutputStream(out)
            oOut.writeObject(multiLoggerTask)
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

    public Map getImportLogs(Task task, String type) {
        MultiLoggerTask multiLoggerTask = (MultiLoggerTask) task
        if(!multiLoggerTask) {
            return [:]
        }
        Map<String, List<Map>> importLogs = [category: [], product: []]
        TaskLogger categoryLogger = multiLoggerTask.getLogger("category")
        if(categoryLogger) {
            for(TaskLogger.Log log : categoryLogger.logVsType[type]) {
                importLogs.category.add(log)
            }
        }
        TaskLogger productLogger = multiLoggerTask.getLogger("product")
        if(productLogger) {
            for(TaskLogger.Log log : productLogger.logVsType[type]) {
                importLogs.product.add(log)
            }
        }
        return importLogs
    }
}
