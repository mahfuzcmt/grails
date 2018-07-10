app.tabs.myAccount = function () {
    this.constructor_args = arguments;
    this.text = $.i18n.prop("my.account");
    this.tip = $.i18n.prop("my.account");
    this.ui_class = "my-account";
    app.tabs.myAccount._super.constructor.apply(this, arguments);
    this.ajax_url = app.baseUrl + "myAccount/loadAppView";
};
var _m = app.tabs.myAccount.inherit(app.MultiTab);

_m.init = function () {
    app.tabs.myAccount._super.init.call(this)
};

_m.onContentLoad = function (data) {
    if (typeof app.tabs.myAccount.tabInitFunctions[data.index] == "function") {
        app.tabs.myAccount.tabInitFunctions[data.index].call(this, data.panel, data.tab);
    }
};

_m.notEditable = true;
app.tabs.myAccount.tabInitFunctions = {};



/********** Manage ********************/

app.tabs.myAccount.tabInitFunctions.manage = function (panel) {
    new app.tabs.myAccount.manage(this, panel)

};
app.tabs.myAccount.manage = function (parentTab, panel) {
    this.parentTab = parentTab;
    this.body = panel;
    this.tool = panel.tool;
    var menu_entries = [
        {
            text: $.i18n.prop("Manage"),
            ui_class: "edit",
            action: "manage"
        }
    ];
    $.each(panel, function(ind, elm){
        this.tabulator = bm.table($(elm), $.extend({
            menu_entries: menu_entries
        }));
        this.tabulator.onActionClick = function (action, data) {
            switch (action) {
                case "manage":
                    editContent();
                    break;
            }
        }
    });

};

editContent = function() {
    var tabId = "my-account-content";
    var tab = app.Tab.getTab(tabId);
    if(!tab) {
        tab = new app.tabs.manageContent({
            id: tabId
        });
        tab.render();
    }
    tab.setActive();
};


/**********Account Details ********************/

app.tabs.myAccount.tabInitFunctions.accountDetails = function (panel) {
    new app.tabs.myAccount.accountDetails(this, panel).init()

};
app.tabs.myAccount.accountDetails = function (parentTab, panel) {
    this.parentTab = parentTab;
    this.body = panel;
    this.tool = panel.tool;
    this.sectionList = {
        info: {ajax_url: app.baseUrl + "myAccount/accountInfo"},
        websiteDetails: {ajax_url: app.baseUrl + "myAccount/websiteDetails"},
        billingAddress: {ajax_url: app.baseUrl + "myAccount/billingAddress"}
    }
};

var _ad = app.tabs.myAccount.accountDetails.inherit(app.MultiSectionView);
_ad.initInfoSection = function (section) {
    var _self = this;
    section.find(".edit-info").on("click", function () {
        _self.editAccountInfo()
    });
};

_ad.editAccountInfo = function () {
    var _self = this;
    this.parentTab.renderCreatePanel(app.baseUrl + "myAccount/accountInfoEdit", $.i18n.prop('edit.account.details'), null, {}, {
        success: function () {
            _self.reload("info");
        }
    })
};

/** Purchase History **/
app.tabs.myAccount.tabInitFunctions.purchaseHistory = function (panel) {
    new app.tabs.myAccount.purchaseHistory(this, panel).init()
};

(function () {
    app.tabs.myAccount.purchaseHistory = function (parentTab, panel) {
        this.parentTab = parentTab;
        this.body = panel;
        this.tool = panel.tool;
        this.ajax_url = app.baseUrl + "myAccount/loadPurchaseHistory"
    };
    var _st = app.tabs.myAccount.purchaseHistory.inherit(app.SingleTableView);
    var _super = app.tabs.myAccount.purchaseHistory._super
})();

/** Invoice **/
app.tabs.myAccount.tabInitFunctions.invoice = function (panel) {
    new app.tabs.myAccount.invoice(this, panel).init()
};

(function () {
    app.tabs.myAccount.invoice = function (parentTab, panel) {
        this.parentTab = parentTab;
        this.body = panel;
        this.tool = panel.tool;
        this.ajax_url = app.baseUrl + "myAccount/loadInvoice"
    };
    var _st = app.tabs.myAccount.invoice.inherit(app.SingleTableView);
    var _super = app.tabs.myAccount.invoice._super
})();

/** Subscription **/
app.tabs.myAccount.tabInitFunctions.subscription = function (panel) {
    var _self = this;
    panel.find(".content").scrollbar();
    panel.find(".purchase-package").on("click", function () {
        var $this = $(this);
        _self.purchase($this.attr("package-id"), $this.siblings(".name").text())
    })
};

_m.purchase = function (packageId, name) {
    var _self = this;
    bm.editPopup(app.baseUrl + "myAccount/cardInfoPopup", $.i18n.prop('purchase.package'), name, {packageId: packageId}, {
        success: function () {
            bm.confirm($.i18n.prop("have.to.reload.changes.effective"), function () {
                window.location.href = app.baseUrl + "admin"
            }, function () {
                _self.reload("packageDetails")
            })
        }
    })
};

/**  Customer Support  **/
app.tabs.myAccount.tabInitFunctions.customerSupport = function (panel) {
    new app.tabs.myAccount.customerSupport(this, panel).init()
};

(function () {
    function getMessageForm(action) {
        return $('<form class="create-edit-form" action="' + app.baseUrl + action + '" method="post"><div class="form-row">' +
            '<textarea name="message" validation="required" class="wceditor" toolbar-type="super_simple"></textarea>' +
            '</div><div class="form-row btn-row"><button type="submit">Submit</button></div></form>');
    }

    function attachEvent() {
        var _self = this;
        this.body.find('.reply-message').on('click', function () {
            var $this = $(this), messageBlock = $this.parents('.message-thread');
            _self.reply(messageBlock)
        });
        this.body.find('.expander').on('click', function () {
            $(this).parents('.message-thread').toggleClass('expanded');
        })
    }

    app.tabs.myAccount.customerSupport = function (parentTab, panel) {
        this.parentTab = parentTab;
        this.body = panel;
        this.tool = panel.tool;
        this.ajax_url = app.baseUrl + "myAccount/loadCustomerSupport"
    };
    var _t = app.tabs.myAccount.customerSupport.inherit(app.SingleTableView);
    var _super = app.tabs.myAccount.customerSupport._super;
    _t.isNonTable = true;
    _t.afterTableReload = attachEvent;

    _t.init = function () {
        var _self = this;
        _super.init.apply(this, arguments);
        this.body.find('.create-new-message').on("click", function () {
            _self.create();
        });
        attachEvent.call(this)
    };
    _t.create = function () {
        if (this.createForm) return;
        var _self = this, form = getMessageForm('myAccount/addSupportMessage');
        _self.body.find('.body').prepend(form.updateUi());
        this.createForm = form;
        form.form({
            ajax: {
                success: function () {
                    form.remove();
                    _self.createForm = null;
                    _self.reload()
                }
            }
        })

    };

    _t.reply = function (messageBlock) {
        if (messageBlock.find('.create-edit-form').length > 0) return;
        var _self = this, form = getMessageForm('myAccount/addSupportMessageReply');
        messageBlock.find('.message-replies').append(form.updateUi());
        form.form({
            preSubmit: function (conf) {
                conf.data = {messageId: messageBlock.attr('message-id')}
            },
            ajax: {
                success: function () {
                    form.remove();
                    _self.reload()
                }
            }
        })
    }
})();

/* Custom Project */
app.tabs.myAccount.tabInitFunctions.customProject = function (panel) {
    new app.tabs.myAccount.customProject(this, panel).init()
};

(function () {
    app.tabs.myAccount.customProject = function (parentTab, panel) {
        this.parentTab = parentTab;
        this.body = panel;
        this.tool = panel.tool;
        this.ajax_url = app.baseUrl + "myAccount/loadCustomProject"
    };
    var _p = app.tabs.myAccount.customProject.inherit(app.SingleTableView);
    var _super = app.tabs.myAccount.customProject._super;

    function attachEvent() {
        var _self = this;
        this.body.find(".manage-project").on("click", function () {
            _self.manage($(this).attr("project-id"))
        })
    }

    _p.init = function () {
        _super.init.apply(this, arguments);
        var _self = this;
        this.body.find(".create-new-project").on("click", function () {
            _self.save()
        });
        this.parentTab.on_global("custom-project-create", function () {
            _self.reload()
        });
        attachEvent.call(this)
    };

    _p.afterTableReload = function () {
        attachEvent.call(this)
    };
    _p.save = function () {
        bm.ajax({
            url: app.baseUrl + "myAccount/saveCustomProject",
            success: function () {
                app.global_event.trigger("custom-project-create")
            }
        })
    };

    _p.manage = function (id) {
        new app.editCustomProject(this.parentTab, id).init()
    }
})();

/**
 * Custom Project Editor
 * */
app.editCustomProject = function (appTab, projectId) {
    this.itemTab = appTab;
    this.projectId = projectId;
    app.editCustomProject._super.constructor.call(this, arguments);
};

var _e = app.editCustomProject.inherit(app.MultiTab);
_e.notEditable = true;
var _super = app.editCustomProject._super;

_e.init = function () {
    var _self = this;
    _self.itemTab.renderCreatePanel(
        app.baseUrl + "myAccount/loadCustomProjectEditor", null, null, {projectId: _self.projectId}, {
            createPanelTemplate: $('<div class="embedded-edit-form-panel create-panel"><div class="header"><span class="toolbar toolbar-right"><span class="tool-group toolbar-btn cancel">' + $.i18n.prop("close") + '</span></span></div><div class="body"></div></div>'),
            ajax: {
                show_success_status: false
            },
            submit_n_cancle: false,
            content_loaded: function (template) {
                _self.body = this;
                _self.header = this.find(".header");
                _super.init.apply(_self, arguments);
            }
        });
};

_e.onContentLoad = function (data) {
    if (typeof app.editCustomProject.tabInitFunctions[data.index] == "function") {
        app.editCustomProject.tabInitFunctions[data.index].call(this, data.panel, data.tab);
    }
};

var initFunctions = app.editCustomProject.tabInitFunctions = {};
initFunctions.overview = function (panel) {
    panel.find("td.action-column .approve").on("click", function () {
        var $this = $(this);
        if (!$this.is(".approved")) {
            bm.ajax({
                url: app.baseUrl + "myAccount/approveProjectMilestone",
                data: {id: $this.attr("entity-id")},
                success: function () {
                    $this.addClass("approved").removeClass("approve")
                }
            });
        }
    })
};

initFunctions.projectDetails = function (panel) {
    var detailsId = panel.find("[name=detailsId]").val()
    panel.find(".add-file").on("click", function () {
        bm.editPopup(app.baseUrl + "myAccount/editProjectDetailsFile", $.i18n.prop("add.file"), null, {detailsId: detailsId}, {
            success: function (resp) {
                panel.find("files").append('<span class="file ' + resp.ext + '">' + resp.name + '</span>')
            }
        })
    });
}

initFunctions.projectFiles = function (panel) {
    var projectId = panel.find("[name=projectId]").val()
    panel.find(".add-file").on("click", function () {
        bm.editPopup(app.baseUrl + "myAccount/editProjectProjectFiles", $.i18n.prop("add.files"), null, {projectId: projectId}, {
            success: function (resp) {
                panel.reload()
            }
        })
    });
};

initFunctions.projectMessages = function(panel) {
    new app.editCustomProject.ProjectMessages(this, panel).init()
};
(function () {
    function getMessageForm(action) {
        return $('<form class="create-edit-form" action="' + app.baseUrl + action + '" method="post"><div class="form-row">' +
            '<textarea name="message" validation="required" class="wceditor" toolbar-type="super_simple"></textarea>' +
            '</div><div class="form-row btn-row"><button type="submit">Submit</button></div></form>');
    }

    function attachEvent() {
        var _self = this;
        this.body.find('.reply-message').on('click', function () {
            var $this = $(this), messageBlock = $this.parents('.message-thread');
            _self.reply(messageBlock)
        });
        this.body.find('.expander').on('click', function () {
            $(this).parents('.message-thread').toggleClass('expanded');
        })
    }

    app.editCustomProject.ProjectMessages = function (parentTab, panel) {
        this.parentTab = parentTab;
        this.projectId = parentTab.projectId;
        this.body = panel;
        this.tool = panel.tool;
        this.ajax_url = app.baseUrl + "myAccount/loadCustomProjectProperties?property=projectMessages&id=" + this.projectId
    };
    var _t = app.editCustomProject.ProjectMessages.inherit(app.SingleTableView);
    var _super = app.editCustomProject.ProjectMessages._super;
    _t.isNonTable = true;
    _t.afterTableReload = attachEvent;

    _t.init = function () {
        var _self = this;
        _super.init.apply(this, arguments);
        this.body.find('.create-new-message').on("click", function () {
            _self.create();
        });
        attachEvent.call(this)
    };

    _t.create = function () {
        if (this.createForm) return;
        var _self = this, form = getMessageForm('myAccount/addProjectMessage');
        _self.body.find('.body').prepend(form.updateUi());
        this.createForm = form;
        form.form({
            preSubmit: function (conf) {
                conf.data = {projectId: _self.projectId}
            },
            ajax: {
                success: function () {
                    form.remove();
                    _self.createForm = null;
                    _self.reload()
                }
            }
        })

    };

    _t.reply = function (messageBlock) {
        if (messageBlock.find('.create-edit-form').length > 0) return;
        var _self = this, form = getMessageForm('myAccount/addProjectMessageReply');
        messageBlock.find('.message-replies').append(form.updateUi());
        form.form({
            preSubmit: function (conf) {
                conf.data = {messageId: messageBlock.attr('message-id'), projectId: _self.projectId}
            },
            ajax: {
                success: function () {
                    form.remove();
                    _self.reload()
                }
            }
        })
    }
})();

(function() {
    var template = '<div class="bmui-stl-entry"><div class="table-actions action-column"></div><div class="name-column"></div></div>',
        actionTemplate = '<span class="tool-icon edit" title="Edit"></span></div>', actions = ["add", "edit", "remove"],
        treeWrap = null, projectId;

    function attachEvent(node) {
        var nameColumn = node.find(".name-column"), input = $('<input class="name" type="text">')
        node.find(".tool-icon").on("click", function() {
            var $this= $(this), action = $this.attr("action")
            switch (action) {
                case "add":
                    createNode(node)
                    break
                case "remove":
                    node.parent().remove()
                    break
                case "edit":
                    if($this.is(".apply")) {
                        if(input.val().trim() == "") {
                            input.addClass("error-input")
                            setTimeout(function() {
                                input.removeClass("error-input")
                            }, 5000)
                        } else {
                            nameColumn.text(input.val().trim())
                            $this.removeClass("apply").addClass("edit")
                        }
                    } else {
                        nameColumn.html(input)
                        $this.removeClass("edit").addClass("apply")
                    }

            }
        })
    }

    function addNode(name, parent) {
        var node = $(template), actionHtml = ''
        actions.forEach(function(action) {
            actionHtml += '<span class="tool-icon ' + action + '" title="' + $.i18n.prop(action) + '" action="' + action + '"></span>'
        })
        node.find('.action-column').html(actionHtml);
        node.find('.name-column').text(name)
        treeWrap.sortableTreeList("addHandle", node, parent);
       attachEvent(node)
    }

    function createNode(parent) {
        var node = $(template), input = $('<input class="name" type="text">');
        node.find('.action-column').html('<span class="tool-icon apply" title="Remove"></span><span class="tool-icon remove" title="Remove"></span>')
        node.addClass("new-node-creator-row").find(".name-column").html(input)
        if(!parent) {
            treeWrap.append(node);
        } else {
            parent.parent().append(node)
        }
        node.find(".apply").on("click", function() {
            if(input.val().trim() == "") {
                input.addClass("error-input")
                setTimeout(function() {
                    input.removeClass("error-input")
                }, 5000)
            } else {
                node.remove()
                addNode(input.val(), parent)
            }
        });
        node.find(".remove").on("click", function() {
            node.remove()
        })
    }

    function getSaveData(nodeWrap) {
        var nodesData = [], nodes = nodeWrap.find(">.bmui-stl-entry-container");
        if(nodes.length) {
            nodes.each(function() {
                var $this = $(this), subContainer = $this.find(">.bmui-stl-sub-container"), entry = $this.find(">.bmui-stl-entry > .name-column");
                nodesData.push({
                    name: entry.text().trim(),
                    children: getSaveData(subContainer)
                })
            });
        }
        return nodesData
    }

    function save(callback) {
        var data = JSON.stringify(getSaveData(treeWrap))
        bm.ajax({
            url: app.baseUrl + "myAccount/saveProjectSitemap",
            data: {sitemap: data, projectId: projectId},
            success: function() {
                callback()
            }
        })
    }

    initFunctions.sitemap = function(panel) {
        var _self = this;
        projectId = this.projectId
        panel.save = save;
        panel.find(".save").on("click", function() {
            save(function() {
                panel.clearDirty()
            })
        });
        treeWrap =  panel.find(".scroll-item-wrapper");
        treeWrap.sortableTreeList({
            change: function(info) {
                panel.setDirty();
            }
        });
        panel.find(".create-new").on("click", function() {
            createNode()
        })

    }
})();
