package com.webcommander.controllers.admin

import com.webcommander.admin.AdministrationService
import com.webcommander.admin.ConfigService
import com.webcommander.admin.DashboardService
import com.webcommander.admin.Dashlet
import com.webcommander.admin.Operator
import com.webcommander.admin.RoleService
import com.webcommander.constants.DomainConstants
import com.webcommander.license.OperatorPendingNotification
import com.webcommander.manager.LicenseManager
import com.webcommander.models.DashletFlow
import com.webcommander.util.AppUtil
import grails.converters.JSON

class AdminBaseController {
    ConfigService configService
    RoleService roleService
    AdministrationService administrationService

    def dashboard() {
        if(params.redirectUrl) {
            redirect(url: params.redirectUrl)
            return
        }
        if(LicenseManager.isProvisionActive() && LicenseManager.IS_ADMIN_DISABLED) {
            render view: "/admin/disabled_access", model: [topType: 'license', message: license.message(notification: new OperatorPendingNotification(LicenseManager.DISABLED_MESSAGE))]
            return
        }
        List<Dashlet> dashlets = Dashlet.list()
        Dashlet reportDashlet = dashlets.find {
            it == "quickReport"
        }
        dashlets = [
            dashlets.find {
                it == "latestStat"
            },
            dashlets.find {
                it == "webContentAndDesign"
            },
            dashlets.find {
                it == "webCommerce"
            },
            dashlets.find {
                it == "administrationAndMarketing"
            },
            dashlets.find {
                it == "favouriteReportChartOne"
            },
            dashlets.find {
                it == "favouriteReportChartTwo"
            }
        ]
        def notifications = LicenseManager.readNotifications()
        def initialConfigPassed = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GET_STARTED_WIZARD, "passed")
        Operator admin = Operator.load(session.admin)
        if(admin == null) {
            redirect(controller: "userAuthentication", action: "logout")
        } else {
            render(view: "/admin/controlPanel", model: [admin: admin, reportDashlet: reportDashlet, dashlets: dashlets, get_started_wizard_passed: initialConfigPassed, permissions: roleService.getPermissions(admin) as JSON, notifications: notifications])
        }
    }

    def loggedUserName() {
        render Operator.get(session.admin).fullName.encodeAsBMHTML()
    }

    def finishWizard() {
        configService.update([[type: DomainConstants.SITE_CONFIG_TYPES.GET_STARTED_WIZARD, configKey: "passed", value: "true"]])
        render([status: "success"] as JSON)
    }

    def updatePermissions() {
        def permissions = roleService.getPermissions(session.admin)
        if(permissions) {
            render([status: "success", permissions: permissions] as JSON)
        } else {
            render([status: "error"] as JSON)
        }
    }

    def loadMaxPricePrecision() {
        render([status: "success", maxPrecision: AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "max_precision")] as JSON)
    }
}
