package com.webcommander.extension.grails.gorm.expression

import org.hibernate.Criteria
import org.hibernate.criterion.CriteriaQuery

/**
 * Created by zobair on 07/12/2014.*/
class PropertyExpression extends Expression {
    String property

    PropertyExpression(String name) {
        property = name
    }

    @Override
    String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) {
        return criteriaQuery.getColumn(criteria, property)
    }
}