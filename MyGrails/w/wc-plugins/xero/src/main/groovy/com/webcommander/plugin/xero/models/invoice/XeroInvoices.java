package com.webcommander.plugin.xero.models.invoice;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by amir on 12/15/2015.
 */
@XmlRootElement(name = "Invoices")
public class XeroInvoices {
    private List<XeroInvoice> invoiceList;

    @XmlElement(name = "Invoice")
    public List<XeroInvoice> getInvoiceList() {
        return invoiceList;
    }

    public void setInvoiceList(List<XeroInvoice> invoiceList) {
        this.invoiceList = invoiceList;
    }

    public void addToInvoiceList(XeroInvoice xeroInvoice) {
        if (invoiceList == null) {
            invoiceList = new ArrayList<XeroInvoice>();
        }
        invoiceList.add(xeroInvoice);
    }
}
