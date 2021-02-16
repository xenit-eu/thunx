package eu.xenit.contentcloud.security.pbac.pdp.spring.gateway.filter;

import eu.xenit.contentcloud.security.pbac.pdp.spring.PolicyAuthorizationManager;
import eu.xenit.contentcloud.security.pbac.predicates.converters.json.ExpressionJsonConverter;
import eu.xenit.contentcloud.security.pbac.predicates.model.Expression;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class PbacGatewayFilterFactory extends AbstractGatewayFilterFactory<PbacGatewayFilterFactory.Config> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PbacGatewayFilterFactory.class);

    public static final String ABAC_QUERY = "abacQuery";
    public static final String ABAC_UNKNOWNS = "abacUnknowns";

    public PbacGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(ABAC_QUERY, ABAC_UNKNOWNS);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new OrderedGatewayFilter((exchange, chain) -> {
            LOGGER.info("--- Hello from ABAC Gateway Filter: ---");
            LOGGER.info(config.getAbacQuery());
            LOGGER.info(config.getAbacUnknowns().toString());

//            List<String> authorization = exchange.getRequest().getHeaders().get("Authorization");
//            if (authorization != null && authorization.size()>0) {
//                String bearer = authorization.get(0);
//                if (bearer.startsWith("Bearer")) {
//                    String[] bearerArray = bearer.split("\\s+");
//                    String abacContextEncoded = null;
//                    try {
//                        abacContextEncoded = opaClient.queryOPA(config.getAbacQuery(), new OpaInput(bearerArray[1]), config.getAbacUnknowns());
//                    } catch (IOException e) {
//                        LOGGER.error(e.getMessage(), e);
//                    }
//
//                    if (abacContextEncoded != null) {
//                        exchange.getRequest()
//                                .mutate()
//                                .header("X-ABAC-Context", abacContextEncoded);
//                    }
//                }
//            }

            Expression<Boolean> expr = exchange.getAttribute(PolicyAuthorizationManager.ABAC_POLICY_PREDICATE_ATTR);

            // serialize this
            var json = new ExpressionJsonConverter().toJson(expr);

            return chain.filter(exchange);
        }, 1);
    }



    public static class Config {
        private String abacQuery;
        private List<String> abacUnknowns;

        public Config() {
        }

        public Config(String abacQuery, List<String> abacUnknowns) {
            this.abacQuery = abacQuery;
            this.abacUnknowns = abacUnknowns;
        }

        public String getAbacQuery() {
            return abacQuery;
        }

        public void setAbacQuery(String abacQuery) {
            this.abacQuery = abacQuery;
        }

        public List<String> getAbacUnknowns() {
            return abacUnknowns;
        }

        public void setAbacUnknowns(List<String> abacUnknowns) {
            this.abacUnknowns = abacUnknowns;
        }
    }
}
