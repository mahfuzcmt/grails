package com.webcommander.extension.grails.gorm.expression

import org.hibernate.Criteria
import org.hibernate.HibernateException
import org.hibernate.criterion.CriteriaQuery
import org.hibernate.criterion.Criterion
import org.hibernate.engine.spi.TypedValue
import org.hibernate.type.StringType

/**
 * Created by LocalZobair on 01/02/2017.*/
class CaseLikeExpression implements Criterion {

    String matchValue
    CaseExpression expression

    CaseLikeExpression(String value, Closure forCaseExpression) {
        matchValue = value
        expression = new CaseExpression(forCaseExpression)
    }

    @Override
    String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        return expression.toSqlString(criteria, criteriaQuery) + " like ?"
    }

    @Override
    TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        List<TypedValue> values = expression.getTypedValues(criteria, criteriaQuery)
        values.add(new TypedValue(StringType.INSTANCE, matchValue))
        return values.toArray([] as TypedValue[])
    }
}