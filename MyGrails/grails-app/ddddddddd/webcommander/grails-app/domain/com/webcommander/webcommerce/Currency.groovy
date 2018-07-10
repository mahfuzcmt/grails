package com.webcommander.webcommerce

import com.webcommander.admin.Country
import com.webcommander.events.AppEventManager
import com.webcommander.util.AppUtil

class Currency {

    Long id
    String name
    String code
    String roundingType = "nearest"//nearest, up, down

    String symbol
    Double conversionRate = 1.00

    Integer decimalPoints = 2//0-10
    Double roundingInterval = 0.05//0.05, 0.5, 1, 5, 10

    Date rateUpdated
    Boolean active = true
    Boolean base = false
    Boolean manualConversion = true

    String url
    String updateScript
    Country country

    static constraints = {
        name(unique: true, maxSize: 100)
        code(unique: true, maxSize: 3)
        symbol(blank: false, maxSize: 3)
        url(nullable: true)
        updateScript(nullable: true)
    }

    static mapping = {
        updateScript type: "text"
    }

    static transients = []

    def beforeUpdate() {
        this.rateUpdated = new Date().gmt()
    }

    def beforeValidate() {
        if(!this.rateUpdated) {
            this.rateUpdated = new Date().gmt()
        }
    }

    static void initialize() {
        def insertSql = [
                ['Australian Dollar', 'AUD', '$', 1.00, true, "AU"],
                ['US Dollar', 'USD', '$', 0.776700, false, "US"],
                ['New Zealand Dollar', 'NZD', '$', 1.04185, false, "NZ"],
                ['Canadian Dollar', 'CAD', '$', 0.967108, false, "CA"],
                ['Pound Sterling', 'GBP', '£', 0.504433, false, "GB"],
                ['Euro', 'EUR', '€', 0.682393, false, "EU"]
        ]
        Closure _init = {
            if(Currency.count() == 0) {
                insertSql.each {
                    Currency currency = new Currency(name: it[0], code: it[1], symbol: it[2], conversionRate: it[3], base: it[4], country: Country.findByCode(it[5]))
                    currency.save()
                }
            }
        }
        if(Country.count()) {
            _init()
        } else {
            AppEventManager.one("country-bootstrap-init", "bootstrap-init", _init)
        }

    }
}
