package com.webcommander.webcommerce

import com.webcommander.admin.Zone
import com.webcommander.calculator.TaxCalculator
import com.webcommander.common.CommonService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.util.AppUtil
import grails.converters.JSON
import grails.util.Holders

class TaxProfile {

    Long id
    String name
    String description

    Boolean isDefault = false

    Collection<TaxRule> rules = []

    static hasMany = [rules: TaxRule]

    static constraints = {
        name(blank: false, unique: true, maxSize: 255)
        description(nullable: true, maxSize: 255)
    }

    static transients = ['getTaxRules', 'getAppliedRule']

    Integer getProductCount(){
        return Product.where {
            taxProfile == this
        }.count()
    }

    @Override
    boolean equals(Object obj) {
        if (!obj instanceof TaxProfile) {
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
            return ("TaxProfile: " + id).hashCode()
        }
        return super.hashCode();
    }

    static void initDefaults() {
        String fileLocation = Holders.servletContext.getRealPath("WEB-INF/dbEntries/tax-profiles.json")
        List rules = JSON.parse new File(fileLocation).text
        Iterator iterator = rules.iterator()
        while (iterator.hasNext()) {
            Map data = (Map) iterator.next();
            TaxProfile _profile = TaxProfile.findByName(data.name)
            if(!_profile) {

                TaxProfile profile = new TaxProfile(name: data.name, description: data.description, isDefault: true)
                data.rules.each {
                    TaxRule rule = TaxRule.findByName(it)
                    if (rule) {
                        profile.addToRules(rule)
                    } else {
                        println("rule not found:::::::::::::::: "+it)
                    }
                }
                profile.save(flush: true)
            }
        }

    }

    static void initialize() {
        Closure _init = {

            if ( TaxProfile.countByIsDefault(true) == 0 ) {
                Zone.initialize()
                TaxCode.initDefaults()
                TaxRule.initDefaults()
                initDefaults()

                String defaultTaxProfile = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "default_tax_profile")
                if (!defaultTaxProfile) {
                    String countryMappedTaxProfile = NamedConstants.DEFAULT_COUNTRY_WITH_DEFAULT_TAX_PROFILE_MAPPING[DomainConstants.TAX_DEFAULT_COUNTRY_TYPE.AUSTRALIA]
                    if (countryMappedTaxProfile) {
                        TaxProfile profile = TaxProfile.findByName(countryMappedTaxProfile)
                        if (profile) {

                            SiteConfig siteConfig = SiteConfig.findByConfigKey("default_tax_profile")
                            siteConfig.value = profile.id.toString()
                            siteConfig.save(flush: true)

                            AppUtil.clearConfig(DomainConstants.SITE_CONFIG_TYPES.TAX)
                        }
                    }
                }

            }

        }
        if(SiteConfig.count()) {
            _init()
        } else {
            AppEventManager.one("site-config-bootstrap-init", "bootstrap-init", _init)
        }
    }

    List<TaxRule> getTaxRules(int max) {
        Integer count = rules.size()
        if(count) {
            if(max && max <= count) {
                count = max
            }
            return rules[0..count-1]
        }
        return [];
    }

    Map getAppliedRule() {
        def rule = TaxCalculator.getAppliedRule(this)
        if (!rule) {
            return [
                    roundingType: "nearest",
                    decimalPoint: 2
            ]
        }
        return [
            roundingType: rule?.roundingType,
            decimalPoint: rule?.decimalPoint
        ]
    }
}
