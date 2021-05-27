package eu.contentcloud.abac.predicates.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
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

    public static SymbolicReference of(String varName, Consumer<PathBuilder> pathCallback) {
        Objects.requireNonNull(varName, "variable cannot be null");

        var pathBuilder = new PathBuilder();
        pathCallback.accept(pathBuilder);

        return new SymbolicReference(new SymbolicRefSubject(Variable.named(varName)),  pathBuilder.getPath());
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

    public static class PathBuilder {

        private List<PathElement> path = new ArrayList<>();

        public PathBuilder path(String path) {
            this.path.add(new StringPathElement(path));
            return this;
        }

        public PathBuilder var(String variable) {
            this.path.add(new VariablePathElement(variable));
            return this;
        }

        public List<PathElement> getPath() {
            return this.path;
        }
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

    public abstract static class PathElementVisitor<T> implements ExpressionVisitor<T> {
        @Override
        public final T visit(FunctionExpression<?> functionExpression) {
            return null;
        }

        @Override
        public final T visit(SymbolicReference symbolicReference) {
            return null;
        }
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