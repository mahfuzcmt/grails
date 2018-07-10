package com.webcommander.plugin.google_analytics_reporting.controllers.admin

import com.webcommander.authentication.annotations.License
import com.webcommander.admin.ConfigService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.google_analytics_reporting.GoogleAnalyticsService
import com.webcommander.util.AppUtil
import com.webcommander.util.HttpUtil
import grails.converters.JSON
import groovy.time.TimeCategory

class GoogleAnalyticsController {
    ConfigService configService
    GoogleAnalyticsService googleAnalyticsService

    @License(required = "allow_google_analytics_reporting_feature")
    def loadAppView() {
        render view: "/plugins/google_analytics_reporting/admin/analytics"
    }

    @License(required = "allow_google_analytics_reporting_feature")
    def loadAnalytics() {
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        if(!config.access_token || !config.refresh_token || !config.profile) {
            render (view: "/plugins/google_analytics_reporting/admin/unconfigured", model: [dummy: true])
            return
        }
        String dateForm
        String dateTo
        if(params.dateForm) {
            dateForm = params.dateForm
            dateTo = params.dateTo
        } else {
            TimeZone zone = TimeZone.getTimeZone("UTC")
            Date startDate = new Date().toZone(session.timezone, TimeZone.getDefault())
            params.dateForm = dateForm = use(TimeCategory, {(startDate - 30.day).toDatePickerFormat(false, zone)})
            params.dateTo = dateTo = startDate.toDatePickerFormat(false, zone)
        }
        switch(params.property) {
            case "summary":
                Map data = googleAnalyticsService.getSummaryTotalTraffic(dateForm, dateTo)
                Map keywordData = googleAnalyticsService.getKeywordsSearchTraffic(dateForm, dateTo)
                Map directData = googleAnalyticsService.getDirectSearchTraffic(dateForm, dateTo)
                Map referralData = googleAnalyticsService.getReferralSearchTraffic(dateForm, dateTo)
                Map organicData = googleAnalyticsService.getOrganicSearchTraffic(dateForm, dateTo)
                Map paidData = googleAnalyticsService.getPaidSearchTraffic(dateForm, dateTo)
                render (view: "/plugins/google_analytics_reporting/admin/reporting/summary", model: [config: config, data: data, keywordData: keywordData, directData: directData, referralData: referralData, organicData: organicData, paidData: paidData])
                break;
            case "engagement":
                Map data = googleAnalyticsService.getSummaryTotalTraffic(dateForm, dateTo)
                render (view: "/plugins/google_analytics_reporting/admin/reporting/engagement", model: [config: config, data: data])
                break;
            case "keyword":
                Map keywordData = googleAnalyticsService.getKeywordsSearchTraffic(dateForm, dateTo)
                render (view: "/plugins/google_analytics_reporting/admin/reporting/keyword", model: [config: config, keywordData: keywordData])
                break;
            case "direct-traffic":
                Map directData = googleAnalyticsService.getDirectSearchTraffic(dateForm, dateTo)
                List dataForLandingPage = googleAnalyticsService.getData(dateForm,dateTo,"gaid::-7","ga:landingPagePath")
                render (view: "/plugins/google_analytics_reporting/admin/reporting/directTraffic", model: [config: config, directData: directData, dataForLandingPageList: dataForLandingPage])
                break;
            case "referral-search-traffic":
                Map referralData = googleAnalyticsService.getReferralSearchTraffic(dateForm, dateTo)
                List dataForSource = googleAnalyticsService.getData(dateForm,dateTo,"gaid::-8","ga:source")
                List dataForLandingPage = googleAnalyticsService.getData(dateForm,dateTo,"gaid::-8","ga:landingPagePath")
                render (view: "/plugins/google_analytics_reporting/admin/reporting/referralSearchTraffic", model: [config: config, referralData: referralData, dataForSourceList: dataForSource, dataForLandingPageList: dataForLandingPage])
                break;
            case "organic-search-traffic":
                List dataForSource = googleAnalyticsService.getData(dateForm,dateTo,"gaid::-5","ga:source")
                List dataForKeyword = googleAnalyticsService.getData(dateForm,dateTo,"gaid::-5","ga:keyword")
                List dataForLandingPage = googleAnalyticsService.getData(dateForm,dateTo,"gaid::-5","ga:landingPagePath")
                Map organicData = googleAnalyticsService.getOrganicSearchTraffic(dateForm, dateTo)
                render (view: "/plugins/google_analytics_reporting/admin/reporting/organicSearchTraffic", model: [config: config, organicData: organicData, dataForSourceList: dataForSource, dataForKeywordList: dataForKeyword, dataForLandingPageList: dataForLandingPage])
                break;
            case "paid-search-traffic":
                Map paidData = googleAnalyticsService.getPaidSearchTraffic(dateForm, dateTo)
                List dataForSource = googleAnalyticsService.getData(dateForm,dateTo,"gaid::-4","ga:source")
                List dataForKeyword = googleAnalyticsService.getData(dateForm,dateTo,"gaid::-4","ga:keyword")
                List dataForLandingPage = googleAnalyticsService.getData(dateForm,dateTo,"gaid::-4","ga:landingPagePath")
                render (view: "/plugins/google_analytics_reporting/admin/reporting/paidSearchTraffic", model: [config: config, paidData: paidData, dataForSourceList: dataForSource,  dataForKeywordList: dataForKeyword, dataForLandingPageList: dataForLandingPage])
                break;
            case "social-media-traffic":
                Map socialData = googleAnalyticsService.getSocialMediaTraffic(dateForm, dateTo)
                render (view: "/plugins/google_analytics_reporting/admin/reporting/socialMediaTraffic", model: [config: config, socialData: socialData])
                break;
            case "page-view":
                Map pageView = googleAnalyticsService.getPageViewsData(dateForm, dateTo)
                List dataForPage = googleAnalyticsService.getPageData(dateForm, dateTo, "gaid::-1","ga:pagetitle")
                render (view: "/plugins/google_analytics_reporting/admin/reporting/pageView", model: [config: config, pageView: pageView, dataForPageList: dataForPage])
                break;
            case "location":
                Map data = googleAnalyticsService.getSummaryTotalTraffic(dateForm, dateTo)
                render (view: "/plugins/google_analytics_reporting/admin/reporting/location", model: [config: config, data: data])
                break;
        }
    }

    def loadDataForKeyword() {
        String dateForm
        String dateTo
        if(params.dateForm) {
            dateForm = params.dateForm
            dateTo = params.dateTo
        } else {
            TimeZone zone = TimeZone.getTimeZone("UTC")
            Date startDate = new Date().toZone(session.timezone, TimeZone.getDefault())
            params.dateForm = dateForm = use(TimeCategory, {(startDate - 30.day).toDatePickerFormat(false, zone)})
            params.dateTo = dateTo = startDate.toDatePickerFormat(false, zone)
        }
        String searchType = params.searchType ;
        String segment = "gaid::-1";
        switch(searchType) {
            case "all":
                segment="gaid::-1";
                break;
            case "organic":
                segment="gaid::-7"
                break;
            case "paid":
                segment="gaid::-4"
                break;
        }
        List dataForKeyword = googleAnalyticsService.getData(dateForm,dateTo,segment,"ga:keyword")
        render (view: "/plugins/google_analytics_reporting/admin/reporting/keywordTableData", model: [dataForKeywordList: dataForKeyword])
    }

    def loadDataForLandingKeyword() {
        String dateForm
        String dateTo
        if(params.dateForm) {
            dateForm = params.dateForm
            dateTo = params.dateTo
        } else {
            TimeZone zone = TimeZone.getTimeZone("UTC")
            Date startDate = new Date().toZone(session.timezone, TimeZone.getDefault())
            params.dateForm = dateForm = use(TimeCategory, {(startDate - 30.day).toDatePickerFormat(false, zone)})
            params.dateTo = dateTo = startDate.toDatePickerFormat(false, zone)
        }
        String searchType = params.searchType ;
        String segment= "gaid::-1";
        switch(searchType) {
            case "all":
                segment="gaid::-1";
                break;
            case "organic":
                segment="gaid::-7"
                break;
            case "paid":
                segment="gaid::-4"
                break;
        }
        List dataForLandingPage = googleAnalyticsService.getData(dateForm, dateTo, segment, "ga:landingPagePath")
        render (view: "/plugins/google_analytics_reporting/admin/reporting/landingTableForKeyword", model: [dataForLandingPageList: dataForLandingPage])
    }

    def loadDataForLocation() {
        String dateForm
        String dateTo
        if(params.dateForm) {
            dateForm = params.dateForm
            dateTo = params.dateTo
        } else {
            TimeZone zone = TimeZone.getTimeZone("UTC")
            Date startDate = new Date().toZone(session.timezone, TimeZone.getDefault())
            params.dateForm = dateForm = use(TimeCategory, {(startDate - 30.day).toDatePickerFormat(false, zone)})
            params.dateTo = dateTo = startDate.toDatePickerFormat(false, zone)
        }
        String searchType = params.searchType ;
        String segment ;
        switch(searchType) {
            case "all":
                segment="gaid::-1";
                break;
            case "direct":
                segment="gaid::-7"
                break;
            case "searchPaid":
                segment="gaid::-4"
                break;
            case "searchOrganic":
                segment="gaid::-5"
                break;
            case "searchAll":
                segment="gaid::-1"
                break;
            case "referral":
                segment="gaid::-8"
                break;
            case "social":
                segment=""
                break;
        }
        List dataForLocation= googleAnalyticsService.getData(dateForm, dateTo, segment,"ga:country")
        render (view: "/plugins/google_analytics_reporting/admin/reporting/locationTableData", model: [dataFroLocationList: dataForLocation])
    }


    def loadGoogleAnalyticsSetting() {
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        if(config.access_token && config.application_name) {
            Map profiles = googleAnalyticsService.getProfiles()
            render view: "/plugins/google_analytics_reporting/admin/googleProfileSetting", model: [profiles: profiles, config: config]
        } else {
            render (view: "/plugins/google_analytics_reporting/admin/analyticsSetting", model: [config: config])
        }
    }

    def profileSettings() {
        def config = [
            [
                configKey: "application_name",
                type: "google_analytics",
                value: params.appName
            ]
        ]
        Map profiles = googleAnalyticsService.getProfiles()
        if(profiles.size()) {
            String profile = profiles.values()[0].keySet()[0]
            config.push([
                configKey: "profile",
                type: "google_analytics",
                value: profile
            ])
        }
        configService.update(config)
        config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_ANALYTICS)
        render view: "/plugins/google_analytics_reporting/admin/googleProfileSetting", model: [profiles: profiles, config: config]
    }

    def authToken() {
        String response = HttpUtil.doPostRequest("https://accounts.google.com/o/oauth2/token", "code=" + params.code.encodeAsURL() + "&grant_type=authorization_code&redirect_uri=" + (app.baseUrl() + "googleAnalytics/" + params.clientId + "/" + params.secret + "/authToken").encodeAsURL() + "&scope=" + "https://www.googleapis.com/auth/analytics.readonly".encodeAsURL() + "&client_id=" + params.clientId + "&client_secret=" + params.secret)
        Map responseMap = JSON.parse(response);
        List config = [
            [
                configKey: "access_token",
                type: "google_analytics",
                value: responseMap.access_token
            ],
            [
                configKey: "refresh_token",
                type: "google_analytics",
                value: responseMap.refresh_token
            ],
            [
                configKey: "client_id",
                type: "google_analytics",
                value: params.clientId
            ],
            [
                configKey: "client_secret",
                type: "google_analytics",
                value: params.secret
            ]
        ]
        configService.update(config)
        render view: "/plugins/google_analytics_reporting/admin/tokenCollectRequest"
    }
}