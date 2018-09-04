<g:if test="${option.type.standard == "image"}">
    <img class="image" title="${option.label.encodeAsBMHTML()}" src="${appResource.getVariationImageUrl(image: option, sizeOrPrefix: "16")}">
</g:if>
<g:elseif test="${option.type.standard == "color"}">
    <span class="color" title="${option.label.encodeAsBMHTML()}" style="background-color: ${option.value}"></span>
</g:elseif>
<g:else>
    <span class="text">${option.value.encodeAsBMHTML()}</span>
</g:else>