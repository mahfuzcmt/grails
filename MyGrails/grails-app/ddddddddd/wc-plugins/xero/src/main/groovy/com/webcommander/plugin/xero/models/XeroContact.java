package com.webcommander.plugin.xero.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Created by amir on 12/14/2015.
 */

@XmlRootElement(name = "Contact")
public class XeroContact {
    private String name;

    private String contactId;

    private String contactNumber ;

    private String firstName;

    private String lastName;

    private String emailAddress;

    private String skypeUserName;

    private String website;

    private ContactPersons contactPersons;

    private Addresses addresses;

    private Phones phones;

    private Boolean isCustomer;

    private String contactStatus;

    private Date updatedDateUTC;

    @XmlElement(name = "Name")
    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "ContactNumber")
    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    @XmlElement(name = "ContactID")
    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }
    @XmlElement(name = "FirstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @XmlElement(name = "LastName")
    String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @XmlElement(name = "EmailAddress")
    String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @XmlElement(name = "ContactPersons")
    public ContactPersons getContactPersons() {
        return contactPersons;
    }

    public void setContactPersons(ContactPersons contactPersons) {
        this.contactPersons = contactPersons;
    }

    @XmlElement(name = "UpdatedDateUTC")
    public Date getUpdatedDateUTC() {
        return updatedDateUTC;
    }

    public void setUpdatedDateUTC(Date updatedDateUTC) {
        this.updatedDateUTC = updatedDateUTC;

    }
    @XmlElement(name = "Addresses")
    public Addresses getAddresses() {
        return addresses;
    }

    public void setAddresses(Addresses addresses) {
        this.addresses = addresses;
    }

    @XmlElement(name = "IsCustomer")
    public Boolean getIsCustomer() {
        return isCustomer;
    }

    public void setIsCustomer(Boolean isCustomer) {
        this.isCustomer = isCustomer;
    }

    @XmlElement(name = "Phones")
    public Phones getPhones() {
        return phones;
    }

    public void setPhones(Phones phones) {
        this.phones = phones;
    }

    @XmlElement(name = "SkypeUserName")
    public String getSkypeUserName() {
        return skypeUserName;
    }

    public void setSkypeUserName(String skypeUserName) {
        this.skypeUserName = skypeUserName;
    }

    @XmlElement(name = "Website")
    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    @XmlElement(name = "ContactStatus")
    public String getContactStatus() {
        return contactStatus;
    }

    public void setContactStatus(String contactStatus) {
        this.contactStatus = contactStatus;
    }
}
