package com.webcommander.webcommerce

import com.webcommander.common.CommonService
import com.webcommander.common.ImageService
import com.webcommander.common.MetaTag
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.item.ImportConf
import com.webcommander.item.ProductRawData
import com.webcommander.manager.HookManager
import com.webcommander.task.Task
import com.webcommander.task.TaskLogger
import com.webcommander.task.TaskService
import com.webcommander.throwables.ApplicationRuntimeException
import grails.gorm.transactions.Transactional
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.transaction.TransactionStatus

import javax.xml.bind.ValidationException
import java.text.SimpleDateFormat

@Transactional
class ProductImportService {

    private static final String DATE_YY = "\\d{1,2}/\\d{1,2}/\\d{2}"
    private static final String DATE_YYYY = "\\d{1,2}/\\d{1,2}/\\d{4}"

    CommonService commonService
    ProductService productService
    ImageService imageService
    TaskService taskService
    CommonImportService commonImportService

    public void setImportConfig(ImportConf conf, Map params) {
        conf.sheetName = params.productWorkSheet
        conf.isOverwrite = params.productOverwrite == "1"
        conf.matchBy = params.productMatchBy
        conf.parentMatchBy = params.productParentMatchBy
        conf.imageMatchBy = params.productImageMatchBy
        conf.videoMatchBy = params.productVideoMatchBy
        conf.imageSource = params.productImagePath ?: "/pub/product import/images"
        conf.videoSource = params.productvideoPath ?: "/pub/product import/videos"
    }

    public Map getProductFieldsMap(Map params) {
        Map<String, Integer> fieldsMap = [:]
        params["product"]?.each { k, v ->
            String field = k.toString()
            String value = v.toString()
            if (value.length()) {
                fieldsMap[field] = Integer.parseInt(value)
            }
        }
        return fieldsMap
    }

    public List<ProductRawData> getAllProductRawData(ImportConf importConf, Sheet sheet) {
        List<ProductRawData> rawDataList = new ArrayList<ProductRawData>()
        int rowNum = 0
        Iterator<Row> iRow = sheet.iterator()
        while (iRow.hasNext()) {
            Row row = iRow.next()
            if (rowNum == 0) { //skip sheet header row
                rowNum = 1
                continue
            }
            ProductRawData rawData = new ProductRawData()
            importConf.fieldsMap.each { field, colNum ->
                rawData."$field" = commonImportService.getCellValue(row, colNum)
            }
            rawData.rowNum = rowNum++
            Boolean isProduct = HookManager.hook("excelImportProductRawData", true, rawData, importConf)
            if(isProduct) rawDataList.add(rawData)
        }
        return rawDataList
    }

    public def saveAll(List<ProductRawData> rawDataList, ImportConf conf, Task task) {
        Map processed = new LinkedHashMap()
        rawDataList.each { rawData ->
            String matchBy = conf.matchBy
            String matchByData = rawData."${matchBy}"
            try {
                def processedRow = processed[rawData[conf.matchBy] ?: ("" + System.currentTimeMillis())]
                if (processedRow) {
                    task.taskLogger.error(rawData[conf.matchBy], "duplicate.entry.found.row", [processedRow])
                    task.meta.productErrorCount++
                } else {
                    if (rawData[conf.matchBy]) {
                        processed[rawData[conf.matchBy]] = rawData.rowNum;
                    }
                    if (!rawData.name) {
                        task.taskLogger.error(matchByData, "product.name.not.found")
                        task.meta.productErrorCount++
                    } else if (!rawData.basePrice) {
                        task.taskLogger.error(matchByData, "product.price.not.found")
                        task.meta.productErrorCount++
                    } else {
                        Product.withNewTransaction { TransactionStatus status ->
                            Product product = Product.createCriteria().get {
                                maxResults 1
                                eq matchBy, rawData[matchBy]
                            }
                            Boolean isUpdate = false
                            if (product) {
                                if (product.isInTrash || product.isParentInTrash) {
                                    task.taskLogger.warning(matchByData, "product.exists.in.trash")
                                    task.meta.productWarningCount++
                                } else {
                                    if (!conf.isOverwrite) {
                                        task.taskLogger.error(matchByData, "product.exists")
                                        task.meta.productErrorCount++
                                    }
                                    update(rawData, product, conf, task)
                                    isUpdate = true
                                }
                            } else {
                                product = save(rawData, conf, task)
                            }
                            if(product) AppEventManager.fire("import-product", [product, conf, task])
                            if(isUpdate) AppEventManager.fire("product-update", [product.id])
                            status.flush()
                        }
                    }
                }
            } catch (ValidationException e) {
                task.taskLogger.error(matchByData, "validation.error")
                task.meta.productErrorCount++
            } catch (Throwable e) {
                log.error("Product Import Log", e)
                task.taskLogger.error(matchByData, "import.error")
                task.meta.productErrorCount++
            }
            task.meta.productProgress = taskService.countProgress(task.meta.totalProductRecord, ++task.meta.productComplete as Integer)
            task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
        }
        AppEventManager.fire("product-import-complete", [conf, task])
    }

    private Product save(ProductRawData rawData, ImportConf conf, Task task) {
        String matchByData = rawData."${conf.matchBy}"
        try {
            Product product = new Product(sku: commonService.getSKUForDomain(Product), productType: DomainConstants.PRODUCT_TYPE.PHYSICAL)
            setEachMappingFieldData(rawData, product, conf, task)
            product.url = commonService.getUrlForDomain(product);
            product.save()
            if (conf.fieldsMap.parent != null && product.parent?.id) {
                product.parent.with {
                    lock()
                    products.add(Product.proxy(product.id))
                    merge()
                }
            }
            if (conf.fieldsMap.image != null) {
                if (rawData.image) {
                    saveProductImages(product, rawData.image, conf.imageSource, task, matchByData)
                } else {
                    commonImportService.emptyFieldWarning("image", task.taskLogger, matchByData)
                    task.meta.productWarningCount++
                }
            }
            task.taskLogger.success(matchByData, "import.success")
            task.meta.productSuccessCount++
            return product
        } catch (NumberFormatException e) {
            task.taskLogger.error(matchByData, "data.format.mismatch")
            task.meta.productErrorCount++
            return null
        } catch(ApplicationRuntimeException ex) {
            task.taskLogger.error(matchByData, ex.localizedMessage)
            task.meta.productErrorCount++
            return null
        } catch (Exception e) {
            log.error("Product Import Error", e)
            task.taskLogger.error(matchByData, "import.error")
            task.meta.productErrorCount++
            return null
        }
    }

    private def update(ProductRawData rawData, Product product, ImportConf conf, Task task) {
        String matchByData = rawData."${conf.matchBy}"
        try {
            Category oldParent = conf.fieldsMap.parent != null ? product.parent : null
            setEachMappingFieldData(rawData, product, conf, task)
            if (conf.fieldsMap.parent != null && product.parent?.id != oldParent?.id) {
                oldParent?.with {
                    lock()
                    products.remove(product)
                    merge()
                }
                product.parent.with {
                    lock()
                    products.add(Product.proxy(product.id))
                    merge()
                }
            }
            product.merge()
            if (conf.fieldsMap.image != null) {
                if (rawData.image) {
                    saveProductImages(product, rawData.image, conf.imageSource, task, matchByData)
                } else {
                    commonImportService.emptyFieldWarning("image", task.taskLogger, matchByData)
                    task.meta.productWarningCount++
                }
            }
            task.taskLogger.success(matchByData, "import.success")
            task.meta.productSuccessCount++
        } catch (NumberFormatException e) {
            task.taskLogger.error(matchByData, "data.format.mismatch")
            task.meta.productErrorCount++
            return
        } catch(ApplicationRuntimeException ex) {
            task.taskLogger.error(matchByData, ex.localizedMessage)
            task.meta.productErrorCount++
            return null
        } catch (Exception e) {
            log.error("Product Import Error", e)
            task.taskLogger.error(matchByData, "import.error")
            task.meta.productErrorCount++
            return
        }
    }

    private def setEachMappingFieldData(ProductRawData rawData, Product product, ImportConf conf, Task task) {
        conf.fieldsMap.each { field, colNum ->
            if (NamedConstants.PRODUCT_IMPORT_EXTRA_FIELDS.containsKey(field) || field == "image") {
                return;
            }
            product."$field" = castRawData(product, rawData, field, conf, task)
        }
    }

    def castRawData(Product product, ProductRawData rawData, String field, ImportConf conf, Task task) {
        TaskLogger taskLogger = task.taskLogger
        String matchByData = rawData."${conf.matchBy}"
        switch (field) {
            case "name":
                if(rawData.name && rawData.name.size() > 100) {
                    throw new ApplicationRuntimeException("field.length.invalid", ["Name", 100])
                }
                return rawData.name
            case "sku":
                if(rawData.sku && rawData.sku.size() > 50) {
                    throw new ApplicationRuntimeException("field.length.invalid", ["SKU", 50])
                }
                return rawData.sku ?: commonService.getSKUForDomain(Product)
            case "productType":
                if(product.id) {
                    return  product.productType
                }
                String productType = rawData.productType?.trim()
                return productType && NamedConstants.PRODUCT_TYPE[productType] ? productType : DomainConstants.PRODUCT_TYPE.PHYSICAL
            case "parent":
                String fieldName = "category"
                if (rawData.parent) {
                    Category parent
                    if (conf.parentMatchBy == "name") {
                        parent = Category.findByName(rawData.parent)
                    } else {
                        parent = Category.findBySku(rawData.parent)
                    }
                    if (parent) {
                        return parent
                    } else {
                        taskLogger.warning(matchByData, "category.not.found")
                        task.meta.productWarningCount++
                        return null
                    }
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            case "isAvailable":
                String fieldName = "availability"
                boolean availability = null
                if (rawData.isAvailable) {
                    availability = isAvailable(rawData.isAvailable)
                } else {
                    commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                }
                task.meta.productWarningCount++
                product.availableFromDate = availableFromDate(rawData.isAvailable)
                product.availableToDate = availableToDate(rawData.isAvailable)
                product.isAvailableOnDateRange = product.availableToDate ? true : false
                return availability
            case "summary":
                String fieldName = "summary"
                if (rawData.summary) {
                    return rawData.summary
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            case "description":
                String fieldName = "description"
                if (rawData.description) {
                    return rawData.description
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            case "basePrice":
                String fieldName = "base.price"
                if (rawData.basePrice) {
                    return rawData.basePrice.toDouble()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            case "costPrice":
                String fieldName = "cost.price"
                if (rawData.costPrice) {
                    return rawData.costPrice.toDouble()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return 0
            case "salePrice":
                String fieldName = "sale.price"
                if (rawData.salePrice) {
                    product.isOnSale = rawData.salePrice.toDouble() > 0
                    return rawData.salePrice.toDouble()
                } else {
                    return 0.0
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return 0
            case "idx":
                String fieldName = "display.order"
                if (rawData.idx) {
                    return Double.valueOf(rawData.idx).intValue()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return 0
            case "isCallForPriceEnabled":
                String fieldName = "call.for.price"
                if (rawData.isCallForPriceEnabled) {
                    return isCallForPriceEnabled(rawData.isCallForPriceEnabled)
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return false
            case "isInventoryEnabled":
                String fieldName = "inventory.tracking"
                if (rawData.isInventoryEnabled) {
                    return isInventoryEnabled(rawData.isInventoryEnabled)
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return false
            case "availableStock":
                String fieldName = "available.stock"
                if (rawData.availableStock) {
                    return Double.valueOf(rawData.availableStock).intValue()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return product.availableStock
            case "lowStockLevel":
                String fieldName = "low.stock.level"
                if (rawData.lowStockLevel) {
                    return rawData.lowStockLevel.toInteger()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            case "minOrderQuantity":
                String fieldName = "minimum.order.quantity"
                if (rawData.minOrderQuantity) {
                    return Double.valueOf(rawData.minOrderQuantity).intValue()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            case "maxOrderQuantity":
                String fieldName = "maximum.order.quantity"
                if (rawData.maxOrderQuantity) {
                    return Double.valueOf(rawData.maxOrderQuantity).intValue()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            case "model":
                String fieldName = "model"
                if (rawData.model) {
                    return rawData.model
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            case "height":
                String fieldName = "height"
                if (rawData.height) {
                    return rawData.height.toDouble()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return 0
            case "width":
                String fieldName = "width"
                if (rawData.width) {
                    return rawData.width.toDouble()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return 0
            case "weight":
                String fieldName = "weight"
                if (rawData.weight) {
                    return rawData.weight.toDouble()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return 0
            case "length":
                String fieldName = "length"
                if (rawData.length) {
                    return rawData.length.toDouble()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return 0
            case "taxProfile":
                String fieldName = "tax.profile"
                if (rawData.taxProfile) {
                    TaxProfile taxProfile = TaxProfile.findByName(rawData.taxProfile)
                    if (taxProfile) {
                        return taxProfile
                    } else {
                        commonImportService.dataNotFoundWarning(fieldName, taskLogger, matchByData)
                        task.meta.productWarningCount++
                    }
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            case "shippingProfile":
                String fieldName = "shipping.profile"
                if (rawData.shippingProfile) {
                    ShippingProfile shippingProfile = ShippingProfile.findByName(rawData.shippingProfile)
                    if (shippingProfile) {
                        return shippingProfile
                    } else {
                        commonImportService.dataNotFoundWarning(fieldName, taskLogger, matchByData)
                        task.meta.productWarningCount++
                    }
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            case "metaTags":
                String fieldName = "meta.tag"
                if (rawData.metaTags) {
                    List<MetaTag> metaTags = commonImportService.generateMetaTags(rawData.metaTags)
                    if (metaTags.size()) {
                        return metaTags
                    } else {
                        commonImportService.dataNotFoundWarning(fieldName, taskLogger, matchByData)
                        task.meta.productWarningCount++
                    }
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            case "videos":
                String fieldName = "video"
                if (rawData.videos) {
                    return null
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            default:
                return null
        }
    }

    public def saveProductImages(Product product, String images, String imageSource, Task task, String matchByData) {
        String filePath = productService.processFilePath(product)
        //remove prev image
        File imgDir = new File(filePath)
        if (imgDir.exists()) {
            Set<String> prefixes = [""] as HashSet //for base image
            prefixes.addAll(imageService.getSizes("product-image").keySet().collect { it + "-" })
            ProductImage.createCriteria().list {
                eq("product", product)
            }.each {
                try {
                    String fileName = it.name
                    File file = new File(filePath + "/" + fileName)
                    file.delete()
                    prefixes.each { prefix ->
                        file = new File(filePath + "/" + prefix + fileName)
                        file.delete()
                    }
                    it.delete()
                } catch (FileNotFoundException ex) {
                    log.error("Error processing on : " + ex)
                } catch (Exception ex) {
                    log.error("Error processing on : " + ex)
                }
            }
        }
        List<String> imageList = images.split(',')
        Integer lastImageIdx = ProductImage.createCriteria().count { eq("product", product) }
        imageList.each {
            String name = it.trim()
            InputStream inputStream = commonImportService.findImageStream(name, imageSource)
            if (inputStream) {
                ProductImage productImage
                try {
                    name = productService.processImageName(filePath, name)
                    productImage = new ProductImage(name: name, product: product, idx: ++lastImageIdx)
                    imageService.createCopies(inputStream, name, productImage, imageService.getSizes("product-image"))
                } catch (Exception exc) {
                    log.error("Copy Import Product Image Error: " + exc)
                    task.taskLogger.warning(matchByData, "product.image.copy.error")
                    task.meta.productWarningCount++
                    return
                }
                try {
                    productImage.save()
                } catch (Exception exc) {
                    log.error("Product Image Save Error: " + exc)
                    task.taskLogger.warning(matchByData, "product.image.save.error")
                    task.meta.productWarningCount++
                    return
                }
                product.addToImages(productImage)
            } else {
                task.taskLogger.warning(matchByData, "product.image.save.error")
                task.meta.productWarningCount++
            }
        }
        product.merge()
    }

    boolean isInventoryEnabled(String trackInventory) {
        if (trackInventory) {
            if (["YES", "Y"].contains(trackInventory)) {
                return true
            }
        }
        return false
    }

    boolean isCallForPriceEnabled(String callForPrice) {
        if (callForPrice) {
            if (["YES", "Y"].contains(callForPrice)) {
                return true
            }
        }
        return false
    }

    boolean isAvailable(String avail) {
        if (avail) {
            List<String> avails = avail.split(",")
            for (String av : avails) {
                if (av.matches(DATE_YY) || av.matches(DATE_YYYY) || ["A", "AVAILABLE", "Y", "YES"].contains(av.toUpperCase())) {
                    return true
                }
            }
        }
        return false
    }

    private Date availableFromDate(String date) {
        if (date) {
            List<String> dates = date.split(",")
            if (dates.size() > 0) {
                SimpleDateFormat fmt = null;
                if (dates[0].matches(DATE_YY)) {
                    fmt = new SimpleDateFormat("dd/MM/yy")
                } else if (dates[0].matches(DATE_YYYY)) {
                    fmt = new SimpleDateFormat("dd/MM/yyyy")
                }
                if (fmt) {
                    return fmt.parse(fmt.format(fmt.parse(dates[0])))
                }
            }
        }
        return null
    }

    private Date availableToDate(String date) {
        if (date) {
            List<String> dates = date.split(",")
            if (dates.size() > 1) {
                dates[1] = dates[1].trim()
                SimpleDateFormat fmt = null;
                if (dates[1].matches(DATE_YY)) {
                    fmt = new SimpleDateFormat("dd/MM/yy")
                } else if (dates[1].matches(DATE_YYYY)) {
                    fmt = new SimpleDateFormat("dd/MM/yyyy")
                }
                if (fmt) {
                    return fmt.parse(fmt.format(fmt.parse(dates[1])))
                }
            }
        }
        return null
    }
}
