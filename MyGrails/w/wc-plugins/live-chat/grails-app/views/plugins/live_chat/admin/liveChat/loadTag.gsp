<div class="toolbar-share">
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col>
            <col style="width: 80px">
        </colgroup>
        <tr>
            <th><g:message code="name"/></th>
            <th class="actions-column"><g:message code="action"/></th>
        </tr>
        <g:each in="${tags}" var="tag" status="i">
            <tr entity-id="${tag.id}">
                <td class="editable" entity-id="${tag.id}">${tag.name.encodeAsBMHTML()}</td>
                <td class="actions-column">
                    <span class="tool-icon remove" entity-id="${tag.id}"></span>
                </td>
            </tr>
        </g:each>
            <tr class="last-row">
                <td><input class="td-full-width" type="text" name="name" validation="required maxlength[100]"></td>
                <td class="actions-column"><span class="tool-icon add add-tag" ></span></td>
            </tr>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>
