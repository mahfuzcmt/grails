package com.webcommander.report

import com.webcommander.AppResourceTagLib
import com.webcommander.admin.Customer
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.util.AppUtil
import com.webcommander.util.DateUtil
import com.webcommander.util.TrashUtil
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.OrderItem
import com.webcommander.webcommerce.Payment
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import com.webcommander.webmarketting.NewsletterSubscriber
import grails.transaction.NotTransactional
import groovy.sql.Sql
import groovy.time.TimeCategory
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.hibernate.sql.JoinType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class ReportingService {

    ProductService productService
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g;
    def dataSource
    @Autowired
    @Qualifier("com.webcommander.AppResourceTagLib")
    AppResourceTagLib appResource

    @NotTransactional
    List getQuicks(List types, Integer duration = null) {
        Date startTime
        Date endTime
        if(duration) {
            endTime = new Date().gmt()
            startTime = endTime - duration.days
        }
        Map orders = Order.executeQuery("select new Map(sum(i.quantity * i.price - i.discount) as sale, sum(i.quantity) as item) from Order o inner join o.items i where o.orderStatus = :status" + (duration ? " and o.created >= :start and o.created <= :end" : ""), [status: DomainConstants.ORDER_STATUS.COMPLETED] + (duration ? [start: startTime, end: endTime] : [:]))[0]
        Long orderCount = Order.createCriteria().count {
            eq "orderStatus", DomainConstants.ORDER_STATUS.COMPLETED
            if(duration) {
                lte "created", endTime
                gte "created", startTime
            }
        }
        return types.collect {
            if(it.title ==  "total.orders") {
                Map results = [:]
                results.id = it.id
                results.title = it.title
                results.class = it.uiClass
                results.value = orderCount
                return results
            }
            else if(it.title == "total.customers") {
                Map results = [:]
                results.id = it.id
                results.title = it.title
                results.class = it.uiClass
                results.value = Customer.createCriteria().count {
                    eq "isInTrash", false
                    eq "status", DomainConstants.CUSTOMER_STATUS.ACTIVE
                    if(duration) {
                        lte "created", startTime
                        gte "created", endTime
                    }
                }
                return results
            }
            else if(it.title == "total.sales") {
                Map results = [:]
                results.id = it.id
                results.title = it.title
                results.class = it.uiClass
                results.value = orders.sale ?: 0
                results.isCurrency = true
                return results
            }
            else if(it.title == "gross.sales") {
                Map results = [:]
                results.id = it.id
                results.title = it.title
                results.class = it.uiClass
                results.value = Order.executeQuery("select sum(shippingCost + shippingTax + handlingCost + totalSurcharge + (select sum(i.quantity * i.price + i.tax - i.discount) from o.items i)) from Order o where o.orderStatus = :status" + (duration ? " and o.created <= :start and o.created >= :end" : ""), [status: DomainConstants.ORDER_STATUS.COMPLETED] + (duration ? [start: startTime, end: endTime] : [:]))[0] ?: 0
                results.isCurrency = true
                return results
            }
            else if(it.title == "total.item.sold") {
                Map results = [:]
                results.id = it.id
                results.title = it.title
                results.class = it.uiClass
                results.value = orders.item ?: 0
                return results
            }
            else if(it.title == "average.order.value") {
                Map results = [:]
                results.id = it.id
                results.title = it.title
                results.class = it.uiClass
                try {
                    results.value = (orders.sale / orderCount)
                } catch (Exception e) {
                    results.value = 0.0
                }
                results.isCurrency = true
                return results
            }
            else if(it.title == "canceled.orders") {
                Map results = [:]
                results.id = it.id
                results.title = it.title
                results.class = it.uiClass
                results.value = Order.createCriteria().count {
                    eq "orderStatus", DomainConstants.ORDER_STATUS.CANCELLED
                    if(duration) {
                        lte "created", startTime
                        gte "created", endTime
                    }
                }
                return results
            }
        }
    }

    List latestSoldProducts(Integer duration) {
        Date endTime = new Date().gmt()
        Date startTime
        use TimeCategory, {
            startTime = endTime - duration.hours
        }
        return OrderItem. createCriteria().list {
            projections {
                property("productId")
                property("productType")
                property("productName")
                property("o.created")
                property("bc.name")
                property("bs.name")
                groupProperty("id")
                sqlProjection("group_concat(v1_.variation separator '~') variation", "variation", new org.hibernate.type.StringType())
            }
            createAlias("variations", "v", JoinType.LEFT_OUTER_JOIN)
            createAlias("order", "o")
            createAlias("o.billing", "ba")
            createAlias("ba.country", "bc")
            createAlias("ba.state", "bs", JoinType.LEFT_OUTER_JOIN)
            ne "o.orderStatus", DomainConstants.ORDER_STATUS.CANCELLED
            eq "o.paymentStatus", DomainConstants.ORDER_PAYMENT_STATUS.PAID
            gte "o.created", startTime
            order "o.created", "desc"
            maxResults 5
        }.collect {
            [productId: it[0], productType: it[1], productName: it[2], created: it[3], country: it[4], state: it[5], entryId: it[6], variation: it[7]]
        }
    }

    List latestActivities(Integer duration) {
        Date endTime = new Date().gmt()
        Date startTime
        use TimeCategory, {
            startTime = endTime - duration.hours
        }
        List orders = Order.executeQuery("select new Map('order' as type, count(i) as count, sum(i.price * i.quantity) as amount, o.created as time, bc.name as country, bs.name as state) from Order o inner join o.items i inner join o.billing ba inner join ba.country bc inner join ba.state bs where o.created >= ? group by o.id order by o.created desc", [startTime], [max: 5])
        List customers = Customer.executeQuery("select new Map('customer' as type, c.firstName as fname, c.lastName as lname, c.created as time, cc.name as country, cs.name as state) from Customer c inner join c.address a inner join a.country cc inner join a.state cs where c.created >= ? order by c.created desc", [startTime], [max: 5])
        List subscribers = NewsletterSubscriber.executeQuery("select new Map('subscriber' as type, n.firstName as fname, n.lastName as lname, n.email as email, n.created as time) from NewsletterSubscriber n where n.created >= ? order by n.created desc", [startTime], [max: 5])
        return (orders + customers + subscribers).sort { a, b -> b.time <=> a.time}.safeSubList(0, 5)
    }

    void populateSummaryNImages(List products) {
        products.each { product ->
            if(product.productType == NamedConstants.CART_OBJECT_TYPES.PRODUCT) {
                Product item = Product.get(product.productId)
                if(item) {
                    if(item.images.size()) {
                        product.image = appResource.getProductResourceRelativePath(productId: product.productId) + "150-" + item.images[0].name
                    }
                    product.summary = item.summary
                }
                if(!product.image) {
                    product.image = appResource.getProductDefaultImageWithPrefix("150")
                }
            } else {
                HookManager.hook("populateSummaryNImages", null, product)
            }
        }
    }

    List loadProductByProperties(Map params) {
        Map duration = getDuration(params)
        Sql sql = new Sql(dataSource)
        String group
        String where = ""
        if(!params.chart && params.filterKey && params.filterValue) {
            String fieldName
            if(params.filterKey == "type") {
                fieldName = "p.product_type"
            } else if(params.filterKey == "category") {
                fieldName = "c.name"
            } else if(params.filterKey == "name") {
                fieldName = "p.name"
            } else {
                fieldName = "i.price"
            }
            where = fieldName + " = ? and "
        }
        if(!params.chart) {
            group = "p.id, v.variation"
        } else if(params.xaxis == "type") {
            group = "p.product_type"
        } else if(params.xaxis == "category") {
            group = "c.id"
        } else if(params.xaxis == "name") {
            group = "p.id, v.variation"
        } else {
            group = "i.price"
        }
        return sql.rows("select p.product_type as type, c.name as category, (case when v.variation is null then p.name else concat(p.name, ' (', v.variation, ')') end) as name, i.price as price, sum(i.quantity) as sold, sum(i.price * i.quantity) as gross from product p inner join order_item i on i.product_id = p.id inner join orders o on o.id = i.order_id left outer join category c on c.id = p.parent_id left outer join (select group_concat(ov.variation separator ':') as variation, ov.item_id as item_id from order_product_variations ov group by ov.item_id) v on v.item_id = i.id where $where o.payment_status = ? and o.created >= ? and o.created <= ? group by $group", (where ? [params.filterValue] : []) + [DomainConstants.ORDER_PAYMENT_STATUS.PAID, duration.start, duration.end])
    }

    List loadProductByCustomers(Map params) {
        Map duration = getDuration(params)
        Sql sql = new Sql(dataSource)
        String group
        String where = ""
        if(!params.chart && params.filterKey && params.filterValue) {
            String fieldName
            if(params.filterKey == "customer.name") {
                fieldName = "concat(c.first_name, ' ', c.last_name)"
            } else if(params.filterKey == "customer.email") {
                fieldName = "c.user_name"
            } else if(params.filterKey == "customer.type") {
                fieldName = "c.is_company"
                if(params.filterValue.toLowerCase() == 'company') {
                    params.filterValue = 1
                } else {
                    params.filterValue = 0
                }
            } else if(params.filterKey == "customer.sex") {
                fieldName = "c.sex"
            }
            where = fieldName + " = ? and "
        }
        if(!params.chart) {
            group = "c.id"
        } else if(params.xaxis == "customer.name") {
            group = "concat(c.first_name, ' ', c.last_name)"
        } else if(params.xaxis == "customer.email") {
            group = "c.id"
        } else if(params.xaxis == "customer.type") {
            group = "c.is_company"
        } else if(params.xaxis == "customer.sex") {
            group = "c.sex"
        }
        return sql.rows("select concat(c.first_name, ' ', c.last_name) as cname, c.user_name as cemail, case when c.is_company = 1 then 'company' else 'individual' end as ctype, c.sex as csex, sum(i.quantity) as sold, sum(i.price * i.quantity) as gross from order_item i inner join orders o on o.id = i.order_id inner join customer c on c.id = o.customer_id where $where i.product_type = ? and o.payment_status = ? and o.created >= ? and o.created <= ? group by $group", (where ? [params.filterValue] : []) + [NamedConstants.CART_OBJECT_TYPES.PRODUCT, DomainConstants.ORDER_PAYMENT_STATUS.PAID, duration.start, duration.end])
    }

    List loadProductByBillingAddress(Map params) {
        Map duration = getDuration(params)
        Sql sql = new Sql(dataSource)
        String group
        String where = ""
        if(!params.chart && params.filterKey && params.filterValue) {
            String fieldName
            if(params.filterKey == "billing.country") {
                fieldName = "c.name"
            } else if(params.filterKey == "billing.region") {
                fieldName = "s.name"
            } else if(params.filterKey == "billing.city") {
                fieldName = "a.city"
            }
            where = fieldName + " = ? and "
        }
        if(!params.chart) {
            group = "c.name, s.name, a.city"
        } else if(params.xaxis == "billing.country") {
            group = "c.name"
        } else if(params.xaxis == "billing.region") {
            group = "s.name"
        } else if(params.xaxis == "billing.city") {
            group = "a.city"
        }
        return sql.rows("select c.name as country, s.name as region, a.city as city, sum(i.quantity) as sold, sum(i.price * i.quantity) as gross from order_item i inner join orders o on o.id = i.order_id inner join address a on a.id = o.billing_id inner join country c on c.id = a.country_id left outer join state s on s.id = a.state_id where $where i.product_type = ? and o.payment_status = ? and o.created >= ? and o.created <= ? group by $group", (where ? [params.filterValue] : []) + [NamedConstants.CART_OBJECT_TYPES.PRODUCT, DomainConstants.ORDER_PAYMENT_STATUS.PAID, duration.start, duration.end])
    }

    List loadProductByShippingAddress(Map params) {
        Map duration = getDuration(params)
        Sql sql = new Sql(dataSource)
        String group
        String where = ""
        if(!params.chart && params.filterKey && params.filterValue) {
            String fieldName
            if(params.filterKey == "shipping.country") {
                fieldName = "c.name"
            } else if(params.filterKey == "shipping.region") {
                fieldName = "s.name"
            } else if(params.filterKey == "shipping.city") {
                fieldName = "a.city"
            }
            where = fieldName + " = ? and "
        }
        if(!params.chart) {
            group = "c.name, s.name, a.city"
        } else if(params.xaxis == "shipping.country") {
            group = "c.name"
        } else if(params.xaxis == "shipping.region") {
            group = "s.name"
        } else if(params.xaxis == "shipping.city") {
            group = "a.city"
        }
        return sql.rows("select c.name as country, s.name as region, a.city as city, sum(i.quantity) as sold, sum(i.price * i.quantity) as gross from order_item i inner join orders o on o.id = i.order_id inner join address a on a.id = o.shipping_id inner join country c on c.id = a.country_id left outer join state s on s.id = a.state_id where $where i.product_type = ? and o.payment_status = ? and o.created >= ? and o.created <= ? group by $group", (where ? [params.filterValue] : []) + [NamedConstants.CART_OBJECT_TYPES.PRODUCT, DomainConstants.ORDER_PAYMENT_STATUS.PAID, duration.start, duration.end])
    }

    List loadProductByPeriod(Map params) {
        Map duration = getDuration(params)
        Sql sql = new Sql(dataSource)
        String group
        String where = ""
        if(!params.chart && params.filterKey && params.filterValue) {
            String fieldName
            if(params.filterKey == "year") {
                fieldName = "year(o.created)"
            } else if(params.filterKey == "month") {
                fieldName = "month(o.created)"
            }
            where = fieldName + " = ? and "
        }
        if(!params.chart) {
            group = "year(o.created), month(o.created)"
        } else if(params.xaxis == "year") {
            group = "year(o.created)"
        } else if(params.xaxis == "month") {
            group = "month(o.created)"
        }
        return sql.rows("select year(o.created) as year, month(o.created) as month, sum(i.quantity) as sold, sum(i.price * i.quantity) as gross from order_item i inner join orders o on o.id = i.order_id where $where i.product_type = ? and o.payment_status = ? and o.created >= ? and o.created <= ? group by $group", (where ? [params.filterValue] : []) + [NamedConstants.CART_OBJECT_TYPES.PRODUCT, DomainConstants.ORDER_PAYMENT_STATUS.PAID, duration.start, duration.end])
    }

    List loadOrderByStatus(Map params) {
        Map duration = getDuration(params)
        Sql sql = new Sql(dataSource)
        String where = ""
        if(!params.chart && params.filterKey && params.filterValue) {
            String fieldName = "o.order_status"
            where = fieldName + " = ? and "
        }
        return sql.rows("select o.order_status as status, sum(oi.sale) as sale, sum(oi.discount) as discount, sum(o.shipping_cost) as shipping, sum(oi.tax) as tax, count(o.id) as count from orders o inner join (select sum(i.price * i.quantity) as sale, sum(i.discount) as discount, sum(i.tax) as tax, i.order_id as oid from order_item i group by i.order_id) oi on oi.oid = o.id where $where o.created >= ? and o.created <= ? group by o.order_status", (where ? [params.filterValue] : []) + [duration.start, duration.end])
    }

    List loadOrderByCustomers(Map params) {
        Map duration = getDuration(params)
        Sql sql = new Sql(dataSource)
        String group
        String where = ""
        if(!params.chart && params.filterKey && params.filterValue) {
            String fieldName
            if(params.filterKey == "customer.email") {
                fieldName = "c.user_name"
            } else if(params.filterKey == "customer.type") {
                fieldName = "c.is_company"
                params.filterValue = params.filterValue.toLowerCase() == "company" ? 1 : 0
            } else if(params.filterKey == "customer.sex") {
                fieldName = "c.sex"
            } else {
                fieldName = "concat(c.first_name, ' ', c.last_name)"
            }
            where = fieldName + " = ? and "
        }
        if(!params.chart) {
            group = "c.id"
        } else if(params.xaxis == "customer.email") {
            group = "c.user_name"
        } else if(params.xaxis == "customer.type") {
            group = "c.is_company"
        } else if(params.xaxis == "customer.sex") {
            group = "c.sex"
        } else {
            group = "concat(c.first_name, ' ', c.last_name)"
        }
        return sql.rows("select concat(c.first_name, ' ', c.last_name) as cname, c.user_name as cemail, case when is_company = 1 then 'company' else 'individual' end as ctype, c.sex as csex, sum(oi.sale) as sale, sum(oi.discount) as discount, sum(o.shipping_cost) as shipping, sum(oi.tax) as tax, count(o.id) as count from orders o inner join customer c on c.id = o.customer_id inner join (select sum(i.price * i.quantity) as sale, sum(i.discount) as discount, sum(i.tax) as tax, i.order_id as oid from order_item i group by i.order_id) oi on oi.oid = o.id where $where o.created >= ? and o.created <= ? group by $group", (where ? [params.filterValue] : []) + [duration.start, duration.end])
    }

    List loadOrderByBillingAddress(Map params) {
        Map duration = getDuration(params)
        Sql sql = new Sql(dataSource)
        String group
        String where = ""
        if(!params.chart && params.filterKey && params.filterValue) {
            String fieldName
            if(params.filterKey == "billing.country") {
                fieldName = "c.name"
            } else if(params.filterKey == "billing.region") {
                fieldName = "s.name"
            } else if(params.filterKey == "billing.city") {
                fieldName = "a.city"
            }
            where = fieldName + " = ? and "
        }
        if(!params.chart) {
            group = "c.name, s.name, a.city"
        } else if(params.xaxis == "billing.country") {
            group = "c.name"
        } else if(params.xaxis == "billing.region") {
            group = "s.name"
        } else if(params.xaxis == "billing.city") {
            group = "a.city"
        }
        return sql.rows("select c.name as country, s.name as region, a.city as city, sum(oi.sale) as sale, sum(oi.discount) as discount, sum(o.shipping_cost) as shipping, sum(oi.tax) as tax, count(o.id) as count from orders o inner join address a on a.id = o.billing_id inner join country c on c.id = a.country_id inner join (select sum(i.price * i.quantity) as sale, sum(i.discount) as discount, sum(i.tax) as tax, i.order_id as oid from order_item i group by i.order_id) oi on oi.oid = o.id left outer join state s on s.id = a.state_id where $where o.created >= ? and o.created <= ? group by $group", (where ? [params.filterValue] : []) + [duration.start, duration.end])
    }

    List loadOrderByShippingAddress(Map params) {
        Map duration = getDuration(params)
        Sql sql = new Sql(dataSource)
        String group
        String where = ""
        if(!params.chart && params.filterKey && params.filterValue) {
            String fieldName
            if(params.filterKey == "shipping.country") {
                fieldName = "c.name"
            } else if(params.filterKey == "shipping.region") {
                fieldName = "s.name"
            } else if(params.filterKey == "shipping.city") {
                fieldName = "a.city"
            }
            where = fieldName + " = ? and "
        }
        if(!params.chart) {
            group = "c.name, s.name, a.city"
        } else if(params.xaxis == "shipping.country") {
            group = "c.name"
        } else if(params.xaxis == "shipping.region") {
            group = "s.name"
        } else if(params.xaxis == "shipping.city") {
            group = "a.city"
        }
        return sql.rows("select c.name as country, s.name as region, a.city as city, sum(oi.sale) as sale, sum(oi.discount) as discount, sum(o.shipping_cost) as shipping, sum(oi.tax) as tax, count(o.id) as count from orders o inner join address a on a.id = o.shipping_id inner join country c on c.id = a.country_id inner join (select sum(i.price * i.quantity) as sale, sum(i.discount) as discount, sum(i.tax) as tax, i.order_id as oid from order_item i group by i.order_id) oi on oi.oid = o.id left outer join state s on s.id = a.state_id where $where o.created >= ? and o.created <= ? group by $group", (where ? [params.filterValue] : []) + [duration.start, duration.end])
    }

    List loadOrderByPeriod(Map params) {
        Map duration = getDuration(params)
        Sql sql = new Sql(dataSource)
        String group
        String where = ""
        if(!params.chart && params.filterKey && params.filterValue) {
            String fieldName
            if(params.filterKey == "year") {
                fieldName = "year(o.created)"
            } else if(params.filterKey == "month") {
                fieldName = "month(o.created)"
            }
            where = fieldName + " = ? and "
        }
        if(!params.chart) {
            group = "year(o.created), month(o.created)"
        } else if(params.xaxis == "year") {
            group = "year(o.created)"
        } else if(params.xaxis == "month") {
            group = "month(o.created)"
        }
        return sql.rows("select year(o.created) as year, month(o.created) as month, sum(oi.sale) as sale, sum(oi.discount) as discount, sum(o.shipping_cost) as shipping, sum(oi.tax) as tax, count(o.id) as count from orders o inner join (select sum(i.price * i.quantity) as sale, sum(i.discount) as discount, sum(i.tax) as tax, i.order_id as oid from order_item i group by i.order_id) oi on oi.oid = o.id where $where o.created >= ? and o.created <= ? group by $group", (where ? [params.filterValue] : []) + [duration.start, duration.end])
    }

    List loadPaymentByProperties(Map params) {
        Map duration = getDuration(params)
        String where = ""
        if(!params.chart && params.filterKey && params.filterValue) {
            String fieldName = "g.name"
            where = fieldName + " = ? and "
        }
        return Payment.executeQuery("select new Map(g.name as method, count(g.id) as trcount, sum(case when p.status = ? then p.amount else 0 end) as refund, sum(case when p.status = ? then p.amount else 0 end) as paid) from Payment p, PaymentGateway g where $where p.gatewayCode = g.code and (p.status = ? or p.status = ?) and p.created >= ? and p.created <= ? group by g.name order by p.created desc", (where ? [params.filterValue] : []) + [DomainConstants.PAYMENT_STATUS.REFUNDED, DomainConstants.PAYMENT_STATUS.SUCCESS, DomainConstants.PAYMENT_STATUS.REFUNDED, DomainConstants.PAYMENT_STATUS.SUCCESS, duration.start, duration.end])
    }

    List loadPaymentByStatus(Map params) {
        Map duration = getDuration(params)
        String where = ""
        if(!params.chart && params.filterKey && params.filterValue) {
            String fieldName = "p.status"
            where = fieldName + " = ? and "
        }
        return Payment.executeQuery("select new Map(p.status as status, count(p.status) as trcount, sum(case when p.status = ? then p.amount else 0 end) as refund, sum(case when p.status = ? then p.amount else 0 end) as paid) from Payment p where $where (p.status = ? or p.status = ?) and p.created >= ? and p.created <= ? group by p.status order by p.created desc", (where ? [params.filterValue] : []) + [DomainConstants.PAYMENT_STATUS.REFUNDED, DomainConstants.PAYMENT_STATUS.SUCCESS, DomainConstants.PAYMENT_STATUS.REFUNDED, DomainConstants.PAYMENT_STATUS.SUCCESS, duration.start, duration.end])
    }

    List loadPaymentByCustomers(Map params) {
        Map duration = getDuration(params)
        String group
        String where = ""
        if(!params.chart && params.filterKey && params.filterValue) {
            String fieldName
            if(params.filterKey == "customer.email") {
                fieldName = "c.userName"
            } else if(params.filterKey == "customer.type") {
                fieldName = "c.isCompany"
                params.filterValue = params.filterValue.toLowerCase() == "company" ? 1 : 0 
            } else if(params.filterKey == "customer.sex") {
                fieldName = "c.sex"
            }
            where = fieldName + " = ? and "
        }
        if(!params.chart) {
            group = "id"
        } else if(params.xaxis == "customer.email") {
            group = "userName"
        } else if(params.xaxis == "customer.type") {
            group = "isCompany"
        } else if(params.xaxis == "customer.sex") {
            group = "sex"
        } else {
            group = "id"
        }
        return Payment.executeQuery("select new Map(concat(c.firstName, ' ', c.lastName) as name, c.userName as email, c.isCompany as company, c.sex as sex, count(c.id) as trcount, sum(case when p.status = ? then p.amount else 0 end) as refund, sum(case when p.status = ? then p.amount else 0 end) as paid) from Customer c, Payment p where $where p.order.customerId = c.id and (p.status = ? or p.status = ?) and p.created >= ? and p.created <= ? group by c.$group order by p.created desc", (where ? [params.filterValue] : []) + [DomainConstants.PAYMENT_STATUS.REFUNDED, DomainConstants.PAYMENT_STATUS.SUCCESS, DomainConstants.PAYMENT_STATUS.REFUNDED, DomainConstants.PAYMENT_STATUS.SUCCESS, duration.start, duration.end])
    }

    List loadPaymentByBillingAddress(Map params) {
        Map duration = getDuration(params)
        String group
        String where = ""
        if(!params.chart && params.filterKey && params.filterValue) {
            String fieldName
            if(params.filterKey == "billing.country") {
                fieldName = "a.country.name"
            } else if(params.filterKey == "billing.region") {
                fieldName = "ps.name"
            } else if(params.filterKey == "billing.city") {
                fieldName = "a.city"
            }
            where = fieldName + " = ? and "
        }
        if(!params.chart) {
            group = "a.country.name, ps.name, a.city"
        } else if(params.xaxis == "billing.country") {
            group = "a.country.name"
        } else if(params.xaxis == "billing.region") {
            group = "ps.name"
        } else if(params.xaxis == "billing.city") {
            group = "a.city"
        }
        return Payment.executeQuery("select new Map(a.country.name as country, ps.name as region, a.city as city, count(p.id) as trcount, sum(case when p.status = ? then p.amount else 0 end) as refund, sum(case when p.status = ? then p.amount else 0 end) as paid) from Payment p inner join p.order o inner join o.billing a left outer join a.state ps where $where (p.status = ? or p.status = ?) and p.created >= ? and p.created <= ? group by $group order by p.created desc", (where ? [params.filterValue] : []) + [DomainConstants.PAYMENT_STATUS.REFUNDED, DomainConstants.PAYMENT_STATUS.SUCCESS, DomainConstants.PAYMENT_STATUS.REFUNDED, DomainConstants.PAYMENT_STATUS.SUCCESS, duration.start, duration.end])
    }

    List loadPaymentByShippingAddress(Map params) {
        Map duration = getDuration(params)
        String group
        String where = ""
        if(!params.chart && params.filterKey && params.filterValue) {
            String fieldName
            if(params.filterKey == "shipping.country") {
                fieldName = "a.country.name"
            } else if(params.filterKey == "shipping.region") {
                fieldName = "ps.name"
            } else if(params.filterKey == "shipping.city") {
                fieldName = "a.city"
            }
            where = fieldName + " = ? and "
        }
        if(!params.chart) {
            group = "a.country.name, ps.name, a.city"
        } else if(params.xaxis == "shipping.country") {
            group = "a.country.name"
        } else if(params.xaxis == "shipping.region") {
            group = "ps.name"
        } else if(params.xaxis == "shipping.city") {
            group = "a.city"
        }
        return Payment.executeQuery("select new Map(a.country.name as country, ps.name as region, a.city as city, count(p.id) as trcount, sum(case when p.status = ? then p.amount else 0 end) as refund, sum(case when p.status = ? then p.amount else 0 end) as paid) from Payment p inner join p.order o inner join o.shipping a left outer join a.state ps where $where (p.status = ? or p.status = ?) and p.created >= ? and p.created <= ? group by $group order by p.created desc", (where ? [params.filterValue] : []) + [DomainConstants.PAYMENT_STATUS.REFUNDED, DomainConstants.PAYMENT_STATUS.SUCCESS, DomainConstants.PAYMENT_STATUS.REFUNDED, DomainConstants.PAYMENT_STATUS.SUCCESS, duration.start, duration.end])
    }

    List loadPaymentByMonth(Map params) {
        Map duration = getDuration(params)
        String group
        String where = ""
        if(!params.chart && params.filterKey && params.filterValue) {
            String fieldName
            if(params.filterKey == "payment.month") {
                fieldName = "month(p.created)"
            } else if(params.filterKey == "payment.year") {
                fieldName = "year(p.created)"
            }
            where = fieldName + " = ? and "
        }
        if(!params.chart) {
            group = "year(p.created), month(p.created)"
        } else if(params.xaxis == "payment.month") {
            group = "month(p.created)"
        } else if(!params.xaxis || params.xaxis == "payment.year") {
            group = "year(p.created)"
        }
        return Payment.executeQuery("select new Map(year(p.created) as year, month(p.created) as month, count(p.id) as trcount, sum(case when p.status = ? then p.amount else 0 end) as refund, sum(case when p.status = ? then p.amount else 0 end) as paid) from Payment p where $where (p.status = ? or p.status = ?) and p.created >= ? and p.created <= ? group by $group order by p.created desc", (where ? [params.filterValue] : []) + [DomainConstants.PAYMENT_STATUS.REFUNDED, DomainConstants.PAYMENT_STATUS.SUCCESS, DomainConstants.PAYMENT_STATUS.REFUNDED, DomainConstants.PAYMENT_STATUS.SUCCESS, duration.start, duration.end])
    }

    List loadTaxByMonth(Map params) {
        Map duration = getDuration(params)
        String where = ""
        if(!params.chart && params.filterKey && params.filterValue) {
            String fieldName
            if(params.filterKey == "year") {
                fieldName = "year(o.created)"
            } else {
                fieldName = "month(o.created)"
            }
            where = fieldName + " = ? and "
        }
        return Order.executeQuery("select new Map(sum(i.tax) as total, year(o.created) as year, month(o.created) as month) from OrderItem i inner join i.order o where $where o.orderStatus != ? and o.paymentStatus = ? and o.created >= ? and o.created <= ? group by year(o.created), month(o.created) order by o.created desc", (where ? [params.filterValue] : []) + [DomainConstants.ORDER_STATUS.CANCELLED, DomainConstants.ORDER_PAYMENT_STATUS.PAID, duration.start, duration.end])
    }

    List loadChartDataForProduct(params) {
        String yDimValueProp
        if(!params.yaxis) {
            params.yaxis = "units.sold"
        }
        if(params.yaxis == "units.sold") {
            yDimValueProp = "sold"
        } else if(params.yaxis == "gross.sales") {
            yDimValueProp = "gross"
        }
        String suffix = params.reportCode.substring(11).camelCase()
        if(!params.xaxis) {
            if(params.reportCode == "product.by.properties") {
                params.xaxis = "type"
            } else if(params.reportCode == "product.by.customers") {
                params.xaxis = "customer.name"
            } else if(params.reportCode == "product.by.billing.address") {
                params.xaxis = "billing.country"
            } else if(params.reportCode == "product.by.shipping.address") {
                params.xaxis = "shipping.country"
            } else if(params.reportCode == "product.by.period") {
                params.xaxis = "year"
            }
        }
        def dimensionFormatter = {
            if(params.xaxis == "type") {
                g.message(code: it.type)
            } else if(params.xaxis == "category") {
                it.category
            } else if(params.xaxis == "name") {
                it.name
            } else if(params.xaxis == "price") {
                it.price
            } else if(params.xaxis == "customer.name") {
                it.cname
            } else if(params.xaxis == "customer.email") {
                it.cemail
            } else if(params.xaxis == "customer.type") {
                g.message(code: it.ctype)
            } else if(params.xaxis == "customer.sex") {
                g.message(code: it.csex)
            } else if(params.xaxis == "billing.country") {
                it.country
            } else if(params.xaxis == "billing.region") {
                it.region
            } else if(params.xaxis == "billing.city") {
                it.city
            } else if(params.xaxis == "shipping.country") {
                it.country
            } else if(params.xaxis == "shipping.region") {
                it.region
            } else if(params.xaxis == "shipping.city") {
                it.city
            } else if(params.xaxis == "year") {
                it.year
            } else if(params.xaxis == "month") {
                g.message(code: DateUtil.MONTHS_FULL_LOWER[it.month - 1])
            }
        }
        params.chart = true
        List payments = this."loadProductBy$suffix"(params)
        return payments.collect {
            [dimension: dimensionFormatter(it), (params.yaxis): it[yDimValueProp]]
        }
    }

    List loadChartDataForOrder(params) {
        String yDimValueProp
        if(!params.yaxis) {
            params.yaxis = "total.sales"
        }
        if(params.yaxis == "total.sales") {
            yDimValueProp = "sale"
        } else if(params.yaxis == "total.shipping") {
            yDimValueProp = "shipping"
        } else if(params.yaxis == "total.taxes") {
            yDimValueProp = "tax"
        } else if(params.yaxis == "total.discounts") {
            yDimValueProp = "discount"
        } else {
            yDimValueProp = "count"
        }
        String suffix = params.reportCode.substring(9).camelCase()
        if(!params.xaxis) {
            if(params.reportCode == "order.by.status") {
                params.xaxis = "status"
            } else if(params.reportCode == "order.by.customers") {
                params.xaxis = "customer.name"
            } else if(params.reportCode == "order.by.billing.address") {
                params.xaxis = "billing.country"
            } else if(params.reportCode == "order.by.shipping.address") {
                params.xaxis = "shipping.country"
            } else if(params.reportCode == "order.by.period") {
                params.xaxis = "year"
            }
        }
        def dimensionFormatter = {
            if(params.xaxis == "status") {
                g.message(code: it.status)
            } else if(params.xaxis == "customer.name") {
                it.cname
            } else if(params.xaxis == "customer.email") {
                it.cemail
            } else if(params.xaxis == "customer.type") {
                g.message(code: it.ctype)
            } else if(params.xaxis == "customer.sex") {
                g.message(code: it.csex)
            } else if(params.xaxis == "billing.country") {
                it.country
            } else if(params.xaxis == "billing.region") {
                it.region
            } else if(params.xaxis == "billing.city") {
                it.city
            } else if(params.xaxis == "shipping.country") {
                it.country
            } else if(params.xaxis == "shipping.region") {
                it.region
            } else if(params.xaxis == "shipping.city") {
                it.city
            } else if(params.xaxis == "year") {
                it.year
            } else if(params.xaxis == "month") {
                g.message(code: DateUtil.MONTHS_FULL_LOWER[it.month - 1])
            }
        }
        params.chart = true
        List payments = this."loadOrderBy$suffix"(params)
        return payments.collect {
            [dimension: dimensionFormatter(it), (params.yaxis): it[yDimValueProp]]
        }
    }

    List loadChartDataForPayment(params) {
        String yDimValueProp
        if(!params.yaxis) {
            params.yaxis = "transaction.count"
        }
        if(params.yaxis == "total.refunds") {
            yDimValueProp = "refund"
        } else if(params.yaxis == "total.payments") {
            yDimValueProp = "paid"
        } else {
            yDimValueProp = "trcount"
        }
        String suffix = params.reportCode.substring(11).camelCase()
        if(!params.xaxis) {
            if(params.reportCode == "payment.by.properties") {
                params.xaxis = "payment.method"
            } else if(params.reportCode == "payment.by.status") {
                params.xaxis = "payment.status"
            } else if(params.reportCode == "payment.by.customers") {
                params.xaxis = "customer.name"
            } else if(params.reportCode == "payment.by.billing.address") {
                params.xaxis = "billing.country"
            } else if(params.reportCode == "payment.by.shipping.address") {
                params.xaxis = "shipping.country"
            } else if(params.reportCode == "payment.by.month") {
                params.xaxis = "payment.month"
            }
        }
        def dimensionFormatter = {
            if(params.xaxis == "payment.method") {
                g.message(code: it.method)
            } else if(params.xaxis == "payment.status") {
                g.message(code: it.status)
            } else if(params.xaxis == "customer.email") {
                it.email
            } else if(params.xaxis == "customer.type") {
                g.message(code: it.company ? 'company' : 'individual')
            } else if(params.xaxis == "customer.sex") {
                g.message(code: it.sex)
            } else if(params.xaxis == "customer.name") {
                it.name
            } else if(params.xaxis == "billing.country") {
                it.country
            } else if(params.xaxis == "billing.region") {
                it.region
            } else if(params.xaxis == "billing.city") {
                it.city
            } else if(params.xaxis == "shipping.country") {
                it.country
            } else if(params.xaxis == "shipping.region") {
                it.region
            } else if(params.xaxis == "shipping.city") {
                it.city
            } else if(params.xaxis == "payment.year") {
                it.year
            } else if(params.xaxis == "payment.month") {
                g.message(code: DateUtil.MONTHS_FULL_LOWER[it.month - 1])
            }
        }
        params.chart = true
        List payments = this."loadPaymentBy$suffix"(params)
        return payments.collect {
            [dimension: dimensionFormatter(it), (params.yaxis): it[yDimValueProp]]
        }
    }

    List loadChartDataForTax(params) {
        Map duration = getDuration(params)
        return Order.executeQuery("select new Map(year(o.created) as year, month(o.created) as month, sum(i.tax) as total) from OrderItem i inner join i.order o where o.orderStatus != ? and o.paymentStatus = ? and o.created >= ? and o.created <= ? group by year(o.created), month(o.created) order by year, month", [DomainConstants.ORDER_STATUS.CANCELLED, DomainConstants.ORDER_PAYMENT_STATUS.PAID, duration.start, duration.end]).collect {
            return [dimension: it.month + "-" + it.year, "total.taxes": it.total]
        }
    }

    private Map getDuration(params) {
        Date now = new Date().gmt();
        Date sessionCurrentTime = now.toZone(AppUtil.session.timezone)
        Date endGMTTime = now.toZone(AppUtil.session.timezone).dayEnd.gmt()
        Date startTime
        switch(params.duration) {
            case "today":
                startTime = sessionCurrentTime.dayStart.gmt(AppUtil.session.timezone)
                break;
            case "yesterday":
                endGMTTime = sessionCurrentTime.dayStart.gmt(AppUtil.session.timezone)
                startTime = endGMTTime - 1.days
                endGMTTime = endGMTTime - 1.seconds
                break;
            case "last.24.hours":
                endGMTTime = sessionCurrentTime.gmt();
                startTime = endGMTTime - 24.hours
                break;
            case "last.seven.days":
                startTime = endGMTTime - 7.days
                break;
            case "last.thirty.days":
                startTime = endGMTTime - 30.days
                break;
            case "this.month":
                startTime = sessionCurrentTime.monthStart.gmt(AppUtil.session.timezone)
                break;
            case "last.month":
                startTime = sessionCurrentTime.monthStart.gmt(AppUtil.session.timezone)
                endGMTTime = startTime
                startTime = endGMTTime - 1.months
                endGMTTime = endGMTTime - 1.seconds
                break;
            case "custom.date.range":
                startTime = params.start.dayStart.gmt(AppUtil.session.timezone)
                endGMTTime = params.end.dayEnd.gmt(AppUtil.session.timezone)
                break;
        }
        return [start: startTime, end: endGMTTime]
    }

    boolean saveFavourite(Long id, String name, String filters, String type) {
        FavouriteReport report = id ? FavouriteReport.get(id) : new FavouriteReport(name: name, filters: filters, type: type)
        if(id) {
            report.name = name
        }
        report.save()
        return !report.hasErrors()
    }

    boolean deleteReport(Long id, String at2_reply, String at1_reply) {
        TrashUtil.preProcessFinalDelete("favourite-report", id, at2_reply != null, at1_reply != null)
        AppEventManager.fire("before-favourite-report-delete", [id, at1_reply])
        FavouriteReport report = FavouriteReport.get(id);
        report.delete()
        AppEventManager.fire("favourite-report-delete", [id])
        return true
    }
}