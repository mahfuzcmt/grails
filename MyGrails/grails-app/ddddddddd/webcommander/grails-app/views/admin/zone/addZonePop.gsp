<div class="zone-body">
    <form class="search-form search-block">
        <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
    </form>
    <div class="body zone-table">
        <table>
            <colgroup>
                <col class="zone-column">
                <col class="actions-column">
            </colgroup>
            <tbody>
                <g:each in="${zones}" var="zone">
                    <tr class="zone-row">
                        <td class="thumb-left">
                            <g:set var="countries" value="${zone.countries}"/>
                            <g:if test="${countries.size()}">
                                <span class="flag-icon ${countries[0].code.toLowerCase()}"></span>
                            </g:if>
                            <span class="title">${zone.name?.encodeAsBMHTML()}</span>
                        </td>
                        <td class="select-column">
                            <button class="submit-button add-zone select-item" type="button" entity-id="${zone.id}" entity-name="${zone.name.encodeAsBMHTML()}"><g:message code="select"/></button>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </div>
    <div class="button-line">
        <button type="button" class="submit-button create-zone"><g:message code="create.zone"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</div>