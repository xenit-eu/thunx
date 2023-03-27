package com.contentgrid.thunx.spring.security;

import com.contentgrid.thunx.pdp.RequestContext;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ServerWebExchange;

@RequiredArgsConstructor
public class ServerWebExchangeRequestContext implements RequestContext {

    @NonNull
    private final ServerWebExchange serverWebExchange;

    @Override
    public String getHttpMethod() {
        return serverWebExchange.getRequest().getMethodValue();
    }

    @Override
    public URI getURI() {
        return serverWebExchange.getRequest().getURI();
    }

    @Override
    public Map<String, List<String>> getQueryParams() {
        return serverWebExchange.getRequest().getQueryParams();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.copyOf(serverWebExchange.getAttributes());
    }
}
