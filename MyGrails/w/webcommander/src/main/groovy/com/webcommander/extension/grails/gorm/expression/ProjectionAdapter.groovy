package com.webcommander.extension.grails.gorm.expression

import org.grails.datastore.mapping.query.Query
import org.grails.orm.hibernate.query.HibernateProjectionAdapter
import org.hibernate.criterion.SimpleProjection
import org.hibernate.type.Type

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * Created by zobair on 08/12/2014.*/
class ProjectionAdapter {
    static class CaseProjection extends Query.Projection {
        CaseExpression expression
        Type type

        CaseProjection(Type type, CaseExpression expression) {
            this.expression = expression
            this.type = type
        }
    }

    static class SumProjection extends Query.Projection {
        Expression expression
        Type type

        SumProjection(Type type, Expression expression) {
            this.expression = expression
            this.type = type
        }
    }

    static initialize() {
        Map projectionsAdapters = HibernateProjectionAdapter.metaClass.getMetaProperty("adapterMap").getProperty(null)
        projectionsAdapters[CaseProjection] = projectionsAdapters[SumProjection] = Proxy.newProxyInstance(HibernateProjectionAdapter.classLoader, [HibernateProjectionAdapter.ProjectionAdapter] as Class[], new InvocationHandler() {
            @Override
            Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if(method.name == "toHibernateProjection") {
                    return ProjectionAdapter.toHibernateProjection(*args)
                }
                return null
            }
        })
    }

    static SimpleProjection toHibernateProjection(Query.Projection projection) {
        if (projection instanceof CaseProjection) {
            return new ExpressionProjection(projection.type, projection.expression)
        } else if (projection instanceof SumProjection) {
            return new SumExpressionProjection(projection.type, projection.expression)
        }
    }
}
