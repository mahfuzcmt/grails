package com.webcommander.plugin.google_analytics_reporting

import com.webcommander.admin.ConfigService
import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.auth.oauth2.CredentialRefreshListener
import com.google.api.client.auth.oauth2.TokenErrorResponse
import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.analytics.Analytics
import com.google.api.services.analytics.model.Accounts
import com.google.api.services.analytics.model.GaData
import com.google.api.services.analytics.model.Profiles
import com.google.api.services.analytics.model.Webproperties
import grails.gorm.transactions.Transactional

@Transactional
class GoogleAnalyticsService {
    ConfigService configService

    private static Long last_instance_time = 0
    private static Analytics cached_instance

    private Analytics initialize() {
        if(last_instance_time + 600000 < new Date().getTime()) {
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport()
            JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance()
            Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
            String clientId = analyticsConfig.client_id
            String clientSecret = analyticsConfig.client_secret
            GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport).setJsonFactory(JSON_FACTORY).setClientSecrets(clientId, clientSecret).addRefreshListener(new CredentialRefreshListener() {
                @Override
                public void onTokenResponse(Credential credential, TokenResponse tokenResponse) throws IOException {
                    List config = [
                        [
                            configKey: "access_token",
                            type: "google_analytics",
                            value: credential.accessToken
                        ],
                        [
                            configKey: "refresh_token",
                            type: "google_analytics",
                            value: credential.refreshToken
                        ]
                    ]
                    configService.update(config)
                    last_instance_time = 0
                    cached_instance = null
                }

                @Override
                public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) throws IOException {
                    List config = [
                        [
                            configKey: "access_token",
                            type: "google_analytics",
                            value: ""
                        ],
                        [
                            configKey: "refresh_token",
                            type: "google_analytics",
                            value: ""
                        ]
                    ]
                    configService.update(config)
                    last_instance_time = 0
                    cached_instance = null
                }
            }).build()
            credential.accessToken = analyticsConfig.access_token
            credential.refreshToken = analyticsConfig.refresh_token
            last_instance_time = new Date().getTime()
            return cached_instance = new Analytics.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(analyticsConfig.application_name).build()
        } else {
            last_instance_time = new Date().getTime()
            return cached_instance
        }
    }

    public Map getProfiles() {
        Analytics analytics = initialize()
        Accounts accounts = analytics.management().accounts().list().execute();
        Map profileMap = [:]
        accounts.getItems().each { account ->
            String acName = account.getName()
            String acId = account.getId()
            Map profiles = profileMap[acName] = [:]
            Webproperties webproperties = analytics.management().webproperties().list(acId).execute();
            webproperties.getItems().each { property ->
                String ptId = property.getId()
                Profiles _profiles = analytics.management().profiles().list(acId, ptId).execute();
                if (!_profiles.getItems().isEmpty()) {
                    _profiles.getItems().each { profile ->
                        profiles[profile.getId()] = profile.getName()
                    }
                }
            }
        }
        return profileMap;
    }

    public Map getSummaryTotalTraffic(String fromDate, String toDate) {
        def timezone = AppUtil.session.timezone
        Date from = (fromDate + " 00:00:00").toDate().gmt(timezone)
        Date to = (toDate + " 23:59:59").toDate().gmt(timezone)
        TimeZone zone = TimeZone.getTimeZone("UTC")
        Analytics analytics = initialize()
        Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        GaData data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:users,ga:pageviews,ga:pageviewsPerSession,ga:avgSessionDuration,ga:bounceRate,ga:newUsers,ga:percentNewSessions").execute();
        Map tabularData = [visits: 0, unique_visit: 0, page_view: 0, page_per_visit: 0, avg_visit_duration: 0, bounce_rate: 0, new_visitor: 0, percentage_new_visitor: 0,recurring_visitor: 0, percentage_recurring_visitor: 0]
        if(data.getTotalResults() > 0 ) {
            loadSingleRow(tabularData, data)
            tabularData.recurring_visitor = tabularData.visits.toLong() - tabularData.new_visitor.toLong()
            tabularData.percentage_recurring_visitor = 100 - tabularData.percentage_new_visitor.toDouble()
            return tabularData
        }
        else {
            return tabularData
        }
    }

    public List getSummaryTotalTrafficChart(String fromDate, String toDate, String analyticsType) {
        def timezone = AppUtil.session.timezone
        Date startTime = (fromDate + " 00:00:00").toDate()
        Date from = startTime.gmt(timezone)
        Date to = (toDate + " 23:59:59").toDate().gmt(timezone)
        TimeZone zone = TimeZone.getTimeZone("UTC")
        Analytics analytics = initialize()
        String dimensions = "ga:date" ;
        GaData data ;
        Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        switch(analyticsType) {
            case "hourly":
                dimensions = "ga:hour";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
            case "daily":
                dimensions = "ga:date";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
            case "weekly":
                dimensions = "ga:week";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
            case "monthly":
                dimensions = "ga:month";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
        }
        data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).execute();
        data.rows.each { row ->
            row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
            row[3] = row[1].toLong() - row[2].toLong()
        }
        return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
    }

    private void loadSingleRow(Map tabularData, GaData results) {
        List<String> row = results.getRows().get(0)
        tabularData.keySet().eachWithIndex { key, i ->
            tabularData[key] = row[i]
        }
    }

    private List getGraphData(GaData results, List headers) {
        List graphData = []
        results.rows.eachWithIndex { row, i ->
            Map tabularData = graphData[i] = [:]
            headers.eachWithIndex { key, j ->
                tabularData[key] = row[j]
            }
        }
        return graphData
    }

    public Map getKeywordsSearchTraffic(String fromDate, String toDate) {
        def timezone = AppUtil.session.timezone
        Date from = (fromDate + " 00:00:00").toDate().gmt(timezone)
        Date to = (toDate + " 23:59:59").toDate().gmt(timezone)
        TimeZone zone = TimeZone.getTimeZone("UTC")
        Analytics analytics = initialize()
        Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        GaData data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:users,ga:pageviews,ga:pageviewsPerSession,ga:avgSessionDuration,ga:bounceRate,ga:newUsers,ga:percentNewSessions").setSegment("gaid::-6").execute();
        Map tabularData = [visits: 0, unique_visit: 0, page_view: 0, page_per_visit: 0, avg_visit_duration: 0, bounce_rate: 0, new_visitor: 0, percentage_new_visitor: 0,recurring_visitor: 0, percentage_recurring_visitor: 0]
        if(data.getTotalResults() > 0 ) {
            loadSingleRow(tabularData, data)
            tabularData.recurring_visitor = tabularData.visits.toLong() - tabularData.new_visitor.toLong()
            tabularData.percentage_recurring_visitor = 100 - tabularData.percentage_new_visitor.toDouble()
            return tabularData
        }
        else {
            return tabularData
        }
    }
    public List getKeywordsSearchTrafficChart(String fromDate, String toDate, String analyticsType) {
        def timezone = AppUtil.session.timezone
        Date startTime = (fromDate + " 00:00:00").toDate()
        Date from = startTime.gmt(timezone)
        Date to = (toDate + " 23:59:59").toDate().gmt(timezone)
        TimeZone zone = TimeZone.getTimeZone("UTC")
        Analytics analytics = initialize()
        String dimensions = "ga:date" ;
        GaData data ;
        Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        switch(analyticsType) {
            case "hourly":
                dimensions = "ga:hour";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-6").execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
            case "daily":
                dimensions = "ga:date";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-6").execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
            case "weekly":
                dimensions = "ga:week";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-6").execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
            case "monthly":
                dimensions = "ga:month";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-6").execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
        }
        data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-6").execute();
        data.rows.each { row ->
            row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
            row[3] = row[1].toLong() - row[2].toLong()
        }
        return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
    }

    public Map getDirectSearchTraffic(String fromDate, String toDate) {
        def timezone = AppUtil.session.timezone
        Date from = (fromDate + " 00:00:00").toDate().gmt(timezone)
        Date to = (toDate + " 23:59:59").toDate().gmt(timezone)
        TimeZone zone = TimeZone.getTimeZone("UTC")
        Analytics analytics = initialize()
        Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        GaData data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:users,ga:pageviews,ga:pageviewsPerSession,ga:avgSessionDuration,ga:bounceRate,ga:newUsers,ga:percentNewSessions").setSegment("gaid::-7").execute();
        Map tabularData = [visits: 0, unique_visit: 0, page_view: 0, page_per_visit: 0, avg_visit_duration: 0, bounce_rate: 0, new_visitor: 0, percentage_new_visitor: 0,recurring_visitor: 0, percentage_recurring_visitor: 0]
        if(data.getTotalResults() > 0 ) {
            loadSingleRow(tabularData, data)
            tabularData.recurring_visitor = tabularData.visits.toLong() - tabularData.new_visitor.toLong()
            tabularData.percentage_recurring_visitor = 100 - tabularData.percentage_new_visitor.toDouble()
            return tabularData
        }
        else {
            return tabularData
        }
    }

    public List getDirectSearchTrafficChart(String fromDate, String toDate, String analyticsType) {
        def timezone = AppUtil.session.timezone
        Date startTime = (fromDate + " 00:00:00").toDate()
        Date from = startTime.gmt(timezone)
        Date to = (toDate + " 23:59:59").toDate().gmt(timezone)
        TimeZone zone = TimeZone.getTimeZone("UTC")
        Analytics analytics = initialize()
        String dimensions = "ga:date" ;
        GaData data ;
        Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        switch(analyticsType) {
            case "hourly":
                dimensions = "ga:hour";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-7").execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
            case "daily":
                dimensions = "ga:date";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-7").execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
            case "weekly":
                dimensions = "ga:week";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-7").execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
            case "monthly":
                dimensions = "ga:month";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-7").execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
        }
        data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-7").execute();
        data.rows.each { row ->
            row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
            row[3] = row[1].toLong() - row[2].toLong()
        }
        return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
    }

    public Map getReferralSearchTraffic(String fromDate, String toDate) {
        def timezone = AppUtil.session.timezone
        Date from = (fromDate + " 00:00:00").toDate().gmt(timezone)
        Date to = (toDate + " 23:59:59").toDate().gmt(timezone)
        TimeZone zone = TimeZone.getTimeZone("UTC")
        Analytics analytics = initialize()
        Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        GaData data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:users,ga:pageviews,ga:pageviewsPerSession,ga:avgSessionDuration,ga:bounceRate,ga:newUsers,ga:percentNewSessions").setSegment("gaid::-8").execute();
        Map tabularData = [visits: 0, unique_visit: 0, page_view: 0, page_per_visit: 0, avg_visit_duration: 0, bounce_rate: 0, new_visitor: 0, percentage_new_visitor: 0,recurring_visitor: 0, percentage_recurring_visitor: 0]
        if(data.getTotalResults() > 0 ) {
            loadSingleRow(tabularData, data)
            tabularData.recurring_visitor = tabularData.visits.toLong() - tabularData.new_visitor.toLong()
            tabularData.percentage_recurring_visitor = 100 - tabularData.percentage_new_visitor.toDouble()
            return tabularData
        }
        else {
            return tabularData
        }
    }

    public List getReferralSearchTrafficChart(String fromDate, String toDate, String analyticsType) {
        def timezone = AppUtil.session.timezone
        Date startTime = (fromDate + " 00:00:00").toDate()
        Date from = startTime.gmt(timezone)
        Date to = (toDate + " 23:59:59").toDate().gmt(timezone)
        TimeZone zone = TimeZone.getTimeZone("UTC")
        Analytics analytics = initialize()
        String dimensions = "ga:date" ;
        GaData data ;
        Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        switch(analyticsType) {
            case "hourly":
                dimensions = "ga:hour";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-8").execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
            case "daily":
                dimensions = "ga:date";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-8").execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
            case "weekly":
                dimensions = "ga:week";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-8").execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
            case "monthly":
                dimensions = "ga:month";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-8").execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
        }
        data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-8").execute();
        data.rows.each { row ->
            row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
            row[3] = row[1].toLong() - row[2].toLong()
        }
        return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
    }

    public Map getPaidSearchTraffic(String fromDate, String toDate) {
        def timezone = AppUtil.session.timezone
        Date from = (fromDate + " 00:00:00").toDate().gmt(timezone)
        Date to = (toDate + " 23:59:59").toDate().gmt(timezone)
        TimeZone zone = TimeZone.getTimeZone("UTC")
        Analytics analytics = initialize()
        Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        GaData data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:users,ga:pageviews,ga:pageviewsPerSession,ga:avgSessionDuration,ga:bounceRate,ga:newUsers,ga:percentNewSessions").setSegment("gaid::-4").execute();
        Map tabularData = [visits: 0, unique_visit: 0, page_view: 0, page_per_visit: 0, avg_visit_duration: 0, bounce_rate: 0, new_visitor: 0, percentage_new_visitor: 0,recurring_visitor: 0, percentage_recurring_visitor: 0]
        if(data.getTotalResults() > 0 ) {
            loadSingleRow(tabularData, data)
            tabularData.recurring_visitor = tabularData.visits.toLong() - tabularData.new_visitor.toLong()
            tabularData.percentage_recurring_visitor = 100 - tabularData.percentage_new_visitor.toDouble()
            return tabularData
        }
        else {
            return tabularData
        }
    }

    public List getPaidSearchTrafficChart(String fromDate, String toDate, String analyticsType) {
        def timezone = AppUtil.session.timezone
        Date startTime = (fromDate + " 00:00:00").toDate()
        Date from = startTime.gmt(timezone)
        Date to = (toDate + " 23:59:59").toDate().gmt(timezone)
        TimeZone zone = TimeZone.getTimeZone("UTC")
        Analytics analytics = initialize()
        String dimensions = "ga:date" ;
        GaData data ;
        Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        switch(analyticsType) {
            case "hourly":
                dimensions = "ga:hour";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-4").execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
            case "daily":
                dimensions = "ga:date";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-4").execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
            case "weekly":
                dimensions = "ga:week";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-4").execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
            case "monthly":
                dimensions = "ga:month";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-4").execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
        }
        data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-4").execute();
        data.rows.each { row ->
            row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
            row[3] = row[1].toLong() - row[2].toLong()
        }
        return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
    }

    public List getUniqueVisitorChart(String fromDate, String toDate, String analyticsType) {
        def timezone = AppUtil.session.timezone
        Date startTime = (fromDate + " 00:00:00").toDate()
        Date from = startTime.gmt(timezone)
        Date to = (toDate + " 23:59:59").toDate().gmt(timezone)
        TimeZone zone = TimeZone.getTimeZone("UTC")
        Analytics analytics = initialize()
        String dimensions = "ga:date" ;
        GaData data ;
        Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        switch(analyticsType) {
            case "hourly":
                dimensions = "ga:hour";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:users").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "unique.visitor"])
                break;
            case "daily":
                dimensions = "ga:date";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:users").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "unique.visitor"])
                break;
            case "weekly":
                dimensions = "ga:week";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:users").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "unique.visitor"])
                break;
            case "monthly":
                dimensions = "ga:month";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:users").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "unique.visitor"])
                break;
        }
        data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:users").setDimensions(dimensions).execute();
        data.rows.each { row ->
            row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
            row[3] = row[1].toLong() - row[2].toLong()
        }
        return getGraphData(data, ["dimension", "visits", "unique.visitor"])
    }
    public List getPageViewChart(String fromDate, String toDate, String analyticsType) {
        def timezone = AppUtil.session.timezone
        Date startTime = (fromDate + " 00:00:00").toDate()
        Date from = startTime.gmt(timezone)
        Date to = (toDate + " 23:59:59").toDate().gmt(timezone)
        TimeZone zone = TimeZone.getTimeZone("UTC")
        Analytics analytics = initialize()
        String dimensions = "ga:date" ;
        GaData data ;
        Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        switch(analyticsType) {
            case "hourly":
                dimensions = "ga:hour";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:pageviews,ga:uniquePageviews").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "total.page.views", "new.page.views"])
                break;
            case "daily":
                dimensions = "ga:date";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone), "ga:pageviews,ga:uniquePageviews").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "total.page.views", "new.page.views"])
                break;
            case "weekly":
                dimensions = "ga:week";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:pageviews,ga:uniquePageviews").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "total.page.views", "new.page.views"])
                break;
            case "monthly":
                dimensions = "ga:month";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:pageviews,ga:uniquePageviews").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "total.page.views", "new.page.views"])
                break;
        }
        data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:pageviews,ga:uniquePageviews").setDimensions(dimensions).execute();
        data.rows.each { row ->
            row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
            row[3] = row[1].toLong() - row[2].toLong()
        }
        return getGraphData(data, ["dimension", "total.page.views", "new.page.views"])
    }

    public List getPagesPerVisitChart(String fromDate, String toDate, String analyticsType) {
        def timezone = AppUtil.session.timezone
        Date startTime = (fromDate + " 00:00:00").toDate()
        Date from = startTime.gmt(timezone)
        Date to = (toDate + " 23:59:59").toDate().gmt(timezone)
        TimeZone zone = TimeZone.getTimeZone("UTC")
        Analytics analytics = initialize()
        String dimensions = "ga:date" ;
        GaData data ;
        Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        switch(analyticsType) {
            case "hourly":
                dimensions = "ga:hour";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:pageviewsPerSession").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1] - row[2]
                }
                return getGraphData(data, ["dimension", "pages.per.visit",""])
                break;
            case "daily":
                dimensions = "ga:date";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone), "ga:pageviewsPerSession").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
                    row[3] = row[1] - row[2]
                }
                return getGraphData(data, ["dimension", "pages.per.visit",""])
                break;
            case "weekly":
                dimensions = "ga:week";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:pageviewsPerSession").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1] - row[2]
                }
                return getGraphData(data, ["dimension", "pages.per.visit",""])
                break;
            case "monthly":
                dimensions = "ga:month";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:pageviewsPerSession").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1] - row[2]
                }
                return getGraphData(data, ["dimension", "pages.per.visit",""])
                break;
        }
        data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:pageviewsPerSession").setDimensions(dimensions).execute();
        data.rows.each { row ->
            row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
            row[3] = row[1] - row[2]
        }
        return getGraphData(data, ["dimension", "pages.per.visit",""])
    }

    public List getAverageVisitDurationChart(String fromDate, String toDate, String analyticsType) {
        def timezone = AppUtil.session.timezone
        Date startTime = (fromDate + " 00:00:00").toDate()
        Date from = startTime.gmt(timezone)
        Date to = (toDate + " 23:59:59").toDate().gmt(timezone)
        TimeZone zone = TimeZone.getTimeZone("UTC")
        Analytics analytics = initialize()
        String dimensions = "ga:date" ;
        GaData data ;
        Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        switch(analyticsType) {
            case "hourly":
                dimensions = "ga:hour";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:avgSessionDuration").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1] - row[2]
                }
                return getGraphData(data, ["dimension", "visits", "visit.duration"])
                break;
            case "daily":
                dimensions = "ga:date";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone), "ga:sessions,ga:avgSessionDuration").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
                    row[3] = row[1] - row[2]
                }
                return getGraphData(data, ["dimension", "visits", "visit.duration"])
                break;
            case "weekly":
                dimensions = "ga:week";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:avgSessionDuration").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1] - row[2]
                }
                return getGraphData(data, ["dimension", "visits", "visit.duration"])
                break;
            case "monthly":
                dimensions = "ga:month";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:avgSessionDuration").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1] - row[2]
                }
                return getGraphData(data, ["dimension", "visits", "visit.duration"])
                break;
        }
        data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:avgSessionDuration").setDimensions(dimensions).execute();
        data.rows.each { row ->
            row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
            row[3] = row[1] - row[2]
        }
        return getGraphData(data, ["dimension", "visits", "visit.duration"])
    }

    public List getBounceRateChart(String fromDate, String toDate, String analyticsType) {
        def timezone = AppUtil.session.timezone
        Date startTime = (fromDate + " 00:00:00").toDate()
        Date from = startTime.gmt(timezone)
        Date to = (toDate + " 23:59:59").toDate().gmt(timezone)
        TimeZone zone = TimeZone.getTimeZone("UTC")
        Analytics analytics = initialize()
        String dimensions = "ga:date" ;
        GaData data ;
        Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        switch(analyticsType) {
            case "hourly":
                dimensions = "ga:hour";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:bounceRate").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1] - row[2]
                }
                return getGraphData(data, ["dimension", "bounce.rate",""])
                break;
            case "daily":
                dimensions = "ga:date";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone), "ga:bounceRate").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
                    row[3] = row[1] - row[2]
                }
                return getGraphData(data, ["dimension", "bounce.rate",""])
                break;
            case "weekly":
                dimensions = "ga:week";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:bounceRate").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1] - row[2]
                }
                return getGraphData(data, ["dimension", "bounce.rate",""])
                break;
            case "monthly":
                dimensions = "ga:month";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:bounceRate").setDimensions(dimensions).execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1] - row[2]
                }
                return getGraphData(data, ["dimension", "bounce.rate",""])
                break;
        }
        data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:bounceRate").setDimensions(dimensions).execute();
        data.rows.each { row ->
            row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
            row[3] = row[1] - row[2]
        }
        return getGraphData(data, ["dimension", "bounce.rate",""])
    }

    public Map getPageViewsData(String fromDate, String toDate) {
        def timezone = AppUtil.session.timezone
        Date from = (fromDate + " 00:00:00").toDate().gmt(timezone)
        Date to = (toDate + " 23:59:59").toDate().gmt(timezone)
        TimeZone zone = TimeZone.getTimeZone("UTC")
        Analytics analytics = initialize()
        Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        GaData data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:pageviews,ga:uniquePageViews,ga:avgTimeOnPage,ga:bounceRate").execute();
        Map tabularData = [page_view: 0, unique_page_view: 0, avg_page_view_duration: 0, bounce_rate: 0]
        if(data.getTotalResults() > 0 ) {
            loadSingleRow(tabularData, data)
            return tabularData
        }
        else {
            return tabularData
        }
    }

    public List getData(String fromDate, String toDate, String segment, String dimension) {
        def timezone = AppUtil.session.timezone
        Date from = (fromDate + " 00:00:00").toDate().gmt(timezone)
        Date to = (toDate + " 23:59:59").toDate().gmt(timezone)
        TimeZone zone = TimeZone.getTimeZone("UTC")
        Analytics analytics = initialize()
        Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        GaData data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:users,ga:pageviews,ga:pageviewsPerSession,ga:avgSessionDuration,ga:bounceRate,ga:newUsers,ga:percentNewSessions").setSegment(segment).setDimensions(dimension).execute();
        getGraphData(data,["source", "visits", "unique_visit", "page_view", "page_per_visit", "avg_visit_duration", "bounce_rate", "new_visitor", "percentage_new_visitor"])
    }

    public List getPageData(String fromDate, String toDate, String segment, String dimension) {
        def timezone = AppUtil.session.timezone
        Date from = (fromDate + " 00:00:00").toDate().gmt(timezone)
        Date to = (toDate + " 23:59:59").toDate().gmt(timezone)
        TimeZone zone = TimeZone.getTimeZone("UTC")
        Analytics analytics = initialize()
        Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        GaData data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:pageviews,ga:uniquePageViews,ga:avgTimeOnPage,ga:bounceRate").setSegment(segment).setDimensions(dimension).execute();
        getGraphData(data,["page", "page_view", "unique_page_view", "avg_page_view_duration", "bounce_rate"])
    }

    public Map getOrganicSearchTraffic(String fromDate, String toDate) {
        def timezone = AppUtil.session.timezone
        Date from = (fromDate + " 00:00:00").toDate().gmt(timezone)
        Date to = (toDate + " 23:59:59").toDate().gmt(timezone)
        TimeZone zone = TimeZone.getTimeZone("UTC")
        Analytics analytics = initialize()
        Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        GaData data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:users,ga:pageviews,ga:pageviewsPerSession,ga:avgSessionDuration,ga:bounceRate,ga:newUsers,ga:percentNewSessions").setSegment("gaid::-5").execute();
        Map tabularData = [visits: 0, unique_visit: 0, page_view: 0, page_per_visit: 0, avg_visit_duration: 0, bounce_rate: 0, new_visitor: 0, percentage_new_visitor: 0,recurring_visitor: 0, percentage_recurring_visitor: 0]
        if(data.getTotalResults() > 0 ) {
            loadSingleRow(tabularData, data)
            tabularData.recurring_visitor = tabularData.visits.toLong() - tabularData.new_visitor.toLong()
            tabularData.percentage_recurring_visitor = 100 - tabularData.percentage_new_visitor.toDouble()
            return tabularData
        }
        else {
            return tabularData
        }
    }

    public List getOrganicSearchTrafficChart(String fromDate, String toDate, String analyticsType) {
        def timezone = AppUtil.session.timezone
        Date startTime = (fromDate + " 00:00:00").toDate()
        Date from = startTime.gmt(timezone)
        Date to = (toDate + " 23:59:59").toDate().gmt(timezone)
        TimeZone zone = TimeZone.getTimeZone("UTC")
        Analytics analytics = initialize()
        String dimensions = "ga:date" ;
        GaData data ;
        Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        switch(analyticsType) {
            case "hourly":
                dimensions = "ga:hour";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-5").execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
            case "daily":
                dimensions = "ga:date";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-5").execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
            case "weekly":
                dimensions = "ga:week";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-5").execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
            case "monthly":
                dimensions = "ga:month";
                data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-5").execute();
                data.rows.each { row ->
                    row[0] = row[0].substring(0,2)
                    row[3] = row[1].toLong() - row[2].toLong()
                }
                return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
                break;
        }
        data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:newUsers").setDimensions(dimensions).setSegment("gaid::-5").execute();
        data.rows.each { row ->
            row[0] = row[0].substring(0, 4) + "-" + row[0].substring(4, 6) + "-" + row[0].substring(6)
            row[3] = row[1].toLong() - row[2].toLong()
        }
        return getGraphData(data, ["dimension", "visits", "new.visitor", "recurring.visitor"])
    }

    public Map getSocialMediaTraffic(String fromDate, String toDate) {
        def timezone = AppUtil.session.timezone
        Date from = (fromDate + " 00:00:00").toDate().gmt(timezone)
        Date to = (toDate + " 23:59:59").toDate().gmt(timezone)
        TimeZone zone = TimeZone.getTimeZone("UTC")
        Analytics analytics = initialize()
        Map analyticsConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        GaData data = analytics.data().ga().get("ga:" + analyticsConfig.profile, from.toDatePickerFormat(false, zone),  to.toDatePickerFormat(false, zone),  "ga:sessions,ga:users,ga:pageviews,ga:pageviewsPerSession,ga:avgSessionDuration,ga:bounceRate,ga:newUsers,ga:percentNewSessions").setSegment("dynamic::ga:source=~facebook,ga:source=~StumbleUpon,ga:source=~linkedin,ga:source=~reddit,ga:source=~blogger,ga:source=~quora,ga:source=~wordpress,ga:source=~tinyurl").execute();
        Map tabularData = [visits: 0, unique_visit: 0, page_view: 0, page_per_visit: 0, avg_visit_duration: 0, bounce_rate: 0, new_visitor: 0, percentage_new_visitor: 0,recurring_visitor: 0, percentage_recurring_visitor: 0]
        if(data.getTotalResults() > 0 ) {
            loadSingleRow(tabularData, data)
            tabularData.recurring_visitor = tabularData.visits.toLong() - tabularData.new_visitor.toLong()
            tabularData.percentage_recurring_visitor = 100 - tabularData.percentage_new_visitor.toDouble()
            return tabularData
        }
        else {
            return tabularData
        }
    }
}