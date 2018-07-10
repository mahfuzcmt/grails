package com.webcommander.extension.grails.gorm.expression

import org.hibernate.Criteria
import org.hibernate.HibernateException
import org.hibernate.criterion.CriteriaQuery
import org.hibernate.criterion.SimpleProjection
import org.hibernate.engine.spi.TypedValue
import org.hibernate.internal.CriteriaImpl
import org.hibernate.type.Type

/**
 * Created by zobair on 08/12/2014.
 */
class ExpressionProjection extends SimpleProjection {
    Expression expression
    Type type

    ExpressionProjection(Type type, Expression expression) {
        this.expression = expression
        this.type = type
    }

    @Override
    Type[] getTypes(Criteria criteria, CriteriaQuery criteriaQuery) {
        return [type] as Type[]
    }

    @Override
    Type[] getTypes(String alias, Criteria criteria, CriteriaQuery criteriaQuery) {
        return null
    }

    void addExpressionValues(Criteria criteria, CriteriaQuery criteriaQuery) {
        List<TypedValue> values = expression.getTypedValues(criteria, criteriaQuery)

        if(values.size()) {
            List<CriteriaImpl.CriterionEntry> parentEntries = criteria.criterionEntries
            if(parentEntries.size() > 0) {
                CriteriaImpl.CriterionEntry firstEntry = parentEntries.first()
                parentEntries[0] = new CriteriaImpl.CriterionEntry(new ExpressionHibernateCriterion(firstEntry.criterion, values), firstEntry.criteria)
            } else {
                parentEntries.add(new CriteriaImpl.CriterionEntry(new BlankHibernateCriterionToAttachValue(values), criteria))
            }
        }
    }

    @Override
    String toSqlString(Criteria criteria, int i, CriteriaQuery criteriaQuery) throws HibernateException {
        String sql = expression.toSqlString(criteria, criteriaQuery) + " as y" + i + '_'
        addExpressionValues(criteria, criteriaQuery)
        return sql
    }
}
