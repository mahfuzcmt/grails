package com.webcommander.webcommerce

import com.webcommander.common.CommonService
import com.webcommander.common.ImageService
import com.webcommander.common.MetaTag
import com.webcommander.events.AppEventManager
import com.webcommander.item.CategoryRawData
import com.webcommander.item.ImportConf
import com.webcommander.task.Task
import com.webcommander.task.TaskLogger
import com.webcommander.task.TaskService
import com.webcommander.util.AppUtil
import grails.util.Holders
import grails.validation.ValidationException
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet

class CategoryImportService {

    static transactional = false

    CommonService commonService
    ImageService imageService
    TaskService taskService
    CommonImportService commonImportService

    void setImportConfig(ImportConf conf, Map params) {
        conf.sheetName = params.categoryWorkSheet
        conf.isOverwrite = params.categoryOverwrite == "1"
        conf.matchBy = params.categoryMatchBy
        conf.parentMatchBy = params.categoryParentMatchBy
        conf.imageMatchBy = params.categoryImageMatchBy
        conf.imageSource = params.categoryImagePath ?: "/pub/category import/images"
    }

    Map getCategoryFieldsMap(Map params) {
        Map<String, Integer> fieldsMap = [:]
        params["category"]?.each { k,v ->
            String field = k.toString()
            String value = v.toString()
            if(value.length()) {
                fieldsMap[field] = Integer.parseInt(value)
            }
        }
        return fieldsMap
    }

    List<CategoryRawData> getAllCategoryRawData(Map<String, Integer> fieldsMap, Sheet sheet) {
        List<CategoryRawData> rawDataList = new ArrayList<CategoryRawData>()
        int rowNum = 0
        Iterator<Row> iRow = sheet.iterator()
        while(iRow.hasNext()) {
            Row row = iRow.next()
            if(rowNum == 0) { //skip sheet header row
                rowNum = 1
                continue
            }
            CategoryRawData rawData = new CategoryRawData()
            fieldsMap.each { field, colNum ->
                rawData."$field" = commonImportService.getCellValue(row, colNum)
            }
            rawData.rowNum = rowNum++
            rawDataList.add(rawData)
        }
        return rawDataList
    }

    def saveAll(List<CategoryRawData> rawDataList, ImportConf conf, Task task) {
        Map processed = new LinkedHashMap()
        for(CategoryRawData rawData : rawDataList) {
            try {
                String matchingKey = conf.matchBy == "name" ? rawData.name + "<>" + rawData.parent : (rawData.sku ?: ("" + System.currentTimeMillis()))
                def processedRow = processed[matchingKey]
                if(processedRow) {
                    task.meta.categoryProgress = taskService.countProgress(task.meta.totalCategoryRecord, task.meta.categoryComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error(rawData[conf.matchBy], "duplicate.entry.found.row", [processedRow])
                    task.meta.categoryErrorCount++
                    continue;
                }
                if(rawData[conf.matchBy]) {
                    processed[matchingKey] = rawData.rowNum;
                }
                if(!rawData.name) {
                    task.meta.categoryProgress = taskService.countProgress(task.meta.totalCategoryRecord, ++task.meta.categoryComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error(rawData[conf.matchBy], "category.name.not.found")
                    task.meta.categoryErrorCount++
                } else {
                    saveOrUpdate(rawData, conf, task)
                }
            } catch(Throwable t) {
                task.meta.categoryProgress = taskService.countProgress(task.meta.totalCategoryRecord, ++task.meta.categoryComplete as Integer)
                task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                task.taskLogger.error(rawData[conf.matchBy], "import.error")
                task.meta.categoryErrorCount++
            }
        }
        AppEventManager.fire("category-import-all-complete")
    }

    private void saveOrUpdate(CategoryRawData rawData, ImportConf conf, Task task) {
        String matchBy = conf.matchBy
        String matchByData = rawData[matchBy]
        Category parent
        Closure proceedImport = {
            Category category = Category.createCriteria().get {
                maxResults 1
                eq matchBy, matchByData
                if(parent) {
                    eq "parent", parent
                }
            }
            if(category) {
                if(category.isInTrash || category.isParentInTrash) {
                    task.taskLogger.warning(matchByData, "category.exists.in.trash")
                    task.meta.categoryWarningCount++
                } else {
                    if(!conf.isOverwrite) {
                        task.taskLogger.error(matchByData, "category.exists")
                        task.meta.categoryErrorCount++
                    }
                    update(rawData, category, conf, task)
                }
            } else {
                save(rawData, conf, task)
            }
            task.meta.categoryProgress = taskService.countProgress(task.meta.totalCategoryRecord, ++task.meta.categoryComplete as Integer)
            task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
        }
        if(rawData.parent) {
            parent = Category.createCriteria().get {
                maxResults 1
                eq conf.parentMatchBy, rawData.parent
            }
            if(!parent) {
                AppEventManager.one("category-import-done-" + rawData.parent, task.token, { _parent ->
                    AppEventManager.off("*", rawData.parent)
                    parent = _parent
                    proceedImport()
                })
                AppEventManager.one("category-import-all-complete", [rawData.parent, task.token], { _parent ->
                    task.meta.categoryProgress = taskService.countProgress(task.meta.totalCategoryRecord, ++task.meta.categoryComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error(matchByData, "parent.category.not.found", [rawData.parent])
                    task.meta.categoryErrorCount++
                })
                return;
            } else {
                proceedImport()
            }
        } else {
            proceedImport()
        }
    }

    private def save(CategoryRawData rawData, ImportConf conf, Task task) {
        String matchByData = rawData."${conf.matchBy}"
        try {
            Category category
            Category.withNewTransaction {
                category = new Category(sku: commonService.getSKUForDomain(Category))
                setEachMappingFieldData(rawData, category, conf, task)
                category.url = commonService.getUrlForDomain(category);
                category.save()
                if(conf.fieldsMap.image != null) {
                    if(rawData.image) {
                        saveCategoryImage(category, rawData.image, conf.imageSource, task, matchByData)
                    } else {
                        commonImportService.emptyFieldWarning("image", task.taskLogger, matchByData)
                        task.meta.categoryWarningCount++
                    }
                }
            }
            AppEventManager.fire("category-import-done-" + rawData[conf.parentMatchBy], [category])
        } catch(NumberFormatException e) {
            task.taskLogger.error(matchByData, "data.format.mismatch")
            task.meta.categoryErrorCount++
            return
        } catch(ValidationException e) {
            e.errors.allErrors.each { error ->
                task.taskLogger.error(matchByData, "hibernate.validation.error." + error.code, error.arguments)
            }
            task.meta.categoryErrorCount++
            return
        } catch(Exception e) {
            log.error("Category Import Error", e)
            task.taskLogger.error(matchByData, "import.error")
            task.meta.categoryErrorCount++
            return
        }
        task.taskLogger.success(matchByData, "import.success")
        task.meta.categorySuccessCount++
    }

    private def update(CategoryRawData rawData, Category category, ImportConf conf, Task task) {
        String matchByData = rawData."${conf.matchBy}"
        try {
            Category.withNewTransaction {
                category.attach()
                setEachMappingFieldData(rawData, category, conf, task)
                category.merge()
                if(conf.fieldsMap.image != null) {
                    if(rawData.image) {
                        saveCategoryImage(category, rawData.image, conf.imageSource, task, matchByData)
                    } else {
                        commonImportService.emptyFieldWarning("image", task.taskLogger, matchByData)
                        task.meta.categoryWarningCount++
                    }
                }
            }
            AppEventManager.fire("category-import-done-" + rawData[conf.parentMatchBy], [category])
        } catch(NumberFormatException e) {
            task.taskLogger.error(matchByData, "data.format.mismatch")
            task.meta.categoryErrorCount++
            return
        } catch(ValidationException e) {
            e.errors.allErrors.each { error ->
                task.taskLogger.error(matchByData, "hibernate.validation.error." + error.code, error.arguments)
            }
            task.meta.categoryErrorCount++
            return
        } catch(Exception e) {
            log.error("Category Import Error", e)
            task.taskLogger.error(matchByData, "import.error")
            task.meta.categoryErrorCount++
            return
        }
        task.taskLogger.success(matchByData, "import.success")
        task.meta.categorySuccessCount++
    }

    private def setEachMappingFieldData(CategoryRawData rawData, Category category, ImportConf conf, Task task) {
        conf.fieldsMap.each { field, colNum ->
            if(field != "image") {
                category."$field" = castRawData(rawData, field, conf, task)
            }
        }
    }

    private def castRawData(CategoryRawData rawData, String field, ImportConf conf, Task task) {
        String matchByData = rawData."${conf.matchBy}"
        TaskLogger taskLogger = task.taskLogger
        switch(field) {
            case "name":
                return rawData.name
            case "sku":
                return rawData.sku ?: commonService.getSKUForDomain(Category)
            case "parent":
                String fieldName = "parent.category"
                if(rawData.parent) {
                    Category parent = Category.createCriteria().get {
                        maxResults 1
                        eq conf.parentMatchBy, rawData.parent

                    }
                    if(parent) {
                        return parent
                    } else {
                        commonImportService.dataNotFoundWarning(fieldName, taskLogger, matchByData)
                        task.meta.categoryWarningCount++
                    }
                }
                return null
            case "summary":
                String fieldName = "summary"
                if(rawData.summary) {
                    return rawData.summary
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.categoryWarningCount++
                return null
            case "description":
                String fieldName = "description"
                if(rawData.description) {
                    return rawData.description
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.categoryWarningCount++
                return null
            case "idx":
                String fieldName = "display.order"
                if(rawData.idx) {
                    return rawData.idx.toInteger()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.categoryWarningCount++
                return null
            case "taxProfile":
                String fieldName = "tax.profile"
                if(rawData.taxProfile) {
                    TaxProfile taxProfile = TaxProfile.findByName(rawData.taxProfile)
                    if(taxProfile) {
                        return taxProfile
                    } else {
                        commonImportService.dataNotFoundWarning(fieldName, taskLogger, matchByData)
                        task.meta.categoryWarningCount++
                    }
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.categoryWarningCount++
                return null
            case "shippingProfile":
                String fieldName = "shipping.profile"
                if(rawData.shippingProfile) {
                    ShippingProfile shippingProfile = ShippingProfile.findByName(rawData.shippingProfile)
                    if(shippingProfile) {
                        return shippingProfile
                    } else {
                        commonImportService.dataNotFoundWarning(fieldName, taskLogger, matchByData)
                        task.meta.categoryWarningCount++
                    }
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.categoryWarningCount++
                return null
            case "metaTags":
                String fieldName = "meta.tags"
                if(rawData.metaTags) {
                    List<MetaTag> metaTags = commonImportService.generateMetaTags(rawData.metaTags)
                    if(metaTags.size()) {
                        return metaTags
                    } else {
                        commonImportService.dataNotFoundWarning(fieldName, taskLogger, matchByData)
                        task.meta.categoryWarningCount++
                    }
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.categoryWarningCount++
                return null
            default:
                return null
        }
    }

    def saveCategoryImage(Category category, String image, String imageLocation, Task task, String matchByData) {
        String filePath = Holders.servletContext.getRealPath("/resources/category/category-${category.id}")
        File dir = new File(filePath)
        if(!dir.exists()) {
            dir.mkdir()
        }
        //remove prev image
        if (category.image) {
            File imgDir = new File(filePath);
            if (imgDir.exists()) {
                imgDir.traverse {
                    it.delete()
                }
                imgDir.delete()
            }
        }
        InputStream inputStream = commonImportService.findImageStream(image, imageLocation)
        if(inputStream) {
            try {
                category.image = image
                imageService.createCopies(inputStream, image, category, imageService.getSizes("category-image"))
            } catch(Exception exc) {
                log.error("Copy Import Category Image Error: " + exc)
                task.taskLogger.warning(matchByData, "category.image.copy.error")
                task.meta.categoryWarningCount++
                return
            }
        } else {
            task.taskLogger.warning(matchByData, "category.image.save.error")
            task.meta.categoryWarningCount++
            return
        }
        category.merge()
    }
}
