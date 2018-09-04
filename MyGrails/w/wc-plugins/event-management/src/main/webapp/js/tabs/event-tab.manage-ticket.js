app.tabs.manageTicket = function(config) {
    this.constructor_args = [config]
    this.id = config.id;
    this.eventId = config.eventId;
    this.eventSessionId = config.eventSessionId
    this.text = $.i18n.prop('manage.tickets');
    this.tip = $.i18n.prop('manage.tickets');
    this.ui_class = "event-ticket edit-tab";
}

var _t = app.tabs.manageTicket.prototype

_t.createTicket = function() {
    var _self = this, formDom;
    window["validateEventSeat"] =  function(value) {
        var pattern = formDom.find("#pattern-storage-" + formDom.find("#section").val()).val()
        pattern = "^" + pattern + "(-" + pattern + ")?$"
        return new RegExp(pattern).test(value)
    }
    this.renderCreatePanel(app.baseUrl + "eventAdmin/createComplementaryTicket", $.i18n.prop("create.complementary.ticket"), "",
        {
            eventId: _self.eventId,
            eventSessionId:_self.eventSessionId
        },
        {
            success : function() {
                _self.reload()
                app.global_event.trigger("ticket-purchased")
            },
            content_loaded: function(form) {
                formDom = form
                form.find("#section").change(function() {
                    form.find("div[name='seat']").obj().clear_all_selection()
                })
            }
        }
    )
};

(function() {
    function attachEvents() {
        var _self = this;
        _self.body.find(".toolbar-btn.create-ticket").on("click", function() {
            _self.createTicket();
        })
    }

    _t.init = function () {
        app.tabs.manageTicket.tab = this;
        attachEvents.call(this);
    }
})();

_t.onSwitchMenuClick = function(type) {
    app.Tab.changeView(this, "manageTicket", type)
    app.tabs.manageTicket.lastView = app.tabs.manageTicket[type]
}

app.tabs.manageTicket.lastView = app.tabs.manageTicket.ticketView = function(config) {
    if(config.eventId) {
        this.ajax_url = app.baseUrl + "eventAdmin/loadVenueTicketView?eventId=" + config.eventId;
    } else {
        this.ajax_url = app.baseUrl + "eventAdmin/loadVenueTicketView?eventSessionId=" + config.eventSessionId
    }
    app.tabs.manageTicket.ticketView._super.constructor.apply(this, arguments);
}

var _tt = app.tabs.manageTicket.ticketView.inherit(app.SingleTableTab, app.tabs.manageTicket)

_tt.switch_menu_entries = [
    {
        text: $.i18n.prop("seat.map"),
        ui_class: "view-switch seat-map list-view",
        action: "seatMap"
    }
];

_tt.menu_entries = [
    {
        text: $.i18n.prop("void"),
        ui_class: "void",
        action: "void"
    }
];

_tt.onActionClick = function(action, data) {
    switch(action) {
        case "void":
            this.voidTicket(data.id);
            break;
    }
};

_tt.voidTicket = function(id) {
    var _self = this
    bm.ajax({
        url: app.baseUrl + "eventAdmin/voidComplementaryTicket",
        data: {ticketNumber: id},
        success : function() {
            _self.reload()
        }
    })
}

app.tabs.manageTicket.seatMap = function(config) {
    this.ui_body_class = "simple-tab seat-map-view";
    if(config.eventId) {
        this.ajax_url = app.baseUrl + "eventAdmin/loadEventSeatMap?eventId=" + config.eventId;
    } else {
        this.ajax_url = app.baseUrl + "eventAdmin/loadEventSeatMap?eventSessionId=" + config.eventSessionId
    }
    app.tabs.manageTicket.seatMap._super.constructor.apply(this, arguments);
}

var _ts = app.tabs.manageTicket.seatMap.inherit(app.Tab, app.tabs.manageTicket);

_ts.switch_menu_entries = [
    {
        text: $.i18n.prop("list.view"),
        ui_class: "view-switch ticket-list list-view",
        action: "ticketView"
    }
];

(function () {
    function bindLocationSelection() {
        var _self = this
        this.body.find(".selector").change(function() {
            _self.reload()
        })
    }
    function attachEvents() {
        var _self = this;
        bindLocationSelection.call(this)
        this.body.find(".app-tab-content-container").scrollbar({
            show_horizontal: true,
            vertical: {
                offset: -2
            },
            horizontal: {
                offset: 12
            }

        })
        _self.body.find(".toolbar-btn.create-ticket").on("click", function() {
            _self.createTicket();
        })
        this.body.find(".reload").click(function() {
            _self.reload()
        })
    }

    _ts.init = function () {
        app.tabs.manageTicket.seatMap._super.init.call(this)
        attachEvents.call(this)
    }

    _ts.reload = function() {
        var _self = this;
        this.body.loader();
        bm.ajax({
            url: app.baseUrl + "eventAdmin/loadEventSeatMap",
            data: {
                eventId: this.eventId,
                eventSessionId: this.eventSessionId,
                section: _self.body.find("select.section-selector").val()
            },
            dataType: "html",
            success: function(resp) {
                resp = $(resp)
                _self.body.find(".section-selector-wrapper").html(resp.find(".section-selector-wrapper").html()).find("select").chosen({disable_search: true})
                _self.body.find(".app-tab-content-container").html(resp.filter(".app-tab-content-container").html())
                _self.body.removeClass("updating");
                _self.body.loader(false);
            }
        })
    }
})();