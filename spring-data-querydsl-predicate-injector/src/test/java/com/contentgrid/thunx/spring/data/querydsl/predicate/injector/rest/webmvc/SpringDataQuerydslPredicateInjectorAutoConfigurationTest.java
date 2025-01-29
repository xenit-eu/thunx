package com.contentgrid.thunx.spring.data.querydsl.predicate.injector.rest.webmvc;

import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver.QuerydslPredicateResolver;
import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver.CollectionFilteringOperationPredicates;
import com.querydsl.core.types.EntityPath;
import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
        "debug=true",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Transactional
@Testcontainers
class SpringDataQuerydslPredicateInjectorAutoConfigurationTest {

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EntityScan
    @EnableJpaRepositories(considerNestedRepositories = true)
    static class TestConfiguration {

        @Bean
        QuerydslPredicateResolver additionalPredicate() {
            return (methodParameter, domainType, parameters) -> {
                if(parameters.containsKey("test") && domainType == TestEntity.class) {
                    return Optional.of(new CollectionFilteringOperationPredicates(
                            QSpringDataQuerydslPredicateInjectorAutoConfigurationTest_TestEntity.testEntity
                                    .value
                                    .eq("entity2")
                    ));
                }
                return Optional.empty();
            };
        }

    }

    @Entity
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    static class TestEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @Basic
        private String value;
    }

    @RepositoryRestResource
    interface TestEntityRepository extends CrudRepository<TestEntity, UUID>, QuerydslPredicateExecutor<TestEntity>,
            QuerydslBinderCustomizer<EntityPath<?>> {

        @Override
        default void customize(QuerydslBindings bindings, EntityPath<?> root) {

        }
    }

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer();

    @Autowired
    TestEntityRepository testEntityRepository;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        var entity1 = new TestEntity(
                null,
                "entity1"
        );

        var entity2 = new TestEntity(
                null,
                "entity2"
        );

        testEntityRepository.saveAll(List.of(entity1, entity2));
    }

    @Test
    void searchEntities() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/testEntities"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.testEntities.length()").value(2));

        // This tests the default querydsl filter
        mockMvc.perform(MockMvcRequestBuilders.get("/testEntities?value=entity1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.testEntities.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.testEntities[0].value").value("entity1"));

        // This tests our custom QuerydslPredicateResolver
        mockMvc.perform(MockMvcRequestBuilders.get("/testEntities?test=1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.testEntities.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.testEntities[0].value").value("entity2"));
    }


}