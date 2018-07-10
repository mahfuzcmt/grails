<%@ page import="com.webcommander.constants.Galleries; com.webcommander.constants.NamedConstants" %>
<div class="suported-gallery">
    <g:if test="${supportedGalleries}">
        <g:each in="${supportedGalleries}" var="slider">
            <license:allowed id="${Galleries.GALLERY_LICENSE[slider]}">
                <div class="gallery-item ${config.gallery == slider ? 'selected' : ''}" gallery-name="${slider}">
                    <img src="${app.systemResourceBaseUrl() + Galleries.TYPES[slider].thumb}">
                    <h4 class="item-details">${NamedConstants.GALLERY_NAMES[slider]}</h4>
                </div>
            </license:allowed>
        </g:each>
    </g:if>
    <g:else>
        <span class="no-supported-gallery">None of the current installed sliders support ${contentType} </span>
    </g:else>
</div>