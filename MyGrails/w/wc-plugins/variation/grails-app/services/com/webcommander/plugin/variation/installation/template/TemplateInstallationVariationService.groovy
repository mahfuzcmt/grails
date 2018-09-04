package com.webcommander.plugin.variation.installation.template

import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.installation.template.InstallationDataHolder
import com.webcommander.manager.HookManager
import com.webcommander.models.TemplateData
import com.webcommander.installation.template.TemplateInstallationService
import com.webcommander.plugin.variation.ProductVariation
import com.webcommander.plugin.variation.VariationDetails
import com.webcommander.plugin.variation.VariationOption
import com.webcommander.plugin.variation.VariationType
import com.webcommander.util.DomainUtil
import com.webcommander.webcommerce.Product
import grails.util.Holders


class TemplateInstallationVariationService {

    static {
        AppEventManager.on("before-product-type-template-content-collect", {TemplateData templateData ->
            TemplateInstallationVariationService.getInstance().collectVariationTypeAndOption(templateData)
        });

        AppEventManager.on("before-product-type-template-content-save", {TemplateData templateData, InstallationDataHolder installationDataHolder ->
            TemplateInstallationVariationService.getInstance().saveVariationTypeAndOption(templateData, installationDataHolder)
        });

        HookManager.register("product-type-template-content", { Map data, Product product->
            return TemplateInstallationVariationService.getInstance().collectProductVariation(data, product)
        });

        AppEventManager.on("product-type-template-content-save", {TemplateData templateData, InstallationDataHolder installationDataHolder, Map data ->
            TemplateInstallationVariationService.getInstance().saveProductVariation(templateData, installationDataHolder, data)
        })
    }

    CommonService commonService
    TemplateInstallationService templateInstallationService

    static TemplateInstallationVariationService getInstance() {
        return Holders.grailsApplication.mainContext.getBean(TemplateInstallationVariationService)
    }

    void collectVariationTypeAndOption(TemplateData templateData) {
        List<VariationType> types = VariationType.list()
        templateData.otherContents["variation_type"] = types.collect {
            Map typeData = DomainUtil.toMap(it, [exclude: ["options"]])
            typeData.options = it.options.collect {
                templateData.resources.add "variation/option/option-${it.id}/"
                return DomainUtil.toMap(it, [exclude: ["type"]])
            }
            return typeData
        }
    }

    void saveVariationTypeAndOption(TemplateData templateData, InstallationDataHolder installationDataHolder) {
        List<Map> types = templateData.getOtherContents("variation_type")
        types.each {Map typeData ->
            try {
                VariationType type = new VariationType()
                DomainUtil.populateDomainInst(type, typeData, [exclude: ["options"]])
                if (!commonService.isUnique(type, "name")) {
                    type.name = commonService.getCopyNameForDomain(type)
                }
                type.save();
                templateInstallationService.saveContentMapping(type.id, "variation_type")
                installationDataHolder.setContentMapping("variation_type", typeData.id, "id", type.id)
                typeData.options.each {Map optionData ->
                    VariationOption option = new VariationOption(type: type);
                    DomainUtil.populateDomainInst(option, optionData, [exclude: ["type"]])
                    option.save()
                    templateInstallationService.moveTemplateData(installationDataHolder, "resources/variation/option/option-${optionData.id}", "variation/option/option-${option.id}")
                    installationDataHolder.setContentMapping("variation_option", optionData.id, "id", option.id)
                    templateInstallationService.saveContentMapping(option.id, "variation_option")
                }
            } catch (Exception ex) {}

        }
    }

    Map collectProductVariation(Map data, Product product) {
        List<ProductVariation> variations = ProductVariation.findAllByProduct(product)
        data.product_variations = variations.collect {
            Map variationData = DomainUtil.toMap(it, [exclude: ["details"]])
            variationData.details = DomainUtil.toMap(it.details, [exclude: []])
            variationData.details.modelData = HookManager.hook("${it.details.model}-variation-details-data", variationData.details.modelData, it.details.modelId)
            return variationData
        }
        return data
    }

    void saveProductVariation(TemplateData templateData, InstallationDataHolder installationDataHolder, Map productData) {
        List variationDataList = productData.product_variations ?: []
        Long productId = installationDataHolder.getContentMapping(DomainConstants.WIDGET_CONTENT_TYPE.PRODUCT, productData.id, "id")
        Product product = Product.get(productId)
        variationDataList.each {Map variationData ->
            ProductVariation productVariation = new ProductVariation(product: product)
            variationData.options = installationDataHolder.getContentMappings("variation_option", variationData.options, "id")
            DomainUtil.populateDomainInst(productVariation, variationData, [exclude: ["details", "product"]])
            VariationDetails details = new VariationDetails(product: product)
            DomainUtil.populateDomainInst(details, variationData.details, [exclude: ["product", "modelId"]])
            AppEventManager.fire("before-variation-details-type-template-content-save", [details, variationData, installationDataHolder])
            details.save()
            productVariation.details = details
            productVariation.save()
        }
    }
}
