package org.easysearch.core;

import java.util.ArrayList;

import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.easysearch.CriteriaPath;
import org.easysearch.SearchCriteria;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class SearchBuilder {
    private final EntityManager entityManager;
    private final SearchCriteria searchCriteria;

    public SearchBuilder(EntityManager entityManager, SearchCriteria searchCriteria) {
        this.entityManager = entityManager;
        this.searchCriteria = searchCriteria;
    }

    public <T> TypedQuery<T> getResultQuery(Class<T> t) {
        CriteriaQuery<T> criteriaQuery = entityManager.getCriteriaBuilder().createQuery(t);
        Root<T> rootEntity = criteriaQuery.from(t);
        addSearchPredicates(criteriaQuery, rootEntity);
        return entityManager.createQuery(criteriaQuery);
    }

    public <T> TypedQuery<Long> getCountQuery(Class<T> t) {
        CriteriaQuery<Long> criteriaQuery = entityManager.getCriteriaBuilder().createQuery(Long.class);
        Root<T> rootEntity = criteriaQuery.from(t);
        addSearchPredicates(criteriaQuery, rootEntity);
        criteriaQuery.select(entityManager.getCriteriaBuilder().countDistinct(rootEntity));
        return entityManager.createQuery(criteriaQuery);
    }

    private <R, T> void addSearchPredicates(CriteriaQuery<R> criteriaQuery, Root<T> rootEntity) {
        CriteriaBuilder crBuilder = entityManager.getCriteriaBuilder();

        List<Predicate> criteria = new ArrayList<Predicate>();
        addSearchCriteriaForPaths(rootEntity, crBuilder, criteria);
        criteriaQuery.where(criteria.toArray(new Predicate[criteria.size()]));
    }

    private <T> void addSearchCriteriaForPaths(Root<T> rootEntity, CriteriaBuilder crBuilder, List<Predicate> criteria) {
        ExpressionParser parser = new SpelExpressionParser();
        EvaluationContext spelContext = new StandardEvaluationContext(searchCriteria);
        for (CriteriaPath criteriaPathDetail : searchCriteria.getPaths()) {
            Object attributeValue = parser.parseExpression(criteriaPathDetail.getAttribute()).getValue(spelContext,
                    criteriaPathDetail.getAttributeType());
            addAttributeToCriteriaIfNotBlank(rootEntity, crBuilder, criteria, criteriaPathDetail, attributeValue);
        }

    }

    private <T> void addAttributeToCriteriaIfNotBlank(Root<T> rootEntity, CriteriaBuilder crBuilder,
            List<Predicate> criteria, CriteriaPath criteriaPathDetail, Object attributeValue) {
        if (attributeValue instanceof String) {
            handleString(rootEntity, crBuilder, criteria, criteriaPathDetail, String.valueOf(attributeValue));
        } else {
            handleObject(rootEntity, crBuilder, criteria, criteriaPathDetail, attributeValue);
        }
    }

    private <T> void handleString(Root<T> rootEntity, CriteriaBuilder crBuilder, List<Predicate> criteria,
            CriteriaPath criteriaPathDetail, String attributeValue) {
        if (StringUtils.isNotBlank(attributeValue)) {
            Path<Object> rootPath = getAttributePath(rootEntity, criteriaPathDetail);
            Expression<String> criteriaExpression = crBuilder.lower(rootPath.as(String.class));
            Predicate condition = crBuilder.equal(criteriaExpression, attributeValue.toLowerCase(Locale.getDefault()));
            criteria.add(condition);
        }
    }

    private <T> void handleObject(Root<T> rootEntity, CriteriaBuilder crBuilder, List<Predicate> criteria,
            CriteriaPath criteriaPathDetail, Object attributeValue) {
        if (attributeValue != null) {
            Path<Object> rootPath = getAttributePath(rootEntity, criteriaPathDetail);
            Predicate condition = crBuilder.equal(rootPath.as(criteriaPathDetail.getAttributeType()), attributeValue);
            criteria.add(condition);
        }
    }

    private <T> Path<Object> getAttributePath(Root<T> rootEntity, CriteriaPath criteriaPathDetail) {
        String path = criteriaPathDetail.getPath();
        String[] pathParts = path.split("\\.");
        Path<Object> rootPath = null;
        for (String pathPart : pathParts) {
            rootPath = (rootPath == null) ? rootEntity.get(pathPart) : rootPath.get(pathPart);
        }
        return rootPath;
    }

}
