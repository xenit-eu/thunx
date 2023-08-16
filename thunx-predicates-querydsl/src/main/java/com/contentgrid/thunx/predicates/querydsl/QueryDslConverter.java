package com.contentgrid.thunx.predicates.querydsl;

import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;

public class QueryDslConverter {

    private final QueryDslConvertingVisitor visitor;
    private final PathBuilderFactory pathBuilderFactory;

    public QueryDslConverter(PropertyAccessStrategy propertyAccessStrategy, PathBuilderFactory pathBuilderFactory) {
        this.visitor = new QueryDslConvertingVisitor(propertyAccessStrategy);
        this.pathBuilderFactory = pathBuilderFactory;
    }

    public Predicate from(ThunkExpression<Boolean> thunk, Class<?> domainType) {
        var entityPath = this.pathBuilderFactory.create(domainType);
        return this.from(thunk, entityPath);
    }

    public Predicate from(ThunkExpression<Boolean> thunk, PathBuilder<?> entityPath) {
        return (Predicate) thunk.accept(this.visitor, new QueryDslConversionContext(entityPath));
    }

}

