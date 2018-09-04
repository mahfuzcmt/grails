package com.webcommander.admin

import com.webcommander.annotations.Initializable
import com.webcommander.beans.SiteMessageSource
import com.webcommander.common.CommonService
import com.webcommander.events.AppEventManager
import com.webcommander.util.AppUtil
import grails.gorm.transactions.Transactional

@Initializable
@Transactional
class MessageSourceService {
    CommonService commonService
    SiteMessageSource siteMessageSource

    static void initialize() {
        AppEventManager.on("plugin-installed", { identifier ->
            siteMessageSource.clearCache()
        })
    }

    private Closure getCriteriaClosure(Map params) {
        def session = AppUtil.session
        Closure closure = {
            if (params.searchText) {
                or {
                    ilike("messageKey", "%${params.searchText.trim().encodeAsLikeText()}%")
                    ilike("message", "%${params.searchText.trim().encodeAsLikeText()}%")
                }
            }
            eq("locale", params.locale)
        }
        return closure;
    }

    List<MessageSource> getMessages(Map params) {
        def listMap = [max: params.max, offset: params.offset];
        return MessageSource.createCriteria().list(listMap) {
            and getCriteriaClosure(params);
            order(params.sort ?: "messageKey", params.dir ?: "asc");
        }
    }

    Integer getCount(Map params) {
        return MessageSource.createCriteria().count {
            and getCriteriaClosure(params)
        }
    }

    Boolean isUnique(Map params) {
        Boolean isUnique = commonService.isUnique(MessageSource, [
                id: params.id,
                field: "messageKey",
                value: params.key,
                compositeField: "locale",
                compositeValue: params.locale
        ]);
        return  isUnique;
    }

    Boolean save(Map params) {
        MessageSource message = params.id ? MessageSource.get(params.id) : new MessageSource() ;
        message.messageKey = params.key
        message.message = params.message
        message.locale = params.locale
        message.save();
        if (!message.hasErrors()) {
            siteMessageSource.clearCache();
            return true
        }
        return false
    }

    Boolean remove(Long id) {
        MessageSource messageSource = MessageSource.get(id)
        messageSource.delete();
        if (!messageSource.hasErrors()) {
            siteMessageSource.clearCache()
            return true
        }
        return false
    }

}
