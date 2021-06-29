package eu.contentcloud.abac.predicates.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class LogicalOperation implements BooleanOperation {

    private final Operator operator;
    private List<Expression<Boolean>> terms;

    private LogicalOperation(Operator operator, Stream<Expression<Boolean>> terms) {
        this.operator = operator;
        this.terms = terms.collect(Collectors.toUnmodifiableList());
    }

    public static LogicalOperation disjunction(Stream<Expression<Boolean>> terms) {
        return new LogicalOperation(Operator.OR, terms);
    }

    public static LogicalOperation disjunction(List<Expression<Boolean>> terms) {
        return disjunction(terms.stream());
    }

    public static LogicalOperation uncheckedDisjunction(List<Expression<?>> terms) {
        return disjunction(terms.stream().map(expr -> (Expression<Boolean>) expr));
    }
    

    public static LogicalOperation disjunction(Expression<Boolean> ... terms) {
        return disjunction(Arrays.stream(terms));
    }

    public static LogicalOperation conjunction(Stream<Expression<Boolean>> terms) {
        return new LogicalOperation(Operator.AND, terms);
    }

    public static LogicalOperation conjunction(Expression<Boolean> ... terms) {
        return conjunction(Arrays.stream(terms));
    }

    public static LogicalOperation conjunction(List<Expression<Boolean>> terms) {
        return conjunction(terms.stream());
    }

    public static LogicalOperation uncheckedConjunction(List<Expression<?>> terms) {
        return conjunction(terms.stream().map(expr -> (Expression<Boolean>) expr));
    }

    public static LogicalOperation negation(Expression<Boolean> term) {
        return new LogicalOperation(Operator.NOT, Stream.of(term));
    }

    @Override
    public boolean canBeResolved() {
        return this.terms.stream().allMatch(t -> t.canBeResolved());
    }

    @Override
    public Boolean resolve() {
        return this.operator.eval(this.terms.stream().map(term -> term.resolve()));
    }
            
    @Override
    public Operator getOperator() {
        return this.operator;
    }

    @Override
    public List<Expression<?>> getTerms() {
        return Collections.unmodifiableList(this.terms);
    }
}
