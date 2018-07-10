package com.webcommander.plugin.ask_question.controllers.admin

import com.webcommander.authentication.annotations.License
import com.webcommander.captcha.CaptchaService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.ask_question.AskQuestionService
import com.webcommander.plugin.ask_question.Question
import com.webcommander.util.AppUtil
import grails.converters.JSON

class AskQuestionAdminController {
    CaptchaService captchaService
    AskQuestionService askQuestionService

    @License(required = "allow_question_answer_feature")
    def loadAppView() {
        params.max = params.max ?: "10";
        Integer count = askQuestionService.getQuestionCount(params);
        List<Question> questions = askQuestionService.getQuestions(params);
        render(view: "/plugins/ask_question/admin/appView", model: [questions: questions, count: count]);
    }

    @License(required = "allow_question_answer_feature")
    def view() {
        Question question = askQuestionService.getQuestion(params.long("id"));
        render(view: "/plugins/ask_question/admin/infoView", model: [question: question]);
    }

    @License(required = "allow_question_answer_feature")
    def reply() {
       Question question = askQuestionService.getQuestion(params.long("id"));
       render(view: "/plugins/ask_question/admin/replyForm", model: [question: question]);
    }

    @License(required = "allow_question_answer_feature")
    def answer() {
        if (askQuestionService.answer(params)) {
            render ([status: "success", message: g.message(code:  "answer.send.success")] as JSON);
        } else {
            render([status: "error", message: g.message(code: "answer.send.failure")] as JSON);
        }
    }

    def delete() {
        Integer result = askQuestionService.deleteQuestion(params);
        if(result) {
            render([status: "success", message: g.message(code: "question.delete.success")] as JSON);
        } else {
            render([status: "error", message: g.message(code: "question.delete.failure")] as JSON);
        }
    }

    def advanceFilter(){
        render(view: "/plugins/ask_question/admin/filter");
    }

    def config(){
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ASK_QUESTION)
        render(view: "/plugins/ask_question/admin/config", model: [config: config])
    }
}
