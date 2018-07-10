<%@ page import="com.webcommander.util.AppUtil" %>
<g:applyLayout name="_productwidget">
    <script type="text/javascript">
        var refer_settings = {
            product_id: '${productData.sku}',
            product_title: '${productData.name.encodeAsBMHTML()}',
            product_desc: '${productData.description.encodeAsJavaScript()}',
            product_image: '${appResource.getProductImageFullUrl(product: productData, imageSize: "")}',
            product_image_list: ['${productData.images.collect { app.siteBaseUrl() + it.urlInfix + it.name}.join("','")}'],
            product_price: ${productData.priceToDisplay},
            product_price_type: '${AppUtil.baseCurrency.code}',
            product_url: '${app.siteBaseUrl()}${productData.getLink()}',
            product_retailer_id: '${configs.api_key}'
        };
        var referPluginS = document.createElement('script');
        referPluginS.async = true;
        referPluginS.type = 'text/javascript';
        var useSSL = 'https:' == document.location.protocol;
        referPluginS.src = (useSSL ? 'https:' : 'http:') + '//www.referboard.com/js/referButton/refer_popup.js';
        var node = document.getElementsByTagName('script')[0];
        node.parentNode.insertBefore(referPluginS, node);
        var Refer_Opentop = Refer_Opentop || {};
        Refer_Opentop.cmd = Refer_Opentop.cmd || [];
    </script>

    <div id="referboard_button_div"> 
        <script type="text/javascript">
            Refer_Opentop.cmd.push(function () {
                Refer_Opentop.load_popup(refer_settings, 'referboard_button_div');
            });
        </script>
    </div> 
</g:applyLayout>