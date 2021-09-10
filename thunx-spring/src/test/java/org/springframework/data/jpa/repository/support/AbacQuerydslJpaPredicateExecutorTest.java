package org.springframework.data.jpa.repository.support;

import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import lombok.*;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.persistence.*;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@EnableJpaRepositories(considerNestedRepositories = true, repositoryFactoryBeanClass = AbacJpaRepositoryFactoryBean.class)
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class AbacQuerydslJpaPredicateExecutorTest {

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer<>("postgres");

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TEntityRepository repo;

    @Autowired
    private CustomCountQueryProvider countQueryProvider;

    @Test
    public void findAllUsesCustomCountQueryProvider() throws Exception {

        entityManager.merge(new TEntity("test"));

        Predicate predicate = Expressions.predicate(Ops.EQ, Expressions.stringPath("name"), Expressions.constant("roger"));
        Pageable pageable = PageRequest.of(1, 10);

        repo.findAll(predicate, pageable);

        verify(countQueryProvider, times(1));
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @RequiredArgsConstructor
    @Entity
    public static class TEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        @NonNull
        private String name;
    }

    interface TEntityRepository extends JpaRepository<TEntity, Long>, QuerydslPredicateExecutor<TEntity> {}
}
