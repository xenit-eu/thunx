package com.contentgrid.thunx.spring.data.rest;

import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver.OperationPredicates;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lombok.NonNull;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.QuerydslRepositoryInvokerAdapter;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.Assert;

class AbacRepositoryInvokerAdapter extends QuerydslRepositoryInvokerAdapter {

    @NonNull
    private final QuerydslPredicateExecutor<Object> executor;
    @NonNull
    private final OperationPredicates predicate;
    private final PlatformTransactionManager transactionManager;

    @NonNull
    private final Class<?> idPropertyType;

    @NonNull
    private final String idName;

    @NonNull
    private final Function<Object, Optional<?>> idFunction;

    @NonNull
    private final PathBuilder<?> pathBuilder;

    private final ConversionService conversionService;

    public AbacRepositoryInvokerAdapter(
            RepositoryInvoker delegate,
            QuerydslPredicateExecutor<Object> executor,
            OperationPredicates predicate,
            PlatformTransactionManager transactionManager,
            RepositoryMetadata repositoryMetadata,
            PersistentEntity<?, ?> persistentEntity,
            EntityInformation<Object, ?> entityInformation,
            PathBuilder<?> pathBuilder,
            ConversionService conversionService
    ) {
        this(delegate, executor, predicate, transactionManager, repositoryMetadata.getIdType(),
                persistentEntity.getRequiredIdProperty().getName(),
                entity -> Optional.ofNullable(entityInformation.getId(entity)),
                pathBuilder,
                conversionService
        );
    }

    public AbacRepositoryInvokerAdapter(
            RepositoryInvoker delegate,
            QuerydslPredicateExecutor<Object> executor,
            OperationPredicates predicate,
            PlatformTransactionManager transactionManager,
            Class<?> idType,
            String idPropertyName,
            Function<Object, Optional<?>> idFunction,
            PathBuilder<?> pathBuilder,
            ConversionService conversionService
    ) {
        super(delegate, executor, predicate.collectionFilterPredicate());
        this.executor = executor;
        this.predicate = predicate;
        this.transactionManager = transactionManager;
        this.idPropertyType = idType;
        this.idName = idPropertyName;
        this.idFunction = idFunction;
        this.pathBuilder = pathBuilder;
        this.conversionService = conversionService;

    }

    /**
     * Invokes the method equivalent to {@link org.springframework.data.repository.CrudRepository#findById(Object)},
     * taking the provided {@link Predicate} into account when looking up an entity by id.
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given id.
     * @throws IllegalStateException if the repository does not expose a find-one-method.
     */
    @Override
    public <T> Optional<T> invokeFindById(Object id) {
        return invokeFindById(id, predicate.readPredicate());
    }

    private <T> Optional<T> invokeFindById(Object id, Predicate predicate) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(predicate);

        var entityIdPath = pathBuilder.get(this.idName, this.idPropertyType);
        Assert.notNull(entityIdPath, "id expression cannot be null");

        builder.and(entityIdPath.eq(Expressions.constant(convertId(id))));

        return (Optional<T>) executor.findOne(Objects.requireNonNull(builder.getValue()));
    }

    /**
     * Converts the given id into the id type of the backing repository.
     *
     * @param id must not be {@literal null}.
     * @see "Copied from ReflectionRepositoryInvoker#convertId(Object) convertId"
     */
    protected Object convertId(Object id) {

        Assert.notNull(id, "Id must not be null");

        if (idPropertyType.isInstance(id)) {
            return id;
        }

        Object result = conversionService.convert(id, idPropertyType);

        if (result == null) {
            throw new IllegalStateException(
                    String.format("Identifier conversion of %s to %s unexpectedly returned null", id, idPropertyType));
        }

        return result;
    }

    /**
     * Invokes the method equivalent to {@link org.springframework.data.repository.CrudRepository#save(Object)} on the
     * repository. When an entity is saved, it is immediately looked up again using
     * {@link AbacRepositoryInvokerAdapter#invokeFindById(Object id)}, which applies the {@link Predicate} from the
     * request context. If this lookup returns an empty {@link Optional}, a {@link ResourceNotFoundException} is thrown
     * that rolls back the current transaction.
     *
     * @return the result of the invocation of the save method
     * @throws IllegalStateException if the repository does not expose a save method.
     */
    @Override
    public <T> T invokeSave(T object) {

        TransactionStatus status = null;
        T entityToReturn;

        try {

            if (transactionManager != null) {
                status = transactionManager.getTransaction(new DefaultTransactionDefinition());
            }

            var maybePreSaveId = idFunction.apply(object);

            Predicate postSavePredicate;
            // when object has no id, there is no pre-save-check required, because it is a newly created entity
            // when object has an 'id', do:
            //   1. invokeFindById without predicate
            //   2. invokeFindById with predicate
            //   case a) - both (1) and (2) are NOT present: create new -> check afterwards the new object was allowed
            //   case b) - both (1) and (2) are present: update -> check afterwards the updates are allowed
            //   case c) - if (1) is NOT present, but (2) is: impossible case
            //   case d) - if (1) is present, but (2) is NOT: permission denied
            if (maybePreSaveId.isPresent()) {
                var preSaveId = maybePreSaveId.get();
                if (super.invokeFindById(preSaveId) /* without predicate */.isPresent()) {
                    // Id set, entity found in DB -> is an existing entity
                    if (this.invokeFindById(preSaveId,
                            predicate.beforeUpdatePredicate()) /* with predicate */.isEmpty()) {
                        throw new ResourceNotFoundException(String.format("id: %s", preSaveId));
                    }
                    postSavePredicate = predicate.afterUpdatePredicate();
                } else {
                    // Id set, but entity not found in DB -> always a new entity (with app-generated ID)
                    postSavePredicate = predicate.afterCreatePredicate();
                }
            } else {
                // No id is set -> always a new entity (with db-generated ID)
                postSavePredicate = predicate.afterCreatePredicate();
            }

            T savedEntity = super.invokeSave(object);

            Object id = this.idFunction.apply(savedEntity).orElseThrow();
            Optional<T> fetchedEntity = this.invokeFindById(id, postSavePredicate);
            if (fetchedEntity.isEmpty()) {
                throw new ResourceNotFoundException(String.format("id: %s", id));
            }

            entityToReturn = fetchedEntity.get();

            if (status != null && !status.isCompleted()) {
                transactionManager.commit(status);
            }


        } catch (Exception e) /* why are we catching pokemons and not ResourceNotFoundException directly ? */ {

            if (status != null && !status.isCompleted()) {
                // every runtime-exception should trigger a rollback anyway, why do we need to do this explicitly ?
                // is it because our invokeSave & invokeFindById _would_ be triggered in separate transactions ?
                transactionManager.rollback(status);
            }
            throw e;
        }

        return entityToReturn;
    }

    @Override
    public void invokeDeleteById(Object id) {
        if (this.invokeFindById(id, predicate.beforeDeletePredicate()).isEmpty()) {
            throw new ResourceNotFoundException(String.format("id: %s", id));
        }
        super.invokeDeleteById(id);
    }
}
