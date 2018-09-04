package com.webcommander.extension.grails.gorm.expression

import org.hibernate.Criteria
import org.hibernate.criterion.CriteriaQuery
import org.hibernate.engine.spi.TypedValue

/**
 * Created by zobair on 07/12/2014.
 */
abstract class Expression {
    abstract String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery)

    List<TypedValue> getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) { [] }
}