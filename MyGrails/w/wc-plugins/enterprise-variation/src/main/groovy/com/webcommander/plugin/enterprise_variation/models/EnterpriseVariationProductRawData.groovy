package com.webcommander.plugin.enterprise_variation.models

import com.webcommander.item.ProductRawData
import com.webcommander.plugin.enterprise_variation.EvariationDetails
import com.webcommander.plugin.variation.ProductVariation
import com.webcommander.plugin.variation.VariationDetails
import com.webcommander.plugin.variation.VariationOption
import com.webcommander.webcommerce.Product

class EnterpriseVariationProductRawData extends ProductRawData {
    String enterpriseVariation
    String baseProduct

    public EnterpriseVariationProductRawData(ProductVariation variation) {
        super(variation.product, ["enterpriseVariation", "baseProduct"])
        Product product = variation.product
        baseProduct = product.sku
        enterpriseVariation = ""
        variation.options.eachWithIndex { VariationOption entry, int i ->
            if(i > 0) {
                enterpriseVariation +=","
            }
            enterpriseVariation += entry.type.name + ":" + entry.value
        }
        VariationDetails details = variation.details
        EvariationDetails eDetails = EvariationDetails.get(details.modelId)
        if(eDetails) {
            Map prop = eDetails.options.collectEntries {
                def field = it.field
                field = field.substring(field.indexOf(".") + 1)
                if(field == "description") {
                    [(field): it.description.content]
                } else {
                    [(field) : it.value]
                }
            }
            List ignores = ["extraProperties", "class", "null"]
            this.properties.each {String key, Object ob ->
                if(!ignores.contains(key)) {
                    def value = null
                    switch (key) {
                        case "rowNum":
                            break
                        case "name":
                            value = eDetails.name
                            break
                        case "sku":
                            value = eDetails.sku
                            break
                        case "image":
                            value = eDetails.images ? eDetails.images.name.join(",") : "";
                            break;
                        case "videos":
                            value = eDetails.videos ? eDetails.videos.name.join(",") : "";
                            break;
                        case "metaTags":
                            value = eDetails.metaTags.collect { it.name + "," + it.value  }.join(",");
                            break;
                        case "isInventoryEnabled":
                            value = eDetails.isInventoryEnabled ? "YES" : "NO";
                            break;
                        case "availableStock":
                            value = eDetails.availableStock
                            break
                        case "lowStockLevel":
                            value = eDetails.lowStockLevel
                            break
                        default:
                            value = prop[key] ?: this[key];
                    }
                    this[key] = value?.toString()
                }
            }

        }
    }
}
