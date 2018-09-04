package com.webcommander.webcommerce

import com.webcommander.admin.Zone
import com.webcommander.common.CommonService
import com.webcommander.manager.HookManager
import com.webcommander.util.AppUtil
import grails.converters.JSON
import grails.util.Holders

class TaxRule {

    Long id
    String name
    String description

    Boolean isDefault = false

    TaxCode code
    String roundingType = "nearest"//up, down
    int decimalPoint = 2//0-9

    Collection<Zone> zones = []

    static hasMany = [zones: Zone]

    static constraints = {
        name(blank: false, maxSize: 100, unique: true)
        description(nullable: true)
        code(nullable: true)
    }

    @Override
    boolean equals(Object obj) {
        if (!obj instanceof TaxRule) {
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
            return ("TaxRule: " + id).hashCode()
        }
        return super.hashCode();
    }

    static void initDefaults() {
        String fileLocation = Holders.servletContext.getRealPath("WEB-INF/dbEntries/tax-rules.json")
        List rules = JSON.parse new File(fileLocation).text
        Iterator iterator = rules.iterator()
        while (iterator.hasNext()) {
            Map data = iterator.next();
            TaxRule _rule = TaxRule.findByName(data.name)
            if(!_rule) {

                TaxRule rule = new TaxRule(name: data.name, code: TaxCode.findByName(data.code), isDefault: true)
                data.zones.each {
                    Zone zone = Zone.findByName(it)
                    if(zone) {
                        rule.addToZones(zone)
                    }
                }
                rule.save(flush: true)
            }
        }

    }

}
