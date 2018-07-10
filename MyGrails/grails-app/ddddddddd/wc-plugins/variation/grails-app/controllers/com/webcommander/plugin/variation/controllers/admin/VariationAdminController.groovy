package com.webcommander.plugin.variation.controllers.admin

import com.webcommander.authentication.annotations.License
import com.webcommander.common.CommonService
import com.webcommander.plugin.variation.ProductVariation
import com.webcommander.plugin.variation.VariationOption
import com.webcommander.plugin.variation.VariationService
import com.webcommander.plugin.variation.VariationType
import com.webcommander.plugin.variation.util.VariationUtils
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.webcommerce.Product
import grails.converters.JSON

class VariationAdminController {
    VariationService variationService
    CommonService commonService

    @License(required = "allow_variation_feature")
    def loadAppView() {
        render(view: "/plugins/variation/admin/tabView", model: [d: true])
    }

    def loadVariationTypes() {
        Integer count = variationService.getTypeCount(params)
        params.max = params.max ?: "10";
        List<VariationType> types = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            variationService.getTypes(params)
        }
        render view: "/plugins/variation/admin/typeView", model: [count: count, types: types];
    }

    def loadVariationOptions() {
        Integer count = variationService.getOptionCount(params)
        params.max = params.max ?: "10";
        List<VariationOption> options = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            variationService.getOptions(params)
        }
        List<VariationType> types = variationService.getTypes([offset: 0, max: -1])
        render view: "/plugins/variation/admin/optionView", model: [count: count, types: types, options: options];
    }

    @License(required = "allow_variation_feature")
    def saveType() {
        def type = variationService.saveType(params)
        if(type) {
            render ([typeId: type.id, status: "success", message: g.message(code: "variation.type.save.success")] as JSON)
        } else {
            render ([status: "error", message: g.message(code: "variation.type.save.failed")] as JSON)
        }
    }


    def deleteType() {
        try {
            if(variationService.deleteType(params, params.at2_reply, params.at1_reply)) {
                render ([status: "success", message: g.message(code: "variation.type.remove.success")] as JSON)
            } else {
                render ([status: "error", message: g.message(code: "variation.type.remove.failed")] as JSON)
            }
        } catch (AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }

    }

    def deleteValue() {
        try {
            if(variationService.deleteValue(params, params.at2_reply, params.at1_reply)) {
                render ([status: "success", message: g.message(code: "variation.value.remove.success")] as JSON)
            } else {
                render ([status: "error", message: g.message(code: "variation.value.remove.failed")] as JSON)
            }
        } catch (AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    @License(required = "allow_variation_feature")
    def saveOption() {
        def uploadedFile = params.representingImage ? request.getFile("representingImage") : null;
        def option = variationService.saveOption(params, uploadedFile)
        if(option) {
            render ([option: option, status: "success", message: g.message(code: "variation.type.save.success")] as JSON)
        } else {
            render ([status: "error", message: g.message(code: "variation.type.save.failed")] as JSON)
        }
    }

    def productEditorTabView() {
        List types = variationService.getUsableTypes()
        Long productId = params.long("id") ?: 0
        Product product = Product.get(productId)
        ProductVariation pVariation = ProductVariation.findByProduct(product)
        List<VariationType> selectedTypes = variationService.getSelectedVariationTypes(productId)
        Boolean hasVariation = variationService.hasVariation(productId)
        String model = variationService.getVariationModel(productId)
        List typeOptionsMaps = []
        selectedTypes.each {
            typeOptionsMaps.push([
                    type: it,
                    all: variationService.getOptionsByType(it.id),
                    selected: variationService.getSelectedVariationOptions(productId, it.id)
            ])
        }
        render view: "/plugins/variation/admin/productEditorView", model: [product: product, variation: pVariation ?: new ProductVariation(),
                                                                           types: types, hasVariation: hasVariation, model: model, selectedTypes: selectedTypes, typeOptionsMaps: typeOptionsMaps]
    }

    def loadCombination() {
        Product product = Product.get(params.pId)
        List<ProductVariation> variations = ProductVariation.findAllByProduct(product)
        List<VariationOption> options = variations ? variations.options.flatten().unique() : []
        List<VariationType> selectedTypes = variationService.getSelectedVariationTypes(product.id)
        List typeOptionsMap = []
        Map config = [:]
        Map selectedValues = params.findAll {it.key.startsWith("combobox")}.sort {it.key}
        selectedTypes.eachWithIndex { type, i ->
            Map typeMap = [
                    type: type,
                    selected: options.findAll {it.type.id == type.id}
            ]
            selectedValues.eachWithIndex { val, idx ->
                def value = val.value.toLong()
                if (typeMap.selected.id.contains(value)) {
                    typeMap['c-value' + (idx+1)] = value
                }
            }
            typeOptionsMap.push(typeMap)
            config.putAll(VariationUtils.getCombinationConfig(i, type.id))
        }
        if (params.config) {
            params['config'].each { k, v ->
                config[k] = v.toLong()
            }
        }
        render(view: "/plugins/variation/admin/variationCombination", model: [product: product, typeOptionsMap: typeOptionsMap, config: config])
    }

    def optionsForType() {
        Long typeId = params.long("id") ?: 0
        render view: "/plugins/variation/admin/chooseOptionsForType", model: [type: VariationType.get(typeId), allOptions: variationService.getOptionsByType(typeId), selectedOptions: []]
    }

    @License(required = "allow_variation_feature")
    def saveVariation() {
        def assigned = variationService.saveVariation(params);
        if(assigned) {
            render ([status: "success", message: g.message(code: "variation.save.success")] as JSON)
        } else {
            render ([status: "error", message: g.message(code: "variation.save.failed")] as JSON)
        }
    }

    def loadConfigView() {
        Long productId = params.long("id") ?: 0
        Product product = Product.get(productId)
        List<VariationType> selectedTypes = variationService.getSelectedVariationTypes(productId)
        Map config = [:]
        selectedTypes.eachWithIndex { type, i ->
            config.putAll(VariationUtils.getCombinationConfig(i, type.id))
        }
        if (params.config) {
            params['config'].each { k, v ->
                config[k] = v.toLong()
            }
        }
        render view: "/plugins/variation/admin/variationView", model: [product: product, config: config, types: selectedTypes]
    }

    def addOption() {
        def updated = variationService.addOption(params);
        if(updated) {
            render ([status: "success", message: g.message(code: "variation.update.success")] as JSON)
        } else {
            render ([status: "error", message: g.message(code: "variation.save.failed")] as JSON)
        }
    }

    def removeOption() {
        Boolean remove = variationService.removeOption(params)
        if(remove) {
            render ([status: "success"] as JSON)
        } else {
            render ([status: "error", message: g.message(code: "option.remove.failed")] as JSON)
        }
    }

    def activateVariation() {
        params.activateOnly = true
        Boolean activate = variationService.activateVariation(params)
        if(activate) {
            render ([status: "success"] as JSON)
        } else {
            render ([status: "error", message: g.message(code: "variation.activation.failed")] as JSON)
        }
    }

    def setDefault() {
        Boolean isDefault = variationService.setDefault(params)
        if(isDefault) {
            render ([status: "success"] as JSON)
        } else {
            render ([status: "error", message: g.message(code: "change.default.failed")] as JSON)
        }
    }

}
