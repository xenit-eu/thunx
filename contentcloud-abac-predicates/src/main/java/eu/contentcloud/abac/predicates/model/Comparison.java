package eu.contentcloud.abac.predicates.model;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class Comparison implements BooleanOperation {

    @Getter
    @NonNull
    private final Operator operator;

    @Getter
    @NonNull
    private final Expression<?> leftTerm;

    @Getter
    @NonNull
    private final Expression<?> rightTerm;


    @Override
    public List<Expression<?>> getTerms() {
        return List.of(this.leftTerm, this.rightTerm);
    }

    public static Comparison areEqual(List<Expression<?>> terms) {
        if (terms.size() != 2) {
            throw new IllegalArgumentException("Expected 2 terms, but got " + terms.size());
        }

        return areEqual(terms.get(0), terms.get(1));
    }

    public static Comparison areEqual(Expression<?> left, Expression<?> right) {
        return new Comparison(Operator.EQUALS, left, right);
    }

    public static Comparison greaterOrEquals(Expression<?> left, Expression<?> right) {
        return new Comparison(Operator.GREATER_THAN_OR_EQUAL_TO, left, right);
    }

    public static Comparison greaterOrEquals(List<Expression<?>> terms) {
        if (terms.size() != 2) {
            throw new IllegalArgumentException("Expected 2 terms, but got " + terms.size());
        }
        return greaterOrEquals(terms.get(0), terms.get(1));
    }

    public static <E> Comparison lessOrEquals(Expression<?> left, Expression<?> right) {
        return new Comparison(Operator.LESS_THEN_OR_EQUAL_TO, left, right);
    }

    public static Comparison lessOrEquals(@NonNull List<Expression<?>> terms) {
        assertTermSizeIsTwo(terms);
        return lessOrEquals(terms.get(0), terms.get(1));
    }


    @Override
    public boolean canBeResolved() {
        return this.leftTerm.canBeResolved() && this.rightTerm.canBeResolved();
    }

    private static void assertTermSizeIsTwo(List<Expression<?>> terms) {
        if (terms.size() == 2) {
            return;
        }
        throw new IllegalArgumentException("Expected 2 terms, but got " + terms.size());
    }

    @Override
    public String toString() {
        return this.toDebugString();
    }

}
