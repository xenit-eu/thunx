package com.contentgrid.thunx.spring.data.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
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

    Predicate predicate = new PathBuilder<>(MyEntity.class, "myEntity")
        .get("attribute", String.class)
        .eq("foo");

    AbacRepositoryInvokerAdapter adapter;

    @BeforeEach
    void setUp() {
        this.adapter = new AbacRepositoryInvokerAdapter(delegate, executor, predicate, transactionManager,
                MyEntity.class, UUID.class, "id", (entity) -> ((MyEntity) entity).getId());
    }

    @Test
    void invokeFindById_redirectedToExecutor() {

        UUID objectId = UUID.randomUUID();
        adapter.invokeFindById(objectId);

        verify(delegate, never()).invokeFindById(objectId);
        verify(executor).findOne(argThat(pred -> {
            var terms = Set.of(pred.toString().split(" && "));
            assertThat(terms)
                    .hasSize(2)
                    .containsExactlyInAnyOrder("myEntity.id = " + objectId, predicate.toString());
            return true;
        }));

    }

    @Test
    void invokeSave_predicateMatches() {

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

        verify(delegate, times(1)).invokeSave(any(MyEntity.class));
        verify(executor, times(1)).findOne(argThat(pred -> {
            var terms = Set.of(pred.toString().split(" && "));
            assertThat(terms).containsExactlyInAnyOrder("myEntity.id = " + id, predicate.toString());
            return true;
        }));

        verify(transactionManager).commit(any(TransactionStatus.class));
        verify(transactionManager, never()).rollback(any(TransactionStatus.class));
    }

    @Test
    void invokeSave_predicateMismatch_shouldThrow() {

        var id = UUID.randomUUID();
        when(delegate.invokeSave(any(MyEntity.class))).then(invocation -> {
            var entity = invocation.getArgument(0, MyEntity.class);
            entity.setId(id);
            return entity;
        });

        // when the inserted entity does NOT match the predicate
        when(executor.findOne(any(Predicate.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.invokeSave(new MyEntity(null, "denied")))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(delegate).invokeSave(any(MyEntity.class));
        verify(executor).findOne(argThat(pred -> {
            var terms = Set.of(pred.toString().split(" && "));
            assertThat(terms).containsExactlyInAnyOrder("myEntity.id = " + id, predicate.toString());
            return true;
        }));

        verify(transactionManager).rollback(any(TransactionStatus.class));
        verify(transactionManager, never()).commit(any(TransactionStatus.class));
    }
}

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class MyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String attribute;
}

