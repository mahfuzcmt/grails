package com.webcommander.webmarketing

import com.webcommander.admin.Customer
import com.webcommander.admin.CustomerGroup
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommanderMailService
import com.webcommander.common.Email
import com.webcommander.config.EmailTemplate
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.tenant.Thread
import com.webcommander.util.AppUtil
import com.webcommander.util.Base64Coder
import com.webcommander.util.TrashUtil
import com.webcommander.webmarketting.Newsletter
import com.webcommander.webmarketting.NewsletterReceiver
import com.webcommander.webmarketting.NewsletterSubscriber
import com.webcommander.webmarketting.NewsletterUnsubscribeHistory
import grails.gorm.transactions.Transactional
import groovy.time.TimeDuration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@Initializable
class NewsletterService {
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    com.webcommander.ApplicationTagLib app
    CommanderMailService commanderMailService

    static void initialize() {
        AppEventManager.on("before-customer-delete", {id ->
            NewsletterReceiver.createCriteria().list {
                eq("receiverType", "customer")
                eq("receiverId", id)
            }*.delete()
        })
        AppEventManager.on("before-customer-group-delete", {id ->
            NewsletterReceiver.createCriteria().list {
                eq("receiverType", "customerGroup")
                eq("receiverId", id)
            }*.delete()
        })
    }

    private Closure getCriteriaClosure(Map params) {
        def session = AppUtil.session
        return {
            if (params.searchText) {
                ilike("title", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if(params.fullName) {
                or {
                    def fullName = params.fullName.trim().encodeAsLikeText()
                    ilike("title", "%${fullName}%")
                    ilike("firstName", "%${fullName}%")
                    ilike("lastName", "%${fullName}%")
                }
            }
            if (params.title) {
                ilike("title", "%${params.title.trim().encodeAsLikeText()}%")
            }
            if (params.subject) {
                ilike("subject", "%${params.subject.trim().encodeAsLikeText()}%")
            }
            if (params.status) {
                eq("isSent", params.status == "true")
            }
            if (params.createdFrom) {
                Date date = params.createdFrom.dayStart.gmt(session.timezone)
                ge("created", date)
            }
            if (params.createdTo) {
                Date date = params.createdTo.dayEnd.gmt(session.timezone)
                le("created", date)
            }
            if (params.scheduledFrom) {
                Date date = params.scheduledFrom.dayStart.gmt(session.timezone)
                ge("scheduleTime", date)
            }
            if (params.scheduledTo) {
                Date date = params.scheduledTo.dayEnd.gmt(session.timezone)
                le("scheduleTime", date)
            }
        }
    }

    private Closure getCriteriaClosureForSubscriber(Map params) {
        def session = AppUtil.session
        return {
            if(params.fullName) {
                or {
                    def fullName = params.fullName.trim().encodeAsLikeText()
                    ilike("title", "%${fullName}%")
                    ilike("firstName", "%${fullName}%")
                    ilike("lastName", "%${fullName}%")
                    ilike("email", "%${fullName}%")
                }
            }
            if (params.createdFrom) {
                Date date = params.createdFrom.dayStart.gmt(session.timezone)
                ge("created", date)
            }
            if (params.createdTo) {
                Date date = params.createdTo.dayEnd.gmt(session.timezone)
                le("created", date)
            }
            if(params.containsKey("isSubscribed")) {
                eq("isSubscribed", params.isSubscribed)
            }
            if(params.unsubscriber) {
                createAlias("subscriber", "s")
                if(params.fullName) {
                    or {
                        def fullName = params.fullName.trim().encodeAsLikeText()
                        ilike("s.title", "%${fullName}%")
                        ilike("s.firstName", "%${fullName}%")
                        ilike("s.lastName", "%${fullName}%")
                        ilike("s.email", "%${fullName}%")
                    }
                }
                if(params.unsubscriberEmail) {
                    ilike("s.email", "%${params.unsubscriberEmail.trim().encodeAsLikeText()}%")
                }
                if (params.subscribedFrom) {
                    Date date = params.subscribedFrom.dayStart.gmt(session.timezone)
                    ge("subscribed", date)
                }
                if (params.subscribedTo) {
                    Date date = params.subscribedTo.dayEnd.gmt(session.timezone)
                    le("subscribed", date)
                }
                if (params.unsubscribedFrom) {
                    Date date = params.unsubscribedFrom.dayStart.gmt(session.timezone)
                    ge("unsubscribed", date)
                }
                if (params.unsubscribedTo) {
                    Date date = params.unsubscribedTo.dayEnd.gmt(session.timezone)
                    le("unsubscribed", date)
                }
                if(params.reason) {
                    ilike("reason", "%${params.reason.trim().encodeAsLikeText()}%")
                }
            } else if(params.subscriber) {
                if(params.subscriberEmail) {
                    ilike("email", "%${params.subscriberEmail.trim().encodeAsLikeText()}%")
                }
            }

        }
    }

    Integer getNewsletterCount(Map params) {
        return Newsletter.createCriteria().count {
            and getCriteriaClosure(params)
        }
    }

    Integer getSubscriberCount(Map params) {
        return NewsletterSubscriber.createCriteria().count {
            and getCriteriaClosureForSubscriber(params)
        }
    }

    Integer getUnsubscriberCount(Map params) {
        return NewsletterUnsubscribeHistory.createCriteria().count {
            and getCriteriaClosure(params)
        }
    }

    List<Newsletter> getNewsletters(Map params) {
        def listMap = [max: params.max, offset: params.offset]
        return Newsletter.createCriteria().list(listMap, getCriteriaClosure(params) << {
            order(params.sort ?: "title", params.dir ?: "asc")
        })
    }

    Newsletter getNewsletter(Long id) {
        return Newsletter.get(id)
    }

    @Transactional
    Newsletter saveNewsletter(Map params) {
        Long id = params.id ? params.id.toLong(0) : null
        Newsletter newsletter = id ? Newsletter.get(id) : new Newsletter()

        newsletter.title = params.title
        newsletter.sender = params.sender
        newsletter.subject = params.subject
        newsletter.body = params.description
        newsletter.isActive = params.active.toBoolean(false)
        newsletter.isSent = false
        if(newsletter.isSent) {
            newsletter.scheduleTime = null
        } else {
            newsletter.scheduleTime = params.scheduleTime?.toDate()?.gmt(AppUtil.session.timezone)
        }

        newsletter.newsletterReceivers*.delete()
        newsletter.newsletterReceivers = []
        newsletter.save()

        if(params.customer) {
            List customerIds = Customer.where {
                id in params.list("customer").collect{it.toLong()}
            }.list()
            customerIds.each {
                NewsletterReceiver nr = new NewsletterReceiver(receiverType: "customer", receiverId: it.id, parent: newsletter, newsletter: newsletter)
                nr.save()
                newsletter.addToNewsletterReceivers(nr)
            }
        }
        if(params.customerGroup) {
            List customerIds = CustomerGroup.where {
                id in params.list("customerGroup").collect{it.toLong()}
            }.list()
            customerIds.each {
                NewsletterReceiver nr = new NewsletterReceiver(receiverType: "customerGroup", receiverId: it.id, parent: newsletter, newsletter: newsletter)
                nr.save()
                newsletter.addToNewsletterReceivers(nr)
            }
        }
        if(params.recipientEmail) {
            List<String> emails = params.list("recipientEmail")
            List<String> names = params.list("recipientName")
            for(int i = 0; i < emails.size(); i++) {
                Email email = Email.findByEmail(emails[i]) ?: new Email(name: names[i], email: emails[i])
                email.save()
                NewsletterReceiver nr = new NewsletterReceiver(receiverType: "email", receiverId: email.id, parent: newsletter, newsletter: newsletter)
                nr.save()
                newsletter.addToNewsletterReceivers(nr)
            }
        }
        if(params.includeAllSubscriber == "true") {
            NewsletterReceiver nr = new NewsletterReceiver(receiverType: "subscriber", parent: newsletter , newsletter: newsletter)
            nr.save()
            newsletter.addToNewsletterReceivers(nr)
        }

        newsletter.merge()
        if(!newsletter.hasErrors()){
            return newsletter
        }
    }

    @Transactional
    boolean deleteNewsletter(Long id) {
        try {
            Newsletter newsletter = Newsletter.get(id)
            newsletter.newsletterReceivers*.delete()
            newsletter.delete()
            return !newsletter.hasErrors()
        } catch (Throwable t) {
            return false
        }
    }

    boolean deleteSelectedNewsletter (List ids) {
        boolean result = true
        ids.each { id->
            if (!deleteNewsletter(id)) {
               result = false
            }
        }
        return result
    }

    @Transactional
    boolean deleteSubscriber(Long id, String at1, String at2) {
        TrashUtil.preProcessFinalDelete("newsletterSubscriber", id, at2 != null, at1 != null)
        AppEventManager.fire("before-newsletterSubscriber-delete", [id])
        NewsletterSubscriber subscriber = NewsletterSubscriber.proxy(id)
        subscriber.delete()
        AppEventManager.fire("newsletterSubscriber-delete", [id])
        return true
    }

    @Transactional
    Integer deleteSelectedSubscriber(List<Long> ids) {
        Integer count = 0
        ids.each {
            try {
                deleteSubscriber(it, "include", "yes")
                count++
            } catch (Throwable e) {
            }
        }
        return count
    }

    @Transactional
    def sendNewsletter (Newsletter newsletter) {
        String activeHtml = ""
        Map refinedMacros = [
            customer_name: 'Customer Name',
            unsubscribe_link: 'Unsubscribe Link'
        ]
        Map commonMacros = commanderMailService.getCommonMacros()
        commonMacros.each {
            refinedMacros[it.key] = it.value
        }
        newsletter.newsletterReceivers = NewsletterReceiver.where{
            parent == newsletter
        }.list()

        Map emailList = [:]
        Map subscriberList = [:]
        String sender = newsletter.sender
        newsletter.newsletterReceivers.each { receiver ->
            if (receiver.receiverType == "customer") {
                Customer customer = Customer.findByIdAndStatus(receiver.receiverId, DomainConstants.CUSTOMER_STATUS.ACTIVE)
                if (customer) {
                    emailList.put(customer.address.email, customer.firstName + " " + customer.lastName)
                }
            } else if (receiver.receiverType == "customerGroup") {
                CustomerGroup customerGroup = CustomerGroup.findByIdAndStatus(receiver.receiverId, 'A')
                if(customerGroup) {
                    customerGroup.customers.each {
                        if(it.status  == DomainConstants.CUSTOMER_STATUS.ACTIVE) {
                            emailList.put(it.address.email, it.firstName + " " + it.lastName)
                        }
                    }
                }
            } else if (receiver.receiverType == "email") {
                Email email = Email.get(receiver.receiverId)
                emailList.put(email.email, email.name)
            } else if (receiver.receiverType == "subscriber") {
                List ns = NewsletterSubscriber.findAllByIsSubscribed(true)
                ns.each {
                    subscriberList.put(it.email, it.id)
                }
            }
        }
        Thread.start {
            AppUtil.initialDummyRequest()
            Newsletter.withNewSession {
                def letter = Newsletter.get(newsletter.id)
                emailList.each { customer ->
                    refinedMacros.each {
                        switch (it.key.toString()) {
                            case "customer_name":
                                it.value = customer.value
                                break
                            case "unsubscribe_link":
                                it.value = ""
                                break
                        }
                    }
                    EmailTemplate emailTemplate = new EmailTemplate()
                    emailTemplate.active = true
                    emailTemplate.subject = letter.subject
                    emailTemplate.contentType = "html"
                    activeHtml = letter.body.replaceAll("%", "%%")
                    emailTemplate.discard()
                    commanderMailService.sendMail(emailTemplate, activeHtml, "", refinedMacros, customer.key, letter.sender)
                }
                subscriberList.each { customer ->
                    refinedMacros.each {
                        switch (it.key.toString()) {
                            case "customer_name":
                                it.value = ""
                                break
                            case "unsubscribe_link":
                                it.value = '<a href="' + app.relativeBaseUrl() + 'newsletter/removeSubscriber?id=' + customer.value + '">Click Here to Unsubscribe</a>'
                                break
                        }
                    }
                    EmailTemplate emailTemplate = new EmailTemplate()
                    emailTemplate.active = true
                    emailTemplate.subject = letter.subject
                    emailTemplate.contentType = "html"
                    activeHtml = letter.body.replaceAll("%", "%%")
                    emailTemplate.discard()
                    commanderMailService.sendMail(emailTemplate, activeHtml, "", refinedMacros, customer.key, letter.sender)
                }
            }
        }
        newsletter.isSent = true
        newsletter.merge()
    }

    def scheduledNewsletters() {
        Date currentTime = new Date().gmt() + new TimeDuration(0, 5, 0,0)
        return Newsletter.createCriteria().list() {
            eq("isSent", false)
            eq("isActive", true)
            le("scheduleTime",  currentTime)
        }
    }

    def getNewsletterSubscriber(Map params) {
        def listMap = [max: params.max, offset: params.offset]
        return NewsletterSubscriber.createCriteria().list(listMap, getCriteriaClosureForSubscriber(params) << {
            order(params.sort ?: "title", params.dir ?: "asc")
        })
    }

    def getNewsletterUnsubscriber(Map params) {
        params.unsubscribe = true
        def listMap = [max: params.max, offset: params.offset]
        return NewsletterUnsubscribeHistory.createCriteria().list(listMap, getCriteriaClosureForSubscriber(params) << {
            order(params.sort ?: "s.firstName", params.dir ?: "asc")
        })
    }

    @Transactional
    public boolean subscribeNewsletter(Map params) {
        NewsletterSubscriber subscriber = NewsletterSubscriber.findByEmail(params.email.trim())
        if(params.name) {
            params.firstName = params.name.trim()
        }
        if(subscriber) {
            subscriber.isSubscribed = true
            if(params.title) {
                subscriber.title = params.title.trim()
            }
            if(params.firstName) {
                subscriber.firstName = params.firstName.trim()
            }
            if(params.lastName) {
                subscriber.lastName = params.lastName.trim()
            }
            subscriber.merge()
        } else {
            subscriber = new NewsletterSubscriber(title: params.title, firstName: params.firstName.trim(), lastName: params.lastName.trim(), email: params.email.trim())
            subscriber.save()
        }
        if(params.immediate) {
            NewsletterUnsubscribeHistory.createCriteria().list([max: 1], {
                eq("subscriber", subscriber)
                order("unsubscribed", "desc")
            }).each {
                it.delete()
            }
        } else {
            String name = (subscriber.title? app.message(code: subscriber.title) + " " : "") + (subscriber.firstName ? " " + subscriber.firstName.encodeAsBMHTML() : "") +  (subscriber.lastName ? " " + subscriber.lastName.encodeAsBMHTML() : "")
                if(EmailTemplate.findByIdentifier("newsletter-subscription-notification").active) {
                    sendSubscriptionNotificationMail(subscriber.email, name)
                }
        }

        return !subscriber.hasErrors()
    }

    @Transactional
    public boolean unsubscribeNewsletter(Map params) {
        NewsletterSubscriber subscriber = NewsletterSubscriber.findByEmail(params.email)
        subscriber.isSubscribed = false
        subscriber.merge()
        NewsletterUnsubscribeHistory history = new NewsletterUnsubscribeHistory(subscriber: subscriber, subscribed: subscriber.created, reason: params.message)
        if(!subscriber.hasErrors()) {
            history.save()
        }
        return !history.hasErrors()
    }

    private boolean sendSubscriptionNotificationMail(String customerEmail, String name = null) {
        String sid = Base64Coder.encode(customerEmail + "::::" + name ?: "")
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("newsletter-subscription-notification")
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "customer_name" :
                    refinedMacros[it.key] = name ?: app.message(code: "customer")
                    break
                case "unsubscribe_link" :
                    refinedMacros[it.key] = app.baseUrl() + "unsubscription?sid=" + sid
                    break
            }
        }
        commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, customerEmail)
    }

    def changeAdministrativeStatus(List<Long> ids, Boolean status) {
        Integer count = 0
        ids.each {
            Newsletter newsletter= Newsletter.get(it)
            newsletter.isActive = status
            count++
        }
        return count
    }
}
