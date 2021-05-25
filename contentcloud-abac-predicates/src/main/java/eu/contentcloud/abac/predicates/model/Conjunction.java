package eu.contentcloud.abac.predicates.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

public class Conjunction implements BooleanExpression {

    @Getter
    private final Collection<Expression<Boolean>> predicates;

    public Conjunction(Collection<Expression<Boolean>> predicates) {
        this.predicates = predicates;
    }

    public static Conjunction of(Stream<Expression<Boolean>> predicates) {
        return new Conjunction(predicates.collect(Collectors.toList()));
    }

    public static Conjunction of(Expression<Boolean>... predicates) {
        return new Conjunction(List.of(predicates));
    }

    @Override
    public boolean canBeResolved() {
        return this.predicates.stream().allMatch(Expression::canBeResolved);
    }

    @Override
    public Operator getOperator() {
        return Operator.AND;
    }

    @Override
    public Collection<Expression<?>> getTerms() {
        return new ArrayList<>(this.getPredicates());
    }
}
