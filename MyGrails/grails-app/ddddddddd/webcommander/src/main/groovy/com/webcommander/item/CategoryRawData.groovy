package com.webcommander.item

class CategoryRawData {

    Integer rowNum
    private name
    private String sku
    private String parent
    private String image
    String summary
    String description
    String idx
    private String taxProfile
    private String shippingProfile
    String metaTags

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

}
