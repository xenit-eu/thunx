package com.contentgrid.thunx.predicates.querydsl;

import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;

public class QueryDslConverter {

    private final QueryDslConvertingVisitor visitor;

    public QueryDslConverter(PropertyAccessStrategy propertyAccessStrategy) {
        this.visitor = new QueryDslConvertingVisitor(propertyAccessStrategy);
    }

    public Predicate from(ThunkExpression<Boolean> thunk, Class<?> domainType) {
        var entityPath = new PathBuilder<>(domainType, toAlias(domainType));
        return this.from(thunk, entityPath);
    }

    public Predicate from(ThunkExpression<Boolean> thunk, PathBuilder<?> entityPath) {
        return (Predicate) thunk.accept(this.visitor, new QueryDslConversionContext(entityPath));
    }


    private static String toAlias(Class<?> domainType) {
        return uncapitalize(domainType.getSimpleName());
    }

    private static String uncapitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        char baseChar = str.charAt(0);
        char updatedChar;
        updatedChar = Character.toLowerCase(baseChar);

        if (baseChar == updatedChar) {
            return str;
        }

        char[] chars = str.toCharArray();
        chars[0] = updatedChar;
        return new String(chars);
    }


}

