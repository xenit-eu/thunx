package com.contentgrid.thunx.spring.data.querydsl;

import com.contentgrid.thunx.predicates.querydsl.FieldByReflectionAccessStrategy;
import com.contentgrid.thunx.predicates.querydsl.QueryDslConverter;
import com.contentgrid.thunx.spring.data.context.AbacContext;
import com.contentgrid.thunx.spring.data.querydsl.EntityPathResolverBasedPathBuilderFactory;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.QuerydslPredicateBuilder;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

@Slf4j
public class AbacQuerydslPredicateBuilder {

    private final QuerydslPredicateBuilder querydslPredicateBuilder;
    private final QueryDslConverter queryDslConverter;

    public AbacQuerydslPredicateBuilder(ConversionService conversionService, EntityPathResolver resolver) {

        Assert.notNull(conversionService, "ConversionService must not be null!");

        this.querydslPredicateBuilder = new QuerydslPredicateBuilder(conversionService, resolver);

        this.queryDslConverter = new QueryDslConverter(new FieldByReflectionAccessStrategy(), new EntityPathResolverBasedPathBuilderFactory(
                resolver));
    }

    @Nullable
    public Predicate getPredicate(TypeInformation<?> type, MultiValueMap<String, String> values, QuerydslBindings bindings) {

        Assert.notNull(bindings, "Context must not be null!");

        BooleanBuilder builder = new BooleanBuilder();

        var abacContext = AbacContext.getCurrentAbacContext();
        if (abacContext != null) {
            Predicate queryDslPredicate = this.queryDslConverter.from(abacContext, type.getType());
            Assert.notNull(queryDslPredicate, "abac expression cannot be null");
            log.debug("ABAC Querydsl Predicate: {}", queryDslPredicate);

            builder.and(queryDslPredicate);
        }

        builder.and(querydslPredicateBuilder.getPredicate(type, values, bindings));

        return builder.getValue();
    }

}
