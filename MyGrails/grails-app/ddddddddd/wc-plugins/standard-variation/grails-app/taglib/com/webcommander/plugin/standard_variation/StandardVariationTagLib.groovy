package com.webcommander.plugin.standard_variation

import com.webcommander.plugin.variation.VariationService

class StandardVariationTagLib {
    static namespace = "standard"
    VariationService variationService

    def adminJss = { Map attrs, body ->
        out << body();
        if(variationService.allowedStandard()) {
            out << "<script type='text/javascript' src='${app.systemResourceBaseUrl()}plugins/standard-variation/js/admin/standard-variation.js'></script>";
        }
    }
}
