var DashboardManager = {
    init: function() {
        var _self = this, workspace = this.workspace = $("#workspace"),  dashboardContainer = this.dashboardContainer = workspace.find(".dashboard-container")
        workspace.find(".dashlet-wrapper").on("click", ".dashlet-ribbon", function() {
            ComponentManager.openTab(ComponentManager.getRibbonData(app.ribbons[$(this).attr("tabId")], $(this).attr("ui_class")))
        });

        dashboardContainer.updateUi();
        dashboardContainer.scrollbar();
        dashboardContainer.find(".dashlet.latestStat .customer-data-table").scrollbar();
        workspace.on("click", ".header > .icon, .dashlet-ribbon > .icon", function() {
            var clickedDashlet = $(this).attr("data-name")
            if(clickedDashlet == "quickReport") {
                var quickReport = workspace.find(".dashlet-quick-report");
                DashboardManager.quickReportConfig(quickReport);
            } else if(clickedDashlet == "webCommerce") {
                var webCommerce = workspace.find(".dashlet-webcommerce");
                DashboardManager.webCommerceConfig(webCommerce);
            } else if(clickedDashlet == "webContentAndDesign") {
                var webContentAndDesign = workspace.find(".dashlet-web-content-design");
                DashboardManager.webContentAndDesignConfig(webContentAndDesign);
            } else if(clickedDashlet == "administrationAndMarketing") {
                var administrationAndMarketing = workspace.find(".dashlet-administration-webmarketing");
                DashboardManager.administrationAndMarketingConfig(administrationAndMarketing);
            } else if(clickedDashlet == "latestStat") {
                var latest = workspace.find(".dashlet-latest-statistics").closest(".dashlet");
                DashboardManager.latestReoprtConfig(latest);
                latest.updateUi();
            } else if(clickedDashlet == "favouriteReportChartOne" || clickedDashlet == "favouriteReportChartTwo"){
                var dashletId = $(this).attr("dashlet-id");
                var placeholder = $(this).closest(".dashlet");
                DashboardManager.favouriteReportConfig($.i18n.prop("config.favourite.report"), placeholder, dashletId);
            }
        });
        app.global_event.on("order-create order-status-change payment-added payment-refunded customer-create delete-customer", function() {
            _self.reloadQuickReport();
        })
        var reportDashlets = workspace.find(".favouriteReportChartOne, .favouriteReportChartTwo")

    },
    postReportConfig: function(dashlets) {
        dashlets.find(".chart-block").chart()
    },
    quickReportConfig: function(quickReport) {
        var beforeSubmit  = function() {
            var checkedItem = this.find(":checked").length;
            if(checkedItem != 5) {
                bm.notify($.i18n.prop("please.select.five.elements"), "alert")
                return false
            }
            return true
        };
        DashboardManager.configDashlet([], $.i18n.prop("config.quick.report"), quickReport , "quickReport", "dashboard/quickReport", beforeSubmit)

    },
    reloadQuickReport: function() {
        var _self = this;
        bm.ajax({
            url: app.baseUrl + "dashboard/quickReport",
            dataType: "html",
            success: function(resp) {
                _self.dashboardContainer.find(".dashlet-quick-report").replaceWith(resp)
            }
        })
    },
    webCommerceConfig : function(placeHolder) {
        var data = [];
        app.ribbons.web_commerce.every(function() {
            var tempdata = {}
            tempdata.uiClass = this.ui_class
            tempdata.tabId = "web_commerce"
            data.push(JSON.stringify(tempdata));
        });

        DashboardManager.configDashlet(data, $.i18n.prop("config.web.commerce"), placeHolder , "webCommerce", "dashboard/webCommerce")
    },
    webContentAndDesignConfig : function(placeHolder) {
        var data = [];
        app.ribbons.web_content.every(function() {
            var tempdata = {}
            tempdata.uiClass = this.ui_class
            tempdata.tabId = "web_content"
            data.push(JSON.stringify(tempdata));
        });
        app.ribbons.web_design.every(function() {
            var tempdata = {}
            tempdata.uiClass = this.ui_class
            tempdata.tabId = "web_design"
            data.push(JSON.stringify(tempdata));
        });

        DashboardManager.configDashlet(data, $.i18n.prop("config.web.content.and.design"), placeHolder , "webContentAndDesign", "dashboard/webContentAndDesign")
    },
    administrationAndMarketingConfig : function(placeHolder) {
        var data = [];
        app.ribbons.administration.every(function() {
            var tempdata = {}
            tempdata.uiClass = this.ui_class
            tempdata.tabId = "administration"
            data.push(JSON.stringify(tempdata));
        });
        app.ribbons.web_marketing.every(function() {
            var tempdata = {}
            tempdata.uiClass = this.ui_class
            tempdata.tabId = "web_marketing"
            data.push(JSON.stringify(tempdata));
        });

        DashboardManager.configDashlet(data, $.i18n.prop("config.administration.and.web.marketing"), placeHolder , "administrationAndMarketing", "dashboard/administrationAndMarketing")
    },
    configDashlet : function(data, title, place, configType, reloadUrl, beforeSubmits) {
        bm.editPopup(app.baseUrl + "dashboard/configPopUp", title, "", {ribbons: data, configType: configType}, {
            width: 400,
            beforeSubmit : beforeSubmits ? beforeSubmits : function() {
                var checkedItem = this.find(":checked").length;
                if(checkedItem != 6) {
                    bm.notify($.i18n.prop("please.select.six.elements"), "alert")
                    return false
                }
                return true
            },
            success: function () {
                place.loader()
                bm.ajax({
                    url: app.baseUrl + reloadUrl,
                    dataType: "html",
                    data: {configType : configType},
                    success: function(resp) {
                        place.replaceWith(resp);
                        place.loader(false)
                    }
                });
            }
        })
    },
    latestReoprtConfig: function(placeholder) {
        bm.editPopup(app.baseUrl + "dashboard/configLatestTablePopUp", $.i18n.prop("config.latest.table"), "", {}, {
            width: 400,
            success: function () {
                placeholder.loader()
                bm.ajax({
                    url: app.baseUrl + "dashboard/latestStatUpdate",
                    dataType: "html",
                    success: function(resp) {
                        placeholder.replaceWith(resp);
                        placeholder.find(".content").scrollbar();
                        placeholder.loader(false)
                    }
                });
            }
        })
    },
    favouriteReportConfig: function(title, place, dashletId) {
        bm.editPopup(app.baseUrl + "dashboard/configFavouriteReportPopUp", title, "", {dashletId: dashletId}, {
            width: 400,
            success: function () {
                place.loader()
                bm.ajax({
                    controller: "commanderReporting",
                    action: "renderFavouriteReportChart",
                    dataType: "html",
                    data: {dashlet: dashletId},
                    success: function(resp) {
                        place.find(".dashlet-favourite-report").replaceWith(resp);
                        place.loader(false)
                        DashboardManager.postReportConfig(place)
                    }
                });
            }
        })
    },
    onScreenHelp: function (callback) {
        ComponentManager.showDashboard()
        var workspace = $("#workspace");
        if(workspace.find(".getting-started-wizard").is(":visible")) {
            workspace.find(".dashboard-toggle").trigger("click");
        }
        var topComponentBar = $("#top-component-bar")
        var count = 1;
        var position = []
        position[0] = workspace.find(".dashboard-container").find(".dashboard-toggle").find(".title")
        position[1] = topComponentBar.find(".component-navigators").find(".administration")
        position[2] = workspace.find(".webContentAndDesign").find(".header .icon")
        position[3] = topComponentBar.find(".component-navigators").find(".web-content")
        position[4] = topComponentBar.find(".component-navigators").find(".web-commerce")
        position[5] = topComponentBar.find(".component-navigators").find(".web-design")
        position[6] = topComponentBar.find(".administrator")
        position[7] = topComponentBar.find(".administrator")
        var atBottom = "100",
            atRight = "-100"
        return bm.customTooltip(app.baseUrl + "dashboard/loadOnScreenHelp", count, position, atBottom, atRight, callback)
    },
    initializeAdministrativeNotification: function() {
        var wrapper = $("#administrative-notification-wrapper")
        if(!wrapper.find(".close").length) {
            var closer = $("<span class='tool-icon close'></span>").appendTo(wrapper)
            closer.click(function() {
                wrapper.remove()
            })
            wrapper.scrollbar({})
        } else {
            wrapper.scrollbar("update")
        }
    }
}