package com.contentgrid.thunx.pdp.opa;

import com.contentgrid.opa.client.OpaClient;
import com.contentgrid.opa.client.api.CompileApi.PartialEvaluationRequest;
import com.contentgrid.thunx.pdp.AuthenticationContext;
import com.contentgrid.thunx.pdp.PolicyDecision;
import com.contentgrid.thunx.pdp.PolicyDecisionPointClient;
import com.contentgrid.thunx.pdp.PolicyDecisions;
import com.contentgrid.thunx.pdp.RequestContext;
import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.contentgrid.thunx.visitor.reducer.ThunkReducerVisitor;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenPolicyAgentPDPClient implements PolicyDecisionPointClient {

    private final OpaClient opaClient;
    private final OpaQueryProvider queryProvider;

    public OpenPolicyAgentPDPClient(OpaClient opaClient, OpaQueryProvider queryProvider) {
        Objects.requireNonNull(opaClient, "opaClient is required");
        Objects.requireNonNull(queryProvider, "queryProvider is required");

        this.opaClient = opaClient;
        this.queryProvider = queryProvider;
    }

    @Override
    public CompletableFuture<PolicyDecision> conditional(
            AuthenticationContext authContext, RequestContext requestContext) {
        // TODO how can we define 'unknowns' in a generic way ?
        // WARNING: do NOT list 'input' as unknown, or it will ignore the whole 'input' object itself
        // WARNING: do NOT list 'data' as unknown, or it will ignore the policy that is loaded in OPA itself
        var request = new PartialEvaluationRequest(
                this.queryProvider.createQuery(requestContext),
                createInput(authContext, requestContext),
                List.of("input.entity"));

        return opaClient.compile(request)
                .thenApply(response ->
                {
                    log.trace("Partial policy evaluation request:\n{}\nResponse:\n{}", request, response);
                    // list of possible partially evaluated queries from OPA
                    // we need to convert this to a single boolean expression
                    var opaQuerySet = response.getResult().getQueries();
                    var converter = new QuerySetToThunkExpressionConverter();
                    return converter.convert(opaQuerySet);
                })
                .thenApply((ThunkExpression<Boolean> thunkExpression) -> {
                    var reducedExpression = thunkExpression
                            .accept(ThunkReducerVisitor.DEFAULT_INSTANCE, null)
                            .assertResultType(Boolean.class);
                    log.trace("Thunx expression:\n{}\nReduced to:\n{}", thunkExpression, reducedExpression);
                    return ThunkExpression.maybeValue(reducedExpression)
                            // if the expression can be resolved right now, there is no remaining predicate
                            .map(result -> result? PolicyDecisions.allowed(): PolicyDecisions.denied())
                            // there is a remaining predicate
                            .orElse(PolicyDecisions.conditional(reducedExpression));
                });
    }

    static Map<String, Object> createInput(AuthenticationContext authContext, RequestContext requestContext) {
        return Map.of(
                "path", uriToPathArray(requestContext.getURI()),
                "method", requestContext.getHttpMethod(),
                "queryParams", requestContext.getQueryParams(),
                "auth", authContext,
                "user", authContext.getUser() // temp for backwards compat with existing policies
        );
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
