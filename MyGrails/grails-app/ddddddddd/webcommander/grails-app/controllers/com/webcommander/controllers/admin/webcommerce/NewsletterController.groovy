package com.webcommander.controllers.admin.webcommerce

import com.webcommander.admin.Customer
import com.webcommander.admin.CustomerGroup
import com.webcommander.authentication.annotations.License
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.common.CommonService
import com.webcommander.common.Email
import com.webcommander.constants.DomainConstants
import com.webcommander.license.blocker.NewsletterLicense
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import com.webcommander.webmarketting.Newsletter
import com.webcommander.webmarketing.NewsletterService
import com.webcommander.webmarketting.NewsletterSubscriber
import com.webcommander.webmarketting.NewsletterUnsubscribeHistory
import grails.converters.JSON

class NewsletterController {
    NewsletterService newsletterService
    CommonService commonService

    @Restriction(permission = "newsletter.view.list")
    def loadAppView() {
        params.max = params.max ?: "10";
        Integer count = newsletterService.getNewsletterCount(params);
        List<Newsletter> newsletters = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.max = max;
            params.offset = offset;
            newsletterService.getNewsletters(params);
        }
        render(view: "/admin/newsletter/appView", model: [newsletters: newsletters, count: count]);
    }

    def advanceFilter() {
        render(view: "/admin/newsletter/filter", model: [d: 0])
    }

    @License(required = "newsletter_limit", checker = NewsletterLicense)
    def edit() {
        Newsletter newsletter = params.id ? newsletterService.getNewsletter(params.long("id")) : new Newsletter();
        String sender =  AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.EMAIL, "sender_email")
        def nr_email = []
        newsletter.newsletterReceivers.each {nr ->
            if(nr.receiverType == "email") {
                Email email = Email.get(nr.receiverId)
                nr_email.add([email: email.email, name: email.name])
            }
        }
        if(newsletter.isSent) {
            newsletter.scheduleTime = null
        }
        String customerReceiverId = ""
        String customerGroupReceiverId = ""
        String emailReceiver = ""
        StringBuilder receiverValue = new StringBuilder();
        newsletter.newsletterReceivers.each { receiver ->
            switch(receiver.receiverType) {
                case "subscriber":
                    receiverValue.append("SUBSCRIBER ")
                    break
                case "customer":
                    customerReceiverId += receiver.receiverId + ","
                    break
                case "customerGroup":
                    customerGroupReceiverId += receiver.receiverId + ","
                    break
                case "email":
                    Email recipient = Email.get(receiver.receiverId)
                    emailReceiver += (recipient.name ?: "") + "<" + recipient.email + ">,"
                    break
            }
        }
        if(customerReceiverId) {
            receiverValue.append("CUSTOMER[" + customerReceiverId.substring(0, customerReceiverId.length() - 1) + "] ")
        }
        if(customerGroupReceiverId) {
            receiverValue.append("GROUP[" + customerGroupReceiverId.substring(0, customerGroupReceiverId.length() - 1) + "] ")
        }
        if(emailReceiver) {
            receiverValue.append("EMAIL[" + emailReceiver.substring(0, emailReceiver.length() - 1) + "]")
        }
        render(view: "/admin/newsletter/infoEdit", model: [newsletter: newsletter, receiverValue: receiverValue, sender: sender]);
    }

    private def send(Newsletter newsletter) {
        try {
            if(!newsletter.isActive) {
                render([status: "alert", message: g.message(code: "inactive.newsletters.cant.sent")] as JSON)
                return;
            }
            newsletterService.sendNewsletter(newsletter);
        } catch (Exception e) {
            render([status: "alert", message: g.message(code: "newsletter.send.failure")] as JSON)
            return;
        }
        render([status: "success", message: g.message(code: "newsletter.send.successful")] as JSON)
    }

    def sendNewsletter() {
        List<Long> ids = params.list("id")*.toLong();

        ids.each {
            Newsletter newsletter = Newsletter.get(it)
            send (newsletter)
        }
    }

    @License(required = "newsletter_limit", checker = NewsletterLicense)
    def saveNewsletter() {
        Newsletter newsletter = newsletterService.saveNewsletter(params);
        if (!newsletter.hasErrors()) {
            if (params.sendMail && newsletter.isActive) {
                render([status: "success", alert: true, message: g.message(code: "newsletter.save.sent.success")] as JSON)
            } else if(params.sendMail) {
                render([status: "alert", message: g.message(code: "inactive.newsletters.cant.sent")] as JSON)
            } else {
                render([status: "success", message: g.message(code: "newsletter.save.success")] as JSON)
            }
        } else {
            render([status: "error", message: g.message(code: "newsletter.save.error")] as JSON)
            return;
        }
        if(params.sendMail  && newsletter.isActive) {
            send(newsletter);
        }
    }

    def view() {
        List customer = [],
             group = [],
             email = [];
        Boolean subscriber = false
        Long id = params.long("id")
        Newsletter newsletter = Newsletter.get(id)
        newsletter.newsletterReceivers.each { nr ->
            if(nr.receiverType == 'customer') {
                Customer cus = Customer.get(nr.receiverId)
                String name = cus.firstName + (cus.lastName ? " " + cus.lastName : "")
                customer.add(name)
            } else if (nr.receiverType == 'customerGroup') {
                group.add(CustomerGroup.get(nr.receiverId).name)
            } else if (nr.receiverType == 'email') {
                email.add(Email.get(nr.receiverId).email)
            } else {
                subscriber = true
            }
        }
        render (view: "/admin/newsletter/infoView",
            model: [
                newsletter: newsletter,
                customers: customer.size() ? customer : null,
                groups: group.size() ? group : null,
                email: email.size() ? email : null,
                subscriber: subscriber
            ])
    }

    def delete() {
        if(newsletterService.deleteNewsletter(params.long("id"))) {
            render([status: "success", message: g.message(code: "newsletter.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "newsletter.delete.failure")] as JSON)
        }
    }

    def deleteSelected () {
        List<Long> ids = []
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        if(newsletterService.deleteSelectedNewsletter(ids)) {
            render([status: "success", message: g.message(code: "newsletter.delete.success")] as JSON)
        }else {
            render([status: "success", message: g.message(code: "newsletter.delete.failure")] as JSON)
        }
    }

    def loadSubscriber () {
        params.subscriber = true
        params.max = params.max ?: "10"
        if(params.searchText) {
            params.fullName = params.searchText
            params.remove("searchText")
        }
        params.isSubscribed = true
        Integer count = newsletterService.getSubscriberCount(params)
        List<NewsletterSubscriber> subscribers = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.max = max
            params.offset = offset
            newsletterService.getNewsletterSubscriber(params)
        }

        render(view: "/admin/newsletter/viewSubscribers", model: [subscribers: subscribers, count: count])
    }

    def loadUnsubscriber () {
        params.unsubscriber = true
        params.max = params.max ?: "10"
        if(params.searchText) {
            params.fullName = params.searchText
            params.remove("searchText")
        }
        Integer count = newsletterService.getUnsubscriberCount(params)
        List<NewsletterUnsubscribeHistory> unsubscribers = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.max = max
            params.offset = offset
            newsletterService.getNewsletterUnsubscriber(params)
        }
        render(view: "/admin/newsletter/viewUnsubscribers", model: [unsubscribers: unsubscribers, count: count])
    }

    def deleteSubscriber () {
        Long id = params.long("id");
        try {
            if (newsletterService.deleteSubscriber(id, params.at1_reply, params.at2_reply)) {
                render([status: "success", message: g.message(code: "subscriber.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "subscriber.could.not.delete")] as JSON)
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def deleteSelectedSubscriber() {
        List<Long> ids = params.list("ids").collect{ it.toLong()}
        Integer total = newsletterService.deleteSelectedSubscriber(ids)
        if(total == ids.size()){
            render([status: "success", message: g.message(code: "subscriber.delete.success")] as JSON)
        } else if(total == 0) {
            render([status: "error", message: g.message(code: "subscriber.could.not.delete")] as JSON)
        } else {
            render([status: "warning", message: g.message(code: "selected.not.deleted", args: [ids.size() - total, ids.size(), g.message(code: "subscriber")])] as JSON)

        }
    }

    def receiverSelector() {
        boolean includeAllSubscriber = false
        List<Customer> customers = []
        List<CustomerGroup> customerGroups = []
        List<Email> emailRecipients = []
        Long newsletterId = params.newsletterId.toLong(0)
        if(newsletterId) {
            Newsletter newsletter = Newsletter.get(newsletterId)
            newsletter.newsletterReceivers.each { receiver ->
                switch(receiver.receiverType) {
                    case "subscriber":
                        includeAllSubscriber = true
                        break
                    case "customer":
                        customers.add(Customer.get(receiver.receiverId))
                        break
                    case "customerGroup":
                        customerGroups.add(CustomerGroup.get(receiver.receiverId))
                        break
                    case "email":
                        emailRecipients.add(Email.get(receiver.receiverId))
                        break
                }
            }
        } else {
            if(params.includeAllSubscriber == "true") {
                includeAllSubscriber = true
            }
            List<Long> customerIds = params.list("customer").collect { it.toLong(0) }
            List<Long> customerGroupIds = params.list("customerGroup").collect { it.toLong(0) }
            List<String> recipientEmails = params.list("recipientEmail")
            List<String> recipientNames = params.list("recipientName")
            customers = customerIds ? Customer.createCriteria().list {
                inList("id", customerIds)
            } : []
            customerGroups = customerGroupIds ? CustomerGroup.createCriteria().list {
                inList("id", customerGroupIds)
            } : []
            recipientEmails.eachWithIndex { email, idx ->
                emailRecipients.add(new Email(email: email, name: recipientNames[idx]))
            }
        }
        render(view: "/admin/common/recipientSelector", model: [customers: customers, customerGroups: customerGroups, emailRecipients: emailRecipients,
                                                                includeAllSubscriber: includeAllSubscriber])
    }

    def isUnique() {
        Long id = 0;
        if (params.id) {
            id = params.long("id");
        }
        if (commonService.isUnique(Newsletter, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }

    def advanceSubscriberFilter() {
        render(view: "/admin/newsletter/subscriberFilter", model: [: ])
    }

    def advanceUnsubscriberFilter() {
        render(view: "/admin/newsletter/unsubscriberFilter", model: [: ])
    }

    def viewReason() {
        Long id = params.long("id");
        NewsletterUnsubscribeHistory history = NewsletterUnsubscribeHistory.get(id);
        render(view: "/admin/newsletter/viewReason", model: [history: history]);
    }

    def loadNewsletterStatusOption() {
        render view: "/admin/newsletter/newsletterStatusOption"
    }

    def changeAdministrativeStatus() {
        List<Long> ids = params.list("id")*.toLong();
        Boolean status = params.active == "true";
        if(newsletterService.changeAdministrativeStatus(ids, status)) {
            render([status: "success", message: g.message(code: "newsletter.update.success")] as JSON)
        }else {
            render([status: "success", message: g.message(code: "newsletter.update.failure")] as JSON)
        }
    }
}
