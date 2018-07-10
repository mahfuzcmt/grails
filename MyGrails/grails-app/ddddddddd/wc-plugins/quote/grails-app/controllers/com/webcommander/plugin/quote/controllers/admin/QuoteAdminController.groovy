package com.webcommander.plugin.quote.controllers.admin

import com.webcommander.authentication.annotations.License
import com.webcommander.admin.AdministrationService
import com.webcommander.admin.ConfigService
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.constants.DomainConstants
import com.webcommander.models.AddressData
import com.webcommander.plugin.quote.Quote
import com.webcommander.plugin.quote.QuoteService
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Address
import com.webcommander.webcommerce.OrderService
import grails.converters.JSON


class QuoteAdminController {
    QuoteService quoteService
    OrderService orderService
    ConfigService configService
    AdministrationService administrationService

    def loadConfig() {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.QUOTE);
        render(view: "/plugins/quote/admin/appConfig", model: [config: config])
    }

    @License(required = "allow_quote_feature")
    @Restriction(permission = "quote.view.list")
    def loadAppView() {
        params.max  = params.max ?: "10"
        params.offset = params.offset ?: "0"
        List<Quote> quotes = quoteService.getQuotes(params)
        Integer count = quoteService.getQuoteCount(params)
        render(view: "/plugins/quote/admin/appView", model: [quotes: quotes, count: count])
    }

    @License(required = "allow_quote_feature")
    @Restriction(permission = "quote.send")
    def sendQuote() {
        Quote quote = Quote.get(params.id)
        try {
            quoteService.sendEmail(quote)
            render ([status: "success", message: g.message(code: "quote.sent.success")] as JSON)
        } catch (Exception ex) {
            render ([status: "error", message: g.message(code: "quote.sent.error")] as JSON)
        }
    }

    @License(required = "allow_quote_feature")
    @Restriction(permission = "quote.view")
    def view() {
        Quote quote = Quote.get(params.id)
        render view: "/plugins/quote/admin/viewPopup", model: [quote: quote]
    }

    @License(required = "allow_quote_feature")
    @Restriction(permission = "quote.manage")
    def requote() {
        Quote quote = Quote.get(params.id)
        render view: "/plugins/quote/admin/editPopup", model: [quote: quote]
    }

    def changeAddress() {
        Quote quote = Quote.get(params.quote)
        Address address = params.address ? orderService.getAddressFromJson(params.address) : quote[params.section];
        AddressData addressData = new AddressData(address)
        def states = administrationService.getStatesForCountry(addressData.countryId)
        Map fieldsConfigs = (Map) AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES["${params.section.toUpperCase()}_ADDRESS_FIELD"]);
        List sortedFields = configService.getSortedFields(fieldsConfigs)
        params.stateName = "stateId"
        render(view: "/plugins/quote/admin/changeAddress", model: [fields: sortedFields, fieldsConfigs: fieldsConfigs, address: addressData,
                                                                  states: states, section: params.section])

    }

    @License(required = "allow_quote_feature")
    @Restriction(permission = "quote.manage")
    def save() {
        Boolean result = quoteService.requote(params)
        if(result) {
            render([status: "success", message: g.message(code: "quote.save.success")] as JSON)
        } else {
            render([status: "success", message: g.message(code: "quote.save.success")] as JSON)
        }
    }

    @License(required = "allow_quote_feature")
    @Restriction(permission = "quote.manage")
    def remove() {
        Boolean result = quoteService.removeQuote(params)
        if(result) {
            render([status: "success", message: g.message(code: "quote.remove.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "quote.remove.error")] as JSON)
        }
    }

    @License(required = "allow_quote_feature")
    @Restriction(permission = "quote.manage")
    def makeOrder() {
        Boolean result = quoteService.makeOrderFromQuote(params.long("quoteId"))
        if(result) {
            render([status: "success", message: g.message(code: "quote.convert.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "quote.convert.error")] as JSON)
        }
    }
}
