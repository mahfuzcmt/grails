package com.webcommander.plugin.xero.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by amir on 12/15/2015.
 */
@XmlRootElement(name = "Addresses")
public class Addresses {
    private List<XeroAddress> addressList;

    @XmlElement(name = "Address")
    public List<XeroAddress> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<XeroAddress> addressList) {
        this.addressList = addressList;
    }
}
