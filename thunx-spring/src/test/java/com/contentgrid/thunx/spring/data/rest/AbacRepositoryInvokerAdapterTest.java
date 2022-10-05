package com.contentgrid.thunx.spring.data.rest;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AbacRepositoryInvokerAdapterTest {

    static class DomainObjectUtilsTest {

        @Test
        void long_jpaAnnotated_idField() {
            var idField = AbacRepositoryInvokerAdapter.DomainObjectUtils.getIdField(JpaEntity.class);
            assertThat(idField).isNotNull();
            assertThat(idField.getName()).isEqualTo("myId");
        }

        @Test
        @Disabled("@Id on getter method not supported (ported as-is from spring-content)")
        void long_jpaAnnotated_idGetter() {
            var idField = AbacRepositoryInvokerAdapter.DomainObjectUtils.getIdField(AccessorJpaEntity.class);
            assertThat(idField).isNotNull();
            assertThat(idField.getName()).isEqualTo("myId");
        }

        @Test
        void jpa_customIdType() {
            var idField = AbacRepositoryInvokerAdapter.DomainObjectUtils.getIdField(JpaEntityWithValueObjectId.class);
            assertThat(idField).isNotNull();
            assertThat(idField.getType()).isEqualTo(CustomIdClass.class);
        }

        @Test
        void long_springAnnotated_idField() {
            var idField = AbacRepositoryInvokerAdapter.DomainObjectUtils.getIdField(SpringDataEntity.class);
            assertThat(idField).isNotNull();
            assertThat(idField.getName()).isEqualTo("otherId");
        }

        @Test
        void id_from_subclass() {
            var idField = AbacRepositoryInvokerAdapter.DomainObjectUtils.getIdField(JpaSubClass.class);
            assertThat(idField).isNotNull();
            assertThat(idField.getName()).isEqualTo("myId");
        }


        static class JpaEntity {
            @javax.persistence.Id
            private Long myId;
        }

        static class JpaSubClass extends JpaEntity {

        }

        static class CustomIdClass {
            private String value;
        }

        static class JpaEntityWithValueObjectId {
            @javax.persistence.Id
            private CustomIdClass id;
        }

        static class AccessorJpaEntity {

            private Long myId;

            @javax.persistence.Id
            public Long getMyId() {
                return this.myId;
            }

        }

        static class SpringDataEntity {
            @org.springframework.data.annotation.Id
            private Long otherId;
        }

    }

}