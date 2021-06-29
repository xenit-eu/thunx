package eu.contentcloud.security.abac.pdp.opa;

import eu.contentcloud.security.abac.pdp.PolicyDecision;
import eu.contentcloud.security.abac.pdp.PolicyDecisionPointClient;
import eu.contentcloud.security.abac.pdp.RequestContext;
import eu.contentcloud.opa.client.OpaClient;
import eu.contentcloud.opa.client.api.CompileApi.PartialEvaluationRequest;
import eu.contentcloud.security.abac.pdp.PolicyDecisions;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import reactor.core.publisher.Mono;

public class OpenPolicyAgentPDPClient implements PolicyDecisionPointClient {

    private final OpaClient opaClient;
    private OpaQueryProvider queryProvider;

    public OpenPolicyAgentPDPClient(OpaClient opaClient, OpaQueryProvider queryProvider) {
        Objects.requireNonNull(opaClient, "opaClient is required");
        Objects.requireNonNull(queryProvider, "queryProvider is required");

        this.opaClient = opaClient;
        this.queryProvider = queryProvider;
    }

    @Override
    public <TPrincipal> Mono<PolicyDecision> conditional(TPrincipal principal, RequestContext requestContext) {
        // TODO how can we define 'unknowns' in a generic way ?
        // WARNING: do NOT list 'input' as unknown, or it will ignore the whole 'input' object itself
        // WARNING: do NOT list 'data' as unknown, or it will ignore the policy that is loaded in OPA itself
        return Mono.fromCompletionStage(() -> {
            var request = new PartialEvaluationRequest(this.queryProvider.createQuery(requestContext),
                    createInput(principal, requestContext), List.of());
            return opaClient.compile(request);
        })
                .map(response -> {
                    // list of possible partially evaluated queries from OPA
                    // we need to convert this to a single boolean expression
                    var opaQuerySet = response.getResult().getQueries();
                    var converter = new QuerySetToThunkExpressionConverter();
                    return converter.convert(opaQuerySet);
                })
                .map(thunkExpression -> {
                    // if the expression can be resolved right now, there is no remaining predicate
                    if (thunkExpression.canBeResolved()) {
                        return thunkExpression.resolve() ? PolicyDecisions.allowed() : PolicyDecisions.denied();
                    } else {
                        // there is a remaining predicate
                        return PolicyDecisions.conditional(thunkExpression);
                    }
                });
    }

    static <TPrincipal> Map<String, Object> createInput(TPrincipal principal, RequestContext requestContext) {
        // TODO map principal
        return Map.of(
                "path", uriToPathArray(requestContext.getURI()),
                "method", requestContext.getHttpMethod(),
                "user", Map.of(
                        "admin", false,
                        "brokers", List.of(),
                        "insurers", List.of()
                ),
                "principal", principal);
    }

    static String[] uriToPathArray(URI uri) {
        Objects.requireNonNull(uri, "Argument 'uri' is required");
        uri = uri.normalize();

        var path = uri.getPath();
        if (path == null) {
            return new String[0];
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (path.length() == 0) {
            return new String[0];
        }

        return path.split("/");
    }


}
