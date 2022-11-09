package com.contentgrid.thunx.spring.data.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.transaction.PlatformTransactionManager;

@ExtendWith(MockitoExtension.class)
class AbacRepositoryInvokerAdapterTest {

    @Mock
    RepositoryInvoker delegate;
    @Mock
    QuerydslPredicateExecutor<Object> executor;
    @Mock
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
    void forwardsFindAllToExecutorWithPredicate() {

        var sort = Sort.by("firstname");
        adapter.invokeFindAll(sort);

        verify(executor, times(1)).findAll(predicate, sort);
        verify(delegate, times(0)).invokeFindAll(sort);
    }

    @Test
    void invokeFindById_redirectedToExecutor() {

        UUID objectId = UUID.randomUUID();
        adapter.invokeFindById(objectId);

        verify(delegate, times(0)).invokeFindById(objectId);
        verify(executor, times(1)).findOne(argThat(pred -> {
            var terms = Set.of(pred.toString().split(" && "));
            assertThat(terms)
                    .hasSize(2)
                    .containsExactlyInAnyOrder("myEntity.id = " + objectId, "myEntity.attribute = foo");
            return true;
        }));

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

