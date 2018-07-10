package com.webcommander.extension.grails.gorm.expression

import org.hibernate.Criteria
import org.hibernate.HibernateException
import org.hibernate.criterion.CriteriaQuery
import org.hibernate.type.Type

/**
 * Created by zobair on 08/12/2014.
 */
class SumExpressionProjection extends ExpressionProjection {
    SumExpressionProjection(Type type, Expression expression) {
        super(type, expression)
    }

    @Override
    String toSqlString(Criteria criteria, int i, CriteriaQuery criteriaQuery) throws HibernateException {
        String sql = "sum(" + expression.toSqlString(criteria, criteriaQuery) + ") as y" + i + '_'
        addExpressionValues(criteria, criteriaQuery)
        return sql
    }
}
