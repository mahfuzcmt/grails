<%@ page import="com.webcommander.content.Album" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="images"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar">
        <div class="tool-group">
            <ui:domainSelect class="medium album-selector" domain="${Album}" value="${params.id.toLong()}"/>
        </div>
    </div>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "remove")]}" keys="['', 'remove']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group toolbar-btn upload"><i></i><g:message code="upload.image"/></div>
        <div class="tool-group">
            <span class="toolbar-item switch-menu"><i></i></span>
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col class="select-column">
            <col class="name-column">
            <col class="image-column">
            <col class="description-column">
            <col class="order-column">
            <col class="actions-column">
        </colgroup>
        <tr>
            <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            <th><g:message code="image.name"/></th>
            <th><g:message code="image"/></th>
            <th><g:message code="description"/></th>
            <th><g:message code="ordering"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${albumImages}">
            <g:each in="${albumImages}" var="image">
                <tr>
                    <td class="select-column"><input entity-id="${image.id}" type="checkbox" class="multiple"></td>
                    <td>${image.name.encodeAsBMHTML()}</td>
                    <td>
                        <input type="hidden" name="originalImageSrc-${image.id}" value="${appResource.getAlbumImageURL(image:image)}">
                        <img class="item-logo" id="album-image-${image.id}" src="${appResource.getAlbumImageURL(image:image, sizeOrPrefix: "thumb")}"></td>
                    <td>${image.description?.truncate(100).encodeAsBMHTML()}</td>
                    <td class="order editable" entity-id="${image.id}" restrict="numeric">
                        ${image.idx}
                    </td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${image.id}" entity-name="${image.name.encodeAsBMHTML()}" entity-url="${appResource.getAlbumImageURL(image:image)}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="6"><g:message code="no.image.found"/></td>
            </tr>
        </g:else>
    </table>
</div>

<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>