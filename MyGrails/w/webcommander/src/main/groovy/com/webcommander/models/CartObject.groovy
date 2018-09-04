package com.webcommander.models

import com.webcommander.webcommerce.TaxProfile

trait CartObject implements Serializable {
    Long id;

    String name;
    String type
    String image
    String altText
    Double effectivePrice
    Double costPrice

    Boolean hasLink = true
    Boolean hasImage = true

    public abstract void validate(Integer quantity);
    public abstract Integer available(Integer quantity);
    public abstract void refresh();
    public abstract String getLink();
    public abstract String getImageLink(String imageSize)
    public TaxProfile resolveTaxProfile() { null };
    public void modifyApiResponse(Map item) {}
    public Boolean iEquals(Object ob) {
        return true
    };
}
