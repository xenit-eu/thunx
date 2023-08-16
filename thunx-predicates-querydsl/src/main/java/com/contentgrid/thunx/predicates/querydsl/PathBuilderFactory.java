package com.contentgrid.thunx.predicates.querydsl;

import com.querydsl.core.types.dsl.PathBuilder;

public interface PathBuilderFactory {
    PathBuilder<?> create(Class<?> domainType);
}
