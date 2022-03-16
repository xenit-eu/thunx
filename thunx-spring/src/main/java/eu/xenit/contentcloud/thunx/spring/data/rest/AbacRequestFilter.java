package eu.xenit.contentcloud.thunx.spring.data.rest;

import eu.xenit.contentcloud.thunx.encoding.ThunkExpressionDecoder;
import eu.xenit.contentcloud.thunx.predicates.model.ThunkExpression;
import eu.xenit.contentcloud.thunx.spring.data.context.AbacContext;
import java.io.IOException;
import java.util.Base64;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
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
            ThunkExpression<Boolean> abacExpression = this.thunkDecoder.decoder(abacContextBytes);
            log.debug("ABAC expression for this context is {}", abacExpression);
            AbacContext.setCurrentAbacContext(abacExpression);
        }

        filterChain.doFilter(servletRequest, servletResponse);

        if(abacContext != null) {
            AbacContext.clear();
        }
    }
}
