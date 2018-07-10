package com.webcommander.controllers.admin

import com.webcommander.authentication.annotations.Restriction
import com.webcommander.common.CustomerPortalService
import com.webcommander.constants.DomainConstants
import com.webcommander.manager.LicenseManager
import com.webcommander.sso.ProvisionAPIService
import com.webcommander.util.AppUtil
import grails.converters.JSON

class MyAccountController {
    ProvisionAPIService provisionAPIService
    CustomerPortalService customerPortalService

    def loadAppView() {
        render(view: "/admin/myAccount/appView")
    }

    /** Account Details**/
    @Restriction(permission = "my_account.view.list")
    def accountDetails() {
        render(view: "/admin/myAccount/accountDetails/accountDetails")
    }


    @Restriction(permission = "my_account.view.list")
    def accountInfo() {
        def accountDetails = provisionAPIService.accountDetailsByLicense()
        render(view: "/admin/myAccount/accountDetails/accountInfo", model: [accountDetails: accountDetails])
    }

    def websiteDetails() {
        def aliases = provisionAPIService.getAliasByLicense()
        render(view: "/admin/myAccount/accountDetails/websiteDetails", model: [aliases: aliases])
    }

    @Restriction(permission = "my_account.edit.info")
    def accountInfoEdit() {
        def accountDetails = provisionAPIService.accountDetailsByLicense()
        render(view: "/admin/myAccount/accountDetails/accountInfoEdit", model: [accountDetails: accountDetails])
    }

    @Restriction(permission = "my_account.edit.info")
    def saveAccountInfo() {
        def result = provisionAPIService.accountDetailsUpdateByLicense(params)
        if(result.success) {
            render([status: "success", message: g.message(code: "account.details.update.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "account.details.update.failure")] as JSON)
        }
    }


    @Restriction(permission = "my_account.purchase.package")
    def cardInfoPopup() {
        render(view: "/admin/myAccount/cardInfoPopup")
    }

    @Restriction(permission = "my_account.purchase.package")
    def upOrDownByOrderInvoicePayment() {
        def result = provisionAPIService.upOrDownByOrderInvoicePayment(params)
        if(result.success) {
            try {
                LicenseManager.fetchLicense()
            } catch (Throwable ignored) {}
            render([status: "success", message: g.message(code: "package.change.successfully")] as grails.converters.JSON)
        } else {
            render([status: "error", message: result.message] as grails.converters.JSON)
        }
    }

    /** Payment Details **/
    def loadPaymentDetails() {
        render(view: "/admin/myAccount/paymentDetails/paymentDetails")
    }

    /***Purchase History***/
    def loadPurchaseHistory() {
        List history = []
        Integer count = 0
        render(view: "/admin/myAccount/purchaseHistory/purchaseHistory", model: [history: history, count: count])
    }

    /*** Invoice ***/
    def loadInvoice() {
        List invoices = []
        Integer count = 0
        render(view: "/admin/myAccount/invoice/invoice", model: [invoices: invoices, count: count])
    }

    /*** Manage***/
    def loadManage() {
        render(view: "/admin/myAccount/manage/manage", model: [])
    }

    /*** Manage Action***/
    def loadManageAction() {
        render(view: "/admin/myAccount/manage/manageContent", model: [])
    }

    /**Subscription**/
    def loadSubscription() {
        Map licenseConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LICENSE)
        def packages = provisionAPIService.commanderWebsitePackages()
        render(view: "/admin/myAccount/subscription/subscription", model: [packages: packages, licenseConfig: licenseConfig])
    }

    /* Customer Support*/
    def loadCustomerSupport(){
        params.max = params.max ?: 5
        params.offset = params.offset ?: 0
        Map result = customerPortalService.getSupportMessages(params)
        render(view: "/admin/myAccount/customerSupport/customerSupport", model: [messages: result.supportMessages, count: result.count])
    }

    def addSupportMessage() {
        customerPortalService.addSupportMessage(params)
        render([status: "success", message: g.message(code: 'support.message.submit.success')] as JSON)
    }

    def addSupportMessageReply() {
        customerPortalService.addSupportMessageReply(params)
        render([status: "success", message: g.message(code: 'support.message.reply.success')] as JSON)
    }

    /* Custom Project */
    def loadCustomProject() {
        params.max = params.max ?: 5
        params.offset = params.offset ?: 0
        Map result = customerPortalService.getCustomProjects(params)
        render(view: "/admin/myAccount/customProject/customProject", model: [projects: result.supportMessages, count: result.count])
    }

    def saveCustomProject() {
        customerPortalService.saveCustomProject(params)
        render([status: "success", message: g.message(code: 'custom.project.save.sucess')] as JSON)
    }

    def loadCustomProjectEditor() {
        render(view: "/admin/myAccount/customProject/customProjectEditor", model: [projectId: params.projectId])
    }

    def loadCustomProjectProperties() {
        switch (params.property) {
            case "overview":
                def result = customerPortalService.getCustomProjectMilestones(params.id)
                render(view: "/admin/myAccount/customProject/overview", model: [milestones: result.milestones])
                break
            case "projectDetails":
                def result = customerPortalService.getCustomProjectDetails(params.id)
                render(view: "/admin/myAccount/customProject/projectDetails", model: [details: result.details])
                break
            case "projectFiles":
                def result = customerPortalService.getCustomProjectFiles(params.id)
                render(view: "/admin/myAccount/customProject/projectFiles", model: [files: result.projectFiles])
                break
            case "projectMessages":
                params.max = params.max ?: 5
                params.offset = params.offset ?: 0
                def result = customerPortalService.getProjectMessages(params)
                render(view: "/admin/myAccount/customProject/projectMessages", model: [messages: result.projectMessages, count: result.count])
                break
            case "sitemap":
                def result = customerPortalService.getProjectSitemap(params.id)
                String sitemap = result.projectSitemap?.sitemap ?: "[]"
                render(view: "/admin/myAccount/customProject/sitemap", model: [items: JSON.parse(sitemap)])

        }
    }

    def approveProjectMilestone() {
        customerPortalService.approveProjectMilestone(params.id)
        render([status: "success", message: g.message(code: "milestone.approve.success")] as JSON)
    }

    def editProjectDetailsFile() {
        render(view: "/admin/myAccount/customProject/editFile", model: [detailsId: params.detailsId])
    }

    def addProjectDetailsFile() {
        Map result = customerPortalService.addProjectDetailsFile(params)
        if(result.status == "success") {
            result.message = g.message(code: "project.details.file.add.success")
        }
        render(result as JSON)
    }

    def editProjectProjectFiles() {
        render(view: "/admin/myAccount/customProject/editProjectFiles", model: [projectId: params.projectId])
    }

    def addProjectProjectFile() {
        Map result = customerPortalService.addProjectProjectFile(params)
        if(result.status == "success") {
            result.message = g.message(code: "project.files.add.success")
        }
        render(result as JSON)
    }

    def addProjectMessage() {
        customerPortalService.addProjectMessage(params)
        render([status: "success", message: g.message(code: 'project.message.submit.success')] as JSON)
    }

    def addProjectMessageReply() {
        customerPortalService.addProjectMessageReply(params)
        render([status: "success", message: g.message(code: 'project.message.reply.success')] as JSON)
    }

    def saveProjectSitemap() {
        customerPortalService.saveProjectSitemap(params)
        render([status: "success", message: g.message(code: 'project.sitemap.save.success')] as JSON)
    }
}
