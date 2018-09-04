package com.webcommander.plugin.compare_product

import com.webcommander.annotations.Initializable
import com.webcommander.events.AppEventManager
import com.webcommander.plugin.compare_product.models.CustomCompareData
import com.webcommander.webcommerce.ProductService
import grails.gorm.transactions.Transactional

@Transactional
@Initializable
class CompareProductService {
    CustomCompareData tempData
    ProductService productService


    static void initialize(){
        AppEventManager.on("before-product-delete", { id ->
            CustomProperties.createCriteria().list {
                eq("product.id", id)
            }*.delete();
        })
    }

    List<CustomCompareData> sortCustomProperties(List<Long> productIds) {
        Long rank = 1L;
        Map<Long, Long> productOrder = [:]
        productIds.each {
            productOrder.put(it, rank)
            rank++;
        }

        List<CustomProperties> customProperties = CustomProperties.createCriteria().list {
            inList ("product.id", productIds)
            order ("product.id")
        };

        List<String> labels = CustomProperties.where {
            inList("product.id", productIds)
        }.distinct("label").list();

        List<CustomCompareData> customCompareDataList = new ArrayList<CustomCompareData>();

        for( l in labels ) {
            String label = l;
            Long count = 0;
            Long idx = 100000000L;
            List<String> description = new ArrayList<String>();
            Map<Long, String> tempDescription = [:]
            for( cp in customProperties) {
                    if(cp.label == label) {
                        tempDescription.put(cp.product.id, cp.description)
                        count++;
                        Long order = productOrder.get(cp.product.id)
                        if(idx == 100000000L || (idx > order)) {
                            idx = order
                        }
                    }
            }
            productIds.each {
                String descriptionString = tempDescription.get(it) ?: "N/A"
                description.add(descriptionString)
            }
            tempData = new CustomCompareData(label, count, idx, description)
            customCompareDataList.add(tempData);
        }

        customCompareDataList.sort { first, second ->
            second.matched <=> first.matched ?: first.rank <=> second.rank
        }
        return customCompareDataList
    }
}
