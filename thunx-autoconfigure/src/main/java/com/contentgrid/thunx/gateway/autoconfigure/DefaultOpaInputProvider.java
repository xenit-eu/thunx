package com.contentgrid.thunx.gateway.autoconfigure;

import com.contentgrid.thunx.pdp.AuthenticationContext;
import com.contentgrid.thunx.pdp.RequestContext;
import com.contentgrid.thunx.pdp.opa.OpaInputProvider;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

class DefaultOpaInputProvider implements OpaInputProvider<AuthenticationContext, RequestContext> {

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
    public Map<String, Object> createInput(AuthenticationContext authContext, RequestContext requestContext) {
        return Map.of(
                "path", uriToPathArray(requestContext.getURI()),
                "method", requestContext.getHttpMethod(),
                "queryParams", requestContext.getQueryParams(),
                "auth", authContext,
                "user", authContext.getUser() // temp for backwards compat with existing policies
        );
    }
}
