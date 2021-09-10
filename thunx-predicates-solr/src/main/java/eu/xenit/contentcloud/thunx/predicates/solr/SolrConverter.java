package eu.xenit.contentcloud.thunx.predicates.solr;

import eu.xenit.contentcloud.thunx.predicates.model.*;

class SolrConverter implements ThunkExpressionVisitor<String> {

    @Override
    public String visit(Scalar<?> scalar) {
        return scalar.getValue().toString();
    }

    @Override
    public String visit(FunctionExpression<?> function) {

        // convert all the terms
        var terms = function.getTerms().stream()
                .map(term -> term.accept(this))
                .toArray(String[]::new);

        switch (function.getOperator()) {
            // case of boolean expressions
            case EQUALS:
                if (terms.length != 2) {
                    throw new IllegalArgumentException(String.format("Expecting function expression with 2 terms. Got %s terms", terms.length));
                }

                return String.format("%s:%s", terms[0], terms[1]);
            case OR:
                StringBuilder builder = new StringBuilder();
                for (int i=0; i < terms.length; i++) {
                    if (i > 0) {
                        builder.append(" OR ");
                    }
                    builder.append(terms[i]);
                }
                return builder.toString();
            default:
                throw new UnsupportedOperationException(
                        "Operation '" + function.getOperator() + "' not implemented");
        }
    }

    @Override
    public String visit(SymbolicReference symbolicReference) {

        String subject = symbolicReference.getSubject().getName();
        if (!"entity".equalsIgnoreCase(subject)) {
            throw new IllegalArgumentException("Expected symbolic-ref subject named 'entity', but got '"+subject+"'");
        }

        StringBuilder builder = new StringBuilder();
        var path = symbolicReference.getPath();
        for (int i=0; i < path.size(); i++) {

            var elem = path.get(i);
            var result = elem.accept(new SymbolicReference.PathElementVisitor<String>() {
                @Override
                public String visit(Scalar<?> scalar) {
                    if (scalar.getResultType().equals(String.class)) {
                        return (String) scalar.getValue();
                    } else {
                        throw new UnsupportedOperationException(
                                "cannot traverse symbolic reference using scalar of type " + scalar.getResultType()
                                        .getSimpleName());
                    }
                }

                @Override
                public String visit(Variable variable) {
                    return variable.getName();
                }
            });

            if (i > 0) {
                builder.append("_");
            }
            builder = builder.append(result);
        }

        return builder.toString();
    }

    @Override
    public String visit(Variable variable) {
        // TODO could there be more variables available, than just the subject-path-builder ?
        throw new UnsupportedOperationException("converting variable to querydsl is not yet implemented");
    }
}
