package com.webcommander.plugin.product_review

import com.webcommander.admin.Customer
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommanderMailService
import com.webcommander.config.StoreDetail
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.tenant.Thread
import com.webcommander.hibernate.ExpressionOrderSupportedDetachedCriteria
import com.webcommander.hibernate.OrderSubqueryExpression
import com.webcommander.manager.HookManager
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import grails.util.TypeConvertingMap
import org.hibernate.sql.JoinType

@Initializable
@Transactional
class ProductReviewService {
    CommanderMailService commanderMailService

    static void initialize() {
        AppEventManager.on("before-customer-delete", {id ->
            Customer customer = Customer.get(id)
            ProductReview.findAllByCustomer(customer)*.delete()
        })
        AppEventManager.on("before-product-delete", {id ->
            ProductReview.createCriteria().list {
                eq("product.id", id)
            }*.delete()
        })
        HookManager.register("availableProductsFilterList", { DetachedCriteria query, filterMap ->
            String orderBy
            if(filterMap["product-sorting"] == "RATING_ASC") {
                orderBy = "asc"
            } else if(filterMap["product-sorting"] == "RATING_DESC") {
                orderBy = "desc"
            }
            if(orderBy) {
                ExpressionOrderSupportedDetachedCriteria criteria = new ExpressionOrderSupportedDetachedCriteria(query)
                criteria.addOrder(new OrderSubqueryExpression(ProductReview.where {
                    def rev = ProductReview
                    eqProperty "prdt.id", "rev.product.id"
                }.sum("rating"), orderBy == "asc"))
                return criteria
            }
            return query
        })
    }

    private Closure getCriteriaClosure(TypeConvertingMap params) {
        def session = AppUtil.session
        return {
            if(params.forRender == true) {
                eq("isActive", true)
            }
            if (params.searchText || params.productId || params.productName || params.sort == "p.name") {
                createAlias("product", "p")
            }
            if(params.searchText || params.reviewerName) {
                createAlias("customer", "c", JoinType.LEFT_OUTER_JOIN)
            }
            if (params.searchText) {
                or {
                    ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
                    ilike("p.name", "%${params.searchText.trim().encodeAsLikeText()}%")
                    ilike("c.firstName", "%${params.searchText.trim().encodeAsLikeText()}%")
                    ilike("c.lastName", "%${params.searchText.trim().encodeAsLikeText()}%")
                }
            }
            if (params.reviewerName){
                or {
                    ilike("name", "%${params.reviewerName.trim().encodeAsLikeText()}%")
                    ilike("c.firstName", "%${params.reviewerName.trim().encodeAsLikeText()}%")
                    ilike("c.lastName", "%${params.reviewerName.trim().encodeAsLikeText()}%")
                }
            }
            if(params.productId) {
                eq("p.id", params.long("productId"))
            }
            if (params.productName) {
                ilike("p.name", "%${params.productName.trim().encodeAsLikeText()}%")
            }
            if(params.forRender == true) {
                eq("isActive", true)
            }
            if (params.dateFrom) {
                Date date = params.dateFrom.dayStart.gmt(session.timezone);
                ge("created", date);
            }
            if (params.dateTo) {
                Date date = params.dateTo.dayStart.gmt(session.timezone);
                le("created", date);
            }
            if (params.ratingFrom) {
                Double value = params.ratingFrom.toDouble();
                ge("rating", value);
            }
            if (params.ratingTo) {
                Double value = params.ratingTo.toDouble();
                le("rating", value);
            }
        }
    }

    List<ProductReview> getReviewList(TypeConvertingMap params) {
        return ProductReview.createCriteria().listDistinct {
            and getCriteriaClosure(params)
            order(params.sort ?: "id", params.dir ?: "desc")
            if (params.forRender) {
                isNotNull("review")
                ne("review", "")
            }
            firstResult params.offset.toInteger()
            maxResults params.max.toInteger()
        }
    }

    Integer getProductReviewCount(TypeConvertingMap params) {
        return ProductReview.createCriteria().get {
            projections {
                countDistinct "id"
            }
            and getCriteriaClosure(params)
        }
    }

    Integer getProductReviewCommentCount(TypeConvertingMap params) {
        return ProductReview.createCriteria().get {
            projections {
                countDistinct "id"
            }
            and getCriteriaClosure(params)
            isNotNull("review")
            ne("review", "")
        }
    }

    Double getAvgRatingByProduct(TypeConvertingMap params) {
        List<ProductReview> reviews = ProductReview.createCriteria().listDistinct {
            and getCriteriaClosure(params)
            gt("rating", new Double(0))
        }
        try {
            return reviews.sum { it.rating } / reviews.size();
        } catch (Exception e) {
            return 0.0;
        }

    }

    ProductReview getReview(Long id){
        return ProductReview.get(id);
    }

    def saveReview(Map params) {
        ProductReview review;
        if (params.id) {
            review = ProductReview.get(params.id)
        } else {
            review = new ProductReview();
            Product product = Product.proxy(params.productId)
            review.product = product;
        }
        if (params.customerId) {
            review.customer = Customer.proxy(params.customerId)
        } else {
            review.name = params.name;
            review.email = params.email
        }
        def showReview = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.REVIEW_RATING, "show_review");
        if (showReview == "immediately") {
            review.isActive = true;
        }
        review.review = params.review;
        params.score = params.score ? params.score.toDouble() : 0.0
        if(params.score < 0 || params.score > 5) {
            throw new ApplicationRuntimeException("rating.should.be.greater.then.0.or.less.then.5")
        }
        review.rating = params.score.toDouble()
        review.save()
        if(review.customer && review.isActive) {
            AppEventManager.fire("after-review-active", [review.customer, review.id])
        }
        sendReviewEmail(review)
        return !review.hasErrors()
    }

    Boolean changeStatus(Long id, String status) {
        ProductReview review = ProductReview.get(id)
        review.isActive = status == "active";
        review.merge();
        if(review.customer && review.isActive) {
            AppEventManager.fire("after-review-active", [review.customer, review.id])
        }
        return !review.hasErrors();
    }

    boolean deleteReview(Map params) {
        List<Long> ids = params.list("id").collect { it.toLong() };
        if (ids.size() > 0) {
            List<ProductReview> reviews = ProductReview.where { id in ids }.list();
            reviews*.delete();
            return true;
        }
        return false
    }

    private def sendReviewEmail(ProductReview review) {
        def storeDetail = StoreDetail.first()
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("product-review-notification")
        if (!macrosAndTemplate.emailTemplate.active) {
            return;
        }
        String name = ""
        String email = ""
        if(review.customer) {
            name = review.customer.fullName()
            email = review.customer.address.email
        } else {
            name = review.name
            email = review.email
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "customer_name" :
                    refinedMacros[it.key] = name.encodeAsBMHTML()
                    break;
                case "product_name" :
                    refinedMacros[it.key] = review.product?.name.encodeAsBMHTML()
                    break;
                case "product_review" :
                    refinedMacros[it.key] = review.review.encodeAsBMHTML() ?: ""
                    break;
                case "product_rating" :
                    refinedMacros[it.key] = review.rating ? "" + review.rating : ""
                    break;
            }
        }
        String recipient = storeDetail.address?.email
        Thread.start {
            AppUtil.initialDummyRequest()
            ProductReview.withNewSession {
                commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, recipient, email)
            }
        }
    }

    def getProductIds(def ratingProductIds, GrailsParameterMap parameters) {
        String to = parameters.score[1]
        String from = parameters.score[0]
        ratingProductIds = ProductReview.where {
            rating >= from.toDouble() && rating <= to.toDouble()
        }.list().product.id
        return ratingProductIds
    }
}
