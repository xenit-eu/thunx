package org.springframework.data.jpa.repository.support;

import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.query.internal.NativeQueryImpl;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.persistence.*;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class PostgresQueryPlanEstimateCountQueryProviderTest {

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer<>("postgres");

    @Autowired
    private EntityManager entityManager;

    @Test
    public void generatesCountEstimateQuery() throws Exception {
        PostgresQueryPlanEstimateCountQueryProvider provider = new PostgresQueryPlanEstimateCountQueryProvider();
        provider.setEntityManager(entityManager);

        Predicate predicate = Expressions.predicate(Ops.EQ, Expressions.stringPath("name"), Expressions.constant("roger"));
        Query actual = provider.createNativeCountQuery(predicate, TEntity.class);

        assertThat(((NativeQueryImpl) actual).getQueryString()).isEqualTo("SELECT count_estimate('SELECT 1 FROM postgres_query_plan_estimate_count_query_provider_test$tentity postgresqu0_ WHERE postgresqu0_.name=roger');");
    }

    @Getter
    @Setter
    @RequiredArgsConstructor()
    @Entity
    public class TEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        @NonNull
        private String name;
    }
}

