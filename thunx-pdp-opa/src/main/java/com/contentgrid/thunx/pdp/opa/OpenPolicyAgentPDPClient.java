package com.contentgrid.thunx.pdp.opa;

import com.contentgrid.opa.client.OpaClient;
import com.contentgrid.opa.client.api.CompileApi.PartialEvaluationRequest;
import com.contentgrid.thunx.pdp.PolicyDecision;
import com.contentgrid.thunx.pdp.PolicyDecisionPointClient;
import com.contentgrid.thunx.pdp.PolicyDecisions;
import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.contentgrid.thunx.visitor.reducer.ThunkReducerVisitor;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OpenPolicyAgentPDPClient<A, R> implements PolicyDecisionPointClient<A, R> {

    @NonNull
    private final OpaClient opaClient;
    @NonNull
    private final OpaQueryProvider<R> queryProvider;
    @NonNull
    private final OpaInputProvider<A, R> inputProvider;

    @Override
    public CompletableFuture<PolicyDecision> conditional(
            A authContext, R requestContext) {
        // TODO how can we define 'unknowns' in a generic way ?
        // WARNING: do NOT list 'input' as unknown, or it will ignore the whole 'input' object itself
        // WARNING: do NOT list 'data' as unknown, or it will ignore the policy that is loaded in OPA itself
        var request = new PartialEvaluationRequest(
                this.queryProvider.createQuery(requestContext),
                this.inputProvider.createInput(authContext, requestContext),
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

}
