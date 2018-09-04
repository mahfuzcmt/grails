<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil" %>
<g:applyLayout name="_productwidget">
    <g:set var="previewImageSize" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.DETAILS]}"/>
    <g:set var="infixUrl" value="${productData.images[0]?.urlInfix ?: "resources/product/product-" + product.id + "/"}"/>
    <g:set var="baseURl" value="${app.baseUrl()}" />
    <div class="social-media-wrapper product-${product.id}">
        <span class="social-media-share email" type="email"></span>
        <span class="social-media-share facebook" type="facebook"></span>
        <span class="social-media-share twitter" type="twitter"></span>
        <span class="social-media-share google-plus" type="google-plus"></span>
        <span class="social-media-share linkedin" type="linkedin"></span>
        <span class="social-media-share pinterest" type="pinterest"></span>
    </div>
    <script type="text/javascript">
        $(".social-media-wrapper.product-${product.id} .social-media-share").on("click", function() {
            var $this = $(this), type = $this.attr("type");
            <g:set var="url" value="${appResource.getProductImageFullUrl(product: productData, imageSize: previewImageSize)}"/>
            if(type == "email") {
                tellAFriendAboutProduct(${product.id})
            } else {
                shareOnSocialMedea(type,  "${baseURl}product/${product.url}", "${product.name.encodeAsBMHTML()}", "${url}")
            }
        })
    </script>
</g:applyLayout>