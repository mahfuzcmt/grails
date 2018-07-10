package com.webcommander.plugin.xero.models;

import javax.xml.bind.annotation.XmlElement;

public class SalesDetails {
    private double unitPrice;
    private String accountCode;
    private String taxType;

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

    @XmlElement(name = "TaxType")
    public String getTaxType() {
        return taxType;
    }

    public void setTaxType(String taxType) {
        this.taxType = taxType;
    }
}
