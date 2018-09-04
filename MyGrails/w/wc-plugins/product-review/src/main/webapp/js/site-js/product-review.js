$(function() {
    app.config.review_message_display_time = app.config.review_message_display_time  || 6000;
    function reviewTabEvents(reviewPanel) {
        function attachEvents () {
            reviewPanel.find(".review-rating").raty({
                half: true,
                path: app.systemResourceUrl + "plugins/product-review/images/raty",
                score:  function() {
                    return $(this).attr('score');
                },
                readOnly: function() {
                    return $(this).hasClass('read-only')
                }
            });
        }
        var reviewForm = reviewPanel.find(".review-form");
        var rating = reviewForm.find(".rating").raty({
            path: app.systemResourceUrl + "plugins/product-review/images/raty",
            half: true,
            cancel: true,
            size: 24
        });
        function hideForm() {
            reviewForm.find("input[name=name]").val("");
            reviewForm.find("input[name=email]").val("");
            reviewForm.find("[name=review]").val("");
            rating.raty('cancel');
            reviewPanel.find(".review-form-container").hide();
            reviewPanel.find(".write-review").show();
        }

        reviewPanel.find(".write-review").on("click", function () {
            reviewPanel.find(".review-form-container").toggle();
            $(this).hide();
        });
        reviewForm.find(".cancel").on("click", hideForm);
        function renderMessage(data, type) {
            var msgSpan = $('<div class="message-block ' + type + '-message">' + data + '</div>');
            reviewPanel.find(".message-container").append(msgSpan);
            msgSpan.scrollHere()
            setTimeout(function(){
                msgSpan.remove();
            }, app.config.review_message_display_time);
        }
        reviewForm.form({
            ajax: true,
            preSubmit: function (ajaxSettings) {
                var form = $(this);
                var review = form.find("[name=review]").val();
                var score = rating.raty("score") ? rating.raty("score") : 0;
                if(score <= 0 && !review) {
                    renderMessage($.i18n.prop("no.review.rating.message"), "error");
                    return false;
                }
                $.extend(ajaxSettings, {
                    success: function (data) {
                        if(data.action == "reload") {
                            _sendReloadRequest();
                        }
                        renderMessage(data.message, "info");
                        hideForm();
                        app.captchaUtil.reloadCaptcha(data.captchaType)
                    } ,
                    error: function(a, b, resp) {
                        renderMessage(resp.message, "error");
                        if(resp.captchaValidation) {
                            app.captchaUtil.reloadCaptcha(resp.captchaType);
                        } else {
                            hideForm();
                        }
                    }
                })
            }
        });
        reviewPanel.find(".review-view-panel").find("paginator").paginator();
        var paginator = reviewPanel.find(".review-view-panel").find(".pagination").obj();
        if(paginator) {
            paginator.showPages = window.innerWidth > 800 ? app.config.number_block_in_paginator_count : app.config.number_block_in_paginator_count_800
        }
        var wrap = reviewPanel.find(".review-show-panel");
        var reLoadWrap = wrap.find(".main-container");
        function _sendReloadRequest() {
            var _config = {
                url: app.baseUrl + "productReview/loadReview",
                appendUrl: null,
                type: "get"
            };
            var productId = reviewPanel.find("input[name=productId]").val();
            _config.param = paginator ? {max: paginator.all ? -1 : paginator.itemsPerPage, offset: (paginator.currentPage - 1) * paginator.itemsPerPage, productId: productId} : {productId: productId};
            wrap.loader();
            $.ajax({
                url: _config.url + (_config.appendUrl || ""),
                type: _config.type,
                data: _config.param,
                dataType: 'html',
                success: function(resp) {
                    onReloadSuccess(resp)
                }
            });
        }
        function onReloadSuccess(resp) {
            resp = $(resp);
            reLoadWrap.html(resp.find(".main-container").html());
            var paginatorTag = resp.find("paginator");
            var total = +paginatorTag.attr("total");
            var max = +paginatorTag.attr("max");
            var offset = +paginatorTag.attr("offset");
            if(!paginator) {
                reLoadWrap.after(resp.find(".page-initiator-container"));
                reviewPanel.find(".review-view-panel").find("paginator").paginator();
                paginator = reviewPanel.find(".review-view-panel").find(".pagination").obj();
                if(paginator) {
                    paginator.showPages = window.innerWidth > 800 ? app.config.number_block_in_paginator_count : app.config.number_block_in_paginator_count_800
                }
            }
            paginator && paginator.update(total, max, Math.floor(offset / max) + 1);
            wrap.loader(false)
            attachEvents();
        }
        if(paginator) {
            paginator.onPageClick = function(){
                _sendReloadRequest();
            };
        }
        attachEvents();
    }
    $(".product-widget.widget-information .bmui-tab").on("tab:load", function (event, ui) {
        var reviewPanel = $(ui.panel).find(".review-panel");
        if(reviewPanel.length) {
            reviewTabEvents(reviewPanel);
        }
    })
    if(location.hash == "#review") {
        $(".product-widget.widget-information .bmui-tab").tabify("activate", "reviewAndRating");
    }
});
