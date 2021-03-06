package eu.xenit.contentcloud.thunx.encoding.json;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import eu.xenit.contentcloud.thunx.predicates.model.ThunkExpression;
import java.util.Map;
import lombok.Data;

@Data
public class UnknownTypeExpressionDto implements JsonExpressionDto {

    private String type;

    @JsonAnySetter
    private Map<String, Object> fields;

    @Override
    public <T> ThunkExpression<T> toExpression() {
        return null;
    }
}
