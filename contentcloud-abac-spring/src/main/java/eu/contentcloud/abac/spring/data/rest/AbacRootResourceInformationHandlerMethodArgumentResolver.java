package eu.contentcloud.abac.spring.data.rest;

import com.mysema.commons.lang.Pair;
import com.querydsl.core.types.Predicate;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.springframework.core.MethodParameter;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.querydsl.binding.AbacQuerydslPredicateBuilder;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.data.rest.webmvc.config.ResourceMetadataHandlerMethodArgumentResolver;
import org.springframework.data.rest.webmvc.config.RootResourceInformationHandlerMethodArgumentResolver;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class AbacRootResourceInformationHandlerMethodArgumentResolver
    extends RootResourceInformationHandlerMethodArgumentResolver {

    private final Repositories repositories;
    private final AbacQuerydslPredicateBuilder predicateBuilder;
    private final QuerydslBindingsFactory factory;

    /**
     * Creates a new {@link AbacRootResourceInformationHandlerMethodArgumentResolver} using the given
     * {@link Repositories}, {@link RepositoryInvokerFactory} and {@link ResourceMetadataHandlerMethodArgumentResolver}.
     *
     * @param repositories must not be {@literal null}.
     * @param invokerFactory must not be {@literal null}.
     * @param resourceMetadataResolver must not be {@literal null}.
     */
    public AbacRootResourceInformationHandlerMethodArgumentResolver(
            Repositories repositories,
            RepositoryInvokerFactory invokerFactory,
            ResourceMetadataHandlerMethodArgumentResolver resourceMetadataResolver,
            AbacQuerydslPredicateBuilder predicateBuilder,
            QuerydslBindingsFactory factory) {

        super(repositories, invokerFactory, resourceMetadataResolver);

        this.repositories = repositories;
        this.predicateBuilder = predicateBuilder;
        this.factory = factory;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.rest.webmvc.config.RootResourceInformationHandlerMethodArgumentResolver#postProcess(org.springframework.data.repository.support.RepositoryInvoker, java.lang.Class, java.util.Map)
     */
    @Override
    protected RepositoryInvoker postProcess(MethodParameter parameter, RepositoryInvoker invoker, Class<?> domainType,
            Map<String, String[]> parameters) {

//        if (!parameter.hasParameterAnnotation(QuerydslPredicate.class)) {
//            return invoker;
//        }

        return repositories.getRepositoryFor(domainType)//
                .filter(it -> QuerydslPredicateExecutor.class.isInstance(it))//
                .map(it -> QuerydslPredicateExecutor.class.cast(it))//
                .flatMap(it -> getRepositoryAndPredicate(it, domainType, parameters))//
                .map(it -> getQuerydslAdapter(invoker, it.getFirst(), it.getSecond()))//
                .orElse(invoker);
    }

    private Optional<Pair<QuerydslPredicateExecutor<?>, Predicate>> getRepositoryAndPredicate(
            QuerydslPredicateExecutor<?> repository, Class<?> domainType, Map<String, String[]> parameters) {

        ClassTypeInformation<?> type = ClassTypeInformation.from(domainType);

        QuerydslBindings bindings = factory.createBindingsFor(type);
        Predicate predicate = predicateBuilder.getPredicate(type, toMultiValueMap(parameters), bindings);

        return Optional.ofNullable(predicate).map(it -> Pair.of(repository, it));
    }

    @SuppressWarnings("unchecked")
    private static RepositoryInvoker getQuerydslAdapter(RepositoryInvoker invoker,
            QuerydslPredicateExecutor<?> repository, Predicate predicate) {
        return new AbacRepositoryInvokerAdapter(invoker, (QuerydslPredicateExecutor<Object>) repository, predicate);
    }

    /**
     * Converts the given Map into a {@link MultiValueMap}.
     *
     * @param source must not be {@literal null}.
     * @return
     */
    private static MultiValueMap<String, String> toMultiValueMap(Map<String, String[]> source) {

        MultiValueMap<String, String> result = new LinkedMultiValueMap<String, String>();

        for (Entry<String, String[]> entry : source.entrySet()) {
            result.put(entry.getKey(), Arrays.asList(entry.getValue()));
        }

        return result;
    }
}
