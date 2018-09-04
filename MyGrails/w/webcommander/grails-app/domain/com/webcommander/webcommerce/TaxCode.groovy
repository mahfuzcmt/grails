package com.webcommander.webcommerce

import com.webcommander.common.CommonService
import com.webcommander.util.AppUtil
import grails.converters.JSON
import grails.util.Holders

class TaxCode {

    Long id
    String name
    String label
    String description
    Double rate = 0.0

    Boolean isDefault = false

    static constraints = {
        name(blank: false, size: 1..100, unique: true)
        label(nullable: true, maxSize: 100)
        description(nullable: true, maxSize: 255)
    }

    @Override
    boolean equals(Object obj) {
        if (!obj instanceof TaxCode) {
            return false;
        }
        if (this.id) {
            return id == obj.id
        }
        return super.equals(obj)
    }

    @Override
    int hashCode() {
        if (this.id) {
            return ("TaxCode: " + id).hashCode()
        }
        return super.hashCode();
    }

    static void initDefaults() {
        String fileLocation = Holders.servletContext.getRealPath("WEB-INF/dbEntries/tax-codes.json")
        List codes = JSON.parse new File(fileLocation).text
        for (Map data : codes) {
            TaxCode _code = TaxCode.findByName(data.name)
            if(!_code) {

                new TaxCode(name: data.name, label: data.label, description: data.description, rate: data.rate, isDefault: true).save(flush: true)

            }
        }

    }

}
