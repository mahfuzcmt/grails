package com.webcommander.plugin.discount

import com.webcommander.admin.Customer
import com.webcommander.admin.CustomerService
import com.webcommander.admin.Operator
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.discount.util.DiscountDataUtil
import com.webcommander.plugin.discount.webcommerce.CustomDiscount
import com.webcommander.plugin.discount.webcommerce.coupon.DiscountCoupon
import com.webcommander.plugin.discount.webcommerce.coupon.DiscountCouponAssoc
import com.webcommander.plugin.discount.webcommerce.coupon.DiscountCouponCode
import com.webcommander.util.AppUtil
import com.webcommander.util.StringUtil
import grails.gorm.transactions.Transactional
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

/**
 * Created by sharif ul islam on 13/03/2018.
 */
@Transactional
class CouponService {

    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g

    CommonService commonService
    CustomerService customerService

    String generateCouponCode() {
        String couponCode = AppUtil.getConfig("discount", "coupon_code_prefix") + StringUtil.uuid.replace("-", "").substring(0, 10)
        if(!commonService.isUnique(DiscountCouponCode, [field: "code", value: couponCode])) {
            return generateCouponCode()
        }
        return couponCode
    }

    CustomDiscount prepareDiscountCoupon(CustomDiscount discount) {

        boolean editMode = discount.id ? true : false

        Operator createdBy = Operator.get(AppUtil.session.admin)

        //DiscountCoupon coupon = editMode ? discount.coupon : new DiscountCoupon()
        DiscountCoupon coupon = discount.coupon ? discount.coupon : new DiscountCoupon()
        if (!coupon.created) {
            coupon.createdBy = createdBy
            coupon.created = new Date().gmt()
        }

        DiscountCouponCode code = discount.coupon && discount.coupon.code ? discount.coupon.code : new DiscountCouponCode()
        code.code = discount.isCouponCodeAutoGenerate && !discount.defaultCouponCode ? generateCouponCode() : discount.defaultCouponCode
        coupon.code = code

        DiscountCouponAssoc assoc = coupon.assoc ? coupon.assoc : new DiscountCouponAssoc()
        assoc.isUniqueCodeEachCustomer = discount.isCreateUniqueCouponEachCustomer
        if (assoc.isUniqueCodeEachCustomer) {
            if (discount.assoc.isAppliedAllCustomer) {
                Map params = [:]
                params.status = DomainConstants.CUSTOMER_STATUS.ACTIVE
                List<Customer> customerList = customerService.getCustomers(params)
                assoc.codes = generateCustomerCouponCode(assoc, customerList)
            } else {
                assoc.codes = generateCustomerCouponCode(assoc, DiscountDataUtil.getAllCustomers(discount))
            }
        } else {
            assoc.codes = []
        }
        coupon.assoc = assoc

        discount.coupon = coupon
        discount.defaultCouponCode = coupon.code.code

        return discount
    }

    List generateCustomerCouponCode(DiscountCouponAssoc assoc, List<Customer> customers) {
        Collection<DiscountCouponCode> codes = []
        if (customers) {
            for (Customer customer : customers) {
                DiscountCouponCode couponCode = assoc.codes ? assoc.codes.find{it.customer.id == customer.id} : null
                if (!couponCode) {
                    couponCode = new DiscountCouponCode()
                    couponCode.assoc = assoc
                    couponCode.code = generateCouponCode()
                }

                couponCode.customer = customer
                codes.add(couponCode)
            }
        }

        // clean orphan codes
        def commons = assoc.codes.intersect(codes)
        def difference = assoc.codes.plus(codes)
        difference.removeAll(commons)
        if (difference) {
            difference*.delete();
        }

        return codes
    }

    boolean deleteCouponCodes (List<Long> ids) {
        if (ids.size() > 0) {
            List<DiscountCouponCode> deleteCodes = DiscountCouponCode.where { id in ids }.list();
            deleteCodes*.delete();
            return true;
        }
        return false
    }

    List<DiscountCouponCode> getCouponCodes (Map params) {
        def listMap = [max: params.max, offset: params.offset];
        return DiscountCouponCode.createCriteria().list(listMap) {
            and getCouponCodeCriteriaClosure(params);
            order(params.sort ?: "code", params.dir ?: "asc");
        }
    }

    Integer getCouponCodeCount (Map params) {
        return DiscountCouponCode.createCriteria().get {
            and getCouponCodeCriteriaClosure(params);
            projections {
                rowCount();
            }
        }
    }

    Boolean enableCode (Map params) {
        DiscountCouponCode code = DiscountCouponCode.get(params.id)
        if (code) {
            code.isActive = true
            code.save()
            return true
        }
        return false
    }

    Boolean disableCode (Map params) {
        DiscountCouponCode code = DiscountCouponCode.get(params.id)
        if (code) {
            code.isActive = false
            code.save()
            return true
        }
        return false
    }

    ///////////////////////////////////////////////////////////

    private Closure getCouponCodeCriteriaClosure(Map params) {
        def session = AppUtil.session;
        Closure closure = {
            if (params.searchText) {
                customer{
                    or {
                        ilike("firstName", "%${params.searchText.trim().encodeAsLikeText()}%")
                        ilike("lastName", "%${params.searchText.trim().encodeAsLikeText()}%")
                        ilike("userName", "%${params.searchText.trim().encodeAsLikeText()}%")
                    }
                }
            }

            if (params.assocId) {
                eq("assoc.id", params.assocId)
            }

            if (params.isActive) {
                eq("isActive", params.isActive)
            }

            if(params.ids) {
                inList("id", params.list("ids").collect {it.toLong()})
            }
        }
        return closure;
    }

}
