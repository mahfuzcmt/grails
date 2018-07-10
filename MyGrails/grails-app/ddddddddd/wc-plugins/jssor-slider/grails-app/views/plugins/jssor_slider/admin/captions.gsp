<div class="header">
    <span class="item-group entity-count title">
        <g:message code="captions"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "remove")]}" keys="['', 'remove']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search search-form-submit"></button>
        </form>
        <div class="tool-group toolbar-btn create"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col class="type-column">
            <col class="caption-column">
            <col class="url-column">
            <col class="animation-column">
            <col class="duration-column">
            <col class="delay-column">
            <col class="actions-column">
        </colgroup>
        <tr>
            <th><g:message code="type"/></th>
            <th><g:message code="caption"/></th>
            <th><g:message code="url"/></th>
            <th><g:message code="animation"/></th>
            <th><g:message code="animation.duration"/></th>
            <th><g:message code="delay"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${captions}">
            <g:each in="${captions}" var="caption" status="i">
                <tr>
                    <td>${g.message(code: caption.type)}</td>
                    <td>${caption.text.encodeAsBMHTML()}</td>
                    <td>${caption.url.encodeAsBMHTML()}</td>
                    <td>${captionTransitions[caption.animation] ?: g.message(code: "none")}</td>
                    <td>${caption.duration}</td>
                    <td>${caption.delay}</td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${caption.id}" entity-image="${caption.image.id}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="7"><g:message code="no.caption.created"/></td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>