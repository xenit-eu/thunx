package eu.xenit.contentcloud.thunx.predicates.querydsl;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import eu.xenit.contentcloud.thunx.predicates.model.ThunkExpression;

public class QueryDslUtils {

    public static Predicate from(ThunkExpression<Boolean> thunk, PathBuilder<?> entityPath) {

        var queryDslExpr = thunk.accept(new QueryDslConverter(entityPath));
        return (Predicate) queryDslExpr;
    }

}

