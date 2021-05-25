package eu.contentcloud.abac.predicates.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SymbolicReference implements Expression<Object> {

    private final SymbolicRefSubject subject;

    public Variable getSubject() {
        return this.subject.getVariable();
    }

    @Getter
    private final List<PathElement> path;

    public String toPath() {
        if (path.isEmpty()) {
            return subject.toPath();
        }

        return String.format("%s.%s", subject, String.join("", "[" + path.toString() + "]"));
    }

    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }


    public static SymbolicReference of(Variable variable, PathElement ... path) {
        Objects.requireNonNull(variable, "variable cannot be null");

        return new SymbolicReference(new SymbolicRefSubject(variable), Arrays.asList(path));
    }

    public static SymbolicReference of(Variable variable, Stream<PathElement> path) {
        Objects.requireNonNull(variable, "variable cannot be null");

        return new SymbolicReference(new SymbolicRefSubject(variable), path.collect(Collectors.toList()));
    }

    public static PathElement path(String path) {
        return new StringPathElement(path);
    }

    public static PathElement var(String variable) {
        return new VariablePathElement(variable);
    }

    @Override
    public Class<?> getResultType() {
        return Object.class;
    }

    @Override
    public boolean canBeResolved() {
        return false;
    }

    public interface PathElement {

        <T> T accept(ExpressionVisitor<T> visitor);
    }

    @Data
    public static class StringPathElement implements PathElement {

        private final StringValue path;

        StringPathElement(String path) {
            this(new StringValue(path));
        }

        StringPathElement(StringValue path) {
            this.path = path;
        }

        @Override
        public <T> T accept(ExpressionVisitor<T> visitor) {
            return visitor.visit(this.getPath());
        }
    }

    @Data
    public static class VariablePathElement implements PathElement {
        private Variable variable;
        VariablePathElement(String varName) {
            this.variable = Variable.named(varName);
        }

        @Override
        public <T> T accept(ExpressionVisitor<T> visitor) {
            return visitor.visit(this.getVariable());
        }
    }

    // This would be a union type, for all possible subjects of the symbolic-reference
    @Data
    private static class SymbolicRefSubject {
        private final Variable variable;

        public String toPath() {
            return variable.getName();
        }
    }
}