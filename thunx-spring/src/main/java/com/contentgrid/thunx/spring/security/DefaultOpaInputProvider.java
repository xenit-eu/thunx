package com.contentgrid.thunx.spring.security;

import com.contentgrid.thunx.pdp.opa.OpaInputProvider;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;

public class DefaultOpaInputProvider implements OpaInputProvider<Authentication, ServerHttpRequest> {

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
    public Map<String, Object> createInput(Authentication authentication, ServerHttpRequest requestContext) {
        var authContext = AuthenticationContextMapper.fromAuthentication(authentication);
        return Map.of(
                "path", uriToPathArray(requestContext.getURI()),
                "method", requestContext.getMethodValue(),
                "queryParams", requestContext.getQueryParams(),
                "auth", authContext,
                "user", authContext.getUser() // temp for backwards compat with existing policies
        );
    }
}
