<div class="toolbar-share">
    <div class="toolbar toolbar-right ">
        <div class="tool-group toolbar-btn create"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            %{--<col class="select-column">--}%
            <col class="identifire-column">
            <col class="location-column">
            <col class="url-column">
            <col class="actions-column">
        </colgroup>
        <tr>
            %{--<th class="select-column"><input class="check-all multiple" type="checkbox"></th>--}%
            <th><g:message code="identifire"/></th>
            <th><g:message code="location"/></th>
            <th><g:message code="url"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:each in="${stores}" var="store" status="i">
            <tr class="${store.isDefault ? "default" : ""}">
                %{--<td class="select-column"><input entity-id="${store.id}" type="checkbox" class="multiple"></td>--}%
                <td>${store.identifire.encodeAsBMHTML()}</td>
                <td>${store.location.encodeAsBMHTML()}</td>
                <td>${store.url }</td>
                <td class="actions-column"><span class="action-navigator collapsed" entity-id="${store.id}" entity-name="${store.name.encodeAsBMHTML()}"></span></td>
            </tr>
        </g:each>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>
