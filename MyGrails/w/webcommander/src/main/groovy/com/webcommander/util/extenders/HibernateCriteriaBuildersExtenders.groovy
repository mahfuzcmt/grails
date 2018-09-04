package com.webcommander.util.extenders

import com.webcommander.extension.grails.gorm.expression.CaseExpression
import com.webcommander.extension.grails.gorm.expression.CaseLikeExpression
import com.webcommander.extension.grails.gorm.expression.ExpressionBuilder
import com.webcommander.extension.grails.gorm.expression.ExpressionProjection
import com.webcommander.extension.grails.gorm.expression.ProjectionAdapter
import com.webcommander.extension.grails.gorm.expression.ProjectionAdapter.SumProjection
import com.webcommander.extension.grails.gorm.expression.ProjectionAdapter.CaseProjection
import com.webcommander.extension.grails.gorm.expression.SubExpression
import grails.gorm.DetachedCriteria
import grails.orm.HibernateCriteriaBuilder
import org.grails.datastore.mapping.query.api.Criteria
import org.grails.datastore.mapping.query.api.QueryableCriteria
import org.hibernate.sql.JoinType
import org.hibernate.type.Type

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * Created by LocalZobair on 10/01/2017.*/
class HibernateCriteriaBuildersExtenders {
    private static short aliasCounter = 0

    private interface JoinableAssociationCriteria extends Criteria {
        JoinType getJoinType()
    }

    static extend() {
        ThreadLocal<Map> QueryMetaInfo = new ThreadLocal<>()

        ProjectionAdapter.initialize()

        HibernateCriteriaBuilder.metaClass.with {
            meta = { info, value = null ->
                Map metaData = QueryMetaInfo.get()
                if (!metaData) {
                    metaData = [(delegate): [:]]
                    QueryMetaInfo.set(metaData)
                }
                if (info == "remove") {
                    metaData.remove(delegate)
                    if (!metaData.size()) {
                        QueryMetaInfo.remove()
                    }
                    return
                }
                if (!metaData[delegate]) {
                    metaData[delegate] = [:]
                }
                metaData = metaData[delegate]
                if (info instanceof Map) {
                    metaData.putAll(info)
                } else {
                    if (value) {
                        metaData[info] = value
                        return value
                    }
                    return metaData[info]
                }
            }

            alias = { String path, join = null ->
                if (path == "root") {
                    return delegate.criteria.alias
                }
                def subCriteria = delegate.criteria.subcriteriaList.find { it.path == path }
                if (subCriteria) {
                    return subCriteria.alias
                } else {
                    if (aliasCounter == 50000) {
                        aliasCounter = 0
                    }
                    String _alias = "alias_${++aliasCounter}"
                    delegate.createAlias(path, _alias, join ?: JoinType.INNER_JOIN)
                    return _alias
                }
            }

            criteria = { Closure criteriaClause ->
                if(criteriaClause != null) {
                    criteriaClause.delegate = delegate
                    criteriaClause.resolveStrategy = Closure.DELEGATE_FIRST
                    criteriaClause()
                }
            }

            caselike = { matchValue, Closure forCaseExpression -> delegate.addToCriteria(new CaseLikeExpression(matchValue, forCaseExpression))}

            iff = { Type type, Closure closure -> delegate.projectionList.elements << new ExpressionProjection(type, new CaseExpression(closure))}

            sub = { Type type, QueryableCriteria criteria -> delegate.projectionList.elements << new ExpressionProjection(type, new SubExpression(criteria))}
        }

        DetachedCriteria.metaClass.with {
            updateAll = { Map map ->
                if(delegate.targetClass.metaClass.getMetaProperty("updated")) {
                    map.updated = new Date().gmt()
                }
                return delegate.class.getDeclaredMethod("updateAll", [Map] as Class[]).invoke(delegate, [map] as Object[]);
            }

            sum = { Type type, Closure closure ->
                def builder = new ExpressionBuilder()
                closure.delegate = builder
                closure.resolveStrategy = Closure.DELEGATE_FIRST
                closure.call()
                DetachedCriteria newCriteria = delegate.clone()
                newCriteria.projectionList.projections << new SumProjection(type, builder.expression)
                return newCriteria
            }

            iff = { Type type, Closure closure ->
                DetachedCriteria newCriteria = delegate.clone()
                newCriteria.projectionList.projections << new CaseProjection(type, new CaseExpression(closure))
                return newCriteria
            }

            createAlias = { String associationPath, String alias, JoinType joinType ->
                Criteria criteria = delegate.createAlias(associationPath, alias)
                return Proxy.newProxyInstance(Criteria.classLoader, [JoinableAssociationCriteria] as Class[], new InvocationHandler() {
                    @Override
                    Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if(method.name == "getJoinType") {
                            return joinType
                        }
                        return method.invoke(criteria, *args)
                    }
                })
            }
        }
    }
}