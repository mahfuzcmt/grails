package com.webcommander

import com.webcommander.report.ReportingService
import groovy.time.TimeCategory
import groovy.time.TimeDuration

class ReportingTagLib {
    ReportingService reportingService
    static namespace = "reporting"

    private chart(rows) {
        List datasetLabels = []
        if(rows.size()) {
            Map first = rows[0]
            out << "<label>"
            out << first.dimension
            out << "</label>"
            first.each { key, value ->
                if(key != "dimension") {
                    datasetLabels.push(key)
                    out << "<div class='data'>"
                    out << value
                    out << "</div>"
                }
            }
            if(rows.size() > 1) {
                rows[1..rows.size()-1].each { row ->
                    out << "<label>"
                    out << row.dimension
                    out << "</label>"
                    datasetLabels.each { key ->
                        out << "<div class='data'>"
                        out << row[key]
                        out << "</div>"
                    }
                }
            }
            datasetLabels.each { key ->
                out << "<div class='dataset'>"
                out << g.message(code: key)
                out << "</div>"
            }
        }
    }

    def renderProductChart = {attrs, body ->
        if(!params.reportCode) {
            params.reportCode = "product.by.properties"
        }
        def rows = reportingService.loadChartDataForProduct(params) ;
        out << "<div class='chart-block ${rows.size() == 0 ? 'no-record-chart' : ''}' chart-type='${attrs.type}'>"
        if(rows.size()) {
            chart(rows)
        } else {
            out << "<div class='no-record-text'>"
            out << g.message(code: "no.records.found")
            out << "</div>"
        }
        out << "</div>"
    }

    def renderOrderChart = {attrs, body ->
        if(!params.reportCode) {
            params.reportCode = "order.by.status"
        }
        def rows = reportingService.loadChartDataForOrder(params) ;
        out << "<div class='chart-block ${rows.size() == 0 ? 'no-record-chart' : ''}' chart-type='${attrs.type}'>"
        if(rows.size()) {
            chart(rows)
        } else {
            out << "<div class='no-record-text'>"
            out << g.message(code: "no.records.found")
            out << "</div>"
        }
        out << "</div>"
    }

    def renderPaymentChart = { attrs, body ->
        if(!params.reportCode) {
            params.reportCode = "payment.by.properties"
        }
        def rows = reportingService.loadChartDataForPayment(params) ;
        out << "<div class='chart-block ${rows.size() == 0 ? 'no-record-chart' : ''}' chart-type='${attrs.type}'>"
        if(rows.size()) {
            chart(rows)
        } else {
            out << "<div class='no-record-text'>"
            out << g.message(code: "no.records.found")
            out << "</div>"
        }
        out << "</div>"
    }

    def renderTaxChart = { attrs, body ->
        def rows = reportingService.loadChartDataForTax(params) ;
        out << "<div class='chart-block ${rows.size() == 0 ? 'no-record-chart' : ''}' chart-type='${attrs.type}'>"
        if(rows.size()) {
            chart(rows)
        } else {
            out << "<div class='no-record-text'>"
            out << g.message(code: "no.records.found")
            out << "</div>"
        }
        out << "</div>"
    }

    def timestamp = { attrs, body ->
        Date time = attrs.time
        Date current = new Date().gmt()
        TimeDuration duration = TimeCategory.minus(current, time)
        if(duration.years != 0) {
            out << duration.years + " " + g.message(code: "years");
        } else if(duration.months != 0) {
            out << duration.months + " " + g.message(code: "months");
        } else if(duration.days != 0) {
            out << duration.days + " " + g.message(code: "days");
        } else if(duration.hours != 0) {
            out << duration.hours + " " + g.message(code: "hours");
        } else if(duration.minutes != 0) {
            out << duration.minutes + " " + g.message(code: "minutes");
        } else {
            out << g.message(code: "few") + " " + g.message(code: "seconds");
        }
        out << " " + g.message(code: "ago")
        if(attrs.state) {
            out << " in " + attrs.state + ", " + attrs.country
        } else if(attrs.country) {
            out << " in " + attrs.country
        }
    }
}
