package com.webcommander.task

import com.webcommander.admin.ConfigService
import com.webcommander.admin.Operator
import com.webcommander.admin.Role
import com.webcommander.admin.UserService
import com.webcommander.constants.DomainConstants
import com.webcommander.design.TemplateService
import com.webcommander.events.AppEventManager
import com.webcommander.installation.template.TemplateInstallationService
import com.webcommander.tenant.TenantContext

class OfflineQueryQueueService {
    TemplateInstallationService templateInstallationService
    UserService userService
    TemplateService templateService
    ConfigService configService

    def createUser(String... arg) {
        String email = arg[0]
        String password = arg[1]
        println("Current Users")
        userService.getUsers([max: -1, offset: 0]).each {
            println("${it.fullName} : ${it.email}" )
        }
        println("\n")

        println("Email: ${email}, Name: ${arg[2]}")
        Operator operator = Operator.findOrCreateByEmail(email);
        operator.roles = [Role.findByName("Admin")]
        operator.isActive = true
        operator.isAPIAccessOnly = false
        operator.fullName = arg[2] ?: (operator.fullName ?: "WebCommander Admin")
        operator.password = password.encodeAsMD5()
        operator.uuid = arg[3]
        operator.save(flush: true)
        println("\n\n\n")
    }

    def deleteOperators(String... args) {
        List<Operator> operators = userService.getUsers([max: -1, offset: 0]);
        operators.each {
            println("Delete Email: ${it.email}, Name: ${it.fullName}")

            Operator user = Operator.proxy(it.id)
            AppEventManager.fire("before-operator-delete", [user.id])
            user.roles = []
            user.save()
            user.delete();

            println("End Delete Email: ${it.email}, Name: ${it.fullName}")
            println("\n\n\n")
        }
    }

    def installTemplate(String... arg) {
        if (arg[0]) {
            Map templateDetails = templateService.getTemplate(arg[0])
            templateInstallationService.install([:], templateDetails)
        }
    }

    def changeHostname(String... arg) {
        if(arg[0]) {
            String url = arg[0]
            String sUrl = url
            if(url.startsWith("http:")) {
                sUrl = "https" + url.substring(4)
            } else {
                sUrl = "https://" + url
                url = "http://" + url
            }
            configService.update([[
              type: DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION,
              configKey: "baseurl",
              value: url
            ], [
              type: DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION,
              configKey: "secured_baseurl",
              value: sUrl
            ]])
        }
    }

    def performQueries() {
        println("Running Offline Query for ${TenantContext.currentTenant}")
        List<OfflineQueryQueue> queries = OfflineQueryQueue.createCriteria().list {
            cache(false)
        }
        println("Number of Queries: ${queries.size()}")
        Integer numberOfQuery = queries.size()
        Integer successCount = 0
        queries.each { query ->
            println("Running Offline Query: ${query.methodName}")
            try {
                if(this.respondsTo(query.methodName)) {
                    println("Method Name: ${query.methodName}")
                    this."${query.methodName}"(query.arg0, query.arg1, query.arg2, query.arg3)
                    successCount++
                    println("Success Method Name: ${query.methodName}")
                }
            } catch (Throwable ex) {
                println("Exception : ${query.methodName} ${ex.getMessage()}")
                log.error(ex.message, ex)
            }
        }
        queries*.delete();
        return [numberOfQuery: numberOfQuery, successCount: successCount]
    }
}
