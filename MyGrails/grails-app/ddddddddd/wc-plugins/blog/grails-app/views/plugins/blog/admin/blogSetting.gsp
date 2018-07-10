<form class="create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" onsubmit="return false" method="POST">
    <input type="hidden" name="type" value="blog">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="general"/></h3>
            <div class="info-content"><g:message code="section.text.blog.general.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="blog.moderator.email"/><span class="suggestion">e.g. peter.smith@abc.com</span></label>
                <input type="text" name="blog.moderator_email" validation="single_email" value="${config.moderator_email}">
            </div>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="post.image"/></h3>
            <div class="info-content"><g:message code="section.text.post.image.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="list.view"/><span class="suggestion"> e.g. 200x200</span></label>
                    <div class="twice-input-row">
                        <input type="text" name="blog.listview_width" class="smaller" validation="required digits max[950] min[48] maxlength[9]" maxlength="9" restrict="numeric" value="${config.listview_width}"><span>X</span><input type="text" name="blog.listview_height" class="smaller" validation="required digits max[950] min[48] maxlength[9]" maxlength="9" restrict="numeric" value="${config.listview_height}">
                    </div>
                </div><div class="form-row mandatory">
                    <label><g:message code="category.details.page"/><span class="suggestion"> e.g. 300x300</span></label>
                    <div class="twice-input-row">
                        <input type="text" name="blog.cat_details_width" class="smaller" validation="required digits max[900] min[48] maxlength[9]" maxlength="9" restrict="numeric" value="${config.cat_details_width}"><span>X</span><input type="text" name="blog.cat_details_height" class="smaller" validation="required digits max[600] min[48] maxlength[9]" maxlength="9" restrict="numeric" value="${config.cat_details_height}">
                </div>
            </div>
            </div>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="post.details.page"/></h3>
            <div class="info-content"><g:message code="section.text.post.details.page.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code='show.hide'/></label>
                <input type="checkbox" class="multiple" name="blog.post_date" value="true" uncheck-value="false"  ${config.post_date == "true" ? "checked" : ""}>
                <span class="value"><g:message code="show.date.in.post"/></span>
                <input type="checkbox" class="multiple" name="blog.post_author" value="true" uncheck-value="false"  ${config.post_author == "true" ? "checked" : ""}>
                <span class="value"><g:message code="show.author.name"/></span>
                <input type="checkbox" class="multiple" name="blog.post_categories" value="true" uncheck-value="false"  ${config.post_categories == "true" ? "checked" : ""}>
                <span class="value"><g:message code="show.categories"/></span>
            </div>
            <div class="blog_post_share_config_panel">
                <div class="form-row">
                    <label><g:message code="enable.post.sharing"/></label>
                    <input type="checkbox" class="multiple" name="blog.facebook" value="true" uncheck-value="false" ${config.facebook == "true" ? "checked" : ""}>
                    <span class="value"><g:message code="facebook"/></span>&nbsp;&nbsp;&nbsp;
                    <input type="checkbox" class="multiple" name="blog.twitter" value="true" uncheck-value="false" ${config.twitter == "true" ? "checked" : ""}>
                    <span class="value"><g:message code="twitter"/></span>&nbsp;&nbsp;&nbsp;
                    <input type="checkbox" class="multiple" name="blog.google" value="true" uncheck-value="false" ${config.google == "true" ? "checked" : ""}>
                    <span class="value"><g:message code="google"/></span>&nbsp;&nbsp;&nbsp;
                    <input type="checkbox" class="multiple" name="blog.linkedIn" value="true" uncheck-value="false" ${config.linkedIn == "true" ? "checked" : ""}>
                    <span class="value"><g:message code="linkedIn"/></span>&nbsp;&nbsp;&nbsp;
                    <input type="checkbox" class="multiple" name="blog.send_friend" value="true" uncheck-value="false" ${config.send_friend == "true" ? "checked" : ""}>
                    <span class="value"><g:message code="send.friend"/></span>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="who.can.comment"/><span class="suggestion">e.g. Anyone</span></label>
                    <g:select class="large" name="blog.comment_restriction" from="${[g.message(code: 'anyone'), g.message(code: 'restricted.customers.only'), g.message(code: 'disable.commenting')]}" keys="${['A', 'R', 'D']}" value="${config.comment_restriction}"/>
                </div><div class="form-row chosen-wrapper">
                <label><g:message code="publish.comment"/><span class="suggestion">e.g. should be Publish immediately</span></label>
                <g:select class="large" name="blog.comment_moderator_approval" from="${[g.message(code: 'publish.immediately'), g.message(code: 'wait.for.moderator.approval')]}" keys="${['false', 'true']}" value="${config.comment_moderator_approval}"/>
            </div>
            </div>
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="show.no.of.comments.initially"/></label>
                    <input class="large" type="text" restrict="numeric" maxlength="9" name="blog.comment_per_page" validation="required  gt[0] maxlength[9]" value="${config.comment_per_page}">
                </div><div class="form-row">
                <label>&nbsp;</label>
                <input type="checkbox" class="single" name="blog.comment_email" value="true" uncheck-value="false" ${config.comment_email == "true" ? "checked" : ""}>
                <span><g:message code="comment.author.must.fill.out.email.field"/></span>
            </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="blog.comment_name" value="true" uncheck-value="false" ${config.comment_name == "true" ? "checked" : ""}>
                    <span><g:message code="comment.author.must.fill.out.name.field"/></span>
                </div><div class="form-row">
                <input type="checkbox" class="single" name="blog.captcha" value="true" uncheck-value="false" ${config.captcha == "true" ? "checked" : ""}>
                <span><g:message code="enable.captcha"/></span>
            </div>
            </div>
            <div class="form-row mandatory">
                <label><g:message code="comment.blacklist"/></label>
                <input class="tiny" type="text" restrict="numeric" maxlength="9" validation="required maxlength[9]" name="blog.black_list_no" value="${config.black_list_no}">
                <span><g:message code="email,name.comment.contains.comma.separated.words.marked.as.spam"/></span>
                <span><g:message code="number.of.time.blacklisted.words.appear"/></span>
            </div>
            <div class="from-row">
                <label><g:message code="comment.blacklist.keywords"/> </label>
                <textarea type="text" name="blog.black_list" class="xx-larger">${config.black_list}</textarea>
            </div>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="blog.category.details.page"/></h3>
            <div class="info-content"><g:message code="section.text.blog.category.details.page.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row chosen-wrapper">
                <label><g:message code="show.post"/><span class="suggestion"> e.g. Summary</span></label>
                <g:select class="medium" toggle-target="show-summary-post-config-panel" name="blog.cat_post_content" from="${[g.message(code: "full.content"), g.message(code: "summary")]}" keys="${['F', 'S']}" value="${config.cat_post_content ?: 'S'}"/>
            </div>
            <div class="double-input-row">
                <div class="show-summary-post-config-panel-S">
                    <div class="form-row">
                        <label><g:message code="no.of.characters"/><span class="suggestion">e.g. 160</span></label>
                        <input type="text" restrict="numeric" validation="required maxlength[9]" maxlength="9" class="medium" name="blog.cat_post_content_length" value="${config.cat_post_content_length ?: 160}">
                    </div><div class="form-row">
                    <label><g:message code="label.for.details.link"/><span class="suggestion">e.g. s.read.more</span></label>
                    <input type="text" class="medium" name="blog.cat_post_read_more" value="${config.cat_post_read_more}">
                </div>
                </div>
            </div>
            <div class="form-row">
                <label><g:message code='show.hide'/></label>
                <input type="checkbox" class="multiple" name="blog.cat_image" value="true" uncheck-value="false" ${config.cat_image == "true" ? "checked" : ""}>
                <span class="value"><g:message code="show.image"/></span>
                <input type="checkbox" class="multiple" name="blog.cat_description" value="true" uncheck-value="false" ${config.cat_description == "true" ? "checked" : ""}>
                <span class="value"><g:message code="show.description"/></span>
                <input type="checkbox" class="multiple" name="blog.cat_post_image" value="true" uncheck-value="false" ${config.cat_post_image == "true" ? "checked" : ""}>
                <span class="value"><g:message code="show.post.image"/></span>
                <input type="checkbox" class="multiple" name="blog.cat_post_author" value="true" uncheck-value="false" ${config.cat_post_author == "true" ? "checked" : ""}>
                <span class="value"><g:message code="show.post.author.name"/></span>
                <input type="checkbox" class="multiple" name="blog.cat_post_date" value="true" uncheck-value="false" ${config.cat_post_date == "true" ? "checked" : ""}>
                <span class="value"><g:message code="show.post.date.time"/></span>
                <input type="checkbox" class="multiple" name="blog.cat_post_categories" value="true" uncheck-value="false" ${config.cat_post_categories == "true" ? "checked" : ""}>
                <span class="value"><g:message code="show.post.categories"/></span>
                <input type="checkbox" class="multiple" name="blog.cat_post_comment_count" value="true" uncheck-value="false" ${config.cat_post_comment_count == "true" ? "checked" : ""}>
                <span class="value"><g:message code="show.no.of.comments"/></span>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>