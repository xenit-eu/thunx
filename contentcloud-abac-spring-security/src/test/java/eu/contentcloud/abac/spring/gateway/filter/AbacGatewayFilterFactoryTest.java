package eu.contentcloud.abac.spring.gateway.filter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
    void test() {
        var request = MockServerHttpRequest.get("/documents");
        var exchange = MockServerWebExchange.from(request);


        this.filterFactory.addAbacContextHeader(exchange);
    }
}