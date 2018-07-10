package com.webcommander.util.extenders

import com.webcommander.admin.Country
import com.webcommander.constants.DomainConstants
import com.webcommander.mock.web.MockWebRequest
import com.webcommander.util.AppUtil
import com.webcommander.util.NumberUtil
import com.webcommander.webcommerce.Currency
import grails.gsp.PageRenderer
import groovy.time.BaseDuration
import groovy.time.TimeCategory
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.NullObject
import org.grails.plugins.web.mime.FormatInterceptor
import org.grails.spring.context.support.PluginAwareResourceBundleMessageSource
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.hibernate.internal.SessionImpl
import org.springframework.web.context.request.RequestContextHolder

import javax.servlet.http.HttpServletRequest
import java.lang.reflect.Field
import java.math.RoundingMode
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat

static init() {

    if(hasProperty("toDate")) {
        return
    } //To Prevent Double Initialization

    Date.metaClass.with {
        gmt = { timeZone = null ->
            long time = delegate.getTime() - (timeZone ?: TimeZone.default).getOffset(delegate)
            return new Date(time)
        }

        toAppFormat = { type, showTime, showZone, timeZone ->
            if(type == "email") {
                return delegate.toEmailFormat()
            } else if(type == "admin") {
                return delegate.toAdminFormat(showTime, showZone, timeZone)
            } else {
                return delegate.toSiteFormat(showTime, showZone, timeZone)
            }
        }

        /**
         * It assumes that calling date is of GMT+0
         */
        toEmailFormat = {
            TimeZone timeZone = TimeZone.getTimeZone(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.EMAIL, "time_zone"))
            int offset = timeZone.getOffset(delegate)
            long time = delegate.getTime() + offset
            String dateFormat = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.EMAIL, "date_format")
            String timeFormat = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.EMAIL, "time_format")
            return new Date(time).toFormattedString(dateFormat, true, timeFormat, true, new SimpleTimeZone(offset, "temp"))
        }

        /**
         * It assumes that calling date is of GMT+0
         */
        toAdminFormat = { showTime, showZone, timeZone ->
            if(!timeZone) {
                timeZone = TimeZone.default
            }
            long time = delegate.getTime() + timeZone.getOffset(delegate)
            String dateFormat = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LOCALE, "admin_date_format")
            String timeFormat = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LOCALE, "admin_time_format")
            return new Date(time).toFormattedString(dateFormat, showTime, timeFormat, showZone, timeZone)
        }

        /**
         * It assumes that calling date is of GMT+0
         */
        toDatePickerFormat = { showTime, timeZone ->
            long time = delegate.getTime() + timeZone.getOffset(delegate)
            String dateFormat = "yyyy-MM-dd"
            return new Date(time).toFormattedString(dateFormat, showTime, "HH:mm:ss", false, timeZone)
        }

        /**
         * It assumes that calling date is of GMT+0
         */
        toSiteFormat = { showTime, showZone, timeZone ->
            if(!timeZone) {
                timeZone = TimeZone.default
            }
            long time = delegate.getTime() + timeZone.getOffset(delegate)
            String dateFormat = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LOCALE, "site_date_format")
            String timeFormat = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LOCALE, "site_time_format")
            return new Date(time).toFormattedString(dateFormat, showTime, timeFormat, showZone, timeZone)
        }

        /**
         * If fomZone not provided it assumes that calling date is of GMT+0
         */
        toZone = { timeZone, fromZone = null ->
            if(!timeZone) {
                return delegate
            }
            long time = delegate.getTime() + timeZone.getOffset(delegate) - (fromZone?.getOffset(delegate) ?: 0) 
            return new Date(time)
        }

        /**
         * It assumes that calling date is of GMT+0
         */
        toFormattedString = { datePattern, showTime, timePattern, showZone, timeZone ->
            String zoneString = ""
            if(showZone) {
                String symbol
                int offset = timeZone.getOffset(delegate)
                if(offset < 0) {
                    symbol = "-"
                    offset *= -1
                } else {
                    symbol = "+"
                }
                double hour = offset / 1000 / 60 / 60
                int hourOffset = Math.floor(hour)
                int minOffset = Math.floor((hour - hourOffset) * 60)
                zoneString = " (GMT" + symbol + hourOffset.toNDigit(2) + ":" + minOffset.toNDigit(2) + ")"
            }
            SimpleDateFormat formatted = new SimpleDateFormat(datePattern + (showTime ? " " + timePattern : ""))
            return formatted.format(delegate) + zoneString
        }

        getDayStart = {
            return delegate.clearTime()
        }

        getMonthStart = {
            Calendar cal = Calendar.getInstance()
            cal.setTime(delegate)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.clear(Calendar.MINUTE)
            cal.clear(Calendar.SECOND)
            cal.clear(Calendar.MILLISECOND)
            return cal.getTime()
        }

        getDayEnd = {
            delegate.clearTime() + 1.days - 1.seconds
        }

        plus = { BaseDuration duration ->
            TimeCategory.plus(delegate, duration)
        }

        minus = { Date duration -> //duration can be date or duration
            TimeCategory.minus(delegate, duration)
        }

        minus = { BaseDuration duration -> //duration can be date or duration
            TimeCategory.minus(delegate, duration)
        }
    }
    Date.metaClass.static.with {
        now = {
            new Date()
        }
        from = { String date, String format ->
            if (format == "editor") {
                format = "yyyy-MM-dd"
            } else if (format == "datetime-editor") {
                format = "yyyy-MM-dd hh:mm tt"
            }
            date.toDate(format)
        }
    }
    
    File.metaClass.with {
        created = {
            BasicFileAttributes attributes = Files.readAttributes(delegate.toPath(), BasicFileAttributes.class)
            FileTime creationTime = attributes.creationTime()
            return creationTime.toMillis()
        }
    }

    HttpServletRequest.metaClass.static.with {
        isBound = {
            RequestContextHolder.requestAttributes != null
        }
        mock = { closure = null ->
            HttpServletRequest _request = PageRenderer.PageRenderRequestCreator.createInstance("/page/dummy")
            _request.IS_DUMMY = true
            GrailsWebRequest webRequest = MockWebRequest.getMockedRequest(_request)
            RequestContextHolder.setRequestAttributes(webRequest)
            if (closure) {
                def returned = closure()
                RequestContextHolder.resetRequestAttributes()
                return returned
            }
            return webRequest
        }
    }
    HttpServletRequest.metaClass.with {
        withMime = { closure ->
            String accept = delegate.getHeader("Accept")
            String mime = accept ? (accept.matches(/.*application\/json.*/) ? "json" : (accept.matches(/.*text\/xml.*/) ? "xml" : "html")) : "html"
            LinkedHashMap<String, Object> formats = null
            def original = closure.delegate
            try {
                final interceptor = new FormatInterceptor()
                closure.delegate = interceptor
                closure.resolveStrategy = Closure.DELEGATE_ONLY
                closure.call()
                formats = interceptor.formatOptions
            } finally {
                closure.delegate = original
                closure.resolveStrategy = Closure.OWNER_FIRST
            }
            if(!formats.size()) {
                return null
            }
            Closure mimeFunc = formats[mime]
            if(mimeFunc) {
                return mimeFunc.call()
            } else {
                return null
            }
        }

        getIp = {
            return delegate.getHeader("X-Real-IP") ?: delegate.getRemoteAddr()
        }

        cache = { type ->
            List cache = delegate[type + "_cache"]
            if(!cache) {
                cache = delegate[type + "_cache"] = []
            }
            return cache
        }
    }

    Integer.metaClass.with {
        toNDigit = { n ->
            return String.format("%0" + n + "d", delegate)
        }

        getMonths = {
            TimeCategory.getMonths(delegate)
        }

        getYears = {
            TimeCategory.getYears(delegate)
        }

        getWeeks = {
            TimeCategory.getWeeks(delegate)
        }

        getDays = {
            TimeCategory.getDays(delegate)
        }

        getHours = {
            TimeCategory.getHours(delegate)
        }

        getMinutes = {
            TimeCategory.getMinutes(delegate)
        }

        getSeconds = {
            TimeCategory.getSeconds(delegate)
        }

        getMilliseconds = {
            TimeCategory.getMilliseconds(delegate)
        }
    }

    Map.metaClass.with {
        merge = { Map _map ->
            Map source = delegate
            _map.each {
                if (source[it.key] instanceof Map && it.value instanceof Map) {
                    source[it.key].merge it.value
                } else if (source[it.key] instanceof Collection && it.value instanceof Collection) {
                    source[it.key].addAll it.value
                } else {
                    source[it.key] = it.value
                }
            }
        }
        lmerge = { _map ->
            Map source = delegate
            _map.each {
                if (!source.containsKey(it.key)) {
                    source.putAt it.key, it.value
                } else if (source[it.key] instanceof Map) {
                    source[it.key].lmerge it.value
                }
            }
        }
        erase = { Closure closure ->
            def toRemove = delegate.findAll closure
            delegate.keySet().removeAll(toRemove.keySet())
        }
    }

    HashMap.metaClass.with {
        list = { _name ->
            Object paramValues = delegate[_name]
            if (paramValues == null) {
                return Collections.EMPTY_LIST
            }
            if (paramValues.getClass().isArray()) {
                return Arrays.asList((Object[])paramValues)
            }
            if (paramValues instanceof Collection) {
                return new ArrayList((Collection)paramValues)
            }
            return Collections.singletonList(paramValues)
        }
    }

    NullObject.getNullObject().getMetaClass().with {
        toInteger = { Integer defaultInt = 0 ->
            return defaultInt
        }

        toLong = { Long defaultLong = 0 ->
            return defaultLong
        }

        toBoolean = { Boolean defaultBoolean = false ->
            return defaultBoolean
        }

        trim = { String defaultString = null ->
            return defaultString
        }
        isNull = {
            return true
        }
    }
    
    Number.metaClass.with {
        toPrice = { showCurrencyCode = false ->
            BigDecimal number = delegate.toBigDecimal()
            Currency currency
            try {
                currency = AppUtil.siteCurrency
            } catch (Exception ex) {
                currency = AppUtil.baseCurrency
            }

            String roundingType = currency.roundingType
            Integer decimalPoints = currency.decimalPoints

            //Double interval = currency.roundingInterval ?: 0.05D
            Double interval = NumberUtil.getInterval(decimalPoints)

            Locale locale = new Locale("", Country.get(currency.countryId).code)

            RoundingMode roundingMode = RoundingMode.HALF_EVEN
            switch (roundingType) {
                case "up":
                    roundingMode = RoundingMode.UP
                    break
                case "down":
                    roundingMode = RoundingMode.DOWN
                    break
                /*case "nearest":
                    roundingMode = RoundingMode.UP
                    break*/
            }
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale)
            numberFormat.setRoundingMode(roundingMode)
            numberFormat.setMaximumFractionDigits(decimalPoints)
            numberFormat.setMinimumFractionDigits(decimalPoints)

            NumberFormat numberFormat1 = new DecimalFormat("00.###############")
            numberFormat1.setCurrency(java.util.Currency.getInstance(currency.code))
            numberFormat1.setRoundingMode(RoundingMode.HALF_EVEN)
            BigDecimal returnValue = numberFormat1.format(interval).toBigDecimal()
            number = number.divide(returnValue, 10, roundingMode).setScale(0, roundingMode).multiply(returnValue)

            String value = numberFormat.format(number.toDouble()).trim()
            value = value.split(" ")[1]
            if(showCurrencyCode) {
                value = currency.code + " " + value
            }
            return value
        }

        toTax = { ruleData = [:], session = AppUtil.session ->
            def currency = session.currency ? Currency.load(session.currency.id) : AppUtil.baseCurrency
            if(!currency) {
                currency = Currency.findByCode("AUD")
            }
            Locale locale = new Locale("", Country.get(currency.countryId).code)
            NumberFormat numberFormat = NumberFormat.getInstance(locale)

            RoundingMode roundingMode

            switch (ruleData.roundingType) {
                case DomainConstants.ROUNDING_TYPE.UP:
                    roundingMode = RoundingMode.UP
                    break
                case DomainConstants.ROUNDING_TYPE.DOWN:
                    roundingMode = RoundingMode.DOWN
                    break
                default:
                    roundingMode = RoundingMode.HALF_EVEN
                    break
            }

            numberFormat.setRoundingMode(roundingMode)
            numberFormat.setMaximumFractionDigits(ruleData.decimalPoint)
            numberFormat.setGroupingUsed(false)

            String value = numberFormat.format(delegate)
            value = ruleData.decimalPoint ? value : (value + ".00")

            return value.toDouble()
        }

        toAdminPrice = { precision = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "max_precision")  as Integer ->
            return delegate.toConfigPrice()
            //return delegate.toFixed(precision, false)
        }

        toConfigPrice = { showCurrencyCode = false ->
            BigDecimal number = delegate.toBigDecimal()
            Currency currency
            try {
                currency = AppUtil.siteCurrency
            } catch (Exception ex) {
                currency = AppUtil.baseCurrency
            }

            String roundingType = currency.roundingType
            Integer decimalPoints = currency.decimalPoints

            //Double interval = currency.roundingInterval ?: 0.05D
            Double interval = NumberUtil.getInterval(decimalPoints)

            Locale locale = new Locale("", Country.get(currency.countryId).code)

            RoundingMode roundingMode = RoundingMode.HALF_EVEN
            switch (roundingType) {
                case "up":
                    roundingMode = RoundingMode.UP
                    break
                case "down":
                    roundingMode = RoundingMode.DOWN
                    break
            }
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale)
            numberFormat.setRoundingMode(roundingMode)
            numberFormat.setMaximumFractionDigits(decimalPoints)
            numberFormat.setMinimumFractionDigits(decimalPoints)
            numberFormat.setGroupingUsed(false);

            NumberFormat numberFormat1 = new DecimalFormat("00.###############")
            numberFormat1.setCurrency(java.util.Currency.getInstance(currency.code))
            numberFormat1.setRoundingMode(RoundingMode.HALF_EVEN)
            BigDecimal returnValue = numberFormat1.format(interval).toBigDecimal()
            number = number.divide(returnValue, 10, roundingMode).setScale(0, roundingMode).multiply(returnValue)

            String value = numberFormat.format(number.toDouble()).trim()
            value = value.split(" ")[1]
            if(showCurrencyCode) {
                value = currency.code + " " + value
            }
            return value
        }

        toCurrency = { targetCurrency = AppUtil.session.currency ->
            if (targetCurrency) {
                return delegate * targetCurrency.conversionRate
            } else {
                return delegate
            }
        }

        toLength = { padZero = true ->
            return delegate.toFixed(2, padZero)
        }

        toWeight = { padZero = true ->
            return delegate.toFixed(3, padZero)
        }

        toFixed = { n, padZero = true ->
            if(delegate instanceof Integer || delegate instanceof Long) {
                return delegate.toDouble().toFixed(n, padZero)
            }
            def string = String.format("%.${n}f", delegate)
            if (!padZero) {
                string = string.replaceAll(/(\.0*|0+)$/, "")
            }
            return string
        }

        getMonths = {
            TimeCategory.getMonths(delegate.intValue())
        }

        getYears = {
            TimeCategory.getYears(delegate.intValue())
        }

        getWeeks = {
            TimeCategory.getWeeks(delegate.intValue())
        }

        getDays = {
            TimeCategory.getDays(delegate.intValue())
        }

        getHours = {
            TimeCategory.getHours(delegate.intValue())
        }

        getMinutes = {
            TimeCategory.getMinutes(delegate.intValue())
        }

        getSeconds = {
            TimeCategory.getSeconds(delegate.intValue())
        }

        getMilliseconds = {
            TimeCategory.getMilliseconds(delegate.intValue())
        }
    }

    String.metaClass.with {
        toDate = { String format = null ->
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format ?: "yyyy-MM-dd hh:mm:ss")
                return dateFormat.parse(delegate)
            } catch (Throwable x) {
                return null
            }
        }

        toInteger = { Integer defaultInt ->
            try {
                return delegate.toInteger()
            } catch (Throwable x) {
                return defaultInt
            }
        }

        textify = {
            return delegate.replaceAll("\\<[^>]*>", "")
        }

        truncate = { Integer length ->
            if (length > 3 && delegate.size() > length) {
                return delegate.substring(0, length - 3) + "..."
            }
            return delegate
        }

        toLong = { Long defaultLong ->
            try {
                return delegate.toLong()
            } catch (Throwable x) {
                return defaultLong
            }
        }

        toDouble = { Double defaultDouble ->
            try {
                return delegate.toDouble()
            } catch (Throwable x) {
                return defaultDouble
            }
        }

        toBoolean = { Boolean defaultBoolean = false ->
            if (delegate == "") {
                return defaultBoolean
            }
            String check = delegate.toLowerCase()
            return check == "true" || check == "yes" || check == "on" || check == "1"
        }

        /**
         * It assumes the format is only a date format
         * default format is yyyy-MM-dd
         */
        getDayStart = { String format = null ->
            return (delegate + " 00:00:00").toDate(format ? format + " hh:mm:ss" : null)
        }

        /**
         * It assumes the format is only a date format
         * default format is yyyy-MM-dd
         */
        getDayEnd = { String format = null ->
            return (delegate + " 23:59:59").toDate(format ? format + " hh:mm:ss" : null)
        }

        sanitize = {
            return delegate.trim().toLowerCase().replaceAll(/[\s\.]/, "-").replaceAll("[^a-z0-9-\\._]+", "-")
        }

        removeLast = { char match ->
            int index = delegate.lastIndexOf((int) match)
            String returnable = delegate
            if (index != -1) {
                returnable = delegate.substring(0, index) + delegate.substring(index + 1)
            }
            return returnable
        }

        replaceLast = { char match, char replace ->
            int index = delegate.lastIndexOf((int) match)
            String returnable = delegate
            if (index != -1) {
                returnable = delegate.substring(0, index) + replace + delegate.substring(index + 1)
            }
            return returnable
        }

        dotCase = {
            def writer = new StringWriter()
            def reader = new StringReader(delegate)
            char data
            while ((data = reader.read()) != -1) {
                if (data > ('@' as char) && data < ('[' as char)) {
                    writer.append(".")
                    writer.append((char) (data.charValue() + 32))
                } else {
                    writer.append(data)
                }
            }
            return writer.toString()
        }

        camelCase = { initial = true ->
            def writer = new StringWriter()
            def reader = new StringReader(delegate)
            char data
            boolean nextUpper = initial
            while ((data = reader.read()) != -1) {
                if(data > ('`' as char) && data < ('{' as char)) {
                    if(nextUpper) {
                        writer.append((char) (data.charValue() - 32))
                        nextUpper = false
                    } else {
                        writer.append(data)
                    }
                } else if ((data > ('@' as char) && data < ('[' as char)) || (data > ('/' as char) && data < (':' as char))) {
                    nextUpper = false
                    writer.append(data)
                } else {
                    nextUpper = true
                }
            }
            return writer.toString()
        }

        toValidFileName = {
            return delegate.replaceAll("", "")
        }

        toAdminPrice = { precision = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "max_precision")  as Integer ->
            return delegate.toDouble().toFixed(precision, false)
        }
    }

    SessionImpl.metaClass.with {
        discard = {
            if(delegate.transactionInProgress) {
                List evictables = []
                delegate.persistenceContext.entitiesByKey.values().each {
                    if(it.isDirty()) {
                        evictables + it
                    }
                }
                evictables*.discard()
            }
        }
    }

    TimeZone.metaClass.with {
        getOffset = { date ->
            if(delegate.inDaylightTime(date)) {
               return delegate.rawOffset + delegate.getDSTSavings()
            } else {
               return delegate.rawOffset
            }
        }
    }

    URL.metaClass.with {
        post = { data ->
            String parameters = WebUtils.toQueryString(data).substring(1)
            HttpURLConnection connection = (HttpURLConnection) delegate.openConnection()
            connection.setDoOutput(true)
            connection.setDoInput(true)
            connection.setInstanceFollowRedirects(false)
            connection.setRequestMethod("POST")
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.setRequestProperty("charset", "utf-8")
            connection.setUseCaches (false)
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream())
            wr.writeBytes(parameters)
            wr.flush()
            wr.close()
            ByteArrayOutputStream out = new ByteArrayOutputStream()
            out << connection.getInputStream()
            connection.disconnect()
            out.flush()
            return out.toString()
        }
    }

    ArrayList.metaClass.with {
        safeSubList = { from, to ->
            if(delegate.size() < to) {
                to = delegate.size()
            }
            return delegate.subList(from, to)
        }
        contains = { Closure cls ->
            ArrayList _this = delegate
            _this.contains new Object() {
                @Override
                boolean equals(Object entry) {
                    return cls(entry)
                }
            }
        }
    }

    Object.metaClass.with {
        getPrivateValue = { name, clazz = null ->
            Field field = (clazz ?: delegate.class).getDeclaredField(name)
            if(!field) {
                return null
            }
            field.setAccessible(true)
            return field.get(delegate)
        }

        setPrivateValue = { name, value, clazz = null ->
            Field field = (clazz ?: delegate.class).getDeclaredField(name)
            if(!field) {
                return
            }
            field.setAccessible(true)
            field.set delegate, value
        }
        isNull = {
            return delegate instanceof NullObject
        }
    }

    PluginAwareResourceBundleMessageSource.metaClass.clearCache = {
        delegate.cachedMergedPluginProperties.clear()
        delegate.cachedMergedBinaryPluginProperties.clear()
        InvokerHelper.invokeSuperMethod(delegate, "clearCache", null)
    }

    HibernateCriteriaBuildersExtenders.extend()
}