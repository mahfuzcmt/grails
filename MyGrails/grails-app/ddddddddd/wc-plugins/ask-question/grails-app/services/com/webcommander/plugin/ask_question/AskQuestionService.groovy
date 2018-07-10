package com.webcommander.plugin.ask_question

import com.webcommander.ApplicationTagLib
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommanderMailService
import com.webcommander.config.StoreDetail
import com.webcommander.events.AppEventManager
import com.webcommander.tenant.Thread
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product
import grails.gorm.transactions.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@Initializable
class AskQuestionService {
    CommanderMailService commanderMailService
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    ApplicationTagLib app

    static void initialize() {
        AppEventManager.on("before-product-delete", { id->
            Question.createCriteria().list {
                eq("product.id", id)
            }*.delete()
        })
    }

    private Closure getCriteriaClosure (params) {
        def session = AppUtil.session;
        return {
            if(params.sort == "p.name" || params.searchText) {
                createAlias("product", "p")
            }

            if(params.searchText) {
                ilike("p.name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.askBy) {
                ilike("name", "%${params.askBy.trim().encodeAsLikeText()}%")
            }
            if (params.status == "replied") {
                eq("status", true)
            }
            if (params.status == "pending") {
                eq("status", false)
            }
            if (params.dateFrom) {
                Date date = params.dateFrom.dayStart.gmt(session.timezone);
                ge("created", date);
            }
            if (params.dateTo) {
                Date date = params.dateTo.dayEnd.gmt(session.timezone);
                le("created", date);
            }
        }
    }

    @Transactional
    Boolean saveQuestion(Map params) {
        Question question = new Question();
        question.name = params.name;
        question.email = params.email;
        question.question = params.question
        question.product = Product.get(params.productId.toLong())
        question.save();
        if(!question.hasErrors()) {
            Thread.start {
                AppUtil.initialDummyRequest();
                sendQuestion(question)
            }
        }
        return !question.hasErrors();
    }

    List<Question> getQuestions(Map params){
        def listMap = [max: params.max, offset: params.offset];
        return Question.createCriteria().list(listMap) {
            and getCriteriaClosure(params)
            order(params.sort ?: "id", params.dir ?: "desc")

        }
    }

    Question getQuestion(Long id) {
       return Question.get(id)
    }

    Integer getQuestionCount(Map params) {
        return Question.createCriteria().count{
            and getCriteriaClosure(params)
        }
    }

    @Transactional
    Boolean answer(params){
        Question question = Question.get(params.id);
        question.answer = params.answer;
        question.status = true
        try{
            sendAnswer(question)
        }catch (Exception e) {
            e.printStackTrace()
            return false;
        }
        question.save()
        return !question.hasErrors()
    }

   def sendAnswer(Question question) {
       Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("product-question-answer")
       if(!macrosAndTemplate.emailTemplate.active) {
           return false;
       }
       Map refinedMacros = macrosAndTemplate.commonMacros
       macrosAndTemplate.macros.each {
           switch (it.key.toString()) {
               case "product_name":
                   refinedMacros[it.key] = question.product.name.encodeAsBMHTML();
                   break;
               case "question":
                   refinedMacros[it.key] = question.question.encodeAsBMHTML();
                   break;
               case "answer":
                   refinedMacros[it.key] = question.answer.encodeAsBMHTML();
                   break;
               case "date":
                   refinedMacros[it.key] = question.created.gmt();
                   break;
               case "writer_name":
                   refinedMacros[it.key] = question.name.encodeAsBMHTML();
                   break;
               case "product_url":
                   refinedMacros[it.key] = app.siteBaseUrl() + "product/" + question.product.url;
                   break;
           }
       }
       commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, question.email)
   }

    def sendQuestion(Question question) {
        Question.withNewSession {
            question.attach();
            StoreDetail storeDetail = StoreDetail.first();
            Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("product-question")
            if(!macrosAndTemplate.emailTemplate.active) {
                return;
            }
            Map refinedMacros = macrosAndTemplate.commonMacros
            macrosAndTemplate.macros.each {
                switch (it.key.toString()) {
                    case "product_name":
                        refinedMacros[it.key] = question.product.name.encodeAsBMHTML();
                        break;
                    case "question":
                        refinedMacros[it.key] = question.question.encodeAsBMHTML();
                        break;
                    case "date":
                        refinedMacros[it.key] = question.created.gmt();
                        break;
                    case "writer_name":
                        refinedMacros[it.key] = question.name.encodeAsBMHTML();
                        break;
                    case "product_url":
                        refinedMacros[it.key] = app.siteBaseUrl() + "product/" + question.product.url;
                        break;
                }
            }
            commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, storeDetail.address.email)
        }
   }

   @Transactional
   Integer deleteQuestion(Map params) {
       List ids = params.list("id").collect{ it.toLong() };
       if(ids.size() > 0) {
           List<Question> questions = Question.where {
               id in ids
           }.list()
           questions*.delete();
           return ids.size()
       }
       return 0;
   }
}
