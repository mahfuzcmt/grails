package com.webcommander.plugin.xero.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by amir on 12/14/2015.
 */
@XmlRootElement(name = "Contacts")
public class XeroContacts {
    private List<XeroContact> contactList;

    @XmlElement(name = "Contact")
    public List<XeroContact> getContactList() {
        return contactList;
    }

    public void setContactList(List<XeroContact> contactList) {
        this.contactList = contactList;
    }
}