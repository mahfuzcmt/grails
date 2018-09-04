<g:set var="videos" value="${productData.videos}"/>
<g:if test="${videos.size() > 0}">
    <app:enqueueSiteJs src="video-js/video.min.js" scriptId="video-js" />
</g:if>

<%
    if(videos.size()) {
        request.css_cache.push("video-js/video-js.min.css")
    }
%>

<g:applyLayout name="_productwidget">
    <div class="bmui-tab">
        <div class="bmui-tab-header-container">
            <g:if test="${isDescriptionEnabled == "true"}">
                <div class="bmui-tab-header" data-tabify-tab-id="description">
                    <span class="title"><g:message code="product.description"/></span>
                </div>
            </g:if>
            <g:if test="${videos.size() > 0}">
                <div class="bmui-tab-header" data-tabify-tab-id="video">
                    <span class="title"><g:message code="video"/></span>
                </div>
            </g:if>
            <plugin:hookTag hookPoint="productInfoTabHeader" attrs="${[productId: product.id]}"/>
        </div>
        <div class="bmui-tab-body-container">
            <g:if test="${isDescriptionEnabled == "true"}">
                <div id="bmui-tab-description">
                    <span class="title">${productData.name.encodeAsBMHTML()}</span>
                    <span class="description">${productData.description}</span>
                </div>
            </g:if>
            <g:if test="${videos.size() > 0}">
                <div id="bmui-tab-video">
                    <g:include view="site/productVideo.gsp" model="[videos: videos]"/>
                </div>
            </g:if>
            <plugin:hookTag hookPoint="productInfoTabBody" attrs="${[:]}"/>
        </div>
    </div>
</g:applyLayout>