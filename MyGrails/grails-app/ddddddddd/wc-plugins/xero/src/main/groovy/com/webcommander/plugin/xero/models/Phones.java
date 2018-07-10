package com.webcommander.plugin.xero.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by amir on 12/15/2015.
 */
@XmlRootElement(name = "Phones")
public class Phones {
    private List<Phone> phoneList;

    @XmlElement(name = "Phone")
    public List<Phone> getPhoneList() {
        return phoneList;
    }

    public void setPhoneList(List<Phone> phoneList) {
        this.phoneList = phoneList;
    }
}
