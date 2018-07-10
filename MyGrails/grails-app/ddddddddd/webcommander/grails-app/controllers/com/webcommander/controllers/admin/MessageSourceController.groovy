package com.webcommander.controllers.admin

import com.webcommander.admin.MessageSource
import com.webcommander.admin.MessageSourceService
import grails.converters.JSON

class MessageSourceController {
    MessageSourceService messageSourceService

    def loadAppView() {
        params.max = params.max ?: "10";
        params.offset = params.offset ?: "0";
        params.locale = params.locale ?: 'all';
        List<MessageSource> messages = messageSourceService.getMessages(params)
        Integer count = messageSourceService.getCount(params);
        render(view: "/admin/messageSource/appView", model: [messages: messages, count: count]);
    }

    def save() {
        if( !messageSourceService.isUnique(params)) {
            render([status: "alert", message: g.message(code: "message.exists")] as JSON)
        } else {
            def result =  messageSourceService.save(params);
            String message = "message.save.${result ? "success" : "failed"}";
            render([status: result ? "success" : "error", message: g.message(code: message)] as JSON)
        }
    }

    def remove() {
        def result =  messageSourceService.remove(params.long("id"));
        String message = "message.delete.${result ? "success" : "failed"}";
        render([status: result ? "success" : "error", message: g.message(code: message)] as JSON)
    }
}
