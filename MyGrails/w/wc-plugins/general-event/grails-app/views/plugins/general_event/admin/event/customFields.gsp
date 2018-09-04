<%@ page import="com.webcommander.plugin.general_event.constants.DomainConstants" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="event.custom.field"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-tool action-menu">
            <span class="tool-text"><g:message code="actions"/></span><span class="action-dropper collapsed"></span>
        </div>
        <div class="tool-group toolbar-btn create"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="table-view no-paginator-table-view">
    <div class="body">
        <table class="content">
            <colgroup>
                <col class="label-column">
                <col class="options-column">
                <col class="type-column">
                <col class="actions-column">
            </colgroup>
            <tr>
                <th><g:message code="field.label"/></th>
                <th><g:message code="options"/></th>
                <th><g:message code="type"/></th>
                <th class="actions-column"><g:message code="actions"/></th>
            </tr>
            <g:if test="${fields}">
                <g:each in="${fields}" var="field" status="i">
                    <tr>
                        <td>${field.label.encodeAsBMHTML()}</td>
                        <td>
                            <g:if test="${field.type == DomainConstants.EVENT_CHECKOUT_FIELD_TYPE.TEXT || field.type == DomainConstants.EVENT_CHECKOUT_FIELD_TYPE.LONG_TEXT}">
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
        <paginator total="${count}" offset="${0}" max="${0}"></paginator>
    </div>
</div>
