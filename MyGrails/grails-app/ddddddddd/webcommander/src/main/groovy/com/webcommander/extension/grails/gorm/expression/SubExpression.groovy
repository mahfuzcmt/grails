package com.webcommander.extension.grails.gorm.expression

import grails.gorm.DetachedCriteria
import org.hibernate.Criteria
import org.hibernate.criterion.CriteriaQuery
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.engine.spi.TypedValue
import org.hibernate.internal.CriteriaImpl
import org.hibernate.loader.criteria.CriteriaJoinWalker
import org.hibernate.loader.criteria.CriteriaQueryTranslator
import org.hibernate.loader.criteria.EntityCriteriaInfoProvider
import org.hibernate.persister.entity.OuterJoinLoadable

/**
 * Created by zobair on 08/12/2014.*/
class SubExpression extends Expression {

    private org.hibernate.criterion.DetachedCriteria query
    private List<TypedValue> values = []

    SubExpression(DetachedCriteria subQuery) {
        query = CriteriaBuilder.getHibernateDetachedCriteria subQuery
    }

    @Override
    String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) {
        final SessionFactoryImplementor factory = criteriaQuery.getFactory()
        CriteriaImpl criteriaImpl = query.criteriaImpl
        final OuterJoinLoadable persister = (OuterJoinLoadable) factory.getEntityPersister(criteriaImpl.getEntityOrClassName())
        criteriaQuery.criteriaInfoMap.put(criteriaImpl, new EntityCriteriaInfoProvider(persister))

        String alias
        if (criteriaImpl.getAlias() == null) {
            alias = criteriaQuery.generateSQLAlias()
        } else {
            alias = criteriaImpl.getAlias() + "_"
        }

        CriteriaQueryTranslator innerQuery = new CriteriaQueryTranslator(factory, criteriaImpl, criteriaImpl.getEntityOrClassName(), alias, criteriaQuery)
        criteriaImpl.setSession(deriveRootSession(criteria))
        final CriteriaJoinWalker walker = new CriteriaJoinWalker(persister, innerQuery, factory, criteriaImpl, criteriaImpl.getEntityOrClassName(), criteriaImpl.getSession().getLoadQueryInfluencers(), innerQuery.getRootSQLALias())
        String sql = "(" + walker.getSQLString() + ")"

        criteriaImpl.criterionEntries.each {
            TypedValue[] tv = it.getCriterion().getTypedValues(it.getCriteria(), innerQuery)
            values.addAll(tv)
        }

        return sql
    }

    private deriveRootSession(Criteria criteria) {
        if (criteria instanceof CriteriaImpl) {
            criteria.session
        } else if (criteria instanceof CriteriaImpl.Subcriteria) {
            deriveRootSession criteria.parent
        }
    }

    @Override
    List<TypedValue> getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) {
        values
    }
}
