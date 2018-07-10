package com.webcommander.plugin.variation

import com.webcommander.events.AppEventManager
import com.webcommander.item.ImportConf
import com.webcommander.item.ProductRawData
import com.webcommander.manager.HookManager
import com.webcommander.plugin.variation.constant.DomainConstants
import com.webcommander.plugin.variation.factory.VariationObjectsProducer
import com.webcommander.plugin.variation.models.ProductVariationRawData
import com.webcommander.plugin.variation.models.VariationServiceModel
import com.webcommander.task.MultiLoggerTask
import com.webcommander.util.AppUtil
import com.webcommander.web.multipart.WebCommanderMultipartFile
import com.webcommander.webcommerce.ExportService
import com.webcommander.webcommerce.Product
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.apache.poi.xssf.usermodel.XSSFSheet
import grails.util.TypeConvertingMap
import org.springframework.web.multipart.MultipartFile

class VariationImportExportService {
    private static VariationImportExportService _variationImportExportService

    VariationService variationService
    ExportService exportService

    private static VariationImportExportService getVariationImportExportService() {
        return _variationImportExportService ?: (_variationImportExportService = Holders.grailsApplication.mainContext.getBean(VariationImportExportService))
    }

    static {
        HookManager.register("productExport", {Integer rowCount, Product product, Map header, productSheet ->
            VariationService variationService = VariationService.getInstance()
            if(variationService.allowed()) {
                variationImportExportService.exportVariation(rowCount, product, header, productSheet)
            }
        })

        HookManager.register("excelImportProductRawData", {Boolean isProduct, ProductRawData rawData, ImportConf importConf ->
           return variationImportExportService.excelImportProductRawData(isProduct, rawData, importConf)
        });

        AppEventManager.on("import-product", {Product product, ImportConf conf, MultiLoggerTask task ->
            try {
                HashMap<String, List<ProductVariationRawData>> variationRawDataHolder = conf.extraData.variationRawDataHolder
                List<ProductVariationRawData> variationRawDataList
                if(variationRawDataHolder && (variationRawDataList = variationRawDataHolder.remove(product.sku))) {
                    variationImportExportService.importProductVariation(product, variationRawDataList, conf, task)
                }
            } catch (Throwable t) {
                task.taskLogger.error(product.name, "Variation couldn't be imported")
                task.meta.productErrorCount++
            }
        })

        AppEventManager.on("product-import-complete", {ImportConf conf, MultiLoggerTask task ->
            try {
                HashMap<String, List<ProductVariationRawData>> variationRawDataHolder = conf.extraData.variationRawDataHolder
                variationRawDataHolder.each { String baseProduct, List<ProductVariationRawData> dataList ->
                    task.taskLogger.error(baseProduct, "Base product not found ${dataList.size()} variation(s) couldn't be imported");
                    task.meta.productErrorCount++
                }
            } catch (Throwable ignore) {}
        })
    }


    Integer exportVariation(Integer rowCount, Product product, Map header, XSSFSheet productSheet) {
        List<ProductVariation> variations = ProductVariation.createCriteria().list {
            eq("product.id", product.id)
            eq("active", true)
        }
        variations.each {
            ProductRawData data = VariationObjectsProducer.getProductRawData(it)
            if(data) {
                exportService.addProductRow(productSheet, header, data, rowCount)
                rowCount++
            }
        }
        return rowCount
    }

    Boolean excelImportProductRawData(Boolean isProduct, ProductRawData rawData, ImportConf importConf) {
        HashMap<String, List<ProductVariationRawData>> variationRawDataHolder = importConf.extraData.variationRawDataHolder
        if (variationRawDataHolder == null) {
            importConf.extraData.variationRawDataHolder = variationRawDataHolder = new HashMap<String, List<ProductVariationRawData>>()
        }
        String baseProduct = rawData.baseProduct?.trim()
        if(baseProduct) {
            List<ProductVariationRawData> variationRawDataList = variationRawDataHolder[baseProduct]
            if(variationRawDataList == null) {
                variationRawDataHolder[baseProduct] = variationRawDataList = new ArrayList<ProductVariationRawData>()
            }
            ProductVariationRawData variationRawData = new ProductVariationRawData(rawData)
            String variations = ""
            for(String model : DomainConstants.VARIATION_MODELS.keySet()) {
                variations = rawData."${model}Variation"
                if(variations) {
                    variationRawData.model = model
                    break
                }
            }
            ArrayList<Map> combination = new ArrayList<Map>()
            List<String> variationList = variations.split(",")
            for (String option : variationList) {
                List<String> list = option.split(":")
                if(list.size() != 2) {
                    combination = null
                    break
                }
                combination.add([optionType: list[0].trim(), optionValue: list[1].trim()])
            }
            variationRawData.combination = combination
            if(variationRawData) variationRawDataList.add(variationRawData)
            return false
        }
        return isProduct
    }

    private Map generateVariationTypeAndOption(Map<String, Set<String>> typesAndOptionMappings, ImportConf conf) {
        Map results = [variationType: []]
        typesAndOptionMappings.each {String typeName, Set<String> options ->
            VariationType type = VariationType.findByName(typeName)
            if(type == null) {
                type = variationService.saveType([name: typeName, standard: DomainConstants.VARIATION_REPRESENTATION.TEXT])
            }
            results["variationType"].add(type.id)
            results[type.id] = [variationOption: []]
            for (String optionValue : options) {
                VariationOption option = VariationOption.findByTypeAndValue(type, optionValue)
                if(option == null) {
                    MultipartFile multipartFile = null
                    File file
                    if(type.standard == DomainConstants.VARIATION_REPRESENTATION.IMAGE && conf.imageSource && (file = new File(Holders.servletContext.getRealPath(conf.imageSource), optionValue)) && file.exists() && file.size() <= 5120) {
                        multipartFile = new WebCommanderMultipartFile(optionValue, file.newInputStream())
                    }
                    option = variationService.saveOption(new TypeConvertingMap([label: optionValue, value: optionValue, type: type.id, order: 0]), multipartFile)
                }
                results[type.id].variationOption.add(option.id)
            }
            results[type.id].variationOption.sort()
        }
        results.variationType.sort()
        return results
    }

    void updateVariation(ProductVariation variation, ProductVariationRawData variationRawData, ImportConf conf, MultiLoggerTask task) {
        VariationServiceModel service = VariationObjectsProducer.getVariationServiceBean(variationRawData.model)
        if(service && service.importVariation(variation, variationRawData, conf, task)) {
            variation.active = true
            variation.save()
        }
    }

    @Transactional
    void importProductVariation(Product product, List<ProductVariationRawData> variationRawDataList, ImportConf conf, MultiLoggerTask task) {
        List<ProductVariation> productVariations = ProductVariation.findAllByProduct(product);
        String variationModel = variationRawDataList[0].model
        if(productVariations.size() == 0) {
            Map<String, Set<String>> typesAndOptionMappings = [:]
            Integer length = variationRawDataList.size()
            Integer noOfOption = null
            for (Integer i = 0; i < length; i++) {
                ProductVariationRawData variationRawData = variationRawDataList[i]
                if(variationRawData.combination && variationRawData.combination.size() == (noOfOption = noOfOption ?: variationRawData.combination.size())) {
                    for (Map option : variationRawData.combination) {
                        if(typesAndOptionMappings[option.optionType] == null) {
                            typesAndOptionMappings[option.optionType] = new HashSet<String>()
                        }
                        typesAndOptionMappings[option.optionType].add(option.optionValue)
                    }
                } else {
                    variationRawDataList.remove(variationRawData)
                    task.taskLogger.error(product.name, "Invalid Variation. Row No: ${variationRawData.rawData.rowNum}")
                    task.meta.productErrorCount++
                    i--
                    length--
                }
            }
            if(typesAndOptionMappings.size() != noOfOption) {
                task.taskLogger.error(product.name, "Invalid Variations")
                return
            }
            if(variationRawDataList.size() == 0) {
                return
            }
            Map params = generateVariationTypeAndOption(typesAndOptionMappings, conf)
            params.model = variationModel
            productVariations = variationService.saveVariation(new TypeConvertingMap(params), product)
        }
        for (ProductVariationRawData variationRawData : variationRawDataList) {
            ProductVariation variation = productVariations.find {
                Integer size = it.options.size()
                return variationRawData.combination && variationRawData.combination.size() == size &&
                        variationRawData.combination.optionType*.toLowerCase().intersect(it.options.type.name*.toLowerCase()).size() == size &&
                        variationRawData.combination.optionValue*.toLowerCase().intersect(it.options.collect { it.value.toLowerCase() }).size() == size
            }
            if(variation) {
                updateVariation(variation, variationRawData, conf, task)
            } else {
                task.taskLogger.error(product.name, "Combination not found. Row No: ${variationRawData.rawData.rowNum}")
                task.meta.productErrorCount++
            }
        }
    }
}
