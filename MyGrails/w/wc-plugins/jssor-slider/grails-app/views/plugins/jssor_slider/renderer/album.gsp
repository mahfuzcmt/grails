<%@ page import="com.webcommander.plugin.jssor_slider.JssorSliderCaption; com.webcommander.Page; com.webcommander.content.AlbumImage" %>
<g:each in="${items as List<AlbumImage>}" status="i" var="image">
    <div class="slide slide-${i+1} ${config.hyperlink_images == "true" ? "hyper-link" : ""}">
    <img u="image" src="${appResource.getAlbumImageURL(image: image)}" link="${config.hyperlink_images == "true" ? image.imageLink() : ""}" target="${image.linkTarget ?: '#'}"/>
    <g:each in="${image.captionList() as List<JssorSliderCaption>}" var="caption" status="j">
        <div u=caption t="${caption.animation ?: ''}" du="${caption.duration}" d=${caption.delay} class="${caption.type} jssor-caption-${j+1}">
        <g:set var="text" value="${caption.text.encodeAsBMHTML()}"/>
        <g:if test="${caption.type == "title"}">
            <span class="text">${text}</span>
        </g:if>
        <g:elseif test="${caption.type == "button"}">
            <g:if test="${caption.url}">
                <a class="submit-button" href="${caption.url.encodeAsBMHTML()}" target="_blank">${caption.text.encodeAsBMHTML()}</a>
            </g:if>
            <g:else>
                <g:set var="button" value="${text?.split(";")}"/>
                <a class="submit-button" href="${button ? (button.size() > 1 ? button[1] : '') : ''}" target="_blank">${button ? button[0] : ''}</a>
            </g:else>
        </g:elseif>
        <g:else>
            ${caption.text}
        </g:else>
        </div>
    </g:each>
    </div>
</g:each>