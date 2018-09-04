<%@ page import="com.webcommander.content.Section; com.webcommander.content.Article" %>
<div class="right-panel grid-view panel">
    <div class="body">
        <div class="content">
            <g:each in="${contents}" var="content">
                <g:if test="${content.value?.size() > 0}">
                    <h4 class="group-label"><g:message code="${content.key}s"/></h4>
                    <g:each in="${content.value}" var="section">
                        <div class="grid-item ${content.key}" content-type="${content.key}" content-id="${section.id}" content-name="${section.name.encodeAsBMHTML()}"
                             entity-owner_id="${section instanceof Section ? '' : section.createdBy?.id}" title="<xmp>${section.name.encodeAsBMHTML()}</xmp>">
                            <span class="float-menu-navigator" content-type="${content.key}"></span>
                            <span class="image"></span>
                            <div class="title">${section.name.encodeAsBMHTML()}</div>
                        </div>
                    </g:each>
                </g:if>
            </g:each>
        </div>
    </div>
    <div class="footer">
        <ui:perPageCountSelector/>
        <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
    </div>
</div>