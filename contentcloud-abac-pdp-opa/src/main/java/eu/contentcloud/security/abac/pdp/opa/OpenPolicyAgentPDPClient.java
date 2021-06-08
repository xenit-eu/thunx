package eu.contentcloud.security.abac.pdp.opa;

import eu.contentcloud.security.abac.pdp.PolicyDecision;
import eu.contentcloud.security.abac.pdp.PolicyDecisionPointClient;
import eu.contentcloud.security.abac.pdp.RequestContext;
import eu.contentcloud.opa.client.OpaClient;
import eu.contentcloud.opa.client.api.CompileApi.PartialEvaluationRequest;
import eu.contentcloud.security.abac.pdp.PolicyDecisions;
import java.util.Objects;
import reactor.core.publisher.Mono;

public class OpenPolicyAgentPDPClient implements PolicyDecisionPointClient {

    private final OpaClient opaClient;

    public OpenPolicyAgentPDPClient(OpaClient opaClient) {
        Objects.requireNonNull(opaClient, "opaClient is required");
        this.opaClient = opaClient;
    }

    @Override
    public <TPrincipal> Mono<PolicyDecision> conditional(TPrincipal principal, RequestContext requestContext) {
        // TODO query & input parameters
        return Mono.fromCompletionStage(() -> opaClient.compile(new PartialEvaluationRequest(null, null, null)))
                .map(response -> {
                    // list of possible partially evaluated queries from OPA
                    // we need to convert this to a single boolean expression
                    var opaQuerySet = response.getResult().getQueries();
                    return opaQuerySet;
                })
                .map(opaQuerySet -> {
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


}
