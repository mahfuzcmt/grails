package com.webcommander.common

import com.webcommander.admin.City
import com.webcommander.constants.DomainConstants
import com.webcommander.content.Album
import com.webcommander.util.AppUtil
import com.webcommander.util.StringUtil
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.webcommerce.Address
import grails.util.Holders
import org.hibernate.sql.JoinType
import org.springframework.context.MessageSource
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
class CommonService {
    MessageSource messageSource

    static CommonService getInstance() {
        return Holders.grailsApplication.mainContext.getBean(CommonService)
    }
    /**
     * If offset be greater than count(total) then it handles safely
     * @return list of entity (if callback is not a closure) having offset of last page
     * @return void and call callback(max, offset, count) (if callback is a closure) with offset of last page
     */
    def withOffset(max, offset, count, callback) {
        max = max ?: -1;
        offset = offset ?: 0;
        if (max != -1) {
            if (max instanceof String) {
                max = max.toInteger();
            }
            if (offset instanceof String) {
                offset = offset.toInteger();
            }
            if (!count && !(callback instanceof Closure)) {
                count = callback.count();
            }
            if (offset >= count) {
                offset = (Math.floor((count -1) / max) * max).intValue()
            }
        }
        if (callback instanceof Closure) {
            return callback(max, offset, count);
        } else {
            return callback.list([max: max, offset: offset])
        }
    }

    String getNameForDomain(def domainObj){
        String proposed
        def domain = domainObj.getClass()
        if(domain == Album){
            proposed = domainObj.name
        }else {
            proposed = domainObj.name.sanitize()
        }
        String attempt;
        int attemptCount = domain.createCriteria().count {
            ilike("name", "${proposed.encodeAsLikeText()}%")
            if (domainObj.id) {
                ne("id", domainObj.id)
            }
        }
        while (true) {
            String suffix = attemptCount == 0 ? "" : ("-" + attemptCount);
            if (proposed.length() + suffix.length() > 100) {
                proposed = proposed.substring(0, 100 - suffix.length());
            }
            attempt = proposed + suffix;
            int c = domain.createCriteria().count {
                eq("name", attempt)
                if (domainObj.id) {
                    ne("id", domainObj.id)
                }
            }
            if (c > 0) {
                attemptCount++;
            } else {
                return attempt
            }
        }
    }


    String getUrlForDomain(def domainObj) {
        return getUrlForDomain(domainObj.getClass(), domainObj.name, domainObj.id)
    }

    String getUrlForDomain(Class domain, String name, Long id = null) {
        String proposed = name.sanitize()
        proposed = proposed == "-" ? domain.simpleName.sanitize() : proposed
        String attempt;
        int attemptCount = 0
        while (true) {
            String suffix = attemptCount == 0 ? "" : ("-" + attemptCount);
            if (proposed.length() + suffix.length() > 50) {
                proposed = proposed.substring(0, 50 - suffix.length());
            }
            attempt = proposed + suffix;
            int c = domain.createCriteria().count {
                eq("url", attempt)
                if (id) {
                    ne("id", id)
                }
            }
            if (c > 0) {
                attemptCount++;
            } else {
                return attempt
            }
        }
    }

    boolean isUnique(def domainObj, String field) {
        def domain = domainObj.class;
        Integer count = domain.createCriteria().count {
            if(domainObj.id) {
                ne("id", domainObj.id)
            }
            eq(field, domainObj[field])
        }
        return count == 0
    }

    boolean isUnique(def domain, Map params) {
        Integer count = domain.createCriteria().count {
            if(params.id){
                ne("id", params.id.toLong())
            }
            eq(params.field, params.value)
            if(params.compositeField){
                eq(params.compositeField, params.compositeValue)
            }
        }
        return count == 0
    }

    public Map responseForUniqueField(def domain, Long id, String field, String value, String customFieldName = null) {
        def element = domain.createCriteria().get {
            ne("id", id ?: 0)
            eq(field, value)
        }
        String existenceStatus = fieldExistenceStatus(element)
        return generateResponseForUniqueCheck(existenceStatus, customFieldName ?: field, value)
    }

    public String fieldExistenceStatus(def element) {
        if(element) {
            if(element.hasProperty("isInTrash") && element.isInTrash) {
                return "in-trash"
            } else {
                return "exist"
            }
        }
        return "not-exist"
    }

    public Map generateResponseForUniqueCheck(String existenceStatus, String field, String value) {
        if(existenceStatus == "in-trash") {
            return [status: "error", existenceStatus: "in-trash", message: messageSource.getMessage("provided.field.value.exists.in.trash", [field, value] as Object[], AppUtil.locale)]
        } else if(existenceStatus == "exist") {
            return [status: "error", existenceStatus: "exist", message: messageSource.getMessage("provided.field.value.exists", [field, value] as Object[], AppUtil.locale)]
        }
        return [status: "success", message: messageSource.getMessage("provided.field.available", [field, value] as Object[], AppUtil.locale)]
    }

    void checkCircularReferenceOfParent(def domain, Long child, Long parent) {
        if (child == parent) {
            throw new ApplicationRuntimeException("parent.can.not.self.child")
        }
        def node = domain.get(parent);
        while (node.parent != null) {
            if (node.parent.id == child) {
                throw new ApplicationRuntimeException("parent.can.not.be.descendant");
            }
            node = node.parent
        }
    }

    public String getSKUForDomain(def domain, String proposed = null) {
        if(!proposed) {
            String code = StringUtil.uuid.replace("-", "")
            proposed = (domain.name.substring(domain.name.lastIndexOf('.') + 1) + "-" + code).substring(0, 20).toUpperCase()
            return proposed
        }
        String attempt;
        int attemptCount = 0
        while (true) {
            String suffix = attemptCount == 0 ? "" : ("-" + attemptCount);
            if (proposed.length() + suffix.length() > 50) {
                proposed = proposed.substring(0, 50 - suffix.length());
            }
            attempt = proposed + suffix;
            int c = domain.createCriteria().count {
                eq("sku", attempt)
            }
            if (c > 0) {
                attemptCount++;
            } else {
                return attempt
            }
        }
    }

    String getCopyNameForDomain(def domainObj) {
        def domain = domainObj.getClass()
        String proposed = domainObj.name;
        String attempt = proposed + "";
        int attemptCount = 0
        while (true) {
            String suffix = " - " + messageSource.getMessage("copy", new Object[0], AppUtil.locale) + (attemptCount == 0 ? "" : (" " + attemptCount));
            if (proposed.length() + suffix.length() > 100) {
                proposed = proposed.substring(0, 100 - suffix.length())
            }
            attempt = proposed + suffix;
            int c = domain.createCriteria().count {
                eq("name", attempt)
            }
            if (c > 0) {
                attemptCount++ ;
            } else {
                return attempt;
            }
        }
    }

    public List getCitiesForState(Long stateId) {
        List cities = City.createCriteria().list {
            projections {
                distinct("name")
            }
            eq('state.id', stateId)
        }
        return cities
    }

    public List getCitiesForPostCode(String postCode) {
        List cities = City.createCriteria().list {
            projections {
                distinct("name")
            }
            eq('postCode', postCode)
        }
        return cities
    }

    public List getCitiesForCountryAndPostCode(Long countryId, String postCode) {
        List cities = City.createCriteria().list {
            createAlias("state", "s", JoinType.LEFT_OUTER_JOIN)
            eq("s.country.id", countryId)
            eq('postCode', postCode)
        }
        return cities
    }

    public List getCitiesForStateAndPostCode(Long stateId, String postCode) {
        if(!stateId) {
            return []
        }
        List cities = City.createCriteria().list {
            eq('state.id', stateId)
            and {
                eq('postCode', postCode)
            }
        }
        return cities
    }

    public Map addressToMap(Address address) {
        return [
                customer_name: address.firstName.encodeAsBMHTML() + address.lastName.encodeAsBMHTML() ?: "",
                first_name: address.firstName.encodeAsBMHTML()?: "",
                last_name: address.lastName.encodeAsBMHTML()?: "",
                address_line1: address.addressLine1.encodeAsBMHTML()?: "",
                address_line2: address.addressLine2.encodeAsBMHTML()?: "",
                state: address.state?.name ?: "",
                city: address.city?: "",
                post_code: address.postCode?: "",
                email: address.email?: "",
                phone: address.phone?: "",
                mobile: address.mobile?: "",
                fax: address.fax?: "",
                country: address.country.name?: ""
        ]
    }

    public Map checkMinimumPurchaseAmount(Double grandTotal){
        Map error = null
        String minimumPurchaseAmountStatus = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "enable_minimum_purchase_amount");
        Double minPurchaseAmount
        if(minimumPurchaseAmountStatus == "true" && grandTotal < (minPurchaseAmount = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "minimum_purchase_amount") as Double)){
            error = [message: "s:error.minimum.purchase.amount", macros: [total_amount: grandTotal.toPrice(), min_purchase_amount: minPurchaseAmount.toPrice(), currencySymbol: AppUtil.baseCurrency.symbol]]
        }
        return error
    }

}
