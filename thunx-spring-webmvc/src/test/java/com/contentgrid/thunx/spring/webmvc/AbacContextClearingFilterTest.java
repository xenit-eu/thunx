package com.contentgrid.thunx.spring.webmvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.contentgrid.thunx.predicates.model.Comparison;
import com.contentgrid.thunx.predicates.model.Scalar;
import com.contentgrid.thunx.spring.security.AbacContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AbacContextClearingFilterTest {

    private final AbacContextClearingFilter filter = new AbacContextClearingFilter();
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void clearAbacContext() {
        AbacContext.clear();
    }

    private static void setAbacContext() {
        AbacContext.setCurrentAbacContext(Comparison.areEqual(Scalar.of(true), Scalar.of(true)));
    }

    @Test
    void clearsContextAfterChainCompletesNormally() throws Exception {
        setAbacContext();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(AbacContext.getCurrentAbacContext()).isNull();
    }

    @Test
    void clearsContextWhenChainThrowsCheckedException() throws Exception {
        setAbacContext();
        doThrow(new ServletException("boom")).when(filterChain).doFilter(request, response);

        assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(ServletException.class);

        assertThat(AbacContext.getCurrentAbacContext()).isNull();
    }

    @Test
    void clearsContextWhenChainThrowsUncheckedException() throws Exception {
        setAbacContext();
        doThrow(new IllegalStateException("boom")).when(filterChain).doFilter(request, response);

        assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(IllegalStateException.class);

        assertThat(AbacContext.getCurrentAbacContext()).isNull();
    }

    @Test
    void delegatesToChainWithSameRequestAndResponse() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(AbacContext.getCurrentAbacContext()).isNull();
    }
}
