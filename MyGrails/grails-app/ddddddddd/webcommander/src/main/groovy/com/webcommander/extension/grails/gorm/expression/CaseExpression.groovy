package com.webcommander.extension.grails.gorm.expression

import grails.gorm.DetachedCriteria
import org.hibernate.Criteria
import org.hibernate.criterion.CriteriaQuery
import org.hibernate.criterion.Criterion
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.engine.spi.TypedValue
import org.hibernate.internal.CriteriaImpl
import org.hibernate.loader.criteria.EntityCriteriaInfoProvider
import org.hibernate.persister.entity.OuterJoinLoadable

/**
 * Created by zobair on 07/12/2014.*/
class CaseExpression extends Expression {
    Closure caseClosure
    Closure ifClosure
    Closure elseClosure
    String ifValue
    String elseValue

    private List<TypedValue> subExpressionValues = []

    CaseExpression(Closure expression) {
        expression.delegate = this
        expression.setResolveStrategy(Closure.DELEGATE_FIRST)
        expression.call()
    }

    String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) {
        String entityName = criteria.getEntityOrClassName()
        final SessionFactoryImplementor factory = criteriaQuery.getFactory();
        final OuterJoinLoadable persister = (OuterJoinLoadable) factory.getEntityPersister(entityName);
        DetachedCriteria caseGCriteria = new DetachedCriteria(Class.forName(entityName), criteria.alias).build caseClosure
        org.hibernate.criterion.DetachedCriteria caseHCriteria = CriteriaBuilder.getHibernateDetachedCriteria(caseGCriteria)
        criteriaQuery.criteriaInfoMap.put(caseHCriteria.criteriaImpl, new EntityCriteriaInfoProvider(persister))
        List<CriteriaImpl.CriterionEntry> entries = caseHCriteria.criteriaImpl.criterionEntries

        StringBuffer sql = new StringBuffer()
        sql << "case when "
        entries.eachWithIndex { criterionEntry, index ->
            if (index > 0) {
                sql << " and "
            } else {
                sql << " "
            }
            Criterion criterion = criterionEntry.criterion
            sql << criterion.toSqlString(criteria, criteriaQuery)
            TypedValue[] tv = criterionEntry.criterion.getTypedValues(criterionEntry.criteria, criteriaQuery)
            subExpressionValues.addAll(tv)
        }
        sql << " then "
        if (ifValue != null) {
            if (ifValue == '$$NULL$$') {
                sql << "null"
            } else {
                sql << ifValue
            }
        } else {
            def builder = new ExpressionBuilder()
            ifClosure.delegate = builder
            ifClosure.resolveStrategy = Closure.DELEGATE_FIRST
            ifClosure.call()
            sql << builder.expression.toSqlString(criteria, criteriaQuery)
            subExpressionValues.addAll(builder.expression.getTypedValues(criteria, criteriaQuery))
        }
        sql << " else "
        if (elseValue != null) {
            if (elseValue == '$$NULL$$') {
                sql << "null"
            } else {
                sql << elseValue
            }
        } else {
            def builder = new ExpressionBuilder()
            elseClosure.delegate = builder
            elseClosure.resolveStrategy = Closure.DELEGATE_FIRST
            elseClosure.call()
            sql << builder.expression.toSqlString(criteria, criteriaQuery)
            subExpressionValues.addAll(builder.expression.getTypedValues(criteria, criteriaQuery))
        }
        sql << " end"
        return sql.toString()
    }

    void check(Closure closure) {
        caseClosure = closure
    }

    void match(Object value) {
        if (value instanceof Closure) {
            ifClosure = value
        } else {
            ifValue = value == null ? '$$NULL$$' : value.toString()
        }
    }

    void otherwise(Object value) {
        if (value instanceof Closure) {
            elseClosure = value
        } else {
            elseValue = value == null ? '$$NULL$$' : value.toString()
        }
    }

    List<TypedValue> getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) {
        return subExpressionValues
    }
}
