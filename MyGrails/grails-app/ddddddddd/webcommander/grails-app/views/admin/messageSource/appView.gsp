<%@ page import="com.webcommander.constants.NamedConstants" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="messages"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group locale-selection-group chosen-container">
            <label><g:message code="locale"/></label>
            <g:select class="medium" name="locale" from="${NamedConstants.LOCALES}" optionKey="key" optionValue="value" value="${params.locale}"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col style="width: 43%"/>
            <col style="width: 50%"/>
            <col style="width: 7%"/>
        </colgroup>
        <tr>
            <th><g:message code="key"/></th>
            <th><g:message code="message"/> </th>
            <th class="actions-column"><g:message code="action"/> </th>
        </tr>
        <g:if test="${messages}">
            <g:each in="${messages}" var="message">
                <tr entity-id="${message.id}">
                    <td class="editable key">${message.messageKey.encodeAsBMHTML()}</td>
                    <td class="editable message">${message.message.encodeAsBMHTML()}</td>
                    <td class="actions-column">
                        <span class="tool-icon remove" entity-id="${message.id}" title="<g:message code="remove" />"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="3"><g:message code="no.message.created"/></td>
            </tr>
        </g:else>
        <tr class="last-row">
            <td><input name="key" type="text" class="td-full-width" placeholder="<g:message code="key"/>" validation="maxlength[200]"></td>
            <td><input name="message" type="text" class="td-full-width" placeholder="<g:message code="message"/>" validation="maxlength[200]"></td>
            <td class="actions-column"><span class="tool-icon add add-row"></span></td>
        </tr>
    </table>
</div>

<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>