package com.contentgrid.thunx.spring.webmvc;

import com.contentgrid.thunx.pdp.opa.OpaInputProvider;
import com.contentgrid.thunx.spring.security.AuthenticationContextMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import org.springframework.security.core.Authentication;

public class ServletOpaInputProvider implements OpaInputProvider<Authentication, HttpServletRequest> {

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

        if (path.isEmpty()) {
            return new String[0];
        }

        return path.split("/");
    }

    @Override
    public Map<String, Object> createInput(Authentication authentication, HttpServletRequest request) {
        var authContext = AuthenticationContextMapper.fromAuthentication(authentication);
        return Map.of(
                "path", uriToPathArray(URI.create(request.getRequestURI())),
                "method", request.getMethod(),
                "auth", authContext,
                "user", authContext.getUser()
        );
    }
}
