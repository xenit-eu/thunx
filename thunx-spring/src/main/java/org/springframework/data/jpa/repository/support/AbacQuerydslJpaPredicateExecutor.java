package org.springframework.data.jpa.repository.support;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class AbacQuerydslJpaPredicateExecutor<T> extends QuerydslJpaPredicateExecutor<T> {

    private final EntityPath path;
    private final Querydsl querydsl;
    private final EntityManager em;

    private final CustomCountQueryProvider countQueryProvider;
    private final Class<?> entityType;

    public AbacQuerydslJpaPredicateExecutor(JpaEntityInformation entityInformation, EntityManager entityManager, EntityPathResolver resolver, CrudMethodMetadata metadata, CustomCountQueryProvider countQueryProvider) {
        super(entityInformation, entityManager, resolver, metadata);
        this.em = entityManager;
        this.entityType = entityInformation.getJavaType();
        this.path = resolver.createPath(entityInformation.getJavaType());
        this.querydsl = new Querydsl(entityManager, new PathBuilder<T>(path.getType(), path.getMetadata()));
        this.countQueryProvider = countQueryProvider;
    }

    @Override
    public Page findAll(Predicate predicate, Pageable pageable) {
        Assert.notNull(predicate, "Predicate must not be null!");
        Assert.notNull(pageable, "Pageable must not be null!");

        final Query countQuery = countQueryProvider.createNativeCountQuery(predicate, entityType);
        JPQLQuery<T> query = querydsl.applyPagination(pageable, createQuery(predicate).select(path));

        return PageableExecutionUtils.getPage(query.fetch(), pageable, countQueryProvider.longSupplier(countQuery));
    }
}
