package com.webcommander.controllers.admin

import com.webcommander.admin.ApplicationService
import com.webcommander.oauth2.OAuthClient
import grails.converters.JSON

class ApplicationController {
    ApplicationService applicationService

    def list() {
        params.max = params.max ?: 10;
        params.offset = params.offset ?: 0;
        Integer count = applicationService.getCount(params);
        List<OAuthClient> clients = applicationService.getApplications(params);
        render(view: "/admin/application/appView", model: [clients: clients, count: count])
    }

    def edit() {
        OAuthClient client = params.id ? OAuthClient.get(params.id) : new OAuthClient();
        render(view: "/admin/application/infoEdit", model: [client: client])
    }

    def save() {
        Boolean result = applicationService.save(params);
        if(result) {
            render([status: "success", message: g.message(code: "application.save.success")] as JSON)
        } else  {
            render([status: "error", message: g.message(code: "application.save.error")] as JSON)
        }
    }

    def delete() {
        Boolean result = applicationService.delete(params.long("id"));
        if(result) {
            render([status: "success", message: g.message(code: "application.delete.success")] as JSON)
        } else  {
            render([status: "error", message: g.message(code: "application.delete.error")] as JSON)
        }
    }

}
