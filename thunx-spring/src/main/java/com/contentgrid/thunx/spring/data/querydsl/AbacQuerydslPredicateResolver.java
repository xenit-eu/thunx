package com.contentgrid.thunx.spring.data.querydsl;

import com.contentgrid.thunx.predicates.querydsl.FieldByReflectionAccessStrategy;
import com.contentgrid.thunx.predicates.querydsl.QueryDslConverter;
import com.contentgrid.thunx.spring.data.context.AbacContext;
import com.querydsl.core.types.Predicate;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.util.Assert;

/**
 * Resolves the QueryDSL Predicate from the Thunx {@link AbacContext} that is sent with the request
 */
@Slf4j
public class AbacQuerydslPredicateResolver implements QuerydslPredicateResolver {

    private final QueryDslConverter queryDslConverter;

    public AbacQuerydslPredicateResolver(EntityPathResolver resolver) {

        this.queryDslConverter = new QueryDslConverter(
                new FieldByReflectionAccessStrategy(),
                new EntityPathResolverBasedPathBuilderFactory(resolver)
        );
    }

    @Override
    public Optional<Predicate> resolve(MethodParameter methodParameter, Class<?> domainType,
            Map<String, String[]> parameters) {
        var abacContext = AbacContext.getCurrentAbacContext();
        if (abacContext != null) {
            Predicate queryDslPredicate = this.queryDslConverter.from(abacContext, domainType);
            Assert.notNull(queryDslPredicate, "abac expression cannot be null");
            log.debug("ABAC Querydsl Predicate: {}", queryDslPredicate);
            return Optional.of(queryDslPredicate);
        }
        return Optional.empty();
    }
}
