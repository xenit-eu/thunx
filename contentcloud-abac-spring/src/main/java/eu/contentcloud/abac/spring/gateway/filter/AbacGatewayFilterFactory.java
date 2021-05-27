package eu.contentcloud.abac.spring.gateway.filter;

import eu.contentcloud.abac.spring.authorization.ReactivePolicyAuthorizationManager;
import eu.contentcloud.abac.predicates.model.Expression;
import eu.contentcloud.security.abac.predicates.converters.json.ExpressionJsonConverter;
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
 * - if present, serialize the expression using an {@link AbacExpressionEncoder}
 * - add this to the HTTP request header
 *
 */
@Component
public class AbacGatewayFilterFactory extends AbstractGatewayFilterFactory<AbacGatewayFilterFactory.Config> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbacGatewayFilterFactory.class);

    public static final String ABAC_CONTEXT_HEADER = "X-ABAC-Context";

    private final AbacExpressionEncoder encoder;

    public AbacGatewayFilterFactory() {
        this(expression -> new ExpressionJsonConverter().encode(expression).getBytes(StandardCharsets.UTF_8));
    }

    public AbacGatewayFilterFactory(AbacExpressionEncoder encoder) {
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
        Expression<Boolean> abacExpression = exchange.getAttribute(ReactivePolicyAuthorizationManager.ABAC_POLICY_PREDICATE_ATTR);
        if (abacExpression != null) {
            var data = this.encoder.encode(abacExpression);
            var encoded = Base64.getEncoder().encodeToString(data);
            exchange.getRequest()
                    .mutate()
                    .header(ABAC_CONTEXT_HEADER, encoded);
        }
    }

    public static class Config {

    }

    @FunctionalInterface
    interface AbacExpressionEncoder {
        byte[] encode(Expression<Boolean> expression);
    }

    @FunctionalInterface
    interface AbacExpressionDecoder {
        Expression<Boolean> decoder(byte[] data);
    }
}
