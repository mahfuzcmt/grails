<%@ page import="com.webcommander.tenant.TenantContext; com.webcommander.constants.*; com.webcommander.util.AppUtil" %>
<div class="right-panel grid-view panel">
    <div class="body">
        <div class="content">
            <g:if test="${categories.size() > 0}">
                <h4 class="group-label"><g:message code="categories"/>  (${categoriesCount})</h4>
                <g:set var="categoryAdminImage" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CATEGORY_IMAGE, "size")[NamedConstants.CATEGORY_IMAGE_SETTINGS.ADMIN]}"/>
                <g:set var="categoryImgWidth" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CATEGORY_IMAGE, NamedConstants.CATEGORY_IMAGE_SIZES.ADMIN_WIDTH)}"/>
                <g:set var="categoryImgHeight" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CATEGORY_IMAGE, NamedConstants.CATEGORY_IMAGE_SIZES.ADMIN_HEIGHT)}"/>
                <g:each in="${categories}" var="category">
                    <div class="grid-item category" auto-type-exclude="url" content-type="category" content-id="${category.id}" content-name="${category.name.encodeAsBMHTML()}" content-url="${category.url}" content-owner_id="${category.createdBy?.id}" title="<xmp>${category.name.encodeAsBMHTML()}</xmp>">
                        <span class="float-menu-navigator" content-type="category"></span>
                        <div class="image" >
                            <img  src="${appResource.getCategoryImageURL(category: category, imageSize: categoryAdminImage)}">
                        </div>
                        <div class="title">${category.name.encodeAsBMHTML()}</div>
                    </div>
                </g:each>
            </g:if>
            <g:if test="${products.size() > 0 || combinedProducts.size() > 0 || includedProducts.size() >0 }">
                <g:set var="productAdminImage" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.ADMIN]}"/>
                <g:set var="productImgWidth" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, NamedConstants.PRODUCT_IMAGE_SIZES.ADMIN_WIDTH)}"/>
                <g:set var="productImgHeight" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, NamedConstants.PRODUCT_IMAGE_SIZES.ADMIN_HEIGHT)}"/>
            </g:if>
            <%
                Closure renderProduct = { render_product, type, clazz, title ->
                    %>
                    <div auto-type-exclude="url" class="grid-item ${clazz}" content-type="${type}" content-id="${render_product.id}" content-name="${title.encodeAsBMHTML()}" content-url="${render_product.url.encodeAsBMHTML()}" content-owner_id="${render_product.createdBy?.id}" title="<xmp>${title.encodeAsBMHTML()}</xmp>">
                        <span class="float-menu-navigator" content-type="${type}" product-type="${render_product.productType}"></span>
                        <g:if test="${render_product.isInventoryEnabled}">
                            <span class="stock-status mark-icon ${render_product.availableStock < 1 ? 'out-of-stock' : (render_product.availableStock <= render_product.lowStockLevel ? 'low-stock' : 'in-stock')}"></span>
                        </g:if>
                        <div class="image" >
                            <img  alt="${render_product.images[0]?.altText}" src="${appResource.getAdminPanelProductImageURL(product: render_product, imageSize:productAdminImage.toString())}">
                        </div>
                        <span class="title" >${title.encodeAsBMHTML()}</span>
                    </div>
                    <%
                }
            %>
            <g:if test="${combinedProducts.size() > 0}">
                <h4 class="group-label"><g:message code="combined.product"/> (${combinedProductCount})</h4>
                <g:each in="${combinedProducts}" var="combinedProduct">
                    ${renderProduct(combinedProduct, 'combined', 'combined_product', combinedProduct.name)}
                </g:each>
            </g:if>
            <g:if test="${products.size() > 0}">
                <h4 class="group-label"><g:message code="products"/> (${productCount})</h4>
                <g:each in="${products}" var="product">
                    ${renderProduct(product, 'product', 'product', product.name)}
                </g:each>
            </g:if>
            <g:if test="${includedProducts.size() > 0}">
                <h4 class="group-label"><g:message code="included.products"/></h4>
                <g:each in="${includedProducts}" var="product">
                    ${renderProduct(product.includedProduct, 'product', 'product', product.label)}
                </g:each>
            </g:if>
        </div>
    </div>
    <div class="footer">
        <ui:perPageCountSelector/>
        <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
    </div>
</div>