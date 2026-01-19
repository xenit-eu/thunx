package com.contentgrid.thunx.spring.data.context;

import com.contentgrid.thunx.encoding.ThunkExpressionDecoder;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @deprecated Use {@link com.contentgrid.thunx.spring.security.AbacRequestFilter} instead
 */
@Slf4j
@Deprecated(since = "0.14.1")
@RequiredArgsConstructor
public class AbacRequestFilter implements Filter {

    private final com.contentgrid.thunx.spring.security.AbacRequestFilter delegate;

    public AbacRequestFilter(ThunkExpressionDecoder thunkDecoder) {
        this(new com.contentgrid.thunx.spring.security.AbacRequestFilter(thunkDecoder));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        delegate.doFilter(request, response, chain);
    }
}
