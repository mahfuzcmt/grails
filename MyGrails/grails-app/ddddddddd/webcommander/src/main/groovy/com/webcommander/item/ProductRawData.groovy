package com.webcommander.item

import com.webcommander.webcommerce.Product

class ProductRawData {
    Map<String, String> extraProperties = new HashMap<String, String>();

    Integer rowNum
    String name
    String sku
    String productType
    String parent
    String isAvailable
    String summary
    String description
    String basePrice
    String costPrice
    String idx
    String salePrice
    String isCallForPriceEnabled
    String isInventoryEnabled
    String availableStock
    String image
    String lowStockLevel
    String minOrderQuantity
    String maxOrderQuantity
    String model
    String height
    String width
    String weight
    String length

    String taxProfile
    String shippingProfile

    String metaTags
    String videos


    public ProductRawData() {}

    public ProductRawData(Product product, List<String> ignores = []) {
        ignores.addAll(["class", "null", "extraProperties"])
        this.properties.each {String key, Object ob ->
            if(!ignores.contains(key)) {
                def value
                switch (key) {
                    case "rowNum":
                        break
                    case "image":
                        value = product.images ? product.images.name.join(",") : "";
                        break;
                    case "videos":
                        value = product.videos ? product.videos.name.join(",") : "";
                        break;
                    case "metaTags":
                        value = product.metaTags.collect { it.name + "," + it.value  }.join(",");
                        break;
                    case "parent":
                        value = product.parent?.sku ?: "";
                        break;
                    case "isInventoryEnabled":
                        value = product.isInventoryEnabled ? "YES" : "NO";
                        break;
                    case "isAvailable":
                        value = product.isAvailable ? "A" : "NA"
                        break
                    case "basePrice":
                        value = product.basePrice ?: 0.0
                        break;
                    case "salePrice":
                        value = product.isOnSale ? product.salePrice : 0.0
                        break;
                    case "shippingProfile":
                    case "taxProfile":
                        value = product[key]?.name
                        break
                    default:
                        value = product[key] ?: "";
                }
                this[key] = value?.toString()
            }
        }
    }

    void setName(String name) {
        this.name = name?.trim()
    }

    void setSku(String sku) {
        this.sku = sku?.trim()
    }

    void setParent(String parent) {
        this.parent = parent?.trim()
    }

    void setImage(String image) {
        this.image = image?.trim()
    }

    void setTaxProfile(String taxProfile) {
        this.taxProfile = taxProfile?.trim()
    }

    void setShippingProfile(String shippingProfile) {
        this.shippingProfile = shippingProfile?.trim()
    }

    def propertyMissing(String name) {
        return extraProperties[name] ?: ""
    }

    def propertyMissing(String name, String value) {
        extraProperties[name] = value
    }
}
