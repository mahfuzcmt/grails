package com.webcommander.plugin.variation.models

import com.webcommander.item.ImportConf
import com.webcommander.plugin.variation.ProductVariation
import com.webcommander.task.Task

interface VariationServiceModel {
    Boolean importVariation(ProductVariation variation, ProductVariationRawData variationRawData, ImportConf conf, Task task);
}