<%@ page import="com.webcommander.plugin.blog.content.BlogCategory" %>
<div class="fee-widget-config-panel blog">
    <g:form controller="frontEndEditor" action="saveWidget" class="config-form" onsubmit="return false;">
        <input type="hidden" name="widgetType" value="blogPost">
        <input type="hidden" name="widgetId" value="${params.widgetId}">
        <div class="fee-config-body fee-noPadding">
            <div class="fee-header-top" style="">
                <div class="fee-row">
                    <div class="config-panel fee-col fee-col-100">
                        <div class="sidebar-group">
                            <div class="sidebar-group-label"><g:message code="show"/></div>
                            <div class="sidebar-group-body">
                                <g:select class="sidebar-input" toggle-target="show-summary-post-config-panel" name="post_content" from="${[g.message(code: "full.content"), g.message(code: "summary")]}" keys="${['F', 'S']}" value="${config.post_content}"/>
                            </div>
                        </div>
                        <div class="sidebar-group">
                            <div class="sidebar-group-label"><g:message code="no.of.characters"/></div>
                            <div class="sidebar-group-body">
                                <input type="text" restrict="numeric" class="sidebar-input" name="content_length" value="${config.content_length}" validation="digits gt[3]">
                            </div>
                        </div>
                        <div class="sidebar-group">
                            <div class="sidebar-group-label"><g:message code="label.for.details.link"/></div>
                            <div class="sidebar-group-body">
                                <input type="text" class="sidebar-input" name="read_more" value="${config.read_more}">
                            </div>
                        </div>
                        <div class="sidebar-group">
                            <div class="sidebar-group-label"><g:message code="height"/></div>
                            <div class="sidebar-group-body">
                                <input type="text" restrict="numeric" class="sidebar-input" name="height" value="${config.height}"/>
                            </div>
                        </div>
                        <div class="sidebar-group blog-selection-config">
                            <div class="sidebar-group-label"><g:message code="selection"/></div>
                            <div class="sidebar-group-body">
                                <g:select class="sidebar-input ${config.selection == "custom" ? 'single-action' : ''}" toggle-target="post-hierarchy" toggle-anim="none" name="selection" from="${[g.message(code: "current.month"), g.message(code: "current.week"), g.message(code: "recent.top"), g.message(code: "custom"), g.message(code: "all")]}" keys="${['current_month', 'current_week', 'recent_top', 'custom', "all"]}" value="${config.selection}"/>
                                <button type="button" class="select-blog-post">Select</button>
                            </div>
                        </div>
                        <div class="sidebar-group">
                            <div class="sidebar-group-label"><g:message code="number.of.post"/></div>
                            <div class="sidebar-group-body">
                                <input type="text" class="sidebar-input" name="post_count" restrict="numeric" value="${config.post_count}" maxlength="9" validation="required gt[0]">
                            </div>
                        </div>
                        <div class="sidebar-group">
                            <div class="sidebar-group-body">
                                <input type="checkbox" class="single" name="image" value="true" uncheck-value="false" ${config.image == "true" ? "checked" : ""}>
                                <label><g:message code="show.image"/></label>
                            </div>
                        </div>
                        <div class="sidebar-group">
                            <div class="sidebar-group-body">
                                <input type="checkbox" class="single" name="author" value="true" uncheck-value="false" ${config.author == "true" ? "checked" : ""}>
                                <label><g:message code="show.author.name"/></label>
                            </div>
                        </div>
                        <div class="sidebar-group">
                            <div class="sidebar-group-body">
                                <input type="checkbox" class="single" name="date" value="true" uncheck-value="false" ${config.date == "true" ? "checked" : ""}>
                                <label><g:message code="show.date.time"/></label>
                            </div>
                        </div>
                        <div class="sidebar-group">
                            <div class="sidebar-group-body">
                                <input type="checkbox" class="single" name="categories" value="true" uncheck-value="false" ${config.categories == "true" ? "checked" : ""}>
                                <label><g:message code="show.categories"/></label>
                            </div>
                        </div>
                        <div class="sidebar-group">
                            <div class="sidebar-group-body">
                                <input type="checkbox" class="single" name="comment_count" value="true" uncheck-value="false" ${config.comment_count == "true" ? "checked" : ""}>
                                <label><g:message code="show.no.of.comments"/></label>
                            </div>
                        </div>
                        <div class="sidebar-group">
                            <div class="sidebar-group-body">
                                <input type="checkbox" class="single" name="pagination" value="true" uncheck-value="false" ${config.pagination == "true" ? "checked" : ""}>
                                <label><g:message code="show.pagination"/></label>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="fee-button-wrapper fee-config-footer">
            <button class="fee-save" type="submit"><g:message code="save"/></button>
            <button class="fee-cancel fee-common" type="button"><g:message code="cancel"/></button>
        </div>
    </g:form>
</div>