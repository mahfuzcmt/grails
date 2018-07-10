<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<g:set var="imageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.GRIDVIEW]}"/>
<g:if test="${productList.size() == 1}">
    <td>&nbsp;</td>
</g:if>
<g:each in="${productList}" var="product">
    <g:set var="url" value="${appResource.getProductImageFullUrl(product: product, imageSize: imageSize)}"/>
    <td style="padding: 10px">
        <p style="text-align:center; height:135px;"><img style="vertical-align:middle; max-width:100%; max-height:135px;" src="${url}" alt="${product.altText.encodeAsBMHTML()}" /></p>
        <p style="font-family:Arial, Helvetica, sans-serif; font-size:13px; font-weight:bold; line-height: 20px; margin-bottom:0; text-align:center;">${product.sku.encodeAsBMHTML()}</p>
        <p style="font-family:Arial, Helvetica, sans-serif; font-size:14px; line-height: 20px; margin-bottom:0; margin-top: 5px; text-align:center;">${product.name.encodeAsBMHTML()}</p>
        <p style="font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height: 20px; margin-bottom:0; height: 40px; overflow:hidden; text-align:center;">${product.summary?.truncate(30)}</p>
        <p style="font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height: 20px; margin-bottom:0; text-align:center; width:190px;"><a style="font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height: 20px; margin-bottom:0;" href="${app.baseUrl() + "product/" + product.url}">View Product</a></p>
    </td>
</g:each>
<g:if test="${productList.size() <= 2}">
    <td>&nbsp;</td>
</g:if>
