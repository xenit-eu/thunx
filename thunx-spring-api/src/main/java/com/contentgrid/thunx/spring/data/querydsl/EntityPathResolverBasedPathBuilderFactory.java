package com.contentgrid.thunx.spring.data.querydsl;

import com.contentgrid.thunx.predicates.querydsl.PathBuilderFactory;
import com.querydsl.core.types.dsl.PathBuilder;
import org.springframework.data.querydsl.EntityPathResolver;

/**
 * Creates a {@link PathBuilder<?>} from a domain type by using the metadata provided by {@link EntityPathResolver}
 */
public class EntityPathResolverBasedPathBuilderFactory implements PathBuilderFactory {

    private final EntityPathResolver resolver;

    public EntityPathResolverBasedPathBuilderFactory(EntityPathResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public PathBuilder<?> create(Class<?> domainType) {
        var entityPath = resolver.createPath(domainType);
        return new PathBuilder<>(domainType, entityPath.getMetadata());
    }
}
