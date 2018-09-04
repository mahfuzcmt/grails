package com.webcommander.plugin.xero.models;

import com.webcommander.plugin.xero.models.invoice.XeroPayment;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by amir on 12/15/2015.
 */
@XmlRootElement(name = "Payments")
public class XeroPayments {
    private List<XeroPayment> paymentList;

    @XmlElement(name = "Payment")
    public List<XeroPayment> getPaymentList() {
        return paymentList;
    }

    public void setPaymentList(List<XeroPayment> paymentList) {
        this.paymentList = paymentList;
    }
}
