<%@ page import="com.webcommander.constants.NamedConstants" %>
<div class="right-panel table-view">

    <div class="header">
        <div class="left-header">
            <h3 class="title"><g:message code="tax.code"/></h3>
            <p class="description"><g:message code="tax.codes.description"/></p>
        </div>
        <div class="toolbar toolbar-right">
            <form class="search-form tool-group">
                <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
            </form>
        </div>
    </div>
    <div class="app-tab-content-container">
        <table class="content">
            <tr>
                <th><g:message code="name"/></th>
                <th><g:message code="label"/></th>
                <th><g:message code="rate"/></th>
                <th class="action-column"></th>
            </tr>
            <g:each in="${codes}" var="code">
                <tr>
                    <td>${code.name}</td>
                    <td>${code.label}</td>
                    <td>${code.rate}%</td>
                    <td class="column actions-column">
                        <span class="action-navigator collapsed" entity-id="${code.id}" entity-name="${code.name.encodeAsBMHTML()}"></span>
                    </td>
                </tr>
            </g:each>
        </table>
    </div>
    <div class="footer">
        <ui:perPageCountSelector/>
        <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
    </div>
</div>
