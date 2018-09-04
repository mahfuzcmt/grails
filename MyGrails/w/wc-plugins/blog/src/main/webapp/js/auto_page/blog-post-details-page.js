app.config.blog_post_comment_response_display_time = app.config.blog_post_comment_response_display_time || 6000;

$(function() {
    initBlogPostDetails($(".blog-post-details"))
});

function initBlogPostDetails(content) {
    var postId = content.find("input[name='postId']").val()
    var commentPanel = content.find(".blog-post-comment-panel")
    var commentPostPanel = content.find(".comment-post-panel")
    var commentCreatePanel = content.find("form#blog-post-create-comment-panel")
    function loadCommentPanel(data) {
        bm.ajax({
            url: app.baseUrl + 'blogPage/loadPostComments',
            dataType: 'json',
            data: data,
            success: function (response) {
                var html = response.html
                commentPanel.html(html);
                bindCommentPanelEvents(html);
                bindReactionEvents();
                bindReplyEvents();
                bindloginAskingPopupEvents();
            }
        })
    }

    function bindCommentPanelEvents(html){
        $(html).on("click", ".view-all-comment", function(){
            loadCommentPanel({id: postId, max : -1})
        })
    }
    commentCreatePanel.form({
        ajax: {
            success: function(response) {
                commentCreatePanel.trigger("reset")
                if(response.status == "approved") {
                    loadCommentPanel({id: postId})
                }
                var msgDiv = $('<div class="message-block info-message">' + response.message + '</div>');
                commentPostPanel.prepend(msgDiv);
                app.captchaUtil.reloadCaptcha(response.captchaType);
                msgDiv.scrollHere()
                setTimeout(function () {
                    msgDiv.remove();
                }, app.config.blog_post_comment_response_display_time);
            },
            error: function(a, b, response) {
                var msgDiv = $('<div class="message-block error-message">' + response.message + '</div>');
                commentPostPanel.prepend(msgDiv);
                msgDiv.scrollHere()
                app.captchaUtil.reloadCaptcha(response.captchaType);
                setTimeout(function () {
                    msgDiv.remove();
                }, app.config.blog_post_comment_response_display_time);
            }
        }
    })
    function bindReactionEvents() {
        var reactionButton = content.find(".reaction")
        reactionButton.on("click", function () {
            var data = $(this).data()
            bm.ajax({
                url: app.baseUrl + 'blogPage/reactBlogComment',
                dataType: 'json',
                data: data,
                success: function (response) {
                    loadCommentPanel({id: postId})
                }
            })
        })
    }

    bindReactionEvents();

    function getReplyPopup(thiz) {
        var data = thiz.data()
        bm.ajax({
            url: app.baseUrl + "blogPage/getReplyPopup",
            dataType: 'html',
            data: data,
            success: function(resp) {
                var content = $(resp);
                content.find(".blog-comment-reply-popup").form({
                    ajax: true,
                    preSubmit: function(ajaxSettings) {
                        $.extend(ajaxSettings, {
                            success: function(resp){
                                bm.notify(resp.message, "success")
                                loadCommentPanel({id: postId})
                                popup.close()
                            },
                            error: function(a, b, resp) {
                                renderMessage(content.find(".body .message-container"), resp.message, "error")
                            }
                        })
                    }
                });
                var popup = renderGlobalSitePopup(content, {clazz: "blog-comment-reply-popup"});
            }
        });
    }

    function bindReplyEvents() {
        var commentReplyPopup = content.find(".btn-comment-reply")
        commentReplyPopup.on("click", function () {
            var thiz = $(this);
            getReplyPopup(thiz)
        })
    }
    bindReplyEvents();

    function logingToReact(){
        content.on("click", ".loging-to-react", function () {
            var data = $(this).data()
            bm.ajax({
                url: app.baseUrl + "blogPage/logingToReact",
                dataType: 'html',
                data: data,
                success: function(resp) {
                    var content = $(resp);
                    renderGlobalSitePopup(content);
                }
            });
        });
    }

    logingToReact();

    bindCommentPanelEvents(content);
}




