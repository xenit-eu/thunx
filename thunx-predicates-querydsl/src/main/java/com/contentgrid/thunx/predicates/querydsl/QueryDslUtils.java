package com.contentgrid.thunx.predicates.querydsl;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.contentgrid.thunx.predicates.model.ThunkExpression;

public class QueryDslUtils {

    public static Predicate from(ThunkExpression<Boolean> thunk, PathBuilder<?> entityPath) {

        var queryDslExpr = thunk.accept(new QueryDslConverter(entityPath));
        return (Predicate) queryDslExpr;
    }

}

