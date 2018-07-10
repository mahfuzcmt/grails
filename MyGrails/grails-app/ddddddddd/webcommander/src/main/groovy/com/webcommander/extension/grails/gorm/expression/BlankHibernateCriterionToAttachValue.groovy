package com.webcommander.extension.grails.gorm.expression

import org.hibernate.Criteria
import org.hibernate.HibernateException
import org.hibernate.criterion.CriteriaQuery
import org.hibernate.criterion.Criterion
import org.hibernate.engine.spi.TypedValue

/**
 * Created by zobair on 11/12/2014.
 */
class BlankHibernateCriterionToAttachValue implements Criterion {

    List<TypedValue> values

    BlankHibernateCriterionToAttachValue(List values) {
        this.values = values
    }

    @Override
    TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        return values.toArray()
    }

    @Override
    String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        return "1 = 1"
    }
}
