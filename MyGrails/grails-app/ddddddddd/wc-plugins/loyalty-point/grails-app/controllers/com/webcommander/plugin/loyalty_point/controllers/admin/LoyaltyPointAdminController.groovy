package com.webcommander.plugin.loyalty_point.controllers.admin

import com.webcommander.admin.ConfigService
import com.webcommander.authentication.annotations.License
import com.webcommander.authentication.annotations.RequiresAdmin
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.loyalty_point.LoyaltyPointService
import com.webcommander.plugin.loyalty_point.PointHistory
import com.webcommander.plugin.loyalty_point.SpecialPointRule
import com.webcommander.util.AppUtil
import grails.converters.JSON
import org.supercsv.io.CsvListWriter
import org.supercsv.prefs.CsvPreference

@RequiresAdmin
class LoyaltyPointAdminController {
    LoyaltyPointService loyaltyPointService;
    CommonService commonService
    ConfigService configService

    @License(required = "allow_loyalty_program_feature")
    def loadAppView() {
        def configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT);
        render(view: "/plugins/loyalty_point/admin/appView", model: [configs: configs]);
    }

/*    @License(required = "allow_loyalty_program_feature")
    def loadReferralAppView() {
        def configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.REFERRAL);
        render(view: "/plugins/loyalty_point/admin/referralAppView", model: [configs: configs]);
    }*/

    def saveConfigs() {
        def configs = []
        params.list("type").each { type ->
            params."${type}".each {
                configs.add([type: type, configKey: it.key, value: it.value]);
            }
        }
        if (configService.update(configs)) {
            render([status: "success", message: g.message(code: "setting.updated.successfully")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "setting.not.updated")] as JSON)
        }
    }

    def addRule() {
        render(view: "/plugins/loyalty_point/admin/createRule")
    }

    def editRule() {
        Long ruleId = params.rule_id as Long
        render(view: "/plugins/loyalty_point/admin/createRule", model: [params: loyaltyPointService.getSpecialPointRule([ruleId]).first()])
    }

    def saveRule() {
        SpecialPointRule specialPointRule = loyaltyPointService.saveSpecialPointRule(params)
        if(specialPointRule) {
            render([status: "success", message: g.message(code: "special.rule.save"), rule: specialPointRule] as JSON)
        } else {
            render([status: "error", message: g.message(code: "special.rule.could.not.save")] as JSON)
        }
    }

    def removeRule() {
        def ruleId = params.ruleId as Long
        if(loyaltyPointService.removeSpecialPointRule(ruleId)){
            render([status: "success", message: g.message(code: "special.rule.removed")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "special.rule.could.not.removed")] as JSON)
        }
    }

    def showSelectedCustomersAndGroups() {
        Long ruleId = params.rule_id as Long
        SpecialPointRule specialPointRule = loyaltyPointService.getSpecialPointRule([ruleId]).first()
        render(view: "/plugins/loyalty_point/admin/showCustomersAndGroups", model: [customers: specialPointRule.customers, customerGroups: specialPointRule.customerGroups])
    }

    def loadReportView() {
        params.max = params.max ?: "10"
        Integer count = loyaltyPointService.getPointHistoryCount(params)
        List<PointHistory> pointHistories = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.max = max
            params.offset = offset
            loyaltyPointService.getPointHistory(params)
        }
        render(view: "/plugins/loyalty_point/report/reportView", model: [pointHistories: pointHistories, count: count])
    }

    def advanceFilter() {
        render(view: "/plugins/loyalty_point/report/filter", model: [:]);
    }

    def exportLog() {
        List<PointHistory> pointHistories = PointHistory.list()
        CsvListWriter listWriter = null
        try {
            response.setHeader("Content-Type", "text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=Loyalty_Point_Log.csv")
            OutputStreamWriter writer = new OutputStreamWriter(response.outputStream)
            listWriter = new CsvListWriter(writer, CsvPreference.EXCEL_PREFERENCE)
            String[] fields = ["Customer Name", "Points", "Sources", "Date"]
            listWriter.writeHeader(fields)
            pointHistories.each {
                List<String> fieldValueList = []
                fieldValueList.add(it.customer.fullName())
                fieldValueList.add(it.pointCredited as String)
                fieldValueList.add(g.message(code: it.type))
                fieldValueList.add(it.created.toString())
                listWriter.write(fieldValueList)
            }
        } finally {
            if( listWriter != null ) {
                listWriter.close()
            }
        }
    }
}
