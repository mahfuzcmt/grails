package com.webcommander.plugin.xero.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by amir on 12/15/2015.
 */
@XmlRootElement(name = "ContactPersons")
public class ContactPersons {
    private List<ContactPerson> contactPersonList;

    @XmlElement(name = "ContactPerson")
    public List<ContactPerson> getContactPersonList() {
        return contactPersonList;
    }

    public void setContactPersonList(List<ContactPerson> contactPersonList) {
        this.contactPersonList = contactPersonList;
    }

    public void addContactPerson(ContactPerson contactPerson) {
        if (contactPersonList == null) {
            contactPersonList = new ArrayList<ContactPerson>();
        }
        contactPersonList.add(contactPerson);
    }
}
