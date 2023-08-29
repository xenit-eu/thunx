package com.contentgrid.thunx.spring.gateway.filter;


import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

import com.contentgrid.thunx.predicates.model.Comparison;
import com.contentgrid.thunx.predicates.model.Scalar;
import com.contentgrid.thunx.predicates.model.Variable;
import com.contentgrid.thunx.spring.security.ReactivePolicyAuthorizationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

class AbacGatewayFilterFactoryTest {

    private AbacGatewayFilterFactory filterFactory;

    @BeforeEach
    void setUp() {
        this.filterFactory = new AbacGatewayFilterFactory();
    }

    @Test
    void addAbacContextHeader_whenPolicyPredicateIsPresent() {
        var request = MockServerHttpRequest.get("/documents");
        var exchange = MockServerWebExchange.from(request);

        var expression = Comparison.areEqual(Scalar.of(5), Variable.named("document.attribute"));
        exchange.getAttributes().put(ReactivePolicyAuthorizationManager.ABAC_POLICY_PREDICATE_ATTR, expression);

        this.filterFactory.addAbacContextHeader(exchange);

        assertThat(exchange.getRequest().getHeaders())

                // get the request header named 'X-ABAC-Context'
                .hasEntrySatisfying("X-ABAC-Context", abacContextHeader -> {
                    assertThat(abacContextHeader)
                            .singleElement()
                            .asInstanceOf(STRING)
                            .isBase64()
                            .decodedAsBase64().asString()
                            .satisfies(json -> {
                                // can we parse this as json ?
                                assertThatJson(json).isObject()
                                        .containsEntry("type", "function")
                                        .containsEntry("operator", "eq")
                                        .hasEntrySatisfying("terms", terms -> assertThatJson(terms).isArray());
                            });
                });
    }
}