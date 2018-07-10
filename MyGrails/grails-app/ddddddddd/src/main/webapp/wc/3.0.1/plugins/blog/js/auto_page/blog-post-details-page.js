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
                bindCommentPanelEvents(html)
                commentPanel.html(html)
            }
        })
    }

    function bindCommentPanelEvents(html){
        $(html).find(".view-all-comment").click(function(){
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
    bindCommentPanelEvents(content);
}




