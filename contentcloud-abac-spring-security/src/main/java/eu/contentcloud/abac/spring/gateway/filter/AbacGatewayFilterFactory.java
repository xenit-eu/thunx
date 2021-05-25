package eu.contentcloud.abac.spring.gateway.filter;

import eu.contentcloud.abac.spring.PolicyAuthorizationManager;
import eu.contentcloud.abac.predicates.model.Expression;
import eu.contentcloud.security.pbac.predicates.converters.json.ExpressionJsonConverter;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class AbacGatewayFilterFactory extends AbstractGatewayFilterFactory<AbacGatewayFilterFactory.Config> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbacGatewayFilterFactory.class);

    public static final String ABAC_CONTEXT_HEADER = "X-ABAC-Context";

    public AbacGatewayFilterFactory() {
        super(Config.class);
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
        Expression<Boolean> abacExpression = exchange.getAttribute(PolicyAuthorizationManager.ABAC_POLICY_PREDICATE_ATTR);
        if (abacExpression != null) {
            var json = new ExpressionJsonConverter().toJson(abacExpression);
            exchange.getRequest()
                    .mutate()
                    .header(ABAC_CONTEXT_HEADER, json);
        }
    }

    public static class Config {

    }
}
