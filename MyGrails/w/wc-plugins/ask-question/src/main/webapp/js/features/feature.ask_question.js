/**
 * Created by sajed on 3/3/14.
 */
app.tabs.ask_question = function(configs) {
    this.text = $.i18n.prop("product.questions");
    this.tip = $.i18n.prop("manage.questions");
    this.ui_class = "ask-question";
    this.ajax_url = app.baseUrl + "askQuestionAdmin/loadAppView";
    app.tabs.ask_question._super.constructor.apply(this, arguments);
}

app.ribbons.administration.push({
    text: $.i18n.prop("product.questions"),
    processor: app.tabs.ask_question,
    ui_class: "ask-question",
    license: "allow_question_answer_feature",
    ecommerce: true
})

app.tabs.ask_question.inherit(app.SingleTableTab)

var _q = app.tabs.ask_question.prototype;

_q.menu_entries = [
    {
        text: $.i18n.prop("reply"),
        ui_class: "reply"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "remove"
    }
];

_q.sortable = {
    list: {
        "1": "p.name",
        "2": "name",
        "3": "created",
        "4": "status"
    },
    dir: "up"
};


_q.onActionClick = function (action, data) {
    switch (action) {
        case "remove":
            this.deleteQuestion(data.id)
            break;
        case "reply":
            this.reply(data.id)
            break;
    }
};

(function(){
    function attachEvent() {

    }

    _q.init = function () {
        app.tabs.ask_question._super.init.call(this);
        attachEvent.call(this);
    };
})();

_q.reply = function(id) {
    var _self = this;
    bm.editPopup(app.baseUrl + "askQuestionAdmin/reply", $.i18n.prop("reply.question"), "", {id: id}, {
        width: 800,
        events: {
            content_loaded: function (popup) {
            }
        },
        success: function () {
            _self.reload();
        }
    });
}
_q.viewQuestion = function(id){
    var _self = this;
    var popup = bm.viewPopup(app.baseUrl + "askQuestionAdmin/view", {id: id}, {width: 700})
    popup.on("content_loaded", function() {
        var dom = popup.getDom();
        dom.find("[name=answer]").on("click", function(){
            _self.reply(id);
            popup.close();
        });
    })
}
_q.deleteQuestion = function(id) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.product.question.delete"), function () {
        bm.ajax({
            url: app.baseUrl + "askQuestionAdmin/delete",
            data: {id: id},
            success: function () {
                _self.reload()
            }
        })
    }, function () {
    });

}

_q.onSelectedActionClick = function(action, selecteds) {
    switch (action) {
        case "remove":
            this.deleteQuestion(selecteds.collect("id"))
            break;
    }
}

_q.advanceSearchUrl = app.baseUrl + "askQuestionAdmin/advanceFilter";
_q.advanceSearchTitle = $.i18n.prop("product.question");
_q.advance_search_popup_clazz = 'ask-question-popup'