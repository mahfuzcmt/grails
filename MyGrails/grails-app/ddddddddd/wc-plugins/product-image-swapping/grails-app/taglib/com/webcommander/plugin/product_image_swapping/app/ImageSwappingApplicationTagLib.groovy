package com.webcommander.plugin.product_image_swapping.app

class ImageSwappingApplicationTagLib {
    static namespace = "imageSwappingApp"

    def productImageView = { attrs, body ->
        out << body()
        if(attrs.product.images.size() > 1) {
             String url = "${appResource.getProductResourceRelativePath(productId: attrs.product.id)}${pageScope.imageSize}-" + attrs.product.images[1].name
            out << "<input type='hidden' name='swapImageUrl' value='${url.encodeAsBMHTML()}'>"
        }
    }
}
