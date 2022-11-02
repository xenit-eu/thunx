package com.contentgrid.thunx.predicates.querydsl;

import com.querydsl.core.types.dsl.PathBuilder;
import lombok.NonNull;
import lombok.Value;

@Value
public class QueryDslConversionContext {

    @NonNull
    PathBuilder<?> pathBuilder;

}
