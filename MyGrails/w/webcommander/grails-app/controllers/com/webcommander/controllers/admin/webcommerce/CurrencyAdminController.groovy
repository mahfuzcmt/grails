package com.webcommander.controllers.admin.webcommerce

import com.webcommander.authentication.annotations.Restriction
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Currency
import com.webcommander.webcommerce.CurrencyService
import grails.converters.JSON

class CurrencyAdminController {
    CommonService commonService;
    CurrencyService currencyService

    @Restriction(permission = "currency.view.list")
    def loadAppView() {
        params.max = params.max ?: "10";
        Integer count = currencyService.getCurrencyCount(params);
        List<Currency> currencies = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.max = max;
            params.offset = offset;
            currencyService.getCurrencies(params);
        }
        render(view: "/admin/currency/appView", model: [currencies: currencies, count: count]);
    }

    def edit() {
        Currency currency = params.id ? Currency.get(params.id) : new Currency();
        render(view: "/admin/currency/infoEdit", model: [currency: currency])
    }

    def view() {
        Currency currency = Currency.get(params.id);
        render(view: "/admin/currency/infoView", model: [currency: currency])
    }

    def save() {
        if (currencyService.saveCurrency(params)) {
            render([status: "success", message: g.message(code: "currency.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "currency.save.failure")] as JSON)
        }
    }

    def delete() {
        List<Long> ids = params.list("id").collect { it.toLong() }
        try {
            currencyService.deleteCurrencies(ids)
            render([status: "success", message: g.message(code: "${ids.size() == 1 ? "currency" : "currencies"}.delete.success")] as JSON)
        } catch (Exception err) {
            render([status: "error", message: g.message(code: "${ids.size() == 1 ? "currency" : "currencies"}.delete.failure")] as JSON)
        }
    }

    def isUnique() {
        render(commonService.responseForUniqueField(Currency, params.long("id"), params.field, params.value) as JSON)
    }

    def setBaseCurrency() {
        try {
            currencyService.setBaseCurrency(params.long("id"))
            render([status: "success", message: g.message(code: "currency.set.as.base")] as JSON)
        }catch (ApplicationRuntimeException err){
            throw err;
        }catch (Exception err) {
            render([status: "error", message: g.message(code: "currency.not.set.as.base")] as JSON)
        }
    }

    def loadCurrencyForSettings() {
        Boolean base = params.base == "true"
        params.active = base ? false : true
        Map settings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CURRENCY)
        List currencies = base ? [Currency.findByBase(true)] : AppUtil.getBean(CurrencyService).getCurrencies(params)
        render(view: "/admin/currency/currencySettings", model: [currencies: currencies, config: settings])
    }

    def addCurrencyPopup() {
        params.active = false
        List currencies = AppUtil.getBean(CurrencyService).getCurrencies(params)
        render(view: "/admin/currency/addCurrencyPopup", model: [currencies: currencies])
    }

    def changeInvocation() {
        Boolean invoke = params.invoke == "true"
        currencyService.changeInvocation(params.long("id"), invoke)
        render([status: "success", message: g.message(code: (invoke ? "currency.add.success" : "currency.delete.success"))] as JSON)
    }
}
