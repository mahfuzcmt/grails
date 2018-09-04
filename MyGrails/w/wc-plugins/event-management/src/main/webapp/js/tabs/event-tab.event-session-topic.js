app.tabs.manageTopic = function(config) {
    this.id = config.id;
    this.sessionId = config.sessionId;
    this.text = $.i18n.prop('manage.topic');
    this.tip = $.i18n.prop('manage.topic');
    this.ui_class = 'event-session-topic edit';
    if(this.sessionId) {
        this.ajax_url = app.baseUrl + 'eventAdmin/loadTopicAppView?sessionId=' + config.sessionId
    }
    app.tabs.manageTopic._super.constructor.apply(this, arguments);
};

var _topic = app.tabs.manageTopic.inherit(app.SingleTableTab);

_topic.sortable = {
    list: {
        "1": "name"
    },
    sorted: "1",
    dir: "up"
};

_topic.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit",
        action: "edit"
    },
    {
        text: $.i18n.prop("view"),
        ui_class: "view",
        action: "view"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "delete",
        action: "remove"
    }
];

_topic.onActionClick = function(action, data) {
    switch(action) {
        case "edit":
            this.edit(data.id, data.name);
            break;
        case "view":
            this.view(data.id);
            break;
        case "remove":
            this.deleteTopic(data.id, data.name);
            break;
    }
};

_topic.onSelectedActionClick = function(action, selecteds) {
    switch(action) {
        case "remove":
            this.deleteSelectedTopic(selecteds.collect("id"));
            break;
    }
};

(function() {
    function attachEvents() {
        var _self = this;
        this.on_global("event-session-topic-updated", function () {
            _self.reload();
        });
        this.on_global("event-session-topic-restore", function() {
            _self.reload();
        });
        this.on("close", function () {
            app.tabs.manageTopic.tab = null;
        });
        this.body.find(".toolbar .create").on("click", function() {
            _self.createTopic();
        });
    }

    _topic.init = function () {
        app.tabs.manageTopic._super.init.call(this);
        app.tabs.manageTopic.tab = this;
        attachEvents.call(this);
    };
})();

_topic.edit = function(id, name) {
    var _self = this;
    var data = {id: id, sessionId: this.sessionId},
        title = $.i18n.prop('edit.event.session.topic');
    if(typeof id == 'undefined') {
        data = {sessionId: this.sessionId}
        title = $.i18n.prop('create.event.session.topic');
    }
    _self.renderCreatePanel(app.baseUrl + 'eventAdmin/editTopic', title, name, data, {
        success: function() {
            app.tabs.manageTopic.tab.reload()
        }
    });
};

_topic.createTopic = function() {
    this.edit(undefined, undefined)
};

_topic.view = function(id) {
    bm.viewPopup(app.baseUrl + 'eventAdmin/viewTopic', {id: id}, {width: 800})
};

_topic.deleteTopic = function(id, name) {
    var _self = this;
    bm.remove("eventSessionTopic", $.i18n.prop("event.session.topic"), $.i18n.prop("confirm.delete.event.session.topic", [name]), app.baseUrl + "eventAdmin/deleteTopic", id, {
        is_final: true,
        success: function () {
            _self.reload()
        }
    })
};

_topic.deleteSelectedTopic = function(ids) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.event.session.topics"), function() {
        bm.ajax({
            url: app.baseUrl + "eventAdmin/deleteSelectedTopics",
            data: {ids: ids},
            success: function () {
                _self.reload();
                _self.body.find(".action-header").hide();
            }
        })
    },function(){
    });
};