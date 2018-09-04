<%@ page import="com.webcommander.manager.HookManager" %>
<g:if test="${productData.spec}">
    <g:applyLayout name="_productwidget">
        <g:set var="path" value="${appResource.getProductSpecFileURL(productData: productData)}"/>
        <div class="info-row product-downloadable-spec">
            <a href="${path}" class="et_pdp_download_spec" et-category="link" target="_blank">
                <span class="file ${productData.spec.substring(productData.spec.lastIndexOf(".") + 1, productData.spec.length())}">
                    <span class="tree-icon"></span>
                </span>
                <span class="name">${productData.spec.encodeAsBMHTML()}</span>
            </a>
        </div>
    </g:applyLayout>
</g:if>