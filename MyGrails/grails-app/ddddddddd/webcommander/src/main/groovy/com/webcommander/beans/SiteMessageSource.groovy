package com.webcommander.beans

import com.webcommander.admin.MessageSource
import com.webcommander.constants.NamedConstants
import com.webcommander.manager.CacheManager
import com.webcommander.util.AppUtil
import com.webcommander.util.TemplateMatcher

import java.text.MessageFormat
import java.util.concurrent.ConcurrentHashMap

class SiteMessageSource {

    Map<String, Properties> getLocalePropCache() {
        Map<String, Properties>  localePropCache = CacheManager.get(NamedConstants.CACHE.SCOPE_APP, "local_prop_cache")
        if(localePropCache == null) {
            CacheManager.cache(NamedConstants.CACHE.SCOPE_APP, localePropCache = new ConcurrentHashMap<>([:]), -1,"local_prop_cache")
        }
        return localePropCache
    }

    TemplateMatcher engine = new TemplateMatcher("%", "%")

    String convert(String code, List args, Map macros = null) {
        return convert(code, args as Object[], macros)
    }

    String convert(String code, Object[] args = null, Map macros = null) {
        if(!localePropCache.size()) {
            initCache();
        }
        if(!code) {
            return code;
        }
        if(code.startsWith("s:")) {
            Locale locale = AppUtil.request.getLocale();
            code = code.substring(2)
            String message;
            if (localePropCache[locale.toLanguageTag()] && localePropCache[locale.toLanguageTag()].getProperty(code)) {
                message = localePropCache[locale.toLanguageTag()].getProperty(code)
            } else if (locale.baseLocale.region && localePropCache[locale.baseLocale.language] && localePropCache[locale.baseLocale.language].getProperty(code)) {
                message = localePropCache[locale.baseLocale.language].getProperty(code)
            } else if (localePropCache['all'] && localePropCache['all'].getProperty(code)) {
                message = localePropCache['all'].getProperty(code)
            } else {
                message = code
            }
            if (args) {
                MessageFormat format = new MessageFormat(message)
                message = format.format(args)
            }
            if(macros) {
                message = engine.replace(message, macros)
            }
            return message.encodeAsBMHTML();
        }
        return code.encodeAsBMHTML();
    }

    void clearCache() {
        if(localePropCache != null) {
            CacheManager.removeCache(NamedConstants.CACHE.SCOPE_APP, "local_prop_cache")
        }
    }

    void initCache() {
        MessageSource.list().each {
            if (!localePropCache[it.locale]) {
                localePropCache[it.locale] = new Properties();
            }
            localePropCache[it.locale].setProperty(it.messageKey, it.message);
        }
    }
}
