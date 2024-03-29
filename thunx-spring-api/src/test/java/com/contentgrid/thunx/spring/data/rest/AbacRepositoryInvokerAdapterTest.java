package com.contentgrid.thunx.spring.data.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver.OperationPredicates;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

@ExtendWith(MockitoExtension.class)
class AbacRepositoryInvokerAdapterTest {

    @Mock
    RepositoryInvoker delegate;
    @Mock
    QuerydslPredicateExecutor<Object> executor;
    @Mock(answer = RETURNS_MOCKS)
    PlatformTransactionManager transactionManager;

    @Spy
    OperationPredicates predicate = new TestOperationPredicates(new PathBuilder<>(MyEntity.class, "myEntity")
            .get("attribute", String.class)
            .eq("foo"));

    @RequiredArgsConstructor
    private static class TestOperationPredicates implements OperationPredicates {

        private final Predicate predicate;

        @Override
        public Optional<Predicate> collectionFilterPredicate() {
            return Optional.ofNullable(predicate);
        }

        @Override
        public Optional<Predicate> readPredicate() {
            return Optional.ofNullable(predicate);
        }

        @Override
        public Optional<Predicate> afterCreatePredicate() {
            return Optional.ofNullable(predicate);
        }

        @Override
        public Optional<Predicate> beforeUpdatePredicate() {
            return Optional.ofNullable(predicate);
        }

        @Override
        public Optional<Predicate> afterUpdatePredicate() {
            return Optional.ofNullable(predicate);
        }

        @Override
        public Optional<Predicate> beforeDeletePredicate() {
            return Optional.ofNullable(predicate);
        }
    }

    AbacRepositoryInvokerAdapter adapter;

    @BeforeEach
    void setUp() {
        this.adapter = new AbacRepositoryInvokerAdapter(delegate, executor, predicate, transactionManager,
                UUID.class, "id", (entity) -> Optional.ofNullable(((MyEntity) entity).getId()),
                new PathBuilder<>(MyEntity.class, "myEntity"),
                new DefaultFormattingConversionService()
        );
    }

    @Test
    void invokeFindById_shouldRedirectToExecutor() {

        UUID objectId = UUID.randomUUID();
        adapter.invokeFindById(objectId);

        verify(delegate, never()).invokeFindById(objectId);
        verify(executor).findOne(argThat(pred -> {
            var terms = Set.of(pred.toString().split(" && "));
            assertThat(terms).containsExactlyInAnyOrder("myEntity.id = " + objectId,
                    predicate.readPredicate().get().toString());
            return true;
        }));

        verify(predicate, atMostOnce()).collectionFilterPredicate();
        verify(predicate, atLeastOnce()).readPredicate();
        verifyNoMoreInteractions(predicate);
    }

    @Test
    void invokeSave_isNew_withoutId_predicateMatches() {

        var id = UUID.randomUUID();
        when(delegate.invokeSave(any(MyEntity.class))).then(invocation -> {
            var entity = invocation.getArgument(0, MyEntity.class);
            entity.setId(id);
            return entity;
        });

        // when the inserted entity matches the predicate
        when(executor.findOne(any(Predicate.class))).thenReturn(Optional.of(new MyEntity(id, "foo")));

        var foo = adapter.invokeSave(new MyEntity(null, "foo"));

        assertThat(foo).isNotNull();
        assertThat(foo.getId()).isEqualTo(id);

        verify(delegate, never()).invokeFindById(any(UUID.class));
        verify(delegate).invokeSave(any(MyEntity.class));
        verify(executor).findOne(any(Predicate.class));

        verify(transactionManager).commit(any(TransactionStatus.class));
        verify(transactionManager, never()).rollback(any(TransactionStatus.class));

        verify(predicate, atMostOnce()).collectionFilterPredicate();
        verify(predicate).afterCreatePredicate();
        verifyNoMoreInteractions(predicate);
    }

    @Test
    void invokeSave_isNew_withProvidedId_predicateMatches() {

        var id = UUID.randomUUID();
        when(delegate.invokeSave(any(MyEntity.class))).then(invocation -> invocation.getArgument(0, MyEntity.class));

        // inserted entity matches the predicate
        when(executor.findOne(any(Predicate.class))).thenReturn(Optional.of(new MyEntity(id, "foo")));

        var foo = adapter.invokeSave(new MyEntity(id, "foo"));

        assertThat(foo).isNotNull();
        assertThat(foo.getId()).isEqualTo(id);

        var inOrderVerifier = inOrder(delegate, executor, transactionManager);
        // check that the entity without predicate already exists -> it does not
        inOrderVerifier.verify(delegate).invokeFindById(any(UUID.class));
        // save the updated entity
        inOrderVerifier.verify(delegate).invokeSave(any(MyEntity.class));
        // verify that the updated entity is still accessible (with predicate) -> it is
        inOrderVerifier.verify(executor).findOne(any(Predicate.class));
        // expect a commit now
        inOrderVerifier.verify(transactionManager).commit(any(TransactionStatus.class));
        inOrderVerifier.verifyNoMoreInteractions();

        verify(predicate, atMostOnce()).collectionFilterPredicate();
        verify(predicate).afterCreatePredicate();
        verifyNoMoreInteractions(predicate);
    }

    @Test
    void invokeSave_isUpdate_predicateMatches() {

        var id = UUID.randomUUID();
        var existingEntity = new MyEntity(id, "foo");
        var saved = new AtomicBoolean(false);

        when(delegate.invokeFindById(any(UUID.class))).thenReturn(Optional.of(existingEntity));
        when(delegate.invokeSave(any(MyEntity.class))).then(invocation -> {
            saved.set(true);
            return invocation.getArgument(0, MyEntity.class);
        });

        var updatedEntity = new MyEntity(id, "bar");
        when(executor.findOne(any(Predicate.class)))
                .then(invocation -> Optional.of(saved.get() ? updatedEntity : existingEntity));

        var savedEntity = adapter.invokeSave(updatedEntity);
        assertThat(savedEntity).isNotNull();
        assertThat(savedEntity.getId()).isEqualTo(id);
        assertThat(savedEntity.getAttribute()).isEqualTo("bar");

        var inOrderVerifier = inOrder(delegate, executor, transactionManager);
        // check that the entity without predicate already exists -> it does
        inOrderVerifier.verify(delegate).invokeFindById(any(UUID.class));
        // check that the entity is accessible with predicate -> it is
        inOrderVerifier.verify(executor).findOne(any(Predicate.class));
        // save the updated entity
        inOrderVerifier.verify(delegate).invokeSave(any(MyEntity.class));
        // verify that the updated entity is still accessible (with predicate) -> it is
        inOrderVerifier.verify(executor).findOne(any(Predicate.class));
        // expect a commit now
        inOrderVerifier.verify(transactionManager).commit(any(TransactionStatus.class));
        inOrderVerifier.verifyNoMoreInteractions();

        verify(predicate, atMostOnce()).collectionFilterPredicate();
        verify(predicate).beforeUpdatePredicate();
        verify(predicate).afterUpdatePredicate();
        verifyNoMoreInteractions(predicate);
    }

    @Test
    void invokeSave_isUpdate_preSavePredicateMismatch_shouldThrow() {

        var id = UUID.randomUUID();
        var existingEntity = new MyEntity(id, "foo");

        // lookup by id without predicate -> returns existing
        when(delegate.invokeFindById(id)).thenReturn(Optional.of(existingEntity));
        // lookup by id with predicate -> returns empty
        when(executor.findOne(any(Predicate.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.invokeSave(new MyEntity(id, "denied")))
                .isInstanceOf(ResourceNotFoundException.class);

        var inOrderVerifier = inOrder(delegate, executor, transactionManager);
        // check that the entity without predicate already exists -> it does
        inOrderVerifier.verify(delegate).invokeFindById(any(UUID.class));
        // check that the entity is accessible with predicate -> it does NOT
        inOrderVerifier.verify(executor).findOne(any(Predicate.class));
        // expect a txn rollback
        inOrderVerifier.verify(transactionManager).rollback(any(TransactionStatus.class));
        inOrderVerifier.verifyNoMoreInteractions();

        verify(predicate, atMostOnce()).collectionFilterPredicate();
        verify(predicate).beforeUpdatePredicate();
        verify(predicate, atMostOnce()).afterUpdatePredicate();
        verifyNoMoreInteractions(predicate);
    }

    @Test
    void invokeSave_isUpdate_postSavePredicateMismatch_shouldThrow() {

        var id = UUID.randomUUID();
        var existingEntity = new MyEntity(id, "foo");
        var saved = new AtomicBoolean(false);

        when(delegate.invokeFindById(any(UUID.class))).thenReturn(Optional.of(existingEntity));
        when(delegate.invokeSave(any(MyEntity.class))).then(invocation -> {
            saved.set(true);
            return invocation.getArgument(0, MyEntity.class);
        });

        when(executor.findOne(any(Predicate.class)))
                .then(invocation -> saved.get() ? Optional.empty() : Optional.of(existingEntity));

        assertThatThrownBy(() -> adapter.invokeSave(new MyEntity(id, "denied")))
                .isInstanceOf(ResourceNotFoundException.class);

        var inOrderVerifier = inOrder(delegate, executor, transactionManager);
        // check that the entity without predicate already exists -> it does
        inOrderVerifier.verify(delegate).invokeFindById(any(UUID.class));
        // check that the entity is accessible with predicate -> it does NOT
        inOrderVerifier.verify(executor).findOne(any(Predicate.class));
        // expect a txn rollback
        inOrderVerifier.verify(transactionManager).rollback(any(TransactionStatus.class));
        inOrderVerifier.verifyNoMoreInteractions();

        verify(predicate, atMostOnce()).collectionFilterPredicate();
        verify(predicate).beforeUpdatePredicate();
        verify(predicate).afterUpdatePredicate();
        verifyNoMoreInteractions(predicate);
    }

    @Test
    void invokeDeleteById_predicateMatches() {
        var id = UUID.randomUUID();
        var existingEntity = new MyEntity(id, "foo");

        when(executor.findOne(any(Predicate.class))).thenReturn(Optional.of(existingEntity));
        Mockito.doNothing().when(delegate).invokeDeleteById(id);

        adapter.invokeDeleteById(id);

        verify(delegate).invokeDeleteById(id);

        verify(predicate, atMostOnce()).collectionFilterPredicate();
        verify(predicate).beforeDeletePredicate();
        verifyNoMoreInteractions(predicate);
    }

    @Test
    void invokeDeleteById_predicateMismatch() {
        var id = UUID.randomUUID();

        when(executor.findOne(any(Predicate.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.invokeDeleteById(id))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(delegate, never()).invokeDeleteById(id);

        verify(predicate, atMostOnce()).collectionFilterPredicate();
        verify(predicate).beforeDeletePredicate();
        verifyNoMoreInteractions(predicate);
    }
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class MyEntity {

    private UUID id;

    private String attribute;
}

