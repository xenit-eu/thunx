package com.contentgrid.thunx.predicates.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor
public class SymbolicReference implements ThunkExpression<Object> {

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

        return String.format("%s.%s",
                subject, path.stream().map(Object::toString).collect(Collectors.joining(".")));
    }

    @Override
    public String toString() {
        if (path.isEmpty()) {
            return subject.toPath();
        }

        return String.format("%s.%s", subject,
                path.stream().map(Object::toString).collect(Collectors.joining(".")));
    }

    public <R, C> R accept(ThunkExpressionVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    public static SymbolicReference of(String variable, PathElement... path) {
        return of(Variable.named(variable), path);
    }

    public static SymbolicReference of(@NonNull Variable variable, PathElement... path) {
        return new SymbolicReference(new SymbolicRefSubject(variable), Arrays.asList(path));
    }

    public static SymbolicReference of(@NonNull String varName, Consumer<PathBuilder> pathCallback) {
        var pathBuilder = new PathBuilder();
        pathCallback.accept(pathBuilder);

        return new SymbolicReference(new SymbolicRefSubject(Variable.named(varName)), pathBuilder.getPath());
    }

    /**
     * Parse the reference and split by '.' while assuming all path-elements are string-types
     */
    public static SymbolicReference parse(@NonNull String reference) {
        var parts = reference.split("\\.");
        var subject = parts[0];

        return of(Variable.named(subject), Arrays.stream(parts).skip(1).map(part -> path(part)));
    }

    public static SymbolicReference of(@NonNull Variable variable, @NonNull Stream<PathElement> path) {
        return SymbolicReference.of(variable, path.collect(Collectors.toList()));
    }

    public static SymbolicReference of(@NonNull Variable variable, @NonNull List<PathElement> path) {
        return new SymbolicReference(new SymbolicRefSubject(variable), path);
    }

    public static PathElement path(String path) {
        return new StringPathElement(path);
    }

    public static PathElement pathVar(String variable) {
        return new VariablePathElement(variable);
    }

    public static PathElement path(ThunkExpression<?> expr) {
        // figure out what type of 'path' this is and convert that to a PathElement
        // for now only String & Var are supported

        if (expr instanceof StringValue) {
            return path(((StringValue) expr).getValue());
        } else if (expr instanceof Variable) {
            return pathVar(((Variable) expr).getName());
        }

        throw new IllegalArgumentException(String.format("Expression '%s' not supported as path element", expr));
    }

    public static class PathBuilder {

        private List<PathElement> path = new ArrayList<>();

        public PathBuilder string(String path) {
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

    public interface PathElement {

        <T> T accept(ContextFreeThunkExpressionVisitor<T> visitor);
    }

    public abstract static class PathElementVisitor<T> extends ContextFreeThunkExpressionVisitor<T> {

        @Override
        public final T visit(FunctionExpression<?> functionExpression) {
            return null;
        }

        @Override
        public final T visit(SymbolicReference symbolicReference) {
            return null;
        }

        @Override
        protected final T visit(CollectionValue collectionValue) {
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

        public String toString() {
            return this.path.getValue();
        }

        @Override
        public <T> T accept(ContextFreeThunkExpressionVisitor<T> visitor) {
            return visitor.visit(this.getPath());
        }
    }

    @Data
    public static class VariablePathElement implements PathElement {

        private Variable variable;

        VariablePathElement(String varName) {
            this.variable = Variable.named(varName);
        }

        public String toString() {
            return "$" + this.variable.getName();
        }

        @Override
        public <T> T accept(ContextFreeThunkExpressionVisitor<T> visitor) {
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

        public String toString() {
            return this.variable.getName();
        }
    }
}