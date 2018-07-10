<%@ page import="com.webcommander.util.AppUtil" %>
<div class="create-edit-form">
    <form action="${app.relativeBaseUrl()}productAdmin/updateImages" class="edit-popup-form image-form" enctype="multipart/form-data" method="post">
        <input type="hidden" name="id" value="${product.id}">
        <div class="form-section">
            <div class="form-section-info">
                <h3><g:message code="image.info"/></h3>
                <div class="info-content"><g:message code="section.text.image.info"/></div>
            </div>
            <div class="form-section-container">
                <input type="file" name="images" file-type="image" queue="product-image-queue-${product.id}" multiple="true">
                <div id="product-image-queue-${product.id}" class="multiple-image-queue"></div>
                <div class="product-image-container">
                    <div class="left-scroller scroll-navigator" style="display: none"></div><div class="right-scroller scroll-navigator" style="display: none"></div>
                    <div class="product-image-wrapper one-line-scroll-content">
                        <g:each in="${product.images}" var="image">
                            <div data-id="${image.id}" data-name="${image.name}" class="image-thumb">
                                <span class="float-menu-navigator" ></span>
                                <input type="hidden" name="imageId" value="${image.id}">
                                <div class="image-container">
                                    <img src="${appResource.getProductImageURL(image: image, size: "150")}">
                                </div>
                            </div>
                        </g:each>
                    </div>
                </div>
                <div class="form-row">
                    <button type="submit" class="submit-button"><g:message code="update"/></button>
                </div>
            </div>
        </div>
    </form>
    <div class="section-separator"></div>
    <form action="${app.relativeBaseUrl()}productAdmin/updateVideos" class="edit-popup-form video-form" enctype="multipart/form-data" method="post">
        <input type="hidden" name="id" value="${product.id}">
        <div class="form-section">
            <div class="form-section-info">
                <h3><g:message code="video.info"/></h3>
                <div class="info-content"><g:message code="section.text.video.info"/></div>
            </div>
            <div class="form-section-container">
                <input type="file" multiple="true" name="videos" file-type="video" queue="product-video-queue" >
                <div id="product-video-queue" class="multiple-video-queue"></div>
                <div class="product-video-container">
                    <div class="left-scroller scroll-navigator" style="display: none"></div><div class="right-scroller scroll-navigator" style="display: none"></div>
                    <div class="product-video-wrapper one-line-scroll-content">
                        <g:each in="${product.videos}" var="video">
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
                    <button type="submit" class="submit-button"><g:message code="update"/></button>
                </div>
            </div>
        </div>
    </form>
    <div class="section-separator"></div>
    <form action="${app.relativeBaseUrl()}productAdmin/updateSpec" method="post" class="edit-popup-form downloadable-spec-form" enctype="multipart/form-data">
        <input type="hidden" name="id" value="${product.id}">
        <div class="form-section">
            <div class="form-section-info">
                <h3><g:message code="downloadable.spec.info"/></h3>
                <div class="info-content"><g:message code="section.text.downloadable.spec.info"/></div>
            </div>
            <div class="form-section-container">
                <div class="form-row">
                    <label><g:message code="spec.file"/></label>
                    <div class="thicker-row">
                        <input name="productSpec" type="file" size-limit="9097152" class="large" text-helper="no">
                    </div>
                </div>
                <div class="form-row">
                    <label></label>
                        <div class="spec-file-block" style="display: ${product.spec ? '' : 'none;'}">
                            <g:if test="${product?.spec?.name}">
                                <span class="file ${product.spec.name.substring(product.spec.name.lastIndexOf(".") + 1, product.spec.name.length())}">
                                    <span class="tree-icon"></span>
                                </span>
                                <span class="name">${product.spec.name}</span>
                                <span class="tool-icon remove" file-name="${product.spec.name}"></span>
                            </g:if>
                        </div>
                </div>
                <div class="form-row">
                    <button type="submit" class="submit-button downloadable-spec-form-submit"><g:message code="update"/></button>
                </div>
            </div>
        </div>
    </form>
</div>