package com.webcommander.controllers.admin

import com.webcommander.manager.LicenseManager
import grails.converters.JSON

class LicenseController {

    def refresh() {
        LicenseManager.fetchLicense()
        LicenseManager.validateLicense()
        Map response = [status: "success", message: g.message(code: "license.refreshed.successfully")]
        List notifications = LicenseManager.readNotifications()
        if(notifications.size()) {
            List notificationMsgs = response.notifications = []
            notifications.each { notification ->
                notificationMsgs.add([type: notification.msgType, message: license.message(notification: notification)])
            }
        }
        render response as JSON
    }

    def refreshToDashboard() {
        LicenseManager.fetchLicense()
        LicenseManager.validateLicense()
        redirect controller: "adminBase", action: "dashboard"
    }
}