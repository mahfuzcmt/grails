<%@ page import="com.webcommander.content.Section; com.webcommander.webcommerce.Category" %>
<div class="tab-accordion-content">
    <form class="search-form search-block">
        <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
    </form>
    <div class="content-table">
        <table>
            <colgroup>
                <col class="name-column">
                <col class="actions-column">
            </colgroup>
            <tbody>
            <g:each in="${children}" var="child">
                <tr>
                    <td class="name"><span class="value">${child.name.encodeAsBMHTML()}</span></td>
                    <td class="select-column">
                        <button class="submit-button add-content" entity-id="${child.id}" entity-name="${child.name.encodeAsBMHTML()}"><g:message code="select"/></button>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
</div>