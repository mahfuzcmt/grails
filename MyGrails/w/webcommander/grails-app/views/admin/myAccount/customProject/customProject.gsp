<div class="toolbar-share">
</div>
<div class="table-view app-tab-content-container">
    <div class="header">
        <span class="heading-title"><g:message code="custom.project"/></span>
        <div class="toolbar toolbar-right">
            <div class="tool-group toolbar-btn create create-new-project"><g:message code="create.new.project"/></div>
        </div>
    </div>
    <div class="body">
        <table class="content">
            <colgroup>
                <col class="info-column">
                <col class="stage-column">
                <col class="manage-column">
            </colgroup>
            <g:each in="${projects}" var="project">
                <tr>
                    <td class="info-column">
                        <div class="title"><span class="project-type">${project.type}</span>-<span class="project-name">${project.name}</span></div>
                        <div class="summary">${project.summary}</div>
                    </td>
                    <td class="stage-column">
                        <span class="column-header"><g:message code="stage"/></span>
                        <span class="column-value">${project.stage}</span>
                    </td>
                    <td class="status-column">
                        <span class="column-header"><g:message code="status"/></span>
                        <span class="column-value"></span>
                    </td>
                    <td class="mange-column">
                        <span class="icon-btn mange manage-project" project-id="${project.id}">manage</span>
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