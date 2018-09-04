package com.webcommander.plugin.google_analytics_reporting

class UrlMappings {
    static mappings = {
        "/googleAnalytics/$clientId/$secret/authToken"(controller: "googleAnalytics", action: "authToken")
    }
}

