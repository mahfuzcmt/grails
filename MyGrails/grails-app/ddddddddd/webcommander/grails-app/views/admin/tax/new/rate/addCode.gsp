<%@ page import="com.webcommander.constants.NamedConstants" %>
<div class="rate-body">
    <form class="search-form search-block">
        <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
    </form>
    <div class="body code-table">
        <table>
            <colgroup>
                <col class="code-column">
                <col class="label-column">
                <col class="actions-column">
            </colgroup>
            <tbody>
            <g:each in="${codes}" var="code">
                <tr>
                    <td class="code-column">${code.name.encodeAsBMHTML()}</td>
                    <td class="label">
                        <span hidden="value">${code.label}</span><span class="tool-icon info" title="${code.rate} %"></span>
                    </td>
                    <td class="select-column">
                        <button class="submit-button add-code select-item" type="button" entity-id="${code.id}" entity-label="${code.label}"
                                entity-name="${code.name.encodeAsBMHTML()}" entity-rate="${code.rate}"><g:message code="select"/></button>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button create-new-code"><g:message code="create.new.code"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</div>