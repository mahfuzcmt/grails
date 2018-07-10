package com.webcommander.hibernate

import grails.gorm.DetachedCriteria
import grails.gorm.PagedResultList
import org.grails.datastore.mapping.query.Query
import org.hibernate.Criteria
import org.hibernate.criterion.Order

/**
 * Created by zobair on 19/08/2014.
 */
class ExpressionOrderSupportedDetachedCriteria<T> extends DetachedCriteria<T> {
    DetachedCriteria baseCriteria
    List<ExpressionOrder> orders = []
    private OverriddenMethods methods

    static {
        ExpressionOrderSupportedDetachedCriteria.metaClass.invokeMethod = { String name, Object args ->
            if(name in ["addOrder", "list"]) {
                return delegate.methods.invokeMethod(name, args)
            }
            return delegate.baseCriteria.invokeMethod(name, args)
        }
    }

    ExpressionOrderSupportedDetachedCriteria(DetachedCriteria criteria) {
        super(null, null)
        baseCriteria = criteria
        if(criteria instanceof ExpressionOrderSupportedDetachedCriteria) {
            baseCriteria = criteria.baseCriteria
            orders.addAll(criteria.orders)
        }
        methods = new OverriddenMethods()
    }

    void addOrder(ExpressionOrder order) {}

    private class OverriddenMethods {
        void addOrder(ExpressionOrder order) {
            orders.add(order)
        }

        List list(Map args = Collections.emptyMap(), Closure additionalCriteria = null) {
            (List)withPopulatedQuery(args, additionalCriteria) { Query query ->
                Criteria criteria = query.@criteria
                orders.each {
                    criteria.addOrder(it)
                }
                if (args?.max) {
                    return new PagedResultList(query)
                }
                return query.list()
            }
        }
    }

}
