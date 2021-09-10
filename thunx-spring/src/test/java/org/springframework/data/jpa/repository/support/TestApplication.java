package org.springframework.data.jpa.repository.support;

import com.querydsl.core.types.Predicate;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.function.LongSupplier;

import static org.mockito.Mockito.spy;

@SpringBootApplication
public class TestApplication {

    @Profile("count-query-provider")
    @Bean
    public CustomCountQueryProvider countQueryProvider(EntityManager em) {
        return spy(new CustomCountQueryProvider() {

            private EntityManager em;

            @Override
            public void setEntityManager(EntityManager em) {
                this.em = em;
            }

            @Override
            public Query createNativeCountQuery(Predicate predicate, Class<?> javaType) {
                return em.createNativeQuery("select count(1) from abac_querydsl_jpa_predicate_executor_test$tentity");
            }

            @Override
            public LongSupplier longSupplier(Query countQuery) {
                return new LongSupplier() {
                    @Override
                    public long getAsLong() {
                        return Long.parseLong(countQuery.getSingleResult().toString());
                    }
                };
            }
        });
    }
}
