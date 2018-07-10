<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="auto.pages"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search.page"/>"><button class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col style="width: 25%">
            <col style="width: 30%">
            <col style="width: 25%">
            <col style="width: 10%">
            <col style="width: 10%">
        </colgroup>
        <tr>
            <th><g:message code="name"/></th>
            <th><g:message code="page.title"/></th>
            <th><g:message code="layout"/></th>
            <th class="status-column"><g:message code="https.only"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${autoPages}">
            <g:each in="${autoPages}" var="page">
                <g:set var="pageName" value="${page.name.replace(".", "_")}"/>
                <g:if test="${((DomainConstants.ECOMMERCE_AUTO_GENERATED_PAGES_CHECKLIST[pageName] == true) && (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')) || (DomainConstants.ECOMMERCE_AUTO_GENERATED_PAGES_CHECKLIST[pageName] == null)}">
                    <tr class="${page.name == 'product' ? 'highlighted' : ''}">
                        <td><g:message code="${page.name}"/></td>
                        <td>${page.title.encodeAsBMHTML()}</td>
                        <td>${page.layout.name.encodeAsBMHTML()}</td>
                        <td class="status-column">
                            <span class="status ${page.isHttps ? "positive" : "negative"}" title="${page.isHttps ? g.message(code: 'yes') : g.message(code: 'no')}"></span>
                        </td>
                        <td class="actions-column">
                            <span class="action-navigator collapsed" entity-id="${page.id}" entity-name="${g.message(code: page.name).encodeAsBMHTML()}" ${page.editorEnable ?
                                    '' : 'disabled-menu-entries="content-edit"'}></span>
                        </td>
                    </tr>
                </g:if>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="5"><g:message code="no.auto.generated.page.found"/> </td>
            </tr>
        </g:else>
    </table>
</div>

<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>