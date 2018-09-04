package com.webcommander.plugin.xero.models;

import javax.xml.bind.annotation.XmlElement;

public class PurchaseDetails {
    private double unitPrice;
    private String accountCode;

    @XmlElement(name = "UnitPrice")
    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    @XmlElement(name = "AccountCode")
    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }
}
