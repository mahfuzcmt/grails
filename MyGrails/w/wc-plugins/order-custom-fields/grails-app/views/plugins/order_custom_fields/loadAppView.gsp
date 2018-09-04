<%@ page import="com.webcommander.plugin.order_custom_fields.DomainConstants" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="fields"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "remove")]}" keys="['', 'remove']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group action-tool action-menu">
            <span class="tool-text"><g:message code="actions"/></span><span class="action-dropper collapsed"></span>
        </div>
        <div class="tool-group toolbar-btn create"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col style="width: 3%">
            <col style="width: 450px">
            <col>
            <col style="width: 200px">
            <col style="width: 150px">
        </colgroup>
        <tr>
            <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            <th><g:message code="field.label"/></th>
            <th><g:message code="options"/></th>
            <th><g:message code="type"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${fields}">
            <g:each in="${fields}" var="field" status="i">
                <tr>
                    <td class="select-column"><input entity-id="${field.id}" type="checkbox" class="multiple"></td>
                    <td>${field.label.encodeAsBMHTML()}</td>
                    <td>
                        <g:if test="${field.type == DomainConstants.ORDER_CHECKOUT_FIELD_TYPE.TEXT || field.type == DomainConstants.ORDER_CHECKOUT_FIELD_TYPE.LONG_TEXT}">
                            ${field.placeholder.encodeAsBMHTML()}
                        </g:if>
                        <g:else>
                            ${field.options.join(", ").encodeAsBMHTML()}
                        </g:else>
                    </td>
                    <td><g:message code="${field.type}"/></td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${field.id}" entity-label="${field.label.encodeAsBMHTML()}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="5"><g:message code="no.field.created"/> </td>
            </tr>
        </g:else>
    </table>
</div>

<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>
