package com.webcommander.admin

import com.webcommander.ApplicationTagLib
import com.webcommander.config.RedirectMapping
import com.webcommander.constants.NamedConstants
import com.webcommander.filter.Redirect301Filter
import com.webcommander.listener.SessionManager
import com.webcommander.task.MultiLoggerTask
import com.webcommander.task.Task
import com.webcommander.task.TaskService
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.CommonImportService
import grails.gorm.transactions.Transactional
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.multipart.MultipartFile
import org.supercsv.io.CsvListWriter
import org.supercsv.io.CsvMapReader
import org.supercsv.prefs.CsvPreference

@Transactional
class RedirectService {
    CommonImportService commonImportService
    TaskService taskService
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    ApplicationTagLib app

    public Map mapping301Redirect(Map params) {
        def pathMatcher = { path, path2, checkRelative = false ->
            return path == path2 || (checkRelative && path == app.relativeBaseUrl() + path2)
        }
        RedirectMapping mapping;
        if (params.id) {
            mapping = RedirectMapping.get(params.id)
        } else {
            mapping = new RedirectMapping();
        }
        String oldUrl = params.oldUrl;
        String newUrl = params.newUrl;
        if (oldUrl.startsWith("http")) {
            URL url = new URL(oldUrl)
            mapping.scheme = url.protocol
            mapping.host = url.host
            mapping.path = url.path
        } else if (oldUrl.startsWith("//")) {
            URL url = new URL("http:" + oldUrl)
            mapping.host = url.host
            mapping.path = url.path
        } else {
            mapping.scheme = null
            mapping.host = null
            mapping.path = oldUrl;
        }
        mapping.oldUrl = oldUrl
        if (mapping.path.endsWith("/") && mapping.path.length() > 1) {
            mapping.path = mapping.path.substring(0, mapping.path.length() - 1)
        }
        if (newUrl.endsWith("/") && newUrl.length() > 1) {
            newUrl = newUrl.substring(0, newUrl.length() - 1)
        }
        if (newUrl.startsWith("http")) {
            URL url = new URL(newUrl);
            if (url.protocol == mapping.scheme && mapping.host == url.host && pathMatcher(mapping.path, url.path)) {
                throw new Exception("old.new.url.are.same");
            }
        } else if (newUrl.startsWith("//")) {
            URL url = new URL(newUrl);
            if (mapping.host == url.host && pathMatcher(mapping.path, url.path)) {
                throw new Exception("old.new.url.are.same");
            }
        } else {
            if (pathMatcher(mapping.path, newUrl)) {
                throw new Exception("old.new.url.are.same");
            }
        }
        Boolean isExists = RedirectMapping.createCriteria().get {
            eq("path", mapping.path)
            if (mapping.id) {
                ne("id", mapping.id)
            }
            or {
                isNull("host")
                if (mapping.host) {
                    eq("host", mapping.host)
                }
            }
            or {
                isNull("scheme")
                if (mapping.scheme) {
                    eq("scheme", mapping.scheme)
                }
            }
            maxResults(1)
        } != null
        if (isExists) {
            throw new Exception("old.url.already.exist");
        }
        mapping.newUrl = newUrl
        mapping.save()
        Redirect301Filter.REDIRECT_ENTRIES[TenantContext.currentTenant] = null
        return [
            id: mapping.id,
            newUrl: mapping.newUrl,
            oldUrl: mapping.oldUrl,
            scheme: mapping.scheme,
            host: mapping.host,
            path: mapping.path
        ]
    }

    @Transactional
    public boolean remove301Redirect(Long mappingId) {
        RedirectMapping redirectMapping = RedirectMapping.get(mappingId);
        redirectMapping.delete();
        Redirect301Filter.REDIRECT_ENTRIES[TenantContext.currentTenant] = null
        return true;
    }

    def export() {
        List<RedirectMapping> mappings = RedirectMapping.list();
        File file = new File(SessionManager.protectedTempFolder.absolutePath, "Redirect export");
        FileWriter writer = new FileWriter(file)
        CsvListWriter csvWriter = new CsvListWriter(writer, CsvPreference.STANDARD_PREFERENCE);
        csvWriter.write(["Old URL", "New URL"])
        mappings.each {
            csvWriter.write([it.oldUrl, it.newUrl])
        }
        csvWriter.close();
        return file;
    }

    Map getHeaderMapping(def reader) {
        Map mapping = [:];
        NamedConstants.REDIRECT_IMPORT_FIELD_MAPPING.values().each {
            mapping[it] = -1
        }
        if (reader instanceof Workbook) {
            reader = (Workbook) reader;
            Sheet sheet = reader.getSheetAt(0);
            Row header = sheet.getAt(0);
            Iterator<Cell> iterator = header.iterator();
            Integer i = 0;
            while (iterator.hasNext()) {
                String value = iterator.next().getStringCellValue().trim();
                if (mapping.containsKey(value)) {
                    mapping[value] = i
                }
                i++;
            }
        }
        return mapping;
    }

    List getRedirectList(def reader, Map fieldMapping) {
        List<Map> redirects = [];
        if (reader instanceof Workbook) {
            Sheet sheet = reader.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.rowIterator();
            Integer i = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (i == 0) {
                    i++;
                    continue;
                }
                Map redirect = [:]
                NamedConstants.REDIRECT_IMPORT_FIELD_MAPPING.each {
                    Integer index = fieldMapping[it.value]
                    redirect."$it.key" = index != -1 ? commonImportService.getCellValue(row, index) : null
                }
                redirects.add(redirect)
                i++;
            }
        } else {
            reader = (CsvMapReader) reader
            String[] headers = reader.getHeader(true);
            Map<String, String> row = null
            while ((row = reader.read(headers)) != null) {
                Map redirect = [:]
                Map filteredRow = [:];
                row.each {
                    filteredRow[it.key.trim()] = it.value;
                }
                NamedConstants.REDIRECT_IMPORT_FIELD_MAPPING.each {
                    redirect."$it.key" = filteredRow[it.value];
                }
                redirects.add(redirect);
            }
        }

        return redirects;
    }

    def saveAll(Task task, List<Map> redirects) {
        AppUtil.initialDummyRequest();
        redirects.each {
            try {
                mapping301Redirect(it)
                task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                task.taskLogger.success("Old URL: $it.oldUrl, New URL: $it.newUrl", "redirect.url.import.success")
                task.meta.successCount++
            } catch (Exception e) {
                task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                String name = "Old URL: $it.oldUrl, New URL: $it.newUrl"
                task.taskLogger.error(name, e.message)
                task.meta.errorCount++;
            }
        }
    }
    def initImport(MultipartFile file, String fileExtension) {
        def reader;
        if (fileExtension == "xls") {
            reader = new HSSFWorkbook(file.inputStream)
        } else if (fileExtension == "xlsx") {
            reader = new XSSFWorkbook(file.inputStream)
        } else {
            reader = new CsvMapReader(new InputStreamReader(file.inputStream), CsvPreference.EXCEL_PREFERENCE)
        }
        MultiLoggerTask task = new MultiLoggerTask("301 Redirect Import")
        task.detail_url = app.relativeBaseUrl() + "taskCommon/progressView";
        task.detail_status_url = app.relativeBaseUrl() + "taskCommon/progressStatus";
        task.detail_viewer = "app.tabs.setting.import_status_viewer"
        task.meta = [
                successCount: 0,
                warningCount: 0,
                errorCount  : 0,
                resourceType: "redirect"
        ]
        Map fieldMapping = getHeaderMapping(reader);
        List<Map> customerDataList = getRedirectList(reader, fieldMapping)
        task.totalRecord = customerDataList.size();
        task.onComplete {
            taskService.saveLogToSession(task);
            taskService.saveLog(task,  ["Status", "Name", "Remark"]);
            Thread.sleep(50000);
        }

        task.onError { Throwable t ->
            taskService.saveLogToSession(task);
            taskService.saveLog(task, ["Status", "Name", "Remark"]);
            Thread.sleep(50000);
        }

        task.async {
            redirects {
                saveAll(task, customerDataList)
            }
        }

        return task

    }
}