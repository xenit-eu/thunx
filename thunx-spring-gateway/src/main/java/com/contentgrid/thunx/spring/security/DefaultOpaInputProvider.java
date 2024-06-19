package com.contentgrid.thunx.spring.security;

import com.contentgrid.thunx.pdp.opa.OpaInputProvider;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;

public class DefaultOpaInputProvider implements OpaInputProvider<Authentication, ServerWebExchange> {

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

    @Override
    public Map<String, Object> createInput(Authentication authentication, ServerWebExchange webExchange) {
        var authContext = AuthenticationContextMapper.fromAuthentication(authentication);
        var requestContext = webExchange.getRequest();
        return Map.of(
                "path", uriToPathArray(requestContext.getURI()),
                "method", requestContext.getMethod().name(),
                "queryParams", requestContext.getQueryParams(),
                "auth", authContext,
                "user", authContext.getUser() // temp for backwards compat with existing policies
        );
    }
}
