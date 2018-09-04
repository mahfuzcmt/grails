app.tabs.plugin = function (configs) {
    this.constructor_args = arguments;
    this.text = $.i18n.prop("plugin");
    this.tip = $.i18n.prop("manage.plugins");
    this.ui_class = "plugin";
    app.tabs.plugin._super.constructor.apply(this, arguments);
    this.sectionList = {
        myPackage: { ajax_url: app.baseUrl + "plugin/myPackagePlugins"},
        allPackage: {ajax_url: app.baseUrl + "plugin/allPackagePlugins"}
    }
    this.ajax_url = app.baseUrl + "plugin/loadAppView";
}
var _p = app.tabs.plugin.inherit(app.MultiSectionTab);

app.ribbons.administration.push({
    text: $.i18n.prop("plugin"),
    processor: app.tabs.plugin,
    ui_class: "plugin"
});

_p.init = function () {
    app.tabs.plugin._super.init.call(this)
}

app.global_event.on('after-ribbon-render', function (e, ribbonBar) {
    if (app.isPermitted("plugin.view.list")) {
        ribbonBar.enable("plugin");
    } else {
        ribbonBar.disable("plugin");
    }
});
_p.initMyPackageSection = function(section) {
    var _self = this;
    _self.myPackPlugins = section.find(".plugin");
    section.find(".content").scrollbar()
    section.find("button.install").on("click", function() {
        var $this = $(this)
        _self.install($this.attr("plugin-id"), $this.siblings(".name").text())
    });
    section.find("button.uninstall").on("click", function() {
        var $this = $(this)
        _self.uninstall({
            id: $this.attr("plugin-id"),
            name: $this.siblings(".name").text()
        })
    });
    var searchBox = section.find(".search-text");
    function filter() {
        var searchText = searchBox.val()
        _self.myPackPlugins.each(function() {
            var $this = $(this), name = $this.find('.name').text().trim();
            if(name.match(new RegExp(searchText, "i"))) {
                $this.show()
            } else {
                $this.hide()
            }
        });
    }
    searchBox.ichange(800, function() {
       filter()
    });
    section.find(".search-form").form({
        preSubmit: function() {
            filter()
            return false
        }
    })
};

_p.initAllPackageSection = function(section) {
    var _self = this
    section.find(".tab-body .plugins").scrollbar()
    section.find(".update-package").on("click", function() {
        var $this = $(this);
        _self.upgradeDowngrade($this.attr("pack-id"), $this.is(".upgrade-package"))
    });
    section.find("#bmui-tab-additional-plugins button.install").on("click", function() {
        var $this = $(this)
        _self.install($this.attr("plugin-id"), $this.siblings(".name").text())
    });
}

app.tabs.plugin.reloadAfterRestart = function(time) {
    $("body").loader();
    function isRestarted(callback) {
        try {
            bm.ajax({
                url: app.baseUrl + "app/isRestarted",
                data: {time: time},
                success: function(resp) {
                    if(resp.isRestarted) {
                        callback();
                    } else {
                        setTimeout(function() {
                            isRestarted(callback)
                        }, 10000);
                    }
                },
                error: function() {
                    setTimeout(function() {
                        isRestarted(callback)
                    }, 10000);
                }
            })
        } catch(ex) {
            setTimeout(function() {
                isRestarted(callback)
            }, 10000);
        }

    }
    isRestarted(function() {
        $("body").loader(false);
        window.location.reload();
    });
}

_p.uninstall = function(data) {
    var _self = this;
    function uninstall(uninstallDependent) {
        bm.ajax({
            url: app.baseUrl + "plugin/uninstall",
            data: {id: data.id, uninstall_dependent: uninstallDependent},
            success: function() {
                if(app.isSoftInstallUninstallEnable == "true") {
                    bm.confirm($.i18n.prop("server.restart.required"), [
                        {
                            clazz: "restart",
                            text: "restart",
                            handler: function() {
                                bm.ajax({
                                    url: app.baseUrl + "plugin/restart",
                                    data: {reason: "After uninstall plugin " + data.name},
                                    success: function (resp) {
                                        app.tabs.plugin.reloadAfterRestart.call(_self, new Date().getTime());
                                    }
                                })
                            }
                        },
                        {
                            clazz: "no",
                            text: "no",
                            handler: function(){}

                        }
                    ]);
                } else {
                    bm.confirm($.i18n.prop("have.to.reload.changes.effective"), function() {
                        window.location.href = app.baseUrl + "admin"
                    }, function() {
                        _self.reload("myPackage")
                    })
                }
            },
            error: function(xhr, status, resp) {
                if(resp.dependents == "yes") {
                    bm.confirm($.i18n.prop("plugin.have.dependent.plugins", [data.name]), function() {
                        uninstall("yes");
                    }, function() {})
                }
            }
        })
    }
    bm.confirm($.i18n.prop("confirm.uninstall.plugin", [data.name]), uninstall, function() {});
}

_p.install = function(identifier, name) {
    var _self = this
    bm.confirm($.i18n.prop("confirm.install.plugin", [name]), function() {
        var waitPop = bm.waitPopup();
        bm.ajax({
            url: app.baseUrl + "plugin/install",
            data: {identifier: identifier, name: name},
            success: function() {
                waitPop.close()
                bm.confirm($.i18n.prop("have.to.reload.changes.effective"), function() {
                    window.location.href = app.baseUrl + "admin"
                }, function() {
                    _self.reload("myPackage")
                })
            },
            complete: function () {
                waitPop.close()
            }
        })
    }, function() {})
};

_p.upgradeDowngrade = function(packageId, isUpgrade, reload) {
    ComponentManager.openTab({
        text: $.i18n.prop("my.account"),
        processor: app.tabs.myAccount,
        ui_class: "my-account"
    });
};