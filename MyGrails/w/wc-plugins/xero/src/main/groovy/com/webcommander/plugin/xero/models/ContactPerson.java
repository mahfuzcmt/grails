package com.webcommander.plugin.xero.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by amir on 12/24/2015.
 */
@XmlRootElement(name = "ContactPerson")
public class ContactPerson {
    private String firstName;
    private String lastName;
    private String emailAddress;
    private Boolean includeInEmails;

    @XmlElement(name = "FirstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    @XmlElement(name = "LastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @XmlElement(name = "EmailAddress")
    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @XmlElement(name = "IncludeInEmails")
    public Boolean getIncludeInEmails() {
        return includeInEmails;
    }

    public void setIncludeInEmails(Boolean includeInEmails) {
        this.includeInEmails = includeInEmails;
    }
}
