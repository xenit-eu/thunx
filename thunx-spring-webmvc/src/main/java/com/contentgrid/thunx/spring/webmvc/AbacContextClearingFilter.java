package com.contentgrid.thunx.spring.webmvc;

import com.contentgrid.thunx.spring.security.AbacContext;
import com.contentgrid.thunx.spring.security.AbacRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Clears the {@link AbacContext} thread-local after every request handled under the {@code opa} ABAC source.
 * <p>
 * Paired with {@link PolicyAuthorizationManager}: that class sets the current {@code AbacContext} predicate
 * from within Spring Security's authorization step, which is not itself a servlet {@link jakarta.servlet.Filter}
 * and therefore has no natural place to clean up afterwards. This filter wraps the rest of the chain and clears
 * the context in a {@code finally} block instead.
 * <p>
 * This is unrelated to {@link AbacRequestFilter}, which backs the {@code header} ABAC source (it reads the
 * context from an {@code X-ABAC-Context} request header and clears it itself). The two never run for the same
 * request: which one is active depends on {@code contentgrid.thunx.abac.source}.
 * <p>
 * If this filter is left out, the predicate set by {@code PolicyAuthorizationManager} is never cleared. Since
 * {@code AbacContext} is backed by an {@link InheritableThreadLocal}, that stale value can leak into the next
 * request served by the same (container-pooled) worker thread, or into any thread spawned from it, granting an
 * unrelated request the wrong authorization predicate.
 */
public class AbacContextClearingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            AbacContext.clear();
        }
    }
}
