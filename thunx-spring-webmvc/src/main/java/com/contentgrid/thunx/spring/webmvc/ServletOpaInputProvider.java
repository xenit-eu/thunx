package com.contentgrid.thunx.spring.webmvc;

import com.contentgrid.thunx.pdp.opa.OpaInputProvider;
import com.contentgrid.thunx.spring.security.AuthenticationContextMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.security.core.Authentication;

/**
 * Default OpaInputProvider for servlet applications, mirroring {@code DefaultOpaInputProvider}
 * (thunx-spring-gateway) field-for-field. Applications whose Rego policy expects a different input
 * shape must supply their own {@code OpaInputProvider<Authentication, HttpServletRequest>} bean.
 */
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

    // request.getQueryString() is used instead of request.getParameterMap(), which would consume
    // the body for application/x-www-form-urlencoded requests.
    static Map<String, List<String>> queryParams(String queryString) {
        if (queryString == null || queryString.isBlank()) {
            return Map.of();
        }

        var result = new LinkedHashMap<String, List<String>>();
        for (String pair : queryString.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            var separatorIndex = pair.indexOf('=');
            var key = separatorIndex >= 0 ? pair.substring(0, separatorIndex) : pair;
            var value = separatorIndex >= 0 ? pair.substring(separatorIndex + 1) : "";
            key = URLDecoder.decode(key, StandardCharsets.UTF_8);
            value = URLDecoder.decode(value, StandardCharsets.UTF_8);
            result.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return result;
    }

    @Override
    public Map<String, Object> createInput(Authentication authentication, HttpServletRequest request) {
        var authContext = AuthenticationContextMapper.fromAuthentication(authentication);
        return Map.of(
                "path", uriToPathArray(URI.create(request.getRequestURI())),
                "method", request.getMethod(),
                "queryParams", queryParams(request.getQueryString()),
                "auth", authContext,
                "user", authContext.getUser() // temp for backwards compat with existing policies
        );
    }
}
