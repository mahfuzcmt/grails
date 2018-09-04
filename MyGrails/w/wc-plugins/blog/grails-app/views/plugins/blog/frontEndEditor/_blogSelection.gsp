<%@ page import="com.webcommander.plugin.blog.content.BlogCategory" %>
<div class="fee-widget-config-panel blog">
    <g:form controller="frontEndEditor" action="saveWidget" class="config-form" onsubmit="return false;">
        <input type="hidden" name="widgetType" value="blogPost">
        <input type="hidden" name="widgetId" value="${params.widgetId}">
        <input type="hidden" name="selectedPosts" value="${params.widgetContent}"/>
        <div class="fee-config-body fee-noPadding">
            <div class="fee-configure-panel fee-padding-30">
                <div class="fee-ignore blog-search-header">
                    <div class="fee-padding-tb-5 fee-padding-lr-10">
                        <div class="fee-search-box fee-search-panel">
                            <input type="text" class="fee-search-input" name="searchText" value="${params.searchText}" placeholder="Search">
                            <button type="button" class="fee-search-button"></button>
                        </div>
                        <div class="fee-section-box fee-search-panel">
                            <ui:domainSelect name="category" domain="${com.webcommander.plugin.blog.content.BlogCategory}" id="category"
                                             prepend="${['': g.message(code: "category")]}" class="always-top"
                                             value="${params.category ? params.category.toLong() : 0}"/>
                        </div>
                    </div>
                </div>
                <div style="clear:both"></div>
                <div class="blog-list-container">
                    <div class="fee-body">
                        <table class="fee-table sortable-table" data-sortBy="${params.sortBy}">
                            <colgroup>
                                <col style="width: 6%">
                                <col style="width: 10%">
                                <col style="width: 35%">
                                <col style="width: 20%">
                                <col style="width: 15%">
                            </colgroup>
                            <tr>
                                <th class="actions-column">
                                    <g:if test="${!params.singleSelect}">
                                        <input class="check-all multiple" type="checkbox">
                                    </g:if>
                                </th>
                                <th><g:message code="visibility"/></th>
                                <th data-sortable="name"><g:message code="title"/></th>
                                <th><g:message code="category"/></th>
                                <th data-sortable="created"><g:message code="date"/></th>
                            </tr>
                            <g:set var="status" value="${[open: 'positive', hidden: 'negative', restricted: 'diplomatic']}"/>
                            <g:each in="${blogs}" var="blog">
                                <tr>
                                    <th class="actions-column" item="${blog.id}" type="blog">
                                        <input class="check-one ${params.singleSelect ? '' : 'multiple'} ${blog.id}" item="${blog.id}" type="checkbox" class="multiple" ${defaults?.contains(blog.id) ? 'checked="checked"' : ''}/>
                                    </th>
                                    <td class="status-column"><span class="status ${status[blog.visibility]}"></span></td>
                                    <td>${blog.name.encodeAsBMHTML()}</td>
                                    <td>${blog.categories.collect { it.name }.join(", ") }</td>
                                    <td>${blog.created.toAdminFormat(false, false, session.timezone)}</td>
                                </tr>
                            </g:each>
                        </table>
                    </div>
                    <div class="fee-footer">
                        <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
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