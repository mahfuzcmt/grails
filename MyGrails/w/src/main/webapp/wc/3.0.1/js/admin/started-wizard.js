var WizardManager = function() {
    var workSpace = $("#workspace");
    var wizard = {};
    var tabObject = {};
    var tab;
    var dashLets = workSpace.find(".dashlet-toggle-panel");
    var startedWiz = workSpace.find(".getting-started-wizard");

    wizard.bindMiniHeader = function() {
        tab.find(".wizard-tab-header .bmui-tab-header").each(function(i) {
            $(this).append("<span class='step-icon'>" + (i + 1) + "</span>");
        })
        startedWiz.addClass("wiz-mini-header");
    }

    wizard.unBindMiniHeader = function() {
        tab.find(".wizard-tab-header .step-icon").remove();
        startedWiz.removeClass("wiz-mini-header");
    }

    tabObject.activate = function(data) {
        tab.find(".bmui-tab-body-container .group-label").html(data.newTab.find(".title-block").html());
        var panel = data.newPanel;
        if(panel.children().length && !panel.find(".get-started-content").length) {
            panel.children().remove();
            panel.append(panel.data("initialPanel"));
        }
        wizard.unBindMiniHeader();
    }

    tabObject.load = function(data) {
        var panel = data.panel;
        switch(data.index) {
            case "storeDetails":
                wizard.storeDetails(panel);
                break;
            case "email":
                wizard.email(panel);
                break;
            case "product":
                wizard.product(panel);
                break;
            case "page":
                wizard.page(panel);
                break;
            case "design":
                wizard.design(panel);
                break;
            case "tax":
                wizard.tax(panel);
                break;
            case "shipping":
                wizard.shipping(panel);
                break;
            case "paymentGateway":
                wizard.paymentGateway(panel);
                break;
            case "launchStore":
                wizard.launchStore(panel);
                break;
        }
        panel.data("initialPanel", panel.find(".get-started-content"));
    }

    wizard.softDone = function(tabKey) {
        var options = tab.obj().options;
        var steps = {storeDetails: "store_done", email: "email_done", product: "product_done", page: "page_done",
            design: "design_done", tax: "tax_done", shipping: "shipping_done", paymentGateway: "payment_done", launchStore: "launch_done"};
        var key = tabKey ? tabKey : options.active;
        bm.ajax({
            url: app.baseUrl + "dashboard/softDone",
            data: {key: steps[key]},
            success: function(resp) {
                var tabHeader = tab.find("[data-tabify-tab-id='" + key + "']");
                if(!tabHeader.find(".tool-icon.done").length) {
                    tabHeader.addClass("step-done");
                    tabHeader.append("<span class='tool-icon done'></span>");
                }
            }
        })
    }

    wizard.attachForm = function(panel, form, config) {
        var initialPanel = panel.find(".get-started-content");
        initialPanel.detach();
        panel.append(form);
        var cancelButton = $('<button type="button" class="cancel-button">'+ $.i18n.prop('cancel') +'</button>');
        var cancel = form.find(".cancel-button");
        if(cancel.length) {
            cancel.replaceWith(cancelButton)
        } else if(form.find(".submit-button").length) {
            form.find(".submit-button").after(cancelButton);
        } else {
            panel.append($("<div class='button-line'></div>").append(cancelButton));
        }
        cancelButton.on("click", function() {
            wizard.backToFront(panel);
        })
        wizard.bindMiniHeader();
        if(form.is("form")) {
            form.form({
                ajax: true,
                preSubmit: function(ajaxSetting) {
                    $.extend(ajaxSetting, {
                        success: function() {
                            if(config && config.success) {
                                config.success(form);
                            }
                            wizard.softDone();
                            wizard.backToFront(panel);
                        }
                    });
                    if(config && config.preSubmit) {
                        return config.preSubmit(form);
                    }
                }
            });
            var data = {inputClass: "medium"}
            if(panel.is("#bmui-tab-launchStore")) {
                data.stateLabel = "state",
                    data.stateName = "billing.state"
            }
            bm.countryChange(form, data);
            bm.metaTagEditor(form.find("#bmui-tab-metatag"));
        }
        form.updateUi();
    }

    wizard.backToFront = function(panel) {
        panel.children().remove();
        panel.append(panel.data("initialPanel"));
        wizard.unBindMiniHeader();
        workSpace.find(".dashboard-container").scrollTop(0);
    }

    wizard.storeDetails = function(panel) {
        panel.find(".i-am-ready").on("click", function() {
            panel.loader();
            bm.ajax({
                url: app.baseUrl + "setting/loadStoreDetails",
                dataType: "html",
                complete: function() {
                    panel.loader(false);
                },
                success: function(resp) {
                    wizard.attachForm(panel, $(resp));
                }
            });
        })
    }

    wizard.email = function(panel) {
        panel.find(".i-am-ready").on("click", function() {
            panel.loader();
            bm.ajax({
                url: app.baseUrl + "setting/loadEmailForm",
                dataType: "html",
                complete: function() {
                    panel.loader(false);
                },
                success: function(resp) {
                    wizard.attachForm(panel, $(resp));
                }
            });
        })
    }

    wizard.product = function(panel) {
        panel.find(".i-am-ready").on("click", function() {
            panel.loader();
            bm.ajax({
                url: app.baseUrl + "productAdmin/loadProductProperties",
                data: {isCombined: false, target: "create", property: "basic"},
                dataType: "html",
                complete: function() {
                    panel.loader(false);
                },
                success: function(resp) {
                    wizard.attachForm(panel, $(resp));
                }
            });
        })
    }

    wizard.page = function(panel) {
        panel.find(".i-am-ready").on("click", function() {
            panel.loader();
            bm.ajax({
                url: app.baseUrl + "pageAdmin/edit",
                dataType: "html",
                complete: function() {
                    panel.loader(false);
                },
                success: function(resp) {
                    var _tab = app.tabs.page.prototype;
                    var form = $(resp);
                    _tab.editContentLoaded(form);
                    var config = {
                        preSubmit: function(form) {
                            return _tab.editFormBeforeSubmit(form);
                        },
                        success: function(form) {
                            _tab.editFormSuccess(form);
                        }
                    }
                    wizard.attachForm(panel, form, config);
                }
            });
        })
    }

    wizard.design = function(panel) {
        panel.find(".i-am-ready").on("click", function() {
            panel.loader();
            bm.ajax({
                url: app.baseUrl + "templateAdmin/reloadTemplate",
                data: {max: 10, offset: 0, wizard: true},
                dataType: "html",
                complete: function() {
                    panel.loader(false);
                },
                success: function(resp) {
                    wizard.attachForm(panel, $(resp));
                }
            });
        })
    }

    wizard.tax = function(panel) {
        panel.find(".i-am-ready").on("click", function() {
            panel.loader();
            bm.ajax({
                url: app.baseUrl + "dashboard/loadReviewTax",
                dataType: "html",
                complete: function() {
                    panel.loader(false);
                },
                success: function(resp) {
                    wizard.attachForm(panel, $(resp));
                }
            });
        })
    }

    wizard.shipping = function(panel) {
        panel.find(".i-am-ready").on("click", function() {
            panel.loader();
            bm.ajax({
                url: app.baseUrl + "shippingAdmin/loadRightShippingPanel",
                dataType: "html",
                complete: function() {
                    panel.loader(false);
                },
                success: function(resp) {
                    wizard.attachShipping(panel, $(resp));
                }
            });
        })
    }

    wizard.attachShipping = function(panel, shipping) {
        var storePanel = panel.find(".get-started-content");
        var button = $('<div class="button-line"><button type="button" class="save shipping-profile">'+ $.i18n.prop('update') + '</button>' +
            '<button type="button" class="cancel-button">'+ $.i18n.prop('cancel') +'</button></div>');
        shipping.append(button);
        button.find(".cancel-button").on("click", function() {
            wizard.backToFront(panel);
        })
        storePanel.detach();
        panel.append(shipping);
        shipping.addClass("right-panel");
        shipping.updateUi();
        wizard.bindMiniHeader();
        var _sp = app.tabs.shipping.prototype;
        var _sProfile = $.extend({}, _sp);
        _sProfile.body = panel;
        _sProfile.attachRightPanel();
        button.find(".save").on("click", function() {
            _sProfile.saveShippingProfile(function() {
                wizard.softDone("shipping");
            });
        })
    }

    wizard.paymentGateway = function(panel) {
        panel.find(".i-am-ready").on("click", function() {
            panel.loader();
            bm.ajax({
                url: app.baseUrl + "paymentGateway/loadAppView",
                data: {wizard: true},
                dataType: "html",
                complete: function() {
                    panel.loader(false);
                },
                success: function(resp) {
                    wizard.attachPaymentGateWay(panel, $(resp));
                }
            });
        })
    }

    wizard.attachPaymentGateWay = function(panel, table) {
        var storePanel = panel.find(".get-started-content");
        var cancelButton = $('<div class="button-line"><button type="button" class="cancel-button">'+ $.i18n.prop('cancel') +'</button></div>');
        table = table.add(cancelButton);
        cancelButton.on("click", ".cancel-button", function() {
            wizard.backToFront(panel);
        })
        storePanel.detach();
        panel.append(table);
        table.updateUi();
        wizard.bindMiniHeader();

        var _pg = {};
        var _fpg = app.tabs.paymentGateway.prototype;
        _pg.init = function() {
            var _self = this;
            var tabulator = this.tabulator = bm.table(panel.find(".payment-gateway-table"), {
                url: app.baseUrl + "paymentGateway/loadAppView?wizard=true",
                menu_entries: _fpg.menu_entries,
                beforeReloadRequest: function () {
                    $.extend(this.param, {wizard: true})
                    _self.beforeReloadRequest(this.param)
                },
                afterLoad: function() {
                    _self.table.tscrollbar("content");
                    if (_self.afterTableReload) {
                        _self.afterTableReload()
                    }
                }
            });
            panel.find(".toolbar-item.reload").on("click", function() {
                _self.tabulator.reload();
            })
            tabulator.body = panel;
            tabulator.clearDirty = function(){};
            tabulator.createPanelTemplate = _fpg.createPanelTemplate;
            tabulator.renderCreatePanel = _fpg.renderCreatePanel;
            tabulator.configPaymentGateway = _fpg.configPaymentGateway;
            tabulator.onActionClick = _fpg.onActionClick;
            _self.beforeReloadRequest = function (param) {
                $.extend(param, {wizard: true});
            };
            this.table = panel.find(".payment-gateway-table table");
        }
        _pg.init();
    }

    wizard.launchStore = function(panel) {
        panel.find(".i-am-ready").on("click", function() {
            panel.loader();
            bm.ajax({
                url: app.baseUrl + "dashboard/loadPaymentForm",
                dataType: "html",
                complete: function() {
                    panel.loader(false);
                },
                success: function(resp) {
                    wizard.attachForm(panel, $(resp));
                }
            });
        })
    }

    wizard.initialize = function() {
        tab = startedWiz.find(".bmui-tab").tabify(tabObject);
        app.global_event.on('tax-profile-update tax-profile-deleted', function(e, id) {
            if(id == 1) {
                wizard.softDone("tax");
            }
        })
    }

    return {
        init: function(callback) {
            if($(".get_started_wizard_passed").length) {
                var favReport = workSpace.find(".favouriteReportChartOne, .favouriteReportChartTwo").addClass("no-record-chart");
                favReport.find(".chart-block").addClass("no-record-chart");
                var dashletWrapper = workSpace.find(".dashlet-wrapper").addClass("wizard-env");
                var wrapperY = dashletWrapper.height();
                dashletWrapper.removeAttr("style");
                dashLets.addClass("fade-in-up");
                wizard.initialize();
                workSpace.find(".dashboard-toggle").click(function() {
                    startedWiz.addClass("fade-in-up");
                    var button = $(this).find(".title");
                    workSpace.find(".dashlet-toggle-panel, .getting-started-wizard").toggle();
                    if(dashLets.is(":visible")) {
                        button.html($.i18n.prop("get.started"));
                        dashletWrapper.removeClass("wizard-env").height(wrapperY);
                        var chart = favReport.find(".chart-block");
                        if(chart.is(".no-record-chart") && callback) {
                            chart.removeClass("no-record-chart");
                            callback(favReport);
                        }
                    } else {
                        dashletWrapper.addClass("wizard-env").removeAttr("style");
                        button.html($.i18n.prop("dashboard"));
                    }
                })
            }
        }
    }
}