package eu.contentcloud.abac.predicates.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

public class Disjunction implements BooleanExpression {

    @Getter
    private final Collection<Expression<Boolean>> predicates;

    public Disjunction(Collection<Expression<Boolean>> predicates) {
        this.predicates = predicates;
    }

    public static Disjunction of(Stream<Expression<Boolean>> predicates) {
        return new Disjunction(predicates.collect(Collectors.toList()));
    }

    public static Disjunction of(Expression<Boolean>... predicates) {
        return new Disjunction(List.of(predicates));
    }


    @Override
    public boolean canBeResolved() {
        return this.predicates.stream().allMatch(Expression::canBeResolved);
    }

    @Override
    public Operator getOperator() {
        return Operator.OR;
    }

    @Override
    public List<Expression<?>> getTerms() {
        return new ArrayList<>(this.getPredicates());
    }
}
