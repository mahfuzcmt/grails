<g:if test="${product.images?.size() > 0}">
    <span class="image-selector multiple">
        <span class="image-holder" image-id=""><span class="dump-image"><g:message code="no.image"/></span></span>
        <g:each in="${product.images}" var="image">
            <span class="image-holder" image-id="${image.id}">
                <img src="${appResource.getProductImageURL(image: image, size: 450)}">
            </span>
        </g:each>
    </span>
</g:if>
<g:else>
    <span class="not-found">
        <g:message code="no.product.image.found"/>
    </span>
</g:else>