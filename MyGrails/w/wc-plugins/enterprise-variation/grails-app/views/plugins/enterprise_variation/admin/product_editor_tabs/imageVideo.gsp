<%@ page import="org.apache.commons.io.FilenameUtils; com.webcommander.util.AppUtil" %>
<div class="toolbar-share">
    <span class="header-title"><g:message code="web.commerce"/> > <g:message code="product"/> > ${product.name.encodeAsBMHTML()} > <g:message code="variation"/> > ${details.name ?: product.name.encodeAsBMHTML() + "_" + details.id} > <g:message code="images.and.video"/> </span>
</div>
<div class="create-edit-form variation-image-video">
    <form action="${app.relativeBaseUrl()}enterpriseVariation/saveProperties" class="edit-popup-form image-form ${details.images ? 'enable' : ''}" enctype="multipart/form-data" method="post">
        <input type="hidden" name="id" value="${details.id}">
        <input type="hidden" name="type" value="image">
        <div class="form-section">
            <div class="form-section-info">
                <h3><g:message code="image.info"/></h3>
                <div class="info-content"><g:message code="section.text.image.info"/></div>
            </div>
            <input type="checkbox" class="multiple active-check" disable-also="image-disable" value="true" ${details.images ? 'checked' : ''}>
            <div class="form-section-container">
                <div class="overlay-panel ${details.images ? '' : 'disabled'}"></div>
                <input type="file" name="images" file-type="image" queue="variation-image-queue-${details.id}" multiple="true">
                <div id="variation-image-queue-${details.id}" class="multiple-image-queue"></div>
                <div class="product-image-container">
                    <div class="left-scroller scroll-navigator" style="display: none"></div><div class="right-scroller scroll-navigator" style="display: none"></div>
                    <div class="product-image-wrapper one-line-scroll-content">
                        <g:each in="${details.images ?: product.images}" var="image">
                            <div data-id="${image.id}" data-name="${image.name}" class="image-thumb">
                                <span class="float-menu-navigator" ></span>
                                <input type="hidden" name="imageId" value="${image.id}">
                                <div class="image-container">
                                    <img src="${appResource.getVariationImageUrl(image: image, sizeOrPrefix: "150")}">
                                </div>
                            </div>
                        </g:each>
                    </div>
                </div>
                <div class="form-row">
                    <button type="submit" class="submit-button image-disable" ${details.images ? '' : 'disabled="disabled"'}><g:message code="update"/></button>
                </div>
            </div>
        </div>
    </form>
    <div class="section-separator"></div>
    <form action="${app.relativeBaseUrl()}enterpriseVariation/saveProperties" class="edit-popup-form video-form ${details.videos ? 'enable' : ''}" enctype="multipart/form-data" method="post">
        <input type="hidden" name="id" value="${details.id}">
        <input type="hidden" name="type" value="video">
        <div class="form-section">
            <div class="form-section-info">
                <h3><g:message code="video.info"/></h3>
                <div class="info-content"><g:message code="section.text.video.info"/></div>
            </div>
            <input type="checkbox" class="multiple active-check" disable-also="video-disable" value="true" ${details.videos ? 'checked' : ''}>
            <div class="form-section-container">
                <div class="overlay-panel ${details.videos ? '' : 'disabled'}"></div>
                <input type="file" multiple="true" name="videos" file-type="video" queue="product-video-queue" >
                <div id="product-video-queue" class="multiple-video-queue"></div>
                <div class="product-video-container">
                    <div class="left-scroller scroll-navigator" style="display: none"></div><div class="right-scroller scroll-navigator" style="display: none"></div>
                    <div class="product-video-wrapper one-line-scroll-content">
                        <g:each in="${details.videos ?: product.videos}" var="video">
                            <div data-id="${video.id}" data-name="${video.name}" class="image-thumb">
                                <span class="tool-icon remove"></span>
                                <input type="hidden" name="videoId" value="${video.id}">
                                <div class="image-container">
                                    <img src="${video.thumbImage}">
                                </div>
                            </div>
                        </g:each>
                    </div>
                </div>
                <div class="form-row">
                    <button type="submit" class="submit-button video-disable" ${details.videos ? '' : 'disabled="disabled"'}><g:message code="update"/></button>
                </div>
            </div>
        </div>
    </form>
    <div class="section-separator"></div>
    <form action="${app.relativeBaseUrl()}enterpriseVariation/updateSpec" method="post" class="edit-popup-form downloadable-spec-form ${details.spec ? 'enable' : ''}" enctype="multipart/form-data">
        <input type="hidden" name="id" value="${details.id}">
        <div class="form-section">
            <div class="form-section-info">
                <h3><g:message code="downloadable.spec.info"/></h3>
                <div class="info-content"><g:message code="section.text.downloadable.spec.info"/></div>
            </div>
            <input type="checkbox" class="multiple active-check" disable-also="spec-disable" value="true" ${details.spec ? 'checked' : ''}>
            <div class="form-section-container">
                <div class="overlay-panel ${details.spec ? '' : 'disabled'}"></div>
                <div class="form-row">
                    <label><g:message code="spec.file"/></label>
                    <div class="thicker-row">
                        <input name="productSpec" type="file" size-limit="9097152" class="large" text-helper="no">
                    </div>
                </div>
                <div class="form-row">
                    <label></label>
                    <div class="spec-file-block">
                        <g:set var="spec" value="${details.spec ?: product.spec}"/>
                        <g:if test="${spec}">
                            <span class="file ${spec.name.substring(spec.name.lastIndexOf(".") + 1, spec.name.length())}">
                                <span class="tree-icon"></span>
                            </span>
                            <span class="name">${spec.name}</span>
                            <span class="tool-icon remove" file-name="${spec.name}"></span>
                        </g:if>
                    </div>
                </div>
                <div class="form-row">
                    <button type="submit" class="submit-button downloadable-spec-form-submit spec-disable" ${details.spec ? '' : 'disabled="disabled"'}><g:message code="update"/></button>
                </div>
            </div>
        </div>
    </form>

</div>