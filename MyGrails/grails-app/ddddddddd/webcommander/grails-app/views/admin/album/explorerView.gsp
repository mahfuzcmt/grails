<%@ page import="org.springframework.web.util.UriUtils" %>
<div class="right-panel grid-view">
    <div class="body">
        <div class="content">
            <div class="album-image-container">
                <input type="hidden" name="selectedAlbum" value="${albumId}">
                <g:each in="${albumImages}" var="ai">
                    <div class="grid-item album-image" content-type="albumImage" content-id="${ai.id}" content-name="${UriUtils.encodePath(ai.name, "UTF-8")}" content-url="${appResource.getAlbumImageURL(image:ai)}" title="${ai.name.encodeAsBMHTML()}">
                        <input type="hidden" name="imageId" value="${ai.id}">
                        <span class="float-menu-navigator" content-type="albumImage"></span>
                        <div class="image">
                            <input type="hidden" name="originalImageSrc-${ai.id}" value="${appResource.getAlbumImageURL(image:ai)}">
                            <img id="album-image-${ai.id}" src="${appResource.getAlbumImageURL(image:ai, sizeOrPrefix: "thumb")}">
                        </div>
                        <span class="title">${ai.name.encodeAsBMHTML()}</span>
                    </div>
                </g:each>
            </div>
        </div>
    </div>
    <div class="footer">
        <ui:perPageCountSelector/>
        <paginator total="${albumImageCount}" offset="${params.offset}" max="${params.max}"></paginator>
    </div>
</div>
