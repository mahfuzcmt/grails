package com.webcommander.extension.grails.gorm.expression

import org.hibernate.Criteria
import org.hibernate.HibernateException
import org.hibernate.criterion.CriteriaQuery
import org.hibernate.criterion.Criterion
import org.hibernate.engine.spi.TypedValue

/**
 * Created by zobair on 11/12/2014.
 */
class ExpressionHibernateCriterion implements Criterion {

    private Criterion originalExpression
    List<TypedValue> values

    ExpressionHibernateCriterion(Criterion expression, List values) {
        originalExpression = expression
        this.values = values
    }

    def methodMissing(String name, def args) {
        return originalExpression."$name"(*args)
    }

    @Override
    TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        TypedValue[] types = originalExpression.getTypedValues(criteria, criteriaQuery)
        List typesL = types as List
        typesL.addAll(values)
        return typesL.toArray()
    }

    @Override
    String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        return originalExpression.toSqlString(criteria, criteriaQuery)
    }
}
