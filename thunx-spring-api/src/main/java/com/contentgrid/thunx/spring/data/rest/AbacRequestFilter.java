package com.contentgrid.thunx.spring.data.rest;

import com.contentgrid.thunx.encoding.ThunkExpressionDecoder;
import com.contentgrid.thunx.predicates.model.ThunkExpression;
import java.io.IOException;
import java.util.Base64;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AbacRequestFilter implements Filter {

    private final ThunkExpressionDecoder thunkDecoder;

    public AbacRequestFilter(ThunkExpressionDecoder thunkDecoder) {
        this.thunkDecoder = thunkDecoder;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException,
            ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String abacContext = request.getHeader("X-ABAC-Context");
        if (abacContext != null) {
            byte[] abacContextBytes = Base64.getDecoder().decode(abacContext);
            // which (version of?) decoder should we use ? -> get that info from JWT or other header ?
            ThunkExpression<Boolean> abacExpression = this.thunkDecoder.decode(abacContextBytes);
            log.debug("ABAC Context: {}", abacExpression);
            AbacContext.setCurrentAbacContext(abacExpression);
        } else {
            throw new IllegalArgumentException("No X-ABAC-Context context present.");
        }

        filterChain.doFilter(servletRequest, servletResponse);

        AbacContext.clear();
    }
}
