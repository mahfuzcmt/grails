package com.webcommander.plugin.enterprise_variation

import com.webcommander.common.MetaTag
import com.webcommander.common.Resource
import com.webcommander.plugin.variation.ProductVariation
import com.webcommander.plugin.variation.VariationDetails

class EvariationDetails {

    Long id
    String name
    String sku
    String url
    Boolean isInventoryEnabled = false
    Integer availableStock = 0
    Integer lowStockLevel = 0
    Resource spec
    Resource productFile

    Collection<VariationInventoryHistory> inventoryHistory = []
    Collection<EvariationDetailsOption> options = []
    Collection<VariationProductImage> images = []
    Collection<VariationProductVideo> videos = []
    Collection<MetaTag> metaTags = []

    static hasMany = [options : EvariationDetailsOption, inventoryHistory: VariationInventoryHistory, images: VariationProductImage, videos: VariationProductVideo, metaTags: MetaTag]

    static transients = ['findProductVariation']

    static constraints = {
        name(blank: false, size: 2..110)
        sku(blank: false, maxSize: 55, unique: true)
        url(blank: false, maxSize: 110, unique: true)
        lowStockLevel(nullable: true)
        spec(nullable: true, maxSize: 200)
        productFile(nullable: true)
    }

    static mapping = {
        metaTags cache: true
        inventoryHistory cache: true
        images sort: "idx", order: "asc"
        images cache: true
        videos cache: true
    }

    public ProductVariation findProductVariation() {
        return ProductVariation.findByDetails(VariationDetails.findByModelAndModelId("enterprise", id))
    }
}
