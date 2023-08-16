package com.contentgrid.thunx.predicates.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.contentgrid.thunx.predicates.model.Comparison;
import com.contentgrid.thunx.predicates.model.LogicalOperation;
import com.contentgrid.thunx.predicates.model.Scalar;
import com.contentgrid.thunx.predicates.model.SymbolicReference;
import com.querydsl.core.types.dsl.PathBuilder;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class QueryDslConverterTest {

    private final QueryDslConverter converter = new QueryDslConverter(new FieldByReflectionAccessStrategy(), domainType -> new PathBuilder<>(domainType, domainType.getSimpleName().toLowerCase(
            Locale.ROOT)));

    static class Document {

        int security;
        boolean confidential;

        @Embedded
        Content content;

        Department department;
    }

    static class Department {
        String id;

        @OneToOne
        Person manager;
    }

    @Embeddable
    static class Content {
        UUID id;
        String filename;
    }

    @Entity
    static class Person {
        String name;
    }

    @Nested
    class Operators {
        @Test
        void is_equal() {
            // document.security == 5
            var thunkExpression = Comparison.areEqual(
                    SymbolicReference.of("entity", path -> path.string("security")),
                    Scalar.of(5)
            );

            var predicate = converter.from(thunkExpression, Document.class);

            assertThat(predicate)
                    .isNotNull()
                    .hasToString("document.security = 5");
        }

        @Test
        void is_not_equal() {
            // document.security != 5
            var thunkExpression = Comparison.notEqual(
                    SymbolicReference.of("entity", path -> path.string("security")),
                    Scalar.of(5)
            );

            var predicate = converter.from(thunkExpression, Document.class);

            assertThat(predicate)
                    .isNotNull()
                    .hasToString("document.security != 5");
        }

        @Test
        void is_greater_than() {
            // document.security > 5
            var thunkExpression = Comparison.greater(
                    SymbolicReference.of("entity", path -> path.string("security")),
                    Scalar.of(5)
            );

            var predicate = converter.from(thunkExpression, Document.class);

            assertThat(predicate)
                    .isNotNull()
                    .hasToString("document.security > 5");
        }

        @Test
        void is_greater_or_equals() {
            // document.security >= 5
            var thunkExpression = Comparison.greaterOrEquals(
                    SymbolicReference.of("entity", path -> path.string("security")),
                    Scalar.of(5)
            );

            var predicate = converter.from(thunkExpression, Document.class);

            assertThat(predicate)
                    .isNotNull()
                    .hasToString("document.security >= 5");
        }

        @Test
        void is_less_than() {
            // document.security < 5
            var thunkExpression = Comparison.less(
                    SymbolicReference.of("entity", path -> path.string("security")),
                    Scalar.of(5)
            );

            var predicate = converter.from(thunkExpression, Document.class);

            assertThat(predicate)
                    .isNotNull()
                    .hasToString("document.security < 5");
        }

        @Test
        void is_less_or_equals() {
            // document.security <= 5
            var thunkExpression = Comparison.lessOrEquals(
                    SymbolicReference.of("entity", path -> path.string("security")),
                    Scalar.of(5)
            );

            var predicate = converter.from(thunkExpression, Document.class);

            assertThat(predicate)
                    .isNotNull()
                    .hasToString("document.security <= 5");
        }

        @Test
        void disjunction() {
            // document.security == 5 OR document.security == 10 OR document.security == 8
            var thunkExpression = LogicalOperation.disjunction(
                    Comparison.areEqual(
                            SymbolicReference.of("entity", path -> path.string("security")),
                            Scalar.of(5)),
                    Comparison.areEqual(
                            SymbolicReference.of("entity", path -> path.string("security")),
                            Scalar.of(10)),
                    Comparison.areEqual(
                            SymbolicReference.of("entity", path -> path.string("security")),
                            Scalar.of(8))
            );

            var predicate = converter.from(thunkExpression, Document.class);

            assertThat(predicate)
                    .isNotNull()
                    .hasToString("document.security = 5 || document.security = 10 || document.security = 8");

        }

        @Test
        void conjunction() {
            // document.security == 5 AND document.department.id == 'HR'
            var thunkExpression = LogicalOperation.conjunction(
                    Comparison.areEqual(
                            SymbolicReference.of("entity", path -> path.string("security")),
                            Scalar.of(5)),
                    Comparison.areEqual(
                            SymbolicReference.of("entity", path -> path.string("department").string("id")),
                            Scalar.of("HR"))
            );

            var predicate = converter.from(thunkExpression, Document.class);

            assertThat(predicate)
                    .isNotNull()
                    .hasToString("document.security = 5 && document.department.id = HR");

        }

        @Test
        void negation() {
            // not(entity.external)
            var thunkExpression = LogicalOperation.uncheckedNegation(List.of(
                    SymbolicReference.of("entity", path -> path.string("confidential"))
            ));

            var predicate = converter.from(thunkExpression, Document.class);

            assertThat(predicate)
                    .isNotNull()
                    .hasToString("!document.confidential");
        }

    }


    @Nested
    class Symbolic {

        @Test
        void compare_attribute_with_null() {
            // document.security == null
            var thunkExpression = Comparison.areEqual(
                    SymbolicReference.of("entity", path -> path.string("security")),
                    Scalar.nullValue()
            );

            var predicate = converter.from(thunkExpression, Document.class);

            assertThat(predicate)
                    .isNotNull()
                    .hasToString("document.security = null");
        }

        @Test
        void compare_relation_shouldThrow() {
            // This is NOT supported, because this expression requires an explicit join
            // document.department.manager != null

            var thunkExpression = Comparison.notEqual(
                    SymbolicReference.of("entity", path -> path.string("department").string("manager")),
                    Scalar.nullValue()
            );

            assertThatThrownBy(() -> converter.from(thunkExpression, Document.class))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cannot use `entity.department.manager` as an expression, because it refers to a relation,"
                            + " not an attribute.");
        }

        @Test
        void unknownField_shouldThrow_illegalArgumentException() {
            var thunkExpression = Comparison.areEqual(
                    SymbolicReference.of("entity", path -> path.string("unknown")),
                    Scalar.of(5)
            );

            assertThatThrownBy(() -> converter.from(thunkExpression, Document.class))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Unknown property 'unknown' on Document, while traversing entity.unknown");
        }

        @Test
        void unknownNestedField_shouldThrow_illegalArgumentException() {
            var thunkExpression = Comparison.areEqual(
                    SymbolicReference.of("entity", path -> path.string("department").string("unknown")),
                    Scalar.of(5)
            );

            assertThatThrownBy(() -> converter.from(thunkExpression, Document.class))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Unknown property 'unknown' on Department, while traversing entity.department.unknown");
        }

        @Test
        void expectSubjectNotNamed_entity_throwsIllegalArgumentException() {
            var thunkExpression = Comparison.areEqual(
                    SymbolicReference.of("document", path -> path.string("security")),
                    Scalar.of(5)
            );


            assertThatThrownBy(() -> converter.from(thunkExpression, Document.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}