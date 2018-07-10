$(function() {
    var _or = app.tabs.order.prototype;

    _or.afterTableReload = _or.afterTableReload.blend(function () {
        var _self = this
        var body = _self.body
        body.find(".gift-wrapper-message-btn").on("click", function(event) {
            var giftWrapperMsg = $(event.target).attr("gift-wrapper-msg");
            var content = "<p class='gift-wrapper-message-body'>"+giftWrapperMsg+"</p>";
            bm.editPopup( "", $.i18n.prop("gift.wrapper.message"),"", {}, {content: content, height: 250});
        })
    });

});