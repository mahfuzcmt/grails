package com.webcommander.calculator

import com.webcommander.admin.Customer
import com.webcommander.admin.CustomerGroup
import com.webcommander.admin.Zone
import com.webcommander.constants.DomainConstants
import com.webcommander.models.AddressData
import com.webcommander.models.ProductData
import com.webcommander.util.AppUtil
import com.webcommander.util.NumberUtil
import com.webcommander.webcommerce.Currency
import com.webcommander.webcommerce.TaxCode
import com.webcommander.webcommerce.TaxProfile
import com.webcommander.webcommerce.TaxRule

class TaxCalculator {
    static TaxRule getAppliedRule(TaxProfile profile) {
        if(!profile) {
            return null
        }

        TaxRule appliedRule
        AddressData selectedAddress = AddressData.resolveAddress();

        if (!selectedAddress) {
            return null
        }

        for(TaxRule rule : profile.rules) {
            if(rule.zones) {
                if(AppUtil.matchAddressWithZones(selectedAddress, rule.zones)) {
                    appliedRule = rule
                }
            }
            if(appliedRule) {
                return appliedRule
            }
        }

        return resolveRestOfWorld(profile.rules)
    }

    static TaxRule resolveRestOfWorld(Collection<TaxRule> rules) {
        if(!rules) {
            return null
        }

        for(TaxRule rule : rules) {
            for (Zone zone : rule.zones) {
                if (DomainConstants.REST_OF_THE_WORLD.equals(zone.name)) {
                    return rule
                }
            }
        }

        return null
    }

    private static TaxCode getAppliedCode(TaxProfile profile) {
        if(!profile) {
            return null
        }
        return getAppliedRule(profile)?.code
    }

    static Map getTaxDetailForProduct(ProductData product) {

        Map noTaxResult = [
            code: false,
            baseAmount: 0,
            saleAmount: 0,
            expectAmount: 0,
            isTaxCodeFound: false
        ]

        TaxCode code = resolveTaxCode(product)

        if(!code) {
            return noTaxResult
        }

        return getTaxDetailForProduct(product, code)
    }

    static Map getTaxDetailForProduct(ProductData product, TaxCode code) {
        Map taxConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX)
        Boolean priceWithTax = taxConfig.is_price_with_tax.toBoolean()

        Map results = [
                code: code.label ?: code.name,
                baseAmount: product.basePrice * code.rate / 100,
                saleAmount: product.isOnSale ? product.salePrice * code.rate / 100 : 0,
                expectAmount: product.isExpectToPay ? (priceWithTax ? code.rate * product.expectToPayPrice / (100 + code.rate) : product.expectToPayPrice * code.rate / 100) : 0,
                rate: code.rate,
                isTaxCodeFound: true,
                taxCodeId: code.id,
        ]

        return results
    }

    static Double getTaxRate(TaxProfile profile) {
        TaxCode code = getAppliedCode(profile)
        if(!code) {
            return 0
        }
        return code.rate / 100
    }

    static Double getTax(TaxProfile profile, Double amount, boolean isInclusive = false) {
        TaxCode code = getAppliedCode(profile)
        if(!code) {
            return 0
        }
        return isInclusive ? (amount * code.rate / (code.rate + 100)) : (amount * code.rate / 100)
    }

    static Double getTax(TaxCode code, Double amount) {
        if(!code) {
            return 0
        }
        return amount * code.rate / 100
    }

    static TaxCode getLowestAppliedCodeFromCustomerGroups (Collection<CustomerGroup> groups) {
        if (!groups || groups.size() == 0) {
            return null
        }
        TaxCode code = null
        groups.each {
            if (it.defaultTaxCode && it.status.equals(DomainConstants.CUSTOMER_STATUS.ACTIVE)) {
                TaxCode curCode = TaxCode.findByName(it.defaultTaxCode)
                if (!code) {
                    code = curCode
                } else if (curCode && code && curCode.rate.doubleValue() < code.rate.doubleValue()) {
                    code = curCode
                }
            }
        }
        return code;
    }

    static TaxCode resolveTaxCode (ProductData product) {
        TaxCode code = null
        if (AppUtil.session.customer) {
            Customer customer = Customer.get(AppUtil.session.customer)
            if (customer.defaultTaxCode) {
                code = TaxCode.findByName(customer.defaultTaxCode)
            } else if (customer.groups.size() > 0) {
                code = getLowestAppliedCodeFromCustomerGroups(customer.groups)
            }
        }

        String configurationType = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "configuration_type");
        if (configurationType.equals(DomainConstants.TAX_CONFIGURATION_TYPE.MANUAL) && (code && code.isDefault)
            || configurationType.equals(DomainConstants.TAX_CONFIGURATION_TYPE.DEFAULT) && (code && !code.isDefault)
            ) {
            return null
        }

        if (!code) {
            TaxProfile profile = product.resolveTaxProfile()
            code = getAppliedCode(profile)
        }
        return code
    }



}