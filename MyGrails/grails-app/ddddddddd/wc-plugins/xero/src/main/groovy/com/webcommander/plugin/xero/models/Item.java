package com.webcommander.plugin.xero.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement(name = "Item")
public class Item {
    private String itemID;
    private String code;
    private String name;
    private String description;
    private PurchaseDetails purchaseDetails;
    private SalesDetails salesDetails;
    private Date updatedDateUTC;

    @XmlElement(name = "ItemID")
    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    @XmlElement(name = "Name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "Code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    @XmlElement(name = "Description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement(name = "PurchaseDetails")
    public PurchaseDetails getPurchaseDetails() {
        return purchaseDetails;
    }

    public void setPurchaseDetails(PurchaseDetails purchaseDetails) {
        this.purchaseDetails = purchaseDetails;
    }

    @XmlElement(name = "SalesDetails")
    public SalesDetails getSalesDetails() {
        return salesDetails;
    }

    public void setSalesDetails(SalesDetails salesDetails) {
        this.salesDetails = salesDetails;
    }

    @XmlElement(name = "UpdatedDateUTC")
    public Date getUpdatedDateUTC() {
        return updatedDateUTC;
    }

    public void setUpdatedDateUTC(Date updatedDateUTC) {
        this.updatedDateUTC = updatedDateUTC;
    }
}
