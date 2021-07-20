package eu.xenit.contentcloud.thunx.encoding.json;

import eu.xenit.contentcloud.thunx.predicates.model.ThunkExpression;
import eu.xenit.contentcloud.thunx.predicates.model.SymbolicReference;
import eu.xenit.contentcloud.thunx.predicates.model.SymbolicReference.PathElement;
import eu.xenit.contentcloud.thunx.predicates.model.Variable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
class JsonSymbolicReferenceDto implements JsonExpressionDto {

    private final String type = "ref";
    private JsonExpressionDto subject;
    private List<JsonExpressionDto> path;

    public JsonSymbolicReferenceDto(JsonExpressionDto subject, List<JsonExpressionDto> path) {
        super();
        this.subject = subject;
        this.path = path;
    }

    public static JsonSymbolicReferenceDto of(String var, String... path) {
        return new JsonSymbolicReferenceDto(
                JsonVariableDto.named(var),
                Arrays.stream(path).map(p -> JsonScalarDto.of(p)).collect(Collectors.toList()));
    }

    @Override
    public ThunkExpression<?> toExpression() throws InvalidExpressionDataException {

        // convert and validate the subject
        var subj = subjectToExpression(this.subject);

        // convert and validate to path-elements
        var pathElements = pathToExpression(this.path);
        return SymbolicReference.of(subj, pathElements);
    }

    private static Stream<PathElement> pathToExpression(List<JsonExpressionDto> path) {
        return path.stream().map(p -> SymbolicReference.path(p.toExpression()));
    }

    private static Variable subjectToExpression(JsonExpressionDto subject) throws InvalidExpressionDataException {
        var subjectVar = subject.toExpression();
        if (!(subjectVar instanceof Variable)) {
            String message = String.format("Symbolic reference '%s' subject type not supported", subjectVar);
            throw new InvalidExpressionDataException(message);
        }

        return (Variable) subjectVar;
    }
}
