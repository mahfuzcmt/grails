$.extend(app, {
    global_event: $({}),
    ribbons: {
        web_design: [],
        web_content: [],
        web_commerce: [],
        web_marketing: [],
        administration: [],
        report: []
    },
    root_layout: undefined,
    tabs: {},
    hook: {
        register: function(name, func) {
            var callbacks = site.registered_hooks[name]
            if(!callbacks) {
                callbacks = site.registered_hooks[name] = []
            }
            callbacks.push(func)
        },
        fire: function(name, response, params) {
            var callbacks = site.registered_hooks[name]
            if(callbacks) {
                callbacks.every(function() {
                    response = this.apply(null, [response].concat(params))
                })
            }
        }
    },
    navigation_item_ref_create_func: {}
})

var ComponentManager = (function () {
    var componentBar, components, workspace, footer, ribbon_menu_map = {}, tabContainer, dashboardContainer

    var ribbonEvent = {
        click: function(ui_class, entity) {
            var tabId = entity.attr("type")
            var namespaces = ui_class.split(">")
            ComponentManager.openTab(ComponentManager.getRibbonData(app.ribbons[tabId], namespaces[0]), null, namespaces[1])
        },
        open: function(entity) {
            app.global_event.trigger("after-ribbon-render", [ribbon_menu_map[entity.attr("type")]])
        }
    }

    return {
        getRibbonData: function(component, ui_class) {
            return component.find("this.ui_class == '" + ui_class + "'")
        },
        showTabs: function() {
            var body = $(document.body)
            if(body.is(".dashboard-env")) {
                body.replaceClass("dashboard-env", "content-tabs-env")
                tabContainer.css({ left: '0' })
            }
        },
        showDashboard: function() {
            var body = $(document.body)
            if(body.is(".content-tabs-env")) {
                body.replaceClass("content-tabs-env", "dashboard-env")
                tabContainer.css({ left: '100%' })
                dashboardContainer.addClass("fade-in-up")
                setTimeout(function() {
                    dashboardContainer.removeClass("fade-in-up")
                }, 500)
            }
        },
        openTab: function(item, data, constructor) {
            var tab = app.Tab.getTab("tab-" + item.ui_class)
            if (!tab) {
                var processor = item.processor
                if(constructor) {
                    processor = item.processor = item.processor._super.constructor[constructor]
                    if(item.supers) {
                        //if(processor._super.constructor._super.constructor != app[item.supers[constructor]]) {
                            processor._super.constructor.inherit(app[item.supers[constructor]])
                            processor.inherit(processor._super.constructor)
                        //}
                    }
                }
                tab = new processor({
                    id: "tab-" + item.ui_class
                })
                tab.render(data)
                tab.on("close", function () {
                    if(tabContainer.children().length == 1) { //only header present
                        ComponentManager.showDashboard()
                    }
                })
                tab.setActive()
            } else if(data) {
                tab.setData(data)
                tab.setActive()
            } else if(constructor) {
                tab.setActive()
                var newTab = app.Tab.changeView(tab, item.ui_class.camelCase(false), constructor, item.supers ? item.supers[constructor] : undefined)
                item.processor = newTab.constructor
            } else {
                tab.setActive()
            }
            ComponentManager.showTabs()
        },
        init: function() {
            var adminMenu = $(".administrator")
            componentBar = $("#top-component-bar")
            components = $(".top-component-bar .component")
            footer = $("#status-bar")
            workspace = $("#workspace")

            var action_menu_entries = [
                {
                    text: $.i18n.prop("visit.site"),
                    ui_class: "visit-site",
                    href: app.siteBaseUrl,
                    target: "_blank"
                },
                {
                    text: $.i18n.prop("help"),
                    ui_class: "help",
                    href: "http://kb.webcommander.com/",
                    target: "_blank"
                },
                {
                    text: $.i18n.prop("my.account"),
                    ui_class: "my-account",
                    action: "my-account"
                },
                {
                    text: $.i18n.prop("on.screen.help"),
                    ui_class: "on-screen-help",
                    action: "on-screen-help"
                },
                {
                    text: $.i18n.prop("logout"),
                    ui_class: "logout",
                    href: app.baseUrl + "userAuthentication/logout"
                }
            ]
            var handler = function(action) {
                switch(action) {
                    case "on-screen-help":
                        DashboardManager.onScreenHelp()
                        break
                    case "my-account":
                        ComponentManager.openTab({
                            text: $.i18n.prop("my.account"),
                            processor: app.tabs.myAccount,
                            ui_class: "my-account"
                        })
                        break
                    case "switch-instance":
                        let instance = arguments[2]
                        bm.ajax({
                            controller: "dashboard",
                            action: "instanceInfo",
                            data: {instanceIdentity: instance.instanceIdentity}
                        }).done(function (resp) {
                            bm.creteCookie("ckisession", resp.ckisession)
                            location.href = `${app.baseUrl}userAuthentication/silentLogin?token=${resp.token}`
                        })
                }
            }

            bm.ajax({
                url: app.baseUrl + "user/getIsMatured",
                success: function (resp) {
                    if(resp.value == false) {
                        var callback = {
                            getStart: function(time) {
                                setTimeout(function() {
                                    if(workspace.find(".getting-started-wizard").length) {
                                        workspace.find(".dashboard-toggle").trigger("click")
                                    }
                                }, time ? time : 1000 * 3)
                            }
                        }
                        DashboardManager.onScreenHelp(callback)
                    }
                }
            })

            let renderMenu = function() {
                bm.menu(action_menu_entries, adminMenu, null, handler, "click", ["right+13 bottom+5", "right top"]).addClass("app-user-menu")
            }

            if(app.licenses) {
                bm.ajax({
                    controller: "dashboard",
                    action: "allInstances"
                }).done(function(resp) {
                    action_menu_entries.splice(action_menu_entries.length - 1, 0, {
                        text: $.i18n.prop("switch.to"),
                        ui_class: "disabled no-action-menu switch-to-instance"
                    })
                    resp.every(function () {
                        action_menu_entries.splice(action_menu_entries.length - 1, 0, {
                            text: this.name,
                            ui_class: `commander-instance ${this.instanceIdentity == app.instanceIdentity ? 'current-instance disabled' : ''}`,
                            action: "switch-instance",
                            data: this
                        })
                    })
                    renderMenu()
                })
            } else {
                renderMenu()
            }

            var hotLink = $("#status-bar .hotlink.settings")
            hotLink.on("click", function() {
                var tabItem = {
                    text: $.i18n.prop("settings"),
                    processor: app.tabs.setting,
                    ui_class: "settings"
                }
                ComponentManager.openTab(tabItem)
            })

           DashboardManager.init()

            $.each(app.ribbons, function(ui_class) {
                var entries = []
                $.each(this, function() {
                    var item = {
                        text: $.i18n.prop(this.text),
                        ui_class: this.ui_class,
                        data: this,
                        license: this.license,
                        permission: this.permission
                    }
                    if(this.views) {
                        item.sub = this.views
                    }
                    if((app.ecommerce.bool() == false) && (item.data.ecommerce == false)) {
                        entries.push(item)
                    } else if((app.ecommerce.bool() == true) && (item.data.ecommerce == true)) {
                        entries.push(item)
                    }

                    if(item.data.ecommerce == undefined) {
                        entries.push(item)
                    }
                })
                var ribbon = bm.menu(entries, components.filter("[type='" + ui_class + "']"), null, ribbonEvent, "click", ["left bottom", "left top+11"])
                ribbon_menu_map[ui_class] = ribbon
                ribbon.addClass("ribbon-menu")
            })
            components.filter("[type='inst_plugin']").click(function() {
                bm.ajax({
                    controller: "plugin",
                    action: "installVisitorListing"
                }).done(function() {
                    bm.notify("installed", "success")
                })
            })
            components.filter(".dashboard").click(function() {
                ComponentManager.showDashboard()
            })
            tabContainer = workspace.children(".content-tabs-container")
            dashboardContainer = workspace.children(".dashboard-container")
        }
    }
})()

$(function () {
    WizardManager().init(DashboardManager.postReportConfig)
    ComponentManager.init()
    app.global_event.on("operator-update", function(ev, id) {
        if(id == app.admin_id) {
            bm.ajax({
                url: app.baseUrl + "adminBase/loggedUserName",
                dataType: "html",
                success: function(resp) {
                    $(".top-component-bar .admin-menu .user-name").html(resp)
                }
            })
        }
    })

    app.global_event.on("after-general-settings-update", function(ev) {
        bm.ajax({
            url: app.baseUrl + "adminBase/loadMaxPricePrecision",
            success: function(resp) {
                app.maxPricePrecision = resp.maxPrecision
            }
        })
    })

    TaskManager.init()

    if($("#administrative-notification-wrapper").length) {
        DashboardManager.initializeAdministrativeNotification()
    }
})