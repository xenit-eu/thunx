package com.contentgrid.thunx.spring.gateway.filter;

import com.contentgrid.thunx.encoding.ThunkExpressionEncoder;
import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.contentgrid.thunx.spring.security.ReactivePolicyAuthorizationManager;
import com.contentgrid.thunx.encoding.json.ExpressionJsonConverter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

/**
 * A Spring Cloud {@link GatewayFilterFactory} that adds the ABAC expression to the http request headers
 * for the proxied service.
 *
 * The {@code AbacGatewayFilterFactory} is responsible to:
 * - retrieve the ABAC expression from the {@link ServerWebExchange} attributes
 * - if present, serialize the expression using an {@link ThunkExpressionEncoder}
 * - add this to the HTTP request header
 *
 */
@Component
public class AbacGatewayFilterFactory extends AbstractGatewayFilterFactory<AbacGatewayFilterFactory.Config> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbacGatewayFilterFactory.class);

    public static final String ABAC_CONTEXT_HEADER = "X-ABAC-Context";

    private final ThunkExpressionEncoder encoder;

    public AbacGatewayFilterFactory() {
        this(expression -> new ExpressionJsonConverter().encode(expression).getBytes(StandardCharsets.UTF_8));
    }

    public AbacGatewayFilterFactory(ThunkExpressionEncoder encoder) {
        super(Config.class);

        this.encoder = Objects.requireNonNull(encoder, "Argument 'encoder' is required");
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new OrderedGatewayFilter((exchange, chain) -> {

            addAbacContextHeader(exchange);

            return chain.filter(exchange);
        }, 1);
    }

    void addAbacContextHeader(ServerWebExchange exchange) {
        ThunkExpression<Boolean> thunkExpression = exchange.getAttribute(ReactivePolicyAuthorizationManager.ABAC_POLICY_PREDICATE_ATTR);
        if (thunkExpression != null) {
            var data = this.encoder.encode(thunkExpression);
            var encoded = Base64.getEncoder().encodeToString(data);
            exchange.getRequest()
                    .mutate()
                    .header(ABAC_CONTEXT_HEADER, encoded);
        }
    }

    public static class Config {

    }

}
