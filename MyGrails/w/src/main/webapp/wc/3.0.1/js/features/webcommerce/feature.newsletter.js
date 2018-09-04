app.tabs.newsletterView = function () {
    this.constructor_args = arguments;
    this.text = $.i18n.prop("newsletters");
    this.tip = $.i18n.prop("manage.newsletters");
    this.ui_class = "newsletters";
    app.tabs.newsletterView._super.constructor.apply(this, arguments);
};

var _nv = app.tabs.newsletterView.inherit(app.SingleTableTab);

(function () {
    function attachEvents() {
        var _self = this;
    }

    _nv.init = function () {
        app.tabs.newsletterView._super.init.call(this);
        attachEvents.call(this)
    }
})();

_nv.onSwitchMenuClick = function(type) {
    app.Tab.changeView(this, "newsletterView", type);
}

app.tabs.newsletterView.newsletter = function () {
    app.tabs.newsletterView.newsletter._super.constructor.apply(this, arguments);
}

var _n = app.tabs.newsletterView.newsletter.inherit(app.tabs.newsletterView);

(function () {
    function attachEvents() {
        var _self = this;
        this.body.find(".toolbar .create").on("mousedown", function () {
            _self.createNewsletter();
        });
        this.on("close", function () {
            app.tabs.newsletterView.newsletter.tab = null;
        });
    }

    _n.init = function () {
        app.tabs.newsletterView.newsletter._super.init.call(this);
        app.tabs.newsletterView.newsletter.tab = this
        attachEvents.call(this);
    }
    app.global_event.on('after-ribbon-render', function (e, ribbonBar) {
        if (app.isPermitted("newsletter.view.list")) {
            ribbonBar.enable("newsletterView");
        } else {
            ribbonBar.disable("newsletterView");
        }
    });
})();

app.ribbons.web_marketing.push({
    text: $.i18n.prop("newsletter"),
    processor: app.tabs.newsletterView.newsletter,
    ui_class: "newsletterView",
    views: [
        {ui_class: 'newsletter', text: $.i18n.prop("newsletter")},
        {ui_class: 'subscriber', text: $.i18n.prop("subscriber")},
        {ui_class: 'unsubscriber', text: $.i18n.prop("unsubscriber")}
    ]

});


_n.sortable = {
    list: {
        "1": "title"
    },
    sorted: "1",
    dir: "up"
};

_n.switch_menu_entries = [
    {
        text: $.i18n.prop("subscriber.list"),
        ui_class: "view-switch subscriber list-view",
        action: "subscriber"
    },
    {
        text: $.i18n.prop("unsubscriber.list"),
        ui_class: "view-switch unsubscriber list-view",
        action: "unsubscriber"
    }
];

_n.action_menu_entries = [
    {
        text: $.i18n.prop("create.newsletter"),
        ui_class: "create-newsletter",
        action: "create.newsletter"
    }
];

_n.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit",
        action: "edit"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "delete"
    },
    {
        text: $.i18n.prop("send"),
        ui_class: "newsletterView",
        action: "send"
    }
]

_n.onActionClick = function (action, data) {
    switch (action) {
        case "edit":
            this.editNewsletter(data.id, data.name);
            break;
        case "delete":
            this.deleteNewsletter(data.id, data.name);
            break
        case "send":
            this.sendNewsletter(data.id);
            break
    }
}

_n.onSelectedActionClick = function (action, selecteds) {
    switch (action) {
        case "remove":
            this.deleteSelectedNewsletters(selecteds.collect("id"))
            break
        case "administrativeStatus":
            this.changeAdministrativeStatus(selecteds)
            break
        case "sendTo":
            this.sendNewsletter(selecteds.collect("id"))
            break
    }
}

_n.ajax_url = app.baseUrl + "newsletter/loadAppView";

_n.advanceSearchUrl = app.baseUrl + "newsletter/advanceFilter"
_n.advanceSearchTitle = $.i18n.prop("newsletter");

_n.editNewsletter = function (id, name) {
    var _self = this;
    var data = {id: id},
        title = $.i18n.prop("edit.newsletter");
    if (typeof id == "undefined") {
        data = {};
        name = "";
        title = id ? $.i18n.prop("edit.newsletter") :$.i18n.prop("create.newsletter");
    }
    this.renderCreatePanel(app.baseUrl + "newsletter/edit", title, name, data, {
        width: 710,
        success: function () {
            _self.reload();
            if(id) {
                app.global_event.trigger("newsletter-update", [id]);
            } else {
                app.global_event.trigger("newsletter-create");
            }
        },
        error: function(form){
            var saveAndSendButton = form.find(".save-and-send");
            saveAndSendButton.removeAttr("disabled");
        },
        content_loaded: function (form) {
            var _self = $(this);
            var sendButton = form.find(".save-and-send");
            form.on("lock", function() {
                sendButton.attr("disabled", "disabled")
            });
            form.on("unlock", function() {
                sendButton.removeAttr("disabled");
            });
            _self.find("input[name='receiver'], .add-receiver").on("click", function() {
                var addReceiverBtn = $(this);
                var config = {url: app.baseUrl + 'newsletter/receiverSelector'};
                var toFormRow = $(this).closest(".form-row");
                if(addReceiverBtn.data("isDirty")) {
                    var data = {
                        includeAllSubscriber: false
                    };
                    if(_self.find("input[name='includeAllSubscriber']").val() == 'true') {
                        data.includeAllSubscriber = true;
                    }
                    config.data = data;
                } else {
                    config.data = {newsletterId: id};
                }
                var onSelectionDone = function(form) {
                    var toFieldVal = "",
                        indexes = [];
                    toFormRow.find('[name=includeAllSubscriber]').remove();
                    if (form.find("input[name=includeAllSubscriber]").prop("checked") == true) {
                        toFormRow.append('<input type="hidden" name="includeAllSubscriber" value="true">');
                        toFieldVal = "SUBSCRIBER" + " "
                    }
                    form.find("input[name='customer']").each(function (i, c) {
                        indexes.push($(c).val());
                    });
                    if (indexes.length) {
                        toFieldVal = toFieldVal + ("CUSTOMER[" + indexes.join(',') + "]") + " "
                    }
                    indexes = [];
                    form.find("input[name='customerGroup']").each(function (i, cg) {
                        indexes.push($(cg).val());
                    });
                    if (indexes.length) {
                        toFieldVal = toFieldVal + ("GROUP[" + indexes.join(',') + "]") + " "
                    }
                    indexes = [];
                    var recipientEmails = form.find("input[name=recipientEmail]");
                    var recipientNames = form.find("input[name=recipientName]");
                    recipientEmails.each(function (i, c) {
                        indexes.push($(recipientNames[i]).val() + "<" + $(c).val() + ">")
                    });
                    if (indexes.length) {
                        toFieldVal = toFieldVal + "EMAIL[" + indexes.join(',') + "]"
                    }
                    _self.find("[name='receiver']").val(toFieldVal).trigger("blur").trigger("validate");
                    addReceiverBtn.data("isDirty", true);
                };
                bm.recipientSelectorPopup(toFormRow, config, onSelectionDone);
            });
            _self.find(".save-and-send").click(function() {
                form.append("<input type='hidden' name='sendMail' value='true'/>")
                form.trigger("submit")
            });
        },
        beforeSubmit: function(form, settings){
            var saveAndSubmitButton = form.find(".save-and-send");
            saveAndSubmitButton.attr("disabled", "disabled");
        }
    })
}

_n.createNewsletter = _n.editNewsletter;

_n.viewNewsletter = function (id, name) {
    bm.viewPopup(app.baseUrl + "newsletter/view", {id: id},{width: 600})
};

_n.deleteNewsletter = function (id, name) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.newsletter",[bm.htmlEncode(name)]), function() {
        bm.ajax({
            url: app.baseUrl + "newsletter/delete",
            data: {id: id},
            success: function() {
                _self.reload();
            }
        })
    }, function() {
    })
};

_n.deleteSelectedNewsletters = function (selecteds) {
    var _self = this
    bm.confirm($.i18n.prop("confirm.delete.selected.newsletter"), function () {
        bm.ajax({
            url: app.baseUrl + "newsletter/deleteSelected",
            data: {ids: selecteds},
            success: function () {
                _self.reload();
                _self.body.find(".action-header").hide();
            }
        })
    }, function() {
    })
}

_n.sendNewsletter = function(id) {
    var _self = this
    bm.ajax({
        url: app.baseUrl + "newsletter/sendNewsletter",
        data: {id: id},
        success: function () {
            _self.reload()
        }
    })
}

_n.changeAdministrativeStatus = function(selecteds) {
    bm.editPopup(app.baseUrl + 'newsletter/loadNewsletterStatusOption', $.i18n.prop('administrative.status'), null , {},  {
        width: 600,
        events: {
            content_loaded: function() {
                var $this = $(this);
                var ids = selecteds.collect("id");
                $.each(ids, function (index, value) {
                    $this.find(".edit-popup-form").append('<input type="hidden" name="id" value="' + value + '">');
                });
            }
        }
    });
}

app.tabs.newsletterView.subscriber = function () {
    app.tabs.newsletterView.subscriber._super.constructor.apply(this, arguments);
}

var _ns = app.tabs.newsletterView.subscriber.inherit(app.tabs.newsletterView);

(function () {
    function attachEvents() {
        var _self = this;
    }

    _ns.init = function(){
        app.tabs.newsletterView.subscriber._super.init.call(this);
        attachEvents.call(this);
    }
})();

_ns.ajax_url = app.baseUrl + "newsletter/loadSubscriber";

_ns.sortable = {
    list: {
        "1": "firstName",
        "2": "email",
        "3": "created"
    },
    sorted: "1",
    dir: "up"
};

_ns.switch_menu_entries = [
    {
        text: $.i18n.prop("newsletter.list"),
        ui_class: "view-switch newsletter list-view",
        action: "newsletter"
    },
    {
        text: $.i18n.prop("unsubscriber.list"),
        ui_class: "view-switch unsubscriber list-view",
        action: "unsubscriber"
    }
];

_ns.menu_entries = [
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "delete"
    }
]

_ns.onActionClick = function (action, data) {
    switch (action) {
        case "delete":
            this.delete(data.id, data.name);
            break;
    }
}

_ns.onSelectedActionClick = function (action, selecteds) {
    switch (action) {
        case "remove":
            this.deleteSelected(selecteds.collect("id"))
            break
    }
}

_ns.advanceSearchUrl = app.baseUrl + "newsletter/advanceSubscriberFilter"
_ns.advanceSearchTitle = $.i18n.prop("subscriber");

_ns.delete = function(id, name) {
    var _self = this;
    bm.remove("newsletterSubscriber", "NewsletterSubscriber", $.i18n.prop("confirm.delete.subscriber", [name]), app.baseUrl + "newsletter/deleteSubscriber", id, {
        success: function () {
            _self.reload();
        },
        is_final: true
    })
}

_ns.deleteSelected = function(selecteds){
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.subscriber"), function(){
        bm.ajax({
            url: app.baseUrl + "newsletter/deleteSelectedSubscriber",
            data: {ids: selecteds},
            success: function() {
                _self.reload()
            }
        })
    }, function(){})
}


/****************Unsubscriber****************/

app.tabs.newsletterView.unsubscriber = function () {
    app.tabs.newsletterView.unsubscriber._super.constructor.apply(this, arguments);
}

var _nu = app.tabs.newsletterView.unsubscriber.inherit(app.tabs.newsletterView);

(function () {
    function attachEvents() {
        var _self = this;
    }

    _nu.init = function(){
        app.tabs.newsletterView.unsubscriber._super.init.call(this);
        attachEvents.call(this);
    }
})();

_nu.ajax_url = app.baseUrl + "newsletter/loadUnsubscriber";

_nu.sortable = {
    list: {
        "1": "s.firstName",
        "2": "s.email",
        "3": "subscribed",
        "4": "unsubscribed"
    },
    sorted: "1",
    dir: "up"
};

_nu.switch_menu_entries = [
    {
        text: $.i18n.prop("newsletter.list"),
        ui_class: "view-switch newsletter list-view",
        action: "newsletter"
    },
    {
        text: $.i18n.prop("subscriber.list"),
        ui_class: "view-switch subscriber list-view",
        action: "subscriber"
    }
];

_nu.menu_entries = [
    {
        text: $.i18n.prop("view.reason"),
        ui_class: "view view-reason",
        action: "view-reason"
    }
]

_nu.onActionClick = function (action, data) {
    switch (action) {
        case "view-reason":
            this.viewReason(data.id);
            break;
    }
}

_nu.advanceSearchUrl = app.baseUrl + "newsletter/advanceUnsubscriberFilter"
_nu.advanceSearchTitle = $.i18n.prop("unsubscriber");

_nu.viewReason = function(id) {
    bm.viewPopup(app.baseUrl + "newsletter/viewReason", {id: id}, {width: 650});
}