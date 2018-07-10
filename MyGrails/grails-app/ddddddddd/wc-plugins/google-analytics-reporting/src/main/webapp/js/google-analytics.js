 bm.onReady(app.tabs, "setting", function() {
    var _gas = app.tabs.setting.prototype;
    _gas.initGoogleAnalyticsSettings = function(data) {
        var panel = data.panel;
        var _self = this
        var form = panel.find("form");
        if(form.is(".google-analytics-authorize")) {
            form.form("prop", "preSubmit", function() {
                var params = form.serializeObject()
                var clientId = params["google_analytics.client_id"];
                var src = "https://accounts.google.com/o/oauth2/auth?client_id="
                src += clientId
                src += "&access_type=offline&approval_prompt=force"
                src += "&redirect_uri="
                src += encodeURIComponent(app.fullURL + "googleAnalytics/" + clientId + "/" + params["google_analytics.client_secret"] + "/authToken") + "&response_type=code&scope=https://www.googleapis.com/auth/analytics.readonly"
                window.open(src, "google_analytics_authorization", "fullscreen=0,height=400, menubar=0, resizable=0, scrollbars=0, status=0, toolbar=0, width=500", true)
                app.googleAnalyticsGotTheTokens = function() {
                    bm.ajax({
                        controller: "googleAnalytics",
                        action: "profileSettings",
                        dataType: "html",
                        data: {appName: params["google_analytics.application_name"]},
                        success: function(html) {
                            var formParent = form.parent()
                            formParent.html(html)
                            formParent.updateUi()
                            app.tabs.setting.prototype.onContentLoad.call(_self, data)
                        }
                    })
                }
                return false
            })
        } else {
            var success = form.form("prop", "ajax.success")
            form.form("prop", "ajax.success", success.blend(function() {
                app.global_event.trigger("google-analytics-configurations-updated")
            }))
        }
    }
});