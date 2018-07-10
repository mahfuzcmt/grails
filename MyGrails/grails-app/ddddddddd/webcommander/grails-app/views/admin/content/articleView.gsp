<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.content.Section" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="articles"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar">
        <div class="tool-group">
            <ui:hierarchicalSelect class="medium section-selector" domain="${Section}" prepend="${["": g.message(code: "all.sections"), "root": g.message(code: "root")]}"/>
        </div>
    </div>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "copy"), g.message(code: "remove")]}" keys="['', 'copy', 'remove']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item add-filter" title="<g:message code="advance.search"/>"><i></i></span>
            <span class="toolbar-item remove-filter disabled" title="<g:message code="remove.search"/>"><i></i></span>
        </div>
        <div class="tool-group action-tool action-menu collapsed">
            <span class="tool-text"><g:message code="actions"/></span><span class="action-dropper"></span>
        </div>
        <div class="tool-group toolbar-btn create"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col class="select-column">
            <col class="visibility-column">
            <col class="article-column">
            <col class="section-column">
            <col class="created-column">
            <col class="updated-column">
            <col class="created-by-column">
            <col class="actions-column">
        </colgroup>
        <tr>
            <th class="select-column"><input type="checkbox" class="check-all multiple"></th>
            <th class="mark-column"><g:message code="visibility"/></th>
            <th><g:message code="article.name"/></th>
            <th><g:message code="section"/></th>
            <th><g:message code="created"/></th>
            <th><g:message code="updated"/></th>
            <th><g:message code="created.by"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${articles}">
            <g:each in="${articles}" var="article">
                <tr>
                    <g:set var="tooltipNotification" value="${[true: 'Published', false: 'Unpublished']}"/>
                    <td class="select-column"><input entity-id="${article.id}" type="checkbox" class="multiple"></td>
                    <td><span class="status ${article.isPublished ? DomainConstants.STATUS.POSITIVE : DomainConstants.STATUS.NEGATIVE}" title="${tooltipNotification[article.isPublished.toString()]}"></span></td>
                    <td>${article.name.encodeAsBMHTML()}</td>
                    <td>${article.section?.name?.encodeAsBMHTML()}</td>
                    <td>${article.created.toAdminFormat(true, false, session.timezone)}</td>
                    <td>${article.updated.toAdminFormat(true, false, session.timezone)}</td>
                    <td>${article.createdBy?.fullName?.encodeAsBMHTML()}</td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${article.id}" entity-name="${article.name.encodeAsBMHTML()}" entity-type="article" entity-owner_id="${article.createdBy?.id}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="8"><g:message code="no.article.created"/> </td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>