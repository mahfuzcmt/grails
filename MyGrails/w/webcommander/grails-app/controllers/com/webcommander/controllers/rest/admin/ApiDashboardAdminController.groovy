package com.webcommander.controllers.rest.admin

import com.webcommander.admin.DashboardService
import com.webcommander.util.RestProcessor

class ApiDashboardAdminController extends RestProcessor{
    DashboardService dashboardService

    def quickReport() {
        Map reports = dashboardService.getQuickReports()
        rest reports
    }

}
