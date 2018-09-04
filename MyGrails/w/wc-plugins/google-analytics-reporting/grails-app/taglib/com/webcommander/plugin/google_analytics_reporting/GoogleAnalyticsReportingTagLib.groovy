package com.webcommander.plugin.google_analytics_reporting

class GoogleAnalyticsReportingTagLib {
    GoogleAnalyticsService googleAnalyticsService

    static namespace = "google"

    def adminJSs = { attrs, body ->
        out << body()
        String url = "plugins/google-analytics-reporting/js/google-analytics.js"
        out << app.javascript(src: url)
    }

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
            datasetLabels.each { key ->
                out << "<div class='dataset'>"
                out << g.message(code: key)
                out << "</div>"
            }
        }
    }

    def all_traffic_chart = { attrs, body ->
        out << "<div class='chart-block' chart-type='${attrs.type}'>"
        List rows = googleAnalyticsService.getSummaryTotalTrafficChart(params.dateForm, params.dateTo, params.analyticsType)
        chart(rows);
        out << "</div>"
    }

    def keyword_chart = { attrs, body ->
        out << "<div class='chart-block' chart-type='${attrs.type}'>"
        List rows = googleAnalyticsService.getKeywordsSearchTrafficChart(params.dateForm, params.dateTo, params.analyticsType)
        chart(rows)
        out << "</div>"
    }

    def direct_traffic_chart = { attrs, body ->
        out << "<div class='chart-block' chart-type='${attrs.type}'>"
        List rows = googleAnalyticsService.getDirectSearchTrafficChart(params.dateForm, params.dateTo, params.analyticsType)
        chart(rows)
        out << "</div>"
    }

    def referral_traffic_chart = { attrs, body ->
        out << "<div class='chart-block' chart-type='${attrs.type}'>"
        List rows = googleAnalyticsService.getReferralSearchTrafficChart(params.dateForm, params.dateTo, params.analyticsType)
        chart(rows)
        out << "</div>"
    }

    def paid_traffic_chart = { attrs, body ->
        out << "<div class='chart-block' chart-type='${attrs.type}'>"
        List rows = googleAnalyticsService.getPaidSearchTrafficChart(params.dateForm, params.dateTo, params.analyticsType)
        chart(rows)
        out << "</div>"
    }

    def unique_visitor_chart = { attrs, body ->
        out << "<div class='chart-block' chart-type='${attrs.type}'>"
        List rows = googleAnalyticsService.getUniqueVisitorChart(params.dateForm, params.dateTo, params.analyticsType)
        chart(rows)
        out << "</div>"
    }

    def page_views_chart = { attrs, body ->
        out << "<div class='chart-block' chart-type='${attrs.type}'>"
        List rows = googleAnalyticsService.getPageViewChart(params.dateForm, params.dateTo, params.analyticsType)
        chart(rows)
        out << "</div>"
    }

    def pages_per_visit_chart = { attrs, body ->
        out << "<div class='chart-block' chart-type='${attrs.type}'>"
        List rows = googleAnalyticsService.getPagesPerVisitChart(params.dateForm, params.dateTo, params.analyticsType)
        chart(rows)
        out << "</div>"
    }

    def average_visit_duration_chart = { attrs, body ->
        out << "<div class='chart-block' chart-type='${attrs.type}'>"
        List rows = googleAnalyticsService.getAverageVisitDurationChart(params.dateForm, params.dateTo, params.analyticsType)
        chart(rows)
        out << "</div>"
    }

    def bounce_rate_chart = { attrs, body ->
        out << "<div class='chart-block' chart-type='${attrs.type}'>"
        List rows = googleAnalyticsService.getBounceRateChart(params.dateForm, params.dateTo, params.analyticsType)
        chart(rows)
        out << "</div>"
    }

    def organic_traffic_chart = { attrs, body ->
        out << "<div class='chart-block' chart-type='${attrs.type}'>"
        List rows = googleAnalyticsService.getOrganicSearchTrafficChart(params.dateForm, params.dateTo, params.analyticsType)
        chart(rows)
        out << "</div>"
    }
}
