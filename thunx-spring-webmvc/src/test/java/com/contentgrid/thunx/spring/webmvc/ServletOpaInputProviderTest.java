package com.contentgrid.thunx.spring.webmvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.ClaimAccessor;

class ServletOpaInputProviderTest {

    private final ServletOpaInputProvider provider = new ServletOpaInputProvider();

    static Stream<Arguments> uriToPathArray() {
        return Stream.of(
                Arguments.of("Empty url", "", new String[0]),
                Arguments.of("root without trailing slash", "https://localhost:8080", new String[0]),
                Arguments.of("root with trailing slash", "https://localhost:8080/", new String[0]),
                Arguments.of("simple path", "https://localhost:8080/api/foo", new String[]{"api", "foo"}),
                Arguments.of("path with duplicate slashes gets normalized", "https://localhost:8080//api//bar",
                        new String[]{"api", "bar"})
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void uriToPathArray(String description, String uri, String[] expected) {
        assertThat(ServletOpaInputProvider.uriToPathArray(URI.create(uri))).isEqualTo(expected);
    }

    static Stream<Arguments> queryParams() {
        return Stream.of(
                Arguments.of("null query string", null, Map.of()),
                Arguments.of("empty query string", "", Map.of()),
                Arguments.of("blank query string", "  ", Map.of()),
                Arguments.of("single value", "foo=bar", Map.of("foo", List.of("bar"))),
                Arguments.of("multiple values for same key", "foo=bar&foo=baz",
                        Map.of("foo", List.of("bar", "baz"))),
                Arguments.of("multiple keys", "foo=bar&hello=world",
                        Map.of("foo", List.of("bar"), "hello", List.of("world"))),
                Arguments.of("flag without value", "foo", Map.of("foo", List.of(""))),
                Arguments.of("explicit empty value", "foo=", Map.of("foo", List.of(""))),
                Arguments.of("url-decodes keys and values", "hello+world=foo%20bar",
                        Map.of("hello world", List.of("foo bar"))),
                Arguments.of("ignores empty pairs", "foo=bar&&hello=world",
                        Map.of("foo", List.of("bar"), "hello", List.of("world")))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void queryParams(String description, String queryString, Map<String, List<String>> expected) {
        assertThat(ServletOpaInputProvider.queryParams(queryString)).isEqualTo(expected);
    }

    @Test
    void createInput_populatesExpectedFields() {
        var request = new MockHttpServletRequest("GET", "/api/foo");
        request.setQueryString("hello=world");

        var jwtToken = (ClaimAccessor) () -> Map.of(
                "sub", "04c2cbec-faad-4dc8-ba6f-edb3d5b902e9",
                "preferred_username", "alice"
        );
        var authentication = new TestingAuthenticationToken(jwtToken, null, AuthorityUtils.NO_AUTHORITIES);

        var input = provider.createInput(authentication, request);

        assertThat(input).containsEntry("path", new String[]{"api", "foo"})
                .containsEntry("method", "GET")
                .containsEntry("queryParams", Map.of("hello", List.of("world")))
                .containsKey("auth")
                .containsEntry("user", Map.of(
                "sub", "04c2cbec-faad-4dc8-ba6f-edb3d5b902e9",
                "preferred_username", "alice"
        ));
    }

    // Regression guard for the queryParams() javadoc claim: calling any parameter-reading method
    // on a real servlet container consumes the body of application/x-www-form-urlencoded requests.
    // createInput() must derive queryParams solely from getQueryString(), never from those methods.
    @Test
    void createInput_neverReadsParametersOrBody() throws Exception {
        var request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/foo");
        when(request.getMethod()).thenReturn("POST");
        when(request.getQueryString()).thenReturn("hello=world");

        var jwtToken = (ClaimAccessor) () -> Map.of("sub", "04c2cbec-faad-4dc8-ba6f-edb3d5b902e9");
        var authentication = new TestingAuthenticationToken(jwtToken, null, AuthorityUtils.NO_AUTHORITIES);

        provider.createInput(authentication, request);

        verify(request, never()).getParameterMap();
        verify(request, never()).getParameter(any());
        verify(request, never()).getParameterNames();
        verify(request, never()).getParameterValues(any());
        verify(request, never()).getInputStream();
        verify(request, never()).getReader();
    }
}
