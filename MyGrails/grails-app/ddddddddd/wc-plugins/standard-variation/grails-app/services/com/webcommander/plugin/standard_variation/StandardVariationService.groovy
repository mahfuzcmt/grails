package com.webcommander.plugin.standard_variation

import com.webcommander.annotations.Initializable
import com.webcommander.events.AppEventManager
import com.webcommander.installation.template.InstallationDataHolder
import com.webcommander.item.ImportConf
import com.webcommander.item.ProductRawData
import com.webcommander.manager.HookManager
import com.webcommander.plugin.standard_variation.constant.DomainConstants
import com.webcommander.plugin.variation.ProductVariation
import com.webcommander.plugin.variation.VariationDetails
import com.webcommander.plugin.variation.VariationService
import com.webcommander.plugin.variation.models.ProductVariationRawData
import com.webcommander.plugin.variation.models.VariationServiceModel
import com.webcommander.task.Task
import com.webcommander.util.DomainUtil
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductImage
import grails.gorm.transactions.Transactional
import grails.util.Holders

@Initializable
@Transactional
class StandardVariationService implements VariationServiceModel {

    static void initialize() {
        AppEventManager.on("before-standard-variation-details-delete", { id ->
            SvariationDetails details = SvariationDetails.get(id)
            details?.delete()
        })

        HookManager.register("standard-variation-details-data", {Map data,  Long id ->
            SvariationDetails details = SvariationDetails.get(id)
            if(details) {
                data = DomainUtil.toMap(details)
            }
            return data
        })

        AppEventManager.on("before-variation-details-type-template-content-save", { VariationDetails details, Map variationData, InstallationDataHolder installationDataHolder->
            Map modelData = variationData.details.modelData
            if(modelData) {
                modelData.imageId = installationDataHolder.getContentMapping("product_image", modelData.imageId, "id")
                SvariationDetails svariationDetails = new SvariationDetails()
                DomainUtil.populateDomainInst(svariationDetails, variationData.details.modelData)
                svariationDetails.save()
                details.modelId = svariationDetails.id
            }
        })
    }

    static {

        HookManager.register("before-price-available-change", { Map model, Map params ->
            VariationService variationService = VariationService.getInstance()
            if(variationService.allowedStandard()) {
                def product = Product.get(params.productId)
                Map config = params.config ?: [:]
                List options = config.options instanceof String ? [config.options] : config.options as List
                ProductVariation variation = config.variation ? ProductVariation.get(config.variation) : variationService.getVariationByOptions(product, options)
                if (variation && !variation.active) {
                    def messageSource = Holders.applicationContext.getBean('messageSource')
                    model = [status: "error", message: messageSource.getMessage("not.available" , [] as Object[], "not.available", Locale.default)]
                }
            }
            return model
        })

        AppEventManager.on("standard-variation-activate", { VariationDetails details ->
            if(!details.modelId) {
                def sVariationDetails = new SvariationDetails().save()
                details.modelId = sVariationDetails.id
                details.save()
            }
        })
    }

    Boolean updateCombination(Map params) {
        ProductVariation variation = ProductVariation.get(params.id)
        VariationDetails variationDetails = variation.details
        SvariationDetails standardVariationDetails
        if(variationDetails.modelId) {
            standardVariationDetails = SvariationDetails.get(variationDetails.modelId);
        } else {
            standardVariationDetails = new SvariationDetails()
        }
        standardVariationDetails.imageId = params.image ? params.image : null
        standardVariationDetails.priceAdjustableType = params.adjustmentType
        standardVariationDetails.price = params.amount ? params['amount'].toDouble() : 0.0
        standardVariationDetails.save()
        variationDetails.modelId = standardVariationDetails.id
        variation.save()
        AppEventManager.fire("variation-update", [variation.product.id])
        return !variation.hasErrors()
    }

    @Override
    Boolean importVariation(ProductVariation variation, ProductVariationRawData variationRawData, ImportConf conf, Task task) {
        ProductRawData rawData = variationRawData.rawData
        String matchByData = "Standard Variation: " + (rawData.name ?: "")
        VariationDetails variationDetails = variation.details
        SvariationDetails standardVariationDetails
        if(variationDetails.modelId) {
            standardVariationDetails = SvariationDetails.get(variationDetails.modelId);
        } else {
            standardVariationDetails = new SvariationDetails()
        }
        if(rawData.basePrice) {
            standardVariationDetails.priceAdjustableType = DomainConstants.PRICE_ADJUSTABLE_TYPE.FIXED
            standardVariationDetails.price = rawData.basePrice.toDouble()
        } else {
            standardVariationDetails.priceAdjustableType = DomainConstants.PRICE_ADJUSTABLE_TYPE.BASE
        }
        if(conf.fieldsMap["image"]) {
            ProductImage image = variation.product.images.find {
                it.name == rawData.image
            }
            if(rawData.image && image == null) { task.taskLogger.warning(matchByData, "Image doesn't exists in base product")}
            standardVariationDetails.imageId = image ? image.id : null
        }
        standardVariationDetails.save()
        variationDetails.modelId = standardVariationDetails.id
        variation.save()
        return true
    }
}
