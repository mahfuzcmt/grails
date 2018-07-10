package com.webcommander.plugin.enterprise_variation

import com.webcommander.plugin.variation.VariationService

class EnterpriseVariationTagLib {
    static namespace = "enterprise"
    VariationService variationService

    def adminJss = { Map attrs, body ->
        out << body();
        if(variationService.allowedEnterprise()) {
            out << "<script type='text/javascript' src='${app.systemResourceBaseUrl()}plugins/enterprise-variation/js/admin/enterprise-variation.js'></script>";
        }
    }
}
