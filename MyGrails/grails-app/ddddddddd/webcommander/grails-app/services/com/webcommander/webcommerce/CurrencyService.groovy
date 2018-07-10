package com.webcommander.webcommerce

import com.webcommander.tenant.Thread
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.SimpleBindings

@Transactional
class CurrencyService {
    ScriptEngine _scriptEngine;

    private Closure getCriteriaClosure(GrailsParameterMap params) {
        return {
            if (params.searchText) {
                or {
                    ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
                    ilike("code", "%${params.searchText.trim().encodeAsLikeText()}%")
                }
            }
            if(params.containsKey("active")) {
                eq("active", params.active == true)
                eq("base", false)
            }
        }
    }

    int getCurrencyCount(GrailsParameterMap params) {
        return Currency.createCriteria().count {
            and getCriteriaClosure(params)
        }
    }

    List<Currency> getCurrencies(GrailsParameterMap params) {
        def listMap = [max: params.max, offset: params.offset];
        return Currency.createCriteria().list(listMap) {
            and getCriteriaClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc")
        }
    }

    boolean saveCurrency(GrailsParameterMap params) {
        Currency currency = params.id ? Currency.get(params.id) : new Currency();
        Double oldConversionRate = currency.conversionRate
        currency.properties = params
        if (!currency.manualConversion) {
            currency.conversionRate = oldConversionRate;
        }
        currency.save();
        if (currency.active && currency.base) {
            setBaseCurrency(currency.id)
        }
        if (!currency.hasErrors() && !currency.manualConversion) {
            Thread.start {
                Currency.withNewSession {
                    Currency target = Currency.get(currency.id)
                    Currency baseCurrency = Currency.createCriteria().get { eq("base", true) }
                    updateCurrencyRate(target, baseCurrency)
                }
            }
        }
        return !currency.hasErrors()
    }

    void deleteCurrencies(List<Long> ids) {
        for (Long id : ids) {
            Currency currency = Currency.proxy(id);
            currency.delete()
        }
    }

    void setBaseCurrency(Long id) {
        Currency currency = Currency.get(id);
        Currency.withNewTransaction {
            Currency.where {
                id != id
                manualConversion == true
            }.list().each {
                it.conversionRate = (it.conversionRate/currency.conversionRate).toDouble()
            }*.save(flush: true);
            Currency.createCriteria().list {
                ne("id", id)
            }.each {
                it.base = false
                it.save()
            }
            Currency.where {
                id == id
            }.updateAll([base: true, active: true, conversionRate: 1D]);
            if (!Currency.get(id).active) {
                throw new ApplicationRuntimeException("inactive.currency.could.not.be.base")
            }

            Thread.start {
                updateCurrencyConversionRates()
            }

        }
    }

    private getScriptEngine() {
        if (!_scriptEngine) {
            _scriptEngine = new ScriptEngineManager().getEngineByExtension("js");
        };
        return _scriptEngine
    }

    void updateCurrencyRate(Currency currency, Currency baseCurrency) {
        try {
            String urlString = currency.url.replace("\$source\$", baseCurrency.code).replace("\$target\$", currency.code);
            URL url = new URL(urlString);
            String data = url.text
            if (currency.updateScript) {
                SimpleBindings bindings = [data: data] as SimpleBindings
                scriptEngine.eval(currency.updateScript, bindings);
                currency.conversionRate = bindings.get("rate").toDouble();
                currency.save()
            } else {
                currency.conversionRate = data.toDouble();
                currency.save()
            }
        } catch (Exception exception) {
            log.error(exception.message)
        };
    }

    void updateCurrencyConversionRates() {
        Currency.withNewSession {
            Currency.withNewTransaction {
                List<Currency> currencies = Currency.createCriteria().list {
                    eq("manualConversion", false)
                    eq("base", false)
                }
                Currency baseCurrency = Currency.where { base == true }.get()
                currencies.each { Currency currency ->
                    updateCurrencyRate(currency, baseCurrency)
                }
            }
        }
        AppUtil.setBaseCurrency()
    }

    Currency changeInvocation(Long id, Boolean status) {
        def currency = Currency.load(id)
        currency.active = status
        return currency.save()
    }

    @Transactional
    Integer saveBasicBulkProperties(Map properties) {

        List<Long> currencyIds = properties.list("currencyId").collect { it.toLong() }

        int count = 0

        currencyIds.each { id ->
            Currency currency = Currency.get(id)

            Double conversionRate = properties."${id}".conversionRate.toDouble(0.0)
            currency.conversionRate = conversionRate

            //println("conversionRate >> "+conversionRate)

            currency.save()
            if (!currency.hasErrors()) {
                count++
            }

        }

        return count
    }

}
